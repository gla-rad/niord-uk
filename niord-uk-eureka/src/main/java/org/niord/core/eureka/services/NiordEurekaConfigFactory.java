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

package org.niord.core.eureka.services;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.EurekaClientConfig;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * THe Niord Eureka Config Factory
 *
 * To allow the eureka client to function properly we need to declare some
 * configuration parameters as beans so that they get picked up correctly.
 * This was mainly an issue during the dev profile live reloading but now
 * seems OK.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@ApplicationScoped
public class NiordEurekaConfigFactory {

    /**
     * Produce a data-center instance configuration from the eureka.properties
     * configuration file.
     *
     * @return the eureka instance configuration
     */
    @Produces
    EurekaInstanceConfig eurekaInstanceConfig() {
        return new MyDataCenterInstanceConfig();
    }

    /**
     * Produce a eureka instance information beans based on the instance
     * configuration provided.
     *
     * @param eurekaInstanceConfig  the eureka instance configuration
     * @return the instance information
     */
    @Produces
    InstanceInfo instanceInfo(EurekaInstanceConfig eurekaInstanceConfig) {
        return new EurekaConfigBasedInstanceInfoProvider(eurekaInstanceConfig).get();
    }

    /**
     * Produce the eureka application information manager optional arguments
     * bean.
     *
     * @return the optional arguments bean
     */
    @Produces
    ApplicationInfoManager.OptionalArgs optionalArgs() {
        return new ApplicationInfoManager.OptionalArgs();
    }

    /**
     * Produce a default eureka client configuration.
     *
     * @return the eureka client configuration
     */
    @Produces
    EurekaClientConfig eurekaClientConfig() {
        return new DefaultEurekaClientConfig();
    }

}
