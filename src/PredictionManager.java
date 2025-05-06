import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Scanner;

/**
 * @author Mir Haque, Raian Pial, Mahdi Mahin, Atif Tausif
 * @version 2.0, May 2025
 */

 public class PredictionManager {

    /*
      NOTES:
      Still working on using the CSV File
      All methods are public...will work on security later
      We are debating if the constructor should even have parameters because we are already given the income and expenses from the CSV File
      Modify Spending doesn't take into account the priority categories
    */

    private double totalIncome;
    private double totalExpenses;

    private String priority1;
    private String priority2;
    private String priority3;

    /**
     * Constructor 
     * @param csvFilePath
     * @throws IOException
     */
    public PredictionManager(String csvFilePath) throws IOException {
        parseCSV(csvFilePath);
    }

    /**
     * Intializes totalIncome and totalExpenses from the CSV file
     * 
     * @param filePath
     * @throws IOException
     */
    private void parseCSV(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        boolean headerSkipped = false;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        HashSet<Integer> years = new HashSet<>();

        while ((line = reader.readLine()) != null) {
            if (!headerSkipped) {
                headerSkipped = true;
                continue; // skip header
            }

            String[] parts = line.split(",");
            if (parts.length != 3) continue;

            String dateStr = parts[0].trim();
            String category = parts[1].trim();
            double amount;

            try {
                amount = Double.parseDouble(parts[2].trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount, skipping line: " + line);
                continue;
            }

            try {
                LocalDate date = LocalDate.parse(dateStr, formatter);
                years.add(date.getYear());
            } catch (Exception e) {
                System.out.println("Invalid date format, skipping line: " + line);
                continue;
            }

            if (amount > 0) {
                totalIncome += amount;
            } else {
                totalExpenses += Math.abs(amount);
            }
        }

        reader.close();

        if (years.size() != 1) {
            throw new IllegalArgumentException("All transactions must be from the same year.");
        }
    }

   /**
    * Returns the total expenses.
    *
    * @return totalExpenses
    */
    public double getTotalExpenses() {
        return totalExpenses;
   }

   /**
    * Returns the total income.
    *
    * @return totalIncome
    */
    public double getTotalIncome() {
        return totalIncome;
    }

   /**
    * Compares income to expenses and tells you if you're doing fine or not.
    *
    * @return "surplus", "deficit", or "balanced"
    */
    public String determineBudgetStatus() {
        if (totalIncome > totalExpenses) {
           return "surplus";
        } else if (totalIncome < totalExpenses) {
           return "deficit";
        } else {
           return "balanced";
        }
   }

    /**
    * Shows how much more you'd have to spend to end up in a deficit.
    *
    * @return extra amount needed to tip the budget into a deficit
    */
    public double determineIncreaseForDeficit() {
        if (totalIncome > totalExpenses) {
            return (totalIncome - totalExpenses) + 0.01; // spending 1 cent more causes deficit
        }
        return 0;
    }

    /**
    * Tells you how much you'd need to cut from your spending to move into a surplus.
    *
    * @return minimum amount you’d need to reduce expenses by
    */
    public double determineDecreaseForSurplus() {
        if (totalExpenses > totalIncome) {
            return (totalExpenses - totalIncome) + 0.01; // reducing 1 cent below income
        }
        return 0;
    }

    /**
    * How much more you could spend in a specific category without going into a deficit.
    *
    * @param category the expense category
    * @return how much more they can spend while staying in surplus
    */
    public double determinePossibleAdditionalSpending(String category) {
        if (determineBudgetStatus().equals("deficit")) {
            return 0;
        }
        double surplus = totalIncome - totalExpenses;
        return surplus;
    }

    /**
    * Set a given category to be higher priority (up to 3).
    * 
    * @param category the category to set as a priority
    */
    public void setBudgetPriorities(String category) {
        if (priority1 == null) {
            priority1 = category;
        } else if (priority2 == null) {
            priority2 = category;
        } else if (priority3 == null) {
            priority3 = category;
        } else {
           System.out.println("Already set 3 priority categories.");
        }
    }


   /**
    * Change spending on a certain category to see how it would affect the total expenses over the course of time
    *
    * @param category the category to modify
    * @param amount   the amount to modify by
    */
    public void modifySpending(String category, int amount) {
    double adjustedAmount = amount;
    double remaining = 0;

    // Adjust amount based on priority level
    if (category.equals(priority1)) {
        System.out.println("Cannot modify spending in top priority category (" + category + ").");
        remaining = amount;
        adjustedAmount = 0;
    } else if (category.equals(priority2)) {
        adjustedAmount = amount * 0.5;
        remaining = amount - adjustedAmount;
    } else if (category.equals(priority3)) {
        adjustedAmount = amount * 0.25;
        remaining = amount - adjustedAmount;
    }

    totalExpenses += adjustedAmount;

    if (totalExpenses < 0) {
        totalExpenses = 0;
    }

    // Ask for another category if the adjustment was limited due to priority
    if (remaining != 0) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter another category to adjust by $" + String.format("%.2f", remaining) + ": ");
        String secondaryCategory = scanner.nextLine();

        totalExpenses += remaining;

        if (totalExpenses < 0) {
            totalExpenses = 0;
        }

        System.out.println("Adjusted spending in " + secondaryCategory + " by $" + String.format("%.2f", remaining));
    }

    // Calculate savings over 1, 2, and 5 years
    double annualSavings = totalIncome - totalExpenses;

    System.out.println("\n--- Savings Projection ---");
    System.out.printf("Annual Savings: $%.2f\n", annualSavings);
    System.out.printf("Savings over 2 years: $%.2f\n", annualSavings * 2);
    System.out.printf("Savings over 5 years: $%.2f\n", annualSavings * 5);
    }
}