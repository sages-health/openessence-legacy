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

Ext.namespace("OE.report.datasource.chart");

/**
 * Search form panel, builds query form using filter dimensions and results grid using detail dimensions.
 *
 * Selected records are returned on submit using name/value <code>configuration.name</code>.
 */
OE.report.datasource.chart.form = function (configuration) {

    var gridId = 'charts-' + Ext.id();

    // chartIndex
    var chartIndex = 0;
    var categoryLimit = -1; //default no limit
    var catMaxLimit = messagesBundle['input.datasource.default.chart.categoryLimit'] || 25;
    var allText = messagesBundle['input.datasource.default.chart.categoryAll'] || 'All';

    var categoryLimitData = [
        [-1, allText]
    ];

    if (catMaxLimit <= 0) {
        catMaxLimit = -1;
    } else {
        for (var i = 1; i <= catMaxLimit; i++) {
            categoryLimitData.push([i, i]);
        }
    }

    var defaultChartRecord = {
        dimensionId: configuration.dimensions[0][0],
        accumId: configuration.accumulations[0][0],
        type: 'pie',
        categoryLimit: categoryLimit
    };

    var cm = new Ext.grid.ColumnModel({
        defaults: {
            xtype: 'combocolumn',
            gridId: gridId,
            sortable: true
        },
        columns: [
            {
                id: 'chartId',
                dataIndex: 'chartId',
                hidden: true
            },
            {
                header: messagesBundle.dimensionId,
                id: 'dimensionId',
                dataIndex: 'dimensionId',
                width: 155,
                editor: new Ext.form.ComboBox({
                    typeAhead: true,
                    triggerAction: 'all',
                    listClass: 'x-combo-list-small',
                    lazyRender: true,
                    mode: 'local',
                    store: new Ext.data.ArrayStore({
                        idIndex: 0,
                        fields: [ 'Id', 'Name' ],
                        data: configuration.dimensions || []
                    }),
                    valueField: 'Id',
                    displayField: 'Name'
                })
            },
            {
                header: messagesBundle.accumId,
                id: 'accumId',
                dataIndex: 'accumId',
                width: 165,
                editor: new Ext.form.ComboBox({
                    typeAhead: true,
                    triggerAction: 'all',
                    listClass: 'x-combo-list-small',
                    lazyRender: true,
                    mode: 'local',
                    store: new Ext.data.ArrayStore({
                        idIndex: 0,
                        fields: [ 'Id', 'Name' ],
                        data: configuration.accumulations || []
                    }),
                    valueField: 'Id',
                    displayField: 'Name'
                })
            },
            {
                header: messagesBundle.chartTypeId,
                id: 'type',
                dataIndex: 'type',
                width: 60,
                editor: new Ext.form.ComboBox({
                    typeAhead: true,
                    triggerAction: 'all',
                    listClass: 'x-combo-list-small',
                    lazyRender: true,
                    mode: 'local',
                    store: new Ext.data.ArrayStore({
                        idIndex: 0,
                        fields: [ 'Id', 'Name' ],
                        data: [
                            ['pie', messagesBundle['input.datasource.default.chart.pie'] || 'Pie'],
                            ['bar', messagesBundle['input.datasource.default.chart.bar'] || 'Bar']
                        ]
                    }),
                    valueField: 'Id',
                    displayField: 'Name'
                })
            },
            {
                header: messagesBundle.chartCategoryLimitId || 'TopN',
                id: 'categoryLimit',
                dataIndex: 'categoryLimit',
                width: 60,
                editor: new Ext.form.ComboBox({
                    typeAhead: true,
                    triggerAction: 'all',
                    listClass: 'x-combo-list-small',
                    lazyRender: true,
                    mode: 'local',
                    store: new Ext.data.ArrayStore({
                        idIndex: 0,
                        fields: [ 'Id', 'Label' ],
                        data: categoryLimitData
                    }),
                    valueField: 'Id',
                    displayField: 'Label'
                })
            }
        ]
    });

    var sm = new Ext.grid.RowSelectionModel({singleSelect: true});
    var store = new Ext.data.JsonStore({
        autoDestroy: true,
        fields: ['chartId', 'dimensionId', 'accumId', 'type', 'categoryLimit'],
        data: [Ext.apply({chartId: chartIndex++}, defaultChartRecord)]
    });

    var grid = new Ext.grid.EditorGridPanel({
        region: 'center',
        id: gridId,
        cm: cm,
        sm: sm,
        store: store,
        width: 400,
        height: 400,
        frame: true,
        clicksToEdit: 1,
        autoExpandColumn: 'accumId',
        deferRowRender: false, // for saved query row setting
        tbar: [
            {
                text: messagesBundle['input.datasource.default.add'],
                handler: function () {
                    addNewChart();
                }
            },
            {
                text: messagesBundle['input.datasource.default.remove'],
                handler: function () {
                    grid.stopEditing();
                    store.remove(sm.getSelected());
                    if (store.getCount() > 0) {
                        grid.startEditing(0, 0);
                    }
                }
            }
        ],
        listeners: {
            viewready: function (g) {
                // FIXME this event is never fired in Ext 3
                g.startEditing(0, 0);
            },
            afterrender: function (grid) {
                // all this code to set an initial value on a grid cell without marking it as dirty!
                if (configuration.charts) {
                    grid.getStore().on('update', function (store, newRecord, operation) {
                        if (operation !== Ext.data.Record.EDIT) {
                            return;
                        }

                        var chartId = newRecord.get('chartId');

                        // get saved chart config for this record
                        var chart = (function () {
                            for (var i = 0; i < configuration.charts.length; i++) {
                                if (configuration.charts[i].chartId === chartId) {
                                    return configuration.charts[i];
                                }
                            }
                        })();
                        if (!chart) {
                            // user updated grid for a chart that isn't saved (e.g. user added chart to existing saved query)
                            return;
                        }

                        Ext.iterate(newRecord.getChanges(), function (key, value) {
                            if (value !== chart[key]) {
                                // don't do anything if value has changed from what's in saved query
                                return;
                            }

                            // get row by chartId
                            var chartIdSelector = '.x-grid3-cell.x-grid3-col .x-grid3-col-chartId:nodeValue(' + chartId + ')';
                            var rowEl = grid.getGridEl().child(chartIdSelector).up('tr');

                            // this is why we gave IDs to the columns
                            rowEl.child('.x-grid3-td-' + key).removeClass('x-grid3-dirty-cell');
                        });
                    });

                    Ext.each(configuration.charts, function (chart) {
                        var index = grid.getStore().find('chartId', chart.chartId);
                        if (index < 0) {
                            // user deleted saved chart from the grid
                            return;
                        }

                        var record = grid.getStore().getAt(index);

                        if (grid.getColumnModel().getColumnById('dimensionId').getEditor().getStore().getById(chart.dimensionId)) {
                            record.set('dimensionId', chart.dimensionId);
                        }
                        if (grid.getColumnModel().getColumnById('accumId').getEditor().getStore().getById(chart.accumId)) {
                            record.set('accumId', chart.accumId);
                        }
                        record.set('type', chart.type);
                        record.set('categoryLimit', chart.categoryLimit);
                    });
                }
            }
        }
    });

    /**
     * Creates a new chart record and starts editing
     */
    function addNewChart() {
        var Chart = grid.getStore().recordType;
        var c = new Chart(Ext.apply({chartId: chartIndex++}, defaultChartRecord));
        grid.stopEditing();
        store.add([c]);
        grid.startEditing(store.getCount(), 0);
    }

    var resultsFormPanel = new Ext.form.FormPanel({
        layout: 'border',
        cls: 'reportPanel',
        border: false,
        monitorValid: true,
        items: [
            {
                xtype: 'textfield',
                inputType: 'hidden',
                name: 'charts',
                allowBlank: false,
                getValue: function () {
                    // To return chart configs
                    var chartArray = [];

                    store.each(function (record) {
                        chartArray.push(record.data);
                    });

                    return chartArray;
                },
                getRawValue: function () {
                    // For validation...
                    return (store.getCount() ? ' ' : '');
                }
            },
            grid
        ],
        buttons: [
            {
                text: messagesBundle[configuration.dataSource + '.ok'] || messagesBundle['input.datasource.default.ok'],
                formBind: true,
                handler: function () {
                    if (configuration.callback) {
                        configuration.callback(resultsFormPanel.getForm().getFieldValues());
                    }
                },
                scope: configuration
            },
            {
                text: messagesBundle[configuration.dataSource + '.cancel'] || messagesBundle['input.datasource.default.cancel'],
                handler: function () {
                    if (configuration.callback) {
                        configuration.callback();
                    }
                },
                scope: configuration
            }
        ]
    });

    return resultsFormPanel;
};
