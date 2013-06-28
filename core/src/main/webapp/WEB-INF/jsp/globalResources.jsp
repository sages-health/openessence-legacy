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

<%--
	JS and CSS used by every page.
	<%@ include %> this file in the head section of your HTML
--%>

<%@ include file="/WEB-INF/jsp/include.jsp" %>

<%-- probably impossible to perform XSS by hijacking the contextPath, but better safe than sorry --%>
<c:set var="globalContextPath" value="${fn:escapeXml(pageContext.servletContext.contextPath)}"/>
<c:set var="globalLocale" value="${pageContext.response.locale}"/>
<c:set var="globalLocale" value="${fn:escapeXml(globalLocale)}"/>

<%-- see http://en.wikipedia.org/wiki/Favicon#Accessibility --%>
<link rel="shortcut icon" href="${globalContextPath}/images/openessence.ico"/>

<link type="text/css" rel="stylesheet" href="${globalContextPath}/js/ext-3.0.3/resources/css/ext-all.css"/>
<link type="text/css" rel="stylesheet" href="${globalContextPath}/js/ext-3.0.3/resources/css/xtheme-tp.css"/>
<link type="text/css" rel="stylesheet" href="${globalContextPath}/css/openessence.css"/>

<%-- TODO use conditional loader (Modernizr.load or yepnope.js) --%>
<script type="text/javascript" src="${globalContextPath}/js/lib/console-polyfill/console-polyfill.js"></script>
	   
<script type="text/javascript" src="${globalContextPath}/js/ext-3.0.3/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="${globalContextPath}/js/ext-3.0.3/ext-all.js"></script>
<%-- <script type="text/javascript" src="${globalContextPath}/js/ext-3.0.3/ext-all-debug.js"></script> --%>

<%-- localized Ext, very important to use locale of response, not request, since this is locale Spring gives you,
 	not what the browser says it supports --%>
<script type="text/javascript" src="${globalContextPath}/js/ext-3.0.3/src/locale/ext-lang-${globalLocale}.js"></script>

<%-- OpenESSENCE JS used by every page --%>
<script type="text/javascript" src="${globalContextPath}/js/oe/app/plugin/extFixOverrides.js"></script>
<script type="text/javascript">
var SELECTED_LOCALE = '${globalLocale}';
Ext.USE_NATIVE_JSON = true;

<%-- don't rely on modifying builtin objects, Ext 4 wised up and stopped that --%>
if (!Ext.String) {
	Ext.String = String;
}
</script>
<script type="text/javascript" src="${globalContextPath}/js/oe/app/util/oeUtils.js"></script>
<script type="text/javascript" src="${globalContextPath}/js/oe/app/widget/Header.js"></script>
