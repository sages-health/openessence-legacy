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

OE.GraphPanel = Ext.extend(Ext.Panel, { // TODO extend OE.DiagramPanel, refactor with chart version
    constructor: function (config) {
        var chartPanel = new Ext.Panel({
            itemId: 'chart-panel',
            region: 'center',
            layout: 'fit',
            flex: 2,
            border: false,
            boxMinHeight: 150
        });
        this.chartPanel = chartPanel;

        config = Ext.apply(config, {
            layout: 'border',
            title: config.title || messagesBundle['panel.timeseries.header'] + ' ' + config.index,
            closable: true,
            items: [chartPanel]
        });

        OE.GraphPanel.superclass.constructor.call(this, config);

        this.loadChart(config);
    },

    loadChart: function () {
        var filters = this.parameters.filters;

        // hack to not include filters property
        var params = Ext.applyIf({filters: []}, this.parameters);
        delete params.filters;

        params.graphExpectedValues = false;

        Ext.apply(params, filters);
        Ext.applyIf(params, {height: 460});

        var me = this;
        OE.data.doAjaxRestricted({
            url: this.url,
            method: 'GET',
            params: params,
            onJsonSuccess: function (response) {
                me.createTimeSeriesPanel(response);
            },
            onRelogin: {callback: this.loadChart, args: []}
        });
    },

    createTimeSeriesPanel: function (response) {
        var chartPanelContent;
        var chartPanelGraph;

        if (response.success === false) {
            Ext.MessageBox.show({
                title: messagesBundle['input.datasource.error'],
                msg: response.message || messagesBundle['input.datasource.error.default'],
                buttons: Ext.MessageBox.OK,
                icon: Ext.MessageBox.ERROR
            });
            return;
        }
        if (response.html) {
            chartPanelContent = this.chartPanel.getComponent('chart-panel-content');
            if (chartPanelContent) {
                //refresh
                chartPanelGraph = chartPanelContent.getComponent('chart-panel-graph');
                if (chartPanelGraph) {
                    chartPanelGraph.body.update(response.html);
                }
                //tack on configuration information
                this.graphConfiguration = response.graphConfiguration || {};

            } else {
                //initial create
                chartPanelGraph = this.createChartPanelGraph(response.html);

                //tack on configuration information
                this.graphConfiguration = response.graphConfiguration || {};

                var items = [chartPanelGraph];

                if (response.details) {
                    var chartPanelDetails = OE.report.datasource.graph.details.init(response);
                    items.push(chartPanelDetails);
                }

                chartPanelContent = new Ext.Panel({
                    itemId: 'chart-panel-content',
                    layout: 'border',
                    flex: 2,
                    border: false,
                    boxMinHeight: 150,
                    autoScroll: true,
                    items: items
                });
                this.chartPanel.add(chartPanelContent);
                this.chartPanel.doLayout();
            }
        }
    },

    createChartPanelGraph: function (html) {
        var me = this;

        var chartPanelGraph = new Ext.Panel({
            title: messagesBundle['panel.chart.header'],
            itemId: 'chart-panel-graph',
            cls: 'reportPanel ',
            region: 'center',
            flex: 2,
            border: false,
            frame: true,
            boxMinHeight: 150,
            autoScroll: true,
            tools: [
                {
                    id: 'refresh',
                    qtip: messagesBundle['graph.resize'],
                    handler: function (event, toolEl, panel) {
                        //resize
                        Ext.apply(me.parameters, {height: panel.getHeight() - 45, width: panel.getWidth() - 20});
                        me.loadChart();
                    }
                },
                {
                    id: 'gear',
                    qtip: messagesBundle['graph.editProperties'],
                    handler: function () {
                        me.showTimeSeriesGraphOptions();
                    }
                },
                {
                    id: 'save',
                    qtip: messagesBundle['graph.downloadChart'],
                    handler: function () {
                        me.showDownloadOptions();
                    }
                }
            ],
            html: html
        });
        return chartPanelGraph;
    },

    showTimeSeriesGraphOptions: function () {
        var me = this;
        var graphId = "graph" + this.imageMapName;
        var treeData = [];
        var parentAry = [];

        if (!Ext.isDefined(me.graphConfiguration.dataDisplayKey)) {
            me.graphConfiguration.dataDisplayKey = "";
        }

        var dataSeriesJSON = this.graphConfiguration.dataSeriesJSON;

        for (var i = 0; i < dataSeriesJSON.length; i++) {
            if (dataSeriesJSON[i].displayAlerts) {
                var seriesName = dataSeriesJSON[i].seriesName;
                var childrenAry = [];

                // parent should be checked iff all its children are
                var allChildrenChecked = true;

                while (i <= dataSeriesJSON.length && dataSeriesJSON[i].seriesName == seriesName) {
                    var childAry = [];
                    childAry.id = "checkbox" + graphId + i;
                    childAry.name = "checkbox" + graphId + i;
                    childAry.text = dataSeriesJSON[i].displayName;
                    childAry.leaf = true;

                    // restore state of check
                    if (Ext.isDefined(me.graphConfiguration.dataDisplayKey[i])) {
                        var checked = me.graphConfiguration.dataDisplayKey[i] !== "0";
                        childAry.checked = checked;
                        if (!checked) {
                            allChildrenChecked = false;
                        }
                        childAry.value = me.graphConfiguration.dataDisplayKey[i];
                    } else {
                        childAry.checked = true;
                        childAry.value = "1";
                    }

                    childrenAry.push(childAry);
                    i++;
                }

                parentAry.text = seriesName;
                parentAry.checked = allChildrenChecked;
                parentAry.children = childrenAry;
                parentAry.expanded = true;
                treeData.push(parentAry);
                i--; // we still have to add the next element in the JSON
            } else {
                parentAry.id = "checkbox" + graphId + i;
                parentAry.name = "checkbox" + graphId + i;
                parentAry.text = dataSeriesJSON[i].seriesName;
                parentAry.leaf = true;

                // restore state of check
                if (Ext.isDefined(me.graphConfiguration.dataDisplayKey[i])) {
                    parentAry.checked = me.graphConfiguration.dataDisplayKey[i] !== "0";
                    parentAry.value = me.graphConfiguration.dataDisplayKey[i];
                } else if (i + 1 == dataSeriesJSON.length){
                    parentAry.checked = false;
                    parentAry.value = "0";
                } else {
                    parentAry.checked = true;
                    parentAry.value = "1";
                }
                treeData.push(parentAry);
            }
        }
        var graphOptionsWindow = null;
        graphOptionsWindow = new Ext.Window({
            title: messagesBundle['graph.options'],
            layout: "fit",
            modal: true,
            width: 387,
            height: 375,
            // ZSM: Do not set closeAction to hide.
            // The form was not rendering correctly if we hide the window
            // This setting works fine for downloadOption window (may be because form is short or layout is diff)
            // closeAction:"hide",
            plain: true,
            items: [
                {
                    xtype: "form",
                    itemId: 'graphOptionsForm',
                    defaultType: "textfield",
                    frame: true,
                    defaults: { labelStyle: "width:150px;", style: "width:200px" },
                    items: [
                        {
                            id: graphId + "graphTitle",
                            fieldLabel: messagesBundle['graph.title'],
                            value: me.graphConfiguration.graphTitle
                        },
                        {
                            id: graphId + "xAxisLabel",
                            fieldLabel: messagesBundle['graph.xLabel'],
                            value: me.graphConfiguration.xAxisLabel
                        },
                        {
                            id: graphId + "yAxisLabel",
                            fieldLabel: messagesBundle['graph.yLabel'],
                            value: me.graphConfiguration.yAxisLabel
                        },
                        {
                            id: graphId + "yAxisMin",
                            xtype: 'numberfield',
                            fieldLabel: messagesBundle['graph.yScaleMin'],
                            value: me.graphConfiguration.yAxisMin.toFixed(2),
                            allowBlank: false,
                            allowNegative: false
                        },
                        {
                            id: graphId + "yAxisMax",
                            xtype: 'numberfield',
                            fieldLabel: messagesBundle['graph.yScaleMax'],
                            value: me.graphConfiguration.yAxisMax.toFixed(2),
                            allowBlank: false,
                            allowNegative: false
                        },
                        {
                            id: graphId + "tree",
                            xtype: 'graphConfigTreePanel',
                            width: 353,
                            height: 160,
                            autoScroll: true,
                            border: true,
                            bodyStyle: "background-color:white; border:1px solid #B5B8C8;",
                            rootVisible: false,
                            root: {
                                nodeType: "async",
                                text: "root",
                                id: graphId + "root",
                                expanded: true,
                                uiProvider: false,
                                children: treeData
                            }
                        }
                    ],
                    buttons: [
                        {// TODO get rid of reset (cancel is already basically a reset)
                            text: messagesBundle['button.reset'],
                            handler: function () {
                                graphOptionsWindow.getComponent('graphOptionsForm').getForm().reset();
                                Ext.getCmp(graphId + "tree").resetCheckedValues();
                            }
                        },
                        {
                            text: messagesBundle['input.datasource.default.ok'],
                            handler: function () {
                                var graphTitle = Ext.getCmp(graphId + "graphTitle");
                                var xAxisLabel = Ext.getCmp(graphId + "xAxisLabel");
                                var yAxisLabel = Ext.getCmp(graphId + "yAxisLabel");
                                var yAxisMin = Ext.getCmp(graphId + "yAxisMin");
                                var yAxisMax = Ext.getCmp(graphId + "yAxisMax");
                                var tree = Ext.getCmp(graphId + "tree");

                                if (!yAxisMin.validate()) {
                                    alert("Please enter a valid positive number for the y-axis scale minimum value");
                                    yAxisMin.focus(true);
                                    return;
                                }

                                if (!yAxisMax.validate()) {
                                    alert("Please enter a valid positive number for the y-axis scale maximum value");
                                    yAxisMax.focus(true);
                                    return;
                                }

                                if ((yAxisMin.getValue() - 0) >= (yAxisMax.getValue() - 0)) {
                                    alert("Please enter a y-axis scale maximum value that is larger than the minimum");
                                    yAxisMax.focus(true);
                                    return;
                                }

                                graphTitle.value = graphTitle.getValue();
                                xAxisLabel.value = xAxisLabel.getValue();
                                yAxisLabel.value = yAxisLabel.getValue();
                                yAxisMin.value = yAxisMin.getValue();
                                yAxisMax.value = yAxisMax.getValue();
                                tree.acceptCheckedValues();

                                var timeSeriesGraphURL = Ext.urlAppend(me.graphConfiguration.address, Ext.urlEncode({
                                    graphDataId: me.graphConfiguration.graphDataId,
                                    graphTitle: graphTitle.value,
                                    xAxisLabel: xAxisLabel.value,
                                    yAxisLabel: yAxisLabel.value,
                                    yAxisMin: yAxisMin.value,
                                    yAxisMax: yAxisMax.value,
                                    dataDisplayKey: tree.getCheckedValues()
                                }));

                                Ext.apply(me.graphConfiguration, {
                                    graphDataId: me.graphConfiguration.graphDataId,
                                    graphTitle: graphTitle.value,
                                    xAxisLabel: xAxisLabel.value,
                                    yAxisLabel: yAxisLabel.value,
                                    yAxisMin: yAxisMin.value,
                                    yAxisMax: yAxisMax.value,
                                    dataDisplayKey: tree.getCheckedValues()
                                });

                                var graphDiv = document.getElementById("graphDiv" + me.graphConfiguration.imageMapName);

                                OE.data.doAjaxRestricted({
                                    url: timeSeriesGraphURL + "&getImageMap=true",
                                    success: function (objServerResponse) {
                                        // reload the graph image map
                                        var imageHTML = objServerResponse.responseText;

                                        // reload the graph
                                        var lastDisplayKeyIndex = me.graphConfiguration.dataDisplayKey.length - 1;
                                        var dataDisplayKey = me.graphConfiguration.dataDisplayKey[lastDisplayKeyIndex];
                                        var expectedCheckboxValue = dataDisplayKey.charAt(dataDisplayKey.length - 1);
                                        imageHTML += '<img src=\"' + timeSeriesGraphURL + '&graphExpectedValues=';

                                        if (expectedCheckboxValue == '0') {
                                            imageHTML += 'false';
                                        } else {
                                            imageHTML += 'true';
                                        }

                                        imageHTML += '" usemap="#' + me.graphConfiguration.imageMapName + '"/>';
                                        graphDiv.innerHTML = imageHTML;
                                    },
                                    failure: function (objServerResponse) {

                                    }
                                }, false);

                                graphOptionsWindow.close();
                            }
                        },
                        {
                            text: messagesBundle['input.datasource.default.cancel'],
                            handler: function () {
                                graphOptionsWindow.close();
                            }
                        }
                    ]
                }
            ]
        }).show();
    },

    showDownloadOptions: function (address, imageMapName) {
        var me = this;
        var graphId = "graph" + imageMapName;

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
                    url: this.graphConfiguration.address,
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
                            handler: function () {
                                var downloadForm = Ext.getCmp(graphId + "downloadForm").getForm();
                                var domForm = downloadForm.getEl().dom;

                                // force submit download form in the "old" html way
                                // this allows the browser to open up a "Save As..."
                                // dialog without submitting to an iframe or a new
                                // blank window
                                domForm.action = Ext.urlAppend(me.graphConfiguration.address, Ext.urlEncode({
                                    graphDataId: me.graphConfiguration.graphDataId
                                }));
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
