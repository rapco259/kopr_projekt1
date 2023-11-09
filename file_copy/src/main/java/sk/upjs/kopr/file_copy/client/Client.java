package sk.upjs.kopr.file_copy.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import sk.upjs.kopr.file_copy.Constants;

public class Client extends Service<Boolean>{

	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private int numberOfTpcConnections;
	
	public Client() {
		
	}
	
	public void setNumberOfTcpConnetions(int numberOfTpcConnections) {
		this.numberOfTpcConnections = numberOfTpcConnections;
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

				oos.writeUTF("START");
				oos.writeInt(numberOfTpcConnections);
				oos.flush();
				
				// to netreba ci ?

				connectToServer();

				return null;

			}

		};
	}

	public void createConnection() {

		try {
			Socket clientSocket = new Socket("localhost", Constants.SERVER_PORT);
			oos = new ObjectOutputStream(clientSocket.getOutputStream());
			ois = new ObjectInputStream(clientSocket.getInputStream());
			System.out.println("som napojeny");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void connectToServer() {
		
		for (int i = 0; i < numberOfTpcConnections; i++) {
			
			try {
				Socket socket = new Socket(Constants.SERVER_HOST, Constants.SERVER_PORT);
				System.out.println("pripajam na server");
				// FileReceiveTask task = new FileReceiveTask(socket, )
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}
		
		
	}

}
