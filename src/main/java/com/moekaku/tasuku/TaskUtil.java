package com.moekaku.tasuku;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class TaskUtil {
    public static void createDeleteAllTask(Workspace ws, String taskName, List<String> filesToDelete) {
        Logger logger = ws.getLoggerFactory().getLogger(TaskUtil.class.getName());
        ws.newCommandTask(taskName, Collections.emptyList(), () -> {
            for(String fileName : filesToDelete) {
                Path path = ws.getFileSystem().getPath(fileName);
                try {
                    logger.info("Deleting " + fileName + " ...");
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
