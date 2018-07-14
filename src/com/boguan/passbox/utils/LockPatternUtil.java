package com.boguan.passbox.utils;

import com.ccst.kuaipan.database.Settings;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.boguan.passbox.activity.BaseActivity;
import com.boguan.passbox.app.MyApplication;
import com.boguan.passbox.model.SettingKey;
import com.boguan.passbox.view.LockPatternView.Cell;

public class LockPatternUtil {

	public static final int CHAR_SIZE = 4096;

	public static void savePatternCell(BaseActivity baseActivity, List<Cell> cells) {
		if (cells == null) {
			return;
		}

		try {
			JSONArray jsonArray = new JSONArray();
			for (Cell cell : cells) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("row", cell.getRow());
				jsonObject.put("column", cell.getColumn());
				jsonArray.put(jsonObject);
			}
			((MyApplication)baseActivity.getApplication()).putSettingEncode(SettingKey.LOCK_PATTERN, jsonArray.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static List<Cell> getLocalCell(BaseActivity baseActivity) {
		List<Cell> cells = new ArrayList<Cell>();

		String JSONArrayString = ((MyApplication)baseActivity.getApplication()).getSettingDecode(SettingKey.LOCK_PATTERN, "[]");

		try {
			JSONArray jsonArray = new JSONArray(JSONArrayString);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				int row = jsonObject.getInt("row");
				int column = jsonObject.getInt("column");
				Cell cell = Cell.of(row, column);
				cells.add(cell);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return cells;
	}

	public static boolean authPatternCell(BaseActivity baseActivity, List<Cell> cells, List<Cell> autoCells) {
		if (Settings.getSettingBoolean(baseActivity, Settings.Field.IS_AUTO_KUAIPAN, false)) {
			return checkPatternCell(autoCells, cells);
		}
		return checkPatternCell(getLocalCell(baseActivity), cells);
	}

	public static boolean checkPatternCell(List<Cell> cells1, List<Cell> cells2) {
		boolean result = true;
		if (cells1.size() == cells2.size()) {
			int size = cells1.size();
			for (int i = 0; i < size; i++) {
				if (!cells1.get(i).equals(cells2.get(i))) {
					result = false;
					break;
				}
			}
		} else {
			result = false;
		}
		return result;
	}

	//加密
	public static String encryptLockPattern(String jsonStr){
		String encryptStr = null;
		try {
			encryptStr=  AES.encrypt(jsonStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return  encryptStr;
	}

	//解密
	public static String decryptLockPattern(String lockStr){
		String JSONArrayString = null;
		try {
			JSONArrayString = AES.decrypt(lockStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return  JSONArrayString;

	}

	// 解密后获得的LockPattern数据
	public static List<Cell> getLockPatternList(BufferedReader bufferedReader){
		List<Cell> cells = new ArrayList<>();
		String lockPatternStr = null;
		StringBuffer buffer = new StringBuffer();

		int size = 0;
		char[] ch = new char[CHAR_SIZE];

		int len = ch.length;

		try {
			do {
				len = bufferedReader.read(ch, 0, len);
				if (len != -1){
					buffer.append(ch);
					size = size + len;
				}

			}while (len !=-1);

			if (buffer.toString().equals("") || buffer.toString().length() <= 0) {
				return cells;
			}
			lockPatternStr = decryptLockPattern(buffer.toString().substring(0, size));

			if (lockPatternStr!=null && !lockPatternStr.equals("")){
				JSONArray jsonArray = new JSONArray(lockPatternStr);
				for (int i = 0; i<jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					int row = jsonObject.getInt("row");
					int column = jsonObject.getInt("column");
					Cell cell = Cell.of(row, column);
					cells.add(cell);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return cells;
	}
}
