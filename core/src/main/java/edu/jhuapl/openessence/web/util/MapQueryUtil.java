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

import edu.jhuapl.openessence.datasource.entry.OeDataEntrySource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedHashMap;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * Query utility class for the MapController.
 */
@Component
public class MapQueryUtil {

    private static final Logger log = LoggerFactory.getLogger(MapQueryUtil.class);

    @Resource
    private DataSource mapDataSource;

    //TODO steal stuff from DetailsQuery if it is good

    public Timestamp performCurrentTimestampQuery() {
        JdbcTemplate pgdb = new JdbcTemplate(mapDataSource);
        return pgdb.queryForObject("select current_timestamp", Timestamp.class);
    }

    public int performNextSequenceValueQuery(String sequenceForMapRequestId) {
        JdbcTemplate pgdb = new JdbcTemplate(mapDataSource);
        return pgdb.queryForInt("select nextval(\'" + sequenceForMapRequestId + "\')");
    }

    public int performDelete(OeDataEntrySource mapLayerDataEntrySource, Object current_time, String postgresCleanup) {
        JdbcTemplate pgdb = new JdbcTemplate(mapDataSource);
        String sql = "delete from " + mapLayerDataEntrySource.getTableName() + " where ? > time_requested + interval '"
                     + postgresCleanup + "'";
        log.debug(sql);
        return pgdb.update(sql, current_time);
    }

    public int performUpdate(OeDataEntrySource mapLayerDataEntrySource, LinkedHashMap<String, Object> updateMap,
                             Collection<String> placeholders) {
        JdbcTemplate pgdb = new JdbcTemplate(mapDataSource);
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(mapLayerDataEntrySource.getTableName());
        sb.append(" (");
        sb.append(StringUtils.join(updateMap.keySet(), ", "));
        sb.append(") values (");
        sb.append(StringUtils.join(placeholders, ", "));
        sb.append(")");
        String sql = sb.toString();
        log.debug(sql);
        return pgdb.update(sql, updateMap.values().toArray());
    }
}
