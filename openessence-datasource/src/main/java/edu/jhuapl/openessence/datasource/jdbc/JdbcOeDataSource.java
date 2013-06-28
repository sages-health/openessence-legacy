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
import edu.jhuapl.openessence.datasource.Filter;
import edu.jhuapl.openessence.datasource.FilterDimension;
import edu.jhuapl.openessence.datasource.OeDataSource;
import edu.jhuapl.openessence.datasource.OeDataSourceAccessException;
import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.QueryManipulationStore;
import edu.jhuapl.openessence.datasource.Record;
import edu.jhuapl.openessence.datasource.SortingDimension;
import edu.jhuapl.openessence.datasource.dataseries.Grouping;
import edu.jhuapl.openessence.datasource.entry.ChildTableDetails;
import edu.jhuapl.openessence.datasource.jdbc.dataseries.GroupingImpl;
import edu.jhuapl.openessence.datasource.jdbc.filter.AndFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.NotFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.OrFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.SqlGeneratingFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.sorting.OrderByFilter;
import edu.jhuapl.openessence.datasource.timeresolution.ResolutionHandlerFactory;
import edu.jhuapl.openessence.datasource.ui.PossibleValuesConfiguration;
import edu.jhuapl.openessence.datasource.util.DbConfigHelper;
import edu.jhuapl.openessence.datasource.util.DbTypesEnum;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

public class JdbcOeDataSource implements OeDataSource, ApplicationContextAware, InitializingBean {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    private ApplicationContext ctx;
    protected JdbcTemplate jdbcTemplate;

    /**
     * <b>DO NOT READ THIS FIELD.</b> Use {@link #getMainDataSource()} instead, which can be overridden by clients.
     */
    @Resource
    private DataSource mainDataSource;

    @Autowired
    private PlatformTransactionManager transactionManager;

    protected DatabaseMetaData databaseMetaData;

    /**
     * <b>DO NOT READ THIS FIELD.</b> Use {@link #getBaseDetailsQuery()} instead, which can be overridden by clients to
     * provide dynamic base queries.
     */
    private String baseQuery;

    protected List<String> baseWhereClauses;
    protected List<String> baseHavingClauses;
    protected Map<String, DimensionBean> accumulationMap;
    protected Map<String, DimensionBean> resultMap;
    protected List<String> resultFilterIds;
    protected Map<String, DimensionBean> childResultMap;
    protected Map<String, DimensionBean> superResultMap;
    protected Map<String, DimensionBean> editMap;
    protected Map<String, DimensionBean> childEditMap;
    protected Map<String, DimensionBean> superEditMap;
    protected Map<String, DimensionBean> autoGenMap;
    protected Map<String, DimensionBean> specialSqlMap;

    private Map<String, DimensionBean> groupingMap;
    private Map<String, DimensionBean> filterMap;
    private Map<String, Object> metadata;
    private Map<String, Object> childEditDimensions;
    private Map<String, String> fksToParent;

    private DimensionJoiner dimensionJoiner;

    private Set<String> roles;

    public JdbcOeDataSource() {
        accumulationMap = new LinkedHashMap<String, DimensionBean>();
        resultMap = new LinkedHashMap<String, DimensionBean>();
        childResultMap = new LinkedHashMap<String, DimensionBean>();
        superResultMap = new LinkedHashMap<String, DimensionBean>();
        resultFilterIds = new ArrayList<String>();
        editMap = new LinkedHashMap<String, DimensionBean>();
        childEditMap = new LinkedHashMap<String, DimensionBean>();
        superEditMap = new LinkedHashMap<String, DimensionBean>();
        autoGenMap = new LinkedHashMap<String, DimensionBean>();
        specialSqlMap = new LinkedHashMap<String, DimensionBean>();
        groupingMap = new LinkedHashMap<String, DimensionBean>();
        filterMap = new LinkedHashMap<String, DimensionBean>();
        metadata = new LinkedHashMap<String, Object>();

        roles = new HashSet<String>();
        jdbcTemplate = new JdbcTemplate();
    }

    public String getDataSourceId() {
        return this.getClass().getSimpleName();
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Add dimension. If a dimension already exists with the same ID, the existing dimension is updated.
     */
    public void addDimension(DimensionBean dimension) {
        if (dimension.getFilterBeanId() != null && dimension.getFilterBeanId().length() > 0) {
            resultFilterIds.add(dimension.getFilterBeanId());
        }
        if (dimension.getIsResult()) {
            resultMap.put(dimension.getId(), dimension);
            superResultMap.put(dimension.getId(), dimension);
        }

        if (dimension.getIsChildResult()) {
            childResultMap.put(dimension.getId(), dimension);
            superResultMap.put(dimension.getId(), dimension);
        }

        if (dimension.getIsFilter()) {
            filterMap.put(dimension.getId(), dimension);
        }

        if (dimension.getIsAccumulation()) {
            accumulationMap.put(dimension.getId(), dimension);
        }

        /** CRUD - DataEntry related ONLY isEdit, isChildEdit, hasSpecialSql, isAutoGen */
        if (dimension.getIsEdit()) {
            editMap.put(dimension.getId(), dimension);
            superEditMap.put(dimension.getId(), dimension);
        }

        if (dimension.getIsChildEdit()) {
            childEditMap.put(dimension.getId(), dimension);
            superEditMap.put(dimension.getId(), dimension);
        }

        if (dimension.hasSpecialSql()) {
            specialSqlMap.put(dimension.getId(), dimension);
        }

        if (dimension.isAutoGen()) {
            autoGenMap.put(dimension.getId(), dimension);
        }

        if (dimension.getIsGrouping()) {
            groupingMap.put(dimension.getId(), dimension);
        }
    }

    public void removeDimension(DimensionBean dimension) {
        resultMap.remove(dimension.getId());
        superResultMap.remove(dimension.getId());
        if (dimension.getFilterBeanId() != null && dimension.getFilterBeanId().length() > 0) {
            resultFilterIds.remove(dimension.getFilterBeanId());
        }
        childResultMap.remove(dimension.getId());
        filterMap.remove(dimension.getId());
        accumulationMap.remove(dimension.getId());
        editMap.remove(dimension.getId());
        superEditMap.remove(dimension.getId());
        childEditMap.remove(dimension.getId());
        specialSqlMap.remove(dimension.getId());
        autoGenMap.remove(dimension.getId());
        groupingMap.remove(dimension.getId());
    }

    public void setDimensions(Collection<? extends DimensionBean> dimensions) {
        for (DimensionBean bean : dimensions) {
            addDimension(bean);
        }
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setBaseDetailsQuery(final String baseQuery) {
        this.baseQuery = baseQuery;
    }

    public String getBaseDetailsQuery() {
        return baseQuery;
    }

    public void setBaseDetailsWhereClauses(final List<String> baseWhereClauses) {
        this.baseWhereClauses = baseWhereClauses;
    }

    public void setBaseDetailsHavingClauses(final List<String> baseHavingClauses) {
        this.baseHavingClauses = baseHavingClauses;
    }

    public void setAccumulations(final List<DimensionBean> accumulations) {
        this.accumulationMap = new LinkedHashMap<String, DimensionBean>(accumulations.size());
        for (final DimensionBean bean : accumulations) {
            this.accumulationMap.put(bean.getId(), bean);
        }
    }

    @Override
    public Dimension getAccumulation(final String id) {
        joinDimensions();

        if (accumulationMap.containsKey(id)) {
            return new DimensionBeanAdapter(accumulationMap.get(id), this);
        } else {
            log.warn("Unrecognized accumulation id \'" + id + "\' was requested.");
            return null;
        }
    }

    @Override
    public Collection<Dimension> getAccumulations() {
        joinDimensions();

        return getAccumulation(accumulationMap.values(), new DimBeanExec<Dimension>() {
            @Override
            public Dimension exec(DimensionBean b) {
                return new DimensionBeanAdapter(b, JdbcOeDataSource.this);
            }
        });
    }

    @Override
    public Dimension getResultDimension(final String id) {
        joinDimensions();

        if (superResultMap.containsKey(id)) {
            return new DimensionBeanAdapter(superResultMap.get(id), this);
        } else {
            // this happens all the time in MapController
            log.trace("Unrecognized result dimension id '{}' was requested.", id);
            return null;
        }
    }

    @Override
    public Collection<Dimension> getResultDimensions() {
        joinDimensions();

        return getDimension(resultMap.values(), new DimBeanExec<Dimension>() {
            @Override
            public Dimension exec(DimensionBean b) {
                return new DimensionBeanAdapter(b, JdbcOeDataSource.this);
            }
        });
    }

    @Override
    public Collection<Dimension> getAllResultDimensions() {
        joinDimensions();

        return getDimension(superResultMap.values(), new DimBeanExec<Dimension>() {
            @Override
            public Dimension exec(DimensionBean b) {
                return new DimensionBeanAdapter(b, JdbcOeDataSource.this);
            }
        });
    }

    @Override
    public SortingDimension getSortingDimension(final String id) {
        joinDimensions();

        if (superResultMap.containsKey(id)) {
            return new SortingDimensionBeanAdapter(resultMap.get(id), this);
        } else {
            log.warn("Unrecognized sorting dimension id \'" + id + "\' was requested.");
            return null;
        }
    }

    @Override
    public Collection<SortingDimension> getSortingDimensions() {
        joinDimensions();

        return getDimension(superResultMap.values(), new DimBeanExec<SortingDimension>() {
            @Override
            public SortingDimension exec(DimensionBean b) {
                return new SortingDimensionBeanAdapter(b, JdbcOeDataSource.this);
            }
        });
    }

    @Override
    public Dimension getSpecialSqlDimension(String id) {
        if (specialSqlMap != null) {

            if (specialSqlMap.get(id) != null) {
                return new DimensionBeanAdapter(specialSqlMap.get(id), this);
            }
            log.warn("Unrecognized special sql dimension id \'" + id + "\' was requested.");
            return null;
        } else {
            return null;
        }
    }

    @Override
    public Collection<Dimension> getSpecialSqlDimensions() {
        if (specialSqlMap != null) {
            return getDimension(specialSqlMap.values(), new DimBeanExec<Dimension>() {
                @Override
                public Dimension exec(DimensionBean b) {
                    return new DimensionBeanAdapter(b, JdbcOeDataSource.this);
                }
            });
        } else {
            return new ArrayList<Dimension>(0);
        }
    }

    @Override
    public FilterDimension getFilterDimension(String id) {
        joinDimensions();

        if (filterMap.get(id) != null) {
            return new FilterDimensionBeanAdapter(filterMap.get(id), this);
        } else {
            log.warn("Unrecognized filter dimension id \'" + id + "\' was requested.");
            return null;
        }
    }

    @Override
    public Collection<FilterDimension> getFilterDimensions() {
        joinDimensions();

        return getDimension(filterMap.values(), new DimBeanExec<FilterDimension>() {
            @Override
            public FilterDimension exec(DimensionBean b) {
                return new FilterDimensionBeanAdapter(b, JdbcOeDataSource.this);
            }
        });
    }

    /**
     * Sets the metadata.
     *
     * @param metadata the metadata
     */
    public void setMetaData(final Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public Map<String, Object> getMetaData() {
        return (metadata == null) ? null : Collections.unmodifiableMap(metadata);
    }

    public void setChildEditDimensions(final Map<String, Object> childEditDimensions) {
        this.childEditDimensions = childEditDimensions;
    }

    public Map<String, Object> getChildEditDimensions() {
        return (childEditDimensions == null) ? null
                                             : Collections.unmodifiableMap(childEditDimensions);
    }

    public void setFksToParent(final Map<String, String> fksToParent) {
        this.fksToParent = fksToParent;
    }

    public Map<String, String> getFksToParent() {
        return (fksToParent == null) ? null : Collections.unmodifiableMap(fksToParent);
    }

    public PossibleValuesConfiguration getPossibleValuesConfigurationFromDimensionBean(final DimensionBean bean)
            throws OeDataSourceException {
        final PossibleValuesConfiguration configuration;

        final String dsId = bean.getPossibleValuesDsName();
        if (dsId != null) {
            final Object dsObject = ctx.getBean(dsId);
            if (dsObject instanceof OeDataSource) {
                configuration = new PossibleValuesConfiguration(bean, (OeDataSource) dsObject);
            } else {
                throw new OeDataSourceException(dsId + " is not an OeDataSource.");
            }
        } else if (bean.getPossibleValuesDsData() != null) {
            configuration = new PossibleValuesConfiguration(bean);
        } else {
            configuration = null;
        }

        return configuration;
    }

    public PossibleValuesConfiguration getPossibleValuesConfigurationFromChildTableDetails(final ChildTableDetails ctd)
            throws OeDataSourceException {
        PossibleValuesConfiguration configuration = null;

        final String dsId = ctd.getPossibleValuesDsName();
        if (dsId != null) {
            final Object dsObject = ctx.getBean(dsId);
            if (dsObject instanceof OeDataSource) {
                configuration = new PossibleValuesConfiguration(ctd, (OeDataSource) dsObject);
            } else {
                throw new OeDataSourceException("DS \"" + dsId + "\" is not an OeDataSource.");
            }
        }

        return configuration;
    }

    protected <T> Collection<T> getDimension(final Collection<DimensionBean> beans, final DimBeanExec<T> ctr) {
        return new AbstractCollection<T>() {

            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    private Iterator<DimensionBean> beanIt = beans.iterator();

                    @Override
                    public boolean hasNext() {
                        return beanIt.hasNext();
                    }

                    @Override
                    public T next() {
                        return ctr.exec(beanIt.next());
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public int size() {
                return beans.size();
            }
        };
    }

    protected <T> Collection<T> getAccumulation(final Collection<DimensionBean> beans, final DimBeanExec<T> ctr) {
        return new AbstractCollection<T>() {

            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    private Iterator<DimensionBean> beanIt = beans.iterator();

                    @Override
                    public boolean hasNext() {
                        return beanIt.hasNext();
                    }

                    @Override
                    public T next() {
                        return ctr.exec(beanIt.next());
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public int size() {
                return beans.size();
            }
        };
    }

    protected void checkFilters(final Collection<Filter> filters) throws OeDataSourceException {
        // now check if all the filters are defined and all the arguments are correct
        for (final Filter f : filters) {
            if (!(f instanceof SqlGeneratingFilter)) {
                throw new OeDataSourceException("Unknown filter " + f);
            }
            final SqlGeneratingFilter sqf = (SqlGeneratingFilter) f;
            sqf.checkFilter(this);
        }
    }

    protected List<Object> getArguments(final Collection<Filter> filters) throws OeDataSourceException {
        final List<Object> arguments = new LinkedList<Object>();
        for (final Filter f : filters) {
            arguments.addAll(((SqlGeneratingFilter) f).getArguments(this));
        }

        return arguments;
    }

    protected Record createRecord(final List<Dimension> queryDimensions, final ResultSet rs) throws SQLException {
        return createRecord(queryDimensions, rs, 0);
    }

    protected Record createRecord(final List<Dimension> queryDimensions, final ResultSet rs, final int offset)
            throws SQLException {
        final Map<String, Dimension> dimensions = new LinkedHashMap<String, Dimension>(queryDimensions.size());
        final Map<String, Object> values = new LinkedHashMap<String, Object>(queryDimensions.size());

        for (int i = 0; i < queryDimensions.size(); i++) {
            final Dimension d = queryDimensions.get(i);
            dimensions.put(d.getId(), d);
            values.put(d.getId(), DataTypeConversionHelper.convert2JavaType(rs, d.getSqlType(), i + 1 + offset));
        }

        return new QueryRecord(dimensions, values);
    }

    /**
     * Handles grouping using resolution handlers to add report date based on chosen resolution
     */
    protected Record createRecord(final List<Dimension> queryDimensions, final ResultSet rs,
                                  final List<DimensionBean> groupingDimensions, final List<ResolutionHandler> handlers,
                                  final List<Integer> colAddedCounts) throws SQLException {
        final
        Map<String, Dimension>
                dimensions =
                new LinkedHashMap<String, Dimension>(queryDimensions.size() + groupingDimensions.size());
        final
        Map<String, Object>
                values =
                new LinkedHashMap<String, Object>(queryDimensions.size() + groupingDimensions.size());

        int colCount = 0;
        for (int i = 0; i < queryDimensions.size(); i++) {
            final Dimension d = queryDimensions.get(i);
            dimensions.put(d.getId(), d);
            values.put(d.getId(), DataTypeConversionHelper.convert2JavaType(rs, d.getSqlType(), i + 1 + 0));
            colCount++;
        }

        for (int i = 0; i < groupingDimensions.size(); i += 1) {
            ResolutionHandler handler = handlers.get(i);
            DimensionBean dim = groupingDimensions.get(i);
            dimensions.put(dim.getId(), new DimensionBeanAdapter(dim, JdbcOeDataSource.this));

            if (handler == null) {
                values.put(dim.getId(), DataTypeConversionHelper.convert2JavaType(rs, dim.getSqlType(), colCount));
                colCount += 1;
            } else {
                int size = colAddedCounts.get(i);
                Object[] vals = new Object[size];
                for (int j = 0; j < size; j += 1) {
                    vals[j] = rs.getObject(colCount + 1 + j);
                }
                colCount += size;
                Object kernelObj;
                try {
                    kernelObj = handler.buildKernel(vals);
                } catch (OeDataSourceException e) {
                    throw new SQLException(e);
                }
                values.put(dim.getId(), kernelObj);
            }
        }
        return new QueryRecord(dimensions, values);
    }


    protected void fixResultDimensions(final QueryManipulationStore queryManipStore) {
        // Chris Carr, 2/25/2013
        // When the SQL for the query was generated, the SELECTed columns were based on the order of the dimensions
        // as they are defined in the Groovy file.  But, the query manipulation store has its result columns based
        // on the order of the HTTP request parameters.  This "usually" works fine for the ExtJS GUI since the user
        // selects them based on the same order as the Groovy file since this is how they are presented.  (The BIG
        // assumption here is that the browser is fairly predictable on the generation of the HTTP request).  If
        // there is a "hiccup" in the browser, or for external programs (i.e. the OE Shim residing in Virgo), they
        // might be in different orders.  So, ensure that the store has the same ordering as the Groovy file now.

        final ArrayList<Dimension> resultList = new ArrayList<Dimension>();

        for (final Dimension dimension : getResultDimensions()) {
            if (queryManipStore.getResults().contains(dimension)) {
                resultList.add(dimension);
            }
        }

        // Chris Carr / Zarna, 6/5/2013
        // Also in RDD when working with charts, some of the pie slices were getting removed.
        // They are using the Country Name for display (result = true), but Country Id (result = false, see FilterBeanId stuff)
        // for the click-through.  We want to ensure that they have all of their data in the select, but it needs to be at the
        // end of the SELECT clause so that it doesn't alter the order for the details table issue from above.
        for (final Dimension dimension : queryManipStore.getResults()) {
            if (!resultList.contains(dimension)) {
                resultList.add(dimension);
            }
        }

        queryManipStore.setResults(resultList);
    }


    protected void fixAccumDimensions(final QueryManipulationStore queryManipStore) {
        final ArrayList<Dimension> accumList = new ArrayList<Dimension>();

        // re-arrange accumulations according to predefined order
        for (final Dimension dimension : getAccumulations()) {
            if (queryManipStore.getAccumulations().contains(dimension)) {
                accumList.add(dimension);
            }
        }

        // If there are any that were not found in accumulations list,
        // append them at the end.
        for (final Dimension dimension : queryManipStore.getAccumulations()) {
            if (!accumList.contains(dimension)) {
                accumList.add(dimension);
            }
        }

        queryManipStore.setAccumulations(accumList);
    }

    public void updateQueryManipStore(QueryManipulationStore queryManipStore) {
        // Default does nothing
    }

    public void detailsQuery(final QueryManipulationStore queryManipStore, final RowCallbackHandler rcbh,
                             final Integer fzparm) throws OeDataSourceException {

        if (jdbcTemplate == null) {
            throw new OeDataSourceException("No JDBC Template configured");
        }

        if (superResultMap == null) {
            throw new OeDataSourceException(
                    "There are no result dimensions for this datasource. Please check 'WARN log' and your groovy configuration for datasource name : "
                    + this.getClass().getCanonicalName());
        }

        fixResultDimensions(queryManipStore);

        fixAccumDimensions(queryManipStore);

        // helper method to override/apply additional filters
        updateQueryManipStore(queryManipStore);

        final Collection<Dimension> results = queryManipStore.getResults();
        if (!CollectionUtils.isEmpty(results)) {
            // now check if each result dimension is okay.
            for (final Dimension d : results) {
                if (d != null) {
                    final DimensionBean bean = superResultMap.get(d.getId());
                    if (resultFilterIds.size() > 0) {
                        if (bean == null && !resultFilterIds.contains(d.getId())) {
                            throw new OeDataSourceException("Unrecognized result dimension " + d.getId());
                        }
                    } else {
                        if (bean == null) {
                            throw new OeDataSourceException("Unrecognized result dimension " + d.getId());
                        }
                    }
                } else {
                    throw new OeDataSourceException(
                            "There was a potential typo either in the Groovy datasource definition file or the dimension Id that was requested for a dimension in that definition file. Check 'WARN log' and dimension Id spellings for this datasource: "
                            + this.getClass().getCanonicalName());
                }
            }
        } else {
            throw new OeDataSourceException("Results must be specified.");
        }

        final Collection<Filter> filters = queryManipStore.getWhereClauseFilters();
        if (filters == null) {
            throw new OeDataSourceException("Filters cannot be null");
        }
        checkFilters(filters);

        final Collection<OrderByFilter> sorters = queryManipStore.getOrderByFilters();
        if (!CollectionUtils.isEmpty(sorters)) {
            for (final OrderByFilter orderby : sorters) {
                if (getResultDimension(orderby.getFilterId()) == null) {
                    throw new OeDataSourceException("Result dimensions must contain all sorting values");
                }
            }
        }

        final List<Object> arguments = getArguments(filters);
        queryManipStore.setArguments(arguments);

        final int fz = (fzparm != null ? fzparm : jdbcTemplate.getFetchSize());
        jdbcTemplate.query(new DetailsPreparedStatementCreator(queryManipStore) {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = super.createPreparedStatement(con);
                ps.setFetchSize(fz);
                return ps;
            }
        }, rcbh);
    }

    @Override
    public Collection<Record> detailsQuery(final QueryManipulationStore queryManipStore) throws OeDataSourceException {

        if (jdbcTemplate == null) {
            throw new OeDataSourceException("No JDBC Template configured");
        }

        if (superResultMap == null) {
            throw new OeDataSourceException(
                    "There are no result dimensions for this datasource. Please check 'WARN log' and your groovy configuration for datasource name : "
                    + this.getClass().getCanonicalName());
        }

        fixResultDimensions(queryManipStore);

        fixAccumDimensions(queryManipStore);

        // helper method to override/apply additional filters
        updateQueryManipStore(queryManipStore);
        List<Grouping> groupings = queryManipStore.getGroupings();
        List<DimensionBean> groupingDimensions = new LinkedList<DimensionBean>();
        List<ResolutionHandler> handlers = new LinkedList<ResolutionHandler>();
        List<Integer> colAddedCounts = new LinkedList<Integer>();
        List<String> groupCols = new LinkedList<String>();

        // Add appropriate grouping columns based on resolution
        if (groupings != null) {
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
                        throw new OeDataSourceException(
                                "Missing resolution for grouping dimension \"" + gi.getId() + "\"");
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
                            handler.getResolutionColumns(bean.getSqlCol(), queryManipStore.getTimezone());

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

        }

        // update joined dimensions
        if (getDimensionJoiner() != null) {
            getDimensionJoiner().joinDimensions();
        }

        final Collection<Dimension> results = queryManipStore.getResults();
        if (!CollectionUtils.isEmpty(results)) {
            // now check if each result dimension is okay.
            for (final Dimension d : results) {
                if (d != null) {
                    final DimensionBean bean = superResultMap.get(d.getId());
                    if (resultFilterIds.size() > 0) {
                        if (bean == null && !resultFilterIds.contains(d.getId())) {
                            throw new OeDataSourceException("Unrecognized result dimension " + d.getId());
                        }
                    } else {
                        if (bean == null) {
                            throw new OeDataSourceException("Unrecognized result dimension " + d.getId());
                        }
                    }
                } else {
                    throw new OeDataSourceException(
                            "There was a potential typo either in the Groovy datasource definition file or the dimension Id that was requested for a dimension in that definition file. Check 'WARN log' and dimension Id spellings for this datasource: "
                            + this.getClass().getCanonicalName());
                }
            }
        } else {
            throw new OeDataSourceException("Results must be specified.");
        }

        final Collection<Filter> filters = queryManipStore.getWhereClauseFilters();
        if (filters == null) {
            throw new OeDataSourceException("Filters cannot be null");
        }
        checkFilters(filters);

        final Collection<OrderByFilter> sorters = queryManipStore.getOrderByFilters();
        if (!CollectionUtils.isEmpty(sorters)) {
            for (final OrderByFilter orderby : sorters) {
                if (getResultDimension(orderby.getFilterId()) == null) {
                    throw new OeDataSourceException("Result dimensions must contain all sorting values");
                }
            }
        }

        final List<Object> arguments = getArguments(filters);
        queryManipStore.setArguments(arguments);

        final List<Dimension> results2 = new ArrayList<Dimension>(results);
        // TODO: EMPTY?
        final List<String> accumulationIds = new ArrayList<String>();
        if (CollectionUtils.isEmpty(queryManipStore.getAccumulations())) {
            // Remove all accumulations from results, based on Id
            for (final Dimension accumulation : getAccumulations()) {
                accumulationIds.add(accumulation.getId());
            }

            for (final Iterator<Dimension> iterator = results2.iterator(); iterator.hasNext(); ) {
                final Dimension dimension = iterator.next();
                if (accumulationIds.contains(dimension.getId())) {
                    iterator.remove();
                }
            }
        } else {
            // DetailsPreparedStatementCreator puts accum dimensions at the end of results,
            // however, DetailsRowMapper does not know about this. That is why rearranging results.
            // Remove accums and add them at the end.
            for (final Iterator<Dimension> iterator = results2.iterator(); iterator.hasNext(); ) {
                final Dimension dimension = iterator.next();
                if (queryManipStore.getAccumulations().contains(dimension)) {
                    iterator.remove();
                }
            }
            for (Dimension d : queryManipStore.getAccumulations()) {
                results2.add(d);
            }
        }

        return (Collection<Record>) jdbcTemplate.query(new DetailsPreparedStatementCreator(queryManipStore, groupCols),
                                                       new DetailsRowMapper(results2, groupingDimensions, handlers,
                                                                            colAddedCounts));

    }

    protected void setArguments(List<Object> arguments, PreparedStatement pStmt)
            throws SQLException {
        int argCount = 1;
        for (Object o : arguments) {
            // TODO NEED TO ADDRESS THE USE CASES FOR THIS null...POKUAM1...what if not nullable column?
            if (o == null) {
                pStmt.setObject(argCount, null);
            } else if (o instanceof java.sql.Timestamp) {
                pStmt.setTimestamp(argCount, (java.sql.Timestamp) o);
            } else if (o instanceof java.util.Date) {
                pStmt.setTimestamp(argCount, new java.sql.Timestamp(((java.util.Date) o).getTime()));
            } else if (o instanceof Integer) {
                pStmt.setInt(argCount, (Integer) o);
            } else if (o instanceof Long) {
                pStmt.setLong(argCount, (Long) o);
            } else if (o instanceof Float) {
                pStmt.setFloat(argCount, (Float) o);
            } else if (o instanceof Double) {
                pStmt.setDouble(argCount, (Double) o);
            } else if (o instanceof String) {
                pStmt.setString(argCount, (String) o);
            } else if (o instanceof Boolean) {
                pStmt.setBoolean(argCount, (Boolean) o);
            } else {
                throw new AssertionError("Unexpected object " + o + " " + o.getClass());
            }
            argCount += 1;
        }
    }

    protected void addWhereClauses(final StringBuilder query, final Collection<Filter> filters) {
        final List<String> whereSql = new ArrayList<String>();

        if (!CollectionUtils.isEmpty(baseWhereClauses)) {
            whereSql.addAll(baseWhereClauses);
        }

        if (!CollectionUtils.isEmpty(filters)) {
            for (final Filter filter : filters) {
                final String sqlSnippet = ((SqlGeneratingFilter) filter).getSqlSnippet(this);
                if (sqlSnippet != null) {
                    whereSql.add(sqlSnippet);
                }
            }
        }

        if (!CollectionUtils.isEmpty(whereSql)) {
            query.append(" WHERE ");
            query.append(StringUtils.collectionToDelimitedString(whereSql, " AND "));
        }
    }

    protected void addHavingClauses(final StringBuilder query) {
        if (!CollectionUtils.isEmpty(baseHavingClauses)) {
            query.append(" HAVING ");
            query.append(StringUtils.collectionToDelimitedString(baseHavingClauses, " AND "));
        }
    }

    protected boolean addOrderByClauses(final StringBuilder query, final Collection<OrderByFilter> sorters)
            throws OeDataSourceException {

        boolean addedSorters = false;

        if (!CollectionUtils.isEmpty(sorters)) {
            query.append(" ORDER BY ");
            addedSorters = true;

            final List<String> orderBySql = new ArrayList<String>();
            for (final OrderByFilter sorter : sorters) {
                String sqlSnippet = "";
                //added to check for an alias on sort
                DimensionBean bean = this.getBean(sorter.getFilterId());
                if (bean != null && bean.getSqlColAlias() != null && !"".equals(bean.getSqlColAlias())) {
                    sqlSnippet = sorter.getSqlSnippet(bean.getSqlColAlias());
                } else {
                    sqlSnippet = sorter.getSqlSnippet(this);
                }
                if (sqlSnippet != null) {
                    orderBySql.add(sqlSnippet);
                } else {
                    throw new OeDataSourceException("You cannot use this dimension '" + sorter.getFilterId()
                                                    + "' to sort until you properly configure its sql column name. Please adjust your groovy definition file.");
                }
            }
            query.append(StringUtils.collectionToDelimitedString(orderBySql, ", "));
        }

        return addedSorters;

    }

    protected void addOrderByClauses(final StringBuilder query, final Collection<OrderByFilter> sorters,
                                     StringBuffer groupCols) throws OeDataSourceException {
        if (!addOrderByClauses(query, sorters) && groupCols.length() > 0) {
            query.append(" ORDER BY ");
        }
        if (groupCols.length() > 0) {
            query.append(groupCols);
        }
    }

    protected void addGroupByClauses(final StringBuilder query, final LinkedHashMap<String, String> columns)
            throws OeDataSourceException {

        query.append(" GROUP BY ");

        final List<String> groupBySql = new ArrayList<String>();
        for (final String column : columns.keySet()) {
            String sqlSnippet = "";
            //added to check for an alias on sort
            DimensionBean bean = this.getBean(column);
            if (bean != null && bean.getSqlColAlias() != null && !"".equals(bean.getSqlColAlias())) {
                sqlSnippet = bean.getSqlColAlias();
            } else {
                sqlSnippet = columns.get(column);
            }
            if (sqlSnippet != null) {
                groupBySql.add(sqlSnippet);
            } else {
                throw new OeDataSourceException("You cannot use this dimension '" + column
                                                + "' in groupby until you properly configure its sql column name. Please adjust your groovy definition file.");
            }
        }
        //query.append(StringUtils.collectionToDelimitedString(columns.values(), ","));
        query.append(StringUtils.collectionToDelimitedString(groupBySql, ", "));
    }

    protected static interface DimBeanExec<T> {

        public T exec(DimensionBean b);
    }

    private class DetailsPreparedStatementCreator implements PreparedStatementCreator {

        private final QueryManipulationStore queryManipulationStore;
        private List<String> groupingCols;

        private DetailsPreparedStatementCreator(QueryManipulationStore queryManipulationStore) {
            this.queryManipulationStore = queryManipulationStore;
        }

        private DetailsPreparedStatementCreator(QueryManipulationStore queryManipulationStore,
                                                List<String> groupingCols) {
            this.queryManipulationStore = queryManipulationStore;
            this.groupingCols = groupingCols;
        }


        @Override
        public PreparedStatement createPreparedStatement(final Connection connection) throws SQLException {
            final StringBuilder query = new StringBuilder();
            query.append("SELECT ");
            // 2013/02/05, S. Chris Carr, I don't see how this syntax would work (it certainly doesn't in PostgreSQL).  "SELECT  DISTINCT , col1, ...."
            if (queryManipulationStore.getDistinctFilterSql().compareTo("") != 0) {
                query.append(queryManipulationStore.getDistinctFilterSql());
                query.append(", ");
            }
            // Add all requested result dimensions
            final LinkedHashMap<String, String> columns = new LinkedHashMap<String, String>();
            final List<String> accumulationSQL = new ArrayList<String>();

            for (final Dimension dimension : queryManipulationStore.getResults()) {
                columns.put(dimension.getId(), dimension.getSqlCol());
            }

            // Remove all accumulations from results, based on Id
            final Collection<Dimension> accumulations = getAccumulations();
            if (!CollectionUtils.isEmpty(accumulations)) {
                for (final Dimension accumulation : accumulations) {
                    if (columns.get(accumulation.getId()) != null) {
                        columns.remove(accumulation.getId());
                    }
                }
            }

            if (columns.size() > 0) {
                query.append(StringUtils.collectionToDelimitedString(columns.values(), ","));
            }

            // Add requested accumulations
            for (final Dimension accumulation : queryManipulationStore.getAccumulations()) {
                accumulationSQL.add(accumulation.getSqlCol());
            }

            if (!CollectionUtils.isEmpty(accumulationSQL) && accumulationSQL.size() > 0) {
                if (columns.values().size() > 0) {
                    query.append(", ");
                }
                query.append(StringUtils.collectionToDelimitedString(accumulationSQL, ","));
            }

            int nameCount = 1;
            // Add group by cols to handle resolution queries
            if (groupingCols != null) {
                for (String groupingCol : groupingCols) {
                    query.append(", ");
                    query.append(groupingCol);
                    query.append(" __" + nameCount);
                    nameCount += 1;
                }
            }

            query.append(" FROM ");
            query.append(getBaseDetailsQuery());

            addWhereClauses(query, queryManipulationStore.getWhereClauseFilters());
            boolean first = true;
            StringBuffer nameList = new StringBuffer();

            // Group By, leave off if no accumulations
            if (groupingCols != null && !groupingCols.isEmpty()) {
                first = true;
                for (int i = 0; i < groupingCols.size(); i++) {
                    if (first) {
                        first = false;
                    } else {
                        nameList.append(", ");
                    }
                    nameList.append(groupingCols.get(i));
                }

                query.append(" GROUP BY ");
                query.append(nameList);
                if (!CollectionUtils.isEmpty(accumulationSQL)) {
                    query.append(StringUtils.collectionToDelimitedString(columns.values(), ","));
                }
            } else {
                // Group By, leave off if no accumulations
                if (!CollectionUtils.isEmpty(accumulationSQL)) {
                    addGroupByClauses(query, columns);
                }
            }

            addHavingClauses(query);

            try {

                Collection<OrderByFilter> mysorters = queryManipulationStore.getOrderByFilters();
                if (mysorters != null && !mysorters.isEmpty()) {
                    for (OrderByFilter orderby : mysorters) {
                        if (getResultDimension(orderby.getFilterId()) == null) {
                            throw new SQLException(
                                    "Invalid sorters exist. Verify that all your sort filters exist in your SELECT clause.");
                        }
                    }
                }
                addOrderByClauses(query, mysorters, nameList);
            } catch (OeDataSourceException e) {
                throw new SQLException("Error occured while building Order By clause.", e);
            }

            PreparedStatement
                    pStmt =
                    connection.prepareStatement(query.toString(), ResultSet.TYPE_FORWARD_ONLY,
                                                ResultSet.CONCUR_READ_ONLY);
            log.debug("DETAILS QUERY w/ params: " + pStmt);
            setArguments(queryManipulationStore.getArguments(), pStmt);
            log.info("DETAILS QUERY w/ args: " + pStmt);
            return pStmt;
        }
    }

    private class DetailsRowMapper implements RowMapper<Record> {

        private final List<Dimension> dimensions;
        private List<ResolutionHandler> handlers;
        private List<Integer> colAddedCounts;
        private List<DimensionBean> groupingDimensions;

        public DetailsRowMapper(List<Dimension> results2, List<DimensionBean> groupingDimensions,
                                List<ResolutionHandler> handlers, List<Integer> colAddedCounts) {
            this.dimensions = results2;
            this.groupingDimensions = groupingDimensions;
            this.handlers = handlers;
            this.colAddedCounts = colAddedCounts;
        }

        @Override
        public Record mapRow(ResultSet rs, int rowNum) throws SQLException {
            if (groupingDimensions != null && groupingDimensions.size() > 0) {
                return createRecord(dimensions, rs, groupingDimensions, handlers, colAddedCounts);
            } else {
                return createRecord(dimensions, rs);
            }

        }
    }

    @Override
    public DatabaseMetaData getDataSourceMetaData() throws OeDataSourceAccessException {
        if (databaseMetaData != null) {
            return databaseMetaData;
        } else {

            try {
                Connection connection = null;
                try {
                    connection = jdbcTemplate.getDataSource().getConnection();
                    databaseMetaData = connection.getMetaData();
                } finally {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException e) {
                throw new OeDataSourceAccessException("error retrieving meta data for the database", e);
            }
            return databaseMetaData;
        }
    }

    // reason for not bring the 'basicdatasource' into this module is to keep it agnostic of the
    // web server. if needed, the main OE code can implement for other types of web servers that may
    // wrap the java jdbcdatasource
    public JdbcTemplate getJdbcTemplate() {
        JdbcTemplate copy = new JdbcTemplate();
        copy.setDataSource(jdbcTemplate.getDataSource());
        return copy;
    }

    public String getDriverVersion() throws OeDataSourceAccessException {
        try {
            log.debug("DriverVersion: " + getDataSourceMetaData().getDriverVersion());
            return getDataSourceMetaData().getDriverVersion();
        } catch (SQLException e) {
            throw new OeDataSourceAccessException("error occured");
        }
    }

    public int getDriverMajorVersion() throws OeDataSourceAccessException {
        return getDataSourceMetaData().getDriverMajorVersion();
    }

    public int getDriverMinorVersion() throws OeDataSourceAccessException {
        return getDataSourceMetaData().getDriverMinorVersion();
    }

    public String getDatabaseProductName() throws OeDataSourceAccessException {
        try {
            return getDataSourceMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            throw new OeDataSourceAccessException("error occured");
        }
    }

    public String getDatabaseProductVersion() throws OeDataSourceAccessException {
        try {
            return getDataSourceMetaData().getDatabaseProductVersion();
        } catch (SQLException e) {
            throw new OeDataSourceAccessException("error occured");
        }
    }

    public int getDatabaseMajorVersion() throws OeDataSourceAccessException {
        try {
            return getDataSourceMetaData().getDatabaseMajorVersion();
        } catch (SQLException e) {
            throw new OeDataSourceAccessException("error occured");
        }
    }

    public int getDatabaseMinorVersion() throws OeDataSourceAccessException {
        try {
            return getDataSourceMetaData().getDatabaseMinorVersion();
        } catch (SQLException e) {
            throw new OeDataSourceAccessException("error occured");
        }
    }

    public boolean isPostgreSqlDBMS() throws OeDataSourceAccessException {
        return getDatabaseProductName().equalsIgnoreCase(DbConfigHelper.POSTGRESQL);
    }

    public boolean isMySqlDBMS() throws OeDataSourceAccessException {
        return getDatabaseProductName().equalsIgnoreCase(DbConfigHelper.MYSQL);
    }

    public DbTypesEnum showMeDbType() throws OeDataSourceAccessException {
        if (isMySqlDBMS()) {
            return DbTypesEnum.MYSQL;
        } else if (isPostgreSqlDBMS()) {
            return DbTypesEnum.PGSQL;
        } else {
            return DbTypesEnum.UNKNOWN;
        }
    }

    // http://forums.oracle.com/forums/thread.jspa?threadID=279238
    // http://commons.apache.org/dbcp/
    public Integer getFetchSize() throws OeDataSourceAccessException {
        if (getDatabaseProductName().equalsIgnoreCase(DbConfigHelper.MYSQL)) {
            return DbConfigHelper.MYSQL_FETCH_SIZE;
        } else if (getDatabaseProductName().equalsIgnoreCase(DbConfigHelper.POSTGRESQL)) {
            return DbConfigHelper.POSTGRESQL_FETCH_SIZE;
        } else {
            return 1;
        }
    }

    public DimensionBean getBean(String fieldId) {
        DimensionBean bean = filterMap.get(fieldId);

        if (bean == null && editMap != null) {
            bean = editMap.get(fieldId);
        }

        if (bean == null && resultMap != null) {
            bean = resultMap.get(fieldId);
        }

        return bean;
    }

    protected ApplicationContext getCtx() {
        return ctx;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }

    @Override
    public Filter andCombinedFilter(List<Filter> subFilters) throws OeDataSourceException {
        return new AndFilter(checkAndCastFilters(subFilters));
    }

    @Override
    public Filter orCombinedFilter(List<Filter> subFilters) throws OeDataSourceException {
        return new OrFilter(checkAndCastFilters(subFilters));
    }

    @Override
    public Filter negatedFilter(Filter filter) throws OeDataSourceException {
        return new NotFilter(checkAndCastFilter(filter));
    }

    private List<SqlGeneratingFilter> checkAndCastFilters(List<Filter> inputFilters)
            throws OeDataSourceException {
        List<SqlGeneratingFilter> resList = new ArrayList<SqlGeneratingFilter>(inputFilters.size());
        for (Filter f : inputFilters) {
            resList.add(checkAndCastFilter(f));
        }
        return resList;
    }

    private SqlGeneratingFilter checkAndCastFilter(Filter inputFilter) throws OeDataSourceException {
        if (inputFilter instanceof SqlGeneratingFilter) {
            return (SqlGeneratingFilter) inputFilter;
        } else {
            throw new OeDataSourceException("Unexpected type " + inputFilter.getClass().getName());
        }
    }

    /**
     * Clients can override this method to perform setup that must run after the ApplicationContext is set and fields are
     * injected. Make sure to call the superclass though!
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // don't do anything in this method, subclasses often forget to call us
        // use init() instead
    }

    /**
     * Performs initialization that should not be overridden by subclasses.
     */
    @PostConstruct
    private void init() {
        jdbcTemplate.setDataSource(getMainDataSource());
    }

    public DataSource getMainDataSource() {
        return mainDataSource;
    }

    public void setJdbcDataSource(DataSource mainDataSource) {
        this.mainDataSource = mainDataSource;
    }

    @Override
    public DimensionJoiner getDimensionJoiner() {
        return dimensionJoiner;
    }

    public void setDimensionJoiner(DimensionJoiner dimensionJoiner) {
        this.dimensionJoiner = dimensionJoiner;
    }

    private void joinDimensions() {
        if (dimensionJoiner != null) {
            dimensionJoiner.joinDimensions();
        }
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public boolean isCaseSensitiveLike() {
        return true;
    }

    @Override
    public int hashCode() {
        int value = getClass().hashCode();
        value = (value * 31) + String.valueOf(getBaseDetailsQuery()).hashCode();
        return value;
    }

    @Override
    public boolean equals(final Object object) {
        if (object != null) {
            if (this == object) {
                return true;
            } else if (getClass().equals(object.getClass())) {
                final JdbcOeDataSource other = (JdbcOeDataSource) object;

                return String.valueOf(getBaseDetailsQuery()).equals(String.valueOf(other.getBaseDetailsQuery()));
            }
        }
        return false;
    }
}
