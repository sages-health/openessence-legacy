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

Ext.namespace("OE.report.datasource.pivot");

OE.report.datasource.pivot.form = function (configuration) {
    var data = [];
    var count = 1;
    Ext.each(configuration.data, function (row) {
        data.push([count++, row[0], row[1], row[2], false]);
    });

    var filterSelected = function (record) {
        return !record.get('selected');
    };
    var dimensionStore = new Ext.data.ArrayStore({
        idIndex: 0,
        fields: ['num', 'id', 'name', 'dimension', 'selected'],
        data: data
    });
    dimensionStore.filterBy(filterSelected);

    var updateStore = function (combo, newValue, oldValue) {
        dimensionStore.filterBy(function () {
            return true;
        });
        if (typeof oldValue.length === "undefined") {
            dimensionStore.getById(oldValue).set('selected', false);
        }
        if (typeof newValue.length === "undefined") {
            dimensionStore.getById(newValue).set('selected', true);
        }
        dimensionStore.filterBy(filterSelected);
    };

    var resultsFormPanel = new Ext.form.FormPanel({
        border: false,
        monitorValid: true,
        items: [
            {
                xtype: 'combo',
                name: 'x',
                fieldLabel: 'X',
                mode: 'local',
                triggerAction: 'all',
                typeAhead: true,
                forceSelection: true,
                selectOnFocus: true,
                store: dimensionStore,
                valueField: 'num',
                displayField: 'name',
                lastQuery: '',
                required: true,
                listeners: {
                    change: updateStore
                }
            },
            {
                xtype: 'combo',
                name: 'y',
                fieldLabel: 'Y',
                mode: 'local',
                triggerAction: 'all',
                typeAhead: true,
                forceSelection: true,
                selectOnFocus: true,
                store: dimensionStore,
                valueField: 'num',
                displayField: 'name',
                lastQuery: '',
                required: true,
                listeners: {
                    change: updateStore
                }
            }
//        , {
//            xtype: 'combo',
//            name: 'z',
//            fieldLabel: 'Z',
//            mode: 'local',
//            triggerAction: 'all',
//            typeAhead: true,
//            forceSelection: true,
//            selectOnFocus: true,
//            store: dimensionStore,
//            valueField: 'num',
//            displayField: 'name',
//            lastQuery: '',
//            listeners: {
//                change: updateStore
//            }
//        }
        ],
        labelWidth: 25,
        padding: 5,
        buttons: [
            {
                text: messagesBundle[configuration.dataSource + '.ok'] || messagesBundle['input.datasource.default.ok'],
                formBind: true,
                handler: function () {
                    if (configuration.callback) {
                        var values = resultsFormPanel.getForm().getFieldValues();
                        var mapped = {};

                        dimensionStore.filterBy(function () {
                            return true;
                        });
                        for (var k in values) {
                            mapped[k] = dimensionStore.getById(values[k]).data;
                        }

                        configuration.callback(mapped);
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
