package com.pyz.audiosample.data;

import android.content.Context;

import com.pyz.audiosample.AppConstants;
import com.pyz.audiosample.util.FileUtil;

import java.io.File;

public class FileRepositoryImpl {
	private final static String TAG = FileRepositoryImpl.class.getSimpleName();
	private volatile static FileRepositoryImpl instance;

	private File recordDirectory;
	private PrefsImpl prefs;

	private FileRepositoryImpl(Context context, PrefsImpl prefs) {
		recordDirectory = FileUtil.getPrivateMusicStorageDir(context, AppConstants.RECORDS_DIR);
		this.prefs = prefs;
	}

	public static FileRepositoryImpl getInstance(Context context, PrefsImpl prefs) {
		if (instance == null) {
			synchronized (FileRepositoryImpl.class) {
				if (instance == null) {
					instance = new FileRepositoryImpl(context, prefs);
				}
			}
		}
		return instance;
	}

	public String getRecordFileName(String format){
		return recordDirectory.getAbsolutePath() + File.separator + "Audio-1." + format;
	}
}
