package com.fges.todoapp;

public interface ToDoManager {
    void insertTask(String task) throws Exception;
    void listTasks() throws Exception;
}
