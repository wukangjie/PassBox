package com.boguan.passbox.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import com.boguan.passbox.R;
import com.boguan.passbox.adapter.ViewHolder;
import com.boguan.passbox.model.Password;
import com.boguan.passbox.model.PasswordItem;
import com.boguan.passbox.service.LocalService.Mainbinder;
import com.boguan.passbox.view.FoldableLayout;

import cn.zdx.lib.annotation.FindViewById;
import cn.zdx.lib.annotation.ViewFinder;
import cn.zdx.lib.annotation.XingAnnotationHelper;

/**
 * 创建密码分组对话框
 */
public class PasswordDialog extends Dialog {

    private Mainbinder mainbinder;
    private Password password;
    private Context myContext;
    
    @FindViewById(R.id.btn_close)
    private View closeButton;

    public PasswordDialog(Context context, Mainbinder mainbinder, Password p) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        this.mainbinder = mainbinder;
        this.password = p;
        this.myContext = context;
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_password_detail);
        initView();
    }

    private void initView() {
        XingAnnotationHelper.findView(this, ViewFinder.create(this));
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dismiss();
            }
        });
        ViewHolder viewHolder = new ViewHolder(new FoldableLayout(myContext), myContext, password.getGroupName(), mainbinder,0);
        XingAnnotationHelper.findView(viewHolder, ViewFinder.create(this));
        viewHolder.mCopyView.setOnClickListener(viewHolder);
        viewHolder.mDeleteView.setOnClickListener(viewHolder);
        viewHolder.mEditView.setOnClickListener(viewHolder);
        viewHolder.bindView(new PasswordItem(getContext(), password), 0, null, null);
        
    }

}
