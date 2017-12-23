package com.moekaku.tasuku.indexed;

import com.google.common.base.Preconditions;
import com.moekaku.tasuku.TaskUtil;
import com.moekaku.tasuku.Workspace;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class OneIndexFileTasks implements IndexedFileTasks {
    private final Workspace ws;
    private final String resolvedPrefix;
    private final String commandName;
    private final int fileTaskCount;
    private final List<String> fileTaskNames;

    public OneIndexFileTasks(Workspace ws, String prefix, String commandName, int count,
                             boolean createTasksImmediately) {
        this.ws = ws;
        this.resolvedPrefix = ws.resolveName(prefix);
        this.commandName = commandName;
        this.fileTaskCount = count;
        fileTaskNames = IntStream.range(0, count).mapToObj(this::getFileTaskName).collect(Collectors.toList());
        if (createTasksImmediately) {
            createTasks();
        }
    }

    public abstract String getFileTaskName(int index);
    public abstract void createFileTask(int index);

    @Override
    public String getCreationCommandName() {
        return resolvedPrefix + "/" + commandName;
    }

    @Override
    public String getCleaningCommandName() {
        return resolvedPrefix + "/" + commandName + "_clean";
    }

    private void createTasks() {
        if (fileTaskCount == 0) return;
        if (ws.taskExists(fileTaskNames.get(0))) return;
        IntStream.range(0, fileTaskCount).forEach(this::createFileTask);
        ws.newCommandTask(getCreationCommandName(), fileTaskNames, null);
        TaskUtil.createDeleteAllTask(ws, getCleaningCommandName(), fileTaskNames);
    }

    @Override
    public String getPrefix() {
        return resolvedPrefix;
    }

    @Override
    public int[] getShape() {
        return new int[] { fileTaskCount };
    }

    @Override
    public Workspace getWorkspace() {
        return ws;
    }

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public String getFileTaskName(int... index) {
        Preconditions.checkArgument(index.length == 1,
                "Invalid arity: this file task group has arity 1");
        return getFileTaskName(index[0]);
    }
}
