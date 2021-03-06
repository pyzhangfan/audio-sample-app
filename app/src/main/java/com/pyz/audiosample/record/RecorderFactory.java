package com.pyz.audiosample.record;

import com.pyz.audiosample.AppConstants;

public class RecorderFactory {

	public static RecorderInterface.Recorder getRecorder(String type) {
		switch (type) {
			case AppConstants.FORMAT_WAV:
				return WavRecorder.getInstance();
			case AppConstants.FORMAT_AAC:
				return AacRecorder.getInstance();
			default:
				return null;
		}
	}
}
