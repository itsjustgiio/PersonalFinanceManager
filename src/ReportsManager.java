import java.io.*;

/**
 * Utility class for analyzing and reporting financial data.
 *
 * <p>This class provides static methods to process yearly financial records
 * and output summaries to either a CSV file or directly to the console.</p>
 *
 * <p>Instances of this class are not allowed.</p>
 */
class ReportsManager { 

    /**
     * Private constructor to prevent instantiation.
     */
    private ReportsManager() {}

    /**
     * Analyzes financial data from the given file and outputs a report consisting of 3 columns per month and 
     * total over the year: income, expenses, and net income 
     *
     * @param account The account being analyzed
     * @param year the year to find and filter the data (must be non-negative)
     * @param writeToFile true to output to a file, false to output to the console
     * @throws IllegalArgumentException if the year is not in YYYY format
     */
    public static void analyzeData(Account account, int year, boolean writeToFile) throws IllegalArgumentException {
        if (year < 1000 || year > 9999) {
            throw new IllegalArgumentException("Invalid Year");
        }
        String userDirectory = System.getProperty("user.dir");
        userDirectory = userDirectory + "/pfm_data/" + account.getUsername() + "/";
        String filePath = userDirectory + year + ".csv";
        boolean isValid = ValidationManager.CheckCSVFileFormat.validCSVFile(filePath) && ValidationManager.CheckCSVContent.validateWholeCSVFile(year, filePath);
        if (isValid == false) {
            System.err.println("Error: Invalid file. Aborting.");
            return;
        }
        File file = new File(filePath);
        double[] monthlyIncomes = new double[12];
        double[] monthlyExpenses = new double[12];
        double[] monthlyNet = new double[12];
        double incomeYear = 0;
        double expensesYear = 0;
        double netYear = 0; 
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String[] dateSplit = parts[0].split("/");
                int month = Integer.parseInt(dateSplit[0]) - 1;
                double money = Double.parseDouble(parts[2]);
                if (money < 0){
                    monthlyExpenses[month] -= money;
                    monthlyNet[month] += money;
                    expensesYear -= money;
                }
                else {
                    monthlyIncomes[month] += money;
                    monthlyNet[month] += money;
                    incomeYear += money;
                }
            }

        } catch (IOException e) {
        	System.out.println("Error");
            e.printStackTrace();
        }
        netYear = incomeYear - expensesYear;
        String yearString = "" + incomeYear;
        int numberSpacing = yearString.length() + 1;
        if (numberSpacing < 8) numberSpacing = 8;
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        if (!writeToFile)  {
            System.out.println(String.format("%-9s | %-" + numberSpacing + "s | %-" + numberSpacing + "s | %s", "Month", "Income", "Expenses", "Net"));
            System.out.println();
            for (int i = 0; i < 12; i++) {
                System.out.println(String.format("%-9s | %-" + numberSpacing + ".2f | %-" + numberSpacing + ".2f | %.2f", months[i], monthlyIncomes[i], monthlyExpenses[i], monthlyNet[i]));
            }
            System.out.println(String.format("%-9s | %-" + numberSpacing + ".2f | %-" + numberSpacing + ".2f | %.2f", year, incomeYear, expensesYear, netYear));
        }
        else {
            String reportFilePath = userDirectory + "Report" + year + ".csv";
            File reportFile = new File(reportFilePath);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(reportFile))) {
            bw.write("Month, income, expenses, net");
            bw.newLine();
            for (int i = 0; i < 12; i++) {
                bw.write(String.format("%s, %.2f, -%.2f, %.2f, ", months[i], monthlyIncomes[i], monthlyExpenses[i], monthlyNet[i]));
                bw.newLine();
            }
            bw.write(String.format("Year, %.2f, -%.2f, %.2f", incomeYear, expensesYear, netYear));
            
            System.out.println("Saved in user " + account.getUsername() + "'s user directory");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
            


    }

}
