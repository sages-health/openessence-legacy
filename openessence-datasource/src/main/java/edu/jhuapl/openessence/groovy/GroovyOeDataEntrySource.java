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
import edu.jhuapl.openessence.datasource.entry.ChildTableDetails;
import edu.jhuapl.openessence.datasource.entry.ParentTableDetails;
import edu.jhuapl.openessence.datasource.jdbc.DimensionBean;
import edu.jhuapl.openessence.datasource.jdbc.entry.JdbcOeDataEntrySource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class GroovyOeDataEntrySource extends JdbcOeDataEntrySource {

    protected List<DimensionBean> dimensionBeans;

    protected GroovyOeDataEntrySource() {
        dimensionBeans = new ArrayList<DimensionBean>();
    }

    /**
     * Initializes a DimensionBean with the input params
     *
     * @param params include{id[String], sqlCol, sqlType, metaData, isResult, isEdit, isChildEdit, specialSql, isAutoGen,
     *               isFilter, isGrouping}
     * @return dimension bean representation of the database table column
     */
    protected DimensionBean init(Map<String, Object> params) {
        DimensionBean bean = new DimensionBean();
        bean.setId((String) params.get("id"));
        bean.setSqlCol((String) params.get("sqlCol"));
        bean.setSqlType((FieldType) params.get("sqlType"));
        bean.setMetaData((Map<String, Object>) params.get("metaData"));
        bean.setIsResult((params.get("isResult") == null) ? false : (Boolean) params.get("isResult"));
        bean.setIsChildResult((params.get("isChildResult") == null) ? false : (Boolean) params.get("isChildResult"));
        bean.setIsEdit((params.get("isEdit") == null) ? false : (Boolean) params.get("isEdit"));
        bean.setIsChildEdit((params.get("isChildEdit") == null) ? false : (Boolean) params.get("isChildEdit"));
        bean.setHasSpecialSql((params.get("specialSql") == null) ? false : true);
        bean.setSpecialSql((params.get("specialSql") == null) ? null : (String) params.get("specialSql"));
        bean.setIsAutoGen((params.get("isAutoGen") == null) ? false : (Boolean) params.get("isAutoGen"));
        bean.setIsFilter((params.get("isFilter") == null) ? false : (Boolean) params.get("isFilter"));
        bean.setIsGrouping((params.get("isGrouping") == null) ? false : (Boolean) params.get("isGrouping"));
        dimensionBeans.add(bean);
        return bean;
    }

    /**
     * Adds a child table's schema details to the list of child table details
     *
     * @param params include{tablename[String], columns[List<String>], pks[Set<String>], fksToParent[Map<String,String>]}
     * @return the ChildTableDetails initialized by the input params
     */
    protected ChildTableDetails addChildTable(final Map<String, Object> params) {
        final ChildTableDetails details = new ChildTableDetails(params);
        addChildTableDetails(details);

        return details;
    }

    /**
     * Initializes the parent/master table's schema details and sets it on the datasource
     *
     * @param params include{pks[Set<String>]}
     * @return the ParentTableDetails initialized by the input params
     */
    protected ParentTableDetails addMasterTable(Map<String, Object> params) {
        ParentTableDetails pDetails = new ParentTableDetails();
        pDetails.setPks((HashSet<String>) params.get("pks"));
        pDetails.setMapRequestId((String) params.get("mapRequestId"));
        pDetails.setSequenceForMapRequestId((String) params.get("sequence"));
        // todo--PHASE OUT setTableName IN THE GROOVY DS FILE
        Object tparam = params.get("tableName");
        super.setTableName(tparam.toString());
        String tname = ((tparam != null && !tparam.equals("")) ? (String) tparam : (getTableName()));
        pDetails.setTableName(tname);
        super.setParentTableDetails(pDetails);
        return pDetails;
    }

    /**
     setDimensions(dimensionBeans);  // @see GroovyOeDataEntrySource.dimensionBeans
     */
}
