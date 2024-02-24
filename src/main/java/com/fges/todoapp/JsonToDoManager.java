package com.fges.todoapp.managers;

import java.nio.file.Path;

public class JsonToDoManager implements ToDoManager {
    private final Path filePath;

    public JsonToDoManager(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public void insertTask(String task) {
        // JSON-specific insert logic
    }

    @Override
    public void listTasks() {
        // JSON-specific list logic
    }
}
