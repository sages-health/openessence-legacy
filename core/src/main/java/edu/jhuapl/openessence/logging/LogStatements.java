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

package edu.jhuapl.openessence.logging;

/**
 * Enum class to ensure consistent logging statements throughout the application.
 *
 * Enum {@link #appCategory} identifies which part of the application the log statement originates.
 *
 * Enum {@link #loggingStmt} contains the descriptive statement to log.
 */
public enum LogStatements {
    //TODO: Internationalize the Strings

    TIME_SERIES(LogStatements.REPORTING, "Time Series Query for user: "),
    DETAILS_QUERY(LogStatements.REPORTING, "Data Details Query for user: "),
    GRAPHING(LogStatements.REPORTING, "System is generating graph for user: "),
    DATA_EXPORT(LogStatements.REPORTING, "System is exporting data details file for user: "),
    DATA_EXPORT_ERROR(LogStatements.REPORTING, "Error occurred when exporting data details for user: "),
    MAP_DATA_QUERY(LogStatements.MAPPING, "Map Data Query for user: "),
    TRANSLATIONS(LogStatements.RESOURCES, "Resource bundle is missing the value for this resource: "),
    LOGINPAGE(LogStatements.LOGIN_CONTROL, "Captured Login Page Activity."),
    LOGINPAGE_SIGNON(LogStatements.LOGIN_CONTROL, "User has signed into the application. "),
    LOGINPAGE_SIGNOFF(LogStatements.LOGIN_CONTROL, "User has signed out of the application. ");

    private String appCategory;
    private String loggingStmt;

    private static final String REPORTING = "Reporting";
    private static final String MAPPING = "Mapping";
    private static final String LOGIN_CONTROL = "LoginControl";
    private static final String RESOURCES = "Resources";

    LogStatements(String name, String stmt) {
        this.appCategory = name;
        this.loggingStmt = stmt;
    }

    public String getAppCategory() {
        return this.appCategory;
    }

    public String getLoggingStmt() {
        return this.loggingStmt;
    }

}
