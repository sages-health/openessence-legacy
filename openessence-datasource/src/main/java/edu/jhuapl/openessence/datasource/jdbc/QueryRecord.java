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

package edu.jhuapl.openessence.datasource.jdbc;

import edu.jhuapl.openessence.datasource.Dimension;
import edu.jhuapl.openessence.datasource.Record;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class QueryRecord implements Record {

    private Map<String, Dimension> dimensions;
    private Map<String, Object> values;

    public QueryRecord(Map<String, Dimension> dimensions, Map<String, Object> values) {
        if (!dimensions.keySet().equals(values.keySet())) {
            throw new IllegalArgumentException("Dimension keyset not equal to value keyset");
        }

        this.dimensions = dimensions;
        this.values = values;
    }

    @Override
    public Dimension getDimension(String resultId) {
        return dimensions.get(resultId);
    }

    @Override
    public Set<String> getResultIds() {
        return Collections.unmodifiableSet(dimensions.keySet());
    }

    @Override
    public Object getValue(String resultId) {
        return values.get(resultId);
    }

    @Override
    public Map<String, Dimension> getDimensions() {
        return dimensions;
    }

    @Override
    public Map<String, Object> getValues() {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof QueryRecord) {
            QueryRecord r = (QueryRecord) o;
            return values.equals(r.values) && dimensions.equals(r.dimensions);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return values.hashCode() + dimensions.hashCode();
    }

    @Override
    public String toString() {
        String res = "{";
        boolean first = true;
        for (String key : values.keySet()) {
            if (first) {
                first = false;
            } else {
                res += ", ";
            }
            res += key + ":" + values.get(key) + "(" + dimensions.get(key).getSqlType() + ")";
        }
        res += "}";
        return res;
    }
}
