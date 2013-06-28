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

package edu.jhuapl.openessence.datasource.map;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import java.util.Map;

// Geoserver complains if we pass null cql_filters
@JsonSerialize(include = Inclusion.NON_NULL)
public class WMSLayer {

    private String name;
    private GetMapQuery getMapQuery;
    private Map<String, Object> layerOptions;
    private String accumulationId;

    private String dataDSName;
    private Map<String, String> dataFieldMap;
    private String grouping;

    /**
     * CQL filter, populated via map controller
     */
    private String cql_filter;

    private Threshold[] thresholds;

    public WMSLayer() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GetMapQuery getGetMapQuery() {
        return getMapQuery;
    }

    public void setGetMapQuery(GetMapQuery getMapQuery) {
        this.getMapQuery = getMapQuery;
    }

    public Map<String, Object> getLayerOptions() {
        return layerOptions;
    }

    public void setLayerOptions(Map<String, Object> layerOptions) {
        this.layerOptions = layerOptions;
    }

    public String getAccumulationId() {
        return accumulationId;
    }

    public void setAccumulationId(String accumulationId) {
        this.accumulationId = accumulationId;
    }

    public String getDataDSName() {
        return dataDSName;
    }

    public void setDataDSName(String dataDSName) {
        this.dataDSName = dataDSName;
    }

    public void setDataFieldMap(Map<String, String> dataFieldMap) {
        this.dataFieldMap = dataFieldMap;
    }

    public Map<String, String> getDataFieldMap() {
        return dataFieldMap;
    }

    public String getGrouping() {
        return grouping;
    }

    public void setGrouping(String grouping) {
        this.grouping = grouping;
    }

    public void setThresholds(Threshold[] thresholds) {
        this.thresholds = thresholds;
    }

    public Threshold[] getThresholds() {
        return thresholds;
    }

    public void setCqlFilter(String cqlFilter) {
        this.cql_filter = cqlFilter;
    }

    @JsonProperty("cql_filter")
    public String getCqlFilter() {
        return cql_filter;
    }
}
