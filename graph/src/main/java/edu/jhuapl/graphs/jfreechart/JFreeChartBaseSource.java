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
import edu.jhuapl.graphs.Encoding;
import edu.jhuapl.graphs.GraphException;
import edu.jhuapl.graphs.GraphSource;
import edu.jhuapl.graphs.GraphSourceBean;
import edu.jhuapl.graphs.RenderedGraph;
import edu.jhuapl.graphs.TimeSeriesInterface;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.encoders.ImageEncoder;
import org.jfree.chart.encoders.ImageEncoderFactory;
import org.jfree.chart.encoders.KeypointPNGEncoderAdapter;
import org.jfree.chart.renderer.AbstractRenderer;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class JFreeChartBaseSource implements GraphSourceBean {

    protected static final Paint DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    protected static final Paint DEFAULT_GRAPH_COLOR = Color.BLACK;
    protected static final Shape DEFAULT_GRAPH_SHAPE = new Ellipse2D.Float(-5F, -5F, 10F, 10F);
    protected static final String DEFAULT_TITLE = "Time Series";
    protected static final String DEFAULT_GRAPH_NO_DATA_MESSAGE = "";
    protected static final Boolean DEFAULT_GRAPH_LEGEND = false;
    protected static final Boolean DEFAULT_GRAPH_TOOL_TIP = false;
    protected static final Font DEFAULT_GRAPH_TITLE_FONT = new Font("SansSerif", Font.BOLD, 15);
    protected static final Boolean DEFAULT_GRAPH_BORDER = false;
    protected static final Boolean DEFAULT_LEGEND_BORDER = false;
    protected static final Double DEFAULT_AXIS_OFFSET = new Double(0);
    /**
     * Specifies the plot color of a graph.  Expects a {@link java.awt.Paint}.  If this is left unspecified, but a
     * background color is specified, plot and background colors will match.
     */
    public static final String PLOT_COLOR = "PlotColor";

    protected List<? extends DataSeriesInterface> data;
    protected Map<String, Object> params;
    protected JFreeChart chart;
    protected boolean initialized;

    public JFreeChartBaseSource() {
        this.data = null;
        this.params = null;
        this.chart = null;
        this.initialized = false;
    }

    /**
     * Gets the {@link JFreeChart} backing this instance.
     *
     * @return the backing {@link JFreeChart}
     */
    public JFreeChart getChart() {
        return chart;
    }

    /**
     * Sets the data, a list of {@link TimeSeriesInterface} to be used by this graph.
     *
     * @param data the time series to be used by this graph.
     */
    public void setData(List<? extends DataSeriesInterface> data) {
        this.data = data;
    }

    /**
     * Sets the parameters to be used by this graph.
     *
     * @param params the parameters of this graph
     */
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    /**
     * Checks whether the graph is initialized.
     *
     * @return true if the graph is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RenderedGraph renderGraph(int width, int height, Encoding encoding) throws GraphException {
        if (!isInitialized()) {
            throw new GraphException("Not Initialized");
        }

        ImageEncoder encoder = getEncoder(encoding);
        ChartRenderingInfo info = new ChartRenderingInfo();
        byte[] data;
        try {
            data = encoder.encode(renderImage(width, height, info));
        } catch (IOException e) {
            throw new GraphException("Image encoding failed", e);
        }

        return new JFreeChartRenderedGraph(info, data, encoding);
    }

    protected <T> T getParam(String fieldName, Class<T> klazz, T defaultValue) {
        return getParam(params, fieldName, klazz, defaultValue);
    }

    protected <T> T getParam(Map<String, Object> params, String fieldName, Class<T> klazz, T defaultValue) {
        Object val = params.get(fieldName);
        if (klazz.isInstance(val)) {
            return klazz.cast(val);
        } else {
            return defaultValue;
        }
    }

    protected void setupFont(Axis axis, String fieldName) {
        if (params.get(fieldName) instanceof Font) {
            axis.setTickLabelFont((Font) params.get(fieldName));
        } else {
            Font numFont = axis.getTickLabelFont();
            if (numFont != null) {
                axis.setTickLabelFont(numFont.deriveFont(6));
            }
        }
    }

    protected void setupRenderer(AbstractRenderer renderer, Paint graphColor, Shape graphShape, Stroke graphStroke) {

        int count = 0;
        for (DataSeriesInterface si : data) {
            Paint seriesColor = getParam(si.getMetadata(), GraphSource.SERIES_COLOR, Paint.class, graphColor);
            Shape seriesShape = getParam(si.getMetadata(), GraphSource.SERIES_SHAPE, Shape.class, graphShape);
            Stroke seriesStroke = getParam(si.getMetadata(), GraphSource.SERIES_STROKE, Stroke.class, graphStroke);

            renderer.setSeriesPaint(count, seriesColor);
            renderer.setSeriesFillPaint(count, seriesColor);
            renderer.setSeriesOutlinePaint(count, seriesColor);
            renderer.setSeriesShape(count, seriesShape);
            renderer.setSeriesStroke(count, seriesStroke);

            count += 1;
        }
    }

    private BufferedImage renderImage(int width, int height, ChartRenderingInfo info) {
        return chart.createBufferedImage(width, height, info);
    }

    private static ImageEncoder getEncoder(Encoding encoding) throws GraphException {
        switch (encoding) {
            case JPEG:
                return ImageEncoderFactory.newInstance("jpeg");
            case PNG:
                return ImageEncoderFactory.newInstance("png");
            case PNG_WITH_TRANSPARENCY:
                //this is noted as being slower for big graphs
                KeypointPNGEncoderAdapter keypointPNGEncoderAdapter = new KeypointPNGEncoderAdapter();
                keypointPNGEncoderAdapter.setEncodingAlpha(true);
                return keypointPNGEncoderAdapter;
            default:
                throw new GraphException("Unrecognized encoding \"" + encoding.toString() + "\"");
        }
    }
}
