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

package edu.jhuapl.openessence.security;

import edu.jhuapl.openessence.datasource.Filter;
import edu.jhuapl.openessence.datasource.OeDataSource;
import edu.jhuapl.openessence.datasource.QueryManipulationStore;
import edu.jhuapl.openessence.datasource.Record;
import edu.jhuapl.openessence.datasource.jdbc.JdbcOeDataSource;
import edu.jhuapl.openessence.datasource.jdbc.filter.EqFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

public class OEUserDetailsService implements UserDetailsService {

    @Resource
    private Map<String, JdbcOeDataSource> dataSources;

    private static final Logger log = LoggerFactory.getLogger(OEUserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException, DataAccessException {

        OeDataSource roleDs = dataSources.get("SecurityMapping");
        if (roleDs == null) {
            throw new IllegalStateException("Required data source \"SecurityMapping\" not defined");
        }

        List<Filter> roleFilters = new ArrayList<>();
        roleFilters.add(new EqFilter("UserName", username));

        List<GrantedAuthority> roles = new ArrayList<>();

        Collection<Record> roleRecs = roleDs.detailsQuery(
                new QueryManipulationStore(roleDs.getAllResultDimensions(), null, roleFilters, null, false,
                                           null));
        for (Record roleRec : roleRecs) {
            String role = (String) roleRec.getValue("Role");
            GrantedAuthority authority = new SimpleGrantedAuthority(role);
            roles.add(authority);
            log.info("Role: " + role);
        }

        OeDataSource ds = dataSources.get("User");
        if (ds == null) {
            throw new IllegalStateException("Required data source \"User\" not defined");
        }

        List<Filter> filters = new ArrayList<>();
        filters.add(new EqFilter("UserName", username));

        Collection<Record> recs =
                ds.detailsQuery(new QueryManipulationStore(ds.getAllResultDimensions(), null, filters, null, false,
                                                           null));

        if (recs.size() == 0) {
            // throw exception, as per interface contract
            throw new UsernameNotFoundException("Username " + username + " not found");
        } else if (recs.size() > 1) {
            // I don't know who wouldn't make username a primary key, but just in case...
            throw new IllegalStateException("Multiple records for username " + username);
        }

        Record rec = recs.toArray(new Record[1])[0];
        String rspassword = (String) rec.getValue("Password");
        String salt = (String) rec.getValue("Salt");
        String algorithm = (String) rec.getValue("Algorithm");
        Map<String, Object> attributes = new HashMap<>();
        for (String rid : rec.getResultIds()) {
            // only process non-spring attributes
            if (!rid.equals("UserName") && !rid.equals("Password")
                && !rid.equals("Enabled")
                && !rid.equals("NonExpired")
                && !rid.equals("CredentialsNonExpired")
                && !rid.equals("AccountNonLocked")) {
                attributes.put(rid, rec.getValue(rid));
            }
        }
        return new OEUser(username, rspassword, roles, attributes, salt, algorithm);
    }
}
