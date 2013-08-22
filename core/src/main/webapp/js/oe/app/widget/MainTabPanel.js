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

OE.MainTabPanel = Ext.extend(Ext.TabPanel, {
    constructor: function (config) {
        config = Ext.apply({
            id: OE.main.tabsPanelId,
            region: 'center',
            layoutOnTabChange: true,
            plain: true,
            activeTab: 0,
            enableTabScroll: true,
            items: [ ]
        }, config);

        OE.MainTabPanel.superclass.constructor.call(this, config);
    },

    initComponent: function () {
        this.on('beforetabchange', function (tabPanel, newTab) {
            // Make sure the navigation panel's node is also set to selected
            var navPanelCtrl = OE.NavPanel.instance;
            var newTabId = newTab.itemId;

            if (!newTabId) {
                // do nothing, just unselect and return
                var selectedNode = navPanelCtrl.selModel.selNode;
                if (selectedNode) {
                    selectedNode.unselect();
                }
            } else {
                if (navPanelCtrl.root.attributes.children && navPanelCtrl.root.attributes.children.length > 0) {
                    for (var i = 0; i < navPanelCtrl.root.attributes.children.length; i++) {
                        var node = navPanelCtrl.root.attributes.children[i];
                        if (node) {
                            if (node.leaf && (newTab.itemId == node.id)) {
                                navPanelCtrl.getNodeById(treeItem.id).select();
                            }
                            else if (!node.leaf && node.children && node.children.length > 0) {
                                for (var j = 0; j < node.children.length; j++) {
                                    var leafNode = node.children[j];

                                    if (leafNode.id == newTabId) {
                                        navPanelCtrl.getNodeById(leafNode.id).select();

                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        OE.MainTabPanel.superclass.initComponent.call(this);
    }
});

OE.MainTabPanel.instance = null;
