package electionsystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test suite for the Ballot class
 * Tests ballot creation, ballot ID retrieval, and vote retrieval functionality
 * Written by Finn Gidden
 */
public class BallotTest {
  
  private Ballot ballot;
  private List<Integer> votes;
  
  /**
   * Set up test fixtures before each test
   * Creates a sample ballot with multiple ranked candidate preferences
   */
  @BeforeEach
  public void setUp() {
    votes = new ArrayList<>(Arrays.asList(3, 1, 4, 2));
    ballot = new Ballot(101, votes);
  }
  
  /**
   * Test that the constructor properly initializes a ballot with valid ID and votes
   */
  @Test
  public void testConstructorValidInputs() {
    assertNotNull(ballot);
    assertEquals(101, ballot.getBallotID());
  }
  
  /**
   * Test constructor with empty vote list
   */
  @Test
  public void testConstructorEmptyVoteList() {
    List<Integer> emptyVotes = new ArrayList<>();
    Ballot emptyBallot = new Ballot(200, emptyVotes);
    assertNotNull(emptyBallot);
    assertEquals(200, emptyBallot.getBallotID());
  }
  
  /**
   * Test constructor with single vote
   */
  @Test
  public void testConstructorSingleVote() {
    List<Integer> singleVote = new ArrayList<>(Arrays.asList(5));
    Ballot singleBallot = new Ballot(300, singleVote);
    assertNotNull(singleBallot);
    assertEquals(5, singleBallot.getVote(0));
  }
  
  /**
   * Test that getBallotID returns the correct ballot identifier
   */
  @Test
  public void testGetBallotID() {
    assertEquals(101, ballot.getBallotID());
  }
  
  /**
   * Test getBallotID with different ballot IDs
   */
  @Test
  public void testGetBallotIDDifferentValues() {
    Ballot ballot1 = new Ballot(1, votes);
    Ballot ballot2 = new Ballot(999, votes);
    Ballot ballot3 = new Ballot(0, votes);
    
    assertEquals(1, ballot1.getBallotID());
    assertEquals(999, ballot2.getBallotID());
    assertEquals(0, ballot3.getBallotID());
  }
  
  /**
   * Test that getVoteCount returns the correct number of votes
   */
  @Test
  public void testGetVoteCount() {
    assertEquals(4, ballot.getVoteCount());
  }

  /**
   * Test that getVote returns the correct candidate ID at rank 0 (first choice)
   */
  @Test
  public void testGetVoteFirstRank() {
    assertEquals(3, ballot.getVote(0));
  }
  
  /**
   * Test that getVote returns the correct candidate ID at rank 1 (second choice)
   */
  @Test
  public void testGetVoteSecondRank() {
    assertEquals(1, ballot.getVote(1));
  }
  
  /**
   * Test that getVote returns the correct candidate ID at rank 2 (third choice)
   */
  @Test
  public void testGetVoteThirdRank() {
    assertEquals(4, ballot.getVote(2));
  }
  
  /**
   * Test that getVote returns the correct candidate ID at the last rank
   */
  @Test
  public void testGetVoteLastRank() {
    assertEquals(2, ballot.getVote(3));
  }
  
  /**
   * Test getVote with all valid rank positions
   */
  @Test
  public void testGetVoteAllRanks() {
    assertEquals(3, ballot.getVote(0));
    assertEquals(1, ballot.getVote(1));
    assertEquals(4, ballot.getVote(2));
    assertEquals(2, ballot.getVote(3));
  }
  
  /**
   * Test that getVote throws IndexOutOfBoundsException for invalid negative rank
   */
  @Test
  public void testGetVoteNegativeRank() {
    assertThrows(IndexOutOfBoundsException.class, () -> {
      ballot.getVote(-1);
    });
  }
  
  /**
   * Test that getVote throws IndexOutOfBoundsException for rank beyond vote list size
   */
  @Test
  public void testGetVoteRankOutOfBounds() {
    assertThrows(IndexOutOfBoundsException.class, () -> {
      ballot.getVote(4);
    });
  }
  
  /**
   * Test that getVote throws IndexOutOfBoundsException for rank far beyond vote list size
   */
  @Test
  public void testGetVoteRankFarOutOfBounds() {
    assertThrows(IndexOutOfBoundsException.class, () -> {
      ballot.getVote(100);
    });
  }
  
  /**
   * Test ballot with long ranked preference list
   */
  @Test
  public void testBallotWithManyVotes() {
    List<Integer> manyVotes = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    Ballot largeBallot = new Ballot(500, manyVotes);
    
    assertEquals(1, largeBallot.getVote(0));
    assertEquals(5, largeBallot.getVote(4));
    assertEquals(10, largeBallot.getVote(9));
  }
  
  /**
   * Test that multiple ballots maintain independent state
   */
  @Test
  public void testMultipleBallotIndependence() {
    List<Integer> votes1 = new ArrayList<>(Arrays.asList(1, 2, 3));
    List<Integer> votes2 = new ArrayList<>(Arrays.asList(4, 5, 6));
    
    Ballot ballot1 = new Ballot(100, votes1);
    Ballot ballot2 = new Ballot(200, votes2);
    
    assertEquals(100, ballot1.getBallotID());
    assertEquals(200, ballot2.getBallotID());
    assertEquals(1, ballot1.getVote(0));
    assertEquals(4, ballot2.getVote(0));
  }
}