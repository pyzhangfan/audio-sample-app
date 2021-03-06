package com.pyz.audiosample.record;

public class RecorderFactory {

	public static RecorderInterface.Recorder getRecorder(String type){
		switch (type){
			case "wav":
				return WavRecorder.getInstance();
			default:
				return null;
		}
	}
}
