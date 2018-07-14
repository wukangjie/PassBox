package com.boguan.passbox.activity.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.ccst.kuaipan.database.Settings;
import com.ccst.kuaipan.database.Settings.*;
import com.ccst.kuaipan.protocol.LoginProtocol;
import com.ccst.kuaipan.protocol.Protocol.*;
import com.ccst.kuaipan.protocol.data.KuaipanHTTPResponse;
import com.ccst.kuaipan.protocol.util.RequestEngine;
import com.ccst.kuaipan.protocol.util.RequestEngine.*;
import com.boguan.passbox.R;
import com.boguan.passbox.activity.MainActivity;
import com.boguan.passbox.activity.OauthActivity;
import com.boguan.passbox.dialog.ExportDialog;
import com.boguan.passbox.dialog.ImportDialog;
import com.boguan.passbox.utils.Constant;

import static com.boguan.passbox.utils.Constant.AUTO_FILE_NAME;
import static com.boguan.passbox.utils.Constant.AUTO_LOCK_PATTERN_FILE_NAME;

public class PasswordExportFragment extends Fragment implements View.OnClickListener, HttpRequestCallback{

    private String fragmentTitle;
    private String defaultHello = "default value";
    ProgressDialog mProgressDialog;

    private Button sdcardBtn;
    private Button autoBtn;
    private LinearLayout skydriveBtn;
    private TextView skydriveTxt;
    private TextView autoPromptTxt;
    private TextView showUserNameTxt;
    private CheckBox autoCheckBox;

    public static PasswordExportFragment newInstance(String s) {
        PasswordExportFragment newFragment = new PasswordExportFragment();
        Bundle bundle = new Bundle();
        bundle.putString("FragmentTitleTxt", s);
        newFragment.setArguments(bundle);
        return newFragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        fragmentTitle = args != null ? args.getString("FragmentTitleTxt") : defaultHello;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.password_export_item, container, false);

        initView(view);


        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        if (fragmentTitle.equals(Constant.TAG_FRAGMENT_IMPORT)){
            sdcardBtn.setText(R.string.import_sdcard_btn_txt);
            skydriveTxt.setText(R.string.import_skydrive_btn_txt);
            autoCheckBox.setVisibility(View.GONE);
            autoBtn.setVisibility(View.GONE);
            autoPromptTxt.setVisibility(View.GONE);
        }else if(fragmentTitle.equals(Constant.TAG_FRAGMENT_EXPORT)){
            sdcardBtn.setText(R.string.export_sdcard_btn_txt);
            skydriveTxt.setText(R.string.export_skydrive_btn_txt);
            autoCheckBox.setVisibility(View.GONE);
            autoBtn.setVisibility(View.GONE);
            autoPromptTxt.setVisibility(View.GONE);
        }else{
            autoCheckBox.setVisibility(View.VISIBLE);
            autoCheckBox.setChecked(Settings.getSettingBoolean(getActivity(), Field.IS_AUTO_KUAIPAN, false));
            autoBtn.setVisibility(View.VISIBLE);
            autoPromptTxt.setVisibility(View.VISIBLE);
            sdcardBtn.setVisibility(View.GONE);
            skydriveBtn.setVisibility(View.GONE);
        }
        Log.d("myLog",Settings.getSettingBoolean(getActivity(), Settings.Field.IS_LOGIN, false)+"");
        if (Settings.getSettingBoolean(getActivity(), Settings.Field.IS_LOGIN, false)){

            showUserNameTxt.setVisibility(View.VISIBLE);
            showUserNameTxt.setText(Settings.getSettingString(getActivity(), Field.USER_NAME, ""));
            autoBtn.setText(Settings.getSettingString(getActivity(),Field.USER_NAME,""));

        }else{
            skydriveTxt.setText(R.string.login_kuaipan_btn_txt);
            showUserNameTxt.setVisibility(View.GONE);
            autoBtn.setText(R.string.login_kuaipan_btn_txt);
        }

        if (Settings.getSettingBoolean(getActivity(),Field.IS_AUTO_KUAIPAN,false)){
            autoBtn.setEnabled(true);
            autoBtn.setBackgroundResource(R.drawable.kuaipan_login_btn);
        }else{
            autoBtn.setEnabled(false);
            autoBtn.setBackgroundColor(getResources().getColor(R.color.gray_color));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.item_export_sdcard_btn:
                if (sdcardBtn.getText().toString().contains("导入")){
                    //密码从SD卡导入

                    if (MainActivity.mainbinder == null)
                        break;
                    ImportDialog importDialog = new ImportDialog(getActivity(), MainActivity.mainbinder,Constant.TAG_IMPORT_SDCARD);
                    importDialog.show();
                }else{
                    //密码导出到SD卡

                    if (MainActivity.mainbinder == null)
                        break;
                    if (!isExistSDCard()) {

                        Toast.makeText(getActivity(), R.string.export_no_sdcard, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    ExportDialog exportDialog = new ExportDialog(getActivity(), MainActivity.mainbinder,Constant.TAG_EXPORT_SDCARD);
                    exportDialog.show();
                }


                break;
            case R.id.item_export_skydrive_layout:
                boolean flag = Settings.getSettingBoolean(getActivity(), Field.IS_LOGIN, false);
                if (flag){
                    if (skydriveTxt.getText().toString().contains("导入")){
                        //密码从网盘导入

                        if (MainActivity.mainbinder == null)
                            break;
                        ImportDialog importDialog = new ImportDialog(getActivity(), MainActivity.mainbinder,Constant.TAG_IMPORT_SKYDRIVE);
                        importDialog.show();

                    }else{
                        //密码导出到网盘
                        if (MainActivity.mainbinder == null)
                            break;
                        ExportDialog exportDialog = new ExportDialog(getActivity(), MainActivity.mainbinder, Constant.TAG_EXPORT_SKYDRIVE);
                        exportDialog.show();
                    }

                }else{
                    RequestEngine.getInstance(getActivity()).requestToken(this);

                }

                break;
            case R.id.item_export_auto_btn:
                RequestEngine.getInstance(getActivity()).requestToken(this);

                 break;

            default:
                break;
        }
    }

    //---------------------------------------------------
    //---- private method -------------------------------
    //---------------------------------------------------

    private void initView(View view){

        sdcardBtn = (Button) view.findViewById(R.id.item_export_sdcard_btn);
        skydriveBtn = (LinearLayout) view.findViewById(R.id.item_export_skydrive_layout);
        autoBtn = (Button) view.findViewById(R.id.item_export_auto_btn);
        autoPromptTxt = (TextView) view.findViewById(R.id.item_auto_prompt_txt);
        showUserNameTxt = (TextView) view.findViewById(R.id.item_export_show_username);
        skydriveTxt = (TextView) view.findViewById(R.id.item_export_skydrive);

        sdcardBtn.setOnClickListener(this);
        skydriveBtn.setOnClickListener(this);
        autoBtn.setOnClickListener(this);

        autoCheckBox = (CheckBox) view.findViewById(R.id.item_export_checkbox);
        autoCheckBox.setChecked(Settings.getSettingBoolean(getActivity(), Field.IS_AUTO_KUAIPAN, false));
        autoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.setSettingBoolean(getActivity(), Field.IS_AUTO_KUAIPAN, isChecked);
                Log.d("myLog","boolean==="+Settings.getSettingBoolean(getActivity(), Field.IS_AUTO_KUAIPAN,false));
                if (isChecked) {
                    autoBtn.setEnabled(true);
                    autoBtn.setBackgroundResource(R.drawable.kuaipan_login_btn);
                } else {
                    autoBtn.setEnabled(false);
                    autoBtn.setBackgroundColor(getResources().getColor(R.color.gray_color));
                }

            }
        });

    }

    private boolean isExistSDCard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }

    @Override
    public void onHttpResult(BaseProtocolData data) {
        if (data.isSuccess) {
            if (data.getType() == ProtocolType.REQUEST_TOKEN_PROTOCOL) {
                Intent intent = new Intent(getActivity(), OauthActivity.class);
                intent.putExtra(OauthActivity.KEY_URL, LoginProtocol.AUTH_URL + RequestEngine.getInstance(getActivity()).getSession().token.key);
                startActivityForResult(intent, 2);

            } else if (data.getType() == ProtocolType.DOWNLOAD_FILE_PROTOCOL) {
                if (data.getmPath().equals(AUTO_LOCK_PATTERN_FILE_NAME)) {
                    // 提示是否使用网盘上的手势密码
                    // Todo.

                } else if (data.getmPath().equals(AUTO_LOCK_PATTERN_FILE_NAME)) {
                    // Todo.
                    // 合并密码库逻辑
                }

            } else {
                Log.d("myLog",KuaipanHTTPResponse.getHttpErrorCodeDescribtion(data.getHttpRequestInfo().getResultCode()));
            }
        }

        mProgressDialog.dismiss();
    }

    @Override
    public void onHttpStart(BaseProtocolData data) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle("Notice");
        mProgressDialog.setMessage("waiting...");
        mProgressDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != 2) {
            return;
        }

        switch (resultCode) {
            case OauthActivity.RES_OK:
                if (autoCheckBox.isChecked()){
                    // 自动同步的场合,先下载手势密码和密码库
                    RequestEngine.getInstance(getActivity()).downloadFile(AUTO_LOCK_PATTERN_FILE_NAME, this);
                    RequestEngine.getInstance(getActivity()).downloadFile(AUTO_FILE_NAME, this);
                } else {

                }
                onResume();
                break;
            default:
                break;
        }
    }
}
