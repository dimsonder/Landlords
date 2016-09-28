package com.mym.landlords.card;

import android.util.Log;

import java.util.ArrayList;

/**对子
 */
public final class Pair extends CardType implements NonBombType {

	public Pair(ArrayList<Card> list) {
		Log.i("Pair","identify");
		if (list == null || list.size() != 2
				|| (!(list.get(0).isSameValueAs(list.get(1))))) {
			throw new IllegalArgumentException("A pair must be 2 same cards!");
		}
		//保护性复制
		Log.i("Pair","copy");
		cardList = new ArrayList<>(list);
	}

	/**
	 * 实现对子牌型大小比对。
	 * @return 如果参数是炸弹，则始终返回负数；否则返回按点数的比较结果。
	 * @see com.mym.landlords.card.CardType#compareTo(com.mym.landlords.card.CardType)
	 */
	@Override
	public int compareTo(CardType another) {
		int superCompare = super.compareTo(another);
		if (superCompare != UNDEFINED_COMPARE) {
			Log.i("Pair","UNDEFINED");
			return superCompare;
		}
		if (!(another instanceof Pair)) {
			throw new ClassCastException("compare to wrong object.");
		}
		return cardList.get(0).compareTo(((Pair) another).cardList.get(0));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Pair ").append(cardList);
		return builder.toString();
	}
}
