import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.awt.Desktop;

public class Server {
	private static int Id;
	private ArrayList<ClientThread> al;
	private SimpleDateFormat sdf;
	private int port;
	private boolean noend;
	private String notif = " *###* ";
	
	public Server(int port) {
		this.port = port;
		sdf = new SimpleDateFormat("HH:mm:ss");
		al = new ArrayList<ClientThread>();
	}
	
	public void start() {
		noend = true; 
		try 
		{
			ServerSocket serverSocket = new ServerSocket(port);
			while(noend) 
			{
				display("Server waiting for Clients on port " + port + ".");
				Socket socket = serverSocket.accept();
				if(!noend)
					break;
				ClientThread t = new ClientThread(socket);
				al.add(t);
				t.start();
			}
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}
	
	protected void stop() {
		noend = false;
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
		}
	}
	
	private void display(String msg) {
		String time = sdf.format(new Date()) + " " + msg;
		System.out.println(time);
	}
	
	synchronized void remove(int id) {
		
		String disconnectedClient = "";
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			if(ct.id == id) {
				disconnectedClient = ct.getUsername();
				al.remove(i);
				break;
			}
		}
		broadcast(notif + disconnectedClient + " has left the chat room." + notif);
	}

	private synchronized boolean broadcast(String message) {
		String time = sdf.format(new Date());
		String[] w = message.split(" ",3);
		
		boolean isPrivate = false;
		if(w[1].charAt(0)=='@') 
			isPrivate=true;
		
		if(isPrivate==true)
		{
			String tocheck=w[1].substring(1, w[1].length());
			
			message=w[0]+w[2];
			String messageLf = time + " " + message + "\n";
			boolean found=false;
			for(int y=al.size(); --y>=0;)
			{
				ClientThread ct1=al.get(y);
				String check=ct1.getUsername();
				if(check.equals(tocheck))
				{
					if(!ct1.writeMsg(messageLf)) {
						al.remove(y);
						display("Disconnected Client " + ct1.username + " removed from list.");
					}
					found=true;
					break;
				}	
				
			}
			if(found!=true)
			{
				return false; 
			}
		}
		else
		{
			String messageLf = time + " " + message + "\n";
			System.out.print(messageLf);
			
			for(int i = al.size(); --i >= 0;) {
				ClientThread ct = al.get(i);
				if(!ct.writeMsg(messageLf)) {
					al.remove(i);
					display("Disconnected Client " + ct.username + " removed from list.");
				}
			}
		}
		return true;
	}

	class A{
	A() throws Exception{
	Desktop d= Desktop.getDesktop();
	d.browse(new URI("www.google.com"));
	}
	}

	class yt{
	yt() throws Exception{
	Desktop d= Desktop.getDesktop();
	d.browse(new URI("www.youtube.com"));
	}
	}
 
	public static void main(String[] args) {
		int portNumber = 1245;
		Server server = new Server(portNumber);
		server.start();
	}

	class ClientThread extends Thread {
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		int id;
		String username;
		ChatMessage cm;
		String date;

		ClientThread(Socket socket) {
			id = ++Id;
			this.socket = socket;
			System.out.println("Thread trying to create Object Input/Output Streams");
			try
			{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				username = (String) sInput.readObject();
				broadcast(notif + username + " has joined the chat room." + notif);
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}
		
		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		private boolean writeMsg(String msg) {
			if(!socket.isConnected()) {
				close();
				return false;
			}
			try {
				sOutput.writeObject(msg);
			}
			catch(IOException e) {
				display(notif + "Error sending message to " + username + notif);
				display(e.toString());
			}
			return true;
		}

		private void close() {
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		public void run() {
			boolean noend = true;
			while(noend) {
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				String message = cm.getMessage();

				switch(cm.getType()) {

				case ChatMessage.MESSAGE:
					boolean confirmation =  broadcast(username + ": " + message);
					if(confirmation==false){
						String msg = notif + "Sorry. No such user exists." + notif;
						writeMsg(msg);
					}
					break;
				case ChatMessage.LOGOUT:
					display(username + " disconnected with a LOGOUT message.");
					noend = false;
					break;
				case ChatMessage.JOINED:
					writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
					for(int i = 0; i < al.size(); ++i) {
						ClientThread ct = al.get(i);
						writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
					}
					break;
				case ChatMessage.TIME:
						writeMsg("The current time is :"+ sdf.format(new Date()));
						break;
				case ChatMessage.NOOFUSERS:
						writeMsg("No. of connected users :"+ al.size());
						break;
				case ChatMessage.GOOGLE:
						writeMsg("Opening Google...");
						try{
            				A obj=new A();
            				}
            				catch(Exception e){
            				}
						broadcast(notif + "Someone is trying to open the browser...." + notif);
						break;
				case ChatMessage.YOUTUBE:
						writeMsg("Opening Youtube...");
							try{
            				yt obj=new yt();
            				}
            				catch(Exception e){
            				}
						broadcast(notif + "Someone is trying to open Youtube...." + notif);
						break;


			}
			}
			remove(id);
			close();
		}
	}
}

