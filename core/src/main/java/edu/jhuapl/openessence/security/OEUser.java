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

package edu.jhuapl.openessence.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import edu.jhuapl.openessence.security.EncryptionDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class OEUser extends User {

    private Map<String, Object> attributes;
    private String salt;
    private String algorithm;

    public OEUser(String username, String password, Collection<? extends GrantedAuthority> authorities,
                  Map<String, Object> attribs, String salt, String algorithm)
            throws IllegalArgumentException {

        super(username, password, authorities);
        attributes = new HashMap<String, Object>();
        if (attribs != null) {
            attributes.putAll(attribs);
        }
        this.salt = salt;
        this.algorithm = algorithm;
    }

    /**
     * Get this user's salt. Used in security.xml (<salt-source user-property="someSalt" />).
     */
    public EncryptionDetails getSalt() {
        return new EncryptionDetails(this.salt, this.algorithm);
    }

    @Override
    public boolean equals(Object rhs) {
        if ((rhs == null) || !(rhs.getClass().equals(getClass())) || !super.equals(rhs)) {
            return false;
        }
        OEUser rhu = (OEUser) rhs;
        for (String k : attributes.keySet()) {
            if (!this.getAttribute(k).equals(rhu.getAttribute(k))) {
                return false; // don't have to continue if false
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int code = super.hashCode();
        for (String k : attributes.keySet()) {
            if (this.getAttribute(k) != null) {
                code = code * (this.getAttribute(k).hashCode() % 7);
            }
        }
        return code;
    }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void getAttribute(String key, Object value) {
        attributes.put(key, value);
    }


    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        if (getAttributes() != null) {
            sb.append("; Attributes: ");
            int i = 0;
            for (String key : getAttributes().keySet()) {
                if (i++ > 0) {
                    sb.append(", ");
                }
                sb.append(key).append("=").append(getAttributes().get(key));
            }
        } else {
            sb.append("No attributes");
        }

        return sb.toString();
    }
}
