package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.*;

public class App {
	public static void main(String[] args) {
		String baseDir = System.getProperty("user.dir");
		String filePath = baseDir + "\\model.csv";
		FileReader filereader;
		List<String[]> allData;
		try {
			filereader = new FileReader(filePath);
			CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
			allData = csvReader.readAll();
		} catch (Exception e) {
			System.out.println("Error reading the CSV file");
			return;
		}

		int numClasses = 5;
		int[][] confMatrix = new int[numClasses][numClasses];
		double ce = 0.0;

		for (String[] row : allData) {
			int y_true = Integer.parseInt(row[0]) - 1; // 0-indexed
			float[] y_predicted = new float[numClasses];
			for (int i = 0; i < numClasses; i++) {
				y_predicted[i] = Float.parseFloat(row[i + 1]);
			}

			// CE: -log(probability of true class)
			double p = Math.min(Math.max(y_predicted[y_true], 1e-15), 1 - 1e-15);
			ce += -Math.log(p);

			// Predicted class = argmax
			int y_pred = 0;
			for (int i = 1; i < numClasses; i++) {
				if (y_predicted[i] > y_predicted[y_pred])
					y_pred = i;
			}
			confMatrix[y_pred][y_true]++;
		}

		ce /= allData.size();

		System.out.println("CE =" + (float) ce);
		System.out.println("Confusion matrix");
		System.out.println("\t\ty=1\t y=2\t y=3\t y=4\t y=5");
		for (int i = 0; i < numClasses; i++) {
			System.out.print("\ty^=" + (i + 1) + "\t");
			for (int j = 0; j < numClasses; j++) {
				System.out.print(confMatrix[i][j] + "\t");
			}
			System.out.println();
		}
	}
}