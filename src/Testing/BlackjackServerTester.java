package src.Testing;

import java.net.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import src.Server.*;
import src.Client.BlackjackClient;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BlackjackServerTester {

	private BlackjackClient client;
	private Socket socket; 
	private ObjectInputStream objectIn;
	private ObjectOutputStream objectOut;
	
	@BeforeEach
	void setUp() throws Exception 
	{
		client = new BlackjackClient("134.154.23.179", 52904);
	}
	
	@Test
	@Order(1)
	void logInMessageReturnsSuccessWithRealCredentials() throws IOException, ClassNotFoundException 
	{
		String serverResponse = client.logIn("luis", "password123");
		assertTrue(serverResponse.equals("Success"));
	}
	
	@Test
	@Order(2)
	void logInMessageReturnsFailureWithFakeCredentials() throws IOException, ClassNotFoundException
	{
		String serverResponse = client.logIn("luis", "fakepassword");
		assertTrue(serverResponse.equals("Failure"));
	}
	
	@Test
	@Order(3)
	void viewProfileReturnsProperAccountDetailsAfterLogInAndCreateUser() throws IOException, ClassNotFoundException
	{
		String serverResponse = client.logIn("luis", "password123");
		serverResponse = client.viewProfile();
		assertTrue(serverResponse.equals("luis,password123,470"));
		
		BlackjackClient newClient = new BlackjackClient("134.154.23.179", 52904);
		serverResponse = newClient.createNewUser("newuser", "newpassword");
		serverResponse = newClient.viewProfile();
		assertTrue(serverResponse.equals("newuser,newpassword,50"));
		// either delete the newuser line in userData or use new credentials everytime this is ran
	}
	
	@Test
	@Order(4)
	void logOutReturnsProperLogOutMessageAfterSuccessfulLogIn()
	{
		String serverResponse = client.logIn("luis", "password123");
		serverResponse = client.viewProfile();
		assertTrue(serverResponse.equals("luis,password123,470"));
		
		serverResponse = client.logOut();
		assertTrue(serverResponse.equals("You are now logged out"));
	}
}
