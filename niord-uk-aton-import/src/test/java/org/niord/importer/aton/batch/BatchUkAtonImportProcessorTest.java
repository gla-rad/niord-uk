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

package org.niord.importer.aton.batch;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Set;

public class BatchUkAtonImportProcessorTest extends TestCase {

    @Test
    public void testParseAtonTypes() {
        BatchUkAtonImportProcessor processor = new BatchUkAtonImportProcessor();
        Set<AtonType> atonTypes = processor.parseAidsTypes("AIS/Racon");

        assertNotNull(atonTypes);
        assertEquals(2, atonTypes.size());
        assertTrue(atonTypes.contains(AtonType.AIS));
        assertTrue(atonTypes.contains(AtonType.RACON));
    }

}