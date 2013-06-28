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

<?xml version="1.0" encoding="ISO-8859-1"?>

<StyledLayerDescriptor version="1.0.0">
    <NamedLayer>
        <Name>${map}</Name>
        <UserStyle>
            <Title>OpenESSENCE Map Style</Title>
            <Abstract>Style for OpenESSENCE maps</Abstract>
            <FeatureTypeStyle>
                <Rule>
                    <Title>Polygon</Title>
                    <PolygonSymbolizer>
                        <Fill>
                            <CssParameter name="fill">#
                                <PropertyName>color</PropertyName>
                            </CssParameter>
                            <CssParameter name="fill-opacity">${fillOpacity}</CssParameter>
                        </Fill>
                        <Stroke>
                            <CssParameter name="stroke">#${stroke}</CssParameter>
                            <CssParameter name="stroke-width">${strokeWidth}</CssParameter>
                            <CssParameter name="stroke-opacity">${strokeOpacity}</CssParameter>
                        </Stroke>
                    </PolygonSymbolizer>
                </Rule>
            </FeatureTypeStyle>
        </UserStyle>
    </NamedLayer>
</StyledLayerDescriptor>
