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

package edu.jhuapl.graphs.jfreechart;

import edu.jhuapl.graphs.GraphException;
import edu.jhuapl.graphs.TimeResolution;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * A factory to produce the {@link DateFormat} used by {@link JFreeChartTimeSeriesGraphSource} to render the domain
 * label, if no {@link DateFormat} is specified in the {@link edu.jhuapl.graphs.GraphSource#GRAPH_DATE_FORMATTER}
 * parameter of the graph metadata.
 */
public class DefaultDateFormatFactory {

    /**
     * Creates a {@link DateFormat} based on the {@link TimeResolution}.
     *
     * @param resolution the resolution
     * @return A DateFormat appropriate for that resolution
     * @throws GraphException if the operation fails
     */
    public DateFormat getFormat(TimeResolution resolution) throws GraphException {
        switch (resolution) {
            case HOURLY:
                return new SimpleDateFormat("dd:hh");
            case DAILY:
                return new SimpleDateFormat("MM-dd");
            case WEEKLY:
                return new SimpleDateFormat("MM-dd");
            case MONTHLY:
                return new SimpleDateFormat("yy-MM");
            case YEARLY:
                return new SimpleDateFormat("yyyy");
            default:
                throw new GraphException("Unrecognized resolution \"" + resolution.toString() + "\"");
        }
    }
}
