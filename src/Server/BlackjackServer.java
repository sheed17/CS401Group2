package src.Server;
import java.net.*;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.*;

public class BlackjackServer
{
	public static void main (String[] args) throws IOException
	{
		try (var listener = new ServerSocket(52904))
		{
			InetAddress localhost = InetAddress.getLocalHost(); 
			System.out.println("The server is now listening on " + localhost.getHostAddress().trim());
			var pool = Executors.newFixedThreadPool(10);
			while (true)
			{
				pool.execute(new clientHandler(listener.accept()));
			}
		}
	}
	
	private static class clientHandler implements Runnable
	{
		private Socket socket;
		private boolean loggedIn = false;
		private boolean connected;
		private String status;
		
		// Test for password checking
		private String username = "ThisIsMyUsername51";
		private String password = "password67!";
		
		public clientHandler(Socket socket)
		{
			this.socket = socket;
		}
		
		
		public void run() 
		{
			try
			{
				connected = true;
				
				var inputStream = socket.getInputStream();
				var objectIn = new ObjectInputStream(inputStream);
				// Normal and object input streams
						
				var outputStream = socket.getOutputStream();
				var objectOut = new ObjectOutputStream(outputStream);
				// Normal and object output streams

				System.out.println("Connection from:  " + socket);
				// Print out that someone has connected and the socket info

				while (connected)
				{
					BlackjackMessage message = (BlackjackMessage) objectIn.readObject();
					if (message.getType() == MessageEnum.LOGIN)
					{
						loggedIn = true;
						status = "Success";
						
						BlackjackMessage login = new BlackjackMessage(MessageEnum.LOGIN, status, "You are now logged in");
						objectOut.writeObject(login);
						// This is how you write and send a message object to client application
					}
					if (message.getType() == MessageEnum.UPDATEBALANCE && loggedIn)
					{
						updateBalance(150);
					}
					else if (message.getType() == MessageEnum.VIEWPROFILE && loggedIn)
					{
						// viewProfile();
						// Debating on whether to keep this or not. Showing the user their profile
						// sounds like more of a GUI thing that simply extracts the information from 
						// the data files and then displays it.
					}
					else if (message.getType() == MessageEnum.UPDATEPASSWORD && loggedIn)
					{
						updatePassword(message.getText());
					}
					else if (message.getType() == MessageEnum.UPDATEUSERNAME && loggedIn)
					{
						updateUsername(message.getText());
					}
					else if (message.getType() == MessageEnum.LOGOUT && loggedIn)
					{
						connected = false;
						objectOut.writeObject(new BlackjackMessage(MessageEnum.LOGOUT, "success", "You are now logged out"));
					}
					
				
				}
			}
			catch (ClassNotFoundException | IOException e)
			{
				// not sure what to put here.
			}
		}
		
		public void updateBalance(int amount)
		{
			
		}
		
		public void updateUsername(String username)
		{
			
		}
		
		public void updatePassword(String password)
		{
			
		}
		
	}
}
