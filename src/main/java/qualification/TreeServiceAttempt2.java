package qualification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TreeServiceAttempt2 {

	private int numNodes;
	private int numRules;
	private int[][] restrictions; // child child LCA
	private int[][] rivalAndMediators;
	private Node[] nodes;

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

				// num nodes is displaced by 1 to account for the implied node above the root
				this.numNodes = 1 + Integer.parseInt(input[0]);
				this.numRules = Integer.parseInt(input[1]);
				this.nodes = createNodes(this.numNodes);
				this.restrictions = new int[this.numRules][3];
				this.rivalAndMediators = new int[this.numNodes][this.numNodes];

				// reads restrictions
				for (int i = 0; i < this.numRules; i++) {
					curLine = br.readLine();

					String[] restriction = curLine.split(" ");
					this.restrictions[i][0] = Integer.parseInt(restriction[0]);
					this.restrictions[i][1] = Integer.parseInt(restriction[1]);
					this.restrictions[i][2] = Integer.parseInt(restriction[2]);
				}

				firstStepUpdateNodes();
				boolean success = true;

				for (int i = 0; i < numRules; i++) {
					success = determineLevelsCycleCheck();
					if (success) {
						boolean treeModified = joinParents(false);
						if (!treeModified) {
							treeModified = joinParents(true);
							if (!treeModified) {
								break;
							}
						}
					} else {
						break;
					}
				}

				if (success) {
					success = joinRootNodes();
				}
				if (success) {
					success = determineLevelsCycleCheck();
				}
				if (success) {
					success = validateAnswer();
				}

				String answer = createAnswer(success);
				sb.append("Case #").append(problemCounter).append(": ").append(answer).append("\n");

				problemCounter++;
			}
			saveOutput(OUTPUT_FILE, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void firstStepUpdateNodes() {
		for (int i = 0; i < numRules; i++) {
			int[] restriction = restrictions[i];
			Node child1 = nodes[restriction[0]];
			Node child2 = nodes[restriction[1]];
			Node mediator = nodes[restriction[2]];

			if (restriction[0] == restriction[2]) {
				//child 1 is also mediator
				mediator.addChildren(child2, null, this.numNodes);
				child2.addParent(null, mediator);
			} else if (restriction[1] == restriction[2]) {
				//child 2 is also mediator
				mediator.addChildren(child1, null, this.numNodes);
				child1.addParent(null, mediator);
			} else {
				mediator.addChildren(child1, child2, this.numNodes);
				child1.addParent(child2, mediator);
				child2.addParent(child1, mediator);
				rivalAndMediators[child1.id][mediator.id] = child2.id;
				rivalAndMediators[child2.id][mediator.id] = child1.id;
			}
		}
	}

	private boolean joinRootNodes() {
		List<Node> roots = new ArrayList<>();
		for (int i = 1; i < numNodes; i++) {
			Node node = nodes[i];
			if (node.level == 1) {
				roots.add(node);
			}
		}

		if (roots.size() == 0) return false;
		Node baseRoot = roots.get(0);
		int numRoots = roots.size();
		for (int i = 1; i < numRoots; i++) {
			roots.get(i).addParent(null, baseRoot);
			baseRoot.addChildren(roots.get(i), null, this.numNodes);
			roots.get(i).parentId = baseRoot.id;
		}
		return determineLevelsCycleCheck();
	}


	private boolean determineLevelsCycleCheck() {
		for (int i = 1; i < numNodes; i++) {
			boolean levelUpdated = false;
			for (int j = 1; j < numNodes; j++) {
				Node node = nodes[j];

				int maxLevel = node.level;
				for (int k = 1; k < numNodes; k++) {
					if (!node.elders[k]) continue;

					if (nodes[k].level + 1 > maxLevel) {
						maxLevel = nodes[k].level + 1;
						node.parentId = k;
						levelUpdated = true;
					}
				}
				node.level = maxLevel;
			}
			if (!levelUpdated) {
				return true;
			}
		}
		return false;
	}


	private boolean joinParents(boolean forcedAdoption) {
		boolean hasChanged = false;
		for (Node node : this.nodes) {
			Map<Integer, Set<Node>> parentsByGrandparent = new HashMap<>();
			for (int i = 1; i < this.numNodes; i++) {
				if (!node.elders[i]) {
					continue;
				}
				Node elder = nodes[i];
				Set<Node> parentSet = parentsByGrandparent.computeIfAbsent(elder.parentId, grandParentId -> new HashSet<>());
				parentSet.add(elder);
			}

			if (parentsByGrandparent.size() == 0) continue;

			for (Set<Node> siblings : parentsByGrandparent.values()) {
				if (siblings.size() == 1) continue;

				Set<Node> removedSiblings = new HashSet<>();
				for (Node sibling1 : siblings) {
					if (removedSiblings.contains(sibling1)) continue;
					removedSiblings.add(sibling1);

					for (Node sibling2 : siblings) {
						if (removedSiblings.contains(sibling2)) continue;

						Node adoptedChild = getAdoptedChild(sibling1, sibling2);
						if (adoptedChild == sibling1) {
							makeFamily(sibling2, sibling1, node);
							hasChanged = true;
							break;
						} else if (adoptedChild == sibling2) {
							makeFamily(sibling1, sibling2, node);
							removedSiblings.add(sibling2);
							hasChanged = true;
						} else if (forcedAdoption) {
							hasChanged = true;
							if (sibling1.id < sibling2.id) {
								makeFamily(sibling1, sibling2, node);
								removedSiblings.add(sibling2);
							} else {
								makeFamily(sibling2, sibling1, node);
								break;
							}
						}
					}
				}
			}
		}
		return hasChanged;
	}

	private boolean validateAnswer() {
		for (int i = 0; i < numRules; i++) {
			int[] restriction = restrictions[i];
			Set<Integer> parents = new HashSet<>();
			Node node1 = nodes[restriction[0]];
			Node node2 = nodes[restriction[1]];
			int mediatorId = restriction[2];

			Node parent = node1;
			boolean foundMediator = false;
			int counter = 0;
			while (parent.id != 0 && counter < this.numNodes) {
				if (parent.id == mediatorId) {
					foundMediator = true;
					break;
				}
				counter++;
				parents.add(parent.id);
				parent = nodes[parent.parentId];
			}
			if (!foundMediator) {
				return false;
			}

			parent = node2;
			foundMediator = false;
			counter = 0;
			while (parent.id != 0 && counter < this.numNodes) {
				if (parent.id == mediatorId) {
					foundMediator = true;
					break;
				} else if (parents.contains(parent.id)) {
					return false;
				}
				counter++;
				parent = nodes[parent.parentId];
			}
			if (!foundMediator) {
				return false;
			}
		}
		return true;
	}

	// determines if the two siblings should be parent child, merge them if they should be
	private Node getAdoptedChild(Node sibling1, Node sibling2) {
		for (int i = 1; i < numNodes; i++) {
			// if a subFamily member is a rival of the sibling1's subFamily
			if (sibling1.subFamily[i] && sibling2.rivals[i]) {

				// other sibling1 has the rival of the sibling1
				int rival = rivalAndMediators[i][sibling1.id];

				// the rival node might not be in the sibling2's subFamily
				if (rival != 0 && sibling2.subFamily[rival]) {
					// if the mediator of the 2 rival nodes is sibling1. sibling2 must be adopted into sibling1's subFamily
					return sibling2;
				}

				rival = rivalAndMediators[i][sibling2.id];
				if (rival != 0 && sibling1.subFamily[rival]) {
					// if the mediator of the 2 rival nodes is sibling2. sibling1 must be adopted into sibling2's subFamily
					return sibling1;
				}
			}
		}
		return null;
	}

	private void makeFamily(Node newParent, Node newChild, Node newGrandChild) {
		newParent.addChildren(newChild, null, this.numNodes);

		newChild.addParent(null, newParent);
		newChild.parentId = newParent.id;
		newChild.level = newParent.level + 1;

		newGrandChild.addParent(null, newChild);
		newGrandChild.parentId = newChild.id;
		newGrandChild.level = newChild.level + 1;
	}

	private Node[] createNodes(int numNodes) {
		Node[] nodes = new Node[numNodes];
		nodes[0] = new Node(numNodes, 0);
		nodes[0].level = 0;
		nodes[0].elders[0] = false;
		nodes[0].parentId = -1;

		for (int i = 1; i < numNodes; i++) {
			nodes[i] = new Node(numNodes, i);
		}
		return nodes;
	}

	private String createAnswer(boolean isSuccess) {
		StringBuilder sb = new StringBuilder();
		if (isSuccess) {
			for (int i = 1; i < numNodes; i++) {
				sb.append(nodes[i].parentId).append(" ");
			}
		} else {
			return "Impossible";
		}
		return sb.toString();
	}

	private static void saveOutput(String fileName, String output) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
			bw.write(output);
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
	}

	public static void main(String[] args) {
		TreeServiceAttempt2 serviceAttempt2 = new TreeServiceAttempt2();
		serviceAttempt2.run();
	}

	class Node {
		private int id;
		private int parentId;
		private int level;
		private boolean[] rivals;
		private boolean[] subFamily;
		private boolean[] elders;

		Node(int numNodes, int id) {
			this.id = id;
			this.level = 1;
			this.parentId = 0;
			this.rivals = new boolean[numNodes];
			this.subFamily = new boolean[numNodes];
			this.elders = new boolean[numNodes];

			this.subFamily[id] = true;
			this.elders[0] = true;
		}

		void addParent(Node rival, Node mediator) {
			for (int i = 1; i < numNodes; i++) {
				this.elders[i] |= mediator.elders[i];
			}
			this.elders[mediator.id] = true;

			if (rival != null) {
				rivals[rival.id] = true;
			}
		}

		void addChildren(Node child1, Node child2, int numNodes) {
			for (int i = 1; i < numNodes; i++) {
				subFamily[i] |= child1.subFamily[i];
				this.rivals[i] |= child1.rivals[i];
			}

			if (child2 != null) {
				for (int i = 1; i < numNodes; i++) {
					subFamily[i] |= child2.subFamily[i];
					this.rivals[i] |= child2.rivals[i];
				}
			}
		}
	}
}
