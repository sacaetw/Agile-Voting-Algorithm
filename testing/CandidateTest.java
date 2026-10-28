package electionsystem;

import org.junit.jupiter.api.Test;

import electionsystem.Ballot;
import electionsystem.Candidate;

import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Comprehensive test suite for the Candidate class.
 * Tests all public methods and verifies correct behavior for various scenarios
 * including constructor initialization, getter methods, and ballot management.
 * Written by Finn Gidden
 */
public class CandidateTest {

  private Candidate candidate;
  private Ballot ballot1;
  private Ballot ballot2;
  private Ballot ballot3;

  /**
   * Sets up test fixtures before each test.
   * Creates a standard candidate and sample ballots for testing.
   */
  @BeforeEach
  public void setUp() {
    candidate = new Candidate(1, "John Smith", "Democratic");
    
    // Create sample ballots for testing addBallot functionality
    List<Integer> votes1 = new ArrayList<>();
    votes1.add(1);
    votes1.add(2);
    votes1.add(3);
    ballot1 = new Ballot(101, votes1);

    List<Integer> votes2 = new ArrayList<>();
    votes2.add(2);
    votes2.add(1);
    ballot2 = new Ballot(102, votes2);

    List<Integer> votes3 = new ArrayList<>();
    votes3.add(1);
    ballot3 = new Ballot(103, votes3);
  }

  /**
   * Tests that the constructor properly initializes all candidate attributes.
   * Verifies that ID, name, and party are set correctly, isEliminated is false,
   * and the ballots list is initialized as empty.
   */
  @Test
  public void testConstructorInitialization() {
    Candidate testCandidate = new Candidate(5, "Jane Doe", "Republican");
    
    assertEquals(5, testCandidate.getCandidateID(), 
        "Candidate ID should match constructor parameter");
    assertEquals("Jane Doe", testCandidate.getCandidateName(), 
        "Candidate name should match constructor parameter");
    assertEquals("Republican", testCandidate.getCandidateParty(), 
        "Candidate party should match constructor parameter");
    assertFalse(testCandidate.getIsEliminated(), 
        "New candidate should not be eliminated by default");
    assertNotNull(testCandidate.getBallots(), 
        "Ballots list should be initialized (not null)");
    assertTrue(testCandidate.getBallots().isEmpty(), 
        "New candidate should have empty ballots list");
  }

  /**
   * Tests getCandidateID() method.
   * Verifies that the method returns the correct candidate identifier.
   */
  @Test
  public void testGetCandidateID() {
    assertEquals(1, candidate.getCandidateID(), 
        "getCandidateID should return the ID set in constructor");
  }

  /**
   * Tests getCandidateID() with various ID values.
   * Verifies correct handling of different integer values including zero and negative.
   */
  @Test
  public void testGetCandidateIDVariousValues() {
    Candidate candidate0 = new Candidate(0, "Test", "Party");
    assertEquals(0, candidate0.getCandidateID(), 
        "getCandidateID should correctly return zero");
    
    Candidate candidateNegative = new Candidate(-5, "Test", "Party");
    assertEquals(-5, candidateNegative.getCandidateID(), 
        "getCandidateID should correctly return negative ID");
    
    Candidate candidateLarge = new Candidate(999999, "Test", "Party");
    assertEquals(999999, candidateLarge.getCandidateID(), 
        "getCandidateID should correctly return large ID");
  }

  /**
   * Tests getCandidateName() method.
   * Verifies that the method returns the correct candidate name.
   */
  @Test
  public void testGetCandidateName() {
    assertEquals("John Smith", candidate.getCandidateName(), 
        "getCandidateName should return the name set in constructor");
  }

  /**
   * Tests getCandidateName() with various name formats.
   * Verifies correct handling of different string values including empty strings
   * and names with special characters.
   */
  @Test
  public void testGetCandidateNameVariousFormats() {
    Candidate shortName = new Candidate(1, "Al", "Party");
    assertEquals("Al", shortName.getCandidateName(), 
        "getCandidateName should handle short names");
    
    Candidate longName = new Candidate(2, "Mary Elizabeth Jane O'Connor-Smith III", "Party");
    assertEquals("Mary Elizabeth Jane O'Connor-Smith III", longName.getCandidateName(), 
        "getCandidateName should handle long names with special characters");
    
    Candidate emptyName = new Candidate(3, "", "Party");
    assertEquals("", emptyName.getCandidateName(), 
        "getCandidateName should handle empty string");
  }

  /**
   * Tests getCandidateParty() method.
   * Verifies that the method returns the correct party affiliation.
   */
  @Test
  public void testGetCandidateParty() {
    assertEquals("Democratic", candidate.getCandidateParty(), 
        "getCandidateParty should return the party set in constructor");
  }

  /**
   * Tests getCandidateParty() with various party values.
   * Verifies correct handling of different party affiliations including
   * independent candidates and empty strings.
   */
  @Test
  public void testGetCandidatePartyVariousValues() {
    Candidate republican = new Candidate(1, "Name", "Republican");
    assertEquals("Republican", republican.getCandidateParty(), 
        "getCandidateParty should return Republican");
    
    Candidate independent = new Candidate(2, "Name", "Independent");
    assertEquals("Independent", independent.getCandidateParty(), 
        "getCandidateParty should return Independent");
    
    Candidate noParty = new Candidate(3, "Name", "");
    assertEquals("", noParty.getCandidateParty(), 
        "getCandidateParty should handle empty party string");
  }

  /**
   * Tests getIsEliminated() method for newly created candidate.
   * Verifies that candidates start as not eliminated.
   */
  @Test
  public void testGetIsEliminatedInitialState() {
    assertFalse(candidate.getIsEliminated(), 
        "Newly created candidate should not be eliminated");
  }

  /**
   * Tests that isEliminated remains false throughout candidate lifecycle.
   * This verifies the default state is maintained.
   */
  @Test
  public void testGetIsEliminatedRemainsConsistent() {
    assertFalse(candidate.getIsEliminated(), 
        "isEliminated should be false initially");
    
    // Add some ballots
    candidate.addBallot(ballot1);
    candidate.addBallot(ballot2);
    
    assertFalse(candidate.getIsEliminated(), 
        "isEliminated should remain false after adding ballots");
  }

  /**
   * Tests getBallots() method for newly created candidate.
   * Verifies that the initial ballots list is empty but not null.
   */
  @Test
  public void testGetBallotsInitialState() {
    List<Integer> ballots = candidate.getBallots();
    
    assertNotNull(ballots, "getBallots should return a list, not null");
    assertTrue(ballots.isEmpty(), "Initial ballots list should be empty");
    assertEquals(0, ballots.size(), "Initial ballots list should have size 0");
  }

  /**
   * Tests addBallot() method with a single ballot.
   * Verifies that the ballot ID is correctly added to the candidate's ballot list.
   */
  @Test
  public void testAddBallotSingle() {
    candidate.addBallot(ballot1);
    
    List<Integer> ballots = candidate.getBallots();
    assertEquals(1, ballots.size(), "Ballots list should contain one ballot ID");
    assertTrue(ballots.contains(101), "Ballots list should contain ballot ID 101");
    assertEquals(101, ballots.get(0), "First ballot ID should be 101");
  }

  /**
   * Tests addBallot() method with multiple ballots.
   * Verifies that multiple ballot IDs are correctly added and maintained in order.
   */
  @Test
  public void testAddBallotMultiple() {
    candidate.addBallot(ballot1);
    candidate.addBallot(ballot2);
    candidate.addBallot(ballot3);
    
    List<Integer> ballots = candidate.getBallots();
    assertEquals(3, ballots.size(), "Ballots list should contain three ballot IDs");
    assertEquals(101, ballots.get(0), "First ballot ID should be 101");
    assertEquals(102, ballots.get(1), "Second ballot ID should be 102");
    assertEquals(103, ballots.get(2), "Third ballot ID should be 103");
  }

  /**
   * Tests that addBallot() maintains the order of ballot additions.
   * Verifies that ballots are stored in the order they were added.
   */
  @Test
  public void testAddBallotMaintainsOrder() {
    candidate.addBallot(ballot3);
    candidate.addBallot(ballot1);
    candidate.addBallot(ballot2);
    
    List<Integer> ballots = candidate.getBallots();
    assertEquals(3, ballots.size(), "Ballots list should contain three ballot IDs");
    assertEquals(103, ballots.get(0), "First ballot ID should be 103 (added first)");
    assertEquals(101, ballots.get(1), "Second ballot ID should be 101 (added second)");
    assertEquals(102, ballots.get(2), "Third ballot ID should be 102 (added third)");
  }

  /**
   * Tests addBallot() with the same ballot added multiple times.
   * Verifies that duplicate ballot IDs can be added (if system allows it).
   */
  @Test
  public void testAddBallotDuplicates() {
    candidate.addBallot(ballot1);
    candidate.addBallot(ballot1);
    
    List<Integer> ballots = candidate.getBallots();
    assertEquals(2, ballots.size(), 
        "Adding same ballot twice should result in two entries");
    assertEquals(101, ballots.get(0), "First entry should be ballot ID 101");
    assertEquals(101, ballots.get(1), "Second entry should be ballot ID 101");
  }

  /**
   * Tests that getBallots() returns the actual list (not a copy).
   * This verifies the current implementation behavior.
   */
  @Test
  public void testGetBallotsReturnsSameReference() {
    candidate.addBallot(ballot1);
    
    List<Integer> ballots1 = candidate.getBallots();
    List<Integer> ballots2 = candidate.getBallots();
    
    assertSame(ballots1, ballots2, 
        "Multiple calls to getBallots should return the same list instance");
  }

  /**
   * Tests complete candidate workflow with all operations.
   * Verifies that all methods work correctly together in a realistic scenario.
   */
  @Test
  public void testCompleteWorkflow() {
    // Create a candidate
    Candidate alice = new Candidate(42, "Alice Johnson", "Green");
    
    // Verify initial state
    assertEquals(42, alice.getCandidateID());
    assertEquals("Alice Johnson", alice.getCandidateName());
    assertEquals("Green", alice.getCandidateParty());
    assertFalse(alice.getIsEliminated());
    assertTrue(alice.getBallots().isEmpty());
    
    // Add ballots
    alice.addBallot(ballot1);
    alice.addBallot(ballot2);
    
    // Verify final state
    assertEquals(2, alice.getBallots().size());
    assertTrue(alice.getBallots().contains(101));
    assertTrue(alice.getBallots().contains(102));
    assertFalse(alice.getIsEliminated());
  }

  /**
   * Tests candidate with edge case values.
   * Verifies handling of boundary conditions and unusual but valid inputs.
   */
  @Test
  public void testEdgeCaseValues() {
    Candidate edgeCandidate = new Candidate(Integer.MAX_VALUE, "Z", "X");
    
    assertEquals(Integer.MAX_VALUE, edgeCandidate.getCandidateID(), 
        "Should handle maximum integer ID");
    assertEquals("Z", edgeCandidate.getCandidateName(), 
        "Should handle single character name");
    assertEquals("X", edgeCandidate.getCandidateParty(), 
        "Should handle single character party");
  }

  /**
   * Tests that multiple candidates can exist independently.
   * Verifies that candidate instances do not interfere with each other.
   */
  @Test
  public void testMultipleCandidatesIndependence() {
    Candidate candidate1 = new Candidate(1, "Alice", "Party A");
    Candidate candidate2 = new Candidate(2, "Bob", "Party B");
    
    candidate1.addBallot(ballot1);
    candidate2.addBallot(ballot2);
    candidate2.addBallot(ballot3);
    
    assertEquals(1, candidate1.getBallots().size(), 
        "Candidate 1 should have 1 ballot");
    assertEquals(2, candidate2.getBallots().size(), 
        "Candidate 2 should have 2 ballots");
    assertFalse(candidate1.getBallots().contains(102), 
        "Candidate 1 should not have candidate 2's ballots");
    assertFalse(candidate2.getBallots().contains(101), 
        "Candidate 2 should not have candidate 1's ballots");
  }
}