package com.pyz.audiosample.record;

import java.io.File;

public interface RecorderInterface {

	interface StateListener {
		void onPrepareRecord();
		void onStartRecord(File output);
		void onPauseRecord();
		void onRecordProgress(long mills, int amp);
		void onStopRecord(File output);
		void onError(String str);
	}

	interface Recorder{
		void setStateListener(StateListener stateListener);
		void prepare(String outputFile, int channelCount, int sampleRate, int bitrate);
		void startRecording();
		void pauseRecording();
		void stopRecording();
		boolean isRecording();
		boolean isPaused();
	}
}
