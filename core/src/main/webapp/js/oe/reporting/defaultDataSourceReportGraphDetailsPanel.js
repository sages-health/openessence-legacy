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

Ext.namespace("OE.report.datasource.graph.details");

/**
 * Default data source report function to display details for graphs/charts.
 */
OE.report.datasource.graph.details.init = function (configuration) {

    function renderLevel(v, p, r) {
        return String.format('<div class="graph-details-detection-{0}">{1}</div>', r.data.Color, v);
    }

    var detailsGridStore = new Ext.ux.data.PagingJsonStore({
        totalProperty: 'detailsTotalRows',
        root: "details",
        lastOptions: {params: {start: 0, limit: Ext.num(dimensionsBundle['page.size'], 50)}},
        fields: [
            'Date',
            'Series',
            {name: 'Level', type: 'float'},
            {name: 'Count', type: 'float'},
            {name: 'Expected', type: 'float'},
            {name: 'Switch', sortType: Ext.data.SortTypes.asUCString},
            {name: 'Color'}
        ],
        sortInfo: { field: 'Date', direction: 'DESC' },
        data: configuration || []
    });

    var detailsGridPanel = new Ext.grid.GridPanel({
        region: 'center',
        store: detailsGridStore,
        columns: [
            {id: 'date', header: messagesBundle["graph.details.column.date"], sortable: true, width: 120, dataIndex: 'Date'},
            {id: 'series', header: messagesBundle["graph.details.column.series"], sortable: true, width: 120, dataIndex: 'Series'},
            {id: 'level', header: messagesBundle["graph.details.column.level"], sortable: true, width: 80, dataIndex: 'Level'},
            {id: 'count', header: messagesBundle["graph.details.column.count"], sortable: true, width: 80, dataIndex: 'Count', renderer: renderLevel},
            {id: 'expected', header: messagesBundle["graph.details.column.expected"], sortable: true, width: 80, dataIndex: 'Expected'}
        ],
        sm: new Ext.grid.RowSelectionModel({
            singleSelect: true
        }),
        border: false,
        loadMask: new Ext.LoadMask(Ext.getBody(), {
            msg: messagesBundle['main.loadmask']
        }),
        bbar: new Ext.PagingToolbar({
            store: detailsGridStore,
            displayInfo: true,
            pageSize: Ext.num(dimensionsBundle['page.size'], 50),
            prependButtons: true,
            items: []
        }),
        width: 440
    });

    return new Ext.Panel({
        title: messagesBundle["graph.details.header"],
        itemId: 'graph-details-grid-panel',
        cls: 'reportPanel',
        layout: 'border',
        region: 'south',
        cmargins: '0 0 6 0',
        //Removed until speed issues with the table collapsing are fixed.
//        collapsible: true,
//        plugins: new Ext.ux.collapsedPanelTitlePlugin(),
        split: true,
        flex: 1,
        boxMinHeight: 100,
        height: 200,
        border: false,
        frame: true,
        items: detailsGridPanel
    });
};
