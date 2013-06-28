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

package edu.jhuapl.openessence.groovy;

import edu.jhuapl.openessence.datasource.FieldType;
import edu.jhuapl.openessence.datasource.jdbc.DimensionBean;
import edu.jhuapl.openessence.datasource.jdbc.dataseries.JdbcDataSeriesSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public abstract class GroovyDataSource extends JdbcDataSeriesSource {

    protected List<DimensionBean> dimensionBeans;

    protected GroovyDataSource() {
        dimensionBeans = new ArrayList<DimensionBean>();
    }

    protected DimensionBean init(Map<String, Object> params) {
        DimensionBean bean = new DimensionBean();
        bean.setId((String) params.get("id"));
        bean.setSqlCol((String) params.get("sqlCol"));
        bean.setSqlType((FieldType) params.get("sqlType"));
        bean.setMetaData((Map<String, Object>) params.get("metaData"));
        bean.setIsResult((params.get("isResult") == null) ? false : (Boolean) params.get("isResult"));
        bean.setIsChildResult((params.get("isChildResult") == null) ? false : (Boolean) params.get("isChildResult"));
        bean.setIsFilter((params.get("isFilter") == null) ? false : (Boolean) params.get("isFilter"));
        bean.setIsGrouping((params.get("isGrouping") == null) ? false : (Boolean) params.get("isGrouping"));
        bean.setIsAccumulation((params.get("isAccumulation") == null) ? false : (Boolean) params.get("isAccumulation"));
        bean.setSqlColAlias((params.get("sqlColAlias") == null) ? "" : (String) params.get("sqlColAlias"));
        bean.setFilterBeanId((params.get("filterBeanId") == null) ? "" : (String) params.get("filterBeanId"));
        dimensionBeans.add(bean);
        return bean;
    }

}
