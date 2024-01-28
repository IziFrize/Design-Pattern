package com.fges.todoapp;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandLineParser {
    private Options options;

    public CommandLineParser() {
        options = new Options();
        options.addRequiredOption("s", "source", true, "File containing the todos");
    }

    public CommandLine parse(String[] args) throws ParseException {
        DefaultParser parser = new DefaultParser();
        return parser.parse(options, args);
    }
}
