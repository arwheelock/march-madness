package marchmadness;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Pool {
	
	private Bracket myBracket;
	private int poolSize;
	private List<String> teams;
	private ProbabilityTable winning;
	private ProbabilityTable choosing;
	
	public Pool(Bracket myBracket, int poolSize, List<String> teams, ProbabilityTable winning, ProbabilityTable choosing) {
		this.myBracket = myBracket;
		this.poolSize = poolSize;		
		this.teams = teams;
		this.winning = winning;
		this.choosing = choosing;
	}

	private double play(int trials) {
		double result = 0;
		for (int i = 0; i < trials; i++) {
			result += play();
		}
		return result / trials;
	}
	
	private double play() {
		Bracket result = new Bracket(teams);
		result.play(winning);			
		int score = myBracket.score(result);
		
		int ties = 1;
		for (int i = 1; i < poolSize; i++) {
			Bracket other = new Bracket(teams);
			other.play(choosing);
			
			int otherScore = other.score(result);
			if (score < otherScore) {
				return 0;
			} else if (score == otherScore) {
				ties++;
			} 
		}
		return 1.0 / ties;
	}
			
	public static void main(String[] args) throws IOException {
		List<String> teams = readTeams("res/teams.csv");
		ProbabilityTable winning = readProbabilityTable("res/winning.csv");
		ProbabilityTable choosing = readProbabilityTable("res/choosing.csv");
		
		Bracket start = new Bracket(teams);
		start.fill(winning);
		System.out.println(start);
		
		List<Bracket> mutants = new Vector<>();
		mutants.add(start);
		while (true) {
			mutants.addAll(mutants.get(0).mutants());
			Map<Bracket, Double> fitnesses = new HashMap<>();

			int rounds = 1000;
			while (mutants.size() > 1) {
				System.out.println(mutants.size());
				for (Bracket mutant : mutants) {
					double fitness = new Pool(mutant, 20, teams, winning, choosing).play(rounds);
					if (fitnesses.containsKey(mutant)) {
						fitnesses.put(mutant, (fitnesses.get(mutant) + 2 * fitness) / 3);
					} else {
						fitnesses.put(mutant, fitness);
					}
				}
				
				Collections.sort(mutants, new Comparator<Bracket>() {
					public int compare(Bracket b1, Bracket b2) {
						return fitnesses.get(b1).compareTo(fitnesses.get(b2));
					}
				});
				
				mutants = mutants.subList((mutants.size() + 1) / 2, mutants.size());
				rounds *= 2;
			}
			
			System.out.println(mutants.get(0).difference(start));
			System.out.println(fitnesses.get(mutants.get(0)));
		}
		
		/*
		List<String> teams = readTeams("res/teams.csv");
		ProbabilityTable winning = readProbabilityTable("res/winning.csv");
		ProbabilityTable choosing = readProbabilityTable("res/choosing.csv");
		
		Bracket start = new Bracket(teams);
		start.fill(winning);
		System.out.println(start);
		
		Bracket best = new Bracket(start);	
		Pool pool = new Pool(best, POOL_SIZE, teams, winning, choosing);
		double bestFitness = pool.play(100000);
		System.out.println(bestFitness); 
		
		while (true) {
			List<Bracket> mutants = new Vector<>();
			while (mutants.size() < 10) {
				Bracket mutant = new Bracket(best);
				mutant.mutate();
				mutants.add(mutant);
			}
			
			Bracket promising = null;
			double promisingFitness = -1;
			for (Bracket bracket : mutants) {
				pool = new Pool(bracket, 20, teams, winning, choosing);
				double fitness = pool.play(10000);
				if (fitness > promisingFitness) {
					promising = bracket;
					promisingFitness = fitness;
				}
			}
			
			if (promisingFitness > bestFitness) {
				pool = new Pool(promising, 20, teams, winning, choosing);
				double fitness = pool.play(100000);
				if (fitness > bestFitness) {
					best = promising;
					bestFitness = fitness;
					System.out.println(best.difference(start));
				}
				System.out.println(fitness);
			}
		}
		*/
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
