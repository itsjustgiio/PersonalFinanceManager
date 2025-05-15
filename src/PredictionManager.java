import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * @author Mir Haque, Raian Pial, Mahdi Mahin, Atif Tausif
 * @version 2.2, May 2025
 */
public class PredictionManager {

    private int totalIncome;
    private int totalExpenses;

    public String priority1;
    public String priority2;
    public String priority3;

    /**
     * Constructor 
     * @param csvFilePath
     * @throws IOException
     */
    public PredictionManager(String csvFilePath) throws IOException {
        parseCSV(csvFilePath);
    }

    /**
     * Initializes totalIncome and totalExpenses from the CSV file
     *
     * @param filePath
     * @throws IOException
     */
    private void parseCSV(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;

        totalIncome = 0;
        totalExpenses = 0;
        Integer expectedYear = null;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            line = line.trim();
            if (line.isEmpty()) continue;

            // Skip header line if detected
            if (lineNumber == 1
                    && line.toLowerCase().contains("date")
                    && line.contains("category")) {
                continue;
            }

            String[] parts = line.split(",");
            if (parts.length != 3) {
                System.err.println("Skipping malformed line " + lineNumber + ": " + line);
                continue;
            }

            String date = parts[0].trim();
            String category = parts[1].trim();
            String amountStr = parts[2].trim();

            // Check if date format is valid
            if (!ValidationManager.CheckCSVContent.validDateFormat(date)) {
                System.err.println("Invalid date format at line " + lineNumber + ": " + date);
                continue;
            }

            // Extract and compare year
            int currentYear = Integer.parseInt(date.split("/")[2]);
            if (expectedYear == null) {
                expectedYear = currentYear;
            } else if (currentYear != expectedYear) {
                reader.close();
                throw new IllegalArgumentException("Mismatch: Expected year "
                        + expectedYear + " but found " + currentYear
                        + " at line " + lineNumber);
            }

            // Check if category is valid
            if (!ValidationManager.CheckCSVContent.validCategories(category)) {
                System.err.println("Invalid category at line " + lineNumber + ": " + category);
                continue;
            }

            // Check if amount is valid
            if (!ValidationManager.CheckCSVContent.validDollarAmount(amountStr)) {
                System.err.println("Invalid dollar amount at line " + lineNumber + ": " + amountStr);
                continue;
            }

            int amount = Integer.parseInt(amountStr);
            if (amount > 0) {
                totalIncome += amount;
            } else {
                totalExpenses += Math.abs(amount);
            }
        }

        reader.close();

        if (expectedYear == null) {
            throw new IllegalArgumentException("CSV file contains no valid dated transactions.");
        }
    }

    /** Getters and budget-status methods omitted for brevity */

    public int getTotalExpenses() { return totalExpenses; }
    public int getTotalIncome()   { return totalIncome; }

    public String determineBudgetStatus() {
        if (totalIncome > totalExpenses) return "surplus";
        if (totalIncome < totalExpenses) return "deficit";
        return "balanced";
    }

    public int determineIncreaseForDeficit() {
        if (totalIncome > totalExpenses) {
            return (totalIncome - totalExpenses) + 1;
        }
        return 0;
    }

    public int determineDecreaseForSurplus() {
        if (totalExpenses > totalIncome) {
            return (totalExpenses - totalIncome) + 1;
        }
        return 0;
    }

    public int determinePossibleAdditionalSpending(String category) {
        if ("deficit".equals(determineBudgetStatus())) {
            return 0;
        }
        return totalIncome - totalExpenses;
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
     * Clears all budget priorities.
     */
    public void clearBudgetPriorities() {
        priority1 = null;
        priority2 = null;
        priority3 = null;
    }

    /**
     * Removes a single priority category and shifts any lower priorities up.
     *
     * @param category the category to remove from priorities
     */
    public void removeBudgetPriority(String category) {
        if (category == null) return;

        if (category.equals(priority1)) {
            priority1 = priority2;
            priority2 = priority3;
            priority3 = null;
        } else if (category.equals(priority2)) {
            priority2 = priority3;
            priority3 = null;
        } else if (category.equals(priority3)) {
            priority3 = null;
        } else {
            System.out.println("Category '" + category + "' is not a current priority.");
        }
    }

    /**
     * Change spending on a certain category to see how it would affect total expenses.
     * Priority categories have multiplier effects:
     * 1. Top priority: no change
     * 2. Second priority: 50%
     * 3. Third priority: 25%
     *
     * Prevents applying leftover to the same category twice.
     *
     * @param category the category to modify
     * @param amount   the amount to modify by
     */
    public void modifySpending(String category, int amount) {
        int adjustedAmount = amount;
        int remaining = 0;

        // Adjust based on priority
        if (category.equals(priority1)) {
            System.out.println("Cannot modify spending in top priority category (" + category + ").");
            remaining = amount;
            adjustedAmount = 0;
        } else if (category.equals(priority2)) {
            adjustedAmount = amount / 2;
            remaining = amount - adjustedAmount;
        } else if (category.equals(priority3)) {
        	adjustedAmount = (amount / 4) * 3;
            remaining = amount - adjustedAmount;
        }

        totalExpenses += adjustedAmount;
        if (totalExpenses < 0) totalExpenses = 0;

        // If there's leftover, prompt for a different category
        String secondaryCategory = null;
        if (remaining > 0) {
            Scanner scanner = new Scanner(System.in);
            do {
                System.out.print("Enter another category to adjust by $" + remaining + ": ");
                secondaryCategory = scanner.nextLine().trim();
                if (secondaryCategory.equals(category)) {
                    System.out.println("Cannot adjust the same category again. Please choose a different category.");
                }
            } while (secondaryCategory.equals(category));

            totalExpenses += remaining;
            if (totalExpenses < 0) totalExpenses = 0;

            System.out.println("Adjusted spending in " + secondaryCategory + " by $" + remaining);
        }

        // Savings projections
        int annualSavings = (int) (totalIncome - totalExpenses);
        System.out.println("\n--- Savings Projection ---");
        System.out.println("Annual Savings: $" + annualSavings);
        System.out.println("Savings over 2 years: $" + (annualSavings * 2));
        System.out.println("Savings over 5 years: $" + (annualSavings * 5));

        System.out.println("\n--- Category-Specific Savings Projection ---");
        if (adjustedAmount > 0) {
            System.out.printf("Category: %-20s | Saved This Year: $%d | 2 Years: $%d | 5 Years: $%d\n",
                    category, adjustedAmount, adjustedAmount * 2, adjustedAmount * 5);
        }
        if (secondaryCategory != null && remaining > 0) {
            System.out.printf("Category: %-20s | Saved This Year: $%d | 2 Years: $%d | 5 Years: $%d\n",
                    secondaryCategory, remaining, remaining * 2, remaining * 5);
        }
    }
}