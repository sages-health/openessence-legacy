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

import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;

import java.util.List;

public class CategoryStackedBarGraphRenderer extends StackedBarRenderer {

    private static final long serialVersionUID = 1103397517467013130L;
    private CategoryItemProperty itemProperty;

    public CategoryStackedBarGraphRenderer(List<? extends DataSeriesInterface> data) {
        itemProperty = new CategoryItemProperty(data);
        setupItemGenerators();
    }

    public boolean getItemVisible(int series, int item) {
        return itemProperty.get(series, item, Boolean.class, super.getItemVisible(series, item),
                                GraphSource.ITEM_VISIBLE);
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
}
