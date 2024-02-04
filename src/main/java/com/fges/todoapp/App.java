package com.fges.todoapp;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import java.io.IOException;

public class App {
    public static void main(String[] args) throws Exception {
        System.exit(exec(args));
    }

    public static int exec(String[] args) {
        CommandLineParser commandLineParser = new CommandLineParser();
        FileProcessor fileProcessor = new FileProcessor();
        TodoManager todoManager = new TodoManager();

        try {
            CommandLine cmd = commandLineParser.parse(args);

            String fileName = cmd.getOptionValue("s");
            java.util.List<String> positionalArgs = cmd.getArgList();
            if (positionalArgs.isEmpty()) {
                System.err.println("Missing Command");
                return 1;
            }

            String command = positionalArgs.get(0);
            String fileContent = fileProcessor.readFile(fileName);
            String fileType = fileName.endsWith(".json") ? "json" : "csv";
            boolean isDone = cmd.hasOption("d");

            if ("insert".equals(command)) {
                if (positionalArgs.size() < 2) {
                    System.err.println("Missing TODO name");
                    return 1;
                }
                String todo = positionalArgs.get(1);
                String updatedContent = todoManager.insertTodo(fileContent, todo, isDone, fileType);
                fileProcessor.writeFile(fileName, updatedContent);
            } else if ("list".equals(command)) {
                System.out.println(todoManager.listTodos(fileContent, isDone, fileType));
            }
        } catch (ParseException | IOException ex) {
            System.err.println("Error: " + ex.getMessage());
            return 1;
        }

        return 0;
    }
}
