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
 * Date menu that uses Epidemiological date picker
 *
 * @see Ext.menu.DateMenu
 */
Ext.ux.EpidemiologicalDateMenu = Ext.extend(Ext.menu.Menu, {
    /**
     * @cfg {Boolean} enableScrolling
     * @hide
     */
    enableScrolling: false,
    /**
     * @cfg {Function} handler
     * Optional. A function that will handle the select event of this menu.
     * The handler is passed the following parameters:<div class="mdetail-params"><ul>
     * <li><code>picker</code> : DatePicker<div class="sub-desc">The Ext.DatePicker.</div></li>
     * <li><code>date</code> : Date<div class="sub-desc">The selected date.</div></li>
     * </ul></div>
     */
    /**
     * @cfg {Object} scope
     * The scope (<tt><b>this</b></tt> reference) in which the <code>{@link #handler}</code>
     * function will be called.  Defaults to this DateMenu instance.
     */
    /**
     * @cfg {Boolean} hideOnClick
     * False to continue showing the menu after a date is selected, defaults to true.
     */
    hideOnClick: true,

    /**
     * @cfg {String} pickerId
     * An id to assign to the underlying date picker. Defaults to <tt>null</tt>.
     */
    pickerId: null,

    /**
     * @cfg {Number} maxHeight
     * @hide
     */
    /**
     * @cfg {Number} scrollIncrement
     * @hide
     */
    /**
     * The {@link Ext.DatePicker} instance for this DateMenu
     * @property picker
     * @type DatePicker
     */
    cls: 'x-date-menu',

    /**
     * @event click
     * @hide
     */

    /**
     * @event itemclick
     * @hide
     */

    initComponent: function () {
        this.on('beforeshow', this.onBeforeShow, this);
        this.strict = Ext.isIE7 && Ext.isStrict;
        if (this.strict) {
            this.on('show', this.onShow, this, {single: true, delay: 20});
        }
        Ext.apply(this, {
            plain: true,
            showSeparator: false,
            items: this.picker = new Ext.ux.EpidemiologicalDatePicker(Ext.applyIf({
                internalRender: this.strict || !Ext.isIE,
                ctCls: 'x-menu-date-item',
                id: this.pickerId
            }, this.initialConfig))
        });
        this.picker.purgeListeners();
        Ext.ux.EpidemiologicalDateMenu.superclass.initComponent.call(this);
        /**
         * @event select
         * Fires when a date is selected from the {@link #picker Ext.DatePicker}
         * @param {DatePicker} picker The {@link #picker Ext.DatePicker}
         * @param {Date} date The selected date
         */
        this.relayEvents(this.picker, ['select']);
        this.on('select', this.menuHide, this);
        if (this.handler) {
            this.on('select', this.handler, this.scope || this);
        }
    },

    menuHide: function () {
        if (this.hideOnClick) {
            this.hide(true);
        }
    },

    onBeforeShow: function () {
        if (this.picker) {
            this.picker.hideMonthPicker(true);
        }
    },

    onShow: function () {
        var el = this.picker.getEl();
        el.setWidth(el.getWidth()); //nasty hack for IE7 strict mode
    }
});
Ext.reg('epiDateMenu', Ext.ux.EpidemiologicalDateMenu);
