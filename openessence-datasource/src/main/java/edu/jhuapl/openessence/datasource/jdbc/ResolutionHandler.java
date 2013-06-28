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

import edu.jhuapl.openessence.datasource.OeDataSourceException;

import java.util.List;

public interface ResolutionHandler {

    /**
     * Gets the designated categorization of the ResolutionHandler: i.e. WEEKLY, DAILY, YEARLY or any other type of
     * category the system would need to know to select the correct implementation of the ResolutionHandler. If supplying
     * a custom ResolutionHandler, this category will fall through and not be used.
     *
     * @return Enum value of the categorization of the ResolutionHandler
     */
    public Enum getCategory();

    /**
     * Gets the resolution colums for the inputted dimensionId
     *
     * @param dimCol dimension id
     */
    public List<String> getResolutionColumns(String dimCol, String timezone) throws OeDataSourceException;

    /**
     *
     * @param resolutionValues
     * @return
     * @throws OeDataSourceException
     */
    public Object buildKernel(Object[] resolutionValues) throws OeDataSourceException;
}
