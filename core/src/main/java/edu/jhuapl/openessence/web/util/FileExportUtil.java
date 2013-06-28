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

package edu.jhuapl.openessence.web.util;

import edu.jhuapl.openessence.datasource.Record;

import org.springframework.util.StringUtils;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

/**
 * Utility methods for exporting the UI data into a file format that can be opened by a 3rd party application. For
 * example, CSV, xls, and txt files
 */
public class FileExportUtil {

    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final
    SimpleDateFormat SIMPLE_DATE_TIME_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS aaa z");


    /**
     * Generates CSV structured text of the data in the DataDetails Grid.
     *
     * @param writer        what to write the CSV data to
     * @param reportHeaders The headers in the DataDetails grid
     * @param records       The collection of records that fill the data details grid
     * @param timezone      time zone of the request/client
     */
    public static void exportGridToCSV(PrintWriter writer, String[] reportHeaders, Collection<Record> records,
                                       TimeZone timezone) {

        SimpleDateFormat dtFormat = (SimpleDateFormat) SIMPLE_DATE_TIME_FORMAT.clone();
        dtFormat.setTimeZone(timezone);

        // building csv from the grid's headers
        String headerList = StringUtils.collectionToDelimitedString(Arrays.asList(reportHeaders), ",");
        writer.append(headerList);
        writer.append('\n');

        // building csv for the collection of Records
        List<String> recordStrings = new ArrayList<String>();
        for (Record r : records) {
            Set<String> rids = r.getResultIds();
            for (String rid : rids) {
                String value = "";
                if (r.getValue(rid) != null) {
                    switch (r.getDimension(rid).getSqlType()) {
                        case DATE:
                            Date date = (Date) r.getValue(rid);
                            value = "\"" + SIMPLE_DATE_FORMAT.format(date) + "\"";
                            break;
                        case DATE_TIME:
                            Date dateTime = (Date) r.getValue(rid);
                            value = "\"" + dtFormat.format(dateTime) + "\"";
                            break;
                        case INTEGER:
                        case FLOAT:
                            value = String.valueOf(r.getValue(rid));
                            break;
                        default:
                            value = "\"" + String.valueOf(r.getValue(rid)) + "\"";
                            break;
                    }
                }
                recordStrings.add(value);
            }
            writer.append(StringUtils.collectionToDelimitedString(recordStrings, ","));
            writer.append('\n');
            recordStrings.clear();
        }
    }
}
