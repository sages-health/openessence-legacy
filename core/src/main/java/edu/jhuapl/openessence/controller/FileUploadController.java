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

import au.com.bytecode.opencsv.CSVReader;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for file upload
 */
@Controller
public class FileUploadController {

    /**
     * Accept a CSV file, process it, and give it back to the client. This could all be done on the client with the
     * HTML5 File API, but that's IE10+.
     *
     * Unfortunately, there's no standard way to do content negotiation on multipart files. This method uses a parameter
     * (_uploadContentType) instead of a header because HTML forms cannot send custom headers.
     *
     * This methods returns a ResponseEntity instead of using @ResponseBody because we must return a content type of
     * text/html (not application/json). This is because the upload form is submitted in an iframe.
     */
    // the params=_uploadContentTYpe=text/csv will do a strict comparison, so don't expect real content negotiation.
    // this means text/csv;utf=8 won't work!
    @RequestMapping(value = "/file", method = RequestMethod.POST, params = "_uploadContentType=text/csv")
    public ResponseEntity<String> uploadCsvFile(@RequestPart MultipartFile file,
                                                @RequestParam(value = "delimiter", defaultValue = ",") char delimiter,
                                                @RequestParam(value = "qualifier", defaultValue = "\"") char qualifier,
                                                @RequestParam(value = "rowsToSkip", defaultValue = "0") int rowsToSkip,
                                                @RequestParam(value = "numRowsToRead", defaultValue = "-1") int numRowsToRead,
                                                @RequestParam("fields") String fields,
                                                @RequestParam("_uploadContentType") String uploadContentType)
            throws IOException {

        if (rowsToSkip < 0) {
            rowsToSkip = 0;
        }

        if (rowsToSkip == 0) {
            // even if client requests 0 rows, give them everything
            // why would they knowingly ask for 0 rows?
            rowsToSkip = -1;
        }

        // TODO send real array over the wire
        String[] splitFields = fields.split(",");

        // CSVReader wraps the Reader is a buffer already, so don't double buffer
        Reader fileReader = new InputStreamReader(file.getInputStream());
        CSVReader csvReader = new CSVReader(fileReader, delimiter, qualifier, rowsToSkip);
        List<String[]> csvData = csvReader.readAll();
        List<String[]> subList;
        if (numRowsToRead > 0) {
            subList = csvData.subList(0, numRowsToRead);
        } else {
            // give them everything
            subList = csvData;
        }

        Map<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("rows", gridToMap(subList, splitFields));
        responseData.put("success", true);

        ObjectMapper mapper = new ObjectMapper();
        return new ResponseEntity<>(mapper.writeValueAsString(responseData), HttpStatus.CREATED);
    }

    /**
     * Convert a grid of data to the map format expected by our Ext grids.
     * TODO think of a better name for this method
     */
    private List<Map<String, String>> gridToMap(Collection<String[]> data, String[] fields) {
        List<Map<String, String>> records = new ArrayList<>();

        for (String[] row : data) {
            Map<String, String> record = new HashMap<>();
            int ix = 0;
            boolean emptyRow = true; // Assume all cells in this row to be empty by default

            for (String field : fields) {
                // if this row does not have enough fields
                if (row.length > ix) {
                    String cell = row[ix];
                    // if no cell has value so far, then check if current cell has value or not
                    if (emptyRow && cell != null && !cell.isEmpty()) {
                        emptyRow = false;
                    }
                    record.put(field, cell);
                }
                ix++;
            }
            // if at least one cell in this row is populated
            if (!emptyRow) {
                records.add(record);
            }
        }

        return records;
    }
}
