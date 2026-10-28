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

/**
 * Comprehensive test suite for the Plurality class.
 * Tests all public methods and verifies correct behavior for various scenarios
 * including constructor initialization, getter methods, and election management.
 * Written by Finn Gidden, James Lu
 */
public class PluralityTest {
  
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
   * Tests that the Plurality constructor correctly initializes an election with basic
   * inputs, verifying that the candidate list, ballot list, and seat count are set properly.
   */
  @Test
  public void testConstructorBasic() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
        "PV", "1",
        "1,1",
        "2,2",
        "3,1");
    
    Plurality election = new Plurality(
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
   * Tests that the Plurality constructor correctly sets the isTest flag to true when
   * test mode is enabled.
   */
  @Test
  public void testConstructorTestModeEnabled() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv", "1,Alice,Party A");
    String ballotFile = createBallotFile("ballots.csv", "PV", "1", "1,1");
    
    Plurality election = new Plurality(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    assertTrue(election.getIsTest());
  }
  
  /**
   * Tests that the Plurality constructor correctly sets the isTest flag to false when
   * test mode is disabled.
   */
  @Test
  public void testConstructorTestModeDisabled() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv", "1,Alice,Party A");
    String ballotFile = createBallotFile("ballots.csv", "PV", "1", "1,1");
    
    Plurality election = new Plurality(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        false
    );
    
    assertFalse(election.getIsTest());
  }
  
  /**
   * Tests that the election type reported in the output is "Plurality" after running
   * an election, verified indirectly through the generateReport output since getType()
   * is protected.
   */
  @Test
  public void testGetType() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv", "1,Alice,Party A");
    String ballotFile = createBallotFile("ballots.csv", "PV", "1", "1,1");
    
    Plurality election = new Plurality(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    // getType() is protected, but we can verify it indirectly through generateReport output
    election.runElection(true);
    String output = outContent.toString();
    assertTrue(output.contains("Plurality"));
  }
  
  /**
   * Tests that a single-seat election with a clear majority winner correctly elects
   * Alice with 3 votes, produces exactly one winner and two losers, and records the
   * correct ballot count on the winning candidate.
   */
  @Test
  public void testRunElectionSingleSeatClearWinner() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "1",
        "1,1",
        "2,1",
        "3,1",
        "4,2",
        "5,3");
    
    Plurality election = new Plurality(
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
    assertEquals(3, winners.get(0).getBallots().size());
  }
  
  /**
   * Tests that a two-seat election correctly elects the two candidates with the most
   * votes (Alice with 3, Bob with 2), producing two winners and two losers with the
   * correct vote counts recorded on each winner.
   */
  @Test
  public void testRunElectionMultipleSeats() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C",
        "4,Diana,Party D");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "2",
        "1,1",
        "2,1",
        "3,1",
        "4,2",
        "5,2",
        "6,3",
        "7,4");
    
    Plurality election = new Plurality(
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
    
    // Alice should win with 3 votes, Bob should win with 2 votes
    assertEquals("Alice", winners.get(0).getCandidateName());
    assertEquals("Bob", winners.get(1).getCandidateName());
    assertEquals(3, winners.get(0).getBallots().size());
    assertEquals(2, winners.get(1).getBallots().size());
  }
  
  /**
   * Tests that when the number of seats equals the number of candidates, all
   * candidates are elected and the losers list is empty.
   */
  @Test
  public void testRunElectionAllSeats() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "3",
        "1,1",
        "2,2",
        "3,3");
    
    Plurality election = new Plurality(
        3,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    List<Candidate> losers = election.getLosers();
    
    assertEquals(3, winners.size());
    assertEquals(0, losers.size());
  }
  
  /**
   * Tests that when two candidates are tied for the sole seat, the tiebreak mechanism
   * resolves the tie and exactly one winner is declared with the correct vote count.
   */
  @Test
  public void testRunElectionTwoWayTie() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "1",
        "1,1",
        "2,1",
        "3,2",
        "4,2",
        "5,3");
    
    Plurality election = new Plurality(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    
    assertEquals(1, winners.size());
    // Winner should be either Alice or Bob (both have 2 votes)
    assertTrue(winners.get(0).getCandidateName().equals("Alice") || 
               winners.get(0).getCandidateName().equals("Bob"));
    assertEquals(2, winners.get(0).getBallots().size());
  }
  
  /**
   * Tests that when all candidates are tied with equal votes for a single seat,
   * the tiebreak mechanism still resolves to exactly one winner with the correct
   * vote count.
   */
  @Test
  public void testRunElectionThreeWayTie() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "1",
        "1,1",
        "2,2",
        "3,3");
    
    Plurality election = new Plurality(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    
    assertEquals(1, winners.size());
    assertEquals(1, winners.get(0).getBallots().size());
  }
  
  /**
   * Tests that when two candidates are tied for the last available seat, the tiebreak
   * mechanism correctly resolves the tie, always electing Alice (clear leader) first
   * and using a coin toss to determine the second seat between Bob and Charlie.
   */
  @Test
  public void testRunElectionTieForLastSeat() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C",
        "4,Diana,Party D");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "2",
        "1,1",
        "2,1",
        "3,1",
        "4,2",
        "5,2",
        "6,3",
        "7,3",
        "8,4");
    
    Plurality election = new Plurality(
        2,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    
    assertEquals(2, winners.size());
    // Alice should definitely win with 3 votes
    assertEquals("Alice", winners.get(0).getCandidateName());
    // Second winner should be Bob or Charlie (both tied with 2 votes)
    assertTrue(winners.get(1).getCandidateName().equals("Bob") || 
               winners.get(1).getCandidateName().equals("Charlie"));
  }
  
  /**
   * Tests that when the number of requested seats exceeds the number of candidates,
   * only as many winners as there are candidates are declared (all candidates win).
   */
  @Test
  public void testRunElectionMoreSeatsThanCandidates() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "5",
        "1,1",
        "2,2");
    
    Plurality election = new Plurality(
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
   * elects that candidate as the sole winner with all ballots assigned to them.
   */
  @Test
  public void testRunElectionSingleCandidate() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "1",
        "1,1",
        "2,1",
        "3,1");
    
    Plurality election = new Plurality(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    
    assertEquals(1, winners.size());
    assertEquals("Alice", winners.get(0).getCandidateName());
    assertEquals(3, winners.get(0).getBallots().size());
  }
  
  /**
   * Tests that a candidate who receives no votes ends up with zero ballots assigned,
   * while the candidate with the most votes is correctly elected as the winner.
   */
  @Test
  public void testRunElectionZeroVotesForCandidate() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "1",
        "1,1",
        "2,1",
        "3,2");
    
    Plurality election = new Plurality(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    List<Candidate> losers = election.getLosers();
    
    assertEquals(1, winners.size());
    assertEquals("Alice", winners.get(0).getCandidateName());
    
    // Charlie should have 0 votes
    Candidate charlie = null;
    for (Candidate c : election.getCandidates()) {
      if (c.getCandidateName().equals("Charlie")) {
        charlie = c;
        break;
      }
    }
    assertNotNull(charlie);
    assertEquals(0, charlie.getBallots().size());
  }
  
  /**
   * Tests that each ballot is assigned to the correct candidate based on the
   * first-choice vote, verifying that Alice receives 2 ballots and Bob receives 1.
   */
  @Test
  public void testBallotAssignmentCorrect() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "1",
        "1,1",
        "2,2",
        "3,1");
    
    Plurality election = new Plurality(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    Candidate alice = null;
    Candidate bob = null;
    
    for (Candidate c : election.getCandidates()) {
      if (c.getCandidateName().equals("Alice")) {
        alice = c;
      } else if (c.getCandidateName().equals("Bob")) {
        bob = c;
      }
    }
    
    assertNotNull(alice);
    assertNotNull(bob);
    assertEquals(2, alice.getBallots().size());
    assertEquals(1, bob.getBallots().size());
  }
  
  /**
   * Tests that running the election twice does not accumulate ballot assignments
   * across runs, verifying that previous ballot assignments are cleared before
   * each execution so vote counts remain accurate.
   */
  @Test
  public void testMultipleRunsClearPreviousAssignments() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "1",
        "1,1",
        "2,2");
    
    Plurality election = new Plurality(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    // Run election twice
    election.runElection(true);
    election.runElection(true);
    
    // Vote counts should be the same (not doubled)
    Candidate alice = null;
    for (Candidate c : election.getCandidates()) {
      if (c.getCandidateName().equals("Alice")) {
        alice = c;
        break;
      }
    }
    
    assertNotNull(alice);
    assertEquals(1, alice.getBallots().size());
  }
  
  /**
   * Tests that no candidate appears in both the winners and losers lists after the
   * election completes, ensuring mutual exclusivity of the two result sets.
   */
  @Test
  public void testWinnersAndLosersNoOverlap() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "1",
        "1,1",
        "2,2",
        "3,3");
    
    Plurality election = new Plurality(
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
   * Tests that every candidate is accounted for after the election completes, meaning
   * the combined size of the winners and losers lists equals the total number of
   * candidates.
   */
  @Test
  public void testAllCandidatesAccountedFor() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C",
        "4,Diana,Party D");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "2",
        "1,1",
        "2,2",
        "3,3",
        "4,4");
    
    Plurality election = new Plurality(
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
   * Tests that the generated report output contains all key election information
   * including the results header, election type, all candidate names, and both the
   * elected and non-elected sections.
   */
  @Test
  public void testReportGeneration() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "1",
        "1,1",
        "2,2");
    
    Plurality election = new Plurality(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    String output = outContent.toString();
    
    // Verify console output contains key information
    assertTrue(output.contains("ELECTION RESULTS"));
    assertTrue(output.contains("Plurality"));
    assertTrue(output.contains("Alice"));
    assertTrue(output.contains("Bob"));
    assertTrue(output.contains("ELECTED"));
    assertTrue(output.contains("NON-ELECTED"));
  }
  
  /**
   * Tests that the generated report output includes vote count information for
   * candidates, confirming that per-candidate ballot totals are printed in the results.
   */
  @Test
  public void testReportContainsVoteCounts() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "1",
        "1,1",
        "2,1",
        "3,2");
    
    Plurality election = new Plurality(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    String output = outContent.toString();
    
    assertTrue(output.contains("2 votes") || output.contains("1 votes"));
  }
  
  /**
   * Tests that candidate CSV entries with leading and trailing whitespace are parsed
   * correctly, producing properly trimmed candidate ID, name, and party values.
   */
  @Test
  public void testParseCandidatesWithWhitespace() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "  1  ,  Alice  ,  Party A  ",
        "2,Bob,Party B");
    
    String ballotFile = createBallotFile("ballots.csv", "PV", "1", "1,1");
    
    Plurality election = new Plurality(
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
   * Tests that ballot CSV entries with leading and trailing whitespace around the
   * ballot ID and vote values are parsed correctly without error.
   */
  @Test
  public void testParseBallotsWithWhitespace() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv", "1,Alice,Party A");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "1",
        "  1  ,  1  ",
        "2,1");
    
    Plurality election = new Plurality(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    assertEquals(2, election.getBallots().size());
  }
  
  /**
   * Tests that empty lines interspersed in candidate and ballot CSV files are skipped
   * during parsing and do not affect the resulting object counts.
   */
  @Test
  public void testParseEmptyLines() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "",
        "2,Bob,Party B");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "1",
        "1,1",
        "",
        "2,2");
    
    Plurality election = new Plurality(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    assertEquals(2, election.getCandidates().size());
    assertEquals(2, election.getBallots().size());
  }
  
  /**
   * Tests that winners are ordered by descending vote count, verifying that Alice
   * (3 votes) appears before Bob (2 votes) in the winners list and that the ordering
   * invariant holds across all winners.
   */
  @Test
  public void testWinnersSortedByVoteCount() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");
    
    String ballotFile = createBallotFile("ballots.csv",
            "PV", "2",
        "1,1",
        "2,1",
        "3,1",
        "4,2",
        "5,2",
        "6,3");
    
    Plurality election = new Plurality(
        2,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );
    
    election.runElection(true);
    
    List<Candidate> winners = election.getWinners();
    
    // Winners should be sorted: Alice (3 votes), Bob (2 votes)
    assertEquals("Alice", winners.get(0).getCandidateName());
    assertEquals("Bob", winners.get(1).getCandidateName());
    assertTrue(winners.get(0).getBallots().size() >= winners.get(1).getBallots().size());
  }

  /**
   * Tests parsing the specific PV rank-column format required by the specification.
   * Ensures that empty commas are handled properly for single-choice voting.
   */
  @Test
  public void testParseBallotsPVSpecFormat() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
            "1,Alice,Party A",
            "2,Bob,Party B",
            "3,Charlie,Party C");

    // Using the exact format from the specification
    String ballotFile = createBallotFile("ballots.csv",
            "PV",
            "2",
            "ballotId,1,2,3",
            "1,1,,",
            "2,,1,",
            "3,,,1,"
    );

    Plurality election = new Plurality(
            2,
            Arrays.asList(ballotFile),
            Arrays.asList(candidateFile),
            true
    );

    List<Ballot> ballots = election.getBallots();
    assertEquals(3, ballots.size());

    // Verify first ballot mapped to Alice
    assertEquals(1, ballots.get(0).getBallotID());
    assertEquals(1, ballots.get(0).getVoteCount());
    assertEquals(1, ballots.get(0).getVote(0));

    // Verify second ballot mapped to Bob
    assertEquals(2, ballots.get(1).getBallotID());
    assertEquals(2, ballots.get(1).getVoteCount());
    assertEquals(2, ballots.get(1).getVote(0));

    // Verify third ballot mapped to Charlie
    assertEquals(3, ballots.get(2).getBallotID());
    assertEquals(3, ballots.get(2).getVoteCount());
    assertEquals(3, ballots.get(2).getVote(0));
  }
}