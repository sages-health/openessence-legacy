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

import edu.jhuapl.bsp.detector.util.DateHelper;
import edu.jhuapl.bsp.detector.util.StringUtils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TemporalDetectorSimpleDataObject implements TemporalDetectorDataInterface {

    protected double[] counts;
    protected java.util.Date startDate;
    protected String timeResolution;
    protected java.util.Date[] dates;
    protected HashMap<String, double[]> regressors;
    protected ArrayList<String> regressorIDs;
    protected Map<String, double[]> outputValues;

    protected double[] levels;
    protected double[] colors;
    protected double[] expecteds;
    protected double[] r2Levels;
    protected double[] switchFlags;
    protected String[] switchInfo;
    protected double[] testStatistics;

    protected int countMaximumFractionDigits;
    protected int expectedMaximumFractionDigits;
    protected int levelMaximumFractionDigits;

    public TemporalDetectorSimpleDataObject() {
        counts = null;
        startDate = null;
        dates = null;
        regressors = new HashMap<String, double[]>();
        regressorIDs = new ArrayList<String>();

        outputValues = new HashMap<String, double[]>();

        levels = null;
        colors = null;
        expecteds = null;
        r2Levels = null;
        switchFlags = null;
        switchInfo = null;

        countMaximumFractionDigits = 0;
        expectedMaximumFractionDigits = 1;
        levelMaximumFractionDigits = 3;
    }

    public TemporalDetectorDataInterface createCopy() {
        TemporalDetectorSimpleDataObject tddo = new TemporalDetectorSimpleDataObject();
        tddo.setColors(this.getColors());
        tddo.setCountMaximumFractionDigits(this.getCountMaximumFractionDigits());
        tddo.setCounts(this.getCounts());
        tddo.setExpectedMaximumFractionDigits(this.expectedMaximumFractionDigits);
        tddo.setExpecteds(this.getExpecteds());
        tddo.setLevelMaximumFractionDigits(this.levelMaximumFractionDigits);
        tddo.setLevels(this.getLevels());
        tddo.setR2Levels(this.getR2Levels());
        tddo.setStartDate(this.getStartDate());
        tddo.setTimeResolution(this.timeResolution);
        tddo.setDates(this.dates);
        tddo.setSwitchFlags(this.getSwitchFlags());
        tddo.setSwitchInfo(this.getSwitchInfo());
        tddo.setTestStatistics(this.getTestStatistics());
        tddo.regressors = new HashMap<String, double[]>(this.regressors);
        tddo.regressorIDs = new ArrayList<String>(this.regressorIDs);
        tddo.outputValues = new HashMap<String, double[]>(this.outputValues);

        return tddo;
    }

    public void cropStartup(int prepullDays) {
        double[] cropCounts = new double[size() - prepullDays];
        double[][] cropRegressors = null;
        double[][] _regressors = getRegressors();
        if (regressorIDs != null && regressorIDs.size() > 0) {
            cropRegressors = new double[regressorIDs.size()][size() - prepullDays];
        }
        // TODO: double check this logic
        for (double[] vals : outputValues.values()) {
            vals = Arrays.copyOfRange(vals, prepullDays, size());
        }

        double[] cropLevels = new double[size() - prepullDays];
        double[] cropColors = new double[size() - prepullDays];
        double[] cropExpecteds = new double[size() - prepullDays];
        double[] cropR2Levels = new double[size() - prepullDays];
        double[] cropSwitchFlags = new double[size() - prepullDays];
        String[] cropSwitchInfo = new String[size() - prepullDays];
        java.util.Date[] cropDates = new java.util.Date[size() - prepullDays];

        for (int i = 0; i < cropCounts.length; i++) {
            if (counts != null) {
                cropCounts[i] = counts[i + prepullDays];
            }
            if (cropRegressors != null) {
                for (int j = 0; j < cropRegressors.length; j++) {
                    cropRegressors[j][i] = _regressors[j][i + prepullDays];
                }
            }
            if (levels != null) {
                cropLevels[i] = levels[i + prepullDays];
            }
            if (colors != null) {
                cropColors[i] = colors[i + prepullDays];
            }
            if (expecteds != null) {
                cropExpecteds[i] = expecteds[i + prepullDays];
            }
            if (r2Levels != null) {
                cropR2Levels[i] = r2Levels[i + prepullDays];
            }
            if (switchFlags != null) {
                cropSwitchFlags[i] = switchFlags[i + prepullDays];
            }
            if (switchInfo != null) {
                cropSwitchInfo[i] = switchInfo[i + prepullDays];
            }
            if (dates != null) {
                cropDates[i] = dates[i + prepullDays];
            }
        }

        if (counts != null) {
            counts = cropCounts;
        }
        if (cropRegressors != null) {
            for (int j = 0; j < cropRegressors.length; j++) {
                regressors.remove(regressorIDs.get(j));
                regressors.put(regressorIDs.get(j), cropRegressors[j]);
            }
        }
        if (levels != null) {
            levels = cropLevels;
        }
        if (colors != null) {
            colors = cropColors;
        }
        if (expecteds != null) {
            expecteds = cropExpecteds;
        }
        if (r2Levels != null) {
            r2Levels = cropR2Levels;
        }
        if (switchFlags != null) {
            switchFlags = cropSwitchFlags;
        }
        if (switchInfo != null) {
            switchInfo = cropSwitchInfo;
        }
        if (dates != null) {
            dates = cropDates;
        }

        // because there is a chance the startDate hasn't been set
        // make sure to call getStartDate() to lazy initialize it at
        // this point.
        java.util.Date date = getStartDate();
        // System.out.println("Cropping Date: " + date);
        if (date != null) {
            int adjustement = -prepullDays;
            if (timeResolution != null && timeResolution.equalsIgnoreCase("weekly")) {
                adjustement *= 7;
            }
            setStartDate(DateHelper.getDate(date, adjustement));
        }
        // System.out.println("Cropped Date: " + getStartDate());
    }

    /**
     * The number of counts -1 if null
     */
    public int size() {
        if (counts != null) {
            return counts.length;
        } else {
            double[] tmpCounts = getCounts();
            if (tmpCounts != null) {
                return tmpCounts.length;
            } else {
                return -1;
            }
        }
    }

    // ///////// INPUT ELEMENTS //////////////

    /**
     * Set input data to the object.
     *
     * @param counts input data to run detection on
     */
    public void setCounts(double[] _counts) {
        counts = _counts;
    }

    /**
     * The counts
     */
    public double[] getCounts() {
        return counts;
    }

    /**
     * Set start date to the object
     *
     * @param startDate first date of the input data
     */
    public void setStartDate(java.util.Date _startDate) {
        startDate = _startDate;
    }

    /**
     * The first date of the counts
     */
    public java.util.Date getStartDate() {
        return startDate;
    }

    public String getTimeResolution() {
        return timeResolution;
    }

    /**
     * Sets the time resolution.
     */
    public void setTimeResolution(String timeResolution) {
        this.timeResolution = timeResolution;
    }

    /**
     * Set input regressor data to the object.
     *
     * @param _regressor input regressor data for detectors to use
     */
    public void setRegressor(String _regressorID, double[] _regressor) {
        if (_regressor != null) {
            regressors.remove(_regressorID);
            regressors.put(_regressorID, _regressor);
            if (!regressorIDs.contains(_regressorID)) {
                // if the id isn't already there, add it
                regressorIDs.add(_regressorID);
            }
        }
    }

    /**
     * Get a list of the regressors by the regressorIDs.
     */
    public double[][] getRegressors() {
        if (regressorIDs.size() > 0) {
            double[][] r = new double[regressorIDs.size()][];
            for (int i = 0; i < regressorIDs.size(); i++) {
                r[i] = getRegressor(regressorIDs.get(i));
            }
            return r;
        } else {
            return null;
        }
    }

    /**
     * A Regressor
     */
    public double[] getRegressor(String _regressorID) {
        return regressors.get(_regressorID);
    }

    /**
     * Get the list of regressorID's currently set
     */
    public String[] getRegressorIDs() {
        return StringUtils.arrayListToStringArray(regressorIDs);
    }

    // ///////// OUTPUT ELEMENTS //////////////

    protected void setupDates() {
        String tr = timeResolution;
        if (tr == null) {
            tr = "daily";
        }

        java.util.Date date = getStartDate();
        int numDays = size();
        if (date != null && numDays >= 0) {
            dates = new java.util.Date[numDays];
            Calendar cal = new GregorianCalendar();
            cal.setTime(date);
            for (int i = 0; i < numDays; i++) {
                dates[i] = cal.getTime();
                if (tr.equalsIgnoreCase("daily")) {
                    cal.add(Calendar.DATE, 1);
                } else if (tr.equalsIgnoreCase("weekly")) {
                    cal.add(Calendar.DATE, 7);
                } else if (tr.equalsIgnoreCase("monthly")) {
                    cal.add(Calendar.MONTH, 1);
                } else if (tr.equalsIgnoreCase("quarterly")) {
                    cal.add(Calendar.MONTH, 3);
                } else if (tr.equalsIgnoreCase("yearly")) {
                    cal.add(Calendar.YEAR, 1);
                }
            }
        }
    }

    /**
     * The array of Dates
     */
    public java.util.Date[] getDates() {
        if (dates != null) {
            return dates;
        } else {
            setupDates();
            return dates;
        }
    }

    /**
     * The array of Dates
     */
    public void setDates(Date[] dates) {
        this.dates = dates;
    }

    /**
     * The Alt Texts
     */
    public String[] getAltTexts() {
        if (size() >= 0) {
            // sometimes dates don't get set correctly,
            // so you have to call getDates here.
            java.util.Date[] tmpDates = getDates();

            // double[] tmpLevels = getLevels();
            // double[] tmpCounts = getCounts();
            // double[] tmpExpecteds = getExpecteds();
            // double[] tmpProviderCounts = getProviderCounts();
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(countMaximumFractionDigits);
            nf.setGroupingUsed(false);
            NumberFormat levelNF = NumberFormat.getInstance();
            levelNF.setMaximumFractionDigits(levelMaximumFractionDigits);
            levelNF.setGroupingUsed(false);
            NumberFormat expectedNF = NumberFormat.getInstance();
            expectedNF.setMaximumFractionDigits(expectedMaximumFractionDigits);
            expectedNF.setGroupingUsed(false);

            SimpleDateFormat sdf = DateHelper.getStandardDateFormat();

            double[][] r = getRegressors();

            String[] altTexts = new String[size()];
            for (int i = 0; i < altTexts.length; i++) {
                altTexts[i] = "";
                if (tmpDates != null) {
                    altTexts[i] += "Date: " + sdf.format(tmpDates[i]) + ", ";
                }
                if (levels != null) {
                    altTexts[i] += "Level: " + levelNF.format(levels[i]) + ", ";
                }
                if (counts != null) {
                    altTexts[i] += "Count: " + nf.format(counts[i]) + ", ";
                }
                if (expecteds != null) {
                    altTexts[i] += "Expected: " + expectedNF.format(expecteds[i]) + ", ";
                }
                if (regressorIDs.size() > 0) {
                    for (int j = 0; j < regressorIDs.size(); j++) {
                        altTexts[i] += regressorIDs.get(j) + ": " + nf.format(r[j][i]) + ", ";
                    }
                }
                if (switchInfo != null) {
                    altTexts[i] += "Switch: " + switchInfo[i] + ", ";
                }

                if (altTexts[i].length() > 0) {
                    altTexts[i] = altTexts[i].substring(0, altTexts[i].length() - 2);
                }
            }
            return altTexts;
        } else {
            return null;
        }
    }

    /**
     * Set output levels to the object
     *
     * @param levels output levels
     */
    public void setLevels(double[] _levels) {
        levels = _levels;
    }

    /**
     * The output levels
     */
    public double[] getLevels() {
        return levels;
    }

    public void setTestStatistics(double[] testStatistics) {
        this.testStatistics = testStatistics;
    }

    public double[] getTestStatistics() {
        return this.testStatistics;
    }

    /**
     * Set output expecteds to the object
     *
     * @param expecteds output expecteds
     */
    public void setExpecteds(double[] _expecteds) {
        expecteds = _expecteds;
    }

    /**
     * The output expecteds
     */
    public double[] getExpecteds() {
        return expecteds;
    }

    /**
     * Set output colors to the object
     *
     * @param colors output colors
     */
    public void setColors(double[] _colors) {
        colors = _colors;
    }

    /**
     * The output colors
     */
    public double[] getColors() {
        return colors;
    }

    /**
     * Set output r2Levels to the object
     *
     * @param r2Levels output r2Levels
     */
    public void setR2Levels(double[] _r2Levels) {
        r2Levels = _r2Levels;
    }

    /**
     * The output r2Levels
     */
    public double[] getR2Levels() {
        return r2Levels;
    }

    /**
     * Set output switchFlags to the object
     *
     * @param switchFlags output switchFlags
     */
    public void setSwitchFlags(double[] _switchFlags) {
        switchFlags = _switchFlags;
    }

    /**
     * The output switchFlags
     */
    public double[] getSwitchFlags() {
        return switchFlags;
    }

    /**
     * Set output switch info to the object
     *
     * @param switchInfo output switch info
     */
    public void setSwitchInfo(String[] _switchInfo) {
        switchInfo = _switchInfo;
    }

    /**
     * The output switch info
     */
    public String[] getSwitchInfo() {
        return switchInfo;
    }

    public int getCountMaximumFractionDigits() {
        return countMaximumFractionDigits;
    }

    public void setCountMaximumFractionDigits(int x) {
        countMaximumFractionDigits = x;
    }

    public void setExpectedMaximumFractionDigits(int x) {
        expectedMaximumFractionDigits = x;
    }

    public void setLevelMaximumFractionDigits(int x) {
        levelMaximumFractionDigits = x;
    }

    public void setOutputValues(String key, double[] vals) {
        outputValues.put(key, vals);
    }

    public double[] getOutputValues(String key) {
        return outputValues.get(key);
    }

    public Set<String> getOutputValueKeys() {
        return outputValues.keySet();
    }
}
