package org.example;

public class FileHandlerFactory {

    private final int availableProcessors;
    private final boolean multiThreadMode;

    public FileHandlerFactory(boolean multiThreadMode) {
        this.multiThreadMode = multiThreadMode;
        availableProcessors = Runtime.getRuntime().availableProcessors();
    }

    public FileHandler getHandler() {
        if (multiThreadMode && availableProcessors > 1) {
            return new MultithreadedHandler(availableProcessors);
        }
        return new SingleThreadedHandler();
    }

}
