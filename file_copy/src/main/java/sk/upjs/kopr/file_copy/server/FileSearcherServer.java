package sk.upjs.kopr.file_copy.server;

import java.io.File;
import java.util.concurrent.BlockingQueue;

public class FileSearcherServer {

    private File rootDir;
    private BlockingQueue<File> fileBlockingQueue;

    public FileSearcherServer(File rootDir, BlockingQueue<File> fileBlockingQueue) {
        this.rootDir = rootDir;
        this.fileBlockingQueue = fileBlockingQueue;
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
                fileBlockingQueue.offer(dir[i]);
            }
        }
    }

}
