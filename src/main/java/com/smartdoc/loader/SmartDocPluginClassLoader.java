/**
 * Copyright © 2010-2017 Nokia
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartdoc.loader;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Represents the classpath built from a maven project's dependencies.
 */
public class SmartDocPluginClassLoader {

    /**
     * Provides a class loader that can be used to load classes from this
     * project classpath.
     *
     * @param project the maven project currently being built
     * @param parent  a classloader which should be used as the parent of the newly
     *                created classloader.
     * @param log     object to which details of the found/loaded classpath elements
     *                can be logged.
     * @return a classloader that can be used to load any class that is
     * contained in the set of artifacts that this project classpath is
     * based on.
     * @throws DependencyResolutionRequiredException if maven encounters a problem resolving project dependencies
     */
    private ClassLoader getClassLoader(MavenProject project, final ClassLoader parent, Log log) throws DependencyResolutionRequiredException {
        @SuppressWarnings("unchecked")
        List<String> classpathElements = project.getCompileClasspathElements();
        final List<URL> classpathUrls = new ArrayList<>(classpathElements.size());
        for (String classpathElement : classpathElements) {
            try {
                log.debug("Adding project artifact to classpath: " + classpathElement);
                classpathUrls.add(new File(classpathElement).toURI().toURL());
            } catch (MalformedURLException e) {
                log.debug("Unable to use classpath entry as it could not be understood as a valid URL: " + classpathElement, e);
            }

        }
        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () ->
                new URLClassLoader(classpathUrls.toArray(new URL[classpathUrls.size()]), parent));
    }

    public ClassLoader getClassLoader(MavenProject project, Log log) {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader newClassLoader = new SmartDocPluginClassLoader().getClassLoader(project, oldClassLoader, log);
            Thread.currentThread().setContextClassLoader(newClassLoader);
            return newClassLoader;
        } catch (DependencyResolutionRequiredException e) {
            log.info("Skipping addition of project artifacts, there appears to be a dependecy resolution problem", e);
        }
        return oldClassLoader;
    }

}
