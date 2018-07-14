package com.boguan.passbox.activity;

import java.util.List;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import com.boguan.passbox.R;
import com.boguan.passbox.utils.Anime;
import com.boguan.passbox.utils.LockPatternUtil;
import com.boguan.passbox.view.LockPatternView;
import com.boguan.passbox.view.LockPatternView.Cell;
import com.boguan.passbox.view.LockPatternView.DisplayMode;
import com.boguan.passbox.view.LockPatternView.OnPatternListener;
import com.ccst.kuaipan.protocol.util.RequestEngine;

import cn.zdx.lib.annotation.FindViewById;

/**
 * 入口，欢迎页
 * 
 * @author zengdexing
 * 
 */
public class EntryActivity extends BaseActivity implements Callback, OnPatternListener {
    public static final String KEY_LOAD_AUTO = "KEY_LOAD_AUTO";
    
	@FindViewById(R.id.entry_activity_iconview)
	private View iconView;
	private Handler handler;

	private final int MESSAGE_START_MAIN = 1;
	private final int MESSAGE_CLEAR_LOCKPATTERNVIEW = 3;
	private final int MESSAGE_START_SETLOCKPATTERN = 4;
	private final int MESSAGE_START_BACK = 5;
	private final int MESSAGE_START_LOGIN_KUAIPAN = 6;

	@FindViewById(R.id.entry_activity_bg)
	private View backgroundView;

	@FindViewById(R.id.entry_activity_lockPatternView)
	private LockPatternView lockPatternView;

	@FindViewById(R.id.entry_activity_tips)
	private TextView tipsView;
	
	private boolean isAutoLoading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entry, Style.TRANS_STATUS_BAR);
		
		Intent it = getIntent();
		isAutoLoading = it.getBooleanExtra(KEY_LOAD_AUTO, false);

		handler = new Handler(this);
		lockPatternView.setOnPatternListener(this);

		if (RequestEngine.getInstance(getMyApplication()).needToAuth()) {
			// 还没有登录网盘,则提示用户登录
			lockPatternView.setEnabled(false);
			handler.sendEmptyMessage(MESSAGE_START_LOGIN_KUAIPAN);
		} else {
			List<Cell> cells = LockPatternUtil.getLocalCell(this);
			if (cells.size() == 0) {
				// 首次使用，没有设置密码，跳转设置密码页
				lockPatternView.setEnabled(false);
				handler.sendEmptyMessageDelayed(MESSAGE_START_SETLOCKPATTERN, 2000);
			}
		}

		tipsView.setText("");
		initAnimation();
		checkPackageName();
	}
	
	

	@Override
    protected void onResume() {
        super.onResume();
    }

    /**
	 * 检查包名，防止打包党简简单单二次打包
	 */
	private void checkPackageName() {
		if (!getPackageName().equals(getString(R.string.package_name)))
			finish();
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MESSAGE_START_SETLOCKPATTERN:
				startActivity(new Intent(this, SetLockpatternActivity.class));
				finish();
				break;

			case MESSAGE_START_MAIN:
				Intent intent = new Intent(this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
				break;
				
			case MESSAGE_START_BACK:
			    finish();
			    break;

			case MESSAGE_CLEAR_LOCKPATTERNVIEW:
				lockPatternView.clearPattern();
				tipsView.setText("");
				break;

			case MESSAGE_START_LOGIN_KUAIPAN:
				startActivity(new Intent(this, LoginKuaiPanActivity.class));
				finish();
				break;
			default:
				break;
		}
		return true;
	}

	/**
	 * 图标动画
	 */
	private void initAnimation() {
		Anime.alphaOutAnimation(new Anime.Done() {
			@Override
			public void ok(View v) {
				Anime.translateAlphaInAnimation(1, iconView, lockPatternView, tipsView);
			}
		}, backgroundView);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onPatternStart() {
		handler.removeMessages(MESSAGE_CLEAR_LOCKPATTERNVIEW);
		tipsView.setText("");
	}

	@Override
	public void onPatternCleared() {
	}

	@Override
	public void onPatternCellAdded(List<Cell> pattern) {
	}

	@Override
	public void onPatternDetected(List<Cell> pattern) {
		if (LockPatternUtil.checkPatternCell(LockPatternUtil.getLocalCell(this), pattern)) {
			// 认证通过
			lockPatternView.setDisplayMode(DisplayMode.Correct);
			if (isAutoLoading) {
			    handler.sendEmptyMessage(MESSAGE_START_BACK);
            } else {
                handler.sendEmptyMessage(MESSAGE_START_MAIN);
            }
		} else {
			// 认证失败
			lockPatternView.setDisplayMode(DisplayMode.Wrong);
			tipsView.setText(R.string.lock_pattern_error);
			handler.sendEmptyMessageDelayed(MESSAGE_CLEAR_LOCKPATTERNVIEW, 1000);
		}

	}
}
