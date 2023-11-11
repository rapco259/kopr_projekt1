package sk.upjs.kopr.file_copy.server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import sk.upjs.kopr.file_copy.Constants;

public class Server {

	private BlockingQueue<File> filesToSend;
	private int TPC_CONNECTIONS;
	private ConcurrentHashMap<String, Long> dataFromClient;
	private ConcurrentHashMap<String, Long> serverFiles;

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
			ExecutorService executor = Executors.newCachedThreadPool();
			while (true) {
				Socket socket = serverSocket.accept();
				System.out.println("niekto sa napojil");
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

				dataFromClient = null;

				// read dir with searcher

				// action od clienta, zatial mi to netreba, treba mi mapu od clienta ze co uz ma
				//String startOrContinue = ois.readUTF();
				dataFromClient = (ConcurrentHashMap<String, Long>) ois.readObject();

				TPC_CONNECTIONS = ois.readInt();

				// dataFromClient is null ? dokoncit a pozriet ukoncovanie vlakien

				// print out dataFromClient with files and their sizes
				//dataFromClient.forEach((k, v) -> System.out.println("key: " + k + " value: " + v));


				// prijmem mapu, bud je prazdna, alebo tam nieco je
				// ak je pradzna tak este som nic nestiahol zo servera
				// ak nie je prazdna, tak uz som nieco stiahol zo servera


				getAllFilesToSend(new File(Constants.FROM_DIR));

				System.out.println("filesToSend: " + filesToSend);
				
				System.out.println("velkost filesToSend: " + filesToSend.size());

				for (int i = 0; i < TPC_CONNECTIONS; i++) {
					Socket connectionSocket = serverSocket.accept();
					System.out.println("prijal som spojenie");
					FileSendTask fileSendTask = new FileSendTask(filesToSend, connectionSocket, dataFromClient, TPC_CONNECTIONS);
					executor.execute(fileSendTask);
				}
			}

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	public void getAllFilesToSend(File rootDir) {
		File[] files = rootDir.listFiles();
		// System.out.println("mam files nejake " + files.length);
		filesToSend = new LinkedBlockingQueue<>();

		assert files != null;
		if (files.length == 0) {
			filesToSend.add(rootDir);
		}

		serverFiles = new ConcurrentHashMap<>();

		FileSearcherServer fileSearcherServer = new FileSearcherServer(rootDir, filesToSend, TPC_CONNECTIONS);
		
		fileSearcherServer.run();

	}
}
