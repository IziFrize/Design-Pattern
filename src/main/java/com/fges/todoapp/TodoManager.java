package com.fges.todoapp;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TodoManager {
    public String insertTodo(String fileContent, String todo, String fileType) throws IOException {
        if (fileType.equals("json")) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(fileContent);
            if (actualObj instanceof MissingNode || actualObj.isNull() || !actualObj.isArray()) {
                actualObj = JsonNodeFactory.instance.arrayNode();
            }

            if (actualObj instanceof ArrayNode arrayNode) {
                arrayNode.add(todo);
            }

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualObj);
        } else if (fileType.equals("csv")) {
            if (!fileContent.endsWith("\n") && !fileContent.isEmpty()) {
                fileContent += "\n";
            }
            fileContent += todo;

            return fileContent;
        }

        return fileContent;
    }

    public String listTodos(String fileContent, String fileType) throws IOException {
        if (fileType.equals("json")) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(fileContent);
            if (actualObj instanceof MissingNode || actualObj.isNull() || !actualObj.isArray()) {
                actualObj = JsonNodeFactory.instance.arrayNode();
            }

            if (actualObj instanceof ArrayNode arrayNode) {
                return StreamSupport.stream(arrayNode.spliterator(), false)
                        .map(node -> "- " + node.asText())
                        .collect(Collectors.joining("\n"));
            }
        } else if (fileType.equals("csv")) {
            return Arrays.stream(fileContent.split("\\n"))
                    .map(todo -> "- " + todo)
                    .collect(Collectors.joining("\n"));
        }

        return "";
    }
}
