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

            if (command.equals("insert")) {
                if (positionalArgs.size() < 2) {
                    System.err.println("Missing TODO name");
                    return 1;
                }
                String todo = positionalArgs.get(1);
                String updatedContent = todoManager.insertTodo(fileContent, todo, fileType);
                fileProcessor.writeFile(fileName, updatedContent);
            } else if (command.equals("list")) {
                System.out.println(todoManager.listTodos(fileContent, fileType));
            }
        } catch (ParseException ex) {
            System.err.println("Fail to parse arguments: " + ex.getMessage());
            return 1;
        } catch (IOException ex) {
            System.err.println("File error: " + ex.getMessage());
            return 1;
        }

        System.err.println("Done.");
        return 0;
    }
}
