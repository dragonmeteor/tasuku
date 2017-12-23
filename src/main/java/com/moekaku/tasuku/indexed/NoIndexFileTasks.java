package com.moekaku.tasuku.indexed;

import com.google.common.base.Preconditions;
import com.moekaku.tasuku.TaskUtil;
import com.moekaku.tasuku.Workspace;

import java.util.Collections;

public abstract class NoIndexFileTasks implements IndexedFileTasks {
    private final Workspace ws;
    private final String resolvedPrefix;
    private final String commandName;

    public NoIndexFileTasks(Workspace ws, String prefix, String commandName) {
        this(ws, prefix, commandName, true);
    }

    public NoIndexFileTasks(Workspace ws, String prefix, String commandName, boolean createTasksImmediately) {
        this.ws = ws;
        this.resolvedPrefix = ws.resolveName(prefix);
        this.commandName = commandName;
        if (createTasksImmediately) {
            createTasks();
        }
    }

    public abstract String getFileTaskName();
    public abstract void createFileTask();

    public void createTasks() {
        if (ws.taskExists(getFileTaskName())) {
            createFileTask();
            ws.newCommandTask(getCreationCommandName(), Collections.singletonList(getFileTaskName()), null);
            TaskUtil.createDeleteAllTask(ws, getCleaningCommandName(),
                    Collections.singletonList(Workspace.getFileName(getFileTaskName())));
        }
    }

    public String getFileTaskName(int... index) {
        Preconditions.checkArgument(index.length == 0,
                "Invalid arity: this indexed file task group has arity 0");
        return getFileTaskName();
    }

    public int[] getShape() {
        return new int[0];
    }

    public String getCreationCommandName() {
        return resolvedPrefix + "/" + commandName;
    }

    public String getCleaningCommandName() {
        return resolvedPrefix + "/" + commandName + "_clean";
    }

    public Workspace getWorkspace() {
        return ws;
    }

    public String getPrefix() {
        return resolvedPrefix;
    }

    public int getArity() {
        return 0;
    }
}
