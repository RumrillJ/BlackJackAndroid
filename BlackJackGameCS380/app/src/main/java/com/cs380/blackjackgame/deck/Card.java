package com.cs380.blackjackgame.deck;

import androidx.annotation.NonNull;

import com.cs380.blackjackgame.R;

import java.util.Locale;

public class Card {
	public static final boolean defaultIsFaceDown = false;

	//CardSuit and CardNumber are both Enums containing the different possible values for each
	//CardNumber has a function getValue() that returns the value of the card (0 for ACE, 1 for TWO, etc)
	@NonNull
	private final CardSuit suit;
	@NonNull
	private final CardNumber number;

	private boolean isFaceDown;

	private int resID;

	//constructors
	public Card(@NonNull CardSuit suit, @NonNull CardNumber number) {//(suit, number)
		this(suit, number, defaultIsFaceDown);
		assignResID();
	}

	public Card(@NonNull CardSuit suit, @NonNull CardNumber number, boolean isFaceDown) {//(suit, number)
		this.suit = suit;
		this.number = number;
		this.isFaceDown = isFaceDown;
		assignResID();
	}

	//public methods
	//getters for the suit, returning the CardSuit and the name of the suit as String
	@NonNull
	public CardSuit getSuit() {
		return suit;
	}

	@NonNull
	public String getSuitName() {
		return suit.toString().toLowerCase(Locale.ROOT);
	}

	//getters for the number, returning the CardNumber, int value of the number, and name of the number as String
	@NonNull
	public CardNumber getCardNumber() {
		return number;
	}

	public int getNum() {
		return number.getValue();
	}

	@NonNull
	public String getNumName() {
		return number.toString().toLowerCase(Locale.ROOT);
	}

	//Getter/setter for isFaceDown boolean, used for rendering cards face up or down
	public boolean isFaceDown() {
		return isFaceDown;
	}

	public void setFaceDown(boolean isFaceDown) {
		this.isFaceDown = isFaceDown;
	}

	//getter for the resource ID, used for getting what card face should be associated with a card
	public int getResID(){return resID;}

	//private methods

	//Assigns the appropriate card resource ID based on the number and suit in a big nested switch statement
	private void assignResID(){
		switch(number){
			case ACE:
				switch(suit){
					case SPADES:
						resID = R.drawable.acespades;
						break;
					case HEARTS:
						resID = R.drawable.acehearts;
						break;
					case CLUBS:
						resID = R.drawable.aceclubs;
						break;
					case DIAMONDS:
						resID = R.drawable.acediamonds;
						break;
				}
				break;
			case TWO:
				switch(suit){
					case SPADES:
						resID = R.drawable.twospades;
						break;
					case HEARTS:
						resID = R.drawable.twohearts;
						break;
					case CLUBS:
						resID = R.drawable.twoclubs;
						break;
					case DIAMONDS:
						resID = R.drawable.twodiamonds;
						break;
				}
				break;
			case THREE:
				switch(suit){
					case SPADES:
						resID = R.drawable.threespades;
						break;
					case HEARTS:
						resID = R.drawable.threehearts;
						break;
					case CLUBS:
						resID = R.drawable.threeclubs;
						break;
					case DIAMONDS:
						resID = R.drawable.threediamonds;
						break;
				}
				break;
			case FOUR:
				switch(suit){
					case SPADES:
						resID = R.drawable.fourspades;
						break;
					case HEARTS:
						resID = R.drawable.fourhearts;
						break;
					case CLUBS:
						resID = R.drawable.fourclubs;
						break;
					case DIAMONDS:
						resID = R.drawable.fourdiamonds;
						break;
				}
				break;
			case FIVE:
				switch(suit){
					case SPADES:
						resID = R.drawable.fivespades;
						break;
					case HEARTS:
						resID = R.drawable.fivehearts;
						break;
					case CLUBS:
						resID = R.drawable.fiveclubs;
						break;
					case DIAMONDS:
						resID = R.drawable.fivediamonds;
						break;
				}
				break;
			case SIX:
				switch(suit){
					case SPADES:
						resID = R.drawable.sixspades;
						break;
					case HEARTS:
						resID = R.drawable.sixhearts;
						break;
					case CLUBS:
						resID = R.drawable.sixclubs;
						break;
					case DIAMONDS:
						resID = R.drawable.sixdiamonds;
						break;
				}
				break;
			case SEVEN:
				switch(suit){
					case SPADES:
						resID = R.drawable.sevenspades;
						break;
					case HEARTS:
						resID = R.drawable.sevenhearts;
						break;
					case CLUBS:
						resID = R.drawable.sevenclubs;
						break;
					case DIAMONDS:
						resID = R.drawable.sevendiamonds;
						break;
				}
				break;
			case EIGHT:
				switch(suit){
					case SPADES:
						resID = R.drawable.eightspades;
						break;
					case HEARTS:
						resID = R.drawable.eighthearts;
						break;
					case CLUBS:
						resID = R.drawable.eightclubs;
						break;
					case DIAMONDS:
						resID = R.drawable.eightdiamonds;
						break;
				}
				break;
			case NINE:
				switch(suit){
					case SPADES:
						resID = R.drawable.ninespades;
						break;
					case HEARTS:
						resID = R.drawable.ninehearts;
						break;
					case CLUBS:
						resID = R.drawable.nineclubs;
						break;
					case DIAMONDS:
						resID = R.drawable.ninediamonds;
						break;
				}
				break;
			case TEN:
				switch(suit){
					case SPADES:
						resID = R.drawable.tenspades;
						break;
					case HEARTS:
						resID = R.drawable.tenhearts;
						break;
					case CLUBS:
						resID = R.drawable.tenclubs;
						break;
					case DIAMONDS:
						resID = R.drawable.tendiamonds;
						break;
				}
				break;
			case JACK:
				switch(suit){
					case SPADES:
						resID = R.drawable.jackspades;
						break;
					case HEARTS:
						resID = R.drawable.jackhearts;
						break;
					case CLUBS:
						resID = R.drawable.jackclubs;
						break;
					case DIAMONDS:
						resID = R.drawable.jackdiamonds;
						break;
				}
				break;
			case QUEEN:
				switch(suit){
					case SPADES:
						resID = R.drawable.queenspades;
						break;
					case HEARTS:
						resID = R.drawable.queenhearts;
						break;
					case CLUBS:
						resID = R.drawable.queenclubs;
						break;
					case DIAMONDS:
						resID = R.drawable.queendiamonds;
						break;
				}
				break;
			case KING:
				switch(suit){
					case SPADES:
						resID = R.drawable.kingspades;
						break;
					case HEARTS:
						resID = R.drawable.kinghearts;
						break;
					case CLUBS:
						resID = R.drawable.kingclubs;
						break;
					case DIAMONDS:
						resID = R.drawable.kingdiamonds;
						break;
				}
				break;
		}
	}
}//end card class
