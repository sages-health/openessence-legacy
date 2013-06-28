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

package edu.jhuapl.graphs.controller;

import edu.jhuapl.graphs.RenderedGraph;
import edu.jhuapl.graphs.jfreechart.JFreeChartGraphSource;
import edu.jhuapl.graphs.jfreechart.utils.HighResChartUtil;

import com.keypoint.PngEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;
import com.sun.media.jai.codec.TIFFField;
import com.sun.media.jai.codecimpl.TIFFImageEncoder;

import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D;
import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;

import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

public class GraphObject {

    private JFreeChartGraphSource graphSource;
    private RenderedGraph renderedGraph;
    private String imageFileName;
    private String imageMapName;
    private String imageMap;
    private String graphDataId;
    private int imageResolution = 500;
    private String dataSeriesJSON;
    private double yAxisMax;
    private double yAxisMin;

    public GraphObject(JFreeChartGraphSource graphSource, RenderedGraph renderedGraph,
                       String imageFileName, String imageMapName, String imageMap, String graphDataId) {
        super();
        this.graphSource = graphSource;
        this.renderedGraph = renderedGraph;
        this.imageFileName = imageFileName;
        this.imageMapName = imageMapName;
        this.imageMap = imageMap;
        this.graphDataId = graphDataId;
    }

    public JFreeChartGraphSource getGraphSource() {
        return graphSource;
    }

    public RenderedGraph getRenderedGraph() {
        return renderedGraph;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public String getImageMapName() {
        return imageMapName;
    }

    public String getImageMap() {
        return imageMap;
    }

    public String getGraphDataId() {
        return graphDataId;
    }

    public int getImageResolution() {
        return imageResolution;
    }

    public void setImageResolution(int imageResolution) {
        this.imageResolution = imageResolution;
    }

    public void writeChartAsPNG(OutputStream out, int graphWidth, int graphHeight)
            throws IOException {
        ChartUtilities.writeChartAsPNG(out, graphSource.getChart(), graphWidth, graphHeight);
    }

    public void writeChartAsHighResolutionPNG(OutputStream out, int graphWidth,
                                              int graphHeight) throws IOException {
        writeChartAsHighResolutionPNG(out, graphWidth, graphHeight, getImageResolution());
    }

    public void writeChartAsHighResolutionPNG(OutputStream out, int graphWidth,
                                              int graphHeight, int resolution) throws IOException {
        ChartPanel cp = new ChartPanel(graphSource.getChart());
        cp.setPreferredSize(new Dimension(graphWidth, graphHeight));
        cp.setSize(new Dimension(graphWidth, graphHeight));
        PngEncoder encoder = new PngEncoder(HighResChartUtil.getHighResChartImage(cp, resolution), false, 0, 9);
        encoder.setDpi(resolution, resolution);
        byte[] pngData = encoder.pngEncode();
        out.write(pngData);
    }

    public void writeChartAsHighResolutionTIFF(OutputStream out, String dir, int graphWidth,
                                               int graphHeight) throws IOException {
        writeChartAsHighResolutionTIFF(out, dir, graphWidth, graphHeight, getImageResolution());

    }

    public void writeChartAsHighResolutionTIFF(OutputStream out, String dir, int graphWidth,
                                               int graphHeight, int resolution) throws IOException {

        try {
            // Source file will be a png file
            String source = getImageFileName();
            if (!source.contains(".png")) {
                source = source + ".png";
            }

            // Create png file object
            File pngFile = new File(dir, source);

            System.out.println("Creating png file: " + pngFile.getAbsolutePath());

            // Write a high resolution PNG file
            BufferedOutputStream out1 = new BufferedOutputStream(new FileOutputStream(pngFile));
            writeChartAsHighResolutionPNG(out1, graphWidth, graphHeight, resolution);
            out1.close();

            // Set resolution for Tiff file
            TIFFEncodeParam param = new TIFFEncodeParam();
            // Create {X,Y}Resolution fields.
            TIFFField fieldXRes = new TIFFField(0x11A, TIFFField.TIFF_RATIONAL, 1,
                                                new long[][]{{resolution, 1}});
            TIFFField fieldYRes = new TIFFField(0x11B, TIFFField.TIFF_RATIONAL, 1,
                                                new long[][]{{resolution, 1}});
            param.setExtraFields(new TIFFField[]{fieldXRes, fieldYRes});

            // Encode a tiff file
            RenderedOp src = JAI.create("fileload", pngFile.getAbsolutePath());
            TIFFImageEncoder encoder = new TIFFImageEncoder(out, param);
            encoder.encode(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeChartAsEPS(OutputStream out, int graphWidth, int graphHeight) throws IOException {
        EPSDocumentGraphics2D g2d = new EPSDocumentGraphics2D(false);
        g2d.setGraphicContext(new GraphicContext());
        g2d.setupDocument(out, graphWidth, graphHeight);
        graphSource.getChart().draw(g2d, new Rectangle(graphWidth, graphHeight));
        g2d.finish();
    }

    public void writeChartAsEMF(OutputStream out, int graphWidth, int graphHeight) throws IOException {
        EMFGraphics2D g2d = new EMFGraphics2D(out, new Dimension(graphWidth, graphHeight));
        g2d.startExport();
        graphSource.getChart().draw((Graphics2D) g2d.create(), new Rectangle(graphWidth, graphHeight));
        g2d.endExport();
        g2d.closeStream();
    }

    public void setYAxisMin(double yAxisMin) {
        this.yAxisMin = yAxisMin;
    }

    public double getYAxisMin() {
        return yAxisMin;
    }

    public void setYAxisMax(double yAxisMax) {
        this.yAxisMax = yAxisMax;
    }

    public double getYAxisMax() {
        return yAxisMax;
    }

    public void setDataSeriesJSON(String dataSeriesJSON) {
        this.dataSeriesJSON = dataSeriesJSON;
    }

    public String getDataSeriesJSON() {
        return dataSeriesJSON;
    }
}
