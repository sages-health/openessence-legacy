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
import edu.jhuapl.graphs.PointInterface;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public abstract class JFreeChartCategoryGraphSource extends JFreeChartBaseSource {

    public static final String DOMAIN_AXIS = "DomainAxis";
    public static final String RANGE_AXIS = "RangeAxis";

    protected static final String DEFAULT_DOMAIN_LABEL = "Category";
    protected static final String DEFAULT_RANGE_LABEL = "Value";

    protected abstract JFreeChart createChart(String title, String xLabel, String yLabel, CategoryDataset dataset,
                                              boolean legend, boolean graphToolTip) throws GraphException;

    @Override
    public void initialize() throws GraphException {
        String title = getParam(GraphSource.GRAPH_TITLE, String.class, DEFAULT_TITLE);
        String xLabel = getParam(GraphSource.GRAPH_X_LABEL, String.class, DEFAULT_DOMAIN_LABEL);
        String yLabel = getParam(GraphSource.GRAPH_Y_LABEL, String.class, DEFAULT_RANGE_LABEL);
        CategoryDataset dataset = makeDataSet();

        chart = createChart(title, xLabel, yLabel, dataset, false, false);

        // start customizing the graph
        Paint backgroundColor = getParam(GraphSource.BACKGROUND_COLOR, Paint.class, DEFAULT_BACKGROUND_COLOR);
        Paint plotColor = getParam(JFreeChartTimeSeriesGraphSource.PLOT_COLOR, Paint.class, backgroundColor);
        Double offset = getParam(GraphSource.AXIS_OFFSET, Double.class, DEFAULT_AXIS_OFFSET);
        Paint
                graphDomainGridlinePaint =
                getParam(GraphSource.GRAPH_DOMAIN_GRIDLINE_PAINT, Paint.class, backgroundColor);
        Paint graphRangeGridlinePaint = getParam(GraphSource.GRAPH_RANGE_GRIDLINE_PAINT, Paint.class, backgroundColor);
        boolean graphBorder = getParam(GraphSource.GRAPH_BORDER, Boolean.class, DEFAULT_GRAPH_BORDER);

        chart.setBackgroundPaint(backgroundColor);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(plotColor);
        plot.setAxisOffset(new RectangleInsets(offset, offset, offset, offset));
        plot.setDomainGridlinePaint(graphDomainGridlinePaint);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(graphRangeGridlinePaint);
        plot.setOutlineVisible(graphBorder);

        // set the axis location
        AxisLocation
                axisLocation =
                getParam(GraphSource.GRAPH_RANGE_AXIS_LOCATION, AxisLocation.class, AxisLocation.TOP_OR_LEFT);
        plot.setRangeAxisLocation(axisLocation);

        // customize the y-axis
        if (params.get(RANGE_AXIS) instanceof ValueAxis) {
            ValueAxis valueAxis = (ValueAxis) params.get(RANGE_AXIS);
            plot.setRangeAxis(valueAxis);
        }

        ValueAxis valueAxis = plot.getRangeAxis();
        Object yAxisFont = params.get(GraphSource.GRAPH_Y_AXIS_FONT);
        Object yAxisLabelFont = params.get(GraphSource.GRAPH_Y_AXIS_LABEL_FONT);
        Double rangeLowerBound = getParam(GraphSource.GRAPH_RANGE_LOWER_BOUND, Double.class, null);
        Double rangeUpperBound = getParam(GraphSource.GRAPH_RANGE_UPPER_BOUND, Double.class, null);
        boolean graphRangeIntegerTick = getParam(GraphSource.GRAPH_RANGE_INTEGER_TICK, Boolean.class, false);
        boolean graphRangeMinorTickVisible = getParam(GraphSource.GRAPH_RANGE_MINOR_TICK_VISIBLE, Boolean.class, true);

        if (yAxisFont instanceof Font) {
            valueAxis.setTickLabelFont((Font) yAxisFont);
        }

        if (yAxisLabelFont instanceof Font) {
            valueAxis.setLabelFont((Font) yAxisLabelFont);
        }

        if (rangeLowerBound != null) {
            valueAxis.setLowerBound(rangeLowerBound);
        }

        if (rangeUpperBound != null) {
            valueAxis.setUpperBound(rangeUpperBound);
        }

        if (graphRangeIntegerTick) {
            valueAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        }

        valueAxis.setMinorTickMarksVisible(graphRangeMinorTickVisible);

        // customize the x-axis
        if (params.get(DOMAIN_AXIS) instanceof CategoryAxis) {
            CategoryAxis domainAxis = (CategoryAxis) params.get(DOMAIN_AXIS);
            plot.setDomainAxis(domainAxis);
        }

        CategoryAxis domainAxis = plot.getDomainAxis();
        Object xAxisFont = params.get(GraphSource.GRAPH_X_AXIS_FONT);
        Object xAxisLabelFont = params.get(GraphSource.GRAPH_X_AXIS_LABEL_FONT);

        if (xAxisFont instanceof Font) {
            domainAxis.setTickLabelFont((Font) xAxisFont);
        }

        if (xAxisLabelFont instanceof Font) {
            domainAxis.setLabelFont((Font) xAxisLabelFont);
        }

        domainAxis.setLabel(xLabel);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        plot.setDomainAxis(domainAxis);

        // change the font of the graph title
        Font titleFont = getParam(GraphSource.GRAPH_FONT, Font.class, DEFAULT_GRAPH_TITLE_FONT);
        TextTitle textTitle = new TextTitle();
        textTitle.setText(title);
        textTitle.setFont(titleFont);
        chart.setTitle(textTitle);

        // makes a wrapper for the legend to remove the border around it
        boolean legend = getParam(GraphSource.GRAPH_LEGEND, Boolean.class, DEFAULT_GRAPH_LEGEND);
        boolean legendBorder = getParam(GraphSource.LEGEND_BORDER, Boolean.class, DEFAULT_LEGEND_BORDER);
        Object legendFont = params.get(GraphSource.LEGEND_FONT);

        if (legend) {
            LegendTitle legendTitle = new LegendTitle(chart.getPlot());
            BlockContainer wrapper = new BlockContainer(new BorderArrangement());

            if (legendBorder) {
                wrapper.setFrame(new BlockBorder(1, 1, 1, 1));
            } else {
                wrapper.setFrame(new BlockBorder(0, 0, 0, 0));
            }

            BlockContainer items = legendTitle.getItemContainer();
            items.setPadding(2, 10, 5, 2);
            wrapper.add(items);
            legendTitle.setWrapper(wrapper);
            legendTitle.setPosition(RectangleEdge.BOTTOM);
            legendTitle.setHorizontalAlignment(HorizontalAlignment.CENTER);

            if (legendFont instanceof Font) {
                legendTitle.setItemFont((Font) legendFont);
            }

            chart.addSubtitle(legendTitle);
        }

        this.initialized = true;
    }

    @SuppressWarnings("unchecked")
    protected CategoryDataset makeDataSet() throws GraphException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int count = 0;

        // first, a pre-processing pass to determine the names of each dataset
        for (DataSeriesInterface series : data) {
            String
                    seriesName =
                    getParam(series.getMetadata(), GraphSource.SERIES_TITLE, String.class, "series" + (count + 1));
            Set<Object> descs = new HashSet<Object>(series.getPoints().size());

            for (PointInterface point : series.getPoints()) {
                if (!descs.contains(point.getDescriminator())) {
                    descs.add(point.getDescriminator());

                    if (point.getDescriminator() instanceof Comparable) {
                        dataset.addValue(point.getValue(), seriesName, (Comparable) point.getDescriminator());
                    } else {
                        throw new GraphException(
                                "Descrminator " + point.getDescriminator() + " not instanceof Comparable");
                    }
                } else {
                    throw new GraphException(
                            "Multiple points in series \"" + seriesName + "\" have the descriminator " + point
                                    .getDescriminator());
                }
            }

            count += 1;
        }

        return dataset;
    }
}
