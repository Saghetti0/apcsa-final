package strategy;

/**
 * This is a "strategy" to help you understand how everything works. Instead
 * of being fully automatic, this strategy prints everything to the terminal,
 * and allows you to play the game directly. 
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import blackjack.game.Card;
import blackjack.game.HandResults;
import blackjack.game.Player;

public class ManualPlayer extends Player {
	private Scanner in = new Scanner(System.in);
	
	public int newRound() {
		// ask the user for an initial bet
		System.out.println("");
		System.out.print("Inital bet? (Current bal: $" + getMoney() + "): $");
		return in.nextInt();
	}
	
	public void playHand(int handNumber) {
		// print the current state of this hand
		
		System.out.println("--- Playing hand #" + handNumber + " ---");
		System.out.println("Hand: " + Arrays.toString(getCurrentHand()) + " (" + getCurrentHandValue() + ")");
		System.out.println("Dealer: " + getDealerCard() + " (" + getDealerHandValue() + ")");
		
		// build a list of possible actions the player can take right now
		
		ArrayList<String> actions = new ArrayList<String>();
		
		actions.add("hit");
		actions.add("stand");
		
		if (canDoubleDown()) {
			actions.add("double");
		}
		
		if (canSplit()) {
			actions.add("split");
		}
		
		// print the actions to the screen
		String actionsStr = "";
		
		for (String a : actions) {
			actionsStr += a + "/";
		}
		
		actionsStr = actionsStr.substring(0, actionsStr.length() - 1);
		
		System.out.print("Action? [" + actionsStr + "] ");
		
		// get a choice from the user
		
		String choice = in.next();
		
		// call an action function, depending on the user's input
		
		if (choice.equals("hit")) {
			Card drawn = hit();
			System.out.println("Hit! Drew " + drawn);
			return;
		}
		
		if (choice.equals("stand")) {
			stand();
			System.out.println("Stood!");
			return;
		}
		
		if (choice.equals("double")) {
			Card drawn = doubleDown();
			System.out.println("Doubled! Drew " + drawn);
			return;
		}
		
		if (choice.equals("split")) {
			int newHand = split();
			System.out.println("Split! New hand is #" + newHand);
			return;
		}
	}
	
	public void handResults(int handNumber, Card[] playerHand, Card[] dealerHand, HandResults results, int payout) {
		// print the results for a hand
		System.out.println("=== Results for hand #" + handNumber + " ===");
		System.out.println("Player: " + Arrays.toString(playerHand) + " (" + evaluate(playerHand) + ")");
		System.out.println("Dealer: " + Arrays.toString(dealerHand) + " (" + evaluate(dealerHand) + ")");
		System.out.println("Results: " + results);
		System.out.println("Payout: $" + payout);
	}
	
	public void onShuffle(boolean duringRound) {
		// print when the deck shuffles
		System.out.println("## The deck just shuffled! duringRound=" + duringRound);
	}

	public boolean buyInsurance() {
		// prompt the user to buy insurance
		System.out.print("Insure? [y/n] ");
		return in.next().equals("y");
	}
}
