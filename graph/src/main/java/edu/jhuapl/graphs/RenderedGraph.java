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

package edu.jhuapl.graphs;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Encapsulates a graph that has been rendered with a particular dimensions and {@link Encoding}.  This object allows
 * access to the rendered {@link #getData() content} of the graph, and can be used to produce {@link
 * #getImageMap(String) image} {@link #writeImageMap(PrintWriter, String) maps}.
 *
 * @see Encoding
 */
public interface RenderedGraph {

    /**
     * Get the rendered byte representation of the graph, either PNG or JPEG encoded bytes.
     *
     * @return the bytes in the graph
     */
    public byte[] getData();

    /**
     * Gets the encoded used when encoding this graph.
     *
     * @return the encoding
     */
    public Encoding getEncoding();

    /**
     * Creates the HTML form of an image map from this graph.  Which points have links is determined by the {@link
     * GraphSource#ITEM_URL} and {@link GraphSource#ITEM_TOOL_TIP} config options on the {@link PointInterface points} used
     * to create graph.
     *
     * @param name the name of the image map
     * @return the html form of an image map
     * @throws GraphException if the image map creation fails
     */
    public String getImageMap(String name) throws GraphException;

    /**
     * Performs the same action as {@link #getImageMap(String)}, but writes the image map to the given {@link
     * PrintWriter}.
     *
     * @param writer the print writer
     * @param name   the name of the image map
     * @throws GraphException if the image map creation fails
     * @throws IOException    if writing the html to the print writer fails
     * @see #getImageMap(String)
     */
    public void writeImageMap(PrintWriter writer, String name) throws GraphException, IOException;

}
