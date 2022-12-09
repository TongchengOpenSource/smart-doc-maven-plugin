package com.smartdoc.mojo;

import com.power.doc.builder.openapi.YApiSwaggerBuilder;
import com.power.doc.model.ApiConfig;
import com.smartdoc.constant.MojoConstants;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Send API documents to YApi
 *
 * @author zhutw 2022/12/09.
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = MojoConstants.YAPI_REST_MOJO, requiresDependencyResolution = ResolutionScope.COMPILE)
public class YApiRestMojo extends BaseDocsGeneratorMojo {

    @Override
    public void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder) {
        try {
            YApiSwaggerBuilder.buildOpenApi(apiConfig, javaProjectBuilder);
        } catch (Exception e) {
            getLog().error(e);
            if (apiConfig.isStrict()) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }
}

