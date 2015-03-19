package com.happyplayer.ui;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.happyplayer.async.AsyncTaskHandler;
import com.happyplayer.model.Mp3Info;
import com.happyplayer.util.ActivityManager;
import com.happyplayer.util.AniUtil;
import com.happyplayer.util.MediaUtils;
import com.happyplayer.widget.TitleRelativeLayout;

public class ScanMusicActivity extends Activity {
	/**
	 * 初始界面
	 */
	private RelativeLayout initRelativeLayout;

	/**
	 * 初始按钮，扫描按钮
	 */
	private TitleRelativeLayout initButton;

	/**
	 * 扫描界面
	 */
	private RelativeLayout scaningRelativeLayout;
	/**
	 * 扫描图片
	 */
	private ImageView scaningPICImageView;
	private AnimationDrawable aniLoading;

	/**
	 * 扫描路径
	 */
	private TextView scanPathTextView;
	/**
	 * 扫描结果
	 */
	private TextView scanResultTextView;
	/**
	 * 取消按钮
	 */
	private RelativeLayout cancelButton;
	/**
	 * 取消扫描
	 */
	private boolean cancelScan = false;
	/**
	 * 正在扫描
	 */
	private boolean isScan = false;
	/**
	 * 完成界面
	 */
	private RelativeLayout finishRelativeLayout;
	/**
	 * 扫描完成后的结果
	 */
	private TextView finishResultTextView;
	/**
	 * 完成按钮
	 */
	private RelativeLayout finishButton;

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// 完成
			if (msg.what == -1) {
				initRelativeLayout.setVisibility(View.INVISIBLE);
				initButton.setVisibility(View.INVISIBLE);

				scaningRelativeLayout.setVisibility(View.INVISIBLE);
				cancelButton.setVisibility(View.INVISIBLE);

				finishRelativeLayout.setVisibility(View.VISIBLE);
				finishButton.setVisibility(View.VISIBLE);

				finishResultTextView.setText(scanResultTextView.getText()
						.toString());

				AniUtil.stopAnimation(aniLoading);

			} else {
				Mp3Info mp3Info = (Mp3Info) msg.obj;

				scanPathTextView.setText(mp3Info.getPath());
				scanResultTextView.setText(msg.what + "首歌曲已添加到本地音乐");
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanmusic);
		init();
		ActivityManager.getInstance().addActivity(this);
	}

	private void init() {

		initRelativeLayout = (RelativeLayout) findViewById(R.id.init);
		initRelativeLayout.setVisibility(View.VISIBLE);
		initButton = (TitleRelativeLayout) findViewById(R.id.initButton);
		initButton.setVisibility(View.VISIBLE);
		initButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				cancelScan = false;
				scanMusic();
			}
		});

		scaningRelativeLayout = (RelativeLayout) findViewById(R.id.scaning);
		scaningRelativeLayout.setVisibility(View.INVISIBLE);

		scaningPICImageView = (ImageView) findViewById(R.id.scaning_pic);
		aniLoading = (AnimationDrawable) scaningPICImageView.getBackground();

		scanPathTextView = (TextView) findViewById(R.id.scanPath);
		scanResultTextView = (TextView) findViewById(R.id.scanResult);

		cancelButton = (TitleRelativeLayout) findViewById(R.id.cancelButton);
		cancelButton.setVisibility(View.INVISIBLE);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				cancel();
			}
		});

		finishRelativeLayout = (RelativeLayout) findViewById(R.id.finish);
		finishRelativeLayout.setVisibility(View.INVISIBLE);
		finishResultTextView = (TextView) findViewById(R.id.finishResult);
		finishButton = (TitleRelativeLayout) findViewById(R.id.finishButton);
		finishButton.setVisibility(View.INVISIBLE);
		finishButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

	}

	protected void cancel() {
		cancelScan = true;
	}

	private void scanMusic() {
		initRelativeLayout.setVisibility(View.INVISIBLE);
		initButton.setVisibility(View.INVISIBLE);

		scaningRelativeLayout.setVisibility(View.VISIBLE);
		cancelButton.setVisibility(View.VISIBLE);

		finishRelativeLayout.setVisibility(View.INVISIBLE);
		finishButton.setVisibility(View.INVISIBLE);

		isScan = true;

		AniUtil.startAnimation(aniLoading);

		new AsyncTaskHandler() {

			@Override
			protected void onPostExecute(Object result) {

				handler.sendEmptyMessage(-1);
			}

			@Override
			protected Object doInBackground() throws Exception {
				Thread.sleep(1000);
				scannerMusic();
				if (!cancelScan)
					Thread.sleep(1000);
				return null;
			}
		}.execute();

	}

	private void scannerMusic() {
		int size = 0;
		// 查询媒体数据库
		Cursor cursor = MediaUtils.getMediaCursor(this);

		for (int i = 0; i < cursor.getCount(); i++) {

			if (cancelScan)
				break;
			Mp3Info mp3Info = MediaUtils.getMp3InfoByCursor(cursor);
			//
			// 将扫描到的数据保存到播放列表
			//
			size++;
			Message msg = new Message();
			msg.what = size;
			msg.obj = mp3Info;

			handler.sendMessage(msg);

		}
		cursor.close();

		isScan = false;

	}

	public void back(View v) {
		if (isScan)
			cancel();
		finish();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (isScan)
				cancel();
			finish();
		}
		return false;
	}
}
