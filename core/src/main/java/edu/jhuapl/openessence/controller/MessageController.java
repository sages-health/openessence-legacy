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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import edu.jhuapl.openessence.i18n.InspectableResourceBundleMessageSource;
import edu.jhuapl.openessence.i18n.SupportedLocale;

@Controller
public class MessageController {
	
	@Resource
	private InspectableResourceBundleMessageSource messageSource;
	
	@Resource
	private Collection<Locale> supportedLocales;
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	    
    /**
     * On any exception, set messages to the empty set and continue.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleException(Exception e) {
 	   log.error(e.getMessage(), e);
 	   
 	   ModelAndView mav = new ModelAndView("messages");
 	   mav.addObject("message", "({})");
 	   
 	   return mav;
    }
    
    @RequestMapping("/messages")
    public ModelAndView getMessages(@RequestParam(defaultValue = "false") boolean prettyPrint, Locale locale) 
    		throws JsonGenerationException, JsonMappingException, IOException {
    	
    	ModelAndView mav = new ModelAndView("messages");
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, prettyPrint);
    	ObjectNode root = mapper.createObjectNode();
    	for (Entry<String, String> e : messageSource.getAllMessages().entrySet()) {
    		root.put(e.getKey(), e.getValue());
    	}
    	mav.addObject("messages", mapper.writeValueAsString(root));
    	
    	return mav;
    }
    
    @RequestMapping("/locale")
	public @ResponseBody Map<String, Collection<SupportedLocale>> locales() {	   
		// don't return a top-level array
		Map<String, Collection<SupportedLocale>> value = new HashMap<String, Collection<SupportedLocale>>();
		List<SupportedLocale> locales = new ArrayList<SupportedLocale>();
		for (Locale l : supportedLocales) {
			locales.add(SupportedLocale.fromLocale(l, messageSource));
		}

		value.put("locales", locales);

		return value;
	}
}
