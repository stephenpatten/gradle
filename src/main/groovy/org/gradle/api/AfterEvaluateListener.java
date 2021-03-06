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
package org.gradle.api;

/**
 * <p>An {@code AfterEvaluateListener} is notified when a project is evaluated. You add can add an
 * AfterEvaluatedListener to a {@link Project} using {@link Project#addAfterEvaluateListener(AfterEvaluateListener)}.</p>
 *
 * @author Hans Dockter
 * @see org.gradle.api.Project#addAfterEvaluateListener(AfterEvaluateListener)
 */
public interface AfterEvaluateListener {
    /**
     * This method is called by a Project when it has been evaluated, and before the evaluated project is made available
     * to other projects.</p>
     *
     * @param project The project which was evaluated.
     */
    void afterEvaluate(Project project);
}
