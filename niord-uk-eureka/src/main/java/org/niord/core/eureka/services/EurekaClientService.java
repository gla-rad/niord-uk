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
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@ApplicationScoped
public class EurekaClientService {

    @Inject
    Logger log;

    @Inject
    EurekaInstanceConfig eurekaInstanceConfig;

    @Inject
    InstanceInfo instanceInfo;

    @Inject
    ApplicationInfoManager.OptionalArgs optionalArgs;

    @Inject
    EurekaClientConfig eurekaClientConfig;

    // Service Variables
    private ApplicationInfoManager applicationInfoManager;
    private EurekaClient eurekaClient;

    /**
     * Initialize the data store.
     */
    void init(@Observes StartupEvent ev) {
        this.applicationInfoManager = initializeApplicationInfoManager();
        this.applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.STARTING);

        this.log.info("Initialising the Eureka Client...");

        this.eurekaClient = initializeEurekaClient();
        this.applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.UP);

        this.log.info("Eureka Client now UP...");
    }

    /**
     * Clean up Lucene index.
     */
    void destroy(@Observes ShutdownEvent ev) {
        this.applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.DOWN);
    }

    /**
     * Initialises the Netflix application info manager instance.
     *
     * @return the initialised application info manager instance
     */
    protected ApplicationInfoManager initializeApplicationInfoManager() {
        if (this.applicationInfoManager == null) {
            this.applicationInfoManager = new ApplicationInfoManager(eurekaInstanceConfig, instanceInfo, optionalArgs);
        }

        return this.applicationInfoManager;
    }

    /**
     * Initialises the Netflix eureka client instance.
     *
     * @return the eureka client
     */
    EurekaClient initializeEurekaClient() {
        if (this.eurekaClient == null) {
            this.eurekaClient = new DiscoveryClient(this.applicationInfoManager, this.eurekaClientConfig);
        }

        return this.eurekaClient;
    }

    /**
     * Returns the registered hostname with the eureka server.
     *
     * @return the registered hostname
     */
    public String getEurekaClientHostname() {
        return this.eurekaClient.getApplicationInfoManager().getInfo().getHostName();
    }

    /**
     * Returns the current service status.
     *
     * @return the service status
     */
    public InstanceInfo.InstanceStatus getStatus() {
        return this.applicationInfoManager.getInfo().getStatus();
    }

    /**
     * Returns the current health status of the client.
     *
     * @return The current health status of the client
     */
    public EurekaHealth getHealth() {
        EurekaHealth eurekaHealth = new EurekaHealth();
        eurekaHealth.setStatus(this.applicationInfoManager.getInfo().getStatus());
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