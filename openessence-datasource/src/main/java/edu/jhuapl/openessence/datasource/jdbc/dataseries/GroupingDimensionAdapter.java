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

package edu.jhuapl.openessence.datasource.jdbc.dataseries;

import edu.jhuapl.openessence.datasource.dataseries.Grouping;
import edu.jhuapl.openessence.datasource.dataseries.GroupingDimension;
import edu.jhuapl.openessence.datasource.jdbc.DimensionBean;
import edu.jhuapl.openessence.datasource.jdbc.DimensionBeanAdapter;
import edu.jhuapl.openessence.datasource.jdbc.JdbcOeDataSource;
import edu.jhuapl.openessence.datasource.jdbc.ResolutionHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GroupingDimensionAdapter extends DimensionBeanAdapter implements GroupingDimension {

    public GroupingDimensionAdapter(DimensionBean bean, JdbcOeDataSource ds) {
        super(bean, ds);
    }

    @Override
    public Collection<String> getResolutions() {
        Map<String, ResolutionHandler> handlers = bean.getResolutionHandlers();
        return handlers != null ? handlers.keySet() : new ArrayList<String>(0);
    }

    @Override
    public Map<String, ResolutionHandler> getResolutionsMap() {
        Map<String, ResolutionHandler> handlers = bean.getResolutionHandlers();
        return handlers != null ? new HashMap<String, ResolutionHandler>(handlers) : null;
    }

    @Override
    public Grouping makeGrouping() {
        return new GroupingImpl(getId(), null);
    }

    @Override
    public Grouping makeGrouping(String resolution) {

        // is the resolution null?
        if (resolution == null) {
            throw new IllegalArgumentException("Resolution cannot be null.");
        }

        // is the resolution in our set, if not, throw an IllegalArgumentException
        Map<String, ResolutionHandler> handlers = bean.getResolutionHandlers();
        if (handlers == null || !handlers.keySet().contains(resolution)) {
            throw new IllegalArgumentException(
                    "Resolution \"" + resolution + "\" not included in resolution set for dimension \"" + bean.getId()
                    + "\"");
        }

        return new GroupingImpl(getId(), resolution);
    }


}
