package blackjack.game;

public class PlayerException extends RuntimeException {
	private static final long serialVersionUID = 1L;

    public PlayerException(){
        super();
    }

    public PlayerException(String message){
        super(message);
    }
}
