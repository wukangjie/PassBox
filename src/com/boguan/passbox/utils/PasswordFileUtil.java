package com.boguan.passbox.utils;

import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import com.boguan.passbox.model.Password;

/**
 * Created by linfangzhou on 15/9/7.
 */
public class PasswordFileUtil {

    public static final int CHAR_SIZE = 4096;

    //加密 密码文件
    public static String encryptPassword(String jsonStr){
        String encryptStr = null;
        try {
            encryptStr=  AES.encrypt(jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  encryptStr;
    }

    //解密 密码文件
    public static String decryptPassword(String passwordStr){
        String JSONArrayString = null;
        try {
            JSONArrayString = AES.decrypt(passwordStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  JSONArrayString;

    }


    // 解密后获得的密码数据
    public static ArrayList<Password> getPasswordList(BufferedReader bufferedReader){
        ArrayList<Password> passwords = new ArrayList<Password>();
        String passwordStr = null;
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
                return passwords;
            }
            passwordStr = PasswordFileUtil.decryptPassword(buffer.toString().substring(0,size - 1));

            if (passwordStr!=null && !passwordStr.equals("")){
                String[] passwordArray = passwordStr.split(Constant.PASSWORD_STR_SPLIT_CHAR);

                for (int i = 0; i<passwordArray.length; i++){
                    Password password = Password.createFormJson(passwordArray[i]);
                    passwords.add(password);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return passwords;
    }
}
