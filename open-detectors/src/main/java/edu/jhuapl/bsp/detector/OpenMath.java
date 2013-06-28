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
//

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import static java.lang.Math.log;
import static java.lang.Math.pow;

//
public class OpenMath {

    /* Simulates MATLAB any() function. Will return true if any value in the
     * input array is non-zero.
     * @param in
     * @return true if a non-zero value is found
     */
    public static boolean any(double[] in) {
        if (in != null) {
            for (int i = 0; i < in.length; i++) {
                if (in[i] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /* Calculates the abs of the values in the input vector.
     * @param in
     *           input data
     * @return default is 0
     */
    public static double[] arrayAbs(double[] in) {
        if (in != null) {
            double[] r = new double[in.length];
            for (int i = 0; i < in.length; i++) {
                r[i] = Math.abs(in[i]);
            }
            return r;
        }
        return new double[0];
    }

    /* Calculates the sum of the values in the input list.
     * @param in
     *           input data
     * @return default is 0
     */
    public static double sum(double[] in) {
        if (in != null) {
            double sum = 0;
            for (int i = 0; i < in.length; i++) {
                sum += in[i];
            }
            if (Double.isNaN(sum) || Double.isInfinite(sum)) {
                return 0;
            } else {
                return sum;
            }
        }
        return 0;
    }

    /* Calculates the sum of the values in the input list.
     * @param in
     *           input data
     * @return default is 0
     */
    public static double sum(int[] in) {
        if (in != null) {
            double sum = 0;
            for (int i = 0; i < in.length; i++) {
                sum += in[i];
            }
            if (Double.isNaN(sum) || Double.isInfinite(sum)) {
                return 0;
            } else {
                return sum;
            }
        }
        return 0;
    }

    /* Calculates the min of the values in the input list.
     * @param in
     *           input data
     * @return default is 0
     */
    public static double min(double[] in) {
        if (in != null) {
            double minvalue = Double.POSITIVE_INFINITY;
            for (int i = 0; i < in.length; i++) {
                minvalue = Math.min(in[i], minvalue);
            }
            return minvalue;
        }
        return Double.NEGATIVE_INFINITY;
    }

    /* Calculates the max of the values in the input list.
     * @param in
     *           input data
     * @return default is 0
     */
    public static double max(double[] in) {
        if (in != null) {
            double minvalue = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < in.length; i++) {
                minvalue = Math.max(in[i], minvalue);
            }
            return minvalue;
        }
        return Double.POSITIVE_INFINITY;
    }

    /* Calculates the average of the values in the input list.
     * @param in
     *           input data
     * @return default is 0
     */
    public static double mean(int[] in) {
        if (in != null && in.length > 0) {
            double sum = 0;
            for (int i = 0; i < in.length; i++) {
                sum += in[i];
            }
            double result = sum / in.length;
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                return 0;
            } else {
                return result;
            }
        }
        return 0;
    }

    /* Calculates the average of the values in the input list.
     * @param in
     *           input data
     * @return default is 0
     */
    public static double mean(double[] in) {
        if (in != null && in.length > 0) {
            double sum = 0;
            for (int i = 0; i < in.length; i++) {
                sum += in[i];
            }
            double result = sum / in.length;
            if (Double.isNaN(result) || Double.isInfinite(result)) {
                return 0;
            } else {
                return result;
            }
        }
        return 0;
    }

    /* Calculates the average of the values in the input list.
     * @param in
     *           input data
     * @return default is 0
     */
    public static double[] mean(double[][] in) {
        if (in != null) {
            int M = in.length, N = in[0].length;
            double sums[] = ones(N, 0);
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < M; j++) {
                    sums[i] += in[j][i];
                }
                sums[i] /= M;
                if (Double.isNaN(sums[i]) || Double.isInfinite(sums[i])) {
                    sums[i] = 0;
                }
            }
            return sums;
        }
        return new double[0];
    }

    /* Calculates the median of the values in the input list.
     * @param in
     *           input data
     * @return default is 0
     */
    public static double[] median(double[][] in) {
        if (in != null) {
            int M = in.length, N = in[0].length;
            double medians[] = ones(N, 0);
            double vals[] = new double[M];
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < M; j++) {
                    vals[j] = in[j][i];
                }
                Arrays.sort(vals);
                Double median = null;
                int index = (int) Math.ceil(vals.length / 2);
                if ((vals.length & 1) == 1) {
                    median = new Double(vals[index]);
                } else {
                    median = new Double((vals[index] + vals[index - 1]) / 2.0);
                }
                if (!median.isNaN() && !median.isInfinite()) {
                    medians[i] = median;
                }
            }
            return medians;
        }
        return new double[0];
    }

    /* Calculates the transpose of a matrix.
     * @param in
     *           input data
     * @return transposed
     */
    public static double[][] transpose(double[][] in) {
        int M = in.length, N = in[0].length;
        double[][] out = new double[N][M];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                out[i][j] = in[j][i];
            }
        }
        return out;
    }

    /* Simulates MATLAB reshape [][] function.
     * @param in
     * @param M
     * @param N
     * @return reshaped data
     */
    public static double[][] reshape(double[] in, int M, int N) {
        int n = 0;
        double[][] out = new double[M][N];
        for (int j = 0; j < N; j++) {
            for (int i = 0; i < M; i++) {
                out[i][j] = in[n++];
            }
        }
        return out;
    }

    /* Simulates MATLAB reshape [] function.
     * @param in
     *           input data
     * @return reshaped data
     */
    public static double[] reshape(double[][] in) {
        int M = in.length, N = in[0].length, n = 0;
        double[] out = new double[M * N];
        for (int j = 0; j < N; j++) {
            for (int i = 0; i < M; i++) {
                out[n++] = in[i][j];
            }
        }
        return out;
    }

    /* Calculate the standard deviation of the list
     * @param in
     * @return default is 0
     */
    public static double std(double[] in) {
        if (in != null && in.length > 1) {
            double sum = 0;
            double avg = OpenMath.mean(in);
            for (int i = 0; i < in.length; i++) {
                sum += ((in[i] - avg) * (in[i] - avg));
            }
            double stddev = Math.sqrt(sum / (in.length - 1));
            if (Double.isNaN(stddev) || Double.isInfinite(stddev)) {
                return 0;
            } else {
                return stddev;
            }
        }
        return 0.0;
    }

    /* Simulates the median function.
     * @param in
     * @return default is 0
     */
    public static double median(double[] in) {
        if (in != null) {
            if (in.length > 0) {
                double[] d = new double[in.length];
                for (int i = 0; i < in.length; i++) {
                    d[i] = in[i];
                }
                Arrays.sort(d);
                Double median = null;
                int index = (int) Math.ceil(d.length / 2);
                if ((d.length & 1) == 1) {
                    median = new Double(d[index]);
                } else {
                    median = new Double((d[index] + d[index - 1]) / 2.0);
                }
                if (median.isNaN() || median.isInfinite()) {
                    return 0.0;
                } else {
                    return median.doubleValue();
                }
            } else {
                return 0.0;
            }
        }
        return 0.0;
    }

    /* Simulates MATLAB find() function. Returns a list of indices.
     * @param d
     * @param val
     * @return array of indices for those elements
     */
    public static int[] findLT(double[] d, double value) {
        if (d != null) {
            ArrayList<Integer> r = new ArrayList<Integer>();
            for (int i = 0; i < d.length; i++) {
                if (d[i] < value) {
                    r.add(i);
                }
            }
            int[] ia = new int[r.size()];
            for (int i = 0; i < ia.length; i++) {
                ia[i] = r.get(i);
            }
            return ia;
        }
        return new int[0];
    }

    /* Simulates MATLAB find( v > 0 ) function. Returns a list of indices.
     * @param d
     * @return array of indices for those non zero elements
     */
    public static int[] find(double[] d) {
        if (d != null) {
            ArrayList<Integer> r = new ArrayList<Integer>();
            for (int i = 0; i < d.length; i++) {
                if (d[i] > 0.0) {
                    r.add(i);
                }
            }
            int[] ia = new int[r.size()];
            for (int i = 0; i < ia.length; i++) {
                ia[i] = r.get(i);
            }
            return ia;
        }
        return new int[0];
    }

    /* Simulates MATLAB find( v > 0 ) function. Returns a list of indices.
     * @param d
     * @return array of indices for those non zero elements
     */
    public static int[] find(int[] d) {
        if (d != null) {
            ArrayList<Integer> r = new ArrayList<Integer>();
            for (int i = 0; i < d.length; i++) {
                if (d[i] > 0) {
                    r.add(i);
                }
            }
            int[] ia = new int[r.size()];
            for (int i = 0; i < ia.length; i++) {
                ia[i] = r.get(i);
            }
            return ia;
        }
        return new int[0];
    }

    /* Simulates the MATLAB feature where values can be extracted from a set by
     * being passed in a set of indicies via an array.
     * @param d
     * @param i
     * @return default is double[0]
     */
    public static double[] dataInd_js(double[] d, int[] i) {
        if (i.length > 0) {
            double[] r = new double[i.length];
            for (int j = 0; j < i.length; j++) {
                r[j] = d[i[j] - 1];
            }
            return r;
        }
        return new double[0];
    }

    /* Simulates the MATLAB feature where values can be extracted from a set by
     * being passed in a set of indicies via an array.
     * @param d
     * @param i
     * @return default is int[0]
     */
    public static int[] dataInd_js(int[] d, int[] i) {
        if (i.length > 0) {
            int[] r = new int[i.length];
            for (int j = 0; j < i.length; j++) {
                r[j] = d[i[j] - 1];
            }
            return r;
        }
        return new int[0];
    }

    /* Simulates the MATLAB feature where values can be extracted from a set by
     * being passed in a set of indicies via an array.
     * @param d
     * @param ai
     * @return default is double[0]
     */
    public static double[] dataInd(double[] d, ArrayList<Integer> ai) {
        if (ai.size() > 0) {
            double[] r = new double[ai.size()];
            for (int j = 0; j < ai.size(); j++) {
                r[j] = d[ai.get(j)];
            }
            return r;
        }
        return new double[0];
    }

    /* Simulates the MATLAB feature where values can be extracted from a set by
     * being passed in a set of indicies via an array.
     * @param d
     * @param i
     * @return default is double[0]
     */
    public static double[] dataInd(double[] d, int[] i) {
        if (i.length > 0) {
            double[] r = new double[i.length];
            for (int j = 0; j < i.length; j++) {
                r[j] = d[i[j]];
            }
            return r;
        }
        return new double[0];
    }

    /* Simulates the MATLAB feature where values can be extracted from a set by
     * being passed in a set of indicies via an array.
     * @param d
     * @param i
     * @return default is int[0]
     */
    public static int[] dataInd(int[] d, int[] i) {
        if (i.length > 0) {
            int[] r = new int[i.length];
            for (int j = 0; j < i.length; j++) {
                r[j] = d[i[j]];
            }
            return r;
        }
        return new int[0];
    }

    /* Simulates the MATLAB feature where values can be set by indices
     * @param d
     * @param i1
     * @param i2
     * @return default is double[0]
     */
    public static double[] dataVec(double[] d, int i1, int i2) {
        if (0 <= i1 && i2 < d.length) {
            double[] r = new double[i2 - i1 + 1];
            for (int j = 0; j < (i2 - i1 + 1); j++) {
                r[j] = d[i1 + j];
            }
            return r;
        }
        return new double[0];
    }

    /* Simulates the MATLAB feature where values can be set by indices
     * @param d
     * @param i1
     * @param i2
     * @return default is int[0]
     */
    public static int[] dataVec(int[] d, int i1, int i2) {
        if (0 <= i1 && i2 < d.length) {
            int[] r = new int[i2 - i1 + 1];
            for (int j = 0; j < (i2 - i1 + 1); j++) {
                r[j] = d[i1 + j];
            }
            return r;
        }
        return new int[0];
    }

    /* Simulates MATLAB array addition capability.
     * @param list
     * @param x
     */
    public static void arrayAdd(ArrayList<Integer> list, int x) {
        for (int i = 0; list != null && i < list.size(); i++) {
            list.set(i, list.get(i) + x);
        }
    }

    /* Simulates MATLAB array addition capability.
     * @param d
     * @param x
     */
    public static int[] arrayMod(int[] d, int x) {
        if (d.length > 0) {
            int[] r = new int[d.length];
            for (int i = 0; i < d.length; i++) {
                r[i] = (d[i] + x) % x;
            }
            return r;
        }
        return new int[0];
    }

    /* Simulates MATLAB array addition capability.
     * @param d
     * @param x
     */
    public static void arrayAdd(double[] d, double x) {
        for (int i = 0; i < d.length; i++) {
            d[i] += x;
        }
    }

    /* Simulates MATLAB array addition capability.
     * @param d
     * @param x
     */
    public static void arrayAdd(int[] d, int x) {
        for (int i = 0; i < d.length; i++) {
            d[i] += x;
        }
    }

    /* Simulates MATLAB array addition capability.
     * @param d
     * @param x
     */
    public static double[] arrayAdd2(double[] d, double x) {
        double[] r = new double[d.length];
        for (int i = 0; i < d.length; i++) {
            r[i] = d[i] + x;
        }
        return r;
    }

    /* Simulates MATLAB array addition capability.
     * @param d
     * @param x
     */
    public static int[] arrayAdd2(int[] d, int x) {
        int[] r = new int[d.length];
        for (int i = 0; i < d.length; i++) {
            r[i] = d[i] + x;
        }
        return r;
    }

    /* Simulates MATLAB interp1 interpolation for lookup table.
     * @param list1 lookup values from
     * @param list2 lookup values to
     * @param x value to be interpolated
     * @return interpolated value
     */
    public static double interp1(double[] list1, double[] list2, double x) {
        int i = Arrays.binarySearch(list1, x);
        if (i >= 0) {
            return list2[i];
        } // found the element
        i = -(i + 1) - 1; // not found, adjust the insertion point
        if (i == 0) {
            return list2[0];
        } else if (i >= list1.length) {
            return list2[list2.length - 1];
        } else {
            return (x - list1[i]) * ((list2[i + 1] - list2[i]) / (list1[i + 1] - list1[i])) + list2[i];
        }
    }

    /* Simulates MATLAB datenum function for date to date number
     * @param year
     * @param month
     * @param day
     * @return date number
     */
    public static int datenum(int year, int month, int day) {
        final int Month[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int t = 0;
        for (int y = 0; y < year; y++) {
            if (y % 400 == 0) {
                t += 1;
            } else if (y % 100 != 0 && y % 4 == 0) {
                t += 1;
            }
        }
        t += year * 365;
        for (int m = 0; m < month - 1; m++) {
            t += Month[m];
        }
        t += day;
        if (month > 2) {
            if (year % 400 == 0) {
                t += 1;
            } else if (year % 100 != 0 && year % 4 == 0) {
                t += 1;
            }
        }
        return t;
    }

    /* Simulates MATLAB ones function.
     * @param M
     * @return ones
     */
    public static double[] ones(int M) {
        double[] out = new double[M];
        for (int i = 0; i < M; i++) {
            out[i] = 1;
        }
        return out;
    }

    /* Simulates MATLAB ones function.
     * @param M
     * @param K
     * @return ones
     */
    public static double[] ones(int M, double K) {
        double[] out = new double[M];
        for (int i = 0; i < M; i++) {
            out[i] = K;
        }
        return out;
    }

    /* Simulates MATLAB numel function.
     * @param in
     * @return number of elements
     */
    public static int numel(double[][] in) {
        return in.length * in[0].length;
    }

    /* Simulates MATLAB numel function.
     * @param in
     * @return number of elements
     */
    public static int numel(double[] in) {
        return in.length;
    }

    /* Simulates MATLAB numel function.
     * @param in
     * @return number of elements
     */
    public static int numel(int[] in) {
        return in.length;
    }

    /* Simulates MATLAB copy array function.
     * @param in
     * @return new copy
     */
    public static double[] copya(double[] in) {
        double[] out = new double[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }

    /* Simulates MATLAB prctile function.
     * @param in
     * @param p
     * @return percentile
     */
    public static double prctile(double[] in, double p) {
        Percentile prc = new Percentile();
        double in2[] = copya(in);
        Arrays.sort(in2);
        prc.setData(in2);
        double result = prc.evaluate(p);
        return result;
    }

    /* Simulates MATLAB normcdf function.
     * @param stat
     * @return value
     */
    public static double normcdf(double stat, double m, double s) {
        double result = 0;
        NormalDistribution normdist = new NormalDistribution(m, s);
        result = normdist.cumulativeProbability(stat);
        return result;
    }

    /* Simulates MATLAB ismember function.
     * @param stat
     * @return value
     */
    public static int[] ismember(int[] A, int[] B) {
        int N = A.length, M = B.length, r[] = new int[N];
        for (int i = 0; i < N; i++) {
            r[i] = 0;
            for (int j = 0; j < M; j++) {
                if (A[i] == B[j]) {
                    r[i] = 1;
                    break;
                }
            }
        }
        return r;
    }

    public static double percentile(double[] x, double p) {
        ArrayList<Double> intermediate = new ArrayList<Double>(x.length);
        for (int i = 0; i < x.length; i++) {
            if (!Double.isInfinite(x[i]) && !Double.isNaN(x[i])) {
                intermediate.add(x[i]);
            }
        }
        Iterator<Double> iter = intermediate.iterator();
        double[] xcopy = new double[intermediate.size()];
        int index = 0;
        while (iter.hasNext()) {
            xcopy[index] = iter.next();
            index++;
        }
        Arrays.sort(xcopy);
        double minprctile = 100 * ((.5) / xcopy.length), maxprctile = 100 * ((xcopy.length - .5) / xcopy.length);
        if (p >= maxprctile) {
            return xcopy[xcopy.length - 1];
        } else if (p <= minprctile) {
            return xcopy[0];
        }
        double[] xprctile = new double[xcopy.length];
        for (int i = 0; i < xprctile.length; i++) {
            xprctile[i] = 100 * (((xprctile.length - (xprctile.length - i - 1)) - .5) / xprctile.length);
        }
        LinearInterpolation LI = new LinearInterpolation(xprctile, xcopy);
        return LI.interpolatedValue(p);
    }

    public static void main(String[] args) {
//    double dt[] = {30, 10, 1, 1, 1, 0, 1, 1, 1, 0, 0, 0};
//    System.out.println (" "+percentile(dt, Double.parseDouble(args[0])));
        double dt[] = {1, 2.33, 3, 4, 5, 6.33, 7, 8, 9, 10.01, 11, 12, 13, 14, 1, 1, 1, 1, 1, 1, 1};
        System.out.println("median=" + median(median(reshape(dt, 3, 7))));
        System.out.println("mean=" + mean(mean(reshape(dt, 3, 7))));
        int Baseline = 56;
        double UCL_R[] = new double[Baseline], UCL_Y[] = new double[Baseline];
        for (int k = 0; k < Baseline; k++) {
            TDistribution tdist = new TDistribution(k + 1);
            UCL_R[k] = tdist.inverseCumulativeProbability(1 - 0.01);
            UCL_Y[k] = tdist.inverseCumulativeProbability(1 - 0.05);
        }
        double[] alpha = {0.4, 0, 0, 05};
        int GUARDBAND = 0;
        double Term1 = alpha[0] / (2 - alpha[0]);
        double Term2[] = new double[Baseline];
        for (int k = 0; k < Term2.length; k++) {
            Term2[k] = 1 / (2 + k);
        }
        double Term3[] = new double[Baseline];
        for (int k = 0; k < Term3.length; k++) {
            Term3[k] = -2 * pow(1 - alpha[0], GUARDBAND + 1) * (1 - pow(1 - alpha[0], 2 + k)) / (2 + k);
        }
        double[] sigmaCoeff = new double[Baseline];
        for (int k = 0; k < Baseline; k++) {
            sigmaCoeff[k] = Math.sqrt(Term1 + Term2[k] + Term3[k]);
        }
        double[] deltaSigma = new double[Baseline];
        for (int k = 0; k < Baseline; k++) {
            deltaSigma[k] =
                    (alpha[0] / UCL_Y[k]) * (0.1289 - (0.2414 - 0.1826 * pow(1 - alpha[0], 4)) * log(10 * 0.05));
        }
        for (int k = 0; k < Baseline; k++) {
            System.out.println(String.format("%.3f %.3f %.3f %.3f %.3f",
                                             UCL_R[k], UCL_Y[k], Term2[k], Term3[k], sigmaCoeff[k]));
        }

    }
}
