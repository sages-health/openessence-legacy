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

package edu.jhuapl.graphs;

import java.text.DateFormat;

/**
 * GraphSource represents a completely configured, yet unrendered, graph of some kind.  To get a rendered graph, call
 * the {@link #renderGraph(int, int, Encoding)} method.
 */
public interface GraphSource {

    /**
     * Specifies the shape of an individual graph item.  Expects a {@link java.awt.Shape}.
     */
    public static final String ITEM_SHAPE = "ItemShape";

    /**
     * Specifies the color of an individual graph item.  Expects a {@link java.awt.Paint}.
     */
    public static final String ITEM_COLOR = "ItemColor";

    /**
     * Specifies the URL of an individual graph item.  Expects a {@link String}.
     */
    public static final String ITEM_URL = "ItemUrl";

    /**
     * Specifies the tooltip of an individual graph item.  Expects a {@link String}.
     */
    public static final String ITEM_TOOL_TIP = "ItemToolTip";

    /**
     * Specifies whether or not the specified item should be drawn.  Expects a {@link Boolean}.
     */
    public static final String ITEM_VISIBLE = "ItemVisible";

    /**
     * Specifies the {@link TimeResolution} of a series in a time series graph.
     */
    public static final String SERIES_TIME_RESOLUTION = "SeriesTimeResolution";

    /**
     * Specifies the label of a series in a time series graph.  Expects a {@link String}.
     */
    public static final String SERIES_TITLE = "SeriesTitle";

    /**
     * Specifies the shape of all items in a series in a time series graph (is overriden by an item's {@link #ITEM_SHAPE}).
     * Expects a {@link java.awt.Shape}.
     */
    public static final String SERIES_SHAPE = "SeriesShape";

    /**
     * Specifies the stroke, or the style of the line that connects data points. Expects a {@link java.awt.Stroke}.
     */
    public static final String SERIES_STROKE = "SeriesStroke";

    /**
     * Specifies the color of a series in a time series graph (is overriden by an item's {@link #ITEM_COLOR}).  Expects a
     * {@link java.awt.Paint}.
     */
    public static final String SERIES_COLOR = "SeriesColor";

    /**
     * Expects a {@link Boolean}.
     */
    public static final String SERIES_VISIBLE = "SeriesVisible";

    /**
     * Expects a {@link Boolean}.
     */
    public static final String SERIES_LINES_VISIBLE = "SeriesLinesVisible";

    /**
     * Specifies the type of graph to be produced.
     */
    public static final String GRAPH_TYPE = "GraphType";

    /**
     * A Time Series graph type.
     */
    public static final String GRAPH_TYPE_TIME_SERIES = "timeSeries";

    /**
     * A bar graph type.
     */
    public static final String GRAPH_TYPE_BAR = "barGraph";

    /**
     * A stacked bar graph type.
     */
    public static final String GRAPH_TYPE_STACKED_BAR = "stackedBarGraph";


    /**
     * A pie graph type.
     */
    public static final String GRAPH_TYPE_PIE = "pieGraph";

    /**
     * A line graph type.
     */
    public static final String GRAPH_TYPE_LINE = "lineGraph";

    /**
     * A dual axis graph type.
     */
    public static final String GRAPH_TYPE_DUAL_AXIS = "dualAxisGraph";

    /**
     * Specifies the title of the graph to be produced.  Expects a {@link String}.
     */
    public static final String GRAPH_TITLE = "GraphTitle";

    /**
     * Specifies the font of the title of the graph.  Expects a {@link java.awt.Font}.
     */
    public static final String GRAPH_FONT = "GraphTitleFont";

    /**
     * Specifies the default shape of all items in the graph (is overriden by an item's {@link #ITEM_SHAPE} and series'
     * {@link #SERIES_SHAPE}).  Expects a {@link java.awt.Shape}.
     */
    public static final String GRAPH_SHAPE = "GraphShape";

    /**
     * Specifies the default color of all items in the graph (is overriden by an item's {@link #ITEM_COLOR} and series'
     * {@link #SERIES_COLOR}.  Expects a {@link java.awt.Paint}.
     */
    public static final String GRAPH_COLOR = "GraphColor";

    /**
     * Controls whether a graph's range axis starts from 0, or from the least value in the series.  Expects a {@link
     * boolean}.
     */
    public static final String GRAPH_RANGE_INCLUDE_0 = "GraphRangeInclude0";

    /**
     * Specifies the lower bound value on the range axis (is overriden by {@link #GRAPH_RANGE_INCLUDE_0}).  Expects a
     * positive {@Link Double}.
     */
    public static final String GRAPH_RANGE_LOWER_BOUND = "GraphRangeLowerBound";

    /**
     * Specifies the upper bound value on the range axis.  Expects a positive {@Link Double}.
     */
    public static final String GRAPH_RANGE_UPPER_BOUND = "GraphRangeUpperBound";

    /**
     * Controls whether a graph's range axis labels should be formatted as integers (as opposed to doubles).  Expects a
     * {@link Boolean}.
     */
    public static final String GRAPH_RANGE_INTEGER_TICK = "GraphRangeIntegerTick";

    /**
     * Controls whether a graph's range axis labels should display its minor ticks.  Expects a {@link Boolean}.
     */
    public static final String GRAPH_RANGE_MINOR_TICK_VISIBLE = "GraphRangeMinorTickVisible";

    /**
     * Expects a {@link java.awt.Color}.
     */
    public static final String GRAPH_RANGE_GRIDLINE_PAINT = "GraphRangeGridlinePaint";

    /**
     * Expects a {@link org.jfree.chart.axis.AxisLocation}.
     */
    public static final String GRAPH_RANGE_AXIS_LOCATION = "GraphRangeAxisLocation";

    /**
     * Specifies the DateFormatter used to render the labels of the domain axis of a time series graph.  Expects a {@link
     * DateFormat}.
     */
    public static final String GRAPH_DATE_FORMATTER = "GraphDateFormatter";

    /**
     * Specifies the label for the domain axis of a graph.  Expects a {@link String}.
     */
    public static final String GRAPH_X_LABEL = "XLabel";

    /**
     * Gets the label for the range axis of a graph.  Expects a {@link String}.
     */
    public static final String GRAPH_Y_LABEL = "YLabel";

    /**
     * Specifies the {@link java.awt.Font} to be used for the domain axis.
     */
    public static final String GRAPH_X_AXIS_FONT = "XAxisFont";

    /**
     * Specifies the {@link java.awt.Font} to be used for the domain axis label.
     */
    public static final String GRAPH_X_AXIS_LABEL_FONT = "XAxisLabelFont";

    /**
     * Specifies the {@link java.awt.Font} to be used for the range axis.
     */
    public static final String GRAPH_Y_AXIS_FONT = "YAxisFont";

    /**
     * Specifies the {@link java.awt.Font} to be used for the range axis label.
     */
    public static final String GRAPH_Y_AXIS_LABEL_FONT = "YAxisLabelFont";

    /**
     * Specifies the lower bound value on the domain axis.  Expects a {@Link java.util.Date}.
     */
    public static final String GRAPH_DOMAIN_LOWER_BOUND = "GraphDomainLowerBound";

    /**
     * Specifies the upper bound value on the domain axis.  Expects a {@Link java.util.Date}.
     */
    public static final String GRAPH_DOMAIN_UPPER_BOUND = "GraphDomainUpperBound";

    /**
     * Specifies the minimum value between marks on the domain axis.  Expects a positive {@link Integer}.
     */
    public static final String GRAPH_MIN_DOMAIN_TICK = "MinDomainTick";

    /**
     * Expects a {@link java.awt.Color}.
     */
    public static final String GRAPH_DOMAIN_GRIDLINE_PAINT = "GraphDomainGridlinePaint";

    /**
     * Specifies the number of minor ticks between each labeled tick.  Expects a postiive {@Link Integer}.
     */
    public static final String GRAPH_MINOR_TICKS = "MinorTicks";

    /**
     * Controls whether the graph's legend should be displayed.  Expects a {@link Boolean}}.
     */
    public static final String GRAPH_LEGEND = "Legend";

    /**
     * Controls whether the tooltips should be displayed on the graph.  Expects a {@link Boolean}}.
     */
    public static final String GRAPH_TOOL_TIP = "HasToolTips";

    /**
     * Specifies the background color of a graph.  Expects a {@link java.awt.Paint}.
     */
    public static final String BACKGROUND_COLOR = "BackgroundColor";

    /**
     * The default stroke used in the graph. Expects a {@link java.awt.Stroke}.
     */
    public static final String GRAPH_STROKE = "GraphStroke";

    /**
     * Expects a {@link Boolean}.
     */
    public static final String GRAPH_BORDER = "GraphBorder";

    /**
     * Expects a {@link Boolean}.
     */
    public static final String LEGEND_BORDER = "LegendBorder";

    /**
     * Expects a {@link java.awt.Font}.
     */
    public static final String LEGEND_FONT = "LegendFont";

    /**
     * Expects a positive {@link Double}.
     */
    public static final String AXIS_OFFSET = "AxisOffset";

    /**
     * Expects a {@link Boolean}.
     */
    public static final String GRAPH_DISPLAY_LABEL = "GraphDisplayLabel";

    /**
     * Expects a {@link java.awt.Color}.
     */
    public static final String GRAPH_LABEL_BACKGROUND_COLOR = "GraphLabelBackgroundColor";

    /**
     * Expects a {@link String}.
     */
    public static final String GRAPH_NO_DATA_MESSAGE = "GraphNoDataMessage";

    /**
     * Renders the graph represented by this graph source to the given dimensions and encoding.
     *
     * @param width    the width of the rendered graph
     * @param height   the height of the rendered graph
     * @param encoding the encoding the graph should be rendered as
     * @return A rendered form of the graph
     * @throws GraphException if the rendering fails
     * @see RenderedGraph
     */
    public RenderedGraph renderGraph(int width, int height, Encoding encoding) throws GraphException;
}
