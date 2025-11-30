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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BlackjackServerTester {

	private Socket socket; 
	private ObjectInputStream objectIn;
	private ObjectOutputStream objectOut;
	
	@BeforeEach
	void setUp() throws Exception 
	{
		this.socket = new Socket("localhost", 52904);
		this.objectOut = new ObjectOutputStream(socket.getOutputStream());
		objectOut.flush();
		this.objectIn = new ObjectInputStream(socket.getInputStream());
	}
	
	@AfterEach
	void close() throws Exception
	{
		socket.close();
	}

	@Test
	@Order(1)
	void logInMessageReturnsSuccessWithRealCredentials() throws IOException, ClassNotFoundException 
	{
		BlackjackMessage message = new BlackjackMessage(MessageEnum.LOGIN, "username51", "password67");
		objectOut.writeObject(message);
		BlackjackMessage serverResponse = (BlackjackMessage) objectIn.readObject();
		assertTrue(serverResponse.getStatus().equals("Success"));
	}
	
	@Test
	@Order(2)
	void logInMessageReturnsFailureWithFakeCredentials() throws IOException, ClassNotFoundException
	{
		BlackjackMessage message = new BlackjackMessage(MessageEnum.LOGIN, "fakeusername", "fakepassword");
		objectOut.writeObject(message);
		BlackjackMessage serverResponse = (BlackjackMessage) objectIn.readObject();
		assertTrue(serverResponse.getStatus().equals("Failure"));
	}
	
	@Test
	@Order(3)
	void viewProfileReturnsProperAccountDetailsAfterLogInAndCreateUser() throws IOException, ClassNotFoundException
	{
		BlackjackMessage message = new BlackjackMessage(MessageEnum.LOGIN, "username51", "password67");
		objectOut.writeObject(message);
		BlackjackMessage serverResponse = (BlackjackMessage) objectIn.readObject();
		objectOut.writeObject(new BlackjackMessage(MessageEnum.VIEWPROFILE, "null", "null"));
		serverResponse = (BlackjackMessage) objectIn.readObject();
		String expectedResult = "username51,password67,150";
		//Update this to the proper balance listed in userData.txt
		assertTrue(serverResponse.getText().equals(expectedResult));
	}
	
	@Test
	@Order(998)
	void updateBalanceShowsProperBalanceAfterMethodCall() throws IOException, ClassNotFoundException
	{
		BlackjackMessage message = new BlackjackMessage(MessageEnum.LOGIN, "username51", "password67");
		objectOut.writeObject(message);
		BlackjackMessage serverResponse = (BlackjackMessage) objectIn.readObject();
		
		objectOut.writeObject(new BlackjackMessage(MessageEnum.UPDATEBALANCE, "add", "150"));
		serverResponse = (BlackjackMessage) objectIn.readObject();
		
		objectOut.writeObject(new BlackjackMessage(MessageEnum.VIEWPROFILE, "null", "null"));
		serverResponse = (BlackjackMessage) objectIn.readObject();
		
		String expectedResult = "username51,password67,300";
		assertTrue(serverResponse.getText().equals(expectedResult));
		
		objectOut.writeObject(new BlackjackMessage(MessageEnum.UPDATEBALANCE, "subtract", "150"));
		serverResponse = (BlackjackMessage) objectIn.readObject();
		
		objectOut.writeObject(new BlackjackMessage(MessageEnum.VIEWPROFILE, "null", "null"));
		serverResponse = (BlackjackMessage) objectIn.readObject();
		expectedResult = "username51,password67,150";
		assertTrue(serverResponse.getText().equals(expectedResult));
	}
	
	@Test
	@Order(999)
	void updateUsernameChangesUsernameUponNextLogIn() throws IOException, ClassNotFoundException
	{
		BlackjackMessage message = new BlackjackMessage(MessageEnum.LOGIN, "username51", "password67");
		objectOut.writeObject(message);
		BlackjackMessage serverResponse = (BlackjackMessage) objectIn.readObject();
		
		objectOut.writeObject(new BlackjackMessage(MessageEnum.UPDATEPASSWORD, "pending", "newpassword"));
		serverResponse = (BlackjackMessage) objectIn.readObject();
		
		objectOut.writeObject(new BlackjackMessage(MessageEnum.UPDATEUSERNAME, "pending", "newusername"));
		serverResponse = (BlackjackMessage) objectIn.readObject();

		objectOut.writeObject(new BlackjackMessage(MessageEnum.VIEWPROFILE, "null", "null"));
		serverResponse = (BlackjackMessage) objectIn.readObject();
		assertTrue(serverResponse.getText().equals("newusername,newpassword,150"));	
		
		objectOut.writeObject(new BlackjackMessage(MessageEnum.UPDATEPASSWORD, "pending", "password67"));
		serverResponse = (BlackjackMessage) objectIn.readObject();
		
		objectOut.writeObject(new BlackjackMessage(MessageEnum.UPDATEUSERNAME, "pending", "username51"));
		serverResponse = (BlackjackMessage) objectIn.readObject();
	}

}
