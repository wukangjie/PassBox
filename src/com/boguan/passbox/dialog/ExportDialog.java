package com.boguan.passbox.dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.boguan.passbox.R;
import com.boguan.passbox.model.AsyncResult;
import com.boguan.passbox.model.AsyncSingleTask;
import com.boguan.passbox.model.Password;
import com.boguan.passbox.service.LocalService.Mainbinder;
import com.boguan.passbox.service.LocalService.OnGetAllPasswordCallback;
import com.boguan.passbox.utils.Constant;
import com.boguan.passbox.utils.PasswordFileUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 密码导出对话框
 * 
 * @author zengdexing
 */
public class ExportDialog extends ProgressDialog implements OnGetAllPasswordCallback{
	private Mainbinder mainbinder;

	/** 导出文件名的格式化 */
	private SimpleDateFormat fileNameFormat;
	private String tag = "";

	public ExportDialog(Context context, Mainbinder mainbinder,String tag) {
		super(context);
		this.mainbinder = mainbinder;
		this.tag = tag;

		setProgressStyle(ProgressDialog.STYLE_SPINNER);
		setMessage(getString(R.string.export_ing));
		setCancelable(false);

		fileNameFormat = new SimpleDateFormat(getString(R.string.export_filename), Locale.getDefault());

	}

	@Override
	public void show() {
		Builder builder= new Builder(getContext());
		if (tag.equals(Constant.TAG_EXPORT_SKYDRIVE)){
			builder.setTitle(R.string.export_to_skydrive);
			builder.setMessage(R.string.export_dialog_attention_to_skydrive);
			builder.setNeutralButton(R.string.i_known, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ExportDialog.super.show();
					mainbinder.getAllPassword(ExportDialog.this);
				}
			});
			builder.setNegativeButton(R.string.export_dialog_cancle, null);
		}else{
			builder.setTitle(R.string.export_to_sd);
			builder.setMessage(R.string.export_dialog_attention_to_sdcard);
			builder.setNeutralButton(R.string.i_known, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ExportDialog.super.show();
					mainbinder.getAllPassword(ExportDialog.this);
				}
			});
			builder.setNegativeButton(R.string.export_dialog_cancle, null);
		}

		builder.show();
	}

	@Override
	public void onGetAllPassword(String groupName, final List<Password> passwords) {
		final List<Password> tempPasswords = new ArrayList<Password>(passwords);
		new AsyncSingleTask<File>() {
			@Override
			protected AsyncResult<File> doInBackground(AsyncResult<File> asyncResult) {
				String fileName = getFileName();
				File file = new File(fileName);
				file.getParentFile().mkdirs();
				file.deleteOnExit();
				PrintWriter printWriter = null;
				try {
					file.createNewFile();
					printWriter = new PrintWriter(file);
//					for (Password password : tempPasswords) {
//						printWriter.println(password.toJSON());
//					}
					StringBuffer buffer = new StringBuffer();
					for (Password password :tempPasswords){
						buffer.append(password.toJSON()+"\n");
					}
					Log.d("myLog","加密前："+buffer.toString());
					String content =PasswordFileUtil.encryptPassword(buffer.toString());
					Log.d("myLog","加密后："+content);
					printWriter.println(content);
					asyncResult.setData(file);
					asyncResult.setResult(0);
				} catch (IOException e) {
					asyncResult.setResult(-1);
					e.printStackTrace();
				} finally {
					if (printWriter != null) {
						printWriter.close();
					}
				}
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<File> asyncResult) {
				dismiss();
				if (asyncResult.getResult() == 0) {
					// 导出成功
					Intent intent;
					String fileName = asyncResult.getData().getName();
					if (tag.equals(Constant.TAG_EXPORT_SKYDRIVE)){

						intent = new Intent(Constant.BROADCAST_UPLOAD_FILE);
						intent.putExtra(Constant.INTENT_KEY_FILENAME,fileName);
						getContext().sendBroadcast(intent);
					}else if(tag.equals(Constant.TAG_EXPORT_AUTO_KUAIPAN)){
						Log.d("myLog","自动上传");
						intent = new Intent(Constant.BROADCAST_AUTO_UPLOAD);
						intent.putExtra(Constant.INTENT_KEY_FILENAME, fileName);
						getContext().sendBroadcast(intent);

					} else{
						intent = new Intent(Constant.BROADCAST_SHOW_SUCCESS_DIALOG);
						intent.putExtra(Constant.INTENT_KEY_FILENAME, fileName);
						getContext().sendBroadcast(intent);
					}

				} else {
					// 导出失败！
					String msg = getContext().getString(R.string.toast_export_failed);
					Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
				}
			}
		}.execute();
	}

	private String getString(int id) {
		return getContext().getString(id);
	}

	/** 获得本地文件保存名 */
	private String getFileName() {
		String fileName = fileNameFormat.format(new Date());

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(Environment.getExternalStorageDirectory().getAbsolutePath());
		stringBuilder.append(File.separator);
		stringBuilder.append("MyPassword");
		stringBuilder.append(File.separator);
		if (tag.equals(Constant.TAG_EXPORT_AUTO_KUAIPAN)){
			String autoFileName = "auto_passwords";
			stringBuilder.append(autoFileName);
		}else{
			stringBuilder.append(fileName);
		}

		stringBuilder.append(".mp");
		return stringBuilder.toString();
	}

	public void autoUpload(){
		mainbinder.getAllPassword(ExportDialog.this);
	}

}
