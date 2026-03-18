package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import com.opencsv.*;

/**
 * Evaluate Single Variable Binary Regression
 */
public class App {
	static void evaluateModel(String filePath, String modelName) {
		FileReader filereader;
		List<String[]> allData;
		try {
			filereader = new FileReader(filePath);
			CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
			allData = csvReader.readAll();
		} catch (Exception e) {
			System.out.println("Error reading the CSV file: " + filePath);
			return;
		}

		int tp = 0, fp = 0, tn = 0, fn = 0;
		double bce = 0.0;
		List<float[]> data = new ArrayList<>();

		for (String[] row : allData) {
			int y_true = Integer.parseInt(row[0]);
			float y_predicted = Float.parseFloat(row[1]);
			data.add(new float[] { y_true, y_predicted });

			// BCE
			double p = Math.min(Math.max(y_predicted, 1e-15), 1 - 1e-15);
			bce += -(y_true * Math.log(p) + (1 - y_true) * Math.log(1 - p));

			// Confusion matrix (threshold = 0.5)
			int y_pred_label = y_predicted >= 0.5 ? 1 : 0;
			if (y_true == 1 && y_pred_label == 1)
				tp++;
			else if (y_true == 0 && y_pred_label == 1)
				fp++;
			else if (y_true == 0 && y_pred_label == 0)
				tn++;
			else
				fn++;
		}

		int n = data.size();
		bce /= n;

		float accuracy = (float) (tp + tn) / n;
		float precision = (float) tp / (tp + fp);
		float recall = (float) tp / (tp + fn);
		float f1 = 2 * precision * recall / (precision + recall);

		// AUC-ROC via trapezoidal rule
		// Sort by predicted probability descending
		data.sort((a, b) -> Float.compare(b[1], a[1]));
		int totalPos = tp + fn;
		int totalNeg = fp + tn;
		double auc = 0.0;
		double prevTpr = 0.0, prevFpr = 0.0;
		int cumTp = 0, cumFp = 0;
		for (float[] point : data) {
			if (point[0] == 1)
				cumTp++;
			else
				cumFp++;
			double tpr = (double) cumTp / totalPos;
			double fpr = (double) cumFp / totalNeg;
			auc += (fpr - prevFpr) * (tpr + prevTpr) / 2.0;
			prevTpr = tpr;
			prevFpr = fpr;
		}

		System.out.println("for " + modelName);
		System.out.println("\t\tBCE =" + (float) bce);
		System.out.println("\t\tConfusion matrix");
		System.out.println("\t\t\t\ty=1      y=0");
		System.out.println("\t\t\ty^=1\t" + tp + "\t" + fp);
		System.out.println("\t\t\ty^=0\t" + fn + "\t" + tn);
		System.out.println("\t\tAccuracy =" + accuracy);
		System.out.println("\t\tPrecision =" + precision);
		System.out.println("\t\tRecall =" + recall);
		System.out.println("\t\tf1 score =" + f1);
		System.out.println("\t\tauc roc =" + (float) auc);
	}

	public static void main(String[] args) {
		String baseDir = System.getProperty("user.dir");
		String[] modelFiles = {
				baseDir + "\\model_1.csv",
				baseDir + "\\model_2.csv",
				baseDir + "\\model_3.csv"
		};
		String[] modelNames = { "model_1.csv", "model_2.csv", "model_3.csv" };

		// We need to track best per metric - re-evaluate storing results
		float[] bceArr = new float[3];
		float[] accArr = new float[3];
		float[] precArr = new float[3];
		float[] recArr = new float[3];
		float[] f1Arr = new float[3];
		float[] aucArr = new float[3];

		for (int i = 0; i < modelFiles.length; i++) {
			FileReader filereader;
			List<String[]> allData;
			try {
				filereader = new FileReader(modelFiles[i]);
				CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
				allData = csvReader.readAll();
			} catch (Exception e) {
				System.out.println("Error reading: " + modelFiles[i]);
				continue;
			}

			int tp = 0, fp = 0, tn = 0, fn = 0;
			double bce = 0.0;
			List<float[]> data = new ArrayList<>();

			for (String[] row : allData) {
				int y_true = Integer.parseInt(row[0]);
				float y_predicted = Float.parseFloat(row[1]);
				data.add(new float[] { y_true, y_predicted });

				double p = Math.min(Math.max(y_predicted, 1e-15), 1 - 1e-15);
				bce += -(y_true * Math.log(p) + (1 - y_true) * Math.log(1 - p));

				int y_pred_label = y_predicted >= 0.5 ? 1 : 0;
				if (y_true == 1 && y_pred_label == 1)
					tp++;
				else if (y_true == 0 && y_pred_label == 1)
					fp++;
				else if (y_true == 0 && y_pred_label == 0)
					tn++;
				else
					fn++;
			}

			int n = data.size();
			bce /= n;
			float accuracy = (float) (tp + tn) / n;
			float precision = (float) tp / (tp + fp);
			float recall = (float) tp / (tp + fn);
			float f1 = 2 * precision * recall / (precision + recall);

			data.sort((a, b) -> Float.compare(b[1], a[1]));
			int totalPos = tp + fn;
			int totalNeg = fp + tn;
			double auc = 0.0;
			double prevTpr = 0.0, prevFpr = 0.0;
			int cumTp = 0, cumFp = 0;
			for (float[] point : data) {
				if (point[0] == 1)
					cumTp++;
				else
					cumFp++;
				double tpr = (double) cumTp / totalPos;
				double fpr = (double) cumFp / totalNeg;
				auc += (fpr - prevFpr) * (tpr + prevTpr) / 2.0;
				prevTpr = tpr;
				prevFpr = fpr;
			}

			bceArr[i] = (float) bce;
			accArr[i] = accuracy;
			precArr[i] = precision;
			recArr[i] = recall;
			f1Arr[i] = f1;
			aucArr[i] = (float) auc;

			System.out.println("for " + modelNames[i]);
			System.out.println("\t\tBCE =" + (float) bce);
			System.out.println("\t\tConfusion matrix");
			System.out.println("\t\t\t\ty=1      y=0");
			System.out.println("\t\t\ty^=1\t" + tp + "\t" + fp);
			System.out.println("\t\t\ty^=0\t" + fn + "\t" + tn);
			System.out.println("\t\tAccuracy =" + accuracy);
			System.out.println("\t\tPrecision =" + precision);
			System.out.println("\t\tRecall =" + recall);
			System.out.println("\t\tf1 score =" + f1);
			System.out.println("\t\tauc roc =" + (float) auc);
		}

		// For BCE: lower is better; for others: higher is better
		System.out.println("According to BCE, The best model is " + modelNames[argMin(bceArr)]);
		System.out.println("According to Accuracy, The best model is " + modelNames[argMax(accArr)]);
		System.out.println("According to Precision, The best model is " + modelNames[argMax(precArr)]);
		System.out.println("According to Recall, The best model is " + modelNames[argMax(recArr)]);
		System.out.println("According to F1 score, The best model is " + modelNames[argMax(f1Arr)]);
		System.out.println("According to AUC ROC, The best model is " + modelNames[argMax(aucArr)]);
	}

	static int argMin(float[] arr) {
		int idx = 0;
		for (int i = 1; i < arr.length; i++)
			if (arr[i] < arr[idx])
				idx = i;
		return idx;
	}

	static int argMax(float[] arr) {
		int idx = 0;
		for (int i = 1; i < arr.length; i++)
			if (arr[i] > arr[idx])
				idx = i;
		return idx;
	}
}