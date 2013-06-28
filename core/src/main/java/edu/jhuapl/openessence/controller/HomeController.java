/*
 * Copyright (c) 2013 The Johns Hopkins University/Applied Physics Laboratory
 *                             All rights reserved.
 *
 * This material may be used, modified, or reproduced by or for the U.S.
 * Government pursuant to the rights granted under the clauses at
 * DFARS 252.227-7013/7014 or FAR 52.227-14.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * NO WARRANTY.   THIS MATERIAL IS PROVIDED "AS IS."  JHU/APL DISCLAIMS ALL
 * WARRANTIES IN THE MATERIAL, WHETHER EXPRESS OR IMPLIED, INCLUDING (BUT NOT
 * LIMITED TO) ANY AND ALL IMPLIED WARRANTIES OF PERFORMANCE,
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT OF
 * INTELLECTUAL PROPERTY RIGHTS. ANY USER OF THE MATERIAL ASSUMES THE ENTIRE
 * RISK AND LIABILITY FOR USING THE MATERIAL.  IN NO EVENT SHALL JHU/APL BE
 * LIABLE TO ANY USER OF THE MATERIAL FOR ANY ACTUAL, INDIRECT,
 * CONSEQUENTIAL, SPECIAL OR OTHER DAMAGES ARISING FROM THE USE OF, OR
 * INABILITY TO USE, THE MATERIAL, INCLUDING, BUT NOT LIMITED TO, ANY DAMAGES
 * FOR LOST PROFITS.
 */

package edu.jhuapl.openessence.controller;

import edu.jhuapl.openessence.config.EnvironmentConfig;
import edu.jhuapl.openessence.datasource.jdbc.JdbcOeDataSource;
import edu.jhuapl.openessence.i18n.InspectableResourceBundleMessageSource;
import edu.jhuapl.openessence.model.MenuItem;
import edu.jhuapl.openessence.web.util.ControllerUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;
import javax.inject.Inject;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Resource
    private InspectableResourceBundleMessageSource messageSource;

    @Resource
    private ConcurrentMap<String, JdbcOeDataSource> dataSources;

    @Inject
    private EnvironmentConfig envConfig;

    /**
     * Most of our requests are hard-coded to depend on the location looking like ../../FOO, so we have to redirect to a
     * URL that looks like that, instead of just forwarding.
     */
    @RequestMapping({"", "/home"})
    public String root() {
        return "redirect:home/main";
    }

    @RequestMapping({"/home/", "/home/main"})
    public ModelAndView main() {
        ModelAndView mav = new ModelAndView("main");
        mav.addObject("mainLayoutResources", envConfig.mainLayoutResources());
        mav.addObject("mainResources", envConfig.mainResources());
        return mav;
    }

    @RequestMapping("/home/getNavigationMenu")
    public
    @ResponseBody
    HashMap<String, MenuItem> getNavigationMenu() {
        HashMap<String, MenuItem> parents = new HashMap<String, MenuItem>();
        // Build base/parent menu items, config index used for order

        String menu = messageSource.getMessage("menu");
        String[] menuItems = menu.split(",");
        for (int index = 0; index < menuItems.length; index++) {
            String item = menuItems[index];
            String trimmedItem = item.trim();
            if (trimmedItem != null && !trimmedItem.isEmpty()) {
                Map<String, Object> config = new HashMap<String, Object>();
                config.put("order", index);
                parents.put(trimmedItem, new MenuItem(trimmedItem, config));
            } else {
                log.warn("Invalid menu item: " + item);
            }
        }

        for (JdbcOeDataSource ds : dataSources.values()) {
            processDataSourceMenuConfiguration(ds, parents);
        }

        return parents;
    }

    private void processDataSourceMenuConfiguration(JdbcOeDataSource datasource, HashMap<String, MenuItem> parents) {
        String dsId = datasource.getClass().getName();
        log.debug("Processing data source: " + dsId);

        if (!ControllerUtils.isUserAuthorized(SecurityContextHolder.getContext().getAuthentication(), datasource)) {
            // don't bother adding menu items user isn't authorized to use
            return;
        }

        Map<String, Object> meta = datasource.getMetaData();
        if (meta != null) {
            Object menuCfg = meta.get("menuCfg");
            if (menuCfg != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> mc = (List<Map<String, Object>>) menuCfg;

                for (Map<String, Object> item : mc) {
                    MenuItem parent = parents.get(item.get("parent"));
                    if (parent != null) {
                        parent.getChildren().add(new MenuItem(dsId, item));
                    } else {
                        log.warn("Menu definition does not contain parent for data source: " + dsId + ".");
                    }
                }
            } else {
                log.debug("Menu config not defined or added to meta data for data source: " + dsId);
            }
        } else {
            log.debug("Meta data not set for data source: " + dsId);
        }

    }

}
