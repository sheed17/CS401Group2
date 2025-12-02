package src.Server;

import java.io.*;
import java.net.Socket;

import src.Game.GameHandler;
import src.Game.Player;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private boolean loggedIn = false;
    private boolean connected = true;
    private int userIndex;

    private Player currentPlayer; // Player associated with this client

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Connection from: " + socket);

            while (connected) 
            {
                BlackjackMessage message = (BlackjackMessage) in.readObject();
                handleMessage(message);
            }
        } catch (Exception e) 
        {
            System.out.println("Client disconnected: " + socket);
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
            BlackjackServer.connectedClients.remove(this);
        }
    }

    private void handleMessage(BlackjackMessage message) throws IOException {
        switch (message.getType()) 
        {
            case LOGIN -> handleLogin(message);
            case NEWUSER -> handleNewUser(message);

            case JOIN_TABLE -> handleJoinTable();
            case PLACE_BET -> handlePlaceBet(message);
            case START_ROUND -> handleStartRound();
            case HIT -> handleHit();
            case STAND -> handleStand();
            case DOUBLE -> handleDouble();
            case SPLIT -> handleSplit();

            case VIEWPROFILE -> handleViewProfile();
            case UPDATEBALANCE -> handleUpdateBalance(message);
            case UPDATEPASSWORD -> handleUpdatePassword(message);
            case UPDATEUSERNAME -> handleUpdateUsername(message);

            case LOGOUT -> handleLogout();
            case GAME_STATE -> {
            	handleGameStateRequest(message);
                break;
            }
        }
        // switch statement to handle every kind of message received by the client
    }


    private void handleLogin(BlackjackMessage message) throws IOException 
    {
        int index = searchUser(message.getStatus(), message.getText(), MessageEnum.LOGIN);
        if (index != -1) 
        {
            loggedIn = true;
            userIndex = index;
            out.writeObject(new BlackjackMessage(MessageEnum.LOGIN, "Success", "You are now logged in"));
        } 
        else 
        {
            out.writeObject(new BlackjackMessage(MessageEnum.LOGIN, "Failure", "Wrong username/password"));
        }
    }

    private void handleNewUser(BlackjackMessage message) throws IOException 
    {
        if (searchUser(message.getStatus(), message.getText(), MessageEnum.NEWUSER) != -1) 
        {
            addNewUser(message.getStatus(), message.getText());
            loggedIn = true;
            out.writeObject(new BlackjackMessage(
                    MessageEnum.NEWUSER,
                    "Success",
                    "Successfully registered and logged in."
            ));
        } else {
            out.writeObject(new BlackjackMessage(
                    MessageEnum.NEWUSER,
                    "Failure",
                    "The username is already in use"
            ));
        }
    }



    private void handleJoinTable() throws IOException 
    {
        if (!loggedIn) return;

        String[] data = BlackjackServer.userData.get(userIndex).split(",");
        Player p = new Player(data[0], data[1]);
        p.setBalance(Integer.parseInt(data[2]));

        this.currentPlayer = p;

        BlackjackServer.gameHandler.join(p);

        String state = BlackjackServer.gameHandler.buildState(p);
        out.writeObject(new BlackjackMessage(
                MessageEnum.GAME_STATE,
                "JOINED",
                state
        ));
    }

    private void handlePlaceBet(BlackjackMessage message) throws IOException 
    {
        if (!loggedIn || currentPlayer == null) return;

        int amount;
        try {
            amount = Integer.parseInt(message.getText());
        } catch (NumberFormatException e) 
        {
            out.writeObject(new BlackjackMessage(
                    MessageEnum.GAME_STATE,
                    "BET_FAILED",
                    "Invalid bet amount."
            ));
            return;
        }

        boolean ok = BlackjackServer.gameHandler.placeBet(currentPlayer, amount);
        if (!ok) 
        {
            out.writeObject(new BlackjackMessage(
                    MessageEnum.GAME_STATE,
                    "BET_FAILED",
                    "Bet failed. Make sure min bet is 5, balance is enough, and round hasn't started."
            ));
        } else {
            String state = BlackjackServer.gameHandler.buildState(currentPlayer);
            out.writeObject(new BlackjackMessage(
                    MessageEnum.GAME_STATE,
                    "BET_OK",
                    state
            ));
        }
    }

    private void handleStartRound() throws IOException 
    {
        if (!loggedIn || currentPlayer == null) return;
        BlackjackServer.gameHandler.startRound();
        sendState("ROUND_STARTED");
    }

    private void handleHit() throws IOException 
    {
        if (!loggedIn || currentPlayer == null) return;
        BlackjackServer.gameHandler.hit(currentPlayer);
        sendState("HIT");
    }

    private void handleStand() throws IOException 
    {
        if (!loggedIn || currentPlayer == null) return;
        BlackjackServer.gameHandler.stand(currentPlayer);
        sendState("STAND");
    }

    private void handleDouble() throws IOException 
    {
        if (!loggedIn || currentPlayer == null) return;
        BlackjackServer.gameHandler.doubleDown(currentPlayer);
        sendState("DOUBLE");
    }

    private void handleSplit() throws IOException 
    {
        if (!loggedIn || currentPlayer == null) return;
        BlackjackServer.gameHandler.split(currentPlayer);
        sendState("SPLIT");
    }

    private void sendState(String status) throws IOException 
    {
        String gamestate = BlackjackServer.gameHandler.buildState(currentPlayer);
        out.writeObject(new BlackjackMessage(
                MessageEnum.GAME_STATE,
                status,
                gamestate
        ));
    }


    private void handleViewProfile() throws IOException 
    {
        if (!loggedIn) return;
        out.writeObject(new BlackjackMessage(
                MessageEnum.VIEWPROFILE,
                "Success",
                BlackjackServer.userData.get(userIndex)
        ));
    }

    private void handleUpdateBalance(BlackjackMessage message) throws IOException 
    {
        if (!loggedIn) return;

        if (message.getStatus().equals("add"))
            updateBalance(Integer.parseInt(message.getText()), "add", userIndex);
        else
            updateBalance(Integer.parseInt(message.getText()), "subtract", userIndex);

        out.writeObject(new BlackjackMessage(
                MessageEnum.UPDATEBALANCE,
                "Success",
                "User Balance has been updated.\nBalance: " + getBalance()
        ));
    }

    private void handleUpdatePassword(BlackjackMessage message) throws IOException 
    {
        if (!loggedIn) return;
        updatePassword(message.getText(), userIndex);
        out.writeObject(new BlackjackMessage(
                MessageEnum.UPDATEPASSWORD,
                "Success",
                "Password has been updated."
        ));
    }

    private void handleUpdateUsername(BlackjackMessage message) throws IOException 
    {
        if (!loggedIn) return;
        updateUsername(message.getText(), userIndex);
        out.writeObject(new BlackjackMessage(
                MessageEnum.UPDATEUSERNAME,
                "Success",
                "Username has been updated."
        ));
    }


    private void handleLogout() throws IOException 
    {
        if (!loggedIn) return;
        loggedIn = false;
        connected = false;
        out.writeObject(new BlackjackMessage(
                MessageEnum.LOGOUT,
                "Success",
                "You are now logged out"
        ));
    }


    private void addNewUser(String username, String password) 
    {
        String newUser = username + "," + password + ",50";
        BlackjackServer.userData.add(newUser);
        userIndex = BlackjackServer.userData.size() - 1;
        BlackjackServer.saveUserData();
    }

    private int searchUser(String username, String password, MessageEnum purpose) 
    {
        if (purpose == MessageEnum.LOGIN) 
        {
            for (int index = 0; index < BlackjackServer.userData.size(); index++) 
            {
                String[] userInfo = BlackjackServer.userData.get(index).split(",");
                if (userInfo[0].equals(username) && userInfo[1].equals(password)) 
                {
                    userIndex = index;
                    return index;
                }
            }
            return -1;
        } 
        else if (purpose == MessageEnum.NEWUSER) 
        {
            for (int index = 0; index < BlackjackServer.userData.size(); index++) 
            {
                String[] userInfo = BlackjackServer.userData.get(index).split(",");
                if (userInfo[0].equals(username)) 
                {
                    return -1;
                }
            }
            return 0;
        }
        return -1;
    }

    private void updateBalance(int amount, String operation, int userIndex) 
    {
        String[] userInfo = BlackjackServer.userData.get(userIndex).split(",");
        int userBalance = Integer.parseInt(userInfo[2]);
        if (operation.equalsIgnoreCase("add"))
            userBalance += amount;
        else
            userBalance -= amount;

        userInfo[2] = Integer.toString(userBalance);
        String userInfoString = userInfo[0] + "," + userInfo[1] + "," + userInfo[2];
        BlackjackServer.userData.set(userIndex, userInfoString);
        BlackjackServer.saveUserData();
    }

    private void updateUsername(String username, int userIndex) 
    {
        String[] userInfo = BlackjackServer.userData.get(userIndex).split(",");
        userInfo[0] = username;
        String userInfoString = userInfo[0] + "," + userInfo[1] + "," + userInfo[2];
        BlackjackServer.userData.set(userIndex, userInfoString);
        BlackjackServer.saveUserData();
    }

    private void updatePassword(String password, int userIndex) 
    {
        String[] userInfo = BlackjackServer.userData.get(userIndex).split(",");
        userInfo[1] = password;
        String userInfoString = userInfo[0] + "," + userInfo[1] + "," + userInfo[2];
        BlackjackServer.userData.set(userIndex, userInfoString);
        BlackjackServer.saveUserData();
    }

    private String getBalance() 
    {
        String[] userInfo = BlackjackServer.userData.get(userIndex).split(",");
        return userInfo[2];
    }
    
    private void handleGameStateRequest(BlackjackMessage msg) throws IOException 
    {
        if (!loggedIn || currentPlayer == null) return;
        String state = BlackjackServer.gameHandler.buildState(currentPlayer);
        out.writeObject(new BlackjackMessage(
                MessageEnum.GAME_STATE,
                "OK",
                state
        ));
    }

}

