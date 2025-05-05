import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Scanner;

class AuthService {
    private final AccountDAO accountDAO;

    public AuthService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    public boolean register(String username, String email, String password) {
        if (accountDAO.getAccountByUsername(username) != null) {
            return false; // Username already exists
        }

        String id = UUID.randomUUID().toString();
        Account account = new Account(id, username, email, password);
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
                // Format: id|username|email|password
                String[] parts = line.split("\\|");
                if (parts.length == 4) {
                    String id = parts[0];
                    String username = parts[1];
                    String email = parts[2];
                    String password = parts[3];
                    Account account = new Account(id, username, email, password);
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
                    account.getEmail(),
                    account.getPassword()
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
public class Account {
    private String id;
    private String username;
    private String email;
    private String password; 

   
    public Account(String id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }
    /*
     * Default constructor added by Integration Team (Aye Chan)
     * 
     * 
     * Reason : Budget.java used new Account() without parameters.
     * 
     */
    public Account() {
    	this.id = "1234567";
    	this.username = "John Smith";
    	this.email ="Jsmith@gamil.com";
    	this.password ="password1234";
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

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
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
                String username = scanner.nextLine();
                System.out.print("Email: ");
                String email = scanner.nextLine();
                System.out.print("Password or PIN: ");
                String password = scanner.nextLine();

                boolean success = authService.register(username, email, password);
                System.out.println(success ? "Account created successfully!" : "Username already exists.");
            } else if (choice == 2) {
                System.out.print("Username: ");
                String username = scanner.nextLine();
                System.out.print("Password or PIN: ");
                String password = scanner.nextLine();

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