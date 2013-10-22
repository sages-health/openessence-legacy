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

// this file is included in every page

// global variables TODO globals are obviously bad, replace with module(s)
(function ($) {
    window.OE = window.OE || {};

    window.OE.contextPath = $("meta[name='_context_path']").attr('content');
    window.OE.servletPath = $("meta[name='_servlet_path']").attr('content');
    window.OE.username = $("meta[name='_username']").attr('content');
    window.OE.lastUser = window.OE.username;
})(jQuery); // we use jQuery way too much to load it through require

requirejs.config({
    baseUrl: OE.contextPath + '/js',
    enforceDefine: true,
    shim: {
        jqueryui: {
            exports: '$'
        },
        filedownload: {
            exports: '$'
        },
        pivottable: {
            exports: '$'
        }
    },
    paths: {
        // libs
        jqueryui: 'lib/jquery-ui/js/jquery-ui-1.10.3.custom.min',
        filedownload: 'lib/filedownload/filedownload.min',
        moment: 'lib/moment/moment.min',
        pivottable: 'lib/pivottable/pivot.min',
        Q: 'lib/q/q.min', // TODO use jQuery promises instead

        // our stuff
        CsvUploadWindow: 'oe/app/widget/CsvUploadWindow', // TODO redo layout according to requirejs conventions
        DetailsPanel: 'oe/app/widget/DetailsPanel'
    }
});


// always send CSRF token with Ajax requests
require(['jquery'], function ($) {
    var header = $("meta[name='_csrf_header']").attr('content');

    // this also works for Ext since we use the jQuery adapter
    $(document).ajaxSend(function (e, xhr) {
        // don't cache token outside closure! it can be updated
        var token = $("meta[name='_csrf']").attr('content');
        xhr.setRequestHeader(header, token);
    });

    // update CSRF token if server responds with one
    $(document).ajaxComplete(function (e, xhr) {
        var token = xhr.getResponseHeader(header);
        if (token) {
            $("meta[name='_csrf']").attr('content', token);
        }
    });
});

// Ext configuration
(function (Ext) {
    Ext.USE_NATIVE_JSON = true;
    // don't rely on modifying builtin objects, Ext 4 wised up and stopped that
    if (!Ext.String) {
        Ext.String = String;
    }
})(Ext);
