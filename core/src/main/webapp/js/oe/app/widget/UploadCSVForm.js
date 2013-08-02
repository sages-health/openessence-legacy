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
 
Ext.ns('OE');

OE.uploadCSVForm = function(configuration){
	var uploadForm = new Ext.form.FormPanel({
		fileUpload: true,
		itemId : 'uploadForm',
		headerCfg: {'Content-type':'multipart/form-data'},
		frame : true,
		bodyStyle: 'padding: 10px 10px 10px 10px;',
		defaults : {
			anchor: '95%',
			allowBlank : false,
			labelWidth : 75,
			msgTarget: 'side'
		},
		items : [ {
			xtype : 'fileuploadfield',
			emptyText : messagesBundle['uploadCSVForm.file.qtip'] || 'Select a CSV file to import data',
			fieldLabel : messagesBundle['uploadCSVForm.file'] || 'CSV File',
			name : 'file',
			buttonText :  messagesBundle['input.datasource.default.Browse'] || 'Browse...'
		}, {
			xtype : 'textfield',
			emptyText : messagesBundle['uploadCSVForm.delimiter.qtip'] || 'Select a delimiter',
			fieldLabel : messagesBundle['uploadCSVForm.delimiter'] || 'Delimiter',
			name : 'delimiter',
			value : configuration.delimiter || ','
		}, {
			xtype : 'textfield',
			emptyText : messagesBundle['uploadCSVForm.qualifier.qtip'] || 'Select a qualifier',
			fieldLabel : messagesBundle['uploadCSVForm.qualifier'] || 'Qualifier',
			name : 'qualifier',
			value : configuration.qualifier || '"'
		}, {
			xtype : 'checkbox',
			emptyText : messagesBundle['uploadCSVForm.headerRow.qtip'] || 'Check to remove the first row',
			fieldLabel : messagesBundle['uploadCSVForm.headerRow'] || 'Remove Header?',
			name : 'headerRow',
			checked : configuration.headerRow || true
		}]
	});
	
	var win = new Ext.Window({
		title : messagesBundle['input.datasource.default.uploadTitle'] || 'Upload Data',
		layout : 'fit',
		width : 450,
		height : 190,
		modal: true,
		items: [ uploadForm ],
		buttons: [
			{
				text: messagesBundle['input.datasource.default.preview'] || 'Preview',
				handler : function(button) {
					var form = uploadForm.getForm();
	
					if (form.isValid()) {
						try {
							form.submit({
								url : '../../oe/input/importCSV',
								params: {dsId: configuration.dataSource, fields: configuration.fields.toString(), 
									rowsToSkip :  form.getFieldValues().headerRow ? 1 : 0 , 
									numRowsToRead : configuration.numRowsToRead},
								waitMsg: messagesBundle['input.datasource.default.uploadWaitMessage'] || 'Uploading the file...',
								failure: function(form, action) {
									var response = Ext.decode(action.response.responseText);
									Ext.Msg.show({
										buttons: Ext.Msg.OK,
										icon: Ext.Msg.ERROR,
										title: 'Error',
										msg: response.message,
										fn: function() {
											win.close();
										}
									});
								},
								success: function(form, action) {
									var previewCallback = function(){
										win.close();
										configuration.successCallback(response);
									};
									var response = Ext.decode(action.response.responseText);
									var config = {fields: configuration.fields, 
									        fieldNames: configuration.fieldNames,
											data: response.rows,
											previewCallback : previewCallback };
									var previewWindow = OE.PreviewGrid(config);
									
									previewWindow.show();
									
								}
							});
						} catch (e) {
							win.close();
						}
					}
				}
			}, {
				text : messagesBundle['button.reset']|| 'Reset',
				handler : function() {
					uploadForm.getForm().reset();
				}
			} ]
		});
	return win;
	
};
//
OE.PreviewGrid = function(configuration){
	
	var cols =[];
	var i = 0;
	 
	for(i=0; i < configuration.fieldNames.length; i++){
		cols.push({
			header : configuration.fieldNames[i],
			dataIndex : configuration.fields[i],
			id : configuration.fields[i],
			width : 100,
			sortable : true
		});
	}
	
	var gridPanel = new Ext.grid.GridPanel({
		store : new Ext.data.JsonStore({
			idIndex : 0,
			fields : configuration.fields,
			root : 'data',
//			sortInfo: {field: OE.util.getStringValue(dimensionMetadata.sortcolumn, "recId"), direction: OE.util.getStringValue(dimensionMetadata.sortorder, 'DESC')},
			data :	configuration
		}),
        columns: cols,
        stripeRows: true,
        height: 350,
        width: 600,
        title: 'Preview',
        // config options for stateful behavior
        stateful: true,
        stateId: 'grid'
    });
	
	var win = new Ext.Window({
		title : messagesBundle['input.datasource.default.Preview'] || 'Preview',
		layout : 'fit',
		width : 610,
		height : 360,
		modal: true,
		items: [ gridPanel],
		buttons: [
			{
				text: messagesBundle['input.datasource.default.Upload'] || 'Upload',
                handler : function(button) {
                	configuration.previewCallback();
                	win.close();
				}
			}, {
				text : messagesBundle['input.datasource.default.cancel'] || 'Cancel',
				handler : function() {
					win.close();
				}
			} ]
		});
	return win;
	
};