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

package edu.jhuapl.bsp.detector;

/**
 * An Interface Definition for Temporal Detectors.
 */
public interface TemporalDetectorInterface {

    /**
     * Run the detector and set any answer params in the TemporalDetectorData Object
     *
     * @param TemporalDetectorDataInterface object that implements TemporalDetectorDataInterface
     */
    public void runDetector(TemporalDetectorDataInterface tddi);

    /**
     * Return the detector ID
     */
    public String getID();

    /**
     * Return the detector Name
     */
    public String getName();

    // ///// THRESHOLD INFO /////

    /**
     * Return the red level threshold
     */
    public double getRedLevel();

    /**
     * Set the red level threshold
     *
     * @param _redLevel the threshold for a red alert
     */
    public void setRedLevel(double _redLevel);

    /**
     * Return the yellow level threshold
     */
    public double getYellowLevel();

    /**
     * Set the yellow level threshold
     *
     * @param _yellowLevel the threshold for a yellow alert
     */
    public void setYellowLevel(double _yellowLevel);

}
