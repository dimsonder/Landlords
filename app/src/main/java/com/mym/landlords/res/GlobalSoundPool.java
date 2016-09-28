package com.mym.landlords.res;

import java.io.IOException;

import com.mym.landlords.ui.Settings;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 *
 */
public final class GlobalSoundPool {

    private static GlobalSoundPool instance;
    private SoundPool soundPool;

    private GlobalSoundPool(Context context) {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    }

    public static GlobalSoundPool getInstance(Context context) {
        if (instance == null) {
            synchronized (GlobalSoundPool.class) {
                instance = new GlobalSoundPool(context);
            }
        }
        return instance;
    }

    /**
     * 加载音效。
     *
     * @param context 上下文信息。
     * @param assets  要加载的音效文件名
     * @return 加载后得到的soundId，可用于播放。
     */
    protected final int loadSound(Context context, String assets) {
        try {
            AssetManager am = context.getAssets();
            AssetFileDescriptor afd = am.openFd(assets);
            return soundPool.load(afd, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 播放指定的音效。
     *
     * @param soundId 通过load方法加载得到的soundId。
     */
    public void playSound(int soundId) {
        if (Settings.isVoiceEnabled()) {
            soundPool.play(soundId, 0.5F, 0.5F, 0, 0, 1.0F);
            //	该方法的第一个参数指定播放哪个声音；leftVolume、rightVolume指定左、右的音量：priority指定播放声音的优先级
            // 数值越大，优先级越高；loop指定是否循环，0为不循环，-1为循环；rate指定播放的比率，数值可从0.5到2， 1为正常比率。
        }
    }
}
