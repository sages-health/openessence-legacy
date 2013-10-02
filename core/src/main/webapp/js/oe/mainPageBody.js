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

Ext.namespace("OE.login", "OE.forms", "OE.main", "OE.collective", "OE.input", "OE.report");

OE.main.tabsPanelId = 'tabsPanel';
OE.main.tabsPanel = OE.MainTabPanel.instance = new OE.MainTabPanel({});

// TODO move this into separate file
Ext.ns('OE.login');

OE.login.homePage = '/oe/home/main';
OE.login.ANONYMOUS = 'unknown';

OE.login.loginForm = function (meta) {
    meta = meta || {};

    var pnl = null;
    pnl = new Ext.FormPanel(
        {
            oeMetaData: meta,
            labelWidth: 70,
            //frame      : true,
            bodyStyle: 'padding : 20px',
            autoHeight: true,
            title: meta.title ? meta.title : messagesBundle["login.sessiontitle"],
            items: [
                {
                    xtype: 'textfield',
                    fieldLabel: messagesBundle["login.username"],
                    name: 'username',
                    allowBlank: false,
                    value: OE.lastUser ? OE.lastUser : ""
                },
                {
                    xtype: 'textfield',
                    fieldLabel: messagesBundle["login.password"],
                    name: 'password',
                    inputType: 'password',
                    allowBlank: false
                }
            ],
            submitFunc: function (/*e*/) {
                var locForm = pnl.getForm();
                var locWin = pnl.findParentByType("window");
                var me = this;
                if (locForm.isValid()) {
                    Ext.Ajax.request({
                        url: OE.contextPath + '/login',
                        method: 'POST',
                        params: {
                            username: pnl.form.getEl().dom.username.value,
                            password: pnl.form.getEl().dom.password.value
                        },
                        callback: function (options, success, response) {
                            locWin.close();
                            OE.login.resultHandler(me.oeMetaData, success, response);
                        }
                    });
                }
            },
            buttons: [
                {
                    text: 'Submit',
                    type: 'submit',
                    handler: function (e) {
                        pnl.getForm().submitFunc(e);
                    }
                },
                {
                    text: 'Reset',
                    type: 'reset',
                    handler: function (/*e*/) {
                        pnl.getForm().reset();
                    }
                },
                {
                    text: 'Cancel',
                    type: 'button',
                    handler: function (/*e*/) {
                        pnl.findParentByType("window").close();
                    }
                }
            ], keys: [
            {
                key: [10, 13],
                scope: this,
                handler: function (k, e) {
                    pnl.getForm().submitFunc(e);
                }
            },
            {
                key: [27],
                scope: this,
                handler: function () {
                    pnl.findParentByType("window").close();
                }
            }
        ]
        }
    );

    // convert onRelogin to an array, since multiple login windows get converted to
    // one login window with multiple onRelogins
    var onRelogin = pnl.getForm().oeMetaData.onRelogin;
    if (Ext.isDefined(onRelogin) && !Ext.isArray(onRelogin)) {
        pnl.getForm().oeMetaData.onRelogin = [onRelogin];
    }

    return pnl;
};

OE.login.resultHandler = function (options, success, response) {

    var showLoginForm = function () {
        OE.login.showLoginForm({
            onRelogin: options.onRelogin,
            store: options.store,
            onJsonSuccess: options.onJsonSuccess,
            title: messagesBundle['login.badLogin']
        });
    };

    // unauthorized/forbidden
    if (response.status === 401 || response.status === 403) {
        showLoginForm();
        return;
    }

    Ext.Ajax.request({
        url: OE.util.getUrl('/login/status'),
        headers: {
            'Accept': 'application/json'
        },
        success: function (response) {
            var data = Ext.decode(response.responseText);
            OE.username = data.name;

            if (OE.lastUser != OE.username) {
                // new user logged in
                window.location = OE.contextPath + OE.login.homePage;
                return;
            }

            OE.lastUser = OE.username;

            if (options.onRelogin) {
                // call each onRelogin callback
                Ext.each(options.onRelogin, function (item) {
                    item.callback.apply(item.scope || this, item.args);
                });

            } else if (options.store) {
                // the requested resource was for a data store, so reload it
                options.store.reload();
            }
        },
        failure: function () {
            // server error, not much to do but try again
            showLoginForm();
        }
    });
};

OE.login.showLoginForm = function (config) {
    if (!OE.login.loginWindow || OE.login.loginWindow.hidden) {
        var pnl = OE.login.loginForm(config);
        var win = new Ext.Window({
            layout: 'fit',
            closable: false,
            resizable: false,
            plain: true,
            border: false,
            modal: true,
            width: 300,
            items: [pnl]
        });
        OE.login.loginWindow = win;
        win.show();
        pnl.find("name", "password")[0].focus();
    } else {
        // there is already an active login window, so just add the new login window's onRelogin
        // to the existing one
        var loginForm = OE.login.loginWindow.items.items[0].getForm();
        loginForm.oeMetaData.onRelogin.push(config.onRelogin);
    }
};

Ext.onReady(function () {

    Ext.QuickTips.init();

    // fix issue on old IEs where inline data URL can't be used for images
    // see http://loianegroner.com/2010/05/extjs-stop-the-page-contains-secure-and-nonsecure-items-warning/
    if (Ext.isIE6 || Ext.isIE7) {
        // setting this on other browsers makes them not use inline data URL, so only set for old IE
        Ext.BLANK_IMAGE_URL = '../../js/ext-' + Ext.version + '/resources/images/default/s.gif';
    }

    // turn on validation errors beside the field globally
    Ext.form.Field.prototype.msgTarget = 'side';

    OE.data.doAjaxRestricted({
        url: OE.util.getUrl('/home/getNavigationMenu'),
        onJsonSuccess: function (response) {
            var nodes = [];
            Ext.iterate(response, function (key, value) {
                if (value.children && value.children.length > 0) {
                    nodes.push(OE.NavPanel.formatNode(value));
                }
            });

            OE.NavPanel.instance = new OE.NavPanel({
                root: new Ext.tree.AsyncTreeNode({
                    expanded: true,
                    children: nodes
                })
            });

            OE.viewport = new Ext.Viewport({
                layout: 'border',
                id: 'mainViewPort',
                border: false,
                items: [ OE.NavPanel.instance, {
                    layout: 'border',
                    region: 'center',
                    border: false,
                    items: [new OE.LoggedInHeader({
                        region: 'north'
                    }), OE.main.tabsPanel]
                } ]
            });
            OE.viewport.doLayout();
        },
        onRelogin: {callback: this, args: []}
    }, false);
});
