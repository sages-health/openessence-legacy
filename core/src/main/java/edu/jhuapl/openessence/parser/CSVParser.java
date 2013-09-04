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
package edu.jhuapl.openessence.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

/**
 * This class provides methods that will read a CSV file and return results as an array.
 */
public class CSVParser {

    public String[][] parse(File file, char delimiter, char qualifier,
                            int rowsToSkip) {
        return parse(file, delimiter, qualifier,
                     rowsToSkip, -1);
    }

    public String[][] parse(File file, char delimiter, char qualifier,
                            int rowsToSkip, int numRowsToRead) {
        List<String[]> data = new ArrayList<String[]>();
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(file), delimiter, qualifier,
                                   rowsToSkip < 0 ? 0 : rowsToSkip);
            int rowsCollected = 0;
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                data.add(nextLine);
                rowsCollected++;
                // read only first N rows if numRowsToRead != -1
                if (numRowsToRead != -1 && rowsCollected >= numRowsToRead) {
                    break;
                }
            }
        } catch (IOException e) {
            Log.error("Could not parse CSV file.", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                Log.error("Could not parse CSV file.", e);
            }
        }

        return data.toArray(new String[data.size()][]);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String>[] parse(File file, char delimiter, char qualifier,
                                       int rowsToSkip, int numRowsToRead, String[] fields) {
        List<Map<String, String>> records = new ArrayList<Map<String, String>>();
        CSVReader reader = null;
        try {
            // CSV read
            reader = new CSVReader(new FileReader(file), delimiter, qualifier,
                                   rowsToSkip < 0 ? 0 : rowsToSkip);

            // count for how many rows we have read from CSV
            int rowsCollected = 0;
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                Map<String, String> recordMap = new HashMap<String, String>();
                int ix = 0;
                // Consider all cells in this row are empty
                boolean emptyRow = true;
                for (String field : fields) {
                    // if this row does not have enough fields
                    if (nextLine.length > ix) {
                        // if no cell has value so far, then check if current cell has value or not
                        if (emptyRow && nextLine[ix] != null && nextLine[ix].length() > 0) {
                            emptyRow = false;
                        }
                        recordMap.put(field, nextLine[ix]);
                    }
                    ix++;
                }
                // if atleast one cell in this row is populated
                if (!emptyRow) {
                    records.add(recordMap);
                }

                rowsCollected++;
                // read only first N rows if numRowsToRead != -1
                if (numRowsToRead != -1 && rowsCollected >= numRowsToRead) {
                    break;
                }
            }
        } catch (IOException e) {
            Log.error("Could not parse CSV file.", e);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.error("Could not parse CSV file.", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                Log.error("Could not parse CSV file.", e);
            }
        }

        return (Map<String, String>[]) records.toArray(new HashMap[records.size()]);
    }
}
