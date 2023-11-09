package sk.upjs.kopr.file_copy;

import java.io.File;
import java.util.concurrent.BlockingQueue;

public class FileSearcher {

	private File rootDir;
	private BlockingQueue<File> fileBlockingQueue;

	public FileSearcher(File rootDir, BlockingQueue<File> fileBlockingQueue) {
		this.rootDir = rootDir;
		this.fileBlockingQueue = fileBlockingQueue;
	}

	public void run() {
		search(rootDir.listFiles());
	}

	private void search(File[] dir) {
		for (int i = 0; i < dir.length; i++) {
			if (dir[i].isDirectory()) {
				search(dir[i].listFiles());
				//System.out.println(dir[i]);
			} else {
				fileBlockingQueue.offer(dir[i]);
				//System.out.println(fileBlockingQueue);
			}
		}
	}

}
