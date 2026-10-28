package electionsystem;
import java.util.List;

/**
 * Ballot implementation.
 * Used in elections, with each ballot containing a list of votes and a unique identifier.
 * Written by Finn Gidden
 */
public class Ballot {
  private int ballotID;
  private List<Integer> votes;

  /**
   * Constructor for Ballot class
   * @param id The unique ballot identifier
   * @param votes Ordered list of candidate IDs representing voter's ranked preferences
   */
  public Ballot(int id, List<Integer> votes){
    this.ballotID = id;
    this.votes = votes;
  }

  /**
   * Returns the candidate ID at the given rank position
   * @param rank The zero-based rank position in the votes list
   * @return The candidate ID at the specified rank
   */
  public int getVote(int rank) {
    return this.votes.get(rank);
  }
  
  /**
   * Returns the unique ballot identifier
   * @return The ballot ID
   */
  public int getBallotID() {
    return this.ballotID;
  }

  /**
   * Returns the length of the ballot's vote list
   * @return The ballot's vote list length
   */
  public int getVoteCount() {
    return this.votes.size();
  }
}