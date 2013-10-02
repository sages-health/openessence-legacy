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

OE.Header = Ext.extend(Ext.Panel, {
    constructor: function (config) {
        Ext.apply(this, config);

        config = Ext.apply({
            cls: 'openessence-header',
            border: false,
            frame: false,
            bodyStyle: {
                background: 'transparent'
            },
            width: '100%',
            defaults: {
                border: false,
                frame: false,
                bodyStyle: {
                    background: 'transparent'
                }
            },
            items: [
                {
                    listeners: {
                        afterlayout: function () {
                            // position username label relative to logout button
                            var logoutButton = new Ext.Element(Ext.DomQuery.selectNode('#logoutBtn'));
                            var username = new Ext.Element(Ext.DomQuery.selectNode('#header-username'));
                            username.setLeft(logoutButton.getX() - username.getWidth());
                        }
                    },
                    html: new Ext.XTemplate(
//						'<span class="header-images">{images}</span>',
                        '<span class="oe-header oe-title">{title}</span>',
                        '<span id="header-username" class="oe-header">{username}</span>'
                    ).apply({
                            images: messagesBundle['app.header.images'],
                            title: messagesBundle['app.title'],
                            username: this.username
                        })
                },
                {
                    xtype: 'button',
                    id: 'logoutBtn',
                    text: messagesBundle['button.logout'],
                    hidden: !Ext.isDefined(this.username),
                    handler: function () {
                        $.ajax({
                            type: 'POST',
                            url: OE.contextPath +  '/logout',
                            success: function (data, textStatus, xhr) {
                                window.location = OE.util.getUrl('/home/main');
                            }
                        });
                    }
                }
            ]
        }, config);

        OE.Header.superclass.constructor.call(this, config);
    }
});
