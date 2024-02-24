package com.fges.todoapp.commands;

import com.fges.todoapp.managers.ToDoManager;

public class ListCommand implements Command {
    private final ToDoManager manager;

    public ListCommand(ToDoManager manager) {
        this.manager = manager;
    }

    @Override
    public void execute() throws Exception {
        manager.listTasks();
    }
}
