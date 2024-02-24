package com.fges.todoapp.commands;

import com.fges.todoapp.managers.ToDoManager;

public class InsertCommand implements Command {
    private final ToDoManager manager;
    private final String task;

    public InsertCommand(ToDoManager manager, String task) {
        this.manager = manager;
        this.task = task;
    }

    @Override
    public void execute() throws Exception {
        manager.insertTask(task);
    }
}
