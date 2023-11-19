package sk.upjs.kopr.file_copy.client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import sk.upjs.kopr.file_copy.Constants;

public class FileReceiveTask implements Runnable {
    private String TO_DIR;
    private Socket socket;
    private ConcurrentHashMap<String, Long> dataFromClient;
    private long offset;
    private ObjectInputStream ois;
    private CountDownLatch latch;


    public FileReceiveTask(String TO_DIR, Socket socket, ConcurrentHashMap<String, Long> dataFromClient, CountDownLatch latch) throws IOException {
        this.TO_DIR = TO_DIR;
        this.socket = socket;
        this.dataFromClient = dataFromClient;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            while (true) {
                // treba ukoncit while cyklus

                String fileName = ois.readUTF();
                //System.out.println("fileName: " + fileName);

                if (fileName.equals(Constants.POISON_PILL.getName())) {
                    //System.out.println("CLIENTOVE VLAKNO DOSTALO POISON PILL, KONCIM CYKLUS");
                    break;
                }

                File file = new File(TO_DIR + "\\" + fileName);
                //System.out.println("file: " + file.getPath());

                //System.out.println("dataFromClient: " + dataFromClient.toString());
                //System.out.println("fileName: " + file.getAbsoluteFile());
                //System.out.println("fileName: " + file.getAbsoluteFile().toString());
                System.out.println(file.getPath().substring(Constants.TO_DIR.lastIndexOf('\\') + 1));

                if (!dataFromClient.containsKey(file.getPath().substring(Constants.TO_DIR.lastIndexOf('\\') + 1))) {
                    offset = 0;
                    //System.out.println("tento subor este nemam: " + file.getName());
                } else {
                    offset = dataFromClient.get(file.getPath().substring(Constants.TO_DIR.lastIndexOf('\\') + 1));
                    //System.out.println("tento subor uz mam s offsetom: " + offset + " a jeho dlzka je " + file.length());
                }

                File parent = file.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }

                RandomAccessFile raf = new RandomAccessFile(file, "rw");

                long fileSize = ois.readLong();

                raf.setLength(fileSize);
                //System.out.println("fileSize: " + fileSize);

                byte[] receivedData = new byte[Constants.BUFFER_SIZE];
                raf.seek(offset); // offset
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
                System.out.println("file: " + file.getPath() + " bol stiahnuty" + " cez vlakno: " + Thread.currentThread().getName());
                // ulozit vsetko co uz mam

                if (offset < fileSize) {
                    dataFromClient.put(fileName, offset);
                    break;
                } else {
                    dataFromClient.put(fileName, fileSize);

                }
            }
            //System.out.println("koniec cyklu, mam vsetko stiahnute, zatvaram socket");
            //ois.close();
            //socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("zatvaram socket V FILERECEIVETASK");
                ois.close();
                socket.close();
                latch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}