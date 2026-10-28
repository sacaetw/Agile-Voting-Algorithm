package electionsystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Abstract base class for election systems.
 * Provides common functionality for both STV and Plurality elections.
 * Written by Finn Gidden
 */
public abstract class Election {
  protected int numSeats;
  protected Date electionDate;
  protected List<Candidate> candidates;
  protected List<Ballot> ballots;
  protected List<Candidate> winners;
  protected List<Candidate> losers;
  protected boolean isTest;
  protected List<Candidate> activeCandidates;
  
  /**
   * Default constructor - initializes lists
   */
  public Election() {
    this.candidates = new ArrayList<>();
    this.ballots = new ArrayList<>();
    this.winners = new ArrayList<>();
    this.losers = new ArrayList<>();
    this.activeCandidates = new ArrayList<>();
    this.electionDate = new Date();
  }
  
  /**
   * Abstract method to run the election algorithm.
   * Must be implemented by subclasses (STV, Plurality).
   * @param isTest If true, runs in test mode
   */
  public abstract void runElection(boolean isTest);
  
  /**
   * Abstract method to get the election type.
   * @return String representing election type ("STV" or "Plurality")
   */
  protected abstract String getType();
  
  /**
   * Generates the election report and prints results summary.
   * Writes election information to console and saves to output file.
   */
  public void generateReport() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    // Print simplified summary to console
    System.out.println("\n========== ELECTION RESULTS ==========");
    System.out.println("Election Type: " + getType());
    System.out.println("Election Date: " + dateFormat.format(electionDate));
    System.out.println("\nCandidates:");
    for (Candidate c : candidates) {
      System.out.println("  " + c.getCandidateName() + " (" + c.getCandidateParty() + ")");
    }
    System.out.println("\nNumber of Candidates: " + candidates.size());
    System.out.println("Number of Seats: " + numSeats);
    System.out.println("Total Ballots: " + ballots.size());
    
    System.out.println("\n--- ELECTED ---");
    for (Candidate c : winners) {
      System.out.println("  " + c.getCandidateName() + " - " +
                         c.getFinalVoteCount() + " votes");
    }
    
    System.out.println("\n--- NON-ELECTED ---");
    for (int i = losers.size() - 1; i >= 0; i--) {
      Candidate c = losers.get(i);
      System.out.println("  " + c.getCandidateName() + " - " +
                         c.getFinalVoteCount() + " votes");
    }
    System.out.println("======================================\n");
    
    // Save report to file
    saveReportToFile();
  }
  
  /**
   * Saves the election report to an output file.
   */
  protected void saveReportToFile() {
    try {
      String filename = getType() + "_Report_" + 
                       new SimpleDateFormat("yyyyMMdd_HHmmss").format(electionDate) + ".txt";
      PrintWriter writer = new PrintWriter(new FileWriter(filename));
      
      writer.println("Election Report");
      writer.println("===============");
      writer.println("Type: " + getType());
      writer.println("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(electionDate));
      writer.println("Seats: " + numSeats);
      writer.println("Total Ballots: " + ballots.size());
      writer.println("Total Candidates: " + candidates.size());
      writer.println();
      
      writer.println("ELECTED CANDIDATES:");
      for (Candidate c : winners) {
        writer.println(c.getCandidateName() + " (" + c.getCandidateParty() + ") - " + 
        c.getFinalVoteCount() + " votes");
      }
      writer.println();
      
      writer.println("NON-ELECTED CANDIDATES:");
      for (int i = losers.size() - 1; i >= 0; i--) {
        Candidate c = losers.get(i);
        writer.println(c.getCandidateName() + " (" + c.getCandidateParty() + ") - " + 
        c.getFinalVoteCount() + " votes");
      }
      
      writer.close();
      System.out.println("Report saved to: " + filename);
      
    } catch (IOException e) {
      System.err.println("Error writing report file: " + e.getMessage());
    }
  }
  
  /**
   * Parses candidates from a CSV file.
   * Expected format: candidateID,candidateName,candidateParty
   * @param filename The candidate file to parse
   */
  protected void parseCandidatesFromFile(String filename) {
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) continue;

        String[] parts = line.split(",");
        if (parts.length >= 3) {
          try {
            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            String party = parts[2].trim();

            Candidate candidate = new Candidate(id, name, party);
            candidates.add(candidate);
            activeCandidates.add(candidate);
          } catch (NumberFormatException e) {
            // Skip header row or malformed lines
          }
        }
      }
    } catch (IOException e) {
      System.err.println("Error reading candidate file: " + e.getMessage());
    }
  }

  /**
   * Parses ballots from a CSV file.
   * Expected format:
   * Line 1: Algorithm
   * Line 2: Seats
   * Line 3: ballotID,candidate1,candidate2...
   * Line 4+: ballotID,rank,rank,rank...
   * @param filename The ballot file to parse
   */
  protected void parseBallotsFromFile(String filename) {
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

      // Skip algorithm line, handled in main
      String algoLine = br.readLine();
      if (algoLine == null) return;

      // Set seats (this could also be skipped as it is handled in main)
      numSeats = Integer.parseInt(br.readLine());

      String line;
      List<Integer> candidateOrder = null; // populated when a rank-column header is detected

      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) continue;

        String[] parts = line.split(",");

        // Detect header row: first field is non-numeric (e.g. "ballotId")
        boolean isHeader = false;
        try {
          Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException e) {
          isHeader = true;
        }

        if (isHeader) {
          // Extract candidate IDs from the header columns
          candidateOrder = new ArrayList<>();
          for (int i = 1; i < parts.length; i++) {
            String val = parts[i].trim();
            if (!val.isEmpty()) {
              try {
                candidateOrder.add(Integer.parseInt(val));
              } catch (NumberFormatException e) {
                // not a candidate ID column, skip
              }
            }
          }
          continue;
        }

        if (parts.length >= 2) {
          try {
            int ballotID = Integer.parseInt(parts[0].trim());
            List<Integer> votes = new ArrayList<>();

            if (candidateOrder != null) {
              // Rank-column format: each value is the rank given to the candidate in that column.
              // Convert to a preference-ordered list of candidate IDs (rank 1 = first choice).
              int numCols = Math.min(parts.length - 1, candidateOrder.size());
              int[][] rankPairs = new int[numCols][2]; // [rank, candidateID]

              for (int i = 0; i < numCols; i++) {
                String val = parts[i + 1].trim();
                int rank = val.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(val);
                rankPairs[i][0] = rank;
                rankPairs[i][1] = candidateOrder.get(i);
              }
              Arrays.sort(rankPairs, (a, b) -> Integer.compare(a[0], b[0]));
              for (int[] pair : rankPairs) {
                votes.add(pair[1]);
              }
            } else {
              // Direct format: values are candidate IDs in preference order
              for (int i = 1; i < parts.length; i++) {
                String vote = parts[i].trim();
                if (!vote.isEmpty()) {
                  votes.add(Integer.parseInt(vote));
                }
              }
            }

            ballots.add(new Ballot(ballotID, votes));
          } catch (NumberFormatException e) {
            System.err.println("Invalid ballot format: " + e.getMessage());
          }
        }
      }
    } catch (IOException e) {
      System.err.println("Error reading ballot file: " + e.getMessage());
    }
  }
  
  /**
   * Parses ballots from multiple files.
   * @param filenames List of ballot file names
   */
  protected void parseBallotsFromFiles(List<String> filenames) {
    for (String filename : filenames) {
      parseBallotsFromFile(filename);
    }
  }
  
  // Getter methods
  
  /**
   * Returns the list of elected candidates.
   * @return Ordered list of winners
   */
  public List<Candidate> getWinners() {
    return winners;
  }
  
  /**
   * Returns the list of non-elected candidates.
   * @return Ordered list of losers
   */
  public List<Candidate> getLosers() {
    return losers;
  }
  
  /**
   * Returns the list of active candidates.
   * @return List of candidates still in contention
   */
  public List<Candidate> getActiveCandidates() {
    return activeCandidates;
  }
  
  /**
   * Returns all candidates in the election.
   * @return Complete list of candidates
   */
  public List<Candidate> getCandidates() {
    return candidates;
  }
  
  /**
   * Returns all ballots cast in the election.
   * @return List of all ballots
   */
  public List<Ballot> getBallots() {
    return ballots;
  }
  
  /**
   * Returns whether the election is running in test mode.
   * @return True if test mode, false otherwise
   */
  protected boolean getIsTest() {
    return isTest;
  }
  
  /**
   * Returns the number of seats being contested.
   * @return Number of seats
   */
  public int getNumSeats() {
    return numSeats;
  }
  
  /**
   * Returns the election date.
   * @return Date the election was run
   */
  public Date getElectionDate() {
    return electionDate;
  }
}