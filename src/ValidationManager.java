/**
 * @author Ayana Tran, 
 * @author Axyl Fredrick
 * @author Susie Marrero
 * @author Omar Quraishi
 * @version 1.1, 12 May 2025
 */
import java.io.File;            // To create File objects and check if files exist, are readable, etc.
import java.io.FileReader;       // To open a file for reading (specifically text files like CSVs).
import java.io.BufferedReader;   // To read text from a file efficiently line-by-line.
import java.io.IOException;      // To handle exceptions during file operations (read errors, etc).


/**
* ValidationManager - utility class to managage validation tasks for CSV files and user credentials
*/
public class ValidationManager {

    /**
	* Class responsible for checking CSV file format (path, existence, readability)
    */
    public final static class CheckCSVFileFormat {
        // Private constructor to prevent instantiation
        private CheckCSVFileFormat() {
        }

        /**
	   *Method to check if the provided file path points to a readable CSV file
           *@param filePath
	   *@return false if filePath is null/empty. otherwise valid and returns file.
        */
        public static boolean validCSVFile(String filePath) {
            if (filePath == null || filePath.isEmpty()) { 
                return false;
            }
            File file = new File(filePath); 
            // Check if file exists, is a file (not a directory), is readable, and ends with ".csv"
            return file.exists() && file.isFile() && file.canRead() && filePath.toLowerCase().endsWith(".csv");
        }
    }

    /**
	* Class responsible for checking CSV content (e.g. dates, categories, years, etc.)
    */
    public final static class CheckCSVContent {

        // Private constructor to prevent instantiation
        private CheckCSVContent() {
        }

        /**
	   *Method to validate if a date string is in correct MM/DD/YYYY format
           *@param date 
	   *@return false if date format is in valid or if it isnt numerical. otherwise true
	*/
        public static boolean validDateFormat(String date) {
            String[] partsOfDate = date.split("/"); 
            if (partsOfDate.length != 3) { // Must have 3 parts: MM, DD, YYYY
                return false;
            }
            // Check if it only contains nums
            for (String part : partsOfDate) { 
                if (!(part.trim().matches("[0-9]\\d*"))) {
                    return false;
                }
            }

            int daysPerMonth[] = {31,28,31,30,31,30,31,31,30,31,30,31};
            // Parsing month, day, and year
            int month = Integer.parseInt(partsOfDate[0]);
            int day = Integer.parseInt(partsOfDate[1]);
            int year = Integer.parseInt(partsOfDate[2]);

            // checking for leap year
            if ((year % 400 == 0) || (year % 4 == 0 && year % 100 != 0)) {
                daysPerMonth[1]++; // February has 29 days in a leap year
            }

            // Validate month, day, and year ranges
            return (month > 0 && month <= 12) && (day > 0 && day <= daysPerMonth[month - 1]) && (year > 0);
        }

        /**
		 * All dates in one CSV file must have same year.
		 * Allowed: File content may or may not be different.
		 * Warning: If CSV with same year is uploaded again (for same user),
		 * give option to cancel or overwrite existing file
                 *@param year, filePath
		 *@return flase if file is invalid, otherwise return true
		 */
        public static boolean isAllSameYear(int year, String filePath) {
            if (!CheckCSVFileFormat.validCSVFile(filePath)) {
                return false; // If file invalid, return false
            }

            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) { // Read each line
                    String[] parts = line.split(","); //We split file by variables
                    String date = parts[0]; // date is the first part
                    if (validDateFormat(date)) {
                        int dateYear = Integer.parseInt(date.split("/")[2]);
                        if (dateYear != year) { 
                            System.err.println("Year differs from expected year in file!");
                            return false;
                        }
                    } else {
                        System.err.println("Invalid date format found in file!");
                        return false;
                    }
                }
                // handling file reading erro
            } catch (IOException e) {
                System.err.println("Error reading file: " + e.getMessage());
                return false;
            }

            return true; 
        }

        /** 
	   *Method to check if a category string is valid ,only letters and underscores allowed
           *@param category 
	   *@return false if category is null/empty, otherwise valid
        */
	
        public static boolean validCategories(String category) {
            if (category == null || category.isEmpty()) {
                return false;
            }
            return category.matches("[a-zA-Z_&]+"); // Category must be only letters, ampersands, or underscores
        }

        /**
	 *Method to check if a dollar amount string is valid
         *@param amount
	 *@return amount trimmed 
        */
        public static boolean validDollarAmount(String amount) {
            return amount.trim().matches("[+-]?\\d+"); // "[+-]?\\d*(\\.\\d{1,2})?" if you want non integer dollar amounts
        }

        /** 
	   *Validation of one individual line
	   *@param expectedYear and line
           *@return false if the line isnt vald and if information/categories dont match.
        */
        public static boolean validateLine(int expectedYear, String line) {
            String[] parts = line.split(",");
            if (parts.length != 3) {
                System.err.println("Invalid line format: Wrong number of columns. Please place proper Date, Category, and Amount entries in CSV file.");
                return false;
            }
    
            String date = parts[0];
            String category = parts[1];
            String amount = parts[2];
    
            if (!validDateFormat(date)) {
                System.err.println("Invalid date format: " + date);
                return false;
            }
    
            int yearInFile = Integer.parseInt(date.split("/")[2]);
            if (yearInFile != expectedYear) {
                System.err.println("Year mismatch: found " + yearInFile + ", expected " + expectedYear);
                return false;
            }
    
            if (!validCategories(category)) {
                System.err.println("Invalid category: " + category);
                return false;
            }
    
            if (!validDollarAmount(amount)) {
                System.err.println("Invalid dollar amount: " + amount);
                return false;
            }

            return true;
        }

        //Validate header for file, method is dummied out because it is no longer needed.
        /* public static boolean validHeader(String line) {
          String parts = line.split(',');
          if (parts.length != 3)
            return false;
          String[] header = {"date", "category", "amount"}
          for (int i = 0; i < 3; i++) {
            if (parts[i].trim().toLowerCase() != header[i])
              return false;
          }
          return true;
        } */

        /**
	   *Validate the entire CSV file
           *@param expectedYear and filePath
	   *@return false if the file is invalid otherwise return true 
        */
        public static boolean validateWholeCSVFile(int expectedYear, String filePath) {
            if (!CheckCSVFileFormat.validCSVFile(filePath)) {
                return false;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
		// dummied out header code below:
		/* = br.readLine();
                if (!validateLine(expectedYear, line) && !validHeader(line)) 
                  return false;*/
                while ((line = br.readLine()) != null) {
                    if (!validateLine(expectedYear, line))
                      return false;
                }
            } catch (IOException e) {
                System.err.println("Error reading file: " + e.getMessage());
                return false;
            }

            return true; // File fully validated
        }
    }

    /**
	 * Used to check for unallowed values during login. Gatekeeps credentials so
	 * only valid credential values are sent to Authentication.
         *
	 */
    public final static class UserCredentialValueLimiter {
        // Private constructor to prevent instantiation
        private UserCredentialValueLimiter() {
        }

        /** 
	   *Restrict username values (letters, numbers, underscore , 3-20 characters)
           *@param username 
	   *return false if username is empty/null, return true otherwise
	*/
        public static boolean restrictUsernameValues(String username) {
            if (username == null || username.isEmpty()) {
                return false;
            }
            return username.matches("[a-zA-Z0-9_]{3,20}$");
        }

        /**
	    *Restrict password values (must have upper, lower, number, special character, at least 8 chars)
            *@param password
	    *@return false if password is null/empty, or false if it contains a space, otherwise password is valid
	*/
        public static boolean restrictPasswordValues(String password) {
            if (password == null || password.isEmpty()) {
                return false;
            }
            if (password.contains(" ")) {
                return false;
            }
            String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,20}$"; // this actually does the restricting , added maximum length per storage teams request
            return password.matches(regex);
        }

        /**
	    *Restrict secret password values (security question answers)
	    *@param secret answer
            *@returns false if secret answer is null/empty, otherwise secret answer is valid 
	*/
        public static boolean restrictSecretPasswordValues(String secretAnswer) {
            if (secretAnswer == null || secretAnswer.trim().isEmpty()) {
                return false;
	    }
	
	   secretAnswer = secretAnswer.trim(); // remove spaces at start/end
	
	   // Must be at least 2 characters long and only letters, numbers, spaces allowed
	   return secretAnswer.matches("[a-zA-Z0-9 ]{2,50}");
        }
    }
}