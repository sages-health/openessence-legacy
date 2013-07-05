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

OE.SAVED_QUERY_DS = 'SavedQuery_Entry';

/**
 * Better version of dataSourceGrid for saved queries.
 */
OE.SavedQueryPanel = Ext.extend(Ext.Panel, {
    constructor: function (config) {
        var me = this;

        // overridable properties
        config = Ext.applyIf(config, {
            collapsed: true
        });

        // non-overridable properties
        config = Ext.apply(config, {
            dataSource: OE.SAVED_QUERY_DS,
            title: messagesBundle[OE.SAVED_QUERY_DS + '.grid'] || config.title || 'Saved Queries',
            cls: 'reportPanel',
            layout: 'fit',
            split: true,
            plugins: new Ext.ux.collapsedPanelTitlePlugin(),
            collapsible: true,
            cmargins: '0 0 6 0',
            floatable: false,
            height: 300,
            border: false,
            frame: true,
            sm: new Ext.grid.RowSelectionModel(), // multi-select
            items: []
        });
        OE.SavedQueryPanel.superclass.constructor.call(this, config);

        OE.data.doAjaxRestricted({
            url: OE.util.getUrl('/ds/' + OE.SAVED_QUERY_DS),
            method: 'GET',
            scope: this,
            onJsonSuccess: function (response) {
                /**
                 * Data source dimensions
                 */
                var dimensions = response.detailDimensions || {};

                /**
                 * Grid meta data
                 */
                var metadata = (response.meta && response.meta.grid ? response.meta.grid : {});

                /**
                 * Columns and Fields used for the grid panel to display data source records, modified by metadata and params
                 */
                var columnsAndFields = OE.datasource.grid.createColumnsAndFields(config.dataSource, dimensions, metadata, config.parameters);

                columnsAndFields.columns[0].width = 80; // make ID column smaller
                columnsAndFields.columns[1].width = 200; // make QueryName column bigger

                var dataStore = new OE.data.RestrictedJsonStore({
                    url: config.url || '../../oe/report/detailsQuery',
                    method: 'POST',
                    baseParams: {
                        dsId: OE.SAVED_QUERY_DS,
                        DataSource: config.queryFormPanel.dataSource
                    },
                    root: 'rows',

                    // Paging support
                    remoteSort: true,
                    totalProperty: 'totalRecords',
                    paramNames: {
                        start: 'firstrecord',   // The parameter name which specifies the start row
                        limit: 'pagesize',      // The parameter name which specifies number of rows to return
                        sort: 'sortcolumn',     // The parameter name which specifies the column to sort on
                        dir: 'sortorder'        // The parameter name which specifies the sort direction
                    },
                    // Check if default/configured column is in list of result columns else use first
                    sortInfo: {field: (Ext.pluck(columnsAndFields.columns, 'id').indexOf(metadata.sortcolumn) != -1 ? metadata.sortcolumn : columnsAndFields.columns[0].id), direction: OE.util.getStringValue(metadata.sortorder, 'ASC')},
                    fields: columnsAndFields.fields
                });
                this.dataStore = dataStore;

                var runAction = new Ext.Action({
                    text: messagesBundle['query.save.run'],
                    disabled: true,
                    scope: this,
                    handler: function () {
                        Ext.each(me.sm.getSelections(), function (item) {
                            delete config.itemId;

                            // b/c OE datasources don't support JSON
                            var savedParameters = Ext.decode(item.data.Parameters);

                            if (savedParameters.filters && savedParameters.filters.lengthOfTime) {
                                // convert lengthOfTime to start and end dates

                                var endDate = me.queryFormPanel.getDefaultEndDate().getTime();
                                savedParameters.filters[me.queryFormPanel.endDateFieldId] = endDate;

                                var startDate = endDate - savedParameters.filters.lengthOfTime;
                                savedParameters.filters[me.queryFormPanel.startDateFieldId] = startDate;
                            }

                            me.queryFormPanel[savedParameters.queryType + 'Callback'](Ext.apply({
                                dsId: savedParameters.dataSource,
                                title: item.data.QueryName,
                                charts: savedParameters.charts,
                                pivot: savedParameters.pivot,
                                results: savedParameters.results,
                                filters: savedParameters.filters,
                                savedQuery: Ext.apply({parameters: savedParameters}, item.data)
                            }, savedParameters.filters));

                            me.collapse();
                        });
                    }
                });

                var deleteAction = new Ext.Action({
                    text: messagesBundle['input.datasource.default.delete'],
                    disabled: true,
                    scope: this,
                    handler: function () {
                        this.deleteReports(this.sm.getSelections());
                    }
                });

                me.sm.addListener('selectionchange', function (sm) {
                    var records = sm.getSelections();
                    var selectionCount = records.length;

                    switch (selectionCount) {
                        case 0:
                        {
                            deleteAction.disable();
                            runAction.disable();
                            break;
                        }
                        default:
                        {
                            deleteAction.enable();
                            runAction.enable();
                            break;
                        }
                    }
                    ;
                }, this, {buffer: 5});

                this.add({
                    xtype: 'grid',
                    store: dataStore,
                    height: 300,
                    autoExpandColumn: 'Parameters',
                    viewConfig: {
                        autoFill: true
                    },
                    cm: new Ext.grid.ColumnModel({
                        defaults: {
                            sortable: OE.util.getBooleanValue(metadata.sortable, true),
                            hidden: OE.util.getBooleanValue(metadata.hidden, false)
                        },
                        columns: columnsAndFields.columns
                    }),
                    sm: me.sm,
                    border: false,
                    loadMask: new Ext.LoadMask(Ext.getBody(), {
                        msg: messagesBundle['main.loadmask']
                    }),
                    tbar: [runAction, deleteAction],
                    bbar: new Ext.ux.DynamicPagingToolbar({
                        store: dataStore,
                        displayInfo: true,
                        pageSize: Ext.num(dimensionsBundle['page.size'], 100)
                    }),
                    enableColumnHide: false,
                    keys: [
                        {
                            key: Ext.EventObject.ENTER,
                            fn: function () {
                                runAction.execute();
                            }
                        },
                        {
                            key: Ext.EventObject.DELETE,
                            fn: function () {
                                deleteAction.execute();
                            }
                        }
                    ],
                    listeners: {
                        rowdblclick: function () {
                            // need to wrap in a function for scoping reasons
                            runAction.execute();
                        }
                    }
                });

                this.doLayout();
                this.ownerCt.doLayout();

                dataStore.load({params: {firstrecord: 0, pagesize: Ext.num(dimensionsBundle['page.size'], 100)}});

            }
        });
    },

    reload: function () {
        this.dataStore.reload();
    },
    load: function (options) {
        this.dataStore.load(options);
    },

    deleteReports: function (records) {
        var me = this;

        if (records && records.length) {
            Ext.Msg.confirm(messagesBundle['input.datasource.default.deleteTitle'], messagesBundle['input.datasource.default.deleteMessage'], function (btn) {
                if (btn == 'yes') {
                    var pkIds = [];

                    for (var index = 0; index < records.length; index++) {
                        var identifiers = me.getItemIdFromData(records[index].data);
                        pkIds.push(identifiers.key);
                    }

                    OE.data.doAjaxRestricted({
                        url: '../../oe/input/delete',
                        params: {dsId: me.dataSource},
                        jsonData: {pkIds: pkIds},
                        onJsonSuccess: function (response) {
                            me.reload();
                        },
                        onRelogin: {callback: me.deleteReports, args: [records]}
                    });
                }
            });
        }
    },

    /**
     * Returns primary key and item id that have been constructed using record.data and pks
     */
    getItemIdFromData: function (data) {
        var primaryKeys = ['Id'];
        if (this.data && this.data.pks) {
            primaryKeys = this.data.pks;
        }
        var primaryKey = {};
        var itemId = [];

        for (var index = 0; index < primaryKeys.length; index++) {
            var key = primaryKeys[index];
            primaryKey[key] = data[key];
            itemId.push(data[key]);
        }

        return {
            key: primaryKey,
            itemId: itemId.join(':')
        };
    }
});
