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

package org.niord.core.eureka.services;


import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.niord.core.eureka.models.EurekaHealth;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * The Eureka Client Implementation.
 */
@ApplicationScoped
public class EurekaClientService {

    @Inject
    Logger log;

    // Service Variables
    private static ApplicationInfoManager applicationInfoManager;
    private static EurekaClient eurekaClient;

    /**
     * Initialize the data store.
     */
    void init(@Observes StartupEvent ev) {
        applicationInfoManager = initializeApplicationInfoManager(new MyDataCenterInstanceConfig());
        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.STARTING);

        log.info("Initialising the Eureka Client...");

        eurekaClient = initializeEurekaClient(applicationInfoManager, new DefaultEurekaClientConfig());
        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.UP);

        log.info("Eureka Client now UP...");
    }

    /**
     * Clean up Lucene index.
     */
    void destroy(@Observes ShutdownEvent ev) {
        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.DOWN);
    }

    /**
     * Initialises the Netflix application info manager instance.
     *
     * @param instanceConfig the instance config
     * @return the initialised application info manager instance
     */
    protected static synchronized ApplicationInfoManager initializeApplicationInfoManager(EurekaInstanceConfig instanceConfig) {
        if (applicationInfoManager == null) {
            InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
            applicationInfoManager = new ApplicationInfoManager(instanceConfig, instanceInfo);
        }

        return applicationInfoManager;
    }

    /**
     * Initialises the Netflix eureka client instance.
     *
     * @param applicationInfoManager the application info manager
     * @param clientConfig the eureka client configuration
     * @return the eureka client
     */
    protected static synchronized EurekaClient initializeEurekaClient(ApplicationInfoManager applicationInfoManager, EurekaClientConfig clientConfig) {
        if (eurekaClient == null) {
            eurekaClient = new DiscoveryClient(applicationInfoManager, clientConfig);
        }

        return eurekaClient;
    }

    /**
     * Returns the current service status.
     *
     * @return the service status
     */
    public InstanceInfo.InstanceStatus getStatus() {
        return applicationInfoManager.getInfo().getStatus();
    }

    /**
     * Returns the current health status of the client.
     *
     * @return The current health status of the client
     */
    public EurekaHealth getHealth() {
        EurekaHealth eurekaHealth = new EurekaHealth();
        eurekaHealth.setStatus(applicationInfoManager.getInfo().getStatus());
        return eurekaHealth;
    }

    /**
     * Returns the current info object of the client.
     *
     * @return The current info object of the client
     */
    public InstanceInfo getInfo() {
        InstanceInfo instanceInfo = this.eurekaClient.getApplicationInfoManager().getInfo();
        return instanceInfo;
    }

}