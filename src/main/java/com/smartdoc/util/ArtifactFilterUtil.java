package com.smartdoc.util;

import org.apache.maven.artifact.Artifact;

/**
 * Artifact filter util
 *
 * @author yu 2020/1/11.
 */
public class ArtifactFilterUtil {

    /**
     * ignoreArtifact
     *
     * @param artifact Artifact
     * @return boolean
     */
    public static boolean ignoreArtifact(Artifact artifact) {
        if ("test".equals(artifact.getScope())) {
            return true;
        }
        String artifactId = artifact.getArtifactId();
        if (artifactId.startsWith("maven")) {
            return true;
        }
        if (artifactId.startsWith("asm")) {
            return true;
        }
        if (artifactId.startsWith("tomcat") || artifactId.startsWith("jboss") || artifactId.startsWith("undertow")) {
            return true;
        }

        if (artifactId.startsWith("jackson")) {
            return true;
        }

        if (artifactId.contains("log4j") || artifactId.contains("logback") || artifactId.contains("slf4j")) {
            return true;
        }

        if (artifactId.startsWith("micrometer") || artifactId.startsWith("spring-boot-actuator")) {
            return true;
        }
        return ignoreArtifactById(artifactId);
    }

    private static boolean ignoreArtifactById(String artifactId) {
        switch (artifactId) {
            case "bcprov-jdk15on":
            case "lombok":
            case "jsqlparser":
            case "disruptor":
            case "commons-codec":
            case "snakeyaml":
            case "spring-boot-autoconfigure":
            case "HikariCP":
            case "mysql-connector-java":
            case "classmate":
            case "commons-lang3":
            case "spring-web":
            case "spring-webmvc":
            case "hibernate-validator":
                return true;
            default:
                return false;
        }
    }

    public static boolean ignoreSpringBootArtifactById(String artifactId) {
        switch (artifactId) {
            case "spring-boot":
            case "spring-boot-starter-actuator":
            case "spring-boot-starter":
            case "spring-boot-starter-undertow":
            case "spring-boot-starter-aop":
            case "spring-boot-starter-json":
            case "spring-boot-starter-web":
                return true;
            default:
                return false;
        }
    }
}
