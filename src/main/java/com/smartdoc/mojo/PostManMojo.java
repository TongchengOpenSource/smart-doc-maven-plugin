package com.smartdoc.mojo;

import com.power.doc.builder.PostmanJsonBuilder;
import com.power.doc.model.ApiConfig;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

import static com.smartdoc.util.MojoUtils.buildConfig;

/**
 * @author xingzi
 * @date 2019 12 07  17:35
 */
@Mojo(name = "postman")
public class PostManMojo extends AbstractMojo {


    @Parameter(property = "configFile",defaultValue = "./src/main/resources/default.json")
    private File configFile;
    @Parameter(property = "projectName")
    private String projectName;
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;


    @Override
    public void execute() {

        ApiConfig apiConfig =  buildConfig(configFile,projectName,project);
        if(apiConfig ==null){
            System.out.println("构建config文件失败 检查配置文件是否正确");
            return;
        }
        PostmanJsonBuilder.buildPostmanApi(apiConfig);
    }

}
