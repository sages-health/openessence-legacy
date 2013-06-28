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

import org.jfree.data.category.DefaultCategoryDataset;

import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class MetadataCategoryDataset extends DefaultCategoryDataset {

    private Map<Comparable, Map<Comparable, Map<String, Object>>> itemMetadata;

    public MetadataCategoryDataset() {
        super();
        itemMetadata = new TreeMap<Comparable, Map<Comparable, Map<String, Object>>>();
    }

    public void addValue(Number value, Comparable rowKey, Comparable columnKey, Map<String, Object> metadata) {
        this.addValue(value, rowKey, columnKey);
        Map<Comparable, Map<String, Object>> rowMap = itemMetadata.get(rowKey);
        if (rowMap == null) {
            rowMap = new TreeMap<Comparable, Map<String, Object>>();
            itemMetadata.put(rowKey, rowMap);
        }
        rowMap.put(columnKey, metadata);
    }

    public Map<String, Object> getMetadata(int row, int column) {
        Comparable rowKey = getRowKey(row);
        Comparable columnKey = getColumnKey(column);

        Map<Comparable, Map<String, Object>> rowMap = itemMetadata.get(rowKey);
        return rowMap != null ? rowMap.get(columnKey) : null;
    }
}
