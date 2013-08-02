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

import com.jolbox.bonecp.BoneCPDataSource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Dumping ground for beans that proxy environment variables.
 */
@Configuration
@Profile("production")
public class EnvironmentConfig {

    // keys of properties in environment
    public static final String MAIN_DB = "jdbc/maindb";

    public static final String GRAPH_DIR = "/oe/config/graph.dir";
    public static final String GRAPH_RETENTION = "/oe/config/graph.retention";

    private static final Logger log = LoggerFactory.getLogger(EnvironmentConfig.class);

    @Inject
    private Environment environment;

    @Bean(destroyMethod = "close")
    public DataSource mainDataSource() {
        // Container-managed DataSource
        DataSource ds = environment.getProperty(MAIN_DB, DataSource.class);
        if (ds != null) {
            return ds;
        }

        log.info("No DataSource found in Environment. Using application-managed connection pool.");

        // config values (mostly) copied from http://jolbox.com/configuration-spring.html
        BoneCPDataSource bcpds = new BoneCPDataSource();
        bcpds.setDriverClass(environment.getRequiredProperty("db.driverClass"));
        bcpds.setJdbcUrl(environment.getRequiredProperty("db.url"));
        bcpds.setUsername(environment.getRequiredProperty("db.username"));
        bcpds.setPassword(environment.getRequiredProperty("db.password"));
        bcpds.setIdleConnectionTestPeriodInMinutes(60);
        bcpds.setIdleMaxAgeInMinutes(240);
        bcpds.setMaxConnectionsPerPartition(30);
        bcpds.setMinConnectionsPerPartition(10);
        bcpds.setPartitionCount(3);
        bcpds.setAcquireIncrement(5);
        bcpds.setStatementsCacheSize(100);
        bcpds.setReleaseHelperThreads(1);
        bcpds.setConnectionTestStatement("SELECT 1");

        // we compare to Boolean.FALSE to avoid NPE from unboxing if property isn't defined
        if (environment.getProperty("db.testConnection", Boolean.class) != Boolean.FALSE) {
            try {
                testDataSource(bcpds);
            } catch (SQLException e) {
                // fail at startup if we can't connect to database, rather than at first query
                throw new IllegalStateException("Exception connecting to database", e);
            }
        } else {
            log.info("db.testConnection set to false. Skipping database connection test.");
        }

        return bcpds;
    }

    private void testDataSource(BoneCPDataSource ds) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute(ds.getConnectionTestStatement());
        }
    }

    @Bean
    public Path graphPath() {
        if (environment.containsProperty(GRAPH_DIR)) {
            // Allowing custom graph dir made the code more complicated and no one was using it.
            // Putting graphs in tmp makes the most sense anyway.
            log.warn("Setting graph directory is no longer supported. " +
                     "Graphs are now always stored in the system temp directory.");
        }

        try {
            Path oegraphsPath = Paths.get(FileUtils.getTempDirectoryPath(), "oegraphs");
            if (!Files.exists(oegraphsPath)) {
                Files.createDirectory(oegraphsPath);
            }

            log.info("Using {} as graph directory", oegraphsPath);

            return oegraphsPath;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * It doesn't make sense to pass around a String instead of a Path, but the graph module expects a String in a lot
     * of places.
     *
     * @deprecated Use {@link #graphPath()} instead
     */
    @Deprecated
    @Bean
    public String graphDir() {
        return graphPath().toString();
    }

    /**
     * <p> It would be nice if we could just put graphs in tmp and not have to run cleanup jobs. Unfortunately, Windows
     * leaves it up to applications to clean up after themselves [1]. Yet another reason Windows is a horrible OS. </p>
     *
     * [1] http://superuser.com/questions/296824/when-is-a-windows-users-temp-directory-cleaned-out.
     */
    @Bean
    public long graphRetention() {
        Long retention = environment.getProperty(GRAPH_RETENTION, Long.class);
        if (retention == null) {
            long defaultRetention = 300000;
            log.info("Graph retention not found in environment. Using {} instead", defaultRetention);
            return defaultRetention;
        } else {
            return retention;
        }
    }

    @Bean
    public String mainLayoutResources() {
        return environment.getProperty("mainLayout.resources");
    }

    @Bean
    public String mainResources() {
        return environment.getProperty("main.resources");
    }

    @Bean
    public String loginResources() {
        return environment.getProperty("login.resources");
    }
}
