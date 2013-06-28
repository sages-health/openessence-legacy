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
import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.ui.PossibleValuesConfiguration;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collections;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.PUBLIC_ONLY)
public class DimensionBeanAdapter implements Dimension {

    protected final DimensionBean bean;
    protected final JdbcOeDataSource ds;

    public DimensionBeanAdapter(DimensionBean bean, JdbcOeDataSource ds) {
        this.bean = bean;
        this.ds = ds;
    }

    @JsonIgnore
    public DimensionBean getDimensionBean() {
        return bean;
    }

    @JsonProperty("name")
    @Override
    public String getId() {
        return bean.getId();
    }

    @JsonProperty("type")
    @Override
    public FieldType getSqlType() {
        return bean.getSqlType();
    }

    @JsonIgnore
    @Override
    public String getSqlCol() {
        return bean.getSqlCol();
    }

    @JsonIgnore
    @Override
    public String getSpecialSql() {
        return bean.getSpecialSql();
    }

    @JsonIgnore
    @Override
    public boolean hasSpecialSql() {
        return bean.hasSpecialSql();
    }

    @JsonProperty("meta")
    @Override
    public Map<String, Object> getMetaData() {
        return bean.getMetaData() != null ? Collections.unmodifiableMap(bean.getMetaData()) : null;
    }

    @JsonProperty("possibleValues")
    @Override
    public PossibleValuesConfiguration getPossibleValuesConfiguration() throws OeDataSourceException {
        return ds.getPossibleValuesConfigurationFromDimensionBean(bean);
    }

    @Override
    public String getDisplayName() {
        return bean.getDisplayName();
    }

    @Override
    public String toString() {
        if (bean != null) {
            return getClass().getSimpleName() + ": " + bean.toString();
        }
        return super.toString();
    }

    public String getFilterBeanId() {
        return bean.getFilterBeanId();
    }

    @Override
    public int hashCode() {
        int value = getClass().hashCode();
        value = (value * 31) + bean.hashCode();
        value = (value * 31) + ds.hashCode();
        return value;
    }

    @Override
    public boolean equals(final Object object) {
        if (object != null) {
            if (this == object) {
                return true;
            } else if (getClass().equals(object.getClass())) {
                final DimensionBeanAdapter other = (DimensionBeanAdapter) object;

                return bean.equals(other.bean) && ds.equals(other.ds);
            }
        }
        return false;
    }
}
