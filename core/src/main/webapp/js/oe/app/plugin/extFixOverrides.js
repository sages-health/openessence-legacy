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

Ext.override(Ext.TabPanel, {
    onRemove: function (c) {
        Ext.TabPanel.superclass.onRemove.call(this, c);
        Ext.destroy(Ext.get(this.getTabEl(c)));
        this.stack.remove(c);
        c.un('disable', this.onItemDisabled, this);
        c.un('enable', this.onItemEnabled, this);
        c.un('titlechange', this.onItemTitleChanged, this);
        c.un('iconchange', this.onItemIconChanged, this);
        c.un('beforeshow', this.onBeforeShowItem, this);
        if (c == this.activeTab) {
            var next = this.stack.next();
            if (next) {
                this.setActiveTab(next);
            } else {
                var count = this.items.getCount();
                for (next = 0; next < count && this.getComponent(next) == c; next++) {
                }
                if (next < count) {
                    this.setActiveTab(next);
                } else {
                    this.activeTab = null;
                }
            }
        }
        this.delegateUpdates();
    },
    onRender: function (ct, position) {
        Ext.TabPanel.superclass.onRender.call(this, ct, position);
        if (this.plain) {
            var pos = this.tabPosition == 'top' ? 'header' : 'footer';
            this[pos].addClass('x-tab-panel-' + pos + '-plain');
        }
        var st = this[this.stripTarget];
        this.stripWrap = st.createChild({cls: 'x-tab-strip-wrap', cn: {
            tag: 'ul', cls: 'x-tab-strip x-tab-strip-' + this.tabPosition}});
        var beforeEl = (this.tabPosition == 'bottom' ? this.stripWrap : null);
        this.stripSpacer = st.createChild({cls: 'x-tab-strip-spacer'}, beforeEl);
        this.strip = new Ext.Element(this.stripWrap.dom.firstChild);
        this.edge = this.strip.createChild({tag: 'li', cls: 'x-tab-edge', cn: {
            tag: 'span', cls: 'x-tab-strip-text', cn: '&#160;'}});
        this.strip.createChild({cls: 'x-clear'});
        this.body.addClass('x-tab-panel-body-' + this.tabPosition);
        if (!this.itemTpl) {
            var tt = new Ext.Template(
                '<li class="{cls}" id="{id}"><a class="x-tab-strip-close" onclick="return false;"></a>',
                '<a class="x-tab-right" href="#" onclick="return false;"><em class="x-tab-left">',
                '<span class="x-tab-strip-inner"><span class="x-tab-strip-text {iconCls}">{text}</span></span>',
                '</em></a></li>'
            );
            tt.disableFormats = true;
            tt.compile();
            Ext.TabPanel.prototype.itemTpl = tt;
        }
        this.items.each(this.initTab, this);
    }
});

Ext.override(Ext.form.BasicForm, {
    /**
     * @cfg {Boolean} trackResetOnLoad If set to <tt>true</tt>, {@link #reset}() resets to the last loaded
     * or {@link #setValues}() data instead of when the form was first created.  Defaults to <tt>false</tt>.
     */
    trackResetOnLoad: true,

    setValues: function (values, fireSelect) {
        if (Ext.isArray(values)) { // array of objects
            for (var i = 0, len = values.length; i < len; i++) {
                var v = values[i];
                var f = this.findField(v.id);
                if (f) {
                    f.setValue(v.value, fireSelect);
                    if (this.trackResetOnLoad) {
                        f.originalValue = f.getValue();
                    }
                }
            }
        } else { // object hash
            var field, id;
            for (id in values) {
                if (!Ext.isFunction(values[id]) && (field = this.findField(id))) {
                    field.setValue(values[id], fireSelect);
                    if (this.trackResetOnLoad) {
                        field.originalValue = field.getValue();
                    }
                }
            }
        }
        return this;
    },
    getFieldValues: function () {
        var o = {};
        this.items.each(function (f) {
            if (!f.skip && !(f.getValue() === null || f.getValue() === '')) {
                if ((f.xtype == 'datefield' || f.xtype == 'epiDateField')) {
                    // Change dates to millis
                    o[f.getName()] = f.getValue().getTime();
                } else {
                    o[f.getName()] = f.getValue();
                }
            }
        });
        return o;
    }
});

Ext.override(Ext.form.DateField, {
    altFormats: "m/d/Y|n/j/Y|n/j/y|m/j/y|n/d/y|m/j/Y|n/d/Y|m-d-y|m-d-Y|m/d|m-d|md|mdy|mdY|d|Y-m-d|W-o",

    parseDate: function (value) {
        if (!value || Ext.isDate(value)) {
            return value;
        }
        var v = Date.parseDate(value, this.format);
        if (!v && this.altFormats) {
            if (!this.altFormatsArray) {
                this.altFormatsArray = this.altFormats.split("|");
            }
            for (var i = 0, len = this.altFormatsArray.length; i < len && !v; i++) {
                v = Date.parseDate(value, this.altFormatsArray[i]);
            }
            // Added parser for milliseconds
            if (!v) {
                v = Date.parseDate(value / 1000, 'U');
            }
        }
        return v;
    }
});

/**
 * Override the getWeekOfYear to provide custom offsets (depending on epi week start day)
 *
 * Default is offset 3 (start day 1) or Monday - Sunday
 */
Ext.apply(Date.prototype, {
    getEpiYear: function () {
        // **************** ESSENCE SQL code to calculate EPI year *************************
//		DECLARE @ISOyear int
//		   DECLARE @ISOweek int
//		   DECLARE @ISOmonth int
//		   SET @ISOyear= DATEPART(yyyy,@DATE)
//		   SET @ISOmonth= DATEPART(mm,@DATE)
//		   SET @ISOweek= dbo.ISOweek(@date)
//		--Special cases: Jan 1-3 may belong to the previous year
//		   IF (@ISOweek=1)
//		      IF (@ISOmonth=12)
//		         SET @ISOyear=@ISOyear + 1 
//		--Special case: Dec 29-31 may belong to the next year
//		   IF (@ISOweek > 50) 
//		      IF (@ISOmonth=1)
//		        SET @ISOyear=@ISOyear - 1
//		   RETURN(@ISOyear)

        var epiYear = this.getFullYear();
        var month = this.getMonth();
        var epiWeek = this.getEpiWeek();
        if (epiWeek == 1) {
            if (month == 11) {
                epiYear = epiYear + 1;
            }
        }
        if (epiWeek > 50) {
            if (month == 0) {
                epiYear = epiYear - 1;
            }
        }
        return epiYear;
    },
    getWeek: function () {
        var onejan = new Date(this.getFullYear(), 0, 1);
        return Math.ceil((((this - onejan) / 86400000) + onejan.getDay() + 1) / 7);
    },
    getEpiWeek: function () {
// **************** ESSENCE SQL code to calculate EPI week *************************		
//		   DECLARE @ISOweek int
//		   SET @ISOweek= DATEPART(wk,@DATE)+1
//		      -DATEPART(wk,CAST(DATEPART(yy,@DATE) as CHAR(4))+'0104')
//		--Special cases: Jan 1-3 may belong to the previous year
//		   IF (@ISOweek=0) 
//		      SET @ISOweek=dbo.ISOweek(CAST(DATEPART(yy,@DATE)-1 
//		         AS CHAR(4))+'12'+ CAST(24+DATEPART(DAY,@DATE) AS CHAR(2)))+1
//		--Special case: Dec 29-31 may belong to the next year
//		   IF ((DATEPART(mm,@DATE)=12) AND 
//		      ((DATEPART(dd,@DATE)-DATEPART(dw,@DATE))>= 28))
//		      SET @ISOweek=1
//		   RETURN(@ISOweek)

        var D4 = new Date(this.getFullYear(), 0, 4);
        var week = this.getWeek() + 1 - D4.getWeek();
        // Special cases: Jan 1-3 may belong to the previous year
        if (week == 0) {
            var DPrev = new Date(this.getFullYear() - 1, 11, 24 + this.getDate());
            week = DPrev.getEpiWeek() + 1;
        }
        // Special case: Dec 29-31 may belong to the next year
        if (this.getMonth() == 11 && (this.getDate() - (this.getDay() + 1) >= 28)) {
            week = 1;
        }
        return week;
    },

    getWeekOfYear: function () {
        // adapted from http://www.merlyn.demon.co.uk/weekcalc.htm
        var ms1d = 864e5, // milliseconds in a day
            ms7d = 7 * ms1d; // milliseconds in a week

        // Used to compute offset based on epi week start day (0 sunday -> 6 saturday), defaults to 1
        // NOTE: (((Math.abs(Ext.num(dimensionsBundle['epidemiological.day.start'], 1) - 6)) + 5) % 7);  Changed 5 to 4 to use CDC epi week
        //       vs. ISO-8601
        // TODO: Test 4 & 6
        var offset = (((Math.abs(Ext.num(dimensionsBundle['epidemiological.day.start'], 1) - 5)) + 6) % 7); // epi week start day offset

        return function () { // return a closure so constants get calculated only once
            var DC3 = Date.UTC(this.getFullYear(), this.getMonth(), this.getDate() + offset) / ms1d, // an Absolute Day Number
                AWN = Math.floor(DC3 / 7), // an Absolute Week Number
                Wyr = new Date(AWN * ms7d).getUTCFullYear();

            return AWN - Math.floor(Date.UTC(Wyr, 0, 7) / ms7d) + 1;
        };
    }(),

    /**
     * Don't infinite loop if this is an invalid date.
     */
    clearTime: function (clone) {
        if (clone) {
            return this.clone().clearTime();
        }

        // get current date before clearing time
        var d = this.getDate();

        // begin patch
        if (isNaN(d)) {
            return this;
        }
        // end patch

        // clear time
        this.setHours(0);
        this.setMinutes(0);
        this.setSeconds(0);
        this.setMilliseconds(0);

        if (this.getDate() != d) { // account for DST (i.e. day of month changed when setting hour = 0)
            // note: DST adjustments are assumed to occur in multiples of 1 hour (this is almost always the case)
            // refer to http://www.timeanddate.com/time/aboutdst.html for the (rare) exceptions to this rule

            // increment hour until cloned date == current date
            for (var hr = 1, c = this.add(Date.HOUR, hr); c.getDate() != d; hr++, c = this.add(Date.HOUR, hr)) {
                ;
            }

            this.setDate(d);
            this.setHours(c.getHours());
        }

        return this;
    }
});

// Update to use message bundle
Ext.override(Ext.DatePicker, {
    /**
     * @cfg {String} todayText The text to display on the button that selects
     *      the current date (defaults to <tt>'Today'</tt>)
     */
    todayText: messagesBundle['datepicker.todayText'] || 'Today',
    /**
     * @cfg {String} okText The text to display on the ok button (defaults to
     *      <tt>'&#160;OK&#160;'</tt> to give the user extra clicking room)
     */
    okText: messagesBundle['datepicker.okText'] || '&#160;OK&#160;',
    /**
     * @cfg {String} cancelText The text to display on the cancel button
     *      (defaults to <tt>'Cancel'</tt>)
     */
    cancelText: messagesBundle['datepicker.cancelText'] || 'Cancel',
    /**
     * @cfg {Function} handler Optional. A function that will handle the select
     *      event of this picker. The handler is passed the following
     *      parameters:<div class="mdetail-params">
     *      <ul>
     *      <li><code>picker</code> : DatePicker<div class="sub-desc">The
     *      Ext.DatePicker.</div></li>
     *      <li><code>date</code> : Date<div class="sub-desc">The selected
     *      date.</div></li>
     *      </ul>
     *      </div>
     */
    /**
     * @cfg {Object} scope The scope (<tt><b>this</b></tt> reference) in
     *      which the <code>{@link #handler}</code> function will be called.
     *      Defaults to this DatePicker instance.
     */
    /**
     * @cfg {String} todayTip The tooltip to display for the button that selects
     *      the current date (defaults to <tt>'{current date} (Spacebar)'</tt>)
     */
    todayTip: messagesBundle['datepicker.todayTip'] || '{0} (Spacebar)',
    /**
     * @cfg {String} minText The error text to display if the minDate validation
     *      fails (defaults to <tt>'This date is before the minimum date'</tt>)
     */
    minText: messagesBundle['datepicker.minText'] || 'This date is before the minimum date',
    /**
     * @cfg {String} maxText The error text to display if the maxDate validation
     *      fails (defaults to <tt>'This date is after the maximum date'</tt>)
     */
    maxText: messagesBundle['datepicker.maxText'] || 'This date is after the maximum date',
    /**
     * @cfg {String} format The default date format string which can be
     *      overriden for localization support. The format must be valid
     *      according to {@link Date#parseDate} (defaults to <tt>'m/d/y'</tt>).
     */
    format: dimensionsBundle['default.date.format'] || 'm/d/y',
    /**
     * @cfg {String} disabledDaysText The tooltip to display when the date falls
     *      on a disabled day (defaults to <tt>'Disabled'</tt>)
     */
    disabledDaysText: messagesBundle['datepicker.disabledTip'] || 'Disabled',
    /**
     * @cfg {String} disabledDatesText The tooltip text to display when the date
     *      falls on a disabled date (defaults to <tt>'Disabled'</tt>)
     */
    disabledDatesText: messagesBundle['datepicker.disabledTip'] || 'Disabled',
    /**
     * @cfg {String} nextText The next month navigation button tooltip (defaults
     *      to <tt>'Next Month (Control+Right)'</tt>)
     */
    nextText: messagesBundle['datepicker.nextText'] || 'Next Month (Control+Right)',
    /**
     * @cfg {String} prevText The previous month navigation button tooltip
     *      (defaults to <tt>'Previous Month (Control+Left)'</tt>)
     */
    prevText: messagesBundle['datepicker.prevText'] || 'Previous Month (Control+Left)',
    /**
     * @cfg {String} monthYearText The header month selector tooltip (defaults
     *      to <tt>'Choose a month (Control+Up/Down to move years)'</tt>)
     */
    monthYearText: messagesBundle['datepicker.monthYearText'] || 'Choose a month (Control+Up/Down to move years)',
    /**
     * @cfg {Number} startDay Day index at which the week should begin, 0-based
     *      (defaults to 0, which is Sunday)
     *
     * If start day is configured set it, otherwise use default
     */
    startDay: Ext.num(dimensionsBundle['epidemiological.day.start'], 0),

    // private
    initComponent: function () {
        Ext.DatePicker.superclass.initComponent.call(this);

        this.value = this.value ? this.value.clearTime(true) : new Date().clearTime();

        this.monthNames = Date.monthNames;
        this.dayNames = Date.dayNames;

        this.addEvents(
            /**
             * @event select Fires when a date is selected
             * @param {DatePicker}
             *            this
             * @param {Date}
             *            date The selected date
             */
            'select'
        );

        if (this.handler) {
            this.on('select', this.handler, this.scope || this);
        }

        this.initDisabledDays();
    }
});

Ext.apply(Ext.form.VTypes, {
    daterange: function (val, field) {
        var dependency = field.ownerCt.getComponent(field.dependency);
        var date = field.parseDate(val);
        if (!date) {
            dependency.setMaxValue();
            dependency.setMinValue();
            return;
        }
        dependency.allowBlank = false;
        if (!field.dateRange || (date.getTime() != field.dateRange.getTime())) {
            if (field.dependency.indexOf('start') >= 0) {
                dependency.setMaxValue(date);
            } else if (field.dependency.indexOf('end') >= 0) {
                dependency.setMinValue(date);
            }
            field.dateRange = date;
            dependency.validate();
        }
        /*
         * Always return true since we're only using this vtype to set the
         * min/max allowed values (these are tested for after the vtype test)
         */
        return true;
    },

    //password confirmation validation
    password: function (val, field) {
        if (field.dependency) {
            var dependency = field.ownerCt.getComponent(field.dependency);
            return (val == dependency.getValue());
        }
        return true;
    },

    passwordText: messagesBundle['input.datasource.default.passwordsDoNotMatch'] || 'Passwords do not match'

});

Ext.override(Ext.form.ComboBox, {
    setValue: function (v, fireSelect) {
        //begin patch
        // Store not loaded yet? Set value when it *is* loaded.
        // Defer the setValue call until after the next load.
        if (this.store.getCount() == 0) {
            this.store.on('load',
                this.setValue.createDelegate(this, [v, fireSelect]), null, {single: true});
            return;
        }
        //end patch
        var text = v;
        if (this.valueField) {
            var r = this.findRecord(this.valueField, v);
            if (r) {
                text = r.data[this.displayField];
                if (fireSelect) {
                    this.fireEvent('select', this, r, this.store.indexOf(r));
                }
            } else if (Ext.isDefined(this.valueNotFoundText)) {
                text = this.valueNotFoundText;
            }
        }
        this.lastSelectionText = text;
        if (this.hiddenField) {
            this.hiddenField.value = v;
        }
        Ext.form.ComboBox.superclass.setValue.call(this, text);
        this.value = v;
        return this;
    },

    /**
     * Fix to handle if value is an array
     */
    findRecord: function (prop, value) {
        var record = undefined;
        if (this.store.getCount() > 0) {
            this.store.each(function (r) {
                if (r.data[prop] == value) {
                    record = r;
                    return false;
                }
                // begin patch
                else if (Ext.isArray(r.data[prop]) && Ext.isArray(value)) { // == fails on arrays
                    if (r.data[prop].length === value.length) {
                        for (var i = 0; i < r.data[prop].length; i++) {
                            if (r.data[prop][i] !== value[i]) {
                                return true; // continue Ext.each
                            }
                        }
                        record = r;
                        return false;
                    }
                }
                // end patch

            });
        }
        return record;
    }
});

Ext.override(Ext.MessageBox, {
    buttonText: {
        ok: messagesBundle['input.datasource.default.ok'] || "OK",
        cancel: messagesBundle['input.datasource.default.cancel'] || "Cancel",
        yes: messagesBundle['input.datasource.default.yes'] || "Yes",
        no: messagesBundle['input.datasource.default.no'] || "No"
    }
});

// Overrides for tool-tips on form fields
Ext.override(Ext.form.Field, {
    afterRender: Ext.form.Field.prototype.afterRender.createSequence(function () {
        if (this.qtip) {
            var target = this.getTipTarget();
            if (target) {
                if (typeof this.qtip == 'object') {
                    Ext.QuickTips.register(Ext.apply({
                        target: target
                    }, this.qtip));
                } else {
                    target.dom.qtip = this.qtip;
                }
            }
        }
    }),
    getTipTarget: function () {
        return this.el;
    }
});
//checkboxes and radios, the main element is a hidden input.
Ext.override(Ext.form.Checkbox, {
    getTipTarget: function () {
        return this.imageEl;
    }
});

/**
 * Don't mark the response as a failure just because it's missing {success: true}, that's
 * what status codes are for.
 * See http://www.sencha.com/forum/showthread.php?14701-Aren-t-Http-Status-Codes-enough&p=73458&viewfull=1#post73458
 */
Ext.override(Ext.form.Action.Submit, {
    success: function (response) {
        // workaround http://www.sencha.com/forum/showthread.php?85908-FIXED-732-Ext-doesn-t-normalize-IE-s-crazy-HTTP-status-code-1223
        if (Ext.isIE && response.status === 1223) {
            response.status = 204;
            response.statusText = 'No Content';
        }

        var result = this.processResponse(response);
        if (result === true || (response.status >= 200 && response.status < 300) || result.success) {
            this.form.afterAction(this, true);
            return;
        }
        this.form.afterAction(this, false);
    },
    failure: function (response) {
        this.response = response;
        if (response.status == '400' && response.statusText == 'Validation Error') {
            var result = this.processResponse(response);
            if (result.errors) {
                this.form.markInvalid(result.errors);
                this.failureType = Ext.form.Action.SERVER_INVALID;
            }
        } else {
            this.failureType = Ext.form.Action.CONNECT_FAILURE;
        }
        this.form.afterAction(this, false);
    },
    handleResponse: function (response) {
        if (this.form.errorReader) {
            var rs = this.form.errorReader.read(response);
            var errors = [];
            if (rs.records) {
                for (var i = 0, len = rs.records.length; i < len; i++) {
                    var r = rs.records[i];
                    errors[i] = r.data;
                }
            }
            if (errors.length < 1) {
                errors = null;
            }
            return {
                success: rs.success,
                errors: errors
            };
        }

        // Ext 3.0 always tries to parse response as JSON, fix: return false if not JSON
        return this.decodeResponse(response);
    }
});

// Fix issue with empty responses, backported from ExtJS 3.4, see https://bugzilla.mozilla.org/show_bug.cgi?id=521301
Ext.override(Ext.form.Action, {
    decodeResponse: function (response) {
        try {
            return Ext.decode(response.responseText);
        } catch (e) {
            return false;
        }
    }
});

// Fix negative height that throws error in IE
Ext.override(Ext.Shadow, {
    realign: function (l, t, w, h) {
        if (!this.el) {
            return;
        }
        var a = this.adjusts, d = this.el.dom, s = d.style;
        s.left = (l + a.l) + "px";
        s.top = (t + a.t) + "px";
        var sw = (w + a.w), sh = (h + a.h), sws = sw + "px", shs = sh + "px";
        if (s.width != sws || s.height != shs) {
            s.width = sws;

            // BEGIN PATCH
            if (Ext.isIE && sh < 0) {
                s.height = "0px";
            } else {
                s.height = shs;
            }
            // END PATCH

            if (!Ext.isIE) {
                var cn = d.childNodes;
                var sww = Math.max(0, (sw - 12)) + "px";
                cn[0].childNodes[1].style.width = sww;
                cn[1].childNodes[1].style.width = sww;
                cn[2].childNodes[1].style.width = sww;
                cn[1].style.height = Math.max(0, (sh - 12)) + "px";
            }
        }
    }
});

// backport deep tree search from 3.4
Ext.override(Ext.tree.AsyncTreeNode, {
    findChildBy: function (fn, scope, deep) {
        var cs = this.childNodes,
            len = cs.length,
            i = 0,
            n,
            res;
        for (; i < len; i++) {
            n = cs[i];
            if (fn.call(scope || n, n) === true) {
                return n;
            } else if (deep) {
                res = n.findChildBy(fn, scope, deep);
                if (res != null) {
                    return res;
                }
            }

        }
        return null;
    }
});
