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

import edu.jhuapl.bsp.detector.temporal.epa.NoDetectorDetector;


public class TimeSeriesModel { // TODO refactor with ChartModel

    private String timeseriesTitle;
    private String timeseriesDetectorClass;
    private boolean includeDetails;
    private boolean graphExpectedValues;
    private String[] graphBaseColors;
    private boolean displayIntervalEndDate;
    private String timeseriesGroupResolution;
    private int width;
    private int height;
    private String[] accumId;
    private String[] timeseriesDenominator;
    private int prepull;
    private String xAxisLabel;
    private String yAxisLabel;

    public TimeSeriesModel() {
        timeseriesTitle = "";
        timeseriesDetectorClass = NoDetectorDetector.class.getName();
        includeDetails = true;
        displayIntervalEndDate = false;
        width = 900;
        height = 425;
        prepull = -1;
    }

    public String getTimeseriesTitle() {
        return timeseriesTitle;
    }

    public void setTimeseriesTitle(String timeseriesTitle) {
        this.timeseriesTitle = timeseriesTitle;
    }

    public String getTimeseriesDetectorClass() {
        return timeseriesDetectorClass;
    }

    public void setTimeseriesDetectorClass(String timeseriesDetectorClass) {
        this.timeseriesDetectorClass = timeseriesDetectorClass;
    }

    public boolean isIncludeDetails() {
        return includeDetails;
    }

    public void setIncludeDetails(boolean includeDetails) {
        this.includeDetails = includeDetails;
    }

    public String[] getGraphBaseColors() {
        return graphBaseColors;
    }

    public void setGraphBaseColors(String[] graphBaseColors) {
        this.graphBaseColors = graphBaseColors;
    }

    public boolean isDisplayIntervalEndDate() {
        return displayIntervalEndDate;
    }

    public void setDisplayIntervalEndDate(boolean displayIntervalEndDate) {
        this.displayIntervalEndDate = displayIntervalEndDate;
    }

    public String getTimeseriesGroupResolution() {
        return timeseriesGroupResolution;
    }

    public void setTimeseriesGroupResolution(String timeseriesGroupResolution) {
        this.timeseriesGroupResolution = timeseriesGroupResolution;
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

    public String[] getAccumId() {
        return accumId;
    }

    public void setAccumId(String[] accumId) {
        this.accumId = accumId;
    }

    public String[] getTimeseriesDenominator() {
        return timeseriesDenominator;
    }

    public void setTimeseriesDenominator(String[] timeseriesDenominator) {
        this.timeseriesDenominator = timeseriesDenominator;
    }

    public int getPrepull() {
        return prepull;
    }

    public void setPrepull(int prepull) {
        this.prepull = prepull;
    }

    public boolean isGraphExpectedValues() {
        return graphExpectedValues;
    }

    public void setGraphExpectedValues(boolean graphExpectedValues) {
        this.graphExpectedValues = graphExpectedValues;
    }

    public String getXAxisLabel() {
        return xAxisLabel;
    }

    public void setXAxisLabel(String xAxisLabel) {
        this.xAxisLabel = xAxisLabel;
    }

    public String getYAxisLabel() {
        return yAxisLabel;
    }

    public void setYAxisLabel(String yAxisLabel) {
        this.yAxisLabel = yAxisLabel;
    }
}
