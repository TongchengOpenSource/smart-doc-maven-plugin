package com.smartdoc.mojo;

import com.power.doc.model.ApiConfig;
import com.smartdoc.constant.GlobalConstants;
import com.smartdoc.util.ClassLoaderUtil;
import com.smartdoc.util.FileUtil;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import static com.smartdoc.util.MojoUtils.buildConfig;

/**
 * @author yu 2020/1/8.
 */
public abstract class AbstractDocsGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    @Parameter(property = "configFile", defaultValue = GlobalConstants.DEFAULT_CONFIG)
    private File configFile;

    @Parameter(property = "projectName")
    private String projectName;

    @Component
    protected RepositorySystem repositorySystem;

    protected JavaProjectBuilder javaProjectBuilder;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        javaProjectBuilder = buildJavaProjectBuilder();
        ApiConfig apiConfig = buildConfig(configFile, projectName, project, getLog());
        if (apiConfig == null) {
            getLog().info(GlobalConstants.ERROR_MSG);
            return;
        }
        if (!FileUtil.isAbsPath(apiConfig.getOutPath())) {
            apiConfig.setOutPath(project.getBasedir().getPath() + "/" + apiConfig.getOutPath());
            getLog().info("API File IN " + apiConfig.getOutPath());
        } else {
            getLog().info("API File IN (C:):" + apiConfig.getOutPath());
        }


        this.executeMojo(apiConfig, javaProjectBuilder);
    }

    public abstract void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder)
            throws MojoExecutionException, MojoFailureException;

    /**
     * Classloading
     *
     * @return
     * @throws MojoExecutionException
     */
    private JavaProjectBuilder buildJavaProjectBuilder() throws MojoExecutionException {
        JavaProjectBuilder javaDocBuilder = new JavaProjectBuilder();
        javaDocBuilder.setEncoding(Charset.defaultCharset().toString());
        javaDocBuilder.setErrorHandler(e -> getLog().warn(e.getMessage()));
        //addSourceTree
        javaDocBuilder.addSourceTree(new File("src/main/java"));
        //sources.stream().map(File::new).forEach(javaDocBuilder::addSourceTree);
        javaDocBuilder.addClassLoader(ClassLoaderUtil.getRuntimeClassLoader(project));
        loadSourcesDependencies(javaDocBuilder);
        return javaDocBuilder;
    }

    /**
     * load sources
     *
     * @param javaDocBuilder
     */
    private void loadSourcesDependencies(JavaProjectBuilder javaDocBuilder) {
        PluginDescriptor pluginDescriptor = ((PluginDescriptor) getPluginContext().get("pluginDescriptor"));
        Stream.concat(project.getDependencies().stream(),
                project.getPlugin(pluginDescriptor.getPluginLookupKey()).getDependencies().stream())
                .forEach(dependency -> {
                    Artifact sourcesArtifact = repositorySystem.createArtifactWithClassifier(dependency.getGroupId(),
                            dependency.getArtifactId(), dependency.getVersion(), dependency.getType(), "sources");
                    loadSourcesDependency(javaDocBuilder, sourcesArtifact);
                });
    }

    private void loadSourcesDependency(JavaProjectBuilder javaDocBuilder, Artifact sourcesArtifact) {
        // create request
        ArtifactResolutionRequest request = new ArtifactResolutionRequest();
        request.setArtifact(sourcesArtifact);
        // resolve deps
        ArtifactResolutionResult result = repositorySystem.resolve(request);

        // load source file into javadoc builder
        result.getArtifacts().forEach(artifact -> {
            try (JarFile jarFile = new JarFile(artifact.getFile())) {
                for (Enumeration<?> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                    JarEntry entry = (JarEntry) entries.nextElement();
                    String name = entry.getName();
                    if (name.endsWith(".java") && !name.endsWith("/package-info.java")) {
                        javaDocBuilder.addSource(
                                new URL("jar:" + artifact.getFile().toURI().toURL().toString() + "!/" + name));
                    }
                }
            } catch (Exception e) {
                getLog().warn("Unable to load jar source " + artifact + " : " + e.getMessage());
            }
        });
    }
}
