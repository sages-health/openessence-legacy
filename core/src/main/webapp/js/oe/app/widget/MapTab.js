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

OE.MapTab = Ext.extend(Ext.Panel, {

    /**
     * Config to pass to getMapData
     */
    getMapData: null,

    constructor: function (config) {
        this.mapPanel = new OE.report.datasource.map.Panel({
            id: 'ext-comp-' + (++Ext.Component.AUTO_ID) + '-map', // TODO is this needed?
            map: {},
            panel: {}
        });

        config = Ext.apply({
            cls: 'reportPanel',
            title: messagesBundle['panel.map.header'],
            closable: true,
            layout: 'fit',
            items: [ this.mapPanel ]
        }, config);

        OE.MapTab.superclass.constructor.call(this, config);
    },

    initComponent: function () {
        OE.MapTab.superclass.initComponent.call(this);
        this.initMap();
    },

    /**
     * Subclasses can override this method to initialize the map differently.
     */
    initMap: function () {
        var me = this;

        this.mapPanel.getMapData(this.getMapData, function (response) {
            if (response.dsMapData) {
                Ext.each(response.dsMapData.baseLayers, function (layer) {
                    me.mapPanel.createWMSBaseLayer(response, layer.name, layer.getMapQuery, layer.layerOptions);
                });

                var thresholds = null;
                Ext.each(response.dsMapData.overlays, function (layer) {

                    // Filter must match selected accumulation if layer has one configured
                    if (!Ext.isDefined(layer.accumulationId) ||
                        !Ext.isDefined(me.getMapData.accumId) || // use all if none selected
                        layer.accumulationId === me.getMapData.accumId) {

                        // TODO: for now, default to first overlay for legend creation (later specify which to use in map info)
                        if (!thresholds && layer.thresholds) {
                            thresholds = layer.thresholds;
                        }

                        me.mapPanel.createWMSOverlay(response, layer.name, Ext.apply(layer.getMapQuery, {
                            cql_filter: layer.cql_filter
                        }), layer.layerOptions, {
                            featureSelected: function (feature) {
                                me.mapPanel.displayFeaturePopup({title: null, feature: feature, layer: layer});
                            }
                        });
                    }
                });

                me.mapPanel.createLegend({html: me.mapPanel.createLegendHTML(thresholds)});
            }

            // remove load mask layer
            me.mapPanel.map.removeLayer(me.mapPanel.map.baseLayer);
            me.mapPanel.map.zoomToExtent(me.mapPanel.extent, true);
        });
    }
});
