package com.smartdoc.mojo;

import com.power.doc.builder.OpenApiBuilder;
import com.power.doc.model.ApiConfig;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Support Open Api 3.0+
 *
 * @author yu 2020/8/19.
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = "openapi", requiresDependencyResolution = ResolutionScope.COMPILE)
public class OpenApiMojo extends BaseDocsGeneratorMojo {

    @Override
    public void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder) {
        try {
            OpenApiBuilder.buildOpenApi(apiConfig, javaProjectBuilder);
        } catch (Exception e) {
            getLog().error(e);
        }
    }
}
