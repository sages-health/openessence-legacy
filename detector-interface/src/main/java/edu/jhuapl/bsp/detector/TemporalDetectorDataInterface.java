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

import java.util.Date;
import java.util.Set;

/**
 * An Interface Definition for Temporal Detector Data.
 */
public interface TemporalDetectorDataInterface {

    /**
     * The number of counts
     */
    public int size();

    /**
     * Delete the first [prepullDays] elements from the internal arrays
     */
    public void cropStartup(int prepullDays);

    // ///////// INPUT ELEMENTS //////////////

    /**
     * Set input data to the object.
     *
     * @param counts input data to run detection on
     */
    public void setCounts(double[] _counts);

    /**
     * The counts
     */
    public double[] getCounts();

    /**
     * Set start date to the object
     *
     * @param startDate first date of the input data
     */
    public void setStartDate(java.util.Date _startDate);

    /**
     * The first date of the counts
     */
    public java.util.Date getStartDate();

    /**
     * Set the time resolution to be used. This is used by the getDates method if setDates is not used.
     */
    public void setTimeResolution(String timeResolution);

    /**
     * Set input regressor data to the object.
     *
     * @param _regressor input regressor data for detectors to use
     */
    public void setRegressor(String regressorID, double[] _regressor);

    /**
     * A Regressor
     */
    public double[] getRegressor(String regressorID);

    /**
     * Get the list of regressorID's currently set
     */
    public String[] getRegressorIDs();

    // ///////// OUTPUT ELEMENTS //////////////

    /**
     * The array of Dates
     */
    public java.util.Date[] getDates();

    /**
     * The array of Dates
     */
    public void setDates(Date[] dates);

    /**
     * The Alt Texts
     */
    public String[] getAltTexts();

    /**
     * Set output levels to the object
     *
     * @param levels output levels
     */
    public void setLevels(double[] _levels);

    /**
     * The output levels
     */
    public double[] getLevels();

    /**
     * Set output expecteds to the object
     *
     * @param expecteds output expecteds
     */
    public void setExpecteds(double[] _expecteds);

    /**
     * The output expecteds
     */
    public double[] getExpecteds();

    /**
     * Set output colors to the object
     *
     * @param colors output colors
     */
    public void setColors(double[] _colors);

    /**
     * The output colors
     */
    public double[] getColors();

    /**
     * Set output r2Levels to the object
     *
     * @param r2Levels output r2Levels
     */
    public void setR2Levels(double[] _r2Levels);

    /**
     * The output r2Levels
     */
    public double[] getR2Levels();

    /**
     * Set output switchFlags to the object
     *
     * @param switchFlags output switchFlags
     */
    public void setSwitchFlags(double[] _switchFlags);

    /**
     * The output switchFlags
     */
    public double[] getSwitchFlags();

    /**
     * Set output switch info to the object
     *
     * @param switchInfo output switch info
     */
    public void setSwitchInfo(String[] _switchInfo);

    /**
     * The output switch info
     */
    public String[] getSwitchInfo();

    public int getCountMaximumFractionDigits();

    public void setCountMaximumFractionDigits(int x);

    public void setExpectedMaximumFractionDigits(int x);

    public void setLevelMaximumFractionDigits(int x);

    public void setOutputValues(String key, double[] vals);

    public double[] getOutputValues(String key);

    public Set<String> getOutputValueKeys();

    public void setTestStatistics(double[] testStatistics);

    public double[] getTestStatistics();
}
