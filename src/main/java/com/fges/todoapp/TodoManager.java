package com.fges.todoapp;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

public class TodoManager {
    public String insertTodo(String fileContent, String todo, boolean isDone, String fileType) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        if (fileType.equals("json")) {
            ArrayNode todos = mapper.readValue(fileContent.isEmpty() ? "[]" : fileContent, ArrayNode.class);
            ObjectNode newTodo = mapper.createObjectNode();
            newTodo.put("name", todo);
            newTodo.put("done", isDone);
            todos.add(newTodo);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(todos);
        } else if (fileType.equals("csv")) {
            return fileContent + todo + (isDone ? ",done" : ",not_done") + "\n";
        }
        return fileContent;
    }

    public String listTodos(String fileContent, boolean onlyDone, String fileType) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder builder = new StringBuilder();
        if (fileType.equals("json")) {
            ArrayNode todos = mapper.readValue(fileContent, ArrayNode.class);
            for (JsonNode todo : todos) {
                String name = todo.get("name").asText();
                boolean done = todo.get("done").asBoolean();
                if (done && onlyDone) {
                    builder.append("Done: ").append(name).append("\n");
                } else if (!onlyDone) {
                    builder.append(done ? "Done: " : "").append(name).append("\n");
                }
            }
        } else if (fileType.equals("csv")) {
            // CSV handling logic here, similar to JSON logic but parsing CSV format
        }
        return builder.toString();
    }
}
