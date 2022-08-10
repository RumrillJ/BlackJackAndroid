package com.cs380.blackjackgame.gamelogic;

import androidx.annotation.NonNull;

public class HumanPlayer extends AbstractPlayer {
	public HumanPlayer(@NonNull String name, @NonNull Game game, long startingMoney) {
		super(name, game, startingMoney);
	}

	// All of the methods below call the AbstractPlayer.doAction() method, which
	// just passed this player object and their action to the Game associated with
	// this player.

	// Then, for each button, link the button back to one of these functions or call
	// the AbstractPlayer.doAction(PlayerAction) method with what action is
	// associated with the button which was pressed.
	public void hit() {
		doAction(PlayerAction.HIT);
	}

	public void stand() {
		doAction(PlayerAction.STAND);
	}

	public void doubleDownAction() {doAction(PlayerAction.DOUBLE_DOWN);}

	public void split() {
		doAction(PlayerAction.SPLIT);
	}

	public void surrender() {doAction(PlayerAction.SURRENDER);}
}