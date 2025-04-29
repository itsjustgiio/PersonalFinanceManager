//File Name: IntegrationModule.java
//Purpose: Contains both MainMenu and ModuleLoader classes for the Integration team
//Team: Integration
//Team Lead: Aye Chan
//Team : Zwe Yee Tun
//	   : Jacob Li
//     : Jason Zhao

import java.io.*;
import java.util.*;

public class IntegrationModule {

	// Displays the primary interface
	// Allows users to navigate to different functional module of the PFM system.

	public static class MainMenu {

		private Account currentUser;

		private AccountDAO accountDAO;
		private AuthService authService;
		private Budget budget;

		public MainMenu() {
			accountDAO = new AccountDAO();
			authService = new AuthService(accountDAO);
		}

//User Authentication : Register or Login
		public void displayLogin() {

			Scanner scanner = new Scanner(System.in);

			while (true) {
				System.out.println("\n1. Register\n2. Login\n3. Exit");
				System.out.print("Select an option: ");
				int choice = Integer.parseInt(scanner.nextLine());

				if (choice == 1) {
					System.out.print("Username: ");
					String username = scanner.nextLine();
					System.out.print("Email: ");
					String email = scanner.nextLine();
					System.out.print("Password: ");
					String password = scanner.nextLine();

					boolean success = authService.register(username, email, password);
					System.out.println(success ? "Account created successfully!" : "Username already exists.");
				} else if (choice == 2) {
					System.out.print("Username: ");
					String username = scanner.nextLine();
					System.out.print("Password: ");
					String password = scanner.nextLine();

					Account account = authService.login(username, password);
					if (account != null) {
						System.out.println("Welcome, " + account.getUsername() + "!");
						currentUser = account;
						try {
							budget = new Budget(currentUser);
						} catch (IOException e) {
							System.out.println("Failed to initialize budget storage.");
						}
						displayMainMenu();
					} else {
						System.out.println("Invalid username or password.");
					}
				} else {
					System.out.println("Exiting...");
					break;
				}
			}

			scanner.close();
		}

		// Displays the main user menu option after successful login
		public void displayMainMenu() {
			Scanner scanner = new Scanner(System.in);

			while (true) {
				System.out.println("\nMain Menu");
				System.out.println("1. Upload or Update Income/Expense CSV");
				System.out.println("2. List Loaded Budget Years");
				System.out.println("3. View Transactions for a Year");
				System.out.println("4. Delete Budget for a Year");
				System.out.println("5. Generate Financial Report");
				System.out.println("6. Perform What-If Budget Prediction");
				System.out.println("7. Logout");
				System.out.print("Select an option: ");

				int option = Integer.parseInt(scanner.nextLine());
				//Upload or Update Income/Expense CSV File
				if (option == 1) {
					try {
						budget.promptToCreateOrUpdate();
					} catch (IOException e) {
						System.out.println("Error loading or updating budget.");
					}
					//List All Loaded Budget Years
				} else if (option == 2) {
					ArrayList<Integer> years = budget.getYears();
					if (years.isEmpty()) {
						System.out.println("No budgets found.");
					} else {
						System.out.println("Loaded Budget Years:");
						for (Integer year : years) {
							System.out.println("- " + year);
						}
					}
					//View All Transactions for a Selected Year
				} else if (option == 3) {
					System.out.print("Enter the year to view transactions: ");
					int year = Integer.parseInt(scanner.nextLine());
					try {
						ArrayList<Budget.Transaction> transactions = budget.readCSV(year);
						if (transactions.isEmpty()) {
							System.out.println("No transactions found for year " + year);
						} else {
							System.out.println("Transactions:");
							for (Budget.Transaction tr : transactions) {
								System.out.printf("%s, %s, %d\n", tr.getDate(), tr.getCategory(), tr.getAmount());
							}
						}
					} catch (IOException e) {
						System.out.println("Error reading transactions: " + e.getMessage());
					}
					//Delete an Existing Budget Year
				} else if (option == 4) {
					try {
						budget.promptToDelete();
					} catch (IOException e) {
						System.out.println("Error deleting budget file.");
					}
					//Generate Financial Summary Report
				} else if (option == 5) {
					System.out.print("Enter year to generate report: ");
					int year = Integer.parseInt(scanner.nextLine());
					String filePath = System.getProperty("user.dir") + "/pfm_data/" + currentUser.getUsername() + "/"
							+ year + ".csv";
					if (ValidationManager.CheckCSVFileFormat.validCSVFile(filePath)) {
						System.out.print("Save report to a file? (y/n): ");
						boolean saveToFile = scanner.nextLine().equalsIgnoreCase("y");
						ReportsManager.analyzeData(filePath, year, saveToFile);
					} else {
						System.out.println("Invalid or missing CSV file.");
					}
					//Perform What-If Budget Scenario Prediction
				} else if (option == 6) {
					System.out.print("Enter year to perform prediction: ");
					int year = Integer.parseInt(scanner.nextLine());
					try {
						ArrayList<Budget.Transaction> transactions = budget.readCSV(year);
						double income = 0, expense = 0;
						for (Budget.Transaction t : transactions) {
							if (t.getAmount() >= 0)
								income += t.getAmount();
							else
								expense += Math.abs(t.getAmount());
						}
						PredictionManager predictor = new PredictionManager(income, expense);
						String status = predictor.determineBudgetStatus();
						System.out.println("Current Budget Status: " + status);
						if (status.equals("surplus")) {
							System.out.println("You can spend an additional: $"
									+ (predictor.determinePossibleAdditionalSpending("any") / 100.0));
						} else if (status.equals("deficit")) {
							System.out.println("You need to cut expenses by: $"
									+ (predictor.determineDecreaseForSurplus() / 100.0));
						}
					} catch (IOException e) {
						System.out.println("Failed to read transactions: " + e.getMessage());
					}
					//Logout and Return to Login Menu
				} else if (option == 7) {
					logoutUser();
					break;
				} else {
					System.out.println("Invalid option. Please try again.");
				}
			}

		}
		//Logout the Current User
		public void logoutUser() {
			if (currentUser != null) {
				System.out.println("Goodbye, " + currentUser.getUsername() + "!");
			} else {
				System.out.println("Goodbye!");
			}
			currentUser = null;
		}

	}

	public static void main(String[] args) {
		MainMenu m = new MainMenu();

		m.displayLogin();

	}

}