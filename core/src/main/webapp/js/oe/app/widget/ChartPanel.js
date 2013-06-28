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

/**
 * A set of OE.Charts grouped together.
 */
Ext.ns('OE');

OE.ChartPanel = Ext.extend(Ext.Panel, {
    constructor: function (config) {
        config = Ext.apply(config, {
            layout: 'border',
            closable: true,
            items: [
                {
                    xtype: 'panel',
                    itemId: 'graphCentralPanel',
                    region: 'center',
                    layout: 'table',
                    autoScroll: true,
                    layoutConfig: {
                        tableAttrs: {
                            style: {
                                width: '100%'
                            }
                        },
                        // TODO: Update this dynamically, via config
                        columns: 2
                    },
                    border: false
                }
            ]
        });

        config = Ext.applyIf(config, {
            title: messagesBundle['panel.chart.header'] + ' ' + config.index
        });

        OE.ChartPanel.superclass.constructor.call(this, config);
    },

    initComponent: function () {
        OE.ChartPanel.superclass.initComponent.call(this);

        var me = this;

        // Create a chart panel for each requested
        Ext.each(this.charts, function (chart) {
            var data = {};

            data.url = me.url;
            data.chart = chart;
            // TODO: Support graphBaseColors
            data.parameters = Ext.apply(
                Ext.apply({
                    height: 250,
                    width: 400,
                    categoryLimit: chart.categoryLimit,
                    plotHorizontal: true,
                    legend: (chart.type === 'pie')
                }, me.parameters),
                {accumId: chart.accumId, results: [chart.dimensionId, chart.accumId], charts: [chart.dimensionId], type: chart.type});

            data.title = String.format(messagesBundle['input.datasource.default.chart.titleTemplate'],
                OE.util.getDimensionName(me.dsId, data.chart.accumId),
                OE.util.getDimensionName(me.dsId, data.chart.dimensionId));

            me.getComponent('graphCentralPanel').add(new OE.Chart(data));
            me.doLayout();
        });
    }
});
