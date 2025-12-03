package src.Testing;

import org.junit.jupiter.api.*;

import src.Game.Dealer;
import src.Game.Player;
import src.Game.Rank;
import src.Game.Suit;
import src.Game.Table;
import src.Game.Card;

import static org.junit.jupiter.api.Assertions.*;


public class TableTest {

    private Table table;
    private Player p1;
    private Player p2;

    @BeforeEach
    public void setup() {
        table = new Table();
        p1 = new Player("Lebron", "Password");
        p2 = new Player("Steph", "Password");

        p1.setBalance(1000);
        p2.setBalance(1000);
    }

    @Test
    public void testAddPlayer() {
        assertTrue(table.addPlayer(p1));
        assertEquals(1, table.getPlayers().size());
        assertTrue(table.getPlayers().contains(p1));
    }

    @Test
    public void testAddPlayerMaxCap() {
        for (int i = 0; i < 7; i++) {
            assertTrue(table.addPlayer(new Player("Player", "Password")));
        }
        assertFalse(table.addPlayer(new Player("TooMany", "Password")));
    }

    @Test
    public void testRemovePlayer() {
        table.addPlayer(p1);
        table.removePlayer(p1);
        assertFalse(table.getPlayers().contains(p1));
    }

    @Test
    public void testHandlePlayerDisconnect() {
        table.addPlayer(p1);
        table.handlePlayerDisconnect(p1);

        assertEquals(0, table.getPlayers().size());
    }

    @Test
    public void testPlaceBetValid() {
        table.addPlayer(p1);
        boolean ok = table.placeBet(p1, 50);

        assertTrue(ok);
        assertEquals(50, p1.getCurrentBet());
    }

    @Test
    public void testPlaceBetInvalidTooLow() {
        table.addPlayer(p1);
        assertFalse(table.placeBet(p1, 1));
    }

    @Test
    public void testPlaceBetTooHigh() {
        table.addPlayer(p1);
        assertFalse(table.placeBet(p1, 50001));
    }

    @Test
    public void testPlaceBetInsufficientBalance() {
        table.addPlayer(p1);
        p1.setBalance(3);
        assertFalse(table.placeBet(p1, 5));
    }

    @Test
    public void testStartRoundDealsCards() {
        table.addPlayer(p1);
        table.placeBet(p1, 50);

        table.startRound();

        assertTrue(p1.getHandSizes()[0] >= 2);
        assertTrue(table.getDealer().getHand().size() >= 2);
        assertTrue(table.isInGame());
    }

    @Test
    public void testEndRoundPlayerBust() {
        table.addPlayer(p1);
        table.placeBet(p1, 50);

        p1.setTotalCardValue(25);

        table.startRound();
        table.endRound();

        assertEquals(0, p1.getCurrentBet());
        assertTrue(p1.getLastResult().contains("YOU LOSE"));
        assertFalse(table.isInGame());
    }

    @Test
    public void testEndRoundDealerBustPlayerWins() {
        table.addPlayer(p1);
        table.placeBet(p1, 100);

        p1.setTotalCardValue(20);
        table.startRound();

        Dealer d = table.getDealer();
        d.getHand().clear();
        d.getHand().add(new Card(Rank.Ten, Suit.Clubs));
        d.getHand().add(new Card(Rank.Ten, Suit.Diamonds));
        d.getHand().add(new Card(Rank.Eight, Suit.Hearts));

        table.endRound();

        assertEquals(1200, p1.getBalance()); 
        assertTrue(p1.getLastResult().contains("WIN"));
    }

    @Test
    public void testEndRoundPush() {
        table.addPlayer(p1);
        table.placeBet(p1, 100);

        p1.setTotalCardValue(18);
        table.startRound();

        Dealer d = table.getDealer();
        d.getHand().clear();
        d.getHand().add(new Card(Rank.Ten, Suit.Spades));
        d.getHand().add(new Card(Rank.Eight, Suit.Diamonds));

        int before = p1.getBalance();
        table.endRound();

        assertEquals(before, p1.getBalance()); 
        assertTrue(p1.getLastResult().contains("Push"));
    }

    @Test
    public void testResetTable() {
        table.addPlayer(p1);
        table.placeBet(p1, 100);

        table.startRound();
        assertTrue(table.isInGame());

        table.resetTable();
        assertFalse(table.isInGame());
        assertEquals(0, table.getDealer().getHand().size());
    }

    @Test
    public void testGetPlayerById() {
        table.addPlayer(p1);
        int id = p1.getUserId();

        assertEquals(p1, table.getPlayer(id));
    }

    @Test
    public void testGetCurrentPlayers() {
        table.addPlayer(p1);
        table.addPlayer(p2);
        assertEquals(2, table.getCurrentPlayers());
    }
}
