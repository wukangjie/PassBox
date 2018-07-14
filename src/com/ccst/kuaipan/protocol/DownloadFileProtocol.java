package com.ccst.kuaipan.protocol;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import android.content.Context;
import android.os.Handler;

import com.ccst.kuaipan.protocol.Protocol.BaseProtocolData;
import com.ccst.kuaipan.protocol.util.RequestEngine.HttpRequestCallback;

public class DownloadFileProtocol {
	
	public static class DownloadProtocol extends BaseProtocolData{
		 public boolean isSuccess = false;

		public DownloadProtocol(Context c, String path, int protocolType,
				HttpRequestCallback callBack) {
			super(c, protocolType, callBack);
			mPath = path;
			try {
				getUserParams().put("path", URLEncoder.encode(path, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			getUserParams().put("root", Protocol.APP_ROOT);
		}

		@Override
		boolean parse(Map<String, Object> resultParams) {
			isSuccess = true;
			return true;
		}

		@Override
		void doCallback() {
			
		}
		
	}

}
