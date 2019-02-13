package marchmadness;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

public class Allan {
	
	private static final int TRIALS = 100000;

	public static void main(String[] args) throws IOException {
		List<String> teams = readTeams("res/teams.csv");
		ProbabilityTable winning = readProbabilityTable("res/winning.csv");
		
		Map<String, Double> expected = new HashMap<>();
		for (String team : teams) {
			expected.put(team, 0.0);
		}
		
		for (int i = 0; i < TRIALS; i++) {
			Bracket bracket = new Bracket(teams);
				bracket.play(winning);	
				
			for (String team : teams) {
				double value;
				switch (bracket.find(team)) {
				case 0: value = 0.32; break;
				case 1: value = 0.16; break;
				case 2: value = 0.08; break;
				case 3: value = 0.04; break;
				case 4: value = 0.02; break;
				case 5: value = 0.0025; break;
				default: value = 0; 
				}
				expected.put(team, expected.get(team) + value);
			}
		}
		
		for (String team : teams) {
			System.out.printf("%s: %f\n", team, expected.get(team) / TRIALS);
		}
	}
	
	private static List<String> readTeams(String file) throws IOException {
		List<String> teams = new Vector<>();		
		Scanner in = new Scanner(new File(file));
		while (in.hasNextLine()) {
			teams.add(in.nextLine());
		}
		in.close();	
		return teams;
	}
	
	private static ProbabilityTable readProbabilityTable(String file) throws IOException {
		ProbabilityTable table = new ProbabilityTable();
		Scanner in = new Scanner(new File(file));
		while (in.hasNextLine()) {
			String[] line = in.nextLine().split(",");
			List<Double> probabilities = new Vector<>();
			for (int i = 1; i < line.length; i++) {
				probabilities.add(Double.parseDouble(line[i]));
			}
			table.add(line[0], probabilities);
		}
		in.close();
		return table;
	}
	
}
