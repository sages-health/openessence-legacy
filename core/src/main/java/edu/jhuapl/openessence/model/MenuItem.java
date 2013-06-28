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

package edu.jhuapl.openessence.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class MenuItem {

    /**
     * The name of the menu item (aka oe data source name/id) Will also be used for text/label and quick tips (from
     * message.properties)
     */
    private String name;

    /**
     * Used to determine sort order
     */
    private Object order;

    /**
     * Tree node attribute
     */
    private Boolean leaf = false;

    /**
     * Child menu items for base menu items
     */
    private Collection<MenuItem> children;

    /**
     * Parent menu item name
     */
    private String parent;

    /**
     * The javascript to be executed
     */
    private String src;

    /**
     * Used to render content via the contentTab
     */
    private String url;

    /**
     * Used to determine if one or multiple instances can be created
     */
    private Boolean allowMultiple;

    /**
     * Not currently configurable
     */
    private Boolean expanded = true;

    /**
     * Used to determine default tabs (if true, tab will be rendered when app loads)
     */
    private Boolean display;

    public MenuItem(String name, Map<String, Object> item) {
        setName(name);
        setOrder(item.get("order"));

        // Toggles parent items on src...
        setSrc((String) item.get("src"));
        if (src == null || "".equals(src)) {
            setChildren(new ArrayList<MenuItem>());
        } else {
            setParent((String) item.get("parent"));
            setUrl((String) item.get("url"));
            setAllowMultiple((Boolean) item.get("allowMultiple"));
            setDisplay((Boolean) item.get("display"));
            setLeaf(true);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getOrder() {
        return order;
    }

    public void setOrder(Object order) {
        this.order = order;
    }

    public Boolean getLeaf() {
        return leaf;
    }

    public void setLeaf(Boolean leaf) {
        this.leaf = leaf;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getAllowMultiple() {
        return allowMultiple;
    }

    public void setAllowMultiple(Boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    public void setChildren(Collection<MenuItem> children) {
        this.children = children;
    }

    public Collection<MenuItem> getChildren() {
        return children;
    }

    public void setDisplay(Boolean display) {
        this.display = display;
    }

    public Boolean getDisplay() {
        return display;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getParent() {
        return parent;
    }

    public void setExpanded(Boolean expanded) {
        this.expanded = expanded;
    }

    public Boolean getExpanded() {
        return expanded;
    }
}
