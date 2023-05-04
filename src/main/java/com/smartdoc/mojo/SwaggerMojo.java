/*
 * smart-doc https://github.com/smart-doc-group/smart-doc
 *
 * Copyright (C) 2018-2023 smart-doc
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.smartdoc.mojo;

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
 *
 * @author xingzi
 * Date 2022/10/13 21:31
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = MojoConstants.SWAGGER_MOJO, requiresDependencyResolution = ResolutionScope.COMPILE)
public class SwaggerMojo extends BaseDocsGeneratorMojo {
    @Override
    public void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder) {
        try {
            SwaggerBuilder.buildOpenApi(apiConfig, javaProjectBuilder);
        } catch (Throwable e) {
            getLog().error(e);
            if (apiConfig.isStrict()) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }
}
