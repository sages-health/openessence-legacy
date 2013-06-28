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

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.Tick;
import org.jfree.chart.axis.TickType;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomLabelNumberAxis extends NumberAxis {

    private static final long serialVersionUID = 3184634416365314383L;
    private Map<Double, String> tickValueToLabelMapping = null;
    private Double lowestTickValue = null;
    private Integer tickCount = null;

    public CustomLabelNumberAxis(Map<Double, String> tickValueToLabelMapping) {
        this.tickValueToLabelMapping = tickValueToLabelMapping;
    }

    public CustomLabelNumberAxis(Map<Double, String> tickValueToLabelMapping, String label) {
        super(label);
        this.tickValueToLabelMapping = tickValueToLabelMapping;
    }

    public void setLowestTickValue(Double lowestTickValue) {
        this.lowestTickValue = lowestTickValue;
    }

    public void setTickCount(Integer tickCount) {
        this.tickCount = tickCount;
    }

    @Override
    protected List<Tick> refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
        List<Tick> result = new ArrayList<Tick>();
        Font tickLabelFont = getTickLabelFont();
        g2.setFont(tickLabelFont);

        if (isAutoTickUnitSelection()) {
            selectAutoTickUnit(g2, dataArea, edge);
        }

        TickUnit tu = getTickUnit();
        double size = tu.getSize();
        int count = this.tickCount != null ? this.tickCount : calculateVisibleTickCount();
        double
                lowestTickValue =
                this.lowestTickValue != null ? this.lowestTickValue : calculateLowestVisibleTickValue();

        if (count <= ValueAxis.MAXIMUM_TICK_COUNT) {
            int minorTickSpaces = getMinorTickCount();

            if (minorTickSpaces <= 0) {
                minorTickSpaces = tu.getMinorTickCount();
            }

            for (int minorTick = 1; minorTick < minorTickSpaces; minorTick++) {
                double minorTickValue = lowestTickValue - size * minorTick / minorTickSpaces;

                if (getRange().contains(minorTickValue)) {
                    result.add(
                            new NumberTick(TickType.MINOR, minorTickValue, "", TextAnchor.TOP_CENTER, TextAnchor.CENTER,
                                           0.0));
                }
            }

            for (int i = 0; i < count; i++) {
                double currentTickValue = lowestTickValue + (i * size);
                String tickLabel = tickValueToLabelMapping.get(currentTickValue);

                if (tickLabel == null) {
                    NumberFormat formatter = getNumberFormatOverride();

                    if (formatter != null) {
                        tickLabel = formatter.format(currentTickValue);
                    } else {
                        tickLabel = getTickUnit().valueToString(currentTickValue);
                    }
                }

                TextAnchor anchor = null;
                TextAnchor rotationAnchor = null;
                double angle = 0.0;

                if (isVerticalTickLabels()) {
                    anchor = TextAnchor.CENTER_RIGHT;
                    rotationAnchor = TextAnchor.CENTER_RIGHT;

                    if (edge == RectangleEdge.TOP) {
                        angle = Math.PI / 2.0;
                    } else {
                        angle = -Math.PI / 2.0;
                    }
                } else {
                    if (edge == RectangleEdge.TOP) {
                        anchor = TextAnchor.BOTTOM_CENTER;
                        rotationAnchor = TextAnchor.BOTTOM_CENTER;
                    } else {
                        anchor = TextAnchor.TOP_CENTER;
                        rotationAnchor = TextAnchor.TOP_CENTER;
                    }
                }

                Tick tick = new NumberTick(new Double(currentTickValue), tickLabel, anchor, rotationAnchor, angle);
                result.add(tick);
                double nextTickValue = lowestTickValue + ((i + 1) * size);

                for (int minorTick = 1; minorTick < minorTickSpaces; minorTick++) {
                    double minorTickValue = currentTickValue + (nextTickValue - currentTickValue) * minorTick /
                                                               minorTickSpaces;

                    if (getRange().contains(minorTickValue)) {
                        result.add(new NumberTick(TickType.MINOR, minorTickValue, "", TextAnchor.TOP_CENTER,
                                                  TextAnchor.CENTER, 0.0));
                    }
                }
            }
        }

        return result;
    }

    @Override
    protected List<Tick> refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
        List<Tick> result = new ArrayList<Tick>();
        Font tickLabelFont = getTickLabelFont();
        g2.setFont(tickLabelFont);

        if (isAutoTickUnitSelection()) {
            selectAutoTickUnit(g2, dataArea, edge);
        }

        TickUnit tu = getTickUnit();
        double size = tu.getSize();
        int count = this.tickCount != null ? this.tickCount : calculateVisibleTickCount();
        double
                lowestTickValue =
                this.lowestTickValue != null ? this.lowestTickValue : calculateLowestVisibleTickValue();

        if (count <= ValueAxis.MAXIMUM_TICK_COUNT) {
            int minorTickSpaces = getMinorTickCount();

            if (minorTickSpaces <= 0) {
                minorTickSpaces = tu.getMinorTickCount();
            }

            for (int minorTick = 1; minorTick < minorTickSpaces; minorTick++) {
                double minorTickValue = lowestTickValue - size * minorTick / minorTickSpaces;

                if (getRange().contains(minorTickValue)) {
                    result.add(
                            new NumberTick(TickType.MINOR, minorTickValue, "", TextAnchor.TOP_CENTER, TextAnchor.CENTER,
                                           0.0));
                }
            }

            for (int i = 0; i < count; i++) {
                double currentTickValue = lowestTickValue + (i * size);
                String tickLabel = tickValueToLabelMapping.get(currentTickValue);

                if (tickLabel == null) {
                    NumberFormat formatter = getNumberFormatOverride();

                    if (formatter != null) {
                        tickLabel = formatter.format(currentTickValue);
                    } else {
                        tickLabel = getTickUnit().valueToString(currentTickValue);
                    }
                }

                TextAnchor anchor = null;
                TextAnchor rotationAnchor = null;
                double angle = 0.0;

                if (isVerticalTickLabels()) {
                    if (edge == RectangleEdge.LEFT) {
                        anchor = TextAnchor.BOTTOM_CENTER;
                        rotationAnchor = TextAnchor.BOTTOM_CENTER;
                        angle = -Math.PI / 2.0;
                    } else {
                        anchor = TextAnchor.BOTTOM_CENTER;
                        rotationAnchor = TextAnchor.BOTTOM_CENTER;
                        angle = Math.PI / 2.0;
                    }
                } else {
                    if (edge == RectangleEdge.LEFT) {
                        anchor = TextAnchor.CENTER_RIGHT;
                        rotationAnchor = TextAnchor.CENTER_RIGHT;
                    } else {
                        anchor = TextAnchor.CENTER_LEFT;
                        rotationAnchor = TextAnchor.CENTER_LEFT;
                    }
                }

                Tick tick = new NumberTick(new Double(currentTickValue), tickLabel, anchor, rotationAnchor, angle);
                result.add(tick);
                double nextTickValue = lowestTickValue + ((i + 1) * size);

                for (int minorTick = 1; minorTick < minorTickSpaces; minorTick++) {
                    double minorTickValue = currentTickValue + (nextTickValue - currentTickValue) * minorTick /
                                                               minorTickSpaces;

                    if (getRange().contains(minorTickValue)) {
                        result.add(new NumberTick(TickType.MINOR, minorTickValue, "", TextAnchor.TOP_CENTER,
                                                  TextAnchor.CENTER, 0.0));
                    }
                }
            }
        }

        return result;
    }
}
