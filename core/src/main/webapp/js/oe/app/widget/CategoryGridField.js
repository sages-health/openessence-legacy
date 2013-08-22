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

Ext.ns('OE');

/**
 * An EditorGridPanel for categories. "Categories" are multi-valued
 * relationships, commonly found in aggregate data, that are pivoted. For
 * example, a given report might include 25 different possible diseases (AKA
 * categories) which can be changed any time. This component handles the
 * extraction of these pivot-able categories into a grid that can be edited by
 * the user.
 */
OE.CategoryGridField = Ext.extend(Ext.grid.EditorGridPanel, {

    constructor: function (config) {
        var me = this;

        this.reportId = config.formConfig.itemId || null;

        var matchTerm = config.matchTerm || 'category';

        config = Ext.applyIf(config, {
            /**
             * regex that identifies categories. This will be matched against each detail dimension's name
             */
            categoryMask: new RegExp('^' + matchTerm + '(\\d+)$'),
            /**
             * These fields must be the dimension IDs corresponding to your
             * category data. Very hackish I know, but data sources don't
             * provide any mechanism for identifying these
             */
            categoryIdField: 'c1_CategoryId',
            categoryValueField: 'c1_Value',
            reportIdField: 'c1_ReportId',
            name: 'data'
        });

        var columns = [];
        var record = {}; // array of counts
        var categoryFields = []; // [{name: 1, type: 'int'}, ...]

        // get categories
        Ext.each(config.formConfig.data.detailDimensions, function (dimension) {
            var categoryId = config.categoryMask.exec(dimension.name);
            if (categoryId) {
                columns.push({
                    header: dimension.displayName,
                    tooltip: dimension.displayName,
                    dataIndex: categoryId[1],
                    width: config.columnWidth || 100,
                    align: 'right',
                    editor: new Ext.form.NumberField({
                        allowBlank: config.allowBlank || true,
                        allowNegative: config.allowNegative || false,
                        allowDecimals: config.allowDecimals || false,
                        minValue: config.minValue || 0,
                        maxValue: config.maxValue || 100000
                    })
                });
                categoryFields.push({name: categoryId[1], type: 'int'});
            }
        });

        if (config.formConfig.record && config.formConfig.record[config.name]) {
            // copy existing record's data
            Ext.each(config.formConfig.record[config.name], function (rec) {
                var categoryId = rec[config.categoryIdField];
                record[categoryId] = rec[config.categoryValueField];
            });
        }

        config = Ext.apply({
            height: 70,
            anchor: '100%',
            clicksToEdit: 1,
            enableColumnMove: false,
            enableHdMenu: false,
            columnLines: true,
            cm: new Ext.grid.ColumnModel(columns),
            store: new Ext.data.JsonStore({
                idIndex: 0,
                fields: categoryFields,
                autoLoad: true,
                root: 'data',
                data: {data: [record] }
            })
        }, config);

        OE.CategoryGridField.superclass.constructor.call(me, config);

        this.on('afterrender', function (me) {
            // add hidden field to store the grid's values
            me.ownerCt.ownerCt.add({
                xtype: 'hidden',
                name: config.name,
                getValue: function () {
                    var data = [];
                    if (me.getValue().length === 0) {
                        return Ext.encode(data);
                    }

                    var row = me.getValue()[0].data;
                    Ext.iterate(row, function (categoryId, value) {
                        if (value !== "") {
                            // OE-DS isn't smart enough to convert empty strings to nulls
                            var datum = {};
                            datum[me.reportIdField] = me.reportId;
                            datum[me.categoryIdField] = categoryId;
                            datum[me.categoryValueField] = value;
                            data.push(datum);
                        }
                    });
                    return Ext.encode(data);
                }
            });
        });
    },

    getValue: function () {
        return this.store.getRange();
    }
});

Ext.reg('categorygridfield', OE.CategoryGridField);
