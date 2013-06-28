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

import edu.jhuapl.openessence.datasource.FieldType;
import edu.jhuapl.openessence.datasource.Filter;
import edu.jhuapl.openessence.datasource.FilterDimension;
import edu.jhuapl.openessence.datasource.OeDataSourceAccessException;
import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.Relation;
import edu.jhuapl.openessence.datasource.jdbc.filter.EqFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.GtFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.GteqFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.ILikeFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.InFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.LikeFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.LtFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.LteqFilter;
import edu.jhuapl.openessence.datasource.jdbc.filter.NeqFilter;
import edu.jhuapl.openessence.datasource.util.FilterDimensionUtils;

public class FilterDimensionBeanAdapter extends DimensionBeanAdapter implements FilterDimension {

    public FilterDimensionBeanAdapter(DimensionBean bean, JdbcOeDataSource ds) {
        super(bean, ds);
    }

    @Override
    public Relation[] getRelations() {
        final Relation[] relations;
        final FieldType type = bean.getSqlType();

        switch (type) {
            case DATE:
            case DATE_TIME:
            case INTEGER:
            case LONG:
            case FLOAT:
            case DOUBLE:
                relations =
                        new Relation[]{Relation.IN, Relation.EQ, Relation.NEQ, Relation.GT, Relation.GTEQ, Relation.LT,
                                       Relation.LTEQ};
                break;
            case TEXT:
                relations = new Relation[]{Relation.IN, Relation.EQ, Relation.NEQ, Relation.LIKE};
                break;
            case BOOLEAN:
                relations = new Relation[]{Relation.EQ, Relation.NEQ};
                break;
            default:
                throw new AssertionError("Unhandled FieldType " + type);
        }

        return relations;
    }

    @Override
    public boolean nullFiltersAllowed() {
        return bean.getNullFiltersAllowed();
    }

    @Override
    public Filter makeFilter(final Relation relation, final Object argument) throws OeDataSourceException {
        FilterDimensionUtils.checkFilter(relation, argument, this.getRelations(), this.nullFiltersAllowed());

        switch (relation) {
            case EQ:
                return new EqFilter(getId(), argument);
            case NEQ:
                return new NeqFilter(getId(), argument);
            case GT:
                return new GtFilter(getId(), argument);
            case GTEQ:
                return new GteqFilter(getId(), argument);
            case LT:
                return new LtFilter(getId(), argument);
            case LTEQ:
                return new LteqFilter(getId(), argument);
            case LIKE:
                try {
                    return ds.isPostgreSqlDBMS() && !ds.isCaseSensitiveLike() ? new ILikeFilter(getId(), argument)
                                                                              : new LikeFilter(getId(), argument);
                } catch (final OeDataSourceAccessException odsae) {
                    throw new OeDataSourceException(odsae);
                }
            case IN:
                return new InFilter(getId(), (Object[]) argument);
            default:
                throw new AssertionError("Unexpected relation " + relation + ".");
        }
    }
}
