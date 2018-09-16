package com.moekaku.tasuku.impl;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.moekaku.tasuku.Workspace;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class WorkspaceImplTest {
    @Test
    public void testResolveName_resolvedName() {
        Workspace workspace = Workspace.builder().build();

        assertThat(workspace.resolveName("//test.txt")).isEqualTo("//test.txt");
    }

    @Test
    public void testResolvedName_unqualifiedName() {
        Workspace workspace = Workspace.builder().build();

        assertThat(workspace.resolveName("test.txt")).isEqualTo("//./test.txt");
    }

    @Test
    public void testResolveName_rootedNames() {
        Workspace workspace = Workspace.builder()
                .root("ROOT0", "cat/")
                .root("ROOT1", "tiger/")
                .root("ROOT2", "lion/")
                .build();

        assertThat(workspace.resolveName("/ROOT0/test.txt")).isEqualTo("//cat/test.txt");
        assertThat(workspace.resolveName("/ROOT1/test.txt")).isEqualTo("//tiger/test.txt");
        assertThat(workspace.resolveName("/ROOT2/test.txt")).isEqualTo("//lion/test.txt");
    }

    @Test
    public void testResolveName_invalidNames() {
        Workspace workspace = Workspace.builder()
                .root("ROOT0", "cat/")
                .root("ROOT1", "tiger/")
                .root("ROOT2", "lion/")
                .build();

        assertThrows(IllegalArgumentException.class, () -> workspace.resolveName("/ROOT0abc"));
        assertThrows(IllegalArgumentException.class, () -> workspace.resolveName("/ROOT10/abc"));
    }

    @Test
    public void testRun_singleTask() {
        Workspace workspace = Workspace.builder().build();

        Runnable command1 = Mockito.mock(Runnable.class);
        workspace.newCommandTask("a", Collections.emptyList(), command1);

        workspace.startSession();
        workspace.run("a");
        workspace.endSession();

        verify(command1).run();
    }

    @Test
    public void testEndSession_withoutStarting() {
        Workspace workspace = Workspace.builder().build();
        assertThrows(IllegalStateException.class, workspace::endSession);
    }

    @Test
    public void testRun_outOfSession() {
        Workspace workspace = Workspace.builder().build();
        workspace.newCommandTask("a", Collections.emptyList(), Mockito.mock(Runnable.class));
        assertThrows(IllegalStateException.class, () -> {
            workspace.run("a");
        });
    }

    @Test
    public void testRun_nonExistingTask() {
        Workspace workspace = Workspace.builder().build();
        workspace.newCommandTask("a", Collections.emptyList(), Mockito.mock(Runnable.class));
        workspace.startSession();
        assertThrows(IllegalArgumentException.class, () -> {
            workspace.run("b");
        });
    }

    @Test
    public void testRun_dependencies() {
        Workspace workspace = Workspace.builder().build();

        int[] count = {0};

        int[] aTime = {0};
        workspace.newCommandTask("a", Collections.emptyList(), () -> {
            aTime[0] = ++count[0];
        });

        int[] bTime = {0};
        workspace.newCommandTask("b", Collections.emptyList(), () -> {
            bTime[0] = ++count[0];
        });

        int[] cTime = {0};
        workspace.newCommandTask("c", Arrays.asList("a", "b"), () -> {
            cTime[0] = ++count[0];
        });

        workspace.startSession();
        workspace.run("c");
        workspace.endSession();

        assertThat(aTime[0]).isGreaterThan(0);
        assertThat(bTime[0]).isGreaterThan(0);
        assertThat(cTime[0]).isGreaterThan(0);

        assertThat(aTime[0]).isLessThan(cTime[0]);
        assertThat(bTime[0]).isLessThan(cTime[0]);
    }

    @Test
    public void testRun_taskNotRunTwiceInASession() {
        Workspace workspace = Workspace.builder().build();

        Runnable aCommand = Mockito.mock(Runnable.class);
        workspace.newCommandTask("a", Collections.emptyList(), aCommand);

        workspace.startSession();
        workspace.run("a");
        verify(aCommand).run();
        workspace.run("a");
        workspace.run("a");
        verifyNoMoreInteractions(aCommand);
        workspace.endSession();
    }

    @Test
    public void testCreateTwoAsksWithTheSameName() {
        Workspace workspace = Workspace.builder().build();
        workspace.newCommandTask("a", Collections.emptyList(), Mockito.mock(Runnable.class));
        assertThrows(IllegalArgumentException.class, () -> {
            workspace.newCommandTask("a", Collections.emptyList(), Mockito.mock(Runnable.class));
        });
    }

    private static void writeTextFile(FileSystem fs, String fileName, Consumer<BufferedWriter> contentProvider) {
        try {
            Path path = fs.getPath(fileName);
            OutputStream stream = Files.newOutputStream(path);
            BufferedWriter fout = new BufferedWriter(new OutputStreamWriter(stream));
            contentProvider.accept(fout);
            fout.close();
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readTextFile(FileSystem fs, String fileName) {
        try {
            Path path = fs.getPath(fileName);
            long fileSize = Files.size(path);
            if (fileSize > Integer.MAX_VALUE) {
                throw new RuntimeException("file too large to read");
            }
            ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
            SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ);
            channel.read(buffer);
            channel.close();
            buffer.rewind();
            return new String(buffer.array());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testFileTasks_singleFile() {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        final Workspace workspace = Workspace.builder()
                .fileSystem(fs)
                .root("ROOT0", "/")
                .build();
        workspace.newFileTask("a.txt", Collections.emptyList(), () -> {
            writeTextFile(workspace.getFileSystem(), "/a.txt", (BufferedWriter fout) -> {
                try {
                    fout.write("Hello, world");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        workspace.startSession();
        workspace.run("a.txt");
        workspace.endSession();

        assertThat(Files.exists(fs.getPath("/a.txt"))).isTrue();
    }

    @Test
    public void testFileTasks_fileTasks_dependencyDoesNotExists() {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        writeTextFile(fs, "/a.txt", (BufferedWriter fout) -> {
            try {
                fout.write("123");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        final Workspace workspace = Workspace.builder()
                .fileSystem(fs)
                .root("ROOT0", "/")
                .build();

        workspace.newFileTask("a.txt", Collections.singletonList("b.txt"), () ->
                writeTextFile(workspace.getFileSystem(), "/a.txt", (BufferedWriter fout) -> {
                    try {
                        fout.write("456");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));

        workspace.newFileTask("b.txt", Collections.emptyList(), () ->
        writeTextFile(workspace.getFileSystem(), "/b.txt", (BufferedWriter fout) -> {
           try {
               fout.write("abc");
           } catch (IOException e) {
               throw new RuntimeException(e);
           }
        }));

        workspace.startSession();
        workspace.run("a.txt");
        workspace.endSession();

        assertThat(Files.exists(fs.getPath("/a.txt"))).isTrue();
        assertThat(Files.exists(fs.getPath("/b.txt"))).isTrue();

        assertThat(readTextFile(fs, "/a.txt")).isEqualTo("456");
    }

    @Test
    public void testRoot_mustEndWithSlash() {
        assertThrows(IllegalArgumentException.class, () -> {
           Workspace.builder().root("ROOT0", "abc");
        });
    }
}