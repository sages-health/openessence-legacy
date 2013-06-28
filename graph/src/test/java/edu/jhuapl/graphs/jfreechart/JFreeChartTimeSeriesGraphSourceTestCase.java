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

import junit.framework.TestCase;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class JFreeChartTimeSeriesGraphSourceTestCase extends TestCase {

    public void testComplexGraph() {
        JFreeChartTimeSeriesGraphSource source = null;
        try {
            source = getComplexGraph();
        } catch (GraphException e) {
            fail();
        }

        validateChart(source.getChart());
    }

    public void testBadTimeResolution() {
        Calendar c = Calendar.getInstance();

        Map<String, Object> params = new HashMap<String, Object>();

        c.set(Calendar.DAY_OF_WEEK, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 5);
        List<TimePoint> series = new LinkedList<TimePoint>();
        series.add(new TimePoint(c.getTime(), 5, params));

        c.set(Calendar.MINUTE, 12);
        series.add(new TimePoint(c.getTime(), 8, params));

        Map<String, Object> seriesParams = new HashMap<String, Object>();
        seriesParams.put(GraphSource.SERIES_TIME_RESOLUTION, TimeResolution.HOURLY);

        try {
            new JFreeChartTimeSeriesGraphSource(Arrays.asList(new DefaultTimeSeries(series, seriesParams)), params);
            fail();
        } catch (GraphException e) {
            // expected
        }

        seriesParams.put(GraphSource.SERIES_TIME_RESOLUTION, TimeResolution.DAILY);
        try {
            new JFreeChartTimeSeriesGraphSource(Arrays.asList(new DefaultTimeSeries(series, seriesParams)), params);
            fail();
        } catch (GraphException e) {
            // expected
        }

        series.clear();

        for (int i = 1; i < 5; i += 1) {
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.DAY_OF_WEEK, i);
            series.add(new TimePoint(c.getTime(), i, params));
        }
        c.set(Calendar.DAY_OF_WEEK, 2);
        c.set(Calendar.HOUR_OF_DAY, 3);
        series.add(new TimePoint(c.getTime(), 2, params));

        seriesParams.put(GraphSource.SERIES_TIME_RESOLUTION, TimeResolution.DAILY);
        try {
            new JFreeChartTimeSeriesGraphSource(Arrays.asList(new DefaultTimeSeries(series, seriesParams)), params);
            fail();
        } catch (GraphException e) {
            // expected
        }

        seriesParams.put(GraphSource.SERIES_TIME_RESOLUTION, TimeResolution.HOURLY);
        try {
            new JFreeChartTimeSeriesGraphSource(Arrays.asList(new DefaultTimeSeries(series, seriesParams)), params);
            // this should go through okay
        } catch (GraphException e) {
            fail();
        }

        series.clear();
        c.set(Calendar.DAY_OF_WEEK, 1);
        series.add(new TimePoint(c.getTime(), 5, params));
        c.set(Calendar.DAY_OF_WEEK, 3);
        series.add(new TimePoint(c.getTime(), 12, params));
        seriesParams.put(GraphSource.SERIES_TIME_RESOLUTION, TimeResolution.DAILY);
        try {
            new JFreeChartTimeSeriesGraphSource(Arrays.asList(new DefaultTimeSeries(series, seriesParams)), params);
            // this should go through on a daily resolution
        } catch (GraphException e) {
            fail();
        }

        seriesParams.put(GraphSource.SERIES_TIME_RESOLUTION, TimeResolution.WEEKLY);
        try {
            new JFreeChartTimeSeriesGraphSource(Arrays.asList(new DefaultTimeSeries(series, seriesParams)), params);
            fail();
        } catch (GraphException e) {
            // expected failure for weekly
        }

        series.clear();

        c.set(Calendar.DAY_OF_WEEK, 1);
        c.set(Calendar.WEEK_OF_MONTH, 2);
        series.add(new TimePoint(c.getTime(), 5, params));

        c.set(Calendar.WEEK_OF_MONTH, 3);
        series.add(new TimePoint(c.getTime(), 8, params));

        seriesParams.put(GraphSource.SERIES_TIME_RESOLUTION, TimeResolution.WEEKLY);
        try {
            new JFreeChartTimeSeriesGraphSource(Arrays.asList(new DefaultTimeSeries(series, seriesParams)), params);
            // this should go through on a weekly resolution
        } catch (GraphException e) {
            fail();
        }

        seriesParams.put(GraphSource.SERIES_TIME_RESOLUTION, TimeResolution.MONTHLY);
        try {
            new JFreeChartTimeSeriesGraphSource(Arrays.asList(new DefaultTimeSeries(series, seriesParams)), params);
            fail();
        } catch (GraphException e) {
            // expected failure for monthly
        }

        series.clear();
        c.set(Calendar.DAY_OF_WEEK, 1);
        c.set(Calendar.WEEK_OF_MONTH, 2);
        c.set(Calendar.MONTH, 2);
        series.add(new TimePoint(c.getTime(), 8, params));

        c.set(Calendar.MONTH, 3);
        series.add(new TimePoint(c.getTime(), 12, params));
        seriesParams.put(GraphSource.SERIES_TIME_RESOLUTION, TimeResolution.MONTHLY);
        try {
            new JFreeChartTimeSeriesGraphSource(Arrays.asList(new DefaultTimeSeries(series, seriesParams)), params);
            // this should go through on a monthly resolution
        } catch (GraphException e) {
            fail();
        }

        seriesParams.put(GraphSource.SERIES_TIME_RESOLUTION, TimeResolution.YEARLY);
        try {
            new JFreeChartTimeSeriesGraphSource(Arrays.asList(new DefaultTimeSeries(series, seriesParams)), params);
            fail();
        } catch (GraphException e) {
            // expected failure for yearly
        }

        series.clear();
        c.set(Calendar.DAY_OF_WEEK, 1);
        c.set(Calendar.WEEK_OF_MONTH, 2);
        c.set(Calendar.MONTH, 2);
        series.add(new TimePoint(c.getTime(), 8, params));

        c.add(Calendar.YEAR, 1);
        series.add(new TimePoint(c.getTime(), 18, params));
        seriesParams.put(GraphSource.SERIES_TIME_RESOLUTION, TimeResolution.YEARLY);
        try {
            new JFreeChartTimeSeriesGraphSource(Arrays.asList(new DefaultTimeSeries(series, seriesParams)), params);
            // this should go through on a yearly resolution
        } catch (GraphException e) {
            fail();
        }

    }

    private void validateChart(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        assertEquals(1, plot.getDatasetCount());
        assertEquals(3, plot.getDataset(0).getSeriesCount());

        // TODO add more please
    }

    private JFreeChartTimeSeriesGraphSource getComplexGraph() throws GraphException {
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
        series.add(new DefaultTimeSeries(ps3, s3Md));

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(GraphSource.BACKGROUND_COLOR, Color.BLUE);
        params.put(GraphSource.GRAPH_TITLE, "Counts");
        params.put(GraphSource.GRAPH_X_LABEL, "Time");
        params.put(GraphSource.GRAPH_Y_LABEL, "Total Counts");

        return new JFreeChartTimeSeriesGraphSource(series, params);
    }

    private void addPoint(Date d, List<TimePoint> points, Random r) {
        addPoint(d, points, r, null, null);
    }

    private void addPoint(Date d, List<TimePoint> points, Random r, String url, String tooltip) {
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

    private double getNextRandomValue(Random r) {
        // we have a 90% chance of something in 0-10, and a 10% chance of something in 10-20
        if (r.nextDouble() <= .9) {
            return 10 * r.nextDouble();
        } else {
            return 10 + 10 * r.nextDouble();
        }
    }
}
