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

import edu.jhuapl.graphs.Encoding;
import edu.jhuapl.graphs.RenderedGraph;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.imagemap.ImageMapUtilities;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * A JFreeChart implementation of {@link RenderedGraph}.  Note, this class is not meant to be used directly by clients,
 * but is meant to be used solely by {@link JFreeChartTimeSeriesGraphSource}.
 *
 * @see RenderedGraph
 * @see JFreeChartTimeSeriesGraphSource
 */
public class JFreeChartRenderedGraph implements RenderedGraph {

    private final ChartRenderingInfo info;
    private final byte[] data;
    private final Encoding encoding;

    /**
     * Creates a new rendered graph.  The info should have already been pre-populated by the JFreeChart code that produces
     * the encoded bytes.
     *
     * @param info     The ChartRenderingInfo
     * @param data     The encoded data
     * @param encoding the encoding used to produce this graph
     */
    public JFreeChartRenderedGraph(ChartRenderingInfo info, byte[] data, Encoding encoding) {
        this.info = info;
        this.data = data;
        this.encoding = encoding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getData() {
        return data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Encoding getEncoding() {
        return encoding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getImageMap(String name) {
        return ImageMapUtilities.getImageMap(name, info);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeImageMap(PrintWriter writer, String name)
            throws IOException {
        ImageMapUtilities.writeImageMap(writer, name, info);
    }
}
