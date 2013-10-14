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

package edu.jhuapl.bsp.detector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import junit.framework.TestCase;
import de.jollyday.HolidayManager;


public class TestJollydayHoliday extends TestCase {

	private HolidayManager hm;

	@Override
	@Before
	protected void setUp() throws Exception {
		// String currentDir = new java.io.File( "." ).getCanonicalPath();
		hm = HolidayManager.getInstance(getClass().getResource("/Holidays_1.xml"));
	}

	@Override
	@After
	protected void tearDown() throws Exception {
		hm = null;
	}

	@Test
	public void testCheckHoliday() {
		Calendar now = Calendar.getInstance();
		Calendar cal = (Calendar) now.clone();

		// Fixed Date: Test for Jan 1st - New year day
		cal.set(2000, 0, 1);
		assertEquals(true, hm.isHoliday(cal));

		// Fixed Date: Test for July 4th - Independence
		cal.set(2010, 6, 4);
		assertEquals(true, hm.isHoliday(cal));

		// Fixed Date: Nov 11th - Veterans Day
		cal.set(2013, 10, 11);
		assertEquals(true, hm.isHoliday(cal));

		// Fixed Date: Christmas day - Dec 25
		cal.set(2014, 11, 25);
		assertEquals(true, hm.isHoliday(cal));

		// Fixed Date: Memorial day - May 30 (only from year 1869  to 1967)
		cal.set(1967, 4, 30);
		assertEquals(true, hm.isHoliday(cal));

		// Fixed Week Day - Memorial day - Last Monday of May (from year 1968)
		cal.set(2013, 4, 27);
		assertEquals(true, hm.isHoliday(cal));
	}
}
