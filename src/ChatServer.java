import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {

	private static ServerSocket server;

	public static void main(String[] args) {

		try {
			server = new ServerSocket(5000);
			System.out.println("Server setup: waiting for client connections");

			ArrayList<ClientHandler> clientHandlers = new ArrayList<ClientHandler>();

			while (true) {
				// keep processing and accepting client connections forever
				Socket serverSocket = server.accept();

				ClientHandler newClientHandler = new ClientHandler(serverSocket, clientHandlers);
				newClientHandler.start();

				clientHandlers.add(newClientHandler);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
