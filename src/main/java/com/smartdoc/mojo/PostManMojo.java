package com.smartdoc.mojo;

import com.power.doc.builder.PostmanJsonBuilder;
import com.power.doc.model.ApiConfig;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * @author xingzi 2019/12/07 17:35
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = "postman")
public class PostManMojo extends AbstractDocsGeneratorMojo {

    @Override
    public void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder)  {
        PostmanJsonBuilder.buildPostmanApi(apiConfig,javaProjectBuilder);
    }
}
