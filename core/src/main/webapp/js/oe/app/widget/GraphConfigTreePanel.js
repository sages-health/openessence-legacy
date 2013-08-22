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

OE.GraphConfigTreePanel = Ext.extend(Ext.tree.TreePanel, {
    refresh: function () {
        var expandedPaths = this.getExpandedPaths();
        this.root.reload();
        this.applyExpandedPaths(expandedPaths);
    },
    getExpandedPaths: function () {
        var expandedPaths = [];

        this.root.cascade(function (n) {
            if (n.expanded) {
                expandedPaths.push(n.getPath());
            }
        }, this);

        return expandedPaths;
    },
    applyExpandedPaths: function (expandedPaths) {
        if (expandedPaths && expandedPaths.length > 0) {
            for (var i = 0; i < expandedPaths.length; i++) {
                this.expandPath(expandedPaths[i]);
            }
        }
    },
    resetCheckedValues: function () {
        this.root.cascade(function (n) {
            if (n.attributes.leaf) {
                n.attributes.checked = n.attributes.value === "1";

                this.propagateCheckedValues(n, n.attributes.checked);
            }
        }, this);

        this.refresh();
    },

    reset: this.resetCheckedValues,

    acceptCheckedValues: function () {
        this.root.cascade(function (n) {
            if (n.attributes.leaf) {
                if (n.attributes.checked) {
                    n.attributes.value = "1";
                } else {
                    n.attributes.value = "0";
                }
            }
        }, this);
    },
    getCheckedValues: function () {
        var s = "";

        this.root.cascade(function (n) {
            if (n.attributes.leaf) {
                if (n.attributes.checked) {
                    s += n.attributes.value;
                } else {
                    s += n.attributes.value;
                }
            }
        }, this);

        return s;
    },
    propagateCheckedValues: function (n) {
        // check parent
        var p = n.parentNode;

        if (p) {
            var unchecked = 0;

            p.eachChild(function (n) {
                if (!n.attributes.checked) {
                    unchecked++;
                }
            });

            p.attributes.checked = unchecked === 0;
        }
        // check children
        n.eachChild(function (c) {
            if (c) {
                c.attributes.checked = n.attributes.checked;
            }
        });
    },
    listeners: {
        checkchange: function (n, checked) {
            this.propagateCheckedValues(n, checked);
            this.refresh();
        }
    }
});
Ext.reg('graphConfigTreePanel', OE.GraphConfigTreePanel);
