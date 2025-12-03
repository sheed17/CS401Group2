package src.Client;

import java.io.*;
import java.net.Socket;

import src.Server.BlackjackMessage;
import src.Server.MessageEnum;

public class BlackjackClient 
{

    private final String host;
    private final int port;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public BlackjackClient(String host, int port) throws IOException 
    {
        this.host = host;
        this.port = port;
        connect();
    }

    private void connect() throws IOException 
    {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    private synchronized BlackjackMessage sendRequest(BlackjackMessage msg) 
    {
        try 
        {
            out.writeObject(msg);
            out.flush();
            return (BlackjackMessage) in.readObject();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            return null;
        }
    }


    public String logIn(String username, String password) 
    {
        BlackjackMessage req = new BlackjackMessage(
                MessageEnum.LOGIN,
                username,
                password
        );
        BlackjackMessage resp = sendRequest(req);
        if (resp == null) return "Failure";
        // GUI checks "Failure" / otherwise success
        return resp.getStatus();
    }

    public String createNewUser(String username, String password) 
    {
        BlackjackMessage req = new BlackjackMessage(
                MessageEnum.NEWUSER,
                username,
                password
        );
        BlackjackMessage resp = sendRequest(req);
        if (resp == null) return "Failure";
        return resp.getStatus();
    }


    public String viewProfile() 
    {
        BlackjackMessage req = new BlackjackMessage(
                MessageEnum.VIEWPROFILE,
                "",
                ""
        );
        BlackjackMessage resp = sendRequest(req);
        if (resp == null) return "";
        return resp.getText();
    }


    public String joinTable() 
    {
        BlackjackMessage req = new BlackjackMessage(
                MessageEnum.JOIN_TABLE,
                "",
                ""
        );
        BlackjackMessage resp = sendRequest(req);
        if (resp == null) return "Connection error.";
        // Text contains game state string
        return resp.getText();
    }

    public String placeBet(int amount) 
    {
        BlackjackMessage req = new BlackjackMessage(
                MessageEnum.PLACE_BET,
                "",
                Integer.toString(amount)
        );
        BlackjackMessage resp = sendRequest(req);
        if (resp == null) return "Connection error.";
        return resp.getText();
    }

    public String startRound() 
    {
        BlackjackMessage req = new BlackjackMessage(
                MessageEnum.START_ROUND,
                "",
                ""
        );
        BlackjackMessage resp = sendRequest(req);
        if (resp == null) return "Connection error.";
        return resp.getText();
    }

    public String hit() 
    {
        BlackjackMessage req = new BlackjackMessage(
                MessageEnum.HIT,
                "",
                ""
        );
        BlackjackMessage resp = sendRequest(req);
        if (resp == null) 
        	return "Connection error.";
        return resp.getText();
    }

    public String stand() 
    {
        BlackjackMessage req = new BlackjackMessage(
                MessageEnum.STAND,
                "",
                ""
        );
        BlackjackMessage resp = sendRequest(req);
        if (resp == null) 
        	return "Connection error.";
        return resp.getText();
    }

    public String doubleDown() 
    {
        BlackjackMessage req = new BlackjackMessage(
                MessageEnum.DOUBLE,
                "",
                ""
        );
        BlackjackMessage resp = sendRequest(req);
        if (resp == null) 
        	return "Connection error.";
        return resp.getText();
    }

    public String split() 
    {
        BlackjackMessage req = new BlackjackMessage(
                MessageEnum.SPLIT,
                "",
                ""
        );
        BlackjackMessage resp = sendRequest(req);
        if (resp == null) 
        	return "Connection error.";
        return resp.getText();
    }


    public String logOut() 
    {
        BlackjackMessage req = new BlackjackMessage(
                MessageEnum.LOGOUT,
                "",
                ""
        );
        BlackjackMessage resp = sendRequest(req);
        if (resp == null) 
        	return "Disconnected.";
        return resp.getText();
    }


    public void close() 
    {
        try 
        {
            if (socket != null) socket.close();
        } 
        catch (IOException ignored) {}
    }
    
    public String getGameState() 
    {
        BlackjackMessage req =
            new BlackjackMessage(MessageEnum.GAME_STATE, "", "");
        BlackjackMessage resp = sendRequest(req);
        if (resp == null) 
        	return null;
        return resp.getText();
    }

    
    public static void main(String[] args) 
    {
        try 
        {
            BlackjackClient client = new BlackjackClient("134.154.54.0", 52904);
            src.GUI.GameGUI gui = new src.GUI.GameGUI(client);
            gui.logInGUI();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            System.out.println("Could not connect to server.");
        }
    }
}

