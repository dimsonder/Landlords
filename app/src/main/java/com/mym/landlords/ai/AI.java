package com.mym.landlords.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.mym.landlords.card.Airplane;
import com.mym.landlords.card.Bomb;
import com.mym.landlords.card.BombType;
import com.mym.landlords.card.Card;
import com.mym.landlords.card.CardSuit;
import com.mym.landlords.card.CardType;
import com.mym.landlords.card.DoubleStraight;
import com.mym.landlords.card.NonBombType;
import com.mym.landlords.card.Pair;
import com.mym.landlords.card.Rocket;
import com.mym.landlords.card.Single;
import com.mym.landlords.card.Straight;
import com.mym.landlords.card.Three;
import com.mym.util.LangUtils;

/**
 * 处理游戏的AI逻辑。
 * <p>所有的字段和方法访问权限均为包级别访问，即外界不可见。方法会通过Player类进行封装，对外界透明。
 * 此外，所有的方法均不做为静态，主要是为了AI的定制化和多样化考虑，可以引入策略模式使得AI更灵活多变。</p>
 *
 */
final class AI {
	
	private final String LOG_TAG/* = "AI"*/;
	/**
	 * 保存关联的AI Player对象。
	 */
	private final Player bindPlayer;
	
	//package access
	AI(Player player){
		bindPlayer = player;
		LOG_TAG = (bindPlayer==null || bindPlayer.getPlayerName()==null)
				? "AI" : bindPlayer.getPlayerName();
	}
	
	/**
	 * 按照牌张大小和牌中炸弹数量分析叫牌的分数。
	 * <p>基本算法如下:王炸记8分，大王4分，小王3分，每个2记2分，每个A记1分，炸弹每个记3分。
	 * 全部加起来后看最后得分，大于8分即可叫3分，大于5分可叫2分，大于3分可叫1分，否则不叫。</p>
	 * @return 返回0~3之间的某个数值。
	 */
	private final int callLandlord(ArrayList<Card> list){
		//force sort 
		Collections.sort(list, Card.COMPARATOR_WITH_SUIT);
		int evaluateScore = 0;
		int size = list.size();
		//判断王炸和单张大小王
		if ( list.get(size-1).getValue()==Card.CARD_VALUE_JOKER_B 
				&& list.get(size-2).getValue()==Card.CARD_VALUE_JOKER_S ){
			evaluateScore += 8;
		}
		else if ( list.get(size-1).getValue()==Card.CARD_VALUE_JOKER_B ){
			evaluateScore += 4;
		}
		else if ( list.get(size-1).getValue()==Card.CARD_VALUE_JOKER_S ){
			evaluateScore += 3;
		}
		//判断2和A/K的数量
		for (Card card:list){
			switch (card.getValue()){
				case Card.CARD_VALUE_2: evaluateScore += 2; break;
				case Card.CARD_VALUE_A: evaluateScore += 1; break;
				case Card.CARD_VALUE_K: evaluateScore += 1; break;
				default: //do nothing
				break;
			}
		}
		//TODO add bomb count;
		
		Log.d(LOG_TAG, "evaluateScore="+evaluateScore);
		
		if ( evaluateScore > 8){
			return Game.BASIC_SCORE_THREE;
		}
		else if (evaluateScore > 5){
			return Game.BASIC_SCORE_TWO;
		}
		else if (evaluateScore > 3){
			return Game.BASIC_SCORE_ONE;
		}
		else{
			return Game.BASIC_SCORE_NONE;
		}
	}
	
	/**
	 * 返回叫牌信息。
	 * @param cards 手牌列表。
	 * @param minScore 最低分（不包含）。这个分数通常是上一个玩家叫的分数。
	 * @return 返回一个比minScore更大的分数，或者 是{@link Game#BASIC_BASIC_SCORE_NONE}，表示不叫。
	 */
	protected final int callLandlord(ArrayList<Card> cards, int minScore) {
		int alalysis = callLandlord(cards);
		return alalysis > minScore ? alalysis : Game.BASIC_SCORE_NONE;
	}
	
	protected CardType followCards(CardType lastType){
		CardType decideType = null;
		ArrayList<CardType> cardTypes = bindPlayer.cardsInfo.cardTypes;
		if (lastType==null){
			//出第一手牌，从最小的打起
			decideType = (cardTypes.get(0));
			//如果是三条，则考虑是否带牌
			if (decideType instanceof Three){
				optimizeThreeAttachments((Three) decideType, cardTypes);
			}
		}
		else{
			//先判断是否需要跟牌
			boolean needToForce = needToFollow(lastType);
			if (lastType instanceof Single){
				decideType = followSingle((Single) lastType, needToForce);
			}
			else if (lastType instanceof Pair){
				decideType = followPair((Pair) lastType, needToForce);
			}
			else if (lastType instanceof Three){
				decideType = followThree((Three) lastType, needToForce);
			}
			else if (lastType instanceof Straight){
				decideType = followStraights((Straight) lastType, needToForce);
			}
			else if (lastType instanceof DoubleStraight){
				decideType = followDoubleStraights((DoubleStraight)lastType, needToForce);
			}
			else {
				for (CardType type : cardTypes) {
					if (type.canAgainstType(lastType)) {
						decideType = type;
						break;
					}
				}
			}
		}
		return decideType;
	}
	
	private CardType findBombType(PlayerCardsInfo info, NonBombType followType){
		ArrayList<CardType> cardTypes = info.cardTypes;
		if (info.bombCount > 0) {
			for (CardType type : cardTypes) {
				if (type instanceof BombType) {
					return type;
				}
			}
		}
		return null;
	}

	/**
	 * 处理顺子的跟牌策略。
	 */
	private CardType followStraights(Straight followType, boolean needToForce){
		PlayerCardsInfo info = bindPlayer.cardsInfo;
		ArrayList<CardType> cardTypes = info.cardTypes;
		// 如果有现成的单牌且比原来的大，则返回
		for (CardType type : cardTypes) {
			if (type instanceof Straight && type.canAgainstType(followType)) {
				return type;
			}
		}
		// 没有，则找炸弹
		CardType bomb = findBombType(info, followType);
		if (bomb!=null){
			return bomb;
		}
		//如果不强制跟牌，则到此为止
		if (!needToForce){
			return null;
		}
		//拆牌跟
		ArrayList<Straight> list = StraightAnalyst.forceGetStraights(
				followType, bindPlayer.getHandCards());
		return list==null || list.isEmpty()? null : list.get(0);
	}
	
	/**
	 * 处理连对的跟牌策略。
	 */
	private CardType followDoubleStraights(DoubleStraight followType, boolean needToForce){
		Log.d(LOG_TAG, "followDoubleStraights:"+needToForce);
		PlayerCardsInfo info = bindPlayer.cardsInfo;
		ArrayList<CardType> cardTypes = info.cardTypes;
		// 如果有现成的单牌且比原来的大，则返回
		for (CardType type : cardTypes) {
			if (type instanceof DoubleStraight && type.canAgainstType(followType)) {
				return type;
			}
		}
		// 没有，则找炸弹
		CardType bomb = findBombType(info, followType);
		if (bomb!=null){
			return bomb;
		}
		//如果不强制跟牌，则到此为止
		if (!needToForce){
			return null;
		}
		Log.d(LOG_TAG, "force :"+bindPlayer.getHandCards());
		//拆牌跟
		ArrayList<DoubleStraight> list = StraightAnalyst.forceGetDoubleStraights(
				followType, bindPlayer.getHandCards());
		return list==null || list.isEmpty()? null : list.get(0);
	}
	
	/**
	 * 处理单牌的跟牌方案。
	 * 
	 * @param followType
	 *            需要被跟的牌
	 * @param needToForce
	 *            是否需要拆牌（强制跟牌）
	 * @return 如果有牌可出，则返回这个牌型对象，否则返回null。注意：返回的不一定是Single对象，还可能是炸弹。
	 */
	private CardType followSingle(Single followType, boolean needToForce) {
		Card followCard = followType.getCardList().get(0); // 取出要跟的牌，便于比较
		PlayerCardsInfo info = bindPlayer.cardsInfo;
		ArrayList<CardType> cardTypes = info.cardTypes;
		if (cardTypes == null) {
			return null;
		}
		// 如果有现成的单牌且比原来的大，则返回
		for (CardType type : cardTypes) {
			if (type instanceof Single && type.compareTo(followType) > 0) {
				return type;
			}
		}
		// 没有，则拆2.注意：如果有王炸则不拆，而如果没有王炸，其必定是单牌，已在前面出过。
		if (info.twoAndJokerCount > 0) {
			for (Card card : bindPlayer.getHandCards()) {
				if (card.getValue() == Card.CARD_VALUE_2
						&& card.compareTo(followCard) > 0) {
					return new Single(card);
				}
			}
		}
		// 没有，则找炸弹
		CardType bomb = findBombType(info, followType);
		if (bomb!=null){
			return bomb;
		}
		// 如果不是强制跟牌，则到此为止
		if (!needToForce) {
			return null;
		}
		// 否则拆顺子的顶牌, 5张以下顺子不拆
		for (CardType type : cardTypes) {
			if (type instanceof Straight) {
				int length = ((Straight) type).length;
				if (length <= 5) {
					continue;
				}
				Card lastCard = type.getCardList().get(length - 1);
				if (lastCard.compareTo(followCard) > 0) {
					return new Single(lastCard);
				}
			}
		}
		// 否则拆三条
		for (CardType type : cardTypes) {
			if (type instanceof Three) {
				// important:只能拆BodyList，而不能拆其带的牌
				Card card = ((Three) type).getBodyList().get(0);
				if (card.compareTo(followCard) > 0) {
					return new Single(card);
				}
			}
		}
		//如果仅有两张牌且为对子，则拆牌单出（如果不是对子则必然在前面已经单出）
		if (cardTypes.size()==1){
			Card card = bindPlayer.getHandCards().get(0);
			if (card.compareTo(followCard)>0){
				return new Single(card);
			}
		}
		// 无牌可出
		return null;
	}

	/**
	 * 处理对子的跟牌方案。
	 * 
	 * @param followType
	 *            需要被跟的牌
	 * @param needToForce
	 *            是否需要拆牌（强制跟牌）
	 * @return 如果有牌可出，则返回这个牌型对象，否则返回null。注意：返回的不一定是Pair对象，还可能是炸弹。
	 */
	private CardType followPair(Pair followType, boolean needToForce) {
		Card followCard = followType.getCardList().get(0); // 取出要跟的牌，便于比较
		PlayerCardsInfo info = bindPlayer.cardsInfo;
		ArrayList<CardType> cardTypes = info.cardTypes;
		if (cardTypes == null) {
			return null;
		}
		// 如果有现成的对子且比原来的大，则返回
		for (CardType type : cardTypes) {
			if (type instanceof Pair && type.compareTo(followType) > 0) {
				return type;
			}
		}
		// 没有，则拆2.注意：王炸不拆
		if (info.twoAndJokerCount > 1) {
			ArrayList<Card> cards = takeoutCards(new int[] { Card.CARD_VALUE_2,
					Card.CARD_VALUE_2 }, bindPlayer.getHandCards());
			if (cards != null) {
				return new Pair(cards);
			}
		}
		// 没有，则找炸弹
		CardType bomb = findBombType(info, followType);
		if (bomb!=null){
			return bomb;
		}
		// 如果不是强制跟牌，则到此为止
		if (!needToForce) {
			return null;
		}
		// 否则拆连对，暂略
		// 否则拆三条
		for (CardType type : cardTypes) {
			if (type instanceof Three) {
				Card card = ((Three) type).getBodyList().get(0);
				//仅比较点数
				if (card.compareTo(followCard) > 0) {
					return new Pair(new ArrayList<Card>(((Three) type)
							.getBodyList().subList(0, 2)));
				}
			}
		}
		// 还是没有牌，不出
		return null;
	}

	/**
	 * 处理三条的跟牌方案。
	 * 
	 * @param followType
	 *            需要被跟的牌
	 * @param needToForce
	 *            是否需要拆牌（强制跟牌）
	 * @return 如果有牌可出，则返回这个牌型对象，否则返回null。注意：返回的不一定是Three对象，还可能是炸弹。
	 */
	private CardType followThree(Three followType, boolean needToForce) {
		PlayerCardsInfo info = bindPlayer.cardsInfo;
		ArrayList<CardType> cardTypes = info.cardTypes;
		// 先确定要带的牌，避免在循环中处理
		CardType attachType = null;
		boolean hasRightAttachment = false;
		if (followType.getAttachType() instanceof Single) {
			// This is a hack. we just create a "least" card and follow it.
			attachType = followSingle(new Single(new Card(CardSuit.Spade,
					Card.CARD_VALUE_3)), needToForce);
			if ( (attachType!=null) && (attachType instanceof Single)) {
				hasRightAttachment = true;
			}
		} else if (followType.getAttachType() instanceof Pair) {
			attachType = followPair(
					new Pair(LangUtils.createList(new Card(CardSuit.Spade,
							Card.CARD_VALUE_3), new Card(CardSuit.Spade,
							Card.CARD_VALUE_3))), needToForce);
			if ( (attachType!=null) && (attachType instanceof Pair)) {
				hasRightAttachment = true;
			}
		}
		Log.v(LOG_TAG, "follow:"+followType+", hasAttach:"+hasRightAttachment+","+attachType);
		if (hasRightAttachment) {
			for (CardType type : cardTypes) {
				if (type instanceof Three && type.compareTo(followType)>0) {
					Three three = ((Three) type);
					//避免出现自己带自己的Bug
					if ( attachType!=null && three.getBodyList().get(0)
							.isSameValueAs(attachType.getCardList().get(0))){
						continue;
					}
					three.setAttachType(attachType);
					return three;
				}
			}
		}
		// 没有牌可以带，则找炸弹
		CardType bomb = findBombType(info, followType);
		if (bomb!=null){
			return bomb;
		}
		// 还是无牌，则不出
		return null;
	}
	
	//出牌前检查三条是否能带上单或对
	protected void optimizeThreeAttachments(Three three,
			ArrayList<CardType> cardTypes) {
		for (CardType tp : cardTypes) {
			// XXX to be optimized: 2以上不带
			if (tp instanceof Single
					&& tp.getCardList().get(0).getValue() < Card.CARD_VALUE_2) {
				three.setAttachType(tp);
				break;
			} else if (tp instanceof Pair
					&& tp.getCardList().get(0).getValue() < Card.CARD_VALUE_2) {
				three.setAttachType(tp);
				break;
			}
			// 否则不带
		}
	}
	
	/**
	 * 
	 * 按照指定的pattern尝试组合出目标牌列表。 
	 * 例如传入参数为[3,3,3]，则将尝试找出三张点数为3的卡牌，并把这些按顺序加入一个列表然后返回。
	 * <p>
	 * 注意： <ul>
	 * <li>该方法的实现假定list是升序排列的。</li> </ul>
	 * </p>
	 * 
	 * @param targetPattern 目标数值模式。
	 * @param list 当前剩余卡牌列表。
	 * @return 如果能找出这样的卡牌，则返回该列表；否则返回 null。
	 */
	protected ArrayList<Card> takeoutCards(int[] targetPattern, ArrayList<Card> list){
		if (targetPattern==null || list==null){
			Log.w(LOG_TAG, "takecards return null due to null param.");
			return null;
		}
		int patternLength = targetPattern.length;
		int cardLength = list.size();
		if (patternLength > cardLength){
			Log.d(LOG_TAG, "takecards return null due to no enough length.");
			return null;
		}
		ArrayList<Card> targetList = new ArrayList<>();
		//创建临时复制数组，然后从中迭代遍历，如果匹配则加入目标列表，并从临时复制数组中删除迭代的元素。
		//最后检查结果，如果目标列表长度合适则返回，否则返回null。
		ArrayList<Card> internalTempList = new ArrayList<>(list);
		Iterator<Card> iterator = internalTempList.iterator();
		int matchIndex = 0;
		while (iterator.hasNext()){
			Card card = iterator.next();
			if (card.getValue()== targetPattern[matchIndex]){
				targetList.add(card);
				iterator.remove();
				matchIndex++;
				//如果已经到了要出牌的张数，则终止循环
				if (matchIndex == targetPattern.length){
					break;
				}
			}
		}
		//手动清除列表内容，方便GC
		internalTempList.clear();
		if (targetList.size() != targetPattern.length){
			targetList.clear();
			Log.v(LOG_TAG, "takecards: pattern="+Arrays.toString(targetPattern)+ ", not found");
			return null;
		}
		Log.d(LOG_TAG, "takecards: pattern="+Arrays.toString(targetPattern)+ ", res="+targetList.toString());
		return targetList;
	}
	
	/**
	 * 将手牌按照一般原则进行组合。
	 * @param list 手牌列表
	 * @return 返回封装后的 PlayerCardsInfo 对象，其中的 cardTypes字段保证不为null且已进行过排序。
	 */
	protected PlayerCardsInfo makeCards(final List<Card> list){
		if (list==null || list.size()==0){
			return null;
		}
		Log.d(LOG_TAG, "cards before make: "+list.toString());
		//复制一个列表以便内部操作，避免直接操纵玩家手牌。
		PlayerCardsInfo playerInfo = new PlayerCardsInfo();
		ArrayList<Card> cloneList= new ArrayList<>(list);
		Collections.sort(cloneList, Card.COMPARATOR_WITH_SUIT);
		//判断王炸是否存在。
		ArrayList<Card> rocket = takeoutCards(new int[] {
				Card.CARD_VALUE_JOKER_S, Card.CARD_VALUE_JOKER_B }, cloneList);
		if (rocket!=null){
			playerInfo.cardTypes.add(new Rocket(rocket));
			cloneList.removeAll(rocket);
		}
		//找出所有的炸弹。通常情况下，炸弹也是不会进行拆分处理的。
		//注意：这里使用lastNotFoundValue跳过相同点数的数值，避免无谓的循环。
		ArrayList<Bomb> bombs = new ArrayList<>();
		for (int i = 0, lastNotFoundValue=0, lastFoundValue=0; i < cloneList.size(); i++) {
			int cardValue = cloneList.get(i).getValue();
			if (lastNotFoundValue==cardValue || lastFoundValue==cardValue){
				continue ;
			}
			int[] bombPattern = new int[] { cardValue, cardValue, cardValue,
					cardValue };
			ArrayList<Card> bombCards = takeoutCards(bombPattern, cloneList);
			if (bombCards != null) {
				lastFoundValue = cardValue;
				bombs.add(new Bomb(bombCards));
			}
			else{
				lastNotFoundValue = cardValue;
			}
		}
		for (Bomb bomb: bombs){
			cloneList.removeAll(bomb.getCardList());
			playerInfo.cardTypes.add(bomb);
		}
		//找出所有的顺子
		List<StraightNumbers> numbers = StraightAnalyst.getAllStraights(cloneList);
		ArrayList<Straight> straights = new ArrayList<>();
		for (StraightNumbers num : numbers){
			ArrayList<Card> tempList = takeoutCards(num.asIntegerArray(), cloneList);
			if (tempList != null){
				Straight tempStr = new Straight(tempList);
				straights.add(tempStr);
			}
		}
		for (Straight str:straights){
			playerInfo.cardTypes.add(str);
			cloneList.removeAll(str.getCardList());
		}
		
		//找出所有的三条
		ArrayList<Three> threesWithNoAirplane = new ArrayList<>();
		for (int i = 0, lastNotFoundValue=0, lastFoundValue=0; i < cloneList.size(); i++) {
			int cardValue = cloneList.get(i).getValue();
			if (lastNotFoundValue==cardValue || lastFoundValue==cardValue){
				continue ;
			}
			int[] threePattern = new int[] { cardValue, cardValue, cardValue};
			ArrayList<Card> threes = takeoutCards(threePattern, cloneList);
			if (threes != null) {
				lastFoundValue = cardValue;
				threesWithNoAirplane.add(new Three(threes));
			}
			else{
				lastNotFoundValue = cardValue;
			}
		}
		for (Three thr: threesWithNoAirplane){
			cloneList.removeAll(thr.getCardList());
		}
		//优化为飞机后再加入三条列表
		if (threesWithNoAirplane.size()>2){
			ArrayList<Airplane> planes = makeAirplaneUsingThree(threesWithNoAirplane);
			for (Airplane air:planes){
				playerInfo.cardTypes.add(air);
				threesWithNoAirplane.removeAll(air.getBodyThrees());
			}
		}
		playerInfo.cardTypes.addAll(threesWithNoAirplane);
		
		//找出所有的对子
		ArrayList<Pair> pairsSeparately = new ArrayList<>();
		for (int i = 0, lastFoundValue=0; i < cloneList.size(); i++) {
			int cardValue = cloneList.get(i).getValue();
			if (cardValue==lastFoundValue){
				continue;
			}
			int[] pairPattern = new int[] { cardValue, cardValue};
			ArrayList<Card> pairs = takeoutCards(pairPattern, cloneList);
			if (pairs != null) {
				lastFoundValue = cardValue;
				pairsSeparately.add(new Pair(pairs));
			}
		}
		for (Pair pair: pairsSeparately){
			cloneList.removeAll(pair.getCardList());
		}
		if (pairsSeparately.size()>2){
			ArrayList<DoubleStraight> dbs = makeDoubleStraightUsingPairs(pairsSeparately);
			for (DoubleStraight db: dbs){
				playerInfo.cardTypes.add(db);
			}
		}
		playerInfo.cardTypes.addAll(pairsSeparately);
		
		//剩下的都是单牌
		Iterator<Card> singleIterator = cloneList.iterator();
		while (singleIterator.hasNext()){
			playerInfo.cardTypes.add(new Single(singleIterator.next()));
			singleIterator.remove();
		}
		Collections.sort(playerInfo.cardTypes, CardType.SORT_COMPARATOR);
		statPlayerCardsInfo(playerInfo);
		return playerInfo;
	}
	
	/**
	 * 使用三条组装飞机。注意：如果能组成飞机，则所有组成飞机的三条都将被移除。
	 */
	private ArrayList<Airplane> makeAirplaneUsingThree(ArrayList<Three> threes){
		int lastValue=0;
		ArrayList<Three> tempAirThreeRef = new ArrayList<>();
		ArrayList<Airplane> tempAirplanes = new ArrayList<>();
		ArrayList<Three> toBeRemoved = new ArrayList<>();
		for (Three tr : threes) {
			int thisValue = tr.getBodyList().get(0).getValue();
			//飞机不会包含2以上的数字
			if (thisValue>=Card.CARD_VALUE_2){
				break;
			}
			// 两个或更多三条可以组成飞机
			if (lastValue != 0 && (thisValue - lastValue != 1) && tempAirThreeRef.size() >= 2) {
				ArrayList<Card> cards = new ArrayList<>();
				for (Three airThr : tempAirThreeRef) {
					toBeRemoved.add(airThr);
					cards.addAll(airThr.getCardList());
				}
				tempAirplanes.add(new Airplane(cards));
				lastValue = 0;
				tempAirThreeRef.clear();
			}
			//否则，如果不连续则清空后加入，连续则直接加入
			else{
				if (lastValue!=0 && thisValue-lastValue!=1){
					tempAirThreeRef.clear();
				}
				lastValue = thisValue;
				tempAirThreeRef.add(tr);
			}
		}
		//循环结束进行最后的判断。由于过程中不连续的已经被剔除，所以这里一定是连续的。
		if (tempAirThreeRef.size() >= 2) {
			ArrayList<Card> cards = new ArrayList<>();
			for (Three airThr : tempAirThreeRef) {
				toBeRemoved.add(airThr);
				cards.addAll(airThr.getCardList());
			}
			tempAirplanes.add(new Airplane(cards));
			tempAirThreeRef.clear();
		}
		threes.removeAll(toBeRemoved);
		return tempAirplanes;
	}
	
	/**
	 * 使用对子组装连对。注意：如果能组成连对，则所有组成连对的对子都将被移除。
	 */
	private ArrayList<DoubleStraight> makeDoubleStraightUsingPairs(
			ArrayList<Pair> pairs) {
		int lastValue = 0;
		ArrayList<Pair> tempPairRef = new ArrayList<>();
		ArrayList<DoubleStraight> tempDoubles = new ArrayList<>();
		ArrayList<Pair> toBeRemoved = new ArrayList<>();
		for (Pair tr : pairs) {
			int thisValue = tr.getCardList().get(0).getValue();
			//如果列表中的对子不再连续或遇到2，且能组成连对
			if (lastValue != 0
					&& ((thisValue - lastValue != 1) || thisValue >= Card.CARD_VALUE_2)) {
				// 三个或更多对子可以组成连对
				if (tempPairRef.size() >= 3) {
					ArrayList<Card> cards = new ArrayList<>();
					for (Pair airThr : tempPairRef) {
						toBeRemoved.add(airThr);
						cards.addAll(airThr.getCardList());
					}
					tempDoubles.add(new DoubleStraight(cards));
					tempPairRef.clear();
				}
			}
			//否则，如果不连续则清空，连续则加入
			else if (lastValue!=0 && thisValue-lastValue!=1){
				tempPairRef.clear();
			}
			else{
				tempPairRef.add(tr);
			}
			// 无论是不是，都更新最新的值
			lastValue = thisValue;
		}
		//循环结束进行最后的判断。由于过程中不连续的已经被剔除，所以这里一定是连续的。
		if (tempPairRef.size() >= 3) {
			//判断其中装的是不是一样的
			ArrayList<Card> cards = new ArrayList<>();
			for (Pair airThr : tempPairRef) {
				toBeRemoved.add(airThr);
				cards.addAll(airThr.getCardList());
			}
			tempDoubles.add(new DoubleStraight(cards));
			tempPairRef.clear();
		}
		pairs.removeAll(toBeRemoved);
		return tempDoubles;
	}
	
	//判断是否需要跟牌
	private boolean needToFollow(CardType cardsToFollow){
		//如果自己是地主，则无需考虑，有牌必打
		if (bindPlayer.isLandlord()){
			return true;
		}
		//如果自己是地主的上家, 默认出牌
		if (bindPlayer.isPriorOfLandlord){
			CardType partnerCards = bindPlayer.getPriorPlayer().getLastCards();
			//搭档没有打,则跟牌
			if (partnerCards==null){
				return true;
			}
			//如果搭档跟了牌且牌比较大，则不跟牌
			if ((cardsToFollow instanceof Single 
					&& partnerCards.getCardList().get(0).getValue() >= Card.CARD_VALUE_A)
					|| (cardsToFollow instanceof Pair 
					&& partnerCards.getCardList().get(0).getValue() >= Card.CARD_VALUE_K)) {
				return false;
			}
			//如果跟的牌是炸弹或顺子，不跟牌
			if (partnerCards instanceof BombType
					|| partnerCards instanceof Straight) {
				return false;
			}
			return true;
		}
		//如果自己是地主的下家
		if (bindPlayer.isNextOfLandlord){
			CardType landCards = bindPlayer.getPriorPlayer().getLastCards();
			//如果地主没有出牌，则不出
			if (landCards==null){
				return false;
			}
			//如果是炸弹或顺子，有牌必跟
			if (landCards instanceof BombType || landCards instanceof Straight){
				return true;
			}
			CardType partnerCards = bindPlayer.getNextPlayer().getLastCards();
			//搭档上次没有跟牌，则打出
			if (partnerCards==null){
				return true;
			}
			//如果当前的牌是搭档打出，默认不打
			if (partnerCards == cardsToFollow) {
				//作为地主下家对卡牌的大小可以小一些
				if ((cardsToFollow instanceof Single && partnerCards
						.getCardList().get(0).getValue() < Card.CARD_VALUE_Q)
						|| (cardsToFollow instanceof Pair && partnerCards
								.getCardList().get(0).getValue() < Card.CARD_VALUE_J)) {
					return true;
				}
				return false;
			}
			//如果有独立的无需拆牌就能打的，则打出
			ArrayList<CardType> cardTypes = bindPlayer.cardsInfo.cardTypes;
			for (CardType type: cardTypes){
				if (type.getClass().equals(cardsToFollow)){
					return true;
				}
			}
			return false;
		}
		return false;
	}
	
	private void statPlayerCardsInfo(PlayerCardsInfo info){
		//force init
		info.hasRocket = false;
		info.bombCount = 0;
		info.pairCount = 0;
		info.singleCount = 0;
		info.threeCount = 0;
		info.straightCount=0;
		
		info.expectedRound = info.cardTypes.size();
		
		for (CardType type: info.cardTypes){
			if (type instanceof Rocket){
				info.hasRocket = true;
			}
			if (type instanceof BombType){
				info.bombCount++;
			}
			else if (type instanceof Straight){
				info.straightCount++;
			}
			else if (type instanceof Three){
				info.threeCount++;
			}
			else if (type instanceof Single){
				info.singleCount++;
			}
			else if (type instanceof Pair){
				info.pairCount++;
			}
		}
	}
	
}
