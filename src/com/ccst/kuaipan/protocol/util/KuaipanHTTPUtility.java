package com.ccst.kuaipan.protocol.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.ccst.kuaipan.protocol.data.HttpRequestInfo.KuaipanURL;
import com.ccst.kuaipan.protocol.data.KuaipanHTTPResponse;
import com.ccst.kuaipan.protocol.exception.KuaipanIOException;
import com.ccst.kuaipan.tool.JSONUtility;
import com.ccst.kuaipan.tool.LogHelper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import com.boguan.passbox.model.Password;
import com.boguan.passbox.utils.Constant;
import com.boguan.passbox.utils.LockPatternUtil;
import com.boguan.passbox.utils.PasswordFileUtil;
import com.boguan.passbox.view.LockPatternView;

public class KuaipanHTTPUtility {

	public interface DownloadKuaipanFile {
		void getFileDataList(List list);
	}

	public final static int BUFFER_SIZE = 4048;
	public final static String CONTENT_HOST = "api-content.dfs.kuaipan.cn";
	public final static String CONV_HOST = "conv.kuaipan.cn";

	public final static String UPLOAD_LOCATE_URL = "http://api-content.dfs.kuaipan.cn/1/fileops/upload_locate";
	public static final int HTTP_TIME_OUT = 30000;

	private KuaipanHTTPUtility() {}

	public static KuaipanHTTPResponse requestByGET(KuaipanURL url)
			throws KuaipanIOException {
		KuaipanHTTPResponse resp = doGet(url);
		return resp;
	}

	public static KuaipanHTTPResponse doUpload(KuaipanURL baseurl, InputStream datastream,
											   long size, ProgressListener lr) throws KuaipanIOException {
		KuaipanHTTPResponse resp = new KuaipanHTTPResponse();

		HttpURLConnection con = getConnectionFromUrl(baseurl.queryByGetUrl(), "POST");

		multipartUploadData(con, datastream, size, lr);

		resp.code = getResponseHTTPStatus(con);
		resp.content = getStringDataFromConnection(con);
		resp.url = baseurl;

		if (con != null)
			con.disconnect();

		LogHelper.log("################################");
		LogHelper.log("Receive result:"+ resp.toString());

		return resp;
	}

	public static KuaipanHTTPResponse doDownload2(KuaipanURL baseurl, OutputStream os, ProgressListener lr)
			throws KuaipanIOException{
		KuaipanHTTPResponse resp = new KuaipanHTTPResponse();
		HttpURLConnection con = getConnectionFromUrl(baseurl.url, "GET");
		resp.code = getResponseHTTPStatus(con);
		resp.url = baseurl;

		if(resp.code == KuaipanHTTPResponse.KUAIPAN_OK) {
			writeStreamFromConnection(con, os, lr);
			resp.content = null;
		} else {
			resp.content = getStringDataFromConnection(con);
		}

		if (con != null)
			con.disconnect();

		LogHelper.log("################################");
		LogHelper.log("Receive result:"+ resp.toString());
		return resp;
	}

	public static List doDownload(final KuaipanURL baseurl, final String tag) {
		ArrayList<Password> passwords = new ArrayList<Password>();
		ArrayList<LockPatternView.Cell> cells = new ArrayList<>();

		final HttpGet httpGet = new HttpGet(baseurl.url);
		final HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.TRUE);
		httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");  // 默认为ISO-8859-1
		httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");  // 默认为US-ASCII
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);    //设置超时
		InputStream is = null;
		final HttpResponse[] response = {null};
		try {
			Log.d("myLog"," httpClient.execute...");
			response[0] = httpClient.execute(httpGet);
			Log.d("myLog"," get response" + response[0]);
			if(response[0].getStatusLine().getStatusCode()== KuaipanHTTPResponse.KUAIPAN_OK) {
				is = response[0].getEntity().getContent();
				// 要考虑大文件的场合, 做分步下载
				// Todo.

				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
				if (tag.equals(Constant.AUTO_LOCK_PATTERN_FILE_NAME)){
					cells = (ArrayList<LockPatternView.Cell>) LockPatternUtil.getLockPatternList(bufferedReader);
					is.close();
					bufferedReader.close();

					return cells;
				} else {
					passwords = PasswordFileUtil.getPasswordList(bufferedReader);
					is.close();
					bufferedReader.close();

					return passwords;
				}
			}
		} catch (ConnectTimeoutException cte) {
			cte.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

//	public static KuaipanHTTPResponse doDownload(KuaipanURL baseurl, OutputStream os, ProgressListener lr)
//			throws KuaipanIOException, ClientProtocolException {
//		HttpGet httpGet = new HttpGet(baseurl.url);
//		HttpClient httpClient = new DefaultHttpClient();
//		httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.TRUE);
//		httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");  // 默认为ISO-8859-1
//		httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");  // 默认为US-ASCII
//		KuaipanHTTPResponse resp = new KuaipanHTTPResponse();
//		try {
//			HttpResponse response = httpClient.execute(httpGet);
//			resp.code = response.getStatusLine().getStatusCode();
//			InputStream is = null;
//			if(resp.code == KuaipanHTTPResponse.KUAIPAN_OK) {
//
//				is = response.getEntity().getContent();
//
//				writeStreamFromConnection(is, response.getEntity().getContentLength(), os, lr);
//				resp.content = null;
//			} else {
//				resp.content = getStringDataFromResponse(is);
//			}
//
//		} catch (IllegalStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return resp;
//
//	}

	private static int getResponseHTTPStatus(HttpURLConnection con) {
		int code = KuaipanHTTPResponse.KUAIPAN_UNKNOWNED_ERROR;
		try {
			code = con.getResponseCode();
		} catch (IOException e) {
			// bad HTTP format from server, it may never happen
			if (con != null) con.disconnect();
		}
		return code;
	}

	private static String stream2String(InputStream is) {
		String result = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[BUFFER_SIZE];
		int len = 0;
		try {
			while((len = is.read(buf)) != -1) {
				baos.write(buf, 0, len);
			}
		} catch (IOException e) {
		}

		try {
			result = new String(baos.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			result = new String(baos.toByteArray());
		} finally {
			try {
				baos.close();
			} catch (IOException e) {}
		}
		return result;
	}

	private static String getStringDataFromResponse(InputStream is){
		try {
			return stream2String(is);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) { }
		}
	}

	private static String getStringDataFromConnection(HttpURLConnection con){
		InputStream is = null;
		try {
			is = con.getInputStream();
		} catch (IOException e) {
			is = con.getErrorStream();
		}

		try {
			return stream2String(is);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) { }
		}
	}

	private static void writeStreamFromConnection(InputStream is, long total, OutputStream os, ProgressListener lr) {
		if (lr != null) lr.started();

		try {
			bufferedWriting(os, is, total, lr);
		} catch (KuaipanIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (lr != null) lr.completed();
			if (is != null)
				try {
					is.close();
				} catch (IOException e) { }
		}
	}

	private static void writeStreamFromConnection(HttpURLConnection con, OutputStream os, ProgressListener lr)
			throws KuaipanIOException {
		InputStream is = null;
		try {
			is = con.getInputStream();
		} catch (IOException e) {
			is = con.getErrorStream();
		}
		int total = con.getContentLength();

		writeStreamFromConnection(is, total, os, lr);
	}

	private static HttpURLConnection getConnectionFromUrl(String baseurl, String method)
			throws KuaipanIOException {
		URL url = null;
		HttpURLConnection con = null;
		try {
			url = new URL(baseurl);
		} catch (MalformedURLException e) {
			// never come here
			e.printStackTrace();
		}

		try {
			con = (HttpURLConnection)url.openConnection();
			con.setConnectTimeout(HTTP_TIME_OUT);
		} catch (IOException e2) {
			// some IO error, maybe timeout.
			throw new KuaipanIOException(e2);
		}

		try {
			con.setRequestMethod(method);
		} catch (ProtocolException e1) {
			// never come here
			if (con != null) {
				con.disconnect();
				con = null;
			}
		}
		return con;
	}

	private static void multipartUploadData(
			HttpURLConnection con, InputStream datastream,
			long size, ProgressListener lr)
			throws KuaipanIOException {
		con.setDoOutput(true);
		String boundary = "--------------------------"+System.currentTimeMillis();
		con.setRequestProperty("connection", "keep-alive");
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		con.setRequestProperty("Cache-Control", "no-cache");
		boundary = "--" + boundary;
		StringBuffer sb = new StringBuffer();
		sb.append(boundary);
		sb.append("\r\n");
		sb.append("Content-Disposition: form-data; name=\"Filedata\"; filename=\""
				+ "myfile" + "\"\r\n");
		sb.append("Content-Type: application/octet-stream\r\n\r\n");
		String endStr = "\r\n"
				+ boundary
				+ "\r\nContent-Disposition: form-data; name=\"Upload\"\r\n\r\nSubmit Query\r\n"
				+ boundary
				+ "--\r\n";
		byte[] endData = endStr.getBytes();
		OutputStream os = null;
		try {
			con.connect();
			os = con.getOutputStream();
			os.write(sb.toString().getBytes());

			if (lr != null) {
				lr.started();
			}
			bufferedWriting(os, datastream, size, lr);
			os.write(endData);

		} catch (IOException e5) {
			throw new KuaipanIOException(e5);
		} finally {
			try {
				if (os != null) {
					os.flush();
					os.close();
				}
			} catch (IOException e) {}
			if (lr != null)
				lr.completed();
		}
	}

	private static KuaipanHTTPResponse doGet(KuaipanURL kpurl)
			throws KuaipanIOException {
		KuaipanHTTPResponse resp = new KuaipanHTTPResponse();

		HttpURLConnection con = getConnectionFromUrl(kpurl.url, "GET");
		resp.code = getResponseHTTPStatus(con);
		resp.content = getStringDataFromConnection(con);
		resp.url = kpurl;

		if (con != null)
			con.disconnect();

		LogHelper.log("################################");
		LogHelper.log("Receive result:"+resp.toString());

		return resp;
	}

	/**
	 * writing with buffer and progress listening
	 * @param to_write
	 * @param to_read
	 * @param total
	 * @param lr
	 * @throws KuaipanIOException
	 */
	private static void bufferedWriting(OutputStream to_write, InputStream to_read, long total, ProgressListener lr)
			throws KuaipanIOException {
		long last_triggered_time = 0L;
		int len = 0;
		int count = 0;
		byte[] buf = new byte[BUFFER_SIZE];
		try {
			while((len = to_read.read(buf)) != -1) {
				to_write.write(buf, 0, len);
				count += len;
				long current_time = System.currentTimeMillis();
				if (lr != null &&
						(current_time-last_triggered_time) > lr.getUpdateInterval()) {
					lr.processing(count, total);
					last_triggered_time = current_time;
				}
			}
		} catch (IOException e) {
			throw new KuaipanIOException(e);
		}
	}



	public static class UploadHostFactory {
		private static String upload_host = null;
		private static long last_refresh_time = 0;

		private static final long REFRESH_INTERVAL = 3600 * 10 * 1000;

		private UploadHostFactory() {}

		public static String getUploadHost() throws KuaipanIOException {
			refreshUploadHost();
			return upload_host;
		}

		private static synchronized void refreshUploadHost()
				throws KuaipanIOException {
			if (upload_host == null ||
					(System.currentTimeMillis() - last_refresh_time) > REFRESH_INTERVAL ) {
				KuaipanHTTPResponse resp = doGet(new KuaipanURL(UPLOAD_LOCATE_URL));
				if (resp.content == null)
					throw new KuaipanIOException(resp.toString());

				Map<String, Object> mapresult = JSONUtility.parse(resp.content);
				if (mapresult == null)
					throw new KuaipanIOException(resp.toString());

				String url = (String) mapresult.get("url");
				if (url == null)
					throw new KuaipanIOException(resp.toString());

				if (url.startsWith("http://"))
					url = url.replaceFirst("http://", "");
				else if (url.startsWith("https://"))
					url = url.replaceFirst("https://", "");

				if (url.endsWith("/"))
					url = url.substring(0, url.length()-1);

				upload_host = url;
				last_refresh_time = System.currentTimeMillis();
			}
		}
	}

	public static boolean checkNetworkState(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		return (info != null && info.isAvailable());
	}

}
