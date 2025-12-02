package src.Server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import src.Game.GameHandler;

public class BlackjackServer 
{

    // Shared user data + game handler
    public static ArrayList<String> userData = new ArrayList<>();
    public static GameHandler gameHandler = new GameHandler();

    // used to track connected clients 
    public static final List<ClientHandler> connectedClients =
            Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws IOException 
    {
        loadUserData();
        InetAddress bindAddress = InetAddress.getByName("192.168.1.130");
        // MAKE SURE TO CHANGE TO THE IP OF WHOEVER IS HOSTING THE SERVER FOR PRESENTATION

        try (ServerSocket listener = new ServerSocket(52904, 50, bindAddress)) {
            System.out.println("Server listening on " + bindAddress.getHostAddress());

            ExecutorService pool = Executors.newFixedThreadPool(10);

            while (true) {
                Socket socket = listener.accept();
                ClientHandler handler = new ClientHandler(socket);
                connectedClients.add(handler);
                pool.execute(handler);
            }
        }
    }


    public static void loadUserData() 
    {
        try {
            File userTxt = new File("UserData.txt");
            if (userTxt.exists()) {
                Scanner inFile = new Scanner(userTxt);
                while (inFile.hasNextLine()) {
                    String data = inFile.nextLine().trim();
                    if (!data.isEmpty()) {
                        userData.add(data);
                    }
                }
                inFile.close();
            }
        } catch (FileNotFoundException e) {
            System.out.println("File was not found.");
        }
    }
	// This method will read from the userData.text file and save every line within it into an arrayList
	// This arrayList will be used for searching through usernames, passwords, etc,

    public static void saveUserData() 
    {
        StringBuilder saveFile = new StringBuilder();
        for (String s : userData) {
            saveFile.append(s).append("\n");
        }
        try {
            FileWriter outFile = new FileWriter("UserData.txt");
            outFile.write(saveFile.toString());
            outFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	// Method used for simply grabbing every string from the userData array list and then saving it to a string which is then
	// written back into UserData.txt for safe keeping
}


