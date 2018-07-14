package com.boguan.passbox.activity;

import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.boguan.passbox.R;
import com.boguan.passbox.adapter.EditGroupPopAdapter;
import com.boguan.passbox.app.MyApplication;
import com.boguan.passbox.bean.EditGroupPopBean;
import com.boguan.passbox.model.Password;
import com.boguan.passbox.model.PasswordGroup;
import com.boguan.passbox.service.LocalService.MainService;
import com.boguan.passbox.service.LocalService.Mainbinder;
import com.boguan.passbox.service.LocalService.OnGetAllPasswordCallback;
import com.boguan.passbox.service.LocalService.OnGetAllPasswordGroupCallback;
import com.boguan.passbox.service.LocalService.OnGetPasswordCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.zdx.lib.annotation.FindViewById;

/**
 * 密码新增和编辑界面
 *
 * @author zengdexing
 */
public class EditPasswordActivity extends BaseActivity implements OnGetPasswordCallback, OnGetAllPasswordCallback,
        OnGetAllPasswordGroupCallback, View.OnFocusChangeListener {
    /**
     * 传入参数 密码 ID
     */
    public static final String ID = "password_id";
    public static final String PASSWORD_GROUP = "password_group";
    /**
     * 添加模式
     */
    private static final int MODE_ADD = 0;
    /**
     * 修改模式
     */
    private static final int MODE_MODIFY = 1;

    /**
     * 当前模式，默认增加
     */
    private int MODE = MODE_ADD;

    /**
     * 修改密码的ID
     */
    private int id;

    /**
     * 数据源
     */
    private Mainbinder mainbinder;
    @FindViewById(R.id.editview_group)
    private TextView mEditGroupView;
    @FindViewById(R.id.editview_title)
    private AutoCompleteTextView mTitleView;
    @FindViewById(R.id.editview_name)
    private AutoCompleteTextView mNameView;
    @FindViewById(R.id.activity_edit_user_line)
    private View mNameLine;
    @FindViewById(R.id.activity_edit_password_line)
    private View mPassLine;
    @FindViewById(R.id.editview_password)
    private AutoCompleteTextView mPassView;
    @FindViewById(R.id.editview_note)
    private AutoCompleteTextView mNoteView;
    @FindViewById(R.id.main_search_right)
    private ImageView mSearchRight;
    @FindViewById(R.id.editview_back)
    private ImageView mEditBack;
    @FindViewById(R.id.editview_collection_img)
    private ImageView mIsCollection;
    @FindViewById(R.id.editview_linear_group)
    private LinearLayout mGroupLinear;
    @FindViewById(R.id.root_card_view)
    private LinearLayout mTitleLinear;
    @FindViewById(R.id.editview_group_text)
    private TextView mGroupText;
    @FindViewById(R.id.activity_edit_password_toolbar)
    private RelativeLayout mEditToolbar;
    @FindViewById(R.id.activity_edit_view)
    private View mShadowView;
    @FindViewById(R.id.activity_edit_title_line)
    private View mTitleLine;
    @FindViewById(R.id.main_item_note_img)
    private ImageView mRemarkImg;
    private boolean mIsCollect = false;
    private String passwordGroup;
    private PopupWindow mPopupWindow;
    private ListView mListView;
    private List<EditGroupPopBean> mEditGroupPopBeen;

    public int dip2px(float dipValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mainbinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mainbinder = (Mainbinder) service;
            if (MODE == MODE_MODIFY) {
                mainbinder.getPassword(id, EditPasswordActivity.this);
            }
            // 获得所有密码、用户名，用于自动完成
            mainbinder.getAllPassword(EditPasswordActivity.this);
            mainbinder.getAllPasswordGroup(EditPasswordActivity.this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);
        id = getIntent().getIntExtra(ID, -1);
        if (id == -1) {
            MODE = MODE_ADD;
        } else {
            MODE = MODE_MODIFY;
        }
        passwordGroup = getIntent().getStringExtra(PASSWORD_GROUP);
        if (passwordGroup == null || passwordGroup.equals("")) {
            passwordGroup = getString(R.string.password_group_default_name);
        }
        Intent intent = new Intent(this, MainService.class);
        this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        mSearchRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveBtnClick();
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });
        mEditBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mIsCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsCollect) {
                    mIsCollection.setImageResource(R.drawable.ic_collection_unclick);
                    mIsCollect = false;
                } else {
                    mIsCollection.setImageResource(R.drawable.ic_collection_click);
                    mIsCollect = true;
                }
            }
        });
        mTitleView.setOnFocusChangeListener(this);
        mNameView.setOnFocusChangeListener(this);
        mPassView.setOnFocusChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private void deletePassword() {
        Builder builder = new Builder(this);
        builder.setMessage(R.string.alert_delete_message);
        builder.setNeutralButton(R.string.yes, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainbinder.deletePassword(id);
                ((MyApplication) getApplication()).setPasswordChanged(true);
                finish();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    public void clearAllPassword() {
        mainbinder.deleteAllPassword();
        ((MyApplication) getApplication()).setPasswordChanged(true);
    }

    private void onSaveBtnClick() {
        if (TextUtils.isEmpty(mTitleView.getText().toString().trim())) {
            Toast.makeText(this, R.string.add_password_save_no_data, Toast.LENGTH_SHORT).show();
        } else {
            Password password = new Password();
            password.setTitle(mTitleView.getText().toString().trim());
            password.setUserName(mNameView.getText().toString().trim());
            password.setPassword(mPassView.getText().toString().trim());
            password.setNote(mNoteView.getText().toString().trim());
            password.setTop(mIsCollect);
            password.setGroupName(passwordGroup);
            if (MODE == MODE_ADD) {
                // 添加
                password.setCreateDate(System.currentTimeMillis());
                mainbinder.insertPassword(password);
            } else {
                // 修改密码
                password.setId(id);
                mainbinder.updatePassword(password);
            }
            ((MyApplication) getApplication()).setPasswordChanged(true);
            finish();
        }
    }

    @Override
    public void onGetPassword(Password password) {
        if (password == null) {
            Toast.makeText(this, R.string.toast_password_has_deleted, Toast.LENGTH_SHORT).show();
            finish();
        }
        mTitleView.setText(password.getTitle());
        mNameView.setText(password.getUserName());
        mPassView.setText(password.getPassword());
        mNoteView.setText(password.getNote());
        mIsCollect = password.isTop();
        mIsCollection.setImageResource(mIsCollect ? R.drawable.ic_collection_click : R.drawable.ic_collection_unclick);
        mTitleView.setSelection(mTitleView.getText().length());
        mGroupText.setText(password.getGroupName());
        if (!mGroupText.equals(R.string.password_group))
            mEditGroupView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onGetAllPassword(String groupName, List<Password> passwords) {
        // 去掉重复
        Set<String> arrays = new HashSet<String>();
        for (int i = 0; i < passwords.size(); i++) {
            Password password = passwords.get(i);
            arrays.add(password.getUserName());
            arrays.add(password.getPassword());
        }
        int id = R.layout.simple_dropdown_item;
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, id, new ArrayList<String>(arrays));
        mNameView.setAdapter(arrayAdapter);
    }

    @Override
    public void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups) {
        mEditGroupPopBeen = new ArrayList<>();
        for (int i = 0; i < passwordGroups.size(); i++) {
            PasswordGroup passwordGroup = passwordGroups.get(i);
            mEditGroupPopBeen.add(new EditGroupPopBean(passwordGroup.getGroupName()));
        }
        mGroupLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mGroupLinear.getWindowToken(), 0);
                showPopupWindow();
            }
        });

    }

    private void showPopupWindow() {
        WindowManager wm = this.getWindowManager();
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        RelativeLayout editGroupRelative;
        View contentView = LayoutInflater.from(this).inflate(R.layout.activity_edit_pop_group, null);
        mPopupWindow = new PopupWindow(contentView,
                width * 2 / 3 + 8, height * 2 / 3, true);
        mPopupWindow.setContentView(contentView);
        mPopupWindow.showAsDropDown(mEditToolbar);
        mListView = (ListView) contentView.findViewById(R.id.activity_edit_pop_list);
        editGroupRelative = (RelativeLayout) contentView.findViewById(R.id.edit_pop_relative);
        mListView.setVerticalScrollBarEnabled(false);
        mListView.setFastScrollEnabled(false);
        EditGroupPopAdapter editGroupPopAdapter = new EditGroupPopAdapter(this);
        editGroupPopAdapter.setmEditGroupPopBeen(mEditGroupPopBeen);
        mListView.setAdapter(editGroupPopAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mGroupText.setText(mEditGroupPopBeen.get(position).getGroupName());
                mEditGroupView.setVisibility(View.VISIBLE);
                passwordGroup = mEditGroupPopBeen.get(position).getGroupName();
                mPopupWindow.dismiss();
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mGroupText.setText(mEditGroupPopBeen.get(position).getGroupName());
                mEditGroupView.setVisibility(View.VISIBLE);
                mPopupWindow.dismiss();
                passwordGroup = mEditGroupPopBeen.get(position).getGroupName();
                return true;
            }
        });
        editGroupRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            switch (v.getId()) {
                case R.id.editview_title:
                    mTitleLine.setBackgroundColor(getResources().getColor(R.color.dropbox_btn_normal));
                    break;
                case R.id.editview_name:
                    mNameLine.setBackgroundColor(getResources().getColor(R.color.dropbox_btn_normal));
                    break;
                case R.id.editview_password:
                    mPassLine.setBackgroundColor(getResources().getColor(R.color.dropbox_btn_normal));
                    break;
            }
        } else {
            switch (v.getId()) {
                case R.id.editview_title:
                    mTitleLine.setBackgroundColor(getResources().getColor(R.color.white));
                    break;
                case R.id.editview_name:
                    mNameLine.setBackgroundColor(getResources().getColor(R.color.white));
                    break;
                case R.id.editview_password:
                    mPassLine.setBackgroundColor(getResources().getColor(R.color.white));
                    break;
            }
        }
    }

}
