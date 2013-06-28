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

import edu.jhuapl.openessence.datasource.dataseries.Grouping;
import edu.jhuapl.openessence.datasource.jdbc.filter.DistinctFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.sorting.OrderByFilter;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Convenience class that prevents explosion of method signatures in the {@link OeDataSource}. Instantiate any of
 * QueryManipulationStore that accepts the parameters intended for the query.
 */
public class QueryManipulationStore {

    private List<Object> arguments;
    private Collection<Dimension> results;
    private Collection<Dimension> accumulations;

    private Collection<Filter> whereClauseFilters;
    private List<OrderByFilter> orderbyFilters;

    private DistinctFilter distinctFilter;
    private List<Grouping> groupings;
    private String timezone;


    /**
     * @param results            dimensions that should appear as results in select query
     * @param accumulations      dimensions that should be summed select query
     * @param whereClauseFilters filters for where clause
     * @param orderbyFilters     filters for order by clause
     * @param isDistinct         flag for distinct query
     * @param timezone           Client/request time zone as a string
     */
    public QueryManipulationStore(Collection<Dimension> results, Collection<Dimension> accumulations,
                                  Collection<Filter> whereClauseFilters,
                                  List<OrderByFilter> orderbyFilters, boolean isDistinct, String timezone) {
        this.results = results;
        this.accumulations = accumulations;

        this.whereClauseFilters =
                CollectionUtils.isEmpty(whereClauseFilters) ? new ArrayList<Filter>() : whereClauseFilters;
        this.orderbyFilters = orderbyFilters;

        this.distinctFilter = isDistinct ? new DistinctFilter() : null;
        this.timezone = timezone;
    }

    public QueryManipulationStore(Collection<Dimension> results, Collection<Dimension> accumulations,
                                  Collection<Filter> whereClauseFilters,
                                  List<OrderByFilter> orderbyFilters, boolean isDistinct) {
        this(results, accumulations, whereClauseFilters, orderbyFilters, isDistinct, null);
    }

    public QueryManipulationStore(Collection<Dimension> results,
                                  Collection<Dimension> accumulations,
                                  Collection<Filter> whereClauseFilters,
                                  List<OrderByFilter> orderbyFilters,
                                  List<Grouping> groupings,
                                  Boolean isDistinct, String timezone) {

        this(results, accumulations, whereClauseFilters, orderbyFilters, isDistinct, timezone);
        this.groupings = groupings;
    }


    public List<Object> getArguments() {
        return arguments;
    }

    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
    }

    public Collection<Dimension> getResults() {
        return results;
    }

    public void setResults(Collection<Dimension> results) {
        this.results = results;
    }

    public Collection<Dimension> getAccumulations() {
        return (accumulations == null ? new ArrayList<Dimension>() : accumulations);
    }

    public void setAccumulations(Collection<Dimension> accumulations) {
        this.accumulations = accumulations;
    }

    public Collection<Filter> getWhereClauseFilters() {
        return whereClauseFilters;
    }

    public void setWhereClauseFilters(Collection<Filter> whereClausefilters) {
        this.whereClauseFilters = whereClausefilters;
    }

    public List<OrderByFilter> getOrderByFilters() {
        return orderbyFilters;
    }

    public void setOrderByFilters(List<OrderByFilter> orderbyfilters) {
        this.orderbyFilters = orderbyfilters;
    }

    public String getDistinctFilterSql() {
        return (distinctFilter == null) ? "" : distinctFilter.getSqlSnippet();
    }

    public void setDistinctFilter(boolean isDistinct) {
        distinctFilter = isDistinct ? new DistinctFilter() : null;
    }

    public List<Grouping> getGroupings() {
        return groupings;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
