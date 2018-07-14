package com.ccst.kuaipan.protocol.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;
import com.ccst.kuaipan.database.Settings;
import com.ccst.kuaipan.database.Settings.Field;
import com.ccst.kuaipan.protocol.CreateDataProtocol.*;
import com.ccst.kuaipan.protocol.DownloadFileProtocol.*;
import com.ccst.kuaipan.protocol.GetFileProtocol.*;
import com.ccst.kuaipan.protocol.GetUserProtocol.*;
import com.ccst.kuaipan.protocol.LoginProtocol;
import com.ccst.kuaipan.protocol.Protocol;
import com.ccst.kuaipan.protocol.Protocol.BaseProtocolData;
import com.ccst.kuaipan.protocol.Protocol.ProtocolType;
import com.ccst.kuaipan.protocol.data.HttpRequestInfo;
import com.ccst.kuaipan.protocol.data.KuaipanHTTPResponse;
import com.ccst.kuaipan.protocol.data.HttpRequestInfo.KuaipanURL;
import com.ccst.kuaipan.protocol.exception.KuaipanAuthExpiredException;
import com.ccst.kuaipan.protocol.session.OauthSession;
import com.ccst.kuaipan.protocol.session.Session;
import com.ccst.kuaipan.tool.*;
import com.boguan.passbox.utils.Constant;


public class RequestEngine {
    public abstract interface HttpRequestCallback {
        public abstract void onHttpResult(BaseProtocolData data);

        public abstract void onHttpStart(BaseProtocolData data);
    }

    public static TaskPool mRequestTaskPool = new TaskPool(1);

    private Context mContext;
    private static RequestEngine mRequestEngine;

    private Session mSession;

    private KuaipanHTTPResponse resp;

    private RequestEngine(Context c) {
        if(!Settings.getSettingBoolean(c, Field.IS_LOGIN, false)){
            mSession = new OauthSession(Protocol.CONSUMER_KEY, Protocol.CONSUMER_SECRET);
        }else{
            mSession = new OauthSession(Protocol.CONSUMER_KEY, Protocol.CONSUMER_SECRET);
            String tokenPairKey = Settings.getSettingString(c, Field.TOKENPAIR_KEY, "");
            String tokenPairSecret = Settings.getSettingString(c, Field.TOKENPAIR_SECRET, "");
            mSession.setAuthToken(tokenPairKey, tokenPairSecret);
        }
    }

    public Session getSession() {
        return mSession;
    }

    public void setSession(Session mSession) {
        this.mSession = mSession;
    }

    public static RequestEngine getInstance(Context c) {
        if (mRequestEngine == null) {
            mRequestEngine = new RequestEngine(c);
        }
        mRequestEngine.mContext = c;
        return mRequestEngine;
    }

    public void execute(BaseProtocolData d) {
            Time time = new Time();
            d.mSendingTime = time.toMillis(true);
            new HttpRequestTask().execute(d);
    }
    
    
    public void send(BaseProtocolData d) {
        createHttpRequestInfo(d);
        clearAllIdle();
        mRequestTaskPool.addTask(d);
    }
    
    private void createHttpRequestInfo(BaseProtocolData d) {
        int type = d.getProtocolType();
        HttpRequestInfo info = d.getHttpRequestInfo();
        TreeMap<String, String> userParams = d.getUserParams();
        info = new HttpRequestInfo(type, userParams);
        Protocol.setRequestUrl(info);
        d.setHttpRequestInfo(info);
        Protocol.setRequestQuery(mSession.consumer, mSession.token, info);
        
        callbackStart(d);
    }
    
    public void clearAllIdle() {
        Time time = new Time();
        time.setToNow();
        for (int i = mRequestTaskPool.mTaskList.size() - 1; i >= 0; --i) {
            TaskPool.TaskInterface inter = mRequestTaskPool.mTaskList.get(i);
            BaseProtocolData data = (BaseProtocolData) inter;
            if (i < mRequestTaskPool.mRunningTaskNumber
                    && (data.mSendingTime == 0)) {
                mRequestTaskPool.mTaskList.remove(i);
                --mRequestTaskPool.mRunningTaskNumber;
            }
        }
    }
    
    private void callbackStart(BaseProtocolData data) {
        if (data.getHttpRequestCallback() != null)
            data.getHttpRequestCallback().onHttpStart(data);
    }
    
    private void callbackResult(BaseProtocolData data) {
        if (data.getHttpRequestCallback() != null)
            data.getHttpRequestCallback().onHttpResult(data);
    }
    
    public class HttpRequestTask extends AsyncTask<BaseProtocolData, Void, BaseProtocolData> {
        @Override
        protected BaseProtocolData doInBackground(final BaseProtocolData... data) {
            resp = new KuaipanHTTPResponse();
            try {
                KuaipanURL url = data[0].getHttpRequestInfo().getKuaipanURL();
                LogHelper.log("################################");
                LogHelper.log("Request url"+ url);

                if (data[0].getType() == ProtocolType.UPLOAD_FILE_PROTOCOL) {
                    File file = new File(data[0].getmPath());
                    FileInputStream  fis = new FileInputStream(file);
                    resp = KuaipanHTTPUtility.doUpload(url, fis, file.length(), new ProgressListener() {
                        
                        @Override
                        public void started() {

                            
                        }
                        
                        @Override
                        public void processing(long bytes, long total) {

                            Log.w("File Up", "Completed: " + bytes*100/total + "%");
                        }
                        
                        @Override
                        public int getUpdateInterval() {

                            return 0;
                        }
                        
                        @Override
                        public void completed() {

                            final Activity act = (Activity) data[0].getmContext();

                            act.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Log.d("myLog", "Upload Done.All Completed.");
                                    String path = data[0].getmPath();
                                    int index = path.lastIndexOf("/");
                                    String fileName = path.substring(index+1,path.length());
                                    Intent intent = new Intent(Constant.BROADCAST_SHOW_SUCCESS_DIALOG);
                                    intent.putExtra(Constant.INTENT_KEY_FILENAME, fileName);
                                    mContext.sendBroadcast(intent);

                                }
                            });
                        }
                    });
                    fis.close();
                } else if(data[0].getType() == ProtocolType.DOWNLOAD_FILE_PROTOCOL){
                    final String path = data[0].getmPath();

                    List list = KuaipanHTTPUtility.doDownload(url, path);
                    resp.code = 200;
                    resp.content = "{\"msg\":\"Succeed\"}";

                    data[0].setAttachedObject(list);
                } else {
                    resp = KuaipanHTTPUtility.requestByGET(url);
                }

                Map<String, Object> result = OauthUtility.parseResponseToMap(resp);

                Log.d("myLog","result===="+result);
                data[0].getHttpRequestInfo().setResultParams(result);
            } catch (KuaipanAuthExpiredException e) {
               Intent intent = new Intent(Constant.BROADCAST_TOKEN_EXPIRED_TO_LOGIN);
                mContext.sendBroadcast(intent);
            } catch (Exception KuaipanIOException) {
                if (!KuaipanHTTPUtility.checkNetworkState(mContext)) {
                    Intent intent = new Intent(Constant.BROADCAST_SHOW_NETWORK_ERROR_DIALOG);
                    mContext.sendBroadcast(intent);
                } else {
                    if (KuaipanIOException.toString().contains("NullPointerException")){
                        Intent intent = new Intent(Constant.BROADCAST_SHOW_FAILED_DIALOG);
                        mContext.sendBroadcast(intent);
                    }
                    resp.code = KuaipanHTTPResponse.KUAIPAN_UNKNOWNED_ERROR;
                }
                Log.d("myLog","Catch/KuaipanHTTPResponse=="+resp.code+"/resp.content="+resp.content+"/KuaipanIOException="+KuaipanIOException.toString());

            } finally {
                data[0].getHttpRequestInfo().setResultCode(resp.code);
                mRequestTaskPool.removeTask(data[0]);
            }
            
            return data[0];
        }

        @Override
        protected void onPostExecute(BaseProtocolData data) {
            if(data != null){
                data.parseData();
                callbackResult(data);
                mRequestTaskPool.removeTask(data);
            }
        }

    }
    
    public void requestToken(HttpRequestCallback callback){
        mSession.unAuth(mContext);
        send(new LoginProtocol.RequestTokenProtocol(mContext, ProtocolType.REQUEST_TOKEN_PROTOCOL, callback));
    }
    
    public void accessToken(HttpRequestCallback callback){
        send(new LoginProtocol.AccessTokenProtocol(mContext, ProtocolType.ACCESS_TOKEN_PROTOCOL, callback));
    }
    
    public void createFolder(String path, HttpRequestCallback callback){
        send(new CreateFolderProtocol(mContext, path, ProtocolType.CREATE_FOLDER_PROTOCOL, callback));
    }
    
    public void uploadFile(final String path, final HttpRequestCallback callback){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Map<String, Object> result = null;
                KuaipanHTTPResponse resp = new KuaipanHTTPResponse();
                KuaipanURL locateUrl = new KuaipanURL("http://api-content.dfs.kuaipan.cn/1/fileops/upload_locate");
                try {
                    LogHelper.log("################################");
                    LogHelper.log("Request url"+ locateUrl);

                    resp = KuaipanHTTPUtility.requestByGET(locateUrl);
                    result = OauthUtility.parseResponseToMap(resp);
                }catch (Exception KuaipanIOException) {
                    resp.code = KuaipanHTTPResponse.KUAIPAN_UNKNOWNED_ERROR;
                }
                
                return (String) result.get("url");
            }

            @Override
            protected void onPostExecute(String data) {
                if(data != null){
                    send(new UploadFileProtocol(mContext, data, path, ProtocolType.UPLOAD_FILE_PROTOCOL, callback));

                }
            }
        }.execute();
    }
    
//    public void downloadFile(final String path, final HttpRequestCallback callback){
//    	send(new DownloadProtocol(mContext, path, ProtocolType.DOWNLOAD_FILE_PROTOCOL, callback));
//    }
    public void downloadFile(final String path, final HttpRequestCallback callback){
        send(new DownloadProtocol(mContext, path, ProtocolType.DOWNLOAD_FILE_PROTOCOL, callback));
    }

    public void getInfoFile(final HttpRequestCallback callback){
        send(new GetPasswordFileProtocol(mContext, ProtocolType.GET_FILE_PROTOCOL, callback));
    }

    public void getUserInfo(final HttpRequestCallback callback){
        send(new GetUserInfoProtocol(mContext, ProtocolType.GET_USER_INFO_PROTOCOL, callback));
    }

    public boolean needToAuth() {
        return Settings.getSettingBoolean(mContext, Field.IS_AUTO_KUAIPAN, true) && mSession.isAuth() == false;
    }

}
