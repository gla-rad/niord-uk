/*
 * Copyright 2016 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.niord.importer.aton.batch;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

/**
 * Reads AtoNs from Excel.
 */
@Dependent
@Named("batchUkAtonImportReader")
public class BatchUkAtonImportReader extends AbstractUkAtonImportReader {

    // Define the field names
    public static final String AREA = "Area";
    public static final String NAME = "Name";
    public static final String TYPE = "Type";
    public static final String LAT = "Latitude";
    public static final String LON = "Longitude";
    public static final String CHARACTER = "Character";
    public static final String RANGE = "Range";
    public static final String FOG_SIGNALS = "HWS";
    public static final String BUOY_TYPE = "Inter-GLA Buoy Type";
    public static final String RADIO_AIDS = "Radio Aids";
    public static final String COMMENT = "Comment";
    public static final String DESIGN_CODE = "TH Design Code";
    public static final String MMSI = "MMSI";
    public static final String[] FIELDS = {
            AREA,
            NAME,
            TYPE,
            LAT,
            LON,
            CHARACTER,
            RANGE,
            FOG_SIGNALS,
            BUOY_TYPE,
            RADIO_AIDS,
            COMMENT,
            DESIGN_CODE,
            MMSI
    };

    /** {@inheritDoc} **/
    @Override
    public String[] getFields() {
        return FIELDS;
    }
}
