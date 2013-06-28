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

package edu.jhuapl.openessence.datasource.entry;

import java.util.List;
import java.util.Set;

public class ParentTableDetails {

    private String tableName;
    private Set<String> pks;
    private List<String> editDimIds;
    private String mapRequestId;
    private String sequenceForMapRequestId;

    private Object meta;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Set<String> getPks() {
        return pks;
    }

    public void setPks(Set<String> pks) {
        this.pks = pks;
    }

    public List<String> getDimensionIds() {
        return editDimIds;
    }

    public void setEditDimensionIds(List<String> editDimIds) {
        this.editDimIds = editDimIds;
    }

    public String getMapRequestId() {
        return mapRequestId;
    }

    public void setMapRequestId(String mapRequestId) {
        this.mapRequestId = mapRequestId;
    }

    public String getSequenceForMapRequestId() {
        return sequenceForMapRequestId;
    }

    public void setSequenceForMapRequestId(String sequenceForMapRequestId) {
        this.sequenceForMapRequestId = sequenceForMapRequestId;
    }

    public void setMeta(Object meta) {
        this.meta = meta;
    }

    public Object getMeta() {
        return meta;
    }
}
