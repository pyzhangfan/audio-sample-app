package com.pyz.audiosample.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pyz.audiosample.AppConstants;
import com.pyz.audiosample.data.PrefsImpl;
import com.pyz.audiosample.data.SettingsMapper;

public class SettingModel extends AndroidViewModel {
	private MutableLiveData<Boolean> hideBitrate = null;

	private PrefsImpl prefs;
	public SettingModel(Application application){
		super(application);
		prefs = PrefsImpl.getInstance(application);
	}

	public LiveData<Boolean> getHideBitrate() {
		if(hideBitrate == null){
			hideBitrate = new MutableLiveData<Boolean>();
			String formatKey = prefs.getSettingFormat();
			if(formatKey.equals(AppConstants.FORMAT_WAV) || formatKey.equals(AppConstants.FORMAT_3GP)){
				hideBitrate.postValue(true);
			}else {
				hideBitrate.postValue(false);
			}
		}
		return hideBitrate;
	}

	public String getSettingFormat(){
		return prefs.getSettingFormat();
	}

	public void setSettingFormat(String formatKey) {
		prefs.setSettingFormat(formatKey);
		if(formatKey.equals(AppConstants.FORMAT_WAV) || formatKey.equals(AppConstants.FORMAT_3GP)){
			hideBitrate.postValue(true);
		}else {
			hideBitrate.postValue(false);
		}
	}

	public String getSettingSampleRate(){
		return SettingsMapper.sampleRateToKey(prefs.getSettingSampleRate());
	}

	public void setSettingSampleRate(int rate) {
		prefs.setSettingSampleRate(rate);
	}

	public String getSettingBitrate(){
		return SettingsMapper.bitrateToKey(prefs.getSettingBitrate());
	}

	public void setSettingBitrate(int bitrate) {
		prefs.setSettingBitrate(bitrate);
	}

	public String getSettingChannelCount(){
		return SettingsMapper.channelCountToKey(prefs.getSettingChannelCount());
	}

	public void setSettingChannelCount(int count) {
		prefs.setSettingChannelCount(count);
	}



}