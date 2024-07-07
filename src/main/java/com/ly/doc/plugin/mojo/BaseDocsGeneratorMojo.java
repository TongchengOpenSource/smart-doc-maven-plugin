/*
 * smart-doc
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
package com.ly.doc.plugin.mojo;

import com.ly.doc.constants.ComponentTypeEnum;
import com.ly.doc.constants.DocLanguage;
import com.ly.doc.helper.JavaProjectBuilderHelper;
import com.ly.doc.model.ApiConfig;
import com.ly.doc.model.ApiDataDictionary;
import com.ly.doc.model.ApiErrorCodeDictionary;
import com.ly.doc.model.ApiGroup;
import com.ly.doc.model.ApiObjectReplacement;
import com.ly.doc.model.ApiReqParam;
import com.ly.doc.model.BodyAdvice;
import com.ly.doc.model.CustomField;
import com.ly.doc.model.RevisionLog;
import com.ly.doc.model.jmeter.JMeter;
import com.ly.doc.model.rpc.RpcApiDependency;
import com.ly.doc.plugin.constant.MojoConstants;
import com.ly.doc.plugin.util.ArtifactFilterUtil;
import com.ly.doc.plugin.util.ClassLoaderUtil;
import com.power.common.constants.Charset;
import com.power.common.util.CollectionUtil;
import com.power.common.util.DateTimeUtil;
import com.power.common.util.RegexUtil;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.library.SortedClassLibraryBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import org.apache.maven.artifact.Artifact;
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
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.reporting.MavenReport;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;

/**
 * @author xezzon
 */
public abstract class BaseDocsGeneratorMojo extends AbstractMojo {

    private static final String DESTINATION_DIR = "smart-doc";

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;
    @Component(hint = "default")
    private DependencyGraphBuilder dependencyGraphBuilder;
    @Component
    private RepositorySystem repositorySystem;
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;
    @Parameter(property = "scope")
    private String scope;
    @Parameter(required = false)
    private Set<String> excludes;
    @Parameter(required = false)
    private Set<String> includes;
    @Parameter(property = "smartdoc.skip", defaultValue = "false")
    private boolean skip;
    @Parameter(defaultValue = "${mojoExecution}")
    private MojoExecution mojoEx;

    /**
     * Specify the output path of the document
     */
    @Parameter(defaultValue = "${project.build.directory}/smart-doc", required = true)
    private File outputDirectory;
    /**
     * Server address, when exporting <code>postman</code> it is recommended to set it to
     * <code>http://{{server}}</code> for convenience and directly in
     * <code>postman</code> Set environment variables.
     * It is recommended to use <code>serverEnv</code> when exporting
     * <code>postman</code> after <code>2.4.8</code>
     * to avoid modifying the configuration during multiple exports.
     */
    @Parameter(defaultValue = "http://127.0.0.1")
    private String serverUrl;
    /**
     * Server address, when exporting <code>postman</code> it is recommended
     * to set it to <code>http://{{server}}</code>
     * to facilitate setting environment variables directly in <code>postman</code>.
     * The configuration is changed to support postman export
     * without globally modifying <code>serverUrl</code>
     * @since 2.4.8
     */
    @Parameter()
    private String serverEnv;
    /**
     * Set the <code>path</code> prefix, such as configuring <code>Servlet ContextPath</code>.
     * @since 2.2.3
     */
    @Parameter()
    private String pathPrefix;
    /**
     * Whether to enable strict mode.
     * Strict mode will force code comments to be checked.
     * When setting this item in <code>2.6.3</code> or later plug-in versions,
     * if annotation errors are detected, the plug-in will be directly interrupted.
     * White nested build cycles.
     * For team use,
     * it is recommended to set it to <code>true</code> to increase the annotation requirements
     * for developers and improve the completeness of the documentation.
     */
    @Parameter(defaultValue = "false")
    private boolean isStrict;
    /**
     * Whether to merge documents into one file, <code>true</code> is generally recommended.
     */
    @Parameter(defaultValue = "false")
    private Boolean allInOne;
    /**
     * Whether to overwrite old files, mainly used for <code>Markdown</code> file coverage.
     */
    @Parameter(defaultValue = "false")
    private Boolean coverOld;
    /**
     * <code>smart-doc</code> supports creating an <code>HTML</code> document page
     * with a debuggable interface similar to <code>Swagger</code>,
     * only in <code>AllInOne</code> function in the mode.
     * Starting from @2.0.1, for HTML documents,
     * debug pages can be generated in both allInOne and non-allInOne modes.
     * @since 2.0.1
     */
    @Parameter(defaultValue = "false")
    private Boolean createDebugPage;
    /**
     * <code>Controller</code> package filtering, multiple packages separated by English commas.
     * <code>2.2.2</code> starts to use regular rules: <code>com.test.controller.*</code>
     * <code>2.7.1</code> starts to support method level regular rules:
     * <code>com.test.controller.TestController.*</code>
     */
    @Parameter()
    private String packageFilters;
    /**
     * Exclude subpackages for <code>packageFilters</code>,
     * multiple packages are separated by English commas Since <code>2.2.2</code>,
     * regular rules must be used: <code>com.test.controller. res.*</code>
     */
    @Parameter()
    private String packageExcludeFilters;
    /**
     * Used only if each <code>Controller</code> generates an <code>HTML</code> file.
     */
    @Parameter(defaultValue = "false")
    private Boolean md5EncryptedHtmlName;
    /**
     * <a href="https://highlightjs.org/">Code Highlight</a> settings
     * based on <code>highlight.js</code>.
     */
    @Parameter()
    private String style;
    /**
     * Interface sorting by title.
     * @since 1.8.7
     */
    @Parameter(defaultValue = "false")
    private Boolean sortByTitle;
    /**
     * Whether to display the interface author name.
     */
    @Parameter(defaultValue = "true")
    private Boolean showAuthor;
    /**
     * Automatically convert camel case input fields to underline format in the document.
     * @since 1.8.7
     */
    @Parameter(defaultValue = "false")
    private Boolean requestFieldToUnderline;
    /**
     * Automatically convert camelCase response fields to underline format in the document.
     * @since 1.8.7
     */
    @Parameter(defaultValue = "false")
    private Boolean responseFieldToUnderline;
    /**
     * Whether to display the enumeration details in the parameter table.
     * @since 1.8.8
     */
    @Parameter(defaultValue = "false")
    private Boolean inlineEnum;
    /**
     * Set the number of recursive executions allowed to avoid some object parsing problems.
     * @since 1.8.8
     */
    @Parameter(defaultValue = "7")
    private Integer recursionLimit;
    /**
     * It only takes effect when all <code>Controller</code> of the project are configured
     * to generate an <code>HTML</code> file.
     * @since 1.9.0
     */
    @Parameter(defaultValue = "index.html")
    private String allInOneDocFileName;
    /**
     * Whether to display request examples in the documentation.
     * @since 1.9.0
     */
    @Parameter(defaultValue = "true")
    private Boolean requestExample;
    /**
     * Whether to display response examples in the documentation.
     * @since 1.9.0
     */
    @Parameter(defaultValue = "true")
    private Boolean responseExample;
    /**
     * Support <code>url</code> suffix of <code>SpringMVC</code> old project.
     * @since 2.1.0
     */
    @Parameter()
    private String urlSuffix;
    /**
     * Internationalization support for mock values.
     */
    @Parameter(defaultValue = "CHINESE")
    private DocLanguage language;
    /**
     * Whether to automatically display the short class name
     * of the generic real type in the comment column.
     * @since 1.9.6
     */
    @Parameter(defaultValue = "false")
    private Boolean displayActualType;
    /**
     * <code>torna</code> platform connects to <code>appKey</code>.
     * @since 2.0.9
     */
    @Parameter()
    private String appKey;
    /**
     * <code>torna</code> platform <code>appToken</code>.
     * @since 2.0.9
     */
    @Parameter(defaultValue = "${tornaToken}")
    private String tornaToken;
    /**
     * <code>torna</code> platform <code>secret</code>.
     * @since 2.0.9
     */
    @Parameter()
    private String secret;
    /**
     * <code>torna</code> platform address, fill in your own private deployment address.
     * @since 2.0.9
     */
    @Parameter()
    private String openUrl;
    /**
     * <code>torna</code> environment name.
     */
    @Parameter()
    private String debugEnvName;
    /**
     * Replace old documents when pushing <code>torna</code>.
     * Changes will still be pushed to the past and covered.
     * This function is mainly to ensure that the code is deleted
     * and not deleted on <code>torna</code>.
     * @since 2.2.4
     */
    @Parameter(defaultValue = "true")
    private Boolean replace;
    /**
     * Push <code>torna</code> configuration interface service address.
     * @since 2.0.9
     */
    @Parameter()
    private String debugEnvUrl;
    /**
     * Whether to print <code>torna</code> push log.
     * @since 2.0.9
     */
    @Parameter(defaultValue = "true")
    private Boolean tornaDebug;
    /**
     * @since 2.0.9
     */
    @Parameter()
    private String author;
    /**
     * Ignore request parameter objects and block parameter objects
     * that do not want to generate documents.
     * @since 1.9.2
     */
    @Parameter()
    private List<String> ignoreRequestParams;
    /**
     * Configure data dictionary
     * Since <code>2.4.6</code>, you can configure the interface implemented by the enumeration.
     * When configuring the interface,
     * the title will be used Description of the class that implements the enumeration.
     * If there are already implemented enumerations that need to be ignored,
     * you can add <code>@ignore</code> to the class that implements the enumeration to ignore them.
     */
    @Parameter()
    private List<ApiDataDictionary> dataDictionaries;
    /**
     * Error code list
     * Since <code>2.4.6</code>, the interface implemented by the enumeration can be configured.
     * When configuring the interface,
     * the title will be used Description of the class that implements the enumeration.
     * If there are already implemented enumerations that need to be ignored,
     * you can add <code>@ignore</code> to the class that implements the enumeration to ignore them.
     */
    @Parameter()
    private List<ApiErrorCodeDictionary> errorCodeDictionaries;
    /**
     * Document change record.
     */
    @Parameter()
    private List<RevisionLog> revisionLogs;
    /**
     * Customize added fields and comments,
     * general users deal with third-party <code>jar</code> package libraries.
     */
    @Parameter()
    private List<CustomField> customResponseFields;
    /**
     * Comments for the custom request body.
     */
    @Parameter()
    private List<CustomField> customRequestFields;
    /**
     * Set public request headers.
     * @since 2.1.3
     */
    @Parameter()
    private List<ApiReqParam> requestHeaders;
    /**
     * Public request parameters (scenarios handled by interceptors).
     * @since 2.2.3
     */
    @Parameter()
    private List<ApiReqParam> requestParams;
    /**
     * The project's open <code>Dubbo API</code> interface module depends on it.
     * After configuration, it is output to the document to facilitate user integration.
     */
    @Parameter()
    private List<RpcApiDependency> rpcApiDependencies;
    /**
     * The <code>Dubbo Consumer</code> integration configuration is added
     * to the document to facilitate quick integration by the integrator.
     */
    @Parameter()
    private String rpcConsumerConfig;
    /**
     * Use custom classes to override other classes for document rendering.
     * @since 1.8.5
     */
    @Parameter()
    private List<ApiObjectReplacement> apiObjectReplacements;
    /**
     * <code>ResponseBodyAdvice</code> is a hook reserved in the <code>Spring</code> framework,
     * which acts after the execution of the <code>Controller</code> method is completed After that,
     * before the <code>http</code> response body is written back to the client,
     * it can easily weave in some of its own business logic processing,
     * so <code>smart-doc</code> also provides unified return settings
     * for <code>ResponseBodyAdvice</code>
     * (do not configure it casually according to the project technology to configure) support,
     * which can be ignored using the ignoreResponseBodyAdvice tag.
     * @since 1.8.9
     */
    @Parameter()
    private BodyAdvice responseBodyAdvice;
    /**
     * Set the <code>RequestBodyAdvice</code> unified request wrapper class.
     * @since 2.1.4
     */
    @Parameter()
    private BodyAdvice requestBodyAdvice;
    /**
     * Group different <code>Controllers</code>.
     * @since 2.2.5
     */
    @Parameter()
    private List<ApiGroup> groups;
    /**
     * Whether to display the request parameter table in the document.
     * @since 2.2.5
     */
    @Parameter(defaultValue = "true")
    private Boolean requestParamsTable;
    /**
     * Whether to display the response parameter table in the document.
     * @since 2.2.5
     */
    @Parameter(defaultValue = "true")
    private Boolean responseParamsTable;
    /**
     * <code>Spring</code> and <code>Apache Dubbo</code> are frameworks
     * that support parsing and generating documents by <code>smart-doc</code> by default
     * and are not configured <code>framework</code> automatically selects <code>Spring</code>
     * or <code>Dubbo</code> according to the triggered document construction scenario.
     * <code>smart-doc</code> currently also supports the <code>JAX-RS</code> standard,
     * so use a framework that supports the <code>JAX-RS</code> standard
     * (such as: <code>Quarkus</code>) can be used as an experience, but it is not complete yet.
     * Optional values:
     * <code>spring</code>, <code>dubbo</code>, <code>JAX-RS</code>, <code>solon</code>
     * @since 2.2.5
     */
    @Parameter()
    private String framework;
    /**
     * <code>randomMock</code> is used to control
     * whether <code>smart-doc</code> generates random <code>mock</code> values,
     * in versions before <code>2.6.9</code>
     * <code>smart-doc</code> will automatically assign parameters
     * and automatically generate random values.
     * The generated values are different each time.
     * Now you can set it to <code>false</code> to control the generation of random values.
     * @since 2.6.9
     */
    @Parameter(defaultValue = "false")
    private Boolean randomMock;
    /**
     * openapi component key generator<br/>
     * <code>RANDOM</code>: supports <code>@Validated</code> group verification<br/>
     * <code>NORMAL</code>: Does not support <code>@Validated</code>,
     * used for <code>openapi</code> generated code
     * @since 2.7.8
     */
    @Parameter(defaultValue = "RANDOM")
    private ComponentTypeEnum componentType;
    /**
     * @since 3.0.0
     */
    @Parameter(defaultValue = "false")
    private Boolean increment;
    /**
     * When uploading <code>Torna</code>,
     * batch uploading of documents is supported,
     * and the size of document batches can be set.
     * @since 3.0.2
     */
    @Parameter()
    private Integer apiUploadNums;
    /**
     * <code>showValidation</code> is used to control whether <code>smart-doc</code>
     * extracts the JSR validation information of fields for display in the documentation.
     * @since 3.0.3
     */
    @Parameter(defaultValue = "true")
    private Boolean showValidation;
    /**
     * Custom Configurations for JMeter Performance Test Script Generation
     * @since 3.0.4
     */
    @Parameter()
    private JMeter jmeter;
    /**
     * When generating documentation,
     * consider whether to include the default HTTP status codes from frameworks
     * such as Spring MVC's default <code>500</code> and <code>404</code> errors.
     * Currently, only the generation of <code>OpenAPI</code> documentation supports this feature.
     * @since 3.0.5
     */
    @Parameter(defaultValue = "false")
    private Boolean addDefaultHttpStatuses;

    protected final ApiConfig apiConfig = new ApiConfig();
    private File reportOutputDirectory;

    protected abstract void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder);

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            return;
        }
        this.getLog().info("------------------------------------------------------------------------");
        this.getLog().info("Smart-doc Start preparing sources at: " + DateTimeUtil.nowStrTime());
        this.buildApiConfig();
        JavaProjectBuilder javaProjectBuilder = this.buildJavaProjectBuilder(project.getBuild().getSourceDirectory());

        String goal = mojoEx.getGoal();
        getLog().info("Smart-doc Starting Create API Documentation at: " + DateTimeUtil.nowStrTime());
        if (!MojoConstants.TORNA_RPC_MOJO.equals(goal) && !MojoConstants.TORNA_REST_MOJO.equals(goal)) {
            getLog().info("API documentation is output to => " + apiConfig.getOutPath().replace("\\", "/"));
        }
        try {
            this.executeMojo(apiConfig, javaProjectBuilder);
        } catch (Exception e) {
            getLog().error(e);
            if (isStrict) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    private void buildApiConfig() {
        apiConfig.setOutPath(getOutPath());
        apiConfig.setBaseDir(project.getBasedir().getAbsolutePath());
        apiConfig.setCodePath(project.getBuild().getSourceDirectory());
        apiConfig.setClassLoader(ClassLoaderUtil.getRuntimeClassLoader(project));
        apiConfig.setServerUrl(serverUrl);
        apiConfig.setServerEnv(serverEnv);
        apiConfig.setPathPrefix(pathPrefix);
        apiConfig.setAllInOne(allInOne);
        apiConfig.setCoverOld(coverOld);
        apiConfig.setCreateDebugPage(createDebugPage);
        apiConfig.setPackageFilters(packageFilters);
        apiConfig.setPackageExcludeFilters(packageExcludeFilters);
        apiConfig.setMd5EncryptedHtmlName(md5EncryptedHtmlName);
        apiConfig.setStyle(style);
        apiConfig.setProjectName(project.getName());
        apiConfig.setSortByTitle(sortByTitle);
        apiConfig.setShowAuthor(showAuthor);
        apiConfig.setRequestFieldToUnderline(requestFieldToUnderline);
        apiConfig.setResponseFieldToUnderline(responseFieldToUnderline);
        apiConfig.setInlineEnum(inlineEnum);
        apiConfig.setRecursionLimit(recursionLimit);
        apiConfig.setAllInOneDocFileName(allInOneDocFileName);
        apiConfig.setRequestExample(requestExample);
        apiConfig.setResponseExample(responseExample);
        apiConfig.setUrlSuffix(urlSuffix);
        apiConfig.setLanguage(language);
        apiConfig.setDisplayActualType(displayActualType);
        apiConfig.setAppKey(appKey);
        apiConfig.setAppToken(tornaToken);
        apiConfig.setSecret(secret);
        apiConfig.setOpenUrl(openUrl);
        apiConfig.setDebugEnvName(debugEnvName);
        apiConfig.setReplace(replace);
        apiConfig.setDebugEnvUrl(debugEnvUrl);
        apiConfig.setTornaDebug(tornaDebug);
        apiConfig.setAuthor(author);
        apiConfig.setIgnoreRequestParams(ignoreRequestParams);
        apiConfig.setDataDictionaries(dataDictionaries);
        apiConfig.setErrorCodeDictionaries(errorCodeDictionaries);
        apiConfig.setRevisionLogs(revisionLogs);
        apiConfig.setCustomResponseFields(customResponseFields);
        apiConfig.setCustomRequestFields(customRequestFields);
        apiConfig.setRequestHeaders(requestHeaders);
        apiConfig.setRequestParams(requestParams);
        apiConfig.setRpcApiDependencies(rpcApiDependencies);
        apiConfig.setRpcConsumerConfig(rpcConsumerConfig);
        apiConfig.setApiObjectReplacements(apiObjectReplacements);
        apiConfig.setResponseBodyAdvice(responseBodyAdvice);
        apiConfig.setRequestBodyAdvice(requestBodyAdvice);
        apiConfig.setGroups(groups);
        apiConfig.setRequestParamsTable(requestParamsTable);
        apiConfig.setResponseParamsTable(responseParamsTable);
        apiConfig.setFramework(framework);
        apiConfig.setRandomMock(randomMock);
        apiConfig.setComponentType(componentType);
        apiConfig.setIncrement(increment);
        apiConfig.setApiUploadNums(apiUploadNums);
        apiConfig.setShowValidation(showValidation);
        apiConfig.setJmeter(jmeter);
        apiConfig.setAddDefaultHttpStatuses(addDefaultHttpStatuses);
    }

    public String getOutputDirectory() {
        return outputDirectory.getAbsolutePath();
    }

    public String getOutputName() {
        return DESTINATION_DIR + "/api";
    }

    public String getCategoryName() {
        return MavenReport.CATEGORY_PROJECT_REPORTS;
    }

    public String getDescription(Locale locale) {
        return project.getDescription();
    }

    public void setReportOutputDirectory(File reportOutputDirectory) {
        if (this.reportOutputDirectory == null) {
            this.reportOutputDirectory = new File(reportOutputDirectory, DESTINATION_DIR);
        }
    }

    public File getReportOutputDirectory() {
        if (reportOutputDirectory == null) {
            return new File(this.getOutputDirectory());
        }
        return reportOutputDirectory;
    }

    public boolean isExternalReport() {
        return true;
    }

    public boolean canGenerateReport() {
        return !skip;
    }

    protected String getOutPath() {
        return getReportOutputDirectory().getAbsolutePath();
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
        javaDocBuilder.addClassLoader(ClassLoaderUtil.getRuntimeClassLoader(project));
        loadSourcesDependencies(javaDocBuilder);
        javaDocBuilder.setEncoding(project.getModel().getModelEncoding());
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
            DependencyNode rootNode = this.dependencyGraphBuilder.buildDependencyGraph(buildingRequest,
                artifactFilter);
            List<DependencyNode> dependencyNodes = rootNode.getChildren();
            List<Artifact> artifactList = this.getArtifacts(dependencyNodes);
            List<String> projectArtifacts = project.getArtifacts().stream()
                .map(moduleName -> moduleName.getGroupId() + ":" + moduleName.getArtifactId())
                .collect(Collectors.toList());
            artifactList.forEach(artifact -> {
                if (ArtifactFilterUtil.ignoreSpringBootArtifactById(artifact)) {
                    return;
                }
                String artifactName = artifact.getGroupId() + ":" + artifact.getArtifactId();
                if (currentProjectModules.contains(artifactName)) {
                    projectArtifacts.add(artifactName);
                    return;
                }
                if (RegexUtil.isMatches(excludes, artifactName)) {
                    return;
                }
                Artifact sourcesArtifact = repositorySystem.createArtifactWithClassifier(artifact.getGroupId(),
                    artifact.getArtifactId(), artifact.getVersion(), artifact.getType(), "sources");
                if (RegexUtil.isMatches(includes, artifactName)) {
                    projectArtifacts.add(artifactName);
                    this.loadSourcesDependency(javaDocBuilder, sourcesArtifact);
                    return;
                }
                if (CollectionUtil.isEmpty(includes)) {
                    projectArtifacts.add(artifactName);
                    this.loadSourcesDependency(javaDocBuilder, sourcesArtifact);
                }
            });

        } catch (DependencyGraphBuilderException e) {
            throw new MojoExecutionException("Can't build project dependency graph", e);
        }
    }

    /**
     * reference https://github.com/jboz/living-documentation
     * @param javaDocBuilder JavaProjectBuilder
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
        // resolve dependencies
        ArtifactResolutionResult result = repositorySystem.resolve(request);
        // load java source file into javadoc builder
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
                getLog().warn("Unable to load jar source " + artifact + " : " + e.getMessage());
                return;
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
                    getLog().warn("syntax error in jar :" + sourceURL);
                    getLog().warn(e.getMessage());
                }
            }
        });
    }

    /**
     * copy from maven-dependency-plugin tree TreeMojo.java
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
