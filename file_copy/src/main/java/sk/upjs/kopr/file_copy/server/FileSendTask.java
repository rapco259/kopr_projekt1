package sk.upjs.kopr.file_copy.server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import sk.upjs.kopr.file_copy.Constants;

public class FileSendTask implements Runnable {
    //16384
    private final BlockingQueue<File> fileToSend;
    private final Socket socket;
    private final ConcurrentHashMap<String, Long> dataFromClient;
    private long offset;
    private File file;
    private String fileName;
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

                //System.out.println("fileName: " + fileName + "datafromclient: " + dataFromClient.toString());

                if (dataFromClient == null || !dataFromClient.containsKey(fileName)) {
                    //System.out.println("tento subor este nemam: " + fileName);
                    offset = 0;
                } else {
                    offset = dataFromClient.get(fileName);
                    //System.out.println("tento subor uz mam s offsetom: " + offset + " a jeho dlzka je " + file.length());
                    if (offset == file.length()) {
                        file = fileToSend.take();
                        //System.out.println("PRESKAKUJEM: " + fileName + " cez vlakno: " + Thread.currentThread().getName());
                        continue;
                    }
                }

                //System.out.println("nacitavam subor: " + file.getPath());

                long fileSize = file.length();
                oos.writeUTF(fileName);
                oos.writeLong(fileSize);
                oos.flush();

                byte[] buffer = new byte[Constants.BUFFER_SIZE];
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                raf.seek(offset);

                while (offset < fileSize) {
                    //System.out.println("offset: " + offset);
                    if (fileSize - offset < buffer.length) {
                        buffer = new byte[(int) (fileSize - offset)];
                    }
                    offset += raf.read(buffer);
                    //System.out.println("zvacsujem offset: " + offset);
                    oos.write(buffer);

                }
                System.out.println("posielam subor: " + fileName + " cez vlakno: " + Thread.currentThread().getName());
                oos.flush();
                raf.close();
                file = fileToSend.take();
            }

            //oos.writeUTF("poison.pill");
            //socket.close();

            // tu bolo toto moje pod tym
            // chcem ukoncit vlakna a zavriet starter aspon

            /*System.out.println("poslal som vsetky subory ktore som mal vo vlakne: " + Thread.currentThread().getName());

            oos.writeUTF("poison.pill");
            System.out.println("posielam poison.pill cez vlakno " + Thread.currentThread().getName());
            oos.flush();

            // tu
            oos.close();
            socket.close();*/

        } catch (IOException | InterruptedException e) {
            System.out.println("Spojenie s clientom sa nahle ukoncilo alebo sa neposlal subor");
        } finally {
            try {
                // tu
                System.out.println("poslal som vsetky subory ktore som mal vo vlakne: " + Thread.currentThread().getName());

                oos.writeUTF("poison.pill");
                System.out.println("posielam poison.pill cez vlakno " + Thread.currentThread().getName());
                oos.flush();

                // tu
                oos.close();
                socket.close();

            } catch (IOException e) {
                System.out.println("Spojenie s clientom sa nahle ukoncilo");
            }
        }
    }
}
