package qualification;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MrX {


	public static void main(String[] args) {
		try {
			ScriptEngineManager sem = new ScriptEngineManager();
			ScriptEngine se = sem.getEngineByName("JavaScript");

			File file = new File("./inputData/qualification/mr_x.txt");
			String OUTPUT_FILE = "./outputData/qualification/mr_x_out.txt";
			BufferedReader br = new BufferedReader(new FileReader(file));

			StringBuilder sb = new StringBuilder();

			String rawEquation;
			int problemCounter = 1;

			// skips first line
			br.readLine();
			while ((rawEquation = br.readLine()) != null) {
				int answer = isXIrrelevant(rawEquation, se) ? 0 : 1;
				sb.append("Case #").append(problemCounter).append(": ").append(answer).append("\n");

				problemCounter++;
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

	private static boolean isXIrrelevant(String rawEquation, ScriptEngine se) {

		String equationXisTrue = rawEquation.replace('x', '1').replace('X', '0');
		String equationXisFalse = rawEquation.replace('x', '0').replace('X', '1');

		try {
			return se.eval(equationXisTrue).equals(se.eval(equationXisFalse));
		} catch (Exception e) {

			System.out.println("Invalid Expression");
			e.printStackTrace();

		}

		return false;
	}
}
