package com.ccst.kuaipan.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.ccst.kuaipan.protocol.Protocol.BaseProtocolData;
import com.ccst.kuaipan.protocol.util.RequestEngine.HttpRequestCallback;
import com.boguan.passbox.R;


public class GetFileProtocol {
	
	public static class GetPasswordFileProtocol extends BaseProtocolData{

		 public GetPasswordFileProtocol(Context c, int protocolType,
				HttpRequestCallback callBack) {
			super(c, protocolType, callBack);
//			String ext = string2ASCII("png").toString();
//			getUserParams().put("filter_ext", "jpg");
		}

		public boolean isSuccess = false;

		@Override
		boolean parse(Map<String, Object> resultParams) {
			ArrayList<String> fileNameList = new ArrayList<String>();
			JSONArray arrays= (JSONArray) resultParams.get("files");
			
			for (int i = 0; i < arrays.size(); i++) {
				JSONObject obj = (JSONObject) arrays.get(i);
				if (obj.toString().contains(".mp")) {
					String filename=obj.get("name").toString();
					if (!filename.contains("auto")){
						fileNameList.add(filename);
					}

				}
				
			}

			//将fileNameList降序排序
			Collections.sort(fileNameList);
			Collections.reverse(fileNameList);

			mAttachedObject = fileNameList;
	         
	     return true;
	       
		}

		@Override
		void doCallback() {
			
			
		}
		
	}
	
	public static int char2ASCII(char c) {   
	     return (int) c;   
	 }   
	 public static int[] string2ASCII(String s) {// 字符串转换为ASCII码   
	        if (s == null || "".equals(s)) {   
	            return null;   
	        }   

	        char[] chars = s.toCharArray();   
	        int[] asciiArray = new int[chars.length];   

	        for (int i = 0; i < chars.length; i++) {   
	            asciiArray[i] = char2ASCII(chars[i]);   
	        }   
	        return asciiArray;   
	    } 

}
