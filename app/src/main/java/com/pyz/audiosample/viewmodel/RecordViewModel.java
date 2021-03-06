package com.pyz.audiosample.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.pyz.audiosample.data.DateRecordState;
import com.pyz.audiosample.record.RecorderFactory;
import com.pyz.audiosample.record.RecorderManager;

public class RecordViewModel extends ViewModel {

	private RecorderManager recorderManager;
	private DateRecordState dateRecordState;

	public void init() {
		dateRecordState = DateRecordState.getInstance();
		recorderManager = RecorderManager.getInstance();
		recorderManager.setRecorder(RecorderFactory.getRecorder("wav"));
	}

	public LiveData<Integer> getRecording() {
		return dateRecordState.getRecordingState();
	}

	public LiveData<Long> getRecordingDuration() {
		return dateRecordState.getRecordingDuration();
	}

	public void record(){
		recorderManager.startRecording();
	}

	public void stopRecord(){
		recorderManager.stopRecording();
	}

}