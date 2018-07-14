package com.boguan.passbox.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.boguan.passbox.R;
import com.boguan.passbox.model.Password;
import com.boguan.passbox.utils.AES;
import com.boguan.passbox.utils.LockPatternUtil;
import com.boguan.passbox.utils.LogUtils;
import com.boguan.passbox.view.LockPatternView;
import com.ccst.kuaipan.protocol.DownloadFileProtocol;
import com.ccst.kuaipan.protocol.LoginProtocol;
import com.ccst.kuaipan.protocol.Protocol;
import com.ccst.kuaipan.protocol.data.KuaipanHTTPResponse;
import com.ccst.kuaipan.protocol.util.KuaipanHTTPUtility;
import com.ccst.kuaipan.protocol.util.RequestEngine;

import java.util.ArrayList;
import java.util.List;

import cn.zdx.lib.annotation.FindViewById;

import static com.boguan.passbox.utils.Constant.AUTO_FILE_NAME;
import static com.boguan.passbox.utils.Constant.AUTO_LOCK_PATTERN_FILE_NAME;
import static com.boguan.passbox.utils.Constant.BROADCAST_SHOW_FAILED_DIALOG;
import static com.boguan.passbox.utils.Constant.INTENT_KEY_AUTO_PASSWORD_LIST;

/**
 * Created by Lymons on 16/4/13.
 */
public class CloudOptionsActivity extends BaseActivity implements RequestEngine.HttpRequestCallback {

    @FindViewById(R.id.btn_login_baidu)
    Button btnLoginBaidu;

    @FindViewById(R.id.btn_login_dropbox)
    Button btnLoginDropbox;

    @FindViewById(R.id.btn_login_kuaipan)
    Button btnLoginKuaipan;

    @FindViewById(R.id.btn_login_xiaomi)
    Button btnLoginXiaomi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_options);

        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mAutoKuaiPanReceiver);
    }

    @Override
    public void onHttpResult(Protocol.BaseProtocolData data) {
        if (data.getType() == Protocol.ProtocolType.REQUEST_TOKEN_PROTOCOL) {
            Intent intent = new Intent(this, OauthActivity.class);
            intent.putExtra(OauthActivity.KEY_URL, LoginProtocol.AUTH_URL + RequestEngine.getInstance(this).getSession().token.key);
            startActivityForResult(intent, 1);
        }else if(data.getType() == Protocol.ProtocolType.DOWNLOAD_FILE_PROTOCOL){
            DownloadFileProtocol.DownloadProtocol protocol = (DownloadFileProtocol.DownloadProtocol)data;
            if (!protocol.isSuccess){

            }

            List<LockPatternView.Cell> cells;
            if (data.getmPath().equals(AUTO_LOCK_PATTERN_FILE_NAME)) {
                cells = (List<LockPatternView.Cell>) data.getAttachedObject();
                SetLockpatternActivity.initPasswordList(this, cells);

            } else if (data.getmPath().equals(AUTO_LOCK_PATTERN_FILE_NAME)) {
                Intent intent = new Intent();
                cells = LockPatternUtil.getLocalCell(this);
                if (cells.size() > 0) {
                    ArrayList<Password> passwords = (ArrayList<Password>) data.getAttachedObject();
                    intent.setClass(this, MainActivity.class);
                    intent.putExtra(INTENT_KEY_AUTO_PASSWORD_LIST, passwords);
                } else {
                    // 没有密码是让用户设置密码
                    intent.setClass(this, EntryActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        } else {
            Log.d("myLog", KuaipanHTTPResponse.getHttpErrorCodeDescribtion(data.getHttpRequestInfo().getResultCode()));
        }
        mProgressDialog.dismiss();
    }

    @Override
    public void onHttpStart(Protocol.BaseProtocolData data) {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle("Notice");
        mProgressDialog.setMessage("waiting...");
        mProgressDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != 1) {
            return;
        }

        switch (resultCode) {
            case OauthActivity.RES_OK:
                RequestEngine.getInstance(getActivity()).downloadFile(AUTO_LOCK_PATTERN_FILE_NAME, CloudOptionsActivity.this);
                RequestEngine.getInstance(getActivity()).downloadFile(AUTO_FILE_NAME, CloudOptionsActivity.this);
                break;
            default:
                break;
        }
    }

    private BroadcastReceiver mAutoKuaiPanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BROADCAST_SHOW_FAILED_DIALOG)){
                showFailedDialog();
            }
        }
    };

    private void showFailedDialog(){
        mProgressDialog.dismiss();
        Dialog dialog = new AlertDialog.Builder(this).setMessage(R.string.dialog_failed_txt).setNeutralButton(R.string.i_known,null).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void showDialog() {
        mProgressDialog.dismiss();
        Dialog dialog = new AlertDialog.Builder(this).setMessage(R.string.dialog_network_error_txt).setNeutralButton(R.string.i_known,null).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


    private void authKuaipan() {
        if (KuaipanHTTPUtility.checkNetworkState(this)) {
            RequestEngine.getInstance(getActivity()).requestToken(this);
        } else {
            showDialog();
        }
    }

    private void initView() {

        try {
            LogUtils.outputLog(AES.encrypt("hello world", "foobar"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_SHOW_FAILED_DIALOG);
        registerReceiver(mAutoKuaiPanReceiver, intentFilter);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authKuaipan();
            }
        };

        btnLoginBaidu.setOnClickListener(clickListener);
        btnLoginXiaomi.setOnClickListener(clickListener);
        btnLoginKuaipan.setOnClickListener(clickListener);
        btnLoginDropbox.setOnClickListener(clickListener);

    }
}
