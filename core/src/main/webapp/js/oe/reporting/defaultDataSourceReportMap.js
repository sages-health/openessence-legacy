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

Ext.namespace('OE.report.datasource.map');

OE.report.datasource.map.Panel = Ext.extend(GeoExt.MapPanel, {

    /**
     * Default extent, override in map meta data
     *
     * left, bottom, right, top
     */
    extent: new OpenLayers.Bounds(-180, -90, 180, 90),

    /**
     * Legend window
     */
    legend: null,

    constructor: function (config) {
        var map = new OpenLayers.Map(this.id + '-Map', Ext.apply(config.map || {}, {
            allOverlays: false,
            maxExtent: this.extent,
            controls: [
                new OpenLayers.Control.Navigation(this.id + '-Navigation'),
                new OpenLayers.Control.ScaleLine(this.id + '-ScaleLine'),
                new OpenLayers.Control.MousePosition(this.id + '-MousePosition'),
                new OpenLayers.Control.KeyboardDefaults(this.id + '-KeyboardDefaults'),
                new OpenLayers.Control.LayerSwitcher(this.id + '-LayerSwitcher')
            ],
            //define loading layer to be used before the first real base layer is loaded
            layers: [new OpenLayers.Layer.Image(
                messagesBundle['map.loadmask'],
                '../../js/ext-' + Ext.version + '/resources/images/tp/grid/loading.gif', this.extent,
                new OpenLayers.Size(0.01, 0.01), {
                    displayInLayerSwitcher: false
                }
            )]
        }));

        Ext.apply(config.panel || {}, {
            border: false,
            items: [
                {
                    xtype: 'gx_zoomslider',
                    id: config.id + '-zoomslider',
                    aggressive: true,
                    vertical: true,
                    height: 120,
                    x: 15,
                    y: 210,
                    plugins: new GeoExt.ZoomSliderTip({
                        template: (messagesBundle['map.zoom.slider.tip'] || "Zoom Level: {zoom}<br>Resolution: {resolution}<br>Scale: 1 : {scale}<br>")
                    })
                }
            ],
            map: map
        });

        OE.report.datasource.map.Panel.superclass.constructor.call(this, config.panel || {});
    },

    /**
     * populated via getMapData()
     */
    wmsserver: null,
    wfsserver: null,
    gwcserver: null,

    loadMask: new Ext.LoadMask(Ext.getBody(), {msg: messagesBundle['map.loadmask']}),

    getMapData: function (parameters, callback, scope) {
        parameters = Ext.apply({}, parameters); // clone
        parameters = Ext.apply(parameters, parameters.filters);
        delete parameters.filters; // TODO make map data accept explicit parameters

        OE.data.doAjaxRestricted({
            url: OE.util.getUrl('/map/mapData'),
            method: 'GET',
            params: parameters,
            scope: this,
            onJsonSuccess: function (response) {
                this.wmsserver = response.systemMapProperties.wmsserver;
                this.wfsserver = response.systemMapProperties.wfsserver;
                this.gwcserver = response.systemMapProperties.gwcserver;

                // Update attributes from map information
                if (response.dsMapData && response.dsMapData.bounds) {
                    var bounds = OpenLayers.Bounds.fromArray(response.dsMapData.bounds);
                    this.extent = bounds;
                    this.map.maxExtent = bounds;
                }

                callback.call(scope || this, response);
            },
            onRelogin: {callback: this.getMapData, scope: this, args: [parameters, callback, scope]}
        });
    },

    /**
     *
     * @param mapData response from ../map/mapData
     * @param name name of layer to create
     * @param server geoserver url
     * @param requestParams GetQuery request parameters, used as 3rd argument to OpenLayer.WMS.Post constructor
     * @param layerOptions extra options to be tagged onto the layer, used as 4th argument to OpenLayer.WMS.Post constructor
     * @returns the {OpenLayers.Layer.WMS.Post} layer that was created
     */
    createWMSLayer: function (mapData, name, server, requestParams, layerOptions) {
        if (Ext.isIE) {
            // use POST for long URLs in IE
            layerOptions = Ext.apply({
                maxGetUrlLength: 2048
            }, layerOptions);
        }
        var layer = new OpenLayers.Layer.WMS(name, server, requestParams, layerOptions);
        this.map.addLayer(layer);
        return layer;
    },

    createWMSBaseLayer: function (mapData, name, requestParams, layerOptions) {
        // default to geo web cache server if configured for base layers
        return this.createWMSLayer(mapData, name, (this.gwcserver || this.wmsserver), requestParams, Ext.apply({
            isBaseLayer: true
        }, layerOptions));
    },

    /**
     * Creates wms overlay layer and controls, adds layer and control to mapObjects (for tracking/cleanup).
     */
    createWMSOverlay: function (mapData, name, requestParams, layerOptions, callbacks) {
        var layer = this.createWMSLayer(mapData, name, this.wmsserver, requestParams, Ext.apply({
            isBaseLayer: false,
            index: new Date().getTime()
        }, layerOptions));

        var control = new OpenLayers.Control.WMSGetFeatureInfo({
            url: this.wmsserver,
            layers: [layer],
            queryVisible: true,
            infoFormat: 'application/vnd.ogc.gml',
            vendorParams: {cql_filter: requestParams.cql_filter},
            maxFeatures: 1,
            index: new Date().getTime()
        });

        control.events.register('getfeatureinfo', this, function (e) {
            if (e && e.features) {
                if (callbacks && callbacks.featureSelected) {
                    callbacks.featureSelected.call(this, e.features[0]);
                }
            }
        });

        this.map.addControl(control);
        control.activate();

        this.mapObjects.push({layers: [layer], controls: [control]});
    },

    /**
     * Created overlay layers and controls for tracking/cleanup
     */
    mapObjects: [],

    /**
     * Removes all layers and controls from map
     */
    cleanupMap: function () {
        // Close visible popups
        Ext.iterate(this.popups, function (k, v) {
            if (v && v.close) {
                v.close();
            }
        });
        this.popups = {};

        Ext.each(this.mapObjects, function (o) {
            // remove controls
            Ext.each(o.controls, function (c) {
                this.map.removeControl(c);
                c.destroy();
            }, this.map);

            // remove layers
            Ext.each(o.layers, function (l) {
                this.map.removeLayer(l);
                l.destroy();
            }, this.map);
        });

        this.mapObjects = [];
    },

    getNavHistory: function () {
        return this.map.getControlsByClass('OpenLayers.Control.NavigationHistory')[0];
    },

    /**
     * Mapping of feature id -> displayed popup
     */
    popups: {},

    /**
     * Takes popupConfig with title, feature, loc, layer
     */
    displayFeaturePopup: function (popupConfig) {
        if (popupConfig.feature) {
            var values = [];
            Ext.iterate(popupConfig.feature.attributes, function (attribute, value) {
                values.push([attribute, value]);
            });

            var popup = new OE.report.datasource.map.Popup({
                title: popupConfig.title,
//				cls: 'reportPanel',
                feature: popupConfig.feature,
                map: this,
                constrain: true,
                renderTo: this.map.div, // constrain to map
                width: 200,
                height: 200,
                collapsible: true,
                layout: 'fit',
                autoScroll: true,
                items: [
                    {
                        xtype: 'grid',
                        store: new Ext.data.ArrayStore({
                            fields: ['Attribute', 'Value'],
                            data: values
                        }),
                        cm: new Ext.grid.ColumnModel({
                            defaults: {
                                width: 80,
                                sortable: false
                            },
                            columns: [
                                {
                                    dataIndex: 'Attribute',
                                    header: 'Attribute' // TODO i18n this if we keep
                                },
                                {
                                    dataIndex: 'Value',
                                    header: messagesBundle['map.parameterPanel.value']
                                }
                            ]
                        }),
                        sm: new Ext.grid.RowSelectionModel({
                            singleSelect: true
                        }),
                        loadMask: new Ext.LoadMask(Ext.getBody(), {
                            msg: messagesBundle['main.loadmask']
                        })
                    }
                ]
            });

            // unselect feature and remove from list of popups on close
            popup.on('close', function () {
                if (OpenLayers.Util.indexOf(popupConfig.layer, popupConfig.feature) > -1) {
                    this.map.getControlsByClass('OpenLayers.Control.SelectFeature')[0].unselect(popupConfig.feature);
                }
                delete this.popups[popupConfig.feature.fid];
            }, this);

            popup.show();
            this.popups[popupConfig.feature.fid] = popup;
            return popup;
        }
    },

    createLegend: function (legendConfig) {
        if (this.legend) {
            this.legend.close();
        }

        // No legend shown if html is not specified
        if (legendConfig && legendConfig.html) {
            this.legend = new Ext.Window({
                title: legendConfig.title || messagesBundle['map.legend'],
                layout: 'fit',
                width: legendConfig.width || 120, // needed for IE
                minWidth: 120,
                constrain: true,
                renderTo: this.body, // constrain to map
                x: 10,
                y: 10,
                closable: false,
                collapsible: true,
                items: [
                    {
                        autoScroll: true,
                        width: legendConfig.width || 120,
                        html: legendConfig.html
                    }
                ]
            });
            this.legend.show();
        } else {
            this.legend = null;
        }
    },

    /**
     * TODO: move back to the map controller so that we can dynamically create legends based on the data
     */
    createLegendHTML: function (thresholds) {
        var legendHTML = '<table>';
        if (thresholds) {
            Ext.each(thresholds, function (threshold) {
                legendHTML +=
                    '<tr><td style="background:#' + threshold.color + ';border:1px solid;"/><td>';

                if (threshold.low == threshold.high) {
                    if (threshold.low == -9999) {
                        legendHTML += 'No Data';
                    } else {
                        legendHTML += threshold.low;
                    }
                } else {
                    legendHTML += threshold.low;

                    if (threshold.high == 9999) {
                        legendHTML += '+';
                    } else {
                        legendHTML += ' to ' + threshold.high + '</td></tr>';
                    }
                }
            });
        } else {
            legendHTML += messagesBundle['map.legendNotApplicableBody'];
        }

        legendHTML += '</table>';
        return legendHTML;
    },

    defaultStyle: null,

    getDefaultStyle: function (dsId, name, callback) {
        if (this.defaultStyle === null) {
            OE.data.doAjaxRestricted({
                url: '../../oe/map/mapStyle',
                method: 'POST',
                scope: this,
                params: {dsId: dsId, name: name},
                success: function (response) {
                    this.defaultStyle = response.responseText;
                    callback.call(this, response.responseText);
                },
                failureTitle: messagesBundle['map.errorTitle'],
                failureMsg: messagesBundle['map.errorBody'],
                onRelogin: {callback: this.getDefaultStyle, args: [dsId, name, callback]}
            }, false);
        }

        return this.defaultStyle;
    }
});
Ext.reg('oe_mappanel', OE.report.datasource.map.Panel);

OE.report.datasource.map.Popup = Ext.extend(GeoExt.Popup, {
    // set a tooltip for unpin since it is confusing
    initTools: function () {
        if (this.unpinnable) { // TODO fix position issues when rendered to map
            this.addTool({
                id: 'unpin',
                qtip: messagesBundle['map.popup.unpin.qtip'],
                handler: this.unanchorPopup.createDelegate(this, [])
            });
        }
        OE.report.datasource.map.Popup.superclass.initTools.call(this);
    },
    /** private: method[position]
     *  Positions the popup relative to its feature
     */
    position: function () {
        var centerLonLat = this.feature.geometry.getBounds().getCenterLonLat();
        if (this._mapMove === true) {
            var visible = this.map.getExtent().containsLonLat(centerLonLat);
            if (visible !== this.isVisible()) {
                this.setVisible(visible);
            }
        }

        if (this.isVisible()) {
            var centerPx = this.map.getViewPortPxFromLonLat(centerLonLat);

            //This works for positioning with the anchor on the bottom.

            var anc = this.anc;
            var dx = anc.getLeft(true) + anc.getWidth() / 2;
            var dy = this.el.getHeight();

            //assuming for now that the map viewport takes up
            //the entire area of the MapPanel

            // FIX: updated set position
            //this.setPosition(centerPx.x + mapBox.x - dx, centerPx.y + mapBox.y - dy);
            this.setPosition(centerPx.x - dx, centerPx.y - dy);
        }
    }
});
