package com.boguan.passbox.utils;

/**
 * Created by linfangzhou on 15/8/26.
 */
public class Constant {
    //-------------- Fragment ----------------

    public static final String TAG_FRAGMENT_IMPORT = "Import";
    public static final String TAG_FRAGMENT_EXPORT = "Export";
    public static final String TAG_FRAGMENT_AUTO = "Auto";

    //-------------- Kuai Pan ----------------

    //解密
    public static final String PASSWORD_STR_SPLIT_CHAR = "\n";

    //auto kuaipan filename
    public static final String AUTO_FILE_NAME = "/auto_passwords.mp";
    public static final String AUTO_LOCK_PATTERN_FILE_NAME = "auto_lock_pattern.mp";

    //Broadcast Action
    public static final String BROADCAST_SHOW_SUCCESS_DIALOG = "broadcast_show_success_dialog";
    public static final String BROADCAST_UPLOAD_FILE = "broadcast_upload_file";
    public static final String BROADCAST_TOKEN_EXPIRED_TO_LOGIN ="broadcast_token_expired_to_login";
    public static final String BROADCAST_SHOW_FAILED_DIALOG = "broadcast_show_failed_dialog";
    public static final String BROADCAST_AUTO_KUAIPAN = "broadcast_auto_kuaipan";
    public static final String BROADCAST_AUTO_UPLOAD = "broadcast_auto_upload";
    public static final String BROADCAST_AUTO_DOWNLOAD = "broadcast_auto_download";
    public static final String BROADCAST_AUTO_DOWNLOAD_LOCK_PATTERN = "broadcast_auto_download_loc_pattern";
    public static final String BROADCAST_SHOW_NETWORK_ERROR_DIALOG = "broadcast_show_network_error_dialog";

    //TAG
    public static final String TAG_EXPORT_SKYDRIVE = "tag_export_skydrive";
    public static final String TAG_EXPORT_SDCARD = "tag_export_sdcard";
    public static final String TAG_IMPORT_SKYDRIVE = "tag_import_skydrive";
    public static final String TAG_IMPORT_SDCARD = "tag_import_sdcard";
    public static final String TAG_EXPORT_AUTO_KUAIPAN = "tag_export_auto_kuaipan";
    public static final String TAG_LOGIN_AUTO_KUAIPAN = "tag_login_auto_kuaipan";

    //Intent Key
    public static final String INTENT_KEY_FILENAME = "FileName";
    public static final String INTENT_KEY_AUTO_PASSWORD_LIST = "passwordList";

    //Password Group Name
    public static final String PASSWORD_GROUP_MONEY_NAME = "理财";
    public static final String PASSWORD_GROUP_COMMUNICATION_NAME = "社交";
    public static final String PASSWORD_GROUP_SHOPPING_NAME = "购物";
    public static final String PASSWORD_GROUP_NEWS_NAME = "新闻";
    public static final String PASSWORD_GROUP_STUDY_NAME = "学习";
    public static final String PASSWORD_GROUP_PLAY_NAME = "娱乐";
    public static final String PASSWORD_GROUP_TRAVEL_NAME = "旅行交通";
    public static final String PASSWORD_GROUP_ALL_NAME = "全部";

    //Item Type
    public static final String PASSWORD_ITEM_BAIDU = "baidu";
    public static final String PASSWORD_ITEM_TUDOU = "tudou";
    public static final String PASSWORD_ITEM_WEIXIN = "weixin";
    public static final String PASSWORD_ITEM_XUNLEI = "xunlei";
}
