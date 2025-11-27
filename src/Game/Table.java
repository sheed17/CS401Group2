import java.util.Arrays;

public class Table {

    private int tableId;
    private Player[] players = new Player[7];
    private Dealer dealer;
    private Shoe shoe;

    private final int minBetAmount = 5;
    private final int maxBetAmount = 50000;
    private int currentPlayers = 0;
    private final int maxPlayers = 7;

    private boolean inGame = false;

    public Table(int tableId) {
        this.tableId = tableId;
        this.dealer = new Dealer();
        this.shoe = new Shoe(6);
    }

    public boolean addPlayer(Player p) {
        if (currentPlayers >= maxPlayers){
          return false;
        }

        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) {
                players[i] = p;
                currentPlayers++;
                return true;
            }
        }
        return false;
    }

    public void removePlayer(Player p) {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == p) {
                players[i] = null;
                currentPlayers--;
                break;
            }
        }
    }

    //handling player disconnect right now i just removed but later will handle logic wanted to get the skeleton of the table class out so far
    public void handlePlayerDisconnect(Player p) {
        removePlayer(p);
        // server will handle notifying client
    }

    public void startRound() {
        if (currentPlayers == 0) return;

        inGame = true;

        // Reset dealer
        dealer.resetHand();

        // Reset all players' hands for new round
        for (Player p : players) {
            if (p != null) {
                p.setNumHands(1);
                p.setTotalCardValue(0);
            }
        }

        // Deal 2 cards to each active player
        for (Player p : players) {
            if (p != null) {
                p.hit(shoe);
                p.hit(shoe);
            }
        }

        // Deal dealer hand
        dealer.hit(shoe);
        dealer.hit(shoe);
    }

    public void endRound() {
        if (!inGame) {
          return;
        }

        dealer.playDealerHand(shoe);

        // Compare dealer hand vs players
        int dealerTotal = dealer.getTotalCardValue();

        for (Player p : players) {
            if (p == null) continue;

            // Every hand the player has
            for (int h = 0; h < p.getNumHands(); h++) {

                int playerValue = p.getTotalCardValue();

                if (playerValue > 21) {
                    // player bust, loses automatically
                    continue;
                }

                if (dealerTotal > 21 || playerValue > dealerTotal) {
                    // Player wins
                    // assuming 1:1 payout
                    int winnings = p.getCurrentBet() * 2;
                    p.setBalance(p.getBalance() + winnings);
                }
            }
        }

        resetTable();
    }

    
    public void checkHands(Player[] players) {
        for (Player p : players) {
            if (p != null && p.getTotalCardValue() == 21) {
                //need to add the blackjack logic
            }
        }
    }


    public void resetTable() {
        inGame = false;
        dealer.resetHand();
    }

    public Player getPlayer(int userId) {
        for (Player p : players) {
            if (p != null && p.getUserId() == userId) {
                return p;
            }
        }
        return null;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    public int getTableId() {
        return tableId;
    }
}
