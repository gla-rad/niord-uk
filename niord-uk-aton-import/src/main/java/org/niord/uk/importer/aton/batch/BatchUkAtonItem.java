/*
 * Copyright (c) 2023 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.niord.uk.importer.aton.batch;

import org.apache.poi.ss.usermodel.Row;

import java.util.Map;

/**
 * Encapsulates a row in an Excel sheet + an index of column indexes.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@SuppressWarnings("unused")
public class BatchUkAtonItem {
    Map<String, Integer> colIndex;
    Row row;

    /** Constructor */
    public BatchUkAtonItem() {
    }

    /** Constructor */
    public BatchUkAtonItem(Map<String, Integer> colIndex, Row row) {
        this.colIndex = colIndex;
        this.row = row;
    }

    /**
     * Gets col index.
     *
     * @return the col index
     */
    public Map<String, Integer> getColIndex() {
        return colIndex;
    }

    /**
     * Sets col index.
     *
     * @param colIndex the col index
     */
    public void setColIndex(Map<String, Integer> colIndex) {
        this.colIndex = colIndex;
    }

    /**
     * Gets row.
     *
     * @return the row
     */
    public Row getRow() {
        return row;
    }

    /**
     * Sets row.
     *
     * @param row the row
     */
    public void setRow(Row row) {
        this.row = row;
    }
}
