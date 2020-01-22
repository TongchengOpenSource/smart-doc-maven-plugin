/*
 * smart-doc https://github.com/shalousun/smart-doc
 *
 * Copyright (C) 2019-2020 smart-doc
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
package com.smartdoc.chain;

import org.apache.maven.artifact.Artifact;

/**
 * @author yu 2020/1/13.
 */
public class StartsWithFilterChain implements FilterChain {

    private FilterChain filterChain;

    @Override
    public void setNext(FilterChain nextInChain) {
        this.filterChain = nextInChain;
    }

    @Override
    public boolean ignoreArtifactById(Artifact artifact) {
        String artifactId = artifact.getArtifactId();
        if (artifactId.startsWith("maven")) {
            return true;
        }
        if (artifactId.startsWith("asm")) {
            return true;
        }
        if (artifactId.startsWith("tomcat") ||
                artifactId.startsWith("jboss") ||
                artifactId.startsWith("undertow")) {
            return true;
        }
        if (artifactId.startsWith("jackson")) {
            return true;
        }
        if (artifactId.startsWith("micrometer") ||
                artifactId.startsWith("spring-boot-actuator")) {
            return true;
        }
        if (artifactId.startsWith("sharding")) {
            return true;
        }
        return this.ignore(filterChain, artifact);
    }
}
