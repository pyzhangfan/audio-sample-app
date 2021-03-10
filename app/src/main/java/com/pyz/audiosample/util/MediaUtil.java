package com.pyz.audiosample.util;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.util.Log;

import java.util.Arrays;

public class MediaUtil {
	private final static String TAG = "MediaUtil";

	private MediaUtil() {
	}

	public static MediaCodecInfo getEncoderCodecInfo(String mimeType) {
		MediaCodecInfo[] mediaCodecInfos =
				new MediaCodecList(MediaCodecList.REGULAR_CODECS).getCodecInfos();
		for (MediaCodecInfo codecInfo : mediaCodecInfos) {
			Log.i(TAG, "info name:" + codecInfo.getName());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				Log.i(TAG, "hardware:" + codecInfo.isHardwareAccelerated() +
						",software:" + codecInfo.isSoftwareOnly() + ",vendor:" + codecInfo.isVendor() +
						",alias:" + codecInfo.isAlias());
			}
			if (!codecInfo.isEncoder())
				continue;
			String[] types = codecInfo.getSupportedTypes();
			Log.i(TAG, "support types:" + Arrays.toString(types));
			for (int j = 0; j < types.length; j++) {
				if (types[j].equalsIgnoreCase(mimeType)) {
					return codecInfo;
				}
			}
		}
		return null;
	}
}
