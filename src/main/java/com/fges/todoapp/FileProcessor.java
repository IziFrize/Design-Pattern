package com.fges.todoapp;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

public class FileProcessor {
    public String readFile(String fileName) throws IOException {
        Path filePath = Paths.get(fileName);
        if (Files.exists(filePath)) {
            return Files.readString(filePath);
        }
        return "";
    }

    public void writeFile(String fileName, String content) throws IOException {
        Path filePath = Paths.get(fileName);
        Files.writeString(filePath, content);
    }
}
