
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

//class Account {
//    String getUsername() {
//        return "";
//    }
//}

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
    // for testing:
    public static void main(String[] args) {
        try {
            var me = new Account();
            var b = new Budget(me);
            var sc = new Scanner(System.in);
            String cmd;
            while (true) {
                System.out.print("enter a command ([l]ist/[r]ead/[u]pdate/[d]elete/[q]uit)\n>>> ");
                cmd = sc.next();
                switch (cmd.charAt(0)) {
                    case 'l':
                        for (var year : b.getYears()) System.out.println(year);
                        break;
                    case 'r':
                        System.out.print("year #: ");
                        for (var tr : b.readCSV(sc.nextInt()))
                            System.out.printf(
                                "%s,%s,%s\n", tr.getDate(), tr.getAmount(), tr.getDate());
                        break;
                    case 'd': b.promptToDelete(); break;
                    case 'u': b.promptToCreateOrUpdate(); break;
                    case 'q': System.exit(0);
                    default: System.err.println("Invalid command.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private String userDataDir;

    /**
     * Constructs a Budget instance for a specific account
     * @params account is the account that will be associated with this budget
     * instance. A valid account needs to be passed in order to create a budget
     * instance.
     */
    public Budget(Account account) throws IOException {
        String dir = System.getProperty("user.dir");
        userDataDir = dir + "/pfm_data/" + account.getUsername();
        var file = new File(userDataDir);
        if (!file.exists() && !file.mkdirs()) {
            throw new IOException("Failed to create directory for user " + account.getUsername());
        }
    }

    /**
     * Represents one transaction, i.e. one row in the CSV file of transactions.
     * @author Shaeem Rockcliffe
     * @author Raphael Spoerri
     * @author Giovanni Carrion
     * @author John Ortega
     * @version %I%, %G%
     */
    public static class Transaction {
        private String date;
        private String category;
        private long amount;

        public Transaction(String date, String category, long amount) {
            this.date = date;
            this.category = category;
            this.amount = amount;
        }

        /**
         * Returns the date the transaction took place, in the format MM/DD/YYYY.
         * @return the date of the transaction
         */
        public String getDate() {
            return date;
        }

        /**
         * Returns a string categorizing the transaction.
         * @return the transaction category
         */
        public String getCategory() {
            return category;
        }

        /**
         * Returns the net change (in cents) to the user's bank account, positive if
         * money was added and negative if money was spent.
         * @return the net change
         */
        public long getAmount() {
            return amount;
        }
    }
    /**
     * Prompts the user for the year number for creating or
     * updating the user's file.
     */
    public void promptToCreateOrUpdate() throws IOException {
        /* not implemented yet */
        Scanner userInput = new Scanner(System.in);

        System.out.print("Enter the year: ");
        int userYear = userInput.nextInt();
        userInput.nextLine();

        if (userYear < 1000 || userYear > 9999) {
            System.out.println("Invalid year. Please provide a valid year.\n");
            return;
        }

        String filename = userDataDir + "/" + userYear + ".csv";
        File savedFile = new File(filename);

        if (savedFile.exists()) {
            System.out.print(
                "CSV data for year + " + userYear + " already exists. Overwrite it (y/n): ");
            String userResponse = userInput.next();

            if (userResponse.toLowerCase().equals("n")) {
                System.out.println("No changes have been made.");
                return;
            }
        }

        if (!savedFile.exists()) {
            savedFile.createNewFile();
        }
        String content = "";

        try (var fileReader = new Scanner(new File(userYear + ".csv"))) {
            while (fileReader.hasNextLine()) {
                content += fileReader.nextLine() + "\n";
            }
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        }

        try (FileWriter fileWriter = new FileWriter(savedFile)) {
            fileWriter.write(content);
            System.out.println("File created/updated successfully: " + savedFile.getName());
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
    /**
     * Prompts the user for`` the year number of the file to
     * delete.
     */
    void promptToDelete() throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the year of the file you want to delete: ");
        String year = scanner.nextLine();

        File fileToDelete = new File(userDataDir + "/" + year + ".csv");

        if (fileToDelete.exists() && fileToDelete.delete()) {
            System.out.println("Successfully deleted: " + fileToDelete.getName());
        } else {
            System.out.println("Failed to delete: " + fileToDelete.getName());
        }
    }
    /**
     * Reads a CSV file for a given year and returns a list of transactions.
     * @param year the year to read
     * @return list of transactions from the file
     * @throws IOException if the file cannot be read
     */
    public ArrayList<Transaction> readCSV(int year) throws IOException {
        ArrayList<Transaction> transactions = new ArrayList<>();
        String filename = userDataDir + "/" + year + ".csv";
        File file = new File(filename);

        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filename);
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",");

                if (parts.length != 3) {
                    System.out.println("Skipping invalid line: " + line);
                    continue;
                }

                String date = parts[0].trim();
                String category = parts[1].trim();
                long amount = Long.parseLong(parts[2].trim());

                transactions.add(new Transaction(date, category, amount));
            }
        }
        return transactions;
    }

    /**
     * Returns a list of years (based on files present in the saved files
     * directory).
     * @return list of years
     */
    public ArrayList<Integer> getYears() {
        ArrayList<Integer> years = new ArrayList<>();
        File directory = new File(userDataDir);

        if (!directory.exists() || !directory.isDirectory()) {
            return years; // Return empty list if null
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return years;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".csv")) {
                String filename = file.getName().replace(".csv", "");
                try {
                    int year = Integer.parseInt(filename);
                    years.add(year);
                } catch (NumberFormatException e) {
                    // Ignore files that are not valid years
                }
            }
        }

        return years;
    }
}
