package src.Game;

import java.util.*;

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

        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUsername().equals(p.getUsername())) {
                players.remove(i);
                break;
            }
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
        }s
        if (p.getCurrentBet() >= minBetAmount) {
            return false;
        }
        return p.bet(amount) > 0;
    }

    public void startRound() {
    	dealer.resetHand();
        if (players.isEmpty()) return;

        inGame = true;
        dealer.resetHand();

        for (Player p : players) {
            if (p.getCurrentBet() < minBetAmount) {
                p.bet(minBetAmount);
            }
        }

        for (Player p : players) {
            p.resetForNewRound();
        }

        for (Player p : players) {
            p.hit(shoe);
            p.hit(shoe);
        }

        dealer.hit(shoe);
        dealer.hit(shoe);
    }


    public void endRound() {
        if (!inGame) return;

        dealer.playDealerHand(shoe);
        int dealerValue = dealer.getTotalCardValue();

        for (Player p : players) {
            int bet = p.getCurrentBet();
            int playerValue = p.getTotalCardValue();

            String result;

            if (bet < minBetAmount) {
                p.setCurrentBet(0);
                continue;
            }

            if (playerValue > 21) {
                result = "BUST — You lose.";
            } else if (dealerValue > 21) {
                p.setBalance(p.getBalance() + bet * 2);
                result = "Dealer busts — YOU WIN!";
            } else if (playerValue > dealerValue) {
                p.setBalance(p.getBalance() + bet * 2);
                result = "YOU WIN!";
            } else if (playerValue == dealerValue) {
                p.setBalance(p.getBalance() + bet);
                result = "Push — No one wins.";
            } else {
                result = "YOU LOSE.";
            }
            p.setLastResult(result);

            p.setCurrentBet(0);

            for (int i = 0; i < p.getHandBets().length; i++) {
                p.getHandBets()[i] = 0;
            }
        }

        inGame = false;
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

    public Shoe getShoe() {
        return shoe;
    }

    public Dealer getDealer() {
        return dealer;
    }

    public int getMinBetAmount() {
        return minBetAmount;
    }

    public boolean isInGame() {
        return inGame;
    }
}



