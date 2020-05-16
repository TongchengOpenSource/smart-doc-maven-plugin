/*
 * smart-doc https://github.com/shalousun/smart-doc
 *
 * Copyright (C) 2018-2020 smart-doc
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

import java.util.HashSet;
import java.util.Set;

/**
 * @author yu 2020/1/13.
 */
public class StartsWithFilterChain implements FilterChain {

    private final static Set<String> PREFIX_SET = new HashSet<>();

    private FilterChain filterChain;

    static {
        PREFIX_SET.add("maven");
        PREFIX_SET.add("asm");
        PREFIX_SET.add("tomcat");
        PREFIX_SET.add("jboss");
        PREFIX_SET.add("undertow");
        PREFIX_SET.add("jackson");
        PREFIX_SET.add("micrometer");
        PREFIX_SET.add("spring-boot-actuator");
        PREFIX_SET.add("sharding");
        PREFIX_SET.add("mybatis-spring-boot-starter");
        PREFIX_SET.add("flexmark");
        PREFIX_SET.add("hibernate-core");
    }

    @Override
    public void setNext(FilterChain nextInChain) {
        this.filterChain = nextInChain;
    }

    @Override
    public boolean ignoreArtifactById(Artifact artifact) {
        String artifactId = artifact.getArtifactId();
        if (PREFIX_SET.stream().anyMatch(artifactId::startsWith)) {
            return true;
        }
        return this.ignore(filterChain, artifact);
    }
}
