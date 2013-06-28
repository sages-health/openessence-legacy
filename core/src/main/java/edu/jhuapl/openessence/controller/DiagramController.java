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

import edu.jhuapl.openessence.datasource.jdbc.JdbcOeDataSource;
import edu.jhuapl.openessence.model.ChartModel;
import edu.jhuapl.openessence.model.TimeSeriesModel;
import edu.jhuapl.openessence.web.util.ErrorMessageException;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.security.Principal;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@Controller
public class DiagramController extends OeController {

    @Inject
    private ReportController reportController;

    // Convention is to use - as separator, see http://stackoverflow.com/a/785798
    @RequestMapping(value = "/ds/{dataSource}/diagrams/time-series", produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Map<String, Object> timeSeriesJson(@PathVariable JdbcOeDataSource dataSource, TimeSeriesModel model,
                                       Principal principal, WebRequest request, HttpServletRequest servletRequest)
            throws ErrorMessageException {

        return reportController.timeSeriesJson(dataSource, model, principal, request, servletRequest);
    }

    @RequestMapping(value = "/ds/{dataSource}/diagrams/chart", produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    Map<String, Object> chartJson(WebRequest request, HttpServletRequest servletRequest,
                                  @RequestParam("dsId") JdbcOeDataSource ds, ChartModel chartModel)
            throws ErrorMessageException {

        // TODO redirect to /chart/pie
        return reportController.chartJson(request, servletRequest, ds, chartModel);
    }

    // TODO /chart/pie, /chart/bar (requires client not sending that in body)
}
