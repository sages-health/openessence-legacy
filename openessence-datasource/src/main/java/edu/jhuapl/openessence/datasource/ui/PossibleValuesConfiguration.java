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

package edu.jhuapl.openessence.datasource.ui;

import edu.jhuapl.openessence.datasource.Dimension;
import edu.jhuapl.openessence.datasource.OeDataSource;
import edu.jhuapl.openessence.datasource.OeDataSourceException;
import edu.jhuapl.openessence.datasource.entry.ChildTableDetails;
import edu.jhuapl.openessence.datasource.entry.OeDataEntrySource;
import edu.jhuapl.openessence.datasource.jdbc.DimensionBean;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PossibleValuesConfiguration {

    private String dsId;
    private Collection<String> keys;
    private Map<String, String> fks;
    private List<List<Object>> data;

    /**
     * TODO: Rename these, currently matches getFields...
     */
    private List<DimensionConfiguration> editDimensions;
    private List<DimensionConfiguration> detailDimensions;
    private List<DimensionConfiguration> filters;

    public PossibleValuesConfiguration(final List<List<Object>> data) {
        setData(data);
    }

    public PossibleValuesConfiguration(final DimensionBean bean) {
        setData(bean.getPossibleValuesDsData());
    }

    public PossibleValuesConfiguration(final DimensionBean bean, final OeDataSource oeds) throws OeDataSourceException {
        setDsId(bean.getPossibleValuesDsName());

        setResultDimensions(oeds.getResultDimensions(), bean.getPossibleValuesDsResults());
        setFilterDimensions(oeds.getFilterDimensions(), bean.getPossibleValuesDsFilters());
        //check if possible values datasource is an entry datasource
        if (oeds instanceof OeDataEntrySource) {
            OeDataEntrySource oedes = (OeDataEntrySource) oeds;
            setKeys(oedes.getParentTableDetails().getPks());
            //TODO do we need to add child dimensions of the possible values ds?
            setEditDimensions(oedes.getEditDimensions());
        }
    }

    public PossibleValuesConfiguration(final ChildTableDetails ctd, final OeDataSource oeds)
            throws OeDataSourceException {
        setDsId(ctd.getPossibleValuesDsName());
        setFks(ctd.getPossibleValuesDsFks());
        setResultDimensions(oeds.getResultDimensions(), ctd.getPossibleValuesDsResults());
        setFilterDimensions(oeds.getFilterDimensions(), ctd.getPossibleValuesDsFilters());
        //check if possible values datasource is an entry datasource
        if (oeds instanceof OeDataEntrySource) {
            OeDataEntrySource oedes = (OeDataEntrySource) oeds;
            setKeys(oedes.getParentTableDetails().getPks());
            setEditDimensions(oedes.getAllEditDimensions());
        }
    }

    public String getDsId() {
        return dsId;
    }

    public void setDsId(String dsId) {
        this.dsId = dsId;
    }

    @JsonProperty("detailDimensions") // for backwards compatibility with client
    public List<DimensionConfiguration> getResultDimensions() {
        return detailDimensions;
    }

    @JsonProperty("editDimensions") // for backwards compatibility with client
    public List<DimensionConfiguration> getEditDimensions() {
        return editDimensions;
    }

    @JsonProperty("filters")
    public List<DimensionConfiguration> getFilterDimensions() {
        return filters;
    }

    /**
     * Set EditDimensions to be all possible edit dimensions determined by the possible values datasource.
     */
    public void setEditDimensions(Collection<? extends Dimension> pvEditDimensions) throws OeDataSourceException {
        this.editDimensions = editDimensionsHelper(pvEditDimensions);
    }

    public void setResultDimensions(Collection<? extends Dimension> dimensions, List<String> pvDimensions)
            throws OeDataSourceException {
        this.detailDimensions = pvDimensionsHelper(dimensions, pvDimensions);
    }

    public void setFilterDimensions(Collection<? extends Dimension> dimensions, List<String> pvDimensions)
            throws OeDataSourceException {
        this.filters = pvDimensionsHelper(dimensions, pvDimensions);
    }

    private List<DimensionConfiguration> pvDimensionsHelper(Collection<? extends Dimension> dimensions,
                                                            List<String> pvDimensions) throws OeDataSourceException {
        final List<DimensionConfiguration> results = new ArrayList<DimensionConfiguration>();

        for (final Dimension dimension : dimensions) {
            // If pv are not defined allow all, else filter to list
            if (pvDimensions == null || pvDimensions.contains(dimension.getId())) {
                results.add(new DimensionConfiguration(dimension));
            }
        }

        return results;
    }

    private List<DimensionConfiguration> editDimensionsHelper(Collection<? extends Dimension> dimensions)
            throws OeDataSourceException {
        final List<DimensionConfiguration> results = new ArrayList<DimensionConfiguration>();
        for (final Dimension dimension : dimensions) {
            results.add(new DimensionConfiguration(dimension));
        }
        return results;
    }

    public void setKeys(Collection<String> keys) {
        this.keys = keys;
    }

    public Collection<String> getKeys() {
        return keys;
    }

    public void setFks(Map<String, String> fks) {
        this.fks = fks;
    }

    public Map<String, String> getFks() {
        return fks;
    }

    public void setData(List<List<Object>> data) {
        this.data = data;
    }

    public List<List<Object>> getData() {
        return data;
    }
}
