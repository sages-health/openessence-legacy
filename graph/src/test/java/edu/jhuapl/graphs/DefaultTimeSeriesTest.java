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

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DefaultTimeSeriesTest extends TestCase {

    public void testDefaultTimeSeries() {
        List<TimePoint> points = new LinkedList<TimePoint>();
        Calendar c = Calendar.getInstance();

        Map<String, Object> pointMap = new HashMap<String, Object>();

        for (int i = 1; i < 4; i += 1) {
            c.set(Calendar.DAY_OF_WEEK, i);
            points.add(new TimePoint(c.getTime(), i, pointMap));
        }

        Map<String, Object> seriesMap = new HashMap<String, Object>();
        seriesMap.put("SeriesResolution", TimeResolution.DAILY);

        DefaultTimeSeries dts = new DefaultTimeSeries(points, seriesMap);
        assertEquals(points, dts.getPoints());
        assertEquals(seriesMap, dts.getMetadata());
    }
}
