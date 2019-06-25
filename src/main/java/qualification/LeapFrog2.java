package qualification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LeapFrog2 {
	public static void main(String[] args) {
		try {
			File file = new File("./inputData/qualification/leapfrog_ch_.txt");
			String OUTPUT_FILE = "./outputData/qualification/leapfrog_ch2.txt";
			BufferedReader br = new BufferedReader(new FileReader(file));

			StringBuilder sb = new StringBuilder();

			int curChar;
			int frogCounter = 0;
			int lilyCounter = 0;
			int problemCounter = 1;
			char answer;

			// removes the first line
			br.readLine();
			while ((curChar = br.read()) != -1) {
				if (curChar == 46) {
					lilyCounter++;
				} else if (curChar == 66) {
					frogCounter++;
				} else if (curChar == 65) {
				} else if (curChar == 10) {
					answer = lilyCounter > 0 && (frogCounter >= 2 || frogCounter >= lilyCounter) ? 'Y' : 'N';
					sb.append("Case #").append(problemCounter).append(": ").append(answer).append("\n");

					//resets
					frogCounter = 0;
					lilyCounter = 0;
					problemCounter++;
				} else if (lilyCounter > 0 && frogCounter >= 2) {
					br.readLine();
					sb.append("Case #").append(problemCounter).append(": ").append('Y').append("\n");

					//resets
					frogCounter = 0;
					lilyCounter = 0;
					problemCounter++;
				} else {
					System.out.println("error: " + curChar);
				}
			}
			saveOutput(OUTPUT_FILE, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void saveOutput(String fileName, String output) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
			bw.write(output);
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
	}
}
