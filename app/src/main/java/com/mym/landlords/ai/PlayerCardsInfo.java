package com.mym.landlords.ai;

import java.util.ArrayList;

import com.mym.landlords.card.CardType;

/**
 * 存放玩家的手牌分析结果。
 */
public final class PlayerCardsInfo {
	
	//hide accessibility
	protected PlayerCardsInfo() {}

	/**单牌的张数。 */
	protected int singleCount = 0;		
	/**独立的对子的个数。 */
	protected int pairCount = 0;		
	/**三条的个数。 */
	protected int threeCount = 0;		
	/**顺子的个数。 */
	protected int straightCount = 0;	
	/**炸弹的个数（含王炸）。*/
	protected int bombCount = 0;		
	/**2和王的个数。 */
	protected int twoAndJokerCount = 0; 
	/**是否有王炸。 */
	protected boolean hasRocket = false;
	protected int expectedRound = 0;	//预期几个回合能出完牌
	protected ArrayList<CardType> cardTypes = new ArrayList<>();//牌型列表

	public int getSingleCount() {
		return singleCount;
	}

	public int getPairCount() {
		return pairCount;
	}

	public int getThreeCount() {
		return threeCount;
	}

	public int getBombCount() {
		return bombCount;
	}

	public int getTwoAndJokerCount() {
		return twoAndJokerCount;
	}

	public boolean isHasRocket() {
		return hasRocket;
	}

	public int getExpectedRound() {
		return expectedRound;
	}

	public ArrayList<CardType> getCardTypes() {
		return cardTypes;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PlayerCardsInfo [singleCount=").append(singleCount)
				.append(", pairCount=").append(pairCount)
				.append(", threeCount=").append(threeCount)
				.append(", straightCount=").append(straightCount)
				.append(", bombCount=").append(bombCount)
				.append(", twoAndJokerCount=").append(twoAndJokerCount)
				.append(", hasRocket=").append(hasRocket)
				.append(", expectedRound=").append(expectedRound)
				.append(", cardTypes=").append(cardTypes).append("]");
		return builder.toString();
	}
	
	protected final void recycle(){
		if (cardTypes!=null){
			cardTypes.clear();
		}
		cardTypes = null;
	}
}
