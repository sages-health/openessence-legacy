<%--
  ~ Copyright (c) 2013 The Johns Hopkins University/Applied Physics Laboratory
  ~                             All rights reserved.
  ~
  ~ This material may be used, modified, or reproduced by or for the U.S.
  ~ Government pursuant to the rights granted under the clauses at
  ~ DFARS 252.227-7013/7014 or FAR 52.227-14.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ NO WARRANTY.   THIS MATERIAL IS PROVIDED "AS IS."  JHU/APL DISCLAIMS ALL
  ~ WARRANTIES IN THE MATERIAL, WHETHER EXPRESS OR IMPLIED, INCLUDING (BUT NOT
  ~ LIMITED TO) ANY AND ALL IMPLIED WARRANTIES OF PERFORMANCE,
  ~ MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT OF
  ~ INTELLECTUAL PROPERTY RIGHTS. ANY USER OF THE MATERIAL ASSUMES THE ENTIRE
  ~ RISK AND LIABILITY FOR USING THE MATERIAL.  IN NO EVENT SHALL JHU/APL BE
  ~ LIABLE TO ANY USER OF THE MATERIAL FOR ANY ACTUAL, INDIRECT,
  ~ CONSEQUENTIAL, SPECIAL OR OTHER DAMAGES ARISING FROM THE USE OF, OR
  ~ INABILITY TO USE, THE MATERIAL, INCLUDING, BUT NOT LIMITED TO, ANY DAMAGES
  ~ FOR LOST PROFITS.
  --%>
<%@ page contentType="text/html;charset=UTF-8" trimDirectiveWhitespaces="true" %>
<%@ include file="/WEB-INF/jsp/include.jsp" %>

<%-- prevent XSS --%>
<c:set var="contextPath" value="${fn:escapeXml(pageContext.servletContext.contextPath)}"/>

<%-- convert to string --%>
<c:set var="contextPath" value="${contextPath}"/>

<c:set var="locale" value="${pageContext.response.locale}"/>
<c:set var="locale" value="${fn:escapeXml(locale)}"/>

<%-- client should request all resources through Basic if they request main through Basic --%>
<c:if test="${requestScope['edu.jhuapl.openessence.security.ApiFilter.basic']}">
    <c:set var="contextPath" value="${contextPath}/api"/>
</c:if>

<c:set var="locale" value="${pageContext.response.locale}"/> <%-- convert to string first --%>
<c:set var="locale" value="${fn:escapeXml(locale)}"/>

<c:set var="openLayersPath" value="${contextPath}/js/OpenLayers-2.12"/>

<!DOCTYPE html>
<html>
<head>
    <!-- The X-UA-Compatible header is only supported starting with Windows Internet Explorer 8.  It must appear in the Web -->
    <!-- page's header (the HEAD section) before all other elements, except for the title element and other meta elements.  -->
    <!--     http://stackoverflow.com/questions/12222832/gwt-ie9-emulate-ie8                     -->
    <!--     http://msdn.microsoft.com/en-us/library/cc288325%28v=vs.85%29.aspx#SetMode          -->
    <meta http-equiv="X-UA-Compatible" content="IE=8">

    <title><spring:message code="app.title" text="app.title"/></title>

    <%-- see http://en.wikipedia.org/wiki/Favicon#Accessibility --%>
    <link rel="shortcut icon" href="${contextPath}/images/openessence.ico"/>

    <link type="text/css" rel="stylesheet"
          href="${contextPath}/js/lib/jquery-ui/css/ui-lightness/jquery-ui-1.10.3.custom.min.css"/>
    <link type="text/css" rel="stylesheet" href="${openLayersPath}/theme/default/style.css"/>
    <link type="text/css" rel="stylesheet" href="${contextPath}/js/geoext-0.7/resources/css/geoext-all.css"/>

    <link type="text/css" rel="stylesheet" href="${contextPath}/js/extplugins/Multiselect.css"/>
    <link type="text/css" rel="stylesheet" href="${contextPath}/js/extplugins/superboxselect/superboxselect.css"/>
    <link type="text/css" rel="stylesheet" href="${contextPath}/js/extplugins/Ext.ux.PasswordMeter/passwordmeter.css"/>
    <link type="text/css" rel="stylesheet"
          href="${contextPath}/js/extplugins/Ext.ux.form.FileUploadField/FileUploadField.css"/>
    <link type="text/css" rel="stylesheet" href="${contextPath}/js/extplugins/Ext.ux.ColorField/Ext.ux.ColorField.css"/>
    <link type="text/css" rel="stylesheet" href="${contextPath}/js/lib/pivottable/pivot.css"/>

    <%-- can also pass ?prettyPrint=true to get nicely formatted bundles, although the file size will be a lot larger --%>
    <script type="text/javascript" src="${contextPath}/oe/messages"></script>

    <link type="text/css" rel="stylesheet" href="${contextPath}/js/ext-3.0.3/resources/css/ext-all.css"/>
    <link type="text/css" rel="stylesheet" href="${contextPath}/js/ext-3.0.3/resources/css/xtheme-tp.css"/>
    <link type="text/css" rel="stylesheet" href="${contextPath}/css/openessence.css"/>

    <%-- TODO use conditional loader (Modernizr.load or yepnope.js) --%>
    <!--[if lt IE 9]>
    <script type="text/javascript" src="${contextPath}/js/lib/html5shiv/html5shiv.js"></script>
    <script type="text/javascript" src="${contextPath}/js/lib/augmentjs/augment.min.js"></script>
    <![endif]-->
    <script type="text/javascript" src="${contextPath}/js/lib/console-polyfill/console-polyfill.js"></script>

    <%-- CSS for this page --%>
    <link type="text/css" rel="stylesheet" href="${contextPath}/css/main.css"/>

    <script type="text/javascript" src="${contextPath}/js/lib/requirejs/require.js"></script>
</head>

<body>
<%-- scripts that are inside body are not needed until later, so their loading can be delayed --%>

<script type="text/javascript" src="${contextPath}/js/ext-3.0.3/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="${contextPath}/js/ext-3.0.3/ext-all.js"></script>
<%--<script type="text/javascript" src="${contextPath}/js/ext-3.0.3/ext-all-debug.js"></script>--%>

<%-- localized Ext, very important to use locale of response, not request, since this is locale Spring gives you,
     not what the browser says it supports --%>
<script type="text/javascript" src="${contextPath}/js/ext-3.0.3/src/locale/ext-lang-${locale}.js"></script>

<%-- OpenESSENCE JS used by every page --%>
<script type="text/javascript" src="${contextPath}/js/oe/app/plugin/extFixOverrides.js"></script>
<script type="text/javascript">
    var SELECTED_LOCALE = '${locale}';
    Ext.USE_NATIVE_JSON = true;

    <%-- don't rely on modifying builtin objects, Ext 4 wised up and stopped that --%>
    if (!Ext.String) {
        Ext.String = String;
    }
</script>
<script type="text/javascript" src="${contextPath}/js/oe/app/util/oeUtils.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/Header.js"></script>

<script src="//code.jquery.com/jquery-1.10.2.min.js"></script>
<script src="${contextPath}/js/lib/jquery-ui/js/jquery-ui-1.10.3.custom.min.js"></script>

<script type="text/javascript">
    Ext.namespace("OE.login", "OE.context");
    OE.context.root = '${contextPath}';
    OE.servletPath = '/oe';
    OE.login.username = '<security:authentication property="name"/>';
    OE.login.lastUser = OE.login.username;
</script>

<%-- Mapping extensions, these need to be defined first since later code references them --%>
<script type="text/javascript" src="${openLayersPath}/OpenLayers.js"></script>
<script type="text/javascript" src="${openLayersPath}/lib/OpenLayers/Lang/${locale}.js"></script>
<script type="text/javascript">
    OpenLayers.Lang.setCode('${locale}');
    if (Ext.isIE) {
        OpenLayers.Tile.Image.prototype.maxGetUrlLength = 2048;
    }
</script>
<script type="text/javascript" src="${contextPath}/js/geoext-0.7/script/GeoExt.js"></script>

<%-- EXTENSIONS & PLUGINS --%>
<script type="text/javascript" src="${contextPath}/js/extplugins/DDView.js"></script>
<script type="text/javascript" src="${contextPath}/js/extplugins/Multiselect.js"></script>
<script type="text/javascript" src="${contextPath}/js/extplugins/superboxselect/SuperBoxSelect.js"></script>
<script type="text/javascript" src="${contextPath}/js/extplugins/Ext.ux.PasswordMeter/Ext.ux.PasswordMeter.js"></script>
<script type="text/javascript"
        src="${contextPath}/js/extplugins/Ext.ux.form.FileUploadField/FileUploadField.js"></script>
<script type="text/javascript" src="${contextPath}/js/extplugins/PagingStore.js"></script>
<script type="text/javascript" src="${contextPath}/js/extplugins/Ext.ux.ColorField/Ext.ux.ColorField.js"></script>

<script type="text/javascript" src="${contextPath}/js/oe/app/plugin/collapsedPanelTitle.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/DynamicPagingToolbar.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/EpidemiologicalDatePicker.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/EpidemiologicalDateMenu.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/EpidemiologicalDateField.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/LinkFormLayout.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/LoggedInHeader.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/NavPanel.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/dataSourceGrid.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/SavedQueryPanel.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/ChartPanel.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/Chart.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/GraphPanel.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/ComboColumn.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/GraphConfigTreePanel.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/CategoryGridField.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/EditorGridField.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/MainTabPanel.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/InputTab.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/MapTab.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/WelcomePanel.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/WelcomeItem.js"></script>

<%-- Core viewport and panels --%>
<script type="text/javascript" src="${contextPath}/js/oe/contentTab.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/welcomeTab.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/mainPageBody.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/data.js"></script>

<script type="text/javascript"
        src="${contextPath}/js/oe/dataEntry/defaultDataSourceConfigurationSearchFormPanel.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/dataEntry/defaultDataSourceConfigurationFormPanel.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/dataEntry/defaultDataSourceConfiguration.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/dataEntry/configFormPanel.js"></script>

<script type="text/javascript"
        src="${contextPath}/js/oe/reporting/defaultDataSourceReportTimeSeriesParametersForm.js"></script>
<script type="text/javascript"
        src="${contextPath}/js/oe/reporting/defaultDataSourceReportGroupBySelectionForm.js"></script>
<script type="text/javascript"
        src="${contextPath}/js/oe/reporting/defaultDataSourceReportChartSelectionForm.js"></script>
<script type="text/javascript"
        src="${contextPath}/js/oe/reporting/defaultDataSourceReportPivotSelectionForm.js"></script>
<script type="text/javascript"
        src="${contextPath}/js/oe/reporting/defaultDataSourceReportResultsParameterPanel.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/reporting/defaultDataSourceReportDetailsPanel.js"></script>
<script type="text/javascript"
        src="${contextPath}/js/oe/reporting/defaultDataSourceReportGraphDetailsPanel.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/reporting/defaultDataSourceReportFormPanel.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/reporting/defaultDataSourceReportMap.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/reporting/defaultDataSourceReportPanel.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/reporting/defaultDataSourceReport.js"></script>
</body>
</html>
