import java.net.*;
import java.io.*;
import java.util.*;

public class Client  {
	private String notif = " *###* ";
	private ObjectInputStream sInput;	
	private ObjectOutputStream sOutput;		
	private Socket socket;				
	
	private String server, username;
	private int port;					

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	Client(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
	}

	public boolean start() {
		try {
			socket = new Socket(server, port);
		} 
		catch(Exception ec) {
			display("Error connecting to server:" + ec);
			return false;
		}
		
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);
	
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}
 
		new ListenFromServer().start();
		try
		{
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		return true;
	}

	private void display(String msg) {

		System.out.println(msg);
		
	}
	
	void sendMsg(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}
	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {}
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {}
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {}
			
	}

	public static void main(String[] args) {
		int portNumber = 1245;
		String serverAddress = "localhost";
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Enter the username: ");
		String userName = sc.nextLine();
		Client client = new Client(serverAddress, portNumber, userName);
		if(!client.start())
			return;
		
		System.out.println("\nWelcome to the ChatBox.");
		System.out.println("Some Useful Commands:");
		System.out.println("1. Simply type the message to send broadcast to all active users");
		System.out.println("2. Type '@username<space>yourmessage' to send message to desired user");
		System.out.println("3. Type 'JOINED' to see list of active clients");
		System.out.println("4. Type 'LOGOUT' to get logged out from the server");
		System.out.println("5. Type 'TIME' to know the current time");
		System.out.println("6. Type 'NOOFUSERS' to know the number of connected users");
		System.out.println("7. Type 'GOOGLE' to open the Google chrome");
		System.out.println("8. Type 'YOUTUBE' to open the Youtube");
		
		while(true) {
			System.out.print("> ");
			String msg = sc.nextLine();
			if(msg.equalsIgnoreCase("LOGOUT")) {
				client.sendMsg(new ChatMessage(ChatMessage.LOGOUT, ""));
				break;
			}
			else if(msg.equalsIgnoreCase("JOINED")) {
				client.sendMsg(new ChatMessage(ChatMessage.JOINED, ""));				
			}
			else if(msg.equalsIgnoreCase("TIME")) {
				client.sendMsg(new ChatMessage(ChatMessage.TIME, ""));				
			}
			else if(msg.equalsIgnoreCase("NOOFUSERS")) {
				client.sendMsg(new ChatMessage(ChatMessage.NOOFUSERS, ""));				
			}
			else if(msg.equalsIgnoreCase("GOOGLE")) {
				client.sendMsg(new ChatMessage(ChatMessage.GOOGLE, ""));				
			}
			else if(msg.equalsIgnoreCase("YOUTUBE")) {
				client.sendMsg(new ChatMessage(ChatMessage.YOUTUBE, ""));				
			}
			else {
				client.sendMsg(new ChatMessage(ChatMessage.MESSAGE, msg));
			}
		}
		sc.close();
		client.disconnect();	
	}

	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject();
					System.out.println(msg);
					System.out.print("> ");
				}
				catch(IOException e) {
					display(notif + "Server has closed the connection: " + e + notif);
					break;
				}
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}

