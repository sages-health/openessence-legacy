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

import edu.jhuapl.openessence.datasource.Dimension;
import edu.jhuapl.openessence.datasource.FieldType;
import edu.jhuapl.openessence.datasource.OeDataSource;
import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.entry.ChildRecordSet;
import edu.jhuapl.openessence.datasource.entry.DbKeyValMap;
import edu.jhuapl.openessence.datasource.jdbc.DataTypeConversionHelper;
import edu.jhuapl.openessence.datasource.jdbc.JdbcOeDataSource;
import edu.jhuapl.openessence.datasource.jdbc.entry.JdbcOeDataEntrySource;
import edu.jhuapl.openessence.datasource.jdbc.entry.TableAwareQueryRecord;
import edu.jhuapl.openessence.model.ChartData;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.ArrayUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTimeZone;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.context.request.WebRequest;

import java.awt.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

public class ControllerUtils {

    public static final String ERROR_MESSAGE_RESPONSE_TEMPLATE = "{ success: false, message: '%s' }";

    private static final String PARSE_ERROR_MESSAGE_TEMPLATE =
            "Request parameter for dimension %s does not match configured type %s.";

    private ControllerUtils() {
    }

    /**
     * Check if the specified user is authorized to access the given data source.
     *
     * @param authentication user's authentication
     */
    public static boolean isUserAuthorized(Authentication authentication, JdbcOeDataSource ds) {
        Set<String> roles = ds.getRoles();
        if (roles == null || roles.isEmpty()) {
            return true;
        }

        for (GrantedAuthority eachAuthority : authentication.getAuthorities()) {
            if (roles.contains(eachAuthority.toString())) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param jdes         The data entry source
     * @param parameterMap map of parameters to parse
     * @return a map of string to value of the parse parameters (as a DbKeyValMap)
     */
    public static DbKeyValMap parseKeyValueMap(final JdbcOeDataEntrySource jdes, final Map<String, ?> parameterMap)
            throws ErrorMessageException {
        final DbKeyValMap dbKeyValMap = new DbKeyValMap();
        // TODO: Can you get here without pks?
        for (final String primaryKey : jdes.getParentTableDetails().getPks()) {
            final Object parameters = parameterMap.get(primaryKey);

            final String parameter;
            if (parameters instanceof String[]) {
                // Parameter map from request
                if (!ArrayUtils.isEmpty((String[]) parameters) && ((String[]) parameters).length == 1) {
                    // Behavior of get parameter, should only have one. (error thrown otherwise)
                    parameter = ((String[]) parameters)[0];
                } else {
                    throw new ErrorMessageException(
                            String.format("Expecting one value for primary key: %s", primaryKey));
                }
            } else {
                // Map from json
                parameter = (String) parameters;
            }

            if (parameter == null) {
                throw new ErrorMessageException(String.format("No request value for primary key: %s", primaryKey));
            }

            final Dimension dimension = jdes.getEditDimension(primaryKey);
            if (dimension == null) {
                throw new IllegalStateException(primaryKey + " is not an edit dimension");
            }

            dbKeyValMap.putAll(formatData(primaryKey, parameter, dimension.getSqlType(), true));
        }

        return dbKeyValMap;
    }

    /**
     * @param jdes    the data entry source
     * @param request the request/parameters to parse for child records
     * @param isAdd   true if called from add, (add needs to skip autogen, etc... fields)
     * @return a list of child record sets with data parsed from request
     */
    public static List<ChildRecordSet> getChildRecordSets(final JdbcOeDataEntrySource jdes,
                                                          final HttpServletRequest request, final boolean isAdd)
            throws ErrorMessageException, IOException {

        final List<ChildRecordSet> recordSets = new ArrayList<ChildRecordSet>();

        final Map<String, Map<String, Dimension>> childDimsByTbl = new LinkedHashMap<String, Map<String, Dimension>>();
        for (final String tableName : jdes.getChildTableMap().keySet()) {
            childDimsByTbl.put(tableName, new LinkedHashMap<String, Dimension>());
            for (String dimensionId : jdes.getChildTableMap().get(tableName).getDimensionIds()) {
                childDimsByTbl.get(tableName).put(dimensionId, jdes.getEditDimension(dimensionId));
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<Map<String, String>>> typeRef = new TypeReference<List<Map<String, String>>>() {
        };

        for (final String tbl : jdes.getChildTableMap().keySet()) {
            final String childTableData = request.getParameter(tbl);
            if (childTableData != null) {
                Set<String> fkToParent = jdes.getChildTableMap().get(tbl).getFksToParent().keySet();
                List<Map<String, String>> records = mapper.readValue(childTableData, typeRef);

                final Set<String> pks = jdes.getChildTableMap().get(tbl).getPks();
                final List<TableAwareQueryRecord> newrecs = new ArrayList<TableAwareQueryRecord>();
                for (final Map<String, String> record : records) {
                    final Map<String, Object> dsFields = new LinkedHashMap<String, Object>();
                    for (final Entry<String, String> entry : record.entrySet()) {
                        final String key = entry.getKey();
                        // 
                        final Dimension dimension = jdes.getEditDimension(entry.getKey());

                        // Add: Foreign key to parent, auto generated, special sql, and pk dimensions are not required
                        // Update: Pks are required
                        final boolean isRequired =
                                (isAdd ? (!fkToParent.contains(key)
                                          && jdes.getAutoGeneratedDimension(dimension.getId()) == null
                                          && jdes.getSpecialSqlDimension(dimension.getId()) == null
                                          && pks.contains(dimension.getId()))
                                       : (pks.contains(dimension.getId())));

                        dsFields.putAll(formatData(key, entry.getValue(), dimension.getSqlType(), isRequired));
                    }
                    newrecs.add(new TableAwareQueryRecord(childDimsByTbl.get(tbl), dsFields));
                }

                recordSets.add(new ChildRecordSet(tbl, pks, newrecs));
            }
        }

        return recordSets;
    }

    /**
     * Creates a map of key to formatted value (if type requires formatting), throws exception if value cannot be
     * formatted.
     *
     * @param key        the string to map to
     * @param value      the string to check/format based on dimension type
     * @param type       the field type (DATE, DATE_TIME, INTEGER, FLOAT)
     * @param isRequired if true throw exception on failure/null
     * @return a map of key to formatted object (if type requires formatting)
     */
    public static Map<String, Object> formatData(final String key, final String value, final FieldType type,
                                                 final boolean isRequired) throws ErrorMessageException {
        final Map<String, Object> data = new HashMap<String, Object>();
        if (type != null) {
            if (value != null) {
                switch (type) {
                    case DATE:
                    case DATE_TIME: {
                        try {
                            data.put(key, DataTypeConversionHelper.dateFromString(value));
                        } catch (Exception e) {
                            throw new ErrorMessageException(e.getMessage());
                        }
                        break;
                    }
                    case INTEGER: {
                        try {
                            data.put(key, Integer.parseInt(value));
                        } catch (NumberFormatException n) {
                            throw new ErrorMessageException(
                                    String.format(PARSE_ERROR_MESSAGE_TEMPLATE, key, FieldType.INTEGER), n);
                        }
                        break;
                    }
                    case LONG: {
                        try {
                            data.put(key, Long.parseLong(value));
                        } catch (NumberFormatException n) {
                            throw new ErrorMessageException(
                                    String.format(PARSE_ERROR_MESSAGE_TEMPLATE, key, FieldType.LONG), n);
                        }
                        break;
                    }
                    case FLOAT: {
                        try {
                            data.put(key, Float.parseFloat(value));
                        } catch (NumberFormatException n) {
                            throw new ErrorMessageException(
                                    String.format(PARSE_ERROR_MESSAGE_TEMPLATE, key, FieldType.FLOAT), n);
                        }
                        break;
                    }
                    case DOUBLE: {
                        try {
                            data.put(key, Double.parseDouble(value));
                        } catch (NumberFormatException n) {
                            throw new ErrorMessageException(
                                    String.format(PARSE_ERROR_MESSAGE_TEMPLATE, key, FieldType.DOUBLE), n);
                        }
                        break;
                    }
                    case BOOLEAN: {
                        data.put(key, Boolean.parseBoolean(value));
                        break;
                    }
                    case TEXT: {
                        data.put(key, value);
                        break;
                    }
                    default:
                        throw new ErrorMessageException(String.format("Unsupported field type for dimension %s", key));
                }
            } else {
                // Throw error if required, if not nullify non required fields (auto generated on adds etc...)
                if (isRequired) {
                    throw new ErrorMessageException(String.format("No value specified for required dimension %s", key));
                } else {
                    data.put(key, null);
                }
            }
        } else {
            throw new ErrorMessageException(String.format("Field type not specified for dimension %s.", key));
        }

        return data;
    }

    /**
     * Builds String to object map for fields/records, updates times to send time in millis
     *
     * @param fields  the set of names/dimensions
     * @param records the map of records/data
     * @return map of field name to data (with times converted to millis)
     */
    public static Map<String, Object> mapDataAndFormatTimeForResponse(final Set<String> fields,
                                                                      final Map<String, Object> records) {
        final Map<String, Object> data = new HashMap<String, Object>();

        for (final String field : fields) {
            final Object object = records.get(field);
            if (object instanceof Timestamp) {
                data.put(field, ((Timestamp) object).getTime());
            } else if (object instanceof Date) {
                data.put(field, ((Date) object).getTime());
            } else {
                data.put(field, object);
            }
        }

        return data;
    }

    public static List<Dimension> getAccumulationsByIds(final OeDataSource ds, final String[] accumulationIds)
            throws OeDataSourceException {
        // if an accumulation is not provided default to all accumulations
        return getAccumulationsByIds(ds, accumulationIds, true);
    }

    public static List<Dimension> getAccumulationsByIds(final OeDataSource ds, final String[] accumulationIds,
                                                        boolean addAllByDefault) throws OeDataSourceException {
        // if an accumulation is not provided default to all accumulations
        final List<Dimension> accumulations;
        if (ArrayUtils.isEmpty(accumulationIds)) {
            accumulations = new ArrayList<Dimension>();
            if (addAllByDefault) {
                accumulations.addAll(ds.getAccumulations());
            }
        } else {
            accumulations = new ArrayList<Dimension>(accumulationIds.length);
            for (final String accumulationId : accumulationIds) {
                final Dimension a = ds.getAccumulation(accumulationId);
                if (a != null) {
                    accumulations.add(a);
                } else {
                    throw new OeDataSourceException("No Such Accumulation " + accumulationId);
                }
            }
        }
        return accumulations;
    }

    public static List<Dimension> getResultDimensionsByIds(final OeDataSource ds, final String[] resultDimensionIds)
            throws OeDataSourceException {
        final List<Dimension> results;
        if (ArrayUtils.isEmpty(resultDimensionIds) || (resultDimensionIds.length == 1 && "all"
                .equalsIgnoreCase(resultDimensionIds[0]))) {
            results = new ArrayList<Dimension>();
            results.addAll(ds.getResultDimensions());
        } else {
            results = new ArrayList<Dimension>(resultDimensionIds.length);
            for (final String resultDimensionId : resultDimensionIds) {
                final Dimension d = ds.getResultDimension(resultDimensionId);
                if (d != null) {
                    results.add(d);
                } else {
                    throw new OeDataSourceException("No Such Result Dimension " + resultDimensionId);
                }
            }
        }
        return results;
    }

    /**
     * Returns a union of the Dimensions in dimension1 and dimension2 based on dimension id.
     */
    public static List<Dimension> unionDimensions(List<Dimension> dimension1, List<Dimension> dimension2) {
        HashMap<String, Dimension> result = new LinkedHashMap<String, Dimension>(dimension1.size() + dimension2.size());
        for (Dimension d : dimension1) {
            result.put(d.getId(), d);
        }
        for (Dimension d : dimension2) {
            result.put(d.getId(), d);
        }
        return new ArrayList<Dimension>(result.values());
    }

    /**
     * Returns a new map that is sorted and then limited to the top {@code limit} values.  It then places the map back
     * in the original sort order minus anything that has been cut.
     */
    public static LinkedHashMap<String, Double> getSortedAndLimitedMap(LinkedHashMap<String, Double> map, Integer limit,
                                                                       String limitLabel) {
        //test if we need to trim
        if (limit <= 0 || limit >= map.size()) {
            return map;
        }

        //sort by value
        Map<String, Double> sortedMap = ControllerUtils.getSortedByValueMap(map);
        //limit and combine results
        Map<String, Double> sortedLimitedMap = ControllerUtils.getLimitedMap(sortedMap, limit, limitLabel);

        //put the original sort order back (minus the values combined)
        LinkedHashMap<String, Double> originalSortResultMap = new LinkedHashMap<String, Double>(limit);
        LinkedHashMap<String, Double> passedValuesMap = new LinkedHashMap<String, Double>(map.size());
        int i = 0;
        for (String key : map.keySet()) {
            if (i < limit) {
                if (sortedLimitedMap.containsKey(key)) {
                    Double value = sortedLimitedMap.get(key);
                    //if value is not null/zero, add it and increment
                    if (value != null && !Double.isNaN(value) && value > 0) {
                        originalSortResultMap.put(key, value);
                        i++;
                    } else { //put it in a list of passed up values for inclusion at the end
                        passedValuesMap.put(key, value);
                    }
                }
            }
        }
        //if we still have room after adding all sorted non zero values... fill the rest with passed values
        if (i < limit) {
            for (String key : passedValuesMap.keySet()) {
                if (i < limit) {
                    originalSortResultMap.put(key, passedValuesMap.get(key));
                    i++;
                }
            }
        }
        //add combined field if it is not null (indicates it was used even if the value is 0)
        Double cVal = sortedLimitedMap.get(limitLabel);
        if (cVal != null && !Double.isNaN(cVal)) {
            originalSortResultMap.put(limitLabel, cVal);
        }
        return originalSortResultMap;
    }

    public static LinkedHashMap<String, ChartData> getSortedAndLimitedChartDataMap(LinkedHashMap<String, ChartData> map,
                                                                                   Integer limit, String limitLabel) {
        //test if we need to trim
        if (limit <= 0 || limit >= map.size()) {
            return map;
        }

        //sort by value
        Map<String, ChartData> sortedMap = ControllerUtils.getSortedByChartDataMap(map);
        //limit and combine results
        Map<String, ChartData> sortedLimitedMap = ControllerUtils.getLimitedChartDataMap(sortedMap, limit, limitLabel);

        //put the original sort order back (minus the values combined)
        LinkedHashMap<String, ChartData> originalSortResultMap = new LinkedHashMap<String, ChartData>(limit);
        LinkedHashMap<String, ChartData> passedValuesMap = new LinkedHashMap<String, ChartData>(map.size());
        int i = 0;
        for (String key : map.keySet()) {
            if (i < limit) {
                if (sortedLimitedMap.containsKey(key)) {
                    ChartData value = sortedLimitedMap.get(key);
                    //if value is not null/zero, add it and increment
                    if (value != null && value.getCount() != null && !Double.isNaN(value.getCount())
                        && value.getCount() > 0) {
                        originalSortResultMap.put(key, value);
                        i++;
                    } else { //put it in a list of passed up values for inclusion at the end
                        passedValuesMap.put(key, value);
                    }
                }
            }
        }
        //if we still have room after adding all sorted non zero values... fill the rest with passed values
        if (i < limit) {
            for (String key : passedValuesMap.keySet()) {
                if (i < limit) {
                    originalSortResultMap.put(key, passedValuesMap.get(key));
                    i++;
                }
            }
        }
        //add combined field if it is not null (indicates it was used even if the value is 0)
        ChartData cVal = sortedLimitedMap.get(limitLabel);
        if (cVal != null && cVal.getCount() != null && !Double.isNaN(cVal.getCount())) {
            originalSortResultMap.put(limitLabel, cVal);
        }
        return originalSortResultMap;
    }


    /**
     * Applies a limit to the map.  All values after limit will be summed and placed in the return map with key {@code
     * limitLabel}. Zero or NaN values do not count towards the limit.
     *
     * @param map The map to limit.  Assumed use case is that the map is already value sorted descending.
     * @return limited map.
     */
    public static LinkedHashMap<String, Double> getLimitedMap(Map<String, Double> map, Integer limit,
                                                              String limitLabel) {
        LinkedHashMap<String, Double> mapValueSort = new LinkedHashMap<String, Double>(limit);
        //add limit by default if the record size is greater than the limit.
        if (limit < map.size()) {
            mapValueSort.put(limitLabel, 0.0);
        }
        //combine any value after limit into a summed bucket
        int i = 0;
        for (String key : map.keySet()) {
            Double value = map.get(key);
            if (i < limit) {
                mapValueSort.put(key, value);
            } else {
                //we've hit the limit, now accumulate
                Double val = mapValueSort.get(limitLabel);
                if (value != null && !Double.isNaN(value)) {
                    mapValueSort.put(limitLabel, val + value);
                }
            }
            //if it is not zero/null count it towards limit
            if (value != null && !Double.isNaN(value) && value > 0) {
                i++;
            }
        }
        return mapValueSort;
    }

    public static LinkedHashMap<String, ChartData> getLimitedChartDataMap(Map<String, ChartData> map, Integer limit,
                                                                          String limitLabel) {
        LinkedHashMap<String, ChartData> mapValueSort = new LinkedHashMap<String, ChartData>(limit);
        //add limit by default if the record size is greater than the limit.
        ChartData lastItem = new ChartData(limitLabel, limitLabel, 0.0);

        if (limit < map.size()) {
            mapValueSort.put(limitLabel, lastItem);
        }
        //combine any value after limit into a summed bucket
        int i = 0;
        for (String key : map.keySet()) {
            ChartData value = map.get(key);
            if (i < limit) {
                mapValueSort.put(key, value);
            } else {
                //we've hit the limit, now accumulate
                Double val = lastItem.getCount();
                if (value != null && value.getCount() != null && !Double.isNaN(value.getCount())) {
                    lastItem.setCount(val + value.getCount());
                }
            }
            //if it is not zero/null count it towards limit
            if (value != null && value.getCount() != null && !Double.isNaN(value.getCount()) && value.getCount() > 0) {
                i++;
            }
        }
//        if(limit < map.size()){
//            mapValueSort.put("", lastItem);
//        }
        return mapValueSort;
    }

    /**
     * Sorts a map by value.
     */
    public static Map<String, Double> getSortedByValueMap(LinkedHashMap<String, Double> unsortedMap) {

        //TODO use this?

	    /*
         *   Collections.sort(dataList, new Comparator<Pair<String, Double>>() {

                        @Override
                        public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
                            return (int) Math.ceil(o2.getValue() - o1.getValue());
                        }
                    });
	     * 
	     */
        ValueComparator dubCompare = new ValueComparator(unsortedMap);
        TreeMap<String, Double> sortedMap = new TreeMap<String, Double>(dubCompare);
        sortedMap.putAll(unsortedMap);
        LinkedHashMap<String, Double> map = new LinkedHashMap<String, Double>(sortedMap);
        return map;
    }

    public static Map<String, ChartData> getSortedByChartDataMap(LinkedHashMap<String, ChartData> unsortedMap) {
        ChartDataComparator chartDataCompare = new ChartDataComparator(unsortedMap);
        TreeMap<String, ChartData> sortedMap = new TreeMap<String, ChartData>(chartDataCompare);
        sortedMap.putAll(unsortedMap);
        LinkedHashMap<String, ChartData> map = new LinkedHashMap<String, ChartData>(sortedMap);
        return map;
    }

    /**
     * Provides sorting for a tree map based on map value.  Equal values will remain in compare order. (possibly insert
     * order?)
     */
    public static class ValueComparator implements Comparator<String> {

        private Map<String, Double> base;

        public ValueComparator(Map<String, Double> base) {
            this.base = new LinkedHashMap<String, Double>(base);
        }

        @Override
        public int compare(String a, String b) {
            Double dubB = base.get(b);
            Double dubA = base.get(a);
            //negate to provide descending order
            int compare = -Double.compare(dubA, dubB);
            //returning 0 for a treemap means the value is overwritten
            //this ensures that if the two values are equal, we preserve their order
            if (compare == 0) {
                compare = 1;
            }
            return compare;
        }
    }

    /**
     * Provides sorting for a tree map based on ChartData map value.  Equal values will remain in compare order.
     * (possibly insert order?)
     */
    public static class ChartDataComparator implements Comparator<String> {

        private Map<String, ChartData> base;

        public ChartDataComparator(Map<String, ChartData> base) {
            this.base = new LinkedHashMap<String, ChartData>(base);
        }

        @Override
        public int compare(String a, String b) {
            ChartData dubB = base.get(b);
            ChartData dubA = base.get(a);
            //negate to provide descending order
            int compare = -Double.compare(dubA.getCount(), dubB.getCount());
            //returning 0 for a treemap means the value is overwritten
            //this ensures that if the two values are equal, we preserve their order
            if (compare == 0) {
                compare = 1;
            }
            return compare;
        }
    }

    /**
     * Checks values for a non zero number.
     *
     * @return true if there are any values > 0
     */
    public static boolean isCollectionValued(Collection<Double> values) {
        if (values != null) {
            for (Double value : values) {
                if (value != null && !Double.isNaN(value) && value > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Parses hex encoded colors into java Color objects.
     *
     * @param color     a default Color if a hex color string fails to parse
     * @param hexColors an array of colors to parse
     * @return array of java.awt.Color objects from hex.
     */
    public static Color[] getColorsFromHex(Color color, String... hexColors) {
        Color[] colors = new Color[hexColors.length];
        for (int i = 0; i < hexColors.length; i++) {
            try {
                colors[i] = Color.decode(hexColors[i]);
            } catch (Exception e) {
                colors[i] = color;
            }
        }
        return colors;
    }

    /**
     * Return a Collection of string ids from dimensions.
     */
    @SuppressWarnings("unchecked")
    public static Collection<String> getDimensionIdsFromCollection(Collection<Dimension> dims) {
        Collection<String> ids = (Collection<String>) CollectionUtils.collect(dims, new Transformer() {
            @Override
            public Object transform(Object input) {
                Dimension dim = (Dimension) input;
                return dim.getId();
            }
        });
        return ids;
    }


    /**
     * Helper method that will return TimeZone as a String from a given request. It reads timezoneOffset parameter from
     * the request and based on offset value, it will create a TimeZone object
     *
     * @param servletRequest Servlet Request having timezoneOffset
     * @return TimeZone as a String in "HH:MM" format. If the request does not have timezoneOffset, it will return
     *         server's TimeZone
     */
    public static String getRequestTimezoneAsHourMinuteString(
            WebRequest servletRequest) {
        String tzString = "04:00"; // EST
        // server timezone offset
        int offset = TimeZone.getDefault().getOffset(
                Calendar.getInstance().getTimeInMillis())
                     / -60000;

        try {
            String timezoneOffset = servletRequest.getParameter("timezoneOffset");
            if (timezoneOffset != null) {
                offset = Integer.parseInt(timezoneOffset);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        int hours = offset / (60);
        int minutes = Math.abs(offset) % 60;
        tzString = String.format("%02d", hours) + ":" + String.format("%02d", minutes);
        return tzString;
    }

    /**
     * Helper method that will return TimeZone object from a given request. It reads timezoneOffset parameter from the
     * request and based on offset value, it will create a TimeZone object
     *
     * @param servletRequest Servlet Request having timezoneOffset
     * @return TimeZone object. If the request does not have timezoneOffset, it will return server's TimeZone
     */
    public static TimeZone getRequestTimezone(WebRequest servletRequest) {
        String timezoneOffset = servletRequest.getParameter("timezoneOffset");
        TimeZone tz = TimeZone.getDefault();
        try {
            if (timezoneOffset != null) {
                // Convert time zone offset from minutes to milliseconds
                // JavaScript time zone offset needs to be multiplied by -1
                // in order to feed it to JODA time zone call
                tz = DateTimeZone.forOffsetMillis(
                        Integer.parseInt(timezoneOffset) * 60 * -1000)
                        .toTimeZone();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tz;
    }
}
