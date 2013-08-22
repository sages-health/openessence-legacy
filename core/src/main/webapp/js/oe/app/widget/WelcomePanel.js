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

Ext.namespace("OE");

OE.WelcomePanel = Ext.extend(Ext.Panel, {
    constructor: function (configuration) {

        // Create the welcome panel, which gives textual descriptions of the
        // items in the navigation panel. Since the navigation panel is user
        // specific, this page will be too.
        Ext.applyIf(configuration, {
            title: configuration.title,
            closable: true,
            border: false,
            autoScroll: true
        });

        OE.WelcomePanel.superclass.constructor.call(this, configuration);
    },

    initComponent: function () {
        OE.WelcomePanel.superclass.initComponent.call(this);

        var me = this;
        var navTree = OE.NavPanel.instance.root;

        // Walk the navigation tree, adding welcome items to the tab panel
        navTree.cascade(function (node) {
            // Don't add yourself to the welcome tab because that doesn't do anything
            if (node.leaf && node.attributes.src != OE.main.welcome.tab.init) {
                me.add(new OE.WelcomeItem({node: node}));
            }
        });
    }
});
