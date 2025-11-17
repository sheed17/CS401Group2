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
				//var objectIn = new ObjectInputStream(inputStream);
				// Normal and object input streams
				
				var scan = new Scanner(inputStream);
				// test code to read strings from the client applications
				
				var outputStream = socket.getOutputStream();
				//var objectOut = new ObjectOutputStream(outputStream);
				// Normal and object output streams
				
				var stringSender = new PrintWriter(outputStream, true);
				// test code to send strings to the client application
				
				System.out.println("Connection from:  " + socket);
				// Print out that someone has connected and the socket info

				while (connected)
				{
					stringSender.println("You are now connected to the server. Please log in.");
					while (loggedIn == false)
					{
						String usernameInput = scan.nextLine().trim();
						
						String passwordInput = scan.nextLine().trim();
						if (username.equals(usernameInput) && password.equals(passwordInput))
						{
							stringSender.println("You are now logged in");
							loggedIn=true;
							connected = false;
						}
						else
							stringSender.println("Incorrect username and/or password.");
					}	
				}
			}
			catch (IOException e)
			{
				// not sure what to put here.
			}
		}
	}
}
