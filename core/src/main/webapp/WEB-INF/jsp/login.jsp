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

<%-- probably impossible to perform XSS by hijacking the contextPath, but better safe than sorry --%>
<c:set var="contextPath" value="${fn:escapeXml(pageContext.servletContext.contextPath)}"/>
<c:set var="locale" value="${pageContext.response.locale}"/>
<c:set var="locale" value="${fn:escapeXml(locale)}"/>

<!DOCTYPE html>
<html>
<head>
    <!-- The X-UA-Compatible header is only supported starting with Windows Internet Explorer 8.  It must appear in the Web -->
    <!-- page's header (the HEAD section) before all other elements, except for the title element and other meta elements.  -->
    <!--     http://stackoverflow.com/questions/12222832/gwt-ie9-emulate-ie8                     -->
    <!--     http://msdn.microsoft.com/en-us/library/cc288325%28v=vs.85%29.aspx#SetMode          -->
    <meta http-equiv="X-UA-Compatible" content="IE=8">

    <title><spring:message code="app.title" text="app.title"/></title>
    <link rel="shortcut icon" href="${contextPath}/images/openessence.ico"/>

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
    <link type="text/css" rel="stylesheet" href="${contextPath}/css/login.css"/>
</head>

<body>

<script type="text/javascript" src="${contextPath}/js/ext-3.0.3/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="${contextPath}/js/ext-3.0.3/ext-all.js"></script>
<%--<script type="text/javascript" src="${contextPath}/js/ext-3.0.3/ext-all-debug.js"></script>--%>

<%-- localized Ext, very important to use locale of response, not request, since this is locale Spring gives you,
     not what the browser says it supports --%>
<script type="text/javascript" src="${contextPath}/js/ext-3.0.3/src/locale/ext-lang-${locale}.js"></script>

<%-- OpenESSENCE JS used by every page --%>
<script type="text/javascript">
    Ext.namespace("OE.login", "OE.context");
    OE.context.root = '${contextPath}';
    OE.servletPath = '/oe';
    var SELECTED_LOCALE = '${locale}';
    Ext.USE_NATIVE_JSON = true;

    <%-- don't rely on modifying builtin objects, Ext 4 wised up and stopped that --%>
    if (!Ext.String) {
        Ext.String = String;
    }
</script>
<script type="text/javascript" src="${contextPath}/js/oe/app/plugin/extFixOverrides.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/login.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/util/oeUtils.js"></script>
<script type="text/javascript" src="${contextPath}/js/oe/app/widget/Header.js"></script>
</body>
</html> 


