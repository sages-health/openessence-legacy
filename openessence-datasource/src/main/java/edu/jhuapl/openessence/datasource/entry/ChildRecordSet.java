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

package edu.jhuapl.openessence.datasource.entry;

import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.jdbc.entry.TableAwareQueryRecord;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Stores children records that are part of the same CRUD behavior and need to be manipulated together. Getter methods
 * return unmodifiable collections. Clients must use exposed methods to add or remove records.
 */

public class ChildRecordSet {

    /**
     * Child tablename to which this ChildRecordSet is associated
     */
    private String childTableName;

    /**
     * pk dimension Ids for the associated child table
     */
    private Set<String> pkIdsForRecordsInSet;

    /**
     * Map of maps. Each inner map associates a "pkId/Values map-to-a ChildRecord."
     */
    private Map<Map<String, Object>, TableAwareQueryRecord> pksValsToRecordMap;

    /**
     * Constructor for ChildRecordSet. Each table aware child record is assigned the tablename and set of pk dimensionIds.
     * A mapping of each child's pk values to the correpsonding child record is also created.
     *
     * @param childTableName       childtable name to which child records belong
     * @param pkIdsForRecordsInSet pk dimensionIds on the child table
     * @param childRecords         collection of child records that will compose the ChildRecordSet
     */
    public ChildRecordSet(String childTableName, Set<String> pkIdsForRecordsInSet,
                          Collection<TableAwareQueryRecord> childRecords) {
        this.childTableName = childTableName;
        this.pkIdsForRecordsInSet = pkIdsForRecordsInSet;

        this.pksValsToRecordMap = new HashMap<Map<String, Object>, TableAwareQueryRecord>();

        if (childRecords != null) {
            for (TableAwareQueryRecord childRecord : childRecords) {
                // tag children with same tablename and pkids
                childRecord.setTablename(childTableName);
                childRecord.setPrimaryKeyIds(pkIdsForRecordsInSet);
                // save each childrec into pk-val to record map
                this.pksValsToRecordMap.put(childRecord.getPrimaryKeysWithValues(), childRecord);
            }
        }
    }

    /**
     * Gets child tablename to which this ChildRecordSet is associated
     *
     * @return child tablename
     */
    public String getChildTableName() {
        return childTableName;
    }

    /**
     * Returns map of maps. Each inner map associates a "pkId/Values map-to-a ChildRecord."
     *
     * @return map of maps that associates pk values to a child {@link edu.jhuapl.openessence.datasource.jdbc.entry.TableAwareQueryRecord}
     */
    public Map<Map<String, Object>, TableAwareQueryRecord> getPksValsToRecordMap() {
        return Collections.unmodifiableMap(pksValsToRecordMap);
    }

    /**
     * Returns an unmodifiable Set of pk dimension Ids for the associated child table
     *
     * @return pkIds unmodifiable set of pk dimension Ids
     */
    public Set<String> getPkIdsForRecordsInSet() {
        return Collections.unmodifiableSet(pkIdsForRecordsInSet);
    }

    /**
     * Returns unmodifiable Collection of child records in this ChildRecordSet
     *
     * @return unmodifiable Collection of child records in this ChildRecordSet
     */
    public Collection<TableAwareQueryRecord> getChildRecords() {
        return Collections.unmodifiableCollection(pksValsToRecordMap.values());
    }

    /**
     * Adds table aware query record to the childrecordset.
     *
     * @param taqr table aware query record to add
     * @throws edu.jhuapl.openessence.datasource.OeDataSourceException
     *          if table aware query record is null, or it's primary keys are null
     */
    public void addChildRecord(TableAwareQueryRecord taqr) throws OeDataSourceException {
        if (taqr == null || taqr.getPrimaryKeyIds() == null) {
            throw new OeDataSourceException("Invalid TableAwareQueryRecord -- record or it's primary keys are null");
        }

        Set<String> setPkIds = taqr.getPrimaryKeyIds();
        if (!setPkIds.equals(this.getPkIdsForRecordsInSet())) {
            throw new OeDataSourceException(
                    "Invalid primary keys for added TableAwareQueryRecord -- they do not match the pkids for this childrecordset.");
        }

        Map<String, Object> idMap = new HashMap<String, Object>();
        for (String id : setPkIds) {
            idMap.put(id, taqr.getValues().get(id));
        }
        pksValsToRecordMap.put(idMap, taqr);
    }

    /**
     * Safely removes all childrecords from this child record set. The primary key ids and tablename are left as is.
     */
    public void removeAllChildRecords() {
        pksValsToRecordMap.clear();
    }

}
