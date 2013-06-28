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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import javax.inject.Inject;
import javax.sql.DataSource;

@Configuration
public class MapConfig {

    /**
     * Key of map {@link DataSource} in {@link Environment}.
     */
    public static final String MAP_DB = "jdbc/mapdb";

    /**
     * Name of the map {@link PropertySource}.
     */
    public static final String MAP_PROPERTY_SOURCE = "maps";

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private ConfigurableEnvironment env;

    @Inject
    private EnvironmentConfig environmentConfig;

    @Bean
    public DataSource mapDataSource() {
        DataSource ds = env.getProperty(MAP_DB, DataSource.class);
        if (ds == null) {
            log.info("Map data source not found in environment. Using main data source instead");
            return environmentConfig.mainDataSource();
        } else {
            return ds;
        }
    }

    @Bean
    public String wmsServer() {
        return env.getProperty("wmsserver", "/geoserver/wms");
    }

    @Bean
    public String wfsServer() {
        return env.getProperty("wfsserver", "/geoserver/wfs");
    }

    @Bean
    public String postgresCleanup() {
        return env.getProperty("postgres.cleanup", "1 minute");
    }

}
