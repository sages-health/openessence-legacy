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

Ext.namespace('OE.login');

Ext.onReady(function () {
    if (Ext.isIE6 || Ext.isIE7) {
        Ext.BLANK_IMAGE_URL = '../../js/ext-' + Ext.version + '/resources/images/default/s.gif';
    }

    OE.login.viewport = new Ext.Viewport({
        id: 'loginViewport',
        layout: 'fit',
        autoScroll: true,
        items: [
            {
                cls: 'login-header',
                border: false,
                frame: false,
                bodyStyle: {
                    background: 'transparent'
                },
                width: '100%',
                html: new Ext.XTemplate('<span class="oe-header oe-title">{title}</span>').apply({title: messagesBundle['app.title']})
            },
            {
                cls: 'login-mainLogin',
                border: false,
                frame: false,
                bodyStyle: {
                    background: 'transparent'
                },
                width: '100%',
                items: [
                    {
                        cls: 'login-mainLogin-logo',
                        border: false,
                        html: messagesBundle['app.logo']
                    },
                    {
                        xtype: 'panel',
                        id: 'mainLogin',
                        headerCssClass: 'loginPanelHeader',
                        bodyCssClass: 'loginPanelBody',
                        extraCls: 'center',
                        title: (function () {
                            var params = Ext.urlDecode(location.search.substring(1));

                            if (params && params.login_error) {
                                return messagesBundle['login.badLogin'];
                            } else {
                                return messagesBundle['login.header'];
                            }
                        })(),
                        //width: '25%',
                        border: true,
                        items: [
                            {
                                xtype: 'form',
                                id: 'loginForm',
                                url: OE.context.root + '/j_spring_security_check',
                                padding: 10,
                                border: false,
                                frame: false,

                                // call this instead of getForm().submit()
                                customSubmit: function () {
                                    Ext.getCmp('loginForm').getForm().submit({
                                        success: function (form, action) {
                                            // navigate to main page
                                            window.location = OE.util.getUrl('/home/main');
                                        },
                                        // we patched Ext.Action.Submit to get this to work without having to drink the Ext kool-aid
                                        failure: function (form, action) {
                                            // back to login page
                                            window.location = OE.util.getUrl('/login/login?login_error=true');
                                        }
                                    });
                                },

                                keys: [
                                    {
                                        key: Ext.EventObject.ENTER,
                                        handler: function () {
                                            Ext.getCmp('loginForm').customSubmit();
                                        }
                                    }
                                ],
                                defaults: {
                                    width: 200
                                },
                                items: [
                                    {
                                        xtype: 'textfield',
                                        name: 'j_username',
                                        fieldLabel: messagesBundle['login.username'],
                                        cls: 'loginField'
                                    },
                                    {
                                        xtype: 'textfield',
                                        name: 'j_password',
                                        fieldLabel: messagesBundle['login.password'],
                                        inputType: 'password',
                                        cls: 'loginField'
                                    },
                                    {
                                        xtype: 'combo',
                                        width: 198, //fixes weird IE combo drop on smaller page
                                        id: 'locale_selector',
                                        fieldLabel: messagesBundle['login.locale'],
                                        mode: 'local',
                                        typeAhead: true,
                                        forceSelection: true,
                                        selectOnFocus: true,
                                        hiddenName: 'locale',
                                        triggerAction: 'all',
                                        displayField: 'displayLanguage',
                                        valueField: 'code',
                                        store: new Ext.data.JsonStore({
                                            url: OE.util.getUrl('/locale'),
                                            root: 'locales',
                                            idProperty: 'code',
                                            fields: ['code', 'displayLanguage'],
                                            autoLoad: true,
                                            listeners: {
                                                load: function (store, records) {
                                                    // SELECTED_LOCALE is a JS variable written to page by globalResources.jsp
                                                    Ext.getCmp('locale_selector').setValue(SELECTED_LOCALE);
                                                }
                                            }
                                        }),
                                        listeners: {
                                            select: function (combo, record) {
                                                document.location = '?locale=' + record.get('code');
                                            }
                                        }
                                    },
                                    {
                                        xtype: 'button',
                                        text: messagesBundle['button.login'],
                                        width: 100,
                                        style: {
                                            margin: 'auto',
                                            'padding-top': '10px'
                                        },
                                        handler: function (button) {
                                            button.ownerCt.customSubmit();
                                        }
                                    }
                                ]
                            }
                        ]
                    }
                ]
            },
            {
                id: 'mainLogos',
                html: messagesBundle['login.images'],
                border: false,
                frame: false,
                bodyStyle: {
                    background: 'transparent',
                    'text-align': 'center'
                }
            },
            {
                id: 'mainInfo',
                cls: 'loginInfoPanel',
                colspan: 3,
                border: false,
                frame: false,
                bodyStyle: {
                    background: 'transparent'
                },
                defaults: {
                    border: false,
                    frame: false,
                    bodyStyle: {
                        background: 'transparent'
                    }
                },
                items: [
                    {
                        cls: 'legalText',
                        html: messagesBundle['legal.license']
                    }
                ]
            }
        ]
    });

    Ext.get(Ext.DomQuery.select("[name='j_username']")).focus();
});
