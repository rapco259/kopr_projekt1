package sk.upjs.kopr.file_copy.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import sk.upjs.kopr.file_copy.Constants;
import sk.upjs.kopr.file_copy.FileSearcher;

public class Server {

	private BlockingQueue<File> filesToSend;
	private int TPC_CONNECTIONS;

	// PRECO STATIC ?????????????????????????
	// vytvorenie lebo main je static

	public Server() {
		startConnection();
	}

	public static void main(String[] args) throws IOException {
		new Server();
	}

	public void startConnection() {

		System.out.println("Server sa spustil");

		try (ServerSocket serverSocket = new ServerSocket(Constants.SERVER_PORT)) {
			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("niekto sa napojil");
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				ConcurrentHashMap<String, Long> dataFromClient = null;

				// read dir with searcher

				// action od clienta, zatial mi to netreba
				String startOrContinue = ois.readUTF();

				TPC_CONNECTIONS = ois.readInt();

				System.out.println(TPC_CONNECTIONS);
				System.out.println(startOrContinue);

				//dataFromClient = (ConcurrentHashMap<String, Long>) ois.readObject();

				// prijmem mapu, bud je prazdna, alebo tam nieco je
				// ak je pradzna tak este som nic nestiahol zo servera
				// ak nie je prazdna, tak uz som nieco stiahol zo servera

				filesToSend = new LinkedBlockingQueue<>();
				getAllFilesToSend(new File(Constants.FROM_DIR));
				
				System.out.println(filesToSend.size());

				ExecutorService executor = Executors.newCachedThreadPool();

				for (int i = 0; i < TPC_CONNECTIONS; i++) {
					Socket connectionSocket = serverSocket.accept();
					// I need to create a fileSendTask for each connection socket and then in fileSendTask i need to
					// take a file from queue and send it to client and then close the connection socket
					FileSendTask fileSendTask = new FileSendTask(executor, filesToSend, connectionSocket, dataFromClient);
					executor.execute(fileSendTask);
				}

				socket.close();

			}

		} catch (Exception e) {

		}

	}

	public void getAllFilesToSend(File rootDir) {
		File[] files = rootDir.listFiles();
		System.out.println("mam files nejake " + files.length);

		filesToSend = new LinkedBlockingQueue<>();

		FileSearcher fileSearcher = new FileSearcher(rootDir, filesToSend);
		
		fileSearcher.run();
		

	}

}
