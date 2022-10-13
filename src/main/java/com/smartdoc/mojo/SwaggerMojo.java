package com.smartdoc.mojo;

import com.power.doc.builder.openapi.OpenApiBuilder;
import com.power.doc.builder.openapi.SwaggerBuilder;
import com.power.doc.model.ApiConfig;
import com.smartdoc.constant.MojoConstants;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Support swagger2.0
 * @author xingzi
 * Date 2022/10/13 21:31
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = MojoConstants.SWAGGER_MOJO, requiresDependencyResolution = ResolutionScope.COMPILE)
public class SwaggerMojo extends BaseDocsGeneratorMojo{
        @Override
        public void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder) {
            try {
                SwaggerBuilder.buildOpenApi(apiConfig, javaProjectBuilder);
            } catch (Exception e) {
                getLog().error(e);
            }
        }
}
