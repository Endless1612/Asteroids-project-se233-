package com.example.Exception;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class ExceptionHandler extends Exception  {
    private static final Logger logger = (Logger) LogManager.getLogger(ExceptionHandler.class);
    private static ExceptionHandler instance;

    public static void handle(Exception e, String contextMessage) {
        //
        logger.error("An error occurred in: " + contextMessage, e);

        Platform.runLater(() -> {

            logger.error("An error occurred. Please restart the game.");
        });
    }

    public static ExceptionHandler getInstance() {
        if (instance == null) {
            instance = new ExceptionHandler();
        }
        return instance;
    }

    public static void setHandlerInstance(ExceptionHandler mockInstance) {
        instance = mockInstance;
    }

    public static void resetHandlerInstance() {
        instance = null;
    }

}

