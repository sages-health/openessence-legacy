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

package edu.jhuapl.graphs.jfreechart;

import edu.jhuapl.graphs.GraphSourceBean;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

public class JFreeChartBarGraphSource extends JFreeChartCategoryGraphSource implements GraphSourceBean {

    public static final String PLOT_ORIENTATION = "PlotOrientation";
    public static final String MAX_BAR_WIDTH = "MaxBarWidth";
    protected static final PlotOrientation DEFAULT_PLOT_ORIENTATION = PlotOrientation.VERTICAL;
    protected static final Double DEFAULT_MAX_BAR_WIDTH = 0.5;

    public JFreeChartBarGraphSource() {
    }

    @Override
    public JFreeChart createChart(String title, String xLabel, String yLabel, CategoryDataset dataset, boolean legend,
                                  boolean graphToolTip) {
        PlotOrientation orientation = getParam(PLOT_ORIENTATION, PlotOrientation.class, DEFAULT_PLOT_ORIENTATION);
        Double maxBarWidth = getParam(MAX_BAR_WIDTH, Double.class, DEFAULT_MAX_BAR_WIDTH);
        JFreeChart result = ChartFactory.createBarChart(title, xLabel, yLabel, dataset, orientation, legend,
                                                        graphToolTip, false);
        CategoryBarGraphRenderer renderer = new CategoryBarGraphRenderer(data);

        renderer.setShadowVisible(false);
        renderer.setBarPainter(new CategoryGraphBarPainter(data));
        renderer.setMaximumBarWidth(maxBarWidth);
        result.getCategoryPlot().setRenderer(renderer);

        return result;
    }
}
