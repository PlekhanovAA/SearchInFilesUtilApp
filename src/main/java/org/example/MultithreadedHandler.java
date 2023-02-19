package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MultithreadedHandler implements FileHandler {
    private static final Logger LOGGER = LogManager.getLogger(MultithreadedHandler.class);

    private final Set<Path> result = Collections.synchronizedSet(new HashSet<>());
    private final AtomicInteger fileCount = new AtomicInteger(0);
    private final long restrictionSizeMB = 5;

    private BlockingQueue<Path> queueFiles;
    private final int queueCapacity;

    private ExecutorService executorService;
    private final int countThreads;

    private String lookingFor;
    private Path pathToFolder;

    private long startTime;

    public MultithreadedHandler(int availableProcessors) {
        countThreads = availableProcessors / 2 + 1;
        queueCapacity = availableProcessors / 2;
    }

    public void startSearching(Path pathToFolder, String lookingFor) {
        LOGGER.info("{} threads are used", countThreads);

        this.lookingFor = lookingFor;
        this.pathToFolder = pathToFolder;
        queueFiles = new LinkedBlockingQueue<>(queueCapacity);
        executorService = Executors.newFixedThreadPool(countThreads);
        startTime = System.currentTimeMillis();

        executorService.execute(this::fileProcessing);
    }

    private void fileProcessing() {
        try {
            for (Path path : Files.newDirectoryStream(pathToFolder)) {
                long sizeKB = Files.size(path) / 1024;
                if (sizeKB < (restrictionSizeMB * 1024)) {
                    fileCount.incrementAndGet();
                    queueFiles.put(path);
                    executorService.execute(this::queueProcessing);
                } else {
                    LOGGER.error("The '{}' file will not be processed because its size is larger than {}MB",
                            path.getFileName(), restrictionSizeMB);
                }
            }
        } catch (InterruptedException | IOException e) {
            LOGGER.error("IOException for '{}' folder that will not be processed", pathToFolder);
        }
    }

    private void queueProcessing() {
        try {
            Path currentPath = queueFiles.take();
            //CharsetDecoder is angry, so I don't use Files.newBufferedReader(path)
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(currentPath.toString())))) {
                while (bufferedReader.ready()) {
                    if (bufferedReader.readLine().contains(lookingFor)) {
                        result.add(currentPath.getFileName());
                        break;
                    }
                }
            } catch (IOException e) {
                LOGGER.error("IOException for '{}' file which will not be processed", currentPath.getFileName());
            }
            if (((ThreadPoolExecutor) executorService).getActiveCount() == 1) {
                showResult();
            }
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException for our threads");
        }
    }

    private void showResult() {
        executorService.shutdown();
        long stopTime = System.currentTimeMillis();
        LOGGER.info("===== RESULTS LIST: ");
        result.forEach(LOGGER::info);
        LOGGER.info("===== CONCLUSION: in {} files it was found '{}'. A total of {} files were processed in {} ms.",
                result.size(), lookingFor, fileCount.get(), (stopTime - startTime));
    }
}
