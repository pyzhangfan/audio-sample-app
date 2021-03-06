package com.pyz.audiosample;

import android.app.Application;

import com.pyz.audiosample.data.FileRepositoryImpl;
import com.pyz.audiosample.data.PrefsImpl;

public class RecordApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		PrefsImpl prefs = PrefsImpl.getInstance(this);
		FileRepositoryImpl.getInstance(this, prefs);
	}
}
