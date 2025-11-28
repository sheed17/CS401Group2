import java.net.*;
import java.util.*;

import javax.swing.JOptionPane;

import java.io.*;

public class BlackjackClient
{
	private static boolean loggedIn = false;
	private Socket socket;
	private ObjectInputStream objectIn;
	private ObjectOutputStream objectOut;

	public BlackjackClient() throws IOException
	{
		this.socket = new Socket("localhost", 52904);
		this.objectOut = new ObjectOutputStream(socket.getOutputStream());
		objectOut.flush();
		this.objectIn = new ObjectInputStream(socket.getInputStream());
	}
	
	
	public static void main(String[] args) throws IOException
	{
		BlackjackClient client = new BlackjackClient();
		// Creates a new client object to be passed into the GUI (not needed for now)
		// Might not be needed in general, commenting out for now as it messes with the method references
		
		GameGUI newGUI = new GameGUI(client);
		newGUI.logInGUI();
	}
	
	public synchronized void send(BlackjackMessage message)
	{
		try
		{
			objectOut.writeObject(message);
			objectOut.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public synchronized BlackjackMessage receive()
	{
		try {
            return (BlackjackMessage) objectIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
	}
	
	
	public String logIn(String username, String password)
	{
		send(new BlackjackMessage(MessageEnum.LOGIN,username,password));	
		BlackjackMessage message = receive();
		if (message.getStatus().equals("Success"))
			loggedIn=true;
		return message.getStatus();
	}
	// GUI version of logIn() method
		
	public String createNewUser(String username, String password)
	{
		send(new BlackjackMessage(MessageEnum.NEWUSER,username,password));
		BlackjackMessage message = receive();
		if (message.getStatus().equals("Success"))
			loggedIn = true;
		return message.getStatus();
	}
	// GUI version of CreateNewUser() method	
		
	public void updateBalance()
	{
		send(new BlackjackMessage(MessageEnum.UPDATEBALANCE, "add", "150"));
		BlackjackMessage serverResponse = receive();
		System.out.println(serverResponse.getText());
		// Basic example of sending a message from the client to the server
	}
	
}