package strategy;

/**
 * A very simple strategy that bets $50 every round, and then stands whenever
 * the hand value is at or above 17. That's it.
 */

import blackjack.game.Player;

public class StandOn17 extends Player {
	public int newRound() {
		// always bet $50
		return 50;
	}
	
	public void playHand(int handNumber) {
		if (getCurrentHandValue() >= 17) {
			stand();
		} else {
			hit();
		}
	}
}
