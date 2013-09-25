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

import edu.jhuapl.openessence.datasource.OeDataSourceAccessException;
import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.jdbc.JdbcOeDataSource;
import edu.jhuapl.openessence.model.ChartModel;
import edu.jhuapl.openessence.model.DataSourceDetails;
import edu.jhuapl.openessence.model.TimeSeriesModel;
import edu.jhuapl.openessence.web.util.ErrorMessageException;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * WIP controller for RESTful interface.
 */
@Controller
public class DataSourceController extends OeController {

    @Inject
    private ReportController reportController;

    @RequestMapping(value = "/ds/{dataSource}", method = RequestMethod.GET)
    public /*@ResponseBody Map<String, Object>*/ String fields(@PathVariable JdbcOeDataSource dataSource)
            throws IOException {
        return "forward:/oe/report/getFields?dsId=" + dataSource.getDataSourceId();
        //return reportController.getFields(dataSource);
    }

    @RequestMapping(value = "/ds/{dataSource}/details", method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public
    @ResponseBody
    DataSourceDetails details(@PathVariable JdbcOeDataSource dataSource, ServletWebRequest request, Principal principal,
                              @RequestParam(value = "firstrecord", defaultValue = "0") long firstRecord,
                              @RequestParam(value = "pagesize", defaultValue = "200") long pageSize)
            throws IOException, OeDataSourceException, OeDataSourceAccessException, ErrorMessageException {

        return reportController.detailsQuery(request, dataSource, firstRecord, pageSize);
    }

    @RequestMapping(value = "/ds/{dataSource}/details", method = RequestMethod.GET, produces = "text/csv")
    public void detailsCSV(@PathVariable JdbcOeDataSource dataSource,
                           ServletWebRequest request, HttpServletResponse response)
            throws IOException, OeDataSourceException, OeDataSourceAccessException, ErrorMessageException {

        reportController.exportGridToFile(dataSource, request, response);
    }

    @RequestMapping(value = "/ds/{dataSource}/timeSeries", method = RequestMethod.GET)
    public
    @ResponseBody
    Map<String, Object> timeSeries(@PathVariable JdbcOeDataSource dataSource,
                                   TimeSeriesModel model, Principal principal, WebRequest request,
                                   HttpServletRequest servletRequest) throws ErrorMessageException {

        return reportController.timeSeriesJson(dataSource, model, principal, request, servletRequest);
    }

    @RequestMapping(value = "/ds/{dataSource}/chart", method = RequestMethod.GET)
    public
    @ResponseBody
    Map<String, Object> chart(@PathVariable JdbcOeDataSource dataSource,
                              WebRequest request, HttpServletRequest servletRequest, ChartModel chartModel)
            throws ErrorMessageException {

        return reportController.chartJson(request, servletRequest, dataSource, chartModel);
    }

}
