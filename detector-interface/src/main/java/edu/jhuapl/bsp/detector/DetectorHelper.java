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

import edu.jhuapl.bsp.detector.util.DateHelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Helper Methods used in detection
 */
public class DetectorHelper {

    public final static double MIN_PROB_LEVEL = 0.001;

    public static void postDetectionColorCoding(double[] data, double[] levels, double[] colors,
                                                double red, double yellow, double defaultLevel,
                                                boolean greaterThan) {
        for (int i = 0; i < levels.length; i++) {
            // remove levels when data == 0;
            // remove negative levels and replace with 0;
            if (data[i] <= 0) {
                levels[i] = defaultLevel;
                colors[i] = 0;
            }

            if (greaterThan) {
                // red alert
                if (levels[i] >= red) {
                    colors[i] = 3;
                }
                // yellow alert
                else if (levels[i] >= yellow) {
                    colors[i] = 2;
                } else if (levels[i] != 0 && levels[i] != 0.5) {
                    colors[i] = 1;
                } else {
                    colors[i] = 0;
                }
            } else {
                // red alert
                if (levels[i] <= red) {
                    colors[i] = 3;
                }
                // yellow alert
                else if (levels[i] <= yellow) {
                    colors[i] = 2;
                } else if (levels[i] != 0 && levels[i] != 0.5) {
                    colors[i] = 1;
                } else {
                    colors[i] = 0;
                }
            }
        }
    }

    /**
     * Turn arraylist into array and turn "null" into a null
     *
     * @param list The array to convert
     */
    public static String[] getStrataArray(ArrayList list) {
        String[] returnArray = null;
        if (list.size() > 0) {
            returnArray = new String[list.size()];
            String tmp = "";
            for (int i = 0; i < list.size(); i++) {
                tmp = (String) list.get(i);
                if (tmp.equalsIgnoreCase("null") || tmp.equalsIgnoreCase("null-null")) {
                    returnArray[i] = null;
                } else {
                    returnArray[i] = tmp;
                }
            }
        } else {
            returnArray = new String[1];
            returnArray[0] = null;
        }
        return returnArray;
    }

    /**
     * Perform a query and return the results.
     *
     * @param conn The database connection to the internal database.
     */
    public static double[] getData(Connection conn, String queryString, java.util.Date startDate,
                                   java.util.Date endDate) {

        int daysDiff = DateHelper.getDateDifference(startDate, endDate);
        double[] data = null;
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(queryString);
            data = DetectorHelper.getResultArray(rs, daysDiff, startDate);
            rs.close();
            statement.close();
        } catch (SQLException sqle) {
            System.err.println("Error - [DetectorHelper] - " + sqle.getMessage());
            sqle.printStackTrace();
        }

        return data;
    }

    /**
     * Perform a query and return the results.
     *
     * @param conn The database connection to the internal database.
     */
    public static double[] getPossibleData(Connection conn, String queryString,
                                           java.util.Date startDate, java.util.Date endDate) {

        int daysDiff = DateHelper.getDateDifference(startDate, endDate);
        double[] data = null;
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(queryString);
            data = DetectorHelper.getResultArray(rs, daysDiff, startDate);
            rs.close();
            statement.close();
        } catch (SQLException sqle) {
            // a possible data query doesn't spit out all the error checking
            // its quite possible the table doesn't exist.
        }

        return data;
    }

    /**
     * Pull the data out of the result set and date template it.
     *
     * @param resultSet The ResultSet from the query.
     * @param size      size of the data array desired.
     * @param startDate the first date of the data array.
     */
    public static double[] getResultArray(ResultSet resultSet, int size, java.util.Date startDate) {
        double[] returnArray = new double[size];

        Calendar cal = new GregorianCalendar();
        if (startDate != null) {
            cal.setTime(startDate);
        } else {
            cal.setTime(new java.util.Date());
            cal.add(Calendar.DATE, -(size - 1));
        }

        Calendar cal2 = new GregorianCalendar();

        try {
            int index = 0;
            while (resultSet.next() && index < size) {
                cal2.setTime(resultSet.getDate("Date"));
                while ((cal.get(Calendar.DATE) != cal2.get(Calendar.DATE)) ||
                       (cal.get(Calendar.MONTH) != cal2.get(Calendar.MONTH)) ||
                       (cal.get(Calendar.YEAR) != cal2.get(Calendar.YEAR))) {
                    cal.add(Calendar.DATE, 1);
                    returnArray[index] = 0;
                    index++;
                }
                returnArray[index] = resultSet.getDouble("Count");
                index++;
                cal.add(Calendar.DATE, 1);
            }
            while (index < size) {
                returnArray[index] = 0;
                index++;
            }
        } catch (SQLException sqle) {
            System.err.println("ERROR - [DetectorHelper]: " + sqle.getMessage());
            sqle.printStackTrace();
        }
        return returnArray;
    }

    /**
     * Helper method used to create the SQL string for the insert into command.
     *
     * @param table      Name of the table to insert the results into.
     * @param fieldNames The field names of the table which will be populated.
     */
    public static String createInsertQueryString(String table, String[] fieldNames) {
        String insertString = "INSERT INTO " + table + " (";
        String tmp = "";
        for (int j = 0; j < (fieldNames.length - 1); j++) {
            tmp = tmp + "[" + fieldNames[j] + "], ";
        }
        insertString = insertString + tmp + "[" + fieldNames[fieldNames.length - 1] + "]) VALUES (";
        tmp = "";
        for (int j = 0; j < (fieldNames.length - 1); j++) {
            tmp = tmp + "?, ";
        }
        insertString = insertString + tmp + "?)";

        return insertString;
    }

    /**
     * Helper method used to instantiate an object from a string.
     *
     * @param className The name of the object to instantiate.
     */
    public static Object createObject(String className) {
        Object object = null;
        try {
            Class classDefinition = Class.forName(className);
            object = classDefinition.newInstance();
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }
        return object;
    }

}
