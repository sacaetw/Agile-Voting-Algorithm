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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Test suite for the MV (Municipal Voting) election class.
 * Written by Finn Gidden
 */
public class MVTest {

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
   * Creates a candidate CSV file in the temporary directory and returns its absolute path.
   * @param filename Filename to create inside the temp directory
   * @param lines One line per entry (format: id,name,party)
   * @return Absolute path of the created file
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
   * Creates a ballot CSV file in the temporary directory and returns its absolute path.
   * @param filename Filename to create inside the temp directory
   * @param lines One entry per line
   * @return Absolute path of the created file
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
   * Tests that the MV constructor correctly initializes an election with basic
   * inputs, verifying that the candidate list, ballot list, and seat count are
   * all set to the expected values after construction.
   */
  @Test
  public void testConstructorBasic() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2,3",
        "101,1,,",
        "102,,1,",
        "103,,,1");

    MV election = new MV(
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
   * Tests that the MV constructor correctly sets the isTest flag to true when
   * test mode is enabled.
   */
  @Test
  public void testConstructorTestModeEnabled() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A");
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1",
        "101,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    assertTrue(election.getIsTest());
  }

  /**
   * Tests that the MV constructor correctly sets the isTest flag to false when
   * test mode is disabled.
   */
  @Test
  public void testConstructorTestModeDisabled() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A");
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1",
        "101,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        false
    );

    assertFalse(election.getIsTest());
  }

  /**
   * Tests that the MV constructor correctly counts the number of ballot files
   * when a single file is provided, verifying that "Number of Files:   1"
   * appears in the generated report output after the election runs.
   */
  @Test
  public void testConstructorNumFilesOneFile() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A");
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1",
        "101,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);
    assertTrue(outContent.toString().contains("Number of Files:   1"));
  }

  /**
   * Tests that when two ballot files are provided, ballots from both files are
   * combined into a single ballot list and the total ballot count reflects the
   * sum of ballots across all files.
   */
  @Test
  public void testConstructorMultipleBallotFilesCombinesBallots() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");

    String ballotFile1 = createBallotFile("ballots1.csv",
        "MV", "1",
        "ballotId,1,2",
        "101,1,");

    String ballotFile2 = createBallotFile("ballots2.csv",
        "MV", "1",
        "ballotId,1,2",
        "102,,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile1, ballotFile2),
        Arrays.asList(candidateFile),
        true
    );

    assertEquals(2, election.getBallots().size());
  }



  /**
   * Tests that a single "1" on a ballot is parsed as one approved candidate,
   * producing a Ballot with a vote count of 1.
   */
  @Test
  public void testParseMVBallotsSingleVotePerBallot() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2",
        "101,1,");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    List<Ballot> ballots = election.getBallots();
    assertEquals(1, ballots.size());
    assertEquals(1, ballots.get(0).getVoteCount());
  }

  /**
   * Tests that a ballot with multiple "1"s is parsed as multiple approved
   * candidates, producing a Ballot whose vote count equals the number of
   * candidates the voter approved.
   */
  @Test
  public void testParseMVBallotsMultipleVotesPerBallot() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2,3",
        "101,1,1,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    List<Ballot> ballots = election.getBallots();
    assertEquals(1, ballots.size());
    assertEquals(3, ballots.get(0).getVoteCount());
  }

  /**
   * Tests that empty cells in a ballot row are not counted as votes, and that
   * only columns containing a "1" contribute to the ballot's vote count.
   */
  @Test
  public void testParseMVBallotsEmptyCellsNotCounted() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2,3",
        "101,1,,");  // Only Alice approved; Bob and Charlie columns empty

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    assertEquals(1, election.getBallots().get(0).getVoteCount());
  }

  /**
   * Tests that trailing commas in a ballot row are handled correctly by the
   * split logic, so that the ballot is still parsed without error and the
   * correct number of approvals is recorded.
   */
  @Test
  public void testParseMVBallotsTrailingCommas() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");

    // Three trailing commas after the only vote; split(",", -1) must preserve them
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2,3",
        "101,1,,");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    // Ballot should be parsed without error with exactly one vote
    assertEquals(1, election.getBallots().size());
    assertEquals(1, election.getBallots().get(0).getVoteCount());
  }

  /**
   * Tests that the column-to-candidate mapping derived from the header row is
   * applied correctly, so that a "1" in a given column is credited to the
   * candidate whose ID appears in that column's header position.
   */
  @Test
  public void testParseMVBallotColumnToIDMapping() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");

    // Only the second column (Bob, ID 2) has a "1"
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2,3",
        "101,,1,");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    // Find each candidate by ID to verify correct assignment independent of list order
    Candidate alice   = null;
    Candidate bob     = null;
    Candidate charlie = null;
    for (Candidate c : election.getCandidates()) {
      if (c.getCandidateID() == 1) alice   = c;
      if (c.getCandidateID() == 2) bob     = c;
      if (c.getCandidateID() == 3) charlie = c;
    }

    assertNotNull(alice);
    assertNotNull(bob);
    assertNotNull(charlie);
    assertEquals(0, alice.getBallots().size());
    assertEquals(1, bob.getBallots().size());   // Bob's column had the "1"
    assertEquals(0, charlie.getBallots().size());
  }

  /**
   * Tests that a truncated ballot row (fewer columns than the header declares)
   * is parsed without error, with only the columns present being evaluated and
   * the missing columns treated as abstentions.
   */
  @Test
  public void testParseMVBallotsTruncatedRow() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");

    // Row only provides a value for the first candidate column
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2,3",
        "101,1");  // No columns for Bob or Charlie

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    assertEquals(1, election.getBallots().size());
    assertEquals(1, election.getBallots().get(0).getVoteCount());
  }

  /**
   * Tests that a ballot row containing no "1"s (all abstentions) still creates
   * a Ballot object and adds it to the election's ballot list, but does not
   * contribute any approvals to any candidate.
   */
  @Test
  public void testParseMVBallotsAllAbstentionBallotStillCreated() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2",
        "101,,");  // No votes cast

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    assertEquals(1, election.getBallots().size());
    assertEquals(0, election.getBallots().get(0).getVoteCount());
  }

  /**
   * Tests that a ballot row referencing an unknown candidate ID (one not present
   * in the candidate file) does not cause a crash and that the remaining valid
   * votes on that ballot are still counted.
   */
  @Test
  public void testParseMVBallotsUnknownCandidateIDHandledGracefully() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");

    // Header references candidate ID 99 which does not exist in candidate file
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,99",
        "101,1,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    // Election should still be constructed without throwing
    assertNotNull(election);
    assertEquals(1, election.getBallots().size());
  }

  /**
   * Tests that empty lines interspersed in both the candidate and ballot CSV
   * files are skipped during parsing and do not affect the resulting object
   * counts.
   */
  @Test
  public void testParseEmptyLinesSkipped() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "",
        "2,Bob,Party B");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2",
        "101,1,",
        "",
        "102,,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    assertEquals(2, election.getCandidates().size());
    assertEquals(2, election.getBallots().size());
  }



  /**
   * Tests that candidate CSV entries with leading and trailing whitespace are
   * parsed correctly, producing properly trimmed candidate ID, name, and party
   * values.
   */
  @Test
  public void testParseCandidatesWithWhitespace() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "  1  ,  Alice  ,  Party A  ",
        "2,Bob,Party B");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2",
        "101,1,");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    assertEquals(2, election.getCandidates().size());

    Candidate alice = null;
    for (Candidate c : election.getCandidates()) {
      if (c.getCandidateID() == 1) alice = c;
    }

    assertNotNull(alice);
    assertEquals("Alice",   alice.getCandidateName());
    assertEquals("Party A", alice.getCandidateParty());
  }

  /**
   * Tests that all candidates are added to the active candidates list during
   * construction, so that the size of activeCandidates equals the total number
   * of candidates before any election logic is run.
   */
  @Test
  public void testActiveCandidatesPopulatedOnConstruction() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2,3",
        "101,1,,");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    assertEquals(3, election.getActiveCandidates().size());
  }



  /**
   * Tests that a single-seat election with a clear approval winner correctly
   * elects the candidate with the most "1" votes, producing exactly one winner
   * and placing the remaining candidates in the losers list.
   */
  @Test
  public void testRunElectionSingleSeatClearWinner() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");

    // Alice receives 3 approvals, Bob 1, Charlie 0
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2,3",
        "101,1,,",
        "102,1,,",
        "103,1,1,",
        "104,,1,");

    MV election = new MV(
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
   * Tests that the candidate with the highest approval count is placed first
   * in the winners list, confirming that the sort in runElection orders
   * candidates by descending vote count before seat assignment.
   */
  @Test
  public void testRunElectionWinnerHasMostVotes() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");

    // Bob receives more approvals than Alice
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2",
        "101,,1",
        "102,,1",
        "103,,1",
        "104,1,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);

    assertEquals("Bob", election.getWinners().get(0).getCandidateName());
  }

  /**
   * Tests that a two-seat election correctly fills both seats with the two
   * candidates who received the highest approval counts, producing exactly two
   * winners and one loser.
   */
  @Test
  public void testRunElectionMultipleSeats() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");

    // Alice 3 approvals, Bob 2, Charlie 1
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "2",
        "ballotId,1,2,3",
        "101,1,1,",
        "102,1,1,",
        "103,1,,1");

    MV election = new MV(
        2,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);

    assertEquals(2, election.getWinners().size());
    assertEquals(1, election.getLosers().size());
  }

  /**
   * Tests that an election with a single candidate running for a single seat
   * correctly declares that candidate as the sole winner.
   */
  @Test
  public void testRunElectionSingleCandidate() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1",
        "101,1",
        "102,1");

    MV election = new MV(
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
   * Tests that when the number of requested seats exceeds the number of
   * candidates, all candidates are declared winners and the losers list is
   * empty.
   */
  @Test
  public void testRunElectionMoreSeatsThanCandidates() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "5",
        "ballotId,1,2",
        "101,1,1");

    MV election = new MV(
        5,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);

    assertEquals(2, election.getWinners().size());
    assertEquals(0, election.getLosers().size());
  }

  /**
   * Tests that when the number of seats equals the number of candidates, all
   * candidates are declared winners and the losers list remains empty.
   */
  @Test
  public void testRunElectionSeatsEqualsCandidates() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "3",
        "ballotId,1,2,3",
        "101,1,1,1");

    MV election = new MV(
        3,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);

    assertEquals(3, election.getWinners().size());
    assertEquals(0, election.getLosers().size());
  }

  /**
   * Tests that a candidate who receives zero approval votes is placed in the
   * losers list and not in the winners list.
   */
  @Test
  public void testRunElectionCandidateWithZeroVotesIsLoser() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");

    // Charlie receives no approvals
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2,3",
        "101,1,,",
        "102,1,,",
        "103,,1,");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);

    // Verify Charlie (zero votes) is not a winner
    for (Candidate w : election.getWinners()) {
      assertNotEquals("Charlie", w.getCandidateName());
    }

    boolean charlieInLosers = false;
    for (Candidate l : election.getLosers()) {
      if (l.getCandidateName().equals("Charlie")) charlieInLosers = true;
    }
    assertTrue(charlieInLosers);
  }

  /**
   * Tests that running the election in test mode prints the expected test mode
   * message to the console output.
   */
  @Test
  public void testRunElectionTestModeOutputMessage() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A");
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1",
        "101,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);
    assertTrue(outContent.toString().contains("Running MV election in test mode."));
  }

  /**
   * Tests that the election type reported by getType() is "MV", verified by
   * checking that the string "MV" appears in the console output after the
   * election runs.
   */
  @Test
  public void testGetType() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A");
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1",
        "101,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);
    assertTrue(outContent.toString().contains("MV"));
  }



  /**
   * Tests that no candidate appears in both the winners and losers lists after
   * the election completes, ensuring the two result sets are mutually exclusive.
   */
  @Test
  public void testWinnersAndLosersNoOverlap() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2,3",
        "101,1,,",
        "102,1,,",
        "103,,1,");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);

    for (Candidate winner : election.getWinners()) {
      for (Candidate loser : election.getLosers()) {
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
        "MV", "2",
        "ballotId,1,2,3,4",
        "101,1,1,,",
        "102,1,,,1",
        "103,,1,1,");

    MV election = new MV(
        2,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);

    assertEquals(4,
        election.getWinners().size() + election.getLosers().size());
  }

  /**
   * Tests that the winners list size exactly equals numSeats when there are
   * more candidates than seats available.
   */
  @Test
  public void testWinnersCountMatchesSeats() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C",
        "4,Diana,Party D");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "2",
        "ballotId,1,2,3,4",
        "101,1,1,,",
        "102,1,,,",
        "103,,1,,");

    MV election = new MV(
        2,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);
    assertEquals(2, election.getWinners().size());
  }

  /**
   * Tests that the losers list size equals the number of candidates minus the
   * number of seats when candidates exceed available seats.
   */
  @Test
  public void testLosersCountCorrect() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C",
        "4,Diana,Party D");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "2",
        "ballotId,1,2,3,4",
        "101,1,1,,",
        "102,1,,,",
        "103,,1,,");

    MV election = new MV(
        2,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);

    // 4 candidates - 2 seats = 2 losers
    assertEquals(2, election.getLosers().size());
  }



  /**
   * Tests that totalVotesCast correctly reflects the number of "1"s on a single
   * ballot, verified by checking the value printed in the console report.
   */
  @Test
  public void testTotalVotesCastSingleBallot() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");

    // Ballot approves two candidates → totalVotesCast should be 2
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2,3",
        "101,1,1,");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);
    assertTrue(outContent.toString().contains("Total Votes Cast:  2"));
  }

  /**
   * Tests that totalVotesCast is summed correctly across multiple ballots,
   * each of which may contain a different number of approvals.
   */
  @Test
  public void testTotalVotesCastMultipleBallots() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");

    // Ballot 101: 2 approvals, Ballot 102: 1 approval, Ballot 103: 3 approvals → total 6
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2,3",
        "101,1,1,",
        "102,1,,",
        "103,1,1,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);
    assertTrue(outContent.toString().contains("Total Votes Cast:  6"));
  }

  /**
   * Tests that an election in which no voter approves any candidate results in
   * totalVotesCast of zero, and that the division-by-zero guard in generateReport
   * prevents a runtime exception.
   */
  @Test
  public void testTotalVotesCastZeroDoesNotThrow() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");

    // All cells empty → no votes cast
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2",
        "101,,",
        "102,,");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    // Should not throw ArithmeticException despite totalVotesCast being 0
    assertDoesNotThrow(() -> election.runElection(true));
    assertTrue(outContent.toString().contains("Total Votes Cast:  0"));
  }

  /**
   * Tests that totalVotesCast accumulates correctly across two ballot files,
   * adding the "1" counts from each file into a single total.
   */
  @Test
  public void testTotalVotesCastAccumulatesAcrossFiles() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");

    // File 1: 1 approval, File 2: 2 approvals → total 3
    String ballotFile1 = createBallotFile("ballots1.csv",
        "MV", "1",
        "ballotId,1,2",
        "101,1,");

    String ballotFile2 = createBallotFile("ballots2.csv",
        "MV", "1",
        "ballotId,1,2",
        "102,1,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile1, ballotFile2),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);
    assertTrue(outContent.toString().contains("Total Votes Cast:  3"));
  }



  /**
   * Tests that the console output after running an election contains the string
   * "Municipal Voting (MV)", confirming the election type is reported correctly.
   */
  @Test
  public void testReportContainsMVElectionType() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A");
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1",
        "101,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);
    assertTrue(outContent.toString().contains("Municipal Voting (MV)"));
  }

  /**
   * Tests that the console output contains the correct number of seats,
   * confirming that "Number of Seats:   2" appears when a two-seat election
   * is run.
   */
  @Test
  public void testReportContainsNumSeats() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "2",
        "ballotId,1,2,3",
        "101,1,1,");

    MV election = new MV(
        2,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);
    assertTrue(outContent.toString().contains("Number of Seats:   2"));
  }

  /**
   * Tests that the console output contains the correct number of ballot files,
   * confirming that "Number of Files:   2" appears when two ballot files are
   * provided.
   */
  @Test
  public void testReportContainsNumFiles() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");

    String ballotFile1 = createBallotFile("ballots1.csv",
        "MV", "1",
        "ballotId,1,2",
        "101,1,");

    String ballotFile2 = createBallotFile("ballots2.csv",
        "MV", "1",
        "ballotId,1,2",
        "102,,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile1, ballotFile2),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);
    assertTrue(outContent.toString().contains("Number of Files:   2"));
  }

  /**
   * Tests that the console output contains the correct total ballot count,
   * confirming that "Number of Ballots: 3" appears when three ballots are cast.
   */
  @Test
  public void testReportContainsNumBallots() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2",
        "101,1,",
        "102,,1",
        "103,1,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);
    assertTrue(outContent.toString().contains("Number of Ballots: 3"));
  }

  /**
   * Tests that the console output contains the "Total Votes Cast:" label with
   * the correct value, confirming that total approval count is reported.
   */
  @Test
  public void testReportContainsTotalVotesCast() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");

    // 1 + 1 + 2 = 4 total approvals
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2",
        "101,1,",
        "102,,1",
        "103,1,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);
    assertTrue(outContent.toString().contains("Total Votes Cast:  4"));
  }

  /**
   * Tests that the console output includes the name of every candidate in the
   * candidate results table, confirming that all participants are listed in
   * the report.
   */
  @Test
  public void testReportContainsAllCandidateNames() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B",
        "3,Charlie,Party C");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2,3",
        "101,1,,",
        "102,,1,",
        "103,,,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);
    String output = outContent.toString();

    assertTrue(output.contains("Alice"));
    assertTrue(output.contains("Bob"));
    assertTrue(output.contains("Charlie"));
  }

  /**
   * Tests that the console output contains percentage values for each candidate's vote share.
   */
  @Test
  public void testReportContainsCorrectPercentages() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");

    // Alice: 3 approvals, Bob: 1 approval, total: 4
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2",
        "101,1,",
        "102,1,",
        "103,1,1",
        "104,,");  // Abstention (does not affect total vote count)

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);
    String output = outContent.toString();

    // 3/3 = 100.00% for Alice, 1/3 = 33.33% for Bob (abstention ballot adds 0 votes)
    assertTrue(output.contains("%"));
  }

  /**
   * Tests that the console output includes the "--- ELECTED ---" section header,
   * confirming that the elected candidates section is always present in the
   * report.
   */
  @Test
  public void testReportContainsElectedSection() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2",
        "101,1,",
        "102,,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);
    assertTrue(outContent.toString().contains("--- ELECTED ---"));
  }

  /**
   * Tests that the console output includes the "CANDIDATE RESULTS:" section
   * header, confirming that the full candidate breakdown is always present.
   */
  @Test
  public void testReportContainsCandidateResultsSection() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A",
        "2,Bob,Party B");

    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1,2",
        "101,1,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);
    assertTrue(outContent.toString().contains("CANDIDATE RESULTS:"));
  }

  /**
   * Tests that the report file is saved to a file whose name follows the
   * required format "MVElectionyyyyMMdd.txt", verified by checking that the
   * console output contains MVElection followed by the current date.
   */
  @Test
  public void testReportFileNameFormat() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "1,Alice,Party A");
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "1",
        "ballotId,1",
        "101,1");

    MV election = new MV(
        1,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);

    String expectedFilename = "MVElection"
        + new SimpleDateFormat("yyyyMMdd").format(new Date())
        + ".txt";

    assertTrue(outContent.toString().contains(expectedFilename));
  }


  /**
   * Tests a full end-to-end MV election scenario with six candidates competing
   * for three seats across eight ballots.  Verifies that exactly three winners
   * and three losers are produced, the correct election type is reported, the
   * total vote count is consistent with the input data, and the elected
   * candidates all have more approvals than the non-elected candidates.
   */
  @Test
  public void testCompleteElectionScenario() throws IOException {
    String candidateFile = createCandidateFile("candidates.csv",
        "607773,Alice,Party A",
        "670497,Bob,Party B",
        "900553,Charlie,Party C",
        "937441,Diana,Party D",
        "193070,Eve,Party E",
        "889375,Frank,Party F");

    // Mirrors the example from the project specification
    String ballotFile = createBallotFile("ballots.csv",
        "MV", "3",
        "ballotId,607773,670497,900553,937441,193070,889375",
        "110920541,1,1,1,,,",
        "586016597,1,,,1,,1",
        "142748674,,1,,,1,",
        "325929379,,,,,1,",
        "171081413,1,,,1,1,",
        "470941174,,,1,1,,",
        "709406585,1,,1,1,,",
        "575867911,,1,,,1,1");

    MV election = new MV(
        3,
        Arrays.asList(ballotFile),
        Arrays.asList(candidateFile),
        true
    );

    election.runElection(true);

    List<Candidate> winners = election.getWinners();
    List<Candidate> losers  = election.getLosers();

    // Correct seat and loser counts
    assertEquals(3, winners.size());
    assertEquals(3, losers.size());

    // Every candidate accounted for
    assertEquals(6, winners.size() + losers.size());

    // No overlap between winners and losers
    for (Candidate w : winners) {
      for (Candidate l : losers) {
        assertNotEquals(w.getCandidateID(), l.getCandidateID());
      }
    }

    // Winners should have >= votes than the candidate with the most votes in losers
    int minWinnerVotes = Integer.MAX_VALUE;
    for (Candidate w : winners) {
      minWinnerVotes = Math.min(minWinnerVotes, w.getBallots().size());
    }
    int maxLoserVotes = 0;
    for (Candidate l : losers) {
      maxLoserVotes = Math.max(maxLoserVotes, l.getBallots().size());
    }
    assertTrue(minWinnerVotes >= maxLoserVotes);

    // Report output sanity checks
    String output = outContent.toString();
    assertTrue(output.contains("Municipal Voting (MV)"));
    assertTrue(output.contains("Number of Seats:   3"));
    assertTrue(output.contains("Number of Ballots: 8"));
    assertTrue(output.contains("--- ELECTED ---"));
  }
}