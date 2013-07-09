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

import edu.jhuapl.bsp.detector.DetectorHelper;
import edu.jhuapl.bsp.detector.TemporalDetectorInterface;
import edu.jhuapl.bsp.detector.TemporalDetectorSimpleDataObject;
import edu.jhuapl.bsp.detector.temporal.epa.NoDetectorDetector;
import edu.jhuapl.graphs.Encoding;
import edu.jhuapl.graphs.GraphException;
import edu.jhuapl.graphs.GraphSource;
import edu.jhuapl.graphs.PointInterface;
import edu.jhuapl.graphs.controller.DefaultGraphData;
import edu.jhuapl.graphs.controller.GraphController;
import edu.jhuapl.graphs.controller.GraphDataHandlerInterface;
import edu.jhuapl.graphs.controller.GraphDataInterface;
import edu.jhuapl.graphs.controller.GraphDataSerializeToDiskHandler;
import edu.jhuapl.graphs.controller.GraphObject;
import edu.jhuapl.openessence.datasource.Dimension;
import edu.jhuapl.openessence.datasource.FieldType;
import edu.jhuapl.openessence.datasource.Filter;
import edu.jhuapl.openessence.datasource.OeDataSource;
import edu.jhuapl.openessence.datasource.OeDataSourceAccessException;
import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.Record;
import edu.jhuapl.openessence.datasource.dataseries.AccumPoint;
import edu.jhuapl.openessence.datasource.dataseries.DataSeriesSource;
import edu.jhuapl.openessence.datasource.dataseries.Grouping;
import edu.jhuapl.openessence.datasource.dataseries.GroupingDimension;
import edu.jhuapl.openessence.datasource.jdbc.JdbcOeDataSource;
import edu.jhuapl.openessence.datasource.jdbc.QueryRecord;
import edu.jhuapl.openessence.datasource.jdbc.ResolutionHandler;
import edu.jhuapl.openessence.datasource.jdbc.dataseries.AccumPointImpl;
import edu.jhuapl.openessence.datasource.jdbc.dataseries.GroupingImpl;
import edu.jhuapl.openessence.datasource.jdbc.entry.JdbcOeDataEntrySource;
import edu.jhuapl.openessence.datasource.jdbc.filter.FieldFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.GteqFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.LteqFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.OneArgOpFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.sorting.OrderByFilter;
import edu.jhuapl.openessence.datasource.jdbc.timeresolution.sql.pgsql.PgSqlDateHelper;
import edu.jhuapl.openessence.datasource.jdbc.timeresolution.sql.pgsql.PgSqlWeeklyHandler;
import edu.jhuapl.openessence.datasource.ui.ChildTableConfiguration;
import edu.jhuapl.openessence.datasource.ui.DimensionConfiguration;
import edu.jhuapl.openessence.datasource.ui.PossibleValuesConfiguration;
import edu.jhuapl.openessence.i18n.InspectableResourceBundleMessageSource;
import edu.jhuapl.openessence.logging.LogStatements;
import edu.jhuapl.openessence.model.ChartData;
import edu.jhuapl.openessence.model.ChartModel;
import edu.jhuapl.openessence.model.DataSourceDetails;
import edu.jhuapl.openessence.model.TimeSeriesModel;
import edu.jhuapl.openessence.web.util.ControllerUtils;
import edu.jhuapl.openessence.web.util.DetailsQuery;
import edu.jhuapl.openessence.web.util.ErrorMessageException;
import edu.jhuapl.openessence.web.util.FileExportUtil;
import edu.jhuapl.openessence.web.util.Filters;
import edu.jhuapl.openessence.web.util.Sorters;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.ArrayUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/report")
public class ReportController extends OeController {

    private static final int DEFAULT_LABEL_LENGTH = 45;
    private static final String PIE = "pie";
    private static final String BAR = "bar";

    private static final int DEFAULT_WEEK_STARTDAY = 1;
    private static final int DEFAULT_DAILY_PREPULL = 40;
    private static final Logger log = LoggerFactory.getLogger(ReportController.class);
    private static final Logger translationLog = LoggerFactory.getLogger("TranslationLogger");

    private DateFormat dateFormatDay = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat dateFormatWeek = new SimpleDateFormat("yyyy-MM-dd-'W'w");
    private DateFormat dateFormatWeekPart = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat dateFormatMonth = new SimpleDateFormat("yyyy-MM");
    private DateFormat dateFormatYear = new SimpleDateFormat("yyyy");

    private static final String DAILY = "daily";
    private static final String WEEKLY = "weekly";
    private static final String MONTHLY = "monthly";
    private static final String YEARLY = "yearly";
    private static final String TIMEZONE_ENABLED = "timezone.enabled";
    private String graphDir;

    @Resource
    private InspectableResourceBundleMessageSource messageSource;

    private Map<String, Integer> intervalMap;

    public ReportController() {
        intervalMap = new HashMap<String, Integer>();
        intervalMap.put("hourly", Calendar.HOUR_OF_DAY);
        intervalMap.put(DAILY, Calendar.DAY_OF_MONTH);
        intervalMap.put(WEEKLY, Calendar.WEEK_OF_YEAR);
        intervalMap.put(MONTHLY, Calendar.MONTH);
    }

    @Autowired
    public void setGraphDir(@Qualifier("graphDir") String graphDir) {
        this.graphDir = graphDir;
        File f = new File(this.graphDir);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                log.error("Unable to make directory " + graphDir);
            }
        }
    }

    @RequestMapping("/getFields")
    // TODO return real domain object
    public
    @ResponseBody
    Map<String, Object> getFields(@RequestParam("dsId") JdbcOeDataSource ds) throws IOException {
        final Map<String, Object> result = new HashMap<String, Object>();
        final List<DimensionConfiguration> filters = new ArrayList<DimensionConfiguration>();
        filters.addAll(getDimensionsInformation(ds.getFilterDimensions()));

        if (ds instanceof DataSeriesSource) {
            final DataSeriesSource dss = (DataSeriesSource) ds;
            // Add reserved fields, accumId, resolution field, and resolutions
            // each available 'entry' is a 'possible value' in the combo

            if (ds.getFilterDimension("accumId") != null) {

                if (!dss.getAccumulations().isEmpty()) {
                    for (final DimensionConfiguration filter : filters) {
                        if ("accumId".equals(filter.getName())) {
                            final List<List<Object>> possvals = new ArrayList<List<Object>>();
                            for (final Dimension accum : dss.getAccumulations()) {
                                final LinkedList<Object> accEntry = new LinkedList<Object>();
                                // Id then display value
                                accEntry.add(accum.getId());
                                if (accum.getDisplayName() == null) {
                                    accEntry.add(accum.getId());
                                } else {
                                    accEntry.add(accum.getDisplayName());
                                }
                                possvals.add(accEntry);
                            }

                            filter.setPossibleValues(new PossibleValuesConfiguration(possvals));
                            break;
                        }
                    }

                    // Group/Resolutions
                    final DimensionConfiguration timeseriesGroupResolution = new DimensionConfiguration();
                    timeseriesGroupResolution.setName("timeseriesGroupResolution");
                    timeseriesGroupResolution.setType(FieldType.TEXT);

                    final Map<String, Object> formMetaData = new HashMap<String, Object>();
                    formMetaData.put("allowBlank", false);
                    final Map<String, Object> metaData = new HashMap<String, Object>();
                    metaData.put("form", formMetaData);

                    timeseriesGroupResolution.setMeta(metaData);

                    final List<List<Object>> groupResolutionValues = new ArrayList<List<Object>>();
                    for (final GroupingDimension gdim : dss.getGroupingDimensions()) {
                        for (final String res : gdim.getResolutions()) {
                            final LinkedList<Object> groupResolutionEntry = new LinkedList<Object>();
                            // Id then display value
                            groupResolutionEntry.add(gdim.getId() + ":" + res);
                            groupResolutionEntry
                                    .add(messageSource.getDataSourceMessage(gdim.getId(), dss) + " / " + messageSource
                                            .getDataSourceMessage(res, dss));
                            groupResolutionValues.add(groupResolutionEntry);
                        }
                    }
                    timeseriesGroupResolution.setPossibleValues(new PossibleValuesConfiguration(groupResolutionValues));
                    filters.add(timeseriesGroupResolution);
                }
            }
        }
        result.put("filters", filters);

        //Updated detail dimensions to use new dimension configuration that populates
        //possibleValues
        List<DimensionConfiguration> detailDimensions = new ArrayList<DimensionConfiguration>();
        detailDimensions.addAll(getDimensionsInformation(ds.getResultDimensions()));
        result.put("detailDimensions", detailDimensions);

        if (ds instanceof JdbcOeDataEntrySource) {
            final JdbcOeDataEntrySource jdes = (JdbcOeDataEntrySource) ds;
            result.put("pks", jdes.getParentTableDetails().getPks());
            final ArrayList<Object> editDimensions = new ArrayList<Object>();
            editDimensions.addAll(getDimensionsInformation(jdes.getEditDimensions()));

            // Add child table information
            for (final String tableName : jdes.getChildTableMap().keySet()) {
                editDimensions.add(new ChildTableConfiguration(tableName, jdes));
            }

            result.put("editDimensions", editDimensions);
        }

        // Data source level meta data
        final Map<String, Object> meta = ds.getMetaData();
        if (meta != null) {
            result.put("meta", meta);
        }

        result.put("success", true);

        return result;
    }

    private List<DimensionConfiguration> getDimensionsInformation(final Collection<? extends Dimension> dimensions)
            throws OeDataSourceException {
        List<DimensionConfiguration> results = new ArrayList<DimensionConfiguration>();

        for (final Dimension dimension : dimensions) {
            results.add(new DimensionConfiguration(dimension));
        }
        return results;
    }

    @RequestMapping("/timeSeriesJson")
    public
    @ResponseBody
    Map<String, Object> timeSeriesJson(@RequestParam("dsId") JdbcOeDataSource ds, TimeSeriesModel model,
                                       Principal principal, WebRequest request, HttpServletRequest servletRequest)
            throws ErrorMessageException {

        Map<String, Object> result = new HashMap<String, Object>();

        DataSeriesSource dss = null;
        if (ds instanceof DataSeriesSource) {
            dss = (DataSeriesSource) ds;

        }

        String groupId = "";
        String resolution = "";

        // TODO put this logic in a custom HandlerMethodArgumentResolver
        if (model.getTimeseriesGroupResolution() != null) {
            String[] parts = model.getTimeseriesGroupResolution().split(":");
            if (parts.length == 2 && !parts[0].trim().isEmpty() && !parts[1].trim().isEmpty()) {
                groupId = parts[0];
                resolution = parts[1];
            }
        }

        if (groupId == null || groupId.isEmpty()) {
            throw new OeDataSourceException("No Grouping Dimension ID specified");
        }

        GroupingDimension groupingDim = dss.getGroupingDimension(groupId);
        // find resolution handlers as appropriate for groupings
        if (resolution == null || "".equals(groupId)) {
            String[] res = groupingDim.getResolutions().toArray(new String[groupingDim.getResolutions().size()]);
            if (res.length > 0) {
                resolution = res[0];
            }
        }

        if (ds.getDimensionJoiner() != null) {
            ds.getDimensionJoiner().joinDimensions();
        }

        List<Dimension> accumulations = ControllerUtils.getAccumulationsByIds(ds, model.getAccumId());
        List<Dimension> timeseriesDenominators =
                ControllerUtils.getAccumulationsByIds(ds, model.getTimeseriesDenominator(), false);

        final List<OrderByFilter> sorts = new ArrayList<OrderByFilter>();
        GroupingImpl group = new GroupingImpl(groupId, resolution);

        if (resolution.equals(DAILY) && model.getPrepull() < 0) {
            model.setPrepull(DEFAULT_DAILY_PREPULL);
        }

        if (model.getPrepull() < 0) {
            model.setPrepull(0);
        }

        //union accumulations to get all results
        List<Dimension> dimensions = new ArrayList<Dimension>(ControllerUtils.unionDimensions(accumulations,
                                                                                              timeseriesDenominators));
        List<Grouping> groupings = new ArrayList<Grouping>();
        groupings.add(groupingDim.makeGrouping(resolution));

        //create results group dimension + all dimensions
        final List<Dimension> results = new ArrayList<Dimension>();
        for (Dimension d : dimensions) {
            results.add(ds.getResultDimension(d.getId()));
        }

        Map<String, ResolutionHandler> resolutionHandlers = dss.getGroupingDimension(group.getId()).getResolutionsMap();
        List<Filter> filters = new Filters().getFilters(request.getParameterMap(), dss, group.getId(),
                                                        model.getPrepull(), resolution,
                                                        getCalWeekStartDay(resolutionHandlers));

        String clientTimezone = null;
        String timezoneEnabledString = messageSource.getMessage(TIMEZONE_ENABLED, "false");
        if (timezoneEnabledString.equalsIgnoreCase("true")) {
            clientTimezone = ControllerUtils.getRequestTimezoneAsHourMinuteString(request);
        }
        //details query for all records
        Collection<Record> records = new DetailsQuery().performDetailsQuery(ds, results, dimensions, filters, sorts,
                                                                            groupings, false, clientTimezone);

        //create graph data and set known configuration
        DefaultGraphData graphData = new DefaultGraphData();
        graphData.setShowSingleSeverityLegends(false);
        graphData.setGraphTitle(model.getTimeseriesTitle());
        graphData.setGraphWidth(model.getWidth());
        graphData.setGraphHeight(model.getHeight());
        graphData.setShowLegend(true);
        graphData.setBackgroundColor(new Color(255, 255, 255, 0));

        // only set an array if they provided one
        if (model.getGraphBaseColors() != null && model.getGraphBaseColors().length > 0) {
            // TODO leverage Spring to convert colors
            graphData.setGraphBaseColors(ControllerUtils.getColorsFromHex(Color.BLACK, model.getGraphBaseColors()));
        }

        String graphTimeSeriesUrl = request.getContextPath() + servletRequest.getServletPath()
                                    + "/report/graphTimeSeries";
        graphTimeSeriesUrl = appendGraphFontParam(ds, graphTimeSeriesUrl);

        //TODO, this still uses the html method from the graph module and then wraps in json...move to a pure json method
        Map<String, Object> timeseriesResult = createTimeseries(principal.getName(), dss,
                                                                filters, group, resolution, model.getPrepull(),
                                                                graphTimeSeriesUrl,
                                                                records, accumulations, timeseriesDenominators,
                                                                model.getTimeseriesDetectorClass(),
                                                                model.isIncludeDetails(),
                                                                model.isDisplayIntervalEndDate(), graphData,
                                                                ControllerUtils.getRequestTimezone(request));

        result.putAll(timeseriesResult);

        return result;
    }

    @RequestMapping("/chartJson")
    public
    @ResponseBody
    Map<String, Object> chartJson(WebRequest request, HttpServletRequest servletRequest,
                                  @RequestParam("dsId") JdbcOeDataSource ds, ChartModel chartModel)
            throws ErrorMessageException {

        log.info(LogStatements.GRAPHING.getLoggingStmt() + request.getUserPrincipal().getName());

        final List<Filter> filters = new Filters().getFilters(request.getParameterMap(), ds, null, 0, null, 0);
        final List<Dimension> results =
                ControllerUtils.getResultDimensionsByIds(ds, request.getParameterValues("results"));

        Dimension filterDimension = null;
        if (results.get(0).getFilterBeanId() != null && results.get(0).getFilterBeanId().length() > 0) {
            filterDimension = ds.getFilterDimension(results.get(0).getFilterBeanId());
        }
        // if not provided, use the result dimension
        // it means name and id columns are same...
        if (filterDimension != null) {
            results.add(results.size(), filterDimension);
        }

        // Subset of results, should check
        final List<Dimension> charts =
                ControllerUtils.getResultDimensionsByIds(ds, request.getParameterValues("charts"));

        final List<Dimension> accumulations =
                ControllerUtils.getAccumulationsByIds(ds, request.getParameterValues("accumId"));

        final List<OrderByFilter> sorts = new ArrayList<OrderByFilter>();
        try {
            sorts.addAll(Sorters.getSorters(request.getParameterMap()));
        } catch (Exception e) {
            log.warn("Unable to get sorters, using default ordering");
        }

        // TODO put this on ChartModel
        //default to white allows clean copy paste of charts from browser
        Color backgroundColor = Color.WHITE;

        String bgParam = request.getParameter("backgroundColor");
        if (bgParam != null && !"".equals(bgParam)) {
            if ("transparent".equalsIgnoreCase(bgParam)) {
                backgroundColor = new Color(255, 255, 255, 0);
            } else {
                backgroundColor = ControllerUtils.getColorsFromHex(Color.WHITE, bgParam)[0];
            }
        }

        String graphBarUrl = request.getContextPath() + servletRequest.getServletPath() + "/report/graphBar";
        graphBarUrl = appendGraphFontParam(ds, graphBarUrl);

        String graphPieUrl = request.getContextPath() + servletRequest.getServletPath() + "/report/graphPie";
        graphPieUrl = appendGraphFontParam(ds, graphPieUrl);

        // TODO eliminate all the nesting in response and just use accumulation and chartID properties
        Map<String, Object> response = new HashMap<String, Object>();
        Map<String, Object> graphs = new HashMap<String, Object>();
        response.put("graphs", graphs);

        String clientTimezone = null;
        String timezoneEnabledString = messageSource.getMessage(TIMEZONE_ENABLED, "false");
        if (timezoneEnabledString.equalsIgnoreCase("true")) {
            clientTimezone = ControllerUtils.getRequestTimezoneAsHourMinuteString(request);
        }
        Collection<Record> records = new DetailsQuery().performDetailsQuery(ds, results, accumulations,
                                                                            filters, sorts, false, clientTimezone);
        final List<Filter> graphFilters =
                new Filters().getFilters(request.getParameterMap(), ds, null, 0, null, 0, false);
        //for each requested accumulation go through each requested result and create a chart
        for (Dimension accumulation : accumulations) {
            Map<String, Object> accumulationMap = new HashMap<String, Object>();
            // Create charts for dimensions (subset of results)
            for (Dimension chart : charts) {
                DefaultGraphData data = new DefaultGraphData();
                data.setGraphTitle(chartModel.getTitle());
                data.setGraphHeight(chartModel.getHeight());
                data.setGraphWidth(chartModel.getWidth());
                data.setShowLegend(chartModel.isLegend());
                data.setBackgroundColor(backgroundColor);
                data.setShowGraphLabels(chartModel.isShowGraphLabels());
                data.setLabelBackgroundColor(backgroundColor);
                data.setPlotHorizontal(chartModel.isPlotHorizontal());
                data.setNoDataMessage(chartModel.getNoDataMessage());
                data.setTitleFont(new Font("Arial", Font.BOLD, 12));

                GraphObject graph = createGraph(ds, request.getUserPrincipal().getName(), records, chart,
                                                filterDimension, accumulation, data, chartModel, graphFilters);
                String graphURL = "";
                if (BAR.equalsIgnoreCase(chartModel.getType())) {
                    graphURL = graphBarUrl;
                } else if (PIE.equalsIgnoreCase(chartModel.getType())) {
                    graphURL = graphPieUrl;
                }
                graphURL = appendUrlParameter(graphURL, "graphDataId", graph.getGraphDataId());

                chartModel.setImageUrl(graphURL);
                chartModel.setImageMap(graph.getImageMap());
                chartModel.setImageMapName(graph.getImageMapName());

                accumulationMap.put(chart.getId(), chartModel);
            }
            graphs.put(accumulation.getId(), accumulationMap);
        }

        log.info(String.format("Chart JSON Details query for %s", request.getUserPrincipal().getName()));

        return response;
    }

    private Map<String, Object> createTimeseries(String userPrincipalName, DataSeriesSource dss, List<Filter> filters,
                                                 GroupingImpl group,
                                                 String timeResolution, Integer prepull, String graphTimeSeriesUrl,
                                                 final Collection<Record> records,
                                                 final List<Dimension> accumulations,
                                                 final List<Dimension> timeseriesDenominators,
                                                 String detectorClass, boolean includeDetails,
                                                 boolean displayIntervalEndDate, GraphDataInterface graphData,
                                                 TimeZone clientTimezone) {

        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, ResolutionHandler> resolutionHandlers = null;
        result.put("success", false);
        try {
            GroupingDimension grpdim = dss.getGroupingDimension(group.getId());
            resolutionHandlers = grpdim.getResolutionsMap();
            String dateFieldName = group.getId();
            Date startDate = null;
            Date endDate = null;
            if (grpdim != null && (grpdim.getSqlType() == FieldType.DATE
                                   || grpdim.getSqlType() == FieldType.DATE_TIME)) {
                for (Filter f : filters) {
                    if (f instanceof OneArgOpFilter) {
                        OneArgOpFilter of = (OneArgOpFilter) f;
                        if (of.getFilterId().equalsIgnoreCase(grpdim.getId()) && (of.getSqlSnippet("")
                                                                                          .contains(">="))) {
                            startDate = (Date) of.getArguments().get(0);
                        } else if (of.getFilterId().equalsIgnoreCase(grpdim.getId()) && (of.getSqlSnippet("")
                                                                                                 .contains("<="))) {
                            endDate = (Date) of.getArguments().get(0);
                        }
                    }
                }
            }
            //union accumulations to get all results
            List<Dimension>
                    dimensions =
                    new ArrayList<Dimension>(ControllerUtils.unionDimensions(accumulations, timeseriesDenominators));

            int timeOffsetMillies = 0;
            String timezoneEnabledString = messageSource.getMessage(TIMEZONE_ENABLED, "false");
            if (timezoneEnabledString.equalsIgnoreCase("true")) {
                timeOffsetMillies = (clientTimezone.getRawOffset() - clientTimezone.getDSTSavings()) -
                                    (TimeZone.getDefault().getRawOffset() - TimeZone.getDefault().getDSTSavings());
            }
            Calendar startDayCal = Calendar.getInstance(clientTimezone);
            startDayCal.setTime(startDate);
            startDayCal.add(Calendar.MILLISECOND, timeOffsetMillies);

            //get data grouped by group dimension
            List<AccumPoint>
                    points =
                    extractAccumulationPoints(userPrincipalName, dss, records, startDayCal.getTime(), endDate,
                                              dimensions, group, resolutionHandlers);
            if (points.size() > 0) {
                DateFormat dateFormat = getDateFormat(timeResolution); //dateFormat.setTimeZone(timezone);
                DateFormat tmpDateFormat = (DateFormat) dateFormat.clone();
                tmpDateFormat.setTimeZone(clientTimezone);

                // number format for level
                NumberFormat numFormat3 = NumberFormat.getNumberInstance();
                numFormat3.setMinimumFractionDigits(0);
                numFormat3.setMaximumFractionDigits(3);

                // number format for expected count
                NumberFormat numFormat1 = NumberFormat.getNumberInstance();
                numFormat1.setMinimumFractionDigits(0);
                numFormat1.setMaximumFractionDigits(1);

                Calendar cal = new GregorianCalendar();
                cal.setTime(startDayCal.getTime());
                //offset start date to match prepull offset
                if (timeResolution.equals("weekly")) {
                    cal.add(Calendar.DATE, (7 * prepull));
                } else if (timeResolution.equals("daily")) {
                    cal.add(Calendar.DATE, prepull);
                }
                Date queryStartDate = cal.getTime();

                //-- Handles Denominator Types -- //
                double[] divisors = new double[points.size()];
                double multiplier = 1.0;
                boolean percentBased = false;
                String yAxisLabel = messageSource.getDataSourceMessage("graph.count", dss);

                boolean isDetectionDetector = !NoDetectorDetector.class
                        .getName().equalsIgnoreCase(detectorClass);

                //if there is a denominator we need to further manipulate the data
                if (timeseriesDenominators != null && !timeseriesDenominators.isEmpty()) {
                    // divisor is the sum of timeseriesDenominators
                    divisors = totalSeriesValues(points, timeseriesDenominators);
                    multiplier = 100.0;
                    percentBased = true;
                    yAxisLabel = messageSource.getDataSourceMessage("graph.percent", dss);
                } else {
                    //the query is for total counts
                    Arrays.fill(divisors, 1.0);
                }

                double[][] allCounts = new double[accumulations.size()][];
                int[][] allColors = new int[accumulations.size()][];
                String[][] allAltTexts = new String[accumulations.size()][];
                String[] dates = new String[]{""};
                double[][] allExpecteds = new double[accumulations.size()][];
                double[][] allLevels = new double[accumulations.size()][];
                String[][] allLineSetURLs = new String[accumulations.size()][];
                String[][] allSwitchInfo = new String[accumulations.size()][];
                String[] lineSetLabels = new String[accumulations.size()];
                boolean[] displayAlerts = new boolean[accumulations.size()];

                //get all results
                Collection<Dimension> dims = new ArrayList<Dimension>(dss.getResultDimensions());
                Collection<String> dimIds = ControllerUtils.getDimensionIdsFromCollection(dims);
                Collection<String> accIds = ControllerUtils.getDimensionIdsFromCollection(dss.getAccumulations());
                //remove extra accumulations in the result set using string ids
                dimIds.removeAll(accIds);

                //for each accumulation we run detection and gather results
                int aIndex = 0;
                for (Dimension accumulation : accumulations) {
                    String accumId = accumulation.getId();

                    // use display name if it has one, otherwise translate its ID
                    String accumIdTranslated = accumulation.getDisplayName();
                    if (accumIdTranslated == null) {
                        accumIdTranslated = messageSource.getDataSourceMessage(accumulation.getId(), dss);
                    }

                    TemporalDetectorInterface TDI =
                            (TemporalDetectorInterface) DetectorHelper.createObject(detectorClass);
                    TemporalDetectorSimpleDataObject TDDO = new TemporalDetectorSimpleDataObject();

                    int[] colors;
                    double[] counts;
                    String[] altTexts;
                    double[] expecteds;
                    double[] levels;
                    String[] switchInfo;
                    String[] urls;

                    //pull the counts from the accum array points
                    double[] seriesDoubleArray = generateSeriesValues(points, accumId);

                    //run divisor before detection
                    for (int i = 0; i < seriesDoubleArray.length; i++) {
                        double div = divisors[i];
                        if (div == 0) {
                            seriesDoubleArray[i] = 0.0;
                        } else {
                            seriesDoubleArray[i] = (seriesDoubleArray[i] / div) * multiplier;
                        }
                    }

                    //run detection
                    TDDO.setCounts(seriesDoubleArray);
                    TDDO.setStartDate(startDate);
                    TDDO.setTimeResolution(timeResolution);

                    try {
                        TDI.runDetector(TDDO);
                    } catch (Exception e) {
                        String errorMessage = "Failure to create Timeseries";
                        if (e.getMessage() != null) {
                            errorMessage = errorMessage + ":<BR>" + e.getMessage();
                        }
                        result.put("message", errorMessage);
                        result.put("success", false);
                        return result;
                    }

                    TDDO.cropStartup(prepull);
                    counts = TDDO.getCounts();
                    int tddoLength = counts.length;

                    if (!DAILY.equalsIgnoreCase(timeResolution)) {
                        //toggle between start date and end date
                        //TDDO.setDates(getOurDates(startDate, endDate, tddoLength, timeResolution));
                        TDDO.setDates(getOurDates(queryStartDate, endDate, tddoLength, timeResolution,
                                                  displayIntervalEndDate));
                    }
                    double[] tcolors = TDDO.getColors();

                    Date[] tdates = TDDO.getDates();
                    altTexts = TDDO.getAltTexts();
                    expecteds = TDDO.getExpecteds();
                    levels = TDDO.getLevels();
                    switchInfo = TDDO.getSwitchInfo();
                    colors = new int[tddoLength];
                    dates = new String[tddoLength];
                    urls = new String[tddoLength];

                    //add the accumId for the current series
                    dimIds.add(accumId);

                    StringBuilder jsCall = new StringBuilder();
                    jsCall.append("javascript:OE.report.datasource.showDetails({");
                    jsCall.append("dsId:'").append(dss.getClass().getName()).append("'");
                    //specify results
                    jsCall.append(",results:[").append(StringUtils.collectionToDelimitedString(dimIds, ",", "'", "'"))
                            .append(']');
                    //specify accumId
                    jsCall.append(",accumId:'").append(accumId).append("'");

                    addJavaScriptFilters(jsCall, filters, dateFieldName);

                    //this builds urls and hover texts
                    int startDay = getWeekStartDay(resolutionHandlers);

                    Calendar c = Calendar.getInstance(clientTimezone);

//				   Calendar curr = Calendar.getInstance();
                    for (int i = 0; i < tddoLength; i++) {
                        colors[i] = (int) tcolors[i];

                        // For a time series data point, set time to be current server time
                        // This will allow us to convert this data point date object to be request timezone date
                        c.setTime(tdates[i]);
                        c.add(Calendar.MILLISECOND, timeOffsetMillies);

                        if (timeResolution.equals(WEEKLY)) {
                            dates[i] = dateFormatWeekPart.format(tdates[i])
                                       + "-W" + PgSqlDateHelper.getWeekOfYear(startDay, c) + "-"
                                       + PgSqlDateHelper.getYear(startDay, c);
                        } else {
                            dates[i] = tmpDateFormat.format(c.getTime());
                        }

                        altTexts[i] = "(" + accumIdTranslated + ") " + // Accum
                                      "Date: " + dates[i] + // Date
                                      ", Level: " + numFormat3.format(levels[i]) + // Level
                                      ", Count: " + ((int) counts[i]) + // Count
                                      ", Expected: " + numFormat1.format(expecteds[i]); // Expected

                        if (switchInfo != null) {
                            altTexts[i] += ", Switch: " + switchInfo[i] + ", ";
                        }

                        // build the click through url
                        StringBuilder tmp = new StringBuilder(jsCall.toString());

                        // add the date field with start and end dates from the data point
                        if (!DAILY.equalsIgnoreCase(timeResolution)) {
                            Calendar timeSet = Calendar.getInstance(clientTimezone);
                            timeSet.setTime(tdates[i]);

                            if (WEEKLY.equalsIgnoreCase(timeResolution)) {
                                timeSet.set(Calendar.DAY_OF_WEEK, startDay + 1);
                                tmp.append(",").append(dateFieldName).append("_start:'")
                                        .append(timeSet.getTimeInMillis()).append("'");
                                timeSet.add(Calendar.DAY_OF_YEAR, 6);
                                tmp.append(",").append(dateFieldName).append("_end:'").append(timeSet.getTimeInMillis())
                                        .append("'");
                            } else if (MONTHLY.equalsIgnoreCase(timeResolution)) {
                                // Compute last day of month
                                timeSet.set(Calendar.DAY_OF_MONTH, 1);
                                timeSet.add(Calendar.MONTH, 1);
                                timeSet.add(Calendar.DAY_OF_YEAR, -1);
                                tmp.append(",").append(dateFieldName).append("_end:'").append(timeSet.getTimeInMillis())
                                        .append("'");
                                // set first day of month
                                timeSet.set(Calendar.DAY_OF_MONTH, 1);
                                tmp.append(",").append(dateFieldName).append("_start:'")
                                        .append(timeSet.getTimeInMillis()).append("'");
                            } else if (YEARLY.equalsIgnoreCase(timeResolution)) {
                                // Compute last day of month
                                timeSet.set(Calendar.DATE, 31);
                                timeSet.add(Calendar.MONTH, Calendar.DECEMBER);
                                tmp.append(",").append(dateFieldName).append("_end:'").append(timeSet.getTimeInMillis())
                                        .append("'");
                                timeSet.set(Calendar.DATE, 1);
                                timeSet.add(Calendar.MONTH, Calendar.JANUARY);
                                tmp.append(",").append(dateFieldName).append("_start:'")
                                        .append(timeSet.getTimeInMillis()).append("'");
                            }
                        } else {
                            // compute end date for individual data points based on the selected resolution
//						   detailsPointEndDate = computeEndDate(tdates[i],timeResolution);
                            // add the date field with start and end dates from the data point
                            tmp.append(",").append(dateFieldName).append("_start:'").append(tdates[i].getTime())
                                    .append("'");
                            tmp.append(",").append(dateFieldName).append("_end:'").append(tdates[i].getTime())
                                    .append("'");
                        }
                        tmp.append("});");
                        urls[i] = tmp.toString();
                    }

                    allCounts[aIndex] = counts;
                    allColors[aIndex] = colors;
                    allAltTexts[aIndex] = altTexts;
                    allExpecteds[aIndex] = expecteds;
                    allLevels[aIndex] = levels;
                    allLineSetURLs[aIndex] = urls;
                    allSwitchInfo[aIndex] = switchInfo;
                    lineSetLabels[aIndex] = accumIdTranslated;
                    displayAlerts[aIndex] = isDetectionDetector;
                    aIndex++;

                    //remove the accumId for the next series
                    dimIds.remove(accumId);
                }

                GraphDataSerializeToDiskHandler hndl = new GraphDataSerializeToDiskHandler(graphDir);
                GraphController gc = getGraphController(null, hndl, userPrincipalName);
                //TODO figure out why I (hodancj1) added this to be accumulation size ~Feb 2012
                // gc.setMaxLegendItems(accumulations.size());

                graphData.setShowSingleAlertLegends(isDetectionDetector);
                graphData.setCounts(allCounts);
                graphData.setColors(allColors);
                graphData.setAltTexts(allAltTexts);
                graphData.setXLabels(dates);
                graphData.setExpecteds(allExpecteds);
                graphData.setLevels(allLevels);
                graphData.setLineSetURLs(allLineSetURLs);
                graphData.setLineSetLabels(lineSetLabels);
                graphData.setDisplayAlerts(displayAlerts);
                // graphData.setDisplaySeverityAlerts(displayAlerts);
                graphData.setPercentBased(percentBased);

                graphData.setXAxisLabel(messageSource.getDataSourceMessage(group.getResolution(), dss));
                graphData.setYAxisLabel(yAxisLabel);

                int maxLabels = graphData.getGraphWidth() / 30;
                graphData.setMaxLabeledCategoryTicks(Math.min(maxLabels, allCounts[0].length));

                StringBuffer sb = new StringBuffer();
                GraphObject graph = gc.writeTimeSeriesGraph(sb, graphData, true, true, false, graphTimeSeriesUrl);

                result.put("html", sb.toString());

                //added to build method calls from javascript
                Map<String, Object> graphConfig = new HashMap<String, Object>();
                graphConfig.put("address", graphTimeSeriesUrl);
                graphConfig.put("graphDataId", graph.getGraphDataId());
                graphConfig.put("imageMapName", graph.getImageMapName());

                graphConfig.put("graphTitle", graphData.getGraphTitle());
                graphConfig.put("xAxisLabel", graphData.getXAxisLabel());
                graphConfig.put("yAxisLabel", graphData.getYAxisLabel());
                graphConfig.put("xLabels", graphData.getXLabels());
                graphConfig.put("graphWidth", graphData.getGraphWidth());
                graphConfig.put("graphHeight", graphData.getGraphHeight());

                graphConfig.put("yAxisMin", graph.getYAxisMin());
                graphConfig.put("yAxisMax", graph.getYAxisMax());

                // fix invalid JSON coming from GraphController
                String dataSeriesJson = graph.getDataSeriesJSON()
                        .replaceFirst("\\{", "")
                                // remove trailing "}"
                        .substring(0, graph.getDataSeriesJSON().length() - 2);

                // read malformed JSON
                ObjectMapper mapper = new ObjectMapper();
                JsonFactory jsonFactory = mapper.getJsonFactory()
                        .configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
                        .configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                JsonParser jsonParser = jsonFactory.createJsonParser(dataSeriesJson);

                // array of String -> Object maps
                TypeReference<Map<String, Object>[]> dataSeriesType = new TypeReference<Map<String, Object>[]>() {
                };

                // write JSON as Map so that it can be serialized properly back to JSON
                Map<String, Object>[] seriesMap = mapper.readValue(jsonParser, dataSeriesType);
                graphConfig.put("dataSeriesJSON", seriesMap);

                if (includeDetails) {
                    int totalPoints = 0;
                    List<HashMap<String, Object>> details = new ArrayList<HashMap<String, Object>>();
                    HashMap<String, Object> detail;
                    for (int i = 0; i < allCounts.length; i++) {
                        for (int j = 0; j < allCounts[i].length; j++) {
                            totalPoints++;
                            detail = new HashMap<String, Object>();
                            detail.put("Date", dates[j]);
                            detail.put("Series", lineSetLabels[i]);
                            detail.put("Level", allLevels[i][j]);
                            detail.put("Count", allCounts[i][j]);
                            if (!ArrayUtils.isEmpty(allExpecteds[i])) {
                                detail.put("Expected", allExpecteds[i][j]);
                            }
                            if (!ArrayUtils.isEmpty(allSwitchInfo[i])) {
                                detail.put("Switch", allSwitchInfo[i][j]);
                            }
                            detail.put("Color", allColors[i][j]);
                            details.add(detail);
                        }
                    }
                    result.put("detailsTotalRows", totalPoints);
                    result.put("details", details);
                }
                result.put("graphConfiguration", graphConfig);
                result.put("success", true);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("<h2>" + messageSource.getDataSourceMessage("graph.nodataline1", dss) + "</h2>");
                sb.append("<p>" + messageSource.getDataSourceMessage("graph.nodataline2", dss) + "</p>");
                result.put("html", sb.toString());
                result.put("success", true);
            }
        } catch (Exception e) {
            log.error("Failure to create Timeseries", e);
        }
        return result;
    }

    protected DateFormat getDateFormat(String timeResolution) {
        DateFormat dateFormat = dateFormatDay;
        String formatKey = "java.date.formatDay";
        String formatError = "[" + formatKey + "]";
        String formatValue = "";
        if (DAILY.equalsIgnoreCase(timeResolution)) {
            formatKey = "java.date.formatDay";
            dateFormat = dateFormatDay;
        } else if (WEEKLY.equalsIgnoreCase(timeResolution)) {
            formatKey = "java.date.formatWeek";
            dateFormat = dateFormatWeek;
        } else if (MONTHLY.equalsIgnoreCase(timeResolution)) {
            formatKey = "java.date.formatMonth";
            dateFormat = dateFormatMonth;
        } else if (YEARLY.equalsIgnoreCase(timeResolution)) {
            formatKey = "java.date.formatYear";
            dateFormat = dateFormatYear;
        }
        formatError = "[" + formatKey + "]";
        formatValue = messageSource.getMessage(formatKey);

        if (!"".equals(formatValue) && !formatError.equals(formatValue)) {
            try {
                dateFormat = new SimpleDateFormat(formatValue);
            } catch (Exception ex) {
                translationLog.error("Error parsing " + formatKey + " into a DateFormat", ex);
            }
        }
        return dateFormat;
    }

    //Original code taken from TemporalDetectorSimpleDataObserver.setupDates and reworked to better handle months
    private Date[] getOurDates(Date queryStartDate, Date endDate, int size, String timeResolution,
                               boolean displayIntervalEndDate) {
        Date startDate = queryStartDate;
        if (displayIntervalEndDate) {
            startDate = computeResolutionBasedEndDate(queryStartDate, timeResolution, endDate);
        }

        Date[] dates = new Date[size];

        String tr = timeResolution;
        if (tr == null) {
            tr = DAILY;
        }
        int zeroFillInterval = intervalMap.keySet().contains(timeResolution) ? intervalMap.get(timeResolution) : -1;
        if (startDate != null && size >= 0) {
            Calendar cal = new GregorianCalendar();
            //forward point allows us to place the accumulated data at the front
            int i = 0;
            for (i = 0; i < size; i++) {
                //reset date to avoid unexpected date changes
                cal.setTime(startDate);
                cal.add(zeroFillInterval, 1 * i);
                if (endDate != null && cal.getTime().after(endDate)) {
                    cal.setTime(endDate);
                }
                //store date after interval addition
                dates[i] = cal.getTime();
            }
        }
        return dates;
    }


    /**
     * Compute end date based on time resolution. Defaults to the original date unless the resolution is weekly, monthly
     * or yearly in which case it is padded accordingly.
     *
     * @param maxDate optionally used to keep the computed date below a maxDate (end date for the query for example)
     * @return Date
     */
    private Date computeResolutionBasedEndDate(Date startDate, String timeResolution, Date maxDate) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(startDate);
        if (WEEKLY.equalsIgnoreCase(timeResolution)) {
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            cal.add(Calendar.DATE, -1);
        } else if (MONTHLY.equalsIgnoreCase(timeResolution)) {
            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.DATE, -1);
        } else if (YEARLY.equalsIgnoreCase(timeResolution)) {
            cal.add(Calendar.YEAR, 1);
            cal.add(Calendar.DATE, -1);
        } else {
            //do nothing for daily currently
        }
        //we want the end date/label to not exceed the query end date
        if (maxDate != null && cal.getTime().after(maxDate)) {
            cal.setTime(maxDate);
        }
        return cal.getTime();
    }

    private GraphObject createGraph(OeDataSource dataSource, final String userPrincipalName,
                                    final Collection<Record> records,
                                    final Dimension dimension, final Dimension filter, final Dimension accumulation,
                                    DefaultGraphData data,
                                    ChartModel chart, List<Filter> filters) {

        String filterId = (filter == null) ? dimension.getId() : filter.getId();
        Map<String, String> possibleKeyValueMap = null;
        if (dimension.getPossibleValuesConfiguration() != null
            && dimension.getPossibleValuesConfiguration().getData() != null) {
            List<List<Object>> dataMap = dimension.getPossibleValuesConfiguration().getData();
            possibleKeyValueMap = new HashMap<String, String>();
            for (int i = 0; i < dataMap.size(); i++) {
                String
                        dispVal =
                        dataMap.get(i).size() == 2 ? dataMap.get(i).get(1).toString()
                                                   : dataMap.get(i).get(0).toString();
                possibleKeyValueMap.put(dataMap.get(i).get(0).toString(), dispVal);
            }
        }

        GraphDataSerializeToDiskHandler hndl = new GraphDataSerializeToDiskHandler(graphDir);
        GraphObject graph = null;

        Color[] colorsFromHex = null;
        //only set an array if they provided one
        if (!ArrayUtils.isEmpty(chart.getGraphBaseColors())) {
            colorsFromHex = ControllerUtils.getColorsFromHex(Color.BLUE, chart.getGraphBaseColors());
            //TODO when we limit the series these colors need augmented.  Create a map of id = graphbasecolor[index] first, then use that map to create a
            //new graph base color array that combines the parameter list with the default list...
            data.setGraphBaseColors(colorsFromHex);
        }

        GraphController gc = getGraphController(null, hndl, userPrincipalName);

        List<Record> recs = new ArrayList<Record>(records);

        String otherLabel = messageSource.getDataSourceMessage("graph.category.other", dataSource);

        LinkedHashMap<String, ChartData>
                recordMap =
                getRecordMap(recs, accumulation.getId(), dimension.getId(), filterId);
        //perform series limit
        recordMap = ControllerUtils.getSortedAndLimitedChartDataMap(recordMap, chart.getCategoryLimit(), otherLabel);

        //if there is no data (all zeros for a pie chart) the chart will not display anything
        if (!ControllerUtils.isCollectionValued(getCountsForChart(recordMap)) && !chart.isShowNoDataGraph()) {
            //this will hide the title and message if there is no data
            data.setGraphTitle("");
            data.setNoDataMessage("");
        }

        // Create urls for each slice/bar
        DataSeriesSource dss = null;
        StringBuilder jsCall = new StringBuilder();
        jsCall.append("javascript:OE.report.datasource.showDetails({");

        if (dataSource instanceof DataSeriesSource) {
            dss = (DataSeriesSource) dataSource;

            Collection<Dimension> dims = new ArrayList<Dimension>(dss.getResultDimensions());
            Collection<String> dimIds = ControllerUtils.getDimensionIdsFromCollection(dims);

            Collection<Dimension> accums = new ArrayList<Dimension>(dss.getAccumulations());

            for (Dimension d : accums) {
                if (dimIds.contains(d.getId()) && d.getId().equals(accumulation.getId())) {

                } else {
                    dimIds.remove(d.getId());
                }
            }

            jsCall.append("dsId:'").append(dss.getClass().getName()).append("'");
            //specify results
            jsCall.append(",results:[").append(StringUtils.collectionToDelimitedString(dimIds, ",", "'", "'"))
                    .append(']');
            //specify accumId
            jsCall.append(",accumId:'").append(accumulation.getId()).append("'");

            addJavaScriptFilters(jsCall, filters, dimension.getId());
        }

        int rSize = recordMap.size();
        int aSize = 1;
        String[] lbl = new String[rSize];
        String[][] txtb = new String[1][rSize];
        double[][] bardat = new double[aSize][rSize];
        String[][] txtp = new String[rSize][1];
        double[][] piedat = new double[rSize][aSize];
        String[][] urlsP = new String[rSize][1];
        String[][] urlsB = new String[1][rSize];
        int i = 0;
        double totalCount = 0;
        DecimalFormat df = new DecimalFormat("#.##");

        for (String key : recordMap.keySet()) {
            if (recordMap.get(key) != null && recordMap.get(key).getCount() != null && !recordMap.get(key).getCount()
                    .isNaN()) {
                totalCount += recordMap.get(key).getCount();
            }
        }

        for (String key : recordMap.keySet()) {
            Double dubVal = recordMap.get(key).getCount();
            String strPercentVal = df.format(100 * dubVal / totalCount);
            lbl[i] = recordMap.get(key).getName();
            //create bar data set
            bardat[0][i] = dubVal;
            txtb[0][i] = lbl[i] + " - " + Double.toString(dubVal) + " (" + strPercentVal + "%)";
            if (lbl[i].length() > DEFAULT_LABEL_LENGTH) {
                lbl[i] = lbl[i].substring(0, DEFAULT_LABEL_LENGTH - 3) + "...";
            }
            //create pie data set
            piedat[i][0] = dubVal;
            txtp[i][0] = lbl[i] + " - " + Double.toString(dubVal) + " (" + strPercentVal + "%)";
            if (lbl[i].length() > DEFAULT_LABEL_LENGTH) {
                lbl[i] = lbl[i].substring(0, DEFAULT_LABEL_LENGTH - 3) + "...";
            }
            //TODO all "Others" to return details of all results except for those in recordMap.keyset
            //We need a "Not" filter
            if (!otherLabel.equals(key)) {
                if (dataSource instanceof DataSeriesSource) {
                    if (dimension.getId().equals(filterId) && possibleKeyValueMap != null) {
                        if (possibleKeyValueMap.containsKey(key)) {
                            urlsP[i][0] = jsCall.toString() + "," + filterId + ":'" + key + "'" + "});";
                            urlsB[0][i] = jsCall.toString() + "," + filterId + ":'" + key + "'" + "});";
                        } else {
                            urlsP[i][0] = jsCall.toString() + "});";
                            urlsB[0][i] = jsCall.toString() + "});";
                        }
                    } else {
                        if (key == null || key.equals("") || key
                                .equals(messageSource.getMessage("graph.dimension.null", "Empty Value"))) {
                            // TODO: This is when we have an ID field also marked as isResult:true and the value is null
                            // We can not provide url param filterId:null as field can be numeric and we get a java.lang.NumberFormatException...
                            urlsP[i][0] = jsCall.toString() + "});";
                            urlsB[0][i] = jsCall.toString() + "});";
                        } else {
                            urlsP[i][0] = jsCall.toString() + "," + filterId + ":'" + key + "'" + "});";
                            urlsB[0][i] = jsCall.toString() + "," + filterId + ":'" + key + "'" + "});";
                        }
                    }
                }
            }

            i++;
        }

        if (BAR.equalsIgnoreCase(chart.getType())) {
            data.setCounts(bardat);
            data.setXLabels(lbl);
            data.setMaxLabeledCategoryTicks(rSize);
            data.setAltTexts(txtb);
            if (jsCall.length() > 0) {
                data.setLineSetURLs(urlsB);
            }
            //TODO add encoding?
            graph = gc.createBarGraph(data, false, true);
        } else if (PIE.equalsIgnoreCase(chart.getType())) {
            data.setCounts(piedat);
            data.setLineSetLabels(lbl);
            data.setAltTexts(txtp);
            if (jsCall.length() > 0) {
                data.setLineSetURLs(urlsP);
            }
            graph = gc.createPieGraph(data, Encoding.PNG_WITH_TRANSPARENCY);
        }
        return graph;
    }

    private Collection<Double> getCountsForChart(LinkedHashMap<String, ChartData> recordMap) {
        Collection<Double> counts = new ArrayList<Double>();
        for (String id : recordMap.keySet()) {
            counts.add(recordMap.get(id).getCount());
        }

        return counts;
    }

    private LinkedHashMap<String, ChartData> getRecordMap(List<Record> records, String accumId, String dimensionId,
                                                          String filterId) {
        LinkedHashMap<String, ChartData> map = new LinkedHashMap<String, ChartData>(records.size());
        int rSize = records.size();
        for (int i = 0; i < rSize; i++) {
            Record record = records.get(i);
            Object accumValue = record.getValue(accumId);
            Object dimenValue = record.getValue(dimensionId);
            Object filterValue = record.getValue(filterId == null ? dimensionId : filterId);
            String dimenString = "";
            String filterString = "";
            if (dimenValue != null) {
                dimenString = String.valueOf(dimenValue);
            } else {
                dimenString = messageSource.getMessage("graph.dimension.null");
            }
            if (filterValue != null) {
                filterString = convertFilter(filterValue);
            } else {
                filterString = messageSource.getMessage("graph.dimension.null");
            }
            try {
                Double dubVal = Double.NaN;
                if (accumValue != null) {
                    dubVal = Double.valueOf(accumValue.toString());
                }
                map.put(filterString, new ChartData(filterString, dimenString, dubVal));
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return map;
    }

    @RequestMapping("/graphTimeSeries")
    public void graphTimeSeries(HttpServletRequest req, HttpServletResponse resp,
                                @RequestParam("graphDataId") String dataId,
                                @RequestParam(required = false) String graphTitle,
                                @RequestParam(required = false) String xAxisLabel,
                                // TODO put these all in a graph model object and let Spring deserialize from JSON
                                @RequestParam(required = false) String yAxisLabel,
                                @RequestParam(required = false) Double yAxisMin,
                                @RequestParam(required = false) Double yAxisMax,
                                @RequestParam(required = false) String dataDisplayKey,
                                @RequestParam(required = false) String getImageMap,
                                @RequestParam(required = false) String imageType,
                                @RequestParam(required = false) String resolution,
                                @RequestParam(required = false) String getHighResFile)
            throws GraphException, IOException {

        GraphDataSerializeToDiskHandler hndl = new GraphDataSerializeToDiskHandler(graphDir);
        GraphController gc = getGraphController(dataId, hndl, req.getUserPrincipal().getName());

        GraphDataInterface data = hndl.getGraphData(dataId);

        if (graphTitle != null) {
            data.setGraphTitle(graphTitle);
        }
        if (xAxisLabel != null) {
            data.setXAxisLabel(xAxisLabel);
        }
        if (yAxisLabel != null) {
            data.setYAxisLabel(yAxisLabel);
        }

        GraphObject graph = gc.createTimeSeriesGraph(data, yAxisMin, yAxisMax, dataDisplayKey);
        BufferedOutputStream out = new BufferedOutputStream(resp.getOutputStream());

        if (getImageMap != null && (getImageMap.equals("1") || getImageMap.equalsIgnoreCase("true"))) {
            resp.setContentType("text/plain;charset=utf-8");
            StringBuffer sb = new StringBuffer();
            sb.append(graph.getImageMap());
            out.write(sb.toString().getBytes());
        } else {
            resp.setContentType("image/png;charset=utf-8");
            String filename = graph.getImageFileName();
            filename = filename.replaceAll("\\s", "_");
            resp.setHeader("Content-disposition", "attachment; filename=" + filename);
            int imageResolution = 300;
            if (resolution != null) {
                try {
                    imageResolution = Integer.parseInt(resolution);
                    graph.writeChartAsHighResolutionPNG(out, data.getGraphWidth(), data.getGraphHeight(),
                                                        imageResolution);
                } catch (Exception e) {
                    log.error("", e);
                }
            } else {
                graph.writeChartAsPNG(out, data.getGraphWidth(), data.getGraphHeight());
            }
        }
    }

    @RequestMapping("/graphBar")
    public void graphBar(HttpServletRequest req, HttpServletResponse resp,
                         @RequestParam("graphDataId") String dataId,
                         @RequestParam(required = false) Integer resolution) throws GraphException, IOException {
        GraphDataSerializeToDiskHandler hndl = new GraphDataSerializeToDiskHandler(graphDir);
        GraphController gc = getGraphController(dataId, hndl, req.getUserPrincipal().getName());

        GraphDataInterface data = hndl.getGraphData(dataId);
        GraphObject graph = gc.createBarGraph(data, false);
        String filename = graph.getImageFileName();
        filename = filename.replaceAll("\\s", "_");
        resp.setContentType("image/png;charset=utf-8");
        resp.setHeader("Content-disposition", "attachment; filename=" + filename);

        OutputStream out = resp.getOutputStream();
        // why can't the graph module handle this?
        if (resolution == null) {
            graph.writeChartAsPNG(out, data.getGraphWidth(), data.getGraphHeight());
        } else {
            graph.writeChartAsHighResolutionPNG(out, data.getGraphWidth(), data.getGraphHeight(), resolution);
        }

    }

    @RequestMapping("/graphPie")
    public void graphPie(HttpServletRequest req, HttpServletResponse resp,
                         @RequestParam("graphDataId") String dataId,
                         @RequestParam(required = false) Integer resolution) throws GraphException, IOException {

        GraphDataSerializeToDiskHandler hndl = new GraphDataSerializeToDiskHandler(graphDir);
        GraphController gc = getGraphController(dataId, hndl, req.getUserPrincipal().getName());

        GraphDataInterface data = hndl.getGraphData(dataId);
        GraphObject graph = gc.createPieGraph(data);
        String filename = graph.getImageFileName();
        filename = filename.replaceAll("\\s", "_");
        resp.setContentType("image/png;charset=utf-8");
        resp.setHeader("Content-disposition", "attachment; filename=" + filename);

        OutputStream out = resp.getOutputStream();
        // why can't the graph module handle this?
        if (resolution == null) {
            graph.writeChartAsPNG(out, data.getGraphWidth(), data.getGraphHeight());
        } else {
            graph.writeChartAsHighResolutionPNG(out, data.getGraphWidth(), data.getGraphHeight(), resolution);
        }
    }

    @RequestMapping("/detailsQuery")
    public
    @ResponseBody
    DataSourceDetails detailsQuery(WebRequest request, @RequestParam("dsId") JdbcOeDataSource ds,
                                   @RequestParam(value = "firstrecord", defaultValue = "0") long firstRecord,
                                   @RequestParam(value = "pagesize", defaultValue = "200") long pageSize)
            throws ErrorMessageException, OeDataSourceException, OeDataSourceAccessException {

        List<Filter> filters = new Filters().getFilters(request.getParameterMap(), ds, null, 0, null, 0);
        List<Dimension> results = ControllerUtils.getResultDimensionsByIds(ds, request.getParameterValues("results"));
        List<Dimension>
                accumulations =
                ControllerUtils.getAccumulationsByIds(ds, request.getParameterValues("accumId"));

        final List<OrderByFilter> sorts = new ArrayList<OrderByFilter>();
        try {
            sorts.addAll(Sorters.getSorters(request.getParameterMap()));
        } catch (Exception e) {
            log.warn("Unable to get sorters, using default ordering");
        }

        String clientTimezone = null;
        String timezoneEnabledString = messageSource.getMessage(
                TIMEZONE_ENABLED, "false");
        if (timezoneEnabledString.equalsIgnoreCase("true")) {
            clientTimezone = ControllerUtils
                    .getRequestTimezoneAsHourMinuteString(request);
        }
        return new DetailsQuery().performDetailsQuery(ds, results, accumulations, filters, sorts, false,
                                                      clientTimezone,
                                                      firstRecord, pageSize, true);
    }

    @RequestMapping("/detailsPivot")
    public
    @ResponseBody
    DataSourceDetails detailsPivot(WebRequest request, @RequestParam("dsId") JdbcOeDataSource ds,
                                   @RequestParam(value = "pivotX") String pivotX,
                                   @RequestParam(value = "pivotY") String pivotY,
                                   @RequestParam(value = "firstrecord", defaultValue = "0") long firstRecord,
                                   @RequestParam(value = "pagesize", defaultValue = "200") long pageSize)
            throws ErrorMessageException, OeDataSourceException, OeDataSourceAccessException {

        // Do the normal details query using the pivots as dimensions
        DataSourceDetails details = this.detailsQuery(request, ds, firstRecord, pageSize);

        // Expand the sparse results into a full matrix
        Map<String, Map<String, Integer>> matrix = new LinkedHashMap<String, Map<String, Integer>>();
        matrix.put("Total", new LinkedHashMap<String, Integer>());
        // TODO: Colin: (Round 2 feature) Perform some check to disable the pivot button if the report doesn't have an ACCUM field, or if it only has 1 "DS" field
        // TODO: Colin: (Round 2 feature) Put this where the button gets created in a JS file.
        String accum = null;
        for (String key : details.getRows().get(0).keySet()) {
            if (!key.equals(pivotX) && !key.equals(pivotY)) {
                accum = key;
                break;
            }
        }

        for (Map<String, Object> row : details.getRows()) {
            // Add a new column to the matrix for the value of the pivotX dimension if needed
            // Otherwise, find the column for the value of the pivotX dimension
            Map<String, Integer> col;
            if (!matrix.containsKey(String.valueOf(row.get(pivotX)))) {
                col = (matrix.size() < 1)
                      ? new LinkedHashMap<String, Integer>()
                      : new LinkedHashMap<String, Integer>(matrix.get(matrix.keySet().iterator().next()));
                for (Map.Entry<String, Integer> e : col.entrySet()) {
                    col.put(e.getKey(), null);
                }
                matrix.put(String.valueOf(row.get(pivotX)), col);
            } else {
                col = matrix.get(String.valueOf(row.get(pivotX)));
            }

            // Make sure that every column in the matrix has a key for the value of the pivotY dimension
            // If not, set it to 0
            for (Map.Entry<String, Map<String, Integer>> e : matrix.entrySet()) {
                if (!e.getValue().containsKey(String.valueOf(row.get(pivotY)))) {
                    e.getValue().put(String.valueOf(row.get(pivotY)), null);
                }
            }

            // Set the pivotX column at pivotY as the value of the accumulation
            col.put(String.valueOf(row.get(pivotY)), (Integer) row.get(accum));

            // Add the accumulation value to the total matrix for bookkeeping
            Integer oldValue = matrix.get("Total").get(String.valueOf(row.get(pivotY)));
            oldValue = (oldValue != null) ? oldValue : 0;
            matrix.get("Total").put(String.valueOf(row.get(pivotY)), oldValue + (Integer) row.get(accum));
        }

        // Calculate totals for each column
        for (Map.Entry<String, Map<String, Integer>> row : matrix.entrySet()) {
            Integer total = 0;
            for (Map.Entry<String, Integer> col : row.getValue().entrySet()) {
                if (col.getValue() != null) {
                    total += col.getValue();
                }
            }
            row.getValue().put("Total", total);
        }

        // Move the total column to the end
        matrix.put("Total", matrix.remove("Total"));

        // Reformat the expanded data back into rows for the grid
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (String key : matrix.get(matrix.keySet().iterator().next()).keySet()) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put(pivotY, key);
            for (String col : matrix.keySet()) {
                row.put(col, matrix.get(col).get(key));
            }
            rows.add(row);
        }

        DataSourceDetails pivotDetails = new DataSourceDetails();
        pivotDetails.setRows(rows);
        pivotDetails.setTotalRecords(rows.size());
        return pivotDetails;
    }

    private int getCalWeekStartDay(Map<String, ResolutionHandler> resolutionHandlers) {
        ResolutionHandler handler = resolutionHandlers.get("weekly");
        int startDay;
        if (handler == null || !(handler instanceof PgSqlWeeklyHandler)) {
            switch (Integer.parseInt(
                    messageSource.getMessage("epidemiological.day.start", Integer.toString(DEFAULT_WEEK_STARTDAY)))) {
                case 0:
                    startDay = Calendar.SUNDAY;
                    break;
                case 2:
                    startDay = Calendar.TUESDAY;
                    break;
                case 3:
                    startDay = Calendar.WEDNESDAY;
                    break;
                case 4:
                    startDay = Calendar.THURSDAY;
                    break;
                case 5:
                    startDay = Calendar.FRIDAY;
                    break;
                case 6:
                    startDay = Calendar.SATURDAY;
                    break;
                default:
                    startDay = Calendar.MONDAY;
                    break;
            }
            return startDay;
        }
        return ((PgSqlWeeklyHandler) handler).getCalWeekStartDay();
    }

    private int getWeekStartDay(Map<String, ResolutionHandler> resolutionHandlers) {
        ResolutionHandler handler = resolutionHandlers.get("weekly");
        if (handler == null || !(handler instanceof PgSqlWeeklyHandler)) {
            return Integer.parseInt(
                    messageSource.getMessage("epidemiological.day.start", Integer.toString(DEFAULT_WEEK_STARTDAY)));
        }
        return ((PgSqlWeeklyHandler) handler).getWeekStartDay();
    }


    /**
     * Extracts AccumPoint from a Collection of <code>records</code> where
     *
     * @param principal, used for logging
     */
    private List<AccumPoint> extractAccumulationPoints(String principal, DataSeriesSource ds,
                                                       final Collection<Record> records,
                                                       Date startDate, Date endDate, List<Dimension> accumulations,
                                                       final GroupingImpl group,
                                                       Map<String, ResolutionHandler> resolutionHandlers) {
        log.info(LogStatements.TIME_SERIES.getLoggingStmt() + principal);
        int startDayCal = getCalWeekStartDay(resolutionHandlers);
        int startDay = getWeekStartDay(resolutionHandlers);

        String resolution = group.getResolution();
        final String groupId = group.getId();
        int zeroFillInterval = intervalMap.keySet().contains(resolution) ? intervalMap.get(resolution) : -1;

        GroupingDimension grpdim = ds.getGroupingDimension(group.getId());
        if (zeroFillInterval != -1 && (grpdim.getSqlType() == FieldType.DATE
                                       || grpdim.getSqlType() == FieldType.DATE_TIME)) {
            ArrayList<AccumPoint> fullVector = new ArrayList<AccumPoint>(records.size());
            //create zero points for each accumulation
            Map<String, Number> zeroes = new HashMap<String, Number>();
            for (Dimension accumulation : accumulations) {
                zeroes.put(accumulation.getId(), 0);
            }
            if (records.size() > 0) {
                final Calendar cal = new GregorianCalendar();
                cal.setTime(startDate);
                Iterator<Record> recordsIterator = records.iterator();
                Record currRecord = (recordsIterator.hasNext()) ? recordsIterator.next() : null;

                //currently iterates over data incrementing cal by the resolution (weekly, daily etc)
                for (int i = 1; !cal.getTime().after(endDate); i++) {
                    // if (DAILY.equalsIgnoreCase(resolution)) {
                    boolean addRecord = false;
                    if (currRecord != null) {
                        // 2013/03/25, SCC, GGF, There are some weird edge cases with selecting data ranges that span
                        // the EDT/EST cross-overs.  Sometimes the "filter" will have the "23:00" and the data will have
                        // "00:00".  So, since we only care about the "date" anyway, clear out any subordinate fields.

                        // Database record date (set hour, minute, second, millisec to 0)
                        final Calendar rowValue = Calendar.getInstance();
                        rowValue.setTime((Date) currRecord.getValue(groupId));
                        rowValue.set(Calendar.HOUR_OF_DAY, 0);
                        rowValue.set(Calendar.MINUTE, 0);
                        rowValue.set(Calendar.SECOND, 0);
                        rowValue.set(Calendar.MILLISECOND, 0);

                        // looping variable date (set hour, minute, second, millisec to 0)
                        final Calendar calValue = Calendar.getInstance();
                        calValue.setTime(cal.getTime());
                        calValue.set(Calendar.HOUR_OF_DAY, 0);
                        calValue.set(Calendar.MINUTE, 0);
                        calValue.set(Calendar.SECOND, 0);
                        calValue.set(Calendar.MILLISECOND, 0);

                        if (resolution.equalsIgnoreCase(DAILY) && rowValue.equals(calValue)) {
                            addRecord = true;
                        } else {
                            Calendar currRecCalendar = new GregorianCalendar();
                            currRecCalendar.setTime((Date) currRecord.getValue(groupId));
                            if (resolution.equalsIgnoreCase(WEEKLY)) {
                                if (PgSqlDateHelper.getYear(startDay, cal) == PgSqlDateHelper
                                        .getYear(startDay, currRecCalendar) &&
                                    PgSqlDateHelper.getWeekOfYear(startDay, cal) == PgSqlDateHelper
                                            .getWeekOfYear(startDay, currRecCalendar)) {
                                    addRecord = true;
                                }
                            } else if (resolution.equalsIgnoreCase(MONTHLY)) {
                                if (cal.get(Calendar.YEAR) == currRecCalendar.get(Calendar.YEAR) &&
                                    cal.get(Calendar.MONTH) == currRecCalendar.get(Calendar.MONTH)) {
                                    addRecord = true;
                                }
                            }
                        }
                        if (addRecord) {
                            //if the current record matches the date put it in
                            fullVector.add(createAccumulationPoint(currRecord, accumulations));
                            currRecord = (recordsIterator.hasNext()) ? recordsIterator.next() : null;
                        }
                    }
                    if (!addRecord) {
                        //add a zero fill
                        Map<String, Dimension> m = new HashMap<String, Dimension>();
                        m.put(groupId, ds.getResultDimension(groupId));
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put(groupId, cal.getTime());
                        Record r = new QueryRecord(m, map);
                        fullVector.add(new AccumPointImpl(zeroes, r));
                    }
                    if (resolution.equalsIgnoreCase(WEEKLY)) {
                        // add 7 days if current date falls on week start date
                        if (i != 1) {
                            cal.add(Calendar.WEEK_OF_YEAR, 1);
                        } else {
                            cal.add(Calendar.DAY_OF_YEAR, 1);
                            while (cal.get(Calendar.DAY_OF_WEEK) != startDayCal) {
                                cal.add(Calendar.DAY_OF_YEAR, 1);
                            }
                        }
                    } else {
                        // reset the date each time to account for +month oddness
                        cal.setTime(startDate);
                        // increment the interval
                        cal.add(zeroFillInterval, 1 * i);
                    }
                }

            }
            return fullVector;
        } else {
            //pretty sure this is raw non filled
            List<AccumPoint> rawVector = new ArrayList<AccumPoint>(records.size());
            for (Record record : records) {
                rawVector.add(createAccumulationPoint(record, accumulations));
            }
            return rawVector;
        }
    }

    private AccumPoint createAccumulationPoint(Record record, List<Dimension> accumulations) {
        Map<String, Number> values = new LinkedHashMap<String, Number>();
        for (Dimension a : accumulations) {
            Object value = record.getValue(a.getId());
            Number number = (Number) value;
            values.put(a.getId(), number);
        }
        AccumPoint accumPoint = new AccumPointImpl(values, record);
        return accumPoint;
    }

    /**
     * Takes a List of SeriesPoints and generates a double array that holds the value of each SeriesPoint
     *
     * @param seriespoints - List<AccumPoint> whose values need to be extracted into a double[] for detectors
     * @param dimId        - The dimension id to pull from each AccumPoint
     * @return pointarray - double[] that holds all the values from the passed in list of SeriesPoint
     */
    private double[] generateSeriesValues(List<AccumPoint> seriespoints, String dimId) {
        //List<Number> pointlist = new ArrayList<Number>();
        double[] pointarray = new double[seriespoints.size()];
        int i = 0;
        for (AccumPoint point : seriespoints) {
            //pointlist.add(point.getValue());
            if (point != null && point.getValue(dimId) != null) {
                pointarray[i] = point.getValue(dimId).doubleValue();
            } else {
                pointarray[i] = Double.NaN;
            }
            i++;
        }
        return pointarray;
    }

    /**
     * Takes a List of AccumPoints and generates a double array that holds the total of accumulations
     *
     * @param points     - List<AccumPoint> whose values need to be extracted into a double[] for detectors
     * @param dimensions - The list of dimensions to sum from each AccumPoint
     * @return double[] that holds all the values from the passed in list of SeriesPoint
     */
    private double[] totalSeriesValues(List<AccumPoint> points, List<Dimension> dimensions) {
        double[] totalArray = new double[points.size()];
        int i = 0;
        for (AccumPoint point : points) {
            if (point != null) {
                for (Dimension dim : dimensions) {
                    Number value = point.getValue(dim.getId());
                    if (value != null) {
                        totalArray[i] = totalArray[i] + value.doubleValue();
                    }
                }
            } else {
                totalArray[i] = Double.NaN;
            }
            i++;
        }
        return totalArray;
    }

    /**
     * Returns a File Download Dialog for a file containing information in the data details grid.
     *
     * @param request  the request contains needed parameters: the 'results' headers that appear in the grid
     * @param response the response object for this request
     */
    @RequestMapping("/exportGridToFile")
    public void exportGridToFile(@RequestParam("dsId") JdbcOeDataSource ds,
                                 ServletWebRequest request, HttpServletResponse response)
            throws ErrorMessageException, IOException {

        TimeZone timezone = ControllerUtils.getRequestTimezone(request);
        response.setContentType("application/json;charset=utf-8");

        final List<Filter> filters = new Filters().getFilters(request.getParameterMap(), ds, null, 0, null, 0);
        final List<Dimension> results =
                ControllerUtils.getResultDimensionsByIds(ds, request.getParameterValues("results"));

        final List<String> columnHeaders = new ArrayList<String>();
        for (final Dimension result : results) {
            if (result.getDisplayName() != null) {
                columnHeaders.add(result.getDisplayName());
            } else {
                columnHeaders.add(messageSource.getDataSourceMessage(result.getId(), ds));
            }
        }

        final List<Dimension> accumulations =
                ControllerUtils.getAccumulationsByIds(ds, request.getParameterValues("accumId"));

        final List<OrderByFilter> sorts = new ArrayList<OrderByFilter>();
        try {
            sorts.addAll(Sorters.getSorters(request.getParameterMap()));
        } catch (Exception e) {
            log.warn("Unable to get sorters, using default ordering");
        }

        String clientTimezone = null;
        String timezoneEnabledString = messageSource.getMessage(TIMEZONE_ENABLED, "false");
        if (timezoneEnabledString.equalsIgnoreCase("true")) {
            clientTimezone = ControllerUtils.getRequestTimezoneAsHourMinuteString(request);
        }
        Collection<Record> points =
                new DetailsQuery().performDetailsQuery(ds, results, accumulations, filters, sorts, false,
                                                       clientTimezone);
        response.setContentType("text/csv;charset=utf-8");

        String filename =
                messageSource.getDataSourceMessage("panel.details.export.file", ds) + "-" + new DateTime()
                        .toString("yyyyMMdd'T'HHmmss") + ".csv";
        response.setHeader("Content-disposition", "attachment; filename=" + filename);

        // Cache-Control = cache and Pragma = cache enable IE to download files over SSL.
        response.setHeader("Cache-Control", "cache");
        response.setHeader("Pragma", "cache");
        FileExportUtil.exportGridToCSV(response.getWriter(), columnHeaders.toArray(new String[columnHeaders.size()]),
                                       points, timezone);
    }

    private String appendUrlParameter(String url, String param, String value) {
        StringBuilder sb = new StringBuilder(url);
        if (url.contains("?")) {
            sb.append('&');
        } else {
            sb.append('?');
        }

        URLCodec codec = new URLCodec();

        try {
            sb.append(codec.encode(param));
        } catch (EncoderException e) {
            log.error("Exception encoding URL param " + param, e);
        }

        try {
            sb.append('=').append(codec.encode(value));
        } catch (EncoderException e) {
            log.error("Exception encoding URL value " + value, e);
        }

        return sb.toString();
    }

    /**
     * If the graph.font property is specified, append its value as a parameter to the given URL. Otherwise, do
     * nothing.
     *
     * @return new URL
     */
    private String appendGraphFontParam(JdbcOeDataSource dataSource, String url) {
        try {
            String graphFont = messageSource.getDataSourceMessage("graph.font", dataSource);
            return appendUrlParameter(url, "font", graphFont);
        } catch (NoSuchMessageException e) {
            log.debug("Property graph.font not found, using default");
            return url;
        }
    }

    /**
     * Get a new GraphContoller instance with sane metadata
     */
    private GraphController getGraphController(String graphDataId, GraphDataHandlerInterface graphDataHandler,
                                               String userId) {
        final String graphFont = messageSource.getMessage("graph.font", "Arial");

        GraphController graphController = new GraphController(graphDataId, graphDataHandler, userId) {

            // TODO graph module needs to be totally rewritten
            private void setGraphMetaData(Map<String, Object> graphMetaData) {
                graphMetaData.put(GraphSource.GRAPH_FONT, new Font(graphFont, Font.BOLD, 14));
                graphMetaData.put(GraphSource.GRAPH_Y_AXIS_FONT, new Font(graphFont, Font.BOLD, 12));
                graphMetaData.put(GraphSource.GRAPH_Y_AXIS_LABEL_FONT, new Font(graphFont, Font.BOLD, 12));
                graphMetaData.put(GraphSource.GRAPH_X_AXIS_FONT, new Font(graphFont, Font.PLAIN, 11));
                graphMetaData.put(GraphSource.GRAPH_X_AXIS_LABEL_FONT, new Font(graphFont, Font.PLAIN, 12));
                graphMetaData.put(GraphSource.LEGEND_FONT, new Font(graphFont, Font.PLAIN, 12));
            }

            @Override
            public void setTimeSeriesGraphMetaData(GraphDataInterface graphData, Double yAxisMin, Double yAxisMax,
                                                   double maxCount, Map<String, Object> graphMetaData) {

                super.setTimeSeriesGraphMetaData(graphData, yAxisMin, yAxisMax, maxCount, graphMetaData);
                setGraphMetaData(graphMetaData);
            }

            @Override
            public void setBarGraphMetaData(GraphDataInterface graphData, boolean stackGraph,
                                            Map<String, Object> graphMetaData) {

                super.setBarGraphMetaData(graphData, stackGraph, graphMetaData);
                setGraphMetaData(graphMetaData);
            }

            @Override
            public void setPieGraphMetaData(GraphDataInterface graphData, Map<String, Object> graphMetaData,
                                            List<PointInterface> points) {
                super.setPieGraphMetaData(graphData, graphMetaData, points);
                setGraphMetaData(graphMetaData);
            }
        };
        Map<String, String> translationMap = graphController.getTranslationMap();
        translationMap.put("Normal", messageSource.getMessage("graph.normal"));
        translationMap.put("Warning", messageSource.getMessage("graph.warning"));
        translationMap.put("Alert", messageSource.getMessage("graph.alert"));
        graphController.setTranslationMap(translationMap);

        return graphController;
    }

    /**
     * Used to add all of the given filters to the JavaScript callback.  This is intended to fix the issue when the user
     * selects more than 1 item in a multi-select filter box.  It will also handle converting dates into their numeric
     * format for later parsing on the backend.
     *
     * @param javaScript   The string builder that contains the JavaScript callback information.
     * @param filters      The filters to add.
     * @param ignoredField Sometimes, we want to ignore adding a certain filter to the callback.
     */
    private static void addJavaScriptFilters(final StringBuilder javaScript, final Collection<Filter> filters,
                                             final String ignoredField) {
        if ((filters != null) && (!filters.isEmpty())) {
            for (final Filter filter : filters) {
                if ((filter != null) && (filter instanceof FieldFilter)) {
                    final FieldFilter fieldFilter = (FieldFilter) filter;
                    final String id = fieldFilter.getFilterId();
                    final List<Object> arguments = fieldFilter.getArguments();

                    if ((id != null) && (!id.equals(ignoredField)) && (arguments != null) && (!arguments.isEmpty())) {
                        if (arguments.size() == 1) {
                            final Object value = arguments.get(0);
                            if (fieldFilter instanceof LteqFilter) {
                                javaScript.append(",").append(id).append("_end:'").append(convertFilter(value))
                                        .append("'");
                            } else if (fieldFilter instanceof GteqFilter) {
                                javaScript.append(",").append(id).append("_start:'").append(convertFilter(value))
                                        .append("'");
                            } else {
                                javaScript.append(",").append(id).append(":'").append(convertFilter(value)).append("'");
                            }
                        } else {
                            javaScript.append(",").append(id).append(":[");
                            for (int loopIndex = 0; loopIndex < arguments.size(); loopIndex++) {
                                javaScript.append(loopIndex == 0 ? "" : ",").append("'")
                                        .append(convertFilter(arguments.get(loopIndex))).append("'");
                            }
                            javaScript.append("]");
                        }
                    }
                }
            }
        }
    }

    /**
     * Used to convert the given dimension value into a Javascript-safe function call value.
     *
     * <p> In the event that the object is a <code>java.util.Date</code>, the numeric will be returned.
     *
     * <p> If <code>null</code> is given, then an empty string is returned.
     *
     * @param value The dimension value to be converted.
     * @return The safe string representation of the given value.
     */
    private static String convertFilter(final Object value) {
        // http://en.wikipedia.org/wiki/Percent-encoding
        //     All Reserved Chars:    ! # $ & ' ( ) * + , / : ; = ? @ [ ]
        //     Others Handled:        % < > ` ~ ^ | { } . - " \ _

        // If a literal _ or % (the single char and variable char wild card symbols in PostgreSQL)
        // are desired in the filter criteria, then they need to be backslash escaped by the user.

        // Yes, some need lots of backslashes due to various levels of decoding between PostgreSQL, Java, Javascript

        if (value == null) {
            return "";
        } else if (value instanceof Date) {
            return String.valueOf(((Date) value).getTime());
        } else {
            return String.valueOf(value)
                    .replaceAll("%", "%25") // To fix query filters: %fever%
                    .replaceAll("\\\\", "\\\\\\\\") // To fix chart groupings: This is a sad face :\
                    .replaceAll("'", "\\\\'") // To fix chart groupings: Prince George's
                    .replaceAll("\"", "&quot;") // To fix chart groupings: Bob said "his quote".
                    .replaceAll(" ", "%20");
        }
    }
}
