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
 * Adds Epidemiological week to the date picker
 */
Ext.ux.EpidemiologicalDatePicker = Ext.extend(Ext.DatePicker, {
    /**
     * @cfg {String} todayText
     * The text to display on the button that selects the current date (defaults to <tt>'Today'</tt>)
     */
    todayText: messagesBundle['epi.datepicker.todayText'] || 'Today',
    /**
     * @cfg {String} okText
     * The text to display on the ok button (defaults to <tt>'&#160;OK&#160;'</tt> to give the user extra clicking room)
     */
    okText: messagesBundle['epi.datepicker.okText'] || '&#160;OK&#160;',
    /**
     * @cfg {String} cancelText
     * The text to display on the cancel button (defaults to <tt>'Cancel'</tt>)
     */
    cancelText: messagesBundle['epi.datepicker.cancelText'] || 'Cancel',
    /**
     * @cfg {Function} handler
     * Optional. A function that will handle the select event of this picker.
     * The handler is passed the following parameters:<div class="mdetail-params"><ul>
     * <li><code>picker</code> : DatePicker<div class="sub-desc">The Ext.DatePicker.</div></li>
     * <li><code>date</code> : Date<div class="sub-desc">The selected date.</div></li>
     * </ul></div>
     */
    /**
     * @cfg {Object} scope
     * The scope (<tt><b>this</b></tt> reference) in which the <code>{@link #handler}</code>
     * function will be called.  Defaults to this DatePicker instance.
     */
    /**
     * @cfg {String} todayTip
     * The tooltip to display for the button that selects the current date (defaults to <tt>'{current date} (Spacebar)'</tt>)
     */
    todayTip: messagesBundle['epi.datepicker.todayTip'] || '{0} (Spacebar)',
    /**
     * @cfg {String} minText
     * The error text to display if the minDate validation fails (defaults to <tt>'This date is before the minimum date'</tt>)
     */
    minText: messagesBundle['epi.datepicker.minText'] || 'This date is before the minimum date',
    /**
     * @cfg {String} maxText
     * The error text to display if the maxDate validation fails (defaults to <tt>'This date is after the maximum date'</tt>)
     */
    maxText: messagesBundle['epi.datepicker.maxText'] || 'This date is after the maximum date',
    /**
     * @cfg {String} format
     * The default date format string which can be overriden for localization support.  The format must be
     * valid according to {@link Date#parseDate} (defaults to <tt>'m/d/y'</tt>).
     */
    format: 'm/d/y',
    /**
     * @cfg {String} disabledDaysText
     * The tooltip to display when the date falls on a disabled day (defaults to <tt>'Disabled'</tt>)
     */
    disabledDaysText: messagesBundle['epi.datepicker.disabledTip'] || 'Disabled',
    /**
     * @cfg {String} disabledDatesText
     * The tooltip text to display when the date falls on a disabled date (defaults to <tt>'Disabled'</tt>)
     */
    disabledDatesText: messagesBundle['epi.datepicker.disabledTip'] || 'Disabled',
    /**
     * @cfg {Array} monthNames
     * An array of textual month names which can be overriden for localization support (defaults to Date.monthNames)
     */
    monthNames: [
        messagesBundle['epi.datepicker.january'] || "January",
        messagesBundle['epi.datepicker.february'] || "February",
        messagesBundle['epi.datepicker.march'] || "March",
        messagesBundle['epi.datepicker.april'] || "April",
        messagesBundle['epi.datepicker.may'] || "May",
        messagesBundle['epi.datepicker.june'] || "June",
        messagesBundle['epi.datepicker.july'] || "July",
        messagesBundle['epi.datepicker.august'] || "August",
        messagesBundle['epi.datepicker.september'] || "September",
        messagesBundle['epi.datepicker.october'] || "October",
        messagesBundle['epi.datepicker.november'] || "November",
        messagesBundle['epi.datepicker.december'] || "December"
    ],
    /**
     * @cfg {Array} dayNames
     * An array of textual day names which can be overriden for localization support (defaults to Date.dayNames)
     */
    dayNames: [
        messagesBundle['epi.datepicker.sunday'] || "Sunday",
        messagesBundle['epi.datepicker.monday'] || "Monday",
        messagesBundle['epi.datepicker.tuesday'] || "Tuesday",
        messagesBundle['epi.datepicker.wednesday'] || "Wednesday",
        messagesBundle['epi.datepicker.thursday'] || "Thursday",
        messagesBundle['epi.datepicker.friday'] || "Friday",
        messagesBundle['epi.datepicker.saturday'] || "Saturday"
    ],
    /**
     * @cfg {String} nextText
     * The next month navigation button tooltip (defaults to <tt>'Next Month (Control+Right)'</tt>)
     */
    nextText: messagesBundle['epi.datepicker.nextText'] || 'Next Month (Control+Right)',
    /**
     * @cfg {String} prevText
     * The previous month navigation button tooltip (defaults to <tt>'Previous Month (Control+Left)'</tt>)
     */
    prevText: messagesBundle['epi.datepicker.prevText'] || 'Previous Month (Control+Left)',
    /**
     * @cfg {String} monthYearText
     * The header month selector tooltip (defaults to <tt>'Choose a month (Control+Up/Down to move years)'</tt>)
     */
    monthYearText: messagesBundle['epi.datepicker.monthYearText'] || 'Choose a month (Control+Up/Down to move years)',
    /**
     * @cfg {Number} startDay
     * Day index at which the week should begin, 0-based (defaults to 0, which is Sunday)
     *
     * If start day is configured set it, otherwise use default
     */
    startDay: Ext.num(dimensionsBundle['epidemiological.day.start'], 0),
    /**
     * @cfg {String/Object} autoCreate <p>A {@link Ext.DomHelper DomHelper} element spec, or true for a default
     * element spec. Used to create the {@link Ext.Component#getEl Element} which will encapsulate this Component.
     * See <tt>{@link Ext.Component#autoEl autoEl}</tt> for details.  Defaults to:</p>
     * <pre><code>{tag: 'input', type: 'text', size: '20', autocomplete: 'off'}</code></pre>
     */
    defaultAutoCreate: {tag: 'input', type: 'text', size: '20', autocomplete: 'off'},

    /**
     * By default uses the getWeekOfYear function, specify new function if needed.
     */
    calculateEpiWeek: function (date) {
        if (dimensionsBundle['use.cdc.epiweek'] == 1) {
            return (date ? date.getEpiWeek() : null);
        }
        return (date ? date.getWeekOfYear() : null);
    },

    /**
     * @cfg {Number} selectedEpiWeek
     * The selected epidemiological week
     */
    EpiWeek: null,

    getEpiWeek: function () {
        return this.EpiWeek;
    },
    setEpiWeek: function (w) {
        if (w) {
            this.EpiWeek = w;
            this.highlightSelectedEpiWeek();
        }
    },
    highlightSelectedEpiWeek: function () {
        this.cells.removeClass('x-date-epi-week-selected');

        this.cells.each(function (c) {
            if (this.EpiWeek) {
                if (c.dom.epi == this.EpiWeek) {
                    c.addClass('x-date-epi-week-selected');
                }
            } else {
                return false;
            }
        }, this);
    },
    onRender: function (container, position) {
        var m = [
                '<table cellspacing="0">',
                '<tr><td class="x-date-left"><a href="#" title="', this.prevText , '">&#160;</a></td><td class="x-date-middle" align="center"></td><td class="x-date-right"><a href="#" title="', this.nextText , '">&#160;</a></td></tr>',
                '<tr><td colspan="3"><table class="x-date-inner" cellspacing="0"><thead><tr>'],
            dn = this.dayNames,
            i;
        m.push('<th><span>', 'E.W.', '</span></th>');
        for (i = 0; i < 7; i++) {
            var d = this.startDay + i;
            if (d > 6) {
                d = d - 7;
            }
            m.push('<th><span>', dn[d].substr(0, 1), '</span></th>');
        }
        m[m.length] =
            '</tr></thead><tbody><tr><td><a href="#" hidefocus="on" class="x-date-epi" tabIndex="1"><em><span></span></em></a></td>';
        m[m.length] = '';
        for (i = 0; i < 42; i++) {
            if (i % 7 === 0 && i !== 0) {
                m[m.length] =
                    '</tr><tr><td><a href="#" hidefocus="on" class="x-date-epi" tabIndex="1"><em><span></span></em></a></td>';
            }
            m[m.length] =
                '<td><a href="#" hidefocus="on" class="x-date-date" tabIndex="1"><em><span></span></em></a></td>';
        }
        m.push('</tr></tbody></table></td></tr>',
            this.showToday ? '<tr><td colspan="3" class="x-date-bottom" align="center"></td></tr>' : '',
            '</table><div class="x-date-mp"></div>');

        var el = document.createElement('div');
        el.className = 'x-date-picker';
        el.innerHTML = m.join('');

        container.dom.insertBefore(el, position);

        this.el = Ext.get(el);
        this.eventEl = Ext.get(el.firstChild);

        this.prevRepeater = new Ext.util.ClickRepeater(this.el.child('td.x-date-left a'), {
            handler: this.showPrevMonth,
            scope: this,
            preventDefault: true,
            stopDefault: true
        });

        this.nextRepeater = new Ext.util.ClickRepeater(this.el.child('td.x-date-right a'), {
            handler: this.showNextMonth,
            scope: this,
            preventDefault: true,
            stopDefault: true
        });

        this.monthPicker = this.el.down('div.x-date-mp');
        this.monthPicker.enableDisplayMode('block');

        this.keyNav = new Ext.KeyNav(this.eventEl, {
            'left': function (e) {
                if (e.ctrlKey) {
                    this.showPrevMonth();
                } else {
                    this.update(this.activeDate.add('d', -1));
                }
            },

            'right': function (e) {
                if (e.ctrlKey) {
                    this.showNextMonth();
                } else {
                    this.update(this.activeDate.add('d', 1));
                }
            },

            'up': function (e) {
                if (e.ctrlKey) {
                    this.showNextYear();
                } else {
                    this.update(this.activeDate.add('d', -8));
                }
            },

            'down': function (e) {
                if (e.ctrlKey) {
                    this.showPrevYear();
                } else {
                    this.update(this.activeDate.add('d', 8));
                }
            },

            'pageUp': function (e) {
                this.showNextMonth();
            },

            'pageDown': function (e) {
                this.showPrevMonth();
            },

            'enter': function (e) {
                e.stopPropagation();
                return true;
            },

            scope: this
        });

        this.el.unselectable();

        this.cells = this.el.select('table.x-date-inner tbody td');
        this.textNodes = this.el.query('table.x-date-inner tbody span');

        this.mbtn = new Ext.Button({
            text: '&#160;',
            tooltip: this.monthYearText,
            renderTo: this.el.child('td.x-date-middle', true)
        });
        this.mbtn.el.child('em').addClass('x-btn-arrow');

        if (this.showToday) {
            this.todayKeyListener = this.eventEl.addKeyListener(Ext.EventObject.SPACE, this.selectToday, this);
            var today = (new Date()).dateFormat(this.format);
            this.todayBtn = new Ext.Button({
                renderTo: this.el.child('td.x-date-bottom', true),
                text: String.format(this.todayText, today),
                tooltip: String.format(this.todayTip, today),
                handler: this.selectToday,
                scope: this
            });
        }
        this.mon(this.eventEl, 'mousewheel', this.handleMouseWheel, this);
        this.mon(this.eventEl, 'click', this.handleDateClick, this, {delegate: 'a.x-date-date'});
        this.mon(this.mbtn, 'click', this.showMonthPicker, this);
        this.onEnable(true);

        if (this.handler) {
            this.handler(this);
        }
    },
    update: function (date, forceRefresh) {

        if (this.rendered) {
            var vd = this.activeDate, vis = this.isVisible();
            this.activeDate = date;
            if (!forceRefresh && vd && this.el) {
                var t = date.getTime();
                if (vd.getMonth() == date.getMonth() && vd.getFullYear() == date.getFullYear()) {
                    this.cells.removeClass('x-date-selected');
                    this.cells.each(function (c) {
                        if (c.dom.firstChild.dateValue == t) {
                            c.addClass('x-date-selected');
                            this.setEpiWeek(c.dom.epi);
                            if (vis) {
                                Ext.fly(c.dom.firstChild).focus(50);
                            }
                            return false;
                        }
                    }, this);
                    return;
                }
            }
            var days = date.getDaysInMonth(),
                firstOfMonth = date.getFirstDateOfMonth(),
                startingPos = firstOfMonth.getDay() - this.startDay;

            if (startingPos < 0) {
                startingPos += 7;
            }
            days += startingPos;

            var pm = date.add('mo', -1),
                prevStart = pm.getDaysInMonth() - startingPos,
                cells = this.cells.elements,
                textEls = this.textNodes,
            // convert everything to numbers so it's fast
                day = 86400000,
                d = (new Date(pm.getFullYear(), pm.getMonth(), prevStart)).clearTime(),
                today = new Date().clearTime().getTime(),
                sel = date.clearTime(true).getTime(),
                min = this.minDate ? this.minDate.clearTime(true) : Number.NEGATIVE_INFINITY,
                max = this.maxDate ? this.maxDate.clearTime(true) : Number.POSITIVE_INFINITY,
                ddMatch = this.disabledDatesRE,
                ddText = this.disabledDatesText,
                ddays = this.disabledDays ? this.disabledDays.join('') : false,
                ddaysText = this.disabledDaysText,
                format = this.format;

            if (this.showToday) {
                var td = new Date().clearTime(),
                    disable = (td < min || td > max ||
                        (ddMatch && format && ddMatch.test(td.dateFormat(format))) ||
                        (ddays && ddays.indexOf(td.getDay()) != -1));

                if (!this.disabled) {
                    this.todayBtn.setDisabled(disable);
                    this.todayKeyListener[disable ? 'disable' : 'enable']();
                }
            }

            var setCellClass = function (cal, cell) {
                cell.title = '';
                var t = d.getTime();
                cell.firstChild.dateValue = t;
                if (t == today) {
                    cell.className += ' x-date-today';
                    cell.title = cal.todayText;
                }
                if (t == sel) {
                    cal.setEpiWeek(cell.epi);
                    cell.className += ' x-date-selected';
                    if (vis) {
                        Ext.fly(cell.firstChild).focus(50);
                    }
                }

                // disabling
                if (t < min) {
                    cell.className = ' x-date-disabled';
                    cell.title = cal.minText;
                    return;
                }
                if (t > max) {
                    cell.className = ' x-date-disabled';
                    cell.title = cal.maxText;
                    return;
                }
                if (ddays) {
                    if (ddays.indexOf(d.getDay()) != -1) {
                        cell.title = ddaysText;
                        cell.className = ' x-date-disabled';
                    }
                }
                if (ddMatch && format) {
                    var fvalue = d.dateFormat(format);
                    if (ddMatch.test(fvalue)) {
                        cell.title = ddText.replace('%0', fvalue);
                        cell.className = ' x-date-disabled';
                    }
                }
            };

            var epiWeekStart = new Date(d);

            var epiOffset = 0;
            var setEPIWeekCell = function (cal, cell, textEl) {
                epiWeekStart.setDate(epiWeekStart.getDate() + 7);
                cell.title = 'Epidemiological Week';
                cell.className = ' x-date-epi-week';
                textEl.innerHTML = cal.calculateEpiWeek(epiWeekStart);
                epiOffset++;
            };

            var i = 0;
            for (; (i - epiOffset) < startingPos; i++) {
                cells[i].epi = this.calculateEpiWeek(epiWeekStart);
                if (i % 8 === 0) {
                    setEPIWeekCell(this, cells[i], textEls[i]);
                } else {
                    textEls[i].innerHTML = (++prevStart);
                    d.setDate(d.getDate() + 1);
                    cells[i].className = 'x-date-prevday';
                    setCellClass(this, cells[i]);
                }
            }
            for (; (i - epiOffset) < days; i++) {
                cells[i].epi = this.calculateEpiWeek(epiWeekStart);
                if (i % 8 === 0) {
                    setEPIWeekCell(this, cells[i], textEls[i]);
                } else {
                    var intDay = (i - epiOffset) - startingPos + 1;
                    textEls[i].innerHTML = (intDay);
                    d.setDate(d.getDate() + 1);
                    cells[i].className = 'x-date-active';
                    setCellClass(this, cells[i]);
                }
            }
            var extraDays = 0;
            for (; (i - epiOffset) < 42; i++) {
                cells[i].epi = this.calculateEpiWeek(epiWeekStart);
                if (i % 8 === 0) {
                    setEPIWeekCell(this, cells[i], textEls[i]);
                } else {
                    textEls[i].innerHTML = (++extraDays);
                    d.setDate(d.getDate() + 1);
                    cells[i].className = 'x-date-nextday';
                    setCellClass(this, cells[i]);
                }
            }

            this.mbtn.setText(this.monthNames[date.getMonth()] + ' ' + date.getFullYear());

            this.highlightSelectedEpiWeek();

            if (!this.internalRender) {
                var main = this.el.dom.firstChild,
                    w = main.offsetWidth;
                this.el.setWidth(w + this.el.getBorderWidth('lr'));
                Ext.fly(main).setWidth(w);
                this.internalRender = true;
                // opera does not respect the auto grow header center column
                // then, after it gets a width opera refuses to recalculate
                // without a second pass
                if (Ext.isOpera && !this.secondPass) {
                    main.rows[0].cells[1].style.width =
                        (w - (main.rows[0].cells[0].offsetWidth + main.rows[0].cells[2].offsetWidth)) + 'px';
                    this.secondPass = true;
                    this.update.defer(10, this, [date]);
                }
            }
        }
    }
});

Ext.reg('epiDatePicker', Ext.ux.EpidemiologicalDatePicker);
