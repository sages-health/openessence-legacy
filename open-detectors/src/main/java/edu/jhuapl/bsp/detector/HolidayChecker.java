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

import de.jollyday.HolidayManager;

import java.util.Calendar;
import java.util.Date;

public class HolidayChecker {

    /**
     * This function returns an array having length same as numberOfDays and each element of the array having values 0
     * or 1. If the corresponding day is a holiday, it will have value 1 else value 0.
     *
     * @param startDate      Start date
     * @param numberOfDays   Number of days from start date
     * @param holidayManager HolidayManager object that knows about holidays for this system
     * @return array of 0 or 1 for a given date range
     */
    public static int[] getHolidays(Date startDate, int numberOfDays, HolidayManager holidayManager) {
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
}
