package com.mym.landlords.widget;

import android.graphics.Canvas;

import com.mym.landlords.res.GameGraphics;

public interface GameScreen {
	/** 用于在界面重绘时展示本界面的必要元素。 */
	void updateUI(GameGraphics graphics, Canvas canvas);
}
