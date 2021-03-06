/*
 * Copyright 2009 the original author or authors.
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
package org.gradle.api.internal.artifacts.dependencies;

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.UnknownDependencyNotation;
import org.gradle.api.artifacts.DependencyConfigurationMappingContainer;
import org.gradle.api.internal.artifacts.ivyservice.DefaultDependencyDescriptorFactory;
import org.gradle.api.internal.artifacts.ivyservice.DependencyDescriptorFactory;

/**
 * @author Hans Dockter
 */
abstract public class AbstractDescriptorDependency extends AbstractDependency {
    private Object userDependencyDescription;
    private DependencyDescriptorFactory dependencyDescriptorFactory = new DefaultDependencyDescriptorFactory();

    protected AbstractDescriptorDependency() {
    }

    public AbstractDescriptorDependency(DependencyConfigurationMappingContainer dependencyConfigurationMappings, Object userDependencyDescription) {
        super(dependencyConfigurationMappings);
        if (!(isValidType(userDependencyDescription)) || !isValidDescription(userDependencyDescription)) {
            throw new UnknownDependencyNotation("Description " + userDependencyDescription + " not valid!");
        }
        if (dependencyConfigurationMappings == null) {
            throw new InvalidUserDataException("Configuration mapping must not be null.");
        }
        this.userDependencyDescription = userDependencyDescription;
    }

    public abstract boolean isValidDescription(Object userDependencyDescription);

    public abstract Class[] userDepencencyDescriptionType();

    private boolean isValidType(Object userDependencyDescription) {
        for (Class clazz : userDepencencyDescriptionType()) {
            if (clazz.isAssignableFrom(userDependencyDescription.getClass())) {
                return true;
            }
        }
        return false;
    }

    public Object getUserDependencyDescription() {
        return userDependencyDescription;
    }

    public void setUserDependencyDescription(Object userDependencyDescription) {
        this.userDependencyDescription = userDependencyDescription;
    }

    public DependencyDescriptorFactory getDependencyDescriptorFactory() {
        return dependencyDescriptorFactory;
    }

    public void setDependencyDescriptorFactory(DependencyDescriptorFactory dependencyDescriptorFactory) {
        this.dependencyDescriptorFactory = dependencyDescriptorFactory;
    }
}
