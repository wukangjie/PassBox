package com.boguan.passbox.dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

import com.boguan.passbox.R;
import com.boguan.passbox.model.AsyncResult;
import com.boguan.passbox.model.AsyncSingleTask;
import com.boguan.passbox.model.Password;
import com.boguan.passbox.service.LocalService.Mainbinder;
import com.boguan.passbox.utils.Constant;
import com.boguan.passbox.utils.PasswordFileUtil;
import com.ccst.kuaipan.protocol.Protocol;
import com.ccst.kuaipan.protocol.util.RequestEngine;
import com.ccst.kuaipan.protocol.util.RequestEngine.HttpRequestCallback;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 导入对话框
 * 
 * @author zengdexing
 * 
 */
public class ImportDialog extends ProgressDialog implements Callback,HttpRequestCallback {
	private Mainbinder mainbinder;
	private Handler handler = new Handler(this);
	private String tag = "";

	public ImportDialog(Context context, Mainbinder mainbinder,String tag) {
		super(context);
		setProgressStyle(ProgressDialog.STYLE_SPINNER);
		setCancelable(false);
		this.mainbinder = mainbinder;
		this.tag = tag;
	}


	private String getString(int id) {
		return getContext().getString(id);
	}

	private String getString(int id, Object... obj) {
		return getContext().getString(id, obj);
	}

	/**
	 * 获取密码保存目录
	 * 
	 * @return
	 */
	private File getRootFile() {
		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyPassword/");
		return file;
	}

	/**
	 * 搜索目录所有的“mp”文件
	 * 
	 * @param aFile
	 *            要搜索的文件夹
	 * @return 该文件夹下保存的文件
	 */
	private ArrayList<SearchResult> searchFile(File aFile) {
		ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();
		File[] files = aFile.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.getName().endsWith(".mp")) {
					if (!file.getName().contains("auto")){
						SearchResult searchResult = new SearchResult();
						searchResult.name = file.getName();
						searchResult.absoluteFilePath = file.getAbsolutePath();
						searchResults.add(searchResult);
					}

				}
			}
		}
		return searchResults;
	}

	/**
	 * 显示
	 */
	@Override
	public void show() {
		if (tag.equals(Constant.TAG_IMPORT_SKYDRIVE)){
			setMessage(getString(R.string.import_search_file_to_skydrive));
		}else{
			setMessage(getString(R.string.import_search_file_to_sdcard));
		}

		super.show();
		handler.sendEmptyMessageDelayed(R.id.msg_import_search_delay_500, 500);
	}

	@Override
	public void dismiss() {
		super.dismiss();
	}

	/**
	 * 搜索文件
	 */
	private void searchFile() {
		new AsyncSingleTask<ArrayList<SearchResult>>() {
			@Override
			protected AsyncResult<ArrayList<SearchResult>> doInBackground(
					AsyncResult<ArrayList<SearchResult>> asyncResult) {
				ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();

				File rootFile = getRootFile();
				searchResults.addAll(searchFile(rootFile));
				searchResults.addAll(searchFile(Environment.getExternalStorageDirectory()));
				asyncResult.setData(searchResults);
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<ArrayList<SearchResult>> asyncResult) {
				dismiss();
				final ArrayList<SearchResult> searchResults = asyncResult.getData();
				if (searchResults.size() == 0) {
					// 搜索失败
					Builder builder = new Builder(getContext());
					builder.setMessage(getString(R.string.import_search_failed_to_sdcard));
					builder.setNegativeButton(R.string.import_sure, null);
					builder.show();
				} else {
					// 搜索成功，用户选择文件
					Builder builder = new Builder(getContext());
					builder.setTitle(R.string.import_chiose_file);
					builder.setItems(getItems(searchResults), new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							SearchResult searchResult = searchResults.get(which);
							Message message = handler.obtainMessage(R.id.msg_import, searchResult);
							message.sendToTarget();
						}
					});
					builder.show();
				}
			}
		}.execute();
	}

	/**
	 * searchResults转化为String[]
	 * 
	 * @param searchResults
	 * @return
	 */
	private String[] getItems(ArrayList<SearchResult> searchResults) {
		String[] result = new String[searchResults.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = searchResults.get(i).name;
		}
		return result;
	}

	@Override
	public void onHttpResult(Protocol.BaseProtocolData data) {
		super.dismiss();
		if (data.getType() == Protocol.ProtocolType.GET_FILE_PROTOCOL){
			if (data.isSuccess) {
				Log.d("myLog","Get File Success");
				showFileListDialog((ArrayList<String>) data.getAttachedObject());
			}else{
				Intent intent = new Intent(Constant.BROADCAST_SHOW_FAILED_DIALOG);
				getContext().sendBroadcast(intent);
			}
		}else if (data.getType() == Protocol.ProtocolType.DOWNLOAD_FILE_PROTOCOL) {
			if (data.isSuccess) {
				Log.d("myLog","Download File Success");
				importFileFromSkyDrive((ArrayList<Password>)data.getAttachedObject());
			}
		}
	}

	@Override
	public void onHttpStart(Protocol.BaseProtocolData data) {
		if (data.getType() == Protocol.ProtocolType.DOWNLOAD_FILE_PROTOCOL) {
			setMessage("正在导入...");
		}
		super.show();
	}

	/** 搜索结果 */
	private static class SearchResult {
		/** 文件名 */
		String name;
		/** 文件的绝对路径 */
		String absoluteFilePath;
	}

	/** 导入文件  sdcard*/
	private void importFile(final SearchResult searchResult) {
		new AsyncSingleTask<ArrayList<Password>>() {
			@Override
			protected AsyncResult<ArrayList<Password>> doInBackground(AsyncResult<ArrayList<Password>> asyncResult) {
				FileInputStream fileInputStream = null;
				try {
					fileInputStream = new FileInputStream(searchResult.absoluteFilePath);
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

					ArrayList<Password> passwords = new ArrayList<Password>();
					String passwordStr = null;
//					while ((passwordStr = bufferedReader.readLine()) != null) {
//						Password password = Password.createFormJson(passwordStr);
//						passwords.add(password);
//					}

					passwords = PasswordFileUtil.getPasswordList(bufferedReader);

					bufferedReader.close();
					asyncResult.setResult(0);
					asyncResult.setData(passwords);
				} catch (Exception e) {
					asyncResult.setResult(-1);
				} finally {
					close(fileInputStream);
				}
				// 延时500毫秒回调结果
				setDelay(500);
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<ArrayList<Password>> asyncResult) {
				dismiss();
				if (asyncResult.getResult() == 0 && mainbinder != null) {
					// 导入文件
					ArrayList<Password> passwords = asyncResult.getData();
					for (Password password : passwords) {
						mainbinder.insertPassword(password);
					}

					Builder builder = new Builder(getContext());
					builder.setMessage(getString(R.string.import_file_successs, passwords.size()));
					builder.setNegativeButton(R.string.import_sure, null);
					builder.show();
				} else {
					// 读取文件失败
					Builder builder = new Builder(getContext());
					builder.setMessage(getString(R.string.import_file_failed));
					builder.setNegativeButton(R.string.import_sure, null);
					builder.show();
				}
			}
		}.execute();
	}

	/** 导入文件 网盘*/
	public void importFileFromSkyDrive(final ArrayList<Password> passwords) {
		new AsyncSingleTask<ArrayList<Password>>() {
			@Override
			protected AsyncResult<ArrayList<Password>> doInBackground(AsyncResult<ArrayList<Password>> asyncResult) {


					asyncResult.setResult(0);
					asyncResult.setData(passwords);

				// 延时500毫秒回调结果
				setDelay(500);
				return asyncResult;
			}

			@Override
			protected void runOnUIThread(AsyncResult<ArrayList<Password>> asyncResult) {
				dismiss();

				if (asyncResult.getResult() == 0 && mainbinder != null) {
					// 导入文件
					ArrayList<Password> passwords = asyncResult.getData();
					for (Password password : passwords) {
						mainbinder.insertPassword(password);
					}

					Builder builder = new Builder(getContext());
					builder.setMessage(getString(R.string.import_file_successs, passwords.size()));
					builder.setNegativeButton(R.string.import_sure, null);
					builder.show();
				} else {
					// 读取文件失败
					Builder builder = new Builder(getContext());
					builder.setMessage(getString(R.string.import_file_failed));
					builder.setNegativeButton(R.string.import_sure, null);
					builder.show();
				}
			}
		}.execute();
	}

	private void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		SearchResult searchResult;
		switch (msg.what) {
			case R.id.msg_import_search_delay_500:
				if(tag.equals(Constant.TAG_IMPORT_SKYDRIVE)){
					//搜索网盘
					searchFileBySkyDrive();
				}else{
					//sd卡
					searchFile();
				}

				break;
			case R.id.msg_import:
				searchResult = (SearchResult) msg.obj;
				setMessage(getContext().getString(R.string.import_ing, searchResult.name));
				super.show();
				importFile(searchResult);
				break;

			default:
				break;
		}
		return true;
	}

	private void searchFileBySkyDrive(){
		RequestEngine.getInstance(getContext()).getInfoFile(this);
	}

	private void showFileListDialog(ArrayList<String> fileNameList) {
		final ArrayList<SearchResult> searchResults = new ArrayList<>();

		if (fileNameList!=null && fileNameList.size()!= 0){
			// 搜索成功，用户选择文件
			for (int i = 0;i < fileNameList.size();i++){
				SearchResult searchResult = new SearchResult();
				searchResult.name = fileNameList.get(i);
				searchResult.absoluteFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyPassword/"+fileNameList.get(i);
				searchResults.add(searchResult);
			}

			Builder builder = new Builder(getContext());
			builder.setTitle(R.string.import_chiose_file);
			builder.setItems(getItems(searchResults), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SearchResult searchResult = searchResults.get(which);
					RequestEngine.getInstance(getContext()).downloadFile("/"+searchResult.name, ImportDialog.this);
				}
			});
			builder.show();

		}else{
			//搜索失败
			Builder builder = new Builder(getContext());
			builder.setMessage(getString(R.string.import_search_failed_to_skydrive));
			builder.setNegativeButton(R.string.import_sure, null);
			builder.show();
		}
	}

}
