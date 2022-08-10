package com.cs380.blackjackgame.gamelogic;

import androidx.annotation.NonNull;
import java.util.concurrent.ThreadLocalRandom;

public class AIPlayer extends AbstractPlayer {
	private final AIDiff difficulty;

	//constructors
	public AIPlayer(@NonNull String name, @NonNull Game game) {
		super(name, game, 500000);
		this.difficulty = AIDiff.TEMPLATE;
	}

	public AIPlayer(@NonNull String name, @NonNull Game game, @NonNull AIDiff difficulty) {
		super(name, game, 500000);
		this.difficulty = difficulty;
	}

	//public functions
//	@Override
	public boolean takeTurn(){
		if(difficulty == AIDiff.DEALER){return dealerAI();}
		else {
			//randomization if the card the dealer has face up is not passed.
			int dealerShowing = ThreadLocalRandom.current().nextInt(13);
			return takeTurn(dealerShowing);
		}
	}

	public boolean takeTurn(int dealerShowing) {
		// Using the https://en.wikipedia.org/wiki/Blackjack#Basic_strategy as a template
		switch(difficulty) {
			case EASY:
				return easyAI(dealerShowing);
			case MEDIUM:
				return mediumAI(dealerShowing);
			case HARD:
				return hardAI(dealerShowing);
			case DEALER:
				return dealerAI();
			case TEMPLATE:
				return templateAI(dealerShowing);
			default:
				break;
		}
   
		return true;
	}

	//private functions
	private boolean templateAI(int dealerShowing) {

		dealerShowing++;	//makes the passed value the actual value
		if(dealerShowing > 10){dealerShowing = 10;} //caps the value to the max blackjack value
		if(dealerShowing == 1){dealerShowing = 11;} //makes ace the highest value for easier patterns

		int val = getHandValue();
		boolean hasAce = hasAce();

		if(val == 21){
			doAction(PlayerAction.STAND);
			return false;
		}

		//these are flags that allow toggling future features to enable parts of the AI for testing
		//they will eventually be removed when everything is in place.
		boolean ddImplemented = true;
		boolean surrenderImplemented = true;
		boolean splitImplemented = false; //note: requires both the above to be implemented

		//Special case block: pairs, can surrender, can double down(soft and hard)
		if(hand.size() == 2) {
			//Special case: pairs
			if (hand.getFirst().getNum() == hand.getLast().getNum() && splitImplemented){
				//getting the type of pairs that the AI has
				int num = hand.getFirst().getNum();

				//special case: double down in pairs
				if(num == 5 && !hasDoubled() && dealerShowing < 10){
					isDoubling();
					doAction(PlayerAction.DOUBLE_DOWN);
					return false;
				}

				//special case: surrender in pairs
				if(dealerShowing == 11 && num == 8){
					doAction(PlayerAction.SURRENDER);
					return false;
				}

				//the rest of the pairs handling
				switch(num){
					case 2:
					case 3:
					case 7:
						if(dealerShowing < 8){doAction(PlayerAction.SPLIT);}
						else{doAction(PlayerAction.HIT);}
						return true;
					case 4:
						if(dealerShowing == 5 || dealerShowing == 6){doAction(PlayerAction.SPLIT);}
						else{doAction(PlayerAction.HIT);}
						return true;
					case 5:
						doAction(PlayerAction.HIT);
						return true;
					case 6:
						if(dealerShowing < 7){doAction(PlayerAction.SPLIT);}
						else{doAction(PlayerAction.HIT);}
						return true;
					case 8:
					case 1:
						doAction(PlayerAction.SPLIT);
						return true;
					case 9:
						if(dealerShowing == 7 || dealerShowing > 9){doAction(PlayerAction.STAND);return false;}
						else{doAction(PlayerAction.SPLIT);return true;}
					case 10:
						doAction(PlayerAction.STAND);
						return false;
					default:
						break;
				}
			}// end special case pairs

			//special case: can surrender
			else if (val < 18 && val > 14 && !hasAce && surrenderImplemented) {
				switch (dealerShowing) {
					case 9: //showing 9
						if (val == 16) {
							doAction(PlayerAction.SURRENDER);
							return false;
						}
						break;
					case 10: //showing 10-king
						if (val == 15 || val == 16) {
							doAction(PlayerAction.SURRENDER);
							return false;
						}
						break;
					case 11: //showing ace
						doAction(PlayerAction.SURRENDER);
						return false;
					default:
						break;
				}
			}//end special case surrender

			//special case: can double down in soft total
			else if(!hasDoubled() && hasAce && ddImplemented){
				switch(dealerShowing){
					case 2:
						if (val == 18){
							isDoubling();
							doAction(PlayerAction.DOUBLE_DOWN);
							return false;
						}
						break;
					case 3:
						if(val > 16 && val < 19){
							isDoubling();
							doAction(PlayerAction.DOUBLE_DOWN);
							return false;
						}
						break;
					case 4:
						if(val > 14 && val < 19){
							isDoubling();
							doAction(PlayerAction.DOUBLE_DOWN);
							return false;
						}
						break;
					case 5:
						if(val > 12 && val < 19){
							isDoubling();
							doAction(PlayerAction.DOUBLE_DOWN);
							return false;
						}
						break;
					case 6:
						if(val > 12 && val < 20){
							isDoubling();
							doAction(PlayerAction.DOUBLE_DOWN);
							return false;
						}
						break;
					default:
						break;
				}
			}

			//special case: can double down in hard totals
			else if (!hasDoubled() && !hasAce && ddImplemented) {
				switch(val){
					case 9:
						isDoubling();
						doAction(PlayerAction.DOUBLE_DOWN);
						return false;
					case 10:
						if(dealerShowing < 10){
							isDoubling();
							doAction(PlayerAction.DOUBLE_DOWN);
							return false;
						}
						break;
					case 11:
						if(dealerShowing < 7 && dealerShowing > 2){
							isDoubling();
							doAction(PlayerAction.DOUBLE_DOWN);
							return false;
						}
						break;
					default:
						break;
				}
			}//end special case double down
		}//end special case block

		//logic for soft totals
		if (hasAce){
			if(val > 17) {
				if(val == 18 && (dealerShowing > 8)){doAction(PlayerAction.HIT); return true;}
				else{doAction(PlayerAction.STAND); return false;}
			}
			else{
				doAction(PlayerAction.HIT); return true;
			}
		}


		//logic for hard totals
		switch(dealerShowing){
			case 2:
			case 3:
				if(val > 12){ doAction(PlayerAction.STAND); return false;}
				else{doAction(PlayerAction.HIT); return true;}
			case 4:
			case 5:
			case 6:
				if(val > 11){doAction(PlayerAction.STAND); return false;}
				else{doAction(PlayerAction.HIT); return true;}
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
				if(val > 16){doAction(PlayerAction.STAND); return false;}
				else{doAction(PlayerAction.HIT); return true;}
			default:
				break;
		}

		return true;
	}//end template ai

	//TODO: Implement different levels of ai based on the initial template ai.
	// placeholder just calls the template AI
	private boolean easyAI(int dealerShowing){return templateAI(dealerShowing);}
	private boolean mediumAI(int dealerShowing){return templateAI(dealerShowing);}
	private boolean hardAI(int dealerShowing){return templateAI(dealerShowing);}

	private boolean dealerAI(){
		int val = getHandValue();

		//note: soft totals automatically handled by how getHandValue calculates soft totals
		if(val < 17){
			doAction(PlayerAction.HIT);
			return true;
		}
		else{ //if val >= 17
			doAction(PlayerAction.STAND);
			return false;
		}
	}//end DealerAI

	public AIDiff getDifficulty(){return difficulty;}
}//end AIPlayer