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

import java.util.PriorityQueue;

public class LinearInterpolation {

    double[] x, y;

    public double getMinVal() {
        return x[0];
    }

    public double getMaxVal() {
        return x[x.length - 1];
    }

    PriorityQueue<DoubleDoublePoint> pq;

    private class DoubleDoublePoint implements Comparable<DoubleDoublePoint> {

        private double x, y;

        public DoubleDoublePoint(double _x, double _y) {
            x = _x;
            y = _y;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public int compareTo(DoubleDoublePoint o) {
            if (o.getX() < this.getX()) {
                return 1;
            } else if (o.getX() == this.getX()) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    private class DoubleIntegerPoint implements Comparable<DoubleIntegerPoint> {

        private double x;
        private int y;

        public DoubleIntegerPoint(double _x, int _y) {
            x = _x;
            y = _y;
        }

        public double getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int compareTo(DoubleIntegerPoint o) {
            if (o.getX() < this.getX()) {
                return 1;
            } else if (o.getX() == this.getX()) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    public LinearInterpolation(double[] _x, double[] _y) {
        x = _x;
        y = _y;
        pq = new PriorityQueue<DoubleDoublePoint>(x.length);
        for (int i = 0; i < x.length; i++) {
            pq.add(new DoubleDoublePoint(x[i], y[i]));
        }
        int index = 0;
        DoubleDoublePoint temp;
        while (!pq.isEmpty()) {
            temp = pq.poll();
            x[index] = temp.getX();
            y[index] = temp.getY();
            index++;
        }
    }

    /**
     * In the event of an invalid value, the array that is returned will contain Double.NaN
     */
    public double[] interpolatedValues(double[] valuesToInterpolate) {
        PriorityQueue<DoubleIntegerPoint> vals = new PriorityQueue<DoubleIntegerPoint>(valuesToInterpolate.length);
        double[] interpolatedValues = new double[valuesToInterpolate.length];
        for (int i = 0; i < valuesToInterpolate.length; i++) {
            if (valuesToInterpolate[i] < x[0] || valuesToInterpolate[i] > x[x.length - 1] || Double
                    .isNaN(valuesToInterpolate[i]) || Double.isInfinite(valuesToInterpolate[i])) {
                interpolatedValues[i] = Double.NaN;
            } else {
                vals.add(new DoubleIntegerPoint(valuesToInterpolate[i], i));
            }
        }
        DoubleIntegerPoint tempVal;
        int index = 0;
        while (!vals.isEmpty()) {
            tempVal = vals.poll();
            index = findIndex(index, tempVal.getX());
            interpolatedValues[tempVal.getY()] = calculateInterpolateValue(index, tempVal.getX());
        }

        return interpolatedValues;
    }

    /**
     * In the event of an invalid value, the array that is returned will contain Double.NaN
     */
    public double interpolatedValue(double valueToInterpolate) {
        double returnedValue;
        if (valueToInterpolate < x[0] || valueToInterpolate > x[0] || Double.isNaN(valueToInterpolate) || Double
                .isInfinite(valueToInterpolate)) {
            returnedValue = Double.NaN;
        }

        int index = findIndex(0, valueToInterpolate);

        returnedValue = calculateInterpolateValue(index, valueToInterpolate);

        return returnedValue;
    }

    private int findIndex(int currIndex, double currVal) {
        for (int i = currIndex; i < x.length - 1; i++) {
            if (currVal == x[i + 1] || currVal == x[i] || (currVal > x[i] && currVal < x[i + 1])) {
                return i;
            }
        }
        return -1; // Should never reach this point because the value was determined to be within the range to begin with.
    }

    private double calculateInterpolateValue(int index, double value) {
        if (index == -1) {
            return -9999;
        }
        if (x[index] == value) {
            return y[index];
        } else if (x[index + 1] == value) {
            return y[index + 1];
        } else {
            return y[index] + (value - x[index]) * ((y[index + 1] - y[index]) / (x[index + 1] - x[index]));
        }
    }
}
