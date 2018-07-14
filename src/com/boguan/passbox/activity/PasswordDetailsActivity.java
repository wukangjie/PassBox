package com.boguan.passbox.activity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.boguan.passbox.R;
import com.boguan.passbox.bean.JsonBean;
import com.boguan.passbox.model.Password;
import com.boguan.passbox.model.PasswordGroup;
import com.boguan.passbox.model.PasswordItem;
import com.boguan.passbox.service.LocalService.MainService;
import com.boguan.passbox.service.LocalService.Mainbinder;
import com.boguan.passbox.service.LocalService.OnGetAllPasswordCallback;
import com.boguan.passbox.service.LocalService.OnGetAllPasswordGroupCallback;
import com.boguan.passbox.service.LocalService.OnGetPasswordCallback;
import com.boguan.passbox.utils.Constant;
import com.boguan.passbox.utils.DateUtils;
import com.boguan.passbox.utils.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cn.zdx.lib.annotation.FindViewById;

/**
 * Created by wukangjie on 2016/12/15.
 */

public class PasswordDetailsActivity extends BaseActivity implements OnGetPasswordCallback, OnGetAllPasswordCallback,
        OnGetAllPasswordGroupCallback, View.OnClickListener {
    /**
     * 传入参数 密码 ID
     */
    public static final String ID = "password_id";
    public static final String PASSWORD_GROUP = "password_group";
    public static final String PASSWORD_USERNAME = "password_username";
    public static final String PASSWORD_PASSWORD = "password_password";
    public static final String PASSWORD_TITLE = "password_title";
    public static final String PASSWORD_IMG = "password_img";
    @FindViewById(R.id.main_item_title)
    public TextView mTitleView;
    @FindViewById(R.id.main_item_date)
    public TextView mDateView;
    @FindViewById(R.id.main_item_name)
    public TextView mNameView;
    @FindViewById(R.id.main_item_password)
    public TextView mPasswordView;
    @FindViewById(R.id.main_item_note)
    public TextView mNoteView;
    @FindViewById(R.id.main_item_copy)
    public View mCopyView;
    @FindViewById(R.id.main_item_delete)
    public View mDeleteView;
    @FindViewById(R.id.main_item_edit)
    public View mEditView;
    @FindViewById(R.id.activity_max_back)
    public ImageView mBackView;
    @FindViewById(R.id.main_item_img)
    private ImageView mItemImg;
    private String mPasswordGroup;
    private String mPasswordUsername;
    private String mPasswordPassword;
    private String mPasswordTitle;
    private Mainbinder mMainbinder;
    final static String mPath = "keyword.json";
    private int mId;
    private String mDataString;
    private ArrayList<JsonBean> mJsonList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_details);
        mId = getIntent().getIntExtra(ID, -1);
        mPasswordUsername = getIntent().getStringExtra(PASSWORD_USERNAME);
        mPasswordGroup = getIntent().getStringExtra(PASSWORD_GROUP);
        mPasswordPassword = getIntent().getStringExtra(PASSWORD_PASSWORD);
        mPasswordTitle = getIntent().getStringExtra(PASSWORD_TITLE);
        Intent intent = new Intent(this, MainService.class);
        this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        mCopyView.setOnClickListener(this);
        mDeleteView.setOnClickListener(this);
        mEditView.setOnClickListener(this);
        mBackView.setOnClickListener(this);
        String jsonStr = JsonUtils.getJson(this, mPath);
        Gson gson = new Gson();
        Type type = new TypeToken<List<JsonBean>>() {
        }.getType();
        mJsonList = gson.fromJson(jsonStr, type);
        int selectPosition = 4;
        //根据标题名字数据判断显示的host和背景颜色
        for (int i = 0; i < mJsonList.size(); i++) {
            ArrayList<String> strs = (ArrayList<String>) mJsonList.get(i).getKey();
            for (int j = 0; j < strs.size(); j++) {
                if (mPasswordTitle.equals(strs.get(j))) {
                    selectPosition = i;
                    break;
                }
            }
        }
        JsonBean mBean = mJsonList.get(selectPosition);
        //cover
        switch (mBean.getIcon()){
            case Constant.PASSWORD_ITEM_BAIDU:
                mItemImg.setImageResource(R.drawable.baidu);
                break;
            case Constant.PASSWORD_ITEM_TUDOU:
                mItemImg.setImageResource(R.drawable.tudou);
                break;
            case Constant.PASSWORD_ITEM_XUNLEI:
                mItemImg.setImageResource(R.drawable.xunlei);
                break;
            case Constant.PASSWORD_ITEM_WEIXIN:
                mItemImg.setImageResource(R.drawable.weixin);
                break;
            default:
                mItemImg.setImageResource(R.drawable.pan_kuaipan);
                break;
        }
        mTitleView.setText(mBean.getHost());
        mNoteView.setMovementMethod(ScrollingMovementMethod.getInstance());

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMainbinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMainbinder = (Mainbinder) service;
            mMainbinder.getPassword(mId, PasswordDetailsActivity.this);
            // 获得所有密码、用户名，用于自动完成
            mMainbinder.getAllPassword(PasswordDetailsActivity.this);
            mMainbinder.getAllPasswordGroup(PasswordDetailsActivity.this);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private void onCopyClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String[] item = new String[]{getResources().getString(R.string.copy_name),
                getResources().getString(R.string.copy_password)};

        builder.setItems(item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // 复制名字
                        ClipboardManager cmbName = (ClipboardManager) getActivity()
                                .getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipDataName = ClipData.newPlainText(null, mPasswordUsername);
                        cmbName.setPrimaryClip(clipDataName);
                        Toast.makeText(getBaseContext(), R.string.copy_name_toast, Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        // 复制密码
                        ClipboardManager cmbPassword = (ClipboardManager) getActivity()
                                .getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText(null, mPasswordPassword);
                        cmbPassword.setPrimaryClip(clipData);
                        Toast.makeText(getBaseContext(), R.string.copy_password_toast, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
    }

    private void onEditClick() {
        Intent intent = new Intent(this, EditPasswordActivity.class);
        intent.putExtra(EditPasswordActivity.ID, mId);
        intent.putExtra(EditPasswordActivity.PASSWORD_GROUP, mPasswordGroup);
        startActivity(intent);
    }

    private void onDeleteClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_delete_message);
        builder.setTitle(mPasswordUsername);
        builder.setNeutralButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMainbinder.deletePassword(mId);
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();

    }

    @Override
    public void onGetPassword(Password password) {

        mNameView.setText(password.getUserName());
        mPasswordView.setText(password.getPassword());
        mNoteView.setText(password.getNote());
        mDataString = DateUtils.formatDate(this, password.getCreateDate());
        mDateView.setText(mDataString);

    }

    @Override
    public void onGetAllPassword(String groupName, List<Password> passwords) {
    }

    @Override
    public void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_item_copy:
                onCopyClick();
                break;
            case R.id.main_item_edit:
                onEditClick();
                break;
            case R.id.main_item_delete:
                onDeleteClick();
                break;
            case R.id.activity_max_back:
                finish();
                break;
        }
    }
}