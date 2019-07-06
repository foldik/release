package com.foldik.versioning;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        String rootDir = System.getenv("ROOT_DIR");
        String branch = System.getenv("BRANCH");
        String source = System.getenv("SOURCE_VERSION");
        String target = System.getenv("TARGET_VERSION");
        String changeSet = System.getenv("CHANGE_SET");
        String gitTag = System.getenv("GIT_TAG");
        List<String> changedFiles = changeSet.lines().collect(Collectors.toList());

        System.out.println("Root dir: " + rootDir);
        System.out.println("Branch: " + branch);
        System.out.println("Source: " + source);
        System.out.println("Target: " + target);
        System.out.println("ChangeSet:\n" + changeSet);

        List<Component> components = readAndUpdateComponents(rootDir, changedFiles);
        updateVersions(rootDir, components);
        updateChangeLogFile(rootDir, gitTag, components);
    }

    public static List<Component> readAndUpdateComponents(String rootDir, List<String> changedFiles) {
        OrderedProperties projectConfig = orderedProperties(rootDir + "/project.properties");

        List<Component> components = new ArrayList<>();
        Map<String, Component> componentMap = new HashMap<>();
        for (Object key : projectConfig.orderedKeys()) {
            String project = (String) key;
            String dir = projectConfig.getProperty(project);
            OrderedProperties componentProperties = orderedProperties(rootDir + "/" + dir + "/gradle.properties");

            ChangeStatus changeStatus = ChangeStatus.NOT_CHANGED;
            if (changedFiles.stream().anyMatch(file -> file.startsWith(dir))) {
                changeStatus = ChangeStatus.PROJECT_CHANGED;
            }

            List<Component> dependencies = componentProperties.orderedKeys()
                    .stream()
                    .map(o -> (String) o)
                    .filter(prop -> !"projectVersion".equals(prop))
                    .map(componentVersionProp -> {
                        String componentName = componentVersionProp.replace("Version", "");
                        Component component = componentMap.get(componentName);
                        if (component == null) {
                            throw new RuntimeException("Didn't find dependency " + componentName + " of " + project);
                        }
                        return component;
                    })
                    .collect(Collectors.toList());

            boolean isDependencyChanged = dependencies.stream().anyMatch(d -> !d.getVersion().equals(d.getNextVersion()));
            if (isDependencyChanged && changeStatus == ChangeStatus.PROJECT_CHANGED) {
                changeStatus = ChangeStatus.DEPENDENCY_AND_PROJECT_CHANGED;
            } else if (isDependencyChanged) {
                changeStatus = ChangeStatus.DEPENDENCY_CHANGED;
            }

            Component component = new Component(
                    project,
                    dir,
                    componentProperties.getProperty("projectVersion"),
                    changeStatus,
                    dependencies);
            component.toFixVersion();
            componentMap.put(component.getName(), component);
            components.add(component);
        }
        return components;
    }

    private static void updateVersions(String rootDir, List<Component> components) {
        for (Component component : components) {
            if (component.getChangeStatus() != ChangeStatus.NOT_CHANGED) {
                StringBuilder gradlePropertiesFile = new StringBuilder();
                gradlePropertiesFile.append("projectVersion=").append(component.getNextVersion());
                for (Component dependency : component.getDependencies()) {
                    gradlePropertiesFile
                            .append(System.lineSeparator())
                            .append(dependency.getName())
                            .append("Version=")
                            .append(dependency.getNextVersion());
                }
                try (OutputStream outputStream = new FileOutputStream(rootDir + "/" + component.getDir() + "/gradle.properties", false)) {
                    outputStream.write(gradlePropertiesFile.toString().getBytes(StandardCharsets.UTF_8));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static OrderedProperties orderedProperties(String projectFile) {
        try (InputStream inputStream = new FileInputStream(projectFile)) {
            OrderedProperties properties = new OrderedProperties();
            properties.load(inputStream);
            return properties;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void updateChangeLogFile(String rootDir, String versionTag, List<Component> components) {
        StringBuilder changeLog = new StringBuilder()
                .append("# ")
                .append(versionTag)
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("## Updated components")
                .append(System.lineSeparator());
        for (Component component : components) {
            if (component.getChangeStatus() != ChangeStatus.NOT_CHANGED) {
                changeLog
                        .append(System.lineSeparator())
                        .append("- ")
                        .append(component.getName())
                        .append(" => ")
                        .append(component.getNextVersion());
            }
        }
        changeLog
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("## Overall component stack")
                .append(System.lineSeparator())
        ;
        for (Component component : components) {
            changeLog
                    .append(System.lineSeparator())
                    .append("- ")
                    .append(component.getName()).append(" => ")
                    .append(component.getNextVersion());
        }
        changeLog.append(System.lineSeparator())
                .append(System.lineSeparator());
        String changeLogFile = rootDir + "/CHANGELOG.md";

        try {
            Files.lines(Path.of(changeLogFile))
                    .forEach(line -> changeLog.append(line).append(System.lineSeparator()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (OutputStream outputStream = new FileOutputStream(changeLogFile, false)) {
            outputStream.write(changeLog.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
