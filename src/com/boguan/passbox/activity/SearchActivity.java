package com.boguan.passbox.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.boguan.passbox.R;
import com.boguan.passbox.activity.fragment.SearchListFragment;
import com.boguan.passbox.dialog.ExportDialog;
import com.boguan.passbox.service.LocalService.MainService;
import com.boguan.passbox.service.LocalService.Mainbinder;
import com.boguan.passbox.utils.Constant;
import com.ccst.kuaipan.protocol.CreateDataProtocol;
import com.ccst.kuaipan.protocol.Protocol;
import com.ccst.kuaipan.protocol.util.RequestEngine;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener, RequestEngine.HttpRequestCallback {
    private ImageView mBackView;
    private EditText mEditView;
    private ImageView mVoiceView;
    private SearchListFragment mSearchListFragment;
    private Mainbinder mainbinder;
    private FrameLayout mSearchFrame;
    public static String mLastQueryString;
    private boolean mIsDelete;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mainbinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mainbinder = (Mainbinder) service;
        }
    };
    private BroadcastReceiver mPasswordChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constant.BROADCAST_AUTO_KUAIPAN)) {
                ExportDialog exportDialog = new ExportDialog(SearchActivity.this, mainbinder, Constant.TAG_EXPORT_AUTO_KUAIPAN);
                exportDialog.autoUpload();
            } else if (action.equals(Constant.BROADCAST_AUTO_UPLOAD)) {
                String path = Environment.getExternalStorageDirectory().getPath() + "/MyPassword/" + intent.getStringExtra(Constant.INTENT_KEY_FILENAME);
                RequestEngine.getInstance(SearchActivity.this).uploadFile(path, SearchActivity.this);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Intent intent = new Intent(this, MainService.class);
        this.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.BROADCAST_AUTO_KUAIPAN);
        intentFilter.addAction(Constant.BROADCAST_AUTO_UPLOAD);
        registerReceiver(mPasswordChangeReceiver, intentFilter);
        mBackView = (ImageView) findViewById(R.id.activity_search_back);
        mVoiceView = (ImageView) findViewById(R.id.activity_search_voice);
        mEditView = (EditText) findViewById(R.id.activity_search_edit);
        mSearchFrame = (FrameLayout) findViewById(R.id.activity_search_frame);
        mBackView.setOnClickListener(this);
        mVoiceView.setOnClickListener(this);
        mEditView.addTextChangedListener(new EditChangedListener());
        mEditView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEditView.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        mSearchListFragment = new SearchListFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        unregisterReceiver(mPasswordChangeReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_search_back:
                finish();
                break;
            case R.id.activity_search_voice:
                if (mIsDelete == true) {
                    mEditView.setText(null);
                }
                break;
        }
    }

    @Override
    public void onHttpResult(Protocol.BaseProtocolData data) {
        if (data.getType() == Protocol.ProtocolType.UPLOAD_FILE_PROTOCOL) {
            CreateDataProtocol.UploadFileProtocol uploadFileProtocol = (CreateDataProtocol.UploadFileProtocol) data;
            if (uploadFileProtocol.isSuccess) {
                Log.d("myLog", "auto upload success");

            } else {
                Log.d("myLog", "auto upload failed");
            }
        }
    }

    @Override
    public void onHttpStart(Protocol.BaseProtocolData data) {
    }


    private class EditChangedListener implements TextWatcher, View.OnClickListener {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (!mEditView.getText().toString().isEmpty()) {
                Drawable mCleanView = getResources().getDrawable(R.drawable.ic_voice);
                mVoiceView.setImageDrawable(mCleanView);
                mIsDelete = false;
            } else {
                mVoiceView.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete));
                mIsDelete = true;
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().length() > 0) {
                mVoiceView.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete));
                mSearchFrame.setVisibility(View.VISIBLE);
                showList(s);
            } else {
                mVoiceView.setImageDrawable(getResources().getDrawable(R.drawable.ic_voice));
                mSearchFrame.setVisibility(View.GONE);
                mVoiceView.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.activity_search_voice:
                    mEditView.setText(null);
                    break;
            }
        }
    }

    private void showList(Editable s) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.activity_search_frame, mSearchListFragment).commit();
        mLastQueryString = s.toString();
        mSearchListFragment.setFilter(s.toString());
    }
}