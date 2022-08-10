package com.cs380.blackjackgame.gamelogic;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cs380.blackjackgame.R;
import com.cs380.blackjackgame.deck.Card;
import com.cs380.blackjackgame.deck.CardNumber;
import com.cs380.blackjackgame.deck.CardSuit;
import com.cs380.blackjackgame.deck.Deck;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public class Game {
	public static final int defaultDeckAmount = 2;
	public static final boolean defaultRevealOnShow = false;

	public static final int[] allAnimationArray = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

	public static final String classLogName = Game.class.getSimpleName() + ".java";

	@NonNull
	private final Context context;
	@NonNull
	private final RelativeLayout playerLayout;
	@NonNull
	private final RelativeLayout dealerLayout;

	// Dealer player holds holds the dealer player AI. This is initialzed in the
	// constructor of Game.
	@NonNull
	public AIPlayer dealerPlayer;

	// Deck holds the current Deck object for this Game object and is initialized
	// and shuffled in the constructor of Game.
	@NonNull
	public Deck deck;

	// Player holds all of the players (not including the dealer but including all
	// players who have busted as well) currently in this Game.
	@NonNull
	public Set<AbstractPlayer> players = new LinkedHashSet<>();

	// Game.playersLeftQueue holds the players we still need to process. This can be
	// thought of as the order of players who still havn't gone yet. The player at
	// the first index of this queue is the next player after Game.currentPlayer.
	@Nullable
	private Queue<AbstractPlayer> playersLeftQueue;

	// Game.currentPlayer holds the current player which is going. Since we need to
	// wait for button presses for a HumanPlayer, we check which player called the
	// Game.doPlayerAction() method. We then check if that player was the current
	// player and process their input.
	@Nullable
	public AbstractPlayer currentPlayer;

	// isDone tracks whether the game has finished or not.
	private boolean isDone = false;

	public SharedPreferences settings;
	public SharedPreferences.Editor edit;

	public Game(@NonNull Context context, @NonNull RelativeLayout playerLayout, @NonNull RelativeLayout dealerLayout) {
		this(context, playerLayout, dealerLayout, defaultDeckAmount);
	}

	public Game(@NonNull Context context, @NonNull RelativeLayout playerLayout, @NonNull RelativeLayout dealerLayout,
				int decks) {
		// Initialze and shuffle the deck object with the amount of decks being equal to
		// the parameter passed to this constructor.


		this.deck = new Deck(decks);

		this.context = context;
		this.playerLayout = playerLayout;
		this.dealerLayout = dealerLayout;

		// Initialize the dealerPlayer field to a new AIPlayer with the Dealer
		// difficulty / AI.
		dealerPlayer = new AIPlayer("Dealer", this, AIDiff.DEALER);
	}

	// Game.doRound() is called once when the game is started. From the player's in
	// the Game.players set, we construct a queue which holds the players we
	// still need to process.
	public void doRound() {
		showCardsAnimateAll(dealerPlayer);

		// Add all players in the game to the Game.playersLeftQueue. Since we are
		// processing the dealer after the game has "finished" (after all player
		// player's have made their move), we do not need to add them to this queue.
		playersLeftQueue = new LinkedList<>(players);
		playersLeftQueue.add(dealerPlayer);

		// Ensure that the Game.playersLeftQueue contains player's in it. If it does,
		// set the current player to the first player in the queue, prompt them for
		// their actions if they are a HumanPlayer, which will allow them to press
		// buttons corresponding to the actions that HumanPlayer can do.

		// If the player is a AIPlayer, all we need to do is call the
		// AIPlayer.takeTurn() method, passing which card the dealer is currently
		// showing so their AI can calculate a move to make.
		if(!isDone) { //(this was placed here as protection)
			if (!playersLeftQueue.isEmpty()) {
				nextPlayer();
			} else {
				// If we have gotten to this point, there were no players in the
				// Game.playersLeftQueue, which means there is no players left to process. Log
				// that this error occurred.
				Log.e(classLogName, "Players left queue was empty on initialization. Players: ");

				for (AbstractPlayer abstractPlayer : players) {
					Log.e(classLogName, abstractPlayer.name);
				}
			}
		}
	}

	public void doPlayerAction(@NonNull AbstractPlayer abstractPlayer, @NonNull PlayerAction playerAction) {
		// Validate that we have a current player and that the AbstractPlayer which
		// called this method was the current player.
		if (currentPlayer != null && currentPlayer.equals(abstractPlayer)) {
			// Stood should be set to true if the player stood when they were allowed to
			// stand.
			boolean stood = false;

			if (isActionValid(abstractPlayer, playerAction)) {
				Log.i(classLogName, "Current Player: " + currentPlayer + " Action: " + playerAction);

				// At this point, whatever playerAction was passed to this function should be
				// valid, so we can process it without further validity checks.

				switch(playerAction){
					case HIT:
						// Add a card to the players hand.
						currentPlayer.hand.add(deck.getCard());

						//show players cards in the correct location
						showCardsAnimateLast(currentPlayer);

						//if the player hit into 21, immediately stand as player protection
						if(currentPlayer.getHandValue() == 21 && currentPlayer instanceof HumanPlayer){
							stood = true;
						}

						if (currentPlayer.isBust()) {
							currentPlayer.endPlay();
						}
						break;

					case STAND:
						// Set the stood boolean to true and do nothing else.
						stood = true;
						break;

					case DOUBLE_DOWN:
						abstractPlayer.doubleDown();
						//as a note: double down doubles the player's bet and adds exactly one more card to the player's hand
						// Add a card to the players hand.
						Card temp = deck.getCard();
						temp.setFaceDown(true);
						currentPlayer.hand.add(temp);

						//show players cards in the correct location
						showCardsAnimateLast(currentPlayer);

						if (currentPlayer.isBust()) {
							currentPlayer.endPlay();
						} else {
							//after the player doubles down, they immediately hit then stand. So the player should automatically stand here.
							stood = true;
						}
						break;

					case SPLIT:
						// TODO: Implement Split
						break;

					case SURRENDER:
						currentPlayer.surrendering();
						break;

					default:
						break;
				}

			} else {
				StringBuilder logStringBuilder = new StringBuilder();
				logStringBuilder.append("Player attempted to do invalid action: ");
				logStringBuilder.append(abstractPlayer.name);
				logStringBuilder.append(". Action: ");
				logStringBuilder.append(playerAction);
				logStringBuilder.append(". Hand: ");

				for (Card card : abstractPlayer.hand) {
					logStringBuilder.append(card.getNumName()).append(" ").append(card.getSuitName());
				}

				Log.e(classLogName, logStringBuilder.toString());

				stood = true;
			}

			// If the player stood or they busted / surrendered...
			if (stood || !currentPlayer.isPlaying()) {
				// ...log whether they busted or not.
				if (currentPlayer.isBust()) {
					Log.i(classLogName, "Player " + abstractPlayer.name + " busted.");
				} else if (currentPlayer.getSurrender()) {
					Log.i(classLogName, "Player " + abstractPlayer.name + " surrendered.");
				}

				// If the current player is not the dealer, we can go to the next player (if
				// there is any).

				nextPlayer();
			}

			if(currentPlayer instanceof HumanPlayer) {
				promptHumanPlayerActions(currentPlayer);
			}
		} else {
			// Player tried to call an action when they wern't supposed to. Log the
			// incident. This might have occured due to the buttons for HumanPlayer actions
			// still being on screen, etc.
			if (currentPlayer == null) {
				Log.e(classLogName, "Player attempted to do action when current player wasn't set. Player: "
						+ abstractPlayer.name + ". Action: " + playerAction);
			} else {
				Log.e(classLogName, "Player attempted to do action out of turn: " + abstractPlayer.name
						+ ". Action: " + playerAction);
			}
		}
	}

	public void nextPlayer() {
		//skip everyone's turns if the dealer has a blackjack
		if(dealerPlayer.hasBlackjack()){
			playersLeftQueue.clear();
		}

		// If their are players still left in the queue, set the current player to that
		// player and begin prompting them for actions.
		if (playersLeftQueue != null && !playersLeftQueue.isEmpty()) {
			setCurrentPlayer(playersLeftQueue.poll());

			if (!currentPlayer.isDealer()) {
				showCardsAnimateAll(currentPlayer);
			}

			//if the player starts with a blackjack, they must immediately stand.
			if(currentPlayer.getHandValue() == 21){
				setCurrentPlayer(playersLeftQueue.poll());
			}

			if (currentPlayer instanceof HumanPlayer) {
				promptHumanPlayerActions(currentPlayer);
			} else {
				clearVisibleButtons();

				// the dealer / AIPlayer will return false if they haven't finished playing, returning true if it has.
				while(currentPlayer.getHandValue() <= 21 && ((AIPlayer) currentPlayer).takeTurn(getDealerShowingCard())){}
			}
		} else {
			// If there are no more players to process, than all but the dealer has played
			// for this round. End the game.
			endGame();
		}
	}

	public void endGame() {
		//NOTE: addWinningsBlackJack and Surrendering adjust the amount of money that has been bet
		//the actual payout happens in the block toward the end of the player loop

		//these are the textViews that will be used for the win/loss text
		Activity activity = Objects.requireNonNull(getActivity());
		TextView winOrloss = activity.findViewById(R.id.winOrLoss);
		TextView betValue = activity.findViewById(R.id.betvalue);

		String winOrLossString = "";
		String betValueString = "";


		// Show the dealers cards, which is what reveal will work on for flipping facedown cards.
		showCardsAndReveal(dealerPlayer);


		boolean dealerBusted = dealerPlayer.isBust();
		int dealerHandValue = dealerPlayer.getHandValue();
		boolean dealerHasBlackjack = dealerPlayer.hasBlackjack();

		//clearCurrentPlayerText();

		// Blank the log to make score total more readable.
		for (int i = 0; i < 4; i++) {
			Log.i(classLogName, "");
		}

		Log.i(classLogName, "Dealer Hand Value: " + (dealerBusted ? "Busted " : "") + dealerHandValue + (dealerPlayer.hasBlackjack() ? ". Has BlackJack." : ""));
		Log.i(classLogName, "");
		Log.i(classLogName, "Player Hand Values: ");

		// For every player, check if they are still in the game. If they are, check if
		// their hand was better than the dealer and output.
		for (AbstractPlayer abstractPlayer : players) {
			// Show the players cards, which is what reveal will work on for flipping facedown cards.
//			showCardsAndReveal(abstractPlayer);
			abstractPlayer.reveal(playerLayout);

			int handValue = abstractPlayer.getHandValue();

			//moved these out of the if for later use
			boolean tied = handValue == dealerHandValue;
			boolean playerWon = false;


			double betDispTemp = abstractPlayer.getBet()/100.0;
			Log.i(classLogName, abstractPlayer.name + " bet " + String.format("%.2f",(betDispTemp)));

			if (abstractPlayer.isPlaying()) {
				boolean playerHasBlackjack = abstractPlayer.hasBlackjack();

				if (dealerBusted || handValue > dealerHandValue) {//player won (moved this to make text color setting code nicer))
					playerWon = true;
					if(playerHasBlackjack){
						abstractPlayer.addWinningsBlackJack();
						Log.i(classLogName,
								"Player " + abstractPlayer.name + " beat the dealer with a blackjack!");

						winOrLossString = abstractPlayer.name + " Won With a BlackJack!";

					}
					else{
						Log.i(classLogName,
								"Player " + abstractPlayer.name + " beat the dealer with a hand value of " + handValue);

						winOrLossString = abstractPlayer.name + " Won With " + handValue;
					}
				} else if (tied) {
					// Taken from: https://en.wikipedia.org/wiki/Blackjack#:~:text=A%20player%20total,value%20of%2021.
					boolean blackjackTrueTie = dealerHasBlackjack && playerHasBlackjack;
					boolean noBlackjacks = !dealerHasBlackjack && !playerHasBlackjack;

					if(blackjackTrueTie) {
						// Both the dealer and the player had a Blackjack, meaning they tied.
						Log.i(classLogName,
								"Player " + abstractPlayer.name + " tied the dealer with a hand value of " + handValue + ", both with Blackjacks");

						winOrLossString = abstractPlayer.name + " Pushed\n Both Had Naturals!";
					} else if(noBlackjacks) {
						// Neither the dealer nor the player had a Blackjack, meaning the tie was just a tie.
						Log.i(classLogName,
								"Player " + abstractPlayer.name + " tied the dealer with a hand value of " + handValue);

						winOrLossString = abstractPlayer.name + " Pushed\n Both Had " + handValue;
					} else {
						// Either the dealer or the player had a Blackjack, meaning that whoever did wins the tie.
						tied = false;
						if(dealerHasBlackjack) {
							playerWon = false;
							Log.i(classLogName,
									"Player " + abstractPlayer.name + " lost to the dealer with a hand value of " + handValue + " without a Blackjack");

							winOrLossString = abstractPlayer.name + " Lost\n Dealer Had a Natural";
						} else {
							playerWon = true;
							abstractPlayer.addWinningsBlackJack();
							Log.i(classLogName,
									"Player " + abstractPlayer.name + " beat the dealer with a hand value of " + handValue + " with a Blackjack");

							winOrLossString = abstractPlayer.name + " Won\n You Had a Natural!";
						}
					}
				} else {
					Log.i(classLogName, "Player " + abstractPlayer.name
							+ " lost against the dealer with a hand value of " + handValue);

					winOrLossString = abstractPlayer.name + " Lost to "+ dealerPlayer.getHandValue();
				}
			} else {
				if(abstractPlayer.isBust()){
					Log.i(classLogName, abstractPlayer.name + " busted with a hand value of " + handValue);

					winOrLossString = abstractPlayer.name + " Busted at "+ handValue;
				}
				else if(abstractPlayer.getSurrender()){
					Log.i(classLogName, abstractPlayer.name + " surrendered with a hand value of " + handValue);

					winOrLossString = abstractPlayer.name + " Surrendered";
				}
			}


			//gets the current bet as a double in the format of xxx.xx
			double betDisplay = abstractPlayer.getBet()/100.0;

			//Will need to insert pauses somewhere to make the cards/victory screen appear individually
			//This if block sets the color of the money won/lost based on if the player won/lost/tied
			//This also gives each player its payout.
			if(playerWon){																				//if won
				abstractPlayer.addWinnings();
				Log.i(classLogName, abstractPlayer.name + " was paid " + String.format("%.2f",(betDisplay)));
				betValue.setTextColor(Color.parseColor("#b0f697"));
				betValueString = betValueString + "+$" + String.format("%.2f",(betDisplay));
			}else if (tied && !dealerBusted){															//if tied (and the dealer didn't bust)
				abstractPlayer.setBet(0);//no money removed from push.
				Log.i(classLogName, abstractPlayer.name + " regained " + String.format("%.2f",(betDisplay)));
				betValue.setTextColor(Color.parseColor("#cdcdcd"));
				betValueString = betValueString+"Received Bet Back";
			}else{																						//if lost
				abstractPlayer.subLosses();
				Log.i(classLogName, abstractPlayer.name + " lost " + String.format("%.2f",(betDisplay)));
				betValue.setTextColor(Color.parseColor("#cf7c7c"));
				betValueString = betValueString + "-$" + String.format("%.2f",(betDisplay));
			}

			//checks if the player it out of money and displays as such if they are
			//also removes them from current players so they can no longer play.
			if(abstractPlayer.getWinnings() <= 0){
				players.remove(abstractPlayer);
				betValue.setTextColor(Color.parseColor("#cf7c7c"));
				betValueString = "You Are Out of Money!";
			}

			//updates the player's text readout in the upper left
			updatePlayerText(abstractPlayer);
			//sets the strings to be what the above ifs determined they need to be
			winOrloss.setText(winOrLossString);
			betValue.setText(betValueString);

			if(abstractPlayer instanceof HumanPlayer){
				settings = context.getSharedPreferences(abstractPlayer.name, 0);
				edit = settings.edit();
				edit.putLong("bank", abstractPlayer.getWinnings());
				edit.commit();
			}
		}

		isDone = true;
	}

	// This should check if the action the player is making is valid.
	public boolean isActionValid(AbstractPlayer abstractPlayer, PlayerAction playerAction) {
		if (abstractPlayer.isBust()) {
			return false;
		} else if (!abstractPlayer.isPlaying()) {
			return false;
		} else if (abstractPlayer.getHandValue() == 21 && playerAction != PlayerAction.STAND) {
			return false;
		} else if (abstractPlayer.hasDoubled() && playerAction == PlayerAction.DOUBLE_DOWN) {
			return false;
		} else if (abstractPlayer.getBet()*2 > abstractPlayer.getWinnings() && playerAction == PlayerAction.DOUBLE_DOWN){
			return false;
		} else if (abstractPlayer.getSurrender() && playerAction == PlayerAction.SURRENDER) {
			return false;
		} else if (abstractPlayer.hand.size() > 2){
			switch(playerAction){
				case SURRENDER:		//surrender/double/split down are only allowed on a hand of 2 cards
				case DOUBLE_DOWN:	//player.hand.size > 2 works for checking surrender/double/split down validity since the initial hand will always be 2
				case SPLIT:			//also works for split in the future since you CAN double down/surrender on each new hand after a split
					return false;
				default:
					break;
			}
		}	else if (abstractPlayer.hand.get(0).getNum() != abstractPlayer.hand.get(1).getNum() && playerAction == PlayerAction.SPLIT) {
			return false; //note, it should only reach this if the player is splitting and their hand size is 2 because of the previous if.
		}

		return true; //if nothing wrong is found, returns as valid
	}

	// This method adds the player passed to the function to the list of players in
	// this Game object.
	public boolean addPlayer(@NonNull AbstractPlayer player) {
		return players.add(player);
	}

	// This method will deal two cards to each player, including the dealer.
	public void dealInitialHand() {
		for (AbstractPlayer abstractPlayer : players) {
			abstractPlayer.hand.add(deck.getCard());
			abstractPlayer.hand.add(deck.getCard());

			showCardsAnimateAll(abstractPlayer);
		}

		Card temp = deck.getCard();
		temp.setFaceDown(true);
		dealerPlayer.hand.add(temp);
		dealerPlayer.hand.add(deck.getCard());

		showCardsAnimateAll(dealerPlayer);
	}

	// This will return which card the dealer is "showing". This will be their
	// second card, or going from index 0 as far left and final index as far right,
	// their left-most card.
	public int getDealerShowingCard() {
		return dealerPlayer.hand.get(1).getNum();
	}

	public boolean isDone() {
		return isDone;
	}

	public void clearCurrentPlayerText() {
		TextView userTurnTextView = Objects.requireNonNull(getActivity()).findViewById(R.id.userTurntxt);
		userTurnTextView.setVisibility(View.INVISIBLE);
		userTurnTextView.setText("");
	}

	@Nullable
	public Activity getActivity() {
		if (context instanceof Activity) {
			return (Activity) context;
		}

		return null;
	}

	public void setCurrentPlayer(@Nullable AbstractPlayer newCurrentPlayer) {
		this.currentPlayer = newCurrentPlayer;
		updatePlayerText(currentPlayer);
	}

	public void promptHumanPlayerActions(@NonNull AbstractPlayer abstractPlayer) {
		if(abstractPlayer instanceof HumanPlayer) {
			Log.i(classLogName, "Player " + abstractPlayer.name + " has " + abstractPlayer.getHandValue());

			Activity activity = getActivity();
			//just set everything to invisible and	 then update what is visible before the next UI call (which will be some time after it runs this function)
			clearVisibleButtons();

			if(abstractPlayer.isPlaying()) {
				boolean initialHand = abstractPlayer.hand.size() == 2;

				activity.findViewById(R.id.Hit).setVisibility(View.VISIBLE);
				activity.findViewById(R.id.Stand).setVisibility(View.VISIBLE);

				// If it is the player's initial hand, check if they can split. If not, then hide
				// the split button.
				if(initialHand) {
					activity.findViewById(R.id.Surrender).setVisibility(View.VISIBLE);

					// If the player hasn't doubled, then they can still double. Show them the button.
					// Can only double with the initial hand.
					if(!abstractPlayer.hasDoubled() && (abstractPlayer.getBet()*2) <= abstractPlayer.getWinnings()) {
						activity.findViewById(R.id.DoubleDwn).setVisibility(View.VISIBLE);
					}

					// ...and their two cards have an equal number, then they can split, so show the
					// button. If not, hide the split button.
					if(abstractPlayer.hand.get(0).getNum() == abstractPlayer.hand.get(1).getNum()) {
						activity.findViewById(R.id.Split).setVisibility(View.VISIBLE);
					}
				}
			} else {
				clearVisibleButtons();
			}
		} else {
			clearVisibleButtons();
		}
	}

	public void clearVisibleButtons() {
		Activity activity = Objects.requireNonNull(getActivity());

		activity.findViewById(R.id.Hit).setVisibility(View.INVISIBLE);
		activity.findViewById(R.id.Stand).setVisibility(View.INVISIBLE);
		activity.findViewById(R.id.DoubleDwn).setVisibility(View.INVISIBLE);
		activity.findViewById(R.id.Surrender).setVisibility(View.INVISIBLE);
		activity.findViewById(R.id.Split).setVisibility(View.INVISIBLE);
	}

	public void showCards(@Nullable AbstractPlayer player) {
		showCards(player, defaultRevealOnShow);
	}

	public void showCardsAnimateLast(@Nullable AbstractPlayer player) {
		if (player != null) {
			showCards(player, defaultRevealOnShow, new int[] { player.hand.size() - 1 });
		}
	}

	public void showCardsAnimateAll(@Nullable AbstractPlayer player) {
		showCards(player, defaultRevealOnShow, allAnimationArray);
	}

	public void showCardsAndReveal(@Nullable AbstractPlayer player) {
		showCards(player, true);
	}

	public void showCards(@Nullable AbstractPlayer player, boolean reveal) {
		showCards(player, reveal, null);
	}

	public void showCards(@Nullable AbstractPlayer player, boolean reveal, @Nullable int[] cardIndexAnimateList) {
		if (player != null) {
			RelativeLayout layout = playerLayout;
			if (player.isDealer()) {
				layout = dealerLayout;
			}

			player.showCards(context, layout, cardIndexAnimateList, player.isDealer());

			if (reveal) {
				player.reveal(layout);
			}
		}
	}

	public void initialize() {

		//clears all the players hands
		for(AbstractPlayer player : players){
			player.emptyHand();
			player.startPlay();
		}

		dealerPlayer.emptyHand();
		dealerPlayer.startPlay();

		// Deals two cards to each player
		dealInitialHand();
		// Start Round
		doRound();
	}

	//needed to prevent overlap of the end game screen and the betting screen
	public void resetIsDone(){
		isDone = false;
	}
	public void setIsDone(){isDone = true;}

	//rebuilt this to take in an AbstractPlayer object so that it can be used if the playerQueue is empty
	public void updatePlayerText(AbstractPlayer player) {
		TextView userTurnTextView = Objects.requireNonNull(getActivity()).findViewById(R.id.userTurntxt);

		userTurnTextView.setVisibility(View.VISIBLE);

		double betDisplay = player.getBet()/100.0;
		double bankDisplay = player.getWinnings()/100.0;

		String playerText = (player.name + "'s Turn");
		String moneyPool = ("\nMoney Pool: " + String.format("%.2f",(bankDisplay)));
		String currentBet = ("\nCurrent Bet: " + String.format("%.2f",(betDisplay)));

		userTurnTextView.setText(playerText + moneyPool + currentBet);
	}
}
