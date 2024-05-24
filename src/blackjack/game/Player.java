package blackjack.game;

public abstract class Player {
	/**
	 * Called when a new round of BlackJack begins.
	 * @return The player's initial bet.
	 */
	public abstract int newRound();

	/**
	 * Called when it's the player's turn to do an action for a hand.
	 * In this function, the player should call {@link hit},
	 * {@link stand}, {@link doubleDown}, or {@link split}, depending on the
	 * current state of the game. Only one of these methods can be called,
	 * after which the function must return.
	 * @param handNumber The hand number that is currently being played.
	 */
	public abstract void playHand(int handNumber);
	
	/**
	 * Called when the round ends for each hand in play.
	 * @param handNumber The hand number that these results are for.
	 * @param playerHand The player's hand.
	 * @param dealerHand The dealer's hand. The first card always is the
	 * face-up card, and the second card is the face-down card.
	 * @param handResults How the hand ended.
	 * @param payout How much money was given to the player as a result of this
	 * hand.
	 */
	public void handResults(int handNumber, Card[] playerHand, Card[] dealerHand, HandResults results, int payout) {
		
	}
	
	/**
	 * Called when the cards are reshuffled.
	 * @param duringRound If the shuffle happens in the middle of a round. A
	 * mid-round shuffle will always be followed by a full shuffle when the 
	 * round ends, which includes all cards.
	 */
	public void onShuffle(boolean duringRound) {
		
	}

	/**
	 * Called if the player can buy insurance. Returning true will buy
	 * insurance for the cost of the initial bet. This will cause the dealer to
	 * check for a blackjack. If they have a blackjack, the player is paid 2 to
	 * 1, and the round ends. If the dealer does not have a blackjack, the
	 * insurance is lost, and the round continues as normal.
	 */
	public boolean buyInsurance() {
		return false;
	}
	
	// TODO: this is tricky due to the dealer's hidden card, and potentially
	// leaks info... figure out?
	/**
	 * Called when a card is drawn by the player or a dealer.
	 * @param card The card that was drawn.
	 * @param handNumber The hand that this card was drawn for. This value is
	 * -1 if it's for the dealer.
	 */
	/*public void onCardDraw(Card card, int handNumber) {
		
	}*/
	
	/**
	 * Draw another card.
	 * @return The card that was drawn.
	 */
	public final Card hit() {
		return parentGame.actionTarget(PlayerAction.HIT);
	}
	
	/**
	 * Stop taking cards.
	 */
	public final void stand() {
		parentGame.actionTarget(PlayerAction.STAND);
	}
	
	/**
	 * Double your initial bet, draw one more card, and then stand.
	 * @return The card that was drawn.
	 */
	public final Card doubleDown() {
		return parentGame.actionTarget(PlayerAction.DOUBLE_DOWN);
	}

	/**
	 * Determine if the current hand can be split.
	 */
	public final boolean canSplit() {
		return parentGame.implCanSplit();
	}
	
	/**
	 * Determine if the player has enough money to double down.
	 */
	public final boolean canDoubleDown() {
		return parentGame.implCanDoubleDown();
	}
	
	/**
	 * Split this hand into a second hand. The second card is drawn 
	 * @return The ID of the newly created hand.
	 */
	public final int split() {
		return parentGame.implSplit();
	}
	
	/**
	 * Get the player's current hand.
	 */
	public final Card[] getCurrentHand() {
		return parentGame.implGetCurrentHand();
	}

	/**
	 * Get the dealer's current face-up card.
	 */
	public final Card getDealerCard() {
		return parentGame.implGetDealerCard();
	}
	
	/**
	 * Get the value of the player's current hand.
	 */
	public final int getCurrentHandValue() {
		return parentGame.implGetCurrentHandValue();
	}
	
	/**
	 * Get the value of the dealer's current hand.
	 */
	public final int getDealerHandValue() {
		return parentGame.implGetDealerHandValue();
	}

	/**
	 * Get the number of cards in the shoe.
	 */
	public final int countShoeCards() {
		return parentGame.implCountShoeCards();
	}

	/**
	 * Get the number of hands currently on the table.
	 * This will be 1 unless a split has occurred.
	 */
	public final int getNumHands() {
		return parentGame.implGetNumHands();
	}

	/**
	 * Get the amount of money that the player currently has.
	 */
	public final int getMoney() {
		return parentGame.implGetMoney();
	}
	
	/**
	 * Get the current round number. This number starts at 0, and for each
	 * round that passes, increases by 1.
	 */
	public final int getRoundNumber() {
		return parentGame.implGetRoundNumber();
	}
	
	/**
	 * Get the number of decks used in game.
	 */
	public final int getNumDecks() {
		return parentGame.implGetNumDecks();
	}
	
	/**
	 * Get the initial bet.
	 */
	public final int getInitialBet() {
		return parentGame.implGetInitialBet();
	}
	
	/**
	 * Evaluate the score of an arbitrary array of cards.
	 */
	public int evaluate(Card[] cards) {
		return Util.evaluate(cards);
	}
	
	/**
	 * Print a debug message to the log. These messages will be visible in the
	 * logs for a game.
	 */
	public void print(String message) {
		parentGame.implPrint(message);
	}
	
	// internals! don't touch!
	
	private Game parentGame = null;
	
	protected void setGame(Game game) {
		if (parentGame == null) {
			parentGame = game;
		} else {
			throw new RuntimeException("tried to setGame but game != null");
		}
	}
}
