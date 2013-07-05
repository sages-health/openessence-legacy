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

Ext.namespace("OE.datasource.grid");

/**
 * Default data source function to build/populate grid(detail dimensions).
 */
OE.datasource.grid.init = function (configuration) {
    var me = this;

    var grid = null; // the resulting grid
    var dataStore = null;

    /**
     * Data source dimensions
     */
    var dimensions = configuration.data.detailDimensions || {};

    /**
     * Grid meta data
     */
    var metadata = (configuration.data.meta && configuration.data.meta.grid ? configuration.data.meta.grid : {});

    /**
     * Selection Model, uses configuration else single row selection
     */
    var sm = (configuration.selectionModel ? configuration.selectionModel : new Ext.grid.RowSelectionModel({singleSelect: true}));

    /**
     * Columns and Fields used for the grid panel to display data source records, modified by metadata and params
     */
    var columnsAndFields = OE.datasource.grid.createColumnsAndFields(configuration.dataSource, dimensions, metadata, configuration.parameters);

    function makeStandardGrid(columnsAndFields, oldColumnsAndFields) {
        dataStore = new OE.data.RestrictedJsonStore({
            url: configuration.url || OE.util.getUrl('/report/detailsQuery'),
            method: 'POST',
            baseParams: (function () {
                var params = {};
                if (configuration.pivot) {
                    params.pivotX = configuration.pivot.x.id;
                    params.pivotY = configuration.pivot.y.id;
                }
                if (configuration.parameters.filters) {
                    // unpack filters, TODO make detailsQuery accept explicit filters
                    Ext.apply(params, configuration.parameters.filters);
                    Ext.applyIf(params, configuration.parameters);
                    delete params.filters;

                    return params;
                } else {
                    return configuration.parameters;
                }
            })(),
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

        /**
         * Construct query to /report/exportGridToFile. Uses the visible columns as result dimensions.
         * Also respects column ordering and sorting.
         */
        function makeExportLink(gridPanel) {
            var columns;

            if (configuration.pivot) {
                columns = [];
                for (var i = 0; i < oldColumnsAndFields.columns.length; i++) {
                    columns.push(oldColumnsAndFields.columns[i]);
                }
            } else {
                columns = gridPanel.getColumnModel().getColumnsBy(function (c) {
                    return Ext.isDefined(c.hidden) && !c.hidden;
                });
            }

            var sortState = gridPanel.getStore().sortInfo;

            var parameters = {
                dsId: configuration.parameters.dsId,
                timezoneOffset: (new Date()).getTimezoneOffset(),
                results: [],
                sortcolumn: sortState.field || columns[0].id,
                sortorder: sortState.direction || 'ASC'
            };

            // TODO make exportGridToFile accept explicit filters
            Ext.apply(parameters, configuration.parameters.filters);

            // Get user configured grid (column order/hidden etc...)
            Ext.each(columns, function (column) {
                parameters.results.push(column.id);
            });

            return Ext.urlAppend('../report/exportGridToFile', Ext.urlEncode(parameters));
        };

        var pagingToolbarItems = [];
        if (configuration.allowExport) {
            pagingToolbarItems.push(new Ext.Panel({
                html: new Ext.XTemplate('<a href="javascript: void(0);" target="_self">{text}</a>').applyTemplate({text: messagesBundle['panel.details.export.link']}),
                listeners: {
                    afterrender: function (link) {
                        new Ext.Element(link.el.query('a')[0]).on('click', function (e, t) {
                            t.href = makeExportLink(grid);
                        });
                    }
                }
            }));
        }

        var pageSize = configuration.pageSize || 100;

        var toolbar = new Ext.ux.DynamicPagingToolbar({
            store: dataStore,
            displayInfo: true,
            pageSize: Ext.num(dimensionsBundle['page.size'], pageSize),
            items: pagingToolbarItems
        });

        var gridClass = configuration.gridClass || Ext.grid.GridPanel;
        var gridConfig = {
            store: dataStore,
            cm: new Ext.grid.ColumnModel({
                defaults: {
                    width: OE.util.getNumberValue(metadata.width, 150),
                    sortable: OE.util.getBooleanValue(metadata.sortable, true),
                    hidden: OE.util.getBooleanValue(metadata.hidden, false)
                },
                columns: columnsAndFields.columns
            }),
            sm: sm,
            border: false,
            loadMask: new Ext.LoadMask(Ext.getBody(), {
                msg: messagesBundle['main.loadmask']
            }),
            bbar: toolbar,
            enableColumnHide: false,
            // TODO: calculate actual width, iterate configs...
            width: (150 * columnsAndFields.columns.length)
        };
        if (typeof configuration.gridExtraConfig !== 'undefined') {
            Ext.apply(gridConfig, configuration.gridExtraConfig);
        }

        grid = new gridClass(gridConfig);

        if (configuration.rowdblclick) {
            // Ext throws an error if you pass an undefined callback
            grid.on('rowdblclick', configuration.rowdblclick);
        }

        dataStore.load({params: {firstrecord: 0, pagesize: pageSize}});
    }

    var gridPanel = new Ext.Panel(Ext.apply({
        cls: 'reportPanel',
        layout: 'fit',
        // need center to maintain compatibility with border layout.  TODO: extract layout info outside of widget
        region: 'center',
        cmargins: '0 0 6 0',
        floatable: false,
        boxMinHeight: 100,
        border: false,
        frame: true,
        reload: function () {
            if (dataStore) {
                dataStore.reload();
            }
        },
        load: function (options) {
            if (dataStore) {
                dataStore.load(options);
            }
        },
        getExtGridPanel: function () {
            if (grid) {
                return grid;
            }
        }
    }, configuration));

    function addPivotYColumn(columnsAndFieldsPivot) {
        columnsAndFieldsPivot.columns.push({
            dataIndex: configuration.pivot.y.id,
            header: configuration.pivot.y.name,
            id: configuration.pivot.y.id
        });
        columnsAndFieldsPivot.fields.push({
            name: configuration.pivot.y.id,
            type: 'string'
        });
    }

    function addPivotTotalColumn(columnsAndFieldsPivot) {
        columnsAndFieldsPivot.columns.push({
            dataIndex: 'Total',
            header: 'Total',
            id: 'Total',
            sortable: false
        });
        columnsAndFieldsPivot.fields.push({
            name: 'Total',
            type: 'int'
        });
    }

    if (configuration.pivot) {
        var columnsAndFieldsPivot = { columns: [], fields: [] };

        if (configuration.pivot.x.dimension.possibleValues.data) {
            // Add the category column (the Y dimension)
            addPivotYColumn(columnsAndFieldsPivot);

            // Add the data columns
            Ext.each(configuration.pivot.x.dimension.possibleValues.data, function (row) {
                var id = 0;
                var name = 1;
                var filter = configuration.parameters.filters[configuration.pivot.x.id];

                // Quick fix to make the virtual x columns respect any applied filters
                // Basically, only show selected x columns if chosen in the filter. If unspecified, show all.
                if (!filter || filter.indexOf(row[id]) != -1) {
                    columnsAndFieldsPivot.columns.push({dataIndex: row[id], header: row[name], id: row[id], sortable: false});
                    columnsAndFieldsPivot.fields.push({name: row[id], type: 'int'});
                }
            });

            // Add the total column
            addPivotTotalColumn(columnsAndFieldsPivot);

            // Render the pivot grid
            makeStandardGrid(columnsAndFieldsPivot, columnsAndFields);
            gridPanel.add(grid);
            gridPanel.doLayout();
        } else {
            var storeFields = [];
            var results = [];
            Ext.each(configuration.pivot.x.dimension.possibleValues.detailDimensions, function (result) {
                storeFields.push(OE.datasource.grid.createFieldFromDimension(result));
                results.push(result.name);
            });

            function getPossibleValues() {
                // Extract the metadata from the dimension, delegating to inner objects if present
                var dimensionMeta = configuration.pivot.x.dimension.meta || {};
                dimensionMeta = Ext.applyIf(dimensionMeta, dimensionMeta.grid);
                dimensionMeta = Ext.applyIf(dimensionMeta, dimensionMeta.form);

                var deferred = Q.defer();
                OE.data.doAjaxRestricted({
                    url: OE.util.getUrl('/report/detailsQuery'),
                    method: 'GET',
                    scope: this,
                    params: {
                        dsId: configuration.pivot.x.dimension.possibleValues.dsId,
                        results: results,
                        pagesize: -1,
                        sortcolumn: dimensionMeta.sortcolumn || configuration.pivot.x.dimension.possibleValues.detailDimensions[0].name, //Use this or first column
                        sortorder: dimensionMeta.sortorder || 'ASC'
                    },
                    onJsonSuccess: function (response) {
                        deferred.resolve(response);
                    },
                    onRelogin: {callback: OE.datasource.grid.init, args: [configuration]}
                });
                return deferred.promise;
            }

            getPossibleValues().then(function (response) {
                // Add the category column (the Y dimension)
                addPivotYColumn(columnsAndFieldsPivot);

                // Add the data columns
                Ext.each(response.rows, function (row) {
                    var id = configuration.pivot.x.dimension.possibleValues.detailDimensions[0].name;
                    var name = configuration.pivot.x.dimension.possibleValues.detailDimensions[1].name;
                    var filter = configuration.parameters.filters[configuration.pivot.x.id];

                    // Quick fix to make the virtual x columns respect any applied filters
                    // Basically, only show selected x columns if chosen in the filter. If unspecified, show all.
                    if (!filter || filter.indexOf(row[id]) != -1) {
                        columnsAndFieldsPivot.columns.push({dataIndex: row[id], header: row[name], id: row[id], sortable: false});
                        columnsAndFieldsPivot.fields.push({name: row[id], type: 'int'});
                    }
                });

                // Add the total column
                addPivotTotalColumn(columnsAndFieldsPivot);

                // Render the pivot grid
                makeStandardGrid(columnsAndFieldsPivot, columnsAndFields);
                gridPanel.add(grid);
                gridPanel.doLayout();
            }).fail(function (e) {
                    console.error(e);
                });
        }
    } else {
        // Render the details grid
        makeStandardGrid(columnsAndFields, null);
        gridPanel.add(grid);
        gridPanel.doLayout();
    }

    return gridPanel;
};

OE.datasource.grid.createColumnsAndFields = function (dsId, dimensions, metadata, parameters) {
    var columns = [];
    var fields = [];

    var results = (parameters && parameters.results ? parameters.results : undefined);

    // Build columns and fields for grid, results for query
    Ext.each(dimensions, function (dimension) {
        if (results === undefined || (results.indexOf(dimension.name) != -1)) {
            columns.push(OE.datasource.grid.createColumnFromDimension(dsId, metadata, dimension));
            fields.push(OE.datasource.grid.createFieldFromDimension(dimension));
        }
    });

    return {columns: columns, fields: fields};
};

/**
 * Builds a column configuration from detail dimension (defaults can be over ridden using grid meta data)
 */
// TODO privatize, this isn't used anywhere but createColumnsAndFields
OE.datasource.grid.createColumnFromDimension = function (dsId, gridMetadata, dimension) {
    var column = {};

    column.id = dimension.name;
    column.dataIndex = dimension.name;

    // Check metadata, otherwise use sensible defaults
    var dimensionMetadata = dimension.meta ? dimension.meta : {};
    var dimensionGridMetadata = dimensionMetadata.grid ? dimensionMetadata.grid : {};
    var dimensionFormMetadata = dimensionMetadata.form ? dimensionMetadata.form : {};

    // Attempts to load header from displayName property, then dimensions bundle
    // using a qualified (data source name) "dot" dimension name, then
    // will try to load from dimensions bundle just using the dimension name, finally will just use the dimension name.
    column.header =
        dimension.displayName || dimensionsBundle[dsId + '.' + dimension.name] || dimensionsBundle[dimension.name] || dimension.name;

    if (dimensionGridMetadata.width != undefined) {
        column.width = OE.util.getNumberValue(dimensionGridMetadata.width, 150);
    }

    if (dimensionGridMetadata.sortable != undefined) {
        column.sortable = OE.util.getBooleanValue(dimensionGridMetadata.sortable, true);
    }

    if (dimensionGridMetadata.hidden != undefined) {
        column.hidden = OE.util.getBooleanValue(dimensionGridMetadata.hidden, false);
    }

    // Renderer based on type (string and date)
    if (dimension.type == 'TEXT' || dimension.type == 'String') {
        column.renderer = Ext.util.Format.htmlEncode;
    } else if (dimension.type == 'DATE' || dimension.type == 'Date' || dimension.type == 'DATE_TIME' || dimension.type == 'Date_Time') {
        column.format = dimensionGridMetadata.format || gridMetadata.format || OE.util.defaultDateFormat;

        column.renderer = function (value, metaData, record, rowIndex, colIndex, store) {
            return OE.util.renderDate(value, this.format || OE.util.defaultDateFormat);
        };
    } else if (dimension.type == 'BOOLEAN') {
        if (dimensionGridMetadata.renderBooleanAsTernary) {
            var overrideBundle = dimensionGridMetadata.overrideBooleanTernary || {};
            column.renderer = function (value, metaData, record, rowIndex, colIndex, store) {
                return OE.util.renderBooleanAsTernary(value, overrideBundle);
            };
        }
    }

    // assume all colorfields want their text to be colored; this makes sense, right?
    if (dimensionFormMetadata.xtype === 'colorfield') {
        var oldRenderer = column.renderer;
        column.renderer = function (value, meta, record) {
            // adapted from Ext.ux.Colorfield
            var r = parseInt(value.slice(1, 3), 16);
            var g = parseInt(value.slice(3, 5), 16);
            var b = parseInt(value.slice(5), 16);
            var textColor = (r + g + b) / 3 > 128 ? '#000' : '#FFF';

            meta.attr = 'style="background:' + value
                + ';color:' + textColor
                + ';border-radius:5px; -webkit-border-radius:5px; -moz-border-radius:5px"';
            return oldRenderer.call(this, value); // delegate to existing renderer
        };
    }

    if (dimensionFormMetadata.xtype === 'queryImage') {
        var oldRenderer = column.renderer;
        column.renderer = function (value, meta, record) {
            var queryImg = "url('" + OE.context.root + "/images/queryimages/";
            if (value == "charts") {
                var chartType = Ext.decode(record.json.Parameters).charts[0].type;
                if (chartType == "pie") {
                    queryImg += "piechart";
                } else {
                    queryImg += "barchart";
                }
            } else {
                queryImg += value;
            }
            meta.attr = 'style="background:' + queryImg
                + '.png\') no-repeat center"';
            return oldRenderer.call(this, ""); // delegate to existing renderer
        };
    }

    return column;
};

/**
 * Builds a field configuration from detail dimension (defaults can be over ridden using grid metadata)
 */
OE.datasource.grid.createFieldFromDimension = function (dimension) {
    var field = {};

    // Must match dataIndex
    field.name = dimension.name;

    switch (dimension.type) {
        case 'Int':
        case 'INTEGER':
        case 'LONG':
        {
            field.type = 'int';
            break;
        }
        case 'DOUBLE':
        case 'FLOAT':
        {
            field.type = 'float';
            break;
        }
        case 'String':
        case 'TEXT':
        {
            field.type = 'string';
            field.sortType = Ext.data.SortTypes.asUCString;
            break;
        }
        case 'Date':
        case 'DATE':
        case 'Date_Time':
        case 'DATE_TIME':
        {
            // Support long dates
            field.type = 'int';
            // field.type = 'date';
            // field.format = metadata.format || OE.util.defaultDateFormat;
            break;
        }
        default:
        {
            field.type = 'auto';
            break;
        }
    }

    return field;
};
