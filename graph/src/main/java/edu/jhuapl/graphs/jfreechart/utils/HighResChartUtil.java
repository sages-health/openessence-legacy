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

package edu.jhuapl.graphs.jfreechart.utils;

import org.jfree.chart.ChartPanel;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class HighResChartUtil {

    private HighResChartUtil() {
    }

    /**
     * Returns a high resolution BufferedImage of the chart. Uses the default DPI_FILE_RESOLUTION.
     *
     * @return the buffered image.
     */
    public static BufferedImage getHighResChartImage(ChartPanel chartPanel) {
        return getHighResChartImage(chartPanel, 300);
    }

    /**
     * Returns a high resolution BufferedImage of the chart. Uses the default DPI_FILE_RESOLUTION.
     *
     * @param resolution The resolution, in dots per inch, of the image to generate.
     * @return the buffered image.
     */
    public static BufferedImage getHighResChartImage(ChartPanel chartPanel, int resolution) {
        int screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
        double scaleRatio = resolution / screenResolution;
        int width = chartPanel.getWidth();
        int height = chartPanel.getHeight();
        int rasterWidth = (int) (width * scaleRatio);
        int rasterHeight = (int) (height * scaleRatio);

        BufferedImage image = new BufferedImage(rasterWidth, rasterHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        g2.transform(AffineTransform.getScaleInstance(scaleRatio, scaleRatio));
        chartPanel.getChart().draw(g2, new Rectangle2D.Double(0, 0, width, height), null);
        g2.dispose();

        return image;
    }
}
