package round1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class GraphsAsAService {
	private int numNodesPlus1;
	private int numEdges;
	private int[][] edges;
	private int[][] requirements;
	private int[][] shortestDistance;

	private void run() {
		try {
			File file = new File("./inputData/round1/graphs_as_a_service.txt");
			String OUTPUT_FILE = "./outputData/round1/graphs_as_a_service_output.txt";
			BufferedReader br = new BufferedReader(new FileReader(file));

			StringBuilder sb = new StringBuilder();

			String curLine;
			int problemCounter = 1;

			// skips first line
			br.readLine();
			while ((curLine = br.readLine()) != null) {
				String[] input = curLine.split(" ");
				numNodesPlus1 = Integer.parseInt(input[0]) + 1;
				numEdges = Integer.parseInt(input[1]);
				edges = new int[numNodesPlus1][numNodesPlus1];
				shortestDistance = new int[numNodesPlus1][numNodesPlus1];
				requirements = new int[numEdges][3];

				for (int i = 0; i < numEdges; i++) {
					curLine = br.readLine();
					input = curLine.split(" ");
					int node1 = Integer.parseInt(input[0]);
					int node2 = Integer.parseInt(input[1]);
					int length = Integer.parseInt(input[2]);

					edges[node1][node2] = length;
					edges[node2][node1] = length;

					requirements[i][0] = node1;
					requirements[i][1] = node2;
					requirements[i][2] = length;
				}

				computeMinSpanningTree();

				String answer = createAnswer(isValid());
				sb.append("Case #").append(problemCounter).append(": ").append(answer);

				problemCounter++;
			}
			saveOutput(OUTPUT_FILE, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isValid() {
		for (int i = 0; i < numEdges; i++) {
			if (requirements[i][2] != shortestDistance[requirements[i][0]][requirements[i][1]]) {
				return false;
			}
			if (requirements[i][2] != edges[requirements[i][0]][requirements[i][1]]) {
				System.out.println("Unexpected edge not equal to shortest distance when requirements are met");
			}
		}
		return true;
	}

	private void computeMinSpanningTree() {
		for (int i = 1; i < numNodesPlus1; i++) {
			computeMinSpanningTree(i);
		}
	}

	private void computeMinSpanningTree(int A) {

		boolean[] isQueued = new boolean[numNodesPlus1];
		int[] shortestDistanceFromA = new int[numNodesPlus1];
		Queue<Integer> queue = new LinkedList<>();

		for (int i = 1; i < numNodesPlus1; i++) {
			//does not allow self calls
			if (edges[A][i] > 0) {
				shortestDistanceFromA[i] = edges[A][i];
				queue.add(i);
				isQueued[i] = true;
			}
		}

		while (!queue.isEmpty()) {
			int updateNode = queue.remove();
			isQueued[updateNode] = false;
			int distanceFromA = shortestDistanceFromA[updateNode];
			for (int i = 1; i < numNodesPlus1; i++) {
				if (i == A) continue;

				// cannot handle negative weighed edges
				if (edges[updateNode][i] > 0 && (shortestDistanceFromA[i] == 0 || distanceFromA + edges[updateNode][i] < shortestDistanceFromA[i])) {
					shortestDistanceFromA[i] = distanceFromA + edges[updateNode][i];
					if (!isQueued[i]) {
						queue.add(i);
						isQueued[i] = true;
					}
				}
			}
		}
		for (int i = 1; i < numNodesPlus1; i++) {
			if ((shortestDistance[A][i] != shortestDistanceFromA[i] && shortestDistance[A][i] != 0) || (shortestDistance[i][A] != shortestDistanceFromA[i] && shortestDistance[A][i] != 0)) {
				System.out.println("contradiction in min spanning tree calculations");
			}
			if (shortestDistanceFromA[i] != 0) {
				shortestDistance[A][i] = shortestDistanceFromA[i];
				shortestDistance[i][A] = shortestDistanceFromA[i];
			}
		}

	}


	public static void main(String[] args) {
		GraphsAsAService graphsAsAService = new GraphsAsAService();
		graphsAsAService.run();
	}


	private String createAnswer(boolean isSuccess) {

		int actualNumEdges = 0;

		StringBuilder sb = new StringBuilder();

		if (isSuccess) {
			for (int i = 1; i < numNodesPlus1; i++) {
				for (int j = i; j < numNodesPlus1; j++) {
					if (edges[i][j] > 0) {
						actualNumEdges++;
						sb.append(i).append(" ").append(j).append(" ").append(edges[i][j]).append('\n');
					}
				}
			}
		} else {
			return "Impossible\n";
		}
		if (actualNumEdges != numEdges) {
			System.out.println("unexpected in createAnswer");
		}
		return numEdges + "\n" + sb.toString();
	}

	private static void saveOutput(String fileName, String output) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
			bw.write(output);
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
	}
}
