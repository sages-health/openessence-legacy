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
 * Search form panel, builds query form using filter dimensions and results grid using detail dimensions.
 *
 * Selected records are returned on submit using name/value <code>configuration.name</code>.
 */
define(['DataSourceGrid'], function (DataSourceGrid) {
    var SearchPanel = Ext.extend(Ext.Panel, {
        constructor: function (configuration) {

            var sm = new Ext.grid.RowSelectionModel({
                singleSelect: OE.util.getBooleanValue(configuration.singleSelect, false)
            });

            var gridPanel = new DataSourceGrid({
                title: messagesBundle[configuration.dataSource + '.results'] ||
                    messagesBundle['input.datasource.default.results'],
                selectionModel: sm,
                parameters: {dsId: configuration.dataSource},
                dataSource: configuration.dataSource,
                data: configuration.data || {}
            });

            var resultsFormPanel = new Ext.form.FormPanel({
                layout: 'border',
                region: 'center',
                border: false,
                monitorValid: true,
                items: [
                    {
                        xtype: 'textfield',
                        inputType: 'hidden',
                        name: 'results',
                        allowBlank: false,
                        getValue: function () {
                            // To return records for get field values
                            return sm.getSelections();
                        },
                        getRawValue: function () {
                            // For validation
                            return sm.getSelections();
                        }
                    },
                    gridPanel
                ],
                buttons: [
                    {
                        text: messagesBundle[configuration.dataSource + '.ok'] ||
                            messagesBundle['input.datasource.default.ok'],
                        formBind: true,
                        handler: function () {
                            if (configuration.callback) {
                                configuration.callback(resultsFormPanel.getForm().getFieldValues());
                            }
                        },
                        scope: configuration
                    },
                    {
                        text: messagesBundle[configuration.dataSource + '.cancel'] ||
                            messagesBundle['input.datasource.default.cancel'],
                        handler: function () {
                            if (configuration.callback) {
                                configuration.callback();
                            }
                        },
                        scope: configuration
                    }
                ]
            });

            configuration = Ext.apply({
                layout: 'border',
                cls: 'reportPanel',
                closable: true,
                border: false,
                items: [new OE.report.ReportForm(Ext.apply(configuration, {
                    detailsNoGroupByCallback: function (parameters) {
                        gridPanel.load({
                            params: Ext.apply(parameters, {
                                firstrecord: 0,
                                pagesize: Ext.num(messagesBundle['page.size'], 100)
                            })
                        });
                    }
                })), resultsFormPanel]
            }, configuration);

            SearchPanel.superclass.constructor.call(this, configuration);
        }
    });

    return SearchPanel;
});
