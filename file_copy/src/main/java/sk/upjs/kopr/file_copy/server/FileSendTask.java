package sk.upjs.kopr.file_copy.server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import sk.upjs.kopr.file_copy.AppController;
import sk.upjs.kopr.file_copy.Constants;
import sk.upjs.kopr.file_copy.FileInfo;
import sk.upjs.kopr.file_copy.FileRequest;
import sk.upjs.kopr.file_copy.client.Client;

public class FileSendTask implements Runnable {
    //16384
    private static final int BLOCK_SIZE = 64000;
    private final BlockingQueue<File> fileToSend;
    private final Socket socket;
    private final ConcurrentHashMap<String, Long> dataFromClient;
    private long offset;
    private File file;
    private String fileName;
    private boolean isInterrup = false;
    private ObjectOutputStream oos;
    private final int TPC_CONNECTIONS;


    public FileSendTask(BlockingQueue<File> fileToSend, Socket socket, ConcurrentHashMap<String, Long> dataFromClient, int TPC_CONNECTIONS) throws FileNotFoundException {
        this.fileToSend = fileToSend;
        this.socket = socket;
        this.dataFromClient = dataFromClient;
        this.TPC_CONNECTIONS = TPC_CONNECTIONS;
    }

    @Override
    public void run() {
        // For each connection i have i will send one file through oos stream

        try {
            file = fileToSend.take();
            oos = new ObjectOutputStream(socket.getOutputStream());

            while (file != Constants.POISON_PILL) {


                fileName = file.getPath().substring(Constants.FROM_DIR.lastIndexOf('\\') + 1);

                if (dataFromClient == null || !dataFromClient.containsKey(fileName)) {
                    offset = 0;
                } else {
                    offset = dataFromClient.get(fileName);
                    if (offset == file.length()) {
                        file = fileToSend.take();
                        continue;
                    }
                }

                //System.out.println("nacitavam subor: " + file.getPath());

                long fileSize = file.length();
                oos.writeUTF(fileName);
                oos.writeLong(fileSize);
                oos.flush();

                byte[] buffer = new byte[BLOCK_SIZE];
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                raf.seek(offset);

                while (offset < fileSize) {
                    if (fileSize - offset < buffer.length) {
                        buffer = new byte[(int) (fileSize - offset)];
                    }
                    offset += raf.read(buffer);
                    oos.write(buffer);
                }
                System.out.println("posielam subor: " + fileName + " cez vlakno: " + Thread.currentThread().getName());
                oos.flush();
                raf.close();

                file = fileToSend.take();
            }

            // tu bolo toto moje pod tym
            // chcem ukoncit vlakna a zavriet starter aspon

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                // moje
                System.out.println("poslal som vsetky subory");

                oos.writeUTF("poison.pill");
                System.out.println("posielam poison.pill cez vlakno " + Thread.currentThread().getName());
                oos.flush();
                // moje
                oos.close();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
