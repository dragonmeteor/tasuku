package com.moekaku.tasuku.impl;

import java.util.List;

public class CommandTask extends AbstractTask {
    private final Runnable action;

    CommandTask(WorkspaceImpl workspace, String name, List<String> dependencies, Runnable action) {
        super(workspace, name, dependencies);
        this.action = action;
    }

    @Override
    public long getTimestamp() {
        return Long.MAX_VALUE;
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
        return true;
    }
}
