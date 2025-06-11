## Personal Finance Manager (PFM)
Modular Java application for managing personal finances with account handling, budget predictions, and CSV-based data storage. Built using OOP principles and modular design.

## 📄 Table of Contents
1. Key Features
2. Tech Stack
3. Architecture & Modules
4. Getting Started
5. Usage
6. Data Storage
7. Testing & Reporting
8. Contributing
9. Contact

## Key Features
- Manage multiple accounts with balances
- Categorize and track expenses/income
- Generate budget predictions
- Read/write transactions using CSV files
- Modular architecture enabling team-owned components

## Tech Stack
Language: Java
Data Storage: CSV in pfm_data/, .txt for accounts
Reporting: CSV-based (Report.csv)
IDE Support: .classpath, .project, and .settings/ files provided

## Architecture & Modules
PFM is organized by module packages:
- **Account Module** – handles account creation and balance tracking
- **Transaction Module** – manages income/expense entries
- **Budget Module** – generates future budget projections
- **Storage Module** – performs CSV import/export
- **Reporting Module** – compiles monthly transaction reports
This modular structure enables easy maintenance and team collaboration.

## Getting Started
1. Clone the repo
```bash
git clone https://github.com/itsjustgiio/PersonalFinanceManager.git
cd PersonalFinanceManager
```
2. Import as a Java project in your IDE (Eclipse, IntelliJ, VS Code).
3. Install dependencies (if using Maven/Gradle):
```bash
mvn clean install
```
4. Run the main class (e.g., Main.java or App.java).

## 🧭 Usage
1. Ensure input files exist in pfm_data/:
  accounts.txt – each line: accountName,initialBalance
  transactions.csv – format (date, account, category, amount, description)
2. Launch the application.
3. Choose options to:
  - View account balances
  - Add a new income/expense
  - Generate a budget forecast
  - Export a monthly report (written to Report.csv)
## 🗄️ Data Storage
- Raw data: stored in plaintext CSV within the pfm_data folder
- Output: Report.csv summarizing monthly inflows, outflows, and net balance

##Testing & Reporting
Unit tests (if available) can be executed via:

```bash
mvn test
```

Transactions report is generated as Report.csv in the repo root after running the app.

##🤝 Contributing
1. Fork this repo
2. Create a feature branch (git checkout -b feature-name)
3. Commit changes (git commit -m "feat: description")
4. Push (git push origin feature-name)
5. Open a Pull Request

## 📬 Contact
Feel free to reach out:
- 📧 Email: [giovannic684@gmail.com](mailto:giovannic684@gmail.com)  
- 💬 Discord: `itsjustgiio`
