package com.fges.todoapp;


import com.fges.todoapp.commands.Command;
import com.fges.todoapp.factories.CommandFactory;
import com.fges.todoapp.factories.ToDoManagerFactory;
import com.fges.todoapp.managers.ToDoManager;
import org.apache.commons.cli.*;

import java.nio.file.Paths;

public class App {
    public static void main(String[] args) throws Exception {
        System.exit(exec(args));
    }

    public static int exec(String[] args) throws Exception {
        Options cliOptions = new Options();
        CommandLineParser parser = new DefaultParser();
        cliOptions.addRequiredOption("s", "source", true, "File containing the todos");

        CommandLine cmd;
        try {
            cmd = parser.parse(cliOptions, args);
        } catch (ParseException ex) {
            System.err.println("Fail to parse arguments: " + ex.getMessage());
            return 1;
        }

        String fileName = cmd.getOptionValue("s");
        String[] positionalArgs = cmd.getArgList().toArray(new String[0]);
        if (positionalArgs.length < 1) {
            System.err.println("Missing Command");
            return 1;
        }

        String commandType = positionalArgs[0];
        ToDoManager manager = ToDoManagerFactory.getManager(Paths.get(fileName));
        Command command = CommandFactory.getCommand(commandType, manager, positionalArgs);

        try {
            command.execute();
            System.err.println("Done.");
            return 0;
        } catch (Exception e) {
            System.err.println("Error executing command: " + e.getMessage());
            return 1;
        }
    }
}
