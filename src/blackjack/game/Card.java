package blackjack.game;

public class Card {
	private final Rank rank;
	private final Suit suit;
	private final CardGroup owner;
	
	public Card(CardGroup owner, Rank rank, Suit suit) {
		this.rank = rank;
		this.suit = suit;
		this.owner = owner;
	}

	/**
	 * Get the rank of this card.
	 */
	public Rank getRank() {
		return rank;
	}
	
	/**
	 * Get the suit of this card.
	 */
	public Suit getSuit() {
		return suit;
	}
	
	@Override
	public String toString() {
		return Util.naturalCase(rank.toString()) + " of " + Util.naturalCase(suit.toString());
	}
	
	/**
	 * INTERNAL! DO NOT CALL THIS METHOD
	 */
	protected void reparent(CardGroup owner) {
		
	}
	
	/**
	 * Get the value for this card. This function treats aces as a 1.
	 */
	public int getValue() {
		switch (this.getRank()) {
		case ACE:
			return 1;
		case TWO:
			return 2;
		case THREE:
			return 3;
		case FOUR:
			return 4;
		case FIVE:
			return 5;
		case SIX:
			return 6;
		case SEVEN:
			return 7;
		case EIGHT:
			return 8;
		case NINE:
			return 9;
		case TEN:
			return 10;
		case JACK:
			return 10;
		case QUEEN:
			return 10;
		case KING:
			return 10;
		}
		// ?!?!
		return 0;
	}
}
