<?php
/**
 * Created by PhpStorm.
 * User: ruyi
 * Date: 16-4-6
 * Time: 下午3:40
 */

class NewGameBase{

    public function __construct($sandbox=false){
        $config = require "config.php";
        $this->app_id = $config['APP_ID'];
        $this->app_secret = $config['APP_SECRET'];
        $this->platform_public_key = $this->_format_public_key($config['PLATFORM_PUBLIC_KEY']);
        $this->app_private_key = $this->_format_private_key($config['APP_PRIVATE_KEY']);
        if($sandbox){
            $this->api_url = "http://luck.passport.newgame.com/";
        }else{
            $this->api_url = "https://passport.newgame.com/";
        }
    }

    protected function _call_api($api_url, $data='', $method='GET'){
        $curl = curl_init($api_url);
        curl_setopt($curl, CURLOPT_TIMEOUT, 10);
        curl_setopt($curl,CURLOPT_RETURNTRANSFER, true);
        if(strtolower($method) == 'post'){
            curl_setopt($curl,CURLOPT_POST,true);
            curl_setopt($curl,CURLOPT_POSTFIELDS, $data);
            curl_setopt($curl, CURLOPT_HTTPHEADER, array(
                'Content-Type: application/json',
                'Content-Length: ' . strlen($data))
            );
        }
        $content = curl_exec($curl);
        $http_code = curl_getinfo($curl, CURLINFO_HTTP_CODE);
        curl_close($curl);

        if($http_code == 200){
            $data = json_decode($content, true);
            return $data['data'];
        }else if($http_code = 400){
            $meta = json_decode($content, true);
            throw new Exception($meta['meta']['message'], $meta['meta']['code']);
        }
        throw new Exception('Access Error');
    }

    protected function _format_public_key($public_key){
        $key = "-----BEGIN PUBLIC KEY-----\n";
        $key .= chunk_split($public_key, 64, "\n");
        $key .= "-----END PUBLIC KEY-----";
        return $key;
    }

    protected function _format_private_key($private_key){
        $key = "-----BEGIN RSA PRIVATE KEY-----\n";
        $key .= chunk_split($private_key, 64, "\n");
        $key .= "-----END RSA PRIVATE KEY-----";
        return $key;
    }

}


class NewgamePay extends NewGameBase{

    public function post_order($subject, $body, $amount, $notify_url, $app_order_id, $app_user_id, $sign_type='rsa'){
        $data = array(
            "app_id" => $this->app_id,
            "subject" => $subject,
            "body" => $body,
            "amount" => $amount,
            "notify_url" => $notify_url,
            "app_order_id" => $app_order_id,
            "notify_url" => $notify_url,
            "app_user_id" => $app_user_id,
            "sign_type" => $sign_type
        );

        $url = $this->api_url . "api/pay/order";
        $data['sign'] = $this->sign($data);

        return $this->_call_api($url, json_encode($data), 'POST');
    }

    public function get_order($app_order_id, $sign_type='rsa'){
        $data = array(
            "app_id" => $this->app_id,
            "app_order_id" => $app_order_id,
            "sign_type" => $sign_type
        );
        $data['sign'] = $this->sign($data);

        $url = $this->api_url . "api/pay/order?" . http_build_query($data);

        return $this->_call_api($url);
    }

    public function verify($data){
        //转换为openssl格式密钥
        $res = openssl_get_publickey($this->platform_public_key);
        //调用openssl内置方法验签，返回bool值
        $sign = $data['sign'];
        $sign_str = $this->_build_sign_str($data);
        $result = (bool)openssl_verify($sign_str, base64_decode($sign), $res, OPENSSL_ALGO_SHA1);
        //释放资源
        openssl_free_key($res);

        //返回资源是否成功
        return $result;
    }

    public function sign($data){
        $sign_type = $data['sign_type'];
        $sign_str = $this->_build_sign_str($data);
        if($sign_type == 'rsa'){
            //转换为openssl密钥
            $res = openssl_get_privatekey($this->app_private_key);
            //调用openssl内置签名方法，生成签名$sign
            openssl_sign($sign_str, $sign, $res, OPENSSL_ALGO_SHA1);
            //释放资源
            openssl_free_key($res);
            //base64编码
            $sign = base64_encode($sign);
        }else{
            $sign = md5($sign_str . $this->app_secret);
        }

        return $sign;
    }

    protected function _build_sign_str($data){
        ksort($data);
        $str = '';
        foreach($data as $key=>$val){
            if($key != 'sign' && $key != 'sign_type'){
                $str .= "&$key=$val";
            }
        }
        $str = ltrim($str, "&");
        return $str;
    }

}



class NewGameOauth extends NewGameBase{

    public function build_auth_url($redirect_uri, $state=''){
        $params = array(
            "app_id" => $this->app_id,
            "redirect_uri" => $redirect_uri,
            "state" => $state
        );

        return $this->api_url . "oauth2/authorize?" . http_build_query($params);
    }

    public function get_access_token($grant_type="authorization_code", $auth_code='', $refresh_token=''){
        $url = $this->api_url . "oauth2/access_token";
        $params = array(
            "grant_type" => $grant_type,
            "code" => $auth_code,
            "app_id" => $this->app_id,
            "app_secret" => $this->app_secret,
            "refresh_token" => $refresh_token
        );
        $url .= "?" . http_build_query($params);

        return $this->_call_api($url);
    }

    public function get_user_info($access_token){
        $url = $this->api_url . "api/users/user_info";
        $params = array(
            "access_token" => $access_token
        );
        $url .= "?" . http_build_query($params);

        return $this->_call_api($url);
    }

}
