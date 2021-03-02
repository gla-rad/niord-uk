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

package org.niord.s125.services;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import org.niord.core.NiordApp;
import org.niord.core.message.MessageService;

import javax.inject.Inject;
import java.io.StringWriter;

public class S125Service {

    @Inject
    MessageService messageService;

    @Inject
    NiordApp app;

    /**
     * Generates S-125 compliant GML for the message
     * @param atonUID the aton UID
     * @param language the language
     * @return the generated GML
     */
    public String generateGML(String atonUID, String language) throws Exception {
        Configuration cfg = new Configuration(Configuration.getVersion());
        cfg.setTemplateLoader(new ClassTemplateLoader(getClass(), "/templates/gml"));

        StringWriter result = new StringWriter();
        return result.toString();
    }
}
