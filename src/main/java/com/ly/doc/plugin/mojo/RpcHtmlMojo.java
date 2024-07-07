/*
 * smart-doc
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
package com.ly.doc.plugin.mojo;

import com.ly.doc.builder.rpc.RpcHtmlBuilder;
import com.ly.doc.model.ApiConfig;
import com.ly.doc.plugin.constant.MojoConstants;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkFactory;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.reporting.MavenMultiPageReport;
import org.apache.maven.reporting.MavenReportException;

import java.util.Locale;

/**
 * @author yu 2020/5/26.
 */
@Execute(phase = LifecyclePhase.COMPILE)
@Mojo(name = MojoConstants.RPC_HTML_MOJO, requiresDependencyResolution = ResolutionScope.COMPILE)
public class RpcHtmlMojo extends BaseDocsGeneratorMojo implements MavenMultiPageReport {

    @Override
    public void executeMojo(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder) {
        RpcHtmlBuilder.buildApiDoc(apiConfig, javaProjectBuilder);
    }

    @Override
    public void generate(Sink sink, SinkFactory sinkFactory, Locale locale)
        throws MavenReportException {
        try {
            super.execute();
        } catch (MojoExecutionException | MojoFailureException e) {
            throw new MavenReportException(e.getMessage(), e);
        }
    }

    @Override
    public void generate(org.codehaus.doxia.sink.Sink sink, Locale locale) throws MavenReportException {
        generate(sink, null, locale);
    }

    public void generate(Sink sink, Locale locale) throws MavenReportException {
        generate(sink, null, locale);
    }

    @Override
    public String getName(Locale locale) {
        return "Smart Doc Dubbo API";
    }
}
