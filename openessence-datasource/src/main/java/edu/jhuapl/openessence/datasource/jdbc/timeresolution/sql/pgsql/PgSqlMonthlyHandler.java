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

package edu.jhuapl.openessence.datasource.jdbc.timeresolution.sql.pgsql;

import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.jdbc.ResolutionHandler;
import edu.jhuapl.openessence.datasource.timeresolution.ResolutionUnitEnum;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PgSqlMonthlyHandler extends PgSqlTimeHandler implements
                                                          ResolutionHandler {

    @Override
    public Enum getCategory() {
        return ResolutionUnitEnum.MONTHLY;
    }


    @Override
    public Object buildKernel(Object[] resolutionValues)
            throws OeDataSourceException {
        Calendar c = Calendar.getInstance();
        int year = intValue(resolutionValues[0], 0);
        int month = intValue(resolutionValues[1], 1);
        c.set(Calendar.YEAR, year);
        // We have to substract one as month is 1-12 while cal month is 0-11
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    @Override
    public List<String> getResolutionColumns(String dimCol, String timezone)
            throws OeDataSourceException {
        String tzString = (timezone != null && timezone.length() > 0) ?
                          (" AT TIME ZONE '" + timezone + "' ") : "";
        List<String> groupCols = new ArrayList<String>(2);
        // YEAR
        //groupCols.add("YEAR(" + dimCol + ")");
        groupCols.add("date_part('year', " + dimCol + tzString + ")");

        // MONTH
        //groupCols.add("MONTH(" + dimCol + ")");
        groupCols.add("date_part('month', " + dimCol + tzString + ")");
        return groupCols;
    }

}
