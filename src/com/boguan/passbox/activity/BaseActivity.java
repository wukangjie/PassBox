package com.boguan.passbox.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.boguan.passbox.app.MyApplication;
import com.boguan.passbox.model.SettingKey;
import com.boguan.passbox.utils.LogUtils;

import java.lang.reflect.Field;

import cn.zdx.lib.annotation.FindViewById;
import cn.zdx.lib.annotation.ViewFinder;
import cn.zdx.lib.annotation.XingAnnotationHelper;

/**
 * 程序基本类，所有的Activity都要继承本类，实现了友盟统计
 *
 */
public class BaseActivity extends Activity implements Handler.Callback {
	protected enum Style {
		HIDE_ACTION_BAR,
		TRANS_STATUS_BAR
	}
    
    public static final long MAX_NO_PASS_TIME = 1000*60;
    protected static long timeAtIntoBackground;
	protected ProgressDialog mProgressDialog;
	protected Handler handler = new Handler(this);


	protected BroadcastReceiver actionCloseBroadcastReceiver = null;
    protected void creatActionCloseBroadcastReceiver() {
        actionCloseBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (((MyApplication)getApplication()).isAlreadyLeftFromForeground() == false) {
                    ((MyApplication)getApplication()).setAlreadyLeftFromForeground(true);
                    timeAtIntoBackground = System.currentTimeMillis();
                }
            }
        };
    }

    protected BroadcastReceiver actionScreenOffBroadcastReceiver = null;
    protected void creatActionScreenOffBroadcastReceiver() {
        actionScreenOffBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (((MyApplication)getApplication()).isAlreadyLeftFromForeground() == false) {
                    ((MyApplication)getApplication()).setAlreadyLeftFromForeground(true);
                    timeAtIntoBackground = System.currentTimeMillis();
                }
            }
        };
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		LogUtils.trace(this);

        if (actionCloseBroadcastReceiver == null) {
            creatActionCloseBroadcastReceiver();
        }
        registerReceiver(actionCloseBroadcastReceiver, new IntentFilter(
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        if (actionScreenOffBroadcastReceiver == null) {
            creatActionScreenOffBroadcastReceiver();
        }

        registerReceiver(actionScreenOffBroadcastReceiver, new IntentFilter(
                Intent.ACTION_SCREEN_OFF));
    }


    @Override
	protected void onResume() {
		super.onResume();
		LogUtils.trace(this);

		long currentTime = System.currentTimeMillis();
		if (((MyApplication)getApplication()).isAlreadyLeftFromForeground() && 
		        timeAtIntoBackground > 0 && 
		        (currentTime - timeAtIntoBackground) > MAX_NO_PASS_TIME) {
            Intent it = new Intent(this, EntryActivity.class);
            it.putExtra(EntryActivity.KEY_LOAD_AUTO, true);
            startActivity(it);
        }
		((MyApplication)getApplication()).setAlreadyLeftFromForeground(false);
		timeAtIntoBackground = 0;
	}

	@Override
	protected void onPause() {
		super.onPause();
		LogUtils.trace(this);
	}
	
	@Override
    protected void onDestroy() {
		LogUtils.trace(this);

	    if (actionCloseBroadcastReceiver != null) {
            unregisterReceiver(actionCloseBroadcastReceiver);
            actionCloseBroadcastReceiver = null;
        }
        
        if (actionScreenOffBroadcastReceiver != null) {
            unregisterReceiver(actionScreenOffBroadcastReceiver);
            actionScreenOffBroadcastReceiver = null;
        }
        
        super.onDestroy();
    }

	@Override
	public boolean handleMessage(Message msg) {
		return true;
	}

    public BaseActivity getActivity() {
		return this;
	}

	public void showToast(int id) {
		showToast(id, Toast.LENGTH_SHORT);
	}

	public void showToast(int id, int duration) {
		Toast.makeText(this, id, duration).show();
	}

	public MyApplication getMyApplication() {
		return (MyApplication) getApplication();
	}

	public String getSetting(SettingKey key, String defValue) {
		return getMyApplication().getSetting(key, defValue);
	}

	public void putSetting(SettingKey key, String value) {
		getMyApplication().putSetting(key, value);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		initAnnotation();
	}

	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		initAnnotation();
	}

	@Override
	public void setContentView(View view, LayoutParams params) {
		super.setContentView(view, params);
		initAnnotation();
	}

	private void initAnnotation() {
		ViewFinder viewFinder = ViewFinder.create(this);
		Class<?> clazz = getClass();
		do {
			findView(clazz, viewFinder);
		} while ((clazz = clazz.getSuperclass()) != BaseActivity.class);
	}

	/** 初始化 {@link FindViewById} */
	private void findView(Class<?> clazz, ViewFinder viewFinder) {
		Field[] fields = clazz.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				XingAnnotationHelper.findView(this, field, viewFinder);
			}
		}
	}

	protected void setContentView(int layoutResID, Style style) {
		switch (style) {
			case HIDE_ACTION_BAR:
				hideActionBar();
				break;
			case TRANS_STATUS_BAR:
				transparentStatusBar();
				break;
			default:
				break;
		}

		setContentView(layoutResID);
	}

	private void hideActionBar() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	private void transparentStatusBar() {
		hideActionBar();

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
			window.setNavigationBarColor(Color.TRANSPARENT);
		}
	}
}
