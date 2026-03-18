package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.*;

/**
 * Evaluate Single Variable Continuous Regression
 */
public class App {
	static float[] evaluateModel(String filePath) {
		FileReader filereader;
		List<String[]> allData;
		try {
			filereader = new FileReader(filePath);
			CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
			allData = csvReader.readAll();
		} catch (Exception e) {
			System.out.println("Error reading the CSV file: " + filePath);
			return null;
		}

		float sumSquaredError = 0.0f;
		float sumAbsoluteError = 0.0f;
		float sumAbsoluteRelativeError = 0.0f;
		int n = 0;

		for (String[] row : allData) {
			float y_true = Float.parseFloat(row[0]);
			float y_predicted = Float.parseFloat(row[1]);

			float error = y_true - y_predicted;
			sumSquaredError += error * error;
			sumAbsoluteError += Math.abs(error);
			if (y_true != 0) {
				sumAbsoluteRelativeError += Math.abs(error / y_true);
			}
			n++;
		}

		float mse = sumSquaredError / n;
		float mae = sumAbsoluteError / n;
		float mare = sumAbsoluteRelativeError / n;

		return new float[] { mse, mae, mare };
	}

	public static void main(String[] args) {
		String[] models = { "model_1.csv", "model_2.csv", "model_3.csv" };

		float bestMSE = Float.MAX_VALUE;
		float bestMAE = Float.MAX_VALUE;
		float bestMARE = Float.MAX_VALUE;
		String bestModelMSE = "";
		String bestModelMAE = "";
		String bestModelMARE = "";

		for (String model : models) {
			float[] metrics = evaluateModel(model);
			if (metrics == null)
				continue;

			System.out.println("for " + model);
			System.out.println("\t\tMSE =" + metrics[0]);
			System.out.println("\t\tMAE =" + metrics[1]);
			System.out.println("\t\tMARE =" + metrics[2]);

			if (metrics[0] < bestMSE) {
				bestMSE = metrics[0];
				bestModelMSE = model;
			}
			if (metrics[1] < bestMAE) {
				bestMAE = metrics[1];
				bestModelMAE = model;
			}
			if (metrics[2] < bestMARE) {
				bestMARE = metrics[2];
				bestModelMARE = model;
			}
		}

		System.out.println("According to MSE, The best model is " + bestModelMSE);
		System.out.println("According to MAE, The best model is " + bestModelMAE);
		System.out.println("According to MARE, The best model is " + bestModelMARE);
	}
}