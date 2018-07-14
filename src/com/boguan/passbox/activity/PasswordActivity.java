package com.boguan.passbox.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import com.ccst.kuaipan.protocol.CreateDataProtocol.*;
import com.ccst.kuaipan.protocol.Protocol;
import com.ccst.kuaipan.protocol.util.RequestEngine;
import java.io.File;
import java.util.ArrayList;
import com.boguan.passbox.R;
import com.boguan.passbox.activity.fragment.PasswordExportFragment;
import com.boguan.passbox.model.Password;
import com.boguan.passbox.utils.Constant;


/**
 * Created by linfangzhou on 15/8/21.
 */
public class PasswordActivity extends FragmentActivity implements RequestEngine.HttpRequestCallback,Handler.Callback {


    private static final String TAG = "PasswordActivity";
    private ViewPager mPager;
    private ArrayList<Fragment> fragmentsList;
    private ImageView ivBottomLine;
    private TextView tvExport, tvImport,tvAuto;
    private Handler handler = new Handler(this);
    private ProgressDialog mProgressDialog;

    private int currIndex = 0;
    private int bottomLineWidth;

    private int offset = 0;
    private int position;
    private int positionTwo;
    private Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_export);
        resources = getResources();
        initActionBar();
        InitTextView();
        InitWidth();
        InitViewPager();
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Constant.BROADCAST_UPLOAD_FILE);
        myIntentFilter.addAction(Constant.BROADCAST_SHOW_SUCCESS_DIALOG);
        myIntentFilter.addAction(Constant.BROADCAST_TOKEN_EXPIRED_TO_LOGIN);
        myIntentFilter.addAction(Constant.BROADCAST_SHOW_FAILED_DIALOG);
        myIntentFilter.addAction(Constant.BROADCAST_AUTO_DOWNLOAD);
        myIntentFilter.addAction(Constant.BROADCAST_SHOW_NETWORK_ERROR_DIALOG);
        registerReceiver(mykuaipanReceiver, myIntentFilter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // TODO Auto-generated method stub
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onHttpResult(Protocol.BaseProtocolData data) {
        mProgressDialog.dismiss();
        if (data.getType() == Protocol.ProtocolType.UPLOAD_FILE_PROTOCOL){
            UploadFileProtocol protocol = (UploadFileProtocol)data;
            if (protocol.isSuccess){
                File file = new File(data.getmPath());
                deleteSDCardFile(file);
            }else{
                showFailedDialog();
            }
        }


    }

    @Override
    public void onHttpStart(Protocol.BaseProtocolData data) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Notice");
        mProgressDialog.setMessage("waiting...");
        mProgressDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mykuaipanReceiver);
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void InitTextView() {
        tvImport = (TextView) findViewById(R.id.tv_tab_activity);
        tvExport = (TextView) findViewById(R.id.tv_tab_groups);
        tvAuto = (TextView) findViewById(R.id.tv_tab_auto_kuaipan);

        tvImport.setOnClickListener(new MyOnClickListener(0));
        tvExport.setOnClickListener(new MyOnClickListener(1));
        tvAuto.setOnClickListener(new MyOnClickListener(2));

    }

    private void InitViewPager() {
        mPager = (ViewPager) findViewById(R.id.mViewPager);
        fragmentsList = new ArrayList<Fragment>();

        Fragment importFragment = PasswordExportFragment.newInstance(Constant.TAG_FRAGMENT_IMPORT);
        Fragment exportFragment = PasswordExportFragment.newInstance(Constant.TAG_FRAGMENT_EXPORT);
        Fragment autoFragment = PasswordExportFragment.newInstance(Constant.TAG_FRAGMENT_AUTO);


        fragmentsList.add(importFragment);
        fragmentsList.add(exportFragment);
        fragmentsList.add(autoFragment);


        mPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentsList));
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    private void InitWidth() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;

        ivBottomLine = (ImageView) findViewById(R.id.iv_bottom_line);
        ivBottomLine.getLayoutParams().width = screenW / 3;
        bottomLineWidth = ivBottomLine.getLayoutParams().width;

        Log.d(TAG, "cursor imageview width=" + bottomLineWidth);


        offset = (int) ((screenW / 2.0 - bottomLineWidth) / 2);
        Log.i("MainActivity", "offset=" + offset);

        position = (int) (screenW / 3.0);
        positionTwo = (int) (screenW / 3.0) + position;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case R.id.msg_import_by_skydrive:

                ArrayList<Password> passwords = (ArrayList<Password>) msg.obj;
                Log.d("myLog", "passwords=====" + passwords);

                if (passwords!=null){
                    for (Password password : passwords) {
                        MainActivity.mainbinder.insertPassword(password);
                    }
                }else{
                    Log.d("myLog","passwords===null");
                }
                break;
            default:
                break;
        }
        return true;
    }


    public class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
        }
    }

    ;

    public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragmentsList;

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
            super(fm);
            this.fragmentsList = fragments;
        }

        @Override
        public int getCount() {
            return fragmentsList.size();
        }

        @Override
        public Fragment getItem(int arg0) {
            return fragmentsList.get(arg0);
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

    }


    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int arg0) {
            Animation animation = null;
            switch (arg0) {
                case 0:
                    if (currIndex == 1) {
                        animation = new TranslateAnimation(position, 0, 0, 0);
                        tvImport.setTextColor(getResources().getColor(R.color.view_pager_line));
                    }else if(currIndex == 2){
                        animation = new TranslateAnimation(positionTwo, 0, 0, 0);
                        tvImport.setTextColor(getResources().getColor(R.color.view_pager_line));
                    }
                    tvExport.setTextColor(resources.getColor(R.color.white));
                    tvAuto.setTextColor(resources.getColor(R.color.white));
                    break;
                case 1:
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, position, 0, 0);
                        tvExport.setTextColor(getResources().getColor(R.color.view_pager_line));
                    }else if(currIndex == 2){
                        animation = new TranslateAnimation(positionTwo, position, 0, 0);
                        tvExport.setTextColor(getResources().getColor(R.color.view_pager_line));
                    }

                    tvImport.setTextColor(resources.getColor(R.color.white));
                    tvAuto.setTextColor(resources.getColor(R.color.white));
                    break;
                case 2:
                    if(currIndex == 0){
                        animation = new TranslateAnimation(offset, positionTwo, 0, 0);
                        tvAuto.setTextColor(getResources().getColor(R.color.view_pager_line));
                    }else if(currIndex == 1){
                        animation = new TranslateAnimation(position, positionTwo, 0, 0);
                        tvAuto.setTextColor(getResources().getColor(R.color.view_pager_line));
                    }
                    tvExport.setTextColor(resources.getColor(R.color.white));
                    tvImport.setTextColor(resources.getColor(R.color.white));
                    break;
            }
            currIndex = arg0;
            animation.setFillAfter(true);
            animation.setDuration(300);
            ivBottomLine.startAnimation(animation);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }


    //接收上传文件广播
    public BroadcastReceiver mykuaipanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constant.BROADCAST_UPLOAD_FILE)){
                String path = Environment.getExternalStorageDirectory().getPath()+"/MyPassword/"+intent.getStringExtra(Constant.INTENT_KEY_FILENAME);
                RequestEngine.getInstance(PasswordActivity.this).uploadFile(path,PasswordActivity.this);
            }else if(action.equals(Constant.BROADCAST_SHOW_SUCCESS_DIALOG)){

                if (intent.getStringExtra(Constant.INTENT_KEY_FILENAME).contains("auto")){
                    return;
                }
                showSuccessDialog(intent.getStringExtra(Constant.INTENT_KEY_FILENAME));
            }else if(action.equals(Constant.BROADCAST_TOKEN_EXPIRED_TO_LOGIN)){

                RequestEngine.getInstance(PasswordActivity.this).requestToken(PasswordActivity.this);
            }else if(action.equals(Constant.BROADCAST_SHOW_FAILED_DIALOG)){

                showFailedDialog();
            }else if(action.equals(Constant.BROADCAST_AUTO_DOWNLOAD)){
                RequestEngine.getInstance(PasswordActivity.this).downloadFile(Constant.AUTO_FILE_NAME, PasswordActivity.this);
            }else if(action.equals(Constant.BROADCAST_SHOW_NETWORK_ERROR_DIALOG)){
                Dialog dialog = new AlertDialog.Builder(PasswordActivity.this).setMessage(R.string.dialog_network_error_txt).setNeutralButton(R.string.i_known,null).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        }
    };
    public void showSuccessDialog(String filename){
        String msg = this.getString(R.string.export_successful, filename);
        Dialog dialog = new AlertDialog.Builder(this).setMessage(msg).setNeutralButton(R.string.i_known,null).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    public void showFailedDialog(){
        Dialog dialog = new AlertDialog.Builder(this).setMessage(R.string.dialog_failed_txt).setNeutralButton(R.string.i_known,null).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public static void deleteSDCardFile(File file){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            if (file.exists() && file.isFile()){
                file.delete();
            }
        }
    }

}
