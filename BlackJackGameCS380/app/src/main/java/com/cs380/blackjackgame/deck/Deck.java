package com.cs380.blackjackgame.deck;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.*;


public class Deck {
	private int size;  //number of cards in the deck
	private int decks; //the number of decks of cards to be shuffled into the deck.
	@NonNull
	private Deque<Card> cardDeck = new ArrayDeque<Card>();

	//constructors
	//default constructor
	public Deck() {
		decks = 2;
		size = decks * 52;
		refreshDeck();
	}

	//Constructor used when user wants to play with more than two decks
	public Deck(int decks) {
		if(decks < 1 || decks > 99) { //99 is arbitrary, this can be changed in the future,
			decks = 2;                //the bounds should be set in the game loop too to give an error message and let the player to rechoose.
		}

		this.decks = decks;
		size = decks * 52;
		refreshDeck();
	}


	//public methods
	//Getter for the current size of the deck, possibly used for rendering a deck later.
	public int getSize() {return size;}

	//Gets the top card from the deck
	//if there isn't enough cards in the deck to take one,
	// refresh the card pile and then reset the size of the deckpile
	@NonNull
	public Card getCard() {
		if (size == 0) {
			refreshDeck();
		}

		size--;
		return cardDeck.pop();
	}

	//shuffleCards shuffles the Deck instance's card pile
	public void shuffleCards(){
		Deque<Card> tempDeck = shuffleCards(cardDeck);
		cardDeck.removeAll(cardDeck);
		cardDeck.addAll(tempDeck);
	}

	//generic shuffle method for any Card Deque
	@NonNull
	public Deque<Card> shuffleCards(Deque<Card> paraDeque) {
		ArrayList<Card> tempList = new ArrayList<>();

		Deque<Card> tempDeck = new ArrayDeque<>();

		tempDeck.addAll(paraDeque);

		for(int i = 0; i < size; i++) {
			tempList.add(tempDeck.poll());
		}

		Collections.shuffle(tempList);

		for(int i = 0; i< tempList.size(); i++) {
			tempDeck.add(tempList.get(i));
		}

		return tempDeck;
	}

	//private methods
	//Puts Card objects in Deck stack until it's gone through the number of specified decks
	//for loop i is for the number of decks, for loop j is for the cards inside that deck
	//there are 13 cards in a suit, so j%13 will iterate through each card and j/13 will go through 0, 1, 2, 3 for each suit.
	private void buildDeck() {
		for (int i = 0; i < decks; i++) {
			for (int j = 0; j < 52; j++) {
				Card temp = new Card(CardSuit.values()[j / 13], CardNumber.values()[j % 13]); // suit = j/13 ; num = j%13

//				Log.i("buildDeck", temp.getNumName() + " of " + temp.getSuitName());

				cardDeck.push(temp);
			}
		}
	}

	//combination of shuffle and build deck
	private void refreshDeck() {
		buildDeck();
		size = decks * 52;
		shuffleCards();
	}

}//end deck class