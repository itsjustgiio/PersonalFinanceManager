
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * The {@code Budget} class will handle all budget related operations such as
 * transactions, updating, creating, deleting, and reading in from CSV files
 *  @author Shaeem Rockcliffe
 *  @author Raphael Spoerri
 *  @author Giovanni Carrion
 *  @author John Ortega
 *  @version %I%, %G%
 */
public class Budget {
    /**
     * Represents one transaction, i.e. one row in the CSV file of transactions.
     * @author Shaeem Rockcliffe
     * @version %I%, %G%
     */
    public class Transaction {
        private int month;
        private int day;
        private int year;
        private String category;
        private long amount;
        
        /**
         * Constructs a {@code Transaction} object, assuming the date and category are valid.
         * @param date the date of the transaction in the format MM/DD/YYYY.
         * @param category the category of the transaction.
         * @param amount the amount in dollars.
         */
        public Transaction(String date, String category, long amount) {
            String[] parts = date.split("/");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid date format: " + date);
            }

            this.month = Integer.parseInt(parts[0]);
            this.day = Integer.parseInt(parts[1]);
            this.year = Integer.parseInt(parts[2]);

            this.category = category;
            this.amount = amount;
        }
        /** Returns the month the transaction took place.
         * @return the month (1-12) of the transaction.
         */
        public int getMonth() { return month; }
        /** Returns the day of the month the transaction took place.
         * @return the day (1-31) of the transaction.
         */
        public int getDay() { return day; }
        /** Returns the year the transaction took place.
         * @return the year (1000-9999) of the transaction (should be the same as the file it came from).
         */
        public int getYear() { return year; }
        /** Returns the category of the transaction.
         * @return the category.
         */
        public String getCategory() { return category; }
        /** Returns the amount of money added or withdrawn.
         * @return the net change in dollars.
         */
        public long getAmount() { return amount; }

        @Override
        public String toString() {
            return String.format("Transaction{%s/%s/%s, %s, %s}", month, day, year, category, amount);
        }
    }

    /**
     * Just for testing.
     * @param args - arguments to program. Ignored
     */
    public static void main(String[] args) {
        try {
            var me = new Account();
            var b = new Budget(me);
            String cmd;
            while (true) {
                System.out.print("enter a command ([l]ist/[r]ead/[u]pdate/[d]elete/[q]uit)\n>>> ");
                if (!scanner.hasNextLine()) break;

                cmd = scanner.next();
                switch (cmd.charAt(0)) {
                    case 'l':
                        for (var year : b.getYears()) System.out.println(year);
                        break;
                    case 'r':
                        for (;;) {
                            System.out.print("Year number: ");
                            if (!scanner.hasNext()) return;
                            if (scanner.hasNextInt()) break;
                            System.err.println("Expected YYYY.");
                            scanner.next();
                        }
                        
                        var csv = b.readCSV(scanner.nextInt());
                        if (csv == null) {
                            System.err.println("Invalid CSV file.");
                            break;
                        }
                        for (var tr : csv)
                            System.out.println(tr);
                        break;
                    case 'd': b.promptToDelete(); break;
                    case 'u': b.promptToCreateOrUpdate(); break;
                    case 'q': System.exit(0);
                    default: System.err.println("Invalid command.");
                }
                System.out.print("\n");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private String userDataDir;
    private static Scanner scanner = new Scanner(System.in);
    /**
     * Constructs a Budget instance for a specific account
     * @param account is the account that will be associated with this budget
     * instance. A valid account needs to be passed in order to create a budget
     * instance.
     */
    public Budget(Account account) {
        String dir = System.getProperty("user.dir");
        userDataDir = dir + "/pfm_data/" + account.getUsername();
        var file = new File(userDataDir);
        if (!file.exists() && !file.mkdirs()) {
            panic("Failed to create directory for user %s.", account.getUsername());
        }
    }

    /**
     * Prompts the user for the path to the file to save. If the file already exists,
     * prompts to confirm overwriting it.<br>
     * Upon expected failures (file does not exist, invalid name, etc.)
     * prints error and returns. Prints error and exits if unexpected I/O error occurs. 
     */
    public void promptToCreateOrUpdate() {
        verifyUserDataDir();

        String inputFilePath = getString("CSV file: ");

        if (inputFilePath.length() < 8) {
            System.err.println("Invalid CSV file. Must be YYYY.csv");
            return;
        }

        String basename = inputFilePath.substring(inputFilePath.length() - 8);

        if (!basename.matches("[1-9]\\d{3}\\.csv")) {
            System.err.println("Invalid CSV file. Must be YYYY.csv.");
            return;
        }

        var inputFile = new File(inputFilePath);
        if (!inputFile.exists()) {
            System.err.println("Cannot find file " + inputFilePath);
            return;
        } else if (inputFile.isDirectory()) {
            System.err.println("Cannot read a directory as a CSV file.");
            return;
        }

        int userYear = Integer.parseInt(basename.substring(0, 4)); // should never throw

        // Validate the year input
        if (userYear < 1000 || userYear > 9999) {
            System.out.println("Invalid year. Please provide a valid year.\n");
            return;
        }
    
    
        String savedFilePath = userDataDir + "/" + userYear + ".csv";
        File savedFile = new File(savedFilePath);
    
        // Check if the file exists, if not, create it
        if (!savedFile.exists()) {
            try {
                if (!savedFile.createNewFile()) {
                    System.err.println("Error: Could not create the file.");
                    return;
                }
            } catch (IOException e) {
                System.err.println("Error creating file: " + e.getMessage());
                return;
            }
        } else {
            // If the file exists, check if it's a directory
            if (savedFile.isDirectory()) {
                System.err.println("Error: A directory with this name already exists.");
                return;
            }

            if (!verifyFileContent(inputFilePath, userYear)) return;

            // Prompt the user to overwrite if the file exists
            String userResponse = getString("CSV data for year already exists. Overwrite it (y/n): ");

            if (!userResponse.equalsIgnoreCase("y") && !userResponse.equalsIgnoreCase("yes")) {
                System.out.println("No changes have been made.");
                return;
            }
        }
    
        // Proceed with copying the file content
        try (BufferedReader fileReader = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter fileWriter = new BufferedWriter(new FileWriter(savedFilePath))) {
    
            String line;
            while ((line = fileReader.readLine()) != null) {
                fileWriter.write(line);
                fileWriter.newLine();
            }
        } catch (IOException e) {
            panic("Unexpected I/O error when saving file: %s.", e.getMessage());
        }

        System.out.println("=> Success.");
    }
    /**
     * Prompts the user for the year number of the file to
     * delete. Upon expected failures (file does not exist, invalid name, etc.)
     * prints error and returns. Prints error and exits if unexpected I/O error occurs. 
     */
    void promptToDelete() {
        verifyUserDataDir();  // Ensures directory exists and is valid
        System.out.print("Year number: ");
        if (!scanner.hasNextLine()) System.exit(0);

        if (!scanner.hasNextInt()) {
            System.err.println("Error: Year must be an integer.");
            return;
        }
        
        int year = scanner.nextInt();
        
        if (year < 1000 || year > 9999) {
            System.err.println("Error: Year must be a 4-digit number.");
            return;
        }
    
        File fileToDelete = new File(userDataDir + "/" + year + ".csv");
    
        if (!fileToDelete.exists()) {
            System.err.println("Error: File does not exist for year " + year + ".");
            return;
        }
    
        if (fileToDelete.isDirectory()) {
            System.err.println("Error: Expected a file, but found a directory.");
            return;
        }
    
        if (!fileToDelete.delete()) {
            System.err.println("Failed to delete file " + fileToDelete.getAbsolutePath());
            return;
        }
    
        System.out.println("Successfully deleted: " + fileToDelete.getName());
    }
    
    /**
     * Reads a CSV file for a given year and returns a list of transactions, or null if
     * the file for that year is missing, has the wrong type, or contains invalid data.
     * @param year the year to read.
     * @return list of transactions from the file, or null.
     */
    public ArrayList<Transaction> readCSV(int year) {
        verifyUserDataDir(); 
    
        String filename = userDataDir + "/" + year + ".csv";
        File file = new File(filename);
    
        if (!file.exists()) {
            System.err.println("Error: File not found: " + filename);
            return null;
        }
    
        if (file.isDirectory()) {
            System.err.println("Error: Expected a file but found a directory: " + filename);
            return null;
        }
    
        if (!verifyFileContent(filename, year)) return null;
    
        ArrayList<Transaction> transactions = new ArrayList<>();
    
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
    
                if (!ValidationManager.CheckCSVContent.validateLine(year, line)) {
                    System.err.println("Skipping invalid line: " + line);
                    continue;
                }
    
                String[] parts = line.split(",");
                String date = parts[0].trim();
                String category = parts[1].trim();
                long amount = Long.parseLong(parts[2].trim());
    
                transactions.add(new Transaction(date, category, amount));
            }
        } catch (IOException e) {
            panic("Failed to read file '%s': %s", filename, e.getMessage());
            // unreachable
        }
    
        return transactions;
    }

    /**
     * Returns a list of years (based on files present in the saved files
     * directory), or null upon failure to read the data directory.
     * @return list of years, or null
     */
    public ArrayList<Integer> getYears() {
        verifyUserDataDir(); 
    
        ArrayList<Integer> years = new ArrayList<>();
        File directory = new File(userDataDir);
    
        File[] files = directory.listFiles();
        if (files == null) {
            System.err.println("Failed to fetch user data files");
            return null;
        }
    
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".csv")) {
                String filename = file.getName().replace(".csv", "");
                try {
                    int year = Integer.parseInt(filename);
                    if (year >= 1000 && year <= 9999) {
                        years.add(year);
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid filenames
                }
            }
        }
    
        return years;
    }

    private void panic(String msg, Object ...args) {
        System.err.println("Fatal error: " + String.format(msg, args));
        System.exit(1);
    }

    /**
     * Whenever accessing the directory {@code userDataDir}, call this method first
     * to ensure the directory is valid.
     */
    private void verifyUserDataDir() {
        var file = new File(userDataDir);
        if (!file.exists()) {
            panic("Internal storage is corrupt: Directory %s is missing.", userDataDir);
        }
        if (!file.isDirectory()) {
            panic("Internal storage is corrupt: %s is not a directory.", userDataDir);
        }
    }


    private String getString(String prompt) {
        System.out.print(prompt);

        if (!scanner.hasNext()) {
            panic("Unexpected end of the input, exiting.");
        }

        return scanner.next();
    }

    private boolean verifyFileContent(String filePath, int year) {
        boolean isValid = ValidationManager.CheckCSVContent.validateWholeCSVFile(year, filePath);
            
        // If invalid, prompt the user
        if (!isValid) {
            String userResponse = getString("The CSV file contains invalid records. Continue anyway? (y/n): ");
            if (!userResponse.equals("y") && !userResponse.equals("yes")) {
                System.out.println("No changes have been made.");
                return false;
            }
        }
        return true;
    }
}
