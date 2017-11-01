package com.moekaku.tasuku.impl;

import com.moekaku.tasuku.Workspace;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static com.google.common.truth.Truth.assertThat;
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
                .root("ROOT0", "cat")
                .root("ROOT1", "tiger")
                .root("ROOT2", "lion")
                .build();

        assertThat(workspace.resolveName("/ROOT0/test.txt")).isEqualTo("//cat/test.txt");
        assertThat(workspace.resolveName("/ROOT1/test.txt")).isEqualTo("//tiger/test.txt");
        assertThat(workspace.resolveName("/ROOT2/test.txt")).isEqualTo("//lion/test.txt");
    }

    @Test
    public void testResolveName_invalidNames() {
        Workspace workspace = Workspace.builder()
                .root("ROOT0", "cat")
                .root("ROOT1", "tiger")
                .root("ROOT2", "lion")
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
}