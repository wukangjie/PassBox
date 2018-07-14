package com.boguan.passbox.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.boguan.passbox.R;
import com.boguan.passbox.utils.Anime;
import com.ccst.kuaipan.database.Settings;

import cn.zdx.lib.annotation.FindViewById;

/**
 * Created by linfangzhou on 15/8/31.
 */
public class LoginKuaiPanActivity extends BaseActivity {

    @FindViewById(R.id.btn_login_kuaipan)
    private Button loginBtn;

    @FindViewById(R.id.btn_login_ingore)
    private Button ingoreBtn;

    @FindViewById(R.id.kuaipan_title_layout)
    private View topLogo;

    @FindViewById(R.id.castle_layout)
    private View castleLogo;

    @FindViewById(R.id.bottom_btn_layout)
    private View bottomLayout;

    @FindViewById(R.id.kuaipan_tips)
    private View bottomTips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kuaipan_login, Style.HIDE_ACTION_BAR);

        initView();
    }

    private void initView(){
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(LoginKuaiPanActivity.this)
                        .title(R.string.dialog_declare_title)
                        .backgroundColor(getResources().getColor(R.color.app_background_dark))
                        .customView(R.layout.dialog_law_delcare, true)
                        .positiveText(R.string.option_agree)
                        .negativeText(R.string.option_deny)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                Intent intent = new Intent(LoginKuaiPanActivity.this, CloudOptionsActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .show();
            }
        });

        ingoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Settings.setSettingBoolean(getActivity(), Settings.Field.IS_AUTO_KUAIPAN, false);
                startActivity(new Intent(LoginKuaiPanActivity.this, SetLockpatternActivity.class));
                finish();
            }
        });

        Anime.alphaInAnimation(null, topLogo, bottomLayout, bottomTips);
        Anime.translateAnimation(castleLogo);
    }
}
