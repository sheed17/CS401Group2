package src.Game;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class Player {

    private int userId;
    private static int count = 0;
    private String username;
    private String password;
    private int balance;
    private int totalCardValue;
    private String lastResult = "";
    // put 12 because mathematically there is no way that with more than 11 you can
 	// still stand and with the 12 you bust
    private Card[][] hands = new Card[4][12];
    private int[] handBets = new int[4];
    private int numHands = 1;
    private int currentBet;
    private int activeHandNumber = 0;
    private int[] handSizes = new int[4];
    private int[] totalHands = new int[4];
    private Table currentTable;

    // constructor
    public Player(String username, String password){
        this.userId = count++;
        this.username = username;
        this.password = password;
        this.balance = 0;
        this.totalCardValue = 0;
        this.currentBet = 0;
    }

    // reset hand before the new round
    public void resetForNewRound() {
        for (int i = 0; i < hands.length; i++) {
            handSizes[i] = 0;
            totalHands[i] = 0;
            handBets[i] = 0; 
            for (int j = 0; j < hands[i].length; j++) {
                hands[i][j] = null;
            }
        }
        numHands = 1;
        activeHandNumber = 0;
        totalCardValue = 0;
    }

    // log in method that reads from the txt file
    public boolean logIn(String u, String p) {
        File file = new File("UserData.txt");

        if (!file.exists()) {
            return false;
        }

        boolean found = false;

        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] userData = line.split(",");
                if (userData.length != 3) break;

                String fileUser = userData[0].trim();
                String filePass = userData[1].trim();
                int fB = Integer.parseInt(userData[2].trim());

                if (u.equals(fileUser) && p.equals(filePass)) {
                    this.username = fileUser;
                    this.password = filePass;
                    this.balance = fB;
                    found = true;
                    break;
                }
            }
            scanner.close();
        } catch (Exception e) {
            return false;
        }

        if (!found) {
            this.username = null;
            this.password = null;
            this.balance = 0;
            return false;
        }

        return true;
    }

    // logout the user and rewrites the value of the balance updated
    public void logout() {
        File file = new File("UserData.txt");
        if (!file.exists()) return;

        try {
            Scanner scanner = new Scanner(file);
            StringBuilder sb = new StringBuilder();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    sb.append("\n");
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length != 3) {
                    sb.append(line).append("\n");
                    continue;
                }
                if (parts[0].trim().equals(this.username) && parts[1].trim().equals(this.password)) {
                    sb.append(parts[0]).append(",").append(parts[1]).append(",").append(this.balance).append("\n");
                } else {
                    sb.append(line).append("\n");
                }
            }
            scanner.close();

            FileWriter fileUpdated = new FileWriter(file, false);
            fileUpdated.write(sb.toString());
            fileUpdated.close();

        } catch (Exception e) {}

        this.balance = 0;
        this.totalCardValue = 0;
        this.currentBet = 0;

        for (int i = 0; i < hands.length; i++) {
            handSizes[i] = 0;
            handBets[i] = 0;
            totalHands[i] = 0;
            for (int j = 0; j < hands[i].length; j++) {
                hands[i][j] = null;
            }
        }
        numHands = 1;
        activeHandNumber = 0;
    }

    // joining an available table
    public void joinTable(Table table) {
        if (table == null) {
            return;
        }

        if (currentTable != null && currentTable != table) {
            currentTable.removePlayer(this);
        }

        boolean joined = table.addPlayer(this);
        if (joined) {
            currentTable = table;
            for (int i = 0; i < hands.length; i++) {
                handSizes[i] = 0;
                handBets[i] = 0;
                totalHands[i] = 0;
                for (int j = 0; j < hands[i].length; j++) {
                    hands[i][j] = null;
                }
            }
            numHands = 1;
            activeHandNumber = 0;
            currentBet = 0;
            totalCardValue = 0;
        }
    }

    // leaving the table and putting the stats to 0
    public void leaveTable() {
        if (currentTable != null) {
            currentTable.removePlayer(this);
            currentTable = null;
        }

        for (int i = 0; i < hands.length; i++) {
            handSizes[i] = 0;
            handBets[i] = 0;
            totalHands[i] = 0;
            for (int j = 0; j < hands[i].length; j++) {
                hands[i][j] = null;
            }
        }
        numHands = 1;
        activeHandNumber = 0;
        this.currentBet = 0;
        this.totalCardValue = 0;
    }

    // bet and check if has enough money to bet 
    public int bet(int money) {
        if (money <= 0) {
            return 0;
        }
        if (money > balance) {
            return 0;
        }
        balance = balance - money;
        currentBet = currentBet + money;
        handBets[activeHandNumber] = handBets[activeHandNumber] + money;
        return money;
    }

    // hit a new card on its hand
    public void hit(Shoe shoe) {
        int handSize = handSizes[activeHandNumber];

        if (handSize >= hands[activeHandNumber].length) {
            return;
        }

        Card newCard = shoe.dealCard();
        hands[activeHandNumber][handSize] = newCard;
        handSizes[activeHandNumber] = handSize + 1;

        int total = 0;
        int aces = 0;

        for (int i = 0; i < handSizes[activeHandNumber]; i++) {
            Card c = hands[activeHandNumber][i];
            if (c == null) continue;

            total += c.getCardValue();
            if (c.getRank() == Rank.Ace) {
                aces = aces + 1;
            }
        }

        while (aces > 0 && total + 10 <= 21) {
            total = total + 10;
            aces = aces - 1;
        }

        totalHands[activeHandNumber] = total;
        this.totalCardValue = total;
    }

    // returns the hand 
    public int stand() {
        return totalHands[activeHandNumber];
    }

    // checks if it can bet the same amount and doubles it or stands 
    public void doubleDown(int betAmount, Shoe shoe) {

        if (handSizes[activeHandNumber] != 2) {
            return; 
        }

        int baseHandBet = handBets[activeHandNumber];
        if (baseHandBet <= 0) {
            return;
        }

        int extraBet = baseHandBet;

        if (extraBet > balance) {
            return; 
        }
        int placed = bet(extraBet);
        if (placed <= 0) {
            return; 
        }

        hit(shoe);
        stand();
    }

    // split the hand if it has 2 hands and check the bets
    public void split(Shoe shoe) {
        if (numHands >= 4) {
            return;
        }
        if (handSizes[activeHandNumber] != 2) {
            return;
        }

        Card first = hands[activeHandNumber][0];
        Card second = hands[activeHandNumber][1];

        if (first == null || second == null) {
            return;
        }

        if (first.getRank() != second.getRank()) {
            return;
        }

        int betForHand = handBets[activeHandNumber];
        if (betForHand <= 0 || balance < betForHand) {
            return;
        }

        balance = balance - betForHand;
        currentBet = currentBet - betForHand;

        int newIndex = numHands;
        numHands =  numHands + 1;

        hands[newIndex][0] = second;
        handSizes[newIndex] = 1;

        hands[activeHandNumber][1] = null;
        handSizes[activeHandNumber] = 1;
        handBets[newIndex] = betForHand;

        if (handSizes[activeHandNumber] < hands[activeHandNumber].length) {
            Card c1 = shoe.dealCard();
            hands[activeHandNumber][handSizes[activeHandNumber]] = c1;
            handSizes[activeHandNumber]++;
        }

        int totalHand1 = 0;
        int acesHand1 = 0;
        for (int i = 0; i < handSizes[activeHandNumber]; i++) {
            Card c = hands[activeHandNumber][i];
            if (c == null) continue;
            totalHand1 = totalHand1 + c.getCardValue();
            if (c.getRank() == Rank.Ace) {
                acesHand1 = acesHand1 + 1;
            }
        }
        while (acesHand1 > 0 && totalHand1 + 10 <= 21) {
            totalHand1 = totalHand1 + 10;
            acesHand1 = acesHand1 - 1;
        }
        totalHands[activeHandNumber] = totalHand1;
        this.totalCardValue = totalHand1;

        if (handSizes[newIndex] < hands[newIndex].length) {
            Card c2 = shoe.dealCard();
            hands[newIndex][handSizes[newIndex]] = c2;
            handSizes[newIndex]++;
        }
        int totalHand2 = 0;
        int acesHand2 = 0;
        for (int i = 0; i < handSizes[newIndex]; i++) {
            Card c = hands[newIndex][i];
            if (c == null) continue;
            totalHand2 = totalHand2 + c.getCardValue();
            if (c.getRank() == Rank.Ace) {
                acesHand2 = acesHand2 + 1;
            }
        }
        while (acesHand2 > 0 && totalHand2 + 10 <= 21) {
            totalHand2 = totalHand2 + 10;
            acesHand2 = acesHand2 - 1;
        }
        totalHands[newIndex] = totalHand2;
    }
    
    // check that if the sum is 21 and if it has 2 cards
    public boolean isBlackjack() {
        int cards = handSizes[activeHandNumber];
        return (cards == 2 && totalCardValue == 21);
    }

    // getters and setters and the tostring
    
    public Card[][] getHands() {
        return hands;
    }

    public int[] getHandSizes() {
        return handSizes;
    }

    public int[] getHandTotals() {
        return totalHands;
    }
    
    public int[] getHandBets() {
        return handBets;
    }

    public int getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return this.username;
    }

    public int getBalance() {
        return this.balance;
    }

    public int getTotalCardValue() {
        return this.totalCardValue;
    }

    public int getCurrentBet() {
        return this.currentBet;
    }

    public int getNumHands() {
        return this.numHands;
    }

    public void setNumHands(int numHands) {
        this.numHands = numHands;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public void setTotalCardValue(int tcv) {
        this.totalCardValue = tcv;
    }

    public void setCurrentBet(int currentBet) {
        this.currentBet = currentBet;
    }
    
    public boolean isBusted() {
        return this.totalCardValue > 21;
    }
    
    public String getLastOutcome() {
        return this.lastResult == null ? "" : this.lastResult;
    }

    public void setLastOutcome(String outcome) {
        this.lastResult = outcome;
    }
    
    public void setLastResult(String r) { 
    	this.lastResult = r; 
    }
    
    public String getLastResult() { 
    	return lastResult; 
    }

    @Override
    public String toString() {
        return "Player ID: " + userId + ", Username: " + username + ", Balance: " + balance;
    }
}
