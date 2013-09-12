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

define(function () { // TODO depend on Ext

    // need to save classes in closure b/c that's the only way to reference superclass
    /**
     * Preview window used by CsvUploadWindow
     */
    var previewWindowClass = Ext.extend(Ext.Window, {
        constructor: function (config) {
            Ext.apply(this, config);

            var previewWindow = this;

            this.cols = (function (previewWindow) {
                var cols = [];
                for (var i = 0; i < previewWindow.fieldNames.length; i++) {
                    cols.push({
                        header: previewWindow.fieldNames[i],
                        dataIndex: previewWindow.fields[i],
                        id: previewWindow.fields[i],
                        width: 100,
                        sortable: true
                    });
                }

                return cols;
            })(previewWindow);

            this.gridPanel = new Ext.grid.GridPanel({
                store: new Ext.data.JsonStore({
                    idIndex: 0,
                    fields: this.fields,
                    root: 'data',
                    data: config
                }),
                columns: this.cols,
                stripeRows: true,
                height: 350,
                width: 600,
                // config options for stateful behavior
                stateful: true,
                stateId: 'grid'
            });

            config = Ext.apply({
                title: messagesBundle['input.datasource.default.Preview'] || 'Preview',
                layout: 'fit',
                width: 610,
                height: 360,
                modal: true,
                items: [this.gridPanel],
                buttons:
                    [
                        {
                            text: messagesBundle['input.datasource.default.Upload'] || 'Upload',
                            handler: function () {
                                previewWindow.previewCallback();
                                previewWindow.close();
                            }
                        }
                    ]
            }, config);

            previewWindowClass.superclass.constructor.call(this, config);
        }
    });

    var uploadWindowClass = Ext.extend(Ext.Window, {
        constructor: function (config) {
            Ext.apply(this, config);

            var uploadWindow = this;

            this.uploadForm = new Ext.FormPanel({
                fileUpload: true,
                itemId: 'uploadForm',
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
                        emptyText: messagesBundle['uploadCSVForm.file.qtip'] || 'Select a CSV file to import data',
                        fieldLabel: messagesBundle['uploadCSVForm.file'] || 'CSV File',
                        name: 'file',
                        buttonText: messagesBundle['input.datasource.default.Browse'] || 'Browse...'
                    },
                    {
                        xtype: 'textfield',
                        emptyText: messagesBundle['uploadCSVForm.delimiter.qtip'] || 'Select a delimiter',
                        fieldLabel: messagesBundle['uploadCSVForm.delimiter'] || 'Delimiter',
                        name: 'delimiter',
                        value: uploadWindow.delimiter || ','
                    },
                    {
                        xtype: 'textfield',
                        emptyText: messagesBundle['uploadCSVForm.qualifier.qtip'] || 'Select a qualifier',
                        fieldLabel: messagesBundle['uploadCSVForm.qualifier'] || 'Qualifier',
                        name: 'qualifier',
                        value: uploadWindow.qualifier || '"'
                    },
                    {
                        xtype: 'checkbox',
                        emptyText: messagesBundle['uploadCSVForm.headerRow.qtip'] || 'Check to remove the first row',
                        fieldLabel: messagesBundle['uploadCSVForm.headerRow'] || 'Remove Header?',
                        name: 'headerRow',
                        checked: uploadWindow.headerRow || true
                    }
                ]
            });

            var uploadForm = this.uploadForm;

            config = Ext.apply({
                title: messagesBundle['input.datasource.default.uploadTitle'] || 'Upload Data',
                layout: 'fit',
                width: 450,
                height: 190,
                modal: true,
                items: [this.uploadForm],
                buttons: [
                    {
                        text: messagesBundle['input.datasource.default.preview'] || 'Preview',
                        handler: function () {
                            var form = uploadForm.getForm();

                            if (!form.isValid()) {
                                return;
                            }

                            try {
                                form.submit({
                                    url: OE.util.getUrl('/file'),
                                    params: {
                                        dsId: uploadWindow.dataSource,
                                        fields: uploadWindow.fields.toString(),
                                        rowsToSkip: form.getFieldValues().headerRow ? 1 : 0 ,
                                        numRowsToRead: uploadWindow.numRowsToRead,

                                        // Unfortunately, there's no standard way to tell a server what the underlying
                                        // content type is for a file upload. We also can't use a custom header, since
                                        // file uploads are not done over Ajax
                                        '_uploadContentType': 'text/csv'
                                    },
                                    waitMsg: messagesBundle['input.datasource.default.uploadWaitMessage'] ||
                                        'Uploading the file...',
                                    failure: function (form, action) {
                                        var response = Ext.decode(action.response.responseText);
                                        Ext.Msg.show({
                                            buttons: Ext.Msg.OK,
                                            icon: Ext.Msg.ERROR,
                                            title: 'Error',
                                            msg: response.message,
                                            fn: function () {
                                                uploadWindow.close();
                                            }
                                        });
                                    },
                                    success: function (form, action) {
                                        var response = Ext.decode(action.response.responseText);
                                        var config = {
                                            fields: uploadWindow.fields,
                                            fieldNames: uploadWindow.fieldNames,
                                            data: response.rows,
                                            previewCallback: function () {
                                                uploadWindow.close();
                                                uploadWindow.successCallback(response);
                                            }
                                        };
                                        new previewWindowClass(config).show();
                                    }
                                });
                            } catch (e) {
                                console.error(e); // don't worry, we have polyfills for IE
                                uploadWindow.close();
                            }
                        }
                    }
                ]
            }, config);

            uploadWindowClass.superclass.constructor.call(this, config);
        }
    });

    return {
        'CsvUploadWindow': uploadWindowClass
    };
});
