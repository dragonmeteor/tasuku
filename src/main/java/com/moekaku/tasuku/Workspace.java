package com.moekaku.tasuku;

import com.google.common.base.Preconditions;
import com.moekaku.tasuku.impl.WorkspaceImpl;
import org.slf4j.ILoggerFactory;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Workspace {
    String DEFAULT_ROOT = "ROOT0";

    String resolveName(String name);
    boolean taskExists(String taskName);
    void newCommandTask(String taskName, List<String> dependencies, Runnable action);
    void newFileTask(String taskName, List<String> dependencies, Runnable action);
    void newPlaceholderTask(String taskName);
    void startSession();
    void endSession();
    void run(String taskName);
    boolean needsToRun(String taskName);
    boolean canRun(String taskName);
    boolean isInSession();
    FileSystem getFileSystem();
    ILoggerFactory getLoggerFactory();
    Path getFilePath(String taskName);
    Map<String, String> getRoots();
    Set<String> getTaskNames();

    interface Builder {
        Builder root(String shortName, String path);
        Builder loggerFactory(ILoggerFactory loggerFactory);
        Builder fileSystem(FileSystem fileSystem);
        Workspace build();
    }

    static Builder builder() {
        return new WorkspaceImpl.Builder();
    }

    static String getFileName(String resolvedTaskName) {
        Preconditions.checkArgument(resolvedTaskName.startsWith("//"),
                "Given task name is not resolved!");
        return resolvedTaskName.substring(2);
    }
}
