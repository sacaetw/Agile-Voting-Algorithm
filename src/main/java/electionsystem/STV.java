package electionsystem;

import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Single Transferable Vote (STV) election implementation.
 * Uses ranked-choice voting with quota-based seat allocation.
 * Written by Finn Gidden
 */
public class STV extends Election {
  
  private int droopQuota;
  private int round;
  private List<Integer> shuffledBallotOrder;
  private List<Map<Integer, List<Integer>>> roundSnapshots;
  private List<Ballot> invalidBallots;
  
  /**
   * Constructor for STV election
   * @param seats Number of seats to fill
   * @param ballotFiles List of ballot file names
   * @param candidateFiles List of candidate file names
   * @param isTest Flag indicating test mode (disables ballot shuffling)
   */
  public STV(int seats, List<String> ballotFiles, List<String> candidateFiles, boolean isTest) {
    super();

    this.numSeats = seats;
    this.electionDate = new Date();
    this.isTest = isTest;
    this.round = 0;
    this.roundSnapshots = new ArrayList<>();
    this.invalidBallots = new ArrayList<>(); 
    
    // Parse candidate files
    for (String candidateFile : candidateFiles) {
      parseCandidatesFromFile(candidateFile);
    }
    
    // Parse ballot files
    parseBallotsFromFiles(ballotFiles);

    // Find ballots with less than 60% of candidates ranked
    int totalCandidates = this.candidates.size();
    int minRequiredRankings = (int) Math.ceil(totalCandidates * 0.6);
    
    this.invalidBallots = new ArrayList<>();
    
    Iterator<Ballot> iterator = this.ballots.iterator();
    while (iterator.hasNext()) {
        Ballot ballot = iterator.next();
        if (ballot.getVoteCount() < minRequiredRankings) {
            this.invalidBallots.add(ballot);
            iterator.remove(); // Remove from parent's 'ballots' list
        }
    }
    // Shuffle ballots unless in test mode
    if (!isTest) {
      shuffleBallots(this.ballots);
    }

    // Record ballot order after shuffle (or original order in test mode) for audit report
    this.shuffledBallotOrder = new ArrayList<>();
    for (Ballot b : this.ballots) {
      this.shuffledBallotOrder.add(b.getBallotID());
    }
  }
  
  /**
   * Computes and stores the Droop quota
   * Formula: floor(totalBallots / (numSeats + 1)) + 1
   * @param ballots Total number of ballots cast
   * @param seats Number of seats to fill
   */
  private void setQuota(int ballots, int seats) {
    this.droopQuota = (ballots / (seats + 1)) + 1;
  }
  
  /**
   * Randomly reorders the ballot list before the first round
   * @param ballots The ballot list to shuffle
   */
  private void shuffleBallots(List<Ballot> ballots) {
    Collections.shuffle(ballots);
  }
  
  /**
   * Returns the current round number
   * @return Current round
   */
  private int getRound() {
    return this.round;
  }

  /**
   * Returns the list of ballots that have less than 60% of candidates ranked
   * @return List of invalid ballots
   */
  public List<Ballot> getInvalidBallots() {
    return invalidBallots;
  }

  /**
   * Takes a snapshot of each active candidate's current ballot assignments
   * and appends it to roundSnapshots for use in the audit report.
   * Called once per round after ballot redistribution is complete.
   */
  private void snapshotRound() {
    Map<Integer, List<Integer>> snapshot = new LinkedHashMap<>();
    for (Candidate candidate : this.activeCandidates) {
      snapshot.put(candidate.getCandidateID(), new ArrayList<>(candidate.getBallots()));
    }
    this.roundSnapshots.add(snapshot);
  }
  
  /**
   * Executes the STV election algorithm
   * @param isTest If true, runs in test mode
   */
  @Override
  public void runElection(boolean isTest) {
    this.isTest = isTest;

    if (this.ballots.isEmpty()) {
        System.out.println("No valid ballots to process. Election cannot proceed.");
        generateReport();
        return;
    }
    
    // Calculate the Droop quota
    setQuota(this.ballots.size(), this.numSeats);
    
    if (isTest) {
      System.out.println("Running STV election in test mode");
      System.out.println("Droop Quota: " + this.droopQuota);
    }
    
    // Main election loop, continues until all seats are filled
    while (true) {
      this.round++;
      
      if (isTest) {
        System.out.println("\n--- Round " + this.round + " ---");
      }
      
      // Check if election is complete
      if (this.winners.size() == this.numSeats) {
        for (Candidate c : this.activeCandidates) {
          c.setFinalVoteCount(c.getBallots().size());
        }
        this.losers.addAll(this.activeCandidates);
        this.activeCandidates.clear();

        generateReport();
        return;
      }
      
      // Check if we need to fill remaining seats from losers
      if (this.winners.size() != this.numSeats && this.activeCandidates.isEmpty()) {
        // Fill remaining seats from back of losers list
        while (this.winners.size() < this.numSeats && !this.losers.isEmpty()) {
          Candidate c = this.losers.remove(this.losers.size() - 1);
          c.setFinalVoteCount(c.getBallots().size());
          this.winners.add(c);
        }
        generateReport();
        return;
      }
      
      // Clear all ballot assignments from previous round
      for (Candidate candidate : this.candidates) {
        candidate.getBallots().clear();
      }
      
      // Redistribute ballots to highest-ranked active candidate
      for (Ballot ballot : this.ballots) {
        Candidate validCandidate = null;
        
        // Go through voter's ranked choices
        for (int rank = 0; rank < ballot.getVoteCount(); rank++) {
          try {
            int candidateID = ballot.getVote(rank);
            
            // Find the candidate with this ID
            Candidate candidate = findCandidateByID(candidateID);
            
            if (candidate != null) {
              // Check if candidate is still active (not eliminated or elected)
              if (this.activeCandidates.contains(candidate) && 
                  !this.losers.contains(candidate)) {
                validCandidate = candidate;
                break; // Found valid candidate for this ballot
              }
            }
          } catch (IndexOutOfBoundsException e) {
            // Ballot doesn't have this many rankings, try next
            break;
          }
        }
        
        // Assign ballot to the valid candidate found
        if (validCandidate != null) {
          validCandidate.addBallot(ballot);
        }
      }

      // Record ballot distribution for this round before any clearing occurs
      snapshotRound();
      
      // Check if any candidates met the quota and should be elected
      boolean someoneElected = false;
      List<Candidate> toRemove = new ArrayList<>();
      
      for (Candidate candidate : this.activeCandidates) {
        if (candidate.getBallots().size() >= this.droopQuota) {
          candidate.setFinalVoteCount(candidate.getBallots().size());
          
          if (isTest) {
            System.out.println("Elected: " + candidate.getCandidateName() + 
                             " with " + candidate.getBallots().size() + " votes");
          }
          
          this.winners.add(candidate);
          toRemove.add(candidate);
          candidate.getBallots().clear();
          someoneElected = true;

          // In STV, typically only one candidate is elected per round
          break;
        }
      }
      
      // Remove elected candidates from active list
      for (Candidate c : toRemove) {
        this.activeCandidates.remove(c);
      }
      
      // If no one was elected, eliminate the candidate with fewest votes
      if (!someoneElected && !this.activeCandidates.isEmpty()) {
        Candidate leastVotes = this.activeCandidates.get(0);
        
        for (Candidate candidate : this.activeCandidates) {
          if (candidate.getBallots().size() < leastVotes.getBallots().size()) {
            leastVotes = candidate;
          }
        }
        
        leastVotes.setFinalVoteCount(leastVotes.getBallots().size());

        if (isTest) {
          System.out.println("Eliminated: " + leastVotes.getCandidateName() + 
                           " with " + leastVotes.getBallots().size() + " votes");
        }

        // Eliminate this candidate
        this.losers.add(leastVotes);
        this.activeCandidates.remove(leastVotes);
        leastVotes.getBallots().clear();
      }
    }
  }
  
  /**
   * Helper method to find a candidate by their ID
   * @param candidateID The ID to search for
   * @return The candidate with matching ID, or null if not found
   */
  private Candidate findCandidateByID(int candidateID) {
    for (Candidate candidate : this.candidates) {
      if (candidate.getCandidateID() == candidateID) {
        return candidate;
      }
    }
    return null;
  }
  
  /**
   * Returns the election type as a string
   * @return "STV"
   */
  @Override
  protected String getType() {
    return "STV";
  }
  
  /**
   * Generates the STV election report with round-by-round details
   * Overrides the base implementation to add STV-specific information
   */
  @Override
  public void generateReport() {
    System.out.println("\n========== STV ELECTION REPORT ==========");
    System.out.println("Droop Quota: " + this.droopQuota);
    System.out.println("Total Rounds: " + this.round);
    System.out.println("==========================================");
    
    // Call parent implementation for standard report
    super.generateReport();
    /*
    * 1. Introduce the concept of the filtered reports to the user
    * 2. Ask the user for the file's name and error check
    * 3. Ask the user for the file's location and error check
    * 4. Use this.invalidBallots to generate a report
    */

    Scanner scanner = new Scanner(System.in);

    // Check if input is available (System.in has data)
    boolean hasInput = false;
    try {
        hasInput = System.in.available() > 0;
    } catch (IOException e) {
        hasInput = false;
    }

    // Skip if in test mode AND no input is available
    if (this.isTest && !hasInput) {
        System.out.println("Test mode: No input available, skipping invalid ballot report generation.");
        scanner.close();
        return;
    }
 
    System.out.println("We will generate a file that lists the ballots that were filtered out (i.e., ballots with less than 60% of candidates ranked)");
    System.out.println("What would you like to be the filename of the report?");
    String filename = scanner.nextLine() + ".txt";
    System.out.println("What would you like to be the file location of the report?");
    String fileLocation = scanner.nextLine();
    File file = new File(fileLocation,filename);
    try(PrintWriter writer = new PrintWriter(new FileWriter(file))){
      
      writer.println("Invalid Ballot Report");
      writer.println("===============");
      for(Ballot b: invalidBallots){
        writer.println(b.getBallotID());
      }
    }
    catch (IOException e) {
      System.err.println("Error writing report file: " + e.getMessage());
    }
  }

  /**
   * Saves the full STV audit report to a text file.
   * Overrides the base implementation to include the shuffled ballot order
   * and a per-round breakdown of ballot assignments for each candidate.
   * The report file is named using the election type and timestamp.
   */
  @Override
  protected void saveReportToFile() {
    try {
      String filename = "STV_Report_" +
                        new SimpleDateFormat("yyyyMMdd_HHmmss").format(electionDate) + ".txt";
      PrintWriter writer = new PrintWriter(new FileWriter(filename));

      // Header
      writer.println("SRS Election");
      writer.println("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(electionDate));
      writer.println();

      // Candidate roster
      writer.println("Candidates (" + candidates.size() + " total):");
      for (Candidate c : candidates) {
        writer.println("  " + c.getCandidateName() + " (" + c.getCandidateParty() + ")");
      }
      writer.println();

      // Election parameters
      writer.println("Seats: " + numSeats);
      writer.println("Total Ballots: " + ballots.size());
      writer.println("Droop Quota: " + this.droopQuota);
      writer.println("Total Rounds: " + this.round);
      writer.println();

      // Elected list in order of election
      writer.println("ELECTED CANDIDATES:");
      for (Candidate c : winners) {
        writer.println("  " + c.getCandidateName() + " (" + c.getCandidateParty() + ")");
      }
      writer.println();

      // Non-elected list in order of elimination
      writer.println("NON-ELECTED CANDIDATES:");
      for (Candidate c : losers) {
        writer.println("  " + c.getCandidateName() + " (" + c.getCandidateParty() + ")");
      }
      writer.println();

      // Ballot order after shuffle (or original order in test mode)
      if (!isTest) {
        writer.println("BALLOT ORDER AFTER SHUFFLE:");
      } else {
        writer.println("BALLOT ORDER (shuffle disabled in test mode):");
      }
      writer.println("  " + shuffledBallotOrder.toString());
      writer.println();

      // Round-by-round ballot distribution
      writer.println("ROUND-BY-ROUND BALLOT DISTRIBUTION:");
      for (int i = 0; i < roundSnapshots.size(); i++) {
        writer.println("  Round " + (i + 1) + ":");
        Map<Integer, List<Integer>> snapshot = roundSnapshots.get(i);
        for (Map.Entry<Integer, List<Integer>> entry : snapshot.entrySet()) {
          // Look up candidate name for readability
          Candidate c = findCandidateByID(entry.getKey());
          String name = (c != null) ? c.getCandidateName() : "ID " + entry.getKey();
          writer.println("    " + name + ": " + entry.getValue().toString() +
                         " (" + entry.getValue().size() + " ballots)");
        }
      }

      writer.close();
      System.out.println("Report saved to: " + filename);

    } catch (IOException e) {
      System.err.println("Error writing report file: " + e.getMessage());
    }
  }
}
