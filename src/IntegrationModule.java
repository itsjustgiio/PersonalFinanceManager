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

		/**
		 * Displays the initial login menu for the user. Handles registration and login
		 * using AuthService. If login is successful, navigates to the main user menu.
		 */

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
					String username;
					while (true) {
						System.out.print("Username: ");
						username = scanner.nextLine().trim();
						if (ValidationManager.UserCredentialValueLimiter.restrictUsernameValues(username))
							break;
						System.out.println(
								"Username must be 3–20 characters long and can only contain letters, numbers, underscores.");
					}

					String email;
					while (true) {
						System.out.print("Email: ");
						email = scanner.nextLine().trim();
						if (email.contains("@") && email.contains("."))
							break;
						System.out.println("Invalid email format.");
					}

					String password;
					while (true) {
						System.out.print("Password: ");
						password = scanner.nextLine().trim();
						if (ValidationManager.UserCredentialValueLimiter.restrictPasswordValues(password))
							break;
						System.out.println(
								"Password must be at least 8 characters, and include upper, lower, number, and special char.");
					}

					String secretQuestion;
					while (true) {
						System.out.print("Secret Question: ");
						secretQuestion = scanner.nextLine().trim();
						if (!secretQuestion.isEmpty())
							break;
						System.out.println("Secret question cannot be empty.");
					}

					String secretAnswer;
					while (true) {
						System.out.print("Secret Answer: ");
						secretAnswer = scanner.nextLine().trim();
						if (ValidationManager.UserCredentialValueLimiter.restrictSecretPasswordValues(secretAnswer))
							break;
						System.out
								.println("Secret answer must be 2–50 characters and only letters, numbers, or spaces.");
					}

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
					System.exit(0);
				}
			}
		}

		/**
		 * Displays the main menu after a user logs in. Provides access to all major
		 * functionalities: 1. Upload or update budget data 2. View loaded budget years
		 * 3. View transactions for a specific year 4. Delete a budget year 5. Generate
		 * a financial report 6. Perform what-if predictions using PredictionManager
		 * 7.Change password (via password or secret question) 8. Delete user account
		 * and associated files 9. Logout and return to login screen
		 */

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

				// Option 1: Upload or Update Income/Expense CSV
				// Invokes Budget module to upload or overwrite CSV data for a given year.

				if (option == 1) {
					budget.promptToCreateOrUpdate();
				}
				// Option 2: List Loaded Budget Years
				// Displays all years for which the current user has uploaded budget data.

				else if (option == 2) {
					ArrayList<Integer> years = budget.getYears();
					if (years.isEmpty()) {
						System.out.println("No budgets found.");
					} else {
						System.out.println("Loaded Budget Years:");
						for (Integer year : years) {
							System.out.println("- " + year);
						}
					}
				}
				// Option 3: View Transactions for a Year
				// Prompts for a year and displays the detailed transaction list for that year.

				else if (option == 3) {
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
				}
				// Option 4: Delete Budget for a Year
				// Prompts for confirmation and deletes the specified year’s CSV data.

				else if (option == 4) {
					budget.promptToDelete();
				}
				// Option 5: Generate Financial Report
				// Prompts for year and generates analysis using ReportsManager.
				// Optionally saves the report to file if user confirms.

				else if (option == 5) {
					System.out.print("Enter year to generate report: ");
					int year = Integer.parseInt(scanner.nextLine());
					String filePath = System.getProperty("user.dir") + "/pfm_data/" + currentUser.getUsername() + "/"
							+ year + ".csv";

					if (ValidationManager.CheckCSVFileFormat.validCSVFile(filePath)) {
						boolean saveToFile = askYesOrNo(scanner, "Save report to a file?");
						ReportsManager.analyzeData(currentUser, year, saveToFile);
					} else {
						System.out.println("No data found for year " + year);
						System.out.println(
								"Please upload it first using option 1 (Upload or Update Income/Expense CSV).");
						continue;
					}

				}
				// Option 6: Perform What-If Budget Prediction
				// Loads user CSV and performs analysis with PredictionManager.
				// Allows setting spending priorities (up to 3).
				// Simulates reducing spending in a category while respecting priorities.
				// Only categories present in the uploaded file are allowed as input.

				else if (option == 6) {
					int year = -1;
					while (true) {
						System.out.print("Enter year to perform prediction: ");
						String input = scanner.nextLine().trim();
						try {
							year = Integer.parseInt(input);
							break;
						} catch (NumberFormatException e) {
							System.out.println("Invalid year. Please enter a valid numeric year.");
						}
					}

					String filePath = System.getProperty("user.dir") + "/pfm_data/" + currentUser.getUsername() + "/"
							+ year + ".csv";
					File file = new File(filePath);

					if (!file.exists()) {
						System.out.println("Prediction failed: No data found for year " + year);
						System.out.println("(Please upload it first using option 1.)");
						continue;

					}

					try {
						PredictionManager pd = new PredictionManager(filePath);
						String status = pd.determineBudgetStatus();
						System.out.println("\nCurrent Budget Status: " + status);

						if (status.equals("surplus")) {
							int extra = pd.determinePossibleAdditionalSpending("Any");
							System.out.printf("You can spend an additional: $%d\n", extra);
						} else if (status.equals("deficit")) {
							int cut = pd.determineDecreaseForSurplus();
							System.out.printf("You need to cut expenses by: $%d\n", cut);
						} else {
							System.out.println("Your budget is balanced – no prediction needed.");
						}

						Set<String> validCategoriesFromFile = new HashSet<>();
						try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
							String line;
							while ((line = reader.readLine()) != null) {
								line = line.trim();
								if (line.isEmpty() || line.toLowerCase().contains("category"))
									continue;
								String[] parts = line.split(",");
								if (parts.length == 3) {
									String category = parts[1].trim();
									if (ValidationManager.CheckCSVContent.validCategories(category)) {
										validCategoriesFromFile.add(category);
									}
								}
							}
						} catch (IOException e) {
							System.out.println("Failed to re-parse CSV categories: " + e.getMessage());
						}
						System.out.println("\nValid categories from your file: " + validCategoriesFromFile);

						pd.clearBudgetPriorities();
						if (askYesOrNo(scanner, "Would you like to set budget priorities?")) {
							for (int i = 1; i <= 3; i++) {
								while (true) {
									System.out.print("Enter priority #" + i + " category (or press ENTER to skip): ");
									String category = scanner.nextLine().trim();
									if (category.isEmpty())
										break;
									if (validCategoriesFromFile.contains(category)) {
										pd.setBudgetPriorities(category);
										break;
									} else {
										System.out.println("Invalid category. Choose one from your loaded CSV.");
									}
								}
							}

							System.out.println("\nYour current budget priorities are:");
							System.out.println("1. " + (pd.priority1 != null ? pd.priority1 : "(none)"));
							System.out.println("2. " + (pd.priority2 != null ? pd.priority2 : "(none)"));
							System.out.println("3. " + (pd.priority3 != null ? pd.priority3 : "(none)"));

							if (askYesOrNo(scanner, "Would you like to change or remove any priorities?")) {
								while (true) {
									System.out.print("Enter a category to remove from priorities: ");
									String remove = scanner.nextLine().trim();
									if (validCategoriesFromFile.contains(remove)) {
										pd.removeBudgetPriority(remove);
										break;
									} else {
										System.out.println("Invalid category. Try again.");
									}
								}

								if (askYesOrNo(scanner, "Would you like to set a new priority in its place?")) {
									while (true) {
										System.out.print("Enter new priority category: ");
										String newPriority = scanner.nextLine().trim();
										if (validCategoriesFromFile.contains(newPriority)) {
											pd.setBudgetPriorities(newPriority);
											break;
										} else {
											System.out.println("Invalid category. Try again.");
										}
									}
								}

								System.out.println("\nUpdated budget priorities:");
								System.out.println("1. " + (pd.priority1 != null ? pd.priority1 : "(none)"));
								System.out.println("2. " + (pd.priority2 != null ? pd.priority2 : "(none)"));
								System.out.println("3. " + (pd.priority3 != null ? pd.priority3 : "(none)"));
							}
						}

						while (askYesOrNo(scanner, "Would you like to simulate modifying spending for a category?")) {
							String category;
							while (true) {
								System.out.print("Enter category to modify (case-sensitive): ");
								category = scanner.nextLine().trim();
								if (validCategoriesFromFile.contains(category))
									break;
								System.out.println("Invalid category. Try again.");
							}

							int amount = 0;
							while (true) {
								System.out.print("Enter adjustment amount in dollars (e.g., 200 to reduce): ");
								try {
									amount = Integer.parseInt(scanner.nextLine().trim());
									if (amount <= 0)
										throw new NumberFormatException();
									break;
								} catch (NumberFormatException e) {
									System.out.println("Invalid amount. Enter a positive number.");
								}
							}

							int oldExpenses = pd.getTotalExpenses();
							pd.modifySpending(category, -amount);
							int newExpenses = pd.getTotalExpenses();

							if (oldExpenses == newExpenses) {
								if (!askYesOrNo(scanner, "Would you like to try a different category?"))
									break;
							} else {
								break;
							}
						}

					} catch (IOException e) {
						System.out.println("Failed to run prediction due to a system error.");
						System.out.println("Details: " + e.getMessage());
					} catch (IllegalArgumentException e) {
						System.out.println("Data validation error: " + e.getMessage());
					}
				}
				// Option 7: Change Password
				// Allows password change using either current password or secret question.

				else if (option == 7) {
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
				}
				// Option 8: Delete My Account
				// Confirms deletion, then removes user record and all associated CSV files.

				else if (option == 8) {
					boolean confirm = askYesOrNo(scanner, "Are you sure you want to delete your account?");
					if (confirm) {

						String userDirPath = System.getProperty("user.dir") + "/pfm_data/" + currentUser.getUsername();
						File userDir = new File(userDirPath);
						if (userDir.exists() && userDir.isDirectory()) {
							for (File file : userDir.listFiles()) {
								if (!file.delete()) {
									System.out.println("Failed to delete file: " + file.getName());
								}
							}
						}
						if (!userDir.delete()) {
							System.out.println("Failed to delete user folder: " + userDirPath);
						}

						accountDAO.deleteAccount(currentUser.getId());
						System.out.println("Your account has been deleted.");
						System.out.println("Goodbye, " + currentUser.getUsername() + "!");
						displayLoginMenu();
					}
				}

				// Option 9: Logout
				// Logs the user out and returns to the login menu.

				else if (option == 9) {
					logoutUser();
					break;
				} else {
					System.out.println("Invalid option. Please try again.");
				}
			}
		}

		/**
		 * Logs out the current user and prints a farewell message. Resets the
		 * currentUser field to null.
		 */

		public void logoutUser() {
			if (currentUser != null) {
				System.out.println("Goodbye, " + currentUser.getUsername() + "!");
			} else {
				System.out.println("Goodbye!");
			}
			currentUser = null;
		}

		/**
		 * Utility method to repeatedly prompt user with a (y/n) question. Returns true
		 * for 'y' and false for 'n'. Repeats until valid input.
		 *
		 * @param scanner  Scanner instance for user input
		 * @param question The prompt to show
		 * @return true if user enters 'y', false if 'n'
		 */

		private static boolean askYesOrNo(Scanner scanner, String question) {
			while (true) {
				System.out.print(question + " (y/n): ");
				String input = scanner.nextLine().trim().toLowerCase();
				if (input.equals("y"))
					return true;
				if (input.equals("n"))
					return false;
				System.out.println("Invalid input. Please enter 'y' or 'n'.");
			}
		}

	}

	/**
	 * Entry point for the application. Initializes and starts the login menu.
	 */

	public static void main(String[] args) {
		MainMenu menu = new MainMenu();
		menu.displayLoginMenu();
	}
}
