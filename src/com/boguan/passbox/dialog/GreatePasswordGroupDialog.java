package com.boguan.passbox.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import com.boguan.passbox.R;
import com.boguan.passbox.model.PasswordGroup;
import com.boguan.passbox.service.LocalService.Mainbinder;
import cn.zdx.lib.annotation.FindViewById;
import cn.zdx.lib.annotation.ViewFinder;
import cn.zdx.lib.annotation.XingAnnotationHelper;

/**
 * 创建密码分组对话框
 */
public class GreatePasswordGroupDialog extends Dialog {

	@FindViewById(R.id.add_passwrdGroup_editview)
	private EditText editText;
	private Mainbinder mainbinder;
	@FindViewById(R.id.add_password_group_cancle_btn)
	private View cancleButton;
	@FindViewById(R.id.add_password_group_sure_btn)
	private View sureButton;
	@FindViewById(R.id.container)
	private View container;
	@FindViewById(R.id.add_password_line)
	private View mLineView;
	private View.OnClickListener onCancleClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			dismiss();
		}
	};

	private View.OnClickListener onSureClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String name = editText.getText().toString().trim();
			if (!name.equals("")) {
				PasswordGroup passwordGroup = new PasswordGroup();
				passwordGroup.setGroupName(name);
				passwordGroup.setImgId(R.drawable.ic_play);
				mainbinder.insertPasswordGroup(passwordGroup);
				dismiss();
			}
		}
	};

	public GreatePasswordGroupDialog(Context context, Mainbinder mainbinder) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
						| WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		this.mainbinder = mainbinder;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_create_password_group);
		XingAnnotationHelper.findView(this, ViewFinder.create(this));
		initView();
	}

	private void initView() {
		cancleButton.setOnClickListener(onCancleClickListener);
		container.setOnClickListener(onCancleClickListener);
		sureButton.setOnClickListener(onSureClickListener);
		editText.requestFocus();
	}

}
