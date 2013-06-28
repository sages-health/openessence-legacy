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

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTick;
import org.jfree.chart.axis.Tick;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A utility DateAxis class that has a rotated tick label effect.  The rotation is given by the {@link
 * #setTickLabelAngle(double)} parameter, in degrees.
 */
public class RotatedTickDateAxis extends DateAxis {

    /**
     *
     */
    private static final long serialVersionUID = 4668050743056211352L;

    private double tickLabelAngle;

    /**
     * Creates a new date axis with the given angle, specified in degrees.
     *
     * @param tickLabelAngle the angle
     * @see DateAxis#DateAxis()
     */
    public RotatedTickDateAxis(double tickLabelAngle) {
        super.setVerticalTickLabels(true);
        setTickLabelAngle(tickLabelAngle);
    }

    /**
     * Creates a new date axis with the given angle, specified in degrees.
     *
     * @param tickLabelAngle the angle
     * @see DateAxis#DateAxis(String)
     */
    public RotatedTickDateAxis(String axisLabel, double tickLabelAngle) {
        super(axisLabel);
        super.setVerticalTickLabels(true);
        setTickLabelAngle(tickLabelAngle);
    }

    /**
     * Creates a new date axis with the given angle, specified in degrees.
     *
     * @param tickLabelAngle the angle
     * @see DateAxis#DateAxis(String, TimeZone, Locale)
     */
    public RotatedTickDateAxis(String axisLabel, TimeZone tz, Locale locale, double tickLabelAngle) {
        super(axisLabel, tz, locale);
        super.setVerticalTickLabels(true);
        setTickLabelAngle(tickLabelAngle);
    }


    /**
     * Sets the tick label angle, in degrees.
     *
     * @param tickLabelAngle the angle
     */
    public void setTickLabelAngle(double tickLabelAngle) {
        this.tickLabelAngle = tickLabelAngle;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
        double rotationAngleInRad = Math.toRadians(tickLabelAngle);
        //Template types are specified here for additional type safety.
        List<Tick> ticks = (List<Tick>) super.refreshTicksHorizontal(g2, dataArea, edge);
        List<Tick> ret = new ArrayList<Tick>();
        for (Tick tick : ticks) {
            if (tick instanceof DateTick) {
                DateTick dateTick = (DateTick) tick;

                //The anchor used depends on the label angle, as follows:
                TextAnchor textAnchor, rotationAnchor;

                double modRadians = rotationAngleInRad % (2 * Math.PI);
                //Handle case where user provided a negative angle value.
                if (modRadians < 0) {
                    modRadians += 2 * Math.PI;
                }

                //For angles between 0-180 degrees:
                if (modRadians <= Math.PI) {
                    textAnchor = TextAnchor.CENTER_LEFT;
                    rotationAnchor = TextAnchor.CENTER_LEFT;
                }
                //For angles between 180-360 degrees:
                else {
                    textAnchor = TextAnchor.CENTER_RIGHT;
                    rotationAnchor = TextAnchor.CENTER_RIGHT;
                }
                ret.add(new DateTick(dateTick.getDate(), dateTick.getText(), textAnchor, rotationAnchor,
                                     rotationAngleInRad));
            } else {
                ret.add(tick);
            }
        }

        return ret;
    }

    @Override
    protected double findMaximumTickLabelHeight(List ticks, Graphics2D g2, Rectangle2D drawArea, boolean vertical) {
        double heightScalingFactor = Math.abs(Math.sin(Math.toRadians(tickLabelAngle)));
        return super.findMaximumTickLabelHeight(ticks, g2, drawArea, vertical) * heightScalingFactor;
    }
}
