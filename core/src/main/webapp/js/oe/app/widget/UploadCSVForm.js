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

OE.uploadCSVForm = function (configuration) {
    var uploadForm = new Ext.form.FormPanel({
        fileUpload: true,
        itemId: 'uploadForm',
        headerCfg: {'Content-type': 'multipart/form-data'},
        frame: true,
        bodyStyle: 'padding: 10px 10px 10px 10px;',
        defaults: {
            anchor: '95%',
            allowBlank: false,
            labelWidth: 75,
            msgTarget: 'side'
        },
        items: [
            {
                xtype: 'fileuploadfield',
                emptyText: messagesBundle['input.datasource.default.ImportFile.qtip'] || 'Select a CSV file to import data',
                fieldLabel: messagesBundle['input.datasource.default.ImportFile'] || 'CSV File',
                name: 'file',
                buttonText: messagesBundle['input.datasource.default.Browse'] || 'Browse...'
            }
        ]
    });
    var win = new Ext.Window({
        title: messagesBundle['input.datasource.default.uploadTitle'] || 'Upload Data',
        layout: 'fit',
        width: 450,
        height: 120,
        modal: true,
        items: [ uploadForm ],
        buttons: [
            {
                text: messagesBundle['input.datasource.default.upload'] || 'Upload',
                handler: function (button) {
                    var form = uploadForm.getForm();

                    if (form.isValid()) {
                        try {
                            form.submit({
                                url: '../../oe/input/importCSV',
                                params: {dsId: configuration.dataSource, fields: configuration.fields,
                                    delimiter: configuration.delimiter || ',', qualifier: configuration.qualifier || '"',
                                    rowsToSkip: configuration.rowsToSkip || 0, numRowsToRead: configuration.numRowsToRead},
                                waitMsg: messagesBundle['input.datasource.default.uploadWaitMessage'] || 'Uploading the file...',
                                failure: function (form, action) {
                                    var response = Ext.decode(action.response.responseText);
                                    Ext.Msg.show({
                                        buttons: Ext.Msg.OK,
                                        icon: Ext.Msg.ERROR,
                                        title: 'Error',
                                        msg: response.message,
                                        fn: function () {
                                            win.close();
                                        }
                                    });
                                },
                                success: function (form, action) {
                                    var response = Ext.decode(action.response.responseText);
                                    win.close();
                                    configuration.successCallback(response);
                                }
                            });
                        } catch (e) {
                            win.close();
                        }
                    }
                }
            },
            {
                text: messagesBundle['button.reset'] || 'Reset',
                handler: function () {
                    uploadForm.getForm().reset();
                }
            }
        ]
    });
    return win;

};
