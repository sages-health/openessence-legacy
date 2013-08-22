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

Ext.namespace("OE.data");

OE.data.defaultOnNoResponse = function () {
    Ext.MessageBox.show({
        title: messagesBundle['input.datasource.error'],
        msg: messagesBundle['input.datasource.error.default'],
        buttons: Ext.MessageBox.OK,
        icon: Ext.MessageBox.ERROR
    });
};

OE.data.defaultOnNoJson = function () {
    Ext.MessageBox.show({
        title: messagesBundle['input.datasource.error'],
        msg: messagesBundle['input.datasource.error.default'],
        buttons: Ext.MessageBox.OK,
        icon: Ext.MessageBox.ERROR
    });
};

OE.data.defaultUnsuccessfulNotLoggedIn = function (meta) {
    OE.login.showLoginForm(meta);
};

OE.data.defaultUnsuccessfulRequest = function (json) {
    Ext.MessageBox.show({
        title: messagesBundle['input.datasource.error'],
        msg: (json && json.message ? json.message
            : messagesBundle['input.datasource.error.default']),
        buttons: Ext.MessageBox.OK,
        icon: Ext.MessageBox.ERROR
    });
};

OE.data.defaultResponseFailure = function (request, response) {
    if (response.status === 403) {
        OE.login.showLoginForm(request);
        return;
    }

    var message = null;
    if (request.failureMsg) {
        message = request.failureMsg;
    } else {
        message = messagesBundle['input.datasource.data.error'];
    }

    if (OE.data.responseIsJson(response)) {
        try {
            var json = Ext.decode(response.responseText);
            message = json.message;
        } catch (e) {
            // leave message as is
        }
    }

    Ext.MessageBox.show({
        title: (request.failureTitle ? request.failureTitle
            : messagesBundle['input.datasource.communication.errorMessage']),
        msg: message,
        buttons: Ext.MessageBox.OK,
        icon: Ext.MessageBox.ERROR
    });
};

/**
 * A custom JsonReader that helps with dealing with responses from the server indicating the user is not
 * logged in.
 */
OE.data.RestrictedJsonReader = Ext.extend(Ext.data.JsonReader, {
    constructor: function (meta, recordType) {
        OE.data.RestrictedJsonReader.superclass.constructor.call(this, meta, recordType);
    },

    read: function (response) {
        if (!response) {
            OE.data.defaultOnNoResponse();
        }

        var json = Ext.decode(response.responseText);
        if (!json) {
            OE.data.defaultOnNoJson();
        }

        if (Ext.isDefined(json.success) && Ext.isDefined(json.isLoggedIn) && !json.success && !json.isLoggedIn) {
            OE.data.defaultUnsuccessfulNotLoggedIn(this.meta);
        }

        if (Ext.isDefined(json.success) && !json.success) {
            OE.data.defaultUnsuccessfulRequest(json);
        }

        return OE.data.RestrictedJsonReader.superclass.read.call(this, response);
    }
});

/**
 * A JsonStore to access restricted resources.
 *
 * Use this instead of a regular Ext.data.JsonStore in almost all cases, as this data store
 * correctly handles when the user isn't logged in, among other cases.
 */
OE.data.RestrictedJsonStore = Ext.extend(Ext.data.Store, {
    constructor: function (config) {
        if (!Ext.isDefined(config.onRelogin)) {
            config.store = this;
        }
        var args = Ext.apply({
            reader: new OE.data.RestrictedJsonReader(config)
        }, config);

        OE.data.RestrictedJsonStore.superclass.constructor.call(this, args);
    }
});

/**
 * Run an Ajax request for a restricted resource. Accepts all the config options of Ext.Ajax.request,
 * plus a few more:
 *
 * onJsonSuccess: function(response) - a callback for when the server responds with {success: true}.
 *    response is the decoded json response from the server. This option is mandatory.
 *
 * onUnsuccessfulNotLoggedIn: function() - an optional callback for when the server responds with
 *  {success: false, isLoggedIn: false}. The default behavior in this case is to show a login form to
 *  the user.
 *
 * onRelogin: function() - what to do when the user logs back in.
 *
 * isJson specifies whether the request is for a Json resource. Defaults to true.
 */
OE.data.doAjaxRestricted = function (request, isJson) {

    var origSuccess = request.success;

    request.success = function (response, options) {
        var json = null;

        // server will respond with JSON even if we didn't expect it to
        // when {success: false}, so check for that
        if (OE.data.responseIsJson(response)) {
            try {
                json = Ext.decode(response.responseText);
            } catch (e) {
                json = null;
            }
        }

        if (json && (!Ext.isDefined(json.success) || json.success === true)) {
            request.onJsonSuccess.call(request.scope || window, json);
        } else if (json && json.success === false && json.isLoggedIn === false) {
            // user not logged in
            if (request.onUnsuccessfulNotLoggedIn) {
                request.onUnsuccessfulNotLoggedIn();
            } else {
                OE.login.showLoginForm(request);
            }
            return;
        } else if (!Ext.isDefined(isJson) || isJson) {
            // bad response from server or {success: false} but not isLoggedIn: false
            Ext.MessageBox.show({
                title: messagesBundle['input.datasource.error'],
                msg: (json && json.message ? json.message
                    : messagesBundle['input.datasource.error.default']),
                buttons: Ext.MessageBox.OK,
                icon: Ext.MessageBox.ERROR
            });
        } // just let Ext.Ajax.request handle the response if we didn't expect json and it's not json

        // call the original success callback, used in places like mainPageBody.js
        if (origSuccess) {
            origSuccess.call(request.scope || window, response, options);
        }
    };

    if (!request.failure) {
        /* Ajax request replied with failure
         * NOTE: this is different than a successful reply of {success: false} */
        request.failure = function (response) {
            OE.data.defaultResponseFailure(request, response);
        };
    }
    Ext.Ajax.timeout = 180000;
    return Ext.Ajax.request(request);
};

OE.data.responseIsJson = function (response) {
    var contentType = response.getResponseHeader('Content-Type');
    return (/^application\/json(;.*)*$/).test(contentType);
};
