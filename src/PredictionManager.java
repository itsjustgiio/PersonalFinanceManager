/**
 * @author Mir Haque, Raian Pial, Mahdi Mahin, Atif Tausif
 * @version 1.1, April 2025
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


   public PredictionManager(double totalIncome, double totalExpenses) {
       this.totalIncome = totalIncome;
       this.totalExpenses = totalExpenses;
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
    * Change spending on a certain category to see how it would affect the total expenses
    *
    * @param category the category to modify
    * @param amount   the amount to modify by
    */
   public void modifySpending(String category, int amount) {
       // Just modify totalExpenses, since we don't track individual category spending here
       totalExpenses += amount;
       if (totalExpenses < 0) {
           totalExpenses = 0;
       }
   }
}