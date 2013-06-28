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

package edu.jhuapl.openessence.datasource.jdbc.entry;

import edu.jhuapl.openessence.datasource.Dimension;
import edu.jhuapl.openessence.datasource.entry.TableAwareRecord;
import edu.jhuapl.openessence.datasource.jdbc.QueryRecord;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TableAwareQueryRecord extends QueryRecord implements TableAwareRecord {

    private String tablename;
    private Set<String> pkIds;
    private Map<String, Dimension> editDimensions;

    /**
     * Constructor to build TableAwareQueryRecord only cognizant of it's dimensions and it's values. This object should
     * be added to a collection and passed to a ChildRecordSet contstructor (which will set the tablename and pkids)
     *
     * @param dimensions map dimension id to dimension
     * @param values     map dimension id to value
     */
    public TableAwareQueryRecord(Map<String, Dimension> dimensions, Map<String, Object> values) {
        super(dimensions, values);
        this.editDimensions = dimensions;
        this.tablename = null;
        this.pkIds = new HashSet<String>();
    }

    public TableAwareQueryRecord(String tableName, Set<String> pkIds, Map<String, Dimension> dimensions,
                                 Map<String, Object> values) {
        super(dimensions, values);
        this.tablename = tableName;
        this.editDimensions = dimensions;
        this.pkIds = pkIds;
    }

    public TableAwareQueryRecord(TableAwareQueryRecord tableAwareQueryRecord) {
        super(new HashMap<String, Dimension>(tableAwareQueryRecord.getDimensions()),
              new HashMap<String, Object>(tableAwareQueryRecord.getValues()));
        this.tablename = tableAwareQueryRecord.getTableName();
        this.editDimensions = new HashMap<String, Dimension>(tableAwareQueryRecord.getEditDimensions());
        this.pkIds = new HashSet<String>(tableAwareQueryRecord.getPrimaryKeyIds());
    }

    @Override
    // TODO: review use case for usefulness. this information is available in TableDetails
    public Set<String> getPrimaryKeyIds() {
        return this.pkIds;
    }

    @Override
    public Map<String, Object> getPrimaryKeysWithValues() {
        Map<String, Object> pkvalmap = new HashMap<String, Object>();
        for (String pkId : pkIds) {
            pkvalmap.put(pkId, super.getValue(pkId));
        }
        return pkvalmap;
    }

    @Override
    public void setPrimaryKeyIds(Set<String> pkIds) {
        this.pkIds.addAll(pkIds);
    }

    @Override
    public String getTableName() {
        return this.tablename;
    }


    public Map<String, Dimension> getEditDimensions() {
        return editDimensions;
    }


    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TableAwareQueryRecord) {
            TableAwareQueryRecord r = (TableAwareQueryRecord) o;
            return super.getValues().equals(r.getValues()) && editDimensions.equals(r.editDimensions) && tablename
                    .equals(r.getTableName()) && pkIds.equals(r.getPrimaryKeyIds());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.getValues().hashCode() + editDimensions.hashCode();
    }

    @Override
    public String toString() {
        String res = "{";
        boolean first = true;
        for (String key : super.getValues().keySet()) {
            if (first) {
                first = false;
            } else {
                res += ", ";
            }
            res += key + ":" + super.getValues().get(key) + "(" + editDimensions.get(key).getSqlType() + ")";
        }
        res += "}";
        return res;
    }
}
