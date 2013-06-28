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

import edu.jhuapl.openessence.datasource.Dimension;
import edu.jhuapl.openessence.datasource.Filter;
import edu.jhuapl.openessence.datasource.OeDataSourceAccessException;
import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.SeriesQueryManipulationStore;
import edu.jhuapl.openessence.datasource.dataseries.AccumPoint;
import edu.jhuapl.openessence.datasource.dataseries.DataSeriesSource;
import edu.jhuapl.openessence.datasource.dataseries.Grouping;
import edu.jhuapl.openessence.datasource.dataseries.GroupingDimension;
import edu.jhuapl.openessence.datasource.jdbc.DataTypeConversionHelper;
import edu.jhuapl.openessence.datasource.jdbc.DimensionBean;
import edu.jhuapl.openessence.datasource.jdbc.DimensionBeanAdapter;
import edu.jhuapl.openessence.datasource.jdbc.JdbcOeDataSource;
import edu.jhuapl.openessence.datasource.jdbc.PluggableResolutionHandler;
import edu.jhuapl.openessence.datasource.jdbc.QueryRecord;
import edu.jhuapl.openessence.datasource.jdbc.ResolutionHandler;
import edu.jhuapl.openessence.datasource.timeresolution.ResolutionHandlerFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JdbcDataSeriesSource extends JdbcOeDataSource implements DataSeriesSource {

    private Map<String, DimensionBean> groupingMap;

    public JdbcDataSeriesSource() {
        super();
        groupingMap = new LinkedHashMap<String, DimensionBean>();
    }

    public String getDataSourceId() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void addDimension(DimensionBean dimension) {
        // TODO just get rid of all these dimension maps already!
        super.addDimension(dimension);
        if (dimension.getIsGrouping()) {
            groupingMap.put(dimension.getId(), dimension);
        }
    }

    @Override
    public void removeDimension(DimensionBean dimension) {
        super.removeDimension(dimension);
        groupingMap.remove(dimension.getId());
    }

    @Override
    public Collection<GroupingDimension> getGroupingDimensions() {
        return getDimension(groupingMap.values(), new DimBeanExec<GroupingDimension>() {
            @Override
            public GroupingDimension exec(DimensionBean b) {
                return new GroupingDimensionAdapter(b, JdbcDataSeriesSource.this);
            }
        });
    }

    @Override
    public GroupingDimension getGroupingDimension(String dimensionId) {
        DimensionBean groupingBean = groupingMap.get(dimensionId);
        return groupingBean != null ? new GroupingDimensionAdapter(groupingBean, this) : null;
    }

    @Override
    public List<AccumPoint> seriesQuery(SeriesQueryManipulationStore seriesQueryManipStore)
            throws OeDataSourceException, OeDataSourceAccessException {
        final List<String> accumIds = seriesQueryManipStore.getAccumIds();
        List<Grouping> groupings = seriesQueryManipStore.getGroupings();
        Collection<Filter> filters = seriesQueryManipStore.getWhereClauseFilters();

        // first check types, make sure that each of the accumulations,
        // groupings, and filters exist
        if (accumulationMap == null) {
            throw new OeDataSourceException(
                    "Data source not initialized properly: missing accumulations property");
        }

        if (accumIds == null) {
            throw new OeDataSourceException("Accumulations cannot be null.");
        }

        if (groupings == null) {
            throw new OeDataSourceException("Groupings cannot be null.");
        }

        if (filters == null) {
            throw new OeDataSourceException("Filters cannot be null.");
        }

        if (accumIds.isEmpty()) {
            throw new OeDataSourceException("At least one accumulation must be provided.");
        }

        for (String accumId : accumIds) {
            DimensionBean bean = accumulationMap.get(accumId);
            if (bean == null) {
                throw new OeDataSourceException("Unknown accumulation \"" + accumId + "\"");
            }
        }

        List<DimensionBean> groupingDimensions = new LinkedList<DimensionBean>();
        List<ResolutionHandler> handlers = new LinkedList<ResolutionHandler>();
        List<Integer> colAddedCounts = new LinkedList<Integer>();
        List<String> groupCols = new LinkedList<String>();

        for (Grouping g : groupings) {
            if (!(g instanceof GroupingImpl)) {
                throw new OeDataSourceException("Unrecognized grouping " + g);
            }

            GroupingImpl gi = (GroupingImpl) g;
            DimensionBean bean = groupingMap.get(gi.getId());
            if (bean == null) {
                throw new OeDataSourceException("Grouping on non-grouping dimension \"" + gi.getId() + "\"");
            }
            groupingDimensions.add(bean);

            if (bean.getResolutionHandlers() != null && gi.getResolution() != null) {
                if (gi.getResolution() == null) {
                    throw new OeDataSourceException("Missing resolution for grouping dimension \"" + gi.getId() + "\"");
                }
                ResolutionHandler handler = bean.getResolutionHandlers().get(gi.getResolution());

                if (handler instanceof PluggableResolutionHandler) {

                } else { //todo
                    if (handler == null) {
                        ResolutionHandlerFactory resolutionHandlerFactory = new ResolutionHandlerFactory();
                        try {
                            handler = resolutionHandlerFactory
                                    .determineResolutionHandler(
                                            showMeDbType(), handler);
                        } catch (OeDataSourceAccessException e) {
                            throw new OeDataSourceException("Unexpected resolution \""
                                                            + gi.getResolution() + "\" for grouping dimension \""
                                                            + gi.getId() + "\"");
                        }
                    }
                    if (handler == null) {
                        throw new OeDataSourceException("Unexpected resolution \""
                                                        + gi.getResolution() + "\" for grouping dimension \""
                                                        + gi.getId() + "\"");
                    }
                }

                List<String>
                        resolutionCols =
                        handler.getResolutionColumns(bean.getSqlCol(), seriesQueryManipStore.getTimezone());
                handlers.add(handler);
                groupCols.addAll(resolutionCols);
                colAddedCounts.add(resolutionCols.size());
            } else {
                if (gi.getResolution() != null) {
                    throw new OeDataSourceException(
                            "Unexpected resolution \"" + gi.getResolution() + "\" for grouping dimension \"" + gi
                                    .getId() + "\"");
                }

                handlers.add(null);
                groupCols.add(bean.getSqlCol());
                colAddedCounts.add(1);
            }

        }

        checkFilters(filters);
        List<Object> arguments = getArguments(filters);

        return (List<AccumPoint>) jdbcTemplate
                .query(new SeriesPreparedStatementCreator(accumIds, groupCols, filters, arguments),
                       new SeriesResultSetExtractor(accumIds, groupingDimensions, handlers, colAddedCounts));
    }

    private class SeriesPreparedStatementCreator implements PreparedStatementCreator {

        private final List<String> accumIds;
        private final List<String> groupingCols;
        private final Collection<Filter> filters;
        private final List<Object> arguments;

        public SeriesPreparedStatementCreator(List<String> accumIds, List<String> groupingCols,
                                              Collection<Filter> filters, List<Object> arguments) {
            this.accumIds = accumIds;
            this.groupingCols = groupingCols;
            this.filters = filters;
            this.arguments = arguments;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            StringBuilder query = new StringBuilder();
            query.append("SELECT ");

            boolean first = true;
            for (String accumId : accumIds) {
                if (first) {
                    first = false;
                } else {
                    query.append(",");
                }

                DimensionBean bean = accumulationMap.get(accumId);
                query.append(bean.getSqlCol());
            }

            int nameCount = 1;

            for (String groupingCol : groupingCols) {
                query.append(", ");
                query.append(groupingCol);
                query.append(" __" + nameCount);
                nameCount += 1;
            }

            query.append(" FROM ");
            query.append(getBaseDetailsQuery());

            addWhereClauses(query, filters);

            if (!groupingCols.isEmpty()) {
                StringBuffer nameList = new StringBuffer();
                first = true;
                for (int i = 1; i < nameCount; i += 1) {
                    if (first) {
                        first = false;
                    } else {
                        nameList.append(", ");
                    }
                    nameList.append("__" + i);
                }

                query.append(" GROUP BY ");
                query.append(nameList);

                addHavingClauses(query);

                query.append(" ORDER BY ");
                query.append(nameList);
            }

            PreparedStatement ps = con.prepareStatement(query.toString());
            log.debug("SERIES QUERY pstmt w/ params: " + ps);
            setArguments(arguments, ps);
            log.debug("SERIES QUERY pstmt w/ args: " + ps);

            return ps;
        }
    }

    private class SeriesResultSetExtractor implements ResultSetExtractor<List<AccumPoint>> {

        private final List<String> accumIds;
        private final List<DimensionBean> groupingDimensions;
        private final List<ResolutionHandler> handlers;
        private final List<Integer> colAddedCounts;


        public SeriesResultSetExtractor(List<String> accumIds, List<DimensionBean> groupingDimensions,
                                        List<ResolutionHandler> handlers, List<Integer> colAddedCounts) {
            this.accumIds = accumIds;
            this.groupingDimensions = groupingDimensions;
            this.handlers = handlers;
            this.colAddedCounts = colAddedCounts;
        }

        @Override
        public List<AccumPoint> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<AccumPoint> result = new LinkedList<AccumPoint>();

            while (rs.next()) {
                Map<String, Number> accumValues = new LinkedHashMap<String, Number>(accumIds.size());
                int colCount = 1;
                for (String accumId : accumIds) {
                    try {
                        accumValues.put(accumId, DataTypeConversionHelper.convert2JavaNumberType(rs, colCount));
                        colCount += 1;
                    } catch (OeDataSourceException e) {
                        throw new OeDataSourceException(
                                "Accumulation result of series " + accumId + " is not a number");
                    }
                }

                Map<String, Object> values = new LinkedHashMap<String, Object>(groupingDimensions.size());
                Map<String, Dimension> dimensions = new LinkedHashMap<String, Dimension>(groupingDimensions.size());
                for (int i = 0; i < groupingDimensions.size(); i += 1) {
                    ResolutionHandler handler = handlers.get(i);
                    DimensionBean dim = groupingDimensions.get(i);
                    dimensions.put(dim.getId(), new DimensionBeanAdapter(dim, JdbcDataSeriesSource.this));
                    if (handler == null) {
                        values.put(dim.getId(),
                                   DataTypeConversionHelper.convert2JavaType(rs, dim.getSqlType(), colCount));
                        colCount += 1;
                    } else {
                        int size = colAddedCounts.get(i);
                        Object[] vals = new Object[size];
                        for (int j = 0; j < size; j += 1) {
                            vals[j] = rs.getObject(colCount + j);
                        }
                        colCount += size;

                        Object kernelObj;
                        kernelObj = handler.buildKernel(vals);

                        values.put(dim.getId(), kernelObj);
                    }
                }

                result.add(new AccumPointImpl(accumValues, new QueryRecord(dimensions, values)));
            }

            return result;
        }

    }
}
