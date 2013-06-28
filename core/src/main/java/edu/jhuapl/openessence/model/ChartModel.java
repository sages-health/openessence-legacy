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

package edu.jhuapl.openessence.model;

import edu.jhuapl.openessence.i18n.InspectableResourceBundleMessageSource;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class ChartModel {

    private String type;
    private int width;
    private int height;
    private String title;
    private boolean legend;
    private boolean showGraphLabels;
    private boolean plotHorizontal;
    private String noDataMessage;
    private boolean showNoDataGraph;
    private String[] graphBaseColors;
    private int categoryLimit;

    private String imageMap;
    private String imageMapName;
    private String imageUrl;

    @Autowired
    private InspectableResourceBundleMessageSource messageSource;

    public ChartModel() {
        title = "";
        categoryLimit = -1; // default to no limit
    }

    /**
     * Initialize properties that require injection.
     */
    @PostConstruct
    public void init() {
        noDataMessage = messageSource.getMessage("graph.charts.nodata");
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public InspectableResourceBundleMessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(
            InspectableResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isLegend() {
        return legend;
    }

    public void setLegend(boolean showLegend) {
        this.legend = showLegend;
    }

    public boolean isShowGraphLabels() {
        return showGraphLabels;
    }

    public void setShowGraphLabels(boolean showGraphLabels) {
        this.showGraphLabels = showGraphLabels;
    }

    public boolean isPlotHorizontal() {
        return plotHorizontal;
    }

    public void setPlotHorizontal(boolean plotHorizontal) {
        this.plotHorizontal = plotHorizontal;
    }

    public String getNoDataMessage() {
        return noDataMessage;
    }

    public void setNoDataMessage(String noDataMessage) {
        this.noDataMessage = noDataMessage;
    }

    public boolean isShowNoDataGraph() {
        return showNoDataGraph;
    }

    public void setShowNoDataGraph(boolean showNoDataGraph) {
        this.showNoDataGraph = showNoDataGraph;
    }

    public String[] getGraphBaseColors() {
        return graphBaseColors;
    }

    public void setGraphBaseColors(String[] graphBaseColors) {
        this.graphBaseColors = graphBaseColors;
    }

    public int getCategoryLimit() {
        return categoryLimit;
    }

    public void setCategoryLimit(int categoryLimit) {
        this.categoryLimit = categoryLimit;
    }

    public String getImageMap() {
        return imageMap;
    }

    public void setImageMap(String imageMap) {
        this.imageMap = imageMap;
    }

    public String getImageMapName() {
        return imageMapName;
    }

    public void setImageMapName(String imageMapName) {
        this.imageMapName = imageMapName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
