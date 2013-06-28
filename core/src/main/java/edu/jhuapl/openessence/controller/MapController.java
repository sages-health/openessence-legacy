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

import edu.jhuapl.openessence.config.MapConfig;
import edu.jhuapl.openessence.datasource.Dimension;
import edu.jhuapl.openessence.datasource.Filter;
import edu.jhuapl.openessence.datasource.QueryManipulationStore;
import edu.jhuapl.openessence.datasource.Record;
import edu.jhuapl.openessence.datasource.entry.OeDataEntrySource;
import edu.jhuapl.openessence.datasource.jdbc.JdbcOeDataSource;
import edu.jhuapl.openessence.datasource.jdbc.filter.sorting.OrderByFilter;
import edu.jhuapl.openessence.datasource.map.MapMetaData;
import edu.jhuapl.openessence.datasource.map.WMSLayer;
import edu.jhuapl.openessence.model.MapData;
import edu.jhuapl.openessence.web.util.ControllerUtils;
import edu.jhuapl.openessence.web.util.ErrorMessageException;
import edu.jhuapl.openessence.web.util.Filters;
import edu.jhuapl.openessence.web.util.MapQueryUtil;
import edu.jhuapl.openessence.web.util.Sorters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;

@Controller
@RequestMapping("/map")
public class MapController extends OeController {

    private static final Logger log = LoggerFactory.getLogger(MapController.class);

    @Inject
    private MapQueryUtil mapQueryUtil;

    @Resource
    private Map<String, JdbcOeDataSource> dataSources;

    @Resource
    private String postgresCleanup;

    @Inject
    private MapConfig mapConfig;

    @RequestMapping("/metadata")
    public
    @ResponseBody
    Map<String, String> getMetadata() {
        Map<String, String> sysMapProps = new LinkedHashMap<String, String>();
        sysMapProps.put("wmsserver", mapConfig.wmsServer());
        sysMapProps.put("wfsserver", mapConfig.wfsServer());
        return sysMapProps;
    }

    /**
     * Populates map data...
     */
    @RequestMapping("/mapData")
    public
    @ResponseBody
    MapData mapData(@RequestParam("dsId") JdbcOeDataSource ds,
                    @RequestParam(required = false) String[] results, @RequestParam(required = false) String[] accumId,
                    NativeWebRequest request) throws ErrorMessageException, IOException {

        MapData mapData = new MapData();

        List<Filter> filters = new Filters().getFilters(request.getParameterMap(), ds, null, 0, null, 0);
        List<Dimension> resultsList = ControllerUtils.getResultDimensionsByIds(ds, results);

        List<Dimension> accumulations = ControllerUtils.getAccumulationsByIds(ds, accumId);
        List<OrderByFilter> sorts = new ArrayList<OrderByFilter>();
        try {
            sorts.addAll(Sorters.getSorters(request.getParameterMap()));
        } catch (Exception e) {
            log.warn("Unable to get sorters, using default ordering");
        }

        mapData.setSystemMapProperties(getMetadata());

        // Add map layer information to response
        Map<String, Object> meta = ds.getMetaData();
        MapMetaData mapInfo = (MapMetaData) meta.get("mapInfo");

        WMSLayer[] overlays = mapInfo.getOverlays();
        if (overlays == null) {
            log.info("No map overlays defined");
            overlays = new WMSLayer[0];
        }

        for (final WMSLayer layer : overlays) {
            final String oeDataSourceName = layer.getDataDSName();
            if (oeDataSourceName != null) {
                final OeDataEntrySource mapLayerDataEntrySource = (OeDataEntrySource) dataSources.get(oeDataSourceName);
                if (mapLayerDataEntrySource == null) {
                    throw new IllegalStateException("No data source named " + oeDataSourceName);
                }

                final Map<String, String> fieldMap = layer.getDataFieldMap();

                final Object current_time;
                final int nextSequenceValue;
                String sequenceForMapRequestId = mapLayerDataEntrySource.getParentTableDetails()
                        .getSequenceForMapRequestId();

                current_time = mapQueryUtil.performCurrentTimestampQuery();
                nextSequenceValue = mapQueryUtil.performNextSequenceValueQuery(sequenceForMapRequestId);

                final String requestIdDimension = mapLayerDataEntrySource.getParentTableDetails().getMapRequestId();
                final String requestIdSQLColumn = mapLayerDataEntrySource.getEditDimension(requestIdDimension)
                        .getSqlCol();
                layer.setCqlFilter(requestIdSQLColumn + "=" + nextSequenceValue);

                // Cleanup...
                int success = mapQueryUtil.performDelete(mapLayerDataEntrySource, current_time, postgresCleanup);
                log.debug("Delete outcome: " + success);

                // Build insert
                final Collection<Record> detailsQuery = ds.detailsQuery(
                        new QueryManipulationStore(resultsList, accumulations, filters, sorts, false,
                                                   ControllerUtils.getRequestTimezoneAsHourMinuteString(request)));
                for (final Record record : detailsQuery) {
                    final LinkedHashMap<String, Object> updateMap = new LinkedHashMap<String, Object>();
                    final Collection<String> placeholders = new ArrayList<String>();

                    // Iterate records using destination/edit dimensions (minimal set)
                    for (final Dimension dimension : mapLayerDataEntrySource.getEditDimensions()) {
                        // Check fieldMapping for dimension, use mapped id
                        final String dimensionId = (fieldMap == null
                                                    || fieldMap.get(dimension.getId()) == null
                                                    ? dimension.getId() : fieldMap.get(dimension.getId()));

                        if (ds.getResultDimension(dimensionId) != null) {
                            updateMap.put(dimension.getSqlCol(), record.getValue(dimensionId));
                            placeholders.add("?");
                        } else {
                            log.debug(String.format("No matching result dimension for %s. (%s)", dimensionId,
                                                    dimension.getId()));
                        }
                    }

                    // Add request id and time
                    updateMap.put(mapLayerDataEntrySource.getEditDimension(
                            mapLayerDataEntrySource.getParentTableDetails().getMapRequestId()).getSqlCol(),
                                  nextSequenceValue);
                    placeholders.add("?");

                    updateMap.put("time_requested", current_time);
                    placeholders.add("?");

                    int updateSuccess = mapQueryUtil.performUpdate(mapLayerDataEntrySource, updateMap, placeholders);
                    log.debug("INSERT OUTCOME: " + updateSuccess);
                }

            }
        }

        // Add map data
        mapData.setDsMapData(mapInfo);

        return mapData;
    }

}
