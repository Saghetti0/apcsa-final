package blackjack.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CardGroup implements Iterable<Card> {
	private List<Card> items = new ArrayList<Card>();

	public int evaluate() {
		return Util.evaluate(toArray());
	}

	public void shuffle() {
		Collections.shuffle(items);
	}

	public int size() {
		return items.size();
	}

	public Card moveCardTo(int index, CardGroup to) {
		Card c = items.remove(index);
		to._add(c);
		return c;
	}

	public Card drawCardTo(CardGroup to) {
		return moveCardTo(items.size() - 1, to);
	}

	public Card[] toArray() {
		Card[] arr = new Card[items.size()];
		
		items.toArray(arr);
		return arr;
	}

	public Card get(int i) {
		return items.get(i);
	}

	@Override
	public Iterator<Card> iterator() {
		return items.iterator();
	}

	public void moveAllTo(CardGroup to) {
		while (size() > 0) {
			moveCardTo(0, to);
		}
	}

	/**
	 * INTERNAL! DO NOT CALL THIS METHOD Instead, use {@link moveCard}
	 */
	protected void _add(Card card) {
		card.reparent(this);
		items.add(card);
	}
}
