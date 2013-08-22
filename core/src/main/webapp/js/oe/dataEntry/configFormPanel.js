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

Ext.namespace("OE.input.user.account");

/**
 * Custom panel for user accounts to update password/user information
 * This panel can be used as a property edit panel as well. e.g. Map settings
 * Requires configuration (consisting of data, data source name, primary keys and a title).
 */
OE.input.user.account.panel = function (configuration) {

    /**
     * The form panel
     */
    var formPanel;

    OE.data.doAjaxRestricted({
        url: '../../oe/report/getFields',
        method: 'POST',
        params: {dsId: configuration.oeds},
        scope: this,
        onJsonSuccess: function (response) {
            formPanel = OE.input.datasource.form.init({
                data: response,
                itemId: messagesBundle['main.navigation.menu.administration.' + configuration.oeds],
                dataSource: configuration.oeds,
                showMessageOnUpdate: true
            });
            loadUserData(configuration);

            var inputTab = {
                itemId: configuration.itemId,
                title: configuration.title,
                layout: 'anchor',
                cls: 'reportPanel',
                closable: true,
                border: false,
                items: [formPanel]
            };

            Ext.getCmp(configuration.destPanel).add(inputTab).show();
            Ext.getCmp(configuration.destPanel).doLayout();
        },
        onRelogin: {callback: OE.input.user.account.panel, args: [configuration]}
    });

    // Build request parameters (data source and primary keys)
    function loadUserData(config) {
        OE.data.doAjaxRestricted({
            url: '../../oe/input/data',
            params: {dsId: configuration.oeds, format: 'json', doNotParseKeys: true},
            scope: this,
            onJsonSuccess: function (response) {
                if (formPanel) {
                    formPanel.getForm().setValues(response, true);
                }
            },
            onRelogin: {callback: loadUserData, args: [config]}
        });
    }
};
