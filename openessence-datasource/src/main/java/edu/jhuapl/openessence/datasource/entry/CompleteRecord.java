package edu.jhuapl.openessence.datasource.entry;

import edu.jhuapl.openessence.datasource.jdbc.entry.TableAwareQueryRecord;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds all data for a parent record and it's possible children records
 */
public class CompleteRecord {

    private TableAwareQueryRecord parentRecord;
    private Map<String, ChildRecordSet> tableNameToChildRecordSet;

    /**
     * Constructor for CompleteRecord that is initialized with the parent record. Other attributes are empty.
     *
     * @param parentRecord TableAwareQueryRecord that serves as "parent" record
     */
    public CompleteRecord(TableAwareQueryRecord parentRecord) {
        this.parentRecord = parentRecord;
        this.tableNameToChildRecordSet = new HashMap<String, ChildRecordSet>();
    }

    /**
     * Constructs a CompleteRecord from the parent record and collection of ChildRecordSets. ChildRecordSets are stored in
     * a map where the key is the corresponding childtable's name.
     *
     * @param parentRecord       TableAwareQueryRecord that serves as "parent" record
     * @param childrenRecordSets ChildRecordSets of children records
     */
    public CompleteRecord(TableAwareQueryRecord parentRecord, Collection<ChildRecordSet> childrenRecordSets) {
        this.parentRecord = parentRecord;
        this.tableNameToChildRecordSet = new HashMap<String, ChildRecordSet>();

        for (ChildRecordSet recordSet : childrenRecordSets) {
            tableNameToChildRecordSet.put(recordSet.getChildTableName(), recordSet);
        }
    }

    /**
     * Adds a ChildRecordSet to the complete record
     *
     * @param childRecordSet ChildRecordSet to add
     */
    public void addChildRecordSet(ChildRecordSet childRecordSet) {
        tableNameToChildRecordSet.put(childRecordSet.getChildTableName(), childRecordSet);
    }

    /**
     * Gets map of child table name to ChildRecordSet for the CompleteRecord
     *
     * @return map of childtable names to corresponding ChildRecordSets for the CompleteRecord
     */
    public Map<String, ChildRecordSet> getTableNameRecordSetMap() {
        return this.tableNameToChildRecordSet;
    }

    /**
     * Gets "parent" record for the CompleteRecord
     *
     * @return "parent" record
     */
    public TableAwareQueryRecord getParentRecord() {
        return parentRecord;
    }

    /**
     * Sets "parent" record for the CompleteRecord
     *
     * @param parentRecord "parent" record to set
     */
    public void setParentRecord(TableAwareQueryRecord parentRecord) {
        this.parentRecord = parentRecord;
    }

    /**
     * Gets sets of "children" records where each childrecordset corresponds to a table
     *
     * @return childrecordsets for a "parent's" children
     */
    public Collection<ChildRecordSet> getChildrenRecordSets() {
        return tableNameToChildRecordSet.values();
    }

    /**
     * Sets ChildrenRecordSets
     *
     * @param childrenRecordSets collection of childrenrecordsets to set
     */
    public void setChildrenRecordSets(Collection<ChildRecordSet> childrenRecordSets) {
        for (ChildRecordSet recordSet : childrenRecordSets) {
            tableNameToChildRecordSet.put(recordSet.getChildTableName(), recordSet);
        }
    }
}

