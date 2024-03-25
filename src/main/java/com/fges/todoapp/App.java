package com.fges.todoapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class App {

    public static void main(String[] args) throws Exception {
        System.exit(exec(args));
    }

    public static int exec(String[] args) throws IOException {
        Options cliOptions = new Options();
        CommandLineParser parser = new DefaultParser();

        cliOptions.addRequiredOption("s", "source", true, "File containing the todos");
        cliOptions.addOption("d", "done", false, "Si présent, la tâche est finie");
        cliOptions.addOption("o", "output", true, "Output file for migrate command");


        CommandLine cmd;
        try {
            cmd = parser.parse(cliOptions, args);
        } catch (ParseException ex) {
            System.err.println("Fail to parse arguments: " + ex.getMessage());
            return 1;
        }

        String fileName = cmd.getOptionValue("s");
        String doneStatus = cmd.hasOption("d") ? cmd.getOptionValue("d", "true") : "false"; // If --done is specified, default to true
        String outputFileName = cmd.getOptionValue("o", "");

        List<String> positionalArgs = cmd.getArgList();
        if (positionalArgs.isEmpty()) {
            System.err.println("Missing Command");
            return 1;
        }

        String command = positionalArgs.get(0);
        Path filePath = Paths.get(fileName);
        String fileContent = Files.exists(filePath) ? Files.readString(filePath) : "";

        try {
            switch (command) {
                case "insert":
                    insertTodo(positionalArgs, fileName, fileContent, doneStatus);
                    break;
                case "list":
                    listTodos(fileName, fileContent, doneStatus);
                    break;
                case "migrate":
                    if (!outputFileName.isEmpty()) {
                        migrateTodos(fileName, outputFileName);
                    } else {
                        System.err.println("Output file not specified");
                        return 1;
                    }
                    break;
                default:
                    System.err.println("Unknown command");
                    return 1;
            }
        } catch (Exception e) {

            return 1;
        }


        return 0;
    }

    private static void migrateTodos(String sourceFileName, String outputFileName) throws IOException {
        Path sourcePath = Paths.get(sourceFileName);
        Path outputPath = Paths.get(outputFileName);


        String sourceContent = Files.readString(sourcePath);
        ObjectMapper mapper = new ObjectMapper();


        if (sourceFileName.endsWith(".csv") && outputFileName.endsWith(".json")) {
            List<Map<String, String>> todos = new ArrayList<>();
            if (Files.exists(outputPath)) {

                String existingContent = Files.readString(outputPath);
                if (!existingContent.isEmpty()) {
                    todos.addAll(mapper.readValue(existingContent, new TypeReference<List<Map<String, String>>>() {}));
                }
            }

            String[] lines = sourceContent.split("\n");
            for (String line : lines) {
                String[] parts = line.split(",,,,", -1);
                Map<String, String> todo = new HashMap<>();
                todo.put("task", parts[0]);
                todo.put("done", parts.length > 1 ? parts[1] : "false");
                todos.add(todo);
            }

            Files.writeString(outputPath, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(todos));
        }

        else if (sourceFileName.endsWith(".json") && outputFileName.endsWith(".csv")) {
            StringBuilder csvBuilder = new StringBuilder();
            if (Files.exists(outputPath)) {
                csvBuilder.append(Files.readString(outputPath));
            }

            JsonNode jsonNode = mapper.readTree(sourceContent);
            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {

                    String task = node.path("task").asText("");
                    String done = node.path("done").asText("false");
                    csvBuilder.append("\n").append(task).append(",,,,").append(done).append("\n");
                }
            }

            Files.writeString(outputPath, csvBuilder.toString());
        }
        else if (sourceFileName.endsWith(".json") && outputFileName.endsWith(".json")) {
            ArrayNode existingTodos = mapper.createArrayNode();
            if (Files.exists(outputPath) && !Files.readString(outputPath).isEmpty()) {
                existingTodos = (ArrayNode) mapper.readTree(Files.readString(outputPath));
            }
            ArrayNode sourceTodos = (ArrayNode) mapper.readTree(sourceContent);

            existingTodos.addAll(sourceTodos);
            Files.writeString(outputPath, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(existingTodos));
        }
        else if (sourceFileName.endsWith(".csv") && outputFileName.endsWith(".csv")) {
            String existingContent = Files.exists(outputPath) ? Files.readString(outputPath) : "";

            StringBuilder combinedContent = new StringBuilder(existingContent);

            if (!existingContent.isEmpty() && !existingContent.endsWith("\n") && !sourceContent.isEmpty()) {
                combinedContent.append("\n");
            }

            combinedContent.append(sourceContent);

            Files.writeString(outputPath, combinedContent.toString());
        }
        else {
            System.err.println("La migration entre des formats différents n'est pas supportée.");
        }
    }




    private static void insertTodo(List<String> positionalArgs, String fileName,
                                   String fileContent, String doneStatus) throws IOException {
        String todo = positionalArgs.get(1).trim();
        Path filePath = Paths.get(fileName);



        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }

        if (fileName.endsWith(".json")) {
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode arrayNode = fileContent.isEmpty() ? mapper.createArrayNode() : (ArrayNode) mapper.readTree(fileContent);

            ObjectNode todoNode = mapper.createObjectNode();
            todoNode.put("task", todo);
            todoNode.put("done", doneStatus);
            arrayNode.add(todoNode);
            Files.writeString(filePath, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode));
        } else if (fileName.endsWith(".csv")) {
            String newLine = todo + ",,,," + doneStatus;
            fileContent += fileContent.isEmpty() ? "" : "\n";
            fileContent += newLine;
            Files.writeString(filePath, fileContent);
        }
    }





    private static void listTodos(String fileName, String fileContent, String doneStatus) throws IOException {
        boolean filterDoneOnly = "true".equals(doneStatus);

        if (fileName.endsWith(".json")) {
            ObjectMapper mapper = new ObjectMapper();
            ArrayNode arrayNode = mapper.readValue(fileContent.isEmpty() ? "[]" : fileContent, ArrayNode.class);
            for (JsonNode node : arrayNode) {
                String task = node.get("task").asText();
                String status = node.has("done") ? node.get("done").asText() : "";

                if (!filterDoneOnly || status.equals("true")) {
                    if (status.equals("true")) {
                        System.out.println("- Done: " + task);
                    } else if (status.equals("false")) {
                        System.out.println("- " + task);
                    } else if (!status.isEmpty()) {
                        System.out.println("- " + status + ": " + task);
                    } else {
                        System.out.println("- " + task);
                    }
                }
            }
        } else if (fileName.endsWith(".csv")) {
            Arrays.stream(fileContent.split("\n"))
                    .forEach(line -> {
                        String[] parts = line.split(",,,,", -1);
                        String task = parts[0];
                        String status = parts.length > 1 ? parts[1] : "";

                        if (!filterDoneOnly || status.equals("true")) {
                            if (status.equals("true")) {
                                System.out.println("- Done: " + task);
                            } else if (status.equals("false")) {
                                if (!task.isEmpty()) {
                                    System.out.println("- " + task);
                                }
                            } else if (!status.isEmpty()) {
                                if (!task.isEmpty()) {
                                    System.out.println("- " + status + ": " + task);
                                }
                            } else {
                                if (!task.isEmpty()) {
                                    System.out.println("- " + task);
                                }
                            }
                        }
                    });
        }
    }
}