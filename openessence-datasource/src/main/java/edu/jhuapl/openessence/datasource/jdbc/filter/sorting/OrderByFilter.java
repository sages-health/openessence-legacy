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

package edu.jhuapl.openessence.datasource.jdbc.filter.sorting;

import edu.jhuapl.openessence.datasource.jdbc.filter.OneArgOpFilter;

public class OrderByFilter extends OneArgOpFilter {

    public final String ASC = SortingDirection.ASC.getSqlSnippet();
    public final String DESC = SortingDirection.DESC.getSqlSnippet();

    public OrderByFilter(String filterId, String operator) {
        super(filterId, null, operator);  // todo cleanup make new class...not really oneargop...
    }

    @Override
    public String getOperator() {
        if (super.getOperator() == null || super.getOperator().equalsIgnoreCase(ASC)) {
            return ASC;
        }
        if (super.getOperator().equalsIgnoreCase(DESC)) {
            return DESC;
        }
        return ASC;
    }

    @Override
    public String getSqlSnippet(String colName) {
        return colName + " " + this.getOperator() + " ";
    }
}
