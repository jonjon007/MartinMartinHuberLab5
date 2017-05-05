package pkgPoker.app.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import netgame.common.Hub;
import pkgPokerBLL.Action;
import pkgPokerBLL.Card;
import pkgPokerBLL.CardDraw;
import pkgPokerBLL.Deck;
import pkgPokerBLL.GamePlay;
import pkgPokerBLL.GamePlayPlayerHand;
import pkgPokerBLL.Player;
import pkgPokerBLL.Rule;
import pkgPokerBLL.Table;

import pkgPokerEnum.eAction;
import pkgPokerEnum.eCardCount;
import pkgPokerEnum.eCardDestination;
import pkgPokerEnum.eCardVisibility;
import pkgPokerEnum.eDrawCount;
import pkgPokerEnum.eGame;
import pkgPokerEnum.eGameState;

public class PokerHub extends Hub {

	private Table HubPokerTable = new Table();
	private GamePlay HubGamePlay;
	private int iDealNbr = 0;

	public PokerHub(int port) throws IOException {
		super(port);
	}

	protected void playerConnected(int playerID) {

		if (playerID == 2) {
			shutdownServerSocket();
		}
	}

	protected void playerDisconnected(int playerID) {
		shutDownHub();
	}

	protected void messageReceived(int ClientID, Object message) {

		if (message instanceof Action) {
			Player actPlayer = (Player) ((Action) message).getPlayer();
			Action act = (Action) message;
			switch (act.getAction()) {
			case Sit:
				HubPokerTable.AddPlayerToTable(actPlayer);
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case Leave:			
				HubPokerTable.RemovePlayerFromTable(actPlayer);
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case TableState:
				resetOutput();
				sendToAll(HubPokerTable);
				break;
			case StartGame:
				//Number passes = max - min + 1 ???????
				// Get the rule from the Action object.
				Rule rle = new Rule(act.geteGame());
				
				//TODO Lab #5 - If neither player has 'the button', pick a random player
				//		and assign the button.				

				//TODO Lab #5 - Start the new instance of GamePlay
				HubGamePlay = new GamePlay(rle, actPlayer.getPlayerID());
				// Add Players to Game
				HubGamePlay.setGamePlayers(HubPokerTable.getHmPlayer());
				// Set the order of players
				HubGamePlay.GetOrder(actPlayer.getiPlayerPosition());
				//int iPos = 1;
				//for(Player plyr : HubGamePlay.getGamePlayers().values()){
					//plyr.setiPlayerPosition(iPos);
					//iPos++;
				//}


			case Draw:
				//TODO Lab #5 -	Draw card(s) for each player in the game.
				for(eDrawCount cCount : eDrawCount.values()){
					for(Player plyr : HubGamePlay.getGamePlayers().values()){
						if(HubGamePlay.getPlayerHand(plyr).getCardsInHand().size() < HubGamePlay.getRule().GetPlayerNumberOfCards()
								&& HubGamePlay.getPlayerHand(plyr).getCardsInHand().size() < cCount.getDrawNo()){
							HubGamePlay.drawCard(plyr, eCardDestination.Player);
							//CardDraw crdDr = new CardDraw(eCardCount.One, eCardDestination.Player, eCardVisibility.VisibleMe);
							//HubGamePlay.getRule().GetDrawCard(cCount);
							//Auto-visibility set?
						}
					}
				}
				if(HubGamePlay.getRule().GetCommunityCardsCount() > 0){
					if(HubGamePlay.getGameCommonHand().getCardsInHand().size() == 0){
						for(int i = 0; i < HubGamePlay.getRule().getCommunityCardsMin(); i++)
							//HubGamePlay.getGameCommonHand().AddCardToHand(HubGamePlay.getGameDeck().Draw());
							HubGamePlay.drawCard(HubGamePlay.getPlayerCommon(), eCardDestination.Community);
					}
					else if(HubGamePlay.getGameCommonHand().getCardsInHand().size() < HubGamePlay.getRule().getCommunityCardsMax())
						HubGamePlay.drawCard(HubGamePlay.getPlayerCommon(), eCardDestination.Community);
				}
				//TODO Lab #5 -	Make sure to set the correct visiblity
				//TODO Lab #5 -	Make sure to account for community cards

				//TODO Lab #5 -	Check to see if the game is over
				HubGamePlay.isGameOver();
				
				resetOutput();
				//	Send the state of the gameplay back to the clients
				sendToAll(HubGamePlay);
				break;
			case ScoreGame:
				// Am I at the end of the game?

				resetOutput();
				sendToAll(HubGamePlay);
				break;
			}
			
		}

	}

}