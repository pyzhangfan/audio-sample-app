package com.pyz.audiosample.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class DateRecordState {

	public final static int STATE_STOP = 0;
	public final static int STATE_START = 1;
	public final static int STATE_PAUSE = 2;
	public final static int STATE_ERROR = 3;
	private MutableLiveData<Integer> recordingState;
	private MutableLiveData<Long> recordingDuration;

	private DateRecordState(){
	}

	public static DateRecordState getInstance(){
		return DateRecordStateHolder.instance;
	}

	private static class DateRecordStateHolder{
		private final static DateRecordState instance = new DateRecordState();
	}

	public void setRecordingState(int state) {
		recordingState.postValue(state);
	}

	public void setRecordingDuration(long duration) {
		recordingDuration.postValue(duration);
	}

	public LiveData<Integer> getRecordingState() {
		if (recordingState == null) {
			recordingState = new MutableLiveData<Integer>(0);
		}
		return recordingState;
	}

	public LiveData<Long> getRecordingDuration(){
		if (recordingDuration == null) {
			recordingDuration = new MutableLiveData<Long>(0l);
		}
		return recordingDuration;
	}

}
