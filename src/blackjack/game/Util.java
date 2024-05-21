package blackjack.game;

public class Util {
	public static String naturalCase(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
	}
	
	public static int evaluate(Card[] cards) {
		boolean hasAce = false;
		int currentValue = 0;

		for (Card card : cards) {
			if (card.getRank() == Rank.ACE) {
				hasAce = true;
			}

			currentValue += card.getValue();
		}

		if (hasAce && currentValue <= 11) {
			currentValue += 10;
		}

		return currentValue;
	}
}
