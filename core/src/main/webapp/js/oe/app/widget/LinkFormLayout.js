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

/**
 * Adds simulated links (with callbacks) for form items
 */
Ext.ux.LinkFormLayout = Ext.extend(Ext.layout.FormLayout, {
    /**
     * @cfg {String} requiredIndicator
     */
    requiredIndicator: '*',

    /**
     * See {@link Ext.layout.FormLayout#getTemplateArgs}.
     */
    getTemplateArgs: function (field) {
        var noLabelSep = !field.fieldLabel || field.hideLabel;
        return {
            id: field.id,
            label: field.fieldLabel,
            labelStyle: this.getLabelStyle(field.labelStyle),
            elementStyle: this.elementStyle || '',
            labelSeparator: noLabelSep ? '' : (Ext.isDefined(field.labelSeparator) ? field.labelSeparator : this.labelSeparator),
            requiredIndicator: (noLabelSep ? '' : (field.allowBlank ? '' : this.requiredIndicator)),
            itemCls: (field.itemCls || this.container.itemCls || '') + (field.hideLabel ? ' x-hide-label' : ''),
            clearCls: field.clearCls || 'x-form-clear-left'
        };
    },

    fieldTpl: (function () {
        var t = new Ext.Template(
            '<div class="x-form-item x-linkform-item {itemCls}" tabIndex="-1">',
            '<label for="{id}" style="{labelStyle}" class="x-form-item-label">{label}{labelSeparator}{requiredIndicator}</label>',
            '<div class="x-form-element" id="x-form-el-{id}" style="{elementStyle}">',
            '</div><div class="{clearCls}"></div>',
            '</div>'
        );
        t.disableFormats = true;
        return t.compile();
    })(),

    linkTpl: (function () {
        var t = new Ext.Template('<div id="x-form-item-link-{0}" class="x-form-item-link"><a>{1}</a></td></div>');
        t.disableFormats = true;
        return t.compile();
    })(),

    helpTpl: (function () {
        var t = new Ext.Template('<div id="x-form-item-help-{0}" class="x-form-item-help"></div>');
        t.disableFormats = true;
        return t.compile();
    })(),

    // private
    renderItem: function (c, position, target) {
        if (c && (c.isFormField || c.fieldLabel) && c.inputType != 'hidden') {
            var args = this.getTemplateArgs(c);
            if (Ext.isNumber(position)) {
                position = target.dom.childNodes[position] || null;
            }

            if (position) {
                c.itemCt = this.fieldTpl.insertBefore(position, args, true);
            } else {
                c.itemCt = this.fieldTpl.append(target, args, true);
            }
            if (!c.rendered) {
                c.render('x-form-el-' + c.id);

                // Add help
                if (c.help) {
                    this.helpTpl.append('x-form-el-' + c.id, [c.id]);
                    Ext.QuickTips.register({
                        target: Ext.get('x-form-item-help-' + c.id),
                        text: c.help
                    });
                }

                // Add link(s) and callback
                if (c.link) {
                    this.linkTpl.append('x-form-el-' + c.id, [c.id, c.link.text || messagesBundle['input.datasource.default.link']]);

                    if (c.link.handler) {
                        Ext.get('x-form-item-link-' + c.id).on('click', c.link.handler, this);
                    }
                } else if (c.links) {
                    Ext.each(c.links, function (link, index) {
                        this.linkTpl.append('x-form-el-' + c.id, [c.id + index, link.text || messagesBundle['input.datasource.default.link']]);

                        if (link.handler) {
                            Ext.get('x-form-item-link-' + c.id + index).on('click', link.handler, this);
                        }
                    }, this);
                }
            } else if (!this.isValidParent(c, target)) {
                Ext.fly('x-form-el-' + c.id).appendChild(c.getPositionEl());
            }
            if (!c.getItemCt) {
                // Non form fields don't have getItemCt, apply it here
                // This will get cleaned up in onRemove
                Ext.apply(c, {
                    getItemCt: function () {
                        return c.itemCt;
                    },
                    customItemCt: true
                });
            }
            c.label = c.getItemCt().child('label.x-form-item-label');
            if (this.trackLabels && !this.isHide(c)) {
                if (c.hidden) {
                    this.onFieldHide(c);
                }
                c.on({
                    scope: this,
                    show: this.onFieldShow,
                    hide: this.onFieldHide
                });
            }
            this.configureItem(c);
        } else {
            Ext.layout.FormLayout.superclass.renderItem.apply(this, arguments);
        }
    }
});

Ext.Container.LAYOUTS.linkform = Ext.ux.LinkFormLayout;
