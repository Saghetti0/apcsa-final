package blackjack;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import blackjack.game.Game;
import blackjack.game.GameLogger;
import blackjack.game.Player;

public class Main {
	public static void main(String[] args) {
		// scan for strategy files
		Scanner scan = new Scanner(System.in);
		String[] strategies = strategyNames();
		
		for (int i=0; i<strategies.length; i++) {
			System.out.println(i+1 + ") " + strategies[i]);
		}
		
		System.out.print("Enter the strategy number you want to run: ");
		int stratNumber = scan.nextInt() - 1;
		String stratName = strategies[stratNumber];
		
		Player p = null;
		
		try {
			p = (Player) Class.forName("strategy." + stratName).newInstance();
		} catch (IllegalAccessException e) {
			System.out.println("");
			System.out.println("Error: the class for the strategy is not defined as public!");
			System.out.println("Make sure that the definition looks like this:");
			System.out.println("");
			System.out.println("    public class " + stratName + " extends Player {");
			System.exit(1);
		} catch (InstantiationException | ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.print("Number of rounds: ");
		int numRounds = scan.nextInt();
		
		System.out.println("Running...");
		
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter("game_log.txt"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		final BufferedWriter fwriter = writer;
		
		Game g = new Game(p, new GameLogger() {
			public void logString(String message) {
				try {
					fwriter.append(message + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		for (int i=0; i<numRounds; i++) {
			g.doRound();
		}
		
		System.out.println("Over " + numRounds + " rounds, " + stratName + " turned $1000 into $" + g.getMoney());
		System.out.println("Check game_log.txt for more details!");
		
		try {
			fwriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
