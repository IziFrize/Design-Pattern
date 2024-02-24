package com.fges.todoapp.managers;

import com.fges.todoapp.managers.ToDoManager;

import java.nio.file.Path;

public class CsvToDoManager implements ToDoManager {
    private final Path filePath;

    public CsvToDoManager(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public void insertTask(String task) {
        // CSV-specific insert logic
    }

    @Override
    public void listTasks() {
        // CSV-specific list logic
    }
}
