package com.moekaku.tasuku;

import com.moekaku.tasuku.impl.FileTaskGroupBuilderImpl;

import java.util.List;
import java.util.function.Consumer;

public interface FileTaskGroupBuilder {
    void add(String fileTaskName, String group);
    void add(String fileTaskName, String... group);
    void add(String fileTaskName, List<String> group);
    String getResolvedPrefix();

    static void build(Workspace ws, String prefix, Consumer<FileTaskGroupBuilder> taskCreator) {
        FileTaskGroupBuilderImpl builder = new FileTaskGroupBuilderImpl(ws, prefix);
        taskCreator.accept(builder);
        builder.build();
    }
}
