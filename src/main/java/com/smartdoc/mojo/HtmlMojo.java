package com.smartdoc.mojo;


import com.power.doc.builder.HtmlApiDocBuilder;
import com.power.doc.model.ApiConfig;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;


/**
 * @author xingzi 2019/12/06 14:50
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = "html")
public class HtmlMojo extends AbstractDocsGeneratorMojo {

    @Override
    public void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder) throws MojoExecutionException, MojoFailureException {
        HtmlApiDocBuilder.buildApiDoc(apiConfig,javaProjectBuilder);
    }
}
