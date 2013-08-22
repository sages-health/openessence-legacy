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

Ext.LinkButton = Ext.extend(Ext.Button, {
    template: new Ext.Template(
        '<table border="0" class="x-btn-wrap"><tbody><tr>',
        '<td class="x-btn-left"><i> </i></td><td class="x-btn-center"><a class="x-btn-text" href="{1}" target="{2}">{0}</a></td><td class="x-btn-right"><i> </i></td>',
        "</tr></tbody></table>"),

    onRender: function (ct, position) {
        var btn, targs = [this.text || ' ', this.href, this.target || "_self"];
        if (position) {
            btn = this.template.insertBefore(position, targs, true);
        } else {
            btn = this.template.append(ct, targs, true);
        }
        var btnEl = btn.child("a:first");
        btnEl.on('focus', this.onFocus, this);
        btnEl.on('blur', this.onBlur, this);

        this.initButtonEl(btn, btnEl);
        Ext.ButtonToggleMgr.register(this);
    },

    onClick: function (e) {
        if (e.button !== 0) {
            return;
        }
        if (!this.disabled) {
            if (this.constructLink) {
                // used to build parameters for exportGridToFile
                this.el.dom.getElementsByTagName('a')[0].href = this.constructLink();
            }
            this.fireEvent("click", this, e);
            if (this.handler) {
                this.handler.call(this.scope || this, this, e);
            }
        }
    }

});
