//File Name: IntegrationModule.java

//Purpose: Integrated Main Menu for PFM system (Beta Version with Account Deletion)
//Team: Integration
//Team Lead: Aye Chan

/* Bug Fix: KAN-16 - Added input validation for numeric menu selection to prevent NumberFormatException - Aye Chan
 * Bug Fix: KAN-17 - Added detailed user-friendly error messages for failed module operations - Aye Chan
 * Bug Fix: KAN-19 - Added logic to delete user's budget CSV files from pfm_data folder when account is deleted - Aye Chan
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
					String username = scanner.nextLine().trim();
					System.out.print("Email: ");
					String email = scanner.nextLine().trim();
					System.out.print("Password: ");
					String password = scanner.nextLine().trim();
					System.out.print("Secret Question: ");
					String secretQuestion = scanner.nextLine().trim();
					System.out.print("Secret Answer: ");
					String secretAnswer = scanner.nextLine().trim();

					boolean success = authService.register(username, email, password, secretQuestion, secretAnswer);
					if (success) {
						System.out.println("Account created successfully!");
					}

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
				System.out.println("7. Change Password ");
				System.out.println("8. Delete My Account");
				System.out.println("9. Logout");
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
						System.out.println("No data found for year " + year);
						System.out.println(
								"Please upload it first using option 1 (Upload or Update Income/Expense CSV).");
						continue;
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

					String filePath = System.getProperty("user.dir") + "/pfm_data/" + currentUser.getUsername() + "/"
							+ year + ".csv";
					File file = new File(filePath);

					if (!file.exists()) {
						System.out.println("Prediction failed: No data found for year " + year);
						System.out.println("Please upload it first using option 1.");
						continue;
					}

					try {
						PredictionManager pd = new PredictionManager(filePath);
						String status = pd.determineBudgetStatus();
						System.out.println("Current Budget Status: " + status);

						if (status.equals("surplus")) {
							int extra = pd.determinePossibleAdditionalSpending("Any");
							System.out.printf("You can spend an additional: $%d%n", extra);
						} else if (status.equals("deficit")) {
							int cut = pd.determineDecreaseForSurplus();
							System.out.printf("You need to cut expenses by: $%d%n", cut);
						} else if (status.equals("balanced")) {
							System.out.println("Your budget is balanced — no prediction needed.");
						}

						// Ask if user wants to simulate changing category spending
						System.out.print("Would you like to simulate modifying spending for a category? (y/n): ");
						String mod = scanner.nextLine().trim();
						if (mod.equalsIgnoreCase("y")) {
							System.out.print("Enter category to modify (case-sensitive): ");
							String category = scanner.nextLine().trim();

							System.out.print("Enter adjustment amount in dollars (e.g., 200 to reduce): ");
							int amount = 0;
							try {
								amount = Integer.parseInt(scanner.nextLine().trim());
							} catch (NumberFormatException e) {
								System.out.println("Invalid number. Cancelling adjustment.");
								return;
							}

							pd.modifySpending(category, -amount); // reduce spending by that amount
						}

					} catch (IOException e) {
						System.out.println("Failed to run prediction due to a system error.");
						System.out.println("Details: " + e.getMessage());
					} catch (IllegalArgumentException e) {
						System.out.println("Data validation error: " + e.getMessage());
					}
				} else if (option == 7) {
					System.out.println("Choose method:");
					System.out.println("1. I know my current password");
					System.out.println("2. I forgot my password but can answer my secret question");
					System.out.print("Select option: ");
					String method = scanner.nextLine().trim();

					if (method.equals("1")) {
						System.out.print("Enter your current password: ");
						String currentPassword = scanner.nextLine().trim();

						if (currentUser.getPassword().equals(currentPassword)) {
							System.out.print("Enter your new password: ");
							String newPassword = scanner.nextLine().trim();

							currentUser.setPassword(newPassword);
							accountDAO.updateAccount(currentUser);
							System.out.println("Password successfully updated.");
						} else {
							System.out.println("Incorrect current password. Password not changed.");
						}

					} else if (method.equals("2")) {
						System.out.println("Secret Question: " + currentUser.getSecretQuestion());
						System.out.print("Your Answer: ");
						String answer = scanner.nextLine().trim();

						if (currentUser.getSecretAnswer().equalsIgnoreCase(answer)) {
							System.out.print("Enter your new password: ");
							String newPassword = scanner.nextLine().trim();

							currentUser.setPassword(newPassword);
							accountDAO.updateAccount(currentUser);
							System.out.println("Password successfully updated.");

						} else {
							System.out.println("Incorrect answer. Password not changed.");
						}
					} else {
						System.out.println("Invalid selection.");
					}
				} else if (option == 8) {
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

				} else if (option == 9) {
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
