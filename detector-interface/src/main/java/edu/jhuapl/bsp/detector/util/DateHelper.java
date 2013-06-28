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

package edu.jhuapl.bsp.detector.util;

import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Class DateHelper handles date-related things in a consistent, sane manner.
 */
public class DateHelper {

    // Returns standard date format needed in SQL queries.
    public static SimpleDateFormat getSQLDateFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf;
    }

    // Returns standard date format used throughout project.
    //
    // NOTE: If standard date format changes, javascript function
    // assembleDateString()
    // in DateHelper method writeDateFormattingJavascript may also need to be
    // changed.
    public static SimpleDateFormat getStandardDateFormat() {
        // cas 07-13-03
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //SimpleDateFormat sdf = new SimpleDateFormat("MMddyy");
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyy");
        return sdf;
    }

    // Returns String representing integer version of day, extracted from supplied
    // dateString,
    // which is assumed to be in standard date formate.
    // Days begin at 1.
    //
    public static String getDayID(String dateString) {
        String returnString;
        java.util.Date date = getDate(dateString);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        returnString = (new Integer(calendar.get(Calendar.DAY_OF_MONTH))).toString();
        System.out.println("DateHelper.getDayID(): dateString='" + dateString
                           + "', returnString='" + returnString + "'");
        return returnString;
    }

    // Returns String representing display version of day, extracted from supplied
    // dateString,
    // which is assumed to be in standard date formate.
    public static String getDayName(String dateString) {
        String returnString;
        java.util.Date date = getDate(dateString);
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        returnString = sdf.format(date);
        return returnString;
    }

    // Returns String representing int, 1 through 7. 1=Sunday.
    public static String getDayOfWeekID(String dateString) {
        String returnString = null;
        java.util.Date date = getDate(dateString);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int dayOfWeekInt = calendar.get(Calendar.DAY_OF_WEEK);
        returnString = (new Integer(dayOfWeekInt)).toString();
        //returnString = (new
        // Integer(calendar.get(Calendar.DAY_OF_WEEK))).toString();
        System.out.println("DateHelper.getDayOfWeekID(): dateString='" + dateString
                           + "', returnString='" + returnString + "'");
        return returnString;
    }

    // Returns String representing display version of day, extracted from supplied
    // dateString,
    // which is assumed to be in standard date formate.
    public static String getDayOfWeekName(String dateString) {
        String returnString = null;
        String dayOfWeekID = getDayOfWeekID(dateString);
        if (dayOfWeekID.equalsIgnoreCase("1")) {
            returnString = "Sunday";
        } else if (dayOfWeekID.equalsIgnoreCase("2")) {
            returnString = "Monday";
        } else if (dayOfWeekID.equalsIgnoreCase("3")) {
            returnString = "Tuesday";
        } else if (dayOfWeekID.equalsIgnoreCase("4")) {
            returnString = "Wednesday";
        } else if (dayOfWeekID.equalsIgnoreCase("5")) {
            returnString = "Thursday";
        } else if (dayOfWeekID.equalsIgnoreCase("6")) {
            returnString = "Friday";
        } else if (dayOfWeekID.equalsIgnoreCase("7")) {
            returnString = "Saturday";
        } else {
            System.out.println("Error: unrecognized dayOfWeekID in DateHelper.getDayOfWeekName()");
        }

        return returnString;
    }

    // Returns String representing integer version of month, extracted from
    // supplied dateString,
    // which is assumed to be in standard date formate.
    // Months begin at 0.
    public static String getMonthID(String dateString) {
        String returnString;
        java.util.Date date = getDate(dateString);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        returnString = (new Integer(calendar.get(Calendar.MONTH))).toString();
        //System.out.println("DateHelper.getMonthID(): dateString='" + dateString +
        // "', returnString='" + returnString + "'");
        return returnString;
    }

    // Returns String representing display version of month, extracted from
    // supplied dateString,
    // which is assumed to be in standard date formate.
    public static String getMonthName(String dateString) {
        String returnString;
        java.util.Date date = getDate(dateString);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM");
        returnString = sdf.format(date);
        return returnString;
    }

    // Returns String representing integer version of year, extracted from
    // supplied dateString,
    // which is assumed to be in standard date formate.
    // Year ID is full 4-digit year string.
    public static String getYearID(String dateString) {
        String returnString;
        java.util.Date date = getDate(dateString);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        returnString = (new Integer(calendar.get(Calendar.YEAR))).toString();
        //System.out.println("DateHelper.getYearID(): dateString='" + dateString +
        // "', returnString='" + returnString + "'");
        return returnString;
    }

    // Returns String representing display version of year, extracted from
    // supplied dateString,
    // which is assumed to be in standard date formate.
    public static String getYearName(String dateString) {
        String returnString;
        java.util.Date date = getDate(dateString);
        SimpleDateFormat sdf = new SimpleDateFormat("yy");
        returnString = sdf.format(date);
        return returnString;
    }

    // Returns Date object corresponding to dateString, assuming it is in standard
    // date format.
    public static java.util.Date getDate(String dateString) {
        java.util.Date returnDate = null;
        SimpleDateFormat sdf = getStandardDateFormat();
        if (dateString != null) {
            try {
                returnDate = sdf.parse(dateString);
            } catch (ParseException pe) {
                System.err.println("ERROR - [DateHelper.getDate(String)] - "
                                   + pe.getMessage());
                pe.printStackTrace();
            }
        }
        return returnDate;
    }

    /**
     * Returns the current date in standard date format.
     */
    public static String GetCurrentDate() {
        java.util.Date CurrentDate = new java.util.Date();
        SimpleDateFormat DateFormatter = getStandardDateFormat();
        return DateFormatter.format(CurrentDate);
    }

    /**
     * Returns Date object for specified number of days ago, relative to today. Allow the ability to pass in what the
     * current date is.
     *
     * @return Date
     */
    public static java.util.Date getDate(int numDaysAgo) {
        //BioSrvConfig config = BioSrvConfig.getInstance();
        String currentDateArg = null;
        java.util.Date returnDate = null;

        Calendar cal = new GregorianCalendar();

        //check currentDate Arge in biosrvconfig
        //currentDateArg = config.getCurrentDateArg();
        if (currentDateArg == null
            //|| currentDateArg.equals(BioSrvConfig.today_currentDateArgValue)) {
                ) {
            cal.setTime(new java.util.Date());
        } else {
            cal.setTime(getDate(currentDateArg));
            //      System.err.println("Today is " + getDate(currentDateArg));
        }

        cal.add(Calendar.DATE, 0 - numDaysAgo);
        returnDate = cal.getTime();

        return returnDate;
    }

    /**
     * Returns Date object for specified number of days ago, relative to specified Date object.
     */
    public static java.util.Date getDate(java.util.Date fromDate, int numDaysAgo) {
        java.util.Date returnDate;

        Calendar cal = new GregorianCalendar();
        cal.setTime(fromDate);
        cal.add(Calendar.DATE, 0 - numDaysAgo);
        returnDate = cal.getTime();

        return returnDate;
    }

    /**
     * Returns String representing date a specified number of days ago, relative to supplied date String.
     */
    public static String getDate(String fromDateString, int numDaysAgo) {
        String returnDateString;

        java.util.Date fromDate = getDate(fromDateString);
        Calendar cal = new GregorianCalendar();
        cal.setTime(fromDate);
        cal.add(Calendar.DATE, 0 - numDaysAgo);
        java.util.Date returnDate = cal.getTime();
        returnDateString = getFormattedDateString(returnDate);

        return returnDateString;
    }

    // Creates standard format date string from integer representations of day,
    // month, and year.
    public static String getDateString(String day, String month, String year) {
        String returnString = null;
        java.util.Date date = getDate(day, month, year);
        if (date != null) {
            returnString = getFormattedDateString(date);
        }
        return returnString;
    }

    // Creates date object from String representations of integers for day, month,
    // and year.
    // If date is invalid (as determined by Calendar object), retuns null.
    public static java.util.Date getDate(String day, String month, String year) {
        java.util.Date returnDate = null;
        Calendar calendar = new GregorianCalendar();
        try {
            calendar.set(Calendar.YEAR, Integer.parseInt(year));
            calendar.set(Calendar.MONTH, Integer.parseInt(month));
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
            returnDate = calendar.getTime();
        } catch (Exception e) {
            System.out.println("DateHelper.getDate(int, int, int): Illegal date");
        }
        return returnDate;
    }

    public static int getDateDifference(String startDateString,
                                        String endDateString) {

        int returnVal = 0;

        if ((startDateString != null) && (endDateString != null)) {
            java.util.Date startDate = getDate(startDateString);
            java.util.Date endDate = getDate(endDateString);
            returnVal = getDateDifference(startDate, endDate);
        }

        return returnVal;
    }

    public static int getDateDifference(java.util.Date startDate,
                                        java.util.Date endDate) {

        int returnVal = 0;
        Calendar cal = Calendar.getInstance();

        cal.setTime(startDate);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        startDate = cal.getTime();

        cal.setTime(endDate);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        endDate = cal.getTime();

        int diff = (int) Math.round((endDate.getTime() - startDate.getTime())
                                    / ((double) (1000 * 60 * 60 * 24)));

        returnVal = diff + 1;

        return returnVal;
    }

    // Returns date string in standard format, corresponding to specified number
    // of days ago.
    public static String getFormattedDateString(java.util.Date date) {
        String returnString = "";

        SimpleDateFormat sdf = getStandardDateFormat();
        returnString = sdf.format(date);

        return returnString;
    }

    // Returns date string in standard format, corresponding to specified number
    // of days ago.
    public static String getSQLFormattedDateString(java.util.Date date) {
        String returnString = "";

        SimpleDateFormat sdf = getSQLDateFormat();
        returnString = sdf.format(date);

        return returnString;
    }

    // Returns date string in standard format, corresponding to specified number
    // of days ago.
    public static String getFormattedDateString(int numDaysAgo) {
        String returnString = "";

        java.util.Date returnDate = getDate(numDaysAgo);
        SimpleDateFormat sdf = getStandardDateFormat();
        returnString = sdf.format(returnDate);

        return returnString;
    }

    // Returns date string in SQL-compatible format, corresponding to specified
    // number of days ago.
    public static String getSQLFormattedDateString(int numDaysAgo) {
        String returnString = "";

        java.util.Date returnDate = getDate(numDaysAgo);
        SimpleDateFormat sdf = getSQLDateFormat();
        returnString = sdf.format(returnDate);

        return returnString;
    }

    // Returns date string in SQL format, assuming supplied date string is in
    // standard format.
    public static String getSQLFormattedDateString(String standardDateString) {
        String returnString = "";

        java.util.Date date = getDate(standardDateString);
        returnString = getSQLFormattedDateString(date);

        return returnString;
    }

    public static int[][] getISOWeekYears(java.util.Date startDate,
                                          java.util.Date endDate) {
        Calendar cal = new GregorianCalendar();
        cal.setMinimalDaysInFirstWeek(4);
        cal.setTime(startDate);

        Calendar nowCal = new GregorianCalendar();
        nowCal.setTime(endDate);

        ArrayList weekList = new ArrayList();
        ArrayList yearList = new ArrayList();

        int size = 0;
        int currentWeek = 0;
        int currentYear = 0;
        int tmpWeek = 0;
        int tmpYear = 0;
        while ((cal.get(Calendar.DATE) != nowCal.get(Calendar.DATE))
               || (cal.get(Calendar.MONTH) != nowCal.get(Calendar.MONTH))
               || (cal.get(Calendar.YEAR) != nowCal.get(Calendar.YEAR))) {

            tmpWeek = cal.get(Calendar.WEEK_OF_YEAR);
            tmpYear = DateHelper.getISOYear(cal.getTime());

            if (tmpWeek != currentWeek || tmpYear != currentYear) {
                weekList.add(new Integer(tmpWeek));
                yearList.add(new Integer(tmpYear));
                currentWeek = tmpWeek;
                currentYear = tmpYear;
            }

            cal.add(Calendar.DATE, 1);
        }
        int[] isoYears = new int[yearList.size()];
        for (int i = 0; i < yearList.size(); i++) {
            isoYears[i] = ((Integer) yearList.get(i)).intValue();
        }
        int[] isoWeeks = new int[weekList.size()];
        for (int i = 0; i < weekList.size(); i++) {
            isoWeeks[i] = ((Integer) weekList.get(i)).intValue();
        }
        int[][] isoWeekYears = {isoWeeks, isoYears};

        return isoWeekYears;
    }

    public static int getISOYear(java.util.Date date) {

        Calendar cal = new GregorianCalendar();
        cal.setMinimalDaysInFirstWeek(4);
        cal.setTime(date);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int week = cal.get(Calendar.WEEK_OF_YEAR);

        if (week == 1 && month == 11) {
            year++;
        } else if (week > 50 && month == 0) {
            year--;
        }
        return year;
    }

    public static int getNumDaysInMonth(String monthID, String yearID) {
        int returnVal = 31;
        if (monthID != null) {
            int integerMonthID = (new Integer(monthID)).intValue();
            if ((integerMonthID == 3) || (integerMonthID == 5)
                || (integerMonthID == 8) || (integerMonthID == 10)) {
                returnVal = 30;
            } else if (integerMonthID == 1) {
                returnVal = 28;
                if (yearID != null) {
                    double integerYearID = (new Integer(yearID)).intValue();
                    System.out.println("integerYearID=" + integerYearID + "; "
                                       + "integerYearID/4=" + (integerYearID / 4) + "; "
                                       + "Math.floor(integerYearID/4)="
                                       + (Math.floor(integerYearID / 4)));
                    if ((integerYearID / 4) == (Math.floor(integerYearID / 4))) {
                        System.out.println("Leap year");
                        returnVal = 29;
                    } else {
                        System.out.println("Not leap year");
                    }
                }
            }
        }
        return returnVal;
    }

  /*
   * // Returns true if date is valid; false else. public static boolean
   * isDateValid(int day, int month, int year) { System.out.println("Entering
   * DateHelper.validateDate(): day=" + day + ", month=" + month + ", year=" +
   * year); boolean returnVal = false; Calendar calendar = new
   * GregorianCalendar(); java.util.Date date = getDate(day, month, year); try {
   * calendar.setTime(date); returnVal = true; } catch(Exception e) { returnVal =
   * false; } System.out.print("Exiting DateHelper.validateDate(): "); if
   * (returnVal) { System.out.println("returnVal=true"); } else {
   * System.out.println("returnVal=false"); } return returnVal; }
   */

  /*
   * // Creates date object from String representations of integers for day,
   * month, and year. // If date is invalid (as determined by Calendar object,
   * assumes day selection may // be inappropriately large for month selection,
   * and decreases day until date is valid // (or until it is apparent that
   * that's not the problem). // Returns valid dateString, or null if it could
   * not be made valid. public static java.util.Date getValidDate(String day,
   * String month, String year) { java.util.Date returnDate = getDate(day,
   * month, year); if (returnDate == null) { while ((day >= 28) && (returnDate ==
   * null)) { day = day - 1; returnDate = getDate(day, month, year); } } return
   * returnDate; }
   */

    public static void writeDateFormattingJavascript(
            PrintWriter out,
            String formContainingDateComponents,
            String formToSubmit) {
        StringBuffer sb = new StringBuffer();
        writeDateFormattingJavascript(sb, formContainingDateComponents,
                                      formToSubmit);
        out.println(sb);
    }

    // This function is used to support forms that allow date components to be
    // selected
    // separately.
    // Names of date components in form are assumed to be: startDay, startMonth,
    // startYear,
    // endDay, endMonth, endYear.
    // IDs associated with these components must be integers. The components'
    // labels are
    // used to assemble standard format startDate and endDate strings to be passed
    // in URLs.
    // JavaScript function handleFormData() can be printed just once on a page and
    // called
    // multiple times on different date filters.
    // Function inputs a description of the date filter, the dateComponentsForm,
    // and
    // the submitForm as arguments. Calling code can provide the arguments when
    // writing the HTML that calls handleFormData().
    // Javascript function getValidDay() converts invalid dates to valid ones
    // (e.g.,
    // if day value is too high for the selected month, it just decreases it
    // automatically).
    // Javascript function assembleDateString() assembles standard format date
    // strings
    // from (label) values for day, month, and year.
    // The main javascript function handleFormData() extracts individual date
    // components from
    // the specified formContainingDateComponents, calls getValidDay() and
    // assembleDateString()
    // to assemble valid formatted date strings, assigns these strings to the
    // "startDate" and
    // "endDate" fields of the specified formToSubmit, submits the form, and
    // returns false.
    //
    // NOTE: If standard date format changes, javascript function
    // assembleDateString()
    // may also need to be changed.
    //
    public static void writeDateFormattingJavascript(
            StringBuffer sb,
            boolean renderFilterAsSeparateControl) {
        sb.append("<script language=\"JavaScript\">\n");
        sb.append("<!-- Begin hiding script contents from old browsers.\n");

        sb.append("function handleFormData(formDescription, dateComponentsForm, submitForm) {\n");
        // Get date-related values from form.
        // These are contained in the dateConfigurationForm.
        sb.append("  var startDayInt = dateComponentsForm.startDateDay.value;\n");
        sb.append("  var startMonthInt = dateComponentsForm.startDateMonth.value;\n");
        sb.append("  var startYearInt = dateComponentsForm.startDateYear.value;\n");
        sb.append("  var endDayInt = dateComponentsForm.endDateDay.value;\n");
        sb.append("  var endMonthInt = dateComponentsForm.endDateMonth.value;\n");
        sb.append("  var endYearInt = dateComponentsForm.endDateYear.value;\n");

        // If date range is invalid (e.g., startDate does not precede endDate) abort
        // and
        // popup alert window.
        sb.append("  if (!(isDateRangeValid(startDayInt, startMonthInt, startYearInt,\n");
        sb.append("      endDayInt, endMonthInt, endYearInt))) {\n");
        sb.append("    alert(formDescription + ' - Invalid date range!\\nPlease reselect start and/or end dates.');\n");
        sb.append("  }\n");
        sb.append("  else {\n");

        // Reset selected day value to be valid, if invalid, based on selected month
        // and year.
        //sb.append(" var startDayInt = getValidDay(startDayInt, startMonthInt,
        // startYearInt);\n");
        //sb.append(" var endDayInt = getValidDay(endDayInt, endMonthInt,
        // endYearInt);\n");

        // Get text versions of selected day, month, and year for startDate.
        // These are contained in the dateConfigurationForm.
        sb.append("  var startDayString = startDayInt;\n");
        sb.append("  var startMonthString = dateComponentsForm.startDateMonth.options"
                  + "[dateComponentsForm.startDateMonth.selectedIndex].text;\n");
        sb.append("  var startYearString = dateComponentsForm.startDateYear.options"
                  + "[dateComponentsForm.startDateYear.selectedIndex].text;\n");

        // Get text versions of selected day, month, and year, for endDate.
        // These are contained in the dateConfigurationForm.
        sb.append("  var endDayString = endDayInt;\n");
        sb.append("  var endMonthString = dateComponentsForm.endDateMonth.options"
                  + "[dateComponentsForm.endDateMonth.selectedIndex].text;\n");
        sb.append("  var endYearString = dateComponentsForm.endDateYear.options"
                  + "[dateComponentsForm.endDateYear.selectedIndex].text;\n");

        // Construct startDate string and endDate string out of text versions of
        // day, month, and year.
        sb.append("  var startDateString = assembleDateString(startDayString, startMonthString, startYearString);\n");
        sb.append("  var endDateString = assembleDateString(endDayString, endMonthString, endYearString);\n");

        // Set values of hidden fields 'startDate' and 'endDate' in
        // allDataConfigurationForm.
        sb.append("  submitForm.startDate.value = startDateString;\n");
        sb.append("  submitForm.endDate.value = endDateString;\n");

        //If not rendering the date filter as a separate control,
        //do not trigger the onSubmit event of submitForm.
        if (true == renderFilterAsSeparateControl) {
            sb.append("  submitForm.submit(submitForm)\n");
        }
        //Need return true because calling code might string together multiple
        // handleFormData() calls.
        else {
            sb.append("  return true;\n");
        }

        sb.append("  }\n"); // end if (isDateRangeValid())
        sb.append("  return false;\n");
        sb.append("}\n");

        // Use Math.abs() function to force strings to be considered as ints.
        sb.append("function isDateRangeValid(startDateDay, startDateMonth, startDateYear,\n");
        sb.append("    endDateDay, endDateMonth, endDateYear) {\n");
        sb.append("  var returnVal = false;\n");
        sb.append("  if (Math.abs(startDateYear) < Math.abs(endDateYear)) {\n");
        sb.append("    returnVal = true;\n");
        sb.append("  }\n");
        sb.append("  else if ((Math.abs(startDateYear) == Math.abs(endDateYear)) &&\n"
                  + "           (Math.abs(startDateMonth) < Math.abs(endDateMonth))) {\n");
        sb.append("    returnVal = true;\n");
        sb.append("  }\n");
        sb.append("  else if ((Math.abs(startDateYear) == Math.abs(endDateYear)) &&\n"
                  + "           (Math.abs(startDateMonth) == Math.abs(endDateMonth)) &&\n"
                  +
                  // wal - 8/14/03 - it is ok to have dates the same, look at data
                  // details.
                  //" (Math.abs(startDateDay) < Math.abs(endDateDay))) {\n");
                  "           (Math.abs(startDateDay) <= Math.abs(endDateDay))) {\n");
        sb.append("    returnVal = true;\n");
        sb.append("  }\n");
        sb.append("  return returnVal;\n");
        sb.append("}\n");
        sb.append("\n");

        sb.append("function assembleDateString(dayString, monthString, yearString) {\n");
        sb.append("  var dateString = \"\" + dayString + monthString + yearString;\n");
        sb.append("  return dateString;\n");
        sb.append("}\n");
        sb.append("\n");

        sb.append("function getNumDaysInMonth(monthID, yearID) {\n");
        sb.append("  var returnVal = 0;\n");
        sb.append("  if ((Math.abs(monthID) == 3) ||\n");
        sb.append("      (Math.abs(monthID) == 5) ||\n");
        sb.append("      (Math.abs(monthID) == 8) ||\n");
        sb.append("      (Math.abs(monthID) == 10)) {\n");
        sb.append("    returnVal = 30;\n");
        sb.append("  }\n");
        sb.append("  else if (Math.abs(monthID) == 1) {\n");
        sb.append("    if ((yearID/4)==Math.floor(yearID/4)) {\n");
        sb.append("      returnVal = 29;\n");
        sb.append("    }\n");
        sb.append("    else {\n");
        sb.append("      returnVal = 28;\n");
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("  else {\n");
        sb.append("    returnVal = 31;\n");
        sb.append("  }\n");
        sb.append("  return returnVal;\n");
        sb.append("}\n");
        sb.append("\n");

        // Calls handleMonthChange(). Need this to reset number of days for
        // February,
        // depending on whether or not selected year is a leap year.
        sb.append("function handleYearChange(selectComponent) {\n");
        sb.append("  var form = selectComponent.form;\n");
        sb.append("  var selectComponentName  = selectComponent.name;\n");
        sb.append("  var monthComponentName;\n");
        sb.append("  if (selectComponentName == 'startDateYear') {\n");
        sb.append("    monthComponentName = 'startDateMonth';\n");
        sb.append("  }\n");
        sb.append("  else {\n");
        sb.append("    monthComponentName = 'endDateMonth';\n");
        sb.append("  }\n");
        sb.append("  var monthComponent = form.elements(monthComponentName);\n");
        sb.append("  return handleMonthChange(monthComponent);\n");
        sb.append("}\n");
        sb.append("\n");

        // Use Math.abs() function to force strings to be considered as ints.
        sb.append("function handleMonthChange(selectComponent) {\n");
        sb.append("  var form = selectComponent.form;\n");
        sb.append("  var monthID = selectComponent.options[selectComponent.selectedIndex].value;\n");
        sb.append("  var yearComponentName;\n");
        sb.append("  var dayComponentName;\n");
        sb.append("  var selectComponentName  = selectComponent.name;\n");
        sb.append("  if (selectComponentName == 'startDateMonth') {\n");
        sb.append("    yearComponentName = 'startDateYear';\n");
        sb.append("    dayComponentName = 'startDateDay';\n");
        sb.append("  }\n");
        sb.append("  else {\n");
        sb.append("    yearComponentName = 'endDateYear';\n");
        sb.append("    dayComponentName = 'endDateDay';\n");
        sb.append("  }\n");
        sb.append(
                "  var yearID = form.elements(yearComponentName).options[form.elements(yearComponentName).selectedIndex].value;\n");
        sb.append("  var numDaysDesiredInOptionsList = getNumDaysInMonth(monthID, yearID);\n");
        sb.append("  var numDaysInOptionsList = form.elements(dayComponentName).options.length;\n");
        sb.append("  while (Math.abs(numDaysInOptionsList) !== Math.abs(numDaysDesiredInOptionsList)) {\n");
        sb.append("    if (Math.abs(numDaysInOptionsList) > Math.abs(numDaysDesiredInOptionsList)) {\n");
        sb.append(
                "      if (Math.abs(form.elements(dayComponentName).selectedIndex) == Math.abs(numDaysInOptionsList - 1)) {\n");
        sb.append("        form.elements(dayComponentName).selectedIndex = numDaysInOptionsList - 2;\n");
        sb.append("      }\n");
        sb.append("      form.elements(dayComponentName).options[Math.abs(numDaysInOptionsList) - 1] = null;\n");
        sb.append("      numDaysInOptionsList = Math.abs(numDaysInOptionsList) - 1;\n");
        sb.append("    }\n");
        sb.append("    else if (Math.abs(numDaysInOptionsList) < Math.abs(numDaysDesiredInOptionsList)) {\n");
        sb.append("      var NewDay = numDaysInOptionsList+1;\n");
        sb.append(
                "      form.elements(dayComponentName).options[Math.abs(numDaysInOptionsList)] = new Option(NewDay, NewDay);\n");
        sb.append("      numDaysInOptionsList = Math.abs(numDaysInOptionsList) + 1;\n");
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("  return true;\n");
        sb.append("}\n");

        sb.append("// End hiding script contents. -->\n");
        sb.append("</script>\n");
    }

    // NOTE: please use writeDateFormattingJavascript(StringBuffer, boolean),
    // which writes a version of handleFormData() that inputs the forms which
    // compose
    // the date filter, instead of this method.
    // Need to keep this method for writing the no-parameter
    // handleFormData() in order to avoid making lots of downstream code changes
    // to date filters displayed on the query page.
    public static void writeDateFormattingJavascript(
            StringBuffer sb,
            String formContainingDateComponents,
            String formToSubmit) {

        sb.append("<script language=\"JavaScript\">\n");
        sb.append("<!-- Begin hiding script contents from old browsers.\n");

        sb.append("function handleFormData() {\n");
        // Get date-related values from form.
        // These are contained in the dateConfigurationForm.
        sb.append("  var startDayInt = document." + formContainingDateComponents
                  + ".startDateDay.value;\n");
        sb.append("  var startMonthInt = document." + formContainingDateComponents
                  + ".startDateMonth.value;\n");
        sb.append("  var startYearInt = document." + formContainingDateComponents
                  + ".startDateYear.value;\n");
        sb.append("  var endDayInt = document." + formContainingDateComponents
                  + ".endDateDay.value;\n");
        sb.append("  var endMonthInt = document." + formContainingDateComponents
                  + ".endDateMonth.value;\n");
        sb.append("  var endYearInt = document." + formContainingDateComponents
                  + ".endDateYear.value;\n");

        // If date range is invalid (e.g., startDate does not precede endDate) abort
        // and
        // popup alert window.
        sb.append("  if (!(isDateRangeValid(startDayInt, startMonthInt, startYearInt,\n");
        sb.append("      endDayInt, endMonthInt, endYearInt))) {\n");
        sb.append("    alert('Invalid date range!\\nPlease reselect start and/or end dates.');\n");
        sb.append("  }\n");
        sb.append("  else {\n");

        // Reset selected day value to be valid, if invalid, based on selected month
        // and year.
        //sb.append(" var startDayInt = getValidDay(startDayInt, startMonthInt,
        // startYearInt);\n");
        //sb.append(" var endDayInt = getValidDay(endDayInt, endMonthInt,
        // endYearInt);\n");

        // Get text versions of selected day, month, and year for startDate.
        // These are contained in the dateConfigurationForm.
        sb.append("  var startDayString = startDayInt;\n");
        sb.append("  var startMonthString = " + "document."
                  + formContainingDateComponents + ".startDateMonth.options["
                  + "document." + formContainingDateComponents
                  + ".startDateMonth.selectedIndex].text;\n");
        sb.append("  var startYearString = " + "document."
                  + formContainingDateComponents + ".startDateYear.options["
                  + "document." + formContainingDateComponents
                  + ".startDateYear.selectedIndex].text;\n");

        // Get text versions of selected day, month, and year, for endDate.
        // These are contained in the dateConfigurationForm.
        sb.append("  var endDayString = endDayInt;\n");
        sb.append("  var endMonthString = " + "document."
                  + formContainingDateComponents + ".endDateMonth.options["
                  + "document." + formContainingDateComponents
                  + ".endDateMonth.selectedIndex].text;\n");
        sb.append("  var endYearString = " + "document."
                  + formContainingDateComponents + ".endDateYear.options["
                  + "document." + formContainingDateComponents
                  + ".endDateYear.selectedIndex].text;\n");

        // Construct startDate string and endDate string out of text versions of
        // day, month, and year.
        sb.append("  var startDateString = assembleDateString(startDayString, startMonthString, startYearString);\n");
        sb.append("  var endDateString = assembleDateString(endDayString, endMonthString, endYearString);\n");

        // Set values of hidden fields 'startDate' and 'endDate' in
        // allDataConfigurationForm.
        sb.append("  document." + formToSubmit
                  + ".startDate.value = startDateString;\n");
        sb.append("  document." + formToSubmit
                  + ".endDate.value = endDateString;\n");
        sb.append("  document." + formToSubmit + ".submit(document." + formToSubmit
                  + ")\n");
        sb.append("  }\n"); // end if (isDateRangeValid())
        sb.append("  return false;\n");
        sb.append("}\n");

        // Use Math.abs() function to force strings to be considered as ints.
        //sb.append("function getValidDay(dayInt, monthInt, yearInt) {\n");
        //sb.append(" var isDateValid = false;\n");
        //sb.append(" while ((!isDateValid) && (Math.abs(dayInt) >= 28)) {\n");
        //sb.append(" isDateValid = true;\n");
        //sb.append(" if (Math.abs(dayInt) == 31) {\n");
        //sb.append(" if ((Math.abs(monthInt) == 1) || (Math.abs(monthInt) == 3)
        // ||\n" +
        //          " (Math.abs(monthInt) == 5) || (Math.abs(monthInt) == 8) || \n" +
        //          " (Math.abs(monthInt) == 10)) {\n");
        //sb.append(" isDateValid = false;\n");
        //sb.append(" }\n");
        //sb.append(" }\n");
        //sb.append(" else if (Math.abs(dayInt) == 30) {\n");
        //sb.append(" if (Math.abs(monthInt) == 1) {\n");
        //sb.append(" isDateValid = false;\n");
        //sb.append(" }\n");
        //sb.append(" }\n");
        //sb.append(" else if (Math.abs(dayInt) == 29) {\n");
        //sb.append(" if (Math.abs(monthInt) == 1) {\n");
        //sb.append(" if ((yearInt/4)!=Math.floor(yearInt/4)) {\n");
        //sb.append(" isDateValid = false;\n");
        //sb.append(" }\n");
        //sb.append(" }\n");
        //sb.append(" }\n");
        //sb.append(" else if (Math.abs(dayInt) == 28) {\n");
        //sb.append(" if (Math.abs(monthInt) == 1) {\n");
        //sb.append(" if ((yearInt/4)==Math.floor(yearInt/4)) {\n");
        //sb.append(" isDateValid = false;\n");
        //sb.append(" }\n");
        //sb.append(" }\n");
        //sb.append(" }\n");
        //sb.append(" if (!isDateValid) {\n");
        //sb.append(" dayInt = Math.abs(dayInt) - 1;\n");
        //sb.append(" }\n");
        //sb.append(" }\n");
        //sb.append(" if (Math.abs(dayInt) < 10) {\n");
        //sb.append(" dayInt = '0' + dayInt;\n");
        //sb.append(" }\n");
        //sb.append(" return dayInt;\n");
        //sb.append("}\n");

        // Use Math.abs() function to force strings to be considered as ints.
        sb.append("function isDateRangeValid(startDateDay, startDateMonth, startDateYear,\n");
        sb.append("    endDateDay, endDateMonth, endDateYear) {\n");
        sb.append("  var returnVal = false;\n");
        sb.append("  if (Math.abs(startDateYear) < Math.abs(endDateYear)) {\n");
        sb.append("    returnVal = true;\n");
        sb.append("  }\n");
        sb.append("  else if ((Math.abs(startDateYear) == Math.abs(endDateYear)) &&\n"
                  + "           (Math.abs(startDateMonth) < Math.abs(endDateMonth))) {\n");
        sb.append("    returnVal = true;\n");
        sb.append("  }\n");
        sb.append("  else if ((Math.abs(startDateYear) == Math.abs(endDateYear)) &&\n"
                  + "           (Math.abs(startDateMonth) == Math.abs(endDateMonth)) &&\n"
                  +
                  // wal - 8/14/03 - it is ok to have dates the same, look at data
                  // details.
                  //" (Math.abs(startDateDay) < Math.abs(endDateDay))) {\n");
                  "           (Math.abs(startDateDay) <= Math.abs(endDateDay))) {\n");
        sb.append("    returnVal = true;\n");
        sb.append("  }\n");
        sb.append("  return returnVal;\n");
        sb.append("}\n");
        sb.append("\n");

        sb.append("function assembleDateString(dayString, monthString, yearString) {\n");
        sb.append("  var dateString = \"\" + dayString + monthString + yearString;\n");
        sb.append("  return dateString;\n");
        sb.append("}\n");
        sb.append("\n");

        sb.append("function getNumDaysInMonth(monthID, yearID) {\n");
        sb.append("  var returnVal = 0;\n");
        sb.append("  if ((Math.abs(monthID) == 3) ||\n");
        sb.append("      (Math.abs(monthID) == 5) ||\n");
        sb.append("      (Math.abs(monthID) == 8) ||\n");
        sb.append("      (Math.abs(monthID) == 10)) {\n");
        sb.append("    returnVal = 30;\n");
        sb.append("  }\n");
        sb.append("  else if (Math.abs(monthID) == 1) {\n");
        sb.append("    if ((yearID/4)==Math.floor(yearID/4)) {\n");
        sb.append("      returnVal = 29;\n");
        sb.append("    }\n");
        sb.append("    else {\n");
        sb.append("      returnVal = 28;\n");
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("  else {\n");
        sb.append("    returnVal = 31;\n");
        sb.append("  }\n");
        sb.append("  return returnVal;\n");
        sb.append("}\n");
        sb.append("\n");

        // Calls handleMonthChange(). Need this to reset number of days for
        // February,
        // depending on whether or not selected year is a leap year.
        sb.append("function handleYearChange(selectComponent) {\n");
        sb.append("  var form = selectComponent.form;\n");
        sb.append("  var selectComponentName  = selectComponent.name;\n");
        sb.append("  var monthComponentName;\n");
        sb.append("  if (selectComponentName == 'startDateYear') {\n");
        sb.append("    monthComponentName = 'startDateMonth';\n");
        sb.append("  }\n");
        sb.append("  else {\n");
        sb.append("    monthComponentName = 'endDateMonth';\n");
        sb.append("  }\n");
        sb.append("  var monthComponent = form.elements(monthComponentName);\n");
        sb.append("  return handleMonthChange(monthComponent);\n");
        sb.append("}\n");
        sb.append("\n");

        // Use Math.abs() function to force strings to be considered as ints.
        sb.append("function handleMonthChange(selectComponent) {\n");
        sb.append("  var form = selectComponent.form;\n");
        sb.append("  var monthID = selectComponent.options[selectComponent.selectedIndex].value;\n");
        sb.append("  var yearComponentName;\n");
        sb.append("  var dayComponentName;\n");
        sb.append("  var selectComponentName  = selectComponent.name;\n");
        sb.append("  if (selectComponentName == 'startDateMonth') {\n");
        sb.append("    yearComponentName = 'startDateYear';\n");
        sb.append("    dayComponentName = 'startDateDay';\n");
        sb.append("  }\n");
        sb.append("  else {\n");
        sb.append("    yearComponentName = 'endDateYear';\n");
        sb.append("    dayComponentName = 'endDateDay';\n");
        sb.append("  }\n");
        sb.append(
                "  var yearID = form.elements(yearComponentName).options[form.elements(yearComponentName).selectedIndex].value;\n");
        sb.append("  var numDaysDesiredInOptionsList = getNumDaysInMonth(monthID, yearID);\n");
        sb.append("  var numDaysInOptionsList = form.elements(dayComponentName).options.length;\n");
        sb.append("  while (Math.abs(numDaysInOptionsList) !== Math.abs(numDaysDesiredInOptionsList)) {\n");
        sb.append("    if (Math.abs(numDaysInOptionsList) > Math.abs(numDaysDesiredInOptionsList)) {\n");
        sb.append(
                "      if (Math.abs(form.elements(dayComponentName).selectedIndex) == Math.abs(numDaysInOptionsList - 1)) {\n");
        sb.append("        form.elements(dayComponentName).selectedIndex = numDaysInOptionsList - 2;\n");
        sb.append("      }\n");
        sb.append("      form.elements(dayComponentName).options[Math.abs(numDaysInOptionsList) - 1] = null;\n");
        sb.append("      numDaysInOptionsList = Math.abs(numDaysInOptionsList) - 1;\n");
        sb.append("    }\n");
        sb.append("    else if (Math.abs(numDaysInOptionsList) < Math.abs(numDaysDesiredInOptionsList)) {\n");
        sb.append("		 var NewDay = numDaysInOptionsList+1;\n");
        sb.append(
                "      form.elements(dayComponentName).options[Math.abs(numDaysInOptionsList)] = new Option(NewDay, NewDay);\n");
        sb.append("      numDaysInOptionsList = Math.abs(numDaysInOptionsList) + 1;\n");
        sb.append("    }\n");
        sb.append("  }\n");
        sb.append("  return true;\n");
        sb.append("}\n");

        sb.append("// End hiding script contents. -->\n");
        sb.append("</script>\n");
    }

    // return a date string if the input date string is not null
    // if it is null, return a date x days back from "today"
    public static String getDateStringWithFailover(String inputDateString,
                                                   int numDaysAgo) {
        String returnString = inputDateString;
        if ((returnString == null) || (returnString == "")) {
            returnString = DateHelper.getFormattedDateString(numDaysAgo);
        }
        return returnString;
    }

    /**
     * If startDate and endDate parameters from URL are the same, set startDate to be 90 days before today. If endDate
     * is after new startDate, set endDate to today. If endDate is not after new startDate, set startDate to be 45 days
     * before endDate, and then set endDate to be 45 days after startDate. Returned ArrayList contains two strings:
     * startDateString and endDateString.
     */
    public static ArrayList setDateRangeDefault(String startDateString,
                                                String endDateString) {
        ArrayList returnArrayList = new ArrayList();
        int windowLength = 90;

        if ((startDateString == null) || (startDateString == "")) {
            startDateString = DateHelper.getFormattedDateString(0);
        }

        if ((endDateString == null) || (endDateString == "")) {
            endDateString = DateHelper.getFormattedDateString(0);
        }

        if (startDateString.equalsIgnoreCase(endDateString)) {
            startDateString = DateHelper.getFormattedDateString(windowLength);
            if (((DateHelper.getDate(startDateString)).before(DateHelper.getDate(endDateString)))) {
                endDateString = DateHelper.getFormattedDateString(0);
            } else {
                startDateString = DateHelper.getDate(endDateString,
                                                     (int) (windowLength / 2));
                endDateString = DateHelper.getDate(startDateString,
                                                   (int) (0 - windowLength));
            }
        }
        returnArrayList.add(startDateString);
        returnArrayList.add(endDateString);
        return returnArrayList;
    }
}
