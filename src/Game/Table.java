package src.Game;

import java.util.ArrayList;
import java.util.List;

public class Table {

    private static int count = 0;
    private int tableId;
    private List<Player> players;
    private Dealer dealer;
    private Shoe shoe;

    private final int minBetAmount = 5;
    private final int maxBetAmount = 50000;
    private final int maxPlayers = 7;

    private boolean inGame = false;

    public Table() {
        this.tableId = ++count;
        this.players = new ArrayList<>();
        this.dealer = new Dealer();
        this.shoe = new Shoe(dealer);
    }


    public boolean addPlayer(Player p) {
        if (players.size() >= maxPlayers) {
        	return false;
        }

        players.add(p);
        return true;
    }

    public void removePlayer(Player p) {
        players.remove(p);
    }

    public void handlePlayerDisconnect(Player p) {
        removePlayer(p);
    }

    public boolean placeBet(Player p, int amount) {

        if (!players.contains(p)) {
            return false;
        }

        if (inGame) {
            return false;
        }

        if (amount < minBetAmount || amount > maxBetAmount) {
            return false;
        }

        if (p.getBalance() < amount) {
            return false;
        }

        // Prevent stacking multiple main bets
        if (p.getCurrentBet() >= minBetAmount) {
            return false;
        }

        return p.bet(amount) > 0;
    }



    public void startRound() {
        if (players.isEmpty()) {
        	return;
        }

        for (Player p : players) {
            if (p.getCurrentBet() < minBetAmount) {
                continue;
            }
        }

        inGame = true;

        dealer.resetHand();

        for (Player p : players) {
            p.setNumHands(1);
            p.setTotalCardValue(0);
        }

        for (Player p : players) {
            if (p.getCurrentBet() >= minBetAmount) {
                p.hit(shoe);
                p.hit(shoe);
            }
        }

        dealer.hit(shoe);
        dealer.hit(shoe);
    }

    public void endRound() {
        if (!inGame) {
        	return;
        }

        dealer.playDealerHand(shoe);
        int dealerValue = dealer.getTotalCardValue();

        for (Player p : players) {
            int playerValue = p.getTotalCardValue();

            if (p.getCurrentBet() < minBetAmount) {
            	continue; 
            }
            if (playerValue > 21) {
            	continue;               
            }

            if (dealerValue > 21 || playerValue > dealerValue) {
                int winnings = p.getCurrentBet() * 2;
                p.setBalance(p.getBalance() + winnings);
            }
        }

        resetTable();
    }

    public void resetTable() {
        inGame = false;
        dealer.resetHand();
    }

    public Player getPlayer(int userId) {
        for (Player p : players) {
            if (p.getUserId() == userId) {
            	return p;
            }
        }
        return null;
    }

    public int getCurrentPlayers() {
        return players.size();
    }

    public int getTableId() {
        return tableId;
    }

    public List<Player> getPlayers() {
        return players;
    }
}




