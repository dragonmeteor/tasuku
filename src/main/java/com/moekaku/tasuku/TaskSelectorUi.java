package com.moekaku.tasuku;

import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TaskSelectorUi extends JFrame implements TreeSelectionListener, ActionListener {
    private final Workspace ws;

    public TaskSelectorUi(Workspace ws) {
        this.ws = ws;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {

    }

    static class TaskNode {
        final String taskName;
        final String displayName;
        final HashSet<String> childrenSet = new HashSet<>();
        final ArrayList<String> childrenList = new ArrayList<>();

        TaskNode(String taskName) {
            this.taskName = taskName;
            this.displayName = FilenameUtils.getName(taskName);
        }

        TaskNode(String taskName, String displayName) {
            this.taskName = taskName;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    static class WorkspaceModel implements TreeModel {
        private final Workspace ws;
        private final HashMap<String, TaskNode> nodes = new HashMap<>();

        WorkspaceModel(Workspace ws) {
            this.ws = ws;
            TaskNode taskRoot = new TaskNode("", "Tasks");
            nodes.put("", taskRoot);
        }

        @Override
        public Object getRoot() {
            return nodes.get("");
        }

        @Override
        public Object getChild(Object parent, int index) {
            return null;
        }

        @Override
        public int getChildCount(Object parent) {
            return 0;
        }

        @Override
        public boolean isLeaf(Object node) {
            return false;
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {

        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            return 0;
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {

        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {

        }
    }
}
