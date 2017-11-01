package com.moekaku.tasuku.impl;

import com.moekaku.tasuku.Workspace;

import java.util.Collections;

public class PlaceholderTask extends AbstractTask {
    PlaceholderTask(WorkspaceImpl workspace, String name) {
        super(workspace, name, Collections.emptyList());
    }

    @Override
    public long getTimestamp() {
        return getFileLastModified(getName());
    }

    @Override
    public boolean canRun() {
        return false;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("A placeholder task cannot be run! " +
                "You either ran it by invoking it directly or by invoking a task " +
                "that depends on the placeholder, but the file with the same name as the " +
                "placeholder does not exists.");
    }

    @Override
    public boolean needsToBeRun() {
        return !fileExists(getName());
    }
}
