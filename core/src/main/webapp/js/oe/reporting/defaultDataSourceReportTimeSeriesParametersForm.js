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

Ext.namespace("OE.report.datasource.timeseries");

/**
 * Search form panel, builds query form using filter dimensions and results grid using detail dimensions.
 *
 * Selected records are returned on submit using name/value <code>configuration.name</code>.
 */
OE.report.datasource.timeseries.form = function (configuration) {

    var titleField = new Ext.form.TextField({
        inputType: 'hidden',
        name: 'timeseriesTitle'
    });

    // Update denominator to include none for pure counts and selected accumulation for percentage, also update ids to be an array
    Ext.each(configuration.fields, function (field) {
        if (field.name === 'timeseriesDenominator') {
            field.store.each(function (record) {
                if (record.data.Id === -1) {
                    // Updates selected accumulations
                    record.data.AccumulationIds = configuration.accumulations;
                } else if (!Ext.isArray(record.data.AccumulationIds)) {
                    record.data.AccumulationIds = record.data.AccumulationIds.split(',');
                }
            });

            // Create selected accumulations, if none exists
            if (field.store.find('Id', -1) === -1) {
                var Denominator = field.store.recordType;
                // TODO: assuming Id and Name
                var selectedAccumulations = new Denominator({
                    Id: -1,
                    Name: (messagesBundle['input.datasource.default.timeseries.denominator.selectedAccumulations'] || 'None'),
                    AccumulationIds: configuration.accumulations
                });
                field.store.insert(0, selectedAccumulations);
            }

            // Update field to return accumulation ids
            field.valueField = 'AccumulationIds';

            field.listeners = {
                select: function (combo, record) {
                    titleField.setValue(record.data.Name);
                }
            };

            // convert saved query denominators to the grouping, e.g. "H1N1, H3N2" becomes "Influenza"
            if (field.value) {
                field.listeners.afterrender = function (combo) {
                    combo.setValue(field.value);
                };
            }
        }
    });

    var resultsFormPanel = new Ext.form.FormPanel({
        cls: 'reportPanel',
        border: false,
        monitorValid: true,
        items: [
            {
                xtype: 'fieldset',
                cls: 'reportFieldSet',
                layout: 'linkform',
                autoWidth: true,
                border: false,
                labelWidth: OE.util.getNumberValue(configuration.metadata.labelWidth, 150),
                defaults: {
                    width: OE.util.getNumberValue(configuration.metadata.width, 200)
                },
                items: [titleField, configuration.fields]
            }
        ],
        buttons: [
            {
                text: messagesBundle[configuration.dataSource + '.ok'] || messagesBundle['input.datasource.default.ok'],
                formBind: true,
                handler: function () {
                    if (configuration.callback) {
                        var values = resultsFormPanel.getForm().getFieldValues();
                        // Fix... for combo not firing a select/clear
                        if (values.timeseriesDenominator === undefined) {
                            values.timeseriesTitle =
                                (messagesBundle[configuration.dataSource + '.input.datasource.default.timeseries.title'] ||
                                    messagesBundle['input.datasource.default.timeseries.title'] || 'Counts');
                        }

                        configuration.callback(values);
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
