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

package edu.jhuapl.openessence.config;

import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.jdbc.JdbcOeDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

@Component
public class DataSourceConverter implements Converter<String, JdbcOeDataSource> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Resource
    private ConcurrentMap<String, JdbcOeDataSource> dataSources;

    @Override
    public JdbcOeDataSource convert(String source) {
        JdbcOeDataSource dataSource = dataSources.get(source);
        if (dataSource == null) {
            log.error("No data source named {} found", source);
            throw new OeDataSourceException("Unknown data source " + source);
        }

        Set<String> roles = dataSource.getRoles();

        if (roles.isEmpty()) {
            return dataSource;
        } else {
            // Get roles of the authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                // This can happen if we're not running behind a ServletRequest, e.g. in an @Async task.
                // It's not a security risk to return the data source because we've already caught non-logged in
                // users (who would also have a null Authentication) at the request level. The remaining checks in this
                // method are to make sure logged in users have appropriate access rights to the data source, but
                // that's not an issue if there's no user associated with this action.
                return dataSource;
            }

            for (GrantedAuthority eachAuthority : authentication.getAuthorities()) {
                if (roles.contains(eachAuthority.toString())) {
                    return dataSource;
                }
            }

            log.warn("User {} is not authorized to access data source {}", authentication.getName(), source);
            throw new AccessDeniedException("Not authorized to access data source");
        }
    }

}
