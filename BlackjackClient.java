import java.net.*;
import java.util.*;
import java.io.*;

public class BlackjackClient
{
	public static void main(String[] args) throws IOException
	{
		String IPAddress;
		// This is temporary
		int portNumber;
		boolean loggedIn = false;
		
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter the IP Address of the Server: ");
		IPAddress = scan.nextLine().trim();
		
		System.out.println("Enter the port number of the server: ");
		portNumber = scan.nextInt();
		scan.nextLine();
		
		try (Socket socket = new Socket(IPAddress, portNumber))
		{
			var input = new Scanner(socket.getInputStream());
			var output = new PrintWriter(socket.getOutputStream(), true);
			System.out.println(input.nextLine());

			// Test for logging into the server;
			while (loggedIn == false)
			{
				System.out.println("Input username: ");
				String username = scan.nextLine().trim();
				output.println(username);
		
				System.out.println("Enter your password: ");
				String password = scan.nextLine().trim();
				output.println(password);
			
				String serverMessage = input.nextLine().trim();
				if (serverMessage.contains("You are now logged in"))
				{
					System.out.println(serverMessage);
					loggedIn = true;
				}
				else 
					System.out.println(serverMessage);
				
			}
		}
	}
}