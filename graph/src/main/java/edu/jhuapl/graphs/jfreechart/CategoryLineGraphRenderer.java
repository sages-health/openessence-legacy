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
import edu.jhuapl.graphs.jfreechart.utils.CategoryItemProperty;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.util.ShapeUtilities;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class CategoryLineGraphRenderer extends LineAndShapeRenderer {

    private static final long serialVersionUID = 1103397517467013130L;
    private final List<? extends DataSeriesInterface> data;
    private CategoryItemProperty itemProperty;

    public CategoryLineGraphRenderer(List<? extends DataSeriesInterface> data) {
        this.data = data;
        itemProperty = new CategoryItemProperty(data);
        setUseFillPaint(true);
        setupItemGenerators();
    }

    @Override
    public Paint getItemFillPaint(int row, int column) {
        return itemProperty.get(row, column, Paint.class, super.getItemPaint(row, column), GraphSource.ITEM_COLOR);
    }

    @Override
    public Paint getItemPaint(int row, int column) {
        DataSeriesInterface dsi = data.get(row);

        if (dsi == null) {
            return null;
        }

        return (Paint) dsi.getMetadata().get(GraphSource.SERIES_COLOR);
    }

    public boolean getItemVisible(int series, int item) {
        return itemProperty.get(series, item, Boolean.class, super.getItemVisible(series, item),
                                GraphSource.ITEM_VISIBLE);
    }

    @Override
    public Shape getItemShape(int row, int column) {
        return itemProperty.get(row, column, Shape.class, super.getItemShape(row, column), GraphSource.ITEM_SHAPE);
    }

    private void setupItemGenerators() {
        setBaseToolTipGenerator(new CategoryToolTipGenerator() {
            @Override
            public String generateToolTip(CategoryDataset dataset, int row, int column) {
                return itemProperty.get(row, column, String.class, null, GraphSource.ITEM_TOOL_TIP);
            }
        });

        setBaseItemURLGenerator(new CategoryURLGenerator() {
            @Override
            public String generateURL(CategoryDataset dataset, int series, int category) {
                return itemProperty.get(series, category, String.class, null, GraphSource.ITEM_URL);
            }
        });
    }

    /**
     * Draw a single data item.
     *
     * @param g2         the graphics device.
     * @param state      the renderer state.
     * @param dataArea   the area in which the data is drawn.
     * @param plot       the plot.
     * @param domainAxis the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset    the dataset.
     * @param row        the row index (zero-based).
     * @param column     the column index (zero-based).
     * @param pass       the pass index.
     */
    @Override
    public void drawItem(Graphics2D g2, CategoryItemRendererState state,
                         Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
                         ValueAxis rangeAxis, CategoryDataset dataset, int row, int column,
                         int pass) {

        // do nothing if item is not visible
        if (!getItemVisible(row, column)) {
            return;
        }

        // do nothing if both the line and shape are not visible
        if (!getItemLineVisible(row, column)
            && !getItemShapeVisible(row, column)) {
            return;
        }

        // nothing is drawn for null...
        Number v = dataset.getValue(row, column);
        if (v == null) {
            return;
        }

        int visibleRow = state.getVisibleSeriesIndex(row);
        if (visibleRow < 0) {
            return;
        }
        int visibleRowCount = state.getVisibleSeriesCount();

        PlotOrientation orientation = plot.getOrientation();

        // current data point...
        double x1;
        if (getUseSeriesOffset()) {
            x1 = domainAxis.getCategorySeriesMiddle(column,
                                                    dataset.getColumnCount(), visibleRow, visibleRowCount,
                                                    getItemMargin(), dataArea, plot.getDomainAxisEdge());
        } else {
            x1 = domainAxis.getCategoryMiddle(column, getColumnCount(),
                                              dataArea, plot.getDomainAxisEdge());
        }
        double value = v.doubleValue();
        double y1 = rangeAxis.valueToJava2D(value, dataArea,
                                            plot.getRangeAxisEdge());

        if (pass == 0 && getItemLineVisible(row, column)) {
            if (column != 0) {
                Number previousValue = dataset.getValue(row, column - 1);

                // Added by Wayne Loschen 5/25/2010
                // Modified this method to draw a line between the last non-null value
                // instead of only drawing a line if the column - 1 value was non-null
                int prevColumnIndex = column - 1;
                if (previousValue == null) {
                    while (prevColumnIndex > 0 && previousValue == null) {
                        prevColumnIndex--;
                        previousValue = dataset.getValue(row, prevColumnIndex);
                    }
                }

                if (previousValue != null) {
                    // previous data point...
                    double previous = previousValue.doubleValue();
                    double x0;
                    if (getUseSeriesOffset()) {
                        // WAL - Replaced column - 1 with prevColumnIndex
                        x0 = domainAxis.getCategorySeriesMiddle(
                                prevColumnIndex, dataset.getColumnCount(),
                                visibleRow, visibleRowCount,
                                getItemMargin(), dataArea,
                                plot.getDomainAxisEdge());
                    } else {
                        // WAL - Replaced column - 1 with prevColumnIndex
                        x0 = domainAxis.getCategoryMiddle(prevColumnIndex,
                                                          getColumnCount(), dataArea,
                                                          plot.getDomainAxisEdge());
                    }
                    double y0 = rangeAxis.valueToJava2D(previous, dataArea,
                                                        plot.getRangeAxisEdge());

                    Line2D line = null;
                    if (orientation == PlotOrientation.HORIZONTAL) {
                        line = new Line2D.Double(y0, x0, y1, x1);
                    } else if (orientation == PlotOrientation.VERTICAL) {
                        line = new Line2D.Double(x0, y0, x1, y1);
                    }
                    g2.setPaint(getItemPaint(row, column));
                    g2.setStroke(getItemStroke(row, column));
                    g2.draw(line);
                }
            }
        }

        if (pass == 1) {
            Shape shape = getItemShape(row, column);
            if (orientation == PlotOrientation.HORIZONTAL) {
                shape = ShapeUtilities.createTranslatedShape(shape, y1, x1);
            } else if (orientation == PlotOrientation.VERTICAL) {
                shape = ShapeUtilities.createTranslatedShape(shape, x1, y1);
            }

            if (getItemShapeVisible(row, column)) {
                if (getItemShapeFilled(row, column)) {
                    if (getUseFillPaint()) {
                        g2.setPaint(getItemFillPaint(row, column));
                    } else {
                        g2.setPaint(getItemPaint(row, column));
                    }
                    g2.fill(shape);
                }
                if (getDrawOutlines()) {
                    if (getUseOutlinePaint()) {
                        g2.setPaint(getItemOutlinePaint(row, column));
                    } else {
                        g2.setPaint(getItemPaint(row, column));
                    }
                    g2.setStroke(getItemOutlineStroke(row, column));
                    g2.draw(shape);
                }
            }

            // draw the item label if there is one...
            if (isItemLabelVisible(row, column)) {
                if (orientation == PlotOrientation.HORIZONTAL) {
                    drawItemLabel(g2, orientation, dataset, row, column, y1,
                                  x1, (value < 0.0));
                } else if (orientation == PlotOrientation.VERTICAL) {
                    drawItemLabel(g2, orientation, dataset, row, column, x1,
                                  y1, (value < 0.0));
                }
            }

            // submit the current data point as a crosshair candidate
            int datasetIndex = plot.indexOf(dataset);
            updateCrosshairValues(state.getCrosshairState(),
                                  dataset.getRowKey(row), dataset.getColumnKey(column),
                                  value, datasetIndex, x1, y1, orientation);

            // add an item entity, if this information is being collected
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                addItemEntity(entities, dataset, row, column, shape);
            }
        }

    }


}
