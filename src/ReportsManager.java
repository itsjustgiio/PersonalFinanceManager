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
     * @param filePath the path of the file to analyze
     * @param year the year to filter the data (must be non-negative)
     * @param writeToFile true to output to a file, false to output to the console
     * @throws IllegalArgumentException if the year is negative
     */
    public static void analyzeData(String filePath, int year, boolean writeToFile) {
        File file = new File(filePath);
        double[] monthlyIncomes = new double[12];
        double[] monthlyExpenses = new double[12];
        double[] monthlyNet = new double[12];
        double incomeYear = 0;
        double expensesYear = 0;
        double netYear = 0; 
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String[] dateSplit = parts[0].split("/");
                int month = Integer.parseInt(dateSplit[0]) - 1;
                double money = Double.parseDouble(parts[2]);
                if (money < 0){
                    monthlyExpenses[month] -= money;
                    expensesYear -= money;
                }
                else {
                    monthlyIncomes[month] += money;
                    incomeYear += money;
                }
            }

        } catch (IOException e) {
        	System.out.println("Error");
            e.printStackTrace();
        }
        
        for (int i = 0; i < 12; i++) {
            monthlyNet[i] = monthlyIncomes[i] - monthlyExpenses[i];
            System.out.printf("Monthly Income: %.2f, Monthly Expenses: %.2f, Monthly net: %.2f\n", monthlyIncomes[i], monthlyExpenses[i], monthlyNet[i]);
        }
        netYear = incomeYear - expensesYear;
        System.out.printf("Year's Income: %.2f, Year's Expenses: %.2f, Year's net: %.2f \n", incomeYear, expensesYear, netYear);
        
        if (writeToFile)  {
            File reportFile = new File("Report.csv");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(reportFile))) {
            bw.write("Month, income, expenses, net");
            bw.newLine();
            String[] months = {"Januray", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
            for (int i = 0; i < 12; i++) {
                bw.write(String.format("%s, %.2f, %.2f, %.2f, ", months[i], monthlyIncomes[i], monthlyExpenses[i], monthlyNet[i]));
                bw.newLine();
            }
            bw.write(String.format("Year, %.2f, %.2f, %.2f", incomeYear, expensesYear, netYear));
            
            } catch (IOException e) {
                e.printStackTrace();
            }

            
        }

    }
}
