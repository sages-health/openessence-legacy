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

import org.springframework.security.web.util.RequestMatcher;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class Http403RequestMatcher implements RequestMatcher {

    /**
     * Set of paths that, when requested, should trigger redirection to login page.
     */
    private static final Set<String> redirectPaths = new HashSet<>();

    static {
        redirectPaths.add("/");
        redirectPaths.add("/oe");
        redirectPaths.add("/oe/");
        redirectPaths.add("/oe/home");
        redirectPaths.add("/oe/home/");
        redirectPaths.add("/oe/home/main");
    }

    /**
     * Returns true iff 403 should be sent. Returns false iff request should be redirected.
     */
    @Override
    public boolean matches(HttpServletRequest request) {
        String path = request.getServletPath();
        if (request.getPathInfo() != null) {
            path += request.getPathInfo();
        }

        if (redirectPaths.contains(path)) {
            return false;
        }

        try {
            Boolean redirect = ServletRequestUtils.getBooleanParameter(request, "redirect");
            if (redirect != null) {
                return redirect;
            }
        } catch (ServletRequestBindingException e) {
            // client sent something funky, just ignore it
        }

        // could also check for something like X-Redirect header, but that's overkill right now

        return true;
    }

}
