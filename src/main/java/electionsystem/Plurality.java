package electionsystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Plurality election implementation.
 * Uses single-choice voting with percentage-based seat allocation.
 * Written by Finn Gidden
 */
public class Plurality extends Election {
  
  /**
   * Constructor for Plurality election
   * @param seats Number of seats to fill
   * @param ballotFiles List of ballot file names
   * @param candidateFiles List of candidate file names  
   * @param isTest Flag indicating test mode
   */
  public Plurality(int seats, List<String> ballotFiles, List<String> candidateFiles, boolean isTest) {
    super();
    
    this.numSeats = seats;
    this.electionDate = new Date();
    this.isTest = isTest;
    
    // Parse candidate files
    for (String candidateFile : candidateFiles) {
      parseCandidatesFromFile(candidateFile);
    }
    
    // Parse ballot files
    parseBallotsFromFiles(ballotFiles);
  }

  /**
   * Executes the plurality election algorithm
   * @param isTest If true, runs in test mode
   */
  @Override
  public void runElection(boolean isTest) {
    this.isTest = isTest;
    
    // Clear previous ballot assignments
    for (Candidate candidate : this.candidates) {
      candidate.getBallots().clear();
    }
    
    // Assign ballots to candidates based on first-choice vote
    for (Ballot ballot : this.ballots) {
      if (ballot.getVoteCount() == 0) continue;
      int candidateID = ballot.getVote(0);
      
      // Find candidate with this ID and add ballot
      for (Candidate candidate : this.candidates) {
        if (candidate.getCandidateID() == candidateID) {
          candidate.addBallot(ballot);
          break;
        }
      }
    }
    
    // Save final vote counts before any modifications
    for (Candidate candidate : this.candidates) {
      candidate.setFinalVoteCount(candidate.getBallots().size());
    }

    // Sort candidates by vote count in descending order (most votes first)
    Collections.sort(this.candidates, new Comparator<Candidate>() {
      @Override
      public int compare(Candidate c1, Candidate c2) {
        return Integer.compare(c2.getFinalVoteCount(), c1.getFinalVoteCount());
      }
    });
    
    // Select winners
    int i = 0;
    while (this.winners.size() < this.numSeats && i < this.candidates.size()) {
      Candidate current = this.candidates.get(i);
      
      // Check if we need to handle a tie for the last seat
      if (this.winners.size() == this.numSeats - 1 && i + 1 < this.candidates.size()) {
        Candidate next = this.candidates.get(i + 1);
        
        // If tied for last seat, use tiebreak
        if (current.getBallots().size() == next.getBallots().size()) {
          Candidate winner = tiebreak(current, next);
          this.winners.add(winner);
          
          // Add both to losers (winner will be removed from losers later)
          Candidate loser = (winner == current) ? next : current;
          
          // Mark which one won and which one lost
          if (winner == current) {
            // next is the loser, current is winner
            i++; // Move to next
            // Add next to losers
            this.losers.add(next);
            i++; // Skip the loser
          } else {
            // current is the loser, next is winner  
            this.losers.add(current);
            i++; // Skip the loser
            i++; // Move past the winner
          }
          continue;
        }
      }
      
      // No tie, just add current candidate as winner
      this.winners.add(current);
      i++;
    }
    
    // Add remaining candidates to losers
    while (i < this.candidates.size()) {
      this.losers.add(this.candidates.get(i));
      i++;
    }
    
    // Generate and display results
    generateReport();
  }

  /**
   * Resolves a tie between two candidates using a fair coin toss
   * @param c1 First tied candidate
   * @param c2 Second tied candidate
   * @return The winning candidate
   */
  private Candidate tiebreak(Candidate c1, Candidate c2) {
    Random random = new Random();
    boolean coinToss = random.nextBoolean();
    
    if (this.isTest) {
      // In test mode, print tiebreak information for verification
      System.out.println("Tiebreak between " + c1.getCandidateName() + 
                        " and " + c2.getCandidateName());
    }
    
    return coinToss ? c1 : c2;
  }
  
  /**
   * Returns the election type as a string
   * @return "Plurality"
   */
  @Override
  protected String getType() {
    return "Plurality";
  }

  /**
   * Displays the plurality election results summary to the console.
   * Overrides the base implementation to include each candidate's
   * percentage share of the total ballots cast.
   * Saves the report file via the parent saveReportToFile().
   */
  @Override
  public void generateReport() {
    int totalBallots = this.ballots.size();

    System.out.println("\n========== ELECTION RESULTS ==========");
    System.out.println("Election Type: " + getType());
    System.out.println("Number of Candidates: " + candidates.size());
    System.out.println("Number of Seats: " + numSeats);
    System.out.println("Total Ballots: " + totalBallots);

    System.out.println("\n--- ELECTED ---");
    for (Candidate c : winners) {
      double pct = (totalBallots > 0)
          ? (double) c.getFinalVoteCount() / totalBallots * 100 : 0.0;
      System.out.printf("  %s - %d votes (%.2f%%)%n",
          c.getCandidateName(), c.getFinalVoteCount(), pct);
    }

    System.out.println("\n--- NON-ELECTED ---");
    for (int i = losers.size() - 1; i >= 0; i--) {
      Candidate c = losers.get(i);
      double pct = (totalBallots > 0)
          ? (double) c.getFinalVoteCount() / totalBallots * 100 : 0.0;
      System.out.printf("  %s - %d votes (%.2f%%)%n",
          c.getCandidateName(), c.getFinalVoteCount(), pct);
    }
    System.out.println("======================================\n");

    // Save report to file using parent implementation
    saveReportToFile();
  }
}