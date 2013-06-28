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

import edu.jhuapl.graphs.GraphSource;
import edu.jhuapl.graphs.TimePointInterface;
import edu.jhuapl.graphs.TimeSeriesInterface;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.util.Map;

/**
 * A specialized {@link XYLineAndShapeRenderer renderer} for JFreeChart time series graphs that selects the item color
 * and shape based on {@link TimeSeriesInterface time series} and {@link TimePointInterface time point} metadata.
 */
public class TimeSeriesRenderer extends XYLineAndShapeRenderer {

    private final TimeSeriesCollection collection;

    /**
     * Creates a TimeSeriesRenderer.
     *
     * @param collection the JFreeChart time series' to be rendered
     */
    public TimeSeriesRenderer(TimeSeriesCollection collection) {
        this.collection = collection;
        setUseFillPaint(true);

        setupItemGenerators();
    }

    /**
     *
     */
    private static final long serialVersionUID = 8201217370053590160L;

    @Override
    public Paint getItemFillPaint(int row, int column) {
        return getItemProperty(row, column, Paint.class, super.getItemFillPaint(row, column), GraphSource.ITEM_COLOR);
    }

    @Override
    public Shape getItemShape(int row, int column) {
        return getItemProperty(row, column, Shape.class, super.getItemShape(row, column), GraphSource.ITEM_SHAPE);
    }

    private Map<String, Object> getMetadata(int row, int column) {
        TimeSeries series = collection.getSeries(row);
        if (series == null) {
            return null;
        }

        TimeSeriesDataItem item = series.getDataItem(column);
        if (!(item instanceof MetadataDataItem)) {
            return null;
        }

        return ((MetadataDataItem) item).getMetadata();
    }

    // Note, we aren't using hard core reflection here (really).  We pass in the Class instance because
    // we need a run-time way of performing the instance check and the cast, and Java generics doesn't
    // give us a way to do this.  However, the caller is expected to simply use "T.class" where T is the
    // return type of the concrete method above.
    private <T> T getItemProperty(int row, int column, Class<T> klazz, T defaultValue, String key) {

        Map<String, Object> metadata = getMetadata(row, column);
        if (metadata == null) {
            return defaultValue;
        }

        Object o = metadata.get(key);
        if (o != null && klazz.isInstance(o)) {
            return klazz.cast(o);
        } else {
            return defaultValue;
        }
    }

    private void setupItemGenerators() {
        setURLGenerator(new XYURLGenerator() {
            @Override
            public String generateURL(XYDataset arg0, int series, int point) {
                return getItemProperty(series, point, String.class, null, GraphSource.ITEM_URL);
            }
        });

        setBaseToolTipGenerator(new XYToolTipGenerator() {
            @Override
            public String generateToolTip(XYDataset arg0, int series, int point) {
                return getItemProperty(series, point, String.class, null, GraphSource.ITEM_TOOL_TIP);
            }
        });
    }
}
