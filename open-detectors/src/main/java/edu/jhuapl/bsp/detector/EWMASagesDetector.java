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

import org.apache.commons.math3.distribution.TDistribution;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import static edu.jhuapl.bsp.detector.OpenMath.any;
import static edu.jhuapl.bsp.detector.OpenMath.arrayAdd;
import static edu.jhuapl.bsp.detector.OpenMath.dataInd;
import static edu.jhuapl.bsp.detector.OpenMath.mean;
import static edu.jhuapl.bsp.detector.OpenMath.std;

/**
 * Runs the main EWMA algorithm
 */
public class EWMASagesDetector implements TemporalDetectorInterface, TemporalDetector {

    static final double OMEGA = 0.4; // the EWMA smoothing coefficient (between 0 and 1) default 0.4
    static final int MIN_DEG_FREEDOM = 2; // the minimum number of degrees of freedom
    static final int MAX_BASELINE_LEN = 28; // the maximum length of the baseline period

    /**
     * The one-sided threshold p-value for rejecting the null hypothesis, corresponding to red alerts
     */
    static final double THRESHOLD_PROBABILITY_RED_ALERT = 0.01;

    /**
     * The one-sided threshold p-value for rejecting the null hypothesis, corresponding to yellow alerts
     */
    static final double THRESHOLD_PROBABILITY_YELLOW_ALERT = 0.05;

    /**
     * the length of the guard band period
     */
    static final int NUM_GUARDBAND = 2;

    /**
     * if true unusually long strings of zeros in the baseline period are removed prior to applying process control
     */
    static final boolean REMOVE_ZEROES = true;

    //
    static final double MIN_PROB_LEVEL = 1E-6;
    static final int NUM_FIT_PARAMS = 1;
    //
    private double data[];
    private double threshPValueR, threshPValueY;
    private double UCL_R[], UCL_Y[], sigmaCoeff[], deltaSigma[], minSigma[];
    private int maxBaseline, numGuardBand, minBaseline, degFreedomRange;
    private boolean removeZeros;
    private double levels[], pvalues[], expectedData[], colors[], r2Levels[], switchFlags[], test_stat[];

    //
    public EWMASagesDetector() {
        maxBaseline = MAX_BASELINE_LEN;
        threshPValueR = THRESHOLD_PROBABILITY_RED_ALERT;
        threshPValueY = THRESHOLD_PROBABILITY_YELLOW_ALERT;
        numGuardBand = NUM_GUARDBAND;
        removeZeros = REMOVE_ZEROES;
        minBaseline = NUM_FIT_PARAMS + MIN_DEG_FREEDOM;
        degFreedomRange = maxBaseline - NUM_FIT_PARAMS;
        readConfigFile();
    }

    @Override
    public String getID() {
        return "probewmazerofilter";
    }

    @Override
    public String getName() {
        return "EWMASages";
    }

    @Override
    public double getRedLevel() {
        return threshPValueR;
    }

    @Override
    public void setRedLevel(double _redLevel) {
        threshPValueR = _redLevel;
    }

    @Override
    public double getYellowLevel() {
        return threshPValueY;
    }

    @Override
    public void setYellowLevel(double _yellowLevel) {
        threshPValueY = _yellowLevel;
    }

    //
    private void calculate(double data[], double omega) {
        setData(data);
        calculate(OMEGA);
    }

    private void calculate(double omega) {
        UCL_R = new double[degFreedomRange];
        UCL_Y = new double[degFreedomRange];
        sigmaCoeff = new double[degFreedomRange];
        deltaSigma = new double[degFreedomRange];
        minSigma = new double[degFreedomRange];
        int[] degFreedom = new int[data.length];
//
        double term1 = omega / (2.0 - omega), term2, term3;
        for (int i = 0; i < degFreedomRange; i++) {
            TDistribution tdist = new TDistribution(i + 1);
            UCL_R[i] = tdist.inverseCumulativeProbability(1 - threshPValueR);
            UCL_Y[i] = tdist.inverseCumulativeProbability(1 - threshPValueY);
            int numBaseline = NUM_FIT_PARAMS + i;
            term2 = 1.0 / numBaseline;
            term3 = -2.0 * Math.pow((1 - omega), (numGuardBand + 1.0)) *
                    (1.0 - Math.pow((1 - omega), numBaseline)) / numBaseline;
            sigmaCoeff[i] = Math.sqrt(term1 + term2 + term3);
            deltaSigma[i] = (omega / UCL_Y[i]) *
                            (0.1289 - (0.2414 - 0.1826 * Math.pow((1 - omega), 4)) *
                                      Math.log(10.0 * threshPValueY));
            minSigma[i] = (omega / UCL_Y[i]) * (1.0 + 0.5 * Math.pow((1 - omega), 2));
        }
//
        levels = new double[data.length];
        Arrays.fill(levels, 0.5);
        pvalues = new double[data.length];
        Arrays.fill(pvalues, 0.5);
        expectedData = new double[data.length];
        Arrays.fill(expectedData, 0);
        colors = new double[data.length];
        r2Levels = new double[data.length];
        Arrays.fill(r2Levels, 0);
        switchFlags = new double[data.length];
        Arrays.fill(switchFlags, 0);
        test_stat = new double[data.length];
        Arrays.fill(test_stat, 0);
//
        FilterBaselineZeros3 zf = new FilterBaselineZeros3();
//
        double smoothedData, sigma, testBase[], baselineData[];
        ArrayList<Integer> ndxBaseline = new ArrayList<Integer>();
        // initialize the smoothed data
        smoothedData = 0;
        for (int i = 1; i < minBaseline + numGuardBand && i < data.length; i++) {
            smoothedData = omega * data[i] + (1 - omega) * smoothedData;
        }
        // initialize the indices of the baseline period
        for (int i = 0; i < minBaseline - 1; i++) {
            ndxBaseline.add(new Integer(i));
        }
        // loop through the days on which to make predictions
        for (int i = minBaseline + numGuardBand; i < data.length; i++) {
            // smooth the data using an exponentially weighted moving average (EWMA)
            smoothedData = omega * data[i] + (1 - omega) * smoothedData;
            // lengthen and advance the baseline period
            if (ndxBaseline.isEmpty() || ndxBaseline.get(ndxBaseline.size() - 1) + 1 < maxBaseline) {
                ndxBaseline.add(0, -1);
            }
            // advance the indices of the baseline period
            arrayAdd(ndxBaseline, 1);
            // remove excess consecutive zeros from the baseline data
            testBase = dataInd(data, ndxBaseline);
            if (removeZeros && FilterBaselineZeros3.filterBaselineZerosTest(testBase)) {
                int[] ndxOK = zf.filterBaselineZeros(testBase);
                baselineData = dataInd(testBase, ndxOK);
            } else {
                baselineData = testBase;
            }
            // check the baseline period is filled with zeros; no prediction can be
            if (!any(baselineData)) {
                continue;
            }
            // the number of degrees of freedom
            degFreedom[i] = baselineData.length - NUM_FIT_PARAMS;
            // there are not enough data points in the baseline period; no prediction can be made
            if (degFreedom[i] < MIN_DEG_FREEDOM) {
                continue;
            }
            // the predicted current value of the data
            expectedData[i] = mean(baselineData);
            // calculate the test statistic
            // the adjusted standard deviation of the baseline data
            sigma = sigmaCoeff[degFreedom[i] - 1] * std(baselineData) + deltaSigma[degFreedom[i] - 1];
            // don't allow values smaller than MinSigma
            sigma = Math.max(sigma, minSigma[degFreedom[i] - 1]);
            // the test statistic
            test_stat[i] = (smoothedData - expectedData[i]) / sigma;
            if (Math.abs(test_stat[i]) > UCL_R[degFreedom[i] - 1]) {
                // the current value of the smoothed data is too extreme; adjust this value for the next iteration
                smoothedData = expectedData[i] + Math.signum(test_stat[i]) * UCL_R[degFreedom[i] - 1] * sigma;
            }
        }
        for (int i = 0; i < data.length; i++) {
            if (Math.abs(test_stat[i]) > 0.0) {
                TDistribution tdist = new TDistribution(degFreedom[i]);
                pvalues[i] = 1 - tdist.cumulativeProbability(test_stat[i]);
                levels[i] = pvalues[i];
            }
        }
    }

    private void readConfigFile() {
        Properties defaultProps = new Properties();
        InputStream in = getClass().getResourceAsStream("/EWMASagesDetector.properties");
        try {
            defaultProps.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String red = defaultProps.getProperty("THRESHOLD_PROBABILITY_RED_ALERT");
        String yellow = defaultProps.getProperty("THRESHOLD_PROBABILITY_YELLOW_ALERT");
        setRedLevel(Double.parseDouble(red));
        setYellowLevel(Double.parseDouble(yellow));
        try {
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void testDetector(TemporalDetectorDataInterface tddi) {
        double[] data = tddi.getCounts();
        calculate(data, OMEGA);
        tddi.setLevels(getLevels());
    }

    @Override
    public void runDetector(TemporalDetectorDataInterface tddi) {
        double[] data = tddi.getCounts();
//
        calculate(data, OMEGA);
        DetectorHelper.postDetectionColorCoding(data, levels, colors, getRedLevel(), getYellowLevel(), 0.5, false);
//
        tddi.setLevels(getLevels());
        tddi.setExpecteds(getExpecteds());
        tddi.setColors(getColors());
        tddi.setR2Levels(getR2Levels());
        tddi.setSwitchFlags(getSwitchFlags());
        tddi.setTestStatistics(getTestStats());
    }

    @Override
    public double[][] runDetector(double[] data, java.util.Date startDate) {
        TemporalDetectorSimpleDataObject tddo = new TemporalDetectorSimpleDataObject();
        tddo.setCounts(data);
        tddo.setStartDate(startDate);
        this.runDetector(tddo);
        double[][] ans = {tddo.getLevels(), tddo.getExpecteds(), tddo.getColors(),
                          tddo.getSwitchFlags(), tddo.getR2Levels()};
        return ans;
    }

    /**
     * @param b
     */
    public void setRemoveZeros(boolean b) {
        removeZeros = b;
    }

    /**
     * @param ds
     */
    public void setData(double[] ds) {
        data = ds;
    }

    /**
     * @return array of levels
     */
    public double[] getLevels() {
        return levels;
    }

    /**
     * @return array of expecteds
     */
    public double[] getExpecteds() {
        return expectedData;
    }

    /**
     * @return array of colors
     */
    public double[] getColors() {
        return colors;
    }

    /**
     * @return array of R2 levels
     */
    public double[] getR2Levels() {
        return r2Levels;
    }

    /**
     * @return array of switch flags
     */
    public double[] getSwitchFlags() {
        return switchFlags;
    }

    /**
     * @return array of test statistics
     */
    public double[] getTestStats() {
        return test_stat;
    }
}
