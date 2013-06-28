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

package edu.jhuapl.openessence.datasource.dataseries;

import edu.jhuapl.openessence.datasource.OeDataSource;
import edu.jhuapl.openessence.datasource.OeDataSourceAccessException;
import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.SeriesQueryManipulationStore;

import java.util.Collection;
import java.util.List;

/**
 * A DataSource that supports time series queries. Such a data source can perform one or more such queries, each type
 * identified by a String identifier. For each such series query, there are a set of {@link #getTimeResolution(String)
 * time resolutions}. The resultant {@link #seriesQuery(String, Date, Date, TimeResolution, Collection) query} will
 * return a list of {@link SeriesPoint time-bound number values}, the time series.
 */
public interface DataSeriesSource extends OeDataSource {

    /**
     * Get the list of grouping dimensions available for the given time series query, identified by its id.
     *
     * @param seriesId The time series id
     * @return A collection of grouping dimensions, or null if no such time series query exists, or if the particular
     *         time series query does not support grouping queries.
     */
    public Collection<GroupingDimension> getGroupingDimensions();

    public GroupingDimension getGroupingDimension(String dimensionId);

    /**
     * Gets a collection of {@link GroupedTimeSeries}, one for each combination of values in the grouping dimensions.
     *
     * @param seriesId          The time series query
     * @param startDate         The start date
     * @param endDate           the end date
     * @param resolution        the time resolution
     * @param groupingDimension the set of grouping dimensions
     * @param filters           the query filters
     * @return A collection of time series, one per combination of values for the query dimensions.
     * @throws OeDataSourceException If the query fails.
     */
    public List<AccumPoint> seriesQuery(SeriesQueryManipulationStore seriesQueryManipulationStore)
            throws OeDataSourceException, OeDataSourceAccessException;
}
