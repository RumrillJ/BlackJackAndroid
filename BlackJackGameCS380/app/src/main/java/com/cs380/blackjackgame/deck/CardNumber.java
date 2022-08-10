package com.cs380.blackjackgame.deck;

public enum CardNumber {
	ACE(0), TWO(1), THREE(2), FOUR(3), FIVE(4), SIX(5), SEVEN(6), EIGHT(7), NINE(8), TEN(9), JACK(10), QUEEN(11), KING(12);

	private final int value;

	CardNumber(int value) {
		this.value = value;
	}

	public int getValue(){
		return value;
	}
}