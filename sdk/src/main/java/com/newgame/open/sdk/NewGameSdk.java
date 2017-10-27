package com.newgame.open.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static com.newgame.open.sdk.NewGameSdkConstants.*;

/**
 * 新游SDK
 *
 * @author yakecanlee
 */
public class NewGameSdk {

    private final String APPID;
    private final String APPSECRET;
    private final String PLATFORM_PUBLIC_KEY;

    private OkHttpClient mOkHttpClient = new OkHttpClient();

    /**
     * 构造方法,传入APPID,APPSECRET,平台公钥的必要参数
     *
     * @param appId
     * @param appSecret
     * @param platformPublicKey
     */
    public NewGameSdk(String appId, String appSecret, String platformPublicKey) {
        this.APPID = appId;
        this.APPSECRET = appSecret;
        this.PLATFORM_PUBLIC_KEY = platformPublicKey;
    }

    /**
     * 执行HTTP请求并封装返回结果
     *
     * @param call
     * @return
     */
    private NewGameResponse executeCall(Call call) {
        NewGameResponse response = new NewGameResponse();
        try {
            Response res = call.execute();
            String content = res.body().string();
            if (res.isSuccessful()) {
                if (content != null) {
                    response.setContent(new ObjectMapper().readValue(content, Map.class));
                    response.setCode(CODE_SUCCESSFUL);
                }
            } else {
                response.setCode(CODE_SERVER_ERROR);
                response.setRawContent(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.setCode(CODE_SYSTEM_ERROR);
            response.setException(e);
        }
        return response;
    }

    /**
     * 获取引导登陆授权页URL
     *
     * @param redirectUrl
     * @param isGuest
     * @param isLuckTest
     * @return
     */
    public String getAuthorizationUrl(String redirectUrl, boolean isGuest, boolean isLuckTest) {
        return getAuthorizationUrl(redirectUrl, "state", isGuest, isLuckTest);
    }

    /**
     * 获取引导登陆授权页URL
     *
     * @param redirectUrl
     * @param state
     * @param isGuest
     * @param isLuckTest
     * @return
     */
    public String getAuthorizationUrl(String redirectUrl, String state, boolean isGuest, boolean isLuckTest) {
        return String.format((isLuckTest ? LUCK_DOMAIN : DOMAIN) + URL_AUTHORIZATION_CODE, APPID, redirectUrl, state, isGuest ? 1 : 0);
    }

    /**
     * 获取访问令牌
     *
     * @param code
     * @param isLuckTest
     * @return
     */
    public NewGameResponse getAccessToken(String code, boolean isLuckTest) {
        Request request = new Request.Builder()
                .url(String.format((isLuckTest ? LUCK_DOMAIN : DOMAIN) + URL_ACCESS_TOKEN, APPID, APPSECRET, code))
                .get()
                .build();
        Call call = mOkHttpClient.newCall(request);
        return this.executeCall(call);
    }

    /**
     * 刷新访问令牌
     *
     * @param refreshToken
     * @param isLuckTest
     * @return
     */
    public NewGameResponse refreshAccessToken(String refreshToken, boolean isLuckTest) {
        Request request = new Request.Builder()
                .url(String.format((isLuckTest ? LUCK_DOMAIN : DOMAIN) + URL_ACCESS_TOKEN_REFRESH, APPID, APPSECRET, refreshToken))
                .get()
                .build();
        Call call = mOkHttpClient.newCall(request);
        return executeCall(call);
    }

    /**
     * 获取用户信息
     *
     * @param accessToken
     * @param isLuckTest
     * @return
     */
    public NewGameResponse getUserInfo(String accessToken, boolean isLuckTest) {
        Request request = new Request.Builder()
                .url(String.format((isLuckTest ? LUCK_DOMAIN : DOMAIN) + URL_USERINFO, accessToken))
                .get()
                .build();
        Call call = mOkHttpClient.newCall(request);
        return executeCall(call);
    }

    /**
     * 生成订单
     * body参数说明:{
     * "subject": "商品名,字符串，必填","body": "商品描述,字符串，必填",
     * "amount": "价格，单位分,整型，必填",
     * "notify_url": "通知地址，字符串，必填",
     * "app_user_name": "应用用户名,字符串",
     * "app_user_id": "应用用户ID（请用新游登录open_id）,字符串，必填",
     * "app_order_id": "应用订单号,字符串，必填",
     * "app_id": "应用ID,字符串，必填"(SDK自动填入,无需包含在body),
     * "imei": "IMEI,字符串",
     * "mac_address": "MAC地址,字符串",
     * "ext": "扩展信息，开发者选填，回调开发者服务器使用",
     * 'sign_type': '签名方式，字符串，可选参数，支持md5，必填(SDK自动填入,无需包含在body)'
     * "sign": "签名，字符串，与支付宝相同的签名方式，具体见后面的签名算法描述，必填(SDK自动填入,无需包含在body)"
     * }
     *
     * @param body
     * @param isLuckTest
     * @return
     * @throws JsonProcessingException
     */
    public NewGameResponse postOrder(Map body, boolean isLuckTest) throws JsonProcessingException {
        body.put("app_id", APPID);
        body.put("sign", sign(body, "md5"));
        body.put("sign_type", "md5");
        Request request = new Request.Builder()
                .url((isLuckTest ? LUCK_DOMAIN : DOMAIN) + URL_ORDER)
                .post(RequestBody.create(MediaType.parse("application/json"), new ObjectMapper().writeValueAsString(body)))
                .build();
        Call call = mOkHttpClient.newCall(request);
        return executeCall(call);
    }

    /**
     * 验签函数
     * 注:body只包含需参与签名参数,不需要验签的参数请勿放入body中
     *
     * @param body
     * @param sign
     * @return
     */
    public boolean verify(Map body, String sign) {
        StringBuilder sb = processSignString(body);

        try {
            KeyFactory kf = KeyFactory.getInstance(SIGNATURE_RSA);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(PLATFORM_PUBLIC_KEY));
            PublicKey publicKey = kf.generatePublic(keySpec);
            Signature signet = Signature.getInstance(SIGNATURE_SHA1WITHRSA);
            signet.initVerify(publicKey);
            signet.update(sb.toString().getBytes(Charsets.UTF_8));
            return signet.verify(Base64.decodeBase64(sign));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 签名
     *
     * @param body
     * @param signMethod
     * @return
     */
    private String sign(Map body, String signMethod) {
        StringBuilder sb = processSignString(body);

        switch (signMethod) {
            case SIGNATURE_MD5:
                //md5
                try {
                    MessageDigest md5 = MessageDigest.getInstance("MD5");
                    byte[] bytes = md5.digest(sb.toString().getBytes());
                    StringBuilder builder = new StringBuilder(40);
                    for (byte x : bytes) {
                        if ((x & 0xff) >> 4 == 0) {
                            builder.append("0").append(Integer.toHexString(x & 0xff));
                        } else {
                            builder.append(Integer.toHexString(x & 0xff));
                        }
                    }
                    return builder.toString();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    return null;
                }
            case SIGNATURE_RSA:

            default:
                return null;
        }
    }

    /**
     * 处理待签名字符串
     *
     * @param body
     * @return
     */
    private StringBuilder processSignString(Map body) {
        //sort
        Iterator<Map.Entry> iterator = sortMap(body).iterator();

        //拼接待签名字符串
        StringBuilder sb = new StringBuilder();
        Map.Entry entry = iterator.next();
        sb.append(entry.getKey()).append('=').append(entry.getValue());
        while (iterator.hasNext()) {
            entry = iterator.next();
            sb.append('&').append(entry.getKey()).append('=').append(entry.getValue());
        }
        sb.append(APPSECRET);
        return sb;
    }

    /**
     * 按字母排序
     *
     * @param map
     * @return
     */
    private List<Map.Entry> sortMap(Map map) {
        List<Map.Entry> infos = new ArrayList<Map.Entry>(map.entrySet());
        Collections.sort(infos, new Comparator<Map.Entry>() {
            @Override
            public int compare(Map.Entry o1, Map.Entry o2) {
                return (o1.getKey().toString().compareTo(o2.getKey().toString()));
            }
        });
        return infos;
    }

}
