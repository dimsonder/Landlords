package com.mym.landlords.res;

import java.io.IOException;

import com.mym.util.BitmapUtil;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * 代表已被加载到内存中的Bitmap对象。
 * 通过 重载的scaleTo方法可以将位图尺寸进行重新缩放。
 */
public final class LiveBitmap {
	private Bitmap bitmap;
	private final int rawWidth;
	private final int rawHeight;

	// 强制使用工厂方法
	private LiveBitmap(Bitmap bitmap, int rawWidth, int rawHeight) {
		super();
		this.bitmap = bitmap;
		this.rawWidth = rawWidth;
		this.rawHeight = rawHeight;
	}

	/**
	 * 按原有尺寸加载图片。
	 * @param context 上下文信息。
	 * @param assets 要加载的Bitmap文件名（必须是Assets文件）。
	 * @return 加载得到的位图；如果失败，返回null。
	 */
	protected static final LiveBitmap loadBitmap(Context context, String assets) {
		return loadBitmap(context, assets, 0, 0);
	}

	/**
	 * 按原有尺寸加载图片，再按指定比例缩放。
	 * @param context 上下文信息。
	 * @param assets 要加载的Bitmap文件名（必须是Assets文件）。
	 * @param scalex x缩放比。
	 * @param scaley y缩放比。
	 * @return 加载得到的位图；如果失败，返回null。
	 */
	protected static final LiveBitmap loadBitmap(Context context, String assets,
			float scalex, float scaley) {
		LiveBitmap instance = null;
		AssetManager am = context.getAssets();
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(am.open(assets));
			int rawWidth = bitmap.getWidth();
			int rawHeight = bitmap.getHeight();
			bitmap = BitmapUtil.scaleBitmap(bitmap, scalex, scaley);
			instance = new LiveBitmap(bitmap, rawWidth, rawHeight);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return instance;
	}
	
	/**
	 * 按原有尺寸加载图片，再缩放到指定尺寸。
	 * @param context 上下文信息。
	 * @param assets 要加载的Bitmap文件名（必须是Assets文件）。
	 * @param width 目标宽度。
	 * @param height 目标高度。
	 * @return 加载得到的位图；如果失败，返回null。
	 */
	protected static final LiveBitmap loadBitmap(Context context, String assets,
			int width, int height) {
		LiveBitmap instance = null;
		AssetManager am = context.getAssets();
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(am.open(assets));
			int rawWidth = bitmap.getWidth();
			int rawHeight = bitmap.getHeight();
			if (width != 0 && height != 0) {
				bitmap = BitmapUtil.scaleBitmap(bitmap, width, height);
			}
			instance = new LiveBitmap(bitmap, rawWidth, rawHeight);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return instance;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public int getRawWidth() {
		return rawWidth;
	}

	public int getRawHeight() {
		return rawHeight;
	}
	
	public int getWidth(){
		return bitmap.getWidth();
	}
	
	public int getHeight(){
		return bitmap.getHeight();
	}
	
	/**
	 * 缩放图片到指定比例。
	 * @param scalew 横向缩放比例
	 * @param scaleh 纵向缩放比例
	 */
	public void scaleTo(float scalew, float scaleh){
		bitmap = BitmapUtil.scaleBitmap(bitmap, scalew, scaleh);
	}
	
	/**
	 * 缩放图片到指定尺寸。
	 * @param width 目标宽度
	 * @param height 目标高度
	 */
	public void scaleTo(int width, int height){
		bitmap = BitmapUtil.scaleBitmap(bitmap, width, height);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LiveBitmap [getRawWidth()=").append(getRawWidth())
				.append(", getRawHeight()=").append(getRawHeight())
				.append(", getWidth()=").append(getWidth())
				.append(", getHeight()=").append(getHeight()).append("]");
		return builder.toString();
	}
	
}
