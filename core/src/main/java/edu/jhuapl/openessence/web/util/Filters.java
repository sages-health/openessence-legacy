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

import edu.jhuapl.openessence.datasource.FieldType;
import edu.jhuapl.openessence.datasource.Filter;
import edu.jhuapl.openessence.datasource.FilterDimension;
import edu.jhuapl.openessence.datasource.OeDataSource;
import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.Relation;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Filters {

    private static final Map<String, Relation> parmSfx = new HashMap<String, Relation>();

    static {
        parmSfx.put("", Relation.EQ);
        parmSfx.put("_start", Relation.GTEQ);
        parmSfx.put("_end", Relation.LTEQ);
    }

    /**
     * Parameters used by OE that should not be used as filters
     */
    private static final Set<String> reservedParameters = new HashSet<String>();

    static {
        reservedParameters.add("timeseriesDenominator");
        reservedParameters.add("timeseriesDetectorClass");
        reservedParameters.add("accumId");
    }

    public List<Filter> getFilters(final Map<String, String[]> parameterValues,
                                   final OeDataSource ds,
                                   final String prePullArg,
                                   final int prePullInc, final String resolution,
                                   int calWeekStartDay) throws ErrorMessageException {
        return getFilters(parameterValues, ds, prePullArg, prePullInc, resolution, calWeekStartDay, true);
    }

    public List<Filter> getFilters(final Map<String, String[]> parameterValues,
                                   final OeDataSource ds,
                                   final String prePullArg,
                                   final int prePullInc, final String resolution,
                                   int calWeekStartDay, final boolean adjustDate) throws ErrorMessageException {

        final List<Filter> filters = new ArrayList<Filter>();
        for (final FilterDimension fd : ds.getFilterDimensions()) {
            final FieldType type = fd.getSqlType();

            for (final String sfx : parmSfx.keySet()) {
                final String key = fd.getId() + sfx;

                if (!reservedParameters.contains(key)) {
                    final String[] values = parameterValues.get(key);

                    // No values for a filter dimension is ok
                    if (!ArrayUtils.isEmpty(values)) {
                        if (values.length == 1) {
                            Object argument = ControllerUtils.formatData(key, values[0], type, false).get(key);

                            final Relation rel = type == FieldType.TEXT ? Relation.LIKE : parmSfx.get(sfx);

                            try {
                                if (fd.getId().equals(prePullArg) && (rel == Relation.GTEQ)) {
                                    argument = muckDate((Date) argument, prePullInc, resolution, calWeekStartDay);
                                }
                                // if end date, account for hours 00:00:00 to 23:59:59
                                if ((type == FieldType.DATE_TIME || type == FieldType.DATE)
                                    && (rel == Relation.LTEQ)) {
                                    GregorianCalendar x = new GregorianCalendar();
                                    x.setTime((Date) argument);

                                    // Only perform this operation if it hasn't already been handled in a previous call.
                                    // When generating a Chart, it will go to the end of the day and adjust the filter
                                    // to be 23:59:59.999 (5/1/13 becomes 5/1/13-23:59:59.999).  But, when the user
                                    // would click-through on a bar/slice, it would accidently perform this action again.
                                    // This resulted in adding another day to the query; now becoming 5/2/13 23:59:59.998.
                                    if (adjustDate) {
                                        x.add(Calendar.DATE, 1);
                                        x.add(Calendar.MILLISECOND, -1);
                                        argument = x.getTime();
                                    }
                                }
                                filters.add(fd.makeFilter(rel, argument));
                            } catch (OeDataSourceException e) {
                                throw new ErrorMessageException("Could not create filter", e);
                            }
                        } else {
                            final Object[] arguments = new Object[values.length];
                            for (int i = 0; i < values.length; i++) {
                                arguments[i] = ControllerUtils.formatData(key, values[i], type, false).get(key);
                            }

                            try {
                                filters.add(fd.makeFilter(Relation.IN, arguments));
                            } catch (OeDataSourceException e) {
                                throw new ErrorMessageException("Could not create filter", e);
                            }
                        }
                    }
                }
            }
        }

        return filters;
    }

    private Date muckDate(Date date, int prepull, String resolution, int calWeekStartDay) {
        GregorianCalendar x = new GregorianCalendar();
        x.setTime(date);
        if (resolution != null && resolution.equals("weekly")) {
            x.add(Calendar.DATE, (-7 * prepull));
            // make sure week starts on week start day defined
            // in message.properties or datasource groovy file
            while (x.get(Calendar.DAY_OF_WEEK) != calWeekStartDay) {
                x.add(Calendar.DATE, -1);
            }
        } else if (resolution != null && resolution.equals("monthly")) {
            x.set(Calendar.DAY_OF_MONTH, 1);
        } else if (resolution != null && resolution.equals("daily")) {
            x.add(Calendar.DATE, -prepull);
        }
        return x.getTime();
    }
}
