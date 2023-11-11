package sk.upjs.kopr.file_copy.client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import sk.upjs.kopr.file_copy.Constants;

public class Client extends Service<Boolean>{

	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	public final int numberOfTpcConnections;
	private ConcurrentHashMap<String, Long> dataFromClient;
	private ExecutorService executor;
	private Socket clientSocket;
	
	public Client(int numberOfTpcConnections) {
		savedData();
		this.executor = Executors.newFixedThreadPool(numberOfTpcConnections);
		this.numberOfTpcConnections = numberOfTpcConnections;
	}

	public void savedData() {
		dataFromClient = new ConcurrentHashMap<>();
	}

	protected Task<Boolean> createTask() {
		return new Task<Boolean>() {

			@Override
			protected Boolean call() throws Exception {
				System.out.println("volam metodu call");
				createConnection();

				// if mam uz nejake data, ak mam tak pokracujem v stahovani, ak nemam tak
				// zacinam stahovanie
				// teraz spravim iba pre start

				//oos.writeUTF("START");

				FileSearcherClient fileSearcherClient = new FileSearcherClient(new File(Constants.TO_DIR), dataFromClient);

				fileSearcherClient.run();

				System.out.println("dataFromClient: " + dataFromClient);

				oos.writeObject(dataFromClient);
				oos.writeInt(numberOfTpcConnections);
				oos.flush();

				connectToServer();

				return true;
			}

		};
	}

	public void createConnection() {

		try {
			clientSocket = new Socket("localhost", Constants.SERVER_PORT);
			oos = new ObjectOutputStream(clientSocket.getOutputStream());
			ois = new ObjectInputStream(clientSocket.getInputStream());
			System.out.println("som napojeny");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void connectToServer() throws IOException {
		
		for (int i = 0; i < numberOfTpcConnections; i++) {
			
			try {
				Socket socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
				System.out.println("pripajam na server");
				FileReceiveTask task = new FileReceiveTask(Constants.TO_DIR, socket, dataFromClient);
				executor.execute(task);

			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}
		executor.shutdown();
		clientSocket.close();
	}
}