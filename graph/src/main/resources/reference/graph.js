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
 * Collection of Javascript functions used to manipulate ESSENCE graphs using AJAX
 *
 */

var optionWindows = new Object();

// TODO fix cases where a non-interactive graph which gets some graph options changed does not 
// retain those changed values upon making the graph interactive
function makeInteractiveGraph(address, imageMapName, graphDataId) {
    var graphURL = address + "?graphDataId=" + graphDataId;
    var graphDiv = document.getElementById("graphDiv" + imageMapName);
    var linkDiv = document.getElementById("linkDiv" + imageMapName);

    Ext.Ajax.request({
        url: graphURL + "&getImageMap=true",
        success: function (objServerResponse) {
            // reload the graph image map
            graphDiv.innerHTML = objServerResponse.responseText;
            // reload the graph
            graphDiv.innerHTML += "<img src=\"" + graphURL + "\" usemap=\"#" + imageMapName + "\"/>";
            // clear the "switch to interactive view" link
            linkDiv.innerHTML = "";
        },
        failure: function (objServerResponse) {
        }
    });
}

function showTimeSeriesGraphOptions(address, imageMapName, graphDataId, graphTitleVal, xAxisLabelVal, yAxisLabelVal, yAxisMinVal, yAxisMaxVal, dataSeriesJSON) {
    var graphId = "graph" + imageMapName;
    var treeData = new Array();
    dataSeriesJSON = eval(dataSeriesJSON);

    for (var i = 0; i < dataSeriesJSON.length; i++) {
        if (dataSeriesJSON[i].displayAlerts) {
            var seriesName = dataSeriesJSON[i].seriesName;
            var parentAry = new Array();
            var childrenAry = new Array();

            while (i < dataSeriesJSON.length && dataSeriesJSON[i].seriesName == seriesName) {
                var childAry = new Array();
                childAry["id"] = "checkbox" + graphId + i;
                childAry["name"] = "checkbox" + graphId + i;
                childAry["text"] = dataSeriesJSON[i].displayName;
                childAry["leaf"] = true;
                childAry["checked"] = true;
                childAry["value"] = "1";
                childrenAry.push(childAry);
                i++;
            }

            parentAry["text"] = seriesName;
            parentAry["checked"] = true;
            parentAry["children"] = childrenAry;
            treeData.push(parentAry);
            i--; // we still have to add the next element in the JSON
        } else {
            var parentAry = new Array();
            parentAry["id"] = "checkbox" + graphId + i;
            parentAry["name"] = "checkbox" + graphId + i;
            parentAry["text"] = dataSeriesJSON[i].seriesName;
            parentAry["leaf"] = true;
            parentAry["checked"] = true;
            parentAry["value"] = "1";
            treeData.push(parentAry);
        }
    }

    if (!optionWindows[graphId]) {
        optionWindows[graphId] = new Ext.Window({
            title: "Graph Options",
            layout: "fit",
            modal: true,
            width: 389,
            height: 375,
            closeAction: "hide",
            plain: true,
            items: [
                {
                    xtype: "form",
                    defaultType: "textfield",
                    frame: true,
                    defaults: { labelStyle: "width:150px;", style: "width:200px" },
                    items: [
                        {
                            id: graphId + "graphTitle",
                            fieldLabel: "Graph Title",
                            value: graphTitleVal
                        },
                        {
                            id: graphId + "xAxisLabel",
                            fieldLabel: "X Axis Title",
                            value: xAxisLabelVal
                        },
                        {
                            id: graphId + "yAxisLabel",
                            fieldLabel: "Y Axis Title",
                            value: yAxisLabelVal
                        },
                        {
                            id: graphId + "yAxisMin",
                            fieldLabel: "Y Axis Scale - Minimum",
                            value: yAxisMinVal.toFixed(2),
                            allowBlank: false,
                            validator: function () {
                                return isNumeric(this.getValue());
                            }
                        },
                        {
                            id: graphId + "yAxisMax",
                            fieldLabel: "Y Axis Scale - Maximum",
                            value: yAxisMaxVal.toFixed(2),
                            allowBlank: false,
                            validator: function () {
                                return isNumeric(this.getValue());
                            }
                        },
                        {
                            id: graphId + "tree",
                            xtype: "checktreepanel",
                            //isFormField:true,
                            //fieldLabel:"Display Series",
                            width: 361,
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
                        {
                            text: "Reset",
                            handler: function () {
                                resetTimeSeriesGraphOptions(graphId);
                            }
                        },
                        {
                            text: "Accept",
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

                                optionWindows[graphId].hide();
                                graphTitle.value = graphTitle.getValue();
                                xAxisLabel.value = xAxisLabel.getValue();
                                yAxisLabel.value = yAxisLabel.getValue();
                                yAxisMin.value = yAxisMin.getValue();
                                yAxisMax.value = yAxisMax.getValue();
                                tree.acceptValue();

                                var timeSeriesGraphURL = address + "?graphDataId=" + escape(graphDataId) + "&graphTitle=" +
                                    escape(graphTitle.value) + "&xAxisLabel=" + escape(xAxisLabel.value) + "&yAxisLabel=" +
                                    escape(yAxisLabel.value) + "&yAxisMin=" + yAxisMin.value + "&yAxisMax=" + yAxisMax.value +
                                    "&dataDisplayKey=" + tree.getValue();
                                var graphDiv = document.getElementById("graphDiv" + imageMapName);

                                if (graphDiv.getElementsByTagName("map").length > 0) {
                                    Ext.Ajax.request({
                                        url: timeSeriesGraphURL + "&getImageMap=true",
                                        success: function (objServerResponse) {
                                            // reload the graph image map
                                            graphDiv.innerHTML = objServerResponse.responseText;
                                            // reload the graph
                                            graphDiv.innerHTML += "<img src=\"" + timeSeriesGraphURL + "\" usemap=\"#" +
                                                imageMapName + "\"/>";
                                        },
                                        failure: function (objServerResponse) {
                                        }
                                    });
                                } else {
                                    // just reload the graph
                                    graphDiv.innerHTML = "<img src=\"" + timeSeriesGraphURL +
                                        "\" title=\"Switch to Interactive View\" onclick=\"makeInteractiveTimeSeriesGraph('" +
                                        address + "', '" + imageMapName + "', '" + graphDataId + "');\"/>";
                                }
                            }
                        },
                        {
                            text: "Cancel",
                            handler: function () {
                                optionWindows[graphId].hide();
                                resetTimeSeriesGraphOptions(graphId);
                            }
                        }
                    ]
                }
            ]
        });
    }

    optionWindows[graphId].show();
}

function resetTimeSeriesGraphOptions(graphId) {
    var graphTitle = Ext.getCmp(graphId + "graphTitle");
    var xAxisLabel = Ext.getCmp(graphId + "xAxisLabel");
    var yAxisLabel = Ext.getCmp(graphId + "yAxisLabel");
    var yAxisMin = Ext.getCmp(graphId + "yAxisMin");
    var yAxisMax = Ext.getCmp(graphId + "yAxisMax");
    var tree = Ext.getCmp(graphId + "tree");

    graphTitle.setValue(graphTitle.value);
    xAxisLabel.setValue(xAxisLabel.value);
    yAxisLabel.setValue(yAxisLabel.value);
    yAxisMin.setValue(yAxisMin.value);
    yAxisMax.setValue(yAxisMax.value);
    tree.resetValue();
}

function isNumeric(input) {
    if ((input - 0) == input && input >= 0 && input.length > 0) {
        return true;
    } else {
        return false;
    }
}
