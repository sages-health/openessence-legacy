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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static edu.jhuapl.bsp.detector.OpenMath.dataInd;
import static edu.jhuapl.bsp.detector.OpenMath.find;
import static edu.jhuapl.bsp.detector.OpenMath.median;

public class FilterBaselineZeros3 {

    public static final double DEFAULT_THRESHOLD_PROB = 0.01;
    public static final int MIN_NUM_ZEROS = 1;
    //
    private double thresholdProb;
    private int minNumZeros;

    //
    public FilterBaselineZeros3() {
        this(DEFAULT_THRESHOLD_PROB, MIN_NUM_ZEROS);
    }

    public FilterBaselineZeros3(double threshold_prob, int minNumZeros) {
        this.thresholdProb = threshold_prob;
        this.minNumZeros = minNumZeros;
    }

    public double getThresholdProb() {
        return thresholdProb;
    }

    public void setThresholdProb(final double thresholdProb) {
        this.thresholdProb = thresholdProb;
    }

    public int getMinNumZeros() {
        return minNumZeros;
    }

    public void setMinNumZeros(final int minNumZeros) {
        this.minNumZeros = minNumZeros;
    }

    public int[] filterBaselineZeros(double[] dt, int len) {
        double[] dt2 = new double[len];
        for (int i = 0; i < len; i++) {
            dt2[i] = dt[i];
        }
        return filterBaselineZeros(dt2);
    }

    /**
     * Remove excess zeros from baseline counts, where excess zeros are those in strings that are unreasonably long
     * compared to the rest of the baseline.
     *
     * @param dt Data to be filtered
     * @return An array of indices that hold valid data
     */
    public int[] filterBaselineZeros(double[] dt) {
        double testData[];
        int i, ndxOK[], ndx1, ndx2, ndxOut[];
        ArrayList<Integer> ndxStart, ndxEnd;
        ndxStart = new ArrayList<Integer>();
        ndxEnd = new ArrayList<Integer>();
        Double numZerosTest[];
//
        testData = new double[1 + dt.length];
        testData[0] = 1;
        for (i = 0; i < dt.length; i++) {
            testData[i + 1] = dt[i];
        }
        for (i = 0; i < testData.length - 1; i++) {
            if (testData[i] != 0 && testData[i + 1] == 0) {
                ndxStart.add(i);
            }
        }
        for (i = 0; i < dt.length; i++) {
            testData[i] = dt[i];
        }
        testData[i] = 1;
        for (i = 0; i < testData.length - 1; i++) {
            if (testData[i] == 0 && testData[i + 1] != 0) {
                ndxEnd.add(i);
            }
        }
        numZerosTest = new Double[ndxStart.size()];
        for (i = 0; i < ndxStart.size(); i++) {
            numZerosTest[i] = new Double(ndxEnd.get(i) - ndxStart.get(i) + 1);
        }
        CompD co = new CompD(numZerosTest);
        Integer[] idx = co.createIdx();
        Arrays.sort(idx, co);
//
        //for (int k : idx) { System.out.println (k+" "+numZerosTest[k]); }

        ndxOK = new int[dt.length];
        for (i = 0; i < dt.length; i++) {
            ndxOK[i] = i;
        }
        for (i = 0; i < idx.length; i++) {
            int key = idx[i];
            double val = numZerosTest[key];
            if (val < minNumZeros) {
                continue;
            }
            for (ndx1 = 0; ndx1 < dt.length; ndx1++) {
                if (ndxOK[ndx1] == ndxStart.get(key)) {
                    break;
                }
            }
            for (ndx2 = 0; ndx2 < dt.length; ndx2++) {
                if (ndxOK[ndx2] == ndxEnd.get(key)) {
                    break;
                }
            }
            ndxOut = new int[ndxOK.length - (ndx2 - ndx1 + 1)];
            int k = 0;

            //    System.out.println (ndxOK.length+" "+ndxOut.length+" "+ndx2+" "+ndx1);

            for (int j = 0; j < ndx1; j++) {
                ndxOut[k++] = ndxOK[j];
            }
            for (int j = ndx2 + 1; j < ndxOK.length; j++) {
                ndxOut[k++] = ndxOK[j];
            }
            int numValuesOut = ndxOut.length;
            if (numValuesOut == 0) {
                break;
            }
            double[] dtOut = dataInd(dt, ndxOut);
            int nsum = 0;
            for (int j = 0; j < dtOut.length; j++) {
                if (dtOut[j] == 0) {
                    nsum++;
                }
            }
            double numZerosOut = Math.max(1, nsum);

//      System.out.println ("nsum="+nsum+" "+numZerosOut+" "+" "+numValuesOut+" "+val);

            if (Math.pow(numZerosOut / numValuesOut, val) > thresholdProb) {
                break;
            }
            ndxOK = ndxOut;
        }
        double[] dtOut = dataInd(dt, ndxOK);
        int nsum = 0;
        for (int j = 0; j < dtOut.length; j++) {
            if (dtOut[j] > 0) {
                nsum++;
            }
        }
        if (nsum < 2) {
            ndxOK = new int[0];
        }
        return ndxOK;
    }

    class CompD implements Comparator<Integer> {

        private final Double[] a;

        public CompD(Double[] a) {
            this.a = a;
        }

        public Integer[] createIdx() {
            Integer[] idx = new Integer[a.length];
            for (int i = 0; i < a.length; i++) {
                idx[i] = i;
            }
            return idx;
        }

        @Override
        public int compare(Integer i1, Integer i2) {
            return a[i2].compareTo(a[i1]);
        }
    }

    public static boolean filterBaselineZerosTest(double[] d) {
        double median = median(d);
        double nonzeromedian = median(dataInd(d, find(d)));
        return (median > 0 || nonzeromedian > 4);
    }
}
