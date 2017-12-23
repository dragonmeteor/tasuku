package com.moekaku.tasuku.indexed;

import com.moekaku.tasuku.Workspace;

public interface IndexedFileTasks {
    String getPrefix();
    String getCreationCommandName();
    String getCleaningCommandName();
    int[] getShape();
    String getFileTaskName(int... index);
    Workspace getWorkspace();
    int getArity();
}
