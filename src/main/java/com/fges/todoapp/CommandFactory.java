package com.fges.todoapp.factories;

import com.fges.todoapp.commands.Command;
import com.fges.todoapp.commands.MigrateCommand;

public class CommandFactory {
    public static Command getCommand(String[] args) {
        // Exemple de parsing d'arguments, à adapter selon votre implémentation
        String sourcePath = null;
        String outputPath = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--source":
                case "-s":
                    sourcePath = args[++i];
                    break;
                case "--output":
                case "-o":
                    outputPath = args[++i];
                    break;
            }
        }

        if (sourcePath != null && outputPath != null) {
            return new MigrateCommand(sourcePath, outputPath);
        } else {
            throw new IllegalArgumentException("Arguments --source and --output are required for migrate command");
        }
    }
}
