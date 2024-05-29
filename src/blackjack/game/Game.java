package blackjack.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Game {
	private int roundNumber = 0;
	private Player player = null;
	private int numDecks = 1;
	private CardGroup shoe = new Shoe(numDecks);
	private CardGroup discard = new CardGroup();
	private CardGroup dealerHand = new CardGroup();
	private List<CardGroup> hands = new ArrayList<CardGroup>();
	// should've really used an object here, this is really dumb
	private boolean[] handDoubleStatus = new boolean[8];
	private boolean isShuffleQueued = false;
	private int playerInitialBet = 0;
	private int playerBalance = 1000;
	private int currentHandIndex = 0;
	private boolean canPlayerCallActions = false;
	private PlayerAction playerActionChoice = null;
	private boolean hasRoundEnded = false;
	private boolean didSplitAces = false;
	private GameLogger logger;
	private boolean clampBets = false;
	
	public Game(Player player, GameLogger logger) {
		this.player = player;
		this.logger = logger;
		player.setGame(this);
	}
	
	private Card drawCardTo(CardGroup to) {
		if (shoe.size() == 0) {
			discard.moveAllTo(shoe);
			shoe.shuffle();
			isShuffleQueued = true;
			logger.logString(">> The deck was shuffled");
			player.onShuffle(true);
		}
		
		return shoe.drawCardTo(to);
	}
	
	private Card[] drawCardTo(CardGroup to, int count) {
		Card[] cards = new Card[count];
		
		for (int i=0; i<count; i++) {
			cards[i] = drawCardTo(to);
		}
		
		return cards;
	}
	
	private boolean hasBlackjack(CardGroup cards) {
		return (cards.size() == 2) && (cards.evaluate() == 21);
	}
	
	private void endHand(int handIndex, HandResults result) {
		int base = playerInitialBet * (handDoubleStatus[handIndex] ? 2 : 1);
		int payout = 0;
		
		switch (result) {
		case PLAYER_BUST:
		case DEALER_HIGHER:
		case DEALER_BLACKJACK:
			// initial wager is lost
			payout = 0;
			break;
		case DRAW:
			// initial wager is paid back
			payout = base;
			break;
		case DEALER_BUST:
		case PLAYER_HIGHER:
			// initial wager is doubled
			payout = 2 * base;
			break;
		case PLAYER_INSURANCE:
			// insurance is untouched by double down
			payout = 2 * playerInitialBet;
			break;
		case PLAYER_BLACKJACK:
			// blackjacks play 3:2 (+ initial bet to recoup the cost)
			// this uses playerInitialBet because this happens before double down
			payout = playerInitialBet + (playerInitialBet + (playerInitialBet / 2));
			break;
		}
		
		playerBalance += payout;
		
		logger.logString("== Results for hand #" + (handIndex + 1) + " ==");
		
		CardGroup p = hands.get(handIndex);
		CardGroup d = dealerHand;
		
		logger.logString("## Player: " + Arrays.toString(p.toArray()) + " (" + p.evaluate() + ")");
		logger.logString("## Dealer: " + Arrays.toString(d.toArray()) + " (" + d.evaluate() + ")");
		switch (result) {
		case DEALER_BLACKJACK:
			logger.logString(">> The dealer wins by getting a blackjack");
			break;
		case DEALER_BUST:
			logger.logString(">> The player wins because the dealer bust");
			break;
		case DEALER_HIGHER:
			logger.logString(">> The dealer wins by having a higher value");
			break;
		case DRAW:
			logger.logString(">> It's a draw");
			break;
		case PLAYER_BLACKJACK:
			logger.logString(">> The player wins by getting a blackjack");
			break;
		case PLAYER_BUST:
			logger.logString(">> The dealer wins because the player bust");
			break;
		case PLAYER_HIGHER:
			logger.logString(">> The player wins by having a higher value");
			break;
		case PLAYER_INSURANCE:
			logger.logString(">> The player wins by buying insurance against blackjack");
			break;
		}
		
		player.handResults(handIndex, hands.get(handIndex).toArray(), dealerHand.toArray(), result, payout);
	}

	public void doRound() {
		initializeRound();
		runRound();
		cleanupRound();
	}
	
	private void initializeRound() {
		logger.logString("");
		logger.logString("=== START OF ROUND #" + (roundNumber + 1) + " ===");
		logger.logString("## Player's balance: $" + playerBalance);
		
		for (int i=0; i<handDoubleStatus.length; i++) {
			handDoubleStatus[i] = false;
		}
		
		// ask the player for their bet
		playerInitialBet = player.newRound();
		
		// bounds check
		if (playerInitialBet % 2 != 0) {
			throw new PlayerException("initial bet must be an even number, got $" + playerInitialBet);
		}
		
		if (playerInitialBet < 2) {
			throw new PlayerException("initial bet must be at least $2, got $" + playerInitialBet);
		}
		
		if (this.clampBets) {
			if (playerInitialBet > playerBalance) {
				playerInitialBet = (playerBalance / 2) * 2;
			}
		} else {			
			if (playerInitialBet > playerBalance) {
				throw new PlayerException("player tried to bet $" + playerInitialBet
						+ " but only has $" + playerBalance);
			}
		}
		
		
		logger.logString(">> The player bet $" + playerInitialBet);
		
		playerBalance -= playerInitialBet;
		
		// set up the dealer and player's hands

		drawCardTo(dealerHand, 2);

		CardGroup firstHand = new CardGroup();
		hands.add(firstHand);
		
		drawCardTo(firstHand, 2);
		
		hasRoundEnded = false;
		currentHandIndex = 0;
		
		logger.logString(">> Cards have been dealt");
	}
	
	private void runRound() {
		// check for player blackjack
		CardGroup firstHand = hands.get(0);
		
		if (hasBlackjack(firstHand)) {
			if (!hasBlackjack(dealerHand)) {
				// blackjack on player, not on dealer
				endHand(0, HandResults.PLAYER_BLACKJACK);
				return;
			} else {
				// blackjack on both player and dealer
				endHand(0, HandResults.DRAW);
				return;
			}
		}
		
		// check for insurance
		Card dealerCard = dealerHand.get(0);
		
		if (dealerCard.getRank() == Rank.ACE && playerBalance >= (playerInitialBet / 2)) {
			// the player gets a chance to buy insurance if the top card is an ace
			// and they have the money to pay for it
			
			if (player.buyInsurance()) {
				// cost is 50% of the initial bet
				playerBalance -= (playerInitialBet / 2);
				
				// check if the dealer has blackjack
				if (hasBlackjack(dealerHand)) {
					logger.logString(">> The player chose to buy insurance, and wins");
					// game ends early due to insurance bet
					endHand(0, HandResults.PLAYER_INSURANCE);
					return;
				} else {
					logger.logString(">> The player chose not to buy insurance");
				}
			}
			// otherwise, the game continues as normal
		}
		
		/*
		 * i'll let you in on a little secret
		 * in order to make the api nicer for the users
		 * the code that runs it has become much more messy and convoluted
		 * in a perfect world, the player returns the action they want, which
		 * the main code acts on. sunshine and rainbows, everything is perfect!
		 * but method calls are more intuitive, and in order to make card counters
		 * happy, they need to be able to see every card that's played.
		 * we fix this by returning the Card that the player has drawn on an action
		 * but that's not super possible if playHand() has already stopped executing
		 * so instead, in functions that have to return cards, execution goes from
		 * Player.hit() or whatev to Game.actionTarget(), which adds a new card
		 * to the current hand, and then returns it back to Player, which can then
		 * give it back to the playHand() code. the expectation is that playHand()
		 * will exit shortly after this whole interaction, where we then handle 
		 * the rest of the action (except for the card draw!!)
		 */
		
		// here comes the core game loop
		// we repeatedly call playHand() for each hand in play
		
		// in order to make the madness work, we need to track the current hand
		currentHandIndex = 0;
		
		while (currentHandIndex < hands.size()) {
			logger.logString("## Hand number: " + (currentHandIndex + 1));
			logger.logString("## Player's hand: " + Arrays.toString(implGetCurrentHand()) + " (" + implGetCurrentHandValue() + ")");
			logger.logString("## Dealer's hand: " + implGetDealerCard() + " (" + implGetDealerHandValue() + ")");
			
			// logging
			
			if (implGetCurrentHandValue() > 21) {
				logger.logString(">> This hand busted!");
			}
			
			if (implGetCurrentHandValue() == 21) {
				logger.logString(">> This hand is exactly 21!");
			}
			
			// reaching exactly 21 or over will advance
			if (implGetCurrentHandValue() >= 21) {
				currentHandIndex++;
				continue;
			}
			
			PlayerAction action = invokePlayHand();
			
			// yet more logging
			
			if (action == PlayerAction.STAND) {
				logger.logString(">> The player stood");
			}
			
			if (action == PlayerAction.SPLIT) {
				logger.logString(">> The player split their hand");
			}
			
			Card[] hand = implGetCurrentHand();

			if (action == PlayerAction.HIT) {
				logger.logString(">> The player hit, and drew a " + hand[hand.length - 1]);
			}
			
			if (action == PlayerAction.DOUBLE_DOWN) {
				logger.logString(">> The player doubled down, and drew a " + hand[hand.length - 1]);
			}
			
			// standing, or splitting aces will advance to the next hand
			if (action == PlayerAction.STAND || 
					(action == PlayerAction.SPLIT && didSplitAces)) {
				currentHandIndex++;
				continue;
			}
			
			// doubling down counts as a stand
			if (action == PlayerAction.DOUBLE_DOWN) {
				// subtract money to pay for the double
				playerBalance -= playerInitialBet;
				
				// mark this hand as doubled
				handDoubleStatus[currentHandIndex] = true;
				
				currentHandIndex++;
				continue;
			}
		}
		
		hasRoundEnded = true;
		
		// once we're done with the player's hand(s), it's time for the dealer
		// by the time we get here, we guarantee the player doesn't have bj
		// but we haven't checked for the dealer, aside from draw and insurance
		
		if (hasBlackjack(dealerHand)) {
			// a dealer blackjack sweeps all hands at this point
			// because we already checked the orig hand for blackjack
			// and split hands with blackjack only count as 21
			
			for (int i=0; i<hands.size(); i++) {
				endHand(i, HandResults.DEALER_BLACKJACK);
			}
			
			return;
		}
		
		// check for busted hands
		int nonBustedHands = hands.size();
		
		for (int i=0; i<hands.size(); i++) {
			CardGroup hand = hands.get(i);
			
			if (hand.evaluate() > 21) {
				endHand(i, HandResults.PLAYER_BUST);
				nonBustedHands--;
			}
		}
		
		// if we have any non-busted hands, the dealer needs to draw cards
		
		if (nonBustedHands > 0) {
			while (dealerHand.evaluate() < 17) {
				drawCardTo(dealerHand);
			}
			
			for (int i=0; i<hands.size(); i++) {
				CardGroup hand = hands.get(i);
				int handScore = hand.evaluate();
				int dealerScore = dealerHand.evaluate();
				
				if (handScore > 21) {
					// we already handled busted hands, ignore them here
					continue;
				}
				
				if (dealerScore > 21) {
					// dealer bust!
					endHand(i, HandResults.DEALER_BUST);
					continue;
				}
				
				if (dealerScore > handScore) {
					endHand(i, HandResults.DEALER_HIGHER);
					continue;
				}
				
				if (handScore > dealerScore) {
					endHand(i, HandResults.PLAYER_HIGHER);
					continue;
				}
				
				if (dealerScore == handScore) {
					endHand(i, HandResults.DRAW);
					continue;
				}
			}
		}
	}
	
	private void cleanupRound() {
		// clean up all the cards on the table
		dealerHand.moveAllTo(discard);
		
		while (hands.size() > 0) {
			hands.get(0).moveAllTo(discard);
			hands.remove(0);
		}
		
		// if we have a shuffle queued, do it!
		if (isShuffleQueued) {
			isShuffleQueued = false;
			discard.moveAllTo(shoe);
			shoe.shuffle();
			logger.logString(">> The deck was shuffled");
			player.onShuffle(false);
		}
		
		hasRoundEnded = false;
		roundNumber++;
	}
	
	/**
	 * helper function to invoke playhand and deal with (most!) of the bs that
	 * comes with it
	 */
	private PlayerAction invokePlayHand() {
		canPlayerCallActions = true;
		playerActionChoice = null;
		
		player.playHand(currentHandIndex);
		
		canPlayerCallActions = false;
		
		if (playerActionChoice == null) {
			throw new PlayerException("an action must be performed");
		}
		
		return playerActionChoice;
	}
	
	/**
	 * this is where execution lands when an action method is called in Player
	 */
	protected Card actionTarget(PlayerAction action) {
		if (!canPlayerCallActions) {
			throw new PlayerException("cannot perform an action at this time");
		}

		// set to false so the player can't do anything else
		canPlayerCallActions = false;
		// and store the action
		playerActionChoice = action;
		
		if (action == PlayerAction.DOUBLE_DOWN) {
			if (!implCanDoubleDown()) {
				throw new PlayerException("cannot double down");
			}
		}
		
		// next, figure out if we need to draw a card
		
		// hitting and doubling down involve drawing a card
		// split draws cards, but the split() method doesn't+can't return it
		// so the logic for drawing when splitting is handled elsewhere
		if (action == PlayerAction.HIT || action == PlayerAction.DOUBLE_DOWN) {
			// get the current hand
			CardGroup currentHand = hands.get(currentHandIndex);
			
			// draw a card
			Card drawnCard = drawCardTo(currentHand);
			
			// and return it
			return drawnCard;
		}
		
		// splitting is handled by its own custom path
		// returning null here only happens on a stand
		
		return null;
	}
	
	protected Card[] implGetCurrentHand() {
		return hands.get(currentHandIndex).toArray();
	}
	
	protected int implSplit() {
		if (!canPlayerCallActions) {
			throw new PlayerException("cannot perform an action at this time");
		}

		// set to false so the player can't do anything else
		canPlayerCallActions = false;
		// and store the action
		playerActionChoice = PlayerAction.SPLIT;
		
		// do the actual split here as well
		
		if (!implCanSplit()) {
			throw new PlayerException("cannot split this hand");
		}
		
		// remove from the player's balance for this hand
		playerBalance -= playerInitialBet;
		
		// splitting aces counts as a stand
		if (implGetCurrentHand()[0].getRank() == Rank.ACE) {
			didSplitAces = true;
		} else {
			didSplitAces = false;
		}
		
		int newIndex = hands.size();
		CardGroup newHand = new CardGroup();
		CardGroup currentHand = hands.get(currentHandIndex);
		
		hands.add(newHand);
		// second card from the current hand becomes first card for the new one
		currentHand.moveCardTo(1, newHand);
		
		// draw an additional card to each
		drawCardTo(currentHand);
		drawCardTo(newHand);

		return newIndex;
	}
	
	protected boolean implCanSplit() {
		if (playerBalance < playerInitialBet) {
			return false;
		}
		
		if (hands.size() >= 4) {
			return false;
		}
		
		Card[] currentHand = implGetCurrentHand();
		
		if (currentHand.length != 2) {
			return false;
		}
		
		if (currentHand[0].getValue() != currentHand[1].getValue()) {
			return false;
		}
		
		return true;
	}
	
	protected boolean implCanDoubleDown() {
		return playerBalance >= playerInitialBet;
	}
	
	protected Card implGetDealerCard() {
		return dealerHand.get(0);
	}
	
	protected int implGetCurrentHandValue() {
		CardGroup currentHand = hands.get(currentHandIndex);
		return currentHand.evaluate();
	}
	
	protected int implGetDealerHandValue() {
		if (hasRoundEnded) {
			return dealerHand.evaluate();
		} else {
			return dealerHand.get(0).getValue();			
		}
	}
	
	protected int implCountShoeCards() {
		return shoe.size();
	}
	
	protected int implGetNumHands() {
		return hands.size();
	}
	
	protected int implGetMoney() {
		return playerBalance;
	}
	
	protected int implGetRoundNumber() {
		return roundNumber;
	}
	
	protected int implGetNumDecks() {
		return numDecks;
	}
	
	protected int implGetInitialBet() {
		return playerInitialBet;
	}
	
	protected void implPrint(String message) {
		logger.logString("DEBUG: " + message);
	}
	
	public int getMoney() {
		return playerBalance;
	}
	
	public void setClampBets(boolean clampBets) {
		this.clampBets = clampBets;
	}
}
