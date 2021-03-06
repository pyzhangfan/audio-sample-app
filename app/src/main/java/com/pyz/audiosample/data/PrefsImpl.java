package com.pyz.audiosample.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.pyz.audiosample.AppConstants;

public class PrefsImpl {

	private static final String PREF_NAME = "record_pref";
	private static final String KEY_FORMAT = "format";
	private static final String KEY_BITRATE = "bitrate";
	private static final String KEY_SAMPLE_RATE = "sample_rate";
	private static final String KEY_CHANNEL_COUNT = "channel_count";

	private SharedPreferences sharedPreferences;

	private volatile static PrefsImpl instance;

	private PrefsImpl(Context context) {
		sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
	}

	public static PrefsImpl getInstance(Context context) {
		if (instance == null) {
			synchronized (PrefsImpl.class) {
				if (instance == null) {
					instance = new PrefsImpl(context);
				}
			}
		}
		return instance;
	}

	public int getFormat() {
		return sharedPreferences.getInt(KEY_FORMAT, AppConstants.RECORDING_FORMAT_M4A);
	}

	public int getBitrate() {
		return sharedPreferences.getInt(KEY_BITRATE, AppConstants.RECORD_ENCODING_BITRATE_128000);
	}

	public int getSampleRate() {
		return sharedPreferences.getInt(KEY_SAMPLE_RATE, AppConstants.RECORD_SAMPLE_RATE_44100);
	}

	public int getChannelCount() {
		return sharedPreferences.getInt(KEY_CHANNEL_COUNT, AppConstants.DEFAULT_CHANNEL_COUNT);
	}
}
