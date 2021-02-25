/*
 * Copyright (c) 2021 GLA UK Research and Development Directive
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.niord.core.eureka.models;

import com.netflix.appinfo.InstanceInfo;

/**
 * The Eureka Health Class.
 *
 * This class implements the return object for the eureka health actuator
 * endpoint.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
public class EurekaHealth {

    // Class Variables
    private InstanceInfo.InstanceStatus status;

    /**
     * Gets status.
     *
     * @return Value of status.
     */
    public InstanceInfo.InstanceStatus getStatus() {
        return status;
    }

    /**
     * Sets new status.
     *
     * @param status New value of status.
     */
    public void setStatus(InstanceInfo.InstanceStatus status) {
        this.status = status;
    }
}
