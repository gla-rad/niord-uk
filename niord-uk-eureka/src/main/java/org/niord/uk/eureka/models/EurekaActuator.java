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


package org.niord.uk.eureka.models;

import org.niord.model.IJsonSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Eureka actuator.
 */
public class EurekaActuator implements IJsonSerializable {

    // Class Variables
    private Map<String, EurekaLink> _links;

    /**
     * Instantiates a new Eureka actuator.
     */
    public EurekaActuator() {
        this._links = new HashMap<>();
    }

    /**
     * Gets links.
     *
     * @return the links
     */
    public Map<String, EurekaLink> get_links() {
        return _links;
    }

    /**
     * Sets links.
     *
     * @param _links the links
     */
    public void set_links(Map<String, EurekaLink> _links) {
        this._links = _links;
    }

    /**
     * Add link.
     *
     * @param id    the id
     * @param value the link value
     */
    public void addLink(String id, EurekaLink value) {
        this._links.put(id, value);
    }
}
