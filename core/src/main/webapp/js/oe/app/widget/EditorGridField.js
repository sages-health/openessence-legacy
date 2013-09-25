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

/**
 * An EditorGridPanel for a datasource. Dimension Joiner Support TBD
 */
OE.EditorGridField = Ext.extend(Ext.grid.EditorGridPanel, {

    constructor: function (config) {
        var me = this;
        // enable editing on first click
        this.clicksToEdit = 1;

        this.reportId = config.formConfig.itemId || null;

        // first field is hidden field to store record id
        // Note: If id field is not used, when we add row,
        // it will duplicate last edited row
        var columns = [
            {
                id: 'recId',
                dataIndex: 'recId',
                sortable: true,
                header: messagesBundle['input.datasource.default.recordId'] || 'RecordId'
            }
        ];
        var rec = {};
        // record Id, we will increment each time we use this variable
        var recId = 1;
        var storeFields = [ 'recId' ]; // [{name: 1, type: 'int'}, ...]
        var hiddenFieldVals = {};
        var keys = config.dimension.possibleValues.keys ? config.dimension.possibleValues.keys : [];

        var dateFormat = OE.util.defaultDateFormat;

        var formatDate = function (val) {
            var value = val;
            if (value && (('string' == typeof value ) || ('number' == typeof value))) {
                value = new Date(value);
            }
            return value ? value.dateFormat(dateFormat) : '';
        };

        var gridId = 'editorgridfield-' + Ext.id();
        var gridHiddenFld = gridId + '-hidden';
        var gridPlugins = [];

        var buildColumn = function (dsName, dimension) {
            var col = {};
            var formMeta = {};
            if (dimension.meta && dimension.meta.form) {
                formMeta = dimension.meta.form;
            }
            col.hidden = formMeta.xtype && formMeta.xtype == 'hidden';
            if (col.hidden) {
                hiddenFieldVals[dimension.name] = '';
            }
            var displayName = OE.util.getDimensionName(dsName, dimension.name);
            col.header = displayName;
            col.tooltip = displayName;
            col.dataIndex = dimension.name;
            col.id = dimension.name;
            col.width = formMeta.width ? formMeta.width : 100;
            col.align = 'right';
            // MUST set gridId for combocolumn to work correctly
            // If this property not set, we will see valueField
            // instead of displayField when we load the grid
            col.gridId = gridId;

            col.sortable = OE.util.getBooleanValue(formMeta.sortable, true);

            var store = null;
            var valueField = null;
            var displayField = null;

            if (dimension.possibleValues) {
                col.width = formMeta.width ? formMeta.width : 150;
                var results = [];

                if (dimension.possibleValues.data) {
                    // Load possible value data array
                    store = new Ext.data.ArrayStore({
                        fields: [ 'Id', 'Name' ],
                        data: dimension.possibleValues.data
                    });
                    valueField = 'Id';
                    displayField = 'Name';
                } else {
                    // Load possible values via details query
                    var storeFields = [];
                    Ext.each(dimension.possibleValues.detailDimensions,
                        function (result) {
                            storeFields.push(OE.datasource.grid.createFieldFromDimension(result));
                            results.push(result.name);
                        });

                    store = new OE.data.RestrictedJsonStore({
                        url: OE.util.getUrl('/report/detailsQuery'),
                        method: 'POST',
                        autoLoad: true,
                        baseParams: {
                            dsId: dimension.possibleValues.dsId,
                            results: results,
                            pagesize: 400
                        },
                        sortInfo: {
                            field: results[1],
                            direction: 'ASC'
                        },
                        root: 'rows',
                        fields: storeFields
                    });
                    valueField = formMeta.valueField || results[0];
                    displayField = formMeta.displayField || results[1] || results[0];
                }

                store.valueField = valueField;
                store.displayField = displayField;

                col.xtype = 'combocolumn';
                col.editor = new Ext.form.ComboBox(Ext.apply({
                    allowBlank: false,
                    mode: 'local',
                    triggerAction: 'all',
                    typeAhead: true,
                    forceSelection: true,
                    selectOnFocus: true,
                    emptyText: displayName,
                    store: store,
                    valueField: valueField,
                    displayField: displayField,
                    listClass: 'x-combo-list-small',
                    lazyRender: true,
                    onloadDefined: false
                }, formMeta));
            } else {
                switch (dimension.type) {
                    case 'Int':
                    case 'INTEGER':
                    case 'DOUBLE':
                    case 'FLOAT':
                    case 'LONG':
                        col.editor = new Ext.form.NumberField(Ext.apply({
                            allowBlank: false
                        }, formMeta));
                        break;
                    case 'DATE_TIME':
                    case 'DATE':
                        col.renderer = formatDate;
                        col.editor = new Ext.form.DateField(Ext.apply({
                            allowBlank: false,
                            format: messagesBundle['default.date.format'] || 'm/d/y'
                        }, formMeta));
                        col.width = formMeta.width ? formMeta.width : 150;
                        break;
                    case 'TEXT':
                        col.editor = new Ext.form.TextField(Ext.apply({
                            allowBlank: false
                        }, formMeta));
                        break;
                    case 'BOOLEAN':
                        var checkColumn = new Ext.grid.CheckColumn(Ext.apply(col, formMeta));
                        col = checkColumn;
                        gridPlugins.push(checkColumn);

                        break;
                }
            }
            return col;
        };

        var buildField = function (dimension) {
            var field = {};
            var formMeta = {};
            if (dimension.meta && dimension.meta.form) {
                formMeta = dimension.meta.form;
            }
            field.allowBlank = formMeta.allowBlank || false;

            field.name = dimension.name;
            switch (dimension.type) {
                case 'Int':
                case 'INTEGER':
                case 'LONG':
                    field.type = 'int';
                    break;
                case 'DOUBLE':
                case 'FLOAT':
                    field.type = 'float';
                    break;
                case 'DATE_TIME':
                case 'DATE':
                    field.type = 'date';
                    break;
                case 'TEXT':
                    field.type = 'string';
                    break;
                case 'BOOLEAN':
                    field.type = 'bool';
                    break;
            }
            return field;
        };

        var buildDefaultRec = function (dimension) {
            var field = {};
            // TODO: We may have to return value based on column type
            field[dimension.name] = '';
            return field;
        };

        Ext.each(config.dimension.possibleValues.editDimensions, function (dimension) {
            columns.push(buildColumn(config.formConfig.dataSource, dimension));
            storeFields.push(buildField(dimension));
            rec = Ext.apply(rec, buildDefaultRec(dimension));
        });

        var grid = this;
        var dimensionMetadata = config.dimensionMetadata;

        function updateBottomToolBar (grid, store) {
            if (grid && grid.getBottomToolbar()) {
                var recordCountMsg = (messagesBundle['input.datasource.default.records'] || 'Records') +
                    ' : ' + store.getCount();
                grid.getBottomToolbar().get(1).setText(recordCountMsg);
            }
        }

        config = Ext.apply({
            id: gridId,
            height: 200,
            anchor: '100%',
            clicksToEdit: 1,
            enableColumnMove: false,
            enableHdMenu: false,
            columnLines: true,
            deferRowRender: false, // for saved
            // query row setting
            cm: new Ext.grid.ColumnModel(columns),
            sm: new Ext.grid.RowSelectionModel({
                singleSelect: true
            }),
            keys: keys,
            bbar: [
                '->',
                {
                    xtype: 'tbtext',
                    text: ''
                }
            ],
            store: new Ext.data.JsonStore({
                idIndex: 0,
                fields: storeFields,
                root: 'data',
                sortInfo: {
                    field: OE.util.getStringValue(dimensionMetadata.sortcolumn, 'recId'),
                    direction: OE.util.getStringValue(dimensionMetadata.sortorder, 'DESC')
                },
                data: {
                    data: []
                },
                listeners: {
                    load: function (store) {
                        updateBottomToolBar(grid, store);
                    },
                    add: function (store) {
                        updateBottomToolBar(grid, store);
                    },
                    remove: function (store) {
                        updateBottomToolBar(grid, store);
                    },
                    update: function (store) {
                        updateBottomToolBar(grid, store);
                    }
                }
            }),
            tbar: [
                {
                    text: messagesBundle['input.datasource.default.import'] || 'Import...',
                    handler: function () {
                        importDataFromCSV(grid);
                    }
                },
                {
                    text: messagesBundle['input.datasource.default.add'] || 'Add...',
                    handler: function () {
                        addNewRow(grid);
                    }
                },
                {
                    text: messagesBundle['input.datasource.default.remove'] || 'Remove...',
                    handler: function () {
                        grid.stopEditing();
                        // remove selected row
                        grid.store.remove(grid.getSelectionModel().getSelected());
                        // put cursor on the first cell...
                        if (grid.store.getCount() > 0) {
                            grid.startEditing(0, 0);
                        }
                    }
                }
            ]
        }, config);

        // Add grid plugins, if there are any added for check column
        if (gridPlugins.length > 0) {
            config.plugins = gridPlugins;
        }

        // returns list of fields that has property hidden=false, do not include recId
        function getNonHiddenFields () {
            var fields = [];
            var numCols = grid.getColumnModel().getColumnCount();
            for (var i = 0; i < numCols; i++) {
                var col = grid.getColumnModel().getColumnAt(i) ;
                if (!col.hidden && col.id != 'recId') {
                    fields.push(col);
                }
            }
            return fields;
        }

        // returns list of field ids that has property hidden=false
        function getNonHiddenFieldIds () {
            var fields = [];
            var cols = getNonHiddenFields();
            for (var i = 0; i < cols.length; i++) {
                fields.push(cols[i].id);
            }
            return fields;
        }

        // returns list of field ids that has property hidden=false
        function getNonHiddenFieldNames () {
            var fields = [];
            var cols = getNonHiddenFields();
            var i = 0;
            for (i = 0; i < cols.length; i++) {
                fields.push(cols[i].header);
            }
            return fields;
        }
        // Translates combo column's display field to id field
        // This will be used during import
        function convertFromStore (newRec, field, value, editorStore) {
            var itemIx = editorStore.findExact(editorStore.displayField, value); // use displayField
            if (itemIx >= 0) {
                newRec[field] =  editorStore.getAt(itemIx).data[editorStore.valueField]; // use id field
            } else {
                // TODO: If the lookup value not found...
                newRec[field] = '';
            }
        }

        // This function is called when we import data from csv
        // It will translate combo display field to id fields,
        // date string/int to JavaScript Date object
        function convertDisplayValuesToIds (grid, rec) {
            var newRec = rec;
            Ext.iterate(rec, function (prop, v) {
                var field = prop;
                var value = rec[prop];
                var column = grid.getColumnModel().getColumnById(field);
                if (column.xtype == 'combocolumn') {
                    var editorStore = column.getEditor().getStore();
                    if (editorStore.getCount() === 0) {
                        editorStore.on('load', convertFromStore.createDelegate(this,
                            [newRec, field, value, editorStore]), null, {single: true});
                        return;
                    } else {
                        convertFromStore(newRec, field , value, editorStore);
                    }
                } else if (column.editor && column.editor.getXType() == 'datefield') {
                    var timestamp=Date.parse(rec[field]);
                    if (isNaN(timestamp) === false) {
                        newRec[field] = new Date(rec[field]);
                    } else {
                        newRec[field] = '';
                    }
                }
            });
            return newRec;
        }

        function importDataFromCSV (grid) {
            var configuration = {
                dataSource: config.dataSource
            };

            // Note that fields will be comma seperated string, NOT an array of string
            // if we send as params, order of the fields may not be same as what we send
            configuration.fieldNames = getNonHiddenFieldNames();
            configuration.fields = getNonHiddenFieldIds();
            if (grid.uploadConfig) {
                configuration.delimiter = grid.uploadConfig.delimiter || ',';
                configuration.qualifier = grid.uploadConfig.qualifier || '"';
                configuration.headerRow = grid.uploadConfig.headerRow || true;
                configuration.numRowsToRead = grid.uploadConfig.numRowsToRead || -1;
            }
            configuration.successCallback = function (response) {
                var recs = response.rows;

                if (recs && recs.length > 0) {
                    grid.stopEditing();

                    for (var ix = 0; ix < recs.length; ix++) {
                        var rec = recs[ix];
                        var Rec = grid.getStore().recordType;
                        // loop through fields and convert Name to Id value for combo fields
                        rec = convertDisplayValuesToIds(grid, rec);
                        var c = new Rec(Ext.apply(Ext.apply({recId: recId++}, rec), hiddenFieldVals));
                        grid.store.addSorted(c);
                    }
                    grid.startEditing(grid.store.getCount(), 0);
                }
            };

            require(['CsvUploadWindow'], function (OE) {
                new OE.CsvUploadWindow(configuration).show();
            });
        }

        function addNewRow (grid) {
            var Rec = grid.getStore().recordType;
            var c = new Rec(Ext.apply(Ext.apply({recId: recId++}, rec), hiddenFieldVals));
            grid.stopEditing();
            grid.store.add([ c ]);
            grid.startEditing(grid.store.getCount(), 0);
        }

        OE.EditorGridField.superclass.constructor.call(me, config);

        function convertToGridDataType (value, dataType) {
            var res = null;
            if (value) {
                switch (dataType) {
                    case 'date':
                        // Change dates to millis
                        res = new Date(value);
                        break;
                    case 'bool':
                        if (!value) {
                            res = false;
                        } else {
                            res = value;
                        }
                        break;
                    case 'float':
                    case 'string':
                    case 'int':
                        if (value !== '') {
                            res = value;
                        }
                        break;

                }
            }
            return res;
        }
        function dataTypeConversion (value, dataType) {
            var res = null;
            if (dataType == 'bool') {
                if (!value) {
                    res = false;
                } else {
                    res = value;
                }
            } else if (value) {
                switch (dataType) {
                    case 'date':
                        // Change dates to millis
                        if (value) {
                            res = value.getTime();
                        }
                        break;
                    case 'float':
                    case 'string':
                    case 'int':
                        if (value !== '') {
                            res = value;
                        }
                        break;
                }
            }
            return res;
        }

        function isGridDataValid (updating, showMessage) {
            var valid = true;
            var errorMessage = '';

            // if we are in process of adding grid records,
            // do not run validation code...
            if (updating) {
                if (showMessage) {
                    return errorMessage;
                } else {
                    return valid;
                }
            }
            // Step 1
            // if keys are defined ensure unique recs
            var keyId = 0;
            var recArray = [];
            var tmpRecs = grid.getValue();
            for (var recId = 0; recId < tmpRecs.length; recId++) {
                var data = tmpRecs[recId].data;
                var keyData = '';

                for (keyId = 0; keyId < grid.keys.length; keyId++) {
                    keyData = keyData + '_' + data[grid.keys[keyId]];
                }
                // Duplicate data
                if (recArray.contains(keyData)) {
                    if (showMessage) {
                        return  messagesBundle['input.datasource.grid.error.duplicateRows'] +
                            ' ' + messagesBundle['input.datasource.default.recordId'] + ' ' + data.recId;
                    } else {
                        return false;
                    }
                }
                recArray.push(keyData);
            }

            // Step 2
            // if field is allowed to be blank, field is valid
            if (grid.allowBlank) {
                if (showMessage) {
                    return errorMessage;
                } else {
                    return valid;
                }
            }
            // if this is a required field, make sure we have at least one row
            else if (grid.getValue() && grid.getValue().length === 0) {
                if (showMessage) {
                    return messagesBundle['input.datasource.grid.error.requiredAtleastOneRow'];
                } else {
                    return false;
                }
            }

            // For each row, ensure required fields have values
            Ext.iterate(grid.getValue(), function (record) {
                var row = record.data;
                Ext.iterate(grid.store.fields.items, function (fld) {

                    // if blank value is not valid for this field
                    if (fld.allowBlank === false) {
                        if (!row[fld.name]) {
                            if (showMessage) {
                                errorMessage = messagesBundle['input.datasource.grid.error.requiredColumn'] +
                                    row.recId;
                            } else {
                                valid = false;
                            }
                            return;
                        }
                    }
                });
            });
            if (showMessage) {
                return  errorMessage;
            } else {
                return valid;
            }
        }

        this.on('afterrender', function (me) {
            // add hidden field to store the
            // grid's values
            me.ownerCt.ownerCt.add({
                xtype: 'hidden',
                id: gridHiddenFld,
                name: config.name,
                updating: false,
                allowBlank: (function (allowBlank) {
                    return !Ext.isDefined(allowBlank) || allowBlank;
                })(this.dimension.meta.form.allowBlank),
                setValue: function (val) {
                    /*jshint loopfunc: true */
                    grid.stopEditing();
                    grid.store.removeAll();
                    // set updating to true so that we do not validate records while adding them
                    this.updating = true;
                    if (val && val.length > 0) {
                        for (var i = 0; i < val.length; i++) {
                            Ext.iterate(grid.store.fields.items, function (fld) {
                                val[i][fld.name] = convertToGridDataType(
                                    val[i][fld.name],fld.type);
                                // check if this field is hidden field
                                // if so, put the value in hiddenFieldVal json
                                // TODO: we should do assignment only once and this only applies
                                // when hidden field has same value for all rows...
                                if (fld.name != 'recId' && grid.colModel.getColumnById(fld.name).hidden) {
                                    hiddenFieldVals[fld.name] = val[i][fld.name];
                                } else if (fld.name == 'recId') {
                                    val[i][fld.name] = recId++;
                                }
                            });
                            var Rec = grid.getStore().recordType;
                            var c = new Rec(Ext.apply(val[i]));
                            grid.store.addSorted(c);
                        }

                        // re-assign record id so that rowids are nicely ordered when we display grid
                        Ext.iterate(me.getValue(), function (record, ix) {
                            var row = record.data;
                            row.recId = ix + 1;
                        });

                    }
                    // once done adding recs, set updating flag to false
                    this.updating = false;
                    grid.startEditing(grid.store.getCount(), 0);
                    updateBottomToolBar(grid, grid.store);
                },
                getValue: function () {
                    var data = [];
                    if (me.getValue().length === 0) {
                        return Ext.encode(data);
                    }

                    Ext.iterate(me.getValue(), function (record) {
                        var row = record.data;
                        var rowJson = {};
                        Ext.iterate(grid.store.fields.items, function (fld) {
                            if (fld.name != 'recId') {
                                rowJson[fld.name] = dataTypeConversion(row[fld.name], fld.type);
                                if (!rowJson[fld.name]) {
                                    if (grid.colModel.getColumnById(fld.name).hidden) {
                                        if (hiddenFieldVals[fld.name]) {
                                            rowJson[fld.name] = hiddenFieldVals[fld.name];
                                        }
                                    }
                                }
                            }
                        });

                        data.push(rowJson);
                    });
                    return Ext.encode(data);
                },
                isValid: function () {
                    return isGridDataValid(this.updating, false);
                }
            });
        });
    },
    getValue: function () {
        return this.store.getRange();
    }

});

Ext.reg('editorgridfield', OE.EditorGridField);
