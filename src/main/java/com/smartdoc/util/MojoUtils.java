package com.smartdoc.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.power.common.util.FileUtil;
import com.power.doc.model.ApiConfig;
import com.power.doc.model.ApiDataDictionary;
import com.power.doc.model.ApiErrorCodeDictionary;
import com.power.doc.model.SourceCodePath;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Objects;

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
     * @param configFile  config file
     * @param projectName project name
     * @param project     Maven project object
     * @return com.power.doc.model.ApiConfig
     */
    public static ApiConfig buildConfig(File configFile, String projectName, MavenProject project, Log log) {
        try {
            URL[] runtimeUrls;
            List runtimeClasspathElements = project.getRuntimeClasspathElements();
            runtimeUrls = new URL[runtimeClasspathElements.size()];
            for (int i = 0; i < runtimeClasspathElements.size(); i++) {
                String element = (String) runtimeClasspathElements.get(i);
                runtimeUrls[i] = new File(element).toURI().toURL();
            }
            String data = FileUtil.getFileContent(new FileInputStream(configFile));
            ApiConfig apiConfig = GSON.fromJson(data, ApiConfig.class);

            List<ApiDataDictionary> apiDataDictionaries = apiConfig.getDataDictionaries();
            List<ApiErrorCodeDictionary> apiErrorCodes = apiConfig.getErrorCodeDictionaries();
            if (apiErrorCodes != null) {
                apiErrorCodes.forEach(
                        apiErrorCode -> {
                            String className = apiErrorCode.getEnumClassName();
                            apiErrorCode.setEnumClass(getClassByClassName(className, runtimeUrls));
                        }
                );
            }
            if (apiDataDictionaries != null) {
                apiDataDictionaries.forEach(
                        apiDataDictionary -> {
                            String className = apiDataDictionary.getEnumClassName();
                            apiDataDictionary.setEnumClass(getClassByClassName(className, runtimeUrls));
                        }
                );
            }
            if (!StringUtils.isBlank(apiConfig.getProjectName())) {
                apiConfig.setProjectName(projectName);
            }
            addSourcePaths(project, apiConfig, log);
            return apiConfig;
        } catch (FileNotFoundException | MalformedURLException | DependencyResolutionRequiredException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据 com.xxx.AClass获取类Class
     *
     * @param className   类名
     * @param runtimeUrls urls
     * @return 类类型
     */
    public static Class getClassByClassName(String className, URL[] runtimeUrls) {
        try {
            URLClassLoader newLoader = new URLClassLoader(runtimeUrls,
                    Thread.currentThread().getContextClassLoader());
            return newLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void addSourcePaths(MavenProject project, ApiConfig apiConfig, Log log) {
        List<String> sourceRoots = project.getCompileSourceRoots();
        sourceRoots.forEach(s -> apiConfig.setSourceCodePaths(SourceCodePath.path().setPath(s)));
        if (project.hasParent()) {
            MavenProject mavenProject = project.getParent();
            if (null != mavenProject) {
                log.info("--- parent project name is [" + mavenProject.getName() + "]");
                File file = mavenProject.getBasedir();
                if (!Objects.isNull(file)) {
                    log.info("--- parent project basedir is " + file.getPath());
                    apiConfig.setSourceCodePaths(SourceCodePath.path().setPath(file.getPath()));
                    log.info("--- smart-doc-maven-plugin loaded resource from " + file.getPath());
                } else {
                    log.info("WARN: smart-doc-maven-plugin checked you have a parent project, but not found basedir.");
                }
            }
        } else {
            log.info("--- This is a single module project.");
        }
    }
}