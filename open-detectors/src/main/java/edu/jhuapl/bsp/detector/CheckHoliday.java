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

import java.util.Calendar;
import java.util.Date;

import de.jollyday.HolidayManager;

public class CheckHoliday {

	/**
	 * This function returns an array having length same as numberOfDays and
	 * each element of the array having values 0 or 1. If the corresponding day
	 * is a holiday, it will have value 1 else value 0
	 * 
	 * @param startDate
	 *            Start date
	 * @param numberOfDays
	 *            Number of days from start date
	 * @param holidayManager
	 *            HolidayManager object that knows about holidays for this
	 *            system
	 * @return array of 0 or 1 for a given date range
	 */
	public static int[] getHolidays(Date startDate, int numberOfDays,
			HolidayManager holidayManager) {
		int holidays[] = new int[numberOfDays];
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.SUNDAY);
		cal.setTime(startDate);
		for (int i = 0; i < numberOfDays; i++) {
			// if holiday manager defined
			if (holidayManager != null) {
				holidays[i] = holidayManager.isHoliday(cal) ? 1 : 0;
			}
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}
		return holidays;
	}
	
    private static int getNQ(int n, int q, int year, int month, Calendar cal) {
        cal.set(year, month - 1, 1);
        int dow = cal.get(Calendar.DAY_OF_WEEK) - 1; // formula requires Sunday to be zero
        if (n - dow < 0) {
            n += 7;
        }
        int dom = 1 + (q - 1) * 7 + ((n - dow) % 7);
        return dom;
    }

    private static int getNL(int nd, int n, int year, int month, Calendar cal) {
        cal.set(year, month - 1, nd);
        int dow = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (dow - n < 0) {
            dow += 7;
        }
        int dom = nd - ((dow - n) % 7);
        return dom;
    }

    public static int[] isHoliday(Date dt, int len) {
        int hols[] = new int[len], day, month, weekday, year;
        Calendar now = Calendar.getInstance();
        Calendar cal = (Calendar) now.clone();
        boolean bH;
//
        cal.setFirstDayOfWeek(Calendar.SUNDAY);
        cal.setTime(dt);
        for (int i = 0; i < len; i++) {
            bH = false;
            day = cal.get(Calendar.DAY_OF_MONTH);
            month = cal.get(Calendar.MONTH) + 1;  // month starts from 0
            weekday = cal.get(Calendar.DAY_OF_WEEK);
            year = cal.get(Calendar.YEAR);
            // New Years, July 4th, Christmas, Veteran's Day, and holidays
            // observed the day before or after the weekend
            if ((month == 1 && day == 1) || (month == 7 && day == 4) || (month == 11 && day == 11) ||
                (month == 12 && day == 25)) {
                bH = true;
            }
            if (weekday == 2 && ((month == 1 && day == 2) || (month == 7 && day == 5) || (month == 11 && day == 12) ||
                                 (month == 12 && day == 26))) {
                bH = true;
            }
            if (weekday == 6 && ((month == 12 && day == 31) || (month == 7 && day == 3) || (month == 11 && day == 10) ||
                                 (month == 12 && day == 24))) {
                bH = true;
            }
            // Martin Luther King Jr. Day
            if (month == 1 && day == getNQ(1, 3, year, month, (Calendar) cal.clone())) {
                bH = true;
            }
            // President's Day
            if (month == 2 && day == getNQ(1, 3, year, month, (Calendar) cal.clone())) {
                bH = true;
            }
            // Memorial Day
            if (month == 5 && day == getNL(31, 1, year, month, (Calendar) cal.clone())) {
                bH = true;
            }
            // Labor Day
            if (month == 9 && day == getNQ(1, 1, year, month, (Calendar) cal.clone())) {
                bH = true;
            }
            // Columbus Day
            if (month == 10 && day == getNQ(1, 2, year, month, (Calendar) cal.clone())) {
                bH = true;
            }
            // Thanksgiving
            if (month == 11 && day == getNQ(4, 4, year, month, (Calendar) cal.clone())) {
                bH = true;
            }
            hols[i] = 0;
            if (bH == true) {
                hols[i] = 1;
            }
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return hols;
    }
}
