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

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import edu.jhuapl.openessence.model.BaseOeResponse;

public class OeController {

	private final Logger log = LoggerFactory.getLogger(getClass());
		
	/**
	 * TypeMismatchException is propagated up from DataSource injection code.
	 */
	@ExceptionHandler(TypeMismatchException.class)
	public @ResponseBody BaseOeResponse handleException(TypeMismatchException e, HttpServletResponse response) {
		if (e.getMostSpecificCause() instanceof AccessDeniedException) {
			// what's actually thrown by DataSourceConverter
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return generateResponse(e);
		} else {
			// thrown from somewhere else
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return generateResponse(e);
		}
	}
	
	/**
	 * Default exception handler to set a sane response status and JSON message.
	 */
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public @ResponseBody BaseOeResponse handleException(Exception e) {
		return generateResponse(e);
	}
	
	private BaseOeResponse generateResponse(Exception e) {
		log.error(e.getMessage(), e);
		BaseOeResponse response = new BaseOeResponse();

		// Spring will often wrap our exceptions, polluting the message, so try to get root message   
		NestedRuntimeException nre = new FatalBeanException("", e); // fake exception so we get the getMostSpecificCause method
		String message = nre.getMostSpecificCause().getMessage();

		response.setMessage(message);
		response.setSuccess(false);
		return response;
	}
}
