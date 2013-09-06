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

Ext.namespace("OE.report");

/**
 * Default data source report function to build/populate form.
 *
 * Requires configuration (consisting of data, data source name, item id and a call backs).
 */
OE.report.ReportForm = Ext.extend(Ext.form.FormPanel, {
    constructor: function (configuration) {
        var me = this;

        this.filters = {};

        this.fields = [];
        this.timeseriesFields = [];
        this.accumulationIds = [];
        this.metadata = {};

        // set dates to default values
        this.setStartDate();
        this.setEndDate();

        this.dataSource = configuration.dataSource;

        if (configuration.data) {
            this.metadata =
                (configuration.data.meta && configuration.data.meta.form ? configuration.data.meta.form : {});

            // Create form fields
            Ext.each(configuration.data.filters || {}, function (filterDimension) {
                if (['timeseriesGroupResolution', 'timeseriesDetectorClass', 'timeseriesDenominator'].indexOf(filterDimension.name) != -1) {
                    // Filter out fields only used for building a time series
                    me.timeseriesFields.push(me.createFormFieldFromDimension(filterDimension));
                } else {
                    me.fields.push(me.createFormFieldFromDimension(filterDimension));
                }
            });
        }

        // Create required (hidden) hidden fields
        this.fields.push({xtype: 'textfield', inputType: 'hidden', name: 'dsId', value: configuration.dataSource});
        // hidden timezone
        var tmpDate = new Date();
        var timezoneOffset = tmpDate.getTimezoneOffset();
        this.fields.push({xtype: 'textfield', inputType: 'hidden', name: 'timezoneOffset', value: timezoneOffset});

        // create buttons
        var buttons = [];
        buttons.push({
            text: messagesBundle['query.clear'],
            handler: function () {
                me.clearForm();
            },
            scope: configuration
        });

        if (configuration.timeseriesCallback && OE.util.getBooleanValue(this.metadata.supportTimeseries, true)) {
            buttons.push({
                text: messagesBundle['query.timeseries'],
                handler: function () {
                    var selectedAccumulations = me.getForm().findField('accumId').getValue();
                    var accumulations = [];
                    Ext.each(configuration.data.detailDimensions, function (dimension) {
                        var name = dimension.name;
                        // Add all if no accumulations are selected, else add selected
                        if (me.accumulationIds.indexOf(name) >= 0 && (selectedAccumulations === "" || selectedAccumulations.indexOf(name) != -1)) {
                            accumulations.push(name);
                        }
                    });

                    me.showTimeSeriesParamatersForm({accumulations: accumulations});
                },
                scope: configuration
            });
        }

        if (configuration.chartsCallback && OE.util.getBooleanValue(this.metadata.supportCharts, true)) {
            buttons.push({
                text: messagesBundle['query.charts'],
                handler: function () {
                    // Get group by dimensions (for user selection)
                    var dimensions = [];
                    var accumulations = [];
                    var selectedAccumulations = me.getForm().findField('accumId').getValue();
                    Ext.each(configuration.data.detailDimensions, function (dimension) {
                        var name = dimension.name;
                        if (me.accumulationIds.indexOf(name) == -1) {
                            // Add non accumulation details dimensions
                            dimensions.push([name, OE.util.getDimensionName(configuration.dataSource, dimension.name)]);
                        } else if (selectedAccumulations === "" || selectedAccumulations.indexOf(name) != -1) {
                            // Add all if no accumulations selected, else only add selected accumulations
                            accumulations.push([name, OE.util.getDimensionName(configuration.dataSource, dimension.name)]);
                        }
                    });
                    me.showChartSelectionForm({dimensions: dimensions, accumulations: accumulations});
                }
            });
        }

        if (configuration.detailsNoGroupByCallback && OE.util.getBooleanValue(this.metadata.supportDetails, true)) {
            buttons.push({
                text: messagesBundle['query.details'],
                handler: function () {
                    configuration.detailsNoGroupByCallback(me.getForm().getFieldValues());
                },
                scope: configuration
            });
        }

        if (configuration.detailsCallback && OE.util.getBooleanValue(this.metadata.supportDetails, true)) {
            buttons.push({
                text: messagesBundle['query.details'],
                handler: function () {
                    // Get group by dimensions (for user selection)
                    var dimensions = [];
                    Ext.each(configuration.data.detailDimensions, function (dimension) {
                        var name = dimension.name;
                        if (me.accumulationIds.indexOf(name) == -1) {
                            dimensions.push([name, OE.util.getDimensionName(configuration.dataSource, dimension.name)]);
                        }
                    });
                    me.showGroupBySelectionForm({dimensions: dimensions});
                },
                scope: configuration
            });
        }

        if (configuration.pivotCallback && OE.util.getBooleanValue(this.metadata.supportPivots, true)) {
            buttons.push({
                text: messagesBundle['query.pivot'],
                handler: function () {
                    // Get all result dimensions
                    var results = [];
                    Ext.each(configuration.data.detailDimensions, function (dimension) {
                        var name = dimension.name;
                        if (me.accumulationIds.indexOf(name) == -1) {
                            results.push([name, OE.util.getDimensionName(configuration.dataSource, dimension.name), dimension]);
                        }
                    });

                    var filters = me.getForm().getFieldValues();
                    var dsId = filters.dsId; // TODO don't store DS ID as hidden field
                    delete filters.dsId;

                    // Add the selected accumulation or all (for the grid)
                    var selectedAccumulations = filters['accumId'];
                    if (selectedAccumulations) {
                        results = results.concat(selectedAccumulations);
                    } else {
                        results = results.concat(me.accumulationIds);
                    }

                    configuration.pivotCallback({
                        dsId: dsId,
                        filters: filters,
                        results: results
                    });
                },
                scope: configuration
            });
        }

        if (configuration.mapCallback && OE.util.getBooleanValue(this.metadata.supportMap, true)) {
            buttons.push({
                text: messagesBundle['query.map'],
                handler: function () {
                    var filters = me.getForm().getFieldValues();
                    var dsId = filters.dsId;
                    delete filters.dsId;

                    configuration.mapCallback({
                        dsId: dsId,
                        filters: filters
                    });
                },
                scope: configuration
            });
        }

        configuration = Ext.apply(configuration, {
            itemId: configuration.itemId || null,
            title: messagesBundle[configuration.dataSource + '.queryentry'] || messagesBundle['panel.query.queryentry'],
            cls: 'reportPanel',
            region: 'north',
            cmargins: '0 0 6 0',
            autoHeight: true,
            collapsible: true,
            plugins: new Ext.ux.collapsedPanelTitlePlugin(),
            floatable: false,
            autoScroll: true,
            border: false,
            frame: true,
            monitorValid: true,
            items: [
                {
                    xtype: 'fieldset',
                    cls: 'reportFieldSet',
                    layout: 'linkform',
                    autoWidth: true,
                    border: false,
                    labelWidth: OE.util.getNumberValue(this.metadata.labelWidth, 150),
                    defaults: {
                        width: OE.util.getNumberValue(this.metadata.width, 200)
                    },
                    items: this.fields
                }
            ],
            buttonAlign: 'left',
            buttons: buttons
        });

        OE.report.ReportForm.superclass.constructor.call(this, configuration);
    },

    getFilters: function () {
        return this.filters;
    },

    /**
     * Returns the current set of filters, but with date start and end converted to lengthOfTime.
     * Does not modify this object.
     */
    convertDateFiltersToLength: function () {
        if (this.startDateFieldId && this.endDateFieldId) {
            var filters = Ext.apply({}, this.filters); // clone

            filters.lengthOfTime = filters[this.endDateFieldId] - filters[this.startDateFieldId];
            delete filters[this.endDateFieldId];
            delete filters[this.startDateFieldId];

            return filters;
        } else {
            // no date interval on this form
            return this.filters;
        }
    },

    populate: function (parameters) {
        // store all filters so we can populate future forms (e.g. TS popup)
        this.filters = parameters.filters || {};
        this.results = parameters.results || [];
        this.charts = parameters.charts || [];

        this.clearForm();
        // 2013-04-30, GGF/SCC: We might have to add more parameters for
        // other "hidden" fields.  This was a side-effect of the 'dsId'
        // field being an xtype of "text", but HTML hidden.
        this.getForm().setValues(Ext.apply({
            dsId: parameters.dsId
        }, this.filters));
    },

    getDefaultStartDate: function () {
        return OE.util.getDate(dimensionsBundle['date.window']);
    },

    getStartDate: function () {
        return this.startDate;
    },

    /**
     * Set the start date on the form. Used for rolling query window.
     */
    setStartDate: function (date) {
        if (!date) {
            date = this.getDefaultStartDate();
        }
        // store start date even if there's no field for it yet so that when we
        // do init the field we can set its value
        this.startDate = date;

        if (this.startDateFieldId) {
            this.getForm().findField(this.startDateFieldId).setValue(date);
            this.filters[this.startDateFieldId] = date;
        }
    },

    getDefaultEndDate: function () {
        return OE.util.getDate(0);
    },

    getEndDate: function () {
        return this.endDate;
    },

    setEndDate: function (date) {
        if (!date) {
            date = this.getDefaultEndDate();
        }
        this.endDate = date;

        if (this.endDateFieldId) {
            this.getForm().findField(this.endDateFieldId).setValue(date);
            this.filters[this.startEndFieldId] = date;
        }
    },

    clearForm: function () {
        var form = this.getForm();
        for (var i = 0; i < form.items.length; i++) {
            var fld = form.items.get(i);
            if (fld.name && (fld.name != 'dsId') && (fld.name != 'timezoneOffset')) {
                if ((fld.xtype && ((fld.xtype == 'combo') || (fld.xtype == 'superboxselect'))) ||
                    (fld.vtype && ((fld.vtype == 'combo') || (fld.vtype == 'superboxselect')))) {
                    fld.clearValue();
                    fld.applyEmptyText();
                } else if ((fld.xtype && (fld.xtype == 'daterange')) || (fld.vtype && (fld.vtype == 'daterange'))) {
                    fld.reset();
                } else {
                    fld.setValue(null);
                    fld.setRawValue(null);
                }
            }
        }
    },

    getSelectedDisplayVals: function () {
        /*jshint loopfunc: true */

        var form = this.getForm();
        var items = form.items;
        var result = {};

        for (var i = 0; i < items.length; i++) {
            var fld = items.get(i);
            if (fld.getRawValue() && fld.getRawValue() !== '') {
                switch (fld.xtype) {
                    case 'combo':
                    case 'datefield':
                        result[fld.name] = fld.getRawValue();
                        break;
                    case 'superboxselect':
                    case 'multiselect':
                        var res = [];
                        Ext.each(fld.getValue(), function (id) {
                            var index = fld.store.find('Id', id);
                            if (index != -1) {
                                res.push(fld.store.getAt(index).data.Name);
                            }
                        });
                        result[fld.name] = res;
                        break;
                    // default will take care of textfield and numberfield
                    default:
                        result[fld.name] = fld.getValue();
                        break;
                }
            }
        }
        return result;
    },

    showTimeSeriesParamatersForm: function (formConfiguration) {
        var me = this;

        // pre-populate time series form
        if (this.filters) {
            Ext.each(this.timeseriesFields, function (tsField) {
                var tsFilter = me.filters[tsField.name];
                if (tsFilter) {
                    tsField.value = tsFilter;
                } else {
                    // don't want to use another tab's TS filter
                    delete tsField.value;
                }
            });
        }

        var form = OE.report.datasource.timeseries.form({
            fields: this.timeseriesFields,
            accumulations: formConfiguration.accumulations,
            metadata: me.metadata,
            dataSource: this.dataSource,
            callback: function (records) {
                if (Ext.isDefined(records)) {
                    var filters = me.getForm().getFieldValues();
                    var ds = filters.dsId;
                    delete filters.dsId;

                    Ext.apply(filters, records);
                    //filters.displayVals = me.getSelectedDisplayVals();
                    me.timeseriesCallback({
                        filters: filters,
                        dsId: ds
                    });
                }

                Ext.getCmp('timeseriesFormWindow').close();
            }
        });

        var win = new Ext.Window({
            layout: 'fit',
            id: 'timeseriesFormWindow',
            title: messagesBundle['input.datasource.default.timeseries'],
            height: 180,
            minHeight: 180,
            width: 420,
            minWidth: 420,
            closable: true,
            resizable: true,
            plain: true,
            border: false,
            modal: true,
            items: [form]
        });
        win.show();

        return form;
    },

    showChartSelectionForm: function (formConfiguration) {
        var me = this;

        var form = OE.report.datasource.chart.form({
            fields: this.timeseriesFields,
            metadata: this.metadata,
            dataSource: this.dataSource,
            dimensions: formConfiguration.dimensions,
            accumulations: formConfiguration.accumulations,
            charts: this.charts, // to auto-populate chart parameters
            callback: function (records) {
                if (Ext.isDefined(records)) {
                    var filters = me.getForm().getFieldValues();
                    var dsId = filters.dsId; // TODO don't store DS ID as hidden field
                    delete filters.dsId;

                    me.chartsCallback({
                        dsId: dsId,
                        filters: filters,
                        displayVals: me.getSelectedDisplayVals(),
                        charts: records.charts
                    });
                }

                Ext.getCmp('chartSelectionFormWindow').close();
            }
        });

        var win = new Ext.Window({
            layout: 'fit',
            id: 'chartSelectionFormWindow',
            title: messagesBundle['input.datasource.default.chart'],
            height: 300,
            minHeight: 300,
            width: 470,
            minWidth: 470,
            closable: true,
            resizable: true,
            plain: true,
            border: false,
            modal: true,
            items: [form]
        });
        win.show();

        return form;
    },

    showGroupBySelectionForm: function (formConfiguration) {
        var me = this;

        var form = OE.report.datasource.groupBy.form({
            data: formConfiguration.dimensions,
            dataSource: this.dataSource,
            results: this.results,
            callback: function (records) {
                if (Ext.isDefined(records)) {
                    var filters = me.getForm().getFieldValues();
                    var dsId = filters.dsId; // TODO don't store DS ID as hidden field
                    delete filters.dsId;

                    var data = Ext.pluck(records.results, 'data');
                    var results = Ext.pluck(data, 'dimensionId');

                    // Add the selected accumulation or all (for the grid)
                    var selectedAccumulations = filters.accumId;
                    if (selectedAccumulations) {
                        results = results.concat(selectedAccumulations);
                    } else {
                        results = results.concat(me.accumulationIds);
                    }

                    me.detailsCallback({
                        dsId: dsId,
                        filters: filters,
                        results: results
                    });
                }

                Ext.getCmp('groupBySelectionFormWindow').close();
            }
        });

        var grid = form.getComponent('groupByGrid');
        grid.on('afterrender', function () {
            Ext.each(me.results, function (dim) {
                var selectionModel = grid.getSelectionModel();
                selectionModel.selectRow(grid.store.find('dimensionId', dim), true);
            });
        });

        var win = new Ext.Window({
            layout: 'fit',
            id: 'groupBySelectionFormWindow',
            title: messagesBundle['input.datasource.default.groupBy'],
            height: 400,
            minHeight: 250,
            width: 250,
            minWidth: 200,
            closable: true,
            resizable: true,
            plain: true,
            border: false,
            modal: true,
            items: [form]
        });
        win.show();

        return form;
    },

    /**
     * Function to build a form field using a filter dimension.
     */
    createFormFieldFromDimension: function (dimension) {
        var field = {};

        // Generic dimension configuration items
        field.name = dimension.name;
        field.fieldLabel = OE.util.getDimensionName(this.dataSource, dimension.name);

        // Setting qtip, if it exists
        var dimensionNameQtip = dimension.name + '.qtip';
        var qtip = OE.util.getDimensionName(this.dataSource, dimensionNameQtip);
        if (qtip != dimensionNameQtip) {
            field.qtip = dimensionNameQtip;
        }

        var dimensionMetadata = (dimension.meta && dimension.meta.form ? dimension.meta.form : {});

        // Generic meta data configuration items
        // Width is defaulted on the field set at data source level meta data
        if (Ext.isDefined(dimensionMetadata.width)) {
            field.width = OE.util.getNumberValue(dimensionMetadata.width, 200);
        }

        // Height is defaulted on the field set at data source level meta data
        if (Ext.isDefined(dimensionMetadata.height)) {
            field.height = OE.util.getNumberValue(dimensionMetadata.height, 100);
        }

        field.allowBlank = OE.util.getBooleanValue(dimensionMetadata.allowBlank, true);

        // Type specific meta data configuration items or hidden (not editable and/or primary key)
        if (dimensionMetadata.xtype === 'hidden') {
            field.xtype = 'hidden';
        } else if (dimension.possibleValues) {
            // One to one (get values)
            field.xtype = (OE.util.getStringValue(dimensionMetadata.xtype, 'combo'));
            if (field.xtype == 'superboxselect') {
                field.listeners = {
                    additem: function () {
                        this.findParentByType('panel').doLayout();
                    }
                };
                field.getValue = function () {
                    var ret = [];
                    this.items.each(function (item) {
                        ret.push(item.value);
                    });
                    return ret.length > 0 ? ret : '';
                };
            }
            var results = [];

            if (dimension.possibleValues.data) {
                // Load possible value data array

                // FIXME we're counting on dimensions that don't want to be translated not being in dimensions.properties
                // TODO allow dimensions to turn off i18n to prevent accidental translation
                field.store = new Ext.data.ArrayStore({
                    fields: ['Id', {name: 'Name', convert: function (v, record) {
                        return OE.util.getDimensionName(this.dataSource, v);
                    }
                    }],
//                    sortInfo: {field: 'Name', direction: OE.util.getStringValue(dimensionMetadata.sortorder, 'ASC')},
                    data: dimension.possibleValues.data
                });
                field.valueField = 'Id';
                field.displayField = 'Name';

                // Prestore accumulation Ids
                if (field.name == 'accumId') {
                    this.accumulationIds = field.store.collect('Id');
                }
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
                    baseParams: {
                        dsId: dimension.possibleValues.dsId,
                        results: results,
                        pagesize: 400
                    },
                    sortInfo: {
                        field: OE.util.getStringValue(dimensionMetadata.sortcolumn, results[1]),
                        direction: OE.util.getStringValue(dimensionMetadata.sortorder, 'ASC')
                    },
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
        } else {
            // Type specific meta data configuration items
            switch (dimension.type) {
                case 'Int':
                case 'INTEGER':
                case 'LONG':
                    field.xtype = 'numberfield';
                    field.allowDecimals = OE.util.getBooleanValue(dimensionMetadata.allowDecimals, false);
                    field.allowNegative = OE.util.getBooleanValue(dimensionMetadata.allowNegative, true);
                    field.maxValue = OE.util.getNumberValue(dimensionMetadata.maxValue, Number.MAX_VALUE);
                    field.minValue = OE.util.getNumberValue(dimensionMetadata.minValue, Number.NEGATIVE_INFINITY);
                    break;
                case 'DOUBLE':
                case 'FLOAT':
                    field.xtype = 'numberfield';
                    field.allowDecimals = OE.util.getBooleanValue(dimensionMetadata.allowDecimals, true);
                    field.decimalPrecision = OE.util.getNumberValue(dimensionMetadata.decimalPrecision, 2);
                    field.allowNegative = OE.util.getBooleanValue(dimensionMetadata.allowNegative, true);
                    field.maxValue = OE.util.getNumberValue(dimensionMetadata.maxValue, Number.MAX_VALUE);
                    field.minValue = OE.util.getNumberValue(dimensionMetadata.minValue, Number.NEGATIVE_INFINITY);
                    break;
                case 'String':
                case 'TEXT':
                    if (Ext.isDefined(dimensionMetadata.maxLength)) {
                        field.maxLength = OE.util.getNumberValue(dimensionMetadata.maxLength, Number.MAX_VALUE);
                    }

                    // Toggle for textarea
                    field.xtype = OE.util.getStringValue(dimensionMetadata.xtype, 'textfield');
                    break;
                case 'Date':
                case 'DATE_TIME':
                case 'DATE':
                    this.startDateFieldId = field.name + '_start';
                    this.endDateFieldId = field.name + '_end';

                    // since we automatically set date fields, we should add them as filters too
                    this.filters[this.startDateFieldId] = dimension.start || this.startDate;
                    this.filters[this.endDateFieldId] = dimension.end || this.endDate;

                    field.xtype = 'fieldset';
                    field.layout = 'column';
                    field.cls = 'report-date-range-fieldset';
                    field.border = false;
                    field.width = 200;
                    field.defaults = {
                        columnWidth: 0.5,
                        xtype: 'datefield',
                        vtype: 'daterange',
                        allowBlank: false,
                        hideLabel: true,
                        format: OE.util.getStringValue(dimensionMetadata.format, OE.util.defaultDateFormat)
                    };
                    field.items = [
                        {
                            name: this.startDateFieldId,
                            itemId: this.startDateFieldId,
                            dependency: this.endDateFieldId,
                            value: this.filters[this.startDateFieldId]
                        },
                        {
                            name: this.endDateFieldId,
                            itemId: this.endDateFieldId,
                            dependency: this.startDateFieldId,
                            value: this.filters[this.endDateFieldId]
                        }
                    ];
                    break;
                default:
                    field.xtype = 'textfield';
                    break;
            }
        }

        // set filter value if it comes with one (e.g. through a saved query)
        if (Ext.isDefined(dimension.value) && !Ext.isDefined(field.value)) {
            field.value = dimension.value;
        }

        return field;
    }
});

// TODO:...
Ext.apply(Ext.form.VTypes, {
    url: function (v) {
        return (/testingurl/).test(v);
    },
    urlText: 'enter valid url',
    urlMask: /testingmask/ });
