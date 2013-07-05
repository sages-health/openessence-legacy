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

Ext.namespace("OE.report.datasource");

/**
 * Default data source report function to build/populate main panel.
 * 
 * Requires configuration (consisting of data, data source name, primary keys and a title).
 */
OE.report.datasource.panel = function(configuration) {
    var n = 0;
    var resultsTabPanel = null;
    var queryFormPanel = null;
    
    function populateQueryFormPanel(parameters, expand) {
        queryFormPanel.populate(parameters);
        queryFormPanel.expand(expand ? expand : true);
    };

    function showTimeSeries(parameters) {
        var tab = resultsTabPanel.add(new OE.GraphPanel({
            url: OE.util.getUrl('/ds/' + configuration.dataSource + '/diagrams/time-series'),
            title: parameters.title, // callee handles if this is undefined
            parameters: parameters,
            queryFormCallback: populateQueryFormPanel,
            index: ++n
        }));
        tab.parameters = parameters || {};
        tab.parameters.queryType = 'timeseries';
        resultsTabPanel.setActiveTab(tab);
        resultsTabPanel.setActiveTab(tab);
        queryFormPanel.collapse(true);
    };

    function showPivot(parameters) {
        parameters = Ext.applyIf({
            url: OE.util.getUrl('/report/detailsPivot'),
            autoTitle: parameters.title,
            gridClass: Ext.grid.GridPanel,
            gridExtraConfig: {},
            pageSize: -1,
            queryType: 'pivot'
        }, parameters);
        showDetails(parameters);
    };
    
    function showDetails(parameters) {
        Ext.applyIf(parameters, {
            gridClass: Ext.grid.GridPanel
        });

        var tab = resultsTabPanel.add(OE.report.datasource.details.init({
            url:   parameters.url,
        	title: parameters.title,
            autoTitle: parameters.autoTitle,
        	data: configuration.data, 
            dataSource: configuration.dataSource,
            parameters: parameters,
            queryFormCallback: populateQueryFormPanel,
            gridClass: parameters.gridClass,
            gridExtraConfig: parameters.gridExtraConfig,
            pageSize: parameters.pageSize,
            pivot: parameters.pivot,
            index: ++n
        }));
        tab.parameters = parameters || {};
        
        // this is a hack so saved queries know what type of query to save
        // TODO move to more object-oriented solution
        tab.parameters.queryType = parameters.queryType || 'details';
        
        resultsTabPanel.setActiveTab(tab);
        queryFormPanel.collapse(true);
        
        return tab;
    };
    
    function showCharts(parameters) {
        var tab = resultsTabPanel.add(new OE.ChartPanel({
            url: OE.util.getUrl('/ds/' + configuration.dataSource + '/diagrams/chart'),
            title: parameters.title,
            //Overriding title for charts since they have an ext title
            parameters: Ext.apply(parameters, {title: ""}),
            charts: parameters.charts,
            queryFormCallback: populateQueryFormPanel,
            index: ++n
        }));
        tab.parameters = parameters || {};
        tab.parameters.queryType = 'charts';
        tab.charts = parameters.charts;
        resultsTabPanel.setActiveTab(tab);
        queryFormPanel.collapse(true);
    };
    
    function showMap(parameters) {
    	// don't send useless parameters over the wire
    	parameters = Ext.applyIf({results: [], charts: []}, parameters);
    	delete parameters.results;
    	delete parameters.charts;
    	
    	n++;
        var tab = resultsTabPanel.add(new configuration.mapTabClass({ // use injected MapTab implementation
            title: parameters.title || messagesBundle['panel.map.header'] + ' ' + n,
            getMapData: parameters,
            queryFormCallback: populateQueryFormPanel
        }));
        tab.parameters = parameters || {};
        tab.parameters.queryType = 'map';
        resultsTabPanel.setActiveTab(tab);
        queryFormPanel.collapse(true);
    };
    
    queryFormPanel = new OE.report.ReportForm({            
        data: configuration.data,
        itemId: configuration.itemId,
        dataSource: configuration.dataSource,
        timeseriesCallback: showTimeSeries,
        chartsCallback: showCharts,
        detailsCallback: showDetails,
        mapCallback: showMap,
        pivotCallback: showPivot
    });
    
    var savedQueryPanel = (function() {
    	var savedQueryConfig = null;
    	try {
    		savedQueryConfig = configuration.data.meta.form.savedQueries;
    	} catch (e) {
    		// null pointer
    		savedQueryConfig = {};
    	}
    	savedQueryConfig = Ext.apply(savedQueryConfig, configuration);
    	
    	return new OE.SavedQueryPanel(Ext.apply({
        	region: 'north', 
        	queryFormPanel: queryFormPanel
    	}, savedQueryConfig));
    })();
        
    resultsTabPanel = new Ext.TabPanel( {
    	cls: 'reportPanel',
        region: 'center',
        border: false,
        boxMinHeight: 150,
        enableTabScroll: true,
        plain: true,
        defaults: {
        	closable: true
        },
        itemTpl: new Ext.XTemplate(
        		 '<li class="{cls}" id="{id}"><a class="x-tab-strip-close" onclick="return false;"></a>',
        		 '<a class="x-tool x-tool-pin saved-query-pin" onclick="return false;"></a>',
                 '<a class="x-tab-right" href="#" onclick="return false;"><em class="x-tab-left">',
                 '<span class="x-tab-strip-inner"><span class="x-tab-strip-text {iconCls}" style="padding-left: 15px;">{text}</span></span>',
                 '</em></a></li>'
        ),
        listeners: {
            'tabchange': function(tabPanel, tab) {
            	if (tab.parameters) {
            		queryFormPanel.populate(tab.parameters);
            	}
            },
            'add': function(tabPanel, tab) {
                //WARNING: This listener is triggered every time a child is added to the tab

            	var header = tab.ownerCt.header;

            	// check for header since add is fired when ANY component is added, not just a tab
            	if (header) {
            		var tabHeader = Ext.get(tab.tabEl);
            		var pinEl = tabHeader.child('.saved-query-pin');

            		// set width to compensate for pin tool
            		tabHeader.setWidth(tabHeader.getWidth() + 20);

            		// register save query tooltip
            		Ext.QuickTips.register({
            			target: pinEl,
            			text: messagesBundle['query.save.tooltip']
            		});

            		// save query on click
            		pinEl.on('click', function() {
            			tabPanel.setActiveTab(tab);

            			if (tab.parameters) {
            				var saveQueryWindow = null;

                    		// form submit function
                    		var submit = function() {
                    			var queryName = saveQueryWindow.getComponent(0).getComponent(0).getValue();
                    			var rollingDateWindow = saveQueryWindow.getComponent(0).getComponent(1).getValue();

                    			OE.data.doAjaxRestricted({
                    				url: OE.util.getUrl('/input/add'),
        							method: 'POST',
        							params: {
        								dsId: 'SavedQuery_Entry',
        								QueryName: queryName,
                                        QueryType: tab.parameters.queryType,
        								DataSource: tab.parameters.dsId,
        								Parameters: Ext.encode({
        									dataSource: tab.parameters.dsId,
        									queryType: tab.parameters.queryType,
        									results: tab.parameters.results,
        									filters: rollingDateWindow ? queryFormPanel.convertDateFiltersToLength() : queryFormPanel.getFilters(),
        									charts: tab.charts,
                                            pivot: tab.parameters.pivot
        								})
        							},
        							onJsonSuccess: function(response) {
        								tab.setTitle(queryName);
        								savedQueryPanel.reload();
        							}
        						});

            					saveQueryWindow.close();
                    		};

                    		saveQueryWindow = new Ext.Window({
                    			width: 400,
                    			title: messagesBundle['query.save.saveThisQuery'],
                    			padding: 20,
                    			bodyCssClass: 'x-panel-body',
                    			items: {
                    				xtype: 'form',
                    				border: false,
                    				labelWidth: 150,
                    				items: [{
                    					fieldLabel: messagesBundle['query.save.name'],
                    					xtype: 'textfield',
                    					width: '95%',
                    					listeners: {
                    						specialKey: function(field, e) {
                    							if (e.getKey() == e.ENTER) {
                    								submit.call(this);
                    							}
                    						}
                    					}
                    				}, {
                    					fieldLabel: messagesBundle['query.save.rollingDateWindow'],
                    					xtype: 'checkbox',
                    					itemId: 'rollingDateWindow',
                    					checked: true
                    				}]
                    			},

                    			// could also put these buttons on the form,
                    			// but they look much better on the window
                    			buttons: [{
                					text: messagesBundle['input.datasource.default.save'],
                					handler: submit
                				}, {
                					text: messagesBundle['input.datasource.default.cancel'],
                					handler: function() {
                						// delay referencing close until saveQueryWindow is initialized
                    					saveQueryWindow.close();
                    				}
                				}]
                    		});

                    		saveQueryWindow.show();
            			}
            		});
            	}
            }
        }
    });
    
    var reportTab = {
        itemId: configuration.itemId,
        title: configuration.title,
        layout: 'border',
        cls: 'reportPanel',
        closable: true,
        border: false,
        items: [savedQueryPanel, {
        	xtype: 'panel',
        	region: 'center',
        	layout: 'border',
        	autoScroll: true,
        	items: [queryFormPanel, resultsTabPanel]
        }],
        drillinCallback: showDetails,
        listeners: {
        	render: function(me) {
        		// Ext's Element scroll event is only for the entire page body, not elements,
        		// so we have to do it at the DOM level
        		me.getEl().dom.children[0].children[0].onscroll = function() {
        			var mapPanel = me.findByType('oe_mappanel')[0];
        			
        			// scrolling messes up the mouse position on the map, so we have to fix it
        			if (mapPanel) {
        				mapPanel.map.updateSize();
            			
            			// move any popup windows so they remain anchored
            			Ext.iterate(mapPanel.popups, function(featureId, popup) {
            				// only reposition if it is still anchored
            				if (!popup.draggable) {
            					popup.position(); // private method
            				}
            			});
        			}
        		};
        	}
        }
    };

    Ext.getCmp(configuration.destination).add(reportTab).show();
    Ext.getCmp(configuration.destination).doLayout();
};

/**
 * Callback from chart drill in to display details for selected point.
 * This is called by JS written to page in ReportController.
 */
OE.report.datasource.showDetails = function(params) {
	if (!params.filters) {
		// TODO make params have explicit filters
		params.filters = Ext.apply({}, params);
		delete params.filters.accumId;
		delete params.filters.results;
		delete params.filters.dsId;
	}
	
    var mainPanel = Ext.getCmp('tabsPanel').getActiveTab();
    if (mainPanel && mainPanel.drillinCallback) {
    	// Removed "return", it was causing issue when we go from 
    	// time series to data details. 
    	// return mainPanel.drillinCallback(params);
    	mainPanel.drillinCallback(params);
    }
};
