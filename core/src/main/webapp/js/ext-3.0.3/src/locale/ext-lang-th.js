/*!
 * Ext JS Library 3.4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */
/**
 * List compiled by KillerNay on the extjs.com forums.
 * Thank you KillerNay!
 *
 * Thailand Translations
 */

Ext.UpdateManager.defaults.indicatorText = '<div class="loading-indicator">ยกร“ร…ร‘ยงรขรร…ลฝ...</div>';

if(Ext.View){
  Ext.View.prototype.emptyText = "";
}

if(Ext.grid.GridPanel){
  Ext.grid.GridPanel.prototype.ddText = "{0} ร ร…ร—รยกรกร…รฉรยทร‘รฉยงรรลฝรกยถร";
}

if(Ext.TabPanelItem){
  Ext.TabPanelItem.prototype.closeText = "ยปร”ลฝรกยทรงยบยนร•รฉ";
}

if(Ext.form.Field){
  Ext.form.Field.prototype.invalidText = "โฌรจร’ยขรยงยชรจรยงยนร•รฉรครรจยถรยกยตรฉรยง";
}

if(Ext.LoadMask){
  Ext.LoadMask.prototype.msg = "ยกร“ร…ร‘ยงรขรร…ลฝ...";
}

Date.monthNames = [
  "รยกรร’โฌร",
  "ยกรรลธร’ร“ลธร‘ยนลพรฌ",
  "รร•ยนร’โฌร",
  "ร รรร’รยน",
  "ลธรรร€ร’โฌร",
  "รร”ยถรยนร’รยน",
  "ยกรยกยฏร’โฌร",
  "รร”ยงรร’โฌร",
  "ยกร‘ยนรร’รยน",
  "ยตรร…ร’โฌร",
  "ลธรรลกร”ยกร’รยน",
  "ลพร‘ยนรร’โฌร"
];

Date.getShortMonthName = function(month) {
  return Date.monthNames[month].substring(0, 3);
};

Date.monthNumbers = {
  "รโฌ" : 0,
  "ยกลธ" : 1,
  "รร•โฌ" : 2,
  "ร รร" : 3,
  "ลธโฌ" : 4,
  "รร”ร" : 5,
  "ยกโฌ" : 6,
  "รโฌ" : 7,
  "ยกร" : 8,
  "ยตโฌ" : 9,
  "ลธร" : 10,
  "ลพโฌ" : 11
};

Date.getMonthNumber = function(name) {
  return Date.monthNumbers[name.substring(0, 1).toUpperCase() + name.substring(1, 3).toLowerCase()];
};

Date.dayNames = [
  "รร’ยทร”ยตรรฌ",
  "ลกร‘ยนยทรรฌ",
  "รร‘ยงโฌร’ร",
  "ลธรร—ลพ",
  "ลธรรร‘รยบลฝร•",
  "รรยกรรฌ",
  "ร รร’รรฌ"
];

Date.getShortDayName = function(day) {
  return Date.dayNames[day].substring(0, 3);
};

if(Ext.MessageBox){
  Ext.MessageBox.buttonText = {
    ok     : "ยตยกร…ยง",
    cancel : "รยกร ร…ร”ยก",
    yes    : "รฃยชรจ",
    no     : "รครรจรฃยชรจ"
  };
}

if(Ext.util.Format){
  Ext.util.Format.date = function(v, format){
    if(!v) return "";
    if(!(v instanceof Date)) v = new Date(Date.parse(v));
    return v.dateFormat(format || "m/d/Y");
  };
}

if(Ext.DatePicker){
  Ext.apply(Ext.DatePicker.prototype, {
    todayText         : "รร‘ยนยนร•รฉ",
    minText           : "This date is before the minimum date",
    maxText           : "This date is after the maximum date",
    disabledDaysText  : "",
    disabledDatesText : "",
    monthNames        : Date.monthNames,
    dayNames          : Date.dayNames,
    nextText          : 'ร ลฝร—รยนยถร‘ลฝรคยป (Control+Right)',
    prevText          : 'ร ลฝร—รยนยกรจรยนรยนรฉร’ (Control+Left)',
    monthYearText     : 'ร ร…ร—รยกร ลฝร—รยน (Control+Up/Down to move years)',
    todayTip          : "{0} (Spacebar)",
    format            : "m/d/y",
    okText            : "&#160;ยตยกร…ยง&#160;",
    cancelText        : "รยกร ร…ร”ยก",
    startDay          : 0
  });
}

if(Ext.PagingToolbar){
  Ext.apply(Ext.PagingToolbar.prototype, {
    beforePageText : "รยนรฉร’",
    afterPageText  : "of {0}",
    firstText      : "รยนรฉร’รกรยก",
    prevText       : "ยกรจรยนรยนรฉร’",
    nextText       : "ยถร‘ลฝรคยป",
    lastText       : "รยนรฉร’รรลฝยทรฉร’ร",
    refreshText    : "รร•ร ยฟรยช",
    displayMsg     : "ยกร“ร…ร‘ยงรกรลฝยง {0} - {1} ลกร’ยก {2}",
    emptyMsg       : 'รครรจรร•ยขรฉรรรร…รกรลฝยง'
  });
}

if(Ext.form.TextField){
  Ext.apply(Ext.form.TextField.prototype, {
    minLengthText : "The minimum length for this field is {0}",
    maxLengthText : "The maximum length for this field is {0}",
    blankText     : "ฟิลด์นี้จะต้อง",
    regexText     : "",
    emptyText     : null
  });
}

if(Ext.form.NumberField){
  Ext.apply(Ext.form.NumberField.prototype, {
    minText : "The minimum value for this field is {0}",
    maxText : "The maximum value for this field is {0}",
    nanText : "{0} is not a valid number"
  });
}

if(Ext.form.DateField){
  Ext.apply(Ext.form.DateField.prototype, {
    disabledDaysText  : "ยปร”ลฝ",
    disabledDatesText : "ยปร”ลฝ",
    minText           : "The date in this field must be after {0}",
    maxText           : "The date in this field must be before {0}",
    invalidText       : "{0} is not a valid date - it must be in the format {1}",
    format            : "m/d/y",
    altFormats        : "m/d/Y|m-d-y|m-d-Y|m/d|m-d|md|mdy|mdY|d|Y-m-d",
    startDay          : 0
  });
}

if(Ext.form.ComboBox){
  Ext.apply(Ext.form.ComboBox.prototype, {
    loadingText       : "ยกร“ร…ร‘ยงรขรร…ลฝ...",
    valueNotFoundText : undefined
  });
}

if(Ext.form.VTypes){
  Ext.apply(Ext.form.VTypes, {
    emailText    : 'This field should be an e-mail address in the format "user@example.com"',
    urlText      : 'This field should be a URL in the format "http:/'+'/www.example.com"',
    alphaText    : 'This field should only contain letters and _',
    alphanumText : 'This field should only contain letters, numbers and _'
  });
}

if(Ext.form.HtmlEditor){
  Ext.apply(Ext.form.HtmlEditor.prototype, {
    createLinkText : 'Please enter the URL for the link:',
    buttonTips : {
      bold : {
        title: 'Bold (Ctrl+B)',
        text: 'Make the selected text bold.',
        cls: 'x-html-editor-tip'
      },
      italic : {
        title: 'Italic (Ctrl+I)',
        text: 'Make the selected text italic.',
        cls: 'x-html-editor-tip'
      },
      underline : {
        title: 'Underline (Ctrl+U)',
        text: 'Underline the selected text.',
        cls: 'x-html-editor-tip'
      },
      increasefontsize : {
        title: 'Grow Text',
        text: 'Increase the font size.',
        cls: 'x-html-editor-tip'
      },
      decreasefontsize : {
        title: 'Shrink Text',
        text: 'Decrease the font size.',
        cls: 'x-html-editor-tip'
      },
      backcolor : {
        title: 'Text Highlight Color',
        text: 'Change the background color of the selected text.',
        cls: 'x-html-editor-tip'
      },
      forecolor : {
        title: 'Font Color',
        text: 'Change the color of the selected text.',
        cls: 'x-html-editor-tip'
      },
      justifyleft : {
        title: 'Align Text Left',
        text: 'Align text to the left.',
        cls: 'x-html-editor-tip'
      },
      justifycenter : {
        title: 'Center Text',
        text: 'Center text in the editor.',
        cls: 'x-html-editor-tip'
      },
      justifyright : {
        title: 'Align Text Right',
        text: 'Align text to the right.',
        cls: 'x-html-editor-tip'
      },
      insertunorderedlist : {
        title: 'Bullet List',
        text: 'Start a bulleted list.',
        cls: 'x-html-editor-tip'
      },
      insertorderedlist : {
        title: 'Numbered List',
        text: 'Start a numbered list.',
        cls: 'x-html-editor-tip'
      },
      createlink : {
        title: 'Hyperlink',
        text: 'Make the selected text a hyperlink.',
        cls: 'x-html-editor-tip'
      },
      sourceedit : {
        title: 'Source Edit',
        text: 'Switch to source editing mode.',
        cls: 'x-html-editor-tip'
      }
    }
  });
}

if(Ext.grid.GridView){
  Ext.apply(Ext.grid.GridView.prototype, {
    sortAscText  : "Sort Ascending",
    sortDescText : "Sort Descending",
    lockText     : "Lock Column",
    unlockText   : "Unlock Column",
    columnsText  : "Columns"
  });
}

if(Ext.grid.GroupingView){
  Ext.apply(Ext.grid.GroupingView.prototype, {
    emptyGroupText : '(None)',
    groupByText    : 'Group By This Field',
    showGroupsText : 'Show in Groups'
  });
}

if(Ext.grid.PropertyColumnModel){
  Ext.apply(Ext.grid.PropertyColumnModel.prototype, {
    nameText   : "Name",
    valueText  : "Value",
    dateFormat : "m/j/Y"
  });
}

if(Ext.layout.BorderLayout && Ext.layout.BorderLayout.SplitRegion){
  Ext.apply(Ext.layout.BorderLayout.SplitRegion.prototype, {
    splitTip            : "Drag to resize.",
    collapsibleSplitTip : "Drag to resize. Double click to hide."
  });
}
