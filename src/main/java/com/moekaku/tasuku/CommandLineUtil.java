package com.moekaku.tasuku;

import java.io.*;

public class CommandLineUtil {
    private static final String COMMAND_FILE_NAME = ".temp/command.sh";
    private static final String COMMAND_DIR = ".temp";

    private static void writeCommandFile(String content) {
        try {
            BufferedWriter fout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(COMMAND_FILE_NAME)));
            fout.write(content);
            fout.flush();
            fout.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getPlatformDependentCommand(String command) {
        String platform = System.getProperty("os.name").toLowerCase();
        if (platform.startsWith("linux")) {
            new File(COMMAND_DIR).mkdirs();
            writeCommandFile(command);
            return COMMAND_FILE_NAME;
        } else {
            return command;
        }
    }

    public static void run(String command) {
        command = getPlatformDependentCommand(command);
        ProcessBuilder builder = new ProcessBuilder(command.split(" "));
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            InputStream input = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String runAndGetOutput(String command) {
        command = getPlatformDependentCommand(command);
        try {
            Process process = Runtime.getRuntime().exec(command);
            InputStream input = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
