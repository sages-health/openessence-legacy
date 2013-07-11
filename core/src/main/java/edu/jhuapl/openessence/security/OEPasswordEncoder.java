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

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class OEPasswordEncoder implements PasswordEncoder {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     *
     * @param rawPass
     * @param encryptDetails an {@link EncryptionDetails} object
     * @return The encrypted version of the password
     * @throws DataAccessException
     */
    @Override
    public String encodePassword(String rawPass, Object encryptDetails) throws DataAccessException {
        if ((encryptDetails == null) || !(encryptDetails.getClass().equals(EncryptionDetails.class))) {
            return "";
        }
        String encPass = "";
        String salt = ((EncryptionDetails) encryptDetails).getSalt();
        String algorithm = ((EncryptionDetails) encryptDetails).getAlgorithm();
        if (algorithm.equals("SHA-1")) {
            log.warn("SHA-1 DEPRECATED, retained for compatibility.");
            encPass = DigestUtils.sha1Hex(salt + rawPass);
        } else if (algorithm.equals("SHA-256")) {
            log.warn("SHA-256 DEPRECATED, retained for compatibility.");
            encPass = DigestUtils.sha256Hex(salt + rawPass);
        } else if (algorithm.equals("SHA-384")) {
            log.warn("SHA-384 DEPRECATED, retained for compatibility.");
            encPass = DigestUtils.sha384Hex(salt + rawPass);
        } else if (algorithm.equals("SHA-512")) {
            log.warn("SHA-512 DEPRECATED, retained for compatibility.");
            encPass = DigestUtils.sha512Hex(salt + rawPass);
        } else if (algorithm.equals("BCrypt")) {
            encPass = BCrypt.hashpw(rawPass, salt);
        }
        return encPass;
    }

    /**
     *
     * @param encPass
     * @param rawPass
     * @param encryptDetails an {@link EncryptionDetails} object
     * @return The encrypted version of the password
     * @throws DataAccessException
     */
    @Override
    public boolean isPasswordValid(String encPass, String rawPass, Object encryptDetails) throws DataAccessException {
        if ((encryptDetails == null) || !(encryptDetails.getClass().equals(EncryptionDetails.class))) {
            return false;
        }
        String algorithm = ((EncryptionDetails) encryptDetails).getAlgorithm();
        boolean checkPass = false;
        if (algorithm.equals("BCrypt")) {
            checkPass = BCrypt.checkpw(rawPass, encPass);
        } else {
            checkPass = encodePassword(rawPass, encryptDetails).equals(encPass);
        }

        return checkPass;
    }
}
