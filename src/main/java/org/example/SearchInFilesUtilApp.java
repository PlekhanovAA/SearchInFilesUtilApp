package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class SearchInFilesUtilApp {
    private static final Logger LOGGER = LogManager.getLogger(SearchInFilesUtilApp.class);

    private static String pathToFolder;
    private static String lookingFor;
    private  static boolean multiThreadMode;

    public static void main(String[] args) {
        LOGGER.info("Application is started...");

        if (isConfigApp() && verifyConfig()) {
            letsGo();
        };
    }

    private static boolean isConfigApp() {
        try (InputStream inputStream = new FileInputStream("./config.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            pathToFolder = properties.getProperty("folder.path");
            lookingFor = properties.getProperty("what.need.to.find");
            multiThreadMode = Boolean.parseBoolean(properties.getProperty("multiThread.mode"));
        } catch (IOException e) {
            LOGGER.error("File config.properties not found");
            return false;
        }

        return true;
    }

    private static boolean verifyConfig() {
        if (pathToFolder == null || lookingFor == null) {
            LOGGER.error("Property folder.path or what.need.to.find not found");
            return false;
        }
        if (lookingFor.length() > 100) {
            LOGGER.error("Property what.need.to.find should not be more 100 characters");
            return false;
        }
        if (!Files.exists(Paths.get(pathToFolder))) {
            LOGGER.error("The folder specified in property folder.path not exist");
            return false;
        }

        return true;
    }

    private static void letsGo() {
        FileHandlerFactory factory = new FileHandlerFactory(multiThreadMode);
        FileHandler fileHandler = factory.getHandler();
        fileHandler.startSearching(Paths.get(pathToFolder), lookingFor);
    }

}