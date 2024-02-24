package com.fges.todoapp.commands;

import com.fges.todoapp.managers.ToDoManager;
import com.fges.todoapp.managers.ToDoManagerFactory;

public class MigrateCommand implements Command {
    private final String sourcePath;
    private final String outputPath;

    public MigrateCommand(String sourcePath, String outputPath) {
        this.sourcePath = sourcePath;
        this.outputPath = outputPath;
    }

    @Override
    public void execute() throws Exception {
        ToDoManager sourceManager = ToDoManagerFactory.getManager(sourcePath);
        ToDoManager targetManager = ToDoManagerFactory.getManager(outputPath);

        var tasks = sourceManager.listTasks();
        for (String task : tasks) {
            targetManager.insertTask(task);
        }
    }
}
