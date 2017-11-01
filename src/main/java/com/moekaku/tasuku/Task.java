package com.moekaku.tasuku;

import java.util.List;

public interface Task extends Runnable {
    long getTimestamp();
    boolean canRun();
    boolean needsToBeRun();
    String getName();
    List<String> getDependencies();
}
