package com.pyz.audiosample.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.dimowner.audiorecorder.app.widget.ChipsView;
import com.dimowner.audiorecorder.app.widget.SettingView;
import com.dimowner.audiorecorder.util.AndroidUtils;
import com.pyz.audiosample.AppConstants;
import com.pyz.audiosample.R;
import com.pyz.audiosample.data.SettingsMapper;
import com.pyz.audiosample.viewmodel.SettingModel;


public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

	private final static String TAG = SettingsActivity.class.getSimpleName();
	private SettingView formatSetting;
	private SettingView sampleRateSetting;
	private SettingView bitrateSetting;
	private SettingView channelsSetting;

	private String[] formats;
	private String[] formatsKeys;
	private String[] sampleRates;
	private String[] sampleRatesKeys;
	private String[] rates;
	private String[] rateKeys;
	private String[] recChannels;
	private String[] recChannelsKeys;

	private SettingModel settingModel;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_settings);
		settingModel = new ViewModelProvider(this,
				new ViewModelProvider.AndroidViewModelFactory(this.getApplication())).get(SettingModel.class);

		findViewById(R.id.btnBack).setOnClickListener(this);
		formatSetting = findViewById(R.id.setting_recording_format);
		formats = getResources().getStringArray(R.array.formats2);
		formatsKeys = new String[]{
//				AppConstants.FORMAT_M4A,
				AppConstants.FORMAT_WAV
//				AppConstants.FORMAT_3GP
		};
		formatSetting.setData(formats, formatsKeys);
		formatSetting.setOnChipCheckListener(new ChipsView.OnCheckListener() {
			@Override
			public void onCheck(String key, String name, boolean checked) {
				Log.d(TAG, "key:" + key + ",name:" + name + ",checked:" + checked);
				settingModel.setSettingFormat(key);
			}
		});
		formatSetting.setTitle(R.string.recording_format);
		formatSetting.setOnInfoClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AndroidUtils.showInfoDialog(SettingsActivity.this, R.string.info_format);
			}
		});

		sampleRateSetting = findViewById(R.id.setting_frequency);
		sampleRates = getResources().getStringArray(R.array.sample_rates2);
		sampleRatesKeys = new String[]{
				SettingsMapper.SAMPLE_RATE_8000,
				SettingsMapper.SAMPLE_RATE_16000,
				SettingsMapper.SAMPLE_RATE_22050,
				SettingsMapper.SAMPLE_RATE_32000,
				SettingsMapper.SAMPLE_RATE_44100,
				SettingsMapper.SAMPLE_RATE_48000,
		};
		sampleRateSetting.setData(sampleRates, sampleRatesKeys);
		sampleRateSetting.setOnChipCheckListener(new ChipsView.OnCheckListener() {
			@Override
			public void onCheck(String key, String name, boolean checked) {
				Log.d(TAG, "key:" + key + ",name:" + name + ",checked:" + checked);
				settingModel.setSettingSampleRate(SettingsMapper.keyToSampleRate(key));
			}
		});
		sampleRateSetting.setTitle(R.string.sample_rate);
		sampleRateSetting.setOnInfoClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AndroidUtils.showInfoDialog(SettingsActivity.this, R.string.info_frequency);
			}
		});

		bitrateSetting = findViewById(R.id.setting_bitrate);
		rates = getResources().getStringArray(R.array.bit_rates2);
		rateKeys = new String[]{
//				SettingsMapper.BITRATE_24000,
				SettingsMapper.BITRATE_48000,
				SettingsMapper.BITRATE_96000,
				SettingsMapper.BITRATE_128000,
				SettingsMapper.BITRATE_192000,
				SettingsMapper.BITRATE_256000,
		};
		bitrateSetting.setData(rates, rateKeys);
		bitrateSetting.setOnChipCheckListener(new ChipsView.OnCheckListener() {
			@Override
			public void onCheck(String key, String name, boolean checked) {
				settingModel.setSettingBitrate(SettingsMapper.keyToBitrate(key));
			}
		});
		bitrateSetting.setTitle(R.string.bitrate);
		bitrateSetting.setOnInfoClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AndroidUtils.showInfoDialog(SettingsActivity.this, R.string.info_bitrate);
			}
		});

		channelsSetting = findViewById(R.id.setting_channels);
		recChannels = getResources().getStringArray(R.array.channels);
		recChannelsKeys = new String[]{
				SettingsMapper.CHANNEL_COUNT_STEREO,
				SettingsMapper.CHANNEL_COUNT_MONO
		};
		channelsSetting.setData(recChannels, recChannelsKeys);
		channelsSetting.setOnChipCheckListener(new ChipsView.OnCheckListener() {
			@Override
			public void onCheck(String key, String name, boolean checked) {
				settingModel.setSettingChannelCount(SettingsMapper.keyToChannelCount(key));
			}
		});
		channelsSetting.setTitle(R.string.channels);
		channelsSetting.setOnInfoClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AndroidUtils.showInfoDialog(SettingsActivity.this, R.string.info_channels);
			}
		});

		formatSetting.setSelected(settingModel.getSettingFormat());
		sampleRateSetting.setSelected(settingModel.getSettingSampleRate());
		bitrateSetting.setSelected(settingModel.getSettingBitrate());
		channelsSetting.setSelected(settingModel.getSettingChannelCount());

		settingModel.getHideBitrate().observe(this, hide ->{
			Log.i(TAG, "hide:" +hide);
			bitrateSetting.setVisibility(hide ? View.GONE : View.VISIBLE);
		});
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnBack:
				finish();
				break;

		}
	}
}
