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

import edu.jhuapl.openessence.datasource.jdbc.JdbcOeDataSource;

import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;

/**
 * Loads and initializes {@link JdbcOeDataSource} instances.
 *
 * A singleton instance of this class is added to the application context by {@link AppConfig#dataSourceLoader()}. It's
 * not a @Component because it's needed before component scanning takes place.
 */
public class DataSourceLoader {

    private final Logger log = LoggerFactory.getLogger(getClass());

    // Spring configures one of these for every ApplicationContext
    @Inject
    private AutowireCapableBeanFactory beanFactory;

    @Inject
    private ResourcePatternResolver resourcePatternResolver;

    /**
     * @param existingDataSources this can't be injected because the code that initializes the data sources delegates to
     *                            this method
     * @return returns dataSources argument, for chaining
     * @see AppConfig#dataSources()
     */
    public ConcurrentMap<String, JdbcOeDataSource> loadDataSources(
            ConcurrentMap<String, JdbcOeDataSource> existingDataSources) {
        try {
            URL groovyRoot = resourcePatternResolver.getResource("classpath:/ds").getURL();
            Resource[] groovyResources = resourcePatternResolver.getResources("classpath:/ds/*.groovy");

            String[] groovyScriptNames = new String[groovyResources.length];
            for (int i = 0; i < groovyResources.length; i++) {
                groovyScriptNames[i] = FilenameUtils.getName(groovyResources[i].getFile().getPath());
            }
            // make sure our loading order hack works, e.g. that ASexes.groovy is loaded before BPatients.groovy
            // Windows and OSX tend to return resources in alphabetical order, but Linux (ext4?) doesn't
            Arrays.sort(groovyScriptNames);

            GroovyScriptEngine groovyScriptEngine = new GroovyScriptEngine(new URL[]{groovyRoot});

            for (String scriptName : groovyScriptNames) {
                try {
                    log.info("Loading Groovy script {}", scriptName);
                    Class<?> clazz = groovyScriptEngine.loadScriptByName(scriptName);
                    JdbcOeDataSource ds = (JdbcOeDataSource) beanFactory.createBean(clazz);

                    existingDataSources.put(ds.getDataSourceId(), ds);

                    // legacy code expects each DS to be a named bean
                    // TODO remove this when legacy code is updated
                    try {
                        BeanDefinition
                                beanDef =
                                BeanDefinitionReaderUtils.createBeanDefinition(null, clazz.getName(), groovyScriptEngine
                                        .getGroovyClassLoader());
                        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
                        registry.registerBeanDefinition(clazz.getName(), beanDef);
                    } catch (ClassNotFoundException e) {
                        log.error("Exception creating bean definition for class " + clazz.getName(), e);
                    }
                } catch (ResourceException e) {
                    // You can have Exception as last param, see http://slf4j.org/faq.html#paramException
                    log.error("Exception loading data source {}", scriptName, e);
                } catch (ScriptException e) {
                    log.error("Exception loading data source {}", scriptName, e);
                }
            }

            return existingDataSources;
        } catch (BeanDefinitionStoreException e) {
            throw new RuntimeException(e);
        } catch (BeansException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
