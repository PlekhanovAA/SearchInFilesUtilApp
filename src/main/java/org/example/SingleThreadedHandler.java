package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class SingleThreadedHandler implements FileHandler {
    private static final Logger LOGGER = LogManager.getLogger(SingleThreadedHandler.class);

    private static final long restrictionSizeMB = 5;
    private static final Set<Path> result = new HashSet<>();

    public void startSearching(Path pathToFolder, String lookingFor) {
        LOGGER.info("Only one thread is used");

        int fileCount = 0;
        long startTime = System.currentTimeMillis();

        try {
            for (Path path : Files.newDirectoryStream(pathToFolder)) {
                long sizeKB = Files.size(path) / 1024;
                if (sizeKB < (restrictionSizeMB * 1024)) {
                    fileCount++;
                    //CharsetDecoder is angry, so I don't use Files.newBufferedReader(path)
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(path.toString())))) {
                        while (bufferedReader.ready()) {
                            if (bufferedReader.readLine().contains(lookingFor)) {
                                result.add(path.getFileName());
                                break;
                            }
                        }
                    } catch (IOException e) {
                        LOGGER.error("IOException for '{}' file which will not be processed", path.getFileName());
                    }
                } else {
                    LOGGER.error("The '{}' file will not be processed because its size is larger than {}MB",
                            path.getFileName(), restrictionSizeMB);
                }
            }
        } catch (IOException e) {
            LOGGER.error("IOException for '{}' folder that will not be processed", pathToFolder);
        }

        long stopTime = System.currentTimeMillis();
        LOGGER.info("===== RESULTS LIST: ");
        result.forEach(LOGGER::info);
        LOGGER.info("===== CONCLUSION: in {} files it was found '{}'. A total of {} files were processed in {} ms.",
                result.size(), lookingFor, fileCount, (stopTime - startTime));
    }

}
