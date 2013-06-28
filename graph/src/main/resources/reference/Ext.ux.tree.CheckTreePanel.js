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

Ext.ns("Ext.ux.tree");

Ext.ux.tree.CheckTreePanel = Ext.extend(Ext.tree.TreePanel, {
    // class to add to CheckTreePanel
    cls: "ux-checktree",

    baseAttrs: {},

    initComponent: function () {
        // use our event model
        this.eventModel = new Ext.ux.tree.CheckTreeEventModel(this);
        // call parent initComponent
        Ext.ux.tree.CheckTreePanel.superclass.initComponent.apply(this, arguments);
        // pass this.baseAttrs and uiProvider down the line
        var baseAttrs = Ext.apply({uiProvider: Ext.ux.tree.CheckTreeNodeUI}, this.baseAttrs);
        Ext.applyIf(this.loader, {baseAttrs: baseAttrs, preloadChildren: true});

        // make sure that nodes are deeply preloaded
        if (true === this.loader.preloadChildren) {
            this.loader.on("load", function (loader, node) {
                node.cascade(function (n) {
                    loader.doPreload(n);
                    n.loaded = true;
                });
            });
        }
    },

    /*
     * reset all check boxes to last accepted state
     */
    resetValue: function () {
        this.root.cascade(function (n) {
            if (n.attributes.leaf) {
                var ui = n.getUI();

                if (ui) {
                    if (n.attributes.value == "1") {
                        ui.setChecked(true);
                    } else {
                        ui.setChecked(false);
                    }
                }
            }
        }, this);
    },

    acceptValue: function () {
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

    getValue: function () {
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

    clearInvalid: Ext.emptyFn,
    markInvalid: Ext.emptyFn,

    validate: function () {
        return true;
    },

    isValid: function () {
        return true;
    },

    getName: function () {
        return this.name || this.id || "";
    },

    getRoot: function () {
        return this.root;
    }
})

Ext.reg("checktreepanel", Ext.ux.tree.CheckTreePanel);

Ext.ux.tree.CheckTreeNodeUI = Ext.extend(Ext.tree.TreeNodeUI, {
    renderElements: function (n, a, targetNode, bulkRender) {
        this.indentMarkup = n.parentNode ? n.parentNode.ui.getChildIndent() : "";
        var checked = n.attributes.checked;
        var href = a.href ? a.href : Ext.isGecko ? "" : "#";
        var buf = [
            "<li class=\"x-tree-node\"><div ext:tree-node-id=\"", n.id, "\" class=\"x-tree-node-el x-tree-node-leaf x-unselectable ", a.cls, "\" unselectable=\"on\">",
            "<span class=\"x-tree-node-indent\">", this.indentMarkup, "</span>",
            "<img src=\"", this.emptyIcon, "\" class=\"x-tree-ec-icon x-tree-elbow\"/>",
            "<img src=\"", a.icon || this.emptyIcon, "\" class=\"x-tree-node-icon", (a.icon ? " x-tree-node-inline-icon" : ""), (a.iconCls ? " " + a.iconCls : ""), "\" unselectable=\"on\"/>",
            "<img src=\"" + this.emptyIcon + "\" class=\"x-tree-checkbox " + (true === checked ? " x-tree-node-checked" : "") + "\"/>",
            "<a hidefocus=\"on\" class=\"x-tree-node-anchor\" href=\"", href, "\" tabIndex=\"1\" ", a.hrefTarget ? " target=\"" + a.hrefTarget + "\"" : "", "><span unselectable=\"on\">", n.text, "</span></a></div>",
            "<ul class=\"x-tree-node-ct\" style=\"display:none;\"></ul>",
            "</li>"
        ].join("");

        var nel;

        if (bulkRender !== true && n.nextSibling && (nel = n.nextSibling.ui.getEl())) {
            this.wrap = Ext.DomHelper.insertHtml("beforeBegin", nel, buf);
        } else {
            this.wrap = Ext.DomHelper.insertHtml("beforeEnd", targetNode, buf);
        }

        this.elNode = this.wrap.childNodes[0];
        this.ctNode = this.wrap.childNodes[1];
        var cs = this.elNode.childNodes;
        this.indentNode = cs[0];
        this.ecNode = cs[1];
        this.iconNode = cs[2];
        this.checkbox = cs[3];
        this.cbEl = Ext.get(this.checkbox);
        this.anchor = cs[4];
        this.textNode = cs[4].firstChild;
    },

    setIconCls: function (iconCls) {
        Ext.fly(this.iconNode).set({ cls: "x-tree-node-icon " + iconCls });
    },

    isChecked: function () {
        return this.node.attributes.checked === true;
    },

    onCheckChange: function () {
        var checked = this.isChecked();
        var tree = this.node.getOwnerTree();
        this.update(checked);
        this.fireEvent("checkchange", this.node, checked);
    },

    setChecked: function (checked, runOnCheckChange) {
        checked = true === checked ? checked : false;
        var cb = this.cbEl || false;

        if (cb) {
            true === checked ? cb.addClass("x-tree-node-checked") : cb.removeClass("x-tree-node-checked");
        }

        this.node.attributes.checked = checked;

        if (typeof runOnCheckChange == "undefined" || runOnCheckChange == true) {
            this.onCheckChange();
        }

        return checked;
    },

    toggleCheck: function () {
        var checked = !this.isChecked();
        this.setChecked(checked);
        return checked;
    },

    update: function (checked) {
        // check parent
        var p = this.node.parentNode;
        var pui = p ? p.getUI() : false;

        if (pui && pui.setChecked) {
            var checked = 0;
            var unchecked = 0;

            p.eachChild(function (n) {
                var ui = n.getUI();

                if (ui && ui.isChecked()) {
                    checked++;
                } else {
                    unchecked++;
                }
            });

            if (unchecked == 0) {
                pui.setChecked(true, false);
            } else {
                pui.setChecked(false, false);
            }
        }

        // check children
        this.node.eachChild(function (n) {
            var ui = n.getUI();

            if (ui && ui.setChecked) {
                ui.setChecked(checked, false);
            }
        });
    },

    onCheckboxClick: function () {
        if (!this.disabled) {
            this.toggleCheck();
        }
    },

    onCheckboxOver: function () {
        this.addClass("x-tree-checkbox-over");
    },

    onCheckboxOut: function () {
        this.removeClass("x-tree-checkbox-over");
    },

    onCheckboxDown: function () {
        this.addClass("x-tree-checkbox-down");
    },

    onCheckboxUp: function () {
        this.removeClass("x-tree-checkbox-down");
    }
});

Ext.ux.tree.CheckTreeEventModel = Ext.extend(Ext.tree.TreeEventModel, {
    initEvents: function () {
        var el = this.tree.getTreeEl();
        el.on("click", this.delegateClick, this);

        if (this.tree.trackMouseOver !== false) {
            el.on("mouseover", this.delegateOver, this);
            el.on("mouseout", this.delegateOut, this);
        }

        el.on("mousedown", this.delegateDown, this);
        el.on("mouseup", this.delegateUp, this);
        el.on("dblclick", this.delegateDblClick, this);
        el.on("contextmenu", this.delegateContextMenu, this);
    },

    delegateOver: function (e, t) {
        if (!this.beforeEvent(e)) {
            return;
        }

        if (this.lastEcOver) {
            this.onIconOut(e, this.lastEcOver);
            delete this.lastEcOver;
        }

        if (this.lastCbOver) {
            this.onCheckboxOut(e, this.lastCbOver);
            delete this.lastCbOver;
        }

        if (e.getTarget(".x-tree-ec-icon", 1)) {
            this.lastEcOver = this.getNode(e);
            this.onIconOver(e, this.lastEcOver);
        } else if (e.getTarget(".x-tree-checkbox", 1)) {
            this.lastCbOver = this.getNode(e);
            this.onCheckboxOver(e, this.lastCbOver);
        }

        if (t = this.getNodeTarget(e)) {
            this.onNodeOver(e, this.getNode(e));
        }
    },

    delegateOut: function (e, t) {
        if (!this.beforeEvent(e)) {
            return;
        }

        if (e.getTarget(".x-tree-ec-icon", 1)) {
            var n = this.getNode(e);
            this.onIconOut(e, n);

            if (n == this.lastEcOver) {
                delete this.lastEcOver;
            }
        } else if (e.getTarget(".x-tree-checkbox", 1)) {
            var n = this.getNode(e);
            this.onCheckboxOut(e, n);

            if (n == this.lastCbOver) {
                delete this.lastCbOver;
            }
        }

        if ((t = this.getNodeTarget(e)) && !e.within(t, true)) {
            this.onNodeOut(e, this.getNode(e));
        }
    },

    delegateDown: function (e, t) {
        if (!this.beforeEvent(e)) {
            return;
        }

        if (e.getTarget(".x-tree-checkbox", 1)) {
            this.onCheckboxDown(e, this.getNode(e));
        }
    },

    delegateUp: function (e, t) {
        if (!this.beforeEvent(e)) {
            return;
        }

        if (e.getTarget(".x-tree-checkbox", 1)) {
            this.onCheckboxUp(e, this.getNode(e));
        }
    },

    delegateOut: function (e, t) {
        if (!this.beforeEvent(e)) {
            return;
        }

        if (e.getTarget(".x-tree-ec-icon", 1)) {
            var n = this.getNode(e);
            this.onIconOut(e, n);

            if (n == this.lastEcOver) {
                delete this.lastEcOver;
            }
        } else if (e.getTarget(".x-tree-checkbox", 1)) {
            var n = this.getNode(e);
            this.onCheckboxOut(e, n);

            if (n == this.lastCbOver) {
                delete this.lastCbOver;
            }
        }

        if ((t = this.getNodeTarget(e)) && !e.within(t, true)) {
            this.onNodeOut(e, this.getNode(e));
        }
    },

    delegateClick: function (e, t) {
        if (!this.beforeEvent(e)) {
            return;
        }

        if (e.getTarget(".x-tree-checkbox", 1)) {
            this.onCheckboxClick(e, this.getNode(e));
        } else if (e.getTarget(".x-tree-ec-icon", 1)) {
            this.onIconClick(e, this.getNode(e));
        } else if (this.getNodeTarget(e)) {
            this.onNodeClick(e, this.getNode(e));
        }
    },

    onCheckboxClick: function (e, node) {
        node.ui.onCheckboxClick();
    },

    onCheckboxOver: function (e, node) {
        node.ui.onCheckboxOver();
    },

    onCheckboxOut: function (e, node) {
        node.ui.onCheckboxOut();
    },

    onCheckboxDown: function (e, node) {
        node.ui.onCheckboxDown();
    },

    onCheckboxUp: function (e, node) {
        node.ui.onCheckboxUp();
    }
});
