package sk.upjs.kopr.file_copy.server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import sk.upjs.kopr.file_copy.Constants;
import sk.upjs.kopr.file_copy.FileInfo;
import sk.upjs.kopr.file_copy.FileRequest;

public class FileSendTask implements Runnable {

    private static final int BLOCK_SIZE = 16384; // 16 kB
    private final BlockingQueue<File> fileToSend;
    private final Socket socket;
    private final ConcurrentHashMap<String, Long> dataFromClient;
    private long offset;
    private File file;
    private String fileName;


    public FileSendTask(BlockingQueue<File> fileToSend, Socket socket, ConcurrentHashMap<String, Long> dataFromClient) throws FileNotFoundException {
        this.fileToSend = fileToSend;
        this.socket = socket;
        this.dataFromClient = dataFromClient;
    }

    @Override
    public void run() {
        // For each connection i have i will send one file through oos stream

        try {
            file = fileToSend.take();
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            while (!fileToSend.isEmpty()) {
                fileName = file.getPath().substring(Constants.FROM_DIR.lastIndexOf('\\') + 1);

                if(dataFromClient == null || !dataFromClient.containsKey(fileName)){
                    offset = 0;
                } else {
                    offset = dataFromClient.get(fileName);
                    if(offset == file.length()){
                        file = fileToSend.take();
                        continue;
                    }
                }

                System.out.println("nacitavam subor: " + file.getPath());

                long fileSize = file.length();
                oos.writeUTF(fileName);
                oos.writeLong(fileSize);
                oos.flush();

                byte[] buffer = new byte[BLOCK_SIZE];
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                raf.seek(offset);

                while(offset < fileSize){
                    if (fileSize - offset < buffer.length) {
                        buffer = new byte[(int) (fileSize - offset)];
                    }
                    offset += raf.read(buffer);
                    oos.write(buffer);
                }
                System.out.println("posielam subor: " + file.getPath());
                oos.flush();
                raf.close();

                file = fileToSend.take();
            }


        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
