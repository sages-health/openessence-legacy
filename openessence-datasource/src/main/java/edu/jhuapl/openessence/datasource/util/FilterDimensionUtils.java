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

package edu.jhuapl.openessence.datasource.util;

import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.Relation;

/**
 * Utility methods for FilterDimensions.
 */
public class FilterDimensionUtils {

    public static void checkFilter(final Relation relation,
                                   final Object argument,
                                   final Relation[] relations,
                                   final boolean nullAllowed) throws OeDataSourceException {
        if (relation == Relation.IN) {
            if (argument instanceof Object[]) {
                final Object[] arguments = (Object[]) argument;
                if (arguments.length == 0) {
                    throw new OeDataSourceException("IN operator must have > 0 arguments");
                }

                if (!nullAllowed) {
                    for (final Object o : arguments) {
                        checkNonNull(o);
                    }
                }
            } else {
                throw new OeDataSourceException("Wrong arg type for Relation \"" + Relation.IN +
                                                "\", not an array.");
            }
        } else if (!relationContains(relations, relation)) {
            throw new OeDataSourceException("Unsupported Relation \"" + relation + "\".");
        } else if (!nullAllowed) {
            checkNonNull(argument);
        }
    }

    public static boolean relationContains(Relation[] relations, Relation target) {
        for (Relation r : relations) {
            if (r == target) {
                return true;
            }
        }
        return false;
    }

    public static void checkNonNull(Object o) throws OeDataSourceException {
        if (o == null) {
            throw new OeDataSourceException("Null argument not allowed");
        }
    }

    // Do not instantiate
    private FilterDimensionUtils() {
    }
}
