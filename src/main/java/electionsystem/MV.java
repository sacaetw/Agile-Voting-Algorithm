package electionsystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Municipal Voting (MV) election implementation.
 * Uses approval voting where voters mark one or more candidates;
 * the top numSeats candidates by total approval count are declared winners.
 * Written by Finn Gidden
 */
public class MV extends Election {

  private int totalVotesCast;
  private int numFiles;

  /**
   * Constructs an MV election, loading candidates and ballots from the supplied files.
   * @param seats Number of seats to fill
   * @param ballotFiles List of ballot CSV file paths
   * @param candidateFiles List of candidate CSV file paths
   * @param isTest If true, runs in test mode
   */
  public MV(int seats, List<String> ballotFiles, List<String> candidateFiles,
            boolean isTest) {
    super();
    this.numSeats       = seats;
    this.electionDate   = new Date();
    this.isTest         = isTest;
    this.totalVotesCast = 0;
    this.numFiles       = ballotFiles.size();

    // Load candidates first so parseMVBallotsFromFile can look them up
    for (String candidateFile : candidateFiles) {
      parseCandidatesFromFile(candidateFile);
    }

    // Load and tally MV ballots
    for (String ballotFile : ballotFiles) {
      parseMVBallotsFromFile(ballotFile);
    }
  }

  /**
   * Parses one MV ballot CSV file and assigns ballots to approved candidates.
   * @param filename Path to the MV ballot CSV file
   */
  private void parseMVBallotsFromFile(String filename) {
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

      br.readLine(); // skip "MV" algorithm line
      br.readLine(); // skip seats line (already set via constructor)

      // Build column-index to candidateID mapping from header row
      String headerLine = br.readLine();
      if (headerLine == null) {
        System.err.println("MV ballot file is missing header row: " + filename);
        return;
      }

      String[] headerParts = headerLine.split(",");
      List<Integer> columnOrder = new ArrayList<>(); // index 0 = first candidate column

      for (int i = 1; i < headerParts.length; i++) {
        String val = headerParts[i].trim();
        if (!val.isEmpty()) {
          try {
            columnOrder.add(Integer.parseInt(val));
          } catch (NumberFormatException e) {
            System.err.println("Non-numeric candidate ID in header, skipping column " + i
                               + " (\"" + val + "\")");
          }
        }
      }

      if (columnOrder.isEmpty()) {
        System.err.println("No candidate IDs found in header of: " + filename);
        return;
      }

      // Parse ballot rows
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) continue;

        // Use -1 limit so trailing commas produce empty strings rather than
        // being silently dropped, preserving column alignment.
        String[] parts = line.split(",", -1);

        try {
          int ballotID = Integer.parseInt(parts[0].trim());
          List<Integer> approvedCandidateIDs = new ArrayList<>();

          for (int col = 0; col < columnOrder.size(); col++) {
            // parts[col + 1] is this candidate's column value; may be absent
            // if the row was truncated (treat as empty / not voted).
            if (col + 1 < parts.length) {
              String val = parts[col + 1].trim();
              if (val.equals("1")) {
                approvedCandidateIDs.add(columnOrder.get(col));
                totalVotesCast++;
              }
            }
          }

          // Create the ballot (votes list = approved candidate IDs)
          Ballot ballot = new Ballot(ballotID, approvedCandidateIDs);
          ballots.add(ballot);

          // Assign ballot to each approved candidate immediately while the
          // approved-ID list is still in scope (avoids needing Ballot#getVotes)
          for (int candidateID : approvedCandidateIDs) {
            Candidate candidate = findCandidateByID(candidateID);
            if (candidate != null) {
              candidate.addBallot(ballot);
            } else {
              System.err.println("Ballot " + ballotID
                  + " references unknown candidate ID " + candidateID
                  + " — vote not counted.");
            }
          }

        } catch (NumberFormatException e) {
          System.err.println("Skipping malformed ballot row: \"" + line + "\"");
        }
      }

    } catch (IOException e) {
      System.err.println("Error reading MV ballot file \"" + filename
                         + "\": " + e.getMessage());
    }
  }

  /**
   * Executes the MV election algorithm
   * @param isTest If true, runs in test mode
   */
  @Override
  public void runElection(boolean isTest) {
    this.isTest = isTest;

    if (isTest) {
      System.out.println("Running MV election in test mode.");
    }

    // Sort candidates descending by approval count
    Collections.sort(candidates, new Comparator<Candidate>() {
      @Override
      public int compare(Candidate c1, Candidate c2) {
        return Integer.compare(c2.getBallots().size(), c1.getBallots().size());
      }
    });

    // Split into winners and losers
    for (int i = 0; i < candidates.size(); i++) {
      if (i < numSeats) {
        winners.add(candidates.get(i));
      } else {
        losers.add(candidates.get(i));
      }
    }

    generateReport();
  }

  /**
   * Generates the MV election report with candidate vote totals and percentages
   * Overrides the base implementation to add MV-specific information
   */
  @Override
  public void generateReport() {
    // Guard against division by zero if no votes were cast
    double totalVotesDouble = (totalVotesCast > 0) ? totalVotesCast : 1;

    System.out.println("\n========== MV ELECTION RESULTS ==========");
    System.out.println("Election Type:     Municipal Voting (MV)");
    System.out.println("Number of Seats:   " + numSeats);
    System.out.println("Number of Files:   " + numFiles);
    System.out.println("Number of Ballots: " + ballots.size());
    System.out.println("Total Votes Cast:  " + totalVotesCast);

    System.out.println("\nCANDIDATE RESULTS:");
    System.out.printf("  %-35s %6s   %s%n", "Candidate", "Votes", "% Share");
    System.out.println("  " + "-".repeat(55));
    for (Candidate c : candidates) {
      int votes = c.getBallots().size();
      double pct = (votes / totalVotesDouble) * 100.0;
      System.out.printf("  %-35s %6d   %6.2f%%%n",
          c.getCandidateName() + " (" + c.getCandidateParty() + ")",
          votes, pct);
    }

    System.out.println("\n--- ELECTED ---");
    for (Candidate c : winners) {
      int votes = c.getBallots().size();
      double pct = (votes / totalVotesDouble) * 100.0;
      System.out.printf("  %-35s %6d   %6.2f%%%n",
          c.getCandidateName(), votes, pct);
    }
    System.out.println("==========================================\n");

    saveMVReportToFile(totalVotesDouble);
  }

  /**
   * Saves the MV election report to a file named MVElectionyyyyMMdd.txt
   * @param totalVotesDouble Pre-computed denominator for percentage calculations
   */
  private void saveMVReportToFile(double totalVotesDouble) {
    String filename = "MVElection"
        + new SimpleDateFormat("yyyyMMdd").format(electionDate)
        + ".txt";

    try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {

      writer.println("MV Election Report");
      writer.println("==================");
      writer.println("Number of Seats:   " + numSeats);
      writer.println("Number of Files:   " + numFiles);
      writer.println("Number of Ballots: " + ballots.size());
      writer.println("Total Votes Cast:  " + totalVotesCast);
      writer.println();

      // All candidates with stats
      writer.printf("%-35s %6s   %s%n", "Candidate", "Votes", "% Share");
      writer.println("-".repeat(55));
      for (Candidate c : candidates) {
        int votes = c.getBallots().size();
        double pct = (votes / totalVotesDouble) * 100.0;
        writer.printf("%-35s %6d   %6.2f%%%n",
            c.getCandidateName() + " (" + c.getCandidateParty() + ")",
            votes, pct);
      }

      // Elected
      writer.println();
      writer.println("ELECTED CANDIDATES:");
      for (Candidate c : winners) {
        int votes = c.getBallots().size();
        double pct = (votes / totalVotesDouble) * 100.0;
        writer.printf("  %-33s %6d   %6.2f%%%n",
            c.getCandidateName() + " (" + c.getCandidateParty() + ")",
            votes, pct);
      }

      // Non-elected
      writer.println();
      writer.println("NON-ELECTED CANDIDATES:");
      for (Candidate c : losers) {
        int votes = c.getBallots().size();
        double pct = (votes / totalVotesDouble) * 100.0;
        writer.printf("  %-33s %6d   %6.2f%%%n",
            c.getCandidateName() + " (" + c.getCandidateParty() + ")",
            votes, pct);
      }

      System.out.println("Report saved to: " + new java.io.File(filename).getAbsolutePath());

    } catch (IOException e) {
      System.err.println("Error writing MV report file: " + e.getMessage());
    }
  }

  /**
   * Helper method to find a candidate by their ID
   * @param candidateID The ID to search for
   * @return The candidate with matching ID, or null if not found
   */
  private Candidate findCandidateByID(int candidateID) {
    for (Candidate c : candidates) {
      if (c.getCandidateID() == candidateID) {
        return c;
      }
    }
    return null;
  }

  /**
   * Returns the election type as a string
   * @return "MV"
   */
  @Override
  protected String getType() {
    return "MV";
  }
}