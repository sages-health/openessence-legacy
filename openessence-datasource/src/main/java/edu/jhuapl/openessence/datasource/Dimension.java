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

package edu.jhuapl.openessence.datasource;

import edu.jhuapl.openessence.datasource.ui.PossibleValuesConfiguration;

import java.util.Map;

/**
 * A dimension of the data source.
 */
public interface Dimension {

    /**
     * The id of the dimension
     *
     * @return the id of the dimension
     */
    public String getId();

    /**
     * The name to display for this dimension. Used in places like dynamic dimensions which cannot pre-supply id -> display
     * name translations in resource files.
     */
    public String getDisplayName();

    /**
     * The type of the dimension.
     *
     * @return The type
     */
    public FieldType getSqlType();

    /**
     * The sql col name of the dimension
     *
     * @return The Sql column name
     */
    public String getSqlCol();

    /**
     * The special sql for the dimension
     *
     * @return The special Sql to use for prepared statement
     */
    public String getSpecialSql();

    /**
     * The special sql for the dimension
     *
     * @return The special Sql to use for prepared statement
     */
    public boolean hasSpecialSql();

    /**
     * Gets the metadata for a dimension.
     *
     * @return the metadata associated with a dimension
     */
    public Map<String, Object> getMetaData();

    /**
     * Gets the possible values configuration object for a dimension.
     *
     * @return a configuration object that has a possible value data source id and optional parameters for results and
     *         filter dimensions.
     */
    public PossibleValuesConfiguration getPossibleValuesConfiguration() throws OeDataSourceException;


    /**
     * Gets filter bean Id for results bean
     *
     * @return filter bean Id for the results bean, else returns null.
     */
    public String getFilterBeanId();
}
