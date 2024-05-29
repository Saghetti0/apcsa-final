package blackjack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import blackjack.game.Game;
import blackjack.game.GameLogger;
import blackjack.game.Player;

public class MultiRunner {
	private static final int NUM_ROUNDS = 1000;
	private static final int NUM_GAMES = 50;
	
	public static void main(String[] args) throws IOException {
		// scan for strategy files
		Scanner scan = new Scanner(System.in);
		String[] strategies = strategyNames();
		
		for (int i=0; i<strategies.length; i++) {
			System.out.println(i+1 + ") " + strategies[i]);
		}
		
		Map<String, Class<Player>> playerClasses = new HashMap<String, Class<Player>>();
		
		for (String strategyName : strategies) {
			System.out.println("Loading " + strategyName);
			
			try {
				Class<Player> playerClass = (Class<Player>) Class.forName("strategy." + strategyName);
				playerClasses.put(strategyName, playerClass);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		for (String stratName : playerClasses.keySet()) {
			System.out.println("Evaluating " + stratName);

			new File("game_logs/" + stratName).mkdirs();
			
			out:
			for (int gameNo=0; gameNo<NUM_GAMES; gameNo++) {
				System.out.println("Game " + gameNo);
				
				Player p;
				try {
					p = playerClasses.get(stratName).newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					System.out.println("An error occurred while initializing the player for this round!");
					e.printStackTrace();
					break out;
				}
				
				String outPrefix = "game_logs/" + stratName + "/game_" + gameNo;				
				
				final BufferedWriter fwriter = new BufferedWriter(new FileWriter(outPrefix + "_detail.txt"));
				final BufferedWriter dataWriter = new BufferedWriter(new FileWriter(outPrefix + "_data.yaml"));
				
				try {
					Game game = new Game(p, msg -> {
						try {
							fwriter.append(msg + "\n");
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
					
					game.setClampBets(true);

					int[] moneyOverTime = new int[NUM_ROUNDS];
					
					int roundNo=0;
					
					for (; roundNo<NUM_ROUNDS; roundNo++) {
						try {							
							game.doRound();
							moneyOverTime[roundNo] = game.getMoney();
						} catch (Exception e) {
							System.out.println("Strategy errored on round " + roundNo + ", check logs");
							fwriter.append("\n\nError: strategy crashed on roundNo=" + roundNo + "\n");
							e.printStackTrace(new PrintWriter(fwriter));
							break;
						}
					}					

					dataWriter.write("round_count: " + roundNo + "\n");
					dataWriter.write("bal_hist: " + Arrays.toString(moneyOverTime) + "\n");
				} finally {
					fwriter.close();
					dataWriter.close();
				}
			}
		}
		
	}
	
	public static String[] strategyNames() {
		File folder = new File("./src/strategy");
		File[] files = folder.listFiles();
		String[] classNames = new String[files.length];
		
		for (int i=0; i<files.length; i++) {
			classNames[i] = files[i].getName().split("[.]")[0];
		}
		
		return classNames;
	}
}
