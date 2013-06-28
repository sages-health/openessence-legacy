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
import edu.jhuapl.openessence.datasource.FieldType;
import edu.jhuapl.openessence.datasource.jdbc.filter.PossibleValuesProvider;

import java.util.List;
import java.util.Map;

public class DimensionBean {

    private String id;
    /**
     * Sql related attributes
     */
    private String sqlCol;
    private FieldType sqlType;
    private boolean hasSpecialSql;
    private String specialSql;
    private boolean isAutoGen;

    private Map<String, Object> metaData;

    private boolean nullFiltersAllowed;

    private boolean isResult;
    private boolean isChildResult;
    private boolean isEdit;
    private boolean isChildEdit;
    private boolean isFilter;
    private boolean isGrouping;
    private boolean isAccumulation;
    private String sqlColAlias;
    private String displayName;
    private String filterBeanId;

    private PossibleValuesProvider possibleValuesProvider;

    private String possibleValuesDsName;
    private List<String> possibleValuesDsResults;
    private List<String> possibleValuesDsFilters;
    private List<List<Object>> possibleValuesDsData;

    private Map<String, ResolutionHandler> resolutionHandlers;

    public DimensionBean() {

    }

    /**
     * Copy an existing Dimension.
     */
    public DimensionBean(Dimension dimension) {
        setId(dimension.getId());
        setDisplayName(dimension.getDisplayName());
        setMetaData(dimension.getMetaData());
        setSpecialSql(dimension.getSpecialSql());
        setSqlCol(dimension.getSqlCol());
        setSqlType(dimension.getSqlType());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSqlCol() {
        return sqlCol;
    }

    public void setSqlCol(String sqlCol) {
        this.sqlCol = sqlCol;
    }

    public FieldType getSqlType() {
        return sqlType;
    }

    public void setSqlType(FieldType type) {
        this.sqlType = type;
    }

    public boolean hasSpecialSql() {
        return hasSpecialSql;
    }

    public void setHasSpecialSql(boolean hasSpecialSql) {
        this.hasSpecialSql = hasSpecialSql;
    }

    public String getSpecialSql() {
        return specialSql;
    }

    public void setSpecialSql(String specialSql) {
        this.specialSql = specialSql;
    }

    public boolean isAutoGen() {
        return isAutoGen;
    }

    public void setIsAutoGen(boolean autoGen) {
        isAutoGen = autoGen;
    }

    public void setNullFiltersAllowed(boolean nullFiltersAllowed) {
        this.nullFiltersAllowed = nullFiltersAllowed;
    }

    public boolean getNullFiltersAllowed() {
        return nullFiltersAllowed;
    }

    public void setIsResult(boolean isResult) {
        this.isResult = isResult;
    }

    public boolean getIsResult() {
        return isResult;
    }

    public void setIsChildResult(boolean isChildResult) {
        this.isChildResult = isChildResult;
    }

    public boolean getIsChildResult() {
        return isChildResult;
    }

    public void setIsEdit(boolean isEdit) {
        this.isEdit = isEdit;
    }

    public boolean getIsEdit() {
        return isEdit;
    }

    public boolean getIsChildEdit() {
        return isChildEdit;
    }

    public void setIsChildEdit(boolean childEdit) {
        isChildEdit = childEdit;
    }

    public void setIsFilter(boolean isFilter) {
        this.isFilter = isFilter;
    }

    public boolean getIsFilter() {
        return isFilter;
    }

    public void setIsGrouping(boolean isGrouping) {
        this.isGrouping = isGrouping;
    }

    public boolean getIsGrouping() {
        return isGrouping;
    }

    public void setIsAccumulation(boolean isAccumulation) {
        this.isAccumulation = isAccumulation;
    }

    public boolean getIsAccumulation() {
        return isAccumulation;
    }

    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void setResolutionHandlers(Map<String, ResolutionHandler> resolutionHandlers) {
        this.resolutionHandlers = resolutionHandlers;
    }

    public Map<String, ResolutionHandler> getResolutionHandlers() {
        return resolutionHandlers;
    }

    public void setPossibleValuesProvider(PossibleValuesProvider provider) {
        this.possibleValuesProvider = provider;
    }

    public PossibleValuesProvider getPossibleValuesProvider() {
        return possibleValuesProvider;
    }

    public void setPossibleValuesDsName(String possibleValuesDsName) {
        this.possibleValuesDsName = possibleValuesDsName;
    }

    public String getPossibleValuesDsName() {
        return possibleValuesDsName;
    }

    public List<String> getPossibleValuesDsResults() {
        return possibleValuesDsResults;
    }

    public void setPossibleValuesDsResults(List<String> possibleValuesDsResults) {
        this.possibleValuesDsResults = possibleValuesDsResults;
    }

    public List<String> getPossibleValuesDsFilters() {
        return possibleValuesDsFilters;
    }

    public void setPossibleValuesDsFilters(List<String> possibleValuesDsFilters) {
        this.possibleValuesDsFilters = possibleValuesDsFilters;
    }

    public List<List<Object>> getPossibleValuesDsData() {
        return possibleValuesDsData;
    }

    public void setPossibleValuesDsData(List<List<Object>> possibleValuesDsData) {
        this.possibleValuesDsData = possibleValuesDsData;
    }

    public void setSqlColAlias(String sqlAlias) {
        this.sqlColAlias = sqlAlias;
    }

    public String getSqlColAlias() {
        return this.sqlColAlias;
    }

    @Override
    public String toString() {
        return "DimensionBean: id=" + id;
    }

    @Override
    public int hashCode() {
        return 31 + ((id == null) ? 0 : id.hashCode());
    }

    /**
     * Two {@code DimensionBeans} are equal iff their IDs are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DimensionBean other = (DimensionBean) obj;
        if (id == null) {
            return other.getId() == null;
        } else {
            return id.equals(other.getId());
        }
    }

    /**
     * @see Dimension#getDisplayName()
     */
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFilterBeanId() {
        return filterBeanId;
    }

    public void setFilterBeanId(String filterBeanId) {
        this.filterBeanId = filterBeanId;
    }
}
