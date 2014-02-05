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
 * Default data source function to build/populate grid(detail dimensions).
 */
define([], function () {
    var DataSourceGrid = Ext.extend(Ext.Panel, {
        constructor: function (configuration) {
            var dataSourceGrid = this;

            Ext.apply(this, configuration);
            Ext.applyIf(this, {
                dimensions: this.data.detailDimensions || {},
                metadata: this.data.meta && this.data.meta.grid ? this.data.meta.grid : {}
            });

            this.sm = this.sm || this.selectionModel || new Ext.grid.RowSelectionModel({singleSelect: true});

            this.columnsAndFields = OE.util.createColumnsAndFields(this.dataSource, this.dimensions,
                this.metadata, this.parameters);

            this.store = this.store || (function () {
                var baseParams = {};
                if (dataSourceGrid.parameters.filters) {
                    // unpack filters, TODO make detailsQuery accept explicit filters
                    Ext.apply(baseParams, dataSourceGrid.parameters.filters);
                    Ext.applyIf(baseParams, dataSourceGrid.parameters);
                    delete baseParams.filters;
                } else {
                    baseParams = dataSourceGrid.parameters;
                }

                var sortField = (function () {
                    var id = Ext.pluck(dataSourceGrid.columnsAndFields.columns, 'id');
                    if (id.indexOf(dataSourceGrid.metadata.sortcolumn) != -1) {
                        return dataSourceGrid.metadata.sortcolumn;
                    } else {
                        return dataSourceGrid.columnsAndFields.columns[0].id;
                    }
                })();

                return new OE.data.RestrictedJsonStore({
                    url: OE.util.getUrl('/report/detailsQuery'),
                    method: 'GET',
                    baseParams: baseParams,
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
                    sortInfo: {
                        field: sortField,
                        direction: OE.util.getStringValue(dataSourceGrid.metadata.sortorder, 'ASC')
                    },
                    fields: dataSourceGrid.columnsAndFields.fields
                });
            })();

            this.grid = this.grid || (function () {
                var pagingToolbarItems = [];
                if (dataSourceGrid.allowExport) {
                    pagingToolbarItems.push(new Ext.Panel({
                        html: new Ext.XTemplate('<a href="javascript: void(0);" target="_self">{text}</a>')
                            .applyTemplate({text: messagesBundle['panel.details.export.link']}),

                        listeners: {
                            afterrender: function (link) {
                                new Ext.Element(link.el.query('a')[0]).on('click', function (e, t) {
                                    t.href = dataSourceGrid.makeExportLink();
                                });
                            }
                        }
                    }));
                }

                var pageSize = configuration.pageSize || 100;

                var toolbar = new Ext.ux.DynamicPagingToolbar({
                    store: dataSourceGrid.store,
                    displayInfo: true,
                    pageSize: Ext.num(messagesBundle['page.size'], pageSize),
                    items: pagingToolbarItems
                });

                var gridClass = configuration.gridClass || Ext.grid.GridPanel;
                var gridConfig = {
                    store: dataSourceGrid.store,
                    cm: new Ext.grid.ColumnModel({
                        defaults: {
                            width: OE.util.getNumberValue(dataSourceGrid.metadata.width, 150),
                            sortable: OE.util.getBooleanValue(dataSourceGrid.metadata.sortable, true),
                            hidden: OE.util.getBooleanValue(dataSourceGrid.metadata.hidden, false)
                        },
                        columns: dataSourceGrid.columnsAndFields.columns
                    }),
                    sm: dataSourceGrid.sm,
                    border: false,
                    loadMask: new Ext.LoadMask(Ext.getBody(), {
                        msg: messagesBundle['main.loadmask']
                    }),
                    bbar: toolbar,
                    enableColumnHide: false,
                    // TODO: calculate actual width, iterate configs...
                    width: (150 * dataSourceGrid.columnsAndFields.columns.length)
                };
                if (typeof configuration.gridExtraConfig !== 'undefined') {
                    Ext.apply(gridConfig, configuration.gridExtraConfig);
                }

                var grid = new gridClass(gridConfig);

                if (configuration.rowdblclick) {
                    // Ext throws an error if you pass an undefined callback
                    grid.on('rowdblclick', configuration.rowdblclick);
                }

                dataSourceGrid.store.load({params: {firstrecord: 0, pagesize: pageSize}});

                return grid;
            })();

            configuration = Ext.apply({
                cls: 'reportPanel',
                layout: 'fit',
                // need center to maintain compatibility with border layout.
                // TODO: extract layout info outside of widget
                region: 'center',
                cmargins: '0 0 6 0',
                floatable: false,
                boxMinHeight: 100,
                border: false,
                frame: true,
                items: [
                    this.grid
                ]
            }, configuration);

            DataSourceGrid.superclass.constructor.call(this, configuration);
        },

        reload: function () {
            return this.store.reload();
        },

        load: function (options) {
            return this.store.load(options);
        },

        getExtGridPanel: function () {
            return this.grid;
        },

        /**
         * Construct query to /report/exportGridToFile. Uses the visible columns as result dimensions.
         * Also respects column ordering and sorting.
         */
        makeExportLink: function () {
            var columns = this.grid.getColumnModel().getColumnsBy(function (c) {
                return Ext.isDefined(c.hidden) && !c.hidden;
            });

            var sortState = this.grid.getStore().sortInfo;

            var parameters = {
                dsId: this.parameters.dsId,
                timezoneOffset: (new Date()).getTimezoneOffset(),
                results: [],
                sortcolumn: sortState.field || columns[0].id,
                sortorder: sortState.direction || 'ASC',
                renderIntToBool: this.metadata.renderIntToBool ? this.metadata.renderIntToBool : false
            };

            // TODO make exportGridToFile accept explicit filters
            Ext.apply(parameters, this.parameters.filters);

            // Get user configured grid (column order/hidden etc...)
            Ext.each(columns, function (column) {
                parameters.results.push(column.id);
            });

            return Ext.urlAppend(OE.util.getUrl('/report/exportGridToFile'), Ext.urlEncode(parameters));
        }
    });

    return DataSourceGrid;
});
