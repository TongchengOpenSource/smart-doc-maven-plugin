package com.smartdoc.mojo;

import com.power.doc.builder.HtmlApiDocBuilder;
import com.power.doc.model.ApiConfig;

import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * @author yu 2020/1/8.
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = "test")
public class TestMojo extends AbstractDocsGeneratorMojo {

    @Override
    public void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder) {
        HtmlApiDocBuilder.builderControllersApi(apiConfig);
    }
}
