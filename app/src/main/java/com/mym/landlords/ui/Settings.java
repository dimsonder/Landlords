package com.mym.landlords.ui;

/**
 * @author cherubim
 */
public final class Settings {
	private static boolean isVoiceEnabled;

	public static boolean isVoiceEnabled() {
		return isVoiceEnabled;
	}

	public static void setVoiceEnabled(boolean isEnabled) {
		isVoiceEnabled = isEnabled;
	}
}
