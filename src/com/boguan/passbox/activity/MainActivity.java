package com.boguan.passbox.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.boguan.passbox.R;
import com.boguan.passbox.activity.fragment.PasswordGroupFragment;
import com.boguan.passbox.activity.fragment.PasswordListFragment;
import com.boguan.passbox.adapter.DrawerAdapter;
import com.boguan.passbox.model.OnSettingGroupChangeListener;
import com.boguan.passbox.service.LocalService.MainService;
import com.boguan.passbox.service.LocalService.Mainbinder;
import com.boguan.passbox.utils.Constant;

import cn.zdx.lib.annotation.FindViewById;

/**
 * 主界面
 *
 * @author zengdexing
 */
public class MainActivity extends BaseActivity implements View.OnClickListener, OnSettingGroupChangeListener, AppBarLayout.OnOffsetChangedListener {
    /**
     * 数据源
     */
    public static Mainbinder mainbinder;
    private long mLastBackKeyTime;
    @FindViewById(R.id.drawer_layout)
    private DrawerLayout mDrawerLayout;
    @FindViewById(R.id.fragment_password_pop)
    private FrameLayout mPopLayout;
    @FindViewById(R.id.main_toolbar)
    private Toolbar mToolbar;
    @FindViewById(R.id.main_fab)
    private FloatingActionButton mFab;
    @FindViewById(R.id.rv_drawer)
    private RecyclerView mRvDrawer;
    @FindViewById(R.id.main_title_center_tv)
    private TextView mCenterTitle;
    @FindViewById(R.id.main_search_right)
    private ImageView mSearchImg;
    @FindViewById(R.id.main_frame_back)
    private FrameLayout mBackFrameLayout;
    @FindViewById(R.id.main_title_center_img)
    private ImageView mCenterImg;
    @FindViewById(R.id.main_appbar)
    private AppBarLayout mAppBar;

    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mIsTextOpen;
    private DrawerAdapter mDrawerAdapter;
    private PasswordListFragment mPasswordListFragment;
    private PasswordGroupFragment mPasswordGroupFragment;
    private FragmentManager mFragmentManager;
    private PasswordGroupFragment.OnPasswordGroupSelected mOnPasswordGroupSelected = new PasswordGroupFragment.OnPasswordGroupSelected() {
        @Override
        public void onPasswordGroupSelected(String passwordGroupName) {
            mIsTextOpen = true;
            showGroup();
            mCenterTitle.setText(passwordGroupName);
            if (mPasswordListFragment != null)
                mPasswordListFragment.showPasswordGroup(passwordGroupName);
        }
    };
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mainbinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mainbinder = (Mainbinder) service;
            initFragment();
        }
    };

    private void initFragment() {
        mFragmentManager = getFragmentManager();
        mPasswordListFragment = (PasswordListFragment) mFragmentManager.findFragmentByTag("PasswordListFragment");
        if (mPasswordListFragment == null)
            mPasswordListFragment = new PasswordListFragment();
        mPasswordListFragment.setDataSource(mainbinder);
        mPasswordGroupFragment = (PasswordGroupFragment) mFragmentManager.findFragmentByTag("PasswordGroupFragment");
        if (mPasswordGroupFragment == null)
            mPasswordGroupFragment = new PasswordGroupFragment();
        mPasswordGroupFragment.setDataSource(mainbinder, mOnPasswordGroupSelected);
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, mPasswordListFragment, "PasswordListFragment");
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_main);
        mToolbar.setTitle("");
        initDrawer();
        initClick();
        Intent intent = new Intent(this, MainService.class);
        this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.BROADCAST_AUTO_KUAIPAN);
        intentFilter.addAction(Constant.BROADCAST_AUTO_UPLOAD);
        mAppBar.addOnOffsetChangedListener(this);
    }

    private void initClick() {
        mCenterTitle.setOnClickListener(this);
        mSearchImg.setOnClickListener(this);
        mFab.setOnClickListener(this);
        mBackFrameLayout.setOnClickListener(this);
    }

    private void initDrawer() {
        mDrawerAdapter = new DrawerAdapter();
        mDrawerAdapter.setOnItemClickListener(new MyOnItemClickListener());
        mRvDrawer.setLayoutManager(new LinearLayoutManager(this));
        mRvDrawer.setAdapter(mDrawerAdapter);
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    /**
     * 双击退出程序
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return false;
        } else {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    long delay = Math.abs(System.currentTimeMillis() - mLastBackKeyTime);
                    if (delay > 4000) {
                        // 双击退出程序
                        showToast(R.string.toast_key_back);
                        mLastBackKeyTime = System.currentTimeMillis();
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }

        return super.onKeyDown(keyCode, event);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_search_right:
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.main_fab:
                Intent intent2 = new Intent(MainActivity.this, EditPasswordActivity.class);
                startActivity(intent2);
                break;
            case R.id.main_title_center_tv:
                showGroup();
                break;
            case R.id.main_frame_back:
                mIsTextOpen = true;
                showGroup();
                break;
        }
    }

    /**
     * 打开分组
     */
    private void showGroup() {
        if (!mIsTextOpen) {
            mPopLayout.setVisibility(View.VISIBLE);
            mBackFrameLayout.setVisibility(View.VISIBLE);
            Drawable nav_up = getResources().getDrawable(R.drawable.ic_up);
            mCenterImg.setImageDrawable(nav_up);
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_password_pop, mPasswordGroupFragment, "PasswordGroupFragment");
            fragmentTransaction.commitAllowingStateLoss();
            mIsTextOpen = true;
        } else {
            Drawable nav_down = getResources().getDrawable(R.drawable.ic_arrow);
            mCenterImg.setImageDrawable(nav_down);
            mPopLayout.setVisibility(View.GONE);
            mBackFrameLayout.setVisibility(View.GONE);
            mIsTextOpen = false;
        }
    }

    @Override
    public void onSettingGroupChangeListener(boolean isClose) {
        if (isClose) {
            mPopLayout.setVisibility(View.GONE);
            Drawable nav_down = getResources().getDrawable(R.drawable.ic_arrow);
            mCenterImg.setImageDrawable(nav_down);
            mBackFrameLayout.setVisibility(View.GONE);
            mIsTextOpen = false;
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset < 0)
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        else
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }


    private class MyOnItemClickListener implements DrawerAdapter.OnItemClickListener {
        @Override
        public void itemClick(DrawerAdapter.DrawerItemNormal drawerItemNormal) {

            switch (drawerItemNormal.titleRes) {
                case R.string.action_my_collection:
                    Toast.makeText(MainActivity.this, R.string.action_my_collection, Toast.LENGTH_SHORT).show();
                    break;
                case R.string.action_login_password:
                    // 软件锁
                    startActivity(new Intent(getActivity(), SetLockpatternActivity.class));
                    break;
                case R.string.action_about_us:
                    // 关于
                    startActivity(new Intent(getActivity(), AboutActivity.class));
                    break;
                case R.string.action_exit:
                    // 退出
                    finish();
                    break;
                case R.string.action_import_and_export:
                    Intent intent1 = new Intent(getActivity(), PasswordActivity.class);
                    startActivity(intent1);
                    break;
                default:
                    break;
            }
            mDrawerAdapter.notifyDataSetChanged();
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }
}
