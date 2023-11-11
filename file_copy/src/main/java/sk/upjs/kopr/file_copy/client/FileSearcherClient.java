package sk.upjs.kopr.file_copy.client;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class FileSearcherClient {
    private File rootDir;
    private ConcurrentHashMap<String, Long> clientFiles;

    public FileSearcherClient(File rootDir, ConcurrentHashMap<String, Long> clientFiles) {
        this.rootDir = rootDir;
        this.clientFiles = clientFiles;
    }

    public void run() {
        // if rootDir has files then search
        if (rootDir.listFiles() != null) {
            search(rootDir.listFiles());
        } else {
            // Exception
        }
    }

    private void search(File[] dir) {
        for (int i = 0; i < dir.length; i++) {
            if (dir[i].isDirectory()) {
                search(dir[i].listFiles());
            } else {
                clientFiles.put(dir[i].getPath(), dir[i].length());
            }
        }
    }
}
