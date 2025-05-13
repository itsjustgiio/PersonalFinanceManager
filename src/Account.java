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

    public boolean register(String username, String email, String password, String secretQuestion, String secretAnswer) {
        
        if (!username.matches("^[a-zA-Z0-9_-]{3,20}$")) {
            System.out.println("Username must be 3-20 characters long and can only contain letters, numbers, underscores, or hyphens.");
            return false;
        }
        if (accountDAO.getAccountByUsername(username) != null) {
            System.out.println("Username already exists.");
            return false;
        }
            
        String id = UUID.randomUUID().toString();
        Account account = new Account(id, username, email, password, secretQuestion, secretAnswer);
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
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    String id = parts[0];
                    String username = parts[1];
                    String email = parts[2];
                    String password = parts[3];
                    String secretQuestion = parts[4];
                    String secretAnswer = parts[5];
                    
                    Account account = new Account(id, username, email, password, secretQuestion, secretAnswer);
                    accounts.put(id, account);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load accounts from file: " + e.getMessage());
        }
    }

    private void saveAccountsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ACCOUNT_FILE))) {
            for (Account account : accounts.values()) {
                String line = String.join("|",
                    account.getId(),
                    account.getUsername(),
                    account.getEmail(),
                    account.getPassword(),
                    account.getSecretQuestion(),
                    account.getSecretAnswer()
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
        String lowerUsername = username.toLowerCase();
        for (Account account : accounts.values()) {
            if (account.getUsername().toLowerCase().equals(lowerUsername)) return account;
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

public class Account {
    private String id;
    private String username;
    private String email;
    private String password;
    private String secretQuestion;
    private String secretAnswer; 

    public Account(String id, String username, String email, String password, String secretQuestion, String secretAnswer) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.secretQuestion = secretQuestion;
        this.secretAnswer = secretAnswer;
    }
    
    // Added by Integration Team
    public Account() {
        this.id = "";
        this.username = "";
        this.email = "";
        this.password = "";
        this.secretQuestion = "";
        this.secretAnswer = "";
    }

    public String getUserDataDir() {
        String baseDir = "users";
        String fullPath = baseDir + "/" + username + "_" + id;
        java.io.File dir = new java.io.File(fullPath);
        if (!dir.exists()) {
            dir.mkdirs();
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

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
    
    public String getSecretQuestion() {
        return secretQuestion;
    }

    public String getSecretAnswer() {
        return secretAnswer;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSecretQuestion(String q) {
        this.secretQuestion = q;
    }

    public void setSecretAnswer(String a) {
        this.secretAnswer = a;
    }
}

class Main {
    public static void main(String[] args) {
        AccountDAO accountDAO = new AccountDAO();
        AuthService authService = new AuthService(accountDAO);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n1. Register\n2. Login\n3. Exit");
            System.out.print("Select an option: ");
            int choice = Integer.parseInt(scanner.nextLine());
            
            if (choice == 1) {
                System.out.print("Username: ");
                String username = scanner.nextLine().trim();
                System.out.print("Email: ");                 
                String email = scanner.nextLine().trim();
                
                // Hide password input
                Console console = System.console();
                String password;
                if (console != null) {
                    char[] passwordArray = console.readPassword("Password or PIN: ");
                    password = new String(passwordArray);
                } else {
                    System.out.print("Password or PIN: ");
                    password = scanner.nextLine().trim();
                }
                
                System.out.print("Secret Question: ");
                String question = scanner.nextLine().trim();
                System.out.print("Secret Answer: ");
                String answer = scanner.nextLine().trim();

                boolean success = authService.register(username, email, password, question, answer);
                System.out.println(success ? "Account created successfully!" : "Username already exists.");
            } else if (choice == 2) {
                System.out.print("Username: "); 
                String username = scanner.nextLine().trim();
                
                // Hide password input
                Console console = System.console();
                String password;
                if (console != null) {
                    char[] passwordArray = console.readPassword("Password or PIN: ");
                    password = new String(passwordArray);
                } else {
                    System.out.print("Password or PIN: ");
                    password = scanner.nextLine().trim();
                }

                Account account = authService.login(username, password);
                if (account != null) {
                    System.out.println("Welcome, " + account.getUsername() + "!");
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
}