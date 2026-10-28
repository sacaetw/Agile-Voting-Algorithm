package electionsystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List; 
import java.util.Scanner;

/**
 * Test suite for Main class functionality.
 * Contains tests for the auto-detection feature where the system identifies
 * the algorithm and seat count directly from the ballot file headers.
 * Written by James Lu
 */
public class MainTest {

    @TempDir
    Path tempDir;

    /**
     * Feature: Algorithm Auto-Detection
     * Scenario: User provides a ballot file with 'PV' as the first line.
     * Expected: The system identifies the algorithm as PV (Plurality).
     */
    @Test
    public void testDetectAlgorithmPVHeader() throws IOException {
        Path file = tempDir.resolve("test_pv.csv");
        try (FileWriter fw = new FileWriter(file.toFile())) {
            fw.write("PV\n1\nballotId,1,2");
        }

        String result = Main.detectAlgorithm(file.toString());
        assertEquals("PV", result, "Should successfully read PV from the first line");
    }

    /**
     * Feature: Algorithm Auto-Detection
     * Scenario: User provides a ballot file with 'STV' as the first line.
     * Expected: The system identifies the algorithm as STV (Plurality).
     */
    @Test
    public void testDetectAlgorithmSTVHeader() throws IOException {
        Path file = tempDir.resolve("test_stv.csv");
        try (FileWriter fw = new FileWriter(file.toFile())) {
            fw.write("STV\n1\nballotId,1,2");
        }

        String result = Main.detectAlgorithm(file.toString());
        assertEquals("STV", result, "Should successfully read STV from the first line");
    }

    /**
     * Feature: Algorithm Auto-Detection
     * Scenario: User provides a ballot file with 'MV' as the first line.
     * Expected: The system identifies the algorithm as MV (Plurality).
     */
    @Test
    public void testDetectAlgorithmMVHeader() throws IOException {
        Path file = tempDir.resolve("test_mv.csv");
        try (FileWriter fw = new FileWriter(file.toFile())) {
            fw.write("MV\n1\nballotId,1,2");
        }

        String result = Main.detectAlgorithm(file.toString());
        assertEquals("MV", result, "Should successfully read MV from the first line");
    }

    /**
     * Feature: Algorithm Auto-Detection
     * Scenario: User provides a ballot file with 'XYZ' as the first line.
     * Expected: The system identifies the algorithm as invalid and prints the correct message.
     */
    @Test
    public void testMain_InvalidAlgorithm_PrintsErrorMessage() throws IOException {
        // Setup temporary files
        Path candidateFile = tempDir.resolve("candidates.csv");
        Files.write(candidateFile, java.util.List.of("1,Joe,Independent"));

        Path invalidBallotFile = tempDir.resolve("invalid_type.csv");
        // XYZ is the invalid algorithm trigger
        Files.write(invalidBallotFile, java.util.List.of("XYZ", "2", "ballotID,C1", "1,1"));

        // Simulating main
        String inputSequence = "run election\n"
                + candidateFile.toString() + "\n"
                + invalidBallotFile.toString() + "\n"
                + "exit\n";

        InputStream simulateInput = new ByteArrayInputStream(inputSequence.getBytes());
        System.setIn(simulateInput);

        // Output capture
        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(capturedOutput));

        try {
            Main.main(new String[0]);

            String output = capturedOutput.toString();
            assertTrue(output.contains("[!] Unknown algorithm detected in ballot file: XYZ"),
                    "The system should have caught the 'XYZ' algorithm error.");

        } finally {
            System.setOut(originalOut);
            System.setIn(System.in);
        }
    }

    /**
     * Feature: Seat Count Auto-Detection
     * Scenario: User provides a ballot file with '3' as the seat count.
     * Expected: The system parses the integer 3 from the second line.
     */
    @Test
    public void testDetectSeats() throws IOException {
        Path file = tempDir.resolve("test_seats.csv");
        try (FileWriter fw = new FileWriter(file.toFile())) {
            fw.write("STV\n3\nballotId,1,2,3");
        }

        int result = Main.detectSeats(file.toString());
        assertEquals(3, result, "Should successfully parse 3 seats from the second line");
    }

    /**
     * Feature: Seat Count Auto-Detection
     * Scenario: The second line contains text instead of a number.
     * Expected: Throws NumberFormatException (which is handled by runElectionFlow).
     */
    @Test
    public void testDetectSeatsThrowsException() throws IOException {
        Path file = tempDir.resolve("error_seats.csv");
        try (FileWriter fw = new FileWriter(file.toFile())) {
            fw.write("STV\nTWO_SEATS\nballotId,1,2");
        }

        assertThrows(NumberFormatException.class, () -> {
            Main.detectSeats(file.toString());
        }, "Should throw exception when seats line is not a valid integer");
    }
    /**
     * Feature: Invalid Ballot Report Generation
     * Scenario: generateReport() is called and user provides a valid filename and directory.
     * Expected: A .txt file is created at the specified location with the correct header.
     */
    @Test
    public void testGenerateReport_CreatesFileWithHeader() throws IOException {
        String reportName = "invalid_ballots";
        String input = reportName + "\n" + tempDir.toString() + "\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        STV stv = new STV(1, List.of("test_ballots.csv"), List.of("test_candidates.csv"), true);
        stv.generateReport();
        System.setIn(System.in);

        File report = new File(tempDir.toString(), reportName + ".txt");
        assertTrue(report.exists(), "Report file should be created");

        List<String> lines = Files.readAllLines(report.toPath());
        assertEquals("Invalid Ballot Report", lines.get(0));
        assertEquals("===============", lines.get(1));
    }

    /**
     * Feature: Invalid Ballot Report Generation
     * Scenario: generateReport() is called with invalid ballots present.
     * Expected: The report file lists the ballot IDs of the invalid ballots.
     */
    @Test
    public void testGenerateReport_ListsInvalidBallotIDs() throws IOException {
        String reportName = "ballots_report";
        String input = reportName + "\n" + tempDir.toString() + "\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        STV stv = new STV(1, List.of("test_ballots.csv"), List.of("test_candidates.csv"), true);
        stv.getInvalidBallots().add(new Ballot(42, new ArrayList<>()));
        stv.generateReport();
        System.setIn(System.in);

        File report = new File(tempDir.toString(), reportName + ".txt");
        List<String> lines = Files.readAllLines(report.toPath());
        assertEquals("42", lines.get(2), "Invalid ballot ID should appear in the report");
    }

    /**
     * Feature: Invalid Ballot Report Generation
     * Scenario: generateReport() appends .txt to the user-supplied filename.
     * Expected: The file on disk has a .txt extension even though the user didn't type one.
     */
    @Test
    public void testGenerateReport_AppendsTxtExtension() throws IOException {
        String reportName = "my_report";
        String input = reportName + "\n" + tempDir.toString() + "\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        STV stv = new STV(1, List.of("test_ballots.csv"), List.of("test_candidates.csv"), true);
        stv.generateReport();
        System.setIn(System.in);

        assertTrue(new File(tempDir.toString(), reportName + ".txt").exists(), "File with .txt must exist");
        assertFalse(new File(tempDir.toString(), reportName).exists(),         "File without .txt must NOT exist");
    }
}
