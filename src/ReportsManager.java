import java.io.*;

class ReportsManager {

	private ReportsManager() {
	}

	public static void analyzeData(String filePath, int year, boolean writeToFile) {
		File file = new File(filePath);
		double[] monthlyIncomes = new double[12];
		double[] monthlyExpenses = new double[12];
		double[] monthlyGross = new double[12];
		double incomeYear = 0;
		double expensesYear = 0;
		double grossYear = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			br.readLine();
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				String[] dateSplit = parts[0].split("/");
				int month = Integer.parseInt(dateSplit[0]) - 1;
				double money = Double.parseDouble(parts[2]);
				if (money < 0) {
					monthlyExpenses[month] -= money;
					expensesYear -= money;
				} else {
					monthlyIncomes[month] += money;
					incomeYear += money;
				}
			}

		} catch (IOException e) {
			System.out.println("Error");
			e.printStackTrace();
		}

		for (int i = 0; i < 12; i++) {
			monthlyGross[i] = monthlyIncomes[i] - monthlyExpenses[i];
			System.out.printf("Monthly Income: %.2f, Monthly Expenses: %.2f, Monthly Gross: %.2f\n", monthlyIncomes[i],
					monthlyExpenses[i], monthlyGross[i]);
		}
		grossYear = incomeYear - expensesYear;
		System.out.printf("Year's Income: %.2f, Year's Expenses: %.2f, Year's Gross: %.2f \n", incomeYear, expensesYear,
				grossYear);

		if (writeToFile) {
			File reportFile = new File("Report.csv");
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(reportFile))) {
				bw.write("Month, income, expenses, gross");
				bw.newLine();
				String[] months = { "Januray", "February", "March", "April", "May", "June", "July", "August",
						"September", "October", "November", "December" };
				for (int i = 0; i < 12; i++) {
					bw.write(String.format("%s, %.2f, %.2f, %.2f, ", months[i], monthlyIncomes[i], monthlyExpenses[i],
							monthlyGross[i]));
					bw.newLine();
				}
				bw.write(String.format("Year, %.2f, %.2f, %.2f", incomeYear, expensesYear, grossYear));

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public static void main(String[] args) {
		System.out.println("Running analyzeData");
		analyzeData("PFM.csv", 2005, true);
	}
}
