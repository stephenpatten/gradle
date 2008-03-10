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

package org.gradle.api.plugins

/**
 * We always cast the closure argument to the JavaConvention object. We try to do this with as less noise as possible.
 * We do this, because we want to use the content assist, reliable refactoring and fail fast.
 *
 * @author Hans Dockter
 */
class DefaultConventionsToPropertiesMapping {
    final static Map DEPENDENCY_MAANGER = [
            targetDir: {_(it).classesDir},
            sourceDirs: {_(it).resourceDirs}
    ]
    final static Map CLEAN = [
            dir: {_(it).buildDir}
    ]
    final static Map RESOURCES = [
            targetDir: {_(it).classesDir},
            sourceDirs: {_(it).resourceDirs}
    ]
    final static Map COMPILE = [
            targetDir: {_(it).classesDir},
            sourceDirs: {_(it).srcDirs},
            sourceCompatibility: {_(it).sourceCompatibility},
            targetCompatibility: {_(it).targetCompatibility},
            dependencyManager: {_(it).project.dependencies}
    ]
    final static Map TEST_RESOURCES = [
            targetDir: {_(it).testClassesDir},
            sourceDirs: {_(it).testResourceDirs}
    ]
    final static Map TEST_COMPILE = [
            targetDir: {_(it).testClassesDir},
            sourceDirs: {_(it).testSrcDirs},
            sourceCompatibility: {_(it).sourceCompatibility},
            targetCompatibility: {_(it).targetCompatibility},
            unmanagedClasspath: {[_(it).classesDir]},
            dependencyManager: {_(it).project.dependencies}
    ]
    final static Map TEST = [
            compiledTestsDir: {_(it).testClassesDir},
            testResultsDir: {_(it).testResultsDir},
            unmanagedClasspath: {[_(it).classesDir, _(it).testClassesDir]},
            dependencyManager: {_(it).project.dependencies}
    ]
    private final static Map ARCHIVE = [
            destinationDir: {_(it).buildDir},
            dependencyManager: {_(it).project.dependencies},
            version: {"${_(it).project.version}"},
            baseName: {"${_(it).project.name}"}
    ]
    final static Map ZIP = ARCHIVE + [
            configurations: {[JavaPlugin.DISTRIBUTE] as String[]}
    ]
    final static Map TAR = ZIP
    final static Map JAR = ARCHIVE + [
            baseDir: {_(it).classesDir},
            configurations: {[JavaPlugin.MASTER] as String[]},
            manifest: {_(it).manifest},
            metaInf: {_(it).metaInf}
    ]
    final static Map WAR = JAR.subMap(JAR.keySet() - 'configurations') + [
            configurations: {[JavaPlugin.DISTRIBUTE] as String[]},
            libConfiguration: {JavaPlugin.RUNTIME}
    ]
    final static Map LIB = [
            tasksBaseName: {"${_(it).project.name}"},
            childrenDependOn: {[JavaPlugin.TEST]},
            defaultArchiveTypes: {_(it).archiveTypes}
    ]
    final static Map DIST = [
            tasksBaseName: {"${_(it).project.name}"},
            childrenDependOn: {[JavaPlugin.LIB]},
            defaultArchiveTypes: {_(it).archiveTypes}
    ]

    static JavaConvention _(def object) {
        object
    }
}