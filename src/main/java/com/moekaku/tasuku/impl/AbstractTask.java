package com.moekaku.tasuku.impl;

import com.moekaku.tasuku.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractTask implements Task {
    final WorkspaceImpl workspace;
    private final String name;
    private final List<String> dependencies;

    AbstractTask(WorkspaceImpl workspace, String name, List<String> dependencies) {
        this.workspace = workspace;
        this.name = workspace.resolveName(name);
        this.dependencies = dependencies.stream()
                .map(workspace::resolveName)
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getDependencies() {
        return dependencies;
    }

    long getFileLastModified(String taskName) {
        Path path = workspace.getFilePath(taskName);
        if (!Files.exists(path)) {
            return Long.MAX_VALUE;
        } else {
            try {
                return Files.getLastModifiedTime(path).toMillis();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    boolean fileExists(String taskName) {
        return Files.exists(workspace.getFilePath(taskName));
    }
}
