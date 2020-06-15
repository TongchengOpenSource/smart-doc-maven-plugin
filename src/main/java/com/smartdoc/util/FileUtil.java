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
package com.smartdoc.util;

import com.power.common.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xingzi  2020/01/09  21:18
 */
public class FileUtil {
    private static String regex = "^[a-zA-Z]:";
    private static Pattern pattern = Pattern.compile(regex);

    public static boolean isAbsPath(String path) {
        if (StringUtil.isEmpty(path)) {
            return false;
        }
        Matcher matcher = pattern.matcher(path);
        if (matcher.find() || path.startsWith("/")) {
            return true;
        } else {
            return false;
        }
    }
}
