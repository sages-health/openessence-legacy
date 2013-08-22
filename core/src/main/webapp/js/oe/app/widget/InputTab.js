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

OE.InputTab = Ext.extend(Ext.Panel, {

    hasSupportEdit: null,
    gridPanel: null,

    constructor: function (config) {
        var me = this;

        this.metadata = (config.data.meta && config.data.meta.form ? config.data.meta.form : {});
        me.hasSupportEdit = OE.util.getBooleanValue(this.metadata.supportEdit, true);

        this.editAction = new Ext.Action({
            text: messagesBundle['input.datasource.default.edit'],
            disabled: true,
            handler: function () {
                // Open a new tab for each of the selected records
                var records = me.gridPanel.selectionModel.getSelections();
                Ext.each(records, function (record) {
                    // grid record only has detailsDimensions, so use full record from server
                    me.openFormTab(record, me.setValuesCallback);
                });
            }
        });

        this.deleteAction = new Ext.Action({
            text: messagesBundle['input.datasource.default.delete'],
            disabled: true,
            handler: function () {
                me.deleteReports(me.gridPanel.selectionModel.getSelections());
            }
        });

        config = Ext.apply({
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            cls: 'reportPanel',
            closable: true,
            border: false,
            defaults: {
                split: true
            },
            tbar: [
                {
                    text: messagesBundle[this.dataSource + '.new'] || messagesBundle['input.datasource.default.new'],
                    handler: function () {
                        me.addFormTab(null, null);
                    }
                },
                this.editAction,
                this.deleteAction
            ]
        }, config);

        OE.InputTab.superclass.constructor.call(this, config);
        if (!me.hasSupportEdit) {
            config.tbar.remove(this.editAction);
        }
    },

    initComponent: function () {
        OE.InputTab.superclass.initComponent.call(this);

        var me = this;

        this.dataSource = this.dataSource || this.oeds;
        this.destination = this.destination || this.destPanel;

        this.formTabPanel = new Ext.TabPanel({
            cls: 'reportPanel',
            flex: 1,
            itemId: this.itemId + '-formTabPanel',
            border: false,
            autoScroll: true,
            enableTabScroll: true,
            plain: true
        });

        var displayDatasource = me.data.meta.displayDatasource;
        if (displayDatasource) {
            OE.data.doAjaxRestricted({
                url: OE.util.getUrl('/ds/' + displayDatasource),
                method: 'GET',
                scope: this,
                onJsonSuccess: function (response) {
                    me.gridPanel = me.createGridPanel(displayDatasource, response);
                    me.insert(0, me.gridPanel);
                    me.doLayout();
                }
            });
        } else {
            me.gridPanel = me.createGridPanel();
            me.insert(0, me.gridPanel);
            me.doLayout();
        }

        this.insert(1, this.formTabPanel);
        this.doLayout();
    },

    createGridPanel: function (dataSource, data) {
        var me = this;
        var selectionModel = new Ext.grid.RowSelectionModel();

        dataSource = dataSource || this.dataSource;
        data = data || this.data;

        selectionModel.addListener('selectionchange', function (sm) {
            var records = sm.getSelections();
            var selectionCount = records.length;

            if (selectionCount === 0) {
                me.editAction.disable();
                me.deleteAction.disable();
            } else {
                me.editAction.enable();
                me.deleteAction.enable();
            }
        }, this, {buffer: 5});

        return OE.datasource.grid.init({
            title: messagesBundle[this.dataSource + '.grid'],  //this = use entry grid title
            itemId: me.itemId + '-grid',
            dataSource: this.dataSource,
            collapsible: false,
            height: 230,
            parameters: { dsId: dataSource},
            data: data || {},
            selectionModel: selectionModel,
            rowdblclick: function () {
                me.openFormTab(selectionModel.getSelected(), me.setValuesCallback);
            },
            plugins: null
        });
    },

    /**
     * Convenience callback to pass to openFormTab to set the form's values.
     */
    setValuesCallback: function (formPanel, serverRecord) {
        // grid record only has detailsDimensions, so use full record from server
        formPanel.getForm().setValues(serverRecord);
    },

    getItemIdFromData: function (data) {
        var primaryKeys = this.data.pks;
        var primaryKey = {};
        var itemId = [];

        for (var index = 0; (primaryKeys && index < primaryKeys.length); index++) {
            var key = primaryKeys[index];
            primaryKey[key] = data[key];
            itemId.push(data[key]);
        }

        return {
            key: primaryKey,
            itemId: itemId.join(':')
        };
    },

    getFullRecord: function (identifiers, callback) {
        var me = this;

        OE.data.doAjaxRestricted({
            url: OE.util.getUrl('/input/data'),
            method: 'GET',
            params: Ext.apply({dsId: this.dataSource}, identifiers.key),
            onJsonSuccess: function (response) {
                callback(response);
            },
            onRelogin: {callback: me.getFullRecord, args: [identifiers, callback]}
        });
    },

    /**
     * Opens an edit form tab corresponding to given record. Creates the tab if
     * it doesn't already exist. Note that the given record is NOT used to populate
     * the form. That's left up to the caller. See setValuesCallback.
     *
     * Don't use this method if you don't have a unique, extant record. If it
     * has to create a new tab, it will query the server for linked entities.
     * This will fail if the record is not already fully in the DB. Use
     * addFormTab in this case (especially since it makes no sense to check for
     * an already open tab with no way to uniquely identify the tab in
     * question).
     *
     * @param record
     *            the record identifying the tab to open
     * @param callback
     *            called with tab that was opened and record used to populate
     *            form
     */
    openFormTab: function (record, callback) {
        var me = this;

        callback = callback || function () {
        };

        // Forms are added with itemId set to appended primary keys, else null
        var identifiers = this.getItemIdFromData(record.data);

        var tab = this.formTabPanel.getComponent(identifiers.itemId);
        if (tab) {
            this.formTabPanel.setActiveTab(tab);
            callback(tab);
        } else {
            this.getFullRecord(identifiers, function (record) {
                var tab = me.addFormTab({
                    record: record,
                    itemId: identifiers.itemId
                });

                // select record in grid

                // be careful, gridPanel isn't actually a GridPanel
                var grid = me.gridPanel.getExtGridPanel();
                if (grid) {
                    var store = grid.getStore();
                    try {
                        var index = store.findBy(function (storeRec) {
                            var equal = true;
                            Ext.iterate(identifiers.key, function (key, value) {
                                if (storeRec.get(key) !== value) {
                                    equal = false;
                                    return false;
                                }
                            });
                            return equal;
                        });

                        me.gridPanel.selectionModel.selectRecords([store.getAt(index)]);
                    } catch (e) {
                        // oh well, don't select record
                        console.error(e);
                    }
                }

                callback(tab, record);
            });
        }
    },

    /**
     * Add a new input form tab. Callers may want to use openFormTab instead, as
     * that first checks that the tab isn't already created.
     */
    addFormTab: function (formPanelConfig) {
        var me = this;

        var formPanel = OE.input.datasource.form.init(Ext.apply({
            data: this.data,
            dataSource: this.dataSource,
            callback: function (tab) {
                me.gridPanel.reload();
                me.formTabPanel.remove(tab);
            }
        }, formPanelConfig));

        this.formTabPanel.add(formPanel).show();
        return formPanel;
    },

    deleteReports: function (records) {
        var me = this;

        if (records && records.length) {
            Ext.Msg.confirm(messagesBundle['input.datasource.default.deleteTitle'], messagesBundle['input.datasource.default.deleteMessage'], function (btn) {
                if (btn == 'yes') {
                    var pkIds = [];

                    for (var index = 0; index < records.length; index++) {
                        var identifiers = me.getItemIdFromData(records[index].data);

                        // Attempts to remove tab via itemIds
                        var tab = me.formTabPanel.getComponent(identifiers.itemId);
                        if (tab) {
                            me.formTabPanel.remove(tab);
                        }

                        pkIds.push(identifiers.key);
                    }

                    OE.data.doAjaxRestricted({
                        url: OE.util.getUrl('/input/delete'),
                        params: {dsId: me.dataSource},
                        jsonData: {pkIds: pkIds},
                        onJsonSuccess: function () {
                            me.gridPanel.reload();
                        },
                        onRelogin: {callback: me.deleteReports, args: [records]}
                    });
                }
            });
        }
    }
});
