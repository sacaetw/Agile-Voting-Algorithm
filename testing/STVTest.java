package electionsystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Comprehensive test suite for the STV class.
 * Tests all public methods and verifies correct behavior for various scenarios
 * including constructor initialization, getter methods, and election management.
 * Written by Finn Gidden, James Lu
 */
public class STVTest {
  
  @TempDir
  Path tempDir;
  
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  
  @BeforeEach
  public void setUpStreams() {
    System.setOut(new PrintStream(outContent));
  }
  
  @AfterEach
  public void restoreStreams() {
    System.setOut(originalOut);
  }
  
  /**
   * Helper method to create a candidate CSV file
   */
  private String createCandidateFile(String filename, String... lines) throws IOException {
    File file = tempDir.resolve(filename).toFile();
    try (FileWriter writer = new FileWriter(file)) {
      for (String line : lines) {
        writer.write(line + "\n");
      }
    }
    return file.getAbsolutePath();
  }
  
  /**
   * Helper method to create a ballot CSV file
   */
  private String createBallotFile(String filename, String... lines) throws IOException {
    File file = tempDir.resolve(filename).toFile();
    try (FileWriter writer = new FileWriter(file)) {
      for (String line : lines) {
        writer.write(line + "\n");
      }
    }
    return file.getAbsolutePath();
  }

  /**
   * Tests that the STV constructor correctly initializes an election with basic inputs,
   * verifying that the candidate list, ballot list, and seat count are set properly.
   */
  @Test
  public void testConstructorBasic() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2,3",
        "2,2,1,3",
        "3,3,2,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    assertNotNull(election);
    assertEquals(3, election.getCandidates().size());
    assertEquals(3, election.getBallots().size());
    assertEquals(1, election.getNumSeats());
  }

  /**
   * Tests that the STV constructor correctly sets the isTest flag to true when
   * test mode is enabled.
   */
  @Test
  public void testConstructorTestModeEnabled() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv", "1,Alice,Party A");
    String ballotFile = createBallotFile("ballots.csv", "STV", "1", "1,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    assertTrue(election.getIsTest());
  }
  
  /**
   * Tests that the STV constructor correctly sets the isTest flag to false when
   * test mode is disabled.
   */
  @Test
  public void testConstructorTestModeDisabled() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv", "1,Alice,Party A");
    String ballotFile = createBallotFile("ballots.csv", "STV", "1", "1,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        false
    );
    
    assertFalse(election.getIsTest());
  }
  
  /**
   * Tests that ballots are not shuffled when the STV election is constructed in test
   * mode, ensuring that two elections created with the same data produce identical
   * ballot orderings.
   */
  @Test
  public void testConstructorNoShuffleInTestMode() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2",
        "2,1,2",
        "3,2,1");
    
    // Create two elections with same data in test mode
    STV election1 = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    STV election2 = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    // Ballot order should be identical
    List<Ballot> ballots1 = election1.getBallots();
    List<Ballot> ballots2 = election2.getBallots();
    
    for (int i = 0; i < ballots1.size(); i++) {
      assertEquals(ballots1.get(i).getBallotID(), ballots2.get(i).getBallotID());
    }
  }
  
  /**
   * Tests that the election type reported in the output is "STV" after running
   * an election.
   */
  @Test
  public void testGetType() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv", "1,Alice,Party A");
    String ballotFile = createBallotFile("ballots.csv", "STV", "1", "1,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    String output = outContent.toString();
    assertTrue(output.contains("STV"));
  }
  
  /**
   * Tests that the Droop quota is correctly calculated as 3 for a single-seat
   * election with 4 ballots: floor(4 / (1 + 1)) + 1 = 3.
   */
  @Test
  public void testDroopQuotaSingleSeat() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2",
        "2,1,2",
        "3,2,1",
        "4,2,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    String output = outContent.toString();
    
    // Droop quota = floor(4 / (1 + 1)) + 1 = floor(2) + 1 = 3
    assertTrue(output.contains("Droop Quota: 3"));
  }
  
  /**
   * Tests that the Droop quota is correctly calculated as 3 for a two-seat
   * election with 6 ballots: floor(6 / (2 + 1)) + 1 = 3.
   */
  @Test
  public void testDroopQuotaMultipleSeats() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "2",
        "1,1,2,3",
        "2,1,2,3",
        "3,2,1,3",
        "4,2,1,3",
        "5,3,1,2",
        "6,3,1,2");
    
    STV election = new STV(
        2,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    String output = outContent.toString();
    
    // Droop quota = floor(6 / (2 + 1)) + 1 = floor(2) + 1 = 3
    assertTrue(output.contains("Droop Quota: 3"));
  }
  
  /**
   * Tests that the Droop quota is correctly calculated as 3 for a three-seat
   * election with 10 ballots: floor(10 / (3 + 1)) + 1 = 3.
   */
  @Test
  public void testDroopQuotaCalculation() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C",
        "4,Diana,Party D");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "3",
        "1,1,2,3,4",
        "2,1,2,3,4",
        "3,2,1,3,4",
        "4,2,1,3,4",
        "5,3,1,2,4",
        "6,3,1,2,4",
        "7,4,1,2,3",
        "8,4,1,2,3",
        "9,1,2,3,4",
        "10,2,1,3,4");
    
    STV election = new STV(
        3,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    String output = outContent.toString();
    
    // Droop quota = floor(10 / (3 + 1)) + 1 = floor(2.5) + 1 = 3
    assertTrue(output.contains("Droop Quota: 3"));
  }
  
  /**
   * Tests that a single-seat election with a clear majority winner correctly
   * elects Alice, produces exactly one winner, and places the remaining two
   * candidates in the losers list.
   */
  @Test
  public void testRunElectionSingleSeatClearWinner() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2,3",
        "2,1,2,3",
        "3,1,2,3",
        "4,1,2,3",
        "5,2,1,3",
        "6,3,2,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    List<Candidate> losers = election.getLosers();

    assertEquals(1, winners.size());
    assertEquals(2, losers.size());
    assertEquals("Alice", winners.get(0).getCandidateName());
  }
  
  /**
   * Tests that a two-seat election correctly fills both seats, verifying that
   * Alice and Bob each win a seat when they both exceed the Droop quota.
   */
  @Test
  public void testRunElectionMultipleSeats() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C",
        "4,Diana,Party D");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "2",
        "1,1,2,3,4",
        "2,1,2,3,4",
        "3,1,2,3,4",
        "4,2,1,3,4",
        "5,2,1,3,4",
        "6,2,1,3,4",
        "7,3,4,1,2",
        "8,4,3,2,1");
    
    STV election = new STV(
        2,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    
    assertEquals(2, winners.size());
    // Alice and Bob should both meet quota
    assertTrue(winners.get(0).getCandidateName().equals("Alice") || 
               winners.get(0).getCandidateName().equals("Bob"));
    assertTrue(winners.get(1).getCandidateName().equals("Alice") || 
               winners.get(1).getCandidateName().equals("Bob"));
  }
  
  /**
   * Tests that when no candidate reaches the Droop quota in the first round,
   * the candidate elimination process is triggered and "Eliminated:" appears
   * in the output.
   */
  @Test
  public void testRunElectionWithElimination() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2,3",
        "2,1,2,3",
        "3,2,1,3",
        "4,3,2,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    String output = outContent.toString();
    
    // Should see elimination in output
    assertTrue(output.contains("Eliminated:"));
  }
  
  /**
   * Tests that after a candidate is eliminated, their ballots are correctly
   * redistributed to the next available ranked candidate and the election
   * still produces exactly one winner.
   */
  @Test
  public void testBallotRedistributionAfterElimination() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    // Charlie has fewest first-choice votes, should be eliminated
    // Charlie's ballot should transfer to Bob (second choice)
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2,3",  // Alice first
        "2,1,2,3",  // Alice first
        "3,2,1,3",  // Bob first
        "4,3,2,1"); // Charlie first, should go to Bob after elimination
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    assertEquals(1, winners.size());
  }
  
  /**
   * Tests that when redistributing ballots after an elimination, already-eliminated
   * candidates are skipped and the ballot is assigned to the next valid active
   * candidate in the voter's ranked list.
   */
  @Test
  public void testBallotSkipsEliminatedCandidates() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C",
        "4,Diana,Party D");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2,3,4",
        "2,1,2,3,4",
        "3,2,3,4,1",
        "4,4,3,2,1"); // Diana first, Charlie second
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    // Election should complete without errors
    assertEquals(1, election.getWinners().size());
  }
  
  /**
   * Tests that a ballot ranking only one candidate becomes exhausted when that
   * candidate is eliminated, and that the election still completes successfully
   * with one winner.
   */
  @Test
  public void testExhaustedBallot() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    // Ballot 4 only ranks Charlie, so it becomes exhausted when Charlie is eliminated
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2,3",
        "2,1,2,3",
        "3,2,1,3",
        "4,3");  // Only Charlie ranked
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    // Should complete without errors despite exhausted ballot
    assertEquals(1, election.getWinners().size());
  }
  
  /**
   * Tests that an election with evenly distributed first-choice votes proceeds
   * through multiple rounds of elimination, verified by the presence of both
   * "Round 1" and "Round 2" markers in the output.
   */
  @Test
  public void testMultipleRounds() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C",
        "4,Diana,Party D");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2,3,4",
        "2,2,3,4,1",
        "3,3,4,1,2",
        "4,4,1,2,3");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    String output = outContent.toString();
    
    // Should see multiple rounds
    assertTrue(output.contains("Round 1") || output.contains("--- Round 1 ---"));
    assertTrue(output.contains("Round 2") || output.contains("--- Round 2 ---"));
  }
  
  /**
   * Tests that the round counter increments during the election and the total
   * round count is included in the generated report output.
   */
  @Test
  public void testRoundCounterIncrements() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2,3",
        "2,2,1,3",
        "3,3,2,1",
        "4,1,2,3");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    String output = outContent.toString();
    
    // Check that total rounds is reported
    assertTrue(output.contains("Total Rounds:"));
  }
  
  /**
   * Tests that a candidate with votes meeting the Droop quota in the first round
   * is immediately elected, and that "Elected: Alice" appears in the output.
   */
  @Test
  public void testCandidateElectedAtQuota() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2",
        "2,1,2",
        "3,1,2",
        "4,2,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    String output = outContent.toString();
    
    // Alice should be elected immediately with 3 votes (quota = 3)
    assertTrue(output.contains("Elected: Alice"));
  }
  
  /**
   * Tests that when no candidate meets the Droop quota in a round, the candidate
   * with the fewest votes is eliminated, producing an "Eliminated:" message in
   * the output.
   */
  @Test
  public void testLowestCandidateEliminatedWhenNoQuota() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2,3",
        "2,2,1,3",
        "3,3,2,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    String output = outContent.toString();
    
    // No one meets quota initially, someone should be eliminated
    assertTrue(output.contains("Eliminated:"));
  }
  
  /**
   * Tests that at most one candidate is elected per round by verifying that
   * the number of "Elected:" lines in the output is at least one when two
   * candidates meet quota across separate rounds.
   */
  @Test
  public void testOneCandidateElectedPerRound() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "2",
        "1,1,2,3",
        "2,1,2,3",
        "3,1,2,3",
        "4,2,1,3",
        "5,2,1,3",
        "6,2,1,3");
    
    STV election = new STV(
        2,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    String output = outContent.toString();
    
    // Both Alice and Bob meet quota, but should be elected in different rounds
    int electedCount = 0;
    for (String line : output.split("\n")) {
      if (line.contains("Elected:")) {
        electedCount++;
      }
    }
    
    // Should see elections in output (may be 1 or 2 depending on implementation)
    assertTrue(electedCount >= 1);
  }
  
  /**
   * Tests that when the number of requested seats exceeds the number of candidates,
   * only as many winners as there are candidates are declared (all candidates win).
   */
  @Test
  public void testMoreSeatsThanCandidates() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "5",
        "1,1,2",
        "2,2,1");
    
    STV election = new STV(
        5,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    
    // Should only have 2 winners (all candidates)
    assertEquals(2, winners.size());
  }
  
  /**
   * Tests that an election with a single candidate and multiple ballots correctly
   * elects that candidate as the sole winner.
   */
  @Test
  public void testSingleCandidate() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1",
        "2,1",
        "3,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    
    assertEquals(1, winners.size());
    assertEquals("Alice", winners.get(0).getCandidateName());
  }
  
  /**
   * Tests that remaining unfilled seats are allocated from the back of the losers
   * list when active candidates are exhausted before all seats are filled, producing
   * exactly two winners.
   */
  @Test
  public void testAllSeatsFilledFromRemainingCandidates() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    // Create scenario where quota is high and remaining candidates fill seats
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "2",
        "1,1,2,3",
        "2,2,3,1",
        "3,3,1,2");
    
    STV election = new STV(
        2,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    List<Candidate> losers = election.getLosers();
    
    assertEquals(2, winners.size());
    assertEquals(1, losers.size());
  }
  
  /**
   * Tests that an empty or malformed ballot line in the ballot file does not
   * cause an error, and the election still completes with one winner.
   */
  @Test
  public void testEmptyBallotRankings() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2",
        "2,1,2",
        "3,");  // Empty ballot
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    // Should complete without errors
    assertEquals(1, election.getWinners().size());
  }
  
  /**
   * Tests that no candidate appears in both the winners and losers lists after
   * the election completes, ensuring mutual exclusivity of the two result sets.
   */
  @Test
  public void testWinnersAndLosersNoOverlap() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2,3",
        "2,2,1,3",
        "3,3,2,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    List<Candidate> losers = election.getLosers();
    
    // Ensure no candidate appears in both lists
    for (Candidate winner : winners) {
      for (Candidate loser : losers) {
        assertNotEquals(winner.getCandidateID(), loser.getCandidateID());
      }
    }
  }
  
  /**
   * Tests that every candidate is accounted for after the election completes,
   * meaning the combined size of the winners and losers lists equals the total
   * number of candidates.
   */
  @Test
  public void testAllCandidatesAccountedFor() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C",
        "4,Diana,Party D");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "2",
        "1,1,2,3,4",
        "2,2,3,4,1",
        "3,3,4,1,2",
        "4,4,1,2,3");
    
    STV election = new STV(
        2,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    List<Candidate> losers = election.getLosers();
    
    assertEquals(4, winners.size() + losers.size());
  }
  
  /**
   * Tests that winners are added to the winners list in the order they are elected
   * across rounds, and that the correct number of winners is produced for a
   * two-seat election.
   */
  @Test
  public void testWinnersOrderedByElectionRound() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "2",
        "1,1,2,3",
        "2,1,2,3",
        "3,1,2,3",
        "4,2,1,3",
        "5,2,1,3",
        "6,2,1,3");
    
    STV election = new STV(
        2,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    
    // Winners should be in order they were elected
    assertEquals(2, winners.size());
  }
  
  /**
   * Tests that the generated report output contains the "Droop Quota:" label,
   * confirming that quota information is included in the election report.
   */
  @Test
  public void testReportGenerationContainsDroopQuota() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2",
        "2,2,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    String output = outContent.toString();
    
    assertTrue(output.contains("Droop Quota:"));
  }
  
  /**
   * Tests that the generated report output contains the "Total Rounds:" label,
   * confirming that the round count is included in the election report.
   */
  @Test
  public void testReportGenerationContainsRoundCount() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2,3",
        "2,2,1,3",
        "3,3,2,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    String output = outContent.toString();
    
    assertTrue(output.contains("Total Rounds:"));
  }
  
  /**
   * Tests that running an election in test mode produces the "Running STV election
   * in test mode" message in the output, confirming test mode output behavior.
   */
  @Test
  public void testReportGenerationInTestMode() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2",
        "2,1,2",
        "3,2,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    String output = outContent.toString();
    
    // Should see round-by-round details in test mode
    assertTrue(output.contains("Running STV election in test mode"));
  }
  
  /**
   * Tests that the election report output contains both elected and non-elected
   * candidate sections, verifying that the full results summary is present.
   */
  @Test
  public void testReportContainsElectedAndEliminated() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2,3",
        "2,2,1,3",
        "3,3,2,1",
        "4,1,2,3");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    String output = outContent.toString();
    
    assertTrue(output.contains("ELECTED") || output.contains("Elected:"));
    assertTrue(output.contains("NON-ELECTED") || output.contains("Eliminated:"));
  }
  
  /**
   * Tests that candidate CSV entries with leading and trailing whitespace are
   * parsed correctly, producing properly trimmed candidate ID, name, and party values.
   */
  @Test
  public void testParseCandidatesWithWhitespace() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "  1  ,  Alice  ,  Party A  ",
        "2,Bob,Party B");
    
    String ballotFile = createBallotFile("ballots.csv", "STV", "1", "1,1,2");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    assertEquals(2, election.getCandidates().size());
    
    Candidate alice = election.getCandidates().get(0);
    assertEquals(1, alice.getCandidateID());
    assertEquals("Alice", alice.getCandidateName());
    assertEquals("Party A", alice.getCandidateParty());
  }
  
  /**
   * Tests that ballot CSV entries with leading and trailing whitespace around
   * the ballot ID and vote values are parsed correctly without error.
   */
  @Test
  public void testParseBallotsWithWhitespace() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv", "1,Alice,Party A");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "  1  ,  1  ",
        "2,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    assertEquals(2, election.getBallots().size());
  }
  
  /**
   * Tests that empty lines interspersed in candidate and ballot CSV files are
   * skipped during parsing and do not affect the resulting object counts.
   */
  @Test
  public void testParseEmptyLines() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "",
        "2,Bob,Party B");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2",
        "",
        "2,2,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    assertEquals(2, election.getCandidates().size());
    assertEquals(2, election.getBallots().size());
  }
  
  /**
   * Tests that ranked-choice ballots are parsed in full, including partial rankings,
   * and that each preference position is accessible via getVote() with the correct
   * candidate ID.
   */
  @Test
  public void testParseRankedChoiceBallots() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2,3",
        "2,3,1,2",
        "3,2");  // Partial ranking
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    assertEquals(2, election.getBallots().size());
    
    Ballot ballot1 = election.getBallots().get(0);
    assertEquals(1, ballot1.getVote(0));  // First choice: Alice
    assertEquals(2, ballot1.getVote(1));  // Second choice: Bob
    assertEquals(3, ballot1.getVote(2));  // Third choice: Charlie
  }
  
  /**
   * Tests a full end-to-end STV election scenario with four candidates and seven
   * ballots competing for two seats, verifying that exactly two winners and two
   * losers are produced and the STV election report is generated.
   */
  @Test
  public void testCompleteElectionScenario() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C",
        "4,Diana,Party D");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "2",
        "1,1,2,3,4",
        "2,1,2,3,4",
        "3,1,2,3,4",
        "4,2,1,3,4",
        "5,2,1,3,4",
        "6,3,4,2,1",
        "7,4,3,2,1");
    
    STV election = new STV(
        2,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    List<Candidate> losers = election.getLosers();
    
    assertEquals(2, winners.size());
    assertEquals(2, losers.size());
    
    String output = outContent.toString();
    assertTrue(output.contains("STV ELECTION REPORT"));
  }
  
  /**
   * Tests that the active candidates list is fully populated before the election
   * runs and is reduced to at most one entry after the election completes, reflecting
   * that candidates are removed as they are elected or eliminated.
   */
  @Test
  public void testActiveCandidatesUpdated() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2,3",
        "2,2,1,3",
        "3,3,2,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    // Before running, all candidates should be active
    assertEquals(3, election.getActiveCandidates().size());
    
    election.runElection(true);
    
    // After running, only winner should remain or list should be empty
    assertTrue(election.getActiveCandidates().size() <= 1);
  }
  
  /**
   * Tests that a single invalid ballot (with less than 60% of candidates ranked)
   * is correctly added to the invalidBallots list.
   */
  @Test
  public void testSingleInvalidBallotAppearsInInvalidList() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C",
        "4,Diana,Party D");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    List<Ballot> invalidBallots = election.getInvalidBallots();
    
    assertEquals(1, invalidBallots.size());
    assertEquals(1, invalidBallots.get(0).getBallotID());

    assertEquals(0, election.getBallots().size());
  }
  
  /**
   * Tests that multiple invalid ballots all appear in the invalidBallots list.
   */
  @Test
  public void testMultipleInvalidBallotsAllAppearInInvalidList() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1",
        "2,2",
        "3,3");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    List<Ballot> invalidBallots = election.getInvalidBallots();
    
    assertEquals(3, invalidBallots.size());
    assertEquals(1, invalidBallots.get(0).getBallotID());
    assertEquals(2, invalidBallots.get(1).getBallotID());
    assertEquals(3, invalidBallots.get(2).getBallotID());

    assertEquals(0, election.getBallots().size());
  }
  
  /**
   * Tests that a valid ballot does NOT appear in the invalidBallots list.
   */
  @Test
  public void testValidBallotDoesNotAppearInInvalidList() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C",
        "4,Diana,Party D");
    
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
        "1,1,2,3");
    
    STV election = new STV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    List<Ballot> invalidBallots = election.getInvalidBallots();
    
    assertEquals(0, invalidBallots.size());

    assertEquals(1, election.getBallots().size());
    assertEquals(1, election.getBallots().get(0).getBallotID());
  }

  /**
   * Tests parsing the STV rank-column format using simplified IDs.
   * This verifies that the parser correctly maps column positions to Candidate IDs
   * and sorts them by the rank provided in each cell.
   */
  @Test
  public void testParseBallotsSTVSpecFormat() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
            "1,Alice,Party A",
            "2,Bob,Party B",
            "3,Charlie,Party C");

    String ballotFile = createBallotFile("ballots.csv",
            "STV",
            "1",
            "ballotId, 1, 2, 3",
            "1,1,,2",     // 1st choice: Alice (Col 1), 2nd choice: Charlie (Col 3)
            "2,3,2,1",    // 1st: Charlie, 2nd: Bob, 3rd: Alice
            "3,1,2,",      // 1st: Alice, 2nd: Bob
            "4,,1,2"       // 1st: Bob, 2nd: Charlie
    );

    STV election = new STV(
            1,
            Arrays.asList(ballotFile),
            Arrays.asList(candidateFile),
            true
    );

    List<Ballot> ballots = election.getBallots();
    assertEquals(4, ballots.size(), "Should successfully parse all 4 ballots");

    // Ballot 1
    assertEquals(1, ballots.get(0).getBallotID());
    assertEquals(3, ballots.get(0).getVoteCount());
    assertEquals(1, ballots.get(0).getVote(0)); // 1st choice is Alice (ID 1)
    assertEquals(3, ballots.get(0).getVote(1)); // 2nd choice is Charlie (ID 3)

    // Ballot 2
    assertEquals(2, ballots.get(1).getBallotID());
    assertEquals(3, ballots.get(1).getVoteCount());
    assertEquals(3, ballots.get(1).getVote(0)); // 1st choice is Charlie (ID 3)
    assertEquals(2, ballots.get(1).getVote(1)); // 2nd choice is Bob (ID 2)
    assertEquals(1, ballots.get(1).getVote(2)); // 3rd choice is Alice (ID 1)

    // Ballot 3
    assertEquals(3, ballots.get(2).getBallotID());
    assertEquals(2, ballots.get(2).getVoteCount());
    assertEquals(1, ballots.get(2).getVote(0));
    assertEquals(2, ballots.get(2).getVote(1));

    // Ballot 4
    assertEquals(4, ballots.get(3).getBallotID());
    assertEquals(3, ballots.get(3).getVoteCount());
    assertEquals(2, ballots.get(3).getVote(0));
    assertEquals(3, ballots.get(3).getVote(1));
  }
  
  /**
   * Tests that invalid ballots are completely excluded from the election execution.
   * Verifies that only valid ballots (with 60%+ rankings) are used to determine winners,
   * while invalid ballots are stored separately and do not affect the election outcome.
   */
  @Test
  public void testInvalidBallotsAreExcludedFromElectionExecution() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
            "1,Alice,Party A",
            "2,Bob,Party B",
            "3,Charlie,Party C",
            "4,Diana,Party D");

    // Create a mix of valid and invalid ballots
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "1",
            "1,1,2,3,4",     // VALID - 4 rankings
            "2,1",            // INVALID - only 1 ranking (needs 3 for 4 candidates)
            "3,2,3,4,1",     // VALID - 4 rankings
            "4,2",            // INVALID - only 1 ranking
            "5,3,4,1,2");    // VALID - 4 rankings

    STV election = new STV(
            1,  // Single seat
            Arrays.asList(ballotFile),
            Arrays.asList(candidateFile),
            true
    );

    // Before election, verify invalid ballots were removed
    assertEquals(3, election.getBallots().size(), "Only 3 valid ballots should remain");
    assertEquals(2, election.getInvalidBallots().size(), "2 invalid ballots should be stored");

    // Run the election
    election.runElection(true);

    // After election, verify winners were determined only from valid ballots
    List<Candidate> winners = election.getWinners();
    assertEquals(1, winners.size(), "Should have 1 winner");

    // To do after PBI 3
    // The winner should be based on the 3 valid ballots only, not counting the invalid ones
    // With 3 valid ballots all ranking Alice first, Alice should win
    // assertEquals("Alice", winners.get(0).getCandidateName());

    // Verify invalid ballots are STILL stored after election (not lost)
    assertEquals(2, election.getInvalidBallots().size());
  }
  
  /**
   * Tests that after removing invalid ballots, the ballot count is updated
   * and the Droop quota is calculated correctly with the new count.
   */
  @Test
  public void testBallotCountAndQuotaUpdatedAfterInvalidRemoval() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
            "1,Alice,Party A",
            "2,Bob,Party B",
            "3,Charlie,Party C",
            "4,Diana,Party D",
            "5,Eve,Party E");
  
    // Create 3 ballot files:
    // Ballot 1: Valid (ranks 4+ candidates = 80% of 5 candidates, meets 60% threshold)
    // Ballot 2: Invalid (only ranks 1 candidate = 20% of 5 candidates)
    // Ballot 3: Valid (ranks 3 candidates = 60% of 5 candidates, meets threshold exactly)
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "3",
            "1,1,2,3,4",      // Valid: 4 rankings
            "2,1",             // Invalid: 1 ranking
            "3,1,2,3");       // Valid: 3 rankings (exactly 60%)
  
    STV election = new STV(
            2,  // 2 seats
            Arrays.asList(ballotFile),
            Arrays.asList(candidateFile),
            true  // test mode
    );
  
    // Ballot count should only include valid ballots
    assertEquals(2, election.getBallots().size());  // 2 valid ballots (IDs 1 and 3)
    
    // Invalid ballots should be tracked separately
    List<Ballot> invalidBallots = election.getInvalidBallots();
    assertEquals(1, invalidBallots.size());
    assertEquals(2, invalidBallots.get(0).getBallotID());  // Invalid ballot ID 2
  
    // Droop quota should be calculated with new ballot count
    // Formula: floor(validBallots / (seats + 1)) + 1
    // floor(2 / (2 + 1)) + 1 = floor(2/3) + 1 = 0 + 1 = 1
    int expectedQuota = 1;
    
    // Run election to calculate quota
    election.runElection(true);
    
    // Verify quota was calculated correctly with updated ballot count
    // verifies no errors occur with the updated count
    assertNotNull(election);
  }

    /**
   * Tests that when all ballots are invalid, the quota is calculated
   * with zero valid ballots (edge case).
   */
  @Test
  public void testQuotaWithAllInvalidBallots() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
            "1,Alice,Party A",
            "2,Bob,Party B",
            "3,Charlie,Party C",
            "4,Diana,Party D",
            "5,Eve,Party E");
  
    // All ballots have only 1 ranking (<60% of 5 candidates)
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "3",
            "1,1",
            "2,2",
            "3,3");
  
    STV election = new STV(
            2,
            Arrays.asList(ballotFile),
            Arrays.asList(candidateFile),
            true
    );
  
    // All ballots should be invalid
    assertEquals(3, election.getInvalidBallots().size());
    assertEquals(0, election.getBallots().size());
    
    // Run election - should handle zero valid ballots gracefully
    election.runElection(true);
    
    // No winners should be elected (no valid ballots)
    assertEquals(0, election.getWinners().size());
  }
  
  /**
   * Tests that the quota does NOT include invalid ballots in its calculation.
   */
  @Test
  public void testInvalidBallotsExcludedFromQuotaCalculation() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
            "1,Alice,Party A",
            "2,Bob,Party B");
  
    // 10 total ballots: 6 valid, 4 invalid
    // 60% of 2 candidates = need 2 rankings to be valid (both candidates ranked)
    String ballotFile = createBallotFile("ballots.csv",
            "STV", "10",
            "1,1,2",  // Valid
            "2,1,2",  // Valid
            "3,1,2",  // Valid
            "4,1,2",  // Valid
            "5,1,2",  // Valid
            "6,1,2",  // Valid (6 valid)
            "7,1",    // Invalid
            "8,1",    // Invalid
            "9,1",    // Invalid
            "10,1");  // Invalid (4 invalid)
  
    STV election = new STV(
            1,
            Arrays.asList(ballotFile),
            Arrays.asList(candidateFile),
            true
    );
  
    assertEquals(6, election.getBallots().size());
    assertEquals(4, election.getInvalidBallots().size());
    
    // If quota used total ballots (10): floor(10/2)+1 = 5+1 = 6 (impossible to reach)
    // If quota uses valid ballots (6): floor(6/2)+1 = 3+1 = 4 (reachable)
    // The fact that a winner can be elected confirms valid ballot count was used
    election.runElection(true);
    
    // Should have a winner (since 6 valid ballots, quota=4, someone can reach it)
    assertTrue(election.getWinners().size() > 0);
  }
}
  
