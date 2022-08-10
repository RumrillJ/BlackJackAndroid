package com.cs380.blackjackgame.gamelogic;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.cs380.blackjackgame.Play_Blackjack;
import com.cs380.blackjackgame.R;
import com.cs380.blackjackgame.cardflipanimation.CardFlipRunnable;
import com.cs380.blackjackgame.deck.Card;

import java.util.LinkedList;

public abstract class AbstractPlayer {
	@NonNull
	public final String name;
	@NonNull
	public final Game game;
	public LinkedList<Card> hand = new LinkedList<>();

	private boolean isPlaying;    //this keeps track of if the player is still in the game
	private boolean hasDoubled; //this is to keep track if the player has doubled this game already
	public long winnings;// this keeps track of the players' money
    public long betting; // keeps track of the player's bets
	private boolean surrendered; // keeps track of whether the player surrendered

	public AbstractPlayer(@NonNull String name, @NonNull Game game, long startingMoney) {
		this.name = name;
		this.game = game;
		this.winnings = startingMoney;
		startPlay();
	}

	public void doAction(PlayerAction playerAction) {
		this.game.doPlayerAction(this, playerAction);
	}

	//returns the total value of the player's current hand (favoring Ace high until that would cause a bust)
	public int getHandValue() {
		int handValue = 0;

		//iterates through each card and adds its value to the handValue
		for (Card card : hand) {
			int cardNum = card.getNum();
			if (cardNum >= 9) {
				handValue += 10;
			} else {
				handValue += (cardNum + 1);
			}
		}

		//if the player has an Ace, add 10 (1 was added in the loop) to the hand value
		//if this causes the player to bust, remove that 10
		if(hasAce()){
			handValue += 10;
			if(handValue > 21){
				handValue -= 10;
			}
		}

		//return the calculated value;
		return handValue;
	}

	//returns a boolean based on if the player has a hand above 21
	public boolean isBust() {return getHandValue() > 21;}

	//iterates through the cards and returns true if it finds a card with num 0 (Ace) and returns false if it doesn't.
	public boolean hasAce(){
		for(Card card : hand){
			if(card.getNum() == 0){return true;}
		}
		return false;
	}

	public boolean hasBlackjack() {
		return getHandValue() == 21 && hand.size() == 2;
	}

	//initializes a player's playing and doubling status for checking during a round.
	//placed this way so it can be called when starting a new round instead of building an entire new set of players.
	public void startPlay(){
		isPlaying = true;
		hasDoubled = false;
		surrendered = false;
	}

	//getter and negative setter for isPlaying, can be checked for if a player is still in the round during calculations/taking turns
	//endPlay should be used on Bust or Surrender.
	public void endPlay() {isPlaying = false;}
	public boolean isPlaying(){return isPlaying;}

	//getter and negative setter for hasDoubled. Since a player can only double once per hand, they shouldn't be able to double again.
	//this may be redundant since doubling requires the player to immediately hit once then stand after doing it.
	public void isDoubling(){hasDoubled = true;}
	public boolean hasDoubled(){return hasDoubled;}

	//boolean check for if the player is an instance of AIPlayer with difficulty dealer.
	//useful for rendering things in the correct locations and for some game logic.
	public boolean isDealer(){
		if(this instanceof AIPlayer){
			if(((AIPlayer) this).getDifficulty() == AIDiff.DEALER){
				return true;
			}
		}
		return false;
	}

	//setter and getter methods for surrendering
	public void setSurrender () {surrendered = true;}
	public boolean getSurrender () {return surrendered;}

    public void addWinnings(long money) {
        winnings += money;
    }

    public void addWinnings () {
		winnings += betting;
	}

	public void addWinningsBlackJack () {
		double temp = Math.round(betting*1.5);

		betting = (long) temp;
	}

    public void subLosses (long money) {
        winnings -= money;
    }

    public void subLosses() {
		winnings -= betting;
	}

    public void setBet (long money) {betting = money;}

    public long getBet () {return betting;}

    public long getWinnings (){return winnings;}

    public void doubleDown (){
        betting = betting*2;
        isDoubling();
    }

    public void surrendering () {
		//rounds up to the nearest cent
		double temp = Math.round(betting/2.0);

        betting = (long) temp;
        endPlay();
        setSurrender();
     }

	public void showCards(Context context, RelativeLayout output) {
		showCards(context, output, null, false);
	}

	public void showCardsAnimateAll(Context context, RelativeLayout output, boolean fromTop) {
		int[] animateArray = new int[hand.size()];

		for(int i = 0; i < hand.size(); i++) {
			animateArray[i] = i;
		}

		showCards(context, output, animateArray, fromTop);
	}

	//Displays the cards of the current player
	//Context and Layout are required for fetching and setting certain information in the UI of the app
	//Layout passed is different depending on if its a player or dealer.
	public void showCards(Context context, RelativeLayout output, @Nullable int[] cardIndexAnimateList, boolean fromTop) {
		//Clear all the current cards in the layout, I may look into just adding and recalculating in the future
		//That would make animation easier, but would require permanently storing all the current ImageViews so I can change their locations.
		output.removeAllViewsInLayout();


		//array for storing ImageViews for later rendering
		ImageView[] cards = new ImageView[hand.size()];

		int i = 0;// iterator for storing ImageViews in the cards array.

		//card loop, this loop places the ImageViews inside the array, gives them an ID, and sets the image associated with them.
		for(Card card : hand){

			//puts a new ImageView within the current context into the current spot in the array, then gives it an ID
			cards[i] = new ImageView(context);
			cards[i].setId(View.generateViewId());

			//if the card is face down, render it face down.
			//(currently an exception here for the player, since they need to see their first face down card)
			if(card.isFaceDown() && (isDealer() || i > 0)){
				//sets the ImageView's image to the card back if it's face down
				cards[i].setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.cardback, null));
				cards[i].setTag(R.drawable.cardback);
			}
			else{
				//sets the ImageView's image to its appropriate card face otherwise.
				cards[i].setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), card.getResID(), null));
				cards[i].setTag(card.getResID());
			}

			i++;
		}//end card for loop

		//This fetches the physical dimensions of the screen and saves the width in pixels.
		DisplayMetrics display = context.getResources().getDisplayMetrics();
		int wScreen = display.widthPixels;
		int hScreen = display.heightPixels;

		//this is the render loop, it actually places the cards in their correct places within the layout.
		for(int j = 0; j < cards.length; j++){

			//adjustable card width, currently 1/4 the width of the screen.
			//this should be set in relation to the screen's width so it remains dynamic.
			int cardWidth = wScreen/4;

			//this sets up the layout parameters of the ImageView
			//in this case it sets the width to the calculated width, and the height to be wrapped by the content
			//(this means it will scale based on the size it needs to be, but since the width is set should always be the same size)
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					cardWidth,
					RelativeLayout.LayoutParams.WRAP_CONTENT);


			//sets the initial amount that the cards are overlapping.
			//here I have it set to the cards overlapping each other by 1/3 their width, this can be adjusted later.
			int overlap =  cardWidth/3;

			//this is the total width of the hand with the given overlap
			//this is a fencepost problem, there are hand.size()-1 overlaps
			//(cardWidth-overlap) is the amount a card sticks out after overlapping
			//(hand.size()-1) is the number of cards that will be sticking out
			//(cardWidth) is the initial card that the others will be sticking out from
			int totalWidth = (cardWidth-overlap)*(hand.size()-1) + (cardWidth);

			//if the total width of all the cards would be wider than the screen, it recalculates the amount of overlap
			//this way all the cards will remain inside the bounds of the screen dynamically.
			//wScreen/10 is arbitrary, and is there to add a buffer on either side of the hand so it doesn't go right up to the screen edges
			//this buffer will be split on either side of the card, so there is a wScreen/20 gap on each side.
			if(totalWidth > (wScreen - wScreen/10)){
				//((cardWidth)*(hand.size()) is the absolute width of the hand if the cards were edge to edge
				//(wScreen - wScreen/10) is the screen width with the added buffer
				//the former subtracted from the latter will get the width of the area that extends past the size of the screen with buffer
				//this extra area is the amount of space that needs to be removed to make the cards be within the bounds
				//this area is divided by (hand.size()-1), or the number of overlaps, to get the size each overlap should be
				overlap = ((cardWidth)*(hand.size()) - (wScreen - wScreen/10))/(hand.size()-1);

				//recalculates the total width with the new overlap
				totalWidth = (cardWidth-overlap)*(hand.size()-1) + (cardWidth);
			}

			//gets the location the cards need to start at to make the whole hand centered
			//(wScreen/2) is the middle of the screen width
			//(totalWidth/2) is the middle of the total hand width
			//by subtracting the former from the latter, you get the location from the left of the screen
			//where the middle of the total hand will be at the middle of the screen
			int centerEverything = (wScreen/2) - (totalWidth/2);


			//this places each card so that they overlap correctly
			//(cardWidth-overlap) being the amount each card sticks out past the previous and j being the number of cards doing this.
			cards[j].setX(centerEverything + (cardWidth-overlap)*j);

			//sets the current card's layout parameters to be the appropriate size
			cards[j].setLayoutParams(lp);

			//adds the card at its calculated location in the passed layout
			output.addView(cards[j]);
		}//end render loop

		if(cardIndexAnimateList != null) {
			for (Integer animationIndex : cardIndexAnimateList) {
				if (animationIndex != null && animationIndex >= 0 && animationIndex < cards.length) {
					ImageView cardImageView = cards[animationIndex];
					cardImageView.bringToFront();
					// Taken from 03-01-2022 17:13:00 PST: https://stackoverflow.com/questions/2614393/defining-z-order-of-views-of-relativelayout-in-android#comment75254704_39572923
					cardImageView.setElevation(1000);

					float finalX = cardImageView.getX();
					float finalY = cardImageView.getY();

					// Taken from 02-25-2022 16:36:36 PST: https://stackoverflow.com/a/63370600
					if(fromTop) {
						cardImageView.setX(0 - 100);
						cardImageView.setY(0 - 100);
					} else {
						cardImageView.setX(0 - 100);
						cardImageView.setY(hScreen + 100);
					}

					cardImageView.animate().x(finalX).y(finalY).setDuration(300);
				}
			}
		}
	}//end showCards()

	public void reveal(@NonNull RelativeLayout layout){
		for(Card card : hand){
			card.setFaceDown(false);
		}

		if (game.getActivity() instanceof Play_Blackjack) {
			Play_Blackjack playBlackjack = (Play_Blackjack) game.getActivity();

			// Taken from 02-23-2022 17:46:24 PST: https://stackoverflow.com/a/7807107
			for (int i = 0; i < layout.getChildCount(); i++) {
				View child = layout.getChildAt(i);

				if (child instanceof ImageView) {
					ImageView imageView = (ImageView) child;

					if (imageView.getTag() != null && imageView.getTag() instanceof Integer) {
						if ((int) imageView.getTag() == R.drawable.cardback) {
							playBlackjack.addCardFlipRunnable(new CardFlipRunnable(imageView,
									ResourcesCompat.getDrawable(game.getActivity().getResources(), R.drawable.cardback,
											null),
									ResourcesCompat.getDrawable(game.getActivity().getResources(),
											hand.get(i).getResID(), null),
									500));
						}
					} else {
						Log.e(this.getClass().getSimpleName(), "Got invalid card image view which didn't have a tag: " + imageView.toString());
					}
				}
			}
		}
	}

	public void emptyHand(){
		if(!hand.isEmpty()){hand.clear();}
	}

}//end Class