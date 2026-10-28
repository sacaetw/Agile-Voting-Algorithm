package electionsystem;
import java.util.List;
import java.util.ArrayList;

/**
 * Candidate implementation.
 * Used in elections, with each candidate tracking their list of winning ballots until eliminated.
 * Each candidate has a name, party, and unique numeric identifier.
 * Written by Finn Gidden
 */
public class Candidate{
  private int candidateID;
  private String name;
  private String party;
  private boolean isEliminated;
  private List<Integer> ballots;
  private int finalVoteCount;

  /**
   * Constructor for Candidate class
   * @param id The unique candidate identifier
   * @param name The candidate's full name
   * @param party The candidate's party affiliation
   */
  public Candidate(int id, String name, String party) {
    this.candidateID = id;
    this.name = name;
    this.party = party;
    this.isEliminated = false;
    this.ballots = new ArrayList<>();
    this.finalVoteCount = 0;
  }

  /**
   * Returns the unique candidate identifier
   * @return The candidate ID
   */
  public int getCandidateID() {
    return this.candidateID;
  }

  /**
   * Returns the candidate's full name
   * @return The candidate's name
   */
  public String getCandidateName() {
    return this.name;
  }

  /**
   * Returns the candidate's party affiliation
   * @return The candidate's party
   */
  public String getCandidateParty() {
    return this.party;
  }

  /**
   * Returns whether the candidate has been eliminated
   * @return True if eliminated, false otherwise
   */
  public boolean getIsEliminated() {
    return this.isEliminated;
  }

  /**
   * Returns the list of ballot IDs currently assigned to this candidate
   * @return List of ballot IDs
   */
  public List<Integer> getBallots() {
    return this.ballots;
  }

  /**
   * Adds a ballot to this candidate's ballot list
   * @param ballot The Ballot object to add
   */
  public void addBallot(Ballot ballot) {
    this.ballots.add(ballot.getBallotID());
  }

  /**
   * Sets the final vote count for this candidate.
   * This preserves the vote count after ballots are cleared during election processing.
   * @param count The final number of votes this candidate received
   */
  public void setFinalVoteCount(int count) {
    this.finalVoteCount = count;
  }

  /**
   * Returns the final vote count for this candidate.
   * This count is preserved even after ballots are cleared.
   * @return The final number of votes
   */
  public int getFinalVoteCount() {
    return this.finalVoteCount;
  }
}