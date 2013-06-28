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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Once constructed, serves as a read-only reference for a child table's schema details.
 */
public class ChildTableDetails extends ParentTableDetails {

    private Map<String, String> fksToParent;
    private Map<String, String> pksFromParent;

    private String possibleValuesDsName;
    private Map<String, String> possibleValuesDsFks;
    private List<String> possibleValuesDsResults;
    private List<String> possibleValuesDsFilters;

    /**
     * @param params
     */
    @SuppressWarnings("unchecked")
    public ChildTableDetails(Map<String, Object> params) {
        super();
        setTableName((String) params.get("tableName"));
        setEditDimensionIds((List<String>) params.get("columns"));
        setPks((HashSet<String>) params.get("pks"));
        setFksToParent((Map<String, String>) params.get("fksToParent"));
        setPossibleValuesDsName((String) params.get("possibleValuesDsName"));
        setPossibleValuesDsFks((Map<String, String>) params.get("possibleValuesDsFks"));
        setPossibleValuesDsResults((List<String>) params.get("possibleValuesDsResults"));
        setPossibleValuesDsFilters((List<String>) params.get("possibleValuesDsFilters"));
        setMeta(params.get("metaData"));
    }

    public Map<String, String> getPksFromParentToChild() {
        return pksFromParent;
    }

    public Map<String, String> getFksToParent() {
        return fksToParent;
    }

    public void setFksToParent(Map<String, String> fksToParent) {
        this.fksToParent = fksToParent;
        this.pksFromParent = new HashMap<String, String>();
        for (String fk : fksToParent.keySet()) {
            pksFromParent.put(fksToParent.get(fk), fk);
        }
    }

    public void setPossibleValuesDsName(String possibleValuesDsName) {
        this.possibleValuesDsName = possibleValuesDsName;
    }

    public String getPossibleValuesDsName() {
        return possibleValuesDsName;
    }

    public void setPossibleValuesDsFks(Map<String, String> possibleValuesDsFks) {
        this.possibleValuesDsFks = possibleValuesDsFks;
    }

    public Map<String, String> getPossibleValuesDsFks() {
        return possibleValuesDsFks;
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
}
