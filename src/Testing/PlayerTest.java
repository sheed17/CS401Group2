package src.Testing;
import src.Game.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import src.Game.Player;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PlayerTest {

    private File userDataFile;
 

    // testing the creation of a userdata.txt file and putting users in it
    @Before
    public void setUp() throws Exception {
        userDataFile = new File("UserData.txt");
        writeUserData(
            "alice,1234,100\n" +
            "bob,abcd,200\n"
        );
    }

    // wirte into the txt file
    private void writeUserData(String content) throws IOException {
        try (FileWriter fw = new FileWriter(userDataFile, false)) {
            fw.write(content);
        }
    }

    // testing log in credentials success
    @Test
    public void testLoginSuccess() {
        Player p = new Player("alice", "1234");
        boolean result = p.logIn("alice", "1234");

        assertTrue("Login should succeed for valid credentials", result);
        assertEquals("Username should be set to alice", "alice", p.getUsername());
        assertEquals("Balance should be loaded from file", 100, p.getBalance());
    }

    // testing log in with wring password of existing users
    @Test
    public void testLoginWrongPassword() {
        Player p = new Player("alice", "wrong");
        boolean result = p.logIn("alice", "wrong");

        assertFalse("Login should fail for wrong password", result);
        assertNull("Username should be cleared on failed login", p.getUsername());
        assertEquals("Balance should be reset to 0 on failed login", 0, p.getBalance());
    }

    // tests log out 
    @Test
    public void testLogout() throws Exception {
        Player p = new Player("alice", "1234");
        boolean result = p.logIn("alice", "1234");
        assertTrue("Precondition: login should succeed", result);

        p.setBalance(250);
        p.logout();

        Player p2 = new Player("alice", "1234");
        boolean result2 = p2.logIn("alice", "1234");
        assertTrue("Login after logout should still succeed", result2);
        assertEquals("Balance should be updated in file after logout", 250, p2.getBalance());

        assertEquals("Balance in memory should be reset to 0 after logout", 0, p.getBalance());
        assertEquals("Current bet should be reset to 0 after logout", 0, p.getCurrentBet());
    }
    
    // testing betting
    @Test
    public void testBet() {
        Player p = new Player("alice", "1234");
        p.setBalance(100);
        int placed = p.bet(20);

        assertEquals(20, placed);
        assertEquals(80, p.getBalance());
        assertEquals(20, p.getCurrentBet());
    }
    
    // testing hit
    @Test
    public void testHit() {
        Player p = new Player("test", "pw");
        Dealer d = new Dealer();
        Shoe shoe = new Shoe(d);
        p.hit(shoe);
        assertEquals(1, p.getHandSizes()[0]);
        assertTrue(p.getHandTotals()[0] > 0);
    }
    
    // testing stand
    
    @Test
    public void testStand() {
        Player p = new Player("test", "pw");

        p.getHands()[0][0] = new Card(Rank.Ten, Suit.Spades);
        p.getHands()[0][1] = new Card(Rank.Six, Suit.Hearts);

        p.getHandSizes()[0] = 2;
        p.getHandTotals()[0] = 16;
        p.setTotalCardValue(16);

        assertEquals(16, p.stand());
    }
    
    
    // testing double down
    @Test
    public void testDoubleDown() {
        Player p = new Player("test", "pw");
        p.setBalance(200);
        p.bet(20);

        p.getHands()[0][0] = new Card(Rank.Eight, Suit.Spades);
        p.getHands()[0][1] = new Card(Rank.Eight, Suit.Hearts);

        p.getHandSizes()[0] = 2;
        p.getHandTotals()[0] = 16;
        p.setTotalCardValue(16);

        Dealer d = new Dealer();
        Shoe shoe = new Shoe(d);

        boolean ok = p.doubleDown(20, shoe);

        assertTrue(ok);
        assertEquals(40, p.getCurrentBet());
        assertEquals(3, p.getHandSizes()[0]); 
    }
    
    // testing split
    @Test
    public void testSplit() {
        Player p = new Player("test", "pw");
        p.setBalance(200);

        p.bet(20);

        Card eight1 = new Card(Rank.Eight, Suit.Spades);
        Card eight2 = new Card(Rank.Eight, Suit.Hearts);

        Card[][] hands = p.getHands();
        int[] handSizes = p.getHandSizes();
        int[] handBets = p.getHandBets();

        hands[0][0] = eight1;
        hands[0][1] = eight2;
        handSizes[0] = 2;            

        int originalBalance = p.getBalance();     
        int originalCurrentBet = p.getCurrentBet();  

        Dealer dealer = new Dealer();
        Shoe shoe = new Shoe(dealer);  

        p.split(shoe);
        assertEquals(2, p.getNumHands());
        assertEquals(2, p.getHandSizes()[0]);
        assertEquals(2, p.getHandSizes()[1]);

        assertEquals(20, handBets[0]);
        assertEquals(20, handBets[1]);

        assertEquals(originalBalance - 20, p.getBalance());

        assertEquals(originalCurrentBet + 20, p.getCurrentBet());

        assertTrue(p.getHandTotals()[0] > 0);
        assertTrue(p.getHandTotals()[1] > 0);
    }
    
}
