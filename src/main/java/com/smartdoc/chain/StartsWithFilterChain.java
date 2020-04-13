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

import java.util.ArrayList;
import java.util.List;

/**
 * @author yu 2020/1/13.
 */
public class StartsWithFilterChain implements FilterChain {

    private final static List<String> PREFIX_LIST = new ArrayList<>();

    private FilterChain filterChain;

    static {
        PREFIX_LIST.add("maven");
        PREFIX_LIST.add("asm");
        PREFIX_LIST.add("tomcat");
        PREFIX_LIST.add("jboss");
        PREFIX_LIST.add("undertow");
        PREFIX_LIST.add("jackson");
        PREFIX_LIST.add("micrometer");
        PREFIX_LIST.add("spring-boot-actuator");
        PREFIX_LIST.add("sharding");
        PREFIX_LIST.add("mybatis-spring-boot-starter");
        PREFIX_LIST.add("flexmark");
    }

    @Override
    public void setNext(FilterChain nextInChain) {
        this.filterChain = nextInChain;
    }

    @Override
    public boolean ignoreArtifactById(Artifact artifact) {
        String artifactId = artifact.getArtifactId();
        if (PREFIX_LIST.stream().anyMatch(artifactId::startsWith)) {
            return true;
        }
        return this.ignore(filterChain, artifact);
    }
}
