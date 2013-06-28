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
import edu.jhuapl.graphs.GraphException;
import edu.jhuapl.graphs.GraphSource;
import edu.jhuapl.graphs.GraphSourceBean;
import edu.jhuapl.graphs.PointInterface;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JFreeChartDualAxisGraphSource
        extends JFreeChartCategoryGraphSource implements GraphSourceBean {

    private List<DataSeriesInterface> tsData;
    private List<DataSeriesInterface> barData;
    private DefaultCategoryDataset tsDataset;
    private DefaultCategoryDataset barDataset;
    private String tsLabel;
    private String barLabel;

    public JFreeChartDualAxisGraphSource() {
        tsData = new ArrayList<DataSeriesInterface>();
        barData = new ArrayList<DataSeriesInterface>();
        tsDataset = new DefaultCategoryDataset();
        barDataset = new DefaultCategoryDataset();
        tsLabel = "";
        barLabel = "";
    }

    @Override
    public JFreeChart createChart(String title, String xLabel, String yLabel,
                                  CategoryDataset dataset, boolean legend, boolean graphToolTip) {
        PlotOrientation orientation = getParam(JFreeChartBarGraphSource.PLOT_ORIENTATION,
                                               PlotOrientation.class,
                                               JFreeChartBarGraphSource.DEFAULT_PLOT_ORIENTATION);
        // create the base plot, which is the time series graph
        JFreeChart dualAxisGraph = ChartFactory.createLineChart(title, xLabel, tsLabel, tsDataset,
                                                                orientation, legend, graphToolTip, false);

        // add the bar graph data to the plot
        dualAxisGraph.getCategoryPlot().setDataset(1, barDataset);

        // create the second y-axis
        dualAxisGraph.getCategoryPlot().setRangeAxis(1, new NumberAxis(barLabel));
        dualAxisGraph.getCategoryPlot().mapDatasetToRangeAxis(1, 1);

        // create separate renderers for the time series and bar graphs to be
        // overlaid on the same plot
        CategoryLineGraphRenderer tsRenderer = new CategoryLineGraphRenderer(tsData);
        CategoryBarGraphRenderer barRenderer = new CategoryBarGraphRenderer(barData);
        setupLineRenderer(tsRenderer);
        setupBarRenderer(barRenderer);
        dualAxisGraph.getCategoryPlot().setRenderer(0, tsRenderer);
        dualAxisGraph.getCategoryPlot().setRenderer(1, barRenderer);

        return dualAxisGraph;
    }

    @Override
    public CategoryDataset makeDataSet() throws GraphException {
        tsData.add(data.get(0));
        barData.add(data.get(1));
        tsLabel = getParam(tsData.get(0).getMetadata(), GraphSource.SERIES_TITLE, String.class,
                           "series1");
        barLabel = getParam(barData.get(0).getMetadata(), GraphSource.SERIES_TITLE, String.class,
                            "series2");

        createDataset(tsData.get(0), tsLabel, tsDataset);
        createDataset(barData.get(0), barLabel, barDataset);

        return null;
    }

    @SuppressWarnings("rawtypes")
    private void createDataset(DataSeriesInterface seriesData, String seriesName, DefaultCategoryDataset dataset)
            throws GraphException {
        Set<Object> pointDescriminators = new HashSet<Object>();

        for (PointInterface point : seriesData.getPoints()) {
            if (!pointDescriminators.contains(point.getDescriminator())) {
                pointDescriminators.add(point.getDescriminator());

                if (point.getDescriminator() instanceof Comparable) {
                    dataset.addValue(point.getValue(), seriesName, (Comparable) point.getDescriminator());
                } else {
                    throw new GraphException("Descriminator [" + point.getDescriminator() +
                                             "] is not an instanceof Comparable");
                }
            } else {
                throw new GraphException("Multiple points in series [" + seriesName +
                                         "] have the descriminator [" + point.getDescriminator() + "]");
            }
        }
    }

    private void setupLineRenderer(CategoryLineGraphRenderer renderer) {
        Shape graphShape = getParam(GraphSource.GRAPH_SHAPE, Shape.class, DEFAULT_GRAPH_SHAPE);
        Paint graphColor = getParam(GraphSource.GRAPH_COLOR, Paint.class, DEFAULT_GRAPH_COLOR);
        Stroke graphStroke = getParam(GraphSource.GRAPH_STROKE, Stroke.class, new BasicStroke());

        Map<String, Object> metadata = tsData.get(0).getMetadata();
        Paint seriesColor = getParam(metadata, GraphSource.SERIES_COLOR, Paint.class, graphColor);
        Shape seriesShape = getParam(metadata, GraphSource.SERIES_SHAPE, Shape.class, graphShape);
        Stroke seriesStroke = getParam(metadata, GraphSource.SERIES_STROKE, Stroke.class,
                                       graphStroke);
        boolean seriesVisible = getParam(metadata, GraphSource.SERIES_VISIBLE, Boolean.class, true);
        boolean seriesLinesVisible = getParam(metadata, GraphSource.SERIES_LINES_VISIBLE,
                                              Boolean.class, true);

        renderer.setSeriesPaint(0, seriesColor);
        renderer.setSeriesFillPaint(0, seriesColor);
        renderer.setSeriesOutlinePaint(0, seriesColor);
        renderer.setSeriesShape(0, seriesShape);
        renderer.setSeriesStroke(0, seriesStroke);
        renderer.setSeriesVisible(0, seriesVisible);
        renderer.setSeriesLinesVisible(0, seriesLinesVisible);
        renderer.setDrawOutlines(false);
    }

    private void setupBarRenderer(CategoryBarGraphRenderer renderer) {
        Double maxBarWidth = getParam(JFreeChartBarGraphSource.MAX_BAR_WIDTH, Double.class,
                                      JFreeChartBarGraphSource.DEFAULT_MAX_BAR_WIDTH);

        renderer.setShadowVisible(false);
        renderer.setBarPainter(new CategoryGraphBarPainter(barData));
        renderer.setMaximumBarWidth(maxBarWidth);
    }
}
