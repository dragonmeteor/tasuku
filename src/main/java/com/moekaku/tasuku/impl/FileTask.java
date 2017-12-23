package com.moekaku.tasuku.impl;

import com.moekaku.tasuku.Task;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.List;

public class FileTask extends AbstractTask {
    private final Runnable action;
    private final Logger logger;

    FileTask(WorkspaceImpl workspace,
             String name,
             List<String> dependencies, Runnable action,
             ILoggerFactory loggerFactory) {
        super(workspace, name, dependencies);
        this.action = action;
        this.logger = loggerFactory.getLogger(getClass().getName());
    }

    @Override
    public long getTimestamp() {
        return getFileLastModified(getName());
    }

    @Override
    public boolean canRun() {
        return true;
    }

    @Override
    public void run() {
        if (action != null) action.run();
    }

    @Override
    public boolean needsToBeRun() {
        if (!fileExists(getName())) {
            logger.info(String.format(
                    "Task %s will be run because the corresponding file does not exists.",
                    getName()));
            return true;
        } else {
            for (String dep : getDependencies()) {
                if (workspace.needsToRun(dep)) {
                    logger.info(String.format(
                            "Task  %s will be run because dependency %s also needs to be run.",
                            getName(), dep));
                    return true;
                } else {
                    long selfTimestamp = getTimestamp();
                    Task depTask = workspace.getTask(dep);
                    if (depTask.getTimestamp() > selfTimestamp) {
                        if (depTask instanceof FileTask || depTask instanceof PlaceholderTask) {
                            logger.info(String.format(
                                    "Task %s needs to be run because task %s has later time stamp.",
                                    getName(), dep));
                        } else if (depTask instanceof CommandTask) {
                            logger.info(String.format(
                                    "Task %s needs to be run because task %s is a command.",
                                    getName(), dep));
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
