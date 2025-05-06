//File Name: IntegrationModule.java
//Purpose: Integrated Main Menu for PFM system (Beta Version with Account Deletion)
//Team: Integration
//Team Lead: Aye Chan

import java.util.*;
import java.io.*;

public class IntegrationModule {

	public static class MainMenu {

		private Account currentUser;
		private AccountDAO accountDAO;
		private AuthService authService;
		private Budget budget;

		public MainMenu() {
			accountDAO = new AccountDAO();
			authService = new AuthService(accountDAO);
		}

		public void displayLoginMenu() {
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
						budget = new Budget(currentUser);
						displayMainMenu();
					} else {
						System.out.println("Invalid username or password.");
					}
				} else {
					System.out.println("Exiting...");
					break;
				}
			}
		}

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
				System.out.println("7. Delete My Account");
				System.out.println("8. Logout");
				System.out.print("Select an option: ");

				int option = Integer.parseInt(scanner.nextLine());

				if (option == 1) {
					budget.promptToCreateOrUpdate();
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
				} else if (option == 3) {
					System.out.print("Enter the year to view transactions: ");
					int year = Integer.parseInt(scanner.nextLine());
					ArrayList<Budget.Transaction> transactions = budget.readCSV(year);
					if (transactions == null || transactions.isEmpty()) {
						System.out.println("No transactions found for year " + year);
					} else {
						System.out.println("Transactions:");
						for (Budget.Transaction tr : transactions) {
							System.out.printf("%02d/%02d/%04d, %s, %d\n", tr.getMonth(), tr.getDay(), tr.getYear(),
									tr.getCategory(), tr.getAmount());
						}
					}
				} else if (option == 4) {
					budget.promptToDelete();
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
				} else if (option == 6) {
					System.out.print("Enter year to perform prediction: ");
					int year = Integer.parseInt(scanner.nextLine());
					String filePath = System.getProperty("user.dir") + "/pfm_data/" + currentUser.getUsername() + "/"
							+ year + ".csv";
					File file = new File(filePath);
					if (!file.exists()) {
						System.out.println("Prediction failed: File does not exist for year " + year);
						continue;
					}
					try {
						PredictionManager pd = new PredictionManager(filePath);
						String status = pd.determineBudgetStatus();
						System.out.println("Current Budget Status: " + status);

						if (status.equals("surplus")) {
							System.out.printf("You can spend an additional: $%.2f\n",
									pd.determinePossibleAdditionalSpending("Any"));
						} else if (status.equals("deficit")) {
							System.out.printf("You need to cut expenses by: $%.2f\n", pd.determineDecreaseForSurplus());
						}
					} catch (IOException e) {
						System.out.println("Failed to read prediction file: " + e.getMessage());
					}
				} else if (option == 7) {
					System.out.print("Are you sure you want to delete your account? (y/n): ");
					String confirm = scanner.nextLine();
					if (confirm.equalsIgnoreCase("y")) {
						accountDAO.deleteAccount(currentUser.getId());
						System.out.println("Your account has been deleted.");
						logoutUser();
						break;
					} else {
						System.out.println("Account deletion canceled.");
					}
				} else if (option == 8) {
					logoutUser();
					break;
				} else {
					System.out.println("Invalid option. Please try again.");
				}
			}
		}

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
		MainMenu menu = new MainMenu();
		menu.displayLoginMenu();
	}
}
