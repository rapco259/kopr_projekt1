package sk.upjs.kopr.file_copy.server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import sk.upjs.kopr.file_copy.FileInfo;
import sk.upjs.kopr.file_copy.FileRequest;

public class FileSendTask implements Runnable {

    private static final int BLOCK_SIZE = 16384; // 16 kB
    private final BlockingQueue<File> fileToSend;
    private final Socket socket;
    private final ConcurrentHashMap<String, Long> dataFromClient;
    private final ExecutorService executor;


    public FileSendTask(ExecutorService executor, BlockingQueue<File> fileToSend, Socket socket, ConcurrentHashMap<String, Long> dataFromClient) throws FileNotFoundException {
        this.fileToSend = fileToSend;
        this.socket = socket;
        this.dataFromClient = dataFromClient;
        this.executor = executor;
    }

    @Override
    public void run() {
        // For each connection i have i will send one file through oos stream
        File file;
        while (!fileToSend.isEmpty()) {

            try {
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                file = fileToSend.take();
                Long offset;
                long len = file.length();
                // zatial je toto 0, potom sa to zmeni podla dataFromClient
                offset = 0L;

                RandomAccessFile raf = new RandomAccessFile(file, "r");
                // oos.writeLong(OFFSET);
                raf.seek(offset);

                long totalRead = offset;
                int read;
                long chunk;
                byte[] buffer;
                if (len - offset < BLOCK_SIZE) {
                    chunk = len - offset;
                    buffer = new byte[(int) chunk];
                } else {
                    chunk = BLOCK_SIZE;
                    buffer = new byte[BLOCK_SIZE];
                }

                oos.writeLong(chunk);

                while (totalRead < len && (read = raf.read(buffer, 0, (int) chunk)) >= 0) {
                    totalRead += read;
                    oos.write(buffer);
                    if (len - totalRead < BLOCK_SIZE) {
                        chunk = len - totalRead;
                        buffer = new byte[(int) chunk];
                    } else {
                        chunk = BLOCK_SIZE;
                        buffer = new byte[BLOCK_SIZE];
                    }
                    oos.writeLong(chunk);
                    oos.flush();

                }


            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }


    }
}
