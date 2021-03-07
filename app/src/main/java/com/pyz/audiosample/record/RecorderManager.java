package com.pyz.audiosample.record;

import com.pyz.audiosample.data.DateRecordState;
import com.pyz.audiosample.data.FileRepositoryImpl;
import com.pyz.audiosample.data.PrefsImpl;

import java.io.File;

public class RecorderManager {
	private final static String TAG = RecorderManager.class.getSimpleName();
	private PrefsImpl prefs;
	private RecorderInterface.Recorder audioRecorder;
	private RecorderInterface.StateListener stateListener;

	public static RecorderManager getInstance() {
		return RecorderManagerHolder.instance;
	}

	private static class RecorderManagerHolder {
		private final static RecorderManager instance = new RecorderManager();
	}

	private RecorderManager() {
		prefs = PrefsImpl.getInstance(null);
		stateListener = new RecorderInterface.StateListener() {
			@Override
			public void onPrepareRecord() {
				audioRecorder.startRecording();
			}

			@Override
			public void onStartRecord(File output) {
				DateRecordState.getInstance().setRecordingState(DateRecordState.STATE_START);
			}

			@Override
			public void onPauseRecord() {
				DateRecordState.getInstance().setRecordingState(DateRecordState.STATE_PAUSE);
			}

			@Override
			public void onRecordProgress(long mills, int amp) {
				DateRecordState.getInstance().setRecordingDuration(mills);
			}

			@Override
			public void onStopRecord(File output) {
				DateRecordState.getInstance().setRecordingState(DateRecordState.STATE_STOP);
			}

			@Override
			public void onError(String str) {
				DateRecordState.getInstance().setRecordingState(DateRecordState.STATE_ERROR);
			}
		};
	}

	public void setRecorder(RecorderInterface.Recorder recorder) {
		audioRecorder = recorder;
		audioRecorder.setStateListener(stateListener);
	}

	public void startRecording() {
		if (!audioRecorder.isRecording()) {
			audioRecorder.prepare(FileRepositoryImpl.getInstance(null, null).getRecordFileName(),
					prefs.getSettingChannelCount(), prefs.getSettingSampleRate(),
					prefs.getSettingBitrate());
		} else if (!audioRecorder.isPaused()) {
			audioRecorder.pauseRecording();
		} else {
			audioRecorder.startRecording();
		}
	}

	public void stopRecording() {
		if (audioRecorder.isRecording()) {
			audioRecorder.stopRecording();
		}
	}
}
