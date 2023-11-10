package sk.upjs.kopr.file_copy.client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import sk.upjs.kopr.file_copy.FileRequest;

public class FileReceiveTask implements Runnable {
    private static final int BUFFER_SIZE = 16384;
    private String TO_DIR;
    private Socket socket;
    private ConcurrentHashMap<String, Long> dataFromClient;
    private long offset;
    private ObjectInputStream ois;


    public FileReceiveTask(String TO_DIR, Socket socket, ConcurrentHashMap<String, Long> dataFromClient) throws IOException {
        this.TO_DIR = TO_DIR;
        this.socket = socket;
        this.dataFromClient = dataFromClient;
    }

    @Override
    public void run() {
        try {
            ois = new ObjectInputStream(socket.getInputStream());

            while (true) {

                String fileName = ois.readUTF();
                System.out.println("fileName: " + fileName);

                if (fileName.equals("END")) {
                    break;
                }

                File file = new File(TO_DIR + "\\" + fileName);
                System.out.println("file: " + file.getPath());


                if (!dataFromClient.containsKey(file.getName())) {
                    offset = 0;
                } else {
                    offset = dataFromClient.get(file.getName());
                }

                File parent = file.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }

                RandomAccessFile raf = new RandomAccessFile(file, "rw");

                long fileSize = ois.readLong();

                raf.setLength(fileSize);
                System.out.println("fileSize: " + fileSize);

                byte[] receivedData = new byte[BUFFER_SIZE];
                raf.seek(0); // offset

                int readBytes = 0;

                while (offset < fileSize) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    if (fileSize - offset < receivedData.length) {
                        readBytes = ois.read(receivedData, 0, (int) (fileSize - offset));
                    } else {
                        readBytes = ois.read(receivedData, 0, receivedData.length);
                    }

                    raf.seek(offset);
                    raf.write(receivedData, 0, readBytes);
                    offset += readBytes;

                }
                raf.close();

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
