package com.fges.todoapp.factories;

import java.nio.file.Path;

public class ToDoManagerFactory {
    public static ToDoManager getManager(Path filePath) {
        if (filePath.toString().endsWith(".json")) {
            return new JsonToDoManager(filePath);
        } else if (filePath.toString().endsWith(".csv")) {
            return new CsvToDoManager(filePath);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + filePath);
        }
    }
}
