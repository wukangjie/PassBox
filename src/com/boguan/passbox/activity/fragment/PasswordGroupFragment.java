package com.boguan.passbox.activity.fragment;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.boguan.passbox.R;
import com.boguan.passbox.activity.BaseActivity;
import com.boguan.passbox.adapter.PasswordGroupAdapter;
import com.boguan.passbox.dialog.GreatePasswordGroupDialog;
import com.boguan.passbox.dialog.UpdatePasswdGroupNameDialog;
import com.boguan.passbox.model.OnSettingGroupChangeListener;
import com.boguan.passbox.model.PasswordGroup;
import com.boguan.passbox.model.SettingKey;
import com.boguan.passbox.service.LocalService.Mainbinder;
import com.boguan.passbox.service.LocalService.OnGetAllPasswordGroupCallback;
import com.boguan.passbox.service.LocalService.OnPasswordGroupChangeListener;
import com.boguan.passbox.utils.Constant;

import java.util.ArrayList;
import java.util.List;

public class PasswordGroupFragment extends Fragment implements OnItemClickListener, OnGetAllPasswordGroupCallback {
    private Mainbinder mMainbinder;
    private PasswordGroupAdapter mPasswordGroupAdapter;
    private OnPasswordGroupSelected mPasswordGroupSelected;
    private List<PasswordGroup> mPasswordGroups;
    private OnSettingGroupChangeListener mOnSettingGroupChangeListener;

    private OnClickListener mAddClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            GreatePasswordGroupDialog cDialog = new GreatePasswordGroupDialog(getActivity(), mMainbinder);
            cDialog.show();
            mOnSettingGroupChangeListener.onSettingGroupChangeListener(true);
        }
    };
    private OnPasswordGroupChangeListener mPasswordGroupListener = new OnPasswordGroupChangeListener() {
        @Override
        public void onNewPasswordGroup(PasswordGroup passwordGroup) {
            mPasswordGroupAdapter.addPasswordGroup(passwordGroup);
            if (mPasswordGroupAdapter.getCount() == 1) {
                selectItem(passwordGroup.getGroupName());
            }
        }

        @Override
        public void onDeletePasswordGroup(String passwordGroupName) {
            boolean result = mPasswordGroupAdapter.removePasswordGroup(passwordGroupName);
            if (result && passwordGroupName.equals(mPasswordGroupAdapter.getmCurrentGroupName())) {
                String selectedname = "";
                if (mPasswordGroupAdapter.getCount() > 0)
                    selectedname = mPasswordGroupAdapter.getItem(0).getGroupName();
                selectItem(selectedname);
            }
        }

        @Override
        public void onUpdateGroupName(String oldGroupName, String newGroupName) {
            int count = mPasswordGroupAdapter.getCount();
            boolean mHasMerge = false;
            for (int i = 0; i < count; i++) {
                PasswordGroup item = mPasswordGroupAdapter.getItem(i);
                if (item.getGroupName().equals(newGroupName)) {
                    mHasMerge = true;
                    break;
                }
            }

            if (mHasMerge) {
                // 有合并的， 移除老的分组
                for (int i = 0; i < count; i++) {
                    PasswordGroup mItem = mPasswordGroupAdapter.getItem(i);
                    if (mItem.getGroupName().equals(oldGroupName)) {
                        mPasswordGroupAdapter.removePasswordGroup(oldGroupName);
                        break;
                    }
                }

            } else {
                /** 分组变化了，改变现在的分组名称 */
                for (int i = 0; i < count; i++) {
                    PasswordGroup mItem = mPasswordGroupAdapter.getItem(i);
                    if (mItem.getGroupName().equals(oldGroupName)) {
                        mItem.setGroupName(newGroupName);
                        mPasswordGroupAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            // 当前选中的名称变了 重新加载
            if (mPasswordGroupAdapter.getmCurrentGroupName().equals(oldGroupName)
                    || mPasswordGroupAdapter.getmCurrentGroupName().equals(newGroupName)) {
                selectItem(newGroupName);
            }
        }
    };

    private OnItemLongClickListener mDeleteClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            // 长按删除密码
            final String passwordGroupName = ((PasswordGroup) (parent.getItemAtPosition(position))).getGroupName();
            if (passwordGroupName.equals(Constant.PASSWORD_GROUP_ALL_NAME)) {
                Toast.makeText(getContext(), R.string.password_group_nodelete_group, Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                CharSequence[] items = new String[]{getString(R.string.password_group_update_group_name),
                        getString(R.string.password_group_merge), getString(R.string.password_group_delete_group)};

                builder.setItems(items, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // 修改分组名
                                UpdatePasswdGroupNameDialog updatePasswdGroupName = new UpdatePasswdGroupNameDialog(
                                        getActivity(), passwordGroupName, mMainbinder);
                                updatePasswdGroupName.show();
                                break;

                            case 1:
                                mergeGroup(passwordGroupName);
                                break;

                            case 2:
                                // 删除分组
                                showDeleteDialog(passwordGroupName);
                                break;
                        }
                    }

                });
                builder.show();

            }

            return true;
        }

    };

    /**
     * 合并分组
     *
     * @param passwordGroupName 原分组名
     */
    private void mergeGroup(final String passwordGroupName) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.password_group_merge_loading));
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(false);
        progressDialog.show();

        // 获取分组回调
        OnGetAllPasswordGroupCallback onGetAllPasswordGroupCallback = new OnGetAllPasswordGroupCallback() {
            @Override
            public void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups) {
                progressDialog.dismiss();
                // 分组获取成功

                if (passwordGroups.size() <= 1) {
                    getBaseActivity().showToast(R.string.password_group_merge_error);
                    return;
                }

                // 用户选择需要合并到的分组
                final List<String> items = new ArrayList<String>();
                for (PasswordGroup passwordGroup : passwordGroups) {
                    if (!passwordGroup.getGroupName().equals(passwordGroupName)) {
                        items.add(passwordGroup.getGroupName());
                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(items.toArray(new String[items.size()]),
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newGroupName = items.get(which);
                                mMainbinder.updatePasswdGroupName(passwordGroupName, newGroupName);
                            }
                        });
                builder.show();
            }
        };

        // 获取所有的分组
        mMainbinder.getAllPasswordGroup(onGetAllPasswordGroupCallback);
    }

    /**
     * 显示删除密码分组对话框
     *
     * @param passwordGroupName 要删除的密码分组
     */
    private void showDeleteDialog(final String passwordGroupName) {
        Builder builder = new Builder(getActivity());
        builder.setMessage(getString(R.string.delete_password_group_message, passwordGroupName));
//		builder.setTitle(R.string.delete_password_group_title);
        builder.setNeutralButton(R.string.delete_password_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMainbinder.deletePasswordgroup(passwordGroupName);
            }
        });
        builder.setNegativeButton(R.string.delete_password_cancle, null);
        builder.show();
    }

    public void setDataSource(Mainbinder mainbinder, OnPasswordGroupSelected onPasswordGroupSelected) {
        this.mMainbinder = mainbinder;
        this.mPasswordGroupSelected = onPasswordGroupSelected;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPasswordGroupAdapter = new PasswordGroupAdapter(getActivity());
        mOnSettingGroupChangeListener = (OnSettingGroupChangeListener) getActivity();
        mMainbinder.registOnPasswordGroupListener(mPasswordGroupListener);
        buildGroup();
        mMainbinder.getAllPasswordGroup(this);
    }

    private void buildGroup() {
        mPasswordGroups = new ArrayList<>();
        mPasswordGroups.add(new PasswordGroup(Constant.PASSWORD_GROUP_MONEY_NAME, R.drawable.ic_financial));
        mPasswordGroups.add(new PasswordGroup(Constant.PASSWORD_GROUP_COMMUNICATION_NAME, R.drawable.ic_social));
        mPasswordGroups.add(new PasswordGroup(Constant.PASSWORD_GROUP_SHOPPING_NAME, R.drawable.ic_shopping));
        mPasswordGroups.add(new PasswordGroup(Constant.PASSWORD_GROUP_NEWS_NAME, R.drawable.ic_news));
        mPasswordGroups.add(new PasswordGroup(Constant.PASSWORD_GROUP_STUDY_NAME, R.drawable.ic_study));
        mPasswordGroups.add(new PasswordGroup(Constant.PASSWORD_GROUP_PLAY_NAME, R.drawable.ic_play));
        mPasswordGroups.add(new PasswordGroup(Constant.PASSWORD_GROUP_TRAVEL_NAME, R.drawable.ic_traffic));
        mPasswordGroups.add(new PasswordGroup(Constant.PASSWORD_GROUP_ALL_NAME, R.drawable.ic_all));
        mMainbinder.insertPasswordGroup(mPasswordGroups);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMainbinder.unregistOnPasswordGroupListener(mPasswordGroupListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_password_group, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.fragment_password_group_listView);
        listView.setAdapter(mPasswordGroupAdapter);
        listView.setVerticalScrollBarEnabled(false);
        listView.setFastScrollEnabled(false);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(mDeleteClickListener);
        View addView = rootView.findViewById(R.id.fragment_password_group_add);
        addView.setOnClickListener(mAddClickListener);
        return rootView;
    }

    private BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PasswordGroup passwordGroup = mPasswordGroupAdapter.getItem(position);
        selectItem(passwordGroup.getGroupName());
    }

    public interface OnPasswordGroupSelected {
        void onPasswordGroupSelected(String passwordGroupName);
    }

    @Override
    public void onGetAllPasswordGroup(List<PasswordGroup> passwordGroups) {
        BaseActivity baseActivity = getBaseActivity();
        if (baseActivity != null) {
            String lastGroupName = baseActivity.getSetting(SettingKey.LAST_SHOW_PASSWORDGROUP_NAME,
                    getString(R.string.password_group_default_name));
            mPasswordGroupAdapter.setmCurrentGroupName(lastGroupName);
            mPasswordGroupAdapter.setData(mBuildGruops(passwordGroups));
        }
    }

    private List<PasswordGroup> mBuildGruops(List<PasswordGroup> passwordGroups) {

        List<PasswordGroup> ps = new ArrayList<>();
        for (PasswordGroup password : passwordGroups) {
            String name = password.getGroupName();
            switch (name) {
                case Constant.PASSWORD_GROUP_MONEY_NAME:
                    password.setImgId(R.drawable.ic_financial);
                    break;
                case Constant.PASSWORD_GROUP_COMMUNICATION_NAME:
                    password.setImgId(R.drawable.ic_social);
                    break;
                case Constant.PASSWORD_GROUP_SHOPPING_NAME:
                    password.setImgId(R.drawable.ic_shopping);
                    break;
                case Constant.PASSWORD_GROUP_NEWS_NAME:
                    password.setImgId(R.drawable.ic_news);
                    break;
                case Constant.PASSWORD_GROUP_STUDY_NAME:
                    password.setImgId(R.drawable.ic_study);
                    break;
                case Constant.PASSWORD_GROUP_PLAY_NAME:
                    password.setImgId(R.drawable.ic_play);
                    break;
                case Constant.PASSWORD_GROUP_TRAVEL_NAME:
                    password.setImgId(R.drawable.ic_traffic);
                    break;
                case Constant.PASSWORD_GROUP_ALL_NAME:
                    password.setImgId(R.drawable.ic_all);
                    break;
                default:
                    password.setImgId(R.drawable.ic_play);
                    break;
            }
            ps.add(password);
        }

        return ps;
    }

    private void selectItem(String selectedname) {
        BaseActivity baseActivity = getBaseActivity();
        baseActivity.putSetting(SettingKey.LAST_SHOW_PASSWORDGROUP_NAME, selectedname);
        mPasswordGroupAdapter.setmCurrentGroupName(selectedname);
        mPasswordGroupSelected.onPasswordGroupSelected(selectedname);
    }
}
