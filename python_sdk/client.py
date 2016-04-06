# -*- coding: utf-8 -*-
__author__ = 'ruyi'

from .config import APP_PRIVATE_KEY, PLATFORM_PUBLIC_KEY, APP_ID, SANDBOX, APP_SECRET
from .utils import core
from .utils.crypt import RSAUtils
import requests, json
import urllib


class NewGamePay(object):

    def __init__(self, url=''):
        self.app_id = APP_ID
        self.app_private_key = APP_PRIVATE_KEY
        self.platform_public_key = PLATFORM_PUBLIC_KEY
        if SANDBOX:
            self.api_url = "http://luck.passport.newgame.com/"
        else:
            self.api_url = "https://passport.newgame.com/"
        if url:
            self.api_url = url


    def post_order(self, subject, body, amount, notify_url, app_order_id, app_user_id):
        """
        发送订单请求，如果成功返回新游支付订单号(order_id)和订单支付页面url(pay_url)
        :param subject: 商品名
        :param body: 商品说明
        :param amount: 单位分，如1元=100
        :param notify_url: 通知url，交易成功后支付服务器会发送POST请求到notify_url
        :param app_order_id: 商家订单号
        :param app_user_id: 商家用户id
        :return: {"id": "23423423423", "pay_url":"支付url，访问时请加上参数redirect_url"}
        """
        url = self.api_url + 'api/pay/order'
        data ={
            "subject": subject,
            "body": body,
            "amount": int(amount),
            "notify_url": notify_url,
            "app_order_id": app_order_id,
            "app_user_id": app_user_id,
            "app_id": self.app_id,
            "sign_type": "rsa"
        }

        sign = self.sign(data)
        data['sign'] = sign
        resp = requests.post(url, data=json.dumps(data), timeout=10)
        if resp.status_code == 200:
            return_data = json.loads(resp.content)
            return return_data['data']
        if resp.status_code == 400:
            error = json.loads(resp.content)
            raise Exception(error['meta']['message'])

        return None


    def verify(self, data):
        """
        使用平台公钥验证返回数据签名
        :param data:
        :return:
        """
        sign = data.get('sign')
        sign_string = core.get_sign_string(data, filter_params=['sign', 'sign_type'])
        rsa = RSAUtils(public_key=self.platform_public_key)
        return rsa.verify(sign_string, sign)


    def sign(self, data):
        """
        使用开发者应用私钥生成数据签名
        :param data:
        :return:
        """
        sign_string = core.get_sign_string(data, filter_params=['sign', 'sign_type'])
        print self.app_private_key
        rsa = RSAUtils(private_key=self.app_private_key)
        return rsa.sign(sign_string)


class NewGameOauth(object):

    def __init__(self, url=''):
        self.app_id = APP_ID
        self.app_secret = APP_SECRET
        if SANDBOX:
            self.api_url = "http://luck.passport.newgame.com/"
        else:
            self.api_url = "https://passport.newgame.com/"
        if url:
            self.api_url = url


    def build_auth_url(self, redirect_uri, state=''):
        params = {
            "app_id": self.app_id,
            "redirect_uri": redirect_uri,
            "state": state
        }
        url = self.api_url + "oauth2/authorize"
        return url + "?" + urllib.urlencode(params)


    def get_access_token(self, grant_type="authorization_code", auth_code='', refresh_token=''):
        url = self.api_url + "oauth2/access_token"
        params = {
            "grant_type": grant_type,
            "code": auth_code,
            "app_id": self.app_id,
            "app_secret": self.app_secret,
            "refresh_token": refresh_token
        }
        url += "?" + urllib.urlencode(params)

        return self._call_api(url)


    def get_user_info(self, access_token):
        url = self.api_url + "api/users/user_info"
        params = {
            "access_token": access_token
        }
        url += "?" + urllib.urlencode(params)

        return self._call_api(url)


    def _call_api(self, api_url):
        resp = requests.get(api_url, timeout=10)
        if resp.status_code == 200:
            return_data = json.loads(resp.content)
            return return_data['data']
        elif resp.status_code == 400:
            error = json.loads(resp.content)
            raise Exception(error['meta']['message'])

        return Exception('Access Error')