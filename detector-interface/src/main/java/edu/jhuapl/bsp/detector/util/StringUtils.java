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

import java.util.ArrayList;

/**
 * Helper class that contains some string manipulating methods
 */
public class StringUtils {

    public static int convertToInt(String s, int defaultValue) {
        int x = defaultValue;

        try {
            x = Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
        }

        return x;
    }

    // Some day you could make this better.
    public static String makePlural(String x) {
        if (x.endsWith("x")) {
            return x + "es";
        } else if (x.endsWith("s")) {
            return x + "es";
        } else if (x.endsWith("y")) {
            return x.substring(0, x.length() - 1) + "ies";
        } else {
            return x + "s";
        }
    }

    public static String[] arrayListToStringArray(ArrayList<String> arrayList) {
        String[] returnArray = null;
        if ((arrayList != null) && (arrayList.size() > 0)) {
            returnArray = new String[arrayList.size()];
            for (int i = 0; i < arrayList.size(); i++) {
                returnArray[i] = arrayList.get(i);
            }
        }
        return returnArray;
    }

    public static String[] convertCommaSeparatedInput(String value) {
        if (value == null) {
            value = "";

        }

        return value.split(",");
    }

    public static String[] convertCarotToPercent(String[] values) {
        for (int i = 0; i < values.length; i++) {
            values[i] = StringUtils.convertCarotToPercent(values[i]);
        }
        return values;
    }

    public static String convertCarotToPercent(String value) {
        if (value != null) {
            value = value.replaceAll("\\^", "%");
        }
        return value;
    }

    public static String stringArrayToString(String[] stringArray) {
        String returnString = "";
        if (stringArray == null) {
            returnString = returnString + "null";
        } else {
            if (stringArray.length > 0) {
                if (stringArray[0] != null) {
                    returnString = returnString + stringArray[0];
                }
            }
            for (int i = 1; i < stringArray.length; i++) {
                if (stringArray[i] != null) {
                    returnString = returnString + ", " + stringArray[i];
                }
            }
        }
        return returnString;
    }

    // Converts an array of strings to a comma-separated list.
    public static String stringArrayToString(Object[] objectArray) {
        String returnString = "";
        if (objectArray != null) {
            String[] stringArray = new String[objectArray.length];
            for (int i = 0; i < objectArray.length; i++) {
                stringArray[i] = (String) objectArray[i];
            }
            returnString = stringArrayToString(stringArray);
        }
        return returnString;
    }

    // Converts an array of strings to a comma-separated list with 'and' before
    // the
    // last item.
    public static String stringArrayToStringWithAnd(String[] stringArray) {
        String returnString = "";

        if ((stringArray == null) || (stringArray.length == 0)) {
            returnString = "null";
        } else {
            for (int i = 0; i < stringArray.length - 1; i++) {
                if (stringArray[i] != null) {
                    returnString = returnString + ", " + stringArray[i];
                }
            }

            if (returnString.length() >= 2) {
                // Chop off initial ", ".
                returnString = returnString.substring(2);

                // Replace final ", " with " and ".
                int lastOccurrence = returnString.lastIndexOf(", ");
                if (lastOccurrence >= 1) {
                    returnString = returnString.substring(0, lastOccurrence) + " and " +
                                   returnString.substring(lastOccurrence + 2);
                }
            }
        }

        return returnString;
    }

    public static ArrayList<String> stringArrayToArrayList(String[] stringArray) {
        ArrayList<String> returnArray = new ArrayList<String>();
        if ((stringArray != null) && (stringArray.length > 0)) {
            for (int i = 0; i < stringArray.length; i++) {
                returnArray.add(stringArray[i]);
            }
        }
        return returnArray;
    }

}
