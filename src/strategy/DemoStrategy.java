package strategy;

import blackjack.game.*;

public class DemoStrategy extends Player {
	// confidence increases for each hand we win
	// and decreases for each hand we lose
	int confidence = 0;
	
	public int newRound() {
		// make our bet based on how confident we are
		
		if (confidence > 0) {
			return 20;
		} else {
			return 10;
		}
	}
	
	public boolean buyInsurance() {
		// buy insurance if this hand is worth 19 or 20
		
		if (getCurrentHandValue() == 19 || getCurrentHandValue() == 20) {
			return true;
		}
		
		return false;
	}
	
	public void playHand(int handNumber) {
		Card[] hand = getCurrentHand();
		
		// always split pairs of aces or eights
		
		// make sure to check if you are allowed to split
		if (canSplit()) {			
			if (hand[0].getRank() == Rank.ACE && hand[1].getRank() == Rank.ACE) {
				split();
				// remember: always return soon after doing an action!
				return;
			}
			
			if (hand[0].getRank() == Rank.EIGHT && hand[1].getRank() == Rank.EIGHT) {
				split();
				return;
			}
		}
		
		// if we're below 12, hit
		
		if (getCurrentHandValue() < 12) {
			hit();
			return;
		}
		
		// if we're 19 or above, stand
		
		if (getCurrentHandValue() >= 19) {
			stand();
			return;
		}
		
		// otherwise, randomly choose between hitting and standing
		
		if (Math.random() > 0.5) {
			hit();
			return;
		} else {
			// and an additional 20% chance that we double down
			if (Math.random() < 0.1) {
				doubleDown();
			} else {				
				stand();
			}
			return;
		}
	}
	
	public void handResults(int handNumber, Card[] playerHand, Card[] dealerHand, HandResults results, int payoff) {
		// we really like blackjacks. if we got a blackjack, our confidence goes up by 2
		
		if (results == HandResults.PLAYER_BLACKJACK) {
			confidence += 2;
			return;
		}
		
		// if the dealer got a blackjack, our confidence goes down by 3
		
		if (results == HandResults.DEALER_BLACKJACK) {
			confidence -= 3;
			return;
		}
		
		// if we won money on this hand, our confidence goes up
		
		if (payoff > getInitialBet()) {
			confidence++;
		}
		
		// if we lost money, our confidence goes down
		
		if (payoff < getInitialBet()) {
			confidence--;
		}
	}
}
