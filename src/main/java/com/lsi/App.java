package com.lsi;

import com.lsi.config.DatabaseConfig;
import com.lsi.ui.CommandLineInterface;

public class App {
    public static void main(String[] args) {
        int exitCode = 0;

        try {
            exitCode = new CommandLineInterface().run(args);
        } finally {
            DatabaseConfig.closeDataSource();
        }

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}