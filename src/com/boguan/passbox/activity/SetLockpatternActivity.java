package com.boguan.passbox.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import com.boguan.passbox.R;
import com.boguan.passbox.app.MyApplication;
import com.boguan.passbox.model.AsyncResult;
import com.boguan.passbox.model.AsyncSingleTask;
import com.boguan.passbox.model.Password;
import com.boguan.passbox.model.SettingKey;
import com.boguan.passbox.utils.Constant;
import com.boguan.passbox.utils.LockPatternUtil;
import com.boguan.passbox.view.LockPatternView;
import com.boguan.passbox.view.LockPatternView.Cell;
import com.boguan.passbox.view.LockPatternView.DisplayMode;
import com.boguan.passbox.view.LockPatternView.OnPatternListener;
import com.ccst.kuaipan.database.Settings;
import com.ccst.kuaipan.database.Settings.Field;
import com.ccst.kuaipan.protocol.Protocol;
import com.ccst.kuaipan.protocol.util.RequestEngine;
import com.ccst.kuaipan.protocol.util.RequestEngine.HttpRequestCallback;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import cn.zdx.lib.annotation.FindViewById;

/**
 * 设置图案解锁界面
 *
 * @author zengdexing
 */
public class SetLockpatternActivity extends BaseActivity implements OnPatternListener, Callback, HttpRequestCallback {
    private static final String APP_NAME = "MyPassword";
    @FindViewById(R.id.set_lockpattern_view)
    private LockPatternView lockPatternView;

    @FindViewById(R.id.set_lockpattern_text)
    private TextView textView;

    private List<LockPatternView.Cell> mCellsList;

    /**
     * 模式 认证
     */
    private static final int MODE_AUTH = 0;
    /**
     * 模式 处于第一步
     */
    private static final int MODE_FIRST_STEP = 1;
    /**
     * 模式 处于第二步
     */
    private static final int MODE_SECOND_STEP = 2;

    private static final int MEG_AUTH_ERROR = 1;
    private static final int MEG_GOTO_SECOND_STEP = 2;
    private static final int MEG_SET_SUCCESS = 3;
    private static final int MEG_GOTO_FIRST_STEP = 4;

    /**
     * 当前处于的模式
     */
    private int mode = MODE_AUTH;
    private Handler handler = new Handler(this);
    private ArrayList<Password> kuaipanPasswords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_lockpattern);
        initActionBar();
        lockPatternView.setOnPatternListener(this);
        initMode();
    }

    /**
     * 初始化模式：如果用户没有设置密码，则处于第一步 {@link #MODE_FIRST_STEP}，否则用户需要验证
     * {@link #MODE_AUTH}
     */
    private void initMode() {
        mCellsList = LockPatternUtil.getLocalCell(this);

        if (mCellsList.size() != 0) {
            mode = MODE_AUTH;
            textView.setText(R.string.set_lock_pattern_auth);
        } else {
            mode = MODE_FIRST_STEP;
            textView.setText(R.string.set_lock_pattern_first_step);
            showFirstUserDialog();
        }
    }

    /**
     * 第一次使用，设置解锁图案
     */
    private void showFirstUserDialog() {
        Builder builder = new Builder(this);
        builder.setMessage(R.string.set_lock_pattern_first_message);
        builder.setNeutralButton(R.string.set_lock_pattern_first_sure, null);
        builder.show();
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
//		actionBar.setDisplayShowTitleEnabled(true);
//		actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public static void initPasswordList(Activity activity, List<Cell> list) {
        LockPatternUtil.savePatternCell((BaseActivity) activity, list);
    }

    private void uploadLockPatternFileToKuaipan(final String lockPatternJsonString) {
        new AsyncSingleTask<File>() {

            @Override
            protected AsyncResult<File> doInBackground(AsyncResult<File> asyncResult) {

                String fileName = Environment.getExternalStorageDirectory().getPath() + "/" + APP_NAME + "/" + Constant.AUTO_LOCK_PATTERN_FILE_NAME;
                File file = new File(fileName);
                file.getParentFile().mkdirs();
                file.deleteOnExit();
                PrintWriter printWriter = null;
                try {
                    file.createNewFile();
                    printWriter = new PrintWriter(file);
                    //加密
                    String content = LockPatternUtil.encryptLockPattern(lockPatternJsonString);
                    printWriter.print(content);
                    asyncResult.setData(file);
                    asyncResult.setResult(0);

                } catch (IOException e) {
                    asyncResult.setResult(-1);
                    e.printStackTrace();
                } finally {
                    if (printWriter != null) {
                        printWriter.close();
                    }
                }

                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<File> asyncResult) {
                String fileName = asyncResult.getData().getName();
                if (asyncResult.getResult() == 0) {
                    String path = Environment.getExternalStorageDirectory().getPath() + "/" + APP_NAME + "/" + fileName;
                    RequestEngine.getInstance(getActivity()).uploadFile(path, SetLockpatternActivity.this);

                } else {
                    //失败！
                    String msg = "失败";
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
            }

        }.execute();
    }

    @Override
    public void onPatternStart() {
        textView.setText(R.string.set_lock_pattern_step_tips);
    }

    @Override
    public void onPatternCleared() {

    }

    @Override
    public void onPatternCellAdded(List<Cell> pattern) {

    }

    private List<Cell> lastCells;

    @Override
    public void onPatternDetected(List<Cell> pattern) {
        switch (mode) {
            case MODE_AUTH:
                if (LockPatternUtil.authPatternCell(this, pattern, mCellsList)) {
                    // 验证通过，到第一步
                    lockPatternView.setDisplayMode(DisplayMode.Correct);
                    lockPatternView.setEnabled(false);
                    textView.setText(R.string.set_lock_pattern_auth_ok);
                    handler.sendEmptyMessageDelayed(4, 1000);
                } else {
                    // 验证不通过，继续输入密码
                    lockPatternView.setEnabled(false);
                    lockPatternView.setDisplayMode(DisplayMode.Wrong);
                    textView.setText(R.string.set_lock_pattern_auth_error);
                    handler.sendEmptyMessageDelayed(MEG_AUTH_ERROR, 1000);
                }
                break;
            case MODE_FIRST_STEP:
                // 第一次输入，记录
                lockPatternView.setEnabled(false);
                lastCells = new ArrayList<LockPatternView.Cell>(pattern);
                textView.setText(R.string.set_lock_pattern_first_step_tips);
                handler.sendEmptyMessageDelayed(MEG_GOTO_SECOND_STEP, 1000);
                break;
            case MODE_SECOND_STEP:
                if (LockPatternUtil.checkPatternCell(lastCells, pattern)) {
                    // 设置成功
                    lockPatternView.setEnabled(false);
                    lockPatternView.setDisplayMode(DisplayMode.Correct);
                    textView.setText(R.string.set_lock_pattern_second_step_tips);
                    handler.sendEmptyMessageDelayed(MEG_SET_SUCCESS, 2000);
                    LockPatternUtil.savePatternCell(this, pattern);

                    String jsonArrayString = ((MyApplication) getApplication()).getSettingDecode(SettingKey.LOCK_PATTERN, "[]");
                    if (Settings.getSettingBoolean(this, Field.IS_AUTO_KUAIPAN, false)) {
                        uploadLockPatternFileToKuaipan(jsonArrayString);
                    }

                } else {
                    // 两次输入密码不一致，到第一步重新输入
                    lockPatternView.setDisplayMode(DisplayMode.Wrong);
                    lockPatternView.setEnabled(false);
                    textView.setText(R.string.set_lock_pattern_second_step_error);
                    handler.sendEmptyMessageDelayed(MEG_GOTO_FIRST_STEP, 1000);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        lockPatternView.setEnabled(true);
        lockPatternView.clearPattern();
        switch (msg.what) {
            case MEG_AUTH_ERROR:
                textView.setText(R.string.set_lock_pattern_auth);
                break;

            case MEG_GOTO_SECOND_STEP:
                mode = MODE_SECOND_STEP;
                textView.setText(R.string.set_lock_pattern_second_step);
                break;
            case MEG_SET_SUCCESS:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constant.INTENT_KEY_AUTO_PASSWORD_LIST, kuaipanPasswords);
                startActivity(intent);
                finish();
                break;

            case MEG_GOTO_FIRST_STEP:
                mode = MODE_FIRST_STEP;
                textView.setText(R.string.set_lock_pattern_first_step);
                break;
            case R.id.msg_import_by_skydrive:
                kuaipanPasswords = (ArrayList<Password>) msg.obj;
                Intent intent1 = new Intent(this, MainActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent1.putExtra(Constant.INTENT_KEY_AUTO_PASSWORD_LIST, kuaipanPasswords);
                startActivity(intent1);
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onHttpResult(Protocol.BaseProtocolData data) {

    }

    @Override
    public void onHttpStart(Protocol.BaseProtocolData data) {

    }
}
