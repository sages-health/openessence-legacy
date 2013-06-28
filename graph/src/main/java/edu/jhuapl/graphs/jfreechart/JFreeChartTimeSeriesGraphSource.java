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
import edu.jhuapl.graphs.Encoding;
import edu.jhuapl.graphs.GraphException;
import edu.jhuapl.graphs.GraphSource;
import edu.jhuapl.graphs.GraphSourceBean;
import edu.jhuapl.graphs.TimePointInterface;
import edu.jhuapl.graphs.TimeResolution;
import edu.jhuapl.graphs.TimeSeriesInterface;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.Quarter;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of {@link GraphSource} for JFreeChart.  Currently, this class produces Time Series graphs.
 *
 * <p>To use this class, you need to use one of the two forms of initialization, either
 * <pre>JFreeChartGraphSource source = new JFreeChartGraphSource(listOfSeries, graphParams);</pre>
 * or
 * <pre>
 * JFreeChartGraphSource source = new JFreeChartGraphSource();
 * source.setData(listOfSeries);
 * source.setParams(graphParams);
 * source.initialize();
 * </pre>
 * Note that the former form will automatically initialize the internal JFreeChart graph.  Note that if you try to
 * {@link #renderGraph(int, int, Encoding) render} the graph without first initializing the internal JFreeChart graph, a
 * {@link DataSourceException} will be thrown. </p> <p>This class is backed by a {@link JFreeChart} instance, which can
 * be accessed by {@link #getChart()}.
 *
 * @see GraphSource
 */
public class JFreeChartTimeSeriesGraphSource extends JFreeChartBaseSource implements GraphSourceBean {

    protected static final TimeResolution DEFAULT_RESOLUTION = TimeResolution.DAILY;
    protected static final Stroke DEFAULT_GRAPH_STROKE = new BasicStroke();
    protected static final String DEFAULT_DOMAIN_LABEL = "Date";
    protected static final String DEFAULT_RANGE_LABEL = "Value";
    //Setting the domain axis lower margin to 0 places the start of the series directly on the y-axis origin - there's no gap.
    private static final Double DEFAULT_DOMAIN_AXIS_LOWER_MARGIN = new Double(.04);
    //Setting the domain axis upper margin to 0 places places the end of the series squarely at the edge of the plot - there's no gap.
    private static final Double DEFAULT_DOMAIN_AXIS_UPPER_MARGIN = new Double(.04);

    /**
     * Specifies the lower margin for the domain axis. Expects a {@link Double}}.
     */
    public static final String DOMAIN_AXIS_LOWER_MARGIN = "DomainAxisLowerMargin";

    /**
     * Specifies the upper margin for the domain axis. Expects a {@link Double}.
     */
    public static final String DOMAIN_AXIS_UPPER_MARGIN = "DomainAxisUpperMargin";

    /**
     * Specifies a particular date axis object to be used in this graph.  Expects a {@link org.jfree.chart.axis.DateAxis}.
     */
    public static final String DATE_AXIS = "DateAxis";

    /**
     * Creates an empty, uninitialized JFreeChartGraphSource.
     */
    public JFreeChartTimeSeriesGraphSource() {
        super();
    }

    /**
     * Creates and initializes a JFreeChartGraphSource with the given data series and graph parameters.
     *
     * @param data        the list of {@link TimeSeriesInterface time series}.
     * @param graphParams the graph parameters
     * @throws GraphException if the initialization fails
     */
    public JFreeChartTimeSeriesGraphSource(List<? extends TimeSeriesInterface> data, Map<String, Object> graphParams)
            throws GraphException {
        this();
        setData(data);
        setParams(graphParams);
        initialize();
    }

    /**
     * Initializes the graph.  This method generates the backing {@link JFreeChart} from the time series and graph
     * parameter data.
     *
     * @throws GraphException if the initialization fails
     */
    public void initialize() throws GraphException {
        String title = getParam(GraphSource.GRAPH_TITLE, String.class, DEFAULT_TITLE);
        String xLabel = getParam(GraphSource.GRAPH_X_LABEL, String.class, DEFAULT_DOMAIN_LABEL);
        String yLabel = getParam(GraphSource.GRAPH_Y_LABEL, String.class, DEFAULT_RANGE_LABEL);
        Shape graphShape = getParam(GraphSource.GRAPH_SHAPE, Shape.class, DEFAULT_GRAPH_SHAPE);
        Paint graphColor = getParam(GraphSource.GRAPH_COLOR, Paint.class, DEFAULT_GRAPH_COLOR);
        boolean legend = getParam(GraphSource.GRAPH_LEGEND, Boolean.class, DEFAULT_GRAPH_LEGEND);
        boolean graphToolTip = getParam(GraphSource.GRAPH_TOOL_TIP, Boolean.class, DEFAULT_GRAPH_TOOL_TIP);
        Stroke graphStroke = getParam(GraphSource.GRAPH_STROKE, Stroke.class, DEFAULT_GRAPH_STROKE);
        Font titleFont = getParam(GraphSource.GRAPH_FONT, Font.class, DEFAULT_GRAPH_TITLE_FONT);
        boolean graphBorder = getParam(GraphSource.GRAPH_BORDER, Boolean.class, DEFAULT_GRAPH_BORDER);
        boolean legendBorder = getParam(GraphSource.LEGEND_BORDER, Boolean.class, DEFAULT_LEGEND_BORDER);
        Double offset = getParam(GraphSource.AXIS_OFFSET, Double.class, DEFAULT_AXIS_OFFSET);

        checkSeriesType(data);
        @SuppressWarnings("unchecked")
        List<? extends TimeSeriesInterface> timeData = (List<? extends TimeSeriesInterface>) data;

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        int seriesCount = 1;
        for (TimeSeriesInterface series : timeData) {
            dataset.addSeries(buildTimeSeries(series, seriesCount));
            seriesCount += 1;
        }

        // actually create the chart
        this.chart = ChartFactory.createTimeSeriesChart(title, xLabel, yLabel, dataset, false, graphToolTip, false);

        // start customizing it
        Paint backgroundColor = getParam(GraphSource.BACKGROUND_COLOR, Paint.class, DEFAULT_BACKGROUND_COLOR);
        Paint plotColor = getParam(JFreeChartTimeSeriesGraphSource.PLOT_COLOR, Paint.class, backgroundColor);
        Paint
                graphDomainGridlinePaint =
                getParam(GraphSource.GRAPH_DOMAIN_GRIDLINE_PAINT, Paint.class, backgroundColor);
        Paint graphRangeGridlinePaint = getParam(GraphSource.GRAPH_RANGE_GRIDLINE_PAINT, Paint.class, backgroundColor);

        this.chart.setBackgroundPaint(backgroundColor);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(plotColor);
        plot.setAxisOffset(new RectangleInsets(offset, offset, offset, offset));
        plot.setDomainGridlinePaint(graphDomainGridlinePaint);
        plot.setRangeGridlinePaint(graphRangeGridlinePaint);

        if (graphBorder) {

        } else {
            plot.setOutlinePaint(null);
        }

        //Use a TextTitle to change the font of the graph title
        TextTitle title1 = new TextTitle();
        title1.setText(title);
        title1.setFont(titleFont);
        chart.setTitle(title1);

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
        }

        boolean include0 = getParam(GraphSource.GRAPH_RANGE_INCLUDE_0, Boolean.class, true);
        NumberAxis numAxis = (NumberAxis) plot.getRangeAxis();
        double rangeLower = getParam(GraphSource.GRAPH_RANGE_LOWER_BOUND, Double.class, numAxis.getLowerBound());
        double rangeUpper = getParam(GraphSource.GRAPH_RANGE_UPPER_BOUND, Double.class, numAxis.getUpperBound());
        boolean graphRangeIntegerTick = getParam(GraphSource.GRAPH_RANGE_INTEGER_TICK, Boolean.class, false);
        boolean graphRangeMinorTickVisible = getParam(GraphSource.GRAPH_RANGE_MINOR_TICK_VISIBLE, Boolean.class, true);

        if (include0) {
            rangeLower = 0;
        }

        numAxis.setRange(rangeLower, rangeUpper);

        if (graphRangeIntegerTick) {
            numAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        }

        numAxis.setMinorTickMarksVisible(graphRangeMinorTickVisible);
        setupFont(numAxis, GraphSource.GRAPH_Y_AXIS_FONT);

        if (params.get(GraphSource.GRAPH_Y_AXIS_LABEL_FONT) instanceof Font) {
            numAxis.setLabelFont(((Font) params.get(GraphSource.GRAPH_Y_AXIS_LABEL_FONT)));
        }

        TimeResolution minimumResolution = getMinimumResolution(timeData);
        DateFormat
                dateFormat =
                getParam(GraphSource.GRAPH_DATE_FORMATTER, DateFormat.class,
                         new DefaultDateFormatFactory().getFormat(minimumResolution));

        if (params.get(DATE_AXIS) instanceof DateAxis) {
            DateAxis dateAxis = (DateAxis) params.get(DATE_AXIS);
            dateAxis.setLabel(xLabel);
            plot.setDomainAxis(dateAxis);
        }
        DateAxis dateAxis = ((DateAxis) plot.getDomainAxis());
        dateAxis.setDateFormatOverride(dateFormat);

        if (params.get(GraphSource.GRAPH_X_AXIS_LABEL_FONT) instanceof Font) {
            dateAxis.setLabelFont(((Font) params.get(GraphSource.GRAPH_X_AXIS_LABEL_FONT)));
        }

        int minTick = getParam(GraphSource.GRAPH_MIN_DOMAIN_TICK, Integer.class, 1);
        if (minTick <= 0) {
            minTick = 1;
        }

        dateAxis.setTickUnit(getDateTickUnit(minimumResolution, minTick), false, false);
        //dateAxis.setMinorTickMarksVisible(true);
        //dateAxis.setMinorTickCount(7);
        dateAxis.setMinorTickMarkOutsideLength(2);

        Integer minorTick = getParam(GraphSource.GRAPH_MINOR_TICKS, Integer.class, null);
        if (minorTick != null) {
            int minorVal = minorTick;
            if (minorVal > 0) {
                dateAxis.setMinorTickCount(minorVal);
            }
        }

        setupFont(dateAxis, GraphSource.GRAPH_X_AXIS_FONT);

        //double lowerMargin = getParam(DOMAIN_AXIS_LOWER_MARGIN, Double.class, DEFAULT_DOMAIN_AXIS_LOWER_MARGIN);
        double lowerMargin = getParam(DOMAIN_AXIS_LOWER_MARGIN, Double.class, DEFAULT_DOMAIN_AXIS_LOWER_MARGIN);
        dateAxis.setLowerMargin(lowerMargin);

        //double upperMargin = getParam(DOMAIN_AXIS_UPPER_MARGIN, Double.class, DEFAULT_DOMAIN_AXIS_UPPER_MARGIN);
        double upperMargin = getParam(DOMAIN_AXIS_UPPER_MARGIN, Double.class, DEFAULT_DOMAIN_AXIS_UPPER_MARGIN);
        dateAxis.setUpperMargin(upperMargin);

        Date domainLower = getParam(GraphSource.GRAPH_DOMAIN_LOWER_BOUND, Date.class, dateAxis.getMinimumDate());
        Date domainUpper = getParam(GraphSource.GRAPH_DOMAIN_UPPER_BOUND, Date.class, dateAxis.getMaximumDate());

        dateAxis.setRange(domainLower, domainUpper);

        // depending on the domain axis range, display either 1 tick per day, week, month or year
        TickUnits standardUnits = new TickUnits();
        standardUnits.add(new DateTickUnit(DateTickUnitType.DAY, 1));
        standardUnits.add(new DateTickUnit(DateTickUnitType.DAY, 7));
        standardUnits.add(new DateTickUnit(DateTickUnitType.MONTH, 1));
        standardUnits.add(new DateTickUnit(DateTickUnitType.YEAR, 1));
        dateAxis.setStandardTickUnits(standardUnits);

        TimeSeriesRenderer renderer = new TimeSeriesRenderer(dataset);
        setupRenderer(renderer, graphColor, graphShape, graphStroke);
        renderer.setBaseFillPaint(Color.BLACK);
        renderer.setSeriesOutlinePaint(0, Color.WHITE);

        //renderer.setUseOutlinePaint(true);

        plot.setRenderer(renderer);
        this.initialized = true;
    }

    protected void setupRenderer(TimeSeriesRenderer renderer, Paint graphColor, Shape graphShape, Stroke graphStroke) {
        int count = 0;
        for (DataSeriesInterface si : data) {
            Paint seriesColor = getParam(si.getMetadata(), GraphSource.SERIES_COLOR, Paint.class, graphColor);
            Shape seriesShape = getParam(si.getMetadata(), GraphSource.SERIES_SHAPE, Shape.class, graphShape);
            Stroke seriesStroke = getParam(si.getMetadata(), GraphSource.SERIES_STROKE, Stroke.class, graphStroke);
            boolean seriesVisible = getParam(si.getMetadata(), GraphSource.SERIES_VISIBLE, Boolean.class, true);
            boolean
                    seriesLinesVisible =
                    getParam(si.getMetadata(), GraphSource.SERIES_LINES_VISIBLE, Boolean.class, true);

            renderer.setSeriesPaint(count, seriesColor);
            renderer.setSeriesFillPaint(count, seriesColor);
            renderer.setSeriesOutlinePaint(count, seriesColor);
            renderer.setSeriesShape(count, seriesShape);
            renderer.setSeriesStroke(count, seriesStroke);
            renderer.setSeriesVisible(count, seriesVisible);
            renderer.setSeriesLinesVisible(count, seriesLinesVisible);

            count += 1;
        }

        renderer.setDrawOutlines(false);
    }

    private TimeSeries buildTimeSeries(TimeSeriesInterface series, int i) throws GraphException {
        Map<String, Object> metadata = series.getMetadata();
        TimeResolution resolution;
        if (metadata.get(GraphSource.SERIES_TIME_RESOLUTION) instanceof TimeResolution) {
            resolution = (TimeResolution) metadata.get(GraphSource.SERIES_TIME_RESOLUTION);
        } else {
            resolution = DEFAULT_RESOLUTION;
        }

        String title = null;
        if (metadata.get(GraphSource.SERIES_TITLE) instanceof String) {
            title = (String) metadata.get(GraphSource.SERIES_TITLE);
        } else {
            title = "series" + i;
        }

        // This method will throw a graph exception if multiple points with the same resolved date are
        // present
        checkSeries(series, resolution);

        TimeSeries s = new TimeSeries(title);
        for (TimePointInterface point : series.getPoints()) {
            s.add(new MetadataTimeSeriesDataItem(makePeriod(point, resolution), point.getValue(), point.getMetadata()));
        }

        return s;
    }

    private static void checkSeriesType(List<? extends DataSeriesInterface> data) throws GraphException {
        for (DataSeriesInterface dataSeries : data) {
            if (!(dataSeries instanceof TimeSeriesInterface)) {
                throw new GraphException("All Series in time series graph must be date based");
            }
        }
    }


    private static void checkSeries(TimeSeriesInterface series,
                                    TimeResolution resolution) throws GraphException {
        Set<Date> seenDates = new HashSet<Date>();
        for (TimePointInterface point : series.getPoints()) {
            Date kernelDate = getKernelDate(point.getDescriminator(), resolution);
            if (seenDates.contains(kernelDate)) {
                throw new GraphException("Time series already contains point with date " + kernelDate);
            }
            seenDates.add(kernelDate);
        }

    }

    private static Date getKernelDate(Date d, TimeResolution resolution) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);

        switch (resolution) {
            case HOURLY:
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                break;
            case DAILY:
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                break;
            case WEEKLY:
                c.set(Calendar.DAY_OF_WEEK, 1);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                break;
            case MONTHLY:
                c.set(Calendar.WEEK_OF_MONTH, 1);
                c.set(Calendar.DAY_OF_WEEK, 1);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                break;
            case QUARTERLY:
                int numMonthsInQuarter = 3;
                // sets to either January 1st, April 1st, July 1st or October 1st
                c.set(Calendar.MONTH, c.get(Calendar.MONTH) / numMonthsInQuarter * numMonthsInQuarter);
                c.set(Calendar.DAY_OF_MONTH, 1);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                break;
            case YEARLY:
                c.set(Calendar.MONTH, Calendar.JANUARY);
                c.set(Calendar.WEEK_OF_MONTH, 1);
                c.set(Calendar.DAY_OF_WEEK, 1);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                break;
        }

        return c.getTime();
    }

    private static RegularTimePeriod makePeriod(TimePointInterface point, TimeResolution resolution)
            throws GraphException {
        switch (resolution) {
            case DAILY:
                return makeDay(point);
            case MONTHLY:
                return makeMonth(point);
            case WEEKLY:
                return makeWeek(point);
            case YEARLY:
                return makeYear(point);
            case HOURLY:
                return makeHour(point);
            case QUARTERLY:
                return makeQuarter(point);
            default:
                throw new GraphException("Unexpected resolution \"" + resolution.toString() + "\"");
        }
    }

    private static Day makeDay(TimePointInterface point) {
        return new Day(point.getDescriminator());
    }

    private static Week makeWeek(TimePointInterface point) {
        return new Week(point.getDescriminator());
    }

    private static Month makeMonth(TimePointInterface point) {
        return new Month(point.getDescriminator());
    }

    private static Quarter makeQuarter(TimePointInterface point) {
        return new Quarter(point.getDescriminator());
    }

    private static Year makeYear(TimePointInterface point) {
        return new Year(point.getDescriminator());
    }

    private static Hour makeHour(TimePointInterface point) {
        return new Hour(point.getDescriminator());
    }

    private static DateTickUnit getDateTickUnit(TimeResolution minimumResolution, int qty) throws GraphException {
        DateTickUnitType dateTypeUnit;
        switch (minimumResolution) {
            case HOURLY:
                dateTypeUnit = DateTickUnitType.HOUR;
                break;
            case DAILY:
            case WEEKLY:
                dateTypeUnit = DateTickUnitType.DAY;
                break;
            case MONTHLY:
                dateTypeUnit = DateTickUnitType.MONTH;
                break;
            case YEARLY:
                dateTypeUnit = DateTickUnitType.YEAR;
                break;
            default:
                throw new GraphException("Unrecognized resolution \"" + minimumResolution + "\"");
        }

        return new DateTickUnit(dateTypeUnit, qty);
    }

    private static TimeResolution getMinimumResolution(List<? extends TimeSeriesInterface> data) {
        TimeResolution minimum = null;
        for (TimeSeriesInterface tsi : data) {
            if (tsi.getMetadata().get(GraphSource.SERIES_TIME_RESOLUTION) instanceof TimeResolution) {
                TimeResolution candidate = (TimeResolution) tsi.getMetadata().get(GraphSource.SERIES_TIME_RESOLUTION);
                if (minimum == null || candidate.ordinal() < minimum.ordinal()) {
                    minimum = candidate;
                }
            }
        }

        return minimum != null ? minimum : TimeResolution.DAILY;
    }
}
