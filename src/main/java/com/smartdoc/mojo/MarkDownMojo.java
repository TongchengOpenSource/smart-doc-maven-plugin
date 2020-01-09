package com.smartdoc.mojo;

import com.power.doc.builder.ApiDocBuilder;
import com.power.doc.model.ApiConfig;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * @author xingzi 2019/12/06 17:38
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = "markdown")
public class MarkDownMojo extends AbstractDocsGeneratorMojo {


    @Override
    public void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder)  {
        ApiDocBuilder.buildApiDoc(apiConfig,javaProjectBuilder);
    }
}
