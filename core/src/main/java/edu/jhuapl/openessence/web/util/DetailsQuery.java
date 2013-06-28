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

package edu.jhuapl.openessence.web.util;

import edu.jhuapl.openessence.datasource.Dimension;
import edu.jhuapl.openessence.datasource.Filter;
import edu.jhuapl.openessence.datasource.OeDataSource;
import edu.jhuapl.openessence.datasource.OeDataSourceAccessException;
import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.QueryManipulationStore;
import edu.jhuapl.openessence.datasource.Record;
import edu.jhuapl.openessence.datasource.dataseries.Grouping;
import edu.jhuapl.openessence.datasource.jdbc.DataTypeConversionHelper;
import edu.jhuapl.openessence.datasource.jdbc.JdbcOeDataSource;
import edu.jhuapl.openessence.datasource.jdbc.filter.sorting.OrderByFilter;
import edu.jhuapl.openessence.model.DataSourceDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DetailsQuery {

    private static final Logger log = LoggerFactory.getLogger(DetailsQuery.class);

    public Collection<Record> performDetailsQuery(final OeDataSource ds,
                                                  final List<Dimension> results,
                                                  final List<Dimension> accumulations,
                                                  final List<Filter> filters,
                                                  final List<OrderByFilter> sorts,
                                                  final boolean isDistinct,
                                                  final String timezone) throws OeDataSourceException {

        final QueryManipulationStore queryManipStore =
                new QueryManipulationStore(results, accumulations, filters, sorts, isDistinct, timezone);

        return ds.detailsQuery(queryManipStore);
    }

    public Collection<Record> performDetailsQuery(final OeDataSource ds,
                                                  final List<Dimension> results,
                                                  final List<Dimension> accumulations,
                                                  final List<Filter> filters,
                                                  final List<OrderByFilter> sorts,
                                                  final List<Grouping> groupings,
                                                  final boolean isDistinct,
                                                  final String timezone) throws OeDataSourceException {
        final QueryManipulationStore queryManipStore =
                new QueryManipulationStore(results, accumulations, filters, sorts, groupings, isDistinct, timezone);

        return ds.detailsQuery(queryManipStore);
    }


    public DataSourceDetails performDetailsQuery(final JdbcOeDataSource ds,
                                                 final List<Dimension> results,
                                                 final List<Dimension> accumulations,
                                                 final List<Filter> filters,
                                                 final List<OrderByFilter> sorts,
                                                 final boolean isDistinct,
                                                 final String timezone,
                                                 final long firstRecord,
                                                 final long pageSize,
                                                 final boolean totalRequested)
            throws OeDataSourceException, OeDataSourceAccessException {

        final AtomicInteger rowNumber = new AtomicInteger(0);
        final QueryManipulationStore queryManipStore =
                new QueryManipulationStore(results, accumulations, filters, sorts, isDistinct, timezone);

        final List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        try {
            ds.detailsQuery(queryManipStore, new RowCallbackHandler() {
                long rownumber = 0;
                long first = firstRecord;

                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    rownumber = rowNumber.incrementAndGet();

                    // caller can turn off paging by passing negative pageSize
                    if (pageSize > 0 && rownumber > firstRecord + pageSize) {
                        if (!totalRequested) {
                            throw new DetailsQueryExitEarlyRuntimeException("Stopped on row " + rownumber);
                        }
                    } else if (rownumber > first) {
                        Map<String, Object> row = new LinkedHashMap<String, Object>();
                        int i = 1;
                        for (final Dimension dim : queryManipStore.getResults()) {
                            final Object val = DataTypeConversionHelper.convert2JavaType(rs, dim.getSqlType(), i++);
                            if (val instanceof Timestamp) {
                                row.put(dim.getId(), ((Timestamp) val).getTime());
                            } else if (val instanceof Date) {
                                row.put(dim.getId(), ((Date) val).getTime());
                            } else if (val instanceof Number) {
                                row.put(dim.getId(), (Number) val);
                            } else if (val instanceof String) {
                                row.put(dim.getId(), (String) val);
                            } else if (val instanceof Boolean) {
                                row.put(dim.getId(), (Boolean) val);
                            } else if (val == null) {
                                row.put(dim.getId(), null);
                            } else {
                                throw new AssertionError("Unexpected field type \"" + val + "\"");
                            }
                        }
                        rows.add(row);
                    }
                }
            }, ds.getFetchSize());
        } catch (DetailsQueryExitEarlyRuntimeException e) {
            log.error(e.getMessage());
        }

        DataSourceDetails details = new DataSourceDetails();
        details.setRows(rows);
        details.setTotalRecords(rowNumber.get());
        return details;
    }
}
