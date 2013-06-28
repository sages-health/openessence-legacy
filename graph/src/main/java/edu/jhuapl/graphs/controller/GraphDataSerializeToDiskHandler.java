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

package edu.jhuapl.graphs.controller;

import edu.jhuapl.graphs.GraphException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GraphDataSerializeToDiskHandler implements GraphDataHandlerInterface {

    private String dir;

    public GraphDataSerializeToDiskHandler(String dir) {
        this.dir = dir;
    }

    public void putGraphData(GraphDataInterface graphData, String graphDataId) throws GraphException {
        try {
            File serializedFile = new File(dir, graphDataId);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(serializedFile));
            oos.writeObject(graphData);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            throw new GraphException("Could not write serialized graph data [" + graphDataId + "] to disk", e);
        }
    }

    public GraphDataInterface getGraphData(String graphDataId) throws GraphException {
        GraphDataInterface graphData;

        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(dir, graphDataId)));
            graphData = (GraphDataInterface) in.readObject();
            in.close();
        } catch (IOException e) {
            throw new GraphException("Could not read serialized graph data [" + graphDataId + "] from disk", e);
        } catch (ClassNotFoundException e) {
            throw new GraphException("Class [GraphDataInterface] not found", e);
        }

        return graphData;
    }
}
