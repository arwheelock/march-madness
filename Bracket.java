package marchmadness;

import java.util.*;

public class Bracket {
	
	private static final int PRINT_WIDTH = 4;
	private static final Random rand = new Random();
	
	private String team;
	private Bracket parent;
	private Bracket left;
	private Bracket right;
		
	public Bracket(int depth) {
		this(depth, null);
	}

	public int find(String team) {
		if (team.equals(this.team) || left == null) {
			return 0;
		}
		return Math.min(left.find(team), right.find(team)) + 1;
	}
	
	private Bracket(int depth, Bracket parent) {
		this.parent = parent;
		if (depth > 0) {
			left = new Bracket(depth - 1, this);
			right = new Bracket(depth - 1, this);
		}
	}
	
	public Bracket(List<String> teams) {
		this(teams, null);
	}
	
	private Bracket(List<String> teams, Bracket parent) {
		this.parent = parent;
		if (teams.size() == 1) {
			team = teams.get(0);
		} else {
			left = new Bracket(teams.subList(0, teams.size() / 2), this);
			right = new Bracket(teams.subList(teams.size() / 2, teams.size()), this);
		}
	}
		
	public Bracket(Bracket toCopy) {
		parent = toCopy.parent;
		team = toCopy.team;
		if (!toCopy.isLeaf()) {
			left = new Bracket(toCopy.left);
			right = new Bracket(toCopy.right);
		}
	}
	
	public String play(ProbabilityTable winning) {
		return play(winning, 0);
	}
	
	private String play(ProbabilityTable winning, int round) {
		if (isLeaf()) {
			return team;
		}
		team = winning.choose(left.play(winning, round + 1), right.play(winning, round + 1), round);
		return team;
	}
	
	public String fill(ProbabilityTable winning) {
		return fill(winning, 0);
	}
	
	private String fill(ProbabilityTable winning, int round) {
		if (isLeaf()) {
			return team;
		}
		String team1 = left.fill(winning, round + 1);
		String team2 = right.fill(winning, round + 1);
		team = winning.probability(team1, team2, round) >= 0.5 ? team1 : team2;
		return team;
	}
	
	public int score(Bracket result) {
		if (isLeaf()) {
			return team.equals(result.team) ? 1 : 0;
		}
		return (team.equals(result.team) ? (int) Math.pow(2, depth() - 1) : 0) 
				+ left.score(result.left) + right.score(result.right);
	}
	
	public int depth() {
		if (isLeaf()) {
			return 1;
		}
		return 1 + Math.max(left.depth(), right.depth());
	}
	
	public int childrenCount() {
		if (isLeaf()) {
			return 1;
		}
		return left.childrenCount() + right.childrenCount();
	}
	
	public int leafCount() {
		if (isLeaf()) {
			return 1;
		}
		return left.leafCount() + right.leafCount();
	}
	
	public int round() {
		if (isRoot()) {
			return 0;
		}
		return parent.round() + 1;
	}
	
	public boolean isLeaf() {
		return left == null && right == null;
	}
	
	public boolean isRoot() {
		return parent == null;
	}
	
	public Bracket difference(Bracket other) {
		Bracket difference = new Bracket(0);
		if (!team.equals(other.team)) {
			difference.team = team + "/" + other.team;
		}
		if (!isLeaf()) {
			difference.left = left.difference(other.left);
			difference.right = right.difference(other.right);
		}	
		return difference;
	}
	
	/*
	public double upsetIndex(ProbabilityTable winning, ProbabilityTable choosing) {
		if (team.equals(parent.team)) {
			return 0;
		}
		double winnerIndex = winning.get(team, left.round()) * (1 - choosing.get(team, left.round()));
		//loserIndex
	} */
	
	public String previousLoser() {
		if (isLeaf()) {
			return null;
		}
		return team.equals(left.team) ? right.team : left.team;
	}
	
	public List<Bracket> mutants() {
		List<Bracket> mutants = new Vector<>();
		int mutateableNodeCount = mutateableNodeCount();
		for (int i = 0; i < mutateableNodeCount; i++) {
			Bracket mutant = new Bracket(this);
			mutant.mutate(i, new Stack<>());
			mutants.add(mutant);
		}
		return mutants;
	}
	
	public void mutate() {
		mutate(rand.nextInt(mutateableNodeCount()), new Stack<>());
	}
	
	private int mutate(int target, Stack<String> banned) {
		if (isLeaf() || target < 0) {
			return target;
		}
		
		if (!banned.contains(team)) {
			if (target == 0) {
				if (team.equals(left.team)) {
					team = right.team;
				} else {
					team = left.team;
				}
			}		
			target--;
		}
		banned.push(team);
		target = left.mutate(target, banned);
		target = right.mutate(target, banned);	
		banned.pop();
		return target;
	}
	
	public int mutateableNodeCount() {
		return mutateableNodeCount(new Stack<String>());
	}
	
	public int mutateableNodeCount(Stack<String> banned) {
		if (isLeaf()) {
			return 0;
		}
		int count = banned.contains(team) ? 0 : 1;
		banned.push(team);
		count += left.mutateableNodeCount(banned)
				+ right.mutateableNodeCount(banned);
		banned.pop();
		return count;
	}
	
	public String toString() {
		return toString(new Stack<>());
	}
	
	private String toString(Stack<Boolean> lefts) {
		StringBuilder builder = new StringBuilder("[" + (team == null ? "" : team) + "]\n");
		if (isLeaf()) {
			return builder.toString();
		}
		builder.append(subTreeString(left, lefts, true));
		builder.append(subTreeString(right, lefts, false));
		
		return builder.toString();
	}
	
	private static String subTreeString(Bracket subTree, Stack<Boolean> lefts, boolean left) {
		StringBuilder builder = new StringBuilder();
		builder.append(indent(lefts));		
		lefts.push(left);
		builder.append(subTree.toString(lefts));
		lefts.pop();
		return builder.toString();
	}
	
	private static String indent(List<Boolean> lefts) {
		StringBuilder builder = new StringBuilder();
		for (boolean left : lefts) {
			builder.append(" " + (left ? "|" : " "));
			for (int i = 1; i < PRINT_WIDTH; i++) {
				builder.append(" ");
			}
		}
		builder.append(" \\");
		for (int i = 1; i < PRINT_WIDTH; i++) {
			builder.append("-");
		}
		return builder.toString();
	}
	
}
