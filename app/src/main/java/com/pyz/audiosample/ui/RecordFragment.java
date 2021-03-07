package com.pyz.audiosample.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pyz.audiosample.R;
import com.pyz.audiosample.util.TimeUtil;
import com.pyz.audiosample.viewmodel.RecordVMFactory;
import com.pyz.audiosample.viewmodel.RecordViewModel;

public class RecordFragment extends Fragment implements View.OnClickListener {

	private static final String TAG = RecordFragment.class.getSimpleName();
	private RecordViewModel recordViewModel;

	private TextView txtTime;
	private TextView txtName;
	private TextView txtRecordInfo;
	private ImageButton btnSetting;
	private ImageButton btnRecordDelete;
	private ImageButton btnRecord;
	private ImageButton btnRecordStop;
	private ImageButton btnRecordsList;

	public static RecordFragment newInstance() {
		return new RecordFragment();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main_fragment, container, false);
		initView(view);
		return view;
	}

	private void initView(View view){
		txtTime = view.findViewById(R.id.txt_time);
		txtName = view.findViewById(R.id.txt_name);
		txtRecordInfo = view.findViewById(R.id.txt_record_info);
		btnSetting = view.findViewById(R.id.btn_settings);
		btnSetting.setOnClickListener(this);
		btnRecordDelete = view.findViewById(R.id.btn_record_delete);
		btnRecordDelete.setOnClickListener(this);
		btnRecord = view.findViewById(R.id.btn_record);
		btnRecord.setOnClickListener(this);
		btnRecordStop = view.findViewById(R.id.btn_record_stop);
		btnRecordStop.setOnClickListener(this);
		btnRecordsList = view.findViewById(R.id.btn_records_list);
		btnRecordsList.setOnClickListener(this);
	}
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
//		recordViewModel = new ViewModelProvider(this).get(RecordViewModel.class);
		recordViewModel = new RecordVMFactory().create(RecordViewModel.class);
		recordViewModel.getRecording().observe(this, state ->{
			if(state == 0){
				btnRecordStop.setVisibility(View.INVISIBLE);
				btnRecord.setImageResource(R.drawable.ic_record);
			}else if(state == 1){
				btnRecordStop.setVisibility(View.VISIBLE);
				btnRecord.setImageResource(R.drawable.ic_pause);
			}else if(state == 2){
				btnRecordStop.setVisibility(View.VISIBLE);
				btnRecord.setImageResource(R.drawable.ic_record_rec);
			}
		});
		recordViewModel.getRecordingDuration().observe(this, duration ->{
			Log.i(TAG, "duration:" +duration);
			txtTime.setText(TimeUtil.getDuration(duration));
		});
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.btn_settings:
				startActivity(new Intent(getActivity(), SettingsActivity.class));
				break;
			case R.id.btn_record:
				recordViewModel.record();
				break;
			case R.id.btn_record_stop:
				recordViewModel.stopRecord();
				break;
			default:
				break;
		}
	}

}