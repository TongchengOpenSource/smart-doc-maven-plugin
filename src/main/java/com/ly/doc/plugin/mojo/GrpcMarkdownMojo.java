/*
 * smart-doc
 *
 * Copyright (C) 2018-2024 smart-doc
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
package com.ly.doc.plugin.mojo;

import com.ly.doc.builder.grpc.GrpcMarkdownBuilder;
import com.ly.doc.model.ApiConfig;
import com.ly.doc.plugin.constant.MojoConstants;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * grpc-markdown
 *
 * @author linwumingshi
 * @since 3.0.7
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = MojoConstants.GRPC_MARKDOWN_MOJO, requiresDependencyResolution = ResolutionScope.COMPILE)
public class GrpcMarkdownMojo extends BaseDocsGeneratorMojo {

    @Override
    public void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder) {
        try {
            GrpcMarkdownBuilder.buildApiDoc(apiConfig, javaProjectBuilder);
        } catch (Throwable e) {
            getLog().error(e);
            if (apiConfig.isStrict()) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }
}
