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

Ext.namespace("OE.main.content.tab");

OE.main.content.tab.init = function (configuration) {
    var contentTab = {
        itemId: configuration.itemId,
        title: configuration.title,
        cls: 'main-tab',
        layout: 'anchor',
        closable: true,
        border: false,
        autoScroll: true,
        items: [
            {
                border: false,
                autoLoad: {
                    url: OE.util.getUrl(configuration.attributes.url),
                    scripts: true,
                    callback: function (el, success, response, options) {
                        /* need to hide the tab because otherwise it will show the error
                         * from the server while we wait for the user to re-login */
                        OE.main.tabsPanel.getActiveTab().hide();
                        OE.main.content.tab.callback(el, success, response, options);
                    }
                }
            }
        ]
    };

    Ext.getCmp(configuration.destPanel).add(contentTab).show();
    Ext.getCmp(configuration.destPanel).doLayout();
};

OE.main.content.tab.callback = function (el, success, response) {
    if (!success) {
        OE.data.defaultResponseFailure({});
    } else {
        var contentType = response.getResponseHeader('Content-Type');
        if (/^application\/json(;.*)*$/.test(contentType)) {
            var json = Ext.decode(response.responseText);
            if (Ext.isDefined(json.success) && !json.success) {
                if (Ext.isDefined(json.isLoggedIn) && !json.isLoggedIn) {
                    // server responded with {success: false, isLoggedIn: false}

                    /* TODO have some kind of error page rendered in the tab instead
                     * of a blank screen when the user hits cancel on the re-login window
                     */

                    OE.data.defaultUnsuccessfulNotLoggedIn({
                        onRelogin: {
                            callback: function (scope) {
                                var updater = scope.getUpdater();
                                updater.update(scope.items.items[0].autoLoad);
                            },
                            args: [OE.main.tabsPanel.getActiveTab()]
                        }

                    });
                } else {
                    // {success: false} but you are logged in
                    OE.data.defaultResponseFailure();
                }
            }
        } else {
            // html response from server, so just render it
            OE.main.tabsPanel.getActiveTab().show();
        }
    }
};
