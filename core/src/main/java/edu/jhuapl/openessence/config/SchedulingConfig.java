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

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;

/**
 * Configuration class for scheduling-related beans.
 */
@Configuration
@EnableScheduling
@Profile(SchedulingConfig.SCHEDULING_PROFILE)
public class SchedulingConfig implements SchedulingConfigurer {

    public static final String SCHEDULING_PROFILE = "scheduling";

    @Inject
    private EnvironmentConfig envConfig;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // it would be nice if we could use Spring's @Scheduled annotations,
        // but that doesn't let us inject the trigger rate
        taskRegistrar.addFixedDelayTask(new GraphCleanupTask(), envConfig.graphRetention());
    }

    /**
     * Task that deletes old graph files. Unfortunately, some archaic operating systems (AKA Windows) do not automatically
     * clean out the system temp directory, so we have to do it ourselves. We also can't rely on Java's temp file feature,
     * since that deletes files when the JVM shuts down, and we will (hopefully) run forever.
     */
    class GraphCleanupTask implements Runnable {

        private final Logger log = LoggerFactory.getLogger(getClass());

        @Override
        public void run() {
            // sanity check before we delete all the user's files
            if (!envConfig.graphPath().startsWith(FileUtils.getTempDirectoryPath())) {
                String msg = "Graph directory is not in system tmp dir.";
                log.error(msg); // exceptions are sometimes suppressed in tasks
                throw new IllegalStateException(msg);
            }

            // we can't just delete the entire directory b/c some graphs might still be in use,
            // e.g. in the middle of being written out to client,
            // so only delete if graph was last touched a long time ago
            long now = System.currentTimeMillis();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(envConfig.graphPath())) {
                for (Path p : stream) {
                    long modifiedTime = Files.getLastModifiedTime(p).toMillis();
                    if (modifiedTime + envConfig.graphRetention() < now) {
                        Files.delete(p);
                        log.debug("Deleted graph file {}", p);
                    }
                }
            } catch (IOException | DirectoryIteratorException e) {
                log.error("", e);
                throw new IllegalStateException(e);
            }
        }

    }

}
