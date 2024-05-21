package strategy;

import java.util.ArrayList;

import blackjack.game.Card;
import blackjack.game.HandResults;
import blackjack.game.Player;
import blackjack.game.Rank;

public class CardTracker extends Player {
	private boolean hasSplitAces = false;
	private boolean hasSeenDealerFirst = false;
	private boolean hasSeenDealerFull = false;
	// all the cards currently on the table
	private ArrayList<Card> cardsOnTable = new ArrayList<Card>();
	// all the cards in the discard pile
	private ArrayList<Card> cardsInDiscard = new ArrayList<Card>();
	// if you want to keep track of the deck, and all the cards that haven't
	// been played yet, you'll need to code that yourself ;)

	public int newRound() {
		hasSplitAces = false;
		hasSeenDealerFirst = false;
		hasSeenDealerFull = false;
		
		// move all cards from the table into the discard pile
		while (cardsOnTable.size() > 0) {
			cardsInDiscard.add(cardsOnTable.remove(0));
		}
		
		return 10;
	}

	public void playHand(int handNumber) {
		// track the dealer's face up card
		
		if (hasSeenDealerFirst == false) {
			hasSeenDealerFirst = true;
			cardsOnTable.add(getDealerCard());
		}
		
		Card[] hand = getCurrentHand();
		
		// split aces
		if (hand[0].getRank() == Rank.ACE && hand[1].getRank() == Rank.ACE) {
			split();
			hasSplitAces = true;
			return;
		}
	}

	/*
	public void handResults(int handNumber, Card[] playerHand, Card[] dealerHand, HandResults results, int balanceChange) {
		
	}

	private void trackCard() {
		
	}
	*/
}