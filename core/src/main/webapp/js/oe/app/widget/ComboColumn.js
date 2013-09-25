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

Ext.namespace("Ext.ux.grid", "Ext.ux.renderer");

Ext.ux.grid.ComboColumn = Ext.extend(Ext.grid.Column, {

    /**
     * The grids id
     */
    gridId: undefined,

    constructor: function (cfg) {
        Ext.ux.grid.ComboColumn.superclass.constructor.call(this, cfg);

        // Check for editor using custom renderer otherwise default
        this.renderer = (this.editor && this.editor.triggerAction) ? Ext.ux.renderer.ComboBoxRenderer(this.editor,
            this.gridId) : function (value) {
            return value; // TODO fix this formatting
        };
    }
});
Ext.grid.Column.types.combocolumn = Ext.ux.grid.ComboColumn;

/**
 *  A renderer to show the display value when using combo boxes in editor grids (instead of value/fk)
 */
Ext.ux.renderer.ComboBoxRenderer = function (combo, gridId) {
    // Get the value from displayfield, else value
    var getValue = function (value) {
        var record = combo.store.getAt(combo.store.findExact(combo.valueField, value));
        if (record) {
            return record.get(combo.displayField);
        }

        return value;
    };

    return function (value) {
        // Ensure combos store is loaded
        if (combo.store.getCount() === 0 && gridId) {
            if (!combo.onloadDefined) {
                // Ensure we are adding onLoad only once per combo column
                // This check was added because onLoad was added to the store 2^n times
                // where n is number of combo columns in this grid
                combo.onloadDefined = true;

                combo.store.on('load', function () {
                        var grid = Ext.getCmp(gridId);
                        if (grid) {
                            grid.getView().refresh();
                        }
                    }, {
                        single: true
                    }
                );
            }

            return value;
        }

        return getValue(value);
    };
};
