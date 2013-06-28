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

package edu.jhuapl.graphs.jfreechart.utils;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryTick;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.text.TextBlock;
import org.jfree.ui.RectangleEdge;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SparselyLabeledCategoryAxis extends CategoryAxis {

    private static final long serialVersionUID = -2491781030111769632L;

    // number of ticks to label
    private final int maxLabeledTicks;
    private final Paint domainGridlinePaint;

    public SparselyLabeledCategoryAxis(int maxLabeledTicks) {
        this.maxLabeledTicks = maxLabeledTicks;
        this.domainGridlinePaint = null;
    }

    public SparselyLabeledCategoryAxis(int maxLabeledTicks, Paint domainGridlinePaint) {
        this.maxLabeledTicks = maxLabeledTicks;
        this.domainGridlinePaint = domainGridlinePaint;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void drawTickMarks(Graphics2D g2, double cursor, Rectangle2D dataArea, RectangleEdge edge, AxisState state) {
        Plot p = getPlot();

        if (p == null) {
            return;
        }

        CategoryPlot plot = (CategoryPlot) p;
        double il = getTickMarkInsideLength();
        double ol = getTickMarkOutsideLength();
        Line2D line = new Line2D.Double();
        List categories = plot.getCategoriesForAxis(this);
        int tickEvery = categories.size() / (maxLabeledTicks == 0 ? 1 : maxLabeledTicks);

        if (tickEvery < 1) {
            tickEvery = 1;
        }

        if (edge.equals(RectangleEdge.TOP)) {
            Iterator iterator = categories.iterator();
            int i = 0;

            while (iterator.hasNext()) {
                Comparable key = (Comparable) iterator.next();

                if (i % tickEvery == 0) {
                    double x = getCategoryMiddle(key, categories, dataArea, edge);
                    g2.setPaint(getTickMarkPaint());
                    g2.setStroke(getTickMarkStroke());
                    line.setLine(x, cursor, x, cursor + il);
                    g2.draw(line);
                    line.setLine(x, cursor, x, cursor - ol);
                    g2.draw(line);

                    if (domainGridlinePaint != null) {
                        drawDomainGridline(g2, plot, dataArea, x);
                    }
                }

                i++;
            }

            state.cursorUp(ol);
        } else if (edge.equals(RectangleEdge.BOTTOM)) {
            Iterator iterator = categories.iterator();
            int i = 0;

            while (iterator.hasNext()) {
                Comparable key = (Comparable) iterator.next();

                if (i % tickEvery == 0) {
                    double x = getCategoryMiddle(key, categories, dataArea, edge);
                    g2.setPaint(getTickMarkPaint());
                    g2.setStroke(getTickMarkStroke());
                    line.setLine(x, cursor, x, cursor - il);
                    g2.draw(line);
                    line.setLine(x, cursor, x, cursor + ol);
                    g2.draw(line);

                    if (domainGridlinePaint != null) {
                        drawDomainGridline(g2, plot, dataArea, x);
                    }
                }

                i++;
            }

            state.cursorDown(ol);
        } else if (edge.equals(RectangleEdge.LEFT)) {
            Iterator iterator = categories.iterator();
            int i = 0;

            while (iterator.hasNext()) {
                Comparable key = (Comparable) iterator.next();

                if (i % tickEvery == 0) {
                    double y = getCategoryMiddle(key, categories, dataArea, edge);
                    g2.setPaint(getTickMarkPaint());
                    g2.setStroke(getTickMarkStroke());
                    line.setLine(cursor, y, cursor + il, y);
                    g2.draw(line);
                    line.setLine(cursor, y, cursor - ol, y);
                    g2.draw(line);

                    if (domainGridlinePaint != null) {
                        drawDomainGridline(g2, plot, dataArea, y);
                    }
                }

                i++;
            }

            state.cursorLeft(ol);
        } else if (edge.equals(RectangleEdge.RIGHT)) {
            Iterator iterator = categories.iterator();
            int i = 0;

            while (iterator.hasNext()) {
                Comparable key = (Comparable) iterator.next();

                if (i % tickEvery == 0) {
                    double y = getCategoryMiddle(key, categories, dataArea, edge);
                    g2.setPaint(getTickMarkPaint());
                    g2.setStroke(getTickMarkStroke());
                    line.setLine(cursor, y, cursor - il, y);
                    g2.draw(line);
                    line.setLine(cursor, y, cursor + ol, y);
                    g2.draw(line);

                    if (domainGridlinePaint != null) {
                        drawDomainGridline(g2, plot, dataArea, y);
                    }
                }

                i++;
            }

            state.cursorRight(ol);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CategoryTick> refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
        List<CategoryTick> standardTicks = super.refreshTicks(g2, state, dataArea, edge);

        if (standardTicks.isEmpty()) {
            return standardTicks;
        }

        int tickEvery = standardTicks.size() / (maxLabeledTicks == 0 ? 1 : maxLabeledTicks);

        if (tickEvery < 1) {
            return standardTicks;
        }

        // replace some labels with blank ones
        List<CategoryTick> fixedTicks = new ArrayList<CategoryTick>(standardTicks.size());

        for (int i = 0; i < standardTicks.size(); i++) {
            CategoryTick tick = standardTicks.get(i);

            if (i % tickEvery == 0) {
                fixedTicks.add(tick);
            } else {
                fixedTicks.add(new CategoryTick(tick.getCategory(), new TextBlock(), tick.getLabelAnchor(),
                                                tick.getRotationAnchor(), tick.getAngle()));
            }
        }

        return fixedTicks;
    }

    private void drawDomainGridline(Graphics2D g2, CategoryPlot plot, Rectangle2D dataArea, double value) {
        Line2D line = null;
        PlotOrientation orientation = plot.getOrientation();

        if (orientation == PlotOrientation.HORIZONTAL) {
            line = new Line2D.Double(dataArea.getMinX(), value, dataArea.getMaxX(), value);
        } else if (orientation == PlotOrientation.VERTICAL) {
            line = new Line2D.Double(value, dataArea.getMinY(), value, dataArea.getMaxY());
        }

        g2.setPaint(domainGridlinePaint);
        Stroke stroke = plot.getDomainGridlineStroke();

        if (stroke == null) {
            stroke = CategoryPlot.DEFAULT_GRIDLINE_STROKE;
        }

        g2.setStroke(stroke);
        g2.draw(line);
    }
}
