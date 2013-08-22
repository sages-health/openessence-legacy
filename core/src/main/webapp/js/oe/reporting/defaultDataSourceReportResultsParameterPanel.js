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

Ext.namespace("OE.report.datasource.result.parameter");

/**
 * Panel to display query parameters (does not show parameters where value is an empty string)
 */
OE.report.datasource.result.parameter.init = function (configuration) {

    var parameters = configuration.parameters;

    // Builds grid and values arrays from query parameters
    var fields = [];
    var columns = [];
    var values = [];

    var dataSource = parameters.dsId;
    for (var key in parameters) {
        if (key != 'displayVals' && parameters[key] !== "" && parameters.hasOwnProperty(key)) {
            // Attempts to load label from dimensions bundle using a qualified (data source name) "dot" dimension name, then
            // will try to load from dimensions bundle just using the dimension name, finally will just use the dimension name.
            var header = dimensionsBundle[dataSource + '.' + key] || dimensionsBundle[key] || key;
            columns.push({dataIndex: key, header: header});
            fields.push(key);
            if (parameters.displayVals) {
                values.push(parameters.displayVals[key] ? parameters.displayVals[key] : parameters[key]);
            }
            else {
                values.push(parameters[key]);
            }
        }
    }

    return new Ext.Panel({
        itemId: 'parameters-panel',
        title: messagesBundle['panel.parameters.header'],
        cls: 'reportPanel',
        layout: 'fit',
        region: 'north',
        cmargins: '0 0 6 0',
        border: false,
        frame: true,
        collapsible: true,
        collapsed: true,
        plugins: new Ext.ux.collapsedPanelTitlePlugin(),
        floatable: false,
        height: 127,
        items: {
            xtype: 'grid',
            store: new Ext.data.ArrayStore({
                fields: fields,
                data: [values]
            }),
            cm: new Ext.grid.ColumnModel({
                defaults: {
                    width: 150,
                    sortable: false
                },
                columns: columns
            }),
            sm: new Ext.grid.RowSelectionModel({
                singleSelect: true
            }),
            border: false,
            loadMask: new Ext.LoadMask(Ext.getBody(), {
                msg: messagesBundle['main.loadmask']
            }),
            tbar: new Ext.Toolbar({
                buttons: [
                    {
                        text: messagesBundle['panel.parameters.button'],
                        tooltip: messagesBundle['panel.parameters.buttonToolTip'],
                        handler: function () {
                            configuration.callback(parameters);
                        },
                        scope: configuration
                    }
                ]
            }),
            width: (150 * fields.length),
            anchor: '100% 100%'
        }
    });
};
