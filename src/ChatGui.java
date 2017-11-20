import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Font;
import java.awt.List;

import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.html.HTMLDocument.Iterator;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JComboBox;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JDesktopPane;
import javax.swing.JTabbedPane;
import javax.swing.JSplitPane;
import javax.swing.JMenuBar;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.JToggleButton;

public class ChatGui {

	private JFrame frame;
	private JTextField serverTextField;
	private JTextField usernameTextField;
	private JTextField inputTextField;
	private JTextArea textArea;
	private JButton connectButton;
	private JButton sendButton;
	private JList usernameJList;
	private int selectedUserListIndex = 0;
	private String selectedUserName = "";
	DefaultListModel listModel = new DefaultListModel();
	
	Socket clientSocket = null;
	private String serverIP = "127.0.0.1";
	private int port = 5000;
	private boolean isConnected = false;
	private String userName = null;
	private String password = null;
	private String inputedText = null;
	private String recievedText = null;
	private String clientList = null;
	ArrayList<Integer> blockUserList = new ArrayList<Integer>();
	ArrayList<String> blockUserNameList = new ArrayList<String>();
	
	readByLine userModel = null;
	
	DataOutputStream dos;
	DataInputStream dis;
	private final JPasswordField passwordField = new JPasswordField();
	private JSeparator separator;
	private JSeparator separator_1;
	private JSeparator separator_2;
	private JLabel receiverLabel;
	private JLabel welcomeLabel;
	private  CheckboxListCellRenderer cellRender = new CheckboxListCellRenderer();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatGui window = new ChatGui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		try {
			new readByLine();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void startClient(String ip, int port, String username) {

	    try {
	        clientSocket = new Socket(ip,port);
	        
	        dos = new DataOutputStream(clientSocket.getOutputStream());
	        dis = new DataInputStream(clientSocket.getInputStream());
	        
	        dos.write(ServerConstants.SETUP_MESSAGE);
	        dos.writeUTF(username);
	        
	        System.out.println("Simple chat client.");
	        textArea.append("Chatting Started! \n");
	        isConnected = true;
	        
	        // setup another thread to handle network I-O from ClientHandler 
	        Thread serverHandler = new Thread()
	        {
	            public void run()
	            {
	                while(true)
	                {
	                    int flag;
	                    try {
	                        flag = dis.read();
	                        System.out.println(flag);
	                        switch(flag){
	                            case ServerConstants.SETUP_ACK: 
	                                	break;
	                            case ServerConstants.CHAT_ACK:
                                		break;
	                            case ServerConstants.UPDATECLIENTLIST: 
                                		String str = dis.readUTF();
                                		updateUserJList(str);
                                		break;
	                            case ServerConstants.CHAT_MESSAGE: 
	                            	
	                            		String inputStr = dis.readUTF() + "\n";
	                            		String [] user = inputStr.split(":");
	                            		String sender = user[0];
	                                	// Check if blocked
	                            		System.out.println("Block List:" + blockUserNameList + "Sender:" + sender);
	                                	textArea.append(blockUserNameList.contains(sender) ? "" : inputStr);
	                                	

	                                	break;
	                            case ServerConstants.BROADCAST_MESSAGE:
	                                	String recievedMessage = dis.readUTF();
	                                	String showMessage = userName + " broadcast : "+ recievedMessage;
	                            		textArea.append(showMessage + "\n");
	                            		break;
	                            case ServerConstants.DISCONNECT_ACK:
	            						textArea.append("Logout Successfully, see you next time!\n\n");
	                            		System.out.println("disconnect confirmed");
	                                	clientSocket.close();
	                                	return;
	                            default:
	                                	System.out.println("flag: " + flag + " incorrect please reconnect");
	                                	clientSocket.close();
	                                	
	                            		connectButton.setText("Connect");
	                            		serverTextField.setEnabled(true);
	                            		usernameTextField.setEnabled(true);
	                            		passwordField.setEnabled(true);
	                            		textArea.setEnabled(false);
	                            		inputTextField.setEditable(false);
	                            		sendButton.setEnabled(false);
	                                	return;
	                        }
	
	                    }
	                    catch (IOException e)
	                    {
	                    		System.err.println(e);
	                    		break;
	                    }
	                }
	            }
	        };
	        serverHandler.start();
	        
	    } catch (UnknownHostException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    
	}
	

	
	/**
	 * Create the application.
	 */
	public ChatGui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setFont(new Font("Tahoma", Font.PLAIN, 11));
		frame.getContentPane().setBackground(new Color(238, 238, 238));
		frame.setBounds(100, 100, 800, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblHost = new JLabel("Server:");
		lblHost.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblHost.setBounds(6, 12, 36, 16);
		frame.getContentPane().add(lblHost);
		
		JLabel lblNewLabel = new JLabel("Username:");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblNewLabel.setBounds(6, 40, 56, 16);
		frame.getContentPane().add(lblNewLabel);
		
		serverTextField = new JTextField();
		serverTextField.setText(serverIP);
		serverTextField.setBounds(78, 6, 399, 26);
		frame.getContentPane().add(serverTextField);
		serverTextField.setColumns(10);
		
		usernameTextField = new JTextField();
		usernameTextField.setBounds(78, 34, 176, 26);
		frame.getContentPane().add(usernameTextField);
		usernameTextField.setColumns(10);
		
		// Connect
		connectButton = new JButton("Connect");
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Connect
				if(connectButton.getText() == "Connect") {
					handelConnect();				
				} else {
					handelDisconnect ();
				}
			}
		});
		connectButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
		connectButton.setBounds(477, 6, 117, 54);
		frame.getContentPane().add(connectButton);
		
		inputTextField = new JTextField();
		inputTextField.setEditable(false);
		inputTextField.setBounds(2, 417, 471, 38);
		frame.getContentPane().add(inputTextField);
		inputTextField.setColumns(10);
		
		sendButton = new JButton("Send");
		sendButton.setEnabled(false);

		sendButton.addActionListener(new ActionListener() {
			// Send Message
			public void actionPerformed(ActionEvent e) {
				String receiver = receiverLabel.getText();
				inputedText = inputTextField.getText();
				if(receiver.equals("All")) {
					broadcastMsg(inputedText);
				} else {
					sendMsg(receiver, inputedText);
				}
		        inputTextField.setText(null);
			}
		});
		sendButton.setBounds(477, 417, 117, 38);
		frame.getContentPane().add(sendButton);
		
		usernameJList = new JList();
		usernameJList.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		
		usernameJList.addListSelectionListener(new ListSelectionListener() {
// UserList Selected Action
			public void valueChanged(ListSelectionEvent e) {
				String toUser = (String) usernameJList.getSelectedValue();
				if (toUser == null) {
					return;
				}
				
				toUser = (String) usernameJList.getSelectedValue();
				selectedUserListIndex = usernameJList.getSelectedIndex();

				System.out.println("To User: " + toUser);
				String [] user = toUser.split(":");
				selectedUserName = user[0];
				receiverLabel.setText(selectedUserName);
				
			}
		});
		usernameJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		usernameJList.setBounds(606, 6, 188, 410);
		frame.getContentPane().add(usernameJList);
		
		JLabel lblNewLabel_1 = new JLabel("Password:");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblNewLabel_1.setBounds(273, 39, 50, 16);
		frame.getContentPane().add(lblNewLabel_1);
		passwordField.setBounds(332, 34, 145, 26);
		frame.getContentPane().add(passwordField);
		
		textArea = new JTextArea();
		textArea.setForeground(Color.BLACK);
		textArea.setFont(new Font("Tahoma", Font.PLAIN, 12));
		textArea.setBounds(6, 101, 588, 315);
		frame.getContentPane().add(textArea);
		textArea.setEditable(false);
		
		separator = new JSeparator();
		separator.setBounds(6, 59, 588, 12);
		frame.getContentPane().add(separator);
		
		separator_1 = new JSeparator();
		separator_1.setBounds(593, 12, 1, 440);
		frame.getContentPane().add(separator_1);
		
		separator_2 = new JSeparator();
		separator_2.setOrientation(SwingConstants.VERTICAL);
		separator_2.setBounds(593, 6, 12, 446);
		frame.getContentPane().add(separator_2);
		
		JLabel lblNewLabel_2 = new JLabel("Send To: ");
		lblNewLabel_2.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblNewLabel_2.setBounds(277, 73, 46, 16);
		frame.getContentPane().add(lblNewLabel_2);
		
		receiverLabel = new JLabel("");
		receiverLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
		receiverLabel.setBounds(332, 73, 262, 16);
		frame.getContentPane().add(receiverLabel);
		
		
// Block Button Action
		JButton blockButton = new JButton("Block/Unblock");
		blockButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				System.out.println("current selected" + selectedUserListIndex);
				if (blockUserList.contains(selectedUserListIndex)) {
					blockUserList.remove((Object) selectedUserListIndex);
					blockUserNameList.remove((Object) selectedUserName);
				} else {
					blockUserList.add(usernameJList.getSelectedIndex());
					blockUserNameList.add(selectedUserName);
				}
				cellRender.blockUser(blockUserList);
				
				usernameJList.setSelectedValue("Blocked", true);
			}
		});
		
		blockButton.setBounds(606, 417, 188, 38);
		frame.getContentPane().add(blockButton);
		
		welcomeLabel = new JLabel("Please Login...");
		welcomeLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		welcomeLabel.setBounds(6, 68, 248, 26);
		frame.getContentPane().add(welcomeLabel);
		

	}

	private void handelConnect () {
		// Port Setting
		userName = usernameTextField.getText();
		password = passwordField.getText();
		if (readByLine.login(userName, password)) {
			serverIP = serverTextField.getText();
			startClient(serverIP, port, userName);
			connectButton.setText("Disconnect");
			isConnected = true;
			
			serverTextField.setEnabled(false);
			usernameTextField.setEnabled(false);
			passwordField.setEnabled(false);
			textArea.setEnabled(true);
			inputTextField.setEditable(true);
			sendButton.setEnabled(true);
			welcomeLabel.setText(userName + ":");
		}
	}
	
	private void handelDisconnect () {
		try {
			System.out.println(ServerConstants.DISCONNECT);
			dos.write(ServerConstants.DISCONNECT);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		emptyUserJList();
		userName = null;
		password = null;
		
		connectButton.setText("Connect");
		serverTextField.setEnabled(true);
		usernameTextField.setEnabled(true);
		passwordField.setEnabled(true);
		textArea.setEnabled(false);
		inputTextField.setEditable(false);
		sendButton.setEnabled(false);
	}
	
	private void broadcastMsg(String msg) {
        try {
        		dos.write(ServerConstants.BROADCAST_MESSAGE);
			dos.writeUTF(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendMsg(String receiver, String msg) {
        try {
        		dos.write(ServerConstants.CHAT_MESSAGE);
        		dos.writeUTF(receiver);
			dos.writeUTF(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void updateUserJList(String usersString) {
		String[] clients = usersString.split(",");
		listModel.clear();
		listModel.addElement("All");
		for(String c: clients) {
			String [] user = c.split("/");
			System.out.println(c);
			String contact = user[0] + ":" + user[1];
			listModel.addElement(contact);
		}
		usernameJList.setModel(listModel);
		usernameJList.setSelectedIndex(0);
		usernameJList.setCellRenderer(cellRender);
	}
	
	private void emptyUserJList() {
		listModel.clear();
		usernameJList.setModel(listModel);
	}
}
