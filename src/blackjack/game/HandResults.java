package blackjack.game;

public enum HandResults {
	DRAW, // draw
	PLAYER_HIGHER, // player wins
	DEALER_HIGHER, // dealer wins
	PLAYER_BUST, // dealer wins
	DEALER_BUST, // player wins
	PLAYER_BLACKJACK, // player wins
	DEALER_BLACKJACK, // dealer wins
	PLAYER_INSURANCE, // player loses, but 0 money is lost
}
