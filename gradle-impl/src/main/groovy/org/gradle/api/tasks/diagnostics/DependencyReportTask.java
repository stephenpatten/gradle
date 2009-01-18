/*
 * Copyright 2008 the original author or authors.
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
package org.gradle.api.tasks.diagnostics;

import org.gradle.api.Project;
import org.gradle.impl.api.dependencies.report.IvyDependencyGraph;
import org.gradle.impl.api.dependencies.report.IvyDependencyGraphBuilder;
import org.gradle.impl.api.internal.dependencies.BaseDependencyManager;
import org.gradle.impl.api.internal.dependencies.DefaultDependencyResolver;

import java.io.IOException;

/**
 * The {@code DependencyReportTask} displays the dependency tree for a project. Can be configured to output to a file,
 * and to optionally output a graphviz compatible "dot" graph. This task is used when you execute the dependency list
 * command-line option.
 *
 * @author Phil Messenger
 */
public class DependencyReportTask extends AbstractReportTask {

    private DependencyReportRenderer renderer = new AsciiReportRenderer();
    private String conf = "runtime";

    public DependencyReportTask(Project project, String name) {
        super(project, name);
    }

    /**
     * Set the configuration to build a dependency report for
     */
    public void setConf(String conf) {
        this.conf = conf;
    }

    public DependencyReportRenderer getRenderer() {
        return renderer;
    }

    /**
     * Set the renderer to use to build a report. If unset, AsciiGraphRenderer will be used.
     */
    public void setRenderer(DependencyReportRenderer renderer) {
        this.renderer = renderer;
    }

    public void generate() throws IOException {
        Project project = getProject();

        IvyDependencyGraphBuilder graphBuilder = new IvyDependencyGraphBuilder();

        BaseDependencyManager depManager = (BaseDependencyManager) project.getDependencies();

        depManager.resolve(conf, false, true);

        DefaultDependencyResolver resolver = (DefaultDependencyResolver) depManager.getDependencyResolver();
        IvyDependencyGraph graph = graphBuilder.buildGraph(project, resolver.getLastResolveReport(), conf);

        renderer.startProject(project);
        renderer.render(graph);
        renderer.completeProject(project);
    }
}
