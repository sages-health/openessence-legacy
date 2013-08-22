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

OE.NavPanel = Ext.extend(Ext.tree.TreePanel, {
    constructor: function (config) {
        var me = this;

        config = Ext.apply({
            id: 'navPanel',
            region: 'west',
            cls: 'openessence-navigation-menu',
            collapsible: false,
            title: messagesBundle['main.navigation.menu'],
            width: Ext.num(dimensionsBundle['main.navigation.width'], 200),
            autoScroll: true,
            split: true,
            floatable: false,
            lines: false,
            loader: new Ext.tree.TreeLoader(),
            rootVisible: false,
            listeners: {
                click: function (node) {
                    me.openTab(node);
                }
            }
        }, config);

        OE.NavPanel.superclass.constructor.call(this, config);
    },

    initComponent: function () {
        var me = this;

        new Ext.tree.TreeSorter(this, {
            folderSort: true,
            dir: "asc",
            property: "order",
            caseSensitive: true
        });

        // Create default tabs based on node attributes
        this.on('afterrender', function () {
            this.root.cascade(function (node) {
                if (node.leaf && node.attributes.display) {
                    me.openTab(node);
                }
            });
        }, null, {single: true});

        OE.NavPanel.superclass.initComponent.call(this);
    },

    /**
     * Open tab corresponding to specified node in NavPanel.
     *
     * @param node
     *            node in NavPanel
     * @param callback
     *            called with the opened tab
     */
    openTab: function (node, callback) {
        if (!node.attributes.leaf) {
            return;
        }

        if (!callback) {
            callback = function () {
            };
        }

        // Lookup tab by item id (using the navigation menu node id)
        var tab = OE.MainTabPanel.instance.getComponent(node.id);

        // Make Q globally available after the tab is opened
        require([OE.context.root + '/js/lib/q/q.min.js'], function (Q) {
            // This is terrible because it breaks IOC but its needed because our code is bad
            window.Q = Q;

            // If allowing multiples then just add the tab with a null item id,
            // otherwise create the tab with an item id or set it as active.
            if (node.attributes.allowMultiple || !tab) {
                node.attributes.src({
                    title: node.text,
                    destPanel: OE.main.tabsPanelId,
                    oeds: node.attributes.name,
                    itemId: (node.attributes.allowMultiple ? null : node.id),
                    attributes: node.attributes,
                    tabAdded: function (tab) {
                        callback(tab);
                    }
                });
            } else {
                OE.MainTabPanel.instance.setActiveTab(tab);
                callback(tab);
            }
        });
    }
});

/**
 * Page's singleton NavPanel instance. Initialized on document ready in
 * mainPageBody. Callers should have this value injected instead of accessing it
 * directly, unless you're lazy.
 *
 * Ext 4 MVC has a similar view singleton convention, because they probably
 * don't know IoC exists.
 */

/*jshint evil: true */

OE.NavPanel.instance = null;

/**
 * Format node returned from server
 */
OE.NavPanel.formatNode = function (node) {
    var nameKey = 'main.navigation.menu.' + (node.parent ? node.parent + '.' : '') + node.name;
    node.text = messagesBundle[nameKey] || node.name;
    node.qtip = messagesBundle[nameKey + '.tip'];
    node.icon = messagesBundle[nameKey + '.icon'];
    node.big_icon = messagesBundle[nameKey + '.big_icon'];
    try {
        // TODO parse into object manually by splitting on .
        node.src = eval(node.src);
    } catch (e) {
        // usually NPE from custom view that wasn't included
        Ext.MessageBox.show({
            title: messagesBundle['input.datasource.error'],
            // users should never get this technical error message, so don't bother translating
            msg: 'Exception evaling data source view ' + node.src + '. It may not be defined.',
            buttons: Ext.MessageBox.OK,
            icon: Ext.MessageBox.ERROR
        });
    }

    var children = node.children;
    if (children) {
        Ext.each(children, OE.NavPanel.formatNode);
    }

    return node;
};
