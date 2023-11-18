package sk.upjs.kopr.file_copy.server;

import sk.upjs.kopr.file_copy.Constants;
import sk.upjs.kopr.file_copy.client.Client;

import java.io.File;
import java.util.concurrent.BlockingQueue;

public class FileSearcherServer {

    private File rootDir;
    private BlockingQueue<File> fileBlockingQueue;
    private int numberOfTpcConnections;

    public FileSearcherServer(File rootDir, BlockingQueue<File> fileBlockingQueue, int numberOfTpcConnections) {
        this.rootDir = rootDir;
        this.fileBlockingQueue = fileBlockingQueue;
        this.numberOfTpcConnections = numberOfTpcConnections;
    }

    public void run() {
        // if rootDir has files then search
        if (rootDir.listFiles() != null) {
            search(rootDir.listFiles());
            for (int i = 0; i < numberOfTpcConnections; i++) {
                fileBlockingQueue.offer(Constants.POISON_PILL);
            }

        } else {
            System.out.println("Nemam co poslat");
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
