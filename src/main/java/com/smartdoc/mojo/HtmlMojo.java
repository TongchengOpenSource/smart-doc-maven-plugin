package com.smartdoc.mojo;


import com.power.doc.builder.HtmlApiDocBuilder;
import com.power.doc.model.ApiConfig;
import com.smartdoc.constant.GlobalConstants;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

import static com.smartdoc.util.MojoUtils.buildConfig;

/**
 * @author xingzi 2019/12/06 14:50
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = "html")
public class HtmlMojo extends AbstractMojo {

    @Parameter(property = "configFile", defaultValue = GlobalConstants.DEFAULT_CONFIG)
    private File configFile;

    @Parameter(property = "projectName")
    private String projectName;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() {
        ApiConfig apiConfig = buildConfig(configFile, projectName, project);
        if (apiConfig == null) {
            System.out.println(GlobalConstants.ERROR_MSG);
            return;
        }
        HtmlApiDocBuilder.builderControllersApi(apiConfig);
    }
}
