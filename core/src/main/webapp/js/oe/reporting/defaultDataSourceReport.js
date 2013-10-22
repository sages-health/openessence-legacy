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

Ext.namespace("OE.report.datasource");

/**
 * Default data source report function.
 *
 * Requires configuration (title, destination panel id, oe data source name and item(localized) id).
 */
OE.report.datasource.main = function (configuration) {

    OE.data.doAjaxRestricted({
        url: OE.util.getUrl('/ds/' + configuration.oeds),
        method: 'GET',
        scope: this,
        onJsonSuccess: function (response) {
            OE.report.datasource.panel({
                itemId: configuration.itemId,
                title: configuration.title,
                destination: configuration.destPanel,
                dataSource: configuration.oeds,
                data: response,

                // TODO use a real IoC solution, e.g. Angular
                detailsTabClass: configuration.detailsTabClass || 'DetailsPanel', // loaded through requirejs
                graphTabClass: configuration.graphTabClass || OE.GraphPanel,
                mapTabClass: configuration.mapTabClass || OE.MapTab
            });

            // add all displayNames to dimensionsBundle
            Ext.each(response.detailDimensions, function (dim) {
                if (Ext.isDefined(dim.displayName)) {
                    if (!dimensionsBundle[dim.name]) { // don't overwrite if there's already a name
                        dimensionsBundle[dim.name] = dim.displayName;
                    }
                }
            });

        },
        onRelogin: {callback: OE.report.datasource.main, args: [configuration]}
    });
};
