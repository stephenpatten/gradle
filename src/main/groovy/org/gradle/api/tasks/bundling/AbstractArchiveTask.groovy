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

package org.gradle.api.tasks.bundling

import org.gradle.api.DependencyManager
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.util.FileSet
import org.gradle.util.GradleUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.gradle.api.tasks.util.FileCollection
import org.gradle.api.tasks.util.AntDirective
import org.gradle.util.ConfigureUtil

import java.io.File;
import java.util.List
import org.gradle.util.GUtil;

/**
 * @author Hans Dockter
 */
public abstract class AbstractArchiveTask extends ConventionTask {
    private static Logger logger = LoggerFactory.getLogger(AbstractArchiveTask.class);

    /**
     * If you create a fileset and don't assign a directory to this fileset, the baseDir value is assigned to the dir
     * property of the fileset.
     */
    private File baseDir

    /**
     * A list with all entities (e.g. filesets) which describe the files of this archive.
     */
    private List resourceCollections = null

    /**
     * Controls if an archive gets created if no files would go into it.  
     */
    boolean createIfEmpty = false

    /**
     * The dir where the created archive is placed.
     */
    private File destinationDir

    /**
     * Usually the archive name is composed out of the baseName, the version and the extension. If the custom name is set,
     * solely the customName is used as the archiveName.
     */
    private String customName

    /**
     * The baseName of the archive.
     */
    private String baseName

    /**
     * The version part of the archive name
     */
    private String version

    /**
     * The extension part of the archive name
     */
    private String extension

    /**
     * The classifier part of the archive name. Default to an empty string. Could be 'src'. 
     */
    private String classifier = ''

    /**
     * Controlls whether the archive adds itself to the dependency configurations. Defaults to true.
     */
    boolean publish = true

    /**
     * The dependency configurations the archive gets added to if publish is true.
     */
    private String[] configurations

    /**
     * The dependency manager to use for adding the archive to the configurations.
     */
    private DependencyManager dependencyManager

    /**
     *
     */
    private List mergeFileSets = []

    /**
     *
     */
    private List mergeGroupFileSets = []

    protected ArchiveDetector archiveDetector = new ArchiveDetector()

    public AbstractArchiveTask(Project project, String name) {
        super(project, name)
        doLast(this.&generateArchive)
    }

    /**
     * Sets the publish property
     *
     * @param publish the value assigned to the publish property
     * @return this
     */
    public AbstractArchiveTask publish(boolean publish) {
        this.publish = publish
        this
    }

    /**
     * Sets (not add) the configurations the archive gets published to.
     *
     * @param publish the value assigned to the publish property
     * @return this
     */
    public AbstractArchiveTask configurations(String[] configurations) {
        this.configurations = configurations
        this
    }

    public void generateArchive(Task task) {
        logger.debug("Creating archive: $name")
        if (!getDestinationDir()) {
            throw new InvalidUserDataException('You mustspecify the destinationDir.')
        }
        createAntArchiveTask().call()
        if (publish) {
            String classifierSnippet = classifier ? ':' + classifier : ''
            getConfigurations().each {getDependencyManager().addArtifacts(it, "${getBaseName()}${classifierSnippet}@${getExtension()}")}
        }
    }

    protected abstract Closure createAntArchiveTask()

    /**
     * Returns the archive name. If the customName is not set, the pattern for the name is:
     * [baseName]-[version].[extension]
     */
    public String getArchiveName() {
        if (customName) { return customName }
        getBaseName() + (getVersion() ? "-${getVersion()}" : "") + (getClassifier() ? "-${getClassifier()}" : "") +
                ".${getExtension()}"
    }

    /**
     * The path where the archive is constructed. The path is simply the destinationDir plus the archiveName.
     * @return a File object with the path to the archive
     */
    public File getArchivePath() {
        new File(getDestinationDir(), getArchiveName())
    }

    public AntDirective antDirective(Closure directive) {
        AntDirective antDirective = new AntDirective(directive)
        resourceCollections(antDirective)
        antDirective
    }

    /**
     * Adds a fileset.
     * @param configureClosure configuration instructions
     * @return the added fileset
     */
    public FileSet fileSet(Closure configureClosure) {
        fileSet([:], configureClosure)
    }

    /**
     * Add a fileset
     * @param args constructor arguments for the FileSet to construct
     * @param configureClosure configuration instructions
     * @return the added fileset
     */
    public FileSet fileSet(Map args = [:], Closure configureClosure = null) {
        createFileSetInternal(args, FileSet, configureClosure)
    }

    protected def createFileSetInternal(Map args, Class type, Closure configureClosure) {
        args.dir = args.dir ?: getBaseDir()
        def fileSet = type.newInstance(args)
        resourceCollections(ConfigureUtil.configure(configureClosure, fileSet))
        fileSet
    }

    /**
     * An arbitrary collection of files to the archive. In contrast to a fileset they don't need to have a common
     * basedir.
     */
    public FileCollection files(File[] files) {
        FileCollection fileCollection = new FileCollection(files as Set)
        resourceCollections(fileCollection)
        fileCollection
    }

    /**
     *
     */
    public AbstractArchiveTask merge(Object[] archiveFiles) {
        Object[] flattenedArchiveFiles = archiveFiles
        Closure configureClosure = ConfigureUtil.extractClosure(flattenedArchiveFiles)
        if (configureClosure) {
            flattenedArchiveFiles = flattenedArchiveFiles[0..archiveFiles.length - 2]
            if (flattenedArchiveFiles.length == 1 && flattenedArchiveFiles instanceof Object[]) {
                flattenedArchiveFiles = flattenedArchiveFiles[0].collect {it}
            }
        }
        GradleUtil.fileList(flattenedArchiveFiles).collect { project.file(it) }.each {
            Class fileSetType = archiveDetector.archiveFileSetType(it)
            if (!fileSetType) { throw new InvalidUserDataException("File $it is not a valid archive or has no valid extension.") }
            def fileSet = fileSetType.newInstance(it)
            ConfigureUtil.configure(configureClosure, fileSet)
            mergeFileSets.add(fileSet)
        }
        this
    }

    /**
     * Defines a fileset of zip-like archives
     */
    public AbstractArchiveTask mergeGroup(def dir, Closure configureClosure = null) {
        if (!dir) { throw new InvalidUserDataException('Dir argument must not be null!') }
        FileSet fileSet = new FileSet(dir as File)
        ConfigureUtil.configure(configureClosure, fileSet)
        mergeGroupFileSets.add(fileSet)
        this
    }

    public AbstractArchiveTask resourceCollections(Object... elements) {
        if (resourceCollections == null) {
            List conventionCollection = getResourceCollections();
            if (conventionCollection != null) {
                resourceCollections = conventionCollection;
            } else {
                resourceCollections = new ArrayList();
            }
        }
        GUtil.flatten(Arrays.asList(elements), resourceCollections);
        return this;
    }

    public File getBaseDir() {
        return conv(baseDir, "baseDir");
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public List getResourceCollections() {
        return conv(resourceCollections, "resourceCollections");
    }

    public void setResourceCollections(List resourceCollections) {
        this.resourceCollections = resourceCollections;
    }

    // todo Uncomment after refacotring to Java
//    public boolean isCreateIfEmpty() {
//        return conv(createIfEmpty, "createIfEmpty");
//    }

    public void setCreateIfEmpty(boolean createIfEmpty) {
        this.createIfEmpty = createIfEmpty;
    }

    public File getDestinationDir() {
        return conv(destinationDir, "destinationDir");
    }

    public void setDestinationDir(File destinationDir) {
        this.destinationDir = destinationDir;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getBaseName() {
        return conv(baseName, "baseName");
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public String getVersion() {
        return conv(version, "version");
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getExtension() {
        return conv(extension, "extension");
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getClassifier() {
        return conv(classifier, "classifier");
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }
//     // todo Uncomment after refacotring to Java
//    public boolean isPublish() {
//        return conv(publish, "publish");
//    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public String[] getConfigurations() {
        return conv(configurations, "configurations");
    }

    public void setConfigurations(String[] configurations) {
        this.configurations = configurations;
    }

    public DependencyManager getDependencyManager() {
        return conv(dependencyManager, "dependencyManager");
    }

    public void setDependencyManager(DependencyManager dependencyManager) {
        this.dependencyManager = dependencyManager;
    }

    public List getMergeFileSets() {
        return mergeFileSets;
    }

    public void setMergeFileSets(List mergeFileSets) {
        this.mergeFileSets = mergeFileSets;
    }

    public List getMergeGroupFileSets() {
        return mergeGroupFileSets;
    }

    public void setMergeGroupFileSets(List mergeGroupFileSets) {
        this.mergeGroupFileSets = mergeGroupFileSets;
    }
}
