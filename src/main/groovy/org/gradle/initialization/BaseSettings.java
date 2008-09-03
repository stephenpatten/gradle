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
package org.gradle.initialization;

import groovy.lang.Closure;
import org.apache.ivy.plugins.resolver.DualResolver;
import org.apache.ivy.plugins.resolver.FileSystemResolver;
import org.gradle.StartParameter;
import org.gradle.api.DependencyManager;
import org.gradle.api.Project;
import org.gradle.api.Settings;
import org.gradle.api.dependencies.ResolverContainer;
import org.gradle.api.internal.dependencies.DependencyManagerFactory;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.util.ClasspathUtil;
import org.gradle.util.GradleUtil;
import org.gradle.util.WrapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @author Hans Dockter
 */
public class BaseSettings implements Settings {
    private static Logger logger = LoggerFactory.getLogger(DefaultSettings.class);

    public static final String BUILD_CONFIGURATION = "build";
    public static final String DEFAULT_BUILD_SRC_DIR = "buildSrc";
    private DependencyManager dependencyManager;

    private BuildSourceBuilder buildSourceBuilder;
    private Map<String, String> additionalProperties = new HashMap();

    private StartParameter startParameter;

    private ISettingsFinder settingsFinder;

    private StartParameter buildSrcStartParameter;

    private List<String> projectPaths = new ArrayList<String>();

    public BaseSettings() {
    }

    public BaseSettings(DependencyManagerFactory dependencyManagerFactory,
                        BuildSourceBuilder buildSourceBuilder, ISettingsFinder settingsFinder, StartParameter startParameter) {
        this.settingsFinder = settingsFinder;
        this.startParameter = startParameter;
        this.dependencyManager = dependencyManagerFactory.createDependencyManager(createBuildDependenciesProject());
        this.buildSourceBuilder = buildSourceBuilder;
        dependencyManager.addConfiguration(BUILD_CONFIGURATION);
        assignBuildSrcStartParameter(startParameter);
    }

    private void assignBuildSrcStartParameter(StartParameter startParameter) {
        buildSrcStartParameter = new StartParameter();
        buildSrcStartParameter.setTaskNames(WrapUtil.toList(JavaPlugin.CLEAN, JavaPlugin.UPLOAD_LIBS));
        buildSrcStartParameter.setSearchUpwards(true);
        buildSrcStartParameter.setGradleUserHomeDir(startParameter.getGradleUserHomeDir());
    }

    public void include(String[] projectPaths) {
        this.projectPaths.addAll(Arrays.asList(projectPaths));
    }

    public void dependencies(Object[] dependencies) {
        dependencyManager.dependencies(WrapUtil.toList(BUILD_CONFIGURATION), dependencies);
    }

    public void dependency(String id, Closure configureClosure) {
        dependencyManager.dependency(WrapUtil.toList(BUILD_CONFIGURATION), id, configureClosure);
    }

    public void clientModule(String id, Closure configureClosure) {
        dependencyManager.clientModule(WrapUtil.toList(BUILD_CONFIGURATION), id, configureClosure);
    }

    public ResolverContainer getResolvers() {
        return dependencyManager.getClasspathResolvers();
    }

    public FileSystemResolver addFlatDirResolver(String name, Object[] dirs) {
        return dependencyManager.addFlatDirResolver(name, dirs);
    }

    public DualResolver addMavenRepo(String[] jarRepoUrls) {
        return dependencyManager.addMavenRepo(jarRepoUrls);
    }

    public DualResolver addMavenStyleRepo(String name, String root, String[] jarRepoUrls) {
        return dependencyManager.addMavenStyleRepo(name, root, jarRepoUrls);
    }

    // todo We don't have command query separation here. This si a temporary thing. If our new classloader handling works out, which
    // adds simply the build script jars to the context classloader we can remove the return argument and simplify our design.
    public URLClassLoader createClassLoader() {
        URLClassLoader classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        Object dependency = null;
        StartParameter startParameter = StartParameter.newInstance(buildSrcStartParameter);
        startParameter.setCurrentDir(new File(settingsFinder.getSettingsDir(), DEFAULT_BUILD_SRC_DIR));
        if (buildSourceBuilder != null) {
            dependency = buildSourceBuilder.createDependency(dependencyManager.getBuildResolverDir(),
                    startParameter);
        }
        logger.debug("Build src dependency: {}", dependency);
        if (dependency != null) {
            dependencyManager.dependencies(WrapUtil.toList(BUILD_CONFIGURATION), dependency);
        } else {
            logger.info("No build sources found.");
        }
        List additionalClasspath = dependencyManager.resolve(BUILD_CONFIGURATION);
        File toolsJar = GradleUtil.getToolsJar();
        if (toolsJar != null) {
            additionalClasspath.add(toolsJar);
        }
        logger.debug("Adding to classpath: {}", additionalClasspath);
        ClasspathUtil.addUrl(classLoader, additionalClasspath);
        return classLoader;
    }

    private Project createBuildDependenciesProject() {
        DefaultProject dummyProjectForDepencencyManager = new DefaultProject();
        dummyProjectForDepencencyManager.setProperty(DependencyManager.GROUP, BUILD_DEPENDENCIES_PROJECT_GROUP);
        dummyProjectForDepencencyManager.setName(BUILD_DEPENDENCIES_PROJECT_NAME);
        dummyProjectForDepencencyManager.setProperty(DependencyManager.VERSION, BUILD_DEPENDENCIES_PROJECT_VERSION);
        try {
            dummyProjectForDepencencyManager.setGradleUserHome(startParameter.getGradleUserHomeDir().getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dummyProjectForDepencencyManager;
    }

    public File getRootDir() {
        return settingsFinder.getSettingsDir();
    }

    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    public void setDependencyManager(DependencyManager dependencyManager) {
        this.dependencyManager = dependencyManager;
    }

    public BuildSourceBuilder getBuildSourceBuilder() {
        return buildSourceBuilder;
    }

    public void setBuildSourceBuilder(BuildSourceBuilder buildSourceBuilder) {
        this.buildSourceBuilder = buildSourceBuilder;
    }

    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public StartParameter getStartParameter() {
        return startParameter;
    }

    public void setStartParameter(StartParameter startParameter) {
        this.startParameter = startParameter;
    }

    public ISettingsFinder getSettingsFinder() {
        return settingsFinder;
    }

    public void setSettingsFinder(ISettingsFinder settingsFinder) {
        this.settingsFinder = settingsFinder;
    }

    public StartParameter getBuildSrcStartParameter() {
        return buildSrcStartParameter;
    }

    public void setBuildSrcStartParameter(StartParameter buildSrcStartParameter) {
        this.buildSrcStartParameter = buildSrcStartParameter;
    }

    public List<String> getProjectPaths() {
        return projectPaths;
    }

    public void setProjectPaths(List<String> projectPaths) {
        this.projectPaths = projectPaths;
    }
}