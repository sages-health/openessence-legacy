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

import edu.jhuapl.graphs.DataSeriesInterface;
import edu.jhuapl.graphs.GraphSource;
import edu.jhuapl.graphs.GraphSourceBean;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;

import java.awt.*;
import java.util.Map;

public class JFreeChartLineGraphSource extends JFreeChartCategoryGraphSource implements GraphSourceBean {

    public static final String PLOT_ORIENTATION = "PlotOrientation";

    protected static final PlotOrientation DEFAULT_PLOT_ORIENTATION = PlotOrientation.VERTICAL;

    public JFreeChartLineGraphSource() {

    }

    @Override
    public JFreeChart createChart(String title, String xLabel, String yLabel, CategoryDataset dataset, boolean legend,
                                  boolean graphToolTip) {
        PlotOrientation orientation = getParam(PLOT_ORIENTATION, PlotOrientation.class, DEFAULT_PLOT_ORIENTATION);

        JFreeChart result =
                ChartFactory.createLineChart(title, xLabel, yLabel, dataset, orientation, legend, graphToolTip, false);

        CategoryLineGraphRenderer r = new CategoryLineGraphRenderer(data);

        Shape graphShape = getParam(GraphSource.GRAPH_SHAPE, Shape.class, DEFAULT_GRAPH_SHAPE);
        Paint graphColor = getParam(GraphSource.GRAPH_COLOR, Paint.class, DEFAULT_GRAPH_COLOR);
        Stroke graphStroke = getParam(GraphSource.GRAPH_STROKE, Stroke.class, new BasicStroke());

        setupRenderer(r, graphColor, graphShape, graphStroke);
        result.getCategoryPlot().setRenderer(r);

        return result;
    }

    protected void setupRenderer(CategoryLineGraphRenderer renderer, Paint graphColor, Shape graphShape,
                                 Stroke graphStroke) {
        int count = 0;

        for (DataSeriesInterface si : data) {
            Map<String, Object> metadata = si.getMetadata();
            Paint seriesColor = getParam(metadata, GraphSource.SERIES_COLOR, Paint.class, graphColor);
            Shape seriesShape = getParam(metadata, GraphSource.SERIES_SHAPE, Shape.class, graphShape);
            Stroke seriesStroke = getParam(metadata, GraphSource.SERIES_STROKE, Stroke.class, graphStroke);
            boolean seriesVisible = getParam(metadata, GraphSource.SERIES_VISIBLE, Boolean.class, true);
            boolean seriesLinesVisible = getParam(metadata, GraphSource.SERIES_LINES_VISIBLE, Boolean.class, true);

            renderer.setSeriesPaint(count, seriesColor);
            renderer.setSeriesFillPaint(count, seriesColor);
            renderer.setSeriesOutlinePaint(count, seriesColor);
            renderer.setSeriesShape(count, seriesShape);
            renderer.setSeriesStroke(count, seriesStroke);
            renderer.setSeriesVisible(count, seriesVisible);
            renderer.setSeriesLinesVisible(count, seriesLinesVisible);

            count += 1;
        }

        renderer.setDrawOutlines(false);
    }
}
