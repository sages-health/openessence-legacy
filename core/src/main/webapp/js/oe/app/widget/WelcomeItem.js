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

OE.WelcomeItem = Ext.extend(Ext.Panel, {
    constructor: function (configuration) {
        Ext.applyIf(configuration, {
            border: false,
            bodyCfg: {
                cls: 'navigation-item'
            }
        });
        OE.WelcomeItem.superclass.constructor.call(this, configuration);
    },

    initComponent: function () {
        OE.WelcomeItem.superclass.initComponent.call(this);

        var me = this;

        // Delegate the icon up to the parent node if we don't have one
        // If all else fails, get a default icon from the message bundle
        var icon = Ext.isDefined(me.node.attributes.big_icon) ? me.node.attributes.big_icon :
                   (Ext.isDefined(me.node.parentNode.attributes.big_icon) ? me.node.parentNode.attributes.big_icon :
                    messagesBundle['main.navigation.panel.default_icon']); // TODO fix this formatting

        this.add(
            new Ext.Panel({
                layout: {
                    type: 'hbox',
                    align: 'middle'
                },
                padding: 5,
                border: false,
                items: [
                    new Ext.Panel({
                        border: false,
                        layout: {
                            type: 'vbox',
                            pack: 'center',
                            align: 'center'
                        },
                        width: 48,  // This has to be the maximum width / height of the icon so that it
                        height: 48, // does layout correctly.
                        items: [
                            new Ext.BoxComponent({
                                autoEl: {
                                    tag: 'img',
                                    src: icon
                                }
                            })
                        ]
                    }),
                    new Ext.Panel({
                        border: false,
                        padding: 5,
                        items: [
                            {
                                html: new Ext.XTemplate('<a href="javascript: void(0);">{text}</a>').applyTemplate({text: me.node.text}),
                                border: false,
                                listeners: {
                                    afterrender: function (link) {
                                        new Ext.Element(link.el.query('a')[0]).on('click', function () {
                                            OE.NavPanel.instance.openTab(me.node);
                                        });
                                    }
                                }
                            },
                            {
                                xtype: 'panel',
                                border: false,
                                html: new Ext.XTemplate('<p>{tip}</p>').applyTemplate({
                                    tip: me.node.attributes.qtip
                                })
                            }
                        ]
                    })
                ]
            })
        );
    }
});
