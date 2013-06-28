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

package edu.jhuapl.openessence.web.util;

import edu.jhuapl.openessence.datasource.OeDataSourceAccessException;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.dao.UncategorizedDataAccessException;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a generic, but user-friendly message to display to the UI when certain types of common data access related
 * exceptions occur.
 */
public class SQLExceptionHandlerUtil {

    //Todo: Internationalize this stuff
    @SuppressWarnings("serial")
    private static Map<Object, String> errorMsg = new HashMap<Object, String>() {
        {
            put(DuplicateKeyException.class.getCanonicalName(), "Duplicate value violates uniqueness constraint.");
            put(TypeMismatchDataAccessException.class.getCanonicalName(),
                "Mismatch between Java type and database type occurred: for example on an attempt to set an object of the wrong type in an RDBMS column.");
            put(UncategorizedDataAccessException.class.getCanonicalName(),
                "Something went wrong with the underlying resource. Check logs for details.");
        }
    };

    public static String getMessageToGiveUI(OeDataSourceAccessException e) {
        Object causingclass = e.getCause().getClass().getCanonicalName();
        return (errorMsg.get(causingclass));
    }
}
