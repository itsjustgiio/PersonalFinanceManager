import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Scanner;
import java.io.*;


class AuthService {
    private final AccountDAO accountDAO;

    public AuthService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    public boolean register(String username, String password , String secretQuestion, String secretAnswer) {
        
        if (!username.matches("^[a-zA-Z0-9_-]{3,20}$")) { // added if statement to fix KAN-4 bug - Arian
            System.out.println("Username must be 3-20 characters long and can only contain letters, numbers, underscores, or hyphens.");
            return false;
        }
        if (accountDAO.getAccountByUsername(username) != null) {
            System.out.println("Username already exists.");   // added the println statement - Arian
            return false; // Username already exists
        }
            
        String id = UUID.randomUUID().toString(); // updated to accept question/answer- Arian
        Account account = new Account(id, username, password, secretQuestion, secretAnswer);
        accountDAO.createAccount(account);
        return true;
    }

    public Account login(String username, String password) {
        Account account = accountDAO.getAccountByUsername(username);
        if (account != null && password.equals(account.getPassword())) {
            return account;
        }
        return null;
    }

    public boolean isAuthenticated(Account account) {
        return account != null;
    }
    public boolean recoverPassword(String username, String answer) {
        Account account = accountDAO.getAccountByUsername(username);
        if (account != null) {
            System.out.println("Secret Question: " + account.getSecretQuestion());
            if (answer.equalsIgnoreCase(account.getSecretAnswer())) {
                System.out.println("Your password is: " + account.getPassword());
                return true;
            } else {
                System.out.println("Incorrect answer.");
            }
        } else {
            System.out.println("Username not found.");
        }
        return false;
    }

}
class AccountDAO {
    private final Map<String, Account> accounts = new HashMap<>();
    private final String ACCOUNT_FILE = "accounts.txt";

    public AccountDAO() {
        loadAccountsFromFile();
    }

    private void loadAccountsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ACCOUNT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) { // added secretQuestion and secretAnswer- Arian
                // Format: id|username| |password|secretQuestion|secretAnswer 
                String[] parts = line.split("\\|");
                
                // Changed the length parts.length == 5 it is not 6 anymore (Aye Chan)
                
                if (parts.length == 5) { // changed from 4 to 6 to accomodate for secret Q and A- Arian
                    String id = parts[0];
                    String username = parts[1];
                    String password = parts[2];
                    String secretQuestion = parts[3];
                    String secretAnswer = parts[4];
                    
                    Account account = new Account(id, username, password, secretQuestion, secretAnswer);
                    accounts.put(id, account);
                }
            }
        } catch (IOException e) {
            // If file doesn't exist or is corrupted, ignore for now
            System.err.println("Could not load accounts from file: " + e.getMessage());
        }
    }

    private void saveAccountsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ACCOUNT_FILE))) {
            for (Account account : accounts.values()) {
                String line = String.join("|",
                    account.getId(),
                    account.getUsername(),
                    account.getPassword(),
                    account.getSecretQuestion(),    // added 2 extra lines
                    account.getSecretAnswer()       // - Arian
                );
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Could not save accounts to file: " + e.getMessage());
        }
    }

    public void createAccount(Account account) {
        accounts.put(account.getId(), account);
        saveAccountsToFile();
    }

    public Account getAccountById(String id) {
        return accounts.get(id);
    }

    public Account getAccountByUsername(String username) {
        for (Account account : accounts.values()) {
            if (account.getUsername().equals(username)) return account;
        }
        return null;
    }

    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }

    public void updateAccount(Account account) {
        accounts.put(account.getId(), account);
        saveAccountsToFile();
    }

    public void deleteAccount(String id) {
        accounts.remove(id);
        saveAccountsToFile();
    }
}
public class Account { // Included both secretQuestion and secretAnswer upon this class
    private String id; // to fix KAN-5 bug- Arian
    private String username;
    private String password;
    private String secretQuestion;
    private String secretAnswer; 

    public Account(String id, String username, String password, String secretQuestion, String secretAnswer) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.secretQuestion = secretQuestion;
        this.secretAnswer = secretAnswer;
    }
    //Default Constructor Created by Integration
    public Account() {
        this.id = "";
        this.username = "";
        this.password = "";
        this.secretQuestion = "";
        this.secretAnswer = "";
    }
    public String getUserDataDir() {
        String baseDir = "users";
        String fullPath = baseDir + "/" + username + "_" + id;
        java.io.File dir = new java.io.File(fullPath);
        if (!dir.exists()) {
            dir.mkdirs(); // create folders if missing
        }
        return fullPath;
    }

    
    // Getters
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
    public String getSecretQuestion() { // gets SecretQuestion- Arian
        return secretQuestion;
    }

    public String getSecretAnswer() { // gets SecretAnswer- Arian
        return secretAnswer;
    }

    // Setters


    public void setPassword(String password) {
        this.password = password;
    }

    public void setSecretQuestion(String q) { // sets SecretQuestion to q - Arian
        this.secretQuestion = q;
    }

    public void setSecretAnswer(String a) { // sets SecretAnswer to a - Arian
        this.secretAnswer = a;
    }

}
class Main {
    public static void main(String[] args) {
        AccountDAO accountDAO = new AccountDAO();
        AuthService authService = new AuthService(accountDAO);
        Scanner scanner = new Scanner(System.in);

        while (true) {
        	System.out.println("\n1. Register\n2. Login\n3. Forgot Password\n4. Exit");
            System.out.print("Select an option: ");
            int choice = Integer.parseInt(scanner.nextLine());
            
            // added a trim in input for username, email, pass, question and answer
            if (choice == 1) {
            	System.out.print("Username and DONT PUT A SPACE: ");
                String username = scanner.nextLine().trim();
                System.out.print("Password or PIN: ");
                String password = scanner.nextLine().trim();
                System.out.print("Secret Question: ");
                String question = scanner.nextLine().trim();
                System.out.print("Secret Answer: ");
                String answer = scanner.nextLine().trim();

                boolean success = authService.register(username, password, question, answer);
                System.out.println(success ? "Account created successfully!" : "Username already exists.");
            } else if (choice == 2) {
                System.out.print("Username: ");
                String username = scanner.nextLine().trim();
                System.out.print("Password or PIN: ");
                String password = scanner.nextLine().trim();

                Account account = authService.login(username, password);
                if (account != null) {
                    System.out.println("Welcome, " + account.getUsername() + "!");
                } else {
                    System.out.println("Invalid username or password.");
                }
            }
            else if (choice == 3) {
                System.out.print("Username: ");
                String username = scanner.nextLine().trim();

                Account account = accountDAO.getAccountByUsername(username);
                if (account != null) {
                    System.out.println("Secret Question: " + account.getSecretQuestion());
                    System.out.print("Your Answer: ");
                    String answer = scanner.nextLine().trim();
                    authService.recoverPassword(username, answer);
                } else {
                    System.out.println("Username not found.");
                }
            } 
            else {
                System.out.println("Exiting...");
                break;
            }
        }

        scanner.close();
    }
}