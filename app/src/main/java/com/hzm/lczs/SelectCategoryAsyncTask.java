package com.hzm.lczs;

import android.os.AsyncTask;

public class SelectCategoryAsyncTask extends AsyncTask<Void, Void, Void> {
	SettingAddOrEditAccountActivity activity;
	
	public SelectCategoryAsyncTask(SettingAddOrEditAccountActivity activity)
	{
		this.activity = activity;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		return null;
	}

}
