import java.awt.geom.FlatteningPathIterator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread {
	Socket serverSocket;
	ArrayList<ClientHandler> clients;
	String clientName = null;
	String receiverName = null;
	String clientIP = null;
	String recievedMessage = null;
	boolean isConnected = false;

	DataInputStream dis = null;
	DataOutputStream dos = null;

	public ClientHandler(Socket serverSocket, ArrayList<ClientHandler> clients) {
		this.serverSocket = serverSocket;
		this.clients = clients;

		clientIP = serverSocket.getRemoteSocketAddress().toString().split(":")[0];
		System.out.println("New client connected" + serverSocket.getRemoteSocketAddress().toString());
		isConnected = true;

		OutputStream os = null;
		InputStream in = null;
		try {
			in = serverSocket.getInputStream();
			os = serverSocket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// the raw input stream only deals with bytes so lets wrap it in a data input
		// stream

		dis = new DataInputStream(in);
		dos = new DataOutputStream(os);

	}

	public void run() {
		System.out.println("Client connected");

		while (isConnected) {
			int flag;
			try {

				flag = dis.read();
				System.out.println(flag);
				// TODO block and wait for the client to send an appropriate message

				switch (flag) {
				case ServerConstants.SETUP_MESSAGE:
					clientName = dis.readUTF();
					System.out.println("Client Name: " + clientName);
					dos.write(ServerConstants.SETUP_ACK);
					bloadcastClientList();
					break;
				case ServerConstants.CHAT_MESSAGE:
					receiverName = dis.readUTF();
					recievedMessage = dis.readUTF();
					for (ClientHandler c : clients) {
						if(c.clientName.equals(receiverName)){
							c.dos.write(ServerConstants.CHAT_MESSAGE);
							c.dos.writeUTF(clientName + ": " + recievedMessage);
						}
					}
					System.out.println(clientName + " : " + recievedMessage);
					dos.write(ServerConstants.CHAT_ACK);
					break;
				case ServerConstants.BROADCAST_MESSAGE:
					recievedMessage = dis.readUTF();
					System.out.println(clientName + " broadcast : " + recievedMessage);
					for (ClientHandler c : clients) {
						c.dos.write(ServerConstants.CHAT_MESSAGE);
						c.dos.writeUTF("Broadcast from " + clientName + ": " + recievedMessage);
					}
					break;
				case ServerConstants.DISCONNECT:
					System.out.println("Recieved disconnect order " + flag + " disconnected!");
					dos.write(ServerConstants.DISCONNECT_ACK);
					isConnected = false;
					serverSocket.close();
					clients.remove(this);
					bloadcastClientList();
					System.out.println(getClientList());
					return;
				case ServerConstants.QUITWITHOUTDISCONNECTING:
					isConnected = false;
					dos.write(ServerConstants.DISCONNECT_ACK);
					System.out.println("QUITWITHOUTDISCONNECTING!");
					serverSocket.close();
					clients.remove(this);
					bloadcastClientList();
					System.out.println(getClientList());
					return;
				// default is sent msg to private
				default:
					isConnected = false;
					dos.write(ServerConstants.DISCONNECT_ACK);
					System.out.println("flag incorrect disconnected!");
					serverSocket.close();
					clients.remove(this);
					bloadcastClientList();
					System.out.println(getClientList());
					return;

				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getClientList() {
		String clientsString = "";
		for (ClientHandler c : clients) {
			clientsString += c.clientName + clientIP + ",";
		}
		return clientsString;
	}

	public void bloadcastClientList() {
		String clientsString = getClientList();
		try {
			for (ClientHandler c : clients) {
				c.dos.write(ServerConstants.UPDATECLIENTLIST);
				c.dos.writeUTF(clientsString);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

}
