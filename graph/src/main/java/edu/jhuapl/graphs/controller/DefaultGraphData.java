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

package edu.jhuapl.graphs.controller;

import java.awt.*;
import java.io.Serializable;

public class DefaultGraphData implements GraphDataInterface, Serializable {

    private static final long serialVersionUID = 4970636346513310253L;

    private double[][] counts = null;
    private int[][] colors = null;
    private String[][] altTexts = null;
    private String[][] lineSetURLs = null;
    private String lineSetURLTarget = null;
    private String[] xLabels = null;
    private String[] lineSetLabels = null;
    private boolean[] displayAlerts = null;
    private boolean[] displaySeverityAlerts = null;
    private double[] lineSymbolSizes = null;
    private String graphTitle = null;
    private String graphNoDataMessage = null;
    private String xAxisLabel = null;
    private String yAxisLabel = null;
    private int graphWidth = 400;
    private int graphHeight = 250;
    private Color[] graphBaseColors = {new Color(0, 0, 205), // medium blue
                                       new Color(0, 206, 209), // dark turquoise
                                       new Color(0, 128, 0), // green
                                       new Color(0, 255, 127), // spring green
                                       new Color(255, 215, 0), // gold
                                       new Color(255, 0, 255), // magenta
                                       new Color(244, 164, 96), // sandy brown
                                       new Color(220, 20, 60), // crimson
                                       new Color(240, 128, 128), // light coral
                                       new Color(238, 130, 238), // violet
                                       new Color(70, 130, 180), // steel blue
                                       new Color(199, 21, 133), // medium violet
                                       // red
                                       new Color(128, 0, 128), // purple
                                       new Color(255, 69, 0), // orange red
                                       new Color(75, 0, 130) // indigo;
    };
    private boolean showLegend = true;
    private boolean showGraphLabels = true;
    private boolean percentBased = false;
    private boolean plotHorizontal = false;
    private int maxLabeledCategoryTicks = 12;

    private double[][] levels = null;
    private double[][] expecteds = null;
    private Double yAxisMin = null;
    private Double yAxisMax = null;
    private String dataDisplayKey = null;
    private boolean showSingleAlertLegends = false;
    private boolean showSingleSeverityLegend = false;
    private Color backgroundColor = Color.WHITE;
    private Color labelBackgroundColor = new Color(255, 255, 255, 0);  //default to transparent label color
    private Font titleFont = new Font("Arial", Font.BOLD, 14);

    @Override
    public double[][] getCounts() {
        return counts;
    }

    @Override
    public void setCounts(double[][] counts) {
        this.counts = counts;
    }

    @Override
    public int[][] getColors() {
        return colors;
    }

    @Override
    public void setColors(int[][] colors) {
        this.colors = colors;
    }

    @Override
    public String[][] getAltTexts() {
        return altTexts;
    }

    @Override
    public void setAltTexts(String[][] altTexts) {
        this.altTexts = altTexts;
    }

    @Override
    public String[][] getLineSetURLs() {
        return lineSetURLs;
    }

    @Override
    public void setLineSetURLs(String[][] lineSetURLs) {
        this.lineSetURLs = lineSetURLs;
    }

    @Override
    public String getLineSetURLTarget() {
        return lineSetURLTarget;
    }

    @Override
    public void setLineSetURLTarget(String lineSetURLTarget) {
        this.lineSetURLTarget = lineSetURLTarget;
    }

    @Override
    public String[] getXLabels() {
        return xLabels;
    }

    @Override
    public void setXLabels(String[] xLabels) {
        this.xLabels = xLabels;
    }

    @Override
    public String[] getLineSetLabels() {
        return lineSetLabels;
    }

    @Override
    public void setLineSetLabels(String[] lineSetLabels) {
        this.lineSetLabels = lineSetLabels;
    }

    @Override
    public boolean[] displayAlerts() {
        return displayAlerts;
    }

    @Override
    public void setDisplayAlerts(boolean[] displayAlerts) {
        this.displayAlerts = displayAlerts;
    }

    @Override
    public boolean[] displaySeverityAlerts() {
        return displaySeverityAlerts;
    }

    @Override
    public void setDisplaySeverityAlerts(boolean[] displaySeverityAlerts) {
        this.displaySeverityAlerts = displaySeverityAlerts;
    }

    @Override
    public double[] getLineSymbolSizes() {
        return lineSymbolSizes;
    }

    @Override
    public void setLineSymbolSizes(double[] lineSymbolSizes) {
        this.lineSymbolSizes = lineSymbolSizes;
    }

    @Override
    public String getGraphTitle() {
        return graphTitle;
    }

    @Override
    public void setGraphTitle(String graphTitle) {
        this.graphTitle = graphTitle;
    }

    @Override
    public String getXAxisLabel() {
        return xAxisLabel;
    }

    @Override
    public void setXAxisLabel(String xAxisLabel) {
        this.xAxisLabel = xAxisLabel;
    }

    @Override
    public String getYAxisLabel() {
        return yAxisLabel;
    }

    @Override
    public void setYAxisLabel(String yAxisLabel) {
        this.yAxisLabel = yAxisLabel;
    }

    @Override
    public int getGraphWidth() {
        return graphWidth;
    }

    @Override
    public void setGraphWidth(int graphWidth) {
        this.graphWidth = graphWidth;
    }

    @Override
    public int getGraphHeight() {
        return graphHeight;
    }

    @Override
    public void setGraphHeight(int graphHeight) {
        this.graphHeight = graphHeight;
    }

    @Override
    public Color[] getGraphBaseColors() {
        return graphBaseColors;
    }

    @Override
    public void setGraphBaseColors(Color[] graphBaseColors) {
        this.graphBaseColors = graphBaseColors;
    }

    @Override
    public boolean showLegend() {
        return showLegend;
    }

    @Override
    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
    }

    @Override
    public boolean percentBased() {
        return percentBased;
    }

    @Override
    public void setPercentBased(boolean percentBased) {
        this.percentBased = percentBased;
    }

    @Override
    public boolean plotHorizontal() {
        return plotHorizontal;
    }

    @Override
    public void setPlotHorizontal(boolean plotHorizontal) {
        this.plotHorizontal = plotHorizontal;
    }

    @Override
    public int getMaxLabeledCategoryTicks() {
        return maxLabeledCategoryTicks;
    }

    @Override
    public void setMaxLabeledCategoryTicks(int maxLabeledCategoryTicks) {
        this.maxLabeledCategoryTicks = maxLabeledCategoryTicks;
    }

    public double[][] getExpecteds() {
        return expecteds;
    }

    public void setExpecteds(double[][] expecteds) {
        this.expecteds = expecteds;
    }

    public double[][] getLevels() {
        return levels;
    }

    public void setLevels(double[][] levels) {
        this.levels = levels;
    }

    public Double getYAxisMin() {
        return yAxisMin;
    }

    public void setYAxisMin(Double yAxisMin) {
        this.yAxisMin = yAxisMin;
    }

    public Double getYAxisMax() {
        return yAxisMax;
    }

    public void setYAxisMax(Double yAxisMax) {
        this.yAxisMax = yAxisMax;
    }

    public String getDataDisplayKey() {
        return dataDisplayKey;
    }

    public void setDataDisplayKey(String dataDisplayKey) {
        this.dataDisplayKey = dataDisplayKey;
    }

    @Override
    public boolean getShowSingleAlertLegend() {

        return this.showSingleAlertLegends;
    }

    @Override
    public boolean getShowSingleSeverityLegend() {
        // TODO Auto-generated method stub
        return this.showSingleSeverityLegend;
    }

    @Override
    public void setShowSingleAlertLegends(boolean showSingleAlertLegends) {
        this.showSingleAlertLegends = showSingleAlertLegends;
    }

    @Override
    public void setShowSingleSeverityLegends(boolean showSingleSeverityLegend) {
        this.showSingleSeverityLegend = showSingleSeverityLegend;

    }

    @Override
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public Color getLabelBackgroundColor() {
        return this.labelBackgroundColor;
    }

    @Override
    public void setLabelBackgroundColor(Color labelBackgroundColor) {
        this.labelBackgroundColor = labelBackgroundColor;
    }

    @Override
    public boolean getShowGraphLabels() {
        return this.showGraphLabels;
    }

    @Override
    public void setShowGraphLabels(boolean show) {
        this.showGraphLabels = show;
    }

    @Override
    public String getNoDataMessage() {
        return this.graphNoDataMessage;
    }

    @Override
    public void setNoDataMessage(String message) {
        this.graphNoDataMessage = message;
    }

    @Override
    public void setTitleFont(Font titleFont) {
        this.titleFont = titleFont;
    }

    @Override
    public Font getTitleFont() {
        return titleFont;
    }
}
