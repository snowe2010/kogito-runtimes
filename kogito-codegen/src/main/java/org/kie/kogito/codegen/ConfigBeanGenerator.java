/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.codegen;

public class ConfigBeanGenerator extends TemplatedGenerator {

    private static final String RESOURCE_CDI = "/class-templates/config/CdiConfigBeanTemplate.java";
    private static final String RESOURCE_SPRING = "/class-templates/config/SpringConfigBeanTemplate.java";

    public ConfigBeanGenerator(String packageName) {
        super(packageName,
              "ConfigBean",
              RESOURCE_CDI,
              RESOURCE_SPRING);
    }

}
