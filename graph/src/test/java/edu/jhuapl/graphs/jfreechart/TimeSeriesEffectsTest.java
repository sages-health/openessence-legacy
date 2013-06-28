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

import edu.jhuapl.graphs.DefaultTimeSeries;
import edu.jhuapl.graphs.GraphException;
import edu.jhuapl.graphs.GraphSource;
import edu.jhuapl.graphs.TimePoint;
import edu.jhuapl.graphs.TimeResolution;
import edu.jhuapl.graphs.jfreechart.utils.RotatedTickDateAxis;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.*;

public class TimeSeriesEffectsTest {

    public static void main(String[] args) throws GraphException {
        JFreeChart chart = getSource().getChart();
//		((XYPlot)chart.getPlot()).setAxisOffset(new RectangleInsets(0, 0, 0, 0));
        JFrame frame = new JFrame("Graph");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ChartPanel content = new ChartPanel(chart);
        content.setPreferredSize(new Dimension(800, 500));

        frame.getContentPane().add(content);

        frame.pack();
        frame.setVisible(true);
    }

    public static JFreeChartGraphSource getSource() throws GraphException {
        Calendar base = Calendar.getInstance();
        base.set(Calendar.WEEK_OF_YEAR, 6);

        Random r = new Random();

        List<TimePoint> ps1 = new LinkedList<TimePoint>();
        List<TimePoint> ps2 = new LinkedList<TimePoint>();
        List<TimePoint> ps3 = new LinkedList<TimePoint>();

        for (int count = 0; count < 4; count += 1) {
            base.set(Calendar.DAY_OF_WEEK, 2);
            addPoint(base.getTime(), ps2, r);

            // now generate five points for ps1 and p23, the daily sets
            addPoint(base.getTime(), ps1, r);
            addPoint(base.getTime(), ps3, r, "showPoint?week=" + count + "&day=0", null);

            for (int i = 1; i < 5; i += 1) {
                base.add(Calendar.DAY_OF_WEEK, 1);
                addPoint(base.getTime(), ps1, r);
                addPoint(base.getTime(), ps3, r, "showPoint?week=" + count + "&day=" + i, null);
            }

            base.set(Calendar.DAY_OF_WEEK, 1);
            base.add(Calendar.WEEK_OF_YEAR, 1);
        }

        List<DefaultTimeSeries> series = new LinkedList<DefaultTimeSeries>();
        Map<String, Object> s1Md = new HashMap<String, Object>();
        s1Md.put(GraphSource.SERIES_TIME_RESOLUTION, TimeResolution.DAILY);
        s1Md.put(GraphSource.SERIES_SHAPE, new Ellipse2D.Float(-5, -5, 10, 10));
        s1Md.put(GraphSource.SERIES_TITLE, "Frederick County Counts");
        series.add(new DefaultTimeSeries(ps1, s1Md));

        Map<String, Object> s2Md = new HashMap<String, Object>();
        s2Md.put(GraphSource.SERIES_TIME_RESOLUTION, TimeResolution.WEEKLY);
        s2Md.put(GraphSource.SERIES_SHAPE, new Ellipse2D.Float(-7, -7, 14, 14));
        s2Md.put(GraphSource.SERIES_TITLE, "NCR Counts");
        series.add(new DefaultTimeSeries(ps2, s2Md));

        Map<String, Object> s3Md = new HashMap<String, Object>();
        s3Md.put(GraphSource.SERIES_TIME_RESOLUTION, TimeResolution.DAILY);
        s3Md.put(GraphSource.SERIES_SHAPE, new Rectangle2D.Float(-3, -3, 6, 6));
        s3Md.put(GraphSource.SERIES_TITLE, "Carroll County Counts");
        BasicStroke
                bs =
                new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{3F, 2F, 8F}, 1F);
        s3Md.put(GraphSource.SERIES_STROKE, bs);
        series.add(new DefaultTimeSeries(ps3, s3Md));

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GraphSource.BACKGROUND_COLOR, Color.BLUE);
        params.put(JFreeChartTimeSeriesGraphSource.PLOT_COLOR, Color.WHITE);
        params.put(GraphSource.GRAPH_TITLE, "Counts");
        params.put(GraphSource.GRAPH_X_LABEL, "Time");
        params.put(GraphSource.GRAPH_Y_LABEL, "Total Counts");
        DateAxis customAxis = new RotatedTickDateAxis(60.);
        params.put(JFreeChartTimeSeriesGraphSource.DATE_AXIS, customAxis);

        JFreeChartGraphSource source = new JFreeChartGraphSource();
        source.setData(series);
        source.setParams(params);
        source.initialize();

        return source;
    }

    private static void addPoint(Date d, List<TimePoint> points, Random r) {
        addPoint(d, points, r, null, null);
    }

    private static void addPoint(Date d, List<TimePoint> points, Random r, String url, String tooltip) {
        double val = getNextRandomValue(r);
        Map<String, Object> md = new HashMap<String, Object>();
        if (val >= 10 && val < 15) {
            md.put(GraphSource.ITEM_COLOR, Color.YELLOW);
        } else if (val >= 10 && val < 20) {
            md.put(GraphSource.ITEM_COLOR, Color.RED);
        }
        if (url != null) {
            md.put(GraphSource.ITEM_URL, url);
        }
        if (tooltip != null) {
            md.put(GraphSource.ITEM_TOOL_TIP, tooltip);
        }

        points.add(new TimePoint(d, val, md));
    }

    private static double getNextRandomValue(Random r) {
        // we have a 90% chance of something in 0-10, and a 10% chance of something in 10-20
        if (r.nextDouble() <= .9) {
            return 10 * r.nextDouble();
        } else {
            return 10 + 10 * r.nextDouble();
        }
    }
}
