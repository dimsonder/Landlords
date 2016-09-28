package com.mym.landlords.card;

import java.util.ArrayList;

/**
 * 卡牌工厂类，负责卡牌的印制\(^o^)/。
 */
public final class CardFactory {
	
	/**
	 * 获得一副新牌。
	 * @return 返回的卡牌按花色排列，大小王在最后。
	 */
	public static ArrayList<Card> newCardPack(){
		ArrayList<Card> cards = new ArrayList<>();
		for (int i=Card.CARD_VALUE_3; i<=Card.CARD_VALUE_2; i++){
			cards.add(new Card(CardSuit.Spade, i));
		}
		for (int i=Card.CARD_VALUE_3; i<=Card.CARD_VALUE_2; i++){
			cards.add(new Card(CardSuit.Heart, i));
		}
		for (int i=Card.CARD_VALUE_3; i<=Card.CARD_VALUE_2; i++){
			cards.add(new Card(CardSuit.Club, i));
		}
		for (int i=Card.CARD_VALUE_3; i<=Card.CARD_VALUE_2; i++){
			cards.add(new Card(CardSuit.Diamond, i));
		}
		cards.add(new Card(CardSuit.Joker, Card.CARD_VALUE_JOKER_S));
		cards.add(new Card(CardSuit.Joker, Card.CARD_VALUE_JOKER_B));
		return cards;
	}
}
