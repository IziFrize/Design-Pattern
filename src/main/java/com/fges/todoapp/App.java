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
            return 1; // Return 1 if there is a failure in parsing arguments.
        }

        String fileName = cmd.getOptionValue("s");
        String doneStatus = cmd.hasOption("d") ? cmd.getOptionValue("d", "true") : "false"; // If --done is specified, default to true
        String outputFileName = cmd.getOptionValue("o", "");

        List<String> positionalArgs = cmd.getArgList();
        if (positionalArgs.isEmpty()) {
            System.err.println("Missing Command");
            return 1; // Return 1 if the command is missing.
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
                        return 1; // Return 1 if the output file is not specified.
                    }
                    break;
                default:
                    System.err.println("Unknown command");
                    return 1; // Return 1 if the command is unknown.
            }
        } catch (Exception e) {
            //System.err.println("An error occurred: " + e.getMessage());
            return 1; // Return 1 if an error occurs during command execution.
        }

        //System.err.println("Done.");
        return 0; // Return 0 if everything worked correctly.
    }

    private static void migrateTodos(String sourceFileName, String outputFileName) throws IOException {
        Path sourcePath = Paths.get(sourceFileName);
        Path outputPath = Paths.get(outputFileName);

        // Lire le contenu du fichier source
        String sourceContent = Files.readString(sourcePath);
        ObjectMapper mapper = new ObjectMapper();

        // Migration de CSV vers JSON
        if (sourceFileName.endsWith(".csv") && outputFileName.endsWith(".json")) {
            List<Map<String, String>> todos = new ArrayList<>();
            if (Files.exists(outputPath)) {
                // Lire et ajouter les todos existants au début de la liste
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
        // Migration de JSON vers CSV
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
        // Migration de JSON vers JSON, en s'assurant de fusionner correctement les tableaux JSON
        else if (sourceFileName.endsWith(".json") && outputFileName.endsWith(".json")) {
            ArrayNode existingTodos = mapper.createArrayNode();
            if (Files.exists(outputPath) && !Files.readString(outputPath).isEmpty()) {
                existingTodos = (ArrayNode) mapper.readTree(Files.readString(outputPath));
            }
            ArrayNode sourceTodos = (ArrayNode) mapper.readTree(sourceContent);

            // Fusionner les tableaux JSON
            existingTodos.addAll(sourceTodos);
            Files.writeString(outputPath, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(existingTodos));
        }
        // Migration de CSV vers CSV, concaténant simplement le contenu
        else if (sourceFileName.endsWith(".csv") && outputFileName.endsWith(".csv")) {
            String existingContent = Files.exists(outputPath) ? Files.readString(outputPath) : "";
            // Initialiser combinedContent avec le contenu existant
            StringBuilder combinedContent = new StringBuilder(existingContent);

            // Vérifier si le contenu existant n'est pas vide et ne se termine pas par un saut de ligne,
            // et que le contenu source n'est pas vide également avant d'ajouter un saut de ligne
            if (!existingContent.isEmpty() && !existingContent.endsWith("\n") && !sourceContent.isEmpty()) {
                combinedContent.append("\n"); // Ajoutez un saut de ligne avant d'ajouter le contenu source
            }

            // Ajouter le contenu source au contenu combiné
            combinedContent.append(sourceContent);

            // Écrire le contenu combiné dans le fichier de destination
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

        // Déterminer le statut basé sur la présence de l'option --done
        // Ici, doneStatus est "true" si --done est présent, sinon "false".
        // Aucune modification n'est nécessaire si vous utilisez déjà cette logique.

        if (!Files.exists(filePath)) {
            Files.createFile(filePath); // Crée le fichier vide
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
            String newLine = todo + ",,,," + doneStatus; // Utilisez le séparateur et ajoutez la tâche avec le statut défini
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

                // Aucune condition de filtrage pour 'false', il doit toujours être affiché
                if (!filterDoneOnly || status.equals("true")) {
                    if (status.equals("true")) {
                        System.out.println("- Done: " + task);
                    } else if (status.equals("false")) {
                        // Afficher la tâche sans préfixe pour le statut 'false'
                        System.out.println("- " + task);
                    } else if (!status.isEmpty()) {
                        // Afficher le statut spécifique s'il n'est pas vide et différent de 'false'
                        System.out.println("- " + status + ": " + task);
                    } else {
                        // Afficher juste la tâche si le statut est vide
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

                        // Aucune condition de filtrage pour 'false', il doit toujours être affiché
                        if (!filterDoneOnly || status.equals("true")) {
                            if (status.equals("true")) {
                                System.out.println("- Done: " + task);
                            } else if (status.equals("false")) {
                                // S'assurer que la tâche n'est pas vide avant d'afficher
                                if (!task.isEmpty()) {
                                    System.out.println("- " + task);
                                }
                            } else if (!status.isEmpty()) {
                                // S'assurer que la tâche n'est pas vide avant d'afficher
                                if (!task.isEmpty()) {
                                    System.out.println("- " + status + ": " + task);
                                }
                            } else {
                                // Si le statut est vide, vérifiez également si la tâche n'est pas vide avant d'afficher
                                if (!task.isEmpty()) {
                                    System.out.println("- " + task);
                                } // Suppression du dernier else et remplacement par un return vide pour ne rien faire si task est vide
                            }
                        }
                    });
        }
    }
}