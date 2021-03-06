package com.pyz.audiosample.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class RecordVMFactory implements ViewModelProvider.Factory{

	@NonNull
	@Override
	public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
		RecordViewModel model = new RecordViewModel();
		model.init();
		return (T) model;
	}
}
