package round1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ClassTreasurer {
	private static int MOD_VALUE = 1000000007;
	private int numStudents;
	private int threshold;
	int problemCounter;
	private char[] votes;
	private final List<Integer> expCycles;

	// find super chains where max (VB-VA) > k
	// record the start and end of the super chains
	// start is when VB > VA
	// end is when total VB = total VA

	public ClassTreasurer(List<Integer> expCycles) {
		this.problemCounter = 1;
		this.expCycles = expCycles;
	}


	private void run() {
		try {
			File file = new File("./inputData/round1/class_treasurer.txt");
			String OUTPUT_FILE = "./outputData/round1/class_treasurer_sample_output.txt";
			BufferedReader br = new BufferedReader(new FileReader(file));

			StringBuilder sb = new StringBuilder();

			String curLine;

			// skips first line
			br.readLine();
			while ((curLine = br.readLine()) != null) {
				String[] input1 = curLine.split(" ");
				String input2 = br.readLine();

				numStudents = Integer.parseInt(input1[0]);
				threshold = Integer.parseInt(input1[1]);
				votes = input2.toCharArray();

				List<Integer> studentsToBribe = chooseStudentsToBribe();
				validate(studentsToBribe);

				//long answer = studentsToBribe.stream().map(index -> Long.valueOf(this.expCycles.get(index))).reduce(0L, Long::sum) % MOD_VALUE;
				int answer = 0;
				for (int index : studentsToBribe) {
					answer = (answer + this.expCycles.get(index)) % MOD_VALUE;
				}
				sb.append("Case #").append(problemCounter).append(": ").append(answer).append("\n");

				problemCounter++;
			}
			saveOutput(OUTPUT_FILE, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean validate(List<Integer> studentsToBribe) {
		int currentDisadvantage = 0;

		for (int index : studentsToBribe) {
			votes[index - 1] = 'A';
		}

		for (int i = 0; i < numStudents; i++) {
			if (votes[i] == 'B') {
				currentDisadvantage++;
				if (threshold < currentDisadvantage) {
					System.out.println(problemCounter + " failed");
					return false;
				}
			} else {
				currentDisadvantage--;
				if (currentDisadvantage <= 0) {
					currentDisadvantage = 0;
				}
			}
		}
		return true;
	}

	// the whole reason this is not a pure greedy algo is because you have to decided between bribing the level 1 student or level 2 student, and you only know that after you know what the peak is
	private List<Integer> chooseStudentsToBribe() {
		if (threshold == 0) {
			return bribeAllOfThem();
		}

		int currentDisadvantage = 0;
		int maxDisadvantage = 0;
		int maxDisadvantageIndex = 0;
		int superSetStart = 0;
		List<Integer> studentsToBribe = new ArrayList<>();

		for (int i = 0; i < numStudents; i++) {
			if (votes[i] == 'B') {
				currentDisadvantage++;

				if (currentDisadvantage == 1) {
					superSetStart = i;
				}
				if (maxDisadvantage < currentDisadvantage) {
					maxDisadvantage = currentDisadvantage;
					maxDisadvantageIndex = i;
				}
			} else {
				currentDisadvantage--;

				// if it just hit 0 and the drop was equal to threshold before, so it is not a peak
				if (currentDisadvantage <= 0) {
					maxDisadvantage = 0;
					maxDisadvantageIndex = 0;
					currentDisadvantage = 0;
				}
				// even if it only dropped by the threshold, there is a person below the threshold that needs to be bribed
				else if (maxDisadvantage - currentDisadvantage == threshold) {

					//I just hit a peak, I'm going to bribe all these students and bring my current level down to 0
					studentsToBribe.addAll(chooseStudentsToBribe(maxDisadvantage, superSetStart, maxDisadvantageIndex));
					maxDisadvantage = 0;
					maxDisadvantageIndex = 0;
					currentDisadvantage = 0;
				}
			}
		}

		// note at the end the maxDisadvantage has to be greater than threshold because is ending. If it is equal, then nothing needs to be done.
		if (maxDisadvantage > threshold) {
			studentsToBribe.addAll(chooseStudentsToBribe(maxDisadvantage, superSetStart, maxDisadvantageIndex));
		}
		return studentsToBribe;
	}

	private List<Integer> chooseStudentsToBribe(int maxDisadvantage, int superSetStart, int superSetEnd) {
		int currentDisadvantage = 0;
		List<Integer> studentsToBribe = new ArrayList<>();

		int lastBribe = maxDisadvantage - this.threshold;
		int currentBribe = lastBribe % 2 == 0 ? 2 : 1;

		if (superSetEnd == 0) {
			System.out.println("unexpected superSetEnd value");
			return new ArrayList<>();
		}

		TreeMap<Integer, Integer> dips = findDips(superSetStart, superSetEnd);

		for (int i = superSetStart; i <= superSetEnd; i++) {
			if (votes[i] == 'B') {
				currentDisadvantage++;

				if (currentDisadvantage == currentBribe) {

					// to shift the index, we start at 0, but students start at 1
					studentsToBribe.add(i + 1);

					// we only need to bribe every other student, even for threshold 1. Threshold 0 is taken care of as a special case
					currentBribe += 2;
					if (currentBribe > lastBribe) {
						return studentsToBribe;
					} else {

						// skip ahead logic - addresses the mini dips that makes us waste bribes on unhelpful students who are at the right levels
						Map.Entry<Integer, Integer> entry;

						// get the smallest entries according to height
						// dips are strictly increasing based on index
						while ((entry = dips.firstEntry()) != null) {
							// if the dip happened already
							if (entry.getValue() <= i) {
								dips.remove(entry.getKey());
							}
							// if the dip is lower than the height of the next student to bribe, we have to skip past the dip or we could waste a bribe
							else if (entry.getKey() < currentBribe) {
								//pretend we just processed the dip
								i = entry.getValue();
								currentDisadvantage = entry.getKey();
								dips.remove(entry.getKey());
							} else {
								// dips are currently unrelated to us
								break;
							}
						}
					}
				}
			} else {
				// we don't care if it drops because we already know that it won't be a peak
				currentDisadvantage--;
			}
		}

		System.out.println("chooseStudentsToBribe completely unexpected");
		return new ArrayList<>();
	}

	// find the relevant dips
	private TreeMap<Integer, Integer> findDips(int start, int end) {
		boolean prevMoveDown = false;
		int previousDisadvantage = 0;

		TreeMap<Integer, Integer> tree = new TreeMap<>();

		for (int i = start; i <= end; i++) {
			if (votes[i] == 'B') {
				// moving up now, so the dip is before this
				if (prevMoveDown) {

					//remove every dip that's higher and before it, because they don't matter
					while (tree.higherKey(previousDisadvantage) != null) {
						tree.remove(tree.higherKey(previousDisadvantage));
					}

					// only the latest dip of the same height matters
					tree.put(previousDisadvantage, i - 1);
				}
				prevMoveDown = false;
				previousDisadvantage++;
			} else {
				prevMoveDown = true;
				previousDisadvantage--;
			}
		}
		return tree;
	}

	private List<Integer> bribeAllOfThem() {
		List<Integer> studentsToBribe = new ArrayList<>();

		for (int i = 0; i < numStudents; i++) {
			if (votes[i] == 'B') {
				studentsToBribe.add(i + 1);
			}
		}
		return studentsToBribe;
	}

	private static List<Integer> computeExpCycle() {
		List<Integer> expRecords = new ArrayList<>();

		int newValue = 1;
		for (int i = 0; i < 1000000; i++) {
			expRecords.add(i, newValue);
			newValue = (newValue * 2) % MOD_VALUE;
		}
		return expRecords;
	}

	public static void main(String[] args) {
		ClassTreasurer classTreasurer = new ClassTreasurer(computeExpCycle());
		classTreasurer.run();
	}

	private static void saveOutput(String fileName, String output) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
			bw.write(output);
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
	}
}
