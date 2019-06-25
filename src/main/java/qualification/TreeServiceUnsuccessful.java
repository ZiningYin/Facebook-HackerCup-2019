package qualification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TreeServiceUnsuccessful {
	private static String IMPOSSIBLE = "Impossible";

	private TreeServiceUnsuccessful() {
	}

	private void run() {
		try {
			File file = new File("./inputData/qualification/sample_input.txt");
			String OUTPUT_FILE = "./outputData/qualification/sample_output.txt";
			BufferedReader br = new BufferedReader(new FileReader(file));

			StringBuilder sb = new StringBuilder();

			String curLine;
			int problemCounter = 1;

			// skips first line
			br.readLine();
			while ((curLine = br.readLine()) != null) {
				String[] input = curLine.split(" ");
				int numNodes = 1 + Integer.parseInt(input[0]);
				int numRules = Integer.parseInt(input[1]);
				Node[] nodes = createNodes(numNodes);

				boolean failed = false;
				for (int i = 0; i < numRules; i++) {
					curLine = br.readLine();
					if (!failed) {
						failed = !addParentAndRival(nodes, curLine);
					}
				}

				String answer = createAnswer(nodes, failed);
				sb.append("Case #").append(problemCounter).append(": ").append(answer).append("\n");

				problemCounter++;
			}
			saveOutput(OUTPUT_FILE, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean addParentAndRival(Node[] nodes, String curLine) {
		String[] input = curLine.split(" ");
		int nodeId1 = Integer.parseInt(input[0]);
		int nodeId2 = Integer.parseInt(input[1]);
		int newParentId = Integer.parseInt(input[2]);

		Node node1 = nodes[nodeId1];
		Node node2 = nodes[nodeId2];

		boolean success = addParent(nodes, nodeId1, nodeId2, newParentId);

		addRival(node1, nodeId2, newParentId);
		addRival(node2, nodeId1, newParentId);

		success = success && validate(nodes[newParentId]);
		return success;
	}

	private boolean addParent(Node[] nodes, int childId1, int childId2, int mediatorId) {

		Node child1 = nodes[childId1];
		Node child2 = nodes[childId2];
		Node mediator = nodes[mediatorId];

		boolean isChild1New = child1.getParent() == null;
		boolean isChild2New = child2.getParent() == null;
		boolean isParentNew = mediator.getParent() == null;

		if (isChild1New && isChild2New && isParentNew) {
			child1.parent = mediator;
			child2.parent = mediator;
			mediator.subtree[childId1] = true;
			mediator.subtree[childId2] = true;

			// make the mediator a root;
			mediator.parent = nodes[0];
		} else if (isChild1New && isChild2New) {
			child1.parent = mediator;
			child2.parent = mediator;
			mediator.subtree[childId1] = true;
			mediator.subtree[childId2] = true;
		} else if (!isChild1New && !isChild2New && isParentNew) {
			// find common parent, do rival check on the 2 nodes that will be join under this new node

		}


		if (childId1 == mediatorId) {
			//if parent's parent and current parent are not the same, have parent add parent
			// are you
		} else if (childId2 == mediatorId) {

		}

		if (child1.subtree[childId2]) {
			System.out.println("Unexpectedly finding rival in subtree: " + childId1 + " " + childId2 + " " + mediatorId);
			return false;
		}
		if (child2.subtree[childId1]) {
			System.out.println("Unexpectedly finding rival in subtree: " + childId1 + " " + childId2 + " " + mediatorId);
			return false;
		}
		return true;
	}

	private void addRival(Node node, int newRivalId, int mediatorId) {
		// updates the rival for all parents up to but not including the mediator
		node.rivals[newRivalId] = true;

		Node nextNode = node.getParent();
		int nextNodeId = nextNode.getId();

		while (nextNodeId != mediatorId) {
			if (nextNodeId == 0) {
				System.out.println("Unexpectedly reached 0 node with " + node.getId() + " " + newRivalId + " " + mediatorId);
				return;
			} else if (nextNode.subtree[newRivalId]) {
				System.out.println("Unexpectedly finding rival in subtree: " + node.getId() + " " + newRivalId + " " + mediatorId);
				return;
			}
			nextNode.rivals[newRivalId] = true;

			nextNode = nextNode.getParent();
			nextNodeId = nextNode.getId();
		}
	}

	private Node[] createNodes(int numNodes) {
		Node[] nodes = new Node[numNodes];
		for (int i = 0; i < numNodes; i++) {
			nodes[i] = new Node(numNodes, i);
		}
		return nodes;
	}

	private boolean validate(Node parent) {
		int numNodes = parent.getRivals().length;
		boolean[] rivals = parent.getRivals();
		boolean[] subtree = parent.getSubtree();
		for (int i = 1; i < numNodes; i++) {
			if (rivals[i] && subtree[i]) {
				// a rival of the subtree is in the subtree and this parent is not the mediator
				return false;
			}
		}
		return true;
	}

	private String createAnswer(Node[] nodes, boolean isFailure) {
		if (isFailure) {
			return IMPOSSIBLE;
		}
		return null;
	}

	private static void saveOutput(String fileName, String output) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
			bw.write(output);
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
	}

	class Node {
		private int id;
		private Node parent;
		private boolean[] subtree;
		private boolean[] rivals;

		Node(int numNodes, int id) {
			this.id = id;
			this.parent = null;

			this.subtree = new boolean[numNodes];
			this.rivals = new boolean[numNodes];
			this.subtree[id] = true;
		}

		int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		Node getParent() {
			return parent;
		}

		public void setParent(Node parent) {
			this.parent = parent;
		}

		boolean[] getSubtree() {
			return subtree;
		}

		public void setSubtree(boolean[] subtree) {
			this.subtree = subtree;
		}

		boolean[] getRivals() {
			return rivals;
		}

		public void setRivals(boolean[] rivals) {
			this.rivals = rivals;
		}
	}

	public static void main(String[] args) {
		TreeServiceUnsuccessful treeServiceUnsuccessful = new TreeServiceUnsuccessful();
		treeServiceUnsuccessful.run();
	}

}
