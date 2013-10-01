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

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p> Clients can request resources using Basic auth by prepending "/api" to the request's servlet path. For example,
 * to request the home page over Basic authentication, instead of form-based authentication, the client would request
 * "/api/oe/home/main" instead of "/oe/home/main". </p>
 *
 * <p>This filter marks the request as using Basic, strips the "/api" prefix, and forwards the request. For this reason,
 * this filter must be placed after Spring Security's filters, since our Spring Security config uses the "/api" prefix
 * to decide whether a request should go through Basic authentication.</p>
 *
 * <p>This filter also throws an {@link AccessDeniedException} if the client requests a /api resource, but doesn't
 * set their User-Agent to be {@link #API_USER_AGENT}. This is to prevent attackers from getting browsers to use Basic,
 * which would compromise CSRF protection. </p>
 */
public class ApiFilter extends OncePerRequestFilter {

    public static final String BASIC_REQUEST_ATTRIBUTE = ApiFilter.class.getName() + ".basic";

    /**
     * User Agent string that API clients must send
     */
    public static final String API_USER_AGENT = "openessence-api-client";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getServletPath().startsWith("/api/")) {
            String userAgent = request.getHeader("User-Agent");
            if (!API_USER_AGENT.equals(userAgent)) {
                // this prevents browsers from getting around CSRF protection
                throw new AccessDeniedException("Bad User-Agent " + userAgent + ". Expected " + API_USER_AGENT
                                                + " for API request");
            }

            request.setAttribute(BASIC_REQUEST_ATTRIBUTE, true);

            String path = request.getServletPath().replaceFirst("^/api/", "/");

            request.getRequestDispatcher(path).forward(request, response);
            // no need to invoke FilterChain since we forwarded

        } else {
            // this shouldn't happen if we mapped this filter to /api/* in web.xml
            // but better safe than sorry
            filterChain.doFilter(request, response);
        }
    }

}
