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

public interface GraphDataInterface {

    /**
     * @return double[a][b] of y-coordinate values to be plotted on the graph (a is the number of data series and b is
     *         the number of data points in that data series)
     */
    public double[][] getCounts();

    public void setCounts(double[][] counts);

    public double[][] getExpecteds();

    public void setExpecteds(double[][] expecteds);

    public double[][] getLevels();

    public void setLevels(double[][] levels);


    /**
     * @return int[a][b] of colors for the data points to be plotted on the graph, where color values are 2 = yellow, 3
     *         = red, 4 = purple and 1 = graph base color (a is the number of data series and b is the number of data
     *         points in that data series)
     */
    public int[][] getColors();

    public void setColors(int[][] colors);

    /**
     * @return String[a][b] of tool tip values for the data points to be plotted on the graph (a is the number of data
     *         series and b is the number of data points in that data series)
     */
    public String[][] getAltTexts();

    public void setAltTexts(String[][] altTexts);

    /**
     * @return String[a][b] of url values for the data points to be plotted on the graph (a is the number of data series
     *         and b is the number of data points in that data series)
     */
    public String[][] getLineSetURLs();

    public void setLineSetURLs(String[][] lineSetURLs);

    public String getLineSetURLTarget();

    public void setLineSetURLTarget(String lineSetURLTarget);

    /**
     * @return String[b] of values that are to be used as labels for the x-axis of the the graph (b is the number of
     *         data points in a data series)
     */
    public String[] getXLabels();

    public void setXLabels(String[] xLabels);

    /**
     * @return String[a] of values that are to be used as labels for each of the data series in the graph (a is the
     *         number of data series)
     */
    public String[] getLineSetLabels();

    public void setLineSetLabels(String[] lineSetLabels);

    /**
     * @return boolean[a] of values which indicate whether alerts should be displayed for a particular data series (a is
     *         the number of data series)
     */
    public boolean[] displayAlerts();

    public void setDisplayAlerts(boolean[] displayAlerts);

    public boolean[] displaySeverityAlerts();

    public void setDisplaySeverityAlerts(boolean[] displaySeverityAlerts);

    /**
     * @return double[a] of values which indicate the size of the points to be displayed for a particular data series (a
     *         is the number of data series)
     */
    public double[] getLineSymbolSizes();

    public void setLineSymbolSizes(double[] lineSymbolSizes);

    public String getGraphTitle();

    public void setGraphTitle(String graphTitle);

    public String getXAxisLabel();

    public void setXAxisLabel(String xAxisLabel);

    public String getYAxisLabel();

    public void setYAxisLabel(String yAxisLabel);

    public int getGraphWidth();

    public void setGraphWidth(int graphWidth);

    public int getGraphHeight();

    public void setGraphHeight(int graphHeight);

    public Color[] getGraphBaseColors();

    public void setGraphBaseColors(Color[] baseColors);

    public boolean showLegend();

    public void setShowLegend(boolean showLegend);

    public boolean percentBased();

    public void setPercentBased(boolean percentBased);

    public boolean plotHorizontal();

    public void setPlotHorizontal(boolean plotHorizontal);

    public int getMaxLabeledCategoryTicks();

    public void setMaxLabeledCategoryTicks(int maxLabeledCategoryTicks);

    public boolean getShowSingleAlertLegend();

    public void setShowSingleAlertLegends(boolean showSingleAlertLegend);

    public boolean getShowSingleSeverityLegend();

    public void setShowSingleSeverityLegends(boolean showSingleSeverityLegend);

    public void setBackgroundColor(Color backgroundColor);

    public Color getBackgroundColor();

    public void setLabelBackgroundColor(Color labelBackgroundColor);

    public Color getLabelBackgroundColor();

    public boolean getShowGraphLabels();

    public void setShowGraphLabels(boolean show);

    public String getNoDataMessage();

    public void setNoDataMessage(String message);

    public void setTitleFont(Font titleFont);

    public Font getTitleFont();

}
