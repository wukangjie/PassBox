package com.boguan.passbox.activity.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Dimension;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import com.boguan.passbox.R;
import com.boguan.passbox.activity.EditPasswordActivity;
import com.boguan.passbox.adapter.PasswordListAdapter;
import com.boguan.passbox.app.OnSettingChangeListener;
import com.boguan.passbox.bean.JsonBean;
import com.boguan.passbox.model.Password;
import com.boguan.passbox.model.SettingKey;
import com.boguan.passbox.service.LocalService.Mainbinder;
import com.boguan.passbox.service.LocalService.OnGetAllPasswordCallback;
import com.boguan.passbox.service.LocalService.OnPasswordChangeListener;
import com.boguan.passbox.service.OnStartDragListener;
import com.boguan.passbox.service.SimpleItemTouchHelperCallback;
import com.boguan.passbox.utils.Constant;
import com.boguan.passbox.utils.JsonUtils;
import com.ccst.kuaipan.database.Settings;
import com.ccst.kuaipan.database.Settings.Field;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 密码列表展示界面
 *
 * @author zengdexing
 */
public class PasswordListFragment extends BaseFragment implements OnSettingChangeListener,
        View.OnClickListener, OnStartDragListener, OnGetAllPasswordCallback {
    /**
     * 数据
     */
    private PasswordListAdapter mMainAdapter;
    /**
     * 数据源
     */
    private Mainbinder mMainbinder;
    private RecyclerView mListView;
    /**
     * 没有数据的提示框
     */
    private View mNoDataView;
    private String mPasswordGroupName;
    private Intent mIntent;
    private ArrayList<JsonBean> mJsonList = new ArrayList<>();
    final static String mPath = "keyword.json";
    private ItemTouchHelper mItemTouchHelper;

    public void setDataSource(Mainbinder mainbinder) {
        this.mMainbinder = mainbinder;
    }

    public void showPasswordGroup(String passwordGroupName) {
        this.mPasswordGroupName = passwordGroupName;
        if (passwordGroupName.equals(Constant.PASSWORD_GROUP_ALL_NAME))
            mMainbinder.getAllPassword(this);
        else
            mMainbinder.getAllPassword(this, passwordGroupName);
    }

    private OnPasswordChangeListener mOnPasswordListener = new OnPasswordChangeListener() {
        @Override
        public void onNewPassword(Password password) {
            if (password.getGroupName().equals(mPasswordGroupName)) {
                mMainAdapter.onNewPassword(password);
                initView();
                if (Settings.getSettingBoolean(getActivity(), Field.IS_AUTO_KUAIPAN, false)) {
                    mIntent = new Intent(Constant.BROADCAST_AUTO_KUAIPAN);
                    getActivity().sendBroadcast(mIntent);
                }
            }
        }

        @Override
        public void onDeletePassword(int id) {
            mMainAdapter.onDeletePassword(id);
            initView();
            if (Settings.getSettingBoolean(getActivity(), Field.IS_AUTO_KUAIPAN, false)) {
                mIntent = new Intent(Constant.BROADCAST_AUTO_KUAIPAN);
                getActivity().sendBroadcast(mIntent);
            }

        }

        @Override
        public void onDeleteAllPassword() {
            mMainAdapter.onDeleteAllPassword();
            initView();
        }

        @Override
        public void onUpdatePassword(Password newPassword) {
            mMainAdapter.onUpdatePassword(newPassword);
            initView();
            if (Settings.getSettingBoolean(getActivity(), Field.IS_AUTO_KUAIPAN, false)) {
                mIntent = new Intent(Constant.BROADCAST_AUTO_KUAIPAN);
                getActivity().sendBroadcast(mIntent);
            }

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainAdapter = new PasswordListAdapter(getActivity(),getScreenHeight(getActivity()) - getBottomStatusHeight(getActivity()) - getTitleHeight(getActivity()));
        getBaseActivity().getMyApplication().registOnSettingChangeListener(SettingKey.JAZZY_EFFECT, this);
        mMainbinder.registOnPasswordListener(mOnPasswordListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregistOnPasswordListener();
        getBaseActivity().getMyApplication().unregistOnSettingChangeListener(SettingKey.JAZZY_EFFECT, this);
    }

    private void unregistOnPasswordListener() {
        if (mMainbinder != null) {
            mMainbinder.unregistOnPasswordListener(mOnPasswordListener);
            mMainbinder = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showPasswordGroup(getBaseActivity().getSetting(SettingKey.LAST_SHOW_PASSWORDGROUP_NAME,
                getString(R.string.password_group_default_name)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_password_list, container, false);
        mListView = (RecyclerView) rootView.findViewById(R.id.main_listview);

        mListView.setAdapter(mMainAdapter);
        String jsonStr = JsonUtils.getJson(getBaseActivity(), mPath);
        Gson gson = new Gson();
        Type type = new TypeToken<List<JsonBean>>() {
        }.getType();
        mJsonList = gson.fromJson(jsonStr, type);
        mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mMainAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mListView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mListView.setNestedScrollingEnabled(true);
        }
        mNoDataView = rootView.findViewById(R.id.main_no_passsword);
        mNoDataView.setOnClickListener(this);
        if (mMainbinder == null) {
            mNoDataView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        } else {
            initView();
        }

        return rootView;
    }

    private void initView() {
        if (mNoDataView != null) {
            if (mMainAdapter.getCount() == 0) {
                mNoDataView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
            } else {
                mNoDataView.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListView = null;
        mNoDataView = null;
    }

    @Override
    public void onGetAllPassword(String groupName, List<Password> passwords) {
        if (mPasswordGroupName.equals(groupName) || groupName == null) {
            mMainAdapter.setPasswordGroup(mPasswordGroupName);
            mMainAdapter.setData(mJsonList, passwords, mMainbinder);
            initView();
        }
    }

    @Override
    public void onSettingChange(SettingKey key) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_no_passsword:             //无数据时添加数据
                Intent intent = new Intent(getActivity(), EditPasswordActivity.class);
                intent.putExtra(EditPasswordActivity.PASSWORD_GROUP, mPasswordGroupName);
                getActivity().startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
    public static int getDpi(Context context){
        int dpi = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics",DisplayMetrics.class);
            method.invoke(display, displayMetrics);
            dpi=displayMetrics.heightPixels;
        }catch(Exception e){
            e.printStackTrace();
        }
        return dpi;
    }

    /**
     * 获取 虚拟按键的高度
     * @param context
     * @return
     */
    public static  int getBottomStatusHeight(Context context){
        int totalHeight = getDpi(context);

        int contentHeight = getScreenHeight(context);

        return totalHeight  - contentHeight;
    }

    /**
     * 标题栏高度
     * @return
     */
    public static int getTitleHeight(Activity activity){
        return  activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }
}
