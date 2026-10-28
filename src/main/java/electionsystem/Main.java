package electionsystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the Plurality / STV Voting System.
 *
 * Provides a text-based command-line interface that accepts three top-level
 * commands:
 *   run election  – configure and execute an election
 *   help          – display usage information
 *   exit          – terminate the program
 * Written by Finn Gidden
 */
public class Main {
  private static final String HELP_TEXT =
      "\n+--------------------------------------------------+\n"
    + "|           VOTING SYSTEM - HELP                   |\n"
    + "+--------------------------------------------------+\n"
    + "| Available commands:                              |\n"
    + "|                                                  |\n"
    + "|  run election: Run a Plurality or STV election.  |\n"
    + "|                You will be prompted for:         |\n"
    + "|                  - Candidate file (.csv)         |\n"
    + "|                  - Ballot file (.csv)            |\n"
    + "|                                                  |\n"
    + "|  help:         Display this help screen.         |\n"
    + "|                                                  |\n"
    + "|  exit:         Terminate the program.            |\n"
    + "+--------------------------------------------------+\n"
    + "| File format notes:                               |\n"
    + "|  Candidate CSV:  id,name,party (one per line)    |\n"
    + "|  Ballot CSV:                                     |\n"
    + "|    Line 1: Algorithm (PV, STV, MV)               |\n"
    + "|    Line 2: Number of seats                       |\n"
    + "|    Line 3: ballotID,candidate1,candidate2...     |\n"
    + "|    Line 4+: Ballots (rankings under candidates)  |\n"
    + "+--------------------------------------------------+\n";
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    System.out.println("Plurality / STV / MV Voting System");
    System.out.println("Type 'help' for usage information.\n");

    while (true) {
      System.out.print("Enter command choice: ");
      String command = scanner.nextLine().trim().toLowerCase();

      switch (command) {
        case "run election":
          runElectionFlow(scanner);
          break;

        case "help":
          System.out.println(HELP_TEXT);
          break;

        case "exit":
          System.out.println("\n  Goodbye!");
          scanner.close();
          return;

        default:
          System.out.println(
              "\n  [!] Invalid option. Please enter run election, help, or exit.\n");
      }
    }
  }

  /**
   * Collects all required parameters from the user, constructs the appropriate
   * Election subclass, and calls runElection().
   */
  private static void runElectionFlow(Scanner scanner) {
    // Assuming the user should only input file names
    boolean isTest = false;

    String candidateFile = promptExistingFile(
        scanner, "Enter candidate file name: ", "candidate");
    List<String> candidateFiles = new ArrayList<>(Arrays.asList(candidateFile));

    // Assuming only one ballot file
    String ballotFile = promptExistingFile(
        scanner, "Enter ballot file name: ", "ballot");

    List<String> ballotFiles = new ArrayList<>(Arrays.asList(ballotFile));

    // Read the election parameters from the first ballot file
    String firstBallotFile = ballotFiles.get(0);
    String algorithm = "";
    int numSeats = 0;

    try (BufferedReader br = new BufferedReader(new FileReader(firstBallotFile))) {
      algorithm = br.readLine().trim().toUpperCase();
      numSeats = Integer.parseInt(br.readLine().trim());
    } catch (Exception e) {
      System.err.println("\n  [!] Error reading header from ballot file: " + e.getMessage());
      return;
    }
    System.out.println();

    try {
      Election election;

      if (algorithm.equals("STV")) {
        election = new STV(numSeats, ballotFiles, candidateFiles, isTest);
      } else if (algorithm.equals("PV")) {
        election = new Plurality(numSeats, ballotFiles, candidateFiles, isTest);
      } else if (algorithm.equals("MV")) {
        election = new MV(numSeats, ballotFiles, candidateFiles, isTest);
      } else {
        System.out.println("  [!] Unknown algorithm detected in ballot file: " + algorithm);
        return;
      }

      election.runElection(isTest);

    } catch (Exception e) {
      System.err.println("\n  [!] An unexpected error occurred while running the election:");
      System.err.println("      " + e.getMessage());
      System.err.println("      Please check your input files and try again.\n");
    }
  }

  /**
   * Prompts the user for a single readable file path, re-prompting if the file cannot be found or read.
   * @param fileDescription Human-readable label used in error messages (e.g. "candidate")
   * @return The valid file path string entered by the user
   */
  private static String promptExistingFile(
      Scanner scanner, String prompt, String fileDescription) {
    while (true) {
      System.out.print(prompt);
      String filename = scanner.nextLine().trim();
      if (filename.isEmpty()) {
        System.out.println("  [!] File name cannot be empty. Please try again.");
        continue;
      }
      File file = new File(filename);
      if (file.exists() && file.isFile() && file.canRead()) {
        return filename;
      }
      System.out.println(
          "  [!] Could not find or read " + fileDescription + " file \""
          + filename + "\". Please check the path and try again.");
    }
  }

  /**
   * Helper to detect algorithm from the first line of a ballot file.
   */
  public static String detectAlgorithm(String filename) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
      return br.readLine().trim().toUpperCase();
    }
  }

  /**
   * Helper to detect seat count from the second line of a ballot file.
   */
  public static int detectSeats(String filename) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
      br.readLine(); // skip first line
      return Integer.parseInt(br.readLine().trim());
    }
  }

}
