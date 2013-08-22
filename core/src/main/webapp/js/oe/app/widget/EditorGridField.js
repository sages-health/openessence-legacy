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

/*jshint loopfunc: true */

Ext.ns('OE');

/**
 * An EditorGridPanel for a datasource. Dimension Joiner Support TBD
 */
OE.EditorGridField = Ext.extend(Ext.grid.EditorGridPanel, {

    constructor: function (config) {
        var me = this;

        this.reportId = config.formConfig.itemId || null;

        // first field is hidden field to store record id
        // Note: If id field is not used, when we add row,
        // it will duplicate last edited row
        var columns = [
            {
                id: 'recId',
                dataIndex: 'recId',
                hidden: true
            }
        ];
        var rec = {};
        // record Id, we will increment each time we use this variable
        var recId = 0;
        var storeFields = [ 'recId' ]; // [{name: 1, type: 'int'}, ...]
        var hiddenFieldVals = {};
        var keys = config.dimension.possibleValues.keys ? config.dimension.possibleValues.keys : [];

        var dateFormat = OE.util.defaultDateFormat;

        var formatDate = function (value) {
            return value ? value.dateFormat(dateFormat) : '';
        };

        var gridId = 'editorgridfield-' + Ext.id();

        var buildColumn = function (dsName, dimension) {
            var col = {};
            var formMeta = {};
            if (dimension.meta && dimension.meta.form) {
                formMeta = dimension.meta.form;
            }
            col.hidden = (formMeta.xtype && formMeta.xtype == 'hidden') ? true
                : false;
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
                        url: '../../oe/report/detailsQuery',
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
                    // TODO: for now, default to key then label
                    valueField = results[0];// 'Id';
                    displayField = results[1];// 'Name';
                }

                col.xtype = 'combocolumn';
                var combo = new Ext.form.ComboBox(Ext.apply({
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
                    lazyRender: true
                }, formMeta));

                col.editor = combo;
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
                            format: 'm/d/y'
                        }, formMeta));
                        col.width = formMeta.width ? formMeta.width : 150;
                        break;
                    case 'TEXT':
                        col.editor = new Ext.form.TextField(Ext.apply({
                            allowBlank: false
                        }, formMeta));
                        break;
                    case 'BOOLEAN':
                        col.xtype = 'checkcolumn';
                        col = Ext.apply(col, formMeta);
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
        config = Ext.apply({
            id: gridId,
            height: 150,
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
            store: new Ext.data.JsonStore({
                idIndex: 0,
                fields: storeFields,
                autoLoad: true,
                root: 'data',
                sortInfo: {field: OE.util.getStringValue(dimensionMetadata.sortcolumn, "recId"), direction: OE.util.getStringValue(dimensionMetadata.sortorder, 'DESC')},
                data: {
                    data: [ Ext.apply({recId: recId++}, rec) ]
                }
            }),
            tbar: [
                {
                    text: messagesBundle['input.datasource.default.add'],
                    handler: function () {
                        addNewRow(grid);
                    },
                    scope: grid
                },
                {
                    text: messagesBundle['input.datasource.default.remove'],
                    handler: function () {
                        grid.stopEditing();
                        // remove selected row
                        grid.store.remove(grid.getSelectionModel().getSelected());
                        // put cursor on the first cell...
                        if (grid.store.getCount() > 0) {
                            grid.startEditing(0, 0);
                        }
                    },
                    scope: grid
                }
            ]
        }, config);

        function addNewRow(grid) {
            var Rec = grid.getStore().recordType;
            var c = new Rec(Ext.apply(Ext.apply({recId: recId++}, rec), hiddenFieldVals));
            grid.stopEditing();
            grid.store.add([ c ]);
            grid.startEditing(grid.store.getCount(), 0);
        }

        OE.EditorGridField.superclass.constructor.call(me, config);

        function convertToGridDataType(value, dataType) {
            var res = null;
            if (value) {
                switch (dataType) {
                    case 'date':
                        // Change dates to millis
                        res = new Date(value);
                        break;
                    case 'float':
                    case 'string':
                    case 'bool':
                    case 'int':
                    {
                        if (value !== '') {
                            res = value;
                        }
                        break;
                    }

                }
            }
            return res;
        }
        function dataTypeConversion(value, dataType) {
            var res = null;
            if (value) {
                switch (dataType) {
                    case 'date':
                        // Change dates to millis
                        res = value.getTime();
                        break;
                    case 'float':
                    case 'string':
                    case 'bool':
                    case 'int':
                    {
                        if (value !== '') {
                            res = value;
                        }
                        break;
                    }
                }
            }
            return res;
        }

        this.on('afterrender', function (me) {
            // add hidden field to store the
            // grid's values
            me.ownerCt.ownerCt.add({
                xtype: 'hidden',
                name: config.name,
                allowBlank: (!Ext.isDefined(this.dimension.meta.form.allowBlank) ||
                    this.dimension.meta.form.allowBlank === true ) ? true : false,
                setValue: function (val) {
                    grid.stopEditing();
                    grid.store.removeAll();
                    var Rec = grid.getStore().recordType;
                    if (val !== null && Ext.isDefined(val) && val.length > 0) {
                        for (var i = 0; i < val.length; i++) {
                            Ext.iterate(grid.store.fields.items, function (fld, j) {
                                val[i][fld.name] = convertToGridDataType(
                                    val[i][fld.name], fld.type);
                                // check if this field is hidden field
                                // if so, put the value in hiddenFieldVal json
                                // TODO: we should do assignment only once and this only applies
                                // when hidden field has same value for all rows...
                                if (fld.name != 'recId' && grid.colModel.getColumnById(fld.name).hidden) {
                                    hiddenFieldVals[fld.name] = val[i][fld.name];
                                }
                            });
                            var c = new Rec(Ext.apply({recId: recId++}, val[i]));
                            grid.store.addSorted(c);
                        }
                    }
                    grid.startEditing(grid.store.getCount(), 0);
                },
                getValue: function () {
                    var data = [];
                    if (me.getValue().length === 0) {
                        return Ext.encode(data);
                    }

                    Ext.iterate(me.getValue(), function (record, ix) {
                        var row = record.data;
                        var rowJson = {};
                        Ext.iterate(grid.store.fields.items, function (fld, i) {
                            if (fld.name != 'recId') {
                                rowJson[fld.name] = dataTypeConversion(row[fld.name], fld.type);
                                if (rowJson[fld.name] === null || !Ext.isDefined(rowJson[fld.name]) || rowJson[fld.name] === '') {
                                    if (grid.colModel.getColumnById(fld.name).hidden) {
                                        if (hiddenFieldVals[fld.name] !== null && Ext.isDefined(hiddenFieldVals[fld.name]) &&
                                            hiddenFieldVals[fld.name] !== '') {

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
                    var valid = true;

                    // Step 1
                    // if keys are defined ensure unique recs
                    var recId = 0;
                    var keyId = 0;
                    var recArray = [];
                    var tmpRecs = me.getValue();
                    for (recId = 0; recId < tmpRecs.length; recId++) {
                        var data = tmpRecs[recId].data;
                        var keyData = '';

                        for (keyId = 0; keyId < grid.keys.length; keyId++) {
                            keyData = keyData + "_" + data[grid.keys[keyId]];
                        }
                        if (recArray.contains(keyData)) {
                            return false;
                        }
                        recArray.push(keyData);
                    }

                    // Step 2
                    // if field is allowed to be blank, field is valid
                    if (me.allowBlank) {
                        return valid;
                    }

                    Ext.iterate(me.getValue(), function (record, ix) {
                        var row = record.data;
                        Ext.iterate(grid.store.fields.items, function (fld, i) {

                            // if blank value is not valid for this field
                            if (fld.allowBlank === false) {
                                if (row[fld.name] === null || !Ext.isDefined(row[fld.name]) || row[fld.name] === '') {
                                    valid = false;
                                }
                            }
                        });
                    });
                    return valid;
                }
            });
        });
    },
    getValue: function () {
        return this.store.getRange();
    }

});

Ext.reg('editorgridfield', OE.EditorGridField);
