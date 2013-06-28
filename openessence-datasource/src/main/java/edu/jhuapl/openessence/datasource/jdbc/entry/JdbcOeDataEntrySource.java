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

package edu.jhuapl.openessence.datasource.jdbc.entry;

import edu.jhuapl.openessence.datasource.Dimension;
import edu.jhuapl.openessence.datasource.FieldType;
import edu.jhuapl.openessence.datasource.Filter;
import edu.jhuapl.openessence.datasource.InvalidOeDataSourceException;
import edu.jhuapl.openessence.datasource.OeDataSourceAccessException;
import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.Record;
import edu.jhuapl.openessence.datasource.UpdateException;
import edu.jhuapl.openessence.datasource.entry.ChildRecordSet;
import edu.jhuapl.openessence.datasource.entry.ChildTableDetails;
import edu.jhuapl.openessence.datasource.entry.CompleteRecord;
import edu.jhuapl.openessence.datasource.entry.DbKeyValMap;
import edu.jhuapl.openessence.datasource.entry.OeDataEntrySource;
import edu.jhuapl.openessence.datasource.entry.ParentTableDetails;
import edu.jhuapl.openessence.datasource.jdbc.DataTypeConversionHelper;
import edu.jhuapl.openessence.datasource.jdbc.DimensionBean;
import edu.jhuapl.openessence.datasource.jdbc.DimensionBeanAdapter;
import edu.jhuapl.openessence.datasource.jdbc.JdbcOeDataSource;
import edu.jhuapl.openessence.datasource.jdbc.filter.EqFilter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class JdbcOeDataEntrySource extends JdbcOeDataSource implements OeDataEntrySource {

    protected String tableName;
    protected Map<String, ChildTableDetails> childTableMap;
    protected ParentTableDetails parentTableDetails;

    private boolean isVersioningMode = false;

    public JdbcOeDataEntrySource() {
        childTableMap = new LinkedHashMap<String, ChildTableDetails>();
    }

    public String getDataSourceId() {
        return this.getClass().getSimpleName();
    }

    /********************************************************************************************************************
     * Getter/Setters/Setup methods for attributes
     *******************************************************************************************************************/

    /**
     * Adds child table details to the oe data entry source
     *
     * @param details ChildTableDetails to add
     */
    public void addChildTableDetails(ChildTableDetails details) {
        if (childTableMap == null || childTableMap.isEmpty()) {
            childTableMap = new LinkedHashMap<String, ChildTableDetails>();
        }

        Set<String> nameSet = childTableMap.keySet();
        String tableName = details.getTableName();
        if (nameSet.contains(tableName)) {
            throw new InvalidOeDataSourceException("ChildTableDetails for " + tableName + " is already defined.");
        } else {
            childTableMap.put(tableName, details);
        }
        log.debug("number of children tables: " + childTableMap.size());
    }

    public void setParentTableDetails(ParentTableDetails parentDetails) {
        this.parentTableDetails = parentDetails;
    }

    public ParentTableDetails getParentTableDetails() {
        return this.parentTableDetails;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName SQL table that this DatasourceDefintion is configured against
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public Map<String, ChildTableDetails> getChildTableMap() {
        return childTableMap;
    }

    public void setChildTableMap(Map<String, ChildTableDetails> childTableMap) {
        this.childTableMap = childTableMap;
    }

    /**
     * @return autoGenMap map of dimension id to dimension bean that is auto-generated during sql insert
     */
    public Map<String, DimensionBean> getAutoGenMap() {
        return this.autoGenMap;
    }

    /**
     * @return specialSqlMap map of dimension id to dimension bean that has special sql
     */
    public Map<String, DimensionBean> getSpecialSqlMap() {
        return this.specialSqlMap;
    }

    /*****************************************************************************************************************
     * C[R]UD - Retrieval/Read/Get methods for Data Entry
     ****************************************************************************************************************/


    /**
     * Steps: 1. Get "parent" record for the input recordPks 2. Add "parent" record to a new CompleteRecord 3. Loop over
     * "children" tablenames--for each-- a) transform "parent's" recordPks into fkToParentMap b) get collection of
     * "children" records for the input params: childtablename, fkToParentMap c) create childrecordset from each
     * collection of "children" records d) add each childrecordset to the CompleteRecord 4. return the CompleteRecord
     *
     * @param recordPks          Primary keys for the parent record
     * @param childrenTableNames the list of child tablenames to also retrieve
     * @return completeRecord corresponding to the underlying select statement.
     */
    @Override
    public CompleteRecord getCompleteRecord(DbKeyValMap recordPks, List<String> childrenTableNames)
            throws OeDataSourceAccessException {
        try {
            // get parent record and add it to a new CompleteRecord
            TableAwareQueryRecord parent = (TableAwareQueryRecord) getParentRecord(recordPks);
            CompleteRecord completeRecord = new CompleteRecord(parent);

            childrenTableNames = (childrenTableNames == null) ? new ArrayList<String>() : childrenTableNames;
            // create children record sets for each child table. children record sets are added to the complete record
            for (String childTable : childrenTableNames) {

                ChildTableDetails childDetails = childTableMap.get(childTable);
                // have to pass the parent's [pkid:values] to the children as [fkid:values]
                DbKeyValMap fkId_ValueMap = new DbKeyValMap();
                Map<String, String> fkToParentMap = childDetails.getFksToParent();
                for (String childCol : fkToParentMap.keySet()) {
                    fkId_ValueMap.put(childCol, recordPks.get(fkToParentMap.get(childCol)));
                }

                // get collection of children records for: [fkid:values], childtablename
                Collection<TableAwareQueryRecord> childRecords = getChildRecords(fkId_ValueMap, childTable);
                ChildRecordSet childRecordsSet = new ChildRecordSet(childTable, childDetails.getPks(), childRecords);
                completeRecord.addChildRecordSet(childRecordsSet);
            }
            return completeRecord;
        } catch (DataAccessException de) {
            throw new OeDataSourceAccessException("Unable to return record. DataAccessException occurred.", de);
        } catch (OeDataSourceException oe) {
            throw new OeDataSourceAccessException("Unable to return record.", oe);
        }
    }

    /**
     * Uses the pkId_ValueMap argument to get a "parent" {@link TableAwareQueryRecord} from this JdbcOeDataEntrySource's
     * master table.
     *
     * @param pkId_ValueMap primary-key Dimension id(s) mapped to value(s)
     * @return the parent TableAwareQueryRecord record having primary-key Dimension id(s) and value(s) in pkId_ValueMap
     * @throws OeDataSourceAccessException if an unexpected error occurs during processing
     */
    public Record getParentRecord(Map<String, Object> pkId_ValueMap) throws OeDataSourceAccessException {
        TableAwareQueryRecord queryRecord = null;

        // get needed parent table details
        ParentTableDetails tableDetails = this.getParentTableDetails();

        // make Filter[] of primary-key ids to pass to editableDetailsQuery
        List<Filter> filterList = new ArrayList<Filter>();
        for (String pkFilterId : tableDetails.getPks()) {
            Object obj = pkId_ValueMap.get(pkFilterId);
            Dimension dim = getEditDimension(pkFilterId);
            // TODO: refactor this to use same value parsing (check controller utils)
            if (dim.getSqlType() == FieldType.DATE_TIME || dim.getSqlType() == FieldType.DATE) {
                if (obj instanceof String) {
                    Date d = null;
                    try {
                        d = DataTypeConversionHelper.dateFromString((String) obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // TODO: keeping same logic for now...
                    }
                    if (d != null) {
                        obj = d;
                    }
                }
            }
            EqFilter eqFilter = new EqFilter(pkFilterId, obj);
            filterList.add(eqFilter);
        }

        try {
            /*
             * Run a detailsQuery with the pkIds as filters, to get a TableAwareQueryRecord with all the
             * editDimensions JdbcOeDataSource's way: Record[] records =
             * (Record[])detailsQuery(getResultDimensions(), Arrays.asList(filters)).toArray();
             */
            Collection<TableAwareQueryRecord>
                    recordCollection =
                    editableDetailsQuery(getEditDimensions(), filterList, getTableName());
            TableAwareQueryRecord[]
                    records =
                    recordCollection.toArray(new TableAwareQueryRecord[recordCollection.size()]);
            if (records != null && records.length > 0) {
                queryRecord = records[0];
                queryRecord.setTablename(getTableName());
            } else if (records == null || records.length > 0) {
                int recLength = records == null ? 0 : records.length;
                throw new OeDataSourceAccessException("Unable to process. " + recLength +
                                                      " parent records returned for the primary key filter used in the query");
            }
        } catch (OeDataSourceException e) {
            throw new OeDataSourceAccessException("Unable to process get record request. See error logs.", e);
        }
        return queryRecord;
    }

    /**
     * Gets "children" records as a Collection of type {@link TableAwareQueryRecord} for the specified childtableName
     *
     * @param fkIdToParent_ValueMap the fkIds:values from the child table back to the parent table
     * @param childTableName        the name of the child table to query
     * @return "children" records as a Collection of type TableAwareQueryRecord. The children match on the fkIds:values
     *         and childtablename
     * @throws OeDataSourceException if error occurs getting children
     */
    public Collection<TableAwareQueryRecord> getChildRecords(Map<String, Object> fkIdToParent_ValueMap,
                                                             String childTableName) throws OeDataSourceException {

        Collection<TableAwareQueryRecord> recs;

        //get needed child table details
        ChildTableDetails tableDetails = childTableMap.get(childTableName);

        if (tableDetails == null) {
            throw new OeDataSourceException(
                    "Unknown childtable--table details do not exist for table " + "\"" + childTableName + "\"");
        }

        // make Filter[] of fk dimension id's and values to pass to editableDetailsQuery
        List<Filter> filterList = new ArrayList<Filter>();
        for (String pkFilterId : tableDetails.getFksToParent().keySet()) {
            EqFilter eqFilter = new EqFilter(pkFilterId, fkIdToParent_ValueMap.get(pkFilterId));
            filterList.add(eqFilter);
        }

        //todo-REUSABLE
        // gather childtable's editable dimensions so they are returned in the result set.
        List<Dimension> editDims = new ArrayList<Dimension>();
        List<String> editDimIds = tableDetails.getDimensionIds();

        for (String editId : editDimIds) {
            if (getEditDimension(editId) == null) {
                throw new OeDataSourceException(
                        "Invalid edit dimension \'" + editId + "\' was used for the child table \'" + tableDetails
                                .getTableName() + "\'");
            }
            editDims.add(getEditDimension(editId));
        }

        recs = editableDetailsQuery(editDims, filterList, childTableName);
        return recs;
    }


    /**
     * **************************************************************************************************************
     * [C]RUD - Create/Add methods for Data Entry **************************************************************************************************************
     */


    @Override
    public Map<String, Object> addCompleteRecord(CompleteRecord completeRecord, boolean ignoreSpecialSql)
            throws OeDataSourceAccessException {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = getTransactionManager().getTransaction(def);
        Map<String, Object> FOREIGN_KEY_VALUE_MAP = new HashMap<String, Object>();

        TableAwareQueryRecord parent = completeRecord.getParentRecord();
        try {
            // 1. add parent record and get the pk-keys for this record entry. pk-keys get passed to children for their fk-keys
            Map<String, Object> parentKeys = addQueryRecord(parent, ignoreSpecialSql);
            DbKeyValMap fkIdMap = new DbKeyValMap(parentKeys);

            // 2. get dimension Id correlation for parentpkIds <--> childpkIds so INSERTs to child tables use correct dimensionId
            Collection<ChildRecordSet> allChildRecSets = completeRecord.getChildrenRecordSets();
            Map<String, ChildRecordSet> childRecSetMap = new HashMap<String, ChildRecordSet>();
            for (ChildRecordSet crs : allChildRecSets) {
                childRecSetMap.put(crs.getChildTableName(), crs);
            }
            Iterator<ChildRecordSet> childRecSetsIter = allChildRecSets.iterator();

            if (childTableMap != null && allChildRecSets.size() > 0) {

                while (childRecSetsIter.hasNext()) {
                    Map<String, String> pksFromParent = null;
                    ChildTableDetails
                            childTableDetails =
                            childTableMap.get(childRecSetsIter.next().getChildTableName());

                    if (childTableDetails != null) {
                        pksFromParent = childTableDetails.getPksFromParentToChild();
                    }

                    // 3. setup fkIds-Values for the children
                    if (pksFromParent != null) {
                        for (String fk : pksFromParent.keySet()) {
                            // i.e. from MasterID --> c1_MasterID & ReportID --> c1_ReportID
                            FOREIGN_KEY_VALUE_MAP.put(pksFromParent.get(fk), fkIdMap.get(fk));
                        }
                    }

                    // 4. for each childRecordSet, add its children records
                    Collection<TableAwareQueryRecord>
                            childrenRecords =
                            childRecSetMap.get(childTableDetails.getTableName()).getChildRecords();
                    for (TableAwareQueryRecord child : childrenRecords) {
                        addChildRecord(child, ignoreSpecialSql, FOREIGN_KEY_VALUE_MAP);
                    }
                    // need to clear old pk/fk dimension Ids for each child table
                    FOREIGN_KEY_VALUE_MAP.clear();

                }
            } else if (childTableMap == null && allChildRecSets.size() > 0) {
                throw new OeDataSourceAccessException(
                        "No data was added. Childtablemap is empty, but childrecordsets were found on this record. Childtables not configured in groovy datasource definition.");
            }

            getTransactionManager().commit(status);
            return parentKeys;

        } catch (DuplicateKeyException dae) {
            getTransactionManager().rollback(status);
            throw new OeDataSourceAccessException(
                    "Error: No data was added. Could not create record due to duplicate key(s).");
        } catch (DataAccessException dae) {
            getTransactionManager().rollback(status);
            throw new OeDataSourceAccessException(
                    "No data was added. DataAccessException occurred while trying to add a complete record.", dae);
        } catch (Exception e) {
            getTransactionManager().rollback(status);
            throw new OeDataSourceAccessException(
                    "No data was added. Problem occurred while trying to add a complete record.", e);
        }
    }

    /**
     * Inserts a parent record into the database
     *
     * @param tableAwareQueryRecord Record to add at the parent table level
     * @param ignoreSpecialSql      the flag to ignore specialSql definitions in the groovy def file. In general, set
     *                              false during add* and set true during update*
     * @param FK_VAL_MAP            map that enables passing the parent's pk values as fk's to the children
     * @return The primary keys with values for an inserted "parent" record. Null is returned for children records.
     * @throws OeDataSourceAccessException if error occurs at database level
     * @throws OeDataSourceException       if error occurs
     */
    private Map<String, Object> addQueryRecord(TableAwareQueryRecord tableAwareQueryRecord, boolean ignoreSpecialSql,
                                               Map<String, Object> FK_VAL_MAP)
            throws OeDataSourceAccessException, OeDataSourceException {

        Map<String, Dimension> editDims = tableAwareQueryRecord.getEditDimensions();
        Set<String> autoGenDimIds = this.getAutoGenMap().keySet();

        Map<String, DimensionBean> specialSqlMap = this.getSpecialSqlMap();
        Set<String> specialSqlIds = specialSqlMap.keySet();

        Map<String, Object> vals = tableAwareQueryRecord.getValues();

        Set<String> dimensionIds = editDims.keySet();

        // remove auto-generated dimensions from the dimension list we'll loop over
        dimensionIds.removeAll(autoGenDimIds);
        if (!ignoreSpecialSql) {
            // set the values of specialsqldimensions with their 'special sql'
            for (String sqlDimId : specialSqlIds) {
                vals.put(sqlDimId, specialSqlMap.get(sqlDimId).getSpecialSql());
            }
        }

        //FK-val map. Used to pass PKs to children for use as FKs
        for (String sqlDimId : FK_VAL_MAP.keySet()) {
            vals.put(sqlDimId, FK_VAL_MAP.get(sqlDimId));
        }

        String[] dimIdArray = dimensionIds.toArray(new String[dimensionIds.size()]);
        List<String> dimIdList = (Arrays.asList(dimIdArray));
        // sorting ensures consistent ordering to utilize efficiencies of PreparedStatements
        Collections.sort(dimIdList);
        return editableInsertQuery(tableAwareQueryRecord.getTableName(), ignoreSpecialSql, dimIdList, editDims, vals);
    }

    /**
     * Inserts a parent record into the database
     *
     * @param tableAwareQueryRecord Record to add at the parent table level
     * @param ignoreSpecialSql      the flag to ignore specialSql definitions in the groovy def file. In general, set
     *                              false during add* and set true during update*
     * @return The primary keys with values for an inserted "parent" record. Null is returned for children records.
     * @throws OeDataSourceAccessException if error occurs at database level
     * @throws OeDataSourceException       if error occurs during processing
     */
    private Map<String, Object> addQueryRecord(TableAwareQueryRecord tableAwareQueryRecord, boolean ignoreSpecialSql)
            throws OeDataSourceAccessException, OeDataSourceException {
        return addQueryRecord(tableAwareQueryRecord, ignoreSpecialSql, new HashMap<String, Object>());

    }

    /**
     * Inserts a child record into the database
     *
     * @param record           Record to add at the child table level
     * @param ignoreSpecialSql the flag to ignore specialSql definitions in the groovy def file. In general, set false
     *                         during add* and set true during update*
     * @param FK_VAL_MAP       map that enables passing parent's pk values to children as fks
     * @throws OeDataSourceAccessException if error occurs at database level
     * @throws OeDataSourceException       if error occurs during processing
     */
    private void addChildRecord(TableAwareQueryRecord record, boolean ignoreSpecialSql, Map<String, Object> FK_VAL_MAP)
            throws OeDataSourceAccessException, OeDataSourceException {
        addQueryRecord(record, ignoreSpecialSql, FK_VAL_MAP);
    }

    /**
     * Inserts a child record into the database -- intended for usage when all fk/pk values are known to be correct. i.e.
     * on an update of childrecordsets -- see performSqlOnChildRecord
     *
     * @param record           Record to add at the child table level
     * @param ignoreSpecialSql the flag to ignore specialSql definitions in the groovy def file. In general, set false
     *                         during add* and set true during update*
     * @throws OeDataSourceException       if error occurs during processing
     * @throws OeDataSourceAccessException if error occurs at database level
     */
    private void addChildRecord(TableAwareQueryRecord record, boolean ignoreSpecialSql)
            throws OeDataSourceAccessException, OeDataSourceException {
        addQueryRecord(record, ignoreSpecialSql);
    }

    /**
     * Executes INSERT SQL Statment using Spring's JdbcTemplate.
     *
     * @param tableName        table to insert values into
     * @param ignoreSpecialSql the flag to ignore specialSql definitions in the groovy def file. In general, set false
     *                         during add* and set true during update*
     * @param dimIds           DimensionIds that we will insert data for //todo ?? why is this needed?
     * @param editDims         editable DimensionIds that we will insert data for
     * @param values           values that correspond to the editable DimensionIds. These values get written into the
     *                         database
     * @return Map of the primary keys and values for the inserted record -- only for the Parent Record - children return
     *         null
     * @throws OeDataSourceAccessException if error occurs at database level
     * @throws OeDataSourceException       if error occurs during processing
     */
    private Map editableInsertQuery(String tableName,
                                    boolean ignoreSpecialSql,
                                    List<String> dimIds,
                                    Map<String, Dimension> editDims,
                                    Map<String, Object> values)
            throws OeDataSourceAccessException, OeDataSourceException {

        List<String> generatedKeys = new ArrayList<String>();
        Set<String> tablePkIds;

        // insert on parent table
        if (tableName.equals(parentTableDetails.getTableName())) {

            // setup KeyHolder from pk_dimIds
            tablePkIds = parentTableDetails.getPks();

            // setup autogen to sqlcol map
            Map<String, Object> superEditCopy = new LinkedHashMap<String, Object>(superEditMap);
            Set<String> superEditKeys = superEditCopy.keySet();
            DualHashBidiMap bidimap = new DualHashBidiMap();
            superEditKeys.retainAll(tablePkIds);
            for (Map.Entry<String, Object> e : superEditCopy.entrySet()) {
                e.setValue(((DimensionBean) e.getValue()).getSqlCol());
                bidimap.put(e.getKey(), e.getValue());
            }

            // setup KeyHolder from pk_dimIds
            generatedKeys.addAll(tablePkIds);  // NOTE: jdbc driver clears this and puts in the autoincs it finds.
            Map<String, Object> generatedKeyMap = new HashMap<String, Object>();
            for (String eachKey : generatedKeys) {
                generatedKeyMap.put(eachKey, null);
            }
            List<Map<String, Object>> keyMapList = new ArrayList<Map<String, Object>>();
            keyMapList.add(generatedKeyMap);
            KeyHolder keyHolder = new GeneratedKeyHolder(keyMapList);

            jdbcTemplate
                    .update(new MultiTableInsertPreparedStatementCreator(tableName, ignoreSpecialSql, dimIds, editDims,
                                                                         values), keyHolder);

            Map<String, Object> keyMap = keyHolder.getKeys();

            // TODO: current implementation of getGeneratedKeys for PGSQL 8.4 returns ALL column/vals...we just want the pk's we know about
            // TODO: CHECK FOR WHAT HAPPENS WITH LOWER/UPPER CASE
            //http://archives.postgresql.org/pgsql-jdbc/2010-04/msg00061.php
            boolean isPostgreSql = isPostgreSqlDBMS();
            if (isPostgreSql) {
                // postgres' implementation of keyholder lowercases the key column
                DbKeyValMap dbkvm = new DbKeyValMap(bidimap);
                Set<String> kyids = dbkvm.keySet();
                for (String ky : kyids) {
                    dbkvm.put(ky, keyMap.get(bidimap.get(ky)));
                }
                kyids.retainAll(tablePkIds);
                keyMap = dbkvm;
            }

            // -OR-
            // if table had no auto-gen keys but the INSERT suceedes, means the pks taken from the 'values' worked.
            // therefore, safe to use these as the "generated" PKs. retains the values that are designated "PK" dimensions
            //
            else if (keyMap == null || keyMap.size() == 0) {
                DbKeyValMap dbkvm = new DbKeyValMap(values);
                Set<String> kyids = dbkvm.keySet();
                kyids.retainAll(tablePkIds);
                keyMap = dbkvm;
            }

            // make sure got *ALL* pkIds/values configured in the ds def.
            List<Map> allkeys = getAllGeneratedKeys(tableName, tablePkIds, new DbKeyValMap(keyMap));

            return (allkeys.size() > 0 ? allkeys.get(0) : null);

        } else { // insert on child table.
            // don't need to know the returned PK ids & vals for children. just do typical INSERT
            jdbcTemplate
                    .update(new MultiTableInsertPreparedStatementCreator(tableName, ignoreSpecialSql, dimIds, editDims,
                                                                         values));
            return null;
        }
    }


    /**
     * **************************************************************************************************************
     * CR[U]D - Update methods for Data Entry **************************************************************************************************************
     */

    @Override
    public void updateCompleteRecord(final DbKeyValMap recordPks, final CompleteRecord replacementRecord)
            throws OeDataSourceAccessException {
        final TransactionDefinition def = new DefaultTransactionDefinition();
        final TransactionStatus status = getTransactionManager().getTransaction(def);

        try {
            final
            CompleteRecord
                    originalRecord =
                    getCompleteRecord(recordPks, new ArrayList<String>(this.getChildTableMap().keySet()));
            if (originalRecord != null) {
                // update ParentRecord
                updateParentRecord(recordPks, replacementRecord.getParentRecord());

                // update Child Record Sets  -todo make use of refactor see ChildRecordSet
                final Map<String, ChildRecordSet> origRecordSetMap = new HashMap<String, ChildRecordSet>();
                for (final ChildRecordSet recSet : originalRecord.getChildrenRecordSets()) {
                    origRecordSetMap.put(recSet.getChildTableName(), recSet);
                }
                updateChildRecordSets(recordPks, origRecordSetMap, replacementRecord.getChildrenRecordSets());
            } else {
                addCompleteRecord(replacementRecord, false);
            }
            getTransactionManager().commit(status);
        } catch (DataAccessException de) {
            getTransactionManager().rollback(status);
            throw new OeDataSourceAccessException(
                    "No records were updated. DataAccessException occured while updating record.", de);
        } catch (OeDataSourceException e) {
            getTransactionManager().rollback(status);
            throw new OeDataSourceAccessException(
                    "No records were updated. OeDataSourceException occured while updating record.", e);
        }

    }

    /**
     * Update existing record in the database(having the specified pk values) with the values of the input completeRecord
     *
     * @param pkIdsValueMap           maps dimensionID to the value. In this case, the dimensions will correspond to PK
     *                                columns
     * @param replacementParentRecord the replacement "parent" record to update the original database record
     */
    private void updateParentRecord(DbKeyValMap pkIdsValueMap, TableAwareQueryRecord replacementParentRecord) {
        jdbcTemplate.update(new MultiTableUpdatePreparedStatementCreator(replacementParentRecord.getTableName(),
                                                                         replacementParentRecord, pkIdsValueMap));
    }

    /**
     * Update existing children recordsets in the database(having the specified pk values) with the values of the input
     * child recordsets
     *
     * @param pkIdsValueMap                 maps dimensionID to the value. In this case, the dimensions will correspond to
     *                                      PK columns
     * @param originalChildrenRecordSetsMap the original childrecordsets mapped by their tablenames
     * @param childRecordSets               the new replacement childrecordsets to swap into the database
     * @throws OeDataSourceAccessException if error occurs at database level
     * @throws OeDataSourceException       if error occurs during processing
     */
    private void updateChildRecordSets(DbKeyValMap pkIdsValueMap,
                                       Map<String, ChildRecordSet> originalChildrenRecordSetsMap,
                                       Collection<ChildRecordSet> childRecordSets)
            throws OeDataSourceException, OeDataSourceAccessException {
        // get childrecordsets for pkids
        // for each : updateChildRecordSet(childRecordSet)
        for (ChildRecordSet recordSet : childRecordSets) {

            //update recordset and use corresponding origrecset for SAME table...needed to categorize pkvals
            updateChildRecordSet(originalChildrenRecordSetsMap.get(recordSet.getChildTableName()), recordSet);
        }
    }

    /**
     * Update existing child recordset in the database(having the specified pk values) with the records in the replacement
     * child recordset
     *
     * @param originalChildRecordSet the original childrecordset
     * @param childRecordSet         the replacement childrecordset
     * @throws OeDataSourceAccessException if error occurs at database level
     * @throws OeDataSourceException       if error occurs during processing
     */
    private void updateChildRecordSet(/*Map<String, Object> childPkIdsValueMap,*/
                                      ChildRecordSet originalChildRecordSet,
                                      ChildRecordSet childRecordSet)
            throws OeDataSourceException, OeDataSourceAccessException {

        Map<String, List<Map<String, Object>>>
                categorizedPKS =
                categorizeChildrenRecords(originalChildRecordSet, childRecordSet);
        performSqlonChildRecord(categorizedPKS, originalChildRecordSet.getPksValsToRecordMap(),
                                childRecordSet.getPksValsToRecordMap());
    }

    /**
     * Executes UPDATE SQL Statment using Spring's JdbcTemplate.
     *
     * @param tablename       table to insert values into
     * @param replRecord      use values of replacement record within the UPDATE statement
     * @param pkIds_ValuesMap maps dimensionID to the value. In this case, the dimensions will correspond to PK columns
     */
    private void editableUpdateStatement(String tablename, TableAwareQueryRecord replRecord,
                                         Map<String, Object> pkIds_ValuesMap) throws OeDataSourceAccessException {
        try {
            jdbcTemplate.update(new MultiTableUpdatePreparedStatementCreator(tablename, replRecord, pkIds_ValuesMap));
        } catch (DataAccessException e) {
            if (e.getCause() instanceof UpdateException) {
                log.info(e.getMessage());
            } else {
                throw new OeDataSourceAccessException(e);
            }
        }
    }

    /**
     * **************************************************************************************************************
     * CRU[D] - Delete methods for Data Entry ****************************************************************************************************************
     */


    @Override
    public void deleteCompleteRecord(DbKeyValMap pkIdsValueMap) throws OeDataSourceAccessException {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = getTransactionManager().getTransaction(def);

        try {
            //TODO: add a non-cascade DELETE
            // assumes "CASCADE ON DELETE" was configured on table to automatically delete children tables
            deleteParentRecord(pkIdsValueMap);
            getTransactionManager().commit(status);
        } catch (DataAccessException de) {
            getTransactionManager().rollback(status);
            throw new OeDataSourceAccessException(
                    "No data was deleted. DataAccessException occurred during deletion of record.", de);
        } catch (OeDataSourceAccessException de) {
            getTransactionManager().rollback(status);
            throw new OeDataSourceAccessException(
                    "No data was deleted. DataAccessException occurred during deletion of record.", de);
        }

    }

    /**
     * Deletes a parent record with the corresponding primary key values. Assumes ON CASCADE is configured on the database
     * to handle the deletion of chilren records
     *
     * @param pkIdsValueMap maps dimensionID to the value. In this case, the dimensions will correspond to PK columns
     * @throws OeDataSourceAccessException if there is any problem executing the delete query.
     */
    public void deleteParentRecord(DbKeyValMap pkIdsValueMap) throws OeDataSourceAccessException {
        deleteQueryRecord(pkIdsValueMap, this.getTableName());
    }

    /**
     * Deletes a child record with the corresponding primary key values.
     *
     * @param pkIdsValueMap maps dimensionID to the value. In this case, the dimensions will correspond to PK columns
     * @param tableName     table to delete the record from
     */
    public void deleteQueryRecord(DbKeyValMap pkIdsValueMap, String tableName) throws OeDataSourceAccessException {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = getTransactionManager().getTransaction(def);
        try {
            editableDeleteStatement(tableName, pkIdsValueMap);
            getTransactionManager().commit(status);
        } catch (DataAccessException de) {
            getTransactionManager().rollback(status);
            throw new OeDataSourceAccessException(
                    "Record was not deleted. DataAccessException occured while deleting record.", de);
        }
    }

    public void deleteQueryRecords(String tablename, List<DbKeyValMap> keysForDeletion)
            throws OeDataSourceAccessException {
        if (tablename == null) {
            throw new OeDataSourceAccessException(
                    "Records were not deleted. No target table was specified to perform the deletion.");
        }

        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = getTransactionManager().getTransaction(def);
        try {
            keysForDeletion = (keysForDeletion == null) ? new ArrayList<DbKeyValMap>() : keysForDeletion;
            for (DbKeyValMap dbKeys : keysForDeletion) {
                editableDeleteStatement(tablename, dbKeys);
            }
            getTransactionManager().commit(status);
        } catch (DataAccessException de) {
            getTransactionManager().rollback(status);
            throw new OeDataSourceAccessException(
                    "Records were not deleted. DataAccessException occured while deleting record.", de);
        }

    }

    /**
     * Executes DELETE SQL Statment using Spring's JdbcTemplate.
     *
     * @param tableName     table to delete values from
     * @param pkIdsValueMap maps dimensionID to the value. In this case, the dimensions will correspond to PK columns
     */
    public void editableDeleteStatement(String tableName, Map<String, Object> pkIdsValueMap) {
        jdbcTemplate.update(new MultiTableDeletePreparedStatementCreator(tableName, pkIdsValueMap));
    }

    /**
     * ************************************************************************************************************
     * Private Classes leveraging Spring's JdbcTemplate for PreparedStatementCreators etc...
     * *************************************************************************************************************
     */

    private class MultiTableUpdatePreparedStatementCreator implements PreparedStatementCreator {

        private final String tablename;
        private final TableAwareQueryRecord replacementRecord;
        private final Map<String, Object> pkIds_ValuesMap;

        private MultiTableUpdatePreparedStatementCreator(String tablename, TableAwareQueryRecord replRecord,
                                                         Map<String, Object> pkIds_ValuesMap) {
            this.tablename = tablename;
            this.replacementRecord = replRecord;
            this.pkIds_ValuesMap = pkIds_ValuesMap;
        }


        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
      /*
    build up the UPDATE string
     1. get baseUPDATE - "UPDATE TABLEx
     2. iterate over all columns add to SETstr
     3. stick on the baseWHERE
     */
            TableAwareQueryRecord newRecord = replacementRecord;

            Map<String, Dimension> dims = newRecord.getEditDimensions();
            Map<String, Object> values = newRecord.getValues();
            Set<String> dimKeys = dims.keySet();
            dimKeys.removeAll(pkIds_ValuesMap.keySet());  // we do NOT want to alter the PK in and update

            if (dims.keySet().size() == 0) {
                throw new UpdateException("No columns to update after removing primary keys");
            }

            List<String> dimIds = new ArrayList<String>((Arrays.asList(dimKeys.toArray(new String[dimKeys.size()]))));
            Collections.sort(dimIds);

            StringBuilder updateStr = new StringBuilder("UPDATE " + tablename + " SET ");
            List<Object> arguments = new ArrayList<Object>();

            boolean isFirst = true;
            for (String dimId : dimIds) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    updateStr.append(", ");
                }
                updateStr.append(dims.get(dimId).getSqlCol() + " = ?");
                arguments.add(values.get(dimId));

            }

            ArrayList<Filter> filters = new ArrayList<Filter>();
            List<Object> filterArgs = new ArrayList<Object>();

            for (String pkId : pkIds_ValuesMap.keySet()) {
                EqFilter eqFil = new EqFilter(pkId, pkIds_ValuesMap.get(pkId));
                filters.add(eqFil);
                filterArgs.add(pkIds_ValuesMap.get(pkId));
                dimIds.add(pkId); // used to determine data type for param on prepared statement
            }

            arguments.addAll(filterArgs);
            addWhereClauses(updateStr, filters);
            log.debug("UPDATE(SQL string w/ placeholders): \n" + updateStr);
            PreparedStatement updateStmt = con.prepareStatement(updateStr.toString());
            setArgumentsOnSqlType(updateStmt, dimIds, arguments);
            log.debug("UPDATE(pstmt w/ values): \n" + updateStmt);
            return updateStmt;
        }
    }


    private class MultiTableDeletePreparedStatementCreator implements PreparedStatementCreator {

        private final String tablename;
        private final Map<String, Object> pkIds_ValuesMap;

        private MultiTableDeletePreparedStatementCreator(String tablename, Map<String, Object> pkIds_ValuesMap) {
            this.tablename = tablename;
            this.pkIds_ValuesMap = pkIds_ValuesMap;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            StringBuilder deleteFromStr = new StringBuilder("DELETE FROM " + tablename + " ");

            ArrayList<Filter> filters = new ArrayList<Filter>();
            List<Object> filterArgs = new ArrayList<Object>();

            for (String pkId : pkIds_ValuesMap.keySet()) {
                EqFilter eqFil = new EqFilter(pkId, pkIds_ValuesMap.get(pkId));
                filters.add(eqFil);
                filterArgs.add(pkIds_ValuesMap.get(pkId));
            }

            addWhereClauses(deleteFromStr, filters);
            log.debug("DELETE(SQL string w/ placeholders): \n" + deleteFromStr);
            PreparedStatement pStmt = con.prepareStatement(deleteFromStr.toString());
            setArguments(filterArgs, pStmt);
            log.debug("DELETE(pstmt w/ values): \n" + pStmt);
            return pStmt;
        }
    }


    /*
    TODO: improve code re-use...or modify...took this from JdbcOeDataSource, but only changed
    lines involving the tablename since using this recursively for child tables...
    ....might just need to change DatasourceDefinitions to always .setTableName()...once that is
    the convention, should be able to get rid of this and just inherit the detailsQuery from the JdbcOeDataSource
    */
    private class MultiTableInsertPreparedStatementCreator implements PreparedStatementCreator {

        private final String tableName;
        private final Map<String, Object> values;
        private final ArrayList<String> dimIds;
        private final Map<String, Dimension> editDims;
        private final boolean ignoreSpecialSql;

        public MultiTableInsertPreparedStatementCreator(String tableName, boolean ignoreSpecialSql, List<String> dimIds,
                                                        Map<String, Dimension> editDims, Map<String, Object> values) {
            this.values = values;
            this.editDims = editDims;
            this.dimIds = new ArrayList<String>(dimIds);
            this.tableName = tableName;
            this.ignoreSpecialSql = ignoreSpecialSql;
        }


        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            // pattern: "INSERT INTO tableX (col1, col2, col3, col4, colN) "
            StringBuilder sqlInsertStr = new StringBuilder("INSERT INTO " + tableName + " (");

            // pattern: "VALUES (?, ?, ?, ?, ?, ?, ?)"
            StringBuilder sqlParmStr = new StringBuilder(" VALUES (");

            // build up SQL string of pattern: "INSERT INTO TABLEx (COL1, ..., COLN) VALUES (?, ..., ?N)"
            @SuppressWarnings("unused") StringBuilder query = new StringBuilder();
            List<String> deleteList = new ArrayList<String>();
            boolean isFirst = true;

            for (String dimId : dimIds) {

                if (isFirst) {
                    isFirst = false;
                } else {
                    sqlInsertStr.append(", ");
                    sqlParmStr.append(", ");
                }

                // adds column names to INSERT
                sqlInsertStr.append(editDims.get(dimId).getSqlCol());     // col1, col2, col3, colN

                // build up value string for edit Dims.
                // if editDim has specialSql, append the specialSql value (instead of a ?) and put editDim on "deleteList"
                // so the switch-statement doesn't try to convert2SqlType(specialSqlvalue) -- specialSqlValue will be a string literal
                if (!ignoreSpecialSql && editDims.get(dimId).hasSpecialSql()
                    || editDims.get(dimId).getSpecialSql() != null) {
                    sqlParmStr.append(values.get(dimId));  //the special Sql
                    deleteList.add(dimId);
                } else {
                    sqlParmStr.append("?");  // ?, ?, ?, ?N
                }
            }

            // still need to set valus for '?' placeholders. this takes off dimIds where we already substituted specialSql values
            dimIds.removeAll(deleteList);

            sqlInsertStr.append(")");
            sqlParmStr.append(")");
            sqlInsertStr.append(sqlParmStr);
            log.debug("INSERT(string w placeholders): \n" + sqlInsertStr);

            //TODO: http://archives.postgresql.org/pgsql-jdbc/2010-04/msg00061.php -- postgresql just tacks on RETURNING * after query in PGSQL 8.4
            PreparedStatement
                    insertPstmt =
                    con.prepareStatement(sqlInsertStr.toString(), Statement.RETURN_GENERATED_KEYS);
            log.debug("INSERT(pstmt w params): \n" + insertPstmt);

            //set pstmt's '?' placeholders with values of the correct sql type
            setArgumentsOnSqlType(insertPstmt, dimIds, values);

            log.debug("INSERT(pstmt with vals): \n" + insertPstmt);
            return insertPstmt;
        }
    }


    /* TODO: improve code re-use...or modify...took from JdbcOeDataSource but ADDED superEditMap and editDims check for the existence of the beans...
    */
    public Collection<TableAwareQueryRecord> editableDetailsQuery(Collection<Dimension> editDims,
                                                                  Collection<Filter> filters, String tableName)
            throws OeDataSourceException {

        if (jdbcTemplate == null) {
            throw new OeDataSourceException("No JDBC Template configured");
        }

        if (editDims == null) {
            throw new OeDataSourceException("Edits cannot be null");
        }

        if (filters == null) {
            throw new OeDataSourceException("Filters cannot be null");
        }

        if (editDims.size() == 0) {
            throw new OeDataSourceException("Cannot have 0 Edit dimensions.");
        }

       /* TODO...do we care about ResultDims anymore for edit functionality? // now check if each result dimension is okay.
        for(Dimension d : editDims) {
			if(resultMap == null) {
				throw new OeDataSourceException(
						"Unrecognized result dimension " + d.getId());
			}

			DimensionBean bean = resultMap.get(d.getId());
			if (bean == null) {
				throw new OeDataSourceException(
						"Unrecognized result dimension " + d.getId());
			}
		}	*/

        // todo - can replace editDims check above...
        if (superEditMap == null) {
            throw new OeDataSourceException("Edits cannot be null");
        }

        // now check if each edit dimension is okay.
        for (Dimension d : editDims) {
            DimensionBean bean = superEditMap.get(d.getId());
            if (bean == null) {
                throw new OeDataSourceException("Unrecognized edit dimension " + d.getId());
            }
        }

        checkFilters(filters);
        List<Object> arguments = getArguments(filters);

        return (Collection<TableAwareQueryRecord>) jdbcTemplate
                .query(new MultiTableSelectPreparedStatementCreator(editDims, filters, arguments, tableName),
                       new EditableDetailsRowMapper(new ArrayList<Dimension>(editDims), tableName));
    }

    private class MultiTableSelectPreparedStatementCreator implements PreparedStatementCreator {

        private final Collection<Dimension> edits;
        private final Collection<Filter> filters;
        private final List<Object> arguments;
        private final String tableName;

        public MultiTableSelectPreparedStatementCreator(Collection<Dimension> editDims, Collection<Filter> filters,
                                                        List<Object> arguments, String tableName) {
            this.edits = editDims;
            this.filters = filters;
            this.arguments = arguments;
            this.tableName = tableName;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            StringBuilder query = new StringBuilder("SELECT ");

            // first add all the edit(i.e. result) dimensions
            boolean first = true;
            for (Dimension d : edits) {
                if (first) {
                    first = false;
                } else {
                    query.append(", ");
                }

                query.append(superEditMap.get(d.getId()).getSqlCol());
            }
            query.append(" FROM ").append(tableName).append(" ");

            addWhereClauses(query, filters);
            log.debug("SELECT(SQL string w/ placeholders): \n" + query);
            PreparedStatement pStmt = con.prepareStatement(query.toString());
            setArguments(arguments, pStmt);
            log.info("SELECT(pstmt w/ args): \n" + pStmt);
            return pStmt;
        }
    }

    private class EditableDetailsRowMapper implements RowMapper {

        private final List<Dimension> dimensions;
        private final String tableName;

        public EditableDetailsRowMapper(List<Dimension> dimensions, String tableName) {
            this.dimensions = dimensions;
            this.tableName = tableName;
        }

        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            return createRecord(dimensions, tableName, rs);
        }
    }

    /***************************************************************************************************************
     * utility and helper methods
     ***************************************************************************************************************/

    /**
     * Helper method for the ADD* related methods where the children records need fkeys that match the already inserted
     * parent record.
     *
     * @param tableName      table to get generated keys
     * @param keyIds         the complete known set of pk ids on the table
     * @param autoGenKeyVals any of the known pk values--i.e. ones generated by dbms, or provided by user calling
     *                       addCompleteRecord
     * @return all the pkeys and their values--supports "compound pkeys"
     * @throws OeDataSourceException if filter dimensions not configured properly
     */
    private List<Map> getAllGeneratedKeys(String tableName, Collection<String> keyIds, DbKeyValMap autoGenKeyVals)
            throws OeDataSourceException, OeDataSourceAccessException {
        if (isMySqlDBMS()) {
            // FOR MYSQL ONLY -- THE COLUMN NAME GETS OVERRITTEN AS "GENERATED_KEY" -- MYSQL ALLOWS ONLY 1 AUTO-INC COLUMN
            if (autoGenKeyVals.containsKey("GENERATED_KEY")) {
                Object autogenkeyval = autoGenKeyVals.get("GENERATED_KEY");
                autoGenKeyVals.clear();
                Set<String> pkids = new HashSet<String>(this.getParentTableDetails().getPks());
                Set<String> autogendids = this.getAutoGenMap().keySet();

                pkids.retainAll(autogendids); //just want the ONE auto-inc on the master table
                // todo - NPE CHECKING!
                autoGenKeyVals.put(pkids.iterator().next(), autogenkeyval);
            }
        } else if (isPostgreSqlDBMS()) {
            // do nothing. nothing altered about key column names.
        }

        boolean first = true;
        StringBuilder whereClause = new StringBuilder(" WHERE ");
        for (String keyId : autoGenKeyVals.keySet()) {
            if (first) {
                first = false;
            } else {
                whereClause.append(" AND ");
            }
            try {
                whereClause.append(getFilterDimension(keyId).getSqlCol()).append(" = ").append("'")
                        .append(DataTypeConversionHelper.convert2SqlType(autoGenKeyVals.get(keyId))).append("'");
            } catch (NullPointerException e) {
                throw new OeDataSourceException(
                        "This primary key designated dimension has not been configured as a FilterDimension: " + keyId,
                        e);
            }
        }

        // BUILDING UP: "SELECT PK1, PK2, PK3, ... PKn FROM table WHERE GENKEY1 = VAL1, GENKEY2 = VAL2, ... GENKEYN = VALn"
        List<String> columnIdList = new ArrayList<String>();
        StringBuilder select = new StringBuilder("SELECT ");
        first = true;

        for (String keyId : keyIds) {
            if (first) {
                first = false;
            } else {
                select.append(",");
            }
            try {
                select.append(getEditDimension(keyId).getSqlCol());
                columnIdList.add(keyId);
            } catch (NullPointerException e) {
                throw new OeDataSourceException(
                        "This primary key designated dimension has not been configured as a EditDimension: " + keyId,
                        e);
            }
        }

        select.append(" FROM ").append(tableName).append(" ");
        select.append(whereClause);

        log.debug("SELECT (string) FOR GETTING ALL GENERATED KEYS: \n" + select);
        return this.jdbcTemplate.query(select.toString(), new GeneratedKeysRowMapper(columnIdList));
    }

    private class GeneratedKeysRowMapper implements RowMapper {

        List<String> columnList;
        @SuppressWarnings("unused")
        DbKeyValMap keyvalmap;

        public GeneratedKeysRowMapper(List<String> columnList) {
            this.columnList = columnList;
        }

        /**
         * Implementations must implement this method to map each row of data in the ResultSet. This method should not call
         * <code>next()</code> on the ResultSet; it is only supposed to map values of the current row.
         *
         * @param rs     the ResultSet to map (pre-initialized for the current row)
         * @param rowNum the number of the current row
         * @return the result object for the current row
         * @throws java.sql.SQLException if a SQLException is encountered getting column values (that is, there's no need to
         *                               catch SQLException)
         */
        @Override
        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> values = new LinkedHashMap<String, Object>(columnList.size());
            int i = 0;
            for (String keyId : columnList) {
                i++;
                values.put(keyId, DataTypeConversionHelper.convert2JavaType(rs, i));
            }
            return values;
        }
    }

    /**
     * Takes collection of db-key/value pairings from the original and replacement records. Returns a map of
     * sqlOperation/list of db-key/value pairings. The 3 sqlOperations are "INSERT" "UPDATE" and "DELETE." Basic logic is
     * this:
     *
     * UPDATE - pk/val exists in original AND replacement record INSERT - pk/val exists in replacement record but NOT in
     * original record DELETE - pk/val exists in origianal record but NOT in replacement record
     *
     * @param originalPks    original record's set of (dbkeys<->values)
     * @param replacementPks replacement record's set of (dbkeys<->values)
     * @return categorization of the original and replacement records' (dbkeys<->values) into INSERT, UPDATE, DELETE
     *         operations
     */
    public Map<String, List<Map<String, Object>>> categorizePKs(Set<Map<String, Object>> originalPks,
                                                                Set<Map<String, Object>> replacementPks) {
        Set<Map<String, Object>> ORIG = originalPks;
        Set<Map<String, Object>> REPL = replacementPks;

        List<Map<String, Object>> UPDATE = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> INSERT = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> DELETE = new ArrayList<Map<String, Object>>();

        UPDATE.addAll(CollectionUtils.intersection(ORIG, REPL));
        INSERT.addAll(CollectionUtils.subtract(REPL, ORIG));
        DELETE.addAll(CollectionUtils.subtract(ORIG, REPL));

        Map<String, List<Map<String, Object>>> categorizedPKs = new HashMap<String, List<Map<String, Object>>>();
        categorizedPKs.put("UPDATE", UPDATE);
        categorizedPKs.put("INSERT", INSERT);
        categorizedPKs.put("DELETE", DELETE);

        log.debug("pks for update: " + UPDATE);
        log.debug("pks for insertion: " + INSERT);
        log.debug("pks for deletion: " + DELETE);
        return categorizedPKs;
    }

    private Map<String, List<Map<String, Object>>> categorizeChildrenRecords(ChildRecordSet origRecordSet,
                                                                             ChildRecordSet replRecordSet) {
        Map<String, List<Map<String, Object>>>
                categorized =
                categorizePKs(origRecordSet.getPksValsToRecordMap().keySet(),
                              replRecordSet.getPksValsToRecordMap().keySet());

        return categorized;
    }

    private void performSqlonChildRecord(Map<String, List<Map<String, Object>>> categorizedPKs,
                                         Map<Map<String, Object>, TableAwareQueryRecord> pkeys_OrigRecord,
                                         Map<Map<String, Object>, TableAwareQueryRecord> pkeys_ReplRecord)
            throws OeDataSourceException, OeDataSourceAccessException {

        class SqlPerformingClass {

            Map<String, List<Map<String, Object>>> categorizedPKs;
            Map<Map<String, Object>, TableAwareQueryRecord> pkeys_OrigRecord;
            Map<Map<String, Object>, TableAwareQueryRecord> pkeys_ReplRecord;

            SqlPerformingClass(Map<String, List<Map<String, Object>>> categorizedPKs,
                               Map<Map<String, Object>, TableAwareQueryRecord> pkeys_OrigRecord,
                               Map<Map<String, Object>, TableAwareQueryRecord> pkeys_ReplRecord) {
                this.categorizedPKs = categorizedPKs;
                this.pkeys_OrigRecord = pkeys_OrigRecord;
                this.pkeys_ReplRecord = pkeys_ReplRecord;
            }

            public void performDelete() throws OeDataSourceAccessException {
                log.debug("local inner class performing DELETE");
                List<Map<String, Object>> pkDeleteList = categorizedPKs.get("DELETE");
                for (Map<String, Object> pk : pkDeleteList) {
                    try {
                        deleteQueryRecord(new DbKeyValMap(pk), pkeys_OrigRecord.get(pk).getTableName());
                    } catch (OeDataSourceAccessException e) {
                        throw new OeDataSourceAccessException("Problem occurred deleting child record on this update.",
                                                              e);
                    }
                }
            }

            public void performUpdate() throws OeDataSourceAccessException {
                log.debug("local inner class performing UPDATE");
                // process UPDATE list
                List<Map<String, Object>> pkUpdateList = categorizedPKs.get("UPDATE");
                for (Map<String, Object> pk : pkUpdateList) {

                    editableUpdateStatement(pkeys_ReplRecord.get(pk).getTableName(),
                                            new TableAwareQueryRecord(pkeys_ReplRecord.get(pk)),
                                            new HashMap<String, Object>(pk));
                }
            }

            public void performInsert() throws OeDataSourceException, OeDataSourceAccessException {
                log.debug("local inner class performing INSERT");
                // process INSERT list -- MUST REUSE SAME FKids!!
                List<Map<String, Object>> pkInsertList = categorizedPKs.get("INSERT");
                for (Map<String, Object> pk : pkInsertList) {
                    addChildRecord(new TableAwareQueryRecord(pkeys_ReplRecord.get(pk)), true);
                }
            }

        }

        // do not attempt to check a bad or incomplete key set, to avoid NPEs

        // pks of replacement record should only be of size 0 IFF all records in the child table are being deleted or don't exist anyway
        if (pkeys_ReplRecord != null && pkeys_ReplRecord.keySet().size() == 0) {
            if (categorizedPKs.get("DELETE") != null) {
                if (categorizedPKs.get("DELETE").size() == 0) {
                    // DO NOTHING
                    return;
                } else {
                    // DELETE ONLY. UPDATES AND INSERTS SHOULD NOT OCCUR
                    new SqlPerformingClass(categorizedPKs, pkeys_OrigRecord, null).performDelete();
                }
            }
        } else {
            // make sure all the keys have been categorized!!!!! before preceding to the processing below. //todo is this solid logic?
            Map<String, Object> pksForRecord = pkeys_ReplRecord.keySet().iterator().next();
            if (!((categorizedPKs.get("UPDATE").contains(pksForRecord)) || (categorizedPKs.get("INSERT")
                                                                                    .contains(pksForRecord)) ||
                  (categorizedPKs.get("DELETE").contains(pksForRecord)))) {
                throw new OeDataSourceException("Uncategorized set of PKs for this Record!!!");
            }
        }

        new SqlPerformingClass(categorizedPKs, pkeys_OrigRecord, pkeys_ReplRecord).performUpdate();
        new SqlPerformingClass(categorizedPKs, pkeys_OrigRecord, pkeys_ReplRecord).performInsert();
        new SqlPerformingClass(categorizedPKs, pkeys_OrigRecord, null).performDelete();


    }

    public boolean isVersioningMode() {
        return this.isVersioningMode;
    }

    /***************************************************************************************************************
     * Overriden methods found in JdbcOeDataSource
     ***************************************************************************************************************/

    /**
     * Builds a Record from the result set
     *
     * @param queryDimensions dimensions that compose the record
     * @param tableName       name of table the record corresponds to
     * @param rs              result set
     * @return built up Record
     * @throws SQLException if problem occurrs processing result set.
     */
    protected Record createRecord(List<Dimension> queryDimensions, String tableName, ResultSet rs) throws SQLException {
        return createRecord(queryDimensions, tableName, rs, 0);
    }


    protected Record createRecord(List<Dimension> queryDimensions, String tablename, ResultSet rs, int offset)
            throws SQLException {
        Map<String, Dimension> dimensions = new LinkedHashMap<String, Dimension>(queryDimensions.size());
        Map<String, Object> values = new LinkedHashMap<String, Object>(queryDimensions.size());

        Set<String> pkIds = new HashSet<String>();
        if (tablename.equals(parentTableDetails.getTableName())) {
            pkIds = parentTableDetails.getPks();
        } else if (childTableMap.get(tablename) != null) {
            pkIds = childTableMap.get(tablename).getPks();
        }

        for (int i = 0; i < queryDimensions.size(); i += 1) {
            Dimension d = queryDimensions.get(i);
            dimensions.put(d.getId(), d);
            values.put(d.getId(), DataTypeConversionHelper.convert2JavaType(rs, d.getSqlType(), i + 1 + offset));
        }

        //NOTE: returns TableAwareQueryRecord (vs. Record in the JdbcOeDataSource base class)
        return new TableAwareQueryRecord(tablename, pkIds, dimensions, values);
    }

    @Override
    public Dimension getAutoGeneratedDimension(String id) {
        if (autoGenMap != null) {

            if (autoGenMap.get(id) != null) {
                return new DimensionBeanAdapter(autoGenMap.get(id), this);
            }
            return null;
        } else {
            return null;
        }
    }


    @Override
    public Collection<Dimension> getAutoGeneratedDimensions() {
        if (autoGenMap != null) {
            return getDimension(autoGenMap.values(), new DimBeanExec<Dimension>() {
                @Override
                public Dimension exec(DimensionBean b) {
                    return new DimensionBeanAdapter(b, JdbcOeDataEntrySource.this);
                }
            });
        } else {
            return new ArrayList<Dimension>(0);
        }
    }


    @Override
    public Dimension getEditDimension(String id) {
        if (superEditMap != null) {
            if (superEditMap.get(id) != null) {
                return new DimensionBeanAdapter(superEditMap.get(id), this);
            }
            return null;
        } else {
            return null;
        }
    }

    @Override
    public Collection<Dimension> getEditDimensions() {
        if (editMap != null) {
            return getDimension(editMap.values(), new DimBeanExec<Dimension>() {
                @Override
                public Dimension exec(DimensionBean b) {
                    return new DimensionBeanAdapter(b, JdbcOeDataEntrySource.this);
                }
            });
        } else {
            return new ArrayList<Dimension>(0);
        }
    }

    @Override
    public Collection<Dimension> getAllEditDimensions() {
        if (superEditMap != null) {
            return getDimension(superEditMap.values(), new DimBeanExec<Dimension>() {
                @Override
                public Dimension exec(DimensionBean b) {
                    return new DimensionBeanAdapter(b, JdbcOeDataEntrySource.this);
                }
            });
        } else {
            return new ArrayList<Dimension>(0);
        }
    }

    /**
     * Sets arguments of the proper type on a PreparedSatement
     *
     * @param pStmt     prepared statement on which to set arguments
     * @param dimIds    dimension ids that map to columns on the prepared statement
     * @param valueList values to set for the paramenters on the prepared statement
     * @throws SQLException if error occurs while processing the prepared statement
     */
    protected void setArgumentsOnSqlType(PreparedStatement pStmt, List<String> dimIds, List<Object> valueList)
            throws SQLException {
        try {
            setArgumentsOnSqlType(pStmt, dimIds, valueList, null);
        } catch (OeDataSourceException e) {
            throw new SQLException("Issue occurred while setting parameters on the prepared statement.", e);
        }
    }

    /**
     * Sets arguments of the proper type on a PreparedSatement
     *
     * @param pStmt    prepared statement on which to set arguments
     * @param dimIds   dimension ids that map to columns on the prepared statement
     * @param valueMap dimIds mapped to values to set for the paramenters on the prepared statement
     * @throws SQLException if error occurs while processing the prepared statement
     */
    protected void setArgumentsOnSqlType(PreparedStatement pStmt, List<String> dimIds, Map<String, Object> valueMap)
            throws SQLException {
        try {
            setArgumentsOnSqlType(pStmt, dimIds, null, valueMap);
        } catch (OeDataSourceException e) {
            throw new SQLException("Issue occurred while setting parameters on the prepared statement.", e);
        }
    }

    /**
     * Sets arguments of the proper type on a PreparedSatement
     *
     * @param pStmt     prepared statement on which to set arguments
     * @param dimIds    dimension ids that map to columns on the prepared statement
     * @param valueList values to set for the paramenters on the prepared statement
     * @param valueMap  dimIds mapped to values to set for the paramenters on the prepared statement
     * @throws SQLException          if error occurs while processing the prepared statement
     * @throws OeDataSourceException if error occurs converting value to it's sql type
     */
    protected void setArgumentsOnSqlType(PreparedStatement pStmt, List<String> dimIds, List<Object> valueList,
                                         Map<String, Object> valueMap) throws SQLException, OeDataSourceException {

        if ((valueList == null && valueMap == null) || (valueList != null && valueMap != null)) {
            throw new OeDataSourceException("Invalid value lists. Only one form of the list can be provided.");
        }

        int argCount = 0;
        Object val;
        boolean isValueList = (valueList != null);

        for (String dimId : dimIds) {
            Dimension dim = getEditDimension(dimId);
            FieldType sqlType = dim.getSqlType();

            if (isValueList) {
                val = valueList.get(argCount);
            } else {
                val = valueMap.get(dimId);
            }

            try {
                argCount++;
                switch (sqlType) {
                    case DATE_TIME:
                        pStmt.setObject(argCount, DataTypeConversionHelper.convert2SqlTimestampType(val),
                                        Types.TIMESTAMP);
                        continue;
                    case DATE:
                        pStmt.setObject(argCount, DataTypeConversionHelper.convert2SqlType(val), Types.DATE);
                        continue;
                    case BOOLEAN:
                        pStmt.setObject(argCount, DataTypeConversionHelper.convert2SqlType(val), Types.BOOLEAN);
                        continue;
                    case FLOAT:
                        pStmt.setObject(argCount, DataTypeConversionHelper.convert2SqlType(val), Types.FLOAT);
                        continue;
                    case DOUBLE:
                        pStmt.setObject(argCount, DataTypeConversionHelper.convert2SqlType(val), Types.DOUBLE);
                        continue;
                    case INTEGER:
                        pStmt.setObject(argCount, DataTypeConversionHelper.convert2SqlType(val), Types.INTEGER);
                        continue;
                    case LONG:
                        pStmt.setObject(argCount, DataTypeConversionHelper.convert2SqlType(val), Types.BIGINT);
                        continue;
                    case TEXT:
                        pStmt.setObject(argCount, DataTypeConversionHelper.convert2SqlType(val), Types.VARCHAR);
                        continue;
                    default:
                        throw new AssertionError("Unexpected sqlType " + sqlType + ".");
                }

            } catch (OeDataSourceException e) {
                throw new SQLException("Error occured converting value \"" + val + "\" to its sql type.", e);
            }
        }
    }
}
