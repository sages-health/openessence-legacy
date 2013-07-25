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

import edu.jhuapl.graphs.DataPoint;
import edu.jhuapl.graphs.DataSeries;
import edu.jhuapl.graphs.Encoding;
import edu.jhuapl.graphs.GraphException;
import edu.jhuapl.graphs.GraphSource;
import edu.jhuapl.graphs.PointInterface;
import edu.jhuapl.graphs.RenderedGraph;
import edu.jhuapl.graphs.jfreechart.JFreeChartBarGraphSource;
import edu.jhuapl.graphs.jfreechart.JFreeChartCategoryGraphSource;
import edu.jhuapl.graphs.jfreechart.JFreeChartGraphSource;
import edu.jhuapl.graphs.jfreechart.utils.CustomLabelNumberAxis;
import edu.jhuapl.graphs.jfreechart.utils.SparselyLabeledCategoryAxis;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GraphController {

    private static final int defaultMaxLegendItems = 50;

    private static final int maxCategoryLabelWidthRatio = 15;
    private static final Color noDataColor = Color.GRAY;
    private static final Color warningDataColor = Color.YELLOW;
    private static final Color alertDataColor = Color.RED;
    private static final Color severeDataColor = Color.MAGENTA;

    private static final Font graphFont = new Font("Arial", Font.BOLD, 14);
    private static final Font rangeAxisFont = new Font("Arial", Font.BOLD, 12);
    private static final Font rangeAxisLabelFont = new Font("Arial", Font.BOLD, 12);
    private static final Font domainAxisFont = new Font("Arial", Font.PLAIN, 11);
    private static final Font domainAxisLabelFont = new Font("Arial", Font.BOLD, 12);
    private static final Font legendFont = new Font("Arial", Font.PLAIN, 11);

    private String graphDataId = null;
    private GraphDataHandlerInterface graphDataHandler = null;
    private int maxLegendItems = defaultMaxLegendItems;
    private Map<String, String> translationMap = new HashMap<String, String>(0);

    public GraphController(String graphDataId, GraphDataHandlerInterface graphDataHandler, String userId) {
        if (graphDataId != null && graphDataId.length() > 0) {
            this.graphDataId = graphDataId;
        } else {
            this.graphDataId = getUniqueId(userId);
        }

        this.graphDataHandler = graphDataHandler;
    }

    public Map<String, String> getTranslationMap() {
        if (translationMap == null) {
            translationMap = new HashMap<String, String>(0);
        }
        return translationMap;
    }

    /**
     * Set a key value map to be used for translations.
     */
    public void setTranslationMap(Map<String, String> translationMap) {
        this.translationMap = translationMap;
    }

    /**
     * Returns a translation of <code>word</code> if available.  Note: matches case.
     *
     * @return a translation from translationMap or <code>word</code>
     */
    protected String getTranslation(String word) {
        if (getTranslationMap() != null && getTranslationMap().containsKey(word)) {
            return getTranslationMap().get(word);
        }
        return word;
    }

    public String getGraphDataId() {
        return graphDataId;
    }

    /**
     * @param maxLegendItems Maximum number of items to display in the legend
     */
    public void setMaxLegendItems(int maxLegendItems) {
        this.maxLegendItems = maxLegendItems;
    }

    public GraphObject writeTimeSeriesGraph(PrintWriter out, GraphDataInterface graphData, boolean useImageMap,
                                            boolean includeFooter, String callBackURL, boolean graphExpected) {
        StringBuffer sb = new StringBuffer();
        GraphObject graph = writeTimeSeriesGraph(sb, graphData, useImageMap, includeFooter, callBackURL, graphExpected);
        out.println(sb);
        return graph;
    }

    public GraphObject writeTimeSeriesGraph(StringBuffer sb, GraphDataInterface graphData, boolean useImageMap,
                                            boolean includeFooter, String callBackURL, boolean graphExpected) {
        return writeTimeSeriesGraph(sb, graphData, useImageMap, includeFooter, true, callBackURL, graphExpected);
    }

    public GraphObject writeTimeSeriesGraph(StringBuffer sb, GraphDataInterface graphData, boolean useImageMap,
                                            boolean includeFooter, boolean includeButtons, String callBackURL,
                                            boolean graphExpected) {
        GraphObject graph = createTimeSeriesGraph(graphData, graphExpected);
        writeGraph(sb, graphData, useImageMap, includeFooter, includeButtons, callBackURL, null, null, graph);
        return graph;
    }

    public void dumpWriteTsGraph(PrintWriter out, GraphDataInterface graphData, boolean includeFooter,
                                 String callBackURL) {
        StringBuffer sb = new StringBuffer();
        dumpWriteTsGraph(sb, graphData, includeFooter, callBackURL);
        out.println(sb);
        return;
    }

    public void dumpWriteTsGraph(StringBuffer sb, GraphDataInterface graphData, boolean includeFooter,
                                 String callBackURL) {
        Map<String, Object> graphMetaData = dumpGraph(graphData);
        writeGraph(sb, graphData, includeFooter, callBackURL, "imageMap" + graphDataId, null, null, graphMetaData);
    }

    public void dumpWriteTsGraph(PrintWriter out, GraphDataInterface graphData, boolean includeFooter,
                                 String callBackURL, String graphURL) {
        StringBuffer sb = new StringBuffer();
        dumpWriteTsGraph(sb, graphData, includeFooter, callBackURL, graphURL);
        out.println(sb);
        return;
    }

    public void dumpWriteTsGraph(StringBuffer sb, GraphDataInterface graphData, boolean includeFooter,
                                 String callBackURL, String graphURL) {
        Map<String, Object> graphMetaData = dumpGraph(graphData);
        writeGraph(sb, graphData, includeFooter, graphURL, callBackURL, "imageMap" + graphDataId, null, null,
                   graphMetaData);
    }


    public GraphObject writePieGraph(PrintWriter out, GraphDataInterface graphData, boolean useImageMap,
                                     String callBackURL) {
        StringBuffer sb = new StringBuffer();
        GraphObject graph = writePieGraph(sb, graphData, useImageMap, callBackURL);
        out.println(sb);
        return graph;
    }

    public GraphObject writePieGraph(StringBuffer sb, GraphDataInterface graphData, boolean useImageMap,
                                     String callBackURL) {
        GraphObject graph = createPieGraph(graphData);
        writeGraph(sb, graphData, useImageMap, false, false, callBackURL, null, null, graph);
        return graph;
    }

    public GraphObject writeBarGraph(PrintWriter out, GraphDataInterface graphData, boolean stackGraph,
                                     boolean useImageMap, String callBackURL) {
        StringBuffer sb = new StringBuffer();
        GraphObject graph = writeBarGraph(sb, graphData, stackGraph, useImageMap, callBackURL);
        out.println(sb);
        return graph;
    }

    public GraphObject writeBarGraph(StringBuffer sb, GraphDataInterface graphData, boolean stackGraph,
                                     boolean useImageMap, String callBackURL) {
        GraphObject graph = createBarGraph(graphData, stackGraph);
        writeGraph(sb, graphData, useImageMap, false, false, callBackURL, null, null, graph);
        return graph;
    }

    public GraphObject writeSeverityGraph(PrintWriter out, GraphDataInterface graphData, boolean useImageMap,
                                          String callBackURL) {
        StringBuffer sb = new StringBuffer();
        GraphObject graph = writeSeverityGraph(sb, graphData, useImageMap, callBackURL);
        out.println(sb);
        return graph;
    }

    public GraphObject writeSeverityGraph(StringBuffer sb, GraphDataInterface graphData, boolean useImageMap,
                                          String callBackURL) {
        GraphObject graph = createSeverityGraph(graphData);
        writeGraph(sb, graphData, useImageMap, false, false, callBackURL, null, null, graph);
        return graph;
    }

    public GraphObject writeDualAxisGraph(PrintWriter out, GraphDataInterface graphData,
                                          boolean useImageMap, String callBackURL) {
        StringBuffer sb = new StringBuffer();
        GraphObject graph = writeDualAxisGraph(sb, graphData, useImageMap, callBackURL);
        out.println(sb);
        return graph;
    }

    public GraphObject writeDualAxisGraph(StringBuffer sb, GraphDataInterface graphData,
                                          boolean useImageMap, String callBackURL) {
        GraphObject graph = createDualAxisGraph(graphData);
        writeGraph(sb, graphData, useImageMap, false, false, callBackURL, null, null, graph);
        return graph;
    }

    public GraphObject createTimeSeriesGraph(GraphDataInterface graphData, boolean graphExpected) {
        return createTimeSeriesGraph(graphData, null, null, null, graphExpected);
    }

    public GraphObject createTimeSeriesGraph(GraphDataInterface graphData, Double yAxisMin, Double yAxisMax,
                                             String displayKey, boolean graphExpected) {
        List<DataSeries> dataSeries = new ArrayList<DataSeries>();
        LegendItemCollection legendItems = new LegendItemCollection();
        Map<String, Object> graphMetaData = new HashMap<String, Object>();
        double maxCount = setDataSeries(graphData, displayKey, false, dataSeries, legendItems, graphExpected);

        setTimeSeriesGraphMetaData(graphData, yAxisMin, yAxisMax, maxCount, graphMetaData);

        return getGraph(graphData, dataSeries, graphMetaData, legendItems, "tsgraph");
    }


    public GraphObject createBarGraph(GraphDataInterface graphData, boolean stackGraph) {
        return createBarGraph(graphData, stackGraph, false);
    }

    /**
     * The useItemColor is still under development to allow each bar to be a different color.
     */
    public GraphObject createBarGraph(GraphDataInterface graphData, boolean stackGraph, boolean useItemColor) {
        List<DataSeries> dataSeries = new ArrayList<DataSeries>();
        LegendItemCollection legendItems = new LegendItemCollection();
        Map<String, Object> graphMetaData = new HashMap<String, Object>();

        setDataSeries(graphData, null, false, dataSeries, legendItems, useItemColor);
        setBarGraphMetaData(graphData, stackGraph, graphMetaData);

        return getGraph(graphData, dataSeries, graphMetaData, legendItems, "bargraph");
    }

    public GraphObject createPieGraph(GraphDataInterface graphData) {
        return createPieGraph(graphData, Encoding.PNG);
    }

    public GraphObject createPieGraph(GraphDataInterface graphData, Encoding encoding) {
        GraphObject graph = null;

        Map<String, Object> graphMetaData = new HashMap<String, Object>();
        List<PointInterface> points = new ArrayList<PointInterface>();
        setPieGraphMetaData(graphData, graphMetaData, points); // I'm ashamed of this code in so many ways
        String graphTitle = (String) graphMetaData.get(GraphSource.GRAPH_TITLE);

        try {
            // add the created chart properties
            JFreeChartGraphSource graphSource = new JFreeChartGraphSource();
            graphSource.setData(Arrays.asList(new DataSeries(points, new HashMap<String, Object>())));
            graphSource.setParams(graphMetaData);
            graphSource.initialize();

            if (graphData.showLegend()) {
                PiePlot plot = (PiePlot) graphSource.getChart().getPlot();
                // use rectangles as the legend shapes
                plot.setLegendItemShape(new Rectangle(7, 8));
                // generate tooltip for the legend items in the following format: "lineSetLabels - count"
                if (graphData.percentBased()) {
                    plot.setLegendLabelToolTipGenerator(new StandardPieSectionLabelGenerator("{0} - {1} ({2})",
                                                                                             new DecimalFormat("#.##"),
                                                                                             new DecimalFormat(
                                                                                                     "#.##%")));
                } else {
                    plot.setLegendLabelToolTipGenerator(new StandardPieSectionLabelGenerator("{0} - {1} ({2})",
                                                                                             new DecimalFormat("#"),
                                                                                             new DecimalFormat(
                                                                                                     "#.##%")));
                }
            }

            // render the graph to get the image map
            RenderedGraph
                    renderedGraph =
                    graphSource.renderGraph(graphData.getGraphWidth(), graphData.getGraphHeight(), encoding);
            String extension = ".dat";
            switch (encoding) {
                case JPEG:
                    extension = ".jpg";
                    break;
                case PNG:
                case PNG_WITH_TRANSPARENCY:
                    extension = ".png";
                    break;
            }

            String imageFileName = getCleanValue(graphTitle) + "_piegraph" + extension;
            // get the image map
            String imageMapName = "imageMap" + graphDataId;
            String imageMap = appendImageMapTarget(renderedGraph.getImageMap(imageMapName),
                                                   graphData.getLineSetURLTarget());

            try {
                // store away the graph data file
                graphDataHandler.putGraphData(graphData, graphDataId);
                graph = new GraphObject(graphSource, renderedGraph, imageFileName, imageMapName, imageMap, graphDataId);
            } catch (GraphException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } catch (GraphException e) {
            System.out.println("Could not create pie graph " + graphTitle);
            e.printStackTrace();
        }

        return graph;
    }

    public GraphObject createSeverityGraph(GraphDataInterface graphData) {
        List<DataSeries> dataSeries = new ArrayList<DataSeries>();
        Map<String, Object> graphMetaData = new HashMap<String, Object>();
        LegendItemCollection legendItems = new LegendItemCollection();
        Color[] graphBaseColors = graphData.getGraphBaseColors();
        Map<Double, String> tickValueToLabelMapping = new HashMap<Double, String>();

        setDataSeries(graphData, null, true, dataSeries, null, false);
        setBarGraphMetaData(graphData, true, graphMetaData);

        // create a custom legend
        legendItems.add(new LegendItem(getTranslation("Severe"), severeDataColor));
        legendItems.add(new LegendItem(getTranslation("Not Severe"), graphBaseColors[0]));
        legendItems.add(new LegendItem(getTranslation("No Data Available"), noDataColor));

        // create custom value axis labels
        String[] lineSetLabels = graphData.getLineSetLabels();
        double lowestTickValue = 0.5;
        int tickCount = lineSetLabels.length;

        for (int i = 0; i < tickCount; i++) {
            tickValueToLabelMapping.put(lowestTickValue + i, lineSetLabels[i]);
        }

        CustomLabelNumberAxis rangeAxis = new CustomLabelNumberAxis(tickValueToLabelMapping);
        rangeAxis.setLowestTickValue(lowestTickValue);
        rangeAxis.setTickCount(tickCount);
        graphMetaData.put(JFreeChartCategoryGraphSource.RANGE_AXIS, rangeAxis);

        return getGraph(graphData, dataSeries, graphMetaData, legendItems, "tsgraph");
    }

    public GraphObject createDualAxisGraph(GraphDataInterface graphData) {
        List<DataSeries> dataSeries = new ArrayList<DataSeries>();
        LegendItemCollection legendItems = new LegendItemCollection();
        Map<String, Object> graphMetaData = new HashMap<String, Object>();
        double maxCount = setDataSeries(graphData, null, false, dataSeries, legendItems, false);

        setTimeSeriesGraphMetaData(graphData, null, null, maxCount, graphMetaData);
        setBarGraphMetaData(graphData, false, graphMetaData);
        graphMetaData.put(GraphSource.GRAPH_TYPE, GraphSource.GRAPH_TYPE_DUAL_AXIS);

        return getGraph(graphData, dataSeries, graphMetaData, legendItems, "dualAxisGraph");
    }

    public static String zipGraphs(List<GraphObject> graphs, String tempDir, String userId) throws GraphException {
        if (graphs != null && graphs.size() > 0) {
            byte[] byteBuffer = new byte[1024];
            String zipFileName = getUniqueId(userId) + ".zip";

            try {
                File zipFile = new File(tempDir, zipFileName);
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
                boolean hasGraphs = false;

                for (GraphObject graph : graphs) {
                    if (graph != null) {
                        byte[] renderedGraph = graph.getRenderedGraph().getData();
                        ByteArrayInputStream in = new ByteArrayInputStream(renderedGraph);
                        int len;

                        zos.putNextEntry(new ZipEntry(graph.getImageFileName()));

                        while ((len = in.read(byteBuffer)) > 0) {
                            zos.write(byteBuffer, 0, len);
                        }

                        in.close();
                        zos.closeEntry();
                        hasGraphs = true;
                    }
                }

                zos.close();

                if (hasGraphs) {
                    return zipFileName;
                } else {
                    return null;
                }
            } catch (IOException e) {
                throw new GraphException("Could not write zip", e);
            }
        }

        return null;
    }

    private void writeSimpleLinkTSGraph(StringBuffer sb, GraphDataInterface graphData, String callbackURL,
                                        String linkURL, GraphObject graph) {
        int graphWidth = graphData.getGraphWidth();
        int graphHeight = graphData.getGraphHeight();
        sb.append("<div style=\"background-color:white; width:" + graphWidth + "px; height:" + graphHeight +
                  "px;\">\n");
        sb.append("<a href=\"").append(linkURL).append("\">");

        String graphURL = callbackURL + "?graphDataId=" + graphDataId;
        ;
        sb.append("<img src=\"").append(graphURL).append("\" title=\"").append(graphData.getGraphTitle())
                .append("\" border=\"0\"/></a>").append("</div>\n");

    }

    private void writeGraph(StringBuffer sb, GraphDataInterface graphData, boolean useImageMap, boolean includeFooter,
                            String callBackURL, String graphOptionsHelpLink, String downloadHelpLink,
                            GraphObject graph) {
        writeGraph(sb, graphData, useImageMap, includeFooter, true, callBackURL, graphOptionsHelpLink, downloadHelpLink,
                   graph);
    }

    private void writeGraph(StringBuffer sb, GraphDataInterface graphData, boolean useImageMap, boolean includeFooter,
                            boolean includeButtons,
                            String callBackURL, String graphOptionsHelpLink, String downloadHelpLink,
                            GraphObject graph) {
        int graphWidth = graphData.getGraphWidth();
        int graphHeight = graphData.getGraphHeight();
        String imageMapName = graph.getImageMapName();
        String graphURL = callBackURL + (callBackURL.indexOf('?') > -1 ? "&" : "?")
                          + "graphDataId=" + graphDataId;

        sb.append("<div style=\"background-color:white; width:" + graphWidth + "px\">\n");
        sb.append("<div id=\"graphDiv" + imageMapName + "\" style=\"width:" + graphWidth + "px; height:" + graphHeight +
                  "px;\">\n");

        if (useImageMap) {
            // write the graph with an image map
            sb.append(graph.getImageMap());
            sb.append("<img src=\"" + graphURL + "\" usemap=\"#" + imageMapName + "\" border=\"0\"/>\n");
        } else {
            // just write the graph with the option to turn it into an interactive graph
            sb.append("<img src=\"" + graphURL +
                      "\" title=\"Switch to Interactive View\" onclick=\"makeInteractiveGraph('" + callBackURL + "', '"
                      +
                      imageMapName + "', '" + graphDataId + "');\" border=\"0\"/>\n");
        }

        sb.append("</div>\n");

        JFreeChart chart = graph.getGraphSource().getChart();
        String graphTitle = chart.getTitle().getText();

        String xAxisLabel = "";
        String yAxisLabel = "";
        double yAxisMin = 0;
        double yAxisMax = 0;

        if (chart.getPlot() instanceof CategoryPlot) {
            CategoryPlot plot = chart.getCategoryPlot();
            xAxisLabel = plot.getDomainAxis().getLabel();
            yAxisLabel = plot.getRangeAxis().getLabel();
            // get the y-axis minimum and maximum range
            yAxisMin = plot.getRangeAxis().getRange().getLowerBound();
            yAxisMax = plot.getRangeAxis().getRange().getUpperBound();
            // get the y-axis minimum and maximum range
            graph.setYAxisMin(yAxisMin);
            graph.setYAxisMax(yAxisMax);
            //set series information for configuration
            graph.setDataSeriesJSON(getDataSeriesJSON(graphData.getLineSetLabels(), graphData.displayAlerts(),
                                                      graphData.displaySeverityAlerts(), "\""));
        }

        //added option to not bring back the button area - cjh
        if (includeButtons) {
            // write the footer
            sb.append("<div style=\"padding:5px; text-align:center;\">\n");
            if (includeFooter) {
                //this keys it to be a timeseries graph so we can do getCategoryPlot
                String dataSeriesJSON = getDataSeriesJSON(graphData.getLineSetLabels(), graphData.displayAlerts(),
                                                          graphData.displaySeverityAlerts(), "&quot;");

                sb.append(
                        "<input type=\"button\" style=\"font-family:Arial; font-size:0.6em;\" value=\"Graph Options\" onclick=\"showTimeSeriesGraphOptions('"
                        +
                        callBackURL + "', '" + imageMapName + "', '" + graphDataId + "', '" + graphTitle + "', '" +
                        xAxisLabel + "', '" + yAxisLabel + "', " + yAxisMin + ", " + yAxisMax + ", '" + dataSeriesJSON +
                        "');\"/>");
                if (graphOptionsHelpLink != null) {
                    sb.append(graphOptionsHelpLink);
                }
            }

            sb.append(
                    "<input type=\"button\" style=\"font-family:Arial; font-size:0.6em;\" value=\"Download\" onclick=\"showDownloadOptions('"
                    +
                    callBackURL + "', '" + imageMapName + "', '" + graphDataId + "');\"/>");

            if (downloadHelpLink != null) {
                sb.append(downloadHelpLink);
            }
            sb.append("</div>\n");
        }
        sb.append("</div>\n");

        if (!useImageMap) {
            sb.append("<div id=\"linkDiv" + imageMapName +
                      "\" style=\"font-family: Arial; font-size: 0.7em; text-align: right\">\n");
            sb.append("*Click anywhere on the graph to <a href=\"#\" onclick=\"makeInteractiveGraph('" + callBackURL +
                      "', '" + imageMapName + "', '" + graphDataId +
                      "'); return false;\">switch to interactive view</a>\n");
            sb.append("</div>\n");
        }
    }

    private void writeGraph(StringBuffer sb, GraphDataInterface graphData, boolean includeFooter,
                            String callBackURL, String imageMapName, String graphOptionsHelpLink,
                            String downloadHelpLink, Map<String, Object> graphMetaData) {
        writeGraph(sb, graphData, includeFooter, callBackURL + "?graphDataId=" + graphDataId, callBackURL, imageMapName,
                   graphOptionsHelpLink, downloadHelpLink, graphMetaData);
    }

    public void writeGraph(StringBuffer sb, GraphDataInterface graphData, boolean includeFooter,
                           String graphURL, String callBackURL, String imageMapName, String graphOptionsHelpLink,
                           String downloadHelpLink, Map<String, Object> graphMetaData) {

        int graphWidth = graphData.getGraphWidth();
        int graphHeight = graphData.getGraphHeight();

        sb.append("<div style=\"background-color:white; width:" + graphWidth + "px\">\n");
        sb.append("<div id=\"graphDiv" + imageMapName + "\" style=\"width:" + graphWidth + "px; height:" + graphHeight +
                  "px;\">\n");

        // just write the graph with the option to turn it into an interactive graph
        sb.append("<img src=\"" + graphURL +
                  "\" title=\"Switch to Interactive View\" onclick=\"makeInteractiveGraph('" + callBackURL + "', '" +
                  imageMapName + "', '" + graphDataId + "');\" border=\"0\"/>\n");

        sb.append("</div>\n");
        // write the footer
        sb.append("<div style=\"padding:5px; text-align:center;\">\n");

        if (includeFooter) {
            String graphTitle = (String) graphMetaData.get(GraphSource.GRAPH_TITLE);
            String xAxisLabel = (String) graphMetaData.get(GraphSource.GRAPH_X_LABEL);
            String yAxisLabel = (String) graphMetaData.get(GraphSource.GRAPH_Y_LABEL);
            String dataSeriesJSON = getDataSeriesJSON(graphData.getLineSetLabels(), graphData.displayAlerts(),
                                                      graphData.displaySeverityAlerts(), "&quot;");
            // get the y-axis minimum and maximum range
            double yAxisMin = (Double) graphMetaData.get(GraphSource.GRAPH_RANGE_LOWER_BOUND);
            double yAxisMax = (Double) graphMetaData.get(GraphSource.GRAPH_RANGE_UPPER_BOUND);

            sb.append(
                    "<input type=\"button\" style=\"font-family:Arial; font-size:0.6em;\" value=\"Graph Options\" onclick=\"showTimeSeriesGraphOptions('"
                    +
                    callBackURL + "', '" + imageMapName + "', '" + graphDataId + "', '" + graphTitle + "', '" +
                    xAxisLabel + "', '" + yAxisLabel + "', " + yAxisMin + ", " + yAxisMax + ", '" + dataSeriesJSON +
                    "');\"/>");

            if (graphOptionsHelpLink != null) {
                sb.append(graphOptionsHelpLink);
            }
        }

        sb.append(
                "<input type=\"button\" style=\"font-family:Arial; font-size:0.6em;\" value=\"Download\" onclick=\"showDownloadOptions('"
                +
                callBackURL + "', '" + imageMapName + "', '" + graphDataId + "');\"/>");

        if (downloadHelpLink != null) {
            sb.append(downloadHelpLink);
        }

        sb.append("</div>\n");
        sb.append("</div>\n");

        sb.append("<div id=\"linkDiv" + imageMapName +
                  "\" style=\"font-family: Arial; font-size: 0.7em; text-align: right\">\n");
        sb.append("*Click anywhere on the graph to <a href=\"#\" onclick=\"makeInteractiveGraph('" + callBackURL +
                  "', '" + imageMapName + "', '" + graphDataId +
                  "'); return false;\">switch to interactive view</a>\n");
        sb.append("</div>\n");
    }


    private double setDataSeries(GraphDataInterface graphData, String displayKey, boolean useNoDataColor,
                                 List<DataSeries> dataSeries, LegendItemCollection legendItems, boolean graphExpected) {
        return setDataSeries(graphData, displayKey, useNoDataColor, dataSeries, legendItems, false, graphExpected);
    }

    /**
     * The useItemColor is still under development to allow each bar to be a different color.
     */
    private double setDataSeries(GraphDataInterface graphData, String displayKey, boolean useNoDataColor,
                                 List<DataSeries> dataSeries, LegendItemCollection legendItems, boolean useItemColor,
                                 boolean graphExpected) {
        double[][] counts = graphData.getCounts();
        double[][] expecteds = graphData.getExpecteds();
        int[][] colors = graphData.getColors();
        String[][] altTexts = graphData.getAltTexts();
        String[][] lineSetURLs = graphData.getLineSetURLs();
        String[] xLabels = graphData.getXLabels();
        String[] lineSetLabels = graphData.getLineSetLabels();
        boolean[] displayAlerts = graphData.displayAlerts();
        boolean[] displaySeverityAlerts = graphData.displaySeverityAlerts();
        double[] lineSymbolSizes = graphData.getLineSymbolSizes();
        Color[] graphBaseColors = graphData.getGraphBaseColors();
        int displayKeyIndex = 0;
        double maxCount = 0;
        boolean singleAlertLegend = graphData.getShowSingleAlertLegend();
        boolean singleSeverityLegend = graphData.getShowSingleSeverityLegend();

        for (int i = 0; i < counts.length; i++) {
            String lineSetLabel = "series" + (i + 1);
            boolean displayAlert = false;
            boolean displaySeverityAlert = false;
            double lineSymbolSize = 7.0;
            try {
                lineSetLabel = lineSetLabels[i];
            } catch (Exception e) {
            }
            try {
                displayAlert = displayAlerts[i];
            } catch (Exception e) {
            }
            try {
                displaySeverityAlert = displaySeverityAlerts[i];
            } catch (Exception e) {
            }
            try {
                lineSymbolSize = lineSymbolSizes[i];
            } catch (Exception e) {
            }

            double xy = lineSymbolSize / 2.0 * -1;
            Color seriesColor = graphBaseColors[i % graphBaseColors.length];
            boolean displayNormalData = displaySeries(displayKey, displayKeyIndex++) ? true : false;
            boolean displayWarningData = displayAlert && displaySeries(displayKey, displayKeyIndex++) ? true : false;
            boolean displayAlertData = displayAlert && displaySeries(displayKey, displayKeyIndex++) ? true : false;
            boolean displaySevereData = displaySeverityAlert &&
                                        displaySeries(displayKey, displayKeyIndex++) ? true : false;
            List<DataPoint> points = new ArrayList<DataPoint>();
            List<DataPoint> epoints = new ArrayList<DataPoint>();

            /** get graph data */

            for (int j = 0; j < counts[i].length; j++) {
                boolean alertDataExists = false;
                String altText = null;
                String lineSetURL = null;
                int color = 1;
                try {
                    altText = altTexts[i][j];
                } catch (Exception e) {
                }
                try {
                    lineSetURL = lineSetURLs[i][j];
                } catch (Exception e) {
                }
                try {
                    color = colors[i][j];
                } catch (Exception e) {
                }

                Map<String, Object> epointMetaData = new HashMap<String, Object>();
                Map<String, Object> pointMetaData = new HashMap<String, Object>();
                pointMetaData.put(GraphSource.ITEM_TOOL_TIP, altText);
                pointMetaData.put(GraphSource.ITEM_URL, lineSetURL);
                pointMetaData.put(GraphSource.ITEM_SHAPE, new Ellipse2D.Double(xy, xy, lineSymbolSize, lineSymbolSize));

                pointMetaData.put(GraphSource.ITEM_COLOR, seriesColor);

                epointMetaData.put(GraphSource.ITEM_COLOR, seriesColor.brighter());
                epointMetaData.put(GraphSource.ITEM_SHAPE, new Rectangle2D.Double(xy, xy, 7, 7));
                epointMetaData.put(GraphSource.ITEM_TOOL_TIP, altText);
                // color 0 = GRAY (no data), color 2 = YELLOW (warning), 3 = RED (alert),
                // 4 = PURPLE (severe)
                if (useNoDataColor && color == 0) {
                    pointMetaData.put(GraphSource.ITEM_COLOR, noDataColor);
                } else if (displayWarningData && color == 2) {
                    alertDataExists = true;
                    pointMetaData.put(GraphSource.ITEM_COLOR, warningDataColor);
                } else if (displayAlertData && color == 3) {
                    alertDataExists = true;
                    pointMetaData.put(GraphSource.ITEM_COLOR, alertDataColor);
                } else if (displaySevereData && color == 4) {
                    alertDataExists = true;
                    pointMetaData.put(GraphSource.ITEM_COLOR, severeDataColor);
                }

                if (useItemColor) {
                    seriesColor = graphBaseColors[j % graphBaseColors.length];
                    pointMetaData.put(GraphSource.ITEM_COLOR, seriesColor);
                }

                if (displayNormalData || alertDataExists) {
                    // only update the maxCount if this data point is visible
                    if (counts[i][j] > maxCount) {
                        maxCount = counts[i][j];
                    }
                } else {
                    // if normal data is supposed to be hidden and no alert data exists, then hide this
                    // data point
                    pointMetaData.put(GraphSource.ITEM_VISIBLE, false);
                    epointMetaData.put(GraphSource.ITEM_VISIBLE, false);
                }

                // if the data is set to the Double.MIN_VALUE, then add it as a null.
                if (counts[i][j] != Double.MIN_VALUE) {
                    points.add(new DataPoint(counts[i][j], xLabels[j], pointMetaData));
                } else {
                    points.add(new DataPoint(null, xLabels[j], pointMetaData));
                }
                if (expecteds[i][j] != Double.MIN_VALUE) {
                    epoints.add(new DataPoint(expecteds[i][j], xLabels[j], epointMetaData));
                } else {
                    epoints.add(new DataPoint(null, xLabels[j], epointMetaData));
                }

            }

            /** add the series */

            // series properties
            Map<String, Object> dataSeriesMetaData = new HashMap<String, Object>();
            dataSeriesMetaData.put(GraphSource.SERIES_TITLE, lineSetLabel);
            dataSeriesMetaData.put(GraphSource.SERIES_COLOR, seriesColor);
            // if normal data is hidden for this series, hide the series connector line
            dataSeriesMetaData.put(GraphSource.SERIES_LINES_VISIBLE, displayNormalData);
            dataSeries.add(new DataSeries(points, dataSeriesMetaData));

            Map<String, Object> eDataSeriesMetaData = new HashMap<String, Object>();
            eDataSeriesMetaData.put(GraphSource.SERIES_COLOR, seriesColor);
            eDataSeriesMetaData.put(GraphSource.SERIES_STROKE, new BasicStroke(
                    1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1.0f, new float[]{10.0f, 6.0f}, 0.0f));
            if (graphExpected) {
                dataSeries.add(new DataSeries(epoints, eDataSeriesMetaData));
            }
            // code to set the text in the legend
            if (legendItems != null) {
                if (displayNormalData && legendItems.getItemCount() < maxLegendItems) {
                    if (displayAlert && !singleAlertLegend) {
                        legendItems.add(new LegendItem(lineSetLabel + ": " + getTranslation("Normal"), seriesColor));
                    }
                    if (graphExpected) {
                        legendItems.add(new LegendItem(lineSetLabel, seriesColor));
                        legendItems.add(new LegendItem(("Expected " + lineSetLabel), seriesColor));
                    } else {
                        legendItems.add(new LegendItem(lineSetLabel, seriesColor));
                    }
                }
                if (!singleAlertLegend && displayWarningData && legendItems.getItemCount() < maxLegendItems) {
                    legendItems.add(new LegendItem(lineSetLabel + ": " + getTranslation("Warning"), warningDataColor));
                }
                if (!singleAlertLegend && displayAlertData && legendItems.getItemCount() < maxLegendItems) {
                    legendItems.add(new LegendItem(lineSetLabel + ": " + getTranslation("Alert"), alertDataColor));
                }
                if (!singleSeverityLegend && displaySevereData && legendItems.getItemCount() < maxLegendItems) {
                    legendItems.add(new LegendItem(lineSetLabel + ": " + getTranslation("Severe"), severeDataColor));
                }
            }
        }

        if (singleAlertLegend && legendItems.getItemCount() < maxLegendItems) {
            legendItems.add(new LegendItem(getTranslation("Warning"), warningDataColor));
            legendItems.add(new LegendItem(getTranslation("Alert"), alertDataColor));
        }
        if (singleSeverityLegend && legendItems.getItemCount() < maxLegendItems) {
            legendItems.add(new LegendItem(getTranslation("Severe"), severeDataColor));
        }
        return maxCount;
    }


    public void setTimeSeriesGraphMetaData(GraphDataInterface graphData, double maxCount,
                                           Map<String, Object> graphMetaData) {
        setTimeSeriesGraphMetaData(graphData, null, null, maxCount, graphMetaData);
    }


    public void setTimeSeriesGraphMetaData(GraphDataInterface graphData, Double yAxisMin, Double yAxisMax,
                                           double maxCount, Map<String, Object> graphMetaData) {
        String graphTitle = graphData.getGraphTitle() != null ? graphData.getGraphTitle() : "";
        String xAxisLabel = graphData.getXAxisLabel() != null ? graphData.getXAxisLabel() : "";
        String yAxisLabel = graphData.getYAxisLabel() != null ? graphData.getYAxisLabel() : "";
        int graphWidth = graphData.getGraphWidth();
        int graphHeight = graphData.getGraphHeight();
        boolean percentBased = graphData.percentBased();
        int maxLabeledCategoryTicks = graphData.getMaxLabeledCategoryTicks();

        graphMetaData.put(GraphSource.GRAPH_TYPE, GraphSource.GRAPH_TYPE_LINE);
        graphMetaData.put(GraphSource.BACKGROUND_COLOR, Color.WHITE);
        graphMetaData.put(GraphSource.GRAPH_TITLE, graphTitle);
        graphMetaData.put(GraphSource.GRAPH_FONT, graphFont);
        graphMetaData.put(GraphSource.GRAPH_MINOR_TICKS, 0);
        graphMetaData.put(GraphSource.GRAPH_LEGEND, graphData.showLegend());
        graphMetaData.put(GraphSource.LEGEND_FONT, legendFont);

        SparselyLabeledCategoryAxis domainAxis;

        if (graphWidth >= 500 && graphHeight >= 300) {
            // this is a larger graph so we can add some additional properties to pretty it up
            graphMetaData.put(GraphSource.GRAPH_BORDER, true);
            graphMetaData.put(GraphSource.AXIS_OFFSET, 5.0);
            graphMetaData.put(GraphSource.GRAPH_RANGE_GRIDLINE_PAINT, Color.lightGray);

            if (yAxisLabel == null && percentBased) {
                yAxisLabel = getTranslation("Percent");
            } else if (yAxisLabel == null) {
                yAxisLabel = getTranslation("Counts");
            }

            if (xAxisLabel == null) {
                xAxisLabel = getTranslation("Date");
            }

            domainAxis = new SparselyLabeledCategoryAxis(maxLabeledCategoryTicks, Color.lightGray);
        } else {
            yAxisLabel = "";
            xAxisLabel = "";
            domainAxis = new SparselyLabeledCategoryAxis(maxLabeledCategoryTicks);
        }

        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createDownRotationLabelPositions(45));
        domainAxis.setMaximumCategoryLabelWidthRatio(maxCategoryLabelWidthRatio);
        graphMetaData.put(JFreeChartCategoryGraphSource.DOMAIN_AXIS, domainAxis);

        graphMetaData.put(GraphSource.GRAPH_RANGE_INTEGER_TICK, !percentBased);
        graphMetaData.put(GraphSource.GRAPH_RANGE_MINOR_TICK_VISIBLE, false);

        if (yAxisMin != null && yAxisMax != null) {
            graphMetaData.put(GraphSource.GRAPH_RANGE_LOWER_BOUND, yAxisMin);
            graphMetaData.put(GraphSource.GRAPH_RANGE_UPPER_BOUND, yAxisMax);
        } else {
            graphMetaData.put(GraphSource.GRAPH_RANGE_LOWER_BOUND, 0.0);

            if (maxCount == 0) {
                // if there is no data, set the upper bound to 1.0, otherwise we
                // get a weird looking y-axis
                graphMetaData.put(GraphSource.GRAPH_RANGE_UPPER_BOUND, 1.0);
            }
            // if the maxCount is less than 1, Y-Axis labels are not displayed.
            // Found during testing % data that may be 1%
            else if (maxCount < 1) {
                graphMetaData.put(GraphSource.GRAPH_RANGE_INTEGER_TICK, false);
                graphMetaData.put(GraphSource.GRAPH_RANGE_UPPER_BOUND, maxCount * 1.05);
            } else {
                graphMetaData.put(GraphSource.GRAPH_RANGE_UPPER_BOUND, maxCount * 1.05);
            }
        }

        graphMetaData.put(GraphSource.GRAPH_Y_LABEL, yAxisLabel);
        graphMetaData.put(GraphSource.GRAPH_Y_AXIS_FONT, rangeAxisFont);
        graphMetaData.put(GraphSource.GRAPH_Y_AXIS_LABEL_FONT, rangeAxisLabelFont);
        graphMetaData.put(GraphSource.GRAPH_X_LABEL, xAxisLabel);
        graphMetaData.put(GraphSource.GRAPH_X_AXIS_FONT, domainAxisFont);
        graphMetaData.put(GraphSource.GRAPH_X_AXIS_LABEL_FONT, domainAxisLabelFont);
    }

    public void setBarGraphMetaData(GraphDataInterface graphData, boolean stackGraph,
                                    Map<String, Object> graphMetaData) {
        String graphTitle = graphData.getGraphTitle() != null ? graphData.getGraphTitle() : "";
        String xAxisLabel = graphData.getXAxisLabel() != null ? graphData.getXAxisLabel() : "";
        String yAxisLabel = graphData.getYAxisLabel() != null ? graphData.getYAxisLabel() : "";
        int graphWidth = graphData.getGraphWidth();
        int graphHeight = graphData.getGraphHeight();
        boolean percentBased = graphData.percentBased();
        boolean plotHorizontal = graphData.plotHorizontal();
        int maxLabeledCategoryTicks = graphData.getMaxLabeledCategoryTicks();

        if (stackGraph) {
            graphMetaData.put(GraphSource.GRAPH_TYPE, GraphSource.GRAPH_TYPE_STACKED_BAR);
        } else {
            graphMetaData.put(GraphSource.GRAPH_TYPE, GraphSource.GRAPH_TYPE_BAR);
        }

        if (plotHorizontal) {
            graphMetaData.put(JFreeChartBarGraphSource.PLOT_ORIENTATION, PlotOrientation.HORIZONTAL);
        }

        graphMetaData.put(GraphSource.GRAPH_LABEL_BACKGROUND_COLOR, Color.WHITE);
        graphMetaData.put(GraphSource.BACKGROUND_COLOR, Color.WHITE);
        graphMetaData.put(GraphSource.GRAPH_TITLE, graphTitle);
        graphMetaData.put(GraphSource.GRAPH_FONT, graphFont);
        graphMetaData.put(GraphSource.GRAPH_BORDER, false);
        graphMetaData.put(GraphSource.GRAPH_LEGEND, graphData.showLegend());
        graphMetaData.put(GraphSource.LEGEND_FONT, legendFont);

        SparselyLabeledCategoryAxis domainAxis;

        if (graphWidth >= 500 && graphHeight >= 300) {
            // this is a larger graph so we can add some additional properties to pretty it up
            graphMetaData.put(GraphSource.GRAPH_BORDER, true);
            graphMetaData.put(GraphSource.AXIS_OFFSET, 5.0);
            graphMetaData.put(GraphSource.GRAPH_RANGE_GRIDLINE_PAINT, Color.lightGray);

            domainAxis = new SparselyLabeledCategoryAxis(maxLabeledCategoryTicks, Color.lightGray);
        } else {
            domainAxis = new SparselyLabeledCategoryAxis(maxLabeledCategoryTicks);
        }

        if (!plotHorizontal) {
            // don't rotate labels if this is a horizontal graph
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createDownRotationLabelPositions(45));
        }

        domainAxis.setMaximumCategoryLabelWidthRatio(maxCategoryLabelWidthRatio);
        graphMetaData.put(JFreeChartCategoryGraphSource.DOMAIN_AXIS, domainAxis);

        graphMetaData.put(GraphSource.GRAPH_RANGE_AXIS_LOCATION, AxisLocation.BOTTOM_OR_LEFT);
        graphMetaData.put(GraphSource.GRAPH_RANGE_INTEGER_TICK, !percentBased);
        graphMetaData.put(GraphSource.GRAPH_RANGE_MINOR_TICK_VISIBLE, false);
        graphMetaData.put(GraphSource.GRAPH_Y_LABEL, yAxisLabel);
        graphMetaData.put(GraphSource.GRAPH_Y_AXIS_FONT, rangeAxisFont);
        graphMetaData.put(GraphSource.GRAPH_Y_AXIS_LABEL_FONT, rangeAxisLabelFont);
        graphMetaData.put(GraphSource.GRAPH_X_LABEL, xAxisLabel);
        graphMetaData.put(GraphSource.GRAPH_X_AXIS_FONT, domainAxisFont);
        graphMetaData.put(GraphSource.GRAPH_X_AXIS_LABEL_FONT, domainAxisLabelFont);
    }

    public void setPieGraphMetaData(GraphDataInterface graphData, Map<String, Object> graphMetaData,
                                    List<PointInterface> points) {
        double[][] counts = graphData.getCounts();
        String[][] altTexts = graphData.getAltTexts();
        String[][] lineSetURLs = graphData.getLineSetURLs();
        String[] lineSetLabels = graphData.getLineSetLabels();
        String graphTitle = graphData.getGraphTitle() != null ? graphData.getGraphTitle() : "";
        String graphNoDataMessage = graphData.getNoDataMessage();
        Color[] graphBaseColors = graphData.getGraphBaseColors();
        boolean graphDisplayLabel = graphData.getShowGraphLabels();

        for (int i = 0; i < counts.length; i++) {
            String lineSetLabel = "series" + (i + 1);
            String altText = null;
            String lineSetURL = null;
            try {
                lineSetLabel = lineSetLabels[i];
            } catch (Exception e) {
            }
            try {
                altText = altTexts[i][0];
            } catch (Exception e) {
            }
            try {
                lineSetURL = lineSetURLs[i][0];
            } catch (Exception e) {
            }

            Map<String, Object> pointMetaData = new HashMap<String, Object>();
            pointMetaData.put(GraphSource.ITEM_TOOL_TIP, altText);
            pointMetaData.put(GraphSource.ITEM_URL, lineSetURL);
            pointMetaData.put(GraphSource.ITEM_COLOR, graphBaseColors[i % graphBaseColors.length]);
            points.add(new DataPoint(counts[i][0], lineSetLabel, pointMetaData));
        }

        Font font = graphFont;
        if (graphData.getTitleFont() != null) {
            font = graphData.getTitleFont();
        }

        graphMetaData.put(GraphSource.GRAPH_TYPE, GraphSource.GRAPH_TYPE_PIE);
        graphMetaData.put(GraphSource.BACKGROUND_COLOR,
                          graphData.getBackgroundColor()); //edit to get graph data background color, defaults to white
        graphMetaData.put(GraphSource.GRAPH_LABEL_BACKGROUND_COLOR, graphData.getLabelBackgroundColor());
        graphMetaData.put(GraphSource.GRAPH_TITLE, graphTitle);
        graphMetaData.put(GraphSource.GRAPH_FONT, font);
        graphMetaData.put(GraphSource.GRAPH_BORDER, false);
        graphMetaData.put(GraphSource.GRAPH_DISPLAY_LABEL, graphDisplayLabel);
        graphMetaData.put(GraphSource.GRAPH_NO_DATA_MESSAGE, graphNoDataMessage);
        graphMetaData.put(GraphSource.GRAPH_LEGEND, graphData.showLegend());
        graphMetaData.put(GraphSource.LEGEND_FONT, legendFont);
    }

    private GraphObject getGraph(GraphDataInterface graphData, List<DataSeries> series,
                                 Map<String, Object> graphMetaData, LegendItemCollection legendItems,
                                 String graphType) {

        GraphObject graph = null;
        String graphTitle = graphData.getGraphTitle();

        try {
            // add the created chart properties
            JFreeChartGraphSource graphSource = new JFreeChartGraphSource();
            graphSource.setData(series);
            graphSource.setParams(graphMetaData);
            graphSource.initialize();

            // add the custom legend
            graphSource.getChart().getCategoryPlot().setFixedLegendItems(legendItems);

            // render the graph to get the image map
            RenderedGraph renderedGraph = graphSource.renderGraph(graphData.getGraphWidth(), graphData.getGraphHeight(),
                                                                  Encoding.PNG);
            String imageFileName = getCleanValue(graphTitle) + "_" + graphType + ".png";
            // get the image map
            String imageMapName = "imageMap" + graphDataId;
            String imageMap = appendImageMapTarget(renderedGraph.getImageMap(imageMapName),
                                                   graphData.getLineSetURLTarget());

            try {
                // store away the graph data file
                graphDataHandler.putGraphData(graphData, graphDataId);

                graph = new GraphObject(graphSource, renderedGraph, imageFileName, imageMapName, imageMap, graphDataId);
            } catch (GraphException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } catch (GraphException e) {
            System.out.println("Could not create graph " + graphTitle);
            e.printStackTrace();
        }

        return graph;
    }

    private Map<String, Object> dumpGraph(GraphDataInterface graphData) {
        // setup the default metadata
        List<DataSeries> dataSeries = new ArrayList<DataSeries>();
        LegendItemCollection legendItems = new LegendItemCollection();
        Map<String, Object> graphMetaData = new HashMap<String, Object>();
        double maxCount = setDataSeries(graphData, null, false, dataSeries, legendItems, false);

        setTimeSeriesGraphMetaData(graphData, null, null, maxCount, graphMetaData);

        // store away the graph data file
        try {
            graphDataHandler.putGraphData(graphData, graphDataId);
        } catch (GraphException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return graphMetaData;
    }

    private static String getUniqueId(String userId) {
        Date currentTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMMdd_hh_mm_ss_SSS");

        if (userId != null && userId.length() > 0) {
            return userId + "_" + sdf.format(currentTime) + "_" + (int) (Math.random() * 10000);
        } else {
            return sdf.format(currentTime) + "_" + (int) (Math.random() * 10000);
        }
    }

    private String appendImageMapTarget(String imageMap, String target) {
        if (target != null && target.length() > 0) {
            return imageMap.replace("<area ", "<area target=\"" + target + "\" ");
        }

        return imageMap;
    }

    private String getCleanValue(String value) {
        if (value == null) {
            return "";
        }

        // illegal characters for file names on windows are \/:*?"<>|
        String cleanValue = value.replaceAll("\\\\", "-");
        cleanValue = cleanValue.replaceAll("/", "-");
        cleanValue = cleanValue.replaceAll(":", "-");
        cleanValue = cleanValue.replaceAll("\\*", "-");
        cleanValue = cleanValue.replaceAll("\\?", "-");
        cleanValue = cleanValue.replaceAll("\"", "-");
        cleanValue = cleanValue.replaceAll("<", "-");
        cleanValue = cleanValue.replaceAll(">", "-");
        cleanValue = cleanValue.replaceAll("\\|", "-");

        cleanValue = cleanValue.replaceAll(";", "-");
        cleanValue = cleanValue.replaceAll("\\+", "-");

        return cleanValue;
    }

    private boolean displaySeries(String displayKey, int seriesIndex) {
        if (displayKey != null && displayKey.length() > seriesIndex && displayKey.charAt(seriesIndex) == '0') {
            return false;
        } else {
            return true;
        }
    }

    private String getDataSeriesJSON(String[] lineSetLabels, boolean[] displayAlerts, boolean[] displaySeverityAlerts,
                                     String quoteStr) {
        String json = "";

        if (lineSetLabels != null) {
            for (int i = 0; i < lineSetLabels.length; i++) {
                boolean displayAlert = false;
                boolean displaySeverityAlert = false;
                try {
                    displayAlert = displayAlerts[i];
                } catch (Exception e) {
                }
                try {
                    displaySeverityAlert = displaySeverityAlerts[i];
                } catch (Exception e) {
                }

                if (displayAlert) {
                    json +=
                            getDataSeriesJSONHelper(lineSetLabels[i],
                                                    lineSetLabels[i] + ": " + getTranslation("Normal"), true, quoteStr);
                    json += ", ";
                    json +=
                            getDataSeriesJSONHelper(lineSetLabels[i],
                                                    lineSetLabels[i] + ": " + getTranslation("Warning"), true,
                                                    quoteStr);
                    json += ", ";
                    json +=
                            getDataSeriesJSONHelper(lineSetLabels[i], lineSetLabels[i] + ": " + getTranslation("Alert"),
                                                    true, quoteStr);
                    json += ", ";

                    if (displaySeverityAlert) {
                        json +=
                                getDataSeriesJSONHelper(lineSetLabels[i],
                                                        lineSetLabels[i] + ": " + getTranslation("Severe"), true,
                                                        quoteStr);
                        json += ", ";
                    }
                } else {
                    json += getDataSeriesJSONHelper(lineSetLabels[i], lineSetLabels[i], false, quoteStr);
                    json += ", ";
                }
            }

            if (json.length() > 0) {
                json = json.substring(0, json.length() - 2);
            }
        }

        return "{ [ " + json + " ] }";
    }

    private String getDataSeriesJSONHelper(String seriesName, String displayName, boolean displayAlerts,
                                           String quoteStr) {
        String json = "";
        json += "{ ";
        json += "seriesName:";
        json += quoteStr + seriesName + quoteStr;
        json += ", displayName:";
        json += quoteStr + displayName + quoteStr;
        json += ", displayAlerts:" + displayAlerts + " ";
        json += "}";
        return json;
    }
}
