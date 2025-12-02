package src.Game;

import java.util.*;
import java.util.concurrent.*;
import src.Server.BlackjackServer;

public class GameHandler {

    private final Table table;
    private final Map<Player, Boolean> playerDone;  
    private boolean roundInProgress;

    private Player currentTurnPlayer;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> currentTimeoutTask;
    private static final int TURN_TIMEOUT_SECONDS = 10;

    public GameHandler() {
        this.table = new Table();
        this.playerDone = new HashMap<>();
        this.roundInProgress = false;
        this.currentTurnPlayer = null;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public synchronized Table getTable() {
        return table;
    }


    public synchronized void join(Player p) {
        if (table.addPlayer(p)) {
            playerDone.put(p, false);
        }
    }

    public synchronized boolean placeBet(Player p, int amount) {
        boolean ok = table.placeBet(p, amount);
        if (ok) {
            playerDone.put(p, false); // this player is active this round
        }
        return ok;
    }



    public synchronized void startRound() {
        if (roundInProgress) return; // already in a round

        roundInProgress = true;

        // reset flags for all players with a bet
        for (Player p : table.getPlayers()) {
            if (p.getCurrentBet() > 0) {
                playerDone.put(p, false);
            }
        }

        // Let the table reset/deal initial cards
        table.startRound();

        // Decide first player in turn order (first with a bet > 0 and not done)
        currentTurnPlayer = findFirstActivePlayer();
        if (currentTurnPlayer != null) {
            scheduleTurnTimeout(currentTurnPlayer);
        } else {
            // No active players; immediately end round
            finishRound();
        }
    }

    public synchronized void hit(Player p) {
        if (!roundInProgress) return;
        if (p.getCurrentBet() <= 0) return;
        if (p != currentTurnPlayer) return; // Not this player's turn

        p.hit(table.getShoe());

        // Auto-finish player if bust or 21+
        if (p.getTotalCardValue() >= 21) {
            markPlayerDoneAndAdvance(p);
        } else {
            // still player's turn, reset timeout
            scheduleTurnTimeout(p);
        }
    }

    public synchronized void stand(Player p) {
        if (!roundInProgress) return;
        if (p.getCurrentBet() <= 0) return;
        if (p != currentTurnPlayer) return; 

        p.stand();
        markPlayerDoneAndAdvance(p);
    }

    public synchronized void doubleDown(Player p) {
        if (!roundInProgress) return;
        if (p.getCurrentBet() <= 0) return;
        if (p != currentTurnPlayer) return;

        boolean ok = p.doubleDown(p.getCurrentBet(), table.getShoe());
        if (ok) {
            // end turn
            markPlayerDoneAndAdvance(p);
        } else {
            // player can still hit/stand.
        }
    }


    public synchronized void split(Player p) {
        if (!roundInProgress) return;
        if (p.getCurrentBet() <= 0) return;
        if (p != currentTurnPlayer) return; // Not this player's turn

        p.split(table.getShoe());
        // Player still acts after split, so keep them as currentTurnPlayer
        // Just reset their timer
        scheduleTurnTimeout(p);
    }


    /**
     * Called whenever a player finishes their turn (stand, bust, 21, double).
     */
    private void markPlayerDoneAndAdvance(Player p) {
        playerDone.put(p, true);
        checkAndMaybeFinishOrAdvance();
    }

    /**
     * If all betting players are done, finish round;
     * otherwise move to the next player in table order.
     */
    private void checkAndMaybeFinishOrAdvance() {
        if (!roundInProgress) return;

        if (allBettingPlayersDone()) {
            finishRound();
        } else {
            advanceToNextPlayer();
        }
    }

    private void advanceToNextPlayer() {
        cancelCurrentTimeout();

        if (table.getPlayers().isEmpty()) {
            finishRound();
            return;
        }

        Player next = findNextActivePlayer(currentTurnPlayer);
        currentTurnPlayer = next;

        if (currentTurnPlayer != null) {
            scheduleTurnTimeout(currentTurnPlayer);
        } else {
            // Fallback: if no next active player found, end round
            finishRound();
        }
    }

    private void finishRound() {
    	if (!roundInProgress) return;

        roundInProgress = false;
        cancelCurrentTimeout();

        // Dealer plays, payouts applied, inGame set false
        table.endRound();

        currentTurnPlayer = null;

        // Mark all players as done
        for (Player p : table.getPlayers()) {
            playerDone.put(p, true);
        }

        for (Player p : table.getPlayers()) {
            String username = p.getUsername();
            int newBalance = p.getBalance();

            for (int i = 0; i < BlackjackServer.userData.size(); i++) {
                String[] parts = BlackjackServer.userData.get(i).split(",");
                if (parts.length >= 3 && parts[0].equals(username)) {
                    parts[2] = Integer.toString(newBalance);
                    BlackjackServer.userData.set(
                            i,
                            parts[0] + "," + parts[1] + "," + parts[2]
                    );
                    break;
                }
            }
        }

        BlackjackServer.saveUserData();
    }

    private boolean allBettingPlayersDone() {
        for (Player p : table.getPlayers()) {
            if (p.getCurrentBet() > 0) {
                Boolean done = playerDone.get(p);
                if (done == null || !done) {
                    return false;
                }
            }
        }
        return true;
    }

    private void scheduleTurnTimeout(Player p) {
        cancelCurrentTimeout();

        currentTimeoutTask = scheduler.schedule(() -> {
            synchronized (GameHandler.this) {
                // If round ended or this is no longer the current player, bail out
                if (!roundInProgress || p != currentTurnPlayer) {
                    return;
                }

                Boolean done = playerDone.get(p);
                if (done != null && done) {
                    return;
                }

                // Player took too long -> auto-stand and kick from table
                System.out.println("Player " + p.getUsername() + " timed out. Auto-stand & kick.");

                p.stand();
                playerDone.put(p, true);

                // Remove from table (kick)
                table.handlePlayerDisconnect(p);

                // Now move on
                checkAndMaybeFinishOrAdvance();
            }
        }, TURN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private void cancelCurrentTimeout() {
        if (currentTimeoutTask != null) {
            currentTimeoutTask.cancel(true);
            currentTimeoutTask = null;
        }
    }

    private Player findFirstActivePlayer() {
        for (Player p : table.getPlayers()) {
            if (p.getCurrentBet() > 0) {
                Boolean done = playerDone.get(p);
                if (done == null || !done) {
                    return p;
                }
            }
        }
        return null;
    }

    private Player findNextActivePlayer(Player current) {
        List<Player> list = table.getPlayers();
        if (list.isEmpty()) return null;

        boolean passedCurrent = (current == null);

        // First pass: from current onward
        for (Player p : list) {
            if (!passedCurrent) {
                if (p == current) {
                    passedCurrent = true;
                }
                continue;
            }
            if (p.getCurrentBet() > 0) {
                Boolean done = playerDone.get(p);
                if (done == null || !done) {
                    return p;
                }
            }
        }

        // Second pass: wrap-around before current
        for (Player p : list) {
            if (p == current) break;
            if (p.getCurrentBet() > 0) {
                Boolean done = playerDone.get(p);
                if (done == null || !done) {
                    return p;
                }
            }
        }

        return null;
    }



    public synchronized String buildState(Player p) {
        StringBuilder sb = new StringBuilder();

        sb.append("=========== MULTIPLAYER BLACKJACK ===========\n\n");

        sb.append("Player: ").append(p.getUsername())
          .append("   Balance: ").append(p.getBalance())
          .append("   Current Bet: ").append(p.getCurrentBet())
          .append("\n");

        sb.append("Round status: ")
          .append(roundInProgress ? "IN PROGRESS" : "WAITING / RESOLVED")
          .append("\n");

        // whose turn is it?
        sb.append("Current turn: ");
        if (roundInProgress && currentTurnPlayer != null) {
            sb.append(currentTurnPlayer.getUsername());
        } else {
            sb.append("None");
        }
        sb.append("\n\n");

        // ----- PLAYER HANDS -----
        sb.append("YOUR HAND(S):\n");
        Card[][] hands = p.getHands();
        int[] handSizes = p.getHandSizes();
        int[] handTotals = p.getHandTotals();

        for (int h = 0; h < p.getNumHands(); h++) {
            if (handSizes[h] == 0) continue;

            sb.append("Hand ").append(h + 1).append(": ");
            for (int i = 0; i < handSizes[h]; i++) {
                Card c = hands[h][i];
                if (c != null) {
                    sb.append(c.toString()).append("  ");
                }
            }
            sb.append("\nTotal: ").append(handTotals[h]).append("\n\n");
        }

        // ----- DEALER INFO -----
        Dealer d = table.getDealer();
        sb.append("DEALER:\n");
        if (roundInProgress) {
            if (!d.getHand().isEmpty()) {
                sb.append("Showing: ").append(d.getHand().get(0)).append("\n");
                if (d.getHand().size() > 1) {
                    sb.append("Hole Card: [Hidden]\n");
                }
            } else {
                sb.append("No cards dealt yet.\n");
            }
        } else {
            sb.append("Dealer Final Hand:\n");
            for (Card c : d.getHand()) {
                sb.append(c.toString()).append("  ");
            }
            sb.append("\nDealer Total: ").append(d.getTotalCardValue()).append("\n");
        }

        // ----- OTHER PLAYERS -----
        sb.append("\nOTHER PLAYERS AT TABLE:\n");
        for (Player other : table.getPlayers()) {
            if (other == p) continue;
            sb.append(" - ").append(other.getUsername())
              .append("  (Balance: ").append(other.getBalance())
              .append(", Bet: ").append(other.getCurrentBet())
              .append(")\n");
        }


        if (!roundInProgress) {
            sb.append("\n====== ROUND RESULTS ======\n");

            // Show THIS player's outcome clearly
            sb.append("\nYour Result: ");
            if (p.isBusted()) sb.append("YOU LOSE (BUST)");
            else if (p.isBlackjack()) sb.append("YOU WIN (BLACKJACK!)");
            else if ("WIN".equals(p.getLastOutcome())) sb.append("YOU WIN");
            else if ("LOSE".equals(p.getLastOutcome())) sb.append("YOU LOSE");
            else sb.append("Push");

            sb.append("\n");

            // Show all players' outcomes
            sb.append("\nAll Player Results:\n");
            for (Player pl : table.getPlayers()) {
                sb.append(" - ").append(pl.getUsername()).append(": ");

                if (pl.isBusted()) sb.append("BUSTED");
                else if (pl.isBlackjack()) sb.append("BLACKJACK!");
                else sb.append("Total ").append(pl.getTotalCardValue());

                sb.append(" | Outcome: ").append(pl.getLastOutcome());
                sb.append("\n");
            }

            sb.append("\n===========================\n");
        }


        sb.append("\n=============================================\n");
        return sb.toString();
    }
}
