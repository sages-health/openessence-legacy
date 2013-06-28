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

package edu.jhuapl.openessence.datasource;

import edu.jhuapl.openessence.datasource.jdbc.DimensionJoiner;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A DataSource can be queried for data.  Subinterfaces of this interface define the various querying behaviors.
 */
public interface OeDataSource {

    public String getDataSourceId();

    public Object getDataSourceMetaData() throws OeDataSourceAccessException;

    /**
     * Get a collection of dimensions that can be used to filter queries on this data source.
     *
     * @return A collection of dimensions
     */
    public Collection<FilterDimension> getFilterDimensions();

    /**
     * Gets a filter dimension with the given id.
     *
     * @param id The id of the filter dimension.
     * @return The filter dimension, or null if no filter dimension exists with this id.
     */
    public FilterDimension getFilterDimension(String id);

    /**
     * Gets the set of result dimensions for this data source.
     *
     * @return The set of result dimensions.
     */
    public Collection<Dimension> getResultDimensions();

    /**
     * Gets collection of ALL result dimensions for this data source--all parent and children result dimensions
     *
     * @return The total collection of parent and child result dimensions.
     */
    public Collection<Dimension> getAllResultDimensions();

    /**
     * Gets a result dimension, given that dimension's id.
     *
     * @param id The result dimension's id.
     * @return The requested dimension, or null if no such dimension exists.
     */
    public Dimension getResultDimension(String id);

    /**
     * Gets the set of sorting dimensions for this data source. This method retrieves all result dimensions--a sorting
     * dimension must be a result dimension
     *
     * @return The set of sorting dimensions.
     */
    public Collection<SortingDimension> getSortingDimensions();

    /**
     * Gets a sorting dimension, given that dimension's id. This method retrieves dimensions from the map of result
     * dimensions- a sorting dimension must be a result dimension
     *
     * @param id The sorting dimension's id.
     * @return The requested sorting dimension, or null if no such dimension exists.
     */
    public SortingDimension getSortingDimension(String id);

    /**
     * Gets a special-sql dimension, given that dimension's id.
     *
     * @param id The SpecialSqlDimension's id.
     * @return The requested SpecialSqlDimension, or null if no such dimension exists.
     */
    public Dimension getSpecialSqlDimension(String id);

    /**
     * Gets the set of SpecialSqlDimensions for this data source.
     *
     * @return The set of SpecialSqlDimensions.
     */
    public Collection<Dimension> getSpecialSqlDimensions();


    /**
     * Return the and-combination of the given filters.
     *
     * @param subFilters The source filters
     * @return the and-combined filters
     * @throws OeDataSourceException if an exception occcurs
     */
    public Filter andCombinedFilter(List<Filter> subFilters) throws OeDataSourceException;

    /**
     * Return the or-combination of the given filters.
     *
     * @param subFilters The source filters
     * @return the or-combined filter
     * @throws OeDataSourceException if an exception occcurs
     */
    public Filter orCombinedFilter(List<Filter> subFilters) throws OeDataSourceException;

    /**
     * Return a filter that is the negated form of the given filter.
     *
     * @param filter The source filter
     * @return the negated filter
     * @throws OeDataSourceException if an exception occcurs
     */
    public Filter negatedFilter(Filter filter) throws OeDataSourceException;

    /**
     * Performs a details query. The collection of {@link Record records} returned will have only those result dimensions
     * given in the results argument, and will match the given set of filters.
     *
     * @param queryManipulationStore QueryManipulationStore that holds the keywords and data to manipulate the sql queries
     *                               filters - The filters to be applied results - The result dimensions requested sorters
     *                               - The order by clauses to be applied
     * @return A collection of Records from the Data Source matching the query.
     * @throws OeDataSourceException If the query fails.
     */
    public Collection<Record> detailsQuery(QueryManipulationStore queryManipulationStore) throws OeDataSourceException;

    /**
     * Gets the metadata for the data source.
     *
     * @return metadata for the datasource
     */
    public Map<String, Object> getMetaData();

    /**
     * Get the set of available accumulations.
     *
     * @return The list of available accumulations.
     */
    public Collection<Dimension> getAccumulations();

    /**
     * Get the accumulation with the supplied id.
     *
     * @param id the id of the accumulation to retrieve
     * @return the accumulation associated with the id
     */
    public Dimension getAccumulation(String id);

    /**
     * Get the current {@link DimensionJoiner}.
     *
     * @return the {@code DimensionJoiner}
     */
    public DimensionJoiner getDimensionJoiner();
}
