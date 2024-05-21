package blackjack.game;

public class Shoe extends CardGroup {
	public Shoe(int numDecks) {
		for (int i=0; i<numDecks; i++) {
			generateDeck();
		}
		
		shuffle();
	}
	
	private void generateDeck() {
		for (Suit s : Suit.values()) {
			for (Rank r : Rank.values()) {
				// this should be the only place where we ever generate cards
				_add(new Card(this, r, s));
			}
		}
	}
}
