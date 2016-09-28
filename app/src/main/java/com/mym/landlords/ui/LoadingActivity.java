package com.mym.landlords.ui;

import java.lang.ref.WeakReference;

import com.mym.landlords.R;
import com.mym.landlords.res.Assets;
import com.mym.landlords.res.Assets.LoadingProgressListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

public class LoadingActivity extends Activity {
	ImageButton about;

	protected static final String LOG_TAG = "LoadingActivity";
	SharedPreferences sharedPreferences;//第一次安装界面显示进度准备进度条（默认1，安装后为0）

	private ProgressBar progressBar;
	
	private AsyncTask<Void, Integer, Void> loadTask = new AsyncTask<Void, Integer, Void>() {
		protected Void doInBackground(Void... params) {
			Assets.loadAssets(LoadingActivity.this, new LoadingProgressListener() {

				@Override
				public void onProgressChanged(int progress) {
					publishProgress(progress);
				}

				@Override
				public void onLoadCompleted() {
					//do nothing.在onPostExecute中执行操作。
				}
			});
			return null;
		};
		
		protected void onProgressUpdate(Integer... values) {
			progressBar.setProgress((int)values[0]);
		};
		
		protected void onPostExecute(Void result) {
			Log.d(LOG_TAG, "loading completed.");
			//隐藏进度条，并将按钮组展示出来。
			progressBar.setVisibility(View.INVISIBLE);
			findViewById(R.id.loading_ll_btns).setVisibility(View.VISIBLE);
		};
	};

	private Handler handler = new LoadHandler(new WeakReference<LoadingActivity>(this));
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);
		sharedPreferences=LoadingActivity.this.getSharedPreferences("State",MODE_PRIVATE);
		SharedPreferences.Editor editor=sharedPreferences.edit();//编辑文件
		int i=sharedPreferences.getInt("time",1);

			progressBar = (ProgressBar) findViewById(R.id.loading_prg);
		if (i==0){
			progressBar.setVisibility(View.INVISIBLE);
		}
			ToggleButton tgb = (ToggleButton) findViewById(R.id.loading_tgb_voice);
			tgb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Settings.setVoiceEnabled(isChecked);
				}
			});
			Settings.setVoiceEnabled(tgb.isChecked());
		if (i==1) {
			handler.sendEmptyMessageDelayed(LoadHandler.MSG_START_LOADING, 1000);//Splash显示时间

			editor.putInt("time", 0);
			editor.commit();
		}
		else {

			handler.sendEmptyMessageDelayed(LoadHandler.MSG_START_LOADING,1);
		}

		 about=(ImageButton)findViewById(R.id.about);
	}
	
	private static final class LoadHandler extends Handler{
		private static final int MSG_START_LOADING = 1;
		private WeakReference<LoadingActivity> wk;

		public LoadHandler(WeakReference<LoadingActivity> wk) {
			super();
			this.wk = wk;
		}
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			LoadingActivity activity = wk.get();
			if (msg.what==MSG_START_LOADING && activity!=null){
				activity.loadTask.execute();
			}
		}
	}
	
	public void onClick(View v){
		switch (v.getId()) {
		case R.id.loading_btn_startgame:
			Intent intent_game=new Intent(LoadingActivity.this,MainActivity.class);
			startActivity(intent_game);
			finish();
			break;
			case R.id.about:
				Intent intent_about=new Intent(LoadingActivity.this,AboutActivity.class);
				startActivity(intent_about);
				finish();
				break;
		//联机对战，暂时未实现，预留！！！！
		//case R.id.bluetooth_game:

			//break;
		default:
			break;
		}
	}
}
