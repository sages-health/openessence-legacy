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

package edu.jhuapl.openessence.datasource.jdbc;

import edu.jhuapl.openessence.datasource.FieldType;
import edu.jhuapl.openessence.datasource.OeDataSourceException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DataTypeConversionHelper {

    public static Object convert2JavaType(ResultSet rs, FieldType t, int col) throws SQLException {
        // NOTE: To return NULL when needed - rs.wasNull() test needs to be performed
        // http://docstore.mik.ua/orelly/java-ent/jenut/ch02_06.htm
        switch (t) {
            case DATE_TIME:
                java.sql.Timestamp dt = rs.getTimestamp(col);
                return (rs.wasNull()) ? null : new java.util.Date(dt.getTime());
            case DATE:
                java.sql.Date d = rs.getDate(col);
                return (rs.wasNull()) ? null : new java.util.Date(d.getTime());
            case FLOAT:
                float floatValue = rs.getFloat(col);
                return (rs.wasNull()) ? null : floatValue;
            case DOUBLE:
                double doubleValue = rs.getDouble(col);
                return (rs.wasNull()) ? null : doubleValue;
            case INTEGER:
                int intValue = rs.getInt(col);
                return (rs.wasNull()) ? null : intValue;
            case LONG:
                long longValue = rs.getLong(col);
                return (rs.wasNull()) ? null : longValue;
            case TEXT:
                String strValue = rs.getString(col);
                return (rs.wasNull()) ? null : strValue;
            case BOOLEAN:
                boolean boolValue = rs.getBoolean(col);
                return (rs.wasNull()) ? null : boolValue;
            default:
                throw new AssertionError("Unexpected field type \"" + t + "\"");
        }
    }

    public static Object convert2JavaType(ResultSet rs, int col) throws SQLException {
        // NOTE: To return NULL when needed - rs.wasNull() test needs to be performed
        // http://docstore.mik.ua/orelly/java-ent/jenut/ch02_06.htm
        ResultSetMetaData rsMetadata = rs.getMetaData();

        if (rsMetadata.getColumnType(col) == Types.INTEGER ||
            rsMetadata.getColumnType(col) == Types.BIGINT) {
            int intValue = rs.getInt(col);
            return (rs.wasNull()) ? null : intValue;
        } else if (rsMetadata.getColumnType(col) == Types.DOUBLE ||
                   rsMetadata.getColumnType(col) == Types.FLOAT ||
                   rsMetadata.getColumnType(col) == Types.REAL ||
                   rsMetadata.getColumnType(col) == Types.DECIMAL ||
                   rsMetadata.getColumnType(col) == Types.NUMERIC) {
            double doubleValue = rs.getDouble(col);
            return (rs.wasNull()) ? null : doubleValue;
        } else if (rsMetadata.getColumnType(col) == Types.VARCHAR ||
                   rsMetadata.getColumnType(col) == Types.NCHAR ||
                   rsMetadata.getColumnType(col) == Types.NVARCHAR) {
            String strValue = rs.getString(col);
            return (rs.wasNull()) ? null : strValue;
        } else if (rsMetadata.getColumnType(col) == Types.BOOLEAN) {
            boolean boolValue = rs.getBoolean(col);
            return (rs.wasNull()) ? null : boolValue;
        } else if (rsMetadata.getColumnType(col) == Types.DATE ||
                   rsMetadata.getColumnType(col) == Types.TIME ||
                   rsMetadata.getColumnType(col) == Types.TIMESTAMP) {
            java.sql.Timestamp sqlDate = rs.getTimestamp(col);
            return (rs.wasNull()) ? null : new java.util.Date(sqlDate.getTime());
        } else {
            // punting to getString() if we can't guess the type from the metadata
            String strValue = rs.getString(col);
            return (rs.wasNull()) ? null : strValue;
        }

    }

    public static Number convert2JavaNumberType(ResultSet rs, int col) throws SQLException, OeDataSourceException {
        // NOTE: To return NULL when needed - rs.wasNull() test needs to be performed
        // http://docstore.mik.ua/orelly/java-ent/jenut/ch02_06.htm
        ResultSetMetaData rsMetadata = rs.getMetaData();

        if (rsMetadata.getColumnType(col) == Types.INTEGER ||
            rsMetadata.getColumnType(col) == Types.BIGINT) {
            int intValue = rs.getInt(col);
            return (rs.wasNull()) ? null : intValue;
        } else if (rsMetadata.getColumnType(col) == Types.DOUBLE ||
                   rsMetadata.getColumnType(col) == Types.FLOAT ||
                   rsMetadata.getColumnType(col) == Types.REAL ||
                   rsMetadata.getColumnType(col) == Types.DECIMAL ||
                   rsMetadata.getColumnType(col) == Types.NUMERIC) {
            double doubleValue = rs.getDouble(col);
            return (rs.wasNull()) ? null : doubleValue;
        } else {
            throw new OeDataSourceException("Not a number type");
        }
    }

    public static Object convert2JavaType(Object o) throws OeDataSourceException {
        if (o == null) {
            return o;
        } else if (o instanceof java.sql.Date) {
            return new java.util.Date(((java.sql.Date) o).getTime());
        } else if (o instanceof Timestamp) {
            return new java.util.Date(((Timestamp) o).getTime());
        } else if (o instanceof java.util.Date || o instanceof String || o instanceof Float ||
                   o instanceof Long || o instanceof Double || o instanceof Integer ||
                   o instanceof Short || o instanceof Boolean) {
            return o;
        } else {
            throw new OeDataSourceException("Unexpected type " + o.getClass().getName() +
                                            " when converting query results.");
        }
    }

    /**
     * Converts an null, Integer, Float, Double, Long, String, java.sql.Date, Timestamp as itself. Converts
     * java.util.Date to a java.sql.Date If you need a java.util.Date as a Timestamp, you must use the
     * convert2SqlTimestampType method.
     *
     * @param o object to convert
     * @return the converted object to be used in a SQL prepared statement
     * @throws OeDataSourceException if any object that is not of the above types inputed
     */
    public static Object convert2SqlType(Object o) throws OeDataSourceException {
        if (o == null) {
            return o;
        } else if (o instanceof Integer || o instanceof Float || o instanceof Double ||
                   o instanceof Long || o instanceof String || o instanceof Boolean ||
                   o instanceof java.sql.Date || o instanceof Timestamp) {
            return o;
        } else if (o instanceof java.util.Date) {
            return new java.sql.Date(((java.util.Date) o).getTime());
        } else {
            throw new OeDataSourceException("Unexpected argument type " + o.getClass().getName() +
                                            " when preparing query");
        }

    }

    /**
     * Converts null and java.util.Date to a java.sql.Timestamp
     *
     * @param o object to convert
     * @return the converted object to be used in a SQL prepared statement
     * @throws OeDataSourceException if any object that is not of the above types inputed
     * @see "http://lavnish.blogspot.com/2007/12/java-calendar-vs-date.html" java.sql.Date stores only date information,
     *      not times. Simply converting a java.util.Date into a java.sql.Date will silently set the time to midnight.
     *
     *      So, to store date/times to be manipulated as java.util.Date objects, don't do this
     *
     *      // BUG: loses time of day preparedStatement.setDate(1, new java.sql.Date(date.getTime()));
     *
     *      do this instead:
     *
     *      preparedStatement.setTimestamp(1, new java.sql.Timestamp(date.getTime())); java.sql.Timestamp is not a date
     */
    public static java.sql.Timestamp convert2SqlTimestampType(Object o)
            throws OeDataSourceException {
        if (o == null) {
            return (java.sql.Timestamp) o;
        }
        if (o instanceof java.sql.Timestamp) {
            return (java.sql.Timestamp) o;
        } else if (o instanceof java.util.Date) {
            return new Timestamp(((java.util.Date) o).getTime());
        } else {
            throw new OeDataSourceException("Unexpected argument type " + o.getClass().getName() +
                                            " when preparing query");
        }
    }

    /**
     * Parses string into a Date object, first attempting as long(millis) then using the SimpleDateFormat: "MM-dd-yyyy"
     *
     * @param value string representation of date/or a long (millis)
     * @return date object parsed from string
     */
    public static java.util.Date dateFromString(final String value) throws Exception {
        java.util.Date d = null;
        try {
            d = new java.util.Date();
            d.setTime(Long.parseLong(value));
        } catch (NumberFormatException nfe) {
            try {
                d = new SimpleDateFormat("MM-dd-yyyy").parse(value);
            } catch (ParseException e) {
                throw new Exception(String.format("Failed to parse '%s' as date", value));
            }
        }
        return d;
    }
}
