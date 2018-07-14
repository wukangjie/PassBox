package com.boguan.passbox.model;

import com.boguan.passbox.utils.DateUtils;
import android.content.Context;

public class PasswordItem {
    public String dataString;
    public Password password;
    private Context context;

    public PasswordItem(Context context, Password password) {
        this.password = password;
        this.context = context;
        initDataString();
    }

    public void initDataString() {
        dataString = DateUtils.formatDate(context, password.getCreateDate());
    }
}
