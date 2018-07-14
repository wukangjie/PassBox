package com.ccst.kuaipan.protocol;

import android.content.Context;
import android.util.Log;

import com.ccst.kuaipan.database.Settings;
import com.ccst.kuaipan.database.Settings.*;
import com.ccst.kuaipan.protocol.Protocol.BaseProtocolData;
import com.ccst.kuaipan.protocol.util.RequestEngine.HttpRequestCallback;
import java.util.Map;

//add obtain data protocol here
public class GetUserProtocol {

    public static class GetUserInfoProtocol extends BaseProtocolData{
        private String userName;
        public GetUserInfoProtocol(Context c, int protocolType,
                                       HttpRequestCallback callBack) {
            super(c, protocolType, callBack);

        }

        public boolean isSuccess = false;



        @Override
        boolean parse(Map<String, Object> resultParams) {


            userName = resultParams.get("user_name").toString();
            if (userName!=null && !userName.equals("")){
                isSuccess = true;
            }

            return true;

        }

        @Override
        void doCallback() {
            Settings.setSettingString(mContext, Field.USER_NAME,userName);
            Log.d("myLog","SettingsUser===="+Settings.getSettingString(mContext,Field.USER_NAME,""));

        }

    }
    
}
