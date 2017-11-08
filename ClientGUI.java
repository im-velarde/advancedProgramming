import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


/*
 * The Client with its GUI
 */
public class ClientGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	// will first hold "Username:", later on "Enter message"
	private JLabel label;
	// to hold the Username and later on the messages
	private JTextField tf;
	// to hold the server address an the port number
	private JTextField tfServer, tfPort;
	// to Logout and get the list of the users
	private JButton login, logout, whoIsIn;
	// for the chat room
	private JTextArea ta;
	//for the friends
	private JPanel chatList;
	// if it is for connection
	private boolean connected;
	// the Client object
	private Client client;
	// the default port number
	private int defaultPort;
	private String defaultHost;
	private ServerGUI sg;

	ArrayList<JButton> chatListButtons = new ArrayList();

	// Constructor connection receiving a socket number
	ClientGUI(String host, int port) {

		super("Chat Client");
		defaultPort = port;
		defaultHost = host;
		
		Server serverEX = new Server(port,sg);
		ArrayList<String> areturnList = new ArrayList();
		areturnList = serverEX.returnList();

		System.out.println(areturnList.size());
		// The NorthPanel with:
		JPanel northPanel = new JPanel(new GridLayout(3,1));
		// the server name anmd the port number
		JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
		// the two JTextField with default value for server address and port number
		tfServer = new JTextField(host);
		tfPort = new JTextField("" + port);
		tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

		serverAndPort.add(new JLabel("Server Address:  "));
		serverAndPort.add(tfServer);
		serverAndPort.add(new JLabel("Port Number:  "));
		serverAndPort.add(tfPort);
		serverAndPort.add(new JLabel(""));
		// adds the Server an port field to the GUI
		northPanel.add(serverAndPort);

		// the Label and the TextField
		label = new JLabel("Enter your username below", SwingConstants.CENTER);
		northPanel.add(label);
		tf = new JTextField("Anonymous");
		tf.setBackground(Color.WHITE);
		northPanel.add(tf);
		add(northPanel, BorderLayout.NORTH);

		// The CenterPanel which is the chat room
		ta = new JTextArea("Welcome to the Chat room\n", 80, 80);
		JPanel centerPanel = new JPanel(new GridLayout(1,1));
		centerPanel.add(new JScrollPane(ta));
		ta.setEditable(false);
		add(centerPanel, BorderLayout.CENTER);

		// the 3 buttons
		login = new JButton("Login");
		login.addActionListener(this);
		logout = new JButton("Logout");
		logout.addActionListener(this);
		logout.setEnabled(false);		// you have to login before being able to logout
		whoIsIn = new JButton("Who is in");
		whoIsIn.addActionListener(this);
		whoIsIn.setEnabled(false);		// you have to login before being able to Who is in

		//where the friends will be listen
		chatList = new JPanel(new GridLayout(0,5));
		for(String usersname: areturnList)
		{
			System.out.println(usersname);
			JButton a_button = new JButton(usersname);
			chatList.add(a_button);
		}

		JPanel southPanel = new JPanel();
		southPanel.add(login);
		southPanel.add(logout);
		southPanel.add(whoIsIn);

		JPanel bottPanel = new JPanel(new GridLayout(2,1));
		bottPanel.add(chatList);
		bottPanel.add(southPanel);
		add(bottPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
		tf.requestFocus();

	}

	// called by the Client to append text in the TextArea 
	void append(String str) {
		ta.append(str);
		ta.setCaretPosition(ta.getText().length() - 1);
	}
	// called by the GUI is the connection failed
	// we reset our buttons, label, textfield
	void connectionFailed() {
		login.setEnabled(true);
		logout.setEnabled(false);
		whoIsIn.setEnabled(false);
		label.setText("Enter your username below");
		tf.setText("Anonymous");
		// reset port number and host name as a construction time
		tfPort.setText("" + defaultPort);
		tfServer.setText(defaultHost);
		// let the user change them
		tfServer.setEditable(false);
		tfPort.setEditable(false);
		// don't react to a <CR> after the username
		tf.removeActionListener(this);
		connected = false;
	}
		
	/*
	* Button or JTextField clicked
	*/
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		// if it is the Logout button
		if(o == logout) {
			client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
			return;
		}
		// if it the who is in button
		if(o == whoIsIn) {
			client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));				
			return;
		}

		// ok it is coming from the JTextField
		if(connected) {
			// just have to send the message
			client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tf.getText()));				
			tf.setText("");
			return;
		}
		

		if(o == login) {
			// ok it is a connection request
			String username = tf.getText().trim();
			// empty username ignore it
			if(username.length() == 0)
				return;
			// empty serverAddress ignore it
			String server = tfServer.getText().trim();
			if(server.length() == 0)
				return;
			// empty or invalid port numer, ignore it
			String portNumber = tfPort.getText().trim();
			if(portNumber.length() == 0)
				return;
			int port = 0;
			try {
				port = Integer.parseInt(portNumber);
			}
			catch(Exception en) {
				return;   // nothing I can do if port number is not valid
			}

			// try creating a new Client with GUI
			client = new Client(server, port, username, this);
			// test if we can start the Client
			if(!client.start()) 
				return;
			tf.setText("");
			label.setText("Enter your message below");
			connected = true;

			//add to the list, make buttons available
			JButton friendBotton = new JButton(username);
			chatListButtons.add(friendBotton);

			
			// disable login button
			login.setEnabled(false);
			// enable the 2 buttons
			logout.setEnabled(true);
			whoIsIn.setEnabled(true);
			// disable the Server and Port JTextField
			tfServer.setEditable(false);
			tfPort.setEditable(false);
			// Action listener for when the user enter a message
			tf.addActionListener(this);
		}

	}

	// to start the whole thing the server
	public static void main(String[] args) {
		new ClientGUI("localhost", 1500);
	}

}
