package org.example;

import java.nio.file.Path;

public interface FileHandler {
    void startSearching(Path pathToFolder, String lookingFor);
}
