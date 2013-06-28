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

import edu.jhuapl.graphs.DataSeriesInterface;
import edu.jhuapl.graphs.PointInterface;

import java.util.List;
import java.util.Map;

public class CategoryItemProperty {

    private final List<? extends DataSeriesInterface> data;

    public CategoryItemProperty(List<? extends DataSeriesInterface> data) {
        this.data = data;
    }

    public <T> T get(int row, int column, Class<T> klazz, T defaultValue, String key) {
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

    private Map<String, Object> getMetadata(int row, int column) {
        DataSeriesInterface dsi = data.get(row);

        if (dsi == null) {
            return null;
        }

        List<? extends PointInterface> points = dsi.getPoints();

        if (points == null) {
            return null;
        }

        if (column < points.size()) {
            return points.get(column).getMetadata();
        }

        return null;
    }
}
