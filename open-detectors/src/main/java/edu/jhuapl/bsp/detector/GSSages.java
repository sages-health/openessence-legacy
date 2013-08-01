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

import edu.jhuapl.bsp.detector.exception.DetectorException;

import org.apache.commons.math3.distribution.TDistribution;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import static edu.jhuapl.bsp.detector.OpenMath.any;
import static edu.jhuapl.bsp.detector.OpenMath.arrayAbs;
import static edu.jhuapl.bsp.detector.OpenMath.arrayAdd;
import static edu.jhuapl.bsp.detector.OpenMath.arrayAdd2;
import static edu.jhuapl.bsp.detector.OpenMath.arrayMod;
import static edu.jhuapl.bsp.detector.OpenMath.copya;
import static edu.jhuapl.bsp.detector.OpenMath.dataInd;
import static edu.jhuapl.bsp.detector.OpenMath.dataInd_js;
import static edu.jhuapl.bsp.detector.OpenMath.dataVec;
import static edu.jhuapl.bsp.detector.OpenMath.find;
import static edu.jhuapl.bsp.detector.OpenMath.findLT;
import static edu.jhuapl.bsp.detector.OpenMath.ismember;
import static edu.jhuapl.bsp.detector.OpenMath.mean;
import static edu.jhuapl.bsp.detector.OpenMath.median;
import static edu.jhuapl.bsp.detector.OpenMath.normcdf;
import static edu.jhuapl.bsp.detector.OpenMath.numel;
import static edu.jhuapl.bsp.detector.OpenMath.ones;
import static edu.jhuapl.bsp.detector.OpenMath.reshape;
import static edu.jhuapl.bsp.detector.OpenMath.std;
import static edu.jhuapl.bsp.detector.OpenMath.transpose;
import static java.lang.Math.abs;
import static java.lang.Math.log;
import static java.lang.Math.max;
import static java.lang.Math.pow;

/**
 * Runs the Generalized Adaptive Smoothing algorithm
 */
public class GSSages implements TemporalDetectorInterface, TemporalDetector {

    private Date startDate;
    private boolean bAutoCoef;
    private int Baseline;
    private double alpha[] = new double[3], Adj, HOLfac, multFac;
    //
    static final int BASELINE = 56; // Multiple of 7
    static final int GUARDBAND = 0;
    static final double ADJ = 0;
    static final double HOLFAC = 0.2; // If 1 no parameter will be adjusted
    static final double SMOOTHVEC0 = 0.3;
    static final double SMOOTHVEC1 = 0.0;
    static final double SMOOTHVEC2 = 0.05;
    static final double APE_LIMIT = 0.5; // This value determines if we update the parameters or not
    static final double THRESHOLD_PROBABILITY_RED_ALERT = 0.01;
    static final double THRESHOLD_PROBABILITY_YELLOW_ALERT = 0.05;
    //
    private double data[];
    private double threshPValueR, threshPValueY;
    private double levels[], pvalues[], expectedData[], colors[], r2Levels[], switchFlags[], test_stat[];

    //
    public GSSages() {
        init();
    }

    private void init() {
        threshPValueR = THRESHOLD_PROBABILITY_RED_ALERT;
        threshPValueY = THRESHOLD_PROBABILITY_YELLOW_ALERT;
        bAutoCoef = true;
        Baseline = BASELINE;
        alpha[0] = SMOOTHVEC0;
        alpha[1] = SMOOTHVEC1;
        alpha[2] = SMOOTHVEC2;
        Adj = ADJ;
        HOLfac = HOLFAC; // this is the input for the holiday adjustment
        readConfigFile();
    }

    @Override
    public String getID() {
        return "gs-sages";
    }

    @Override
    public String getName() {
        return "GeneralizedadaptiveSmoothing";
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
    private void calculate(double data[]) {
        if (data.length <= BASELINE) {
            String message = getName() + " detector needs at least " + BASELINE + " data points to run detection.";
            throw new DetectorException(message);
        }
        init();
        setData(data);
        calculate();
    }

    private void calculate() {
        FilterBaselineZeros3 zf = new FilterBaselineZeros3();
        boolean bSparseFlag;
        int i;
        double ck, c0[], ytemp[] = new double[0];
        double UCL_R[], UCL_Y[], sigmaCoeff[], deltaSigma[], Sigma = 0, minSigma[];
        UCL_R = new double[Baseline];
        UCL_Y = new double[Baseline];
        int degFreedom;
        minSigma = new double[Baseline];
//
        levels = ones(data.length, 0.5);
        pvalues = ones(data.length, -9999);
        expectedData = ones(data.length, 0);
        colors = new double[data.length];
        r2Levels = ones(data.length, 0);
        switchFlags = ones(data.length, 0);
        test_stat = ones(data.length, -9999);
//
        double datak[] = copya(data);
//    double datakr[][] = reshape (datak, Baseline/7, 7);
//    c0 = mean(datakr); ck = mean(mean(datakr));
        double datakr[][] = reshape(datak, 7, Baseline / 7);
        c0 = mean(transpose(datakr));
        ck = mean(mean(transpose(datakr)));
        for (int n0 = 0; n0 < c0.length; n0++) {
            c0[n0] = c0[n0] / ck;
        } // starting seasonality coefficients
//for (int k=0; k<c0.length; k++) { System.out.println (ck+"/"+c0[k]+" "); } System.out.println(); System.out.println();
        double m0 = mean(mean(transpose(datakr))); // starting level (mean)
//
        double m[] = ones(data.length, m0);
        double b[] = ones(data.length, 0); // initialize trend at 0
// Depending on the mean of the baseline choose smoothing coefficients
// alpha is a vector with 3 coefficients - alpha(1) - level alpha(2)- trend
// alpha(3) is seasonality (corresponds to alpha beta and gamma in the paper
        if (median(reshape(datakr)) == 0) {
            alpha[0] = 0.4;
            alpha[1] = 0;
            alpha[2] = 0;
            c0 = ones(7, 1);
            Adj = 0.2;
            bSparseFlag = true;
        } else {
            if (bAutoCoef == true) {
                if (m0 < 1) {
                    alpha[0] = 0.05;
                    alpha[1] = 0;
                    alpha[2] = 0.1;
                } else if (m0 < 10) {
                    alpha[0] = 0.1;
                    alpha[1] = 0;
                    alpha[2] = 0.05;
                } else if (m0 < 100) {
                    alpha[0] = 0.15;
                    alpha[1] = 0;
                    alpha[2] = 0.05;
                } else /*(m0 >= 100)*/ {
                    alpha[0] = 0.4;
                    alpha[1] = 0;
                    alpha[2] = 0.05;
                }
            }
            bSparseFlag = false;
        }
        int HOL[] = CheckHoliday.isHoliday(startDate, data.length); // Holiday function
        // Format of the parameterList:
        // HOL - vector of holidays
        final int season = 7; // Seasonality
        double y[] = copya(datak);
//
        final int b0 = 0; // No trend
        // Initialize values
        m[season - 1] = m0;
        b[season - 1] = b0;
        b[2 * season - 1] = b0;
        double c[] = ones(data.length, 1);
        arrayAdd(y, Adj); // add adjustment to the input series
        for (i = 0; i < 7; i++) {
            c[i] = max(c0[i], 0.01);
        } // make sure seasonality coefficients are not 0s
        for (i = 7; i < 14; i++) {
            c[i] = c[i - 7];
        } // initialize seasonality coefficient vector
//for (int k=0; k<14; k++) { System.out.println (c[k]+" "); } System.out.println(); System.out.println();
        m[2 * season - 1] = m0; // initialize vector of levels
        double denom[] = ones(y.length, 0);
//
        double y_Pred[] = ones(y.length, 0); // initialize predictions
        int ndxBaseline[] = new int[14];
        for (i = 0; i < 14; i++) {
            ndxBaseline[i] = i + 1;
        } // starting baseline
//
        for (i = 2 * season + GUARDBAND; i < y.length; i++) { // beginning at day 15 + Guardband
            // use the indices of the entire baseline period
            // checking that there are at least 7 non-zero values "together"
            int ndxOK[] = zf.filterBaselineZeros(dataInd_js(datak, ndxBaseline));
            int ndxBaselineOK[] = dataInd(ndxBaseline, ndxOK);
            if (numel(ndxBaselineOK) >= 7) {
                if (HOL[i] == 1 &&
                    !((y[i] < (c[i - 6] * m[i - 6] + denom[i - 1]) && y[i] > (c[i - 6] * m[i - 6] - denom[i - 1]))
                      || HOLfac == 1.0)) {
                    // if holiday - check if the values within reasonable limits (+/- 1 standard deviation from the mean)
                    multFac = HOLfac; // potentially change to use seasonality parameter from the last weekend
                } else {
                    multFac = c[i - season];
                }
                // If mean of the recent "good" data all of a sudden is greater
                // than 5 - start updating seasonal coefficients;
//System.out.println (datak.length+" "+ndxBaselineOK.length+" "+datakr.length+" "+datakr[0].length+" "+ndxBaseline[ndxBaseline.length-1]);
                if ((mean(dataInd_js(datak, ndxBaselineOK)) >= 5) && (median(reshape(datakr)) == 0)) {
//System.out.println ("1");
                    alpha[2] = 0.05;
                    bSparseFlag = false;
                    if ((numel(ndxBaselineOK) >= 14) &&
                        (ndxBaselineOK[ndxBaselineOK.length - 1] - ndxBaselineOK[ndxBaselineOK.length - 1 - 13]
                         == 13)) {
                        datakr = reshape(
                                dataInd_js(y, dataVec(ndxBaselineOK, ndxBaselineOK.length - 1 - 13,
                                                      ndxBaselineOK.length - 1)),
                                7, 2);
                        c0 = mean(transpose(datakr));
                        ck = mean(mean(transpose(datakr)));
                        for (int k = i - season, n0 = 0; k < i; k++, n0++) {
                            c[k] = c0[n0] / ck;
                        }
                    }
                }
                // Updating of parameters
                m[i] = alpha[0] * y[i] / c[i - season] + (1 - alpha[0]) * (m[i - 1] + b[i - 1]);
                // Level for counts cannot become negative
                m[i] = max(m[i], 0.5);
                b[i] = alpha[1] * (m[i] - m[i - 1]) + (1 - alpha[1]) * b[i - 1];
                y_Pred[i] = max(multFac * (m[i] + GUARDBAND * b[i]), 0); // prediction
                if (m[i - 1] == 0) {
                    c[i] = alpha[2] * y[i] + (1 - alpha[2]) * c[i - season];
                } else {
                    c[i] = alpha[2] * y[i] / m[i - 1] + (1 - alpha[2]) * c[i - season];
                }
            } else { // In case baseline is not complete keep the old parameters
                m[i] = m[i - 1];
                b[i] = b[i - 1];
                c[i] = c[i - season];
            }
//
            int memList2[] = findLT(arrayAbs(arrayAdd2(dataVec(c, i - 6, i), -c[i])), 0.1);
            int memList3[] = arrayMod(arrayAdd2(memList2, i + 2), 7);
            int memList[] = ismember(arrayMod(ndxBaselineOK, 7), memList3);
            int HOLlist[] = dataInd_js(HOL, ndxBaselineOK);
            for (int k = 0; k < ndxBaselineOK.length; k++) {
                if (HOLlist[k] == 1) {
                    memList[k] = 0;
                }
            }
            memList = find(memList);
            if (numel(dataInd_js(y, dataInd(ndxBaseline, memList))) <= 4) {
//System.out.println ("2");
                denom[i] = std(dataInd_js(y, ndxBaseline));
            } else {
//System.out.println ("3");
                denom[i] = std(dataInd_js(y, dataInd(ndxBaselineOK, memList)));
            }
            // For EWMA switch
            // the term due to the smoothed data
            if (bSparseFlag) {
                for (int k = 0; k < Baseline; k++) {
                    TDistribution tdist = new TDistribution(k + 1);
                    UCL_R[k] = tdist.inverseCumulativeProbability(1 - 0.01);
                    UCL_Y[k] = tdist.inverseCumulativeProbability(1 - 0.05);
                }
                degFreedom = numel(ndxBaselineOK) - 1;
                double Term1 = alpha[0] / (2.0 - alpha[0]);
                // the term due to the baseline mean
                double Term2[] = new double[Baseline];
                for (int k = 0; k < Term2.length; k++) {
                    Term2[k] = 1.0 / (2.0 + k);
                }
                // the term due to twice their covariance
                double Term3[] = new double[Baseline];
                for (int k = 0; k < Term3.length; k++) {
                    Term3[k] = -2 * pow(1 - alpha[0], GUARDBAND + 1) * (1 - pow(1 - alpha[0], 2.0 + k)) / (2.0 + k);
                }
                // the correction factor for sigma
                sigmaCoeff = new double[Baseline];
                for (int k = 0; k < Baseline; k++) {
                    sigmaCoeff[k] = Math.sqrt(Term1 + Term2[k] + Term3[k]);
                }
                deltaSigma = new double[Baseline];
                for (int k = 0; k < Baseline; k++) {
                    deltaSigma[k] =
                            (alpha[0] / UCL_Y[k]) * (0.1289 - (0.2414 - 0.1826 * pow(1 - alpha[0], 4)) * log(
                                    10 * 0.05)); // hard-coded yellow threshold to 0.05
                }
                if (!any(dataInd_js(y, ndxBaselineOK))) {
                    Sigma = 0;
                } else {
                    Sigma = sigmaCoeff[degFreedom - 1] * std(dataInd_js(y, ndxBaselineOK)) + deltaSigma[degFreedom - 1];
                    for (int k = 0; k < Baseline; k++) {
                        minSigma[k] = (alpha[0] / UCL_Y[k]) * (1 + 0.5 * (1 - alpha[0]) * (1 - alpha[0]));
                    }
                    Sigma = max(Sigma, minSigma[degFreedom - 1]);
                }
            }
//
            denom[i] = max(denom[i], 0.5);
            // making sure that denominator is big enough
            // Don't update the mean if seasonal coefficient is small, or
            // there is a drastic jump in the mean
            if (c[i] < 0.05 || m[i] / m[i - 1] > 10) {
                m[i] = m[i - 1];
            }
            // Don't update parameters if
            // 1) prediction error is too big
            // 2) prediction is negative
            // 3) Holiday
            // 4) Day after holiday
            if ((
                        (abs(y_Pred[i] - y[i] + Adj) / denom[i] > APE_LIMIT) &&
                        (numel(ndxBaseline) == numel(ndxBaselineOK)) &&
                        (y[i] > c[8 + (i % 7) - 1] * OpenMath.percentile(dataInd_js(y, ndxBaseline), 95.0)))
                || HOL[i] == 1) {
                m[i] = m[i - 1];
                b[i] = b[i - 1];
                c[i] = c[i - season];
            }
            test_stat[i] =
                    (y[i] - y_Pred[i] - Adj)
                    / denom[i]; // Calculating the test statistics(removing the adjustment added on line 69
            if (bSparseFlag) {
                ytemp = dataInd_js(y, ndxBaselineOK);
                Sigma = Math.max(Sigma, 0.5);
                test_stat[i] = (m[i] - mean(ytemp) + Adj) / Sigma;
            }
            if ((y[i] - Adj) == 0) { // if value is 0 to begin with - return 0 for the statistic
                test_stat[i] = 0;
            }
            if (ndxBaselineOK.length == 0) {
                test_stat[i] = 0;
            }
            pvalues[i] = 1 - normcdf(test_stat[i], 0, 1); // Using Gaussian (normal) 0,1 distribution table value
            if (ndxBaseline[ndxBaseline.length - 1] < Baseline) {
                // increase baseline vector
                int bt[] = ndxBaseline;
                ndxBaseline = new int[bt.length + 1];
                ndxBaseline[0] = 0;
                for (int k = 1; k < ndxBaseline.length; k++) {
                    ndxBaseline[k] = bt[k - 1];
                }
            }
            arrayAdd(ndxBaseline, 1); // go forward by one day
//System.out.println (String.format ("%.4f %.4f %.4f %.4f %.4f %.4f %.4f %.4f %.4f",
//  denom[i], pvalues[i], m[i], c[i], test_stat[i], y_Pred[i], (bSparseFlag?1.0:0.0), m0, multFac));
        }
        arrayAdd(y_Pred, -Adj); // remove adjustment from prediction
//
        for (i = 0; i < data.length; i++) {
            levels[i] = pvalues[i];
        }
        expectedData = y_Pred;
    }

    private void readConfigFile() {
        Properties defaultProps = new Properties();
        InputStream in = getClass().getResourceAsStream("/GSSages.properties");
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
        setStartDate(tddi.getStartDate());
        calculate(data);
        tddi.setLevels(getLevels());
    }

    @Override
    public void runDetector(TemporalDetectorDataInterface tddi) {
        double[] data = tddi.getCounts();
//
        setStartDate(tddi.getStartDate());
        calculate(data);
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
     * @param bautocoef
     */
    public void setAutoCoef(boolean bautocoef) {
        bAutoCoef = bautocoef;
    }

    /**
     * @param date
     */
    public void setStartDate(java.util.Date date) {
        startDate = date;
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
