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

package edu.jhuapl.graphs.jfreechart;

import edu.jhuapl.graphs.DataSeriesInterface;
import edu.jhuapl.graphs.GraphException;
import edu.jhuapl.graphs.GraphSource;
import edu.jhuapl.graphs.PointInterface;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

public class JFreeChartPieGraphSource extends JFreeChartBaseSource {

    public JFreeChartPieGraphSource() {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() throws GraphException {
        String title = getParam(GraphSource.GRAPH_TITLE, String.class, DEFAULT_TITLE);
        boolean legend = getParam(GraphSource.GRAPH_LEGEND, Boolean.class, DEFAULT_GRAPH_LEGEND);
        boolean graphToolTip = getParam(GraphSource.GRAPH_TOOL_TIP, Boolean.class, DEFAULT_GRAPH_TOOL_TIP);
        boolean graphDisplayLabel = getParam(GraphSource.GRAPH_DISPLAY_LABEL, Boolean.class, false);
        boolean legendBorder = getParam(GraphSource.LEGEND_BORDER, Boolean.class, DEFAULT_LEGEND_BORDER);
        boolean graphBorder = getParam(GraphSource.GRAPH_BORDER, Boolean.class, DEFAULT_GRAPH_BORDER);
        Font titleFont = getParam(GraphSource.GRAPH_FONT, Font.class, DEFAULT_GRAPH_TITLE_FONT);
        String noDataMessage = getParam(GraphSource.GRAPH_NO_DATA_MESSAGE, String.class, DEFAULT_GRAPH_NO_DATA_MESSAGE);
        PieGraphData pieGraphData = makeDataSet();
        Map<Comparable, Paint> colors = pieGraphData.colors;

        this.chart = ChartFactory.createPieChart(title, pieGraphData.data, false, graphToolTip, false);

        Paint backgroundColor = getParam(GraphSource.BACKGROUND_COLOR, Paint.class, DEFAULT_BACKGROUND_COLOR);
        Paint plotColor = getParam(JFreeChartTimeSeriesGraphSource.PLOT_COLOR, Paint.class, backgroundColor);
        Paint
                labelColor =
                getParam(JFreeChartTimeSeriesGraphSource.GRAPH_LABEL_BACKGROUND_COLOR, Paint.class,
                         DEFAULT_BACKGROUND_COLOR);

        this.chart.setBackgroundPaint(backgroundColor);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(plotColor);
        plot.setNoDataMessage(noDataMessage);

        if (!graphDisplayLabel) {
            plot.setLabelGenerator(null);
        } else {
            plot.setInteriorGap(0.001);
            plot.setMaximumLabelWidth(.3);
//	        plot.setIgnoreNullValues(true);
            plot.setIgnoreZeroValues(true);
            plot.setShadowPaint(null);
//	        plot.setOutlineVisible(false);
            //TODO use title font?
            Font font = plot.getLabelFont();
            plot.setLabelLinkStyle(PieLabelLinkStyle.CUBIC_CURVE);
            plot.setLabelFont(font.deriveFont(font.getSize2D() * .75f));
            plot.setLabelBackgroundPaint(labelColor);
            plot.setLabelShadowPaint(null);
            plot.setLabelOutlinePaint(null);
            plot.setLabelGap(0.001);
            plot.setLabelLinkMargin(0.0);
            plot.setLabelLinksVisible(true);
//	        plot.setSimpleLabels(true);
//	        plot.setCircular(true);
        }

        if (!graphBorder) {
            plot.setOutlineVisible(false);
        }

        if (title != null && !"".equals(title)) {
            TextTitle title1 = new TextTitle();
            title1.setText(title);
            title1.setFont(titleFont);
            title1.setPadding(3, 2, 5, 2);
            chart.setTitle(title1);
        } else {
            chart.setTitle((TextTitle) null);
        }
        plot.setLabelPadding(new RectangleInsets(1, 1, 1, 1));
        //Makes a wrapper for the legend to remove the border around it
        if (legend) {
            LegendTitle legend1 = new LegendTitle(chart.getPlot());
            BlockContainer wrapper = new BlockContainer(new BorderArrangement());

            if (legendBorder) {
                wrapper.setFrame(new BlockBorder(1, 1, 1, 1));
            } else {
                wrapper.setFrame(new BlockBorder(0, 0, 0, 0));
            }

            BlockContainer items = legend1.getItemContainer();
            items.setPadding(2, 10, 5, 2);
            wrapper.add(items);
            legend1.setWrapper(wrapper);
            legend1.setPosition(RectangleEdge.BOTTOM);
            legend1.setHorizontalAlignment(HorizontalAlignment.CENTER);

            if (params.get(GraphSource.LEGEND_FONT) instanceof Font) {
                legend1.setItemFont(((Font) params.get(GraphSource.LEGEND_FONT)));
            }

            chart.addSubtitle(legend1);
            plot.setLegendLabelGenerator(new PieGraphLabelGenerator());
        }

        for (Comparable category : colors.keySet()) {
            plot.setSectionPaint(category, colors.get(category));
        }

        plot.setToolTipGenerator(new PieGraphToolTipGenerator(pieGraphData));
        plot.setURLGenerator(new PieGraphURLGenerator(pieGraphData));

        initialized = true;
    }

    @SuppressWarnings("unchecked")
    public PieGraphData makeDataSet() throws GraphException {
        if (data.size() != 1) {
            throw new GraphException("Was expecting a single series, receieved " + data.size());
        }

        DataSeriesInterface dsi = data.get(0);

        DefaultPieDataset pieData = new DefaultPieDataset();
        Map<Comparable, Paint> colors = new HashMap<Comparable, Paint>(dsi.getPoints().size());
        Map<Comparable, String> toolTips = new HashMap<Comparable, String>(dsi.getPoints().size());
        Map<Comparable, String> urls = new HashMap<Comparable, String>(dsi.getPoints().size());

        for (PointInterface p : dsi.getPoints()) {
            if (p.getDescriminator() instanceof Comparable) {
                Comparable key = (Comparable) p.getDescriminator();
                pieData.setValue(key, p.getValue());

                Paint itemColor = getParam(p.getMetadata(), ITEM_COLOR, Paint.class, null);
                String itemToolTip = getParam(p.getMetadata(), ITEM_TOOL_TIP, String.class, null);
                String itemURL = getParam(p.getMetadata(), ITEM_URL, String.class, null);
                if (itemColor != null) {
                    colors.put(key, itemColor);
                }
                if (itemToolTip != null) {
                    toolTips.put(key, itemToolTip);
                }
                if (itemURL != null) {
                    urls.put(key, itemURL);
                }
            }
        }

        return new PieGraphData(pieData, colors, toolTips, urls);
    }

    private static class PieGraphData {

        public final PieDataset data;
        public final Map<Comparable, Paint> colors;
        public final Map<Comparable, String> toolTips;
        public final Map<Comparable, String> urls;

        public PieGraphData(final PieDataset data, final Map<Comparable, Paint> colors,
                            final Map<Comparable, String> toolTips, final Map<Comparable, String> urls) {
            this.data = data;
            this.colors = colors;
            this.toolTips = toolTips;
            this.urls = urls;
        }
    }

    private static class PieGraphToolTipGenerator implements PieToolTipGenerator {

        private final PieGraphData pieGraphData;

        public PieGraphToolTipGenerator(PieGraphData pieGraphData) {
            this.pieGraphData = pieGraphData;
        }

        @Override
        public String generateToolTip(PieDataset dataset, Comparable key) {
            return pieGraphData.toolTips.get(key);
        }
    }

    private static class PieGraphURLGenerator implements PieURLGenerator {

        private final PieGraphData pieGraphData;

        public PieGraphURLGenerator(PieGraphData pieGraphData) {
            this.pieGraphData = pieGraphData;
        }

        @Override
        public String generateURL(PieDataset dataset, Comparable key, int pieIndex) {
            return pieGraphData.urls.get(key);
        }
    }

    private static class PieGraphLabelGenerator implements PieSectionLabelGenerator {

        @Override
        public AttributedString generateAttributedSectionLabel(PieDataset dataset, Comparable key) {
            return null;
        }

        @Override
        public String generateSectionLabel(PieDataset dataset, Comparable key) {
            String label = (String) key;

            if (label.length() <= 30) {
                return label;
            } else {
                return label.substring(0, 28) + "...";
            }
        }
    }
}
