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

package edu.jhuapl.bsp.detector.temporal.epa;

import edu.jhuapl.bsp.detector.TemporalDetector;
import edu.jhuapl.bsp.detector.TemporalDetectorDataInterface;
import edu.jhuapl.bsp.detector.TemporalDetectorInterface;
import edu.jhuapl.bsp.detector.TemporalDetectorSimpleDataObject;

import java.util.Date;

//import edu.jhuapl.bsp.detector.TemporalDetectorDataObject;

/**
 * This class doesn't switch. TODO Rename NoOpDetector and move to DetectorInterface
 */
public class NoDetectorDetector implements TemporalDetectorInterface, TemporalDetector {

    private static String ALGORITHM_NAME = "nodetectordetector";

    private double redLevel = 0.0;

    private double yellowLevel = 0.0;

    public NoDetectorDetector() {
        setRedLevel(0.01);
        setYellowLevel(0.05);
    }

    public String getID() {
        return ALGORITHM_NAME;
    }

    public String getName() {
        return ALGORITHM_NAME;
    }

    public double getRedLevel() {
        return redLevel;
    }

    public double getYellowLevel() {
        return yellowLevel;
    }

    public void runDetector(TemporalDetectorDataInterface data) {
        // Inputs
        double[] counts = data.getCounts();

        // outputs --
        double[] colors = new double[counts.length];
        double[] levels = new double[counts.length];
        double[] expected = new double[counts.length];
        double[] switchFlags = new double[counts.length];

        for (int currentLook = 0; currentLook < counts.length; currentLook++) {
            expected[currentLook] = counts[currentLook];
            switchFlags[currentLook] = 1.0;
            levels[currentLook] = 1.0;
            colors[currentLook] = 0;
        }

        // Algorithm is done...
        data.setLevels(levels);
        data.setExpecteds(expected);
        data.setSwitchFlags(switchFlags);
        data.setColors(colors);

    }

    public void setRedLevel(double level) {
        this.redLevel = level;
    }

    public void setYellowLevel(double level) {
        this.yellowLevel = level;
    }

    public double[][] runDetector(double[] data, Date startDate) {
        TemporalDetectorSimpleDataObject tddo = new TemporalDetectorSimpleDataObject();
        tddo.setCounts(data);
        tddo.setStartDate(startDate);
        runDetector(tddo);
        double[][] ans = {tddo.getLevels(), tddo.getExpecteds(), tddo.getColors(),
                          tddo.getSwitchFlags(), tddo.getR2Levels()};
        return ans;
    }

}
