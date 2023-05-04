/*
 * smart-doc https://github.com/smart-doc-group/smart-doc
 *
 * Copyright (C) 2018-2023 smart-doc
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.smartdoc.mojo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import com.power.common.constants.Charset;
import com.power.common.util.CollectionUtil;
import com.power.common.util.DateTimeUtil;
import com.power.common.util.RegexUtil;
import com.power.common.util.StringUtil;
import com.power.doc.helper.JavaProjectBuilderHelper;
import com.power.doc.model.ApiConfig;
import com.smartdoc.constant.GlobalConstants;
import com.smartdoc.constant.MojoConstants;
import com.smartdoc.util.ArtifactFilterUtil;
import com.smartdoc.util.ClassLoaderUtil;
import com.smartdoc.util.FileUtil;
import com.smartdoc.util.MojoUtils;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.library.SortedClassLibraryBuilder;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;


/**
 * @author yu 2020/1/8.
 */
public abstract class BaseDocsGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Component
    protected RepositorySystem repositorySystem;
    protected JavaProjectBuilder javaProjectBuilder;
    @Component(role = org.apache.maven.project.ProjectBuilder.class)
    protected ProjectBuilder projectBuilder;
    @Parameter(defaultValue = "${localRepository}", required = true, readonly = true)
    private ArtifactRepository localRepository;
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;
    @Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
    private List<MavenProject> reactorProjects;
    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;
    @Parameter(property = "scope")
    private String scope;
    @Parameter(property = "configFile", defaultValue = GlobalConstants.DEFAULT_CONFIG)
    private File configFile;
    @Parameter(property = "projectName")
    private String projectName;
    @Parameter(required = false)
    private Set excludes;
    @Parameter(required = false)
    private Set includes;
    @Parameter(property = "skip")
    private String skip;
    @Parameter(defaultValue = "${mojoExecution}")
    private MojoExecution mojoEx;
    private DependencyNode rootNode;
    private List<String> projectArtifacts;
    @Parameter(defaultValue = "${tornaToken}")
    private String tornaToken;

    public abstract void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder)
        throws MojoExecutionException, MojoFailureException;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //skip
        if ("true".equals(skip)) {
            return;
        }
        if (Objects.nonNull(configFile) && !configFile.exists()) {
            // Throwing an exception will cause an error in the multi-module maven project build.
            this.getLog().warn("Can't find config file: " + configFile.getName() + " from [" + project.getName()
                + "],If it is a non-web module, please ignore the error.");
            return;
        }
        this.getLog().info("------------------------------------------------------------------------");
        this.getLog().info("Smart-doc Start preparing sources at: " + DateTimeUtil.nowStrTime());
        projectArtifacts = project.getArtifacts().stream().map(moduleName -> moduleName.getGroupId() + ":" + moduleName.getArtifactId())
            .collect(Collectors.toList());
        ApiConfig apiConfig = MojoUtils.buildConfig(configFile, projectName, project, projectBuilder, session, projectArtifacts, getLog());
        if (Objects.isNull(apiConfig)) {
            this.getLog().info(GlobalConstants.ERROR_MSG);
            return;
        }
        if (StringUtil.isNotEmpty(tornaToken)) {
            apiConfig.setAppToken(tornaToken);
        }
        javaProjectBuilder = buildJavaProjectBuilder(apiConfig.getCodePath());
        javaProjectBuilder.setEncoding(Charset.DEFAULT_CHARSET);
        String rpcConsumerConfig = apiConfig.getRpcConsumerConfig();
        if (!FileUtil.isAbsPath(rpcConsumerConfig) && StringUtil.isNotEmpty(rpcConsumerConfig)) {
            apiConfig.setRpcConsumerConfig(project.getBasedir().getPath() + "/" + rpcConsumerConfig);
        }

        String goal = mojoEx.getGoal();
        String outPath = apiConfig.getOutPath();
        if (StringUtil.isEmpty(outPath)) {
            if (!MojoConstants.TORNA_REST_MOJO.equals(goal) && !MojoConstants.TORNA_RPC_MOJO.equals(goal)) {
                this.getLog().error("Smart-doc out path can't be null or empty.");
                throw new RuntimeException("Smart-doc out path can't be null or empty.");
            }
        }
        if (!FileUtil.isAbsPath(outPath) && StringUtil.isNotEmpty(outPath)) {
            apiConfig.setOutPath(project.getBasedir().getPath() + "/" + outPath);
        }
        getLog().info("Smart-doc Starting Create API Documentation at: " + DateTimeUtil.nowStrTime());
        if (!MojoConstants.TORNA_RPC_MOJO.equals(goal) && !MojoConstants.TORNA_REST_MOJO.equals(goal)) {
            getLog().info("API documentation is output to => " + apiConfig.getOutPath().replace("\\", "/"));
        }
        this.executeMojo(apiConfig, javaProjectBuilder);
    }


    /**
     * Classloading
     */
    private JavaProjectBuilder buildJavaProjectBuilder(String codePath) throws MojoExecutionException {
        SortedClassLibraryBuilder classLibraryBuilder = new SortedClassLibraryBuilder();
        classLibraryBuilder.setErrorHander(e -> getLog().error("Parse error", e));
        JavaProjectBuilder javaDocBuilder = JavaProjectBuilderHelper.create(classLibraryBuilder);
        javaDocBuilder.setEncoding(Charset.DEFAULT_CHARSET);
        javaDocBuilder.setErrorHandler(e -> getLog().warn(e.getMessage()));
        //addSourceTree
        javaDocBuilder.addSourceTree(new File(codePath));
        //sources.stream().map(File::new).forEach(javaDocBuilder::addSourceTree);
        javaDocBuilder.addClassLoader(ClassLoaderUtil.getRuntimeClassLoader(project));
        loadSourcesDependencies(javaDocBuilder);
        return javaDocBuilder;
    }

    /**
     * load sources
     */
    private void loadSourcesDependencies(JavaProjectBuilder javaDocBuilder) throws MojoExecutionException {
        try {
            List<String> currentProjectModules = getCurrentProjectArtifacts(this.project);
            ArtifactFilter artifactFilter = this.createResolvingArtifactFilter();
            ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(this.session.getProjectBuildingRequest());
            buildingRequest.setProject(this.project);
            this.rootNode = this.dependencyGraphBuilder.buildDependencyGraph(buildingRequest, artifactFilter);
            List<DependencyNode> dependencyNodes = this.rootNode.getChildren();
            List<Artifact> artifactList = this.getArtifacts(dependencyNodes);
            artifactList.forEach(artifact -> {
                if (ArtifactFilterUtil.ignoreSpringBootArtifactById(artifact)) {
                    return;
                }
                String artifactName = artifact.getGroupId() + ":" + artifact.getArtifactId();
                if (currentProjectModules.contains(artifactName)) {
                    this.projectArtifacts.add(artifactName);
                    return;
                }
                if (RegexUtil.isMatches(excludes, artifactName)) {
                    return;
                }
                Artifact sourcesArtifact = repositorySystem.createArtifactWithClassifier(artifact.getGroupId(),
                    artifact.getArtifactId(), artifact.getVersion(), artifact.getType(), "sources");
                if (RegexUtil.isMatches(includes, artifactName)) {
                    this.projectArtifacts.add(artifactName);
                    this.loadSourcesDependency(javaDocBuilder, sourcesArtifact);
                    return;
                }
                if (CollectionUtil.isEmpty(includes)) {
                    this.projectArtifacts.add(artifactName);
                    this.loadSourcesDependency(javaDocBuilder, sourcesArtifact);
                }
            });

        } catch (DependencyGraphBuilderException e) {
            throw new MojoExecutionException("Can't build project dependency graph", e);
        }
    }

    /**
     * reference https://github.com/sfauvel/livingdocumentation
     *
     * @param javaDocBuilder  JavaProjectBuilder
     * @param sourcesArtifact Artifact
     */
    private void loadSourcesDependency(JavaProjectBuilder javaDocBuilder, Artifact sourcesArtifact) {
        String artifactName = sourcesArtifact.getGroupId() + ":" + sourcesArtifact.getArtifactId();
        getLog().debug("smart-doc loaded artifact:" + artifactName);
        // create request
        ArtifactResolutionRequest request = new ArtifactResolutionRequest();
        request.setArtifact(sourcesArtifact);
        //request.setResolveTransitively(true);
        request.setRemoteRepositories(project.getRemoteArtifactRepositories());
        // resolve deps
        ArtifactResolutionResult result = repositorySystem.resolve(request);
        // load source file into javadoc builder
        result.getArtifacts().forEach(artifact -> {
            JarFile jarFile;
            String sourceURL;
            try {
                sourceURL = artifact.getFile().toURI().toURL().toString();
                if (getLog().isDebugEnabled()) {
                    getLog().debug("smart-doc loaded jar source:" + sourceURL);
                }
                jarFile = new JarFile(artifact.getFile());
            } catch (IOException e) {
                throw new RuntimeException("Unable to load jar source " + artifact + " : " + e.getMessage());
            }
            for (Enumeration<?> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = (JarEntry) entries.nextElement();
                String name = entry.getName();
                try {
                    if (name.endsWith(".java") && !name.endsWith("/package-info.java")) {
                        String uri = "jar:" + sourceURL + "!/" + name;
                        if (getLog().isDebugEnabled()) {
                            getLog().debug(uri);
                        }
                        javaDocBuilder.addSource(new URL(uri));
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    getLog().error("syntax error in jar :" + sourceURL);
                }
            }
        });
    }

    /**
     * reference maven-dependency-plugin tree
     *
     * @return ArtifactFilter
     */
    private ArtifactFilter createResolvingArtifactFilter() {
        ScopeArtifactFilter filter;
        if (this.scope != null) {
            this.getLog().debug("+ Resolving dependency tree for scope '" + this.scope + "'");
            filter = new ScopeArtifactFilter(this.scope);
        } else {
            filter = null;
        }
        return filter;
    }

    private List<Artifact> getArtifacts(List<DependencyNode> dependencyNodes) {
        List<Artifact> artifacts = new ArrayList<>();
        if (CollectionUtil.isEmpty(dependencyNodes)) {
            return artifacts;
        }
        for (DependencyNode dependencyNode : dependencyNodes) {
            if (ArtifactFilterUtil.ignoreArtifact(dependencyNode.getArtifact())) {
                continue;
            }
            artifacts.add(dependencyNode.getArtifact());
            if (dependencyNode.getChildren().size() > 0) {
                artifacts.addAll(getArtifacts(dependencyNode.getChildren()));
            }
        }
        return artifacts;
    }

    private List<String> getCurrentProjectArtifacts(MavenProject project) {
        if (!project.hasParent()) {
            return new ArrayList<>(0);
        }
        List<String> finalArtifactsName = new ArrayList<>();
        MavenProject mavenProject = project.getParent();
        if (Objects.nonNull(mavenProject)) {
            File file = mavenProject.getBasedir();
            if (!Objects.isNull(file)) {
                String groupId = mavenProject.getGroupId();
                List<String> moduleList = mavenProject.getModules();
                moduleList.forEach(str -> finalArtifactsName.add(groupId + ":" + str));
            }
        }
        return finalArtifactsName;
    }
}
