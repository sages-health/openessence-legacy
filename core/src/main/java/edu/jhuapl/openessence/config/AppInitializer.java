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

package edu.jhuapl.openessence.config;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Hook into ApplicationContext startup. Useful for things like adding PropertySources to the Environment and
 * programmatically configuring the active profiles.
 *
 * Enabled in web.xml (context-param contextInitializerClasses).
 */
public class AppInitializer implements ApplicationContextInitializer<ConfigurableWebApplicationContext> {

    private static final String BUILTIN_PROP_SOURCE = "builtins";

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void initialize(ConfigurableWebApplicationContext applicationContext) {
        addPropertySources(applicationContext);

        // see http://bugzilla.slf4j.org/show_bug.cgi?id=184 for why we need ""
        log.info("Active profiles: {}{}", "", applicationContext.getEnvironment().getActiveProfiles());
    }

    private void addPropertySources(ConfigurableWebApplicationContext ctx) {
        ConfigurableEnvironment env = ctx.getEnvironment();

        // add properties that don't come from .properties files
        env.getPropertySources().addFirst(getBuiltinPropertySource(ctx));

        try {
            Resource[] classpathPropResources = ctx.getResources("classpath:/config/*.properties");
            for (PropertySource<?> p : getPropertySources(Arrays.asList(classpathPropResources))) {
                env.getPropertySources().addFirst(p);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Add builtin properties, i.e. properties added to environment that do not come from .properties files.
     */
    private MapPropertySource getBuiltinPropertySource(ConfigurableWebApplicationContext ctx) {
        Map<String, Object> builtinProps = new HashMap<String, Object>();
        builtinProps.put("contextPath", ctx.getServletContext().getContextPath());

        return new MapPropertySource(BUILTIN_PROP_SOURCE, builtinProps);
    }

    private List<PropertySource<?>> getPropertySources(Collection<Resource> resources) {
        List<PropertySource<?>> propertySources = new ArrayList<PropertySource<?>>();

        for (Resource r : resources) {
            String filename = r.getFilename();
            if (filename == null) {
                throw new IllegalArgumentException("Cannot have resource with no file");
            }

            String name = FilenameUtils.getBaseName(r.getFilename());

            Properties source;
            try {
                source = PropertiesLoaderUtils.loadProperties(r);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

            propertySources.add(new PropertiesPropertySource(name, source));

            try {
                log.info("Adding file {} as property source named '{}'", r.getFile(), name);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        return propertySources;
    }

}
