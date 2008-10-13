/*
 * Copyright 2007-2008 the original author or authors.
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
package org.gradle.api.initialization;

import org.gradle.api.Project;

import java.io.File;
import java.util.Set;

/**
 * <p>A {@code ProjectDescriptor} declares the configuration required to create and evaluate a {@link Project}.</p>
 *
 * <p> A {@code ProjectDescriptor} is created when you add a project to the build from the settings script, using {@link
 * Settings#include(String[])} or {@link Settings#includeFlat(String[])}. You can access the descriptors using one of
 * the lookup methods on the {@link Settings} object.</p>
 *
 * @author Hans Dockter
 */
public interface ProjectDescriptor {
    /**
     * Returns the name of this project.
     *
     * @return The name of the project. Never returns null.
     */
    String getName();

    /**
     * Sets the name of this project.
     *
     * @param name The new name for the project. Should not be null
     */
    void setName(String name);

    /**
     * Returns the project directory of this project.
     *
     * @return The project directory. Never returns null.
     */
    File getDir();

    /**
     * Sets the project directory of this project.
     *
     * @param dir The new project directory. Should not be null.
     */
    void setDir(File dir);

    /**
     * Returns the parent of this project, if any. Returns null if this project is the root project.
     *
     * @return The parent, or null if this is the root project.
     */
    ProjectDescriptor getParent();

    /**
     * Returns the children of this project, if any.
     *
     * @return The children. Returns an empty set if this project does not have any children.
     */
    Set<ProjectDescriptor> getChildren();

    /**
     * Returns the path of this project. The path can be used as a unique identifier for this project.
     *
     * @return The path. Never returns null.
     */
    String getPath();
}
