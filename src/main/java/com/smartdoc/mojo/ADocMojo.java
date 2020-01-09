package com.smartdoc.mojo;

import com.power.doc.builder.AdocDocBuilder;
import com.power.doc.model.ApiConfig;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * @author xingzi  2019/12/13  16:23
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = "adoc")
public class ADocMojo extends AbstractDocsGeneratorMojo {

    @Override
    public void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder)   {
        AdocDocBuilder.buildApiDoc(apiConfig,javaProjectBuilder);
    }

}
