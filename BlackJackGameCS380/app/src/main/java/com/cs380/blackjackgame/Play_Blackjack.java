package com.cs380.blackjackgame;

import android.app.Dialog;
import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cs380.blackjackgame.cardflipanimation.CardFlipRunnable;
import com.cs380.blackjackgame.gamelogic.AIPlayer;
import com.cs380.blackjackgame.gamelogic.AbstractPlayer;
import com.cs380.blackjackgame.gamelogic.Game;
import com.cs380.blackjackgame.gamelogic.HumanPlayer;
import com.cs380.blackjackgame.gamelogic.PlayerAction;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Play_Blackjack extends AppCompatActivity {
	public Button quitGame;
	public Button Hit;
	public Button Stand;
	public Button Doubledwn;
	public Button Split;
	public Game game;
	public Button Replay;
	public Button guide;
	public Button submitBet;
	public RelativeLayout bettingScreen;
	public LinkedList<AbstractPlayer> playersToPrompt;
	public EditText	inpBet;
	public TextView promptTxt;
	public InputMethodManager imm;
	public RelativeLayout endGameText;

	private HashSet<CardFlipRunnable> cardFlipRunnablesSet = new HashSet<>();

	public SharedPreferences settings;
	public SharedPreferences curProf;

	// All HumanPlayer's and AIPLayer's (other than the dealer, which is handled
	// automatically) should be added to the Game.players set.
	// public AbstractPlayer player;
	// public HumanPlayer Human;

	// The dealer is automatically created by constructing Game (new Game()) and is
	// located at Game.dealer
//	public AIPlayer Dealer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_blackjack);

		//this is an input manager that allows you to modify (ie: show/hide) the keyboard manually.
		imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);

		// Start the card flip animation timer.
		// Taken from 02-08-2022 17:02:20 PST: https://stackoverflow.com/a/12567961
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				cardFlipTickRunnableList();
			}
		}, 0, 1);

		curProf = getSharedPreferences("CurrentProfile", 0);
		String name = curProf.getString("profile", "DEFAULT");

		settings = getSharedPreferences(name, 0);
		long bank = settings.getLong("bank", 500000);
		int decks = settings.getInt("decks", 2);


		game = new Game(this, findViewById(R.id.userscr), findViewById(R.id.dealerscr), decks);

		if (bank > 0){
			game.addPlayer(new HumanPlayer(name, game, bank));
		}

		//added code does work for multiple players, but commented out for testing while browsing hands/bets are being implemented
		//game.addPlayer(new HumanPlayer("Player 2", game, 5000.00));


		// We need to assign variables to the Hit, Stand, and Doubledwn fields of
		// Play_Blackjack, else we will NullPointerException.
		Hit = findViewById(R.id.Hit);
		Stand = findViewById(R.id.Stand);
		Doubledwn = findViewById(R.id.DoubleDwn);

		findViewById(R.id.Hit).setOnClickListener(view -> game.currentPlayer.doAction(PlayerAction.HIT));

		findViewById(R.id.Stand).setOnClickListener(view -> game.currentPlayer.doAction(PlayerAction.STAND));

		findViewById(R.id.DoubleDwn).setOnClickListener(view -> game.currentPlayer.doAction(PlayerAction.DOUBLE_DOWN));

		findViewById(R.id.Surrender).setOnClickListener(view -> game.currentPlayer.doAction(PlayerAction.SURRENDER));

		findViewById(R.id.Split).setOnClickListener(view -> game.currentPlayer.doAction(PlayerAction.SPLIT));

		// split button
		Split = findViewById(R.id.Split);

		// quit game button
		quitGame = findViewById(R.id.quit);
		quitGame.setOnClickListener((View v) -> Return_to_Menu());

		Replay = findViewById(R.id.Replay);
		Replay.setOnClickListener((View v) -> replay());

		//popup window
		guide = findViewById(R.id.rules);
		guide.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showpopUP();
			}
		});



		//runs the runnable on the handler that causes the app to check if the game is done every 1/100 second
		//if it's done, display the restart/quit buttons
		handler.postDelayed(endGameCheck, 10);

		endGameText = findViewById(R.id.endGameText);


		//bet listener stuff
		submitBet = findViewById(R.id.submitBet);
		bettingScreen = findViewById(R.id.bettingLayout);
		inpBet = findViewById(R.id.betInput);
		promptTxt = findViewById(R.id.betPrompt);

		//initializes bet collection state
		playersToPrompt = new LinkedList<>(game.players);

		//if there are no players (ie the game rand out of players from bankruptcy or players going in had no money)
		if(!playersToPrompt.isEmpty()){
			bettingScreen.setVisibility(View.VISIBLE);
			updateBetPromptText();
			//need to reset this outside of the game initialization due to the exit game/replay buttons overlapping
			game.resetIsDone();
		} else{
			TextView winOrloss = findViewById(R.id.winOrLoss);
			TextView betValue = findViewById(R.id.betvalue);

			String winOrLossString = "Cannot Start Game!";

			betValue.setTextColor(Color.parseColor("#cf7c7c"));
			String betValueString = "All Players Are Out of Money!";

			winOrloss.setText(winOrLossString);
			betValue.setText(betValueString);

			game.setIsDone();
		}

		findViewById(R.id.submitBet).setOnClickListener(view -> {
			if(!playersToPrompt.isEmpty()){
				if(playersToPrompt.getFirst() instanceof HumanPlayer) {
					//get the user's input double, and round it to the nearest cent
					String check = inpBet.getText().toString();
					double bet = -1;
					if(!"".equals(check)){
						bet = Math.round(100 * Double.parseDouble(check));
					}

					//casts as int for storage and math to prevent background rounding errors
					int temp = (int) bet;//rounds to the nearest cent

					//if bet is valid, set the bet and remove the player from the queue
					if(temp > 0 && temp <= playersToPrompt.getFirst().getWinnings()){
						playersToPrompt.getFirst().setBet(temp);
						playersToPrompt.removeFirst();
					}
				}

				//clears the input in the window and closes the keyboard automatically
				inpBet.setText("");
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

				//things to run after the player has entered a value if the list is still not empty
				if(!playersToPrompt.isEmpty()){
					if(playersToPrompt.getFirst() instanceof HumanPlayer) {
						updateBetPromptText();
					}

					//prompt the AI through the list until it hits another human player (if ever)
					while(!playersToPrompt.isEmpty() && playersToPrompt.getFirst() instanceof AIPlayer){
						//if the AIPlayer isn't a dealer, get their bet (Dealers don't make bets)
						if(!playersToPrompt.getFirst().isDealer()) {
							//(AIPlayer) playersToPrompt.getFirst().makeBet() TODO: Implement AI betting
						}
						//when the ai is done making their bet, remove them from the queue
						playersToPrompt.removeFirst();
					}
				}
				//if the players left to prompt for their bet is empty after entering a value, start the game.
				else{
					//hide the window and start the game
					bettingScreen.setVisibility(View.INVISIBLE);
					game.initialize();
				}
			}
		});


	}


	// Taken from 02-22-2022 13:43:33 PST: https://stackoverflow.com/a/24359962
	@Override
	public void onBackPressed() {
//		super.onBackPressed();
		// TODO: Prompt user if they want to go back to the main menu if a game is currently active.
		Return_to_Menu();
	}

	private final Handler handler = new Handler();
	private final Runnable endGameCheck = new Runnable(){
		@Override
		public void run(){
			if (game.isDone()) {
				game.clearVisibleButtons();
				quitGame.setVisibility(View.VISIBLE);

				//if there are no players, you can't replay
				//this will work with removing players when their bank is empty.
				if(!game.players.isEmpty()) {
					Replay.setVisibility(View.VISIBLE);
				}

				endGameText.setVisibility(View.VISIBLE);
			}
			handler.postDelayed(this, 10);
		}
	};



	public void Return_to_Menu() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	public void replay(){
		//re-initializes the game state

		//need to reset this outside of the game initialization due to the exit game/replay buttons overlapping
		game.resetIsDone();
		quitGame.setVisibility(View.INVISIBLE);
		Replay.setVisibility(View.INVISIBLE);
		endGameText.setVisibility(View.INVISIBLE);

		//initializes bet collection state
		playersToPrompt = new LinkedList<>(game.players);
		updateBetPromptText();
		bettingScreen.setVisibility(View.VISIBLE);
	}

	public void updateBetPromptText(){
		AbstractPlayer curPlayer = playersToPrompt.getFirst();

		//gets the current player from the prompt list, takes their bank, and makes it into a double in the format of xxx.xx
		double bankDisplay = curPlayer.getWinnings()/100.0;

		//displays their current bank and name within the prompt window
		String currentMoney = ("Banked Money: " + String.format("%.2f",bankDisplay));
		String playerNamePrompt = ("\n" + curPlayer.name + ", Please Enter Your Bet:");

		promptTxt.setText(currentMoney + playerNamePrompt);

	}

	public void addCardFlipRunnable(@Nullable CardFlipRunnable cardFlipRunnable) {
		// TODO: Can be replaced by overriding the equals() and hashcode() methods in
		// CardFlipRunnable and let the set decide what is a dupicate or not.
		if (cardFlipRunnable != null) {
			boolean valid = true;
			for (CardFlipRunnable otherCardFlipRunnable : cardFlipRunnablesSet) {
				if (cardFlipRunnable.imageView.equals(otherCardFlipRunnable)) {
					valid = false;
					break;
				}
			}

			if (valid) {
				cardFlipRunnablesSet.add(cardFlipRunnable);
			}
		}
	}

	private static boolean inRunnable = false;

	private void cardFlipTickRunnableList() {
		if (!inRunnable) {
			inRunnable = true;
			// This method is called directly by the timer
			// and runs in the same thread as the timer.

			// We call the method that will work with the UI
			// through the runOnUiThread method.

			HashSet<CardFlipRunnable> removeSet = new HashSet<>();
			for (CardFlipRunnable cardFlipRunnable : new HashSet<>(cardFlipRunnablesSet)) {
				if (!cardFlipRunnable.cancel) {
					this.runOnUiThread(cardFlipRunnable);
				} else {
					removeSet.add(cardFlipRunnable);
				}
			}

			cardFlipRunnablesSet.removeAll(removeSet);

			inRunnable = false;
		}
	}

	public void showpopUP(){
		//create new dialog that sets the popup view the designated xml file and sets background as well.
		Dialog dialog = new Dialog(this, R.style.dialog_popup);
		dialog.setContentView(R.layout.popup_dialogue);
		dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_color);
		dialog.show();
	}

}