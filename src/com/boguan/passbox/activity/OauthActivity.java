package com.boguan.passbox.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.boguan.passbox.R;
import com.ccst.kuaipan.protocol.LoginProtocol.AccessTokenProtocol;
import com.ccst.kuaipan.protocol.Protocol.BaseProtocolData;
import com.ccst.kuaipan.protocol.Protocol.ProtocolType;
import com.ccst.kuaipan.protocol.util.RequestEngine;
import com.ccst.kuaipan.protocol.util.RequestEngine.HttpRequestCallback;

public class OauthActivity extends Activity implements HttpRequestCallback {
    private WebView mWebView;
    private MyWebViewClient mClient;
    private String mWebUrl;
    private static String tag = "";
    public static Handler handler;
    public static final String KEY_URL = "url";
    public static final int RES_OK = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauth_activity);
        mWebUrl = getIntent().getStringExtra(KEY_URL);
        if (TextUtils.isEmpty(mWebUrl)) {
            finish();
            return;
        }
        initData();
    }

    private void initData() {
        mWebView = (WebView) findViewById(R.id.oauth_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.clearCache(true);
        mClient = new MyWebViewClient();
        mWebView.setWebViewClient(mClient);
        mWebView.loadUrl(mWebUrl);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView = null;
        System.gc();
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            RequestEngine.getInstance(OauthActivity.this).accessToken(OauthActivity.this);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onHttpResult(BaseProtocolData data) {
        if (data.isSuccess) {
            if (data.getType() == ProtocolType.ACCESS_TOKEN_PROTOCOL) {
                if (!((AccessTokenProtocol) data).isSuccess) {
                    showFailedDialog();
                    return;
                } else {
                    RequestEngine.getInstance(OauthActivity.this).getUserInfo(OauthActivity.this);
                }

                Log.d("myLog", "ACCESS_TOKEN_PROTOCOL===");
            } else if (data.getType() == ProtocolType.REQUEST_TOKEN_PROTOCOL) {
                Log.d("myLog", "REQUEST_TOKEN_PROTOCOL===");
            } else if (data.getType() == ProtocolType.GET_USER_INFO_PROTOCOL) {
                setResult(RES_OK);
                finish();
            }
        }
    }

    @Override
    public void onHttpStart(BaseProtocolData data) {

    }

    public void showFailedDialog() {
        Dialog dialog = new AlertDialog.Builder(this).setMessage(R.string.dialog_failed_txt).setNeutralButton(R.string.i_known, null).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}