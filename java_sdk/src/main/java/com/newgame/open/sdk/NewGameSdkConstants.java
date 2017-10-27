package com.newgame.open.sdk;

/**
 * 新游SDK常量定义
 *
 * @author yakecanlee
 */
public class NewGameSdkConstants {

    /**
     * 请求域名
     */
    public static final String DOMAIN = "https://passport.newgame.com";
    public static final String LUCK_DOMAIN = "http://luck.passport.newgame.com";

    /**
     * 请求路径
     */
    public static final String URL_AUTHORIZATION_CODE = "/oauth2/authorize?app_id=%s&redirect_uri=%s&state=%s&guest=%s";
    public static final String URL_ACCESS_TOKEN = "/oauth2/access_token?app_id=%s&app_secret=%s&grant_type=authorization_code&code=%s";
    public static final String URL_ACCESS_TOKEN_REFRESH = "/oauth2/access_token?app_id=%s&app_secret=%s&grant_type=refresh_token&refresh_token=%s";
    public static final String URL_USERINFO = "/api/users/user_info?access_token=%s";
    public static final String URL_ORDER = "/api/pay/order";
    public static final String URL_REPORT_DEVICEID = "/api/device_id";
    public static final String URL_REPORT_DATA = "/api/report";

    /**
     * 本地运行错误码
     */
    public static final int CODE_SUCCESSFUL = 1;
    public static final int CODE_SERVER_ERROR = 0;
    public static final int CODE_SYSTEM_ERROR = -1;

    /**
     * 错误码常量定义
     */
    public static final int ERROR_CODE_NOAPP = 50001;
    public static final int ERROR_CODE_GRANT_TYPE_ERROR = 50002;
    public static final int ERROR_CODE_INVALID_REFRESH_TOKEN = 50004;
    public static final int ERROR_CODE_INVALID_APP_SECRET = 50005;
    public static final int ERROR_CODE_INVALID_ACCESS_TOKEN = 50008;
    public static final int ERROR_CODE_EXPIRED_ACCESS_TOEKN = 50011;
    public static final int ERROR_CODE_EXPIRED_REFRESH_TOKEN = 50013;
    public static final int ERROR_CODE_INVALID_AUTHORIZATION_CODE = 50048;

    /**
     * 加解密方法
     */
    public static final String SIGNATURE_RSA = "rsa";
    public static final String SIGNATURE_MD5 = "md5";
    public static final String SIGNATURE_SHA1WITHRSA = "SHA1withRSA";

}
