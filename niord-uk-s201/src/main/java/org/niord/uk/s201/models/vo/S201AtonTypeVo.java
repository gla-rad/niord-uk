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
package org.niord.uk.s201.models.vo;

import org.niord.uk.s201.models.S201AtonTypes;

import java.util.List;

/**
 * The S-201 AtoN Feature Type Vo Class.
 * <p>
 * This is a VO object to transfer the information about the currently
 * supported S-201 feature type entries.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class S201AtonTypeVo {

    // Class Variables
    private String name;
    private String description;
    private boolean equipment;
    private List<String> josmNodeTypes;

    /**
     * Constructor based on a provided S-125 AtoN Types enum entry.
     *
     * @param s125AtonTypes the S-125 AtoN Types enum entry
     */
    public S201AtonTypeVo(S201AtonTypes s125AtonTypes) {
        this.name = s125AtonTypes.getName();
        this.description = s125AtonTypes.getDescription();
        this.equipment = s125AtonTypes.isEquipment();
        this.josmNodeTypes = s125AtonTypes.getJosmNodeTypes();
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Is equipment boolean.
     *
     * @return the boolean
     */
    public boolean isEquipment() {
        return equipment;
    }

    /**
     * Sets equipment.
     *
     * @param equipment the equipment
     */
    public void setEquipment(boolean equipment) {
        this.equipment = equipment;
    }

    /**
     * Gets josm node types.
     *
     * @return the josm node types
     */
    public List<String> getJosmNodeTypes() {
        return josmNodeTypes;
    }

    /**
     * Sets josm node types.
     *
     * @param josmNodeTypes the josm node types
     */
    public void setJosmNodeTypes(List<String> josmNodeTypes) {
        this.josmNodeTypes = josmNodeTypes;
    }

    /**
     * Translates the object back into the S-201 AtoN Types enum.
     *
     * @return the respective S-201 AtoN Types enum entry
     */
    public S201AtonTypes getS125AtonType() {
        return S201AtonTypes.fromSeamarkType(this.getName());
    }
}
