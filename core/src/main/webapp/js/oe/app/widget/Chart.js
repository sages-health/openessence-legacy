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
 * An individual chart.
 */
Ext.ns('OE');

OE.Chart = Ext.extend(Ext.Panel, { // TODO refactor with Graph version, have generic Diagram parent class
    constructor: function (config) {
        config = Ext.apply(config, {
            title: config.title || messagesBundle['panel.chart.header'],
            layout: 'fit',
            cls: 'chartPanel',
            frame: true,
            border: true,
            autoScroll: true,
            tools: [
                {
                    id: 'save',
                    qtip: messagesBundle['graph.downloadChart'],
                    scope: this,
                    handler: function () {
                        this.showDownloadOptions();
                    }
                }
            ]
        });

        OE.Chart.superclass.constructor.call(this, config);

        OE.data.doAjaxRestricted({
            url: this.url,
            params: Ext.applyIf(this.parameters, this.parameters.filters), // TODO make chartJson accept explicit filters
            scope: this,
            onJsonSuccess: function (response) {
                if (response.graphs) {
                    var graphJson = response.graphs[this.chart.accumId][this.chart.dimensionId];

                    Ext.applyIf(this, graphJson); // sets a bunch of graph properties

                    this.body.update(graphJson.imageMap + '<img src="' + graphJson.imageUrl + '" usemap="#' +
                        graphJson.imageMapName + '"/>');
                }
            }
        });
    },

    showDownloadOptions: function () {
        var me = this;

        var graphId = "graph" + this.imageMapName;

        var imageTypeChangeHandler = function (checkbox, checked) {
            if (checked) {
                var downloadForm = Ext.getCmp(checkbox.graphId + "downloadForm");
                var isDisabled = (checkbox.inputValue === "emf" || checkbox.inputValue === "eps");

                downloadForm.items.each(function (item) {
                    var itemName = item.name;

                    if (itemName && itemName.substr(0, 10) === "resolution") {
                        item.setDisabled(isDisabled);
                    }
                });
            }
        };

        var downloadWindow = null;
        downloadWindow = new Ext.Window({
            title: messagesBundle['graph.downloadOptions'],
            modal: true,
            closeAction: "hide",
            width: 400,
            height: 210,
            plain: true,
            items: [
                {
                    xtype: "form",
                    id: graphId + "downloadForm",
                    url: me.imageUrl,
                    defaultType: "radiogroup",
                    frame: true,
                    defaults: {
                        style: "width:200px"
                    },
                    items: [
                        {
                            xtype: "textfield",
                            id: "getHighResFile",
                            name: "getHighResFile",
                            hidden: true,
                            value: 1
                        },
                        {
                            fieldLabel: messagesBundle['graph.imageType'] || "Image type",
                            columns: 3,
                            items: [
                                {    name: "imageType", inputValue: "png", boxLabel: 'PNG',
                                    graphId: graphId, handler: imageTypeChangeHandler, checked: true
                                }
                            ]
                        },
                        {
                            fieldLabel: messagesBundle['graph.resolution'] || "Resolution (dpi)",
                            columns: 3,
                            items: [
                                { name: "resolution", inputValue: "100", boxLabel: '100', checked: true},
                                { name: "resolution", inputValue: "150", boxLabel: '150'},
                                { name: "resolution", inputValue: "200", boxLabel: '200'},
                                { name: "resolution", inputValue: "250", boxLabel: '250'},
                                { name: "resolution", inputValue: "300", boxLabel: '300'},
                                { name: "resolution", inputValue: "350", boxLabel: '350'}
                            ]
                        }
                    ],

                    buttons: [
                        {
                            text: messagesBundle['graph.download'],
                            formBind: true,
                            scope: this,
                            handler: function () {
                                var downloadForm = Ext.getCmp(graphId + "downloadForm").getForm();
                                var domForm = downloadForm.getEl().dom;

                                // force submit download form in the "old" html way
                                // this allows the browser to open up a "Save As..."
                                // dialog without submitting to an iframe or a new
                                // blank window
                                domForm.action = this.imageUrl;
                                domForm.submit();

                                downloadWindow.close();
                            }
                        },
                        {
                            text: messagesBundle['input.datasource.default.cancel'],
                            handler: function () {
                                downloadWindow.close();
                            }
                        }
                    ]
                },
                {
                    xtype: "label",
                    text: messagesBundle['graph.highResDownloadWarning']
                }
            ]
        }).show();
    }
});
