package org.example.userservice;

import org.example.userservice.console.ConsoleApp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Application starting...");

        try {
            ConsoleApp app = new ConsoleApp();
            app.start();
        } catch (Exception e) {
            logger.error("Fatal error: {}", e.getMessage(), e);
            System.err.println("Критическая ошибка: " + e.getMessage());
        } finally {
            logger.info("Application finished");
        }
    }
}