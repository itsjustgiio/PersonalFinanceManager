//File Name: IntegrationModule.java
//Purpose: Integrated Main Menu for PFM system (Beta Version with Account Deletion)
//Team: Integration
//Team Lead: Aye Chan

/* Bug Fix: KAN-16 - Added input validation for numeric menu selection to prevent NumberFormatException
 * Bug Fix: KAN-17 - Added detailed user-friendly error messages for failed module operations
 * Bug Fix: KAN-19 - Added logic to delete user's budget CSV files from pfm_data folder when account is deleted
*/

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
				int choice = -1;
				while (true) {
				    System.out.print("Select an option: ");
				    String input = scanner.nextLine();
				    try {
				        choice = Integer.parseInt(input);
				        break;
				    } catch (NumberFormatException e) {
				        System.out.println("Invalid input. Please enter a number.");
				    }
				}

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
				int option = -1;
				while (true) {
				    System.out.print("Select an option: ");
				    String input = scanner.nextLine();
				    try {
				        option = Integer.parseInt(input);
				        break;
				    } catch (NumberFormatException e) {
				        System.out.println("Invalid input. Please enter a number.");
				    }
				}


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
						ReportsManager.analyzeData(currentUser, year, saveToFile);
					} else {
						System.out.println("Invalid or missing CSV file.");
					}
				} else if (option == 6) {
				    System.out.print("Enter year to perform prediction: ");
				    int year = -1;
				    try {
				        year = Integer.parseInt(scanner.nextLine());
				    } catch (NumberFormatException e) {
				        System.out.println("Invalid year. Please enter a valid numeric year.");
				        continue;
				    }

				    String filePath = System.getProperty("user.dir") + "/pfm_data/" + currentUser.getUsername() + "/" + year + ".csv";
				    File file = new File(filePath);

				    if (!file.exists()) {
				        System.out.println("Prediction failed: No data found for year " + year);
				        System.out.println("Please upload it first using option 1 (Upload or Update Income/Expense CSV).");
				        continue;
				    }

				    try {
				        PredictionManager pd = new PredictionManager(filePath);
				        String status = pd.determineBudgetStatus();
				        System.out.println("Current Budget Status: " + status);

				        if (status.equals("surplus")) {
				            System.out.printf("You can spend an additional: $%.2f%n", pd.determinePossibleAdditionalSpending("Any"));
				        } else if (status.equals("deficit")) {
				            System.out.printf("You need to cut expenses by: $%.2f%n", pd.determineDecreaseForSurplus());
				        } else {
				            System.out.println("Unknown budget status. Please check your data.");
				        }

				    } catch (IOException e) {
				        System.out.println("Failed to run prediction due to a system error.");
				        System.out.println("Details: " + e.getMessage());
				    }
				

				} else if (option == 7) {
				    System.out.print("Are you sure you want to delete your account? (y/n): ");
				    String confirm = scanner.nextLine();

				    if (confirm.equalsIgnoreCase("y")) {
				        // Delete associated files
				        String userDirPath = System.getProperty("user.dir") + "/pfm_data/" + currentUser.getUsername();
				        File userDir = new File(userDirPath);
				        if (userDir.exists() && userDir.isDirectory()) {
				            for (File file : userDir.listFiles()) {
				                if (!file.delete()) {
				                    System.out.println("⚠ Failed to delete file: " + file.getName());
				                }
				            }
				            if (!userDir.delete()) {
				                System.out.println("⚠ Failed to delete user folder: " + userDirPath);
				            }
				        }

				        accountDAO.deleteAccount(currentUser.getId());
				        System.out.println("Your account has been deleted.");
				        System.out.println("Goodbye, " + currentUser.getUsername() + "!");
				        displayLoginMenu();
				        
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
