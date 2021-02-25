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

import org.apache.commons.lang.StringUtils;
import org.niord.core.aton.AtonTag;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for representing an OSM fog signal model
 *
 * @see <a href="http://wiki.openstreetmap.org/wiki/Seamarks/Fog_Signals">OSM Fog Signals Documentation</a>
 */
@SuppressWarnings("unused")
public class FogSignalSeamark {

    /*************************/
    /** Enums               **/
    /*************************/

    /** Fog signal types **/
    public enum Type {
        fog_signal
   }

    /** Fog signal categories **/
    public enum Category {
        explosive,
        diaphone,
        siren,
        nautophone,
        reed,
        tyfon,
        bell,
        whistle,
        gong,
        horn
    }

    /*************************/
    /** Variables           **/
    /*************************/

    Type type = Type.fog_signal;
    Category category;
    Double range;
    Integer frequency;
    String group;
    Double period;
    String sequence;

    /*************************/
    /** Functions           **/
    /*************************/

    /** Checks if the fog signal is valid */
    public boolean isValid() {
        return type != null && category != null;
    }

    /** Converts this entity to a list of AtoN tags **/
    public List<AtonTag> toOsm() {

        List<AtonTag> tags = new ArrayList<>();

        addAtonTag(tags, "seamark:type", type);
        addAtonTag(tags, "seamark:fog_signal:category", category);

        // For the next attributes, the documentation at http://wiki.openstreetmap.org/wiki/Seamarks/Fog_Signals
        // specifies using "seamark:light:<attribute>" keys. However, these may collide if we have a combined
        // light + fog-signal. So, I assume that it is an error, and htat we should use "seamark:fog_signal:<attribute>"
        addAtonTag(tags, "seamark:fog_signal:range", range);
        addAtonTag(tags, "seamark:fog_signal:frequency", frequency);
        addAtonTag(tags, "seamark:fog_signal:group", group);
        addAtonTag(tags, "seamark:fog_signal:period", period);
        addAtonTag(tags, "seamark:fog_signal:sequence", sequence);

        return tags;
    }

    /** Adds the tag if the value is well-defined */
    public static void addAtonTag(List<AtonTag> tags, String key, Object val) {
        String str = val != null ? val.toString() : null;
        if (StringUtils.isNotBlank(str)) {
            tags.add(new AtonTag(key, str));
        }
    }

    @Override
    public String toString() {
        return toOsm().toString();
    }

    /*************************/
    /** Getters and Setters **/
    /*************************/

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Double getRange() {
        return range;
    }

    public void setRange(Double range) {
        this.range = range;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Double getPeriod() {
        return period;
    }

    public void setPeriod(Double period) {
        this.period = period;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
}
