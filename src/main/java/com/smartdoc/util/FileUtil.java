package com.smartdoc.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xingzi  2020/01/09  21:18
 */
public class FileUtil {
    private static String regex = "^[a-zA-Z]:";
    private static Pattern pattern = Pattern.compile(regex);

    public static boolean isAbsPath(String path) {
        Matcher matcher = pattern.matcher(path);
        if (matcher.find() || path.startsWith("/")) {
            return true;
        } else {
            return false;

        }
    }

}
