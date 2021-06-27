/*
 * smart-doc https://github.com/shalousun/smart-doc
 *
 * Copyright (C) 2018-2021 smart-doc
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
package com.smartdoc.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.power.common.util.FileUtil;
import com.power.common.util.StringUtil;
import com.power.doc.model.*;
import com.smartdoc.constant.GlobalConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

import static com.smartdoc.constant.GlobalConstants.FILE_SEPARATOR;

/**
 * @author xingzi 2019/12/07 21:19
 */
public class MojoUtils {

    /**
     * Gson Object
     */
    public final static Gson GSON = new GsonBuilder().addDeserializationExclusionStrategy(new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
            return false;
        }
    }).create();

    /**
     * Build ApiConfig
     *
     * @param configFile       config file
     * @param projectName      project name
     * @param project          Maven project object
     * @param projectArtifacts project artifacts
     * @param log              maven plugin log
     * @return com.power.doc.model.ApiConfig
     * @throws MojoExecutionException MojoExecutionException
     */
    public static ApiConfig buildConfig(File configFile, String projectName, MavenProject project, List<String> projectArtifacts, Log log) throws MojoExecutionException {
        try {
            ClassLoader classLoader = ClassLoaderUtil.getRuntimeClassLoader(project);
            String data = FileUtil.getFileContent(new FileInputStream(configFile));
            ApiConfig apiConfig = GSON.fromJson(data, ApiConfig.class);
            List<ApiDataDictionary> apiDataDictionaries = apiConfig.getDataDictionaries();
            List<ApiErrorCodeDictionary> apiErrorCodes = apiConfig.getErrorCodeDictionaries();
            List<ApiConstant> apiConstants = apiConfig.getApiConstants();
            BodyAdvice responseBodyAdvice = apiConfig.getResponseBodyAdvice();
            BodyAdvice requestBodyAdvice = apiConfig.getRequestBodyAdvice();
            if (Objects.nonNull(apiErrorCodes)) {
                apiErrorCodes.forEach(
                        apiErrorCode -> {
                            String className = apiErrorCode.getEnumClassName();
                            apiErrorCode.setEnumClass(getClassByClassName(className, classLoader));
                        }
                );
            }
            if (Objects.nonNull(apiDataDictionaries)) {
                apiDataDictionaries.forEach(
                        apiDataDictionary -> {
                            String className = apiDataDictionary.getEnumClassName();
                            apiDataDictionary.setEnumClass(getClassByClassName(className, classLoader));
                        }
                );
            }
            if (Objects.nonNull(apiConstants)) {
                apiConstants.forEach(
                        apiConstant -> {
                            String className = apiConstant.getConstantsClassName();
                            apiConstant.setConstantsClass(getClassByClassName(className, classLoader));
                        }
                );
            }
            if (Objects.nonNull(responseBodyAdvice) && StringUtil.isNotEmpty(responseBodyAdvice.getClassName())) {
                responseBodyAdvice.setWrapperClass(getClassByClassName(responseBodyAdvice.getClassName(), classLoader));
            }
            if (Objects.nonNull(requestBodyAdvice) && StringUtil.isNotEmpty(requestBodyAdvice.getClassName())) {
                requestBodyAdvice.setWrapperClass(getClassByClassName(requestBodyAdvice.getClassName(), classLoader));
            }
            if (StringUtils.isBlank(apiConfig.getProjectName())) {
                apiConfig.setProjectName(projectName);
            }
            addSourcePaths(project, apiConfig, projectArtifacts, log);
            return apiConfig;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 根据 com.xxx.AClass获取类Class
     *
     * @param className   类名
     * @param classLoader urls
     * @return 类类型
     */
    public static Class getClassByClassName(String className, ClassLoader classLoader) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * addSourcePath
     *
     * @param project
     * @param apiConfig
     * @param projectArtifacts
     * @param log
     */
    private static void addSourcePaths(MavenProject project, ApiConfig apiConfig, List<String> projectArtifacts, Log log) {
        List<SourceCodePath> sourceCodePaths = new ArrayList<>();
        // key is module's artifact name, value is module's path
        Map<String, String> modules = new HashMap<>();
        getRootPath(project, modules, log);
        modules.entrySet().forEach(entry -> {
            String key = entry.getKey();
            String modulePath =  entry.getValue();
            projectArtifacts.forEach(artifactName -> {
                if (artifactName.equals(key)) {
                    sourceCodePaths.add(SourceCodePath.builder().setPath(modulePath));
                }
            });
        });

        sourceCodePaths.add(SourceCodePath.builder()
                .setPath(project.getBasedir() + GlobalConstants.SOURCE_CODE_PATH));
        SourceCodePath[] codePaths = new SourceCodePath[sourceCodePaths.size()];
        sourceCodePaths.toArray(codePaths);

        log.info("Artifacts that the current project depends on: " + GSON.toJson(projectArtifacts));
        log.info("Smart-doc has loaded the source code path: " + GSON.toJson(sourceCodePaths)
                .replace("\\", "/").replaceAll("//","/"));

        apiConfig.setSourceCodePaths(codePaths);
    }

    /**
     * get project sourceCode
     *
     * @param file project root path
     * @param path sourceCodePath
     */
    private static void getSourceCodeFilePath(File file, List<String> path) {
        File[] fs = file.listFiles();
        assert fs != null;
        for (File f : fs) {
            if (f.isDirectory()) {
                if (f.getPath().endsWith(GlobalConstants.SOURCE_CODE_PATH) ||
                        f.getPath().endsWith(GlobalConstants.SOURCE_CODE_PATH_REVERSE)) {
                    path.add(f.getPath());
                }
                getSourceCodeFilePath(f, path);
            }
        }
    }

    /**
     * get RootParentPath
     *
     * @param project
     * @return
     */
    private static File getRootPath(MavenProject project, Map<String, String> moduleList, Log log) {
        if (project.hasParent()) {
            MavenProject mavenProject = project.getParent();
            if (log.isDebugEnabled()) {
                log.debug(project.getName() + " parent is: " + mavenProject.getName());
            }
            if (null != mavenProject) {
                if (mavenProject.getBasedir() == null) {
                    return project.getBasedir();
                } else {
                    List<String> modules = mavenProject.getModules();
                    String groupId = mavenProject.getGroupId();
                    for (String module : modules) {
                        moduleList.put(groupId + ":" + module, mavenProject.getBasedir() + FILE_SEPARATOR +
                                module + FILE_SEPARATOR + GlobalConstants.SOURCE_CODE_PATH);
                    }
                    return getRootPath(mavenProject, moduleList, log);
                }
            } else {
                return project.getBasedir();
            }
        } else {
            return project.getBasedir();
        }
    }
}