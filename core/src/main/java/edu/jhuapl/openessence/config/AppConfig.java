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

import edu.jhuapl.graphs.jfreechart.JFreeChartGraphSource;
import edu.jhuapl.openessence.datasource.jdbc.JdbcOeDataSource;
import edu.jhuapl.openessence.i18n.InspectableResourceBundleMessageSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;

/**
 * Bootstrap the application. This configuration class is guaranteed to always be loaded.
 */
@Configuration
@ComponentScan(basePackages = "edu.jhuapl.openessence")
public class AppConfig {

    @Inject
    private EnvironmentConfig envConfig;

    @Inject
    private Environment env;

    @Bean
    public DataSourceTransactionManager transactionManager() {
        DataSourceTransactionManager manager = new DataSourceTransactionManager();
        manager.setDataSource(envConfig.mainDataSource());
        return manager;
    }

    // DataSourceLoader makes sense as an @Component, but component scanning is
    // not guaranteed to have run before AppConfig is wired
    @Bean
    public DataSourceLoader dataSourceLoader() {
        return new DataSourceLoader();
    }

    /**
     * @return a {@link ConcurrentMap} (so that data sources can be swapped in and out)
     */
    @Bean
    public ConcurrentMap<String, JdbcOeDataSource> dataSources() {
        return dataSourceLoader().loadDataSources(new ConcurrentHashMap<String, JdbcOeDataSource>());
    }

    @Bean
    public InspectableResourceBundleMessageSource messageSource() {
        InspectableResourceBundleMessageSource messageSource = new InspectableResourceBundleMessageSource();

        messageSource.setBasenames("classpath:/i18n/messages");

        InspectableResourceBundleMessageSource parentMessageSource = new InspectableResourceBundleMessageSource();
        parentMessageSource.setBasename("classpath:/i18n/core-messages");
        messageSource.setParentMessageSource(parentMessageSource);

        return messageSource;
    }

    /**
     * Bean version of {@link InspectableResourceBundleMessageSource#getLocales() messageSource.getLocales()}. Clients
     * should try to use this instead, since {@code getLocales()} touches the filesystem every time it is called, while
     * this bean is only instantiated once on startup.
     */
    @Bean
    public Collection<Locale> supportedLocales() throws IOException {
        return messageSource().getLocales();
    }

    @Bean
    public JFreeChartGraphSource graphSource() {
        return new JFreeChartGraphSource();
    }

}
