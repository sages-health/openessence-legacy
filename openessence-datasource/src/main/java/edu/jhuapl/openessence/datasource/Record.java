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

import java.util.Map;
import java.util.Set;


/**
 * Represents an individual record returned by a query.
 */
public interface Record {

    /**
     * Get a set of all result ids.
     *
     * @return a set of result ids
     */
    public Set<String> getResultIds();

    /**
     * Gets the value associated with this result id.  The type of the value is determined by the Dimension of the result.
     *
     * @param resultId The id of the result
     * @return The value of the result.
     */
    public Object getValue(String resultId);

    /**
     * Gets the dimension associated with this result id.
     *
     * @param resultId The id of the result.
     * @return The Dimension of the result.
     */
    public Dimension getDimension(String resultId);

    /**
     * Gets all dimensions for the record. Dimensions are mapped by the string dimension Id
     *
     * @return mapping of dimensionIds to dimensions for the Record
     */
    public Map<String, Dimension> getDimensions();

    /**
     * Gets all values for the record. Values are mapped by the string dimension Id
     *
     * @return mapping of dimensionIds to values for the Record
     */
    public Map<String, Object> getValues();
}
