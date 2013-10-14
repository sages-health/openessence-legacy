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

Ext.namespace("OE.util");

/**
 * Formats date to (m-d-Y), ex: 02-22-2011
 * @deprecated Use Moment.js's localized date formats, e.g. moment().format('L'), which allows the client to render
 * dates according to the user's locale.
 */
OE.util.defaultDateFormat = (function () {
    var defaultFormat = 'm-d-Y';

    try {
        return messagesBundle['default.date.format'] || defaultFormat;
    } catch (e) {
        // dimensionsBundle isn't defined on login page
        return defaultFormat;
    }
})();

/**
 *
 * @deprecated Use Moment.js, e.g. moment(date).format('L')
 */
OE.util.renderDate = function (d, f) {
    var date = new Date(d);
    if (Ext.isDate(date)) {
        if (isNaN(date.getTime())) {
            // invalid date, display empty string instead of NaN/NaN/NaN
            return '';
        } else {
            return date.format(f ? f : OE.util.defaultDateFormat);
        }
    } else {
        return '';
    }
};

OE.util.renderBooleanAsTernary = function (v, overrideBundle) {
    if (Ext.isDefined(v)) {
        if (v) {
            return (overrideBundle && overrideBundle['true']) || messagesBundle['default.ternary.boolean.true'] || true;
        } else {
            return (overrideBundle && overrideBundle['false']) || messagesBundle['default.ternary.boolean.false'] || false;
        }
    }
    return (overrideBundle && overrideBundle['null']) || dimensionsBundle['default.ternary.boolean.null'] || null;
};

/**
 * Returns an array of missing epi weeks
 */
OE.util.getMissingReports = function (store) {
    var missingWeeks = [];
    // TODO: getWeekOfYear
    var currentEW;

    if (messagesBundle['use.cdc.epiweek'] == 1) {
        currentEW = new Date().getEpiWeek();
    }
    else {
        currentEW = new Date().getWeekOfYear();
    }

    // TODO: range
    var results = store.query('EpiWeek', currentEW, true, false);
    if (!results || results.length === 0) {
        missingWeeks[0] = currentEW;
    }

    return missingWeeks;
};

/**
 * Returns html to display missing weeks
 */
OE.util.getNoReportsItems = function (weeks) {
    var reports = [];

    reports.push('<ul>');
    if (weeks && weeks.length > 0) {
        for (var index = 0; index < weeks.length; index++) {
            reports.push('<li>');
            reports.push(messagesBundle['input.reports.form.noReports']);
            reports.push(' ');
            reports.push(weeks[index]);
            reports.push('</li>');
        }
    }
    reports.push('</ul>');

    return reports.join('');
};

/**
 * Returns true if object has no properties
 */
OE.util.isEmptyObject = function (obj) {
    for (var key in obj) {
        if (obj.hasOwnProperty(key)) {
            return false;
        }
    }
    return true;
};

/**
 * Displays a confirmation message given the report id
 */
OE.util.reportConfirmationDialog = function (id) {
    if (id) {
        Ext.MessageBox.show({
            title: messagesBundle['input.datasource.confirmation'],
            msg: String.format(messagesBundle['input.datasource.confirmation.message'], id),
            buttons: Ext.MessageBox.OK,
            icon: Ext.MessageBox.INFO
        });
    }
};

/**
 * Function to update forms titles and flag when changes exist in form
 */
OE.util.updateFormTitle = function (form, id, attributes) {
    var title = [];
    var dirty = (form.getForm().isDirty() ? '*' : '');

    // Displays: a combination of '*ID: attributes:...'
    title.push((id ? id : messagesBundle['input.reports.new']));

    if (attributes) {
        for (var index = 0; index < attributes.length; index++) {
            var attribute = attributes[index];
            if (attribute) {
                title.push(attribute);
            }
        }
//        title = title.concat(attributes);
    }

    form.setTitle(dirty + title.join(': '));
};

/**
 * Returns an epidemiological configuration with week and year.
 */
OE.util.getEpiWeek = function (date) {

    // ExtJS date format to get week from date
    // W     ISO-8601 week number of year, weeks starting on Monday                    01 to 53
    // o     ISO-8601 year number (identical to (Y), but if the ISO week number (W)    Examples: 1998 or 2004
    //       belongs to the previous or next year, that year is used instead)
    return {week: date.format('W'), year: date.format('o')};
};

/**
 * Evaluates boolean parameters for (true or 'true')
 */
OE.util.getBooleanValue = function (parameter, defaultTo) {
    // If the parameter has been specified...
    var returnValue = defaultTo;

    if (parameter === true || parameter === 'true') {
        returnValue = true;
    } else if (parameter === false || parameter === 'false') {
        returnValue = false;
    }

    return returnValue;
};

/**
 * Evaluates parameter for undefined
 */
OE.util.getStringValue = function (parameter, defaultTo) {
    var returnValue = defaultTo;

    if (Ext.isDefined(parameter)) {
        returnValue = parameter;
    }

    return returnValue;
};

/**
 * Attempts to interpret supplied number value, if result is NaN returns default
 */
OE.util.getNumberValue = function (parameter, defaultTo) {
    var returnValue = defaultTo;

    var number = Number(parameter);
    if (!isNaN(number)) {
        returnValue = number;
    }

    return returnValue;
};

/**
 * Creates a button using the provided image and provided text tool tip
 */
OE.util.getIndicatorIconButton = function (icon, text) {
    return new Ext.Button({
        icon: icon,
        cls: 'help-button',
        tooltip: text,
        handleMouseEvents: false
    });
};

/**
 * Attempts to load label from dimensions bundle using a qualified (data source name) "dot" dimension name, then
 * will try to load from dimensions bundle just using the dimension name, finally will just use the dimension name.
 */
OE.util.getDimensionName = function (dataSource, dimensionName) {
    return (dimensionsBundle[dataSource + '.' + dimensionName] || dimensionsBundle[dimensionName] || dimensionName);
};

OE.util.getEmptyText = function (name) {
    return (messagesBundle['query.select'] + ' ' + name + '...');
};

/**
 * Returns date with time cleared
 */
OE.util.getDate = function (delta) {
    var force = dimensionsBundle['date.forceToday'];
    var d = force ? new Date(Date.parse(force)) : new Date();
    var inc = delta ? delta : 0;
    d.setDate(d.getDate() - inc);
    return new Date(d).clearTime();
};

OE.util.getUrl = function (path) {
    return OE.contextPath + OE.servletPath + path;
};

