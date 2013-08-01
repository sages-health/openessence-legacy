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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Runs the CDC Ears algorithms C1
 */
public class EarsC1 implements TemporalDetectorInterface, TemporalDetector {

    public double redLevel;
    public double yellowLevel;

    public EarsC1() {
        readConfigFile();
    }

    public String getID() {
        return "c1";
    }

    public String getName() {
        return "CDC-C1";
    }

    protected double getDefaultLevel() {
        return 0;
    }

    protected boolean isLevelTestGreaterThan() {
        return true;
    }

    public double getRedLevel() {
        return redLevel;
    }

    public double getYellowLevel() {
        return yellowLevel;
    }

    public void setRedLevel(double _redLevel) {
        redLevel = _redLevel;
    }

    public void setYellowLevel(double _yellowLevel) {
        yellowLevel = _yellowLevel;
    }

    private void readConfigFile() {
        Properties defaultProps = new Properties();
        InputStream in = getClass().getResourceAsStream("/EarsC1.properties");
        try {
            defaultProps.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String red = defaultProps.getProperty("redLevel");
        String yellow = defaultProps.getProperty("yellowLevel");
        setRedLevel(Double.parseDouble(red));
        setYellowLevel(Double.parseDouble(yellow));
        try {
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Run the detector and set any answer params in the TemporalDetectorData Object
     *
     * @param tddi object that implements TemporalDetectorDataInterface
     */
    public void runDetector(TemporalDetectorDataInterface tddi) {
        double[] data = tddi.getRegressor("lagCount");
        if (data == null) {
            System.out.println("EWMA: No Lag Counts - Using Real Counts");
            data = tddi.getCounts();
        }

        double[] levels = Ears.calculateC1(data);
        double[] expecteds = new double[levels.length];
        double[] colors = new double[levels.length];
        for (int i = 0; i < levels.length; i++) {
            // CDC algs have no expecteds
            expecteds[i] = 0;
        }

        DetectorHelper.postDetectionColorCoding(data, levels, colors, getRedLevel(),
                                                getYellowLevel(), getDefaultLevel(),
                                                isLevelTestGreaterThan());
        tddi.setLevels(levels);
        tddi.setExpecteds(expecteds);
        tddi.setColors(colors);
    }

    /**
     * Run the detector and return an array containing the Level, Expected, and Color information for the days given in
     * the data object.
     *
     * @param data      counts from the start date to the end date in order.
     * @param startDate the first date in the data array.
     * @return Array containing the Level(0), Expected(1), and Color(2) values per day.
     */
    public double[][] runDetector(double[] data, java.util.Date startDate) {
        TemporalDetectorSimpleDataObject tddo = new TemporalDetectorSimpleDataObject();
        tddo.setCounts(data);
        tddo.setStartDate(startDate);
        this.runDetector(tddo);
        double[][] ans = {tddo.getLevels(), tddo.getExpecteds(), tddo.getColors()};
        return ans;
    }
}
