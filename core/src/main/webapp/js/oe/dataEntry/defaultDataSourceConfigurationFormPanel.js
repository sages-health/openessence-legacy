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

Ext.namespace("OE.input.datasource.form");

Ext.override(Ext.TabPanel, {
    /**
     * Set the title of a specific tab
     */
    setTabTitle: function (itemId, newTitle) {
        var tabEl = this.getTabEl(itemId);
        Ext.fly(tabEl).child('.x-tab-strip-text', true).innerHTML = newTitle;
    }
});

/**
 * Default data source configuration function to build/populate form.
 *
 * Requires configuration (consisting of data, data source name, item id and a call back).
 */
OE.input.datasource.form.init = function (configuration) {

    var fields = [];

    var metadata = {};

    var primaryKeys;

    if (configuration.data) {
        metadata = (configuration.data.meta && configuration.data.meta.form ? configuration.data.meta.form : {});
        primaryKeys = (configuration.data ? configuration.data.pks : undefined);

        // Create form fields
        Ext.each(configuration.data.editDimensions || {}, function (editDimension) {
            fields.push(createFormFieldFromDimension(editDimension));
        });
    }

    /**
     * Function to build a form field using an edit dimension.
     */
    function createFormFieldFromDimension(dimension) {
        /*jshint evil: true */

        var dimensionMetadata = (dimension.meta && dimension.meta.form ? dimension.meta.form : {});
        var field = Ext.applyIf({ formConfig: configuration }, dimensionMetadata);

        // Generic dimension configuration items
        field.name = dimension.name;
        field.fieldLabel = OE.util.getDimensionName(configuration.dataSource, dimension.name);
        field.dimension = dimension;
        field.dimensionMetadata = dimensionMetadata;

        // Setting qtip, if it exists (moved for grids)
        var dimensionNameQtip = dimension.name + '.qtip';
        var qtip = OE.util.getDimensionName(configuration.dataSource, dimensionNameQtip);
        if (qtip != dimensionNameQtip) {
            field.qtip = qtip;
        }

        // Setting help, if it exists (moved for grids)
        var dimensionNameHelp = dimension.name + '.help';
        var help = OE.util.getDimensionName(configuration.dataSource, dimensionNameHelp);
        if (help != dimensionNameHelp) {
            field.help = help;
        }

        // Generic meta data configuration items
        // Allow blank is defaulted on the field set at data source level meta data
        if (Ext.isDefined(dimensionMetadata.allowBlank)) {
            field.allowBlank = OE.util.getBooleanValue(dimensionMetadata.allowBlank, false);
        }

        // Width is defaulted on the field set at data source level meta data
        if (Ext.isDefined(dimensionMetadata.width)) {
            field.width = OE.util.getNumberValue(dimensionMetadata.width, 200);
        }
        // Height is defaulted on the field set at data source level meta data
        if (Ext.isDefined(dimensionMetadata.height)) {
            field.height = OE.util.getNumberValue(dimensionMetadata.height, 20);
        }

        // Type specific meta data configuration items or hidden (not editable and/or primary key)
        if (dimensionMetadata.xtype === 'hidden') {
            field.xtype = 'hidden';
            // ItemId used to flag edits
        } else if (configuration.itemId && (primaryKeys && primaryKeys.indexOf(dimension.name) >= 0)) {
            field.xtype = 'hidden';
        } else if (dimensionMetadata.custom) {
            if (dimensionMetadata.custom.fieldFunction) {
                var fieldFunction = eval(dimensionMetadata.custom.fieldFunction);
                //apply the result of calling custom field function
                Ext.apply(field, fieldFunction(dimensionMetadata.custom.fieldParameters));
                //apply any possible overrides in custom datasource configuration
                Ext.apply(field, dimensionMetadata.custom.fieldProperties);
            }
        } else if (dimensionMetadata.xtype === 'categorygridfield') {
            field.xtype = dimensionMetadata.xtype;
        } else if (dimensionMetadata.xtype === 'editorgridfield') {
            field.xtype = dimensionMetadata.xtype;
            field.dataSource = configuration.dataSource; // FIXME why isn't this set for all fields?
            field.dimension = dimension;
            field.dimensionMetadata = dimensionMetadata;
            field.width = OE.util.getNumberValue(dimensionMetadata.width, 600);
            field.height = OE.util.getNumberValue(dimensionMetadata.height, 200);
        } else if (dimension.possibleValues) {
            if (dimensionMetadata.xtype === 'grid') {
                field.xtype = 'fieldset';
                field.layout = 'border';
                field.border = true;
                field.frame = true;
                field.cls = 'reportGridFieldSet';
                field.width = OE.util.getNumberValue(dimensionMetadata.width, 600);
                field.height = OE.util.getNumberValue(dimensionMetadata.height, 200);
                field.name = undefined;
                field.items = [];

                var columnsAndFields = OE.datasource.grid.createColumnsAndFields(dimension.possibleValues.dsId, dimension.possibleValues.detailDimensions, metadata);

                var baseParams = {dsId: dimension.possibleValues.dsId, results: Ext.pluck(dimension.possibleValues.detailDimensions, 'name')};

                var pvChildFks = [];
                var pvDimensionKeys = [];
                var pvFks = dimension.possibleValues.fks;
                Ext.iterate(pvFks, function (key, value) {
                    pvChildFks.push(key);
                    pvDimensionKeys.push(value);
                });

                var parentChildFks = [];
                var parentKeys = [];
                var pFks = dimension.fksToParent;
                Ext.iterate(pFks, function (key, value) {
                    parentChildFks.push(key);
                    pvDimensionKeys.push(value);
                });

                // If true, allows only one result from search (disables add once one record has been selected)
                var singleSelect = OE.util.getBooleanValue(dimensionMetadata.singleSelect, false);

                var dataStore = new OE.data.RestrictedJsonStore({
                    url: OE.util.getUrl('/report/detailsQuery'),
                    method: 'POST',
                    baseParams: baseParams,
                    root: 'rows',
                    // TODO: currently only accepts single id...
                    idProperty: pvDimensionKeys[0],
                    sortInfo: {field: OE.util.getStringValue(dimensionMetadata.sortcolumn, columnsAndFields.columns[0].id), direction: OE.util.getStringValue(dimensionMetadata.sortorder, 'ASC')},
                    fields: columnsAndFields.fields,
                    listeners: {
                        load: function (store, records) {
                            toggleAddButton(records.length);
                        },
                        remove: function (store, records) {
                            toggleAddButton((records.length ? records.length : 0));
                        }
                    }
                });

                var toggleAddButton = function (recordCount) {
                    if (!singleSelect || recordCount < 1) {
                        addButton.enable();
                    } else {
                        addButton.disable();
                    }
                };

                var addButton = new Ext.Button({
                    text: messagesBundle[configuration.dataSource + '.add'] || messagesBundle['input.datasource.default.add'],
                    enabled: false,
                    handler: function () {
                        showSearchForm(Ext.apply(dimension, {store: grid.getStore(), singleSelect: singleSelect}));
                    }
                });

                var dimensionGridMetadata = (dimension.meta && dimension.meta.grid ? dimension.meta.grid : {});

                // We apply a different strategy for grids, add to tbar
                var tbarButtons = [];
                if (field.qtip) {
                    tbarButtons.push(OE.util.getIndicatorIconButton('../../images/information.png', field.qtip));
                    field.qtip = undefined;
                }
                if (field.help) {
                    tbarButtons.push(OE.util.getIndicatorIconButton('../../images/help.png', field.help));
                    field.help = undefined;
                }
                tbarButtons.push(['->', addButton, {
                    text: messagesBundle[configuration.dataSource + '.remove'] || messagesBundle['input.datasource.default.remove'],
                    handler: function () {
                        var selections = grid.getSelectionModel().getSelections();

                        var n = selections.length;
                        if (n > 0) {
                            var refreshLimit = 5;

                            grid.suspendEvents();

                            var store = grid.getStore();
                            Ext.each(selections, function (record) {
                                store.remove(record);

                                if (n <= refreshLimit) {
                                    store.fireEvent("update", store, record, Ext.data.Record.COMMIT);
                                }
                            });
                            grid.resumeEvents();

                            if (n > refreshLimit) {
                                store.fireEvent("datachanged", store);
                            }
                        }
                    }
                }, {
                    text: messagesBundle[configuration.dataSource + '.removeAll'] || messagesBundle['input.datasource.default.removeAll'],
                    handler: function () {
                        grid.getStore().removeAll();
                    }
                }
                ]);

                var grid = new Ext.grid.GridPanel({
                    store: dataStore,
                    region: 'center',
                    cm: new Ext.grid.ColumnModel({
                        defaults: {
                            // TODO: from dimension meta data grid config
                            width: OE.util.getNumberValue(dimensionGridMetadata.width, 150),
                            sortable: OE.util.getBooleanValue(dimensionGridMetadata.sortable, true),
                            hidden: OE.util.getBooleanValue(dimensionGridMetadata.hidden, false)
                        },
                        //filterable: true,
                        columns: columnsAndFields.columns
                    }),
                    // TODO: from dimension meta data grid config
                    sm: new Ext.grid.RowSelectionModel({singleSelect: singleSelect}),
                    border: false,
                    loadMask: new Ext.LoadMask(Ext.getBody(), {
                        msg: messagesBundle['main.loadmask']
                    }),
                    width: (150 * columnsAndFields.columns.length),
                    tbar: tbarButtons
                });
                field.items.push(grid);

                field.items.push({
                    xtype: 'hidden',
                    name: dimension.name,
                    allowBlank: field.allowBlank,
                    isValid: function () {
                        if (field.allowBlank) {
                            return true;
                        }
                        return dataStore && dataStore.getCount() > 0;

                    },
                    setValue: function (v) {
                        this.value = v;
                        if (this.rendered) {
                            this.el.dom.value = (Ext.isEmpty(v) ? '' : v);
                            this.validate();
                        }

                        // Build filter to query for configured child records
                        if (!Ext.isEmpty(v)) {
                            var filter = {};
                            if (Ext.isArray(v)) {
                                filter[pvDimensionKeys[0]] = Ext.pluck(v, pvChildFks[0]);
                            } else if (Ext.isObject(v)) {
                                filter[pvDimensionKeys[0]] = v[pvChildFks[0]];
                            } else {
                                filter[pvDimensionKeys[0]] = v;
                            }

                            dataStore.load({params: filter});
                        }

                        return this;
                    },
                    getValue: function () {
                        var dataArray = [];

                        var fkToParent = formPanel.getForm().findField(parentKeys[0]).getValue();
                        dataStore.each(function (record) {
                            var data = {};
                            data[parentChildFks[0]] = (fkToParent === "" ? null : fkToParent);
                            data[pvChildFks[0]] = record.data[pvDimensionKeys[0]];
                            dataArray.push(data);
                        });

                        return (Ext.encode(dataArray));
                    }
                });
            } else {
                // TODO move this to separate function
                (function () {
                    // One to one (get values)
                    if (dimensionMetadata.xtype === 'multiselect' || dimensionMetadata.xtype === 'superboxselect') {
                        field.xtype = dimensionMetadata.xtype;//'multiselect';
                        var results = [];
                        var baseParams = {dsId: dimension.possibleValues.dsId, results: Ext.pluck(dimension.possibleValues.detailDimensions, 'name')};

                        var pvChildFks = [];
                        var pvDimensionKeys = [];
                        var pvFks = dimension.possibleValues.fks;
                        Ext.iterate(pvFks, function (key, value) {
                            pvChildFks.push(key);
                            pvDimensionKeys.push(value);
                        });

                        var parentChildFks = [];
                        var parentKeys = [];
                        var pFks = dimension.fksToParent;
                        Ext.iterate(pFks, function (key, value) {
                            parentChildFks.push(key);
                            pvDimensionKeys.push(value);
                        });

                        if (dimension.possibleValues.data) {
                            // Load possible value data array
                            field.store = new Ext.data.ArrayStore({
                                fields: ['Id', 'Name'],
                                data: dimension.possibleValues.data
                            });
                            field.valueField = 'Id';
                            field.displayField = 'Name';
                        } else {
                            // Load possible values via details query
                            var storeFields = [];
                            Ext.each(dimension.possibleValues.detailDimensions, function (result) {
                                storeFields.push(OE.datasource.grid.createFieldFromDimension(result));
                                results.push(result.name);
                            });

                            field.store = new OE.data.RestrictedJsonStore({
                                url: OE.util.getUrl('/report/detailsQuery'),
                                method: 'POST',
                                autoLoad: true,
                                baseParams: {dsId: dimension.possibleValues.dsId, results: results, pagesize: 400},
                                sortInfo: {field: OE.util.getStringValue(dimensionMetadata.sortcolumn, results[1]), direction: OE.util.getStringValue(dimensionMetadata.sortorder, 'ASC')},
                                root: 'rows',
                                fields: storeFields
                            });
                            field.valueField = dimensionMetadata.valueField || results[0];
                            field.displayField = dimensionMetadata.displayField || results[1] || results[0];

                        }

                        field.hiddenName = dimension.name;
                        field.mode = 'local';
                        field.triggerAction = 'all';
                        field.typeAhead = true;
                        field.forceSelection = true;
                        field.selectOnFocus = true;
                        field.emptyText = OE.util.getEmptyText(field.fieldLabel);

                        if (field.xtype === 'multiselect') {
                            field.getValue = function () {
                                var dataArray = [];
                                var fkToParent = formPanel.getForm().findField(parentKeys[0]).getValue();
                                if (this.view) {
                                    var selectionsArray = this.view.getSelectedIndexes();
                                    if (selectionsArray.length !== 0) {
                                        for (var i = 0; i < selectionsArray.length; i++) {
                                            var data = {};
                                            data[parentChildFks[0]] = (fkToParent === "" ? null : fkToParent);
                                            data[pvChildFks[0]] =
                                                this.store.getAt(selectionsArray[i]).get(((this.valueField !== null) ? this.valueField : this.valueField));
                                            dataArray.push(data);
                                        }
                                    }
                                }
                                if (dataArray.length > 0) {
                                    return (Ext.encode(dataArray));
                                }
                                //this enables validation to work properly (multiselect checks for value.length < 1)
                                return '';
                            };
                            field.setValue = function (v) {
                                //begin patch
                                // View not rendered.  Defer call to setValue when rendered.
                                if (!this.view) {
                                    this.on('render',
                                        this.setValue.createDelegate(this, [v]), null, {single: true});
                                    return;
                                }
                                // Store not loaded yet? Set value when it *is* loaded.
                                // Defer the setValue call until after the next load.
                                if (this.view.store.getCount() === 0) {
                                    this.view.store.on('load',
                                        this.setValue.createDelegate(this, [v]), null, {single: true});
                                    return;
                                }
                                //end patch

                                //pull just the child id for this widget
                                var values = Ext.pluck(v, pvChildFks[0]);

                                var index;
                                var selections = [];
                                this.view.clearSelections();
                                this.hiddenField.dom.value = '';

                                if (!values || (values === '')) {
                                    return;
                                }

                                if (!(values instanceof Array)) {
                                    values = values.split(this.delimiter);
                                }
                                for (var i = 0; i < values.length; i++) {
                                    index = this.view.store.indexOf(this.view.store.query(this.valueField,
                                        new RegExp('^' + values[i] + '$', "i")).itemAt(0));
                                    selections.push(index);
                                }
                                this.view.select(selections);
                                this.hiddenField.dom.value = values;//this.getValue();
                                this.validate();
                            };
                        } else if (field.xtype === 'superboxselect') {
                            field.getValue = function () {
                                var dataArray = [];

                                var fkToParent = formPanel.getForm().findField(parentKeys[0]).getValue();
                                if (this.rendered) {
                                    this.items.each(function (item) {
                                        var data = {};
                                        data[parentChildFks[0]] = (fkToParent === "" ? null : fkToParent);
                                        data[pvChildFks[0]] = item.value;
                                        dataArray.push(data);
                                    });
                                }
                                if (dataArray.length > 0) {
                                    return (Ext.encode(dataArray));
                                }
                                //this enables validation to work properly (multiselect checks for value.length < 1)
                                return '';
                            };
                            field.setValue = function (v) {
                                if (!this.rendered) {
                                    this.on('render',
                                        this.setValue.createDelegate(this, [v]), null, {single: true});
                                    return;
                                }
                                // Store not loaded yet? Set value when it *is* loaded.
                                // Defer the setValue call until after the next load.
                                if (this.store.getCount() === 0) {
                                    this.store.on('load',
                                        this.setValue.createDelegate(this, [v]), null, {single: true});
                                    return;
                                }
                                //end patch

                                //pull just the child id for this widget
                                var value = Ext.pluck(v, pvChildFks[0]);
                                this.removeAllItems().resetStore();
                                this.remoteLookup = [];

                                if (Ext.isEmpty(value)) {
                                    return;
                                }

                                var values = value;
                                if (!Ext.isArray(value)) {
                                    value = '' + value;
                                    values = value.split(this.valueDelimiter);
                                }

                                Ext.each(values, function (val) {
                                    var record = this.findRecord(this.valueField, val);
                                    if (record) {
                                        this.addRecord(record);
                                    } else if (this.mode === 'remote') {
                                        this.remoteLookup.push(val);
                                    }
                                }, this);

                                if (this.mode === 'remote') {
                                    var q = this.remoteLookup.join(this.queryValuesDelimiter);
                                    this.doQuery(q, false, true); //3rd param to specify a values query
                                }
                            };
                        }
                    } else {
                        // TODO move this to separate function
                        (function () {
                            field.xtype = dimensionMetadata.xtype || 'combo';
                            var results = [];

                            if (dimension.possibleValues.data) {
                                // Load possible value data array
                                field.store = new Ext.data.ArrayStore({
                                    fields: ['Id', 'Name'],
                                    data: dimension.possibleValues.data
                                });
                                field.valueField = 'Id';
                                field.displayField = 'Name';
                            } else {
                                // Load possible values via details query
                                var storeFields = [];
                                Ext.each(dimension.possibleValues.detailDimensions, function (result) {
                                    storeFields.push(OE.datasource.grid.createFieldFromDimension(result));
                                    results.push(result.name);
                                });

                                field.store = new OE.data.RestrictedJsonStore({
                                    url: OE.util.getUrl('/report/detailsQuery'),
                                    method: 'POST',
                                    autoLoad: true,
                                    baseParams: {dsId: dimension.possibleValues.dsId, results: results, pagesize: 400},
                                    sortInfo: {field: OE.util.getStringValue(dimensionMetadata.sortcolumn, results[1]), direction: OE.util.getStringValue(dimensionMetadata.sortorder, 'ASC')},
                                    root: 'rows',
                                    fields: storeFields
                                });
                                field.valueField = dimensionMetadata.valueField || results[0];
                                field.displayField = dimensionMetadata.displayField || results[1] || results[0];
                            }

                            field.hiddenName = dimension.name;
                            field.mode = 'local';
                            field.triggerAction = 'all';
                            field.typeAhead = true;
                            field.forceSelection = true;
                            field.selectOnFocus = true;
                            field.emptyText = OE.util.getEmptyText(field.fieldLabel);
                        })();
                    }
                })();
            }
        } else {
            switch (dimension.type) {
                case 'BOOLEAN':
                    field.xtype = dimensionMetadata.xtype || 'checkbox';
                    break;
                case 'Int':
                case 'INTEGER':
                case 'LONG':
                    field.xtype = dimensionMetadata.xtype || 'numberfield';
                    field.minValue = OE.util.getNumberValue(dimensionMetadata.minValue, Number.NEGATIVE_INFINITY);
                    field.maxValue = OE.util.getNumberValue(dimensionMetadata.maxValue, Number.MAX_VALUE);
                    field.allowDecimals = OE.util.getBooleanValue(dimensionMetadata.allowDecimals, false);
                    field.allowNegative = OE.util.getBooleanValue(dimensionMetadata.allowNegative, true);
                    break;
                case 'DOUBLE':
                case 'FLOAT':
                    field.xtype = dimensionMetadata.xtype || 'numberfield';
                    field.minValue = OE.util.getNumberValue(dimensionMetadata.minValue, Number.NEGATIVE_INFINITY);
                    field.maxValue = OE.util.getNumberValue(dimensionMetadata.maxValue, Number.MAX_VALUE);
                    field.allowDecimals = OE.util.getBooleanValue(dimensionMetadata.allowDecimals, true);
                    field.decimalPrecision = OE.util.getNumberValue(dimensionMetadata.decimalPrecision, 2);
                    field.allowNegative = OE.util.getBooleanValue(dimensionMetadata.allowNegative, true);
                    break;
                case 'String':
                case 'TEXT':
                    if (Ext.isDefined(dimensionMetadata.maxLength)) {
                        field.maxLength = OE.util.getNumberValue(dimensionMetadata.maxLength, Number.MAX_VALUE);
                    }

                    // Options are: textfield, textarea, and colorfield
                    field.xtype = dimensionMetadata.xtype ||
                        OE.util.getStringValue(dimensionMetadata.xtype, 'textfield');
                    if (field.xtype === 'colorfield') {
                        // Override afterRender for colorfield
                        // IE and firefox did not render color field correctly
                        // if value of this field was undefined
                        field.afterRender = function () {
                            Ext.ux.ColorField.superclass.afterRender.call(this);

                            if (this.value && Ext.isDefined(this.value)) {
                                this.el.setStyle('background', this.value);
                            } else if (this.formConfig && this.formConfig.record && this.formConfig.record[field.name]) {
                                this.el.setStyle('background', this.formConfig.record[field.name]);
                            }
                            this.detectFontColor();
                        };
                    }

                    //Hide passwords
                    if (field.xtype === 'textfield') {
                        if (OE.util.getBooleanValue(dimensionMetadata.password, false)) {

                            field.xtype = dimensionMetadata.xtype || 'fieldset';
                            field.width = OE.util.getNumberValue(metadata.labelWidth, 500);
                            field.border = false;
                            field.cls = 'reportFormPanel';
                            field.layout = 'linkform';

                            var confirmpass = new Ext.form.TextField({
                                xtype: 'textfield',
                                itemId: 'confirmpass',
                                width: OE.util.getNumberValue(metadata.labelWidth, 200),
                                height: OE.util.getNumberValue(metadata.labelHeight, 20),
                                dependency: 'pass',
                                fieldLabel: (messagesBundle['input.datasource.default.confirm'] + ' ' + field.fieldLabel),
                                inputType: 'password',
                                vtype: 'password',
                                allowBlank: field.allowBlank,
                                skip: true,
                                name: 'confirmpassword'
                            });

                            var pass = new Ext.ux.PasswordMeter({
                                //new Ext.form.TextField({
                                itemId: 'pass',
                                width: OE.util.getNumberValue(metadata.labelWidth, 200),
                                height: OE.util.getNumberValue(metadata.labelHeight, 20),
                                dependency: 'confirmpass',
                                fieldLabel: field.fieldLabel,
                                inputType: 'password',
                                allowBlank: field.allowBlank,
                                hideLabel: false,
                                name: field.name,
                                help: field.help,
                                qtip: field.qtip,
                                listeners: {
                                    resize: function (comp, adjWidth) {
                                        this.objMeter.setWidth(adjWidth);
                                    }
                                },
                                //listen for validateValue call
                                validator: function () {
                                    if (confirmpass.getValue() !== '') {
                                        confirmpass.validate();
                                    }
                                    //return true to continue validateValue process
                                    return true;
                                },
                                setValue: function (v) {
                                    // Set value of confirm field so both match on load
                                    confirmpass.setValue(v);
                                    Ext.ux.PasswordMeter.superclass.setValue.call(this, v);
                                    if (this.objMeter) {
                                        this.updateMeterVal(this, v);
                                    }
                                    confirmpass.validate();
                                    return this;
                                }
                            });

                            field.items = [pass, confirmpass];

                            // Clear out field label for fieldset
                            field.fieldLabel = '';
                        }
                    }
                    break;
                case 'Date':
                case 'DATE':
                case 'Date_Time':
                case 'DATE_TIME':
                    Ext.applyIf(field, {
                        xtype: 'datefield',
                        format: OE.util.getStringValue(dimensionMetadata.format, OE.util.defaultDateFormat),
                        value: new Date()
                    });
                    break;
                default:
                    field.xtype = dimensionMetadata.xtype || 'textfield';
                    break;
            }
        }

        return field;
    }

    function showSearchForm(configuration) {
        var win = null;

        var form = OE.input.datasource.search.form({
            dataSource: configuration.possibleValues.dsId,
            data: configuration.possibleValues,
            singleSelect: configuration.singleSelect,
            callback: function (values) {
                if (values && configuration.store) {
                    var newData = Ext.pluck(values.results, 'data');
                    Ext.each(newData, function (item) {
                        if (!configuration.store.data.containsKey(item[configuration.store.idProperty])) {
                            configuration.store.loadData({rows: item}, true);
                        }
                    });
                }
                win.close();
            }
        });

        win = new Ext.Window({
            layout: 'fit',
            title: messagesBundle[configuration.dataSource + '.search'] || messagesBundle['input.datasource.default.search'],
            height: 600,
            minHeight: 400,
            width: 500,
            minWidth: 300,
            closable: true,
            resizable: true,
            plain: true,
            border: false,
            modal: true,
            items: [form]
        });
        win.show();

        return form;
    }

    var formPanel = new Ext.form.FormPanel({
        itemId: configuration.itemId || null,
        title: (configuration.itemId ? configuration.itemId : (messagesBundle[configuration.dataSource + '.new'] || messagesBundle['input.datasource.default.new'])),
        cls: 'reportFormPanel',
        layout: 'anchor',
        autoHeight: true,
        autoWidth: true,
        closable: true,
        autoScroll: true,
        border: false,
        monitorValid: true,
        items: [
            {
                xtype: 'fieldset',
                cls: 'reportFieldSet',
                layout: 'linkform',
                autoWidth: true,
                border: false,
                labelWidth: OE.util.getNumberValue(metadata.labelWidth, 150),
                defaults: {
                    width: OE.util.getNumberValue(metadata.width, 200),
                    allowBlank: OE.util.getBooleanValue(metadata.allowBlank, false)
                },
                items: fields
            }
        ],
        buttonAlign: 'left',
        buttons: [
            {
                text: messagesBundle[configuration.dataSource + '.save'] || messagesBundle['input.datasource.default.save'],
                formBind: true,
                handler: function () {
                    var values = formPanel.getForm().getFieldValues();
                    values.dsId = configuration.dataSource;

                    OE.data.doAjaxRestricted({
                        url: OE.util.getUrl('/input/' + (configuration.itemId ? 'update' : 'add')),
                        method: 'POST',
                        params: values,
                        onJsonSuccess: function (response) {
                            // if it is an update request, update form using response
                            if (response.success) {
                                formPanel.getForm().setValues(response, true);
                                var tabPanel = formPanel.findParentByType('tabpanel');
                                // once the changes are saved, remove * from the tab title
                                if (tabPanel) {
                                    var title = tabPanel.activeTab.title;
                                    var newTitle = title.substring(0, 1) == '*' ? title.substring(1) : title;
                                    tabPanel.setTabTitle(tabPanel.activeTab.itemId, newTitle);
                                }
                                // Show the success message once the changes are saved
                                if (configuration.itemId && configuration.showMessageOnUpdate === true) {
                                    Ext.MessageBox.alert(messagesBundle['input.datasource.default.update.message.title'],
                                        messagesBundle['input.datasource.default.update.message.text']);
                                }
                            }

                            if (configuration.callback) {
                                configuration.callback(formPanel);
                            }
                        },
                        onRelogin: {callback: this, args: []}
                    });

                    // disable form to prevent resubmissions
                    formPanel.disable();
                },
                scope: configuration
            }
        ]
    });

    // For each field, add onchange event so that we can update tabpanel title
    formPanel.getForm().items.each(function (field) {
        field.on('change', function () {
            var tabPanel = formPanel.findParentByType('tabpanel');
            if (tabPanel && tabPanel.activeTab.itemId) {
                var title = tabPanel.activeTab.title;
                var newTitle = (title.substring(0, 1) == '*' ? '' : '*') + title;
                tabPanel.setTabTitle(tabPanel.activeTab.itemId, newTitle);
            }
        });
    });

    return formPanel;
};
