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

import edu.jhuapl.openessence.datasource.FieldType;
import edu.jhuapl.openessence.datasource.QueryManipulationStore;
import edu.jhuapl.openessence.datasource.Record;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Joins dimensions, i.e. adds dimensions from one datasource into another. *
 */
public class DimensionJoiner {

    private JdbcOeDataSource baseDs;
    private Map<? extends JdbcOeDataSource, ? extends Collection<DimensionBean>> dimensionJoinMapping;
    private Map<JdbcOeDataSource, Collection<DimensionBean>> previouslyJoinedDimensions;

    public DimensionJoiner(JdbcOeDataSource baseDs,
                           Map<? extends JdbcOeDataSource, ? extends Collection<DimensionBean>> dimensionJoinMapping) {
        this.baseDs = baseDs;
        this.dimensionJoinMapping = dimensionJoinMapping;
        this.previouslyJoinedDimensions = new HashMap<JdbcOeDataSource, Collection<DimensionBean>>();
    }

    public void joinDimensions() {
        for (Entry<? extends JdbcOeDataSource, ? extends Collection<DimensionBean>> e : dimensionJoinMapping
                .entrySet()) {
            Collection<DimensionBean> dimensionsToJoin = e.getValue();
            Collection<DimensionBean> dimensionsJustAdded = new HashSet<DimensionBean>();

            for (DimensionBean otherDimension : dimensionsToJoin) {
                QueryManipulationStore
                        store =
                        new QueryManipulationStore(e.getKey().getResultDimensions(), null, null, null, false);

                for (Record r : e.getKey().detailsQuery(store)) {
                    DimensionBean joinedDimension = onDimensionJoin(e.getKey(), otherDimension, r);
                    baseDs.addDimension(joinedDimension);
                    // dimension has now been joined
                    dimensionsJustAdded.add(joinedDimension);
                }
            }
            // check for otherDs's previously joined dimensions
            // remove all previously joined dimensions that no longer exist
            if (previouslyJoinedDimensions.containsKey(e.getKey())) {
                Iterator<? extends DimensionBean> iterator = previouslyJoinedDimensions.get(e.getKey()).iterator();
                while (iterator.hasNext()) {
                    DimensionBean oldBean = iterator.next();
                    if (!dimensionsJustAdded.contains(oldBean)) {
                        baseDs.removeDimension(oldBean);
                        iterator.remove();
                    }
                }
            }
            previouslyJoinedDimensions.put(e.getKey(), dimensionsJustAdded);
        }
    }

    /**
     * Called when a dimension is added (AKA "joined into") the base datasource. Clients can override this method to add a
     * customized dimension. The return value of this method is added to the base datasource's set of dimensions.
     *
     * @param otherDs        - the other datasource whose dimension was added to the base datasource
     * @param otherDimension - dimension added to the base datasource
     * @param r              - row in table corresponding to the other dimension
     * @return the dimension to add to the base datasource's set of dimensions
     */
    protected DimensionBean onDimensionJoin(JdbcOeDataSource otherDs, DimensionBean otherDimension, Record r) {
        // return sensible defaults
        DimensionBean bean = new DimensionBean();
        bean.setIsAccumulation(true);
        bean.setSqlType(FieldType.INTEGER);
        bean.setIsResult(true);

        return bean;
    }

}
