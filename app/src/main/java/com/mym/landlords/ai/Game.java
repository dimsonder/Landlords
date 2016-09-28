package com.mym.landlords.ai;

import java.util.ArrayList;

import com.mym.landlords.card.Card;

/**
 * 代表一盘游戏的实体类，用于记录游戏内的各个变量。
 */
public final class Game {
	
	public static enum Status{
		/** 准备（发牌）阶段 */
		Preparing,
		/** 叫地主阶段 */
		CallingLandlord,
		/** 进行阶段 */
		Playing,
		/** 牌局结束展示AI手牌 */
		ShowingAICards,
		/** 游戏结束阶段 */
		Gameover;
	}
	/** 如取此值，表示不叫。 */
	public static final int BASIC_SCORE_NONE = 0;
	public static final int BASIC_SCORE_ONE = 1;
	public static final int BASIC_SCORE_TWO = 2;
	public static final int BASIC_SCORE_THREE = 3;
	
	/** 炸弹总数 */
	public int boomCount;	
	/** 基础积分。 */
	public int basicScore;
	/** 当前状态 */
	public Status status;
	/** 地主玩家 */
	public Player landlordPlayer;
	
	public ArrayList<Card> playerLeftCards = new ArrayList<>();		//记牌器功能
	public ArrayList<Card> playerHumanCards = new ArrayList<>();	//记牌器功能
	public ArrayList<Card> playerRightCards = new ArrayList<>();	//记牌器功能
	
	private static Game instance = new Game();
	
	//隐藏构造方法
	private Game(){}
	
	/**
	 * 开始一局新游戏。注意：前一局的所有记录将丢失。
	 * @return 返回一个初始化后的游戏变量。
	 */
	public static Game newGame(){
		//采用享元模式设计，始终使用同一个对象，降低内存消耗。
		instance.basicScore = 0;
		instance.boomCount =0;
		instance.status = Status.Preparing;
		instance.landlordPlayer = null;
		instance.playerHumanCards.clear();
		instance.playerLeftCards.clear();
		instance.playerRightCards.clear();
		return instance;
	}
}
