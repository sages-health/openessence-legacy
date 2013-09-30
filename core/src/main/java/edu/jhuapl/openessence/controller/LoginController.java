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

package edu.jhuapl.openessence.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import edu.jhuapl.openessence.config.EnvironmentConfig;
import edu.jhuapl.openessence.security.LoginStatus;

// weird Spring bug: @RequestMapping("/login") here causes login method mapping, which was also @RequestMapping("/login"), to fail
// workaround: just include full path in method mapping
@Controller
public class LoginController extends OeController {

    public static final String LOGIN_PAGE_PATH = "/login/login";

	@Inject
	private EnvironmentConfig envConfig;
	
	@RequestMapping(LOGIN_PAGE_PATH)
	public ModelAndView login() {
		ModelAndView mav = new ModelAndView("login");
		mav.addObject("mainLayoutResources", envConfig.mainLayoutResources());
		mav.addObject("loginResources", envConfig.loginResources());
		return mav;
	}

	@RequestMapping("/login/status")
	public @ResponseBody LoginStatus status(Principal principal, Authentication auth) {
		LoginStatus loginStatus = new LoginStatus();

		if (principal == null) {
			// not logged in
			// TODO let name be null if user isn't logged in, requires changes to client
			loginStatus.setName("unknown");
			loginStatus.setAuthorities(Collections.<String>emptyList());
		} else {
			loginStatus.setName(principal.getName());
			List<String> authorities = new ArrayList<String>();
			for (GrantedAuthority a : auth.getAuthorities()) {
				authorities.add(a.getAuthority());
			}
			loginStatus.setAuthorities(authorities);
		}

		return loginStatus;
	}
}
