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

define([], function () {
    var DetailsPanel = Ext.extend(Ext.Panel, {
        constructor: function (configuration) {
            var dataSource = configuration.dataSource;
            var entryDataSource = configuration.dataSource + '_Entry';

            // open edit form on row click
            var onRowClick = function (grid, rowIndex) {
                // TODO see if this works with paging
                var record = grid.getStore().getAt(rowIndex);

                var navNode = OE.NavPanel.instance.root.findChildBy(function (n) {
                    return n.leaf && n.attributes.name === entryDataSource;
                }, this, true);

                if (navNode) { // can't open form if we don't have an entry data source
                    OE.NavPanel.instance.openTab(navNode, function (tab) {
                        try {
                            tab.openFormTab(record, tab.setValuesCallback);
                        } catch (e) {
                            if (e instanceof OE.InputTab.MissingIdentifierError) {
                                // This is expected if the primary key wasn't included in the record.
                                // In this case, the form tab can't be opened for the record since we
                                // don't know what record to open.
                                console.warn("Can't open form tab without identifier");
                            } else {
                                console.error(e);
                            }
                        }
                    });
                } else {
                    console.warn('No data source named ' + entryDataSource + ' defined');
                }
            };

            configuration = Ext.apply({
                layout: 'fit',
                closable: true,
                items: [
                    OE.datasource.grid.init({
                        url: configuration.url,
                        parameters: configuration.parameters,
                        dataSource: dataSource,
                        data: configuration.data || {},
                        allowExport: true,
                        gridClass: configuration.gridClass,
                        gridExtraConfig: (function () {
                            var gridExtraConfig = Ext.apply({
                                cls: 'details-grid'
                            }, configuration.gridExtraConfig);
                            gridExtraConfig.listeners = gridExtraConfig.listeners || {};

                            gridExtraConfig.listeners = Ext.apply({
                                rowclick: onRowClick
                            }, gridExtraConfig.listeners);

                            return gridExtraConfig;

                        })(),
                        pageSize: configuration.pageSize,
                        pivot: configuration.pivot
                    })
                ]
            }, configuration);

            DetailsPanel.superclass.constructor.call(this, configuration);
        }
    });
    return {
        DetailsPanel: DetailsPanel
    };
});

