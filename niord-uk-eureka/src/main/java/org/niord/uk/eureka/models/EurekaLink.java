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

import java.util.ArrayList;
import java.util.List;

/**
 * The Eureka Health Class.
 * <p>
 * This class implements the return object for the eureka link values.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class EurekaLink implements IJsonSerializable {

    // Class Variables
    private List<String> href;
    private boolean templated;

    /**
     * Instantiates a new Eureka link.
     */
    public EurekaLink() {
        href = new ArrayList<>();
        templated = false;
    }

    /**
     * Instantiates a new Eureka link.
     *
     * @param href      the href
     * @param templated the templated
     */
    public EurekaLink(List<String> href, boolean templated) {
        this.href = href;
        this.templated = templated;
    }

    /**
     * Gets href.
     *
     * @return the href
     */
    public List<String> getHref() {
        return href;
    }

    /**
     * Sets href.
     *
     * @param href the href
     */
    public void setHref(List<String> href) {
        this.href = href;
    }

    /**
     * Is templated boolean.
     *
     * @return the boolean
     */
    public boolean isTemplated() {
        return templated;
    }

    /**
     * Sets templated.
     *
     * @param templated the templated
     */
    public void setTemplated(boolean templated) {
        this.templated = templated;
    }
}
