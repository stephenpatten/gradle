/*
 * Copyright 2007 the original author or authors.
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

package org.gradle.initialization

import org.gradle.api.Project
import org.gradle.api.internal.project.*
import org.gradle.initialization.DefaultSettings
import org.gradle.util.PathHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
* @author Hans Dockter
*/
class ProjectsLoader {
    Logger logger = LoggerFactory.getLogger(ProjectsLoader)

    ProjectFactory projectFactory

    DefaultProject rootProject

    DefaultProject currentProject

    BuildScriptProcessor buildScriptProcessor

    BuildScriptFinder buildScriptFinder

    PluginRegistry pluginRegistry

    ProjectsLoader() {

    }

    ProjectsLoader(ProjectFactory projectFactory, BuildScriptProcessor buildScriptProcessor, BuildScriptFinder buildScriptFinder, PluginRegistry pluginRegistry) {
        this.projectFactory = projectFactory
        this.buildScriptProcessor = buildScriptProcessor
        this.buildScriptFinder = buildScriptFinder
        this.pluginRegistry = pluginRegistry
    }

    ProjectsLoader load(DefaultSettings settings, File gradleUserHomeDir) {
        rootProject = createProjects(settings, gradleUserHomeDir)
        currentProject = DefaultProject.findProject(rootProject, PathHelper.getCurrentProjectPath(rootProject.rootDir, settings.currentDir))
        this
    }

    private DefaultProject createProjects(DefaultSettings settings, File gradleUserHomeDir) {
        logger.debug("Creating the projects and evaluating the project files!")
        File propertyFile = new File(gradleUserHomeDir, 'gradle.properties')
        Properties properties = new Properties()
        logger.debug("Looking for properties from: $propertyFile")
        if (!propertyFile.isFile()) {
            logger.debug('Property file does not exists. We continue!')
        } else {
            properties = new Properties()
            properties.load(new FileInputStream(propertyFile))
            logger.debug("Adding properties: $properties")
        }
        DefaultProject rootProject = projectFactory.createProject(settings.rootDir.name, null, settings.rootDir, null, projectFactory, buildScriptProcessor, buildScriptFinder, pluginRegistry)
        addPropertiesToProject(gradleUserHomeDir, properties, rootProject)
        settings.projectPaths.each {
            List folders = it.split(Project.PATH_SEPARATOR)
            DefaultProject parent = rootProject
            folders.each {name ->
                if (!parent.childProjects[name]) {
                    parent.childProjects[name] = parent.addChildProject(name)
                    addPropertiesToProject(gradleUserHomeDir, properties, parent.childProjects[name])
                }
                parent = parent.childProjects[name]
            }
        }
        rootProject
    }

    private addPropertiesToProject(File gradleUserHomeDir, Map properties, Project project) {
        project.gradleUserHome = gradleUserHomeDir.canonicalPath
        properties.each {key, value ->
            project."$key" = value
        }
    }

}