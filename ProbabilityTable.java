package marchmadness;

import java.util.*;

public class ProbabilityTable {

	private Map<String, List<Double>> table;
	
	public ProbabilityTable() {
		table = new HashMap<>();
	}
	
	public void add(String team, List<Double> values) {
		table.put(team, values);
	}
	
	public double get(String team, int round) {
		return table.get(team).get(round);
	}
	
	public String choose(String team1, String team2, int round) {
		return Math.random() < probability(team1, team2, round) ? team1 : team2;
	}
	
	public double probability(String team1, String team2, int round) {
		return get(team1, round) / (get(team1, round) + get(team2, round));
	}
	
}
