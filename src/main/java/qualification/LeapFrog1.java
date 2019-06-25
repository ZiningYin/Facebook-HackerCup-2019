package qualification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LeapFrog1 {
	public static void main(String[] args) {
		try {
			File file = new File("./inputData/qualification/leapfrog_ch_.txt");
			String OUTPUT_FILE = "./outputData/qualification/leapfrog_ch1.txt";
			BufferedReader br = new BufferedReader(new FileReader(file));

			StringBuilder sb = new StringBuilder();
			int numProblems = Integer.parseInt(br.readLine());


			int curChar;
			int frogLilyCounter = 0;
			int problemCounter = 1;
			boolean hasLily = false;
			char answer;

			while ((curChar = br.read()) != -1) {
				if (curChar == 46) {
					hasLily = true;
					frogLilyCounter--;
				} else if (curChar == 66) {
					frogLilyCounter++;
				} else if (curChar == 65) {
				} else if (curChar == 10) {
					answer = hasLily && frogLilyCounter >= 0 ? 'Y' : 'N';
					sb.append("Case #").append(problemCounter).append(": ").append(answer).append("\n");

					//resets
					hasLily = false;
					frogLilyCounter = 0;
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
