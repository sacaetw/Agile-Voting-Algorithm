# **Software Requirements Specification** 

# **Voting System**

**Version 1.0 approved**

**Prepared by Finn Gidden (gidde032), James Lu (lu000605), Nicholas Nguyen, and Carter Randall (randa464) **

**CSCI 4081 SP26**

**02/09/26**

## **Table of Contents** {#table-of-contents}

**Table of Contents	[ii](#table-of-contents)**

**Revision History	[ii](#revision-history)**

**1\.	Introduction	[1](#1.-introduction)**  
1.1	Purpose	[1](#1.1-purpose)  
1.2	Document Conventions	[1](#1.2-document-conventions)  
1.3	Intended Audience and Reading Suggestions	[1](#1.3-intended-audience-and-reading-suggestions)  
1.4	Product Scope	[1](#1.4-product-scope)  
1.5	References	[1](#1.5-references)

**2\.	Overall Description	[2](#2.-overall-description)**  
2.1	Product Perspective	[2](#2.1-product-perspective)  
2.2	Product Functions	[2](#2.2-product-functions)  
2.3	User Classes and Characteristics	[2](#2.3-user-classes-and-characteristics)  
2.4	Operating Environment	[2](#2.4-operating-environment)  
2.5	Design and Implementation Constraints	[2](#2.5-design-and-implementation-constraints)  
2.6	User Documentation	[2](#2.6-user-documentation)  
2.7	Assumptions and Dependencies	[3](#2.7-assumptions-and-dependencies)

**3\.	External Interface Requirements	[3](#3.-external-interface-requirements)**  
3.1	User Interfaces	[3](#3.1-user-interfaces)  
3.2	Hardware Interfaces	[3](#3.2-hardware-interfaces)  
3.3	Software Interfaces	[3](#3.3-software-interfaces)  
3.4	Communications Interfaces	[3](#3.4-communications-interfaces)

**4\.	System Features	[4](#4.-system-features)**  
4.1	System Feature 1	[4](#4.1-system-feature-1)  
4.2	System Feature 2 (and so on)	[4](#4.2-system-feature-2-\(and-so-on\))

**5\.	Other Nonfunctional Requirements	[4](#5.-other-nonfunctional-requirements)**  
5.1	Performance Requirements	[4](#5.1-performance-requirements)  
5.2	Safety Requirements	[5](#5.2-safety-requirements)  
5.3	Security Requirements	[5](#5.3-security-requirements)  
5.4	Software Quality Attributes	[5](#5.4-software-quality-attributes)  
5.5	Business Rules	[5](#5.5-business-rules)

**6\.	Other Requirements	[5](#6.-other-requirements)**

**Appendix A: Glossary	[5](#appendix-a:-glossary)**

**Appendix B: Analysis Models	[5](#appendix-b:-analysis-models)**

**Appendix C: To Be Determined List	[6](#appendix-c:-to-be-determined-list)**

## **Revision History** {#revision-history}

| Name | Date | Reason For Changes | Version |
| :---- | :---- | :---- | :---- |
| Finn Gidden | 2/09/2026 | IEEE SRS template uploaded | 1.0 |

## **1\. 	Introduction** {#1.-introduction}

### **1.1 	Purpose**  {#1.1-purpose}

This document specifies the software requirements for a voting software, covering both its STV and plurality voting systems. It explains the features and functionality of the software, its interface and user conventions, and the constraints on its use.

### **1.2	Document Conventions** {#1.2-document-conventions}

This SRS was created following the IEEE template for System Requirement Specification Documents. Requirements are assumed to have their own priority levels, and are grouped by type (functional vs. non-functional) and category (hardware, software, security, etc.).

### **1.3	Intended Audience and Reading Suggestions** {#1.3-intended-audience-and-reading-suggestions}

This document is intended for users, testers, and developers of the voting software, containing instructions for readers to deploy, interact with, and use the software, alongside in-depth descriptions of the system's features and requirements. The introduction and overall description sections are the best place to start for all types of reader. Sections 3.2 and onwards will prove most useful for testers and developers of the software.

### **1.4	Product Scope** {#1.4-product-scope}

The Voting System software is a tool that election officials can use to process ballots and determine election results. Users can set how many seats there are to fill, input CSV files containing voters' ballots, and choose the voting algorithm used to determine election results. With this information, the software calculates the election results, notifies the user of the winner(s), and, in the case of STV elections, creates a text file containing a report of the election data. Voting System allows users to quickly and efficiently calculate election results and generate corresponding documentation, processing up to 100,000 ballots in under 5 minutes.

### **1.5	References** {#1.5-references}

IEEE Template for System Requirement Specification Documents:
[srs_template-ieee.doc](https://github.com/user-attachments/files/25246968/srs_template-ieee.doc)

## **2\.	Overall Description** {#2.-overall-description}

### **2.1	Product Perspective** {#2.1-product-perspective}

The product is intended for the processing of voting results using either plurality voting with a coin-flip tiebreaker or STV voting. It is intended to handle up to 100,000 ballots' worth of data provided in .csv files. The product is standalone from other software and runs primarily on UMN CSE lab machines.

### **2.2	Product Functions** {#2.2-product-functions}

User-Interactive Features:
* UI: A user interface presented within a terminal
* Input Processing: Read the number of seats needed by the user
* Input Processing: Read the voting system that the user requests
* Data Processing: Read in and process one or more files of ballot data, determining # of candidates and ballots
* Data Processing: Read in and process one file of candidate information
* Help: Allows the user to access a help screen when they are stuck

Voting Functions:
* Plurality: Allows the user to conduct a plurality vote
* Plurality (Test): Allows the user to conduct a test plurality vote with a test file
* STV: Allows the user to conduct an STV vote with a ballot shuffle
* STV (Test): Allows the user to conduct a test STV vote with a test file and no shuffle

Results:
* On-screen: A simple description of the election, its statistics, its candidates, and its results printed to the UI
* Text File: A detailed report of the election inputs and results in a text file; intended for auditing purposes
* Elected/Non-elected List: Lists including results for each candidate.

### **2.3	User Classes and Characteristics** {#2.3-user-classes-and-characteristics}

* Election officials for large elections (i.e. state, federal elections) who must count votes using plurality voting.
* Election officials of certain cities (i.e. San Francisco, CA) who must count votes using Droop-quota voting.
* Election officials for non-government voting (i.e. company leadership voting) who need to count a notable amount of ballots.

### **2.4	Operating Environment** {#2.4-operating-environment}

UMN CSE lab machines.

### **2.5	Design and Implementation Constraints** {#2.5-design-and-implementation-constraints}

The Voting System is developed in Java and requires some level of familiarity with a terminal display to utilize the user interface.

### **2.6	User Documentation** {#2.6-user-documentation}

After starting the program, the number of seats, type of election, and whether or not the run is a test should be specified. When prompted (that is, when the user is not using test mode), the user should state the files containing the voting information. The voting results will be displayed in the UI as a quick summary; more details can be found in a specified file.

### **2.7	Assumptions and Dependencies** {#2.7-assumptions-and-dependencies}

The Voting System is developed in Java specifically for UMN CSE lab machines. Thus, users are required to have Java installed to run it, and stable execution may not be guaranteed on other devices.

## **3\.	External Interface Requirements** {#3.-external-interface-requirements}
### **3.1	User Interfaces** {#3.1-user-interfaces}
The user is prompted to input the number of seats to fill and the voting algorithm (i.e., plurality voting or STV voting with Droop quota). The user is then prompted for the file name that contains candidate information and the file name(s) that contain the ballots. Multiple files of ballots can be input. In the case of the Droop Quota, the user will get two lists, one for the elected and the other for the non-elected. The user is asked if the election is a test election. After the election, details of the election will be shown. The type of election, number of ballots, number of seats, number of candidates, elected candidates, non-elected candidates, statistical information (e.g., percentage of votes received and order of winning and losing candidates). A help window should be provided for the user, and if selected, the window should give a tutorial on running the program

****UI Visualizer****
Main election loop:

<img width="394" height="87" alt="Screenshot 2026-02-20 at 11 58 42 AM" src="https://github.com/user-attachments/assets/bcbe6092-8818-454b-86b3-e1d91ea769e0" />

Help loop:

<img width="232" height="91" alt="Screenshot 2026-02-20 at 12 00 02 PM" src="https://github.com/user-attachments/assets/89c02805-75d5-4afb-9a4f-d7c817c45c21" />

Incorrect argument handling and exiting:

<img width="541" height="137" alt="Screenshot 2026-02-20 at 11 59 43 AM" src="https://github.com/user-attachments/assets/662bdbaa-69b9-461b-8c23-c41d4b080a07" />

### **3.2	Hardware Interfaces** {#3.2-hardware-interfaces}
Programs must be able to run on a CSE Lab Machine. These machines have 32 GB RAM and an Intel Core i7.

### **3.3	Software Interfaces** {#3.3-software-interfaces}
The product is to be run on Linux. The programming language of choice is Java. CSV files will be read in for input. The java.util library will be used.

### **3.4	Communications Interfaces** {#3.4-communications-interfaces}
No communication interfaces required.

## **4\.	System Features** {#4.-system-features}

### **4.1	System Feature 1** {#4.1-system-feature-1}

**Name:** User wants plurality voting without testing  
**ID:** UC_001  
**Description:** The user inputs plurality voting as the voting algorithm  
**Actors:** User who wants plurality voting  
**Preconditions:** The user should have seats to fill and ballot data in files 

**Basic flow:**
  1. User is prompted for the seats to fill and inputs number of seats to fill
  2. User is prompted for the voting algorithm and inputs "plurality"
  3. User is asked for testing and answers that testing is not wanted
  4. User is prompted for and inputs the file name that has candidate information and files that contain the ballots
  5. Files are read
  6. Plurality algorithm is ran on data
  7. Report of election details is generated
  8. Details of election are displayed
     
**Alternative flows:**  
- AC_01 User wants help
  1. User enters help command
  2. Tutorial information for the system is displayed
     
**Exception flows:**  
- EX_01 User enters invalid filename  
  1. Message is displayed to indicate the filename was invalid
  2. Prompt to asking for filename repeats

- EX_02 User enters invalid voting algorithm  
  1. Message is displayed to indicate invalid voting algorithm
  2. Prompt to asking for voting algorithm repeats

**Post Conditions:** Details of election are displayed until user closes program

### **4.2	System Feature 2** {#4.2-system-feature-2}

**Name:** User wants STV voting without testing  
**ID:** UC_002  
**Description:** The user inputs STV voting as the voting algorithm  
**Actors:** User who wants STV voting  
**Preconditions:** The user should have seats to fill, and ballot data in files 

**Basic flow:**  
  1. User is prompted for the seats to fill and inputs number of seats to fill
  2. User is prompted for the voting algorithm and inputs "STV"
  3. User is prompted and answers if shuffling is wanted
  4. User is asked for testing and answers that testing is not wanted
  5. User is prompted for and inputs the file name that has candidate information and files that contain the ballots
  6. Files are read
  7. STV algorithm is ran on data
  8. Report is generated of election details and the order of the ballots after the shuffle if the shuffle was turned on
  9. Details of election are displayed

**Alternative flows:**  
- AC_01 User wants help  
  1. User enters help command
  2. Tutorial information for the system is displayed

**Exception flows:**  
- EX_01 User enters invalid filename  
  1. Message is displayed to indicate invalid filename
  2. Prompt to asking for filename repeats

- EX_02 User enters invalid voting algorithm  
  1. Message is displayed to indicate invalid voting algorithm
  2. Prompt to asking for voting algorithm repeats

**Post conditions:** Details of election are displayed until user closes the program  

### **4.3	System Feature 3** {#4.3-system-feature-3}

**Name:** User wants plurality voting with testing  
**ID:** UC_003  
**Description:** The user inputs plurality voting as the voting algorithm  
**Actors:** User who wants plurality voting  
**Preconditions:** The user should have seats to fill, and ballot data in files  

**Basic flow:**  
  1. User is prompted for the seats to fill and inputs number of seats to fill
  2. User is prompted for the voting algorithm and inputs "plurality"
  3. User is asked for testing and answers that testing is wanted
  4. User is prompted for and inputs the file name that has candidate information and files that contain the ballots
  5. Files are read
  6. Plurality algorithm is ran on data
  7. Report is generated of election details
  8. Details of election are displayed

**Alternative flows:**  
- AC_01 User wants help  
  1. User enters help command
  2. Tutorial information for the system is displayed

**Exception flows:**  
- EX_01 User enters invalid filename  
  1. Message is displayed to indicate invalid filename
  2. Prompt to asking for filename repeats

- EX_02 User enters invalid voting algorithm  
  1. Message is displayed to indicate invalid voting algorithm
  2. Prompt to asking for voting algorithm repeats

**Post conditions:** Details of election are displayed until user closes the program  

### **4.4	System Feature 4** {#4.4-system-feature-4}

**Name:** Users wants STV voting with testing  
**ID:** UC_004  
**Description:** The user inputs STV voting as the voting algorithm  
**Actors:** User who wants STV voting  
**Preconditions:** The user should have seats to fill, and ballot data in files  

**Basic flow:**  
  1. User is prompted for the seats to fill and inputs number of seats to fill
  2. User is prompted for the voting algorithm and inputs "STV"
  3. User is asked for testing and answers that testing is wanted
  4. Shuffle option is disabled
  5. User is prompted for and inputs the file name that has candidate information and files that contain the ballots.
  6. Files are read
  7. STV algorithm is ran on data
  8. Report is generated of election details
  9. Details of election are displayed

**Alternative flows:**  
- AC_01 User wants help  
  1. User enters help command
  2. Tutorial information for the system is displayed

**Exception flows:**  
- EX_01 User enters invalid filename  
  1. Message is displayed to indicate invalid filename
  2. Prompt to asking for filename repeats

- EX_02 User enters invalid voting algorithm  
  1. Message is displayed to indicate invalid voting algorithm
  2. Prompt to asking for voting algorithm repeats

**Post conditions:** Details of election are displayed until user closes the program

### **4.5	System Feature 5** {#4.5-system-feature-5}

**Name:** User accesses audit report of STV election  
**ID:** UC_005  
**Description:** The user accesses the text file containing STV election details  
**Actors:** User who wants the STV election audit report  
**Preconditions:** The user has ran an STV election  

**Basic flow:**  
  1. STV election completes, and election data is stored in a report text file
  2. User navigates to the report file
  3. Report file contains:
     - SRS election, date of the election, candidates’ names and parties, and the total number of candidates
     - Number of seats to be filled by the election
     - Elected list, in the order in which the candidates were added to the list
     - Non-Elected list, in the order in which candidates were added to the list

**Alternative flows:**  
- AC_01 User wants help  
  1. User enters help command
  2. Tutorial information for the system is displayed

**Exception flows:**  
- EX_01 STV election fails
  1. Message is displayed to indicate invalid filename or voting algorithm
  2. Prompt to ask for filename repeats and basic flow is entered oncemore

**Post conditions:** Report text file is open until user closes the file

### **4.6	System Feature 6** {#4.6-system-feature-6}

**Name:** User views help window   
**ID:** UC_006   
**Description:** The user accesses the help window with information on how to run the program   
**Actors:** Users who wants help running the program   
**Preconditions:** The system is initiated   

**Basic flow:**  
  1. User enters help command
  2. Help window opens up with information on how to run the program
  3. User closes help window
  
  
**Alternative flows:**  
- AC_01 User requests help accidentally
  1. User closes the help window 
  2. User returns to the program in their previous state

**Exception flows:**  
- EX_01 Help command not recognized
  1. Message is displayed to indicate invalid commmand 
  2. Propmpt to ask the user if they want to open the help window
  
**Post conditions:** The system returns to the state before help was requested 

### **4.7	System Feature 7** {#4.7-system-feature-7}

**Name:** Election results are displayed   
**ID:** UC_007   
**Description:** After the election is proccesses, the system displays a snapshot of the election results   
**Actors:** User who wants election results   
**Preconditions:** The user has run an election and all ballots have been processes and the winner has been determined   

**Basic flow:**  
  1. The system completes a election algorithm (STV or plurality)
  2. A snapshot is displayed with all of the statistics and results from the election
  3. User closes the snapshot to return back to the system
  
  
**Alternative flows:**  
- AC_01 User wants help  
  1. User enters help command
  2. Tutorial information for the system is displayed

**Exception flows:**  
- EX_01 Display cannot be generated
  1. Message is displayed to indicate the results could not be generated
  2. Prompt to ask the user to rerun the election
     
**Post conditions:** The system asks the user for their next action

### **4.8	System Feature 8** {#4.8-system-feature-8}

**Name:** Program is tested with command-line arguments   
**ID:** UC_008   
**Description:** A tester tests the system using command line arguments instead of using the user interface   
**Actors:** Tester   
**Preconditions:** The system is ready to run with CSV files in the directory   

**Basic flow:**  
  1. User launches the program in the terminal command line, passing arguments for: the number of seats, algorithm, canidate file, and ballot files for the election   
  2. The system validates all of the arguments    
  3. The election is run   
  4. The system displays the results of the election   
  
  
**Alternative flows:**  
- AC_01 User wants help  
  1. User enters help command
  2. Tutorial information for the system is printed

**Exception flows:**  
- EX_01 Wrong number of arguments  
  1. Message is displayed to indicate that the arguments are invalid
  2. The program exits

- EX_02 Invalid Algorithm  
  1. Message is displayed to indicate the algorithm must be 'plurarity' or 'STV'
  2. The program exits
     
**Post conditions:** Election runs and displays results and the program exits

### **4.9	System Feature 9** {#4.9-system-feature-9}

**Name:** Taking in a candidate file and ballot files   
**ID:** UC_009
**Description:** User interface asks for candidate files and election files from user   
**Actors:** User who wants to process an election
**Preconditions:** The system is ready to run and the user has candidate files and ballot files. Files are stored in the same directory as program files. 

**Basic flow:**  
  1. User is prompted for candidate file
  2. User submits filename of candidate file
  3. User is prompted for ballot files
  4. User submits the file names of ballot files   
  
  
**Alternative flows:**  
- AC_01 User wants help  
  1. User enters help command
  2. Tutorial information for the formatting of the file names

**Exception flows:**  
- EX_01 Filenames aren't valid  
  1. Message is displayed to indicate that one of the file names aren't valid
  2. The user is prompted again for the file names
     
**Post conditions:** System stores the information and uses the file names for the rest of the program's flow.

----


## **5\.	Other Nonfunctional Requirements** {#5.-other-nonfunctional-requirements}

### **5.1	Performance Requirements** {#5.1-performance-requirements}

The voting system must be able to process an election in under 5 minutes. The minimum system requirements will be determined later as the program is developed.

### **5.2	Safety Requirements** {#5.2-safety-requirements}

To ensure that user data will not be lost or damaged from use of the program testing will be done on the program to ensure there are no bugs.

### **5.3	Security Requirements** {#5.3-security-requirements}

The voting system doesn't have any security issues that need to be handled. The security of the voting is handled at the voting precinct.

### **5.4	Software Quality Attributes** {#5.4-software-quality-attributes}

The user will be provided with a text based user interface with simple features. It will be well designed and the interactions will be easy for the user to understand. A help window will also be provided on the interface with information to provide details for the user on how to run the program.

### **5.5	Business Rules** {#5.5-business-rules}

If a shuffle needs to be disabled users, programmers, testers and administrators will be able to flag the program to disable the shuffle.

## **6\. Other Requirements** {#6.-other-requirements}

No notable requirements at the moment.

## **Appendix A: Glossary** {#appendix-a:-glossary}

* Droop Formula: ⌊(NumberOfBallots)/(NumberOfSeats + 1)⌋ + 1
* Plurality Voting: A voting system where the candidate who receives the majority of the votes wins.
* STV Voting: A voting system allowing for the ranking of multiple candidates per ballot, where the winning condition is reaching the amount of votes specified by the Droop Formula. While the votes are being counted, a candidate who meets the formula threshold is immediately elected.

## **Appendix B: Analysis Models** {#appendix-b:-analysis-models}

None applicable at the moment.

## **Appendix C: To Be Determined List** {#appendix-c:-to-be-determined-list}

N/A
