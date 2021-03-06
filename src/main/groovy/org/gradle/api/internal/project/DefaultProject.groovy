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

package org.gradle.api.internal.project

import org.gradle.api.AfterEvaluateListener
import org.gradle.api.DependencyManager
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.BuildInternal
import org.gradle.api.internal.artifacts.DependencyManagerFactory
import org.gradle.groovy.scripts.ScriptSource
import org.gradle.util.ConfigureUtil
import org.gradle.api.internal.project.*

/**
 * @author Hans Dockter
 */
class DefaultProject extends AbstractProject {
    public DefaultProject(String name) {
        super(name);
    }

    public DefaultProject(String name, ProjectInternal parent, File projectDir, String buildFileName, ScriptSource scriptSource,
                          ClassLoader buildScriptClassLoader, ITaskFactory taskFactory,
                          DependencyManagerFactory dependencyManagerFactory, AntBuilderFactory antBuilderFactory, 
                          BuildScriptProcessor buildScriptProcessor,
                          PluginRegistry pluginRegistry, IProjectRegistry projectRegistry,
                          BuildInternal build) {
        super(name, parent, projectDir, buildFileName, scriptSource, buildScriptClassLoader, taskFactory, dependencyManagerFactory,
                antBuilderFactory, buildScriptProcessor, pluginRegistry, projectRegistry, build);
    }

    def propertyMissing(String name) {
        property(name)
    }

    def methodMissing(String name, args) {
        dynamicObjectHelper.invokeMethod(name, args)
    }

    void setProperty(String name, value) {
        dynamicObjectHelper.setProperty(name, value)
    }

    public Task createTask(String name, Closure action) {
        return createTask(new HashMap(), name, action);
    }

    public Task createTask(Map args, String name, Closure action) {
        return createTask(args, name).doFirst(action);
    }

    public AntBuilder ant(Closure configureClosure) {
        return (AntBuilder) ConfigureUtil.configure(configureClosure, getAnt(), Closure.OWNER_FIRST);
    }

    public DependencyManager dependencies(Closure configureClosure) {
        return (DependencyManager) ConfigureUtil.configure(configureClosure, dependencies);
    }

    public void subprojects(Closure configureClosure) {
        configureProjects(getSubprojects(), configureClosure);
    }

    public void allprojects(Closure configureClosure) {
        configureProjects(getAllprojects(), configureClosure);
    }

    public void configureProjects(Iterable<Project> projects, Closure configureClosure) {
        for (Project project : projects) {
            ConfigureUtil.configure(configureClosure, project);
        }
    }

    public Project project(String path, Closure configureClosure) {
        return (Project) ConfigureUtil.configure(configureClosure, project(path));
    }

    public Task task(String path, Closure configureClosure) {
        Task task = task(path);
        if (configureClosure != null) {
            task.configure(configureClosure);
        }
        return task;
    }

    public void addAfterEvaluateListener(Closure afterEvaluateListener) {
        addAfterEvaluateListener(afterEvaluateListener as AfterEvaluateListener)
    }

    public Object configure(Object object, Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, object)
    }


}
