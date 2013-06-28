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

package edu.jhuapl.openessence.datasource.jdbc.filter;

import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.jdbc.JdbcOeDataSource;

import java.util.LinkedList;
import java.util.List;

public abstract class MultiFilterCombiner implements SqlGeneratingFilter {

    protected List<SqlGeneratingFilter> subFilters;
    protected String combiningOp;

    protected MultiFilterCombiner(List<SqlGeneratingFilter> subFilters, String combiningOp) {
        this.subFilters = subFilters;
        this.combiningOp = combiningOp;
    }

    @Override
    public String getSqlSnippet(JdbcOeDataSource ds) {
        StringBuilder sb = new StringBuilder();
        if (!subFilters.isEmpty()) {
            sb.append("(");
            sb.append(subFilters.get(0).getSqlSnippet(ds));
            for (int i = 1; i < subFilters.size(); i += 1) {
                sb.append(" ");
                sb.append(combiningOp);
                sb.append(" ");
                sb.append(subFilters.get(i).getSqlSnippet(ds));
            }

            sb.append(")");
        }

        return sb.toString();
    }

    public List<Object> getArguments(JdbcOeDataSource ds) {
        List<Object> args = new LinkedList<Object>();
        for (SqlGeneratingFilter subFilter : subFilters) {
            args.addAll(subFilter.getArguments(ds));
        }

        return args;
    }

    public void checkFilter(JdbcOeDataSource ds) throws OeDataSourceException {
        for (SqlGeneratingFilter subFilter : subFilters) {
            subFilter.checkFilter(ds);
        }
    }
}
