package com.alttd.chat.util;

public class ALogger {

    private static org.slf4j.Logger logger;

    public static void init(org.slf4j.Logger log) {
        logger = log;
    }

    private void log(String message) {
        logger.info(message);
    }

    public static void warn(String message) {
        logger.warn(message);
    }

    public static void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void error(String message) {
        logger.error(message);
    }

    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}
