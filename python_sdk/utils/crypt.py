# !-*- coding:utf-8 -*-

import base64
from Crypto import Random
from Crypto.Cipher import PKCS1_v1_5, AES
from Crypto.Hash import SHA
from Crypto.Signature import PKCS1_v1_5 as Pk
from Crypto.PublicKey import RSA
import os
import hashlib


class RSAUtils(object):
    """RSA辅助类
    """
    public_key = None
    private_key = None

    def __init__(self, public_key=None, private_key=None):
        self.public_key = public_key
        self.private_key = private_key

    def rsa_base64_encrypt(self, data):
        """ rsa加密
        1. rsa encrypt
        2. base64 encrypt
        :param data: 数据
        :param public_key: 公钥
        """
        public_key = self.get_public_key()

        cipher = PKCS1_v1_5.new(public_key)
        return base64.b64encode(cipher.encrypt(data))

    def rsa_base64_decrypt(self, data):
        """
        :param data: 数据
        :param private_key: 私钥

        1. base64 decrypt
        2. rsa decrypt
        示例代码

       key = RSA.importKey(open('privkey.der').read())
        >>>
        >>> dsize = SHA.digest_size
        >>> sentinel = Random.new().read(15+dsize)      # Let's assume that average data length is 15
        >>>
        >>> cipher = PKCS1_v1_5.new(key)
        >>> message = cipher.decrypt(ciphertext, sentinel)
        >>>
        >>> digest = SHA.new(message[:-dsize]).digest()
        >>> if digest==message[-dsize:]:                # Note how we DO NOT look for the sentinel
        >>>     print "Encryption was correct."
        >>> else:
        >>>     print "Encryption was not correct."
        """
        private_key = self.get_private_key()

        cipher = PKCS1_v1_5.new(private_key)
        return cipher.decrypt(base64.b64decode(data), Random.new().read(15 + SHA.digest_size))

    def sign(self, sign_data):
        """ RSA签名
        :param private_key: 私钥
        :param sign_data: 需要签名的字符串
        """

        private_key = self.get_private_key()

        h = SHA.new(sign_data)
        signer = Pk.new(private_key)
        signn = signer.sign(h)
        signn = base64.b64encode(signn)
        return signn

    def verify(self, data, sign):
        """
        RSA验签
        结果：如果验签通过，则返回True
             如果验签不通过，则返回False
        :param data: str 数据
        :param sign: str 签名
        :param public_key: str 公钥
        """

        public_key = self.get_public_key()

        signn = base64.b64decode(sign)
        verifier = Pk.new(public_key)
        return verifier.verify(SHA.new(data), signn)

    def get_public_key(self):
        public_key = self.public_key
        return RSA.importKey('-----BEGIN PUBLIC KEY-----\n' + public_key + '\n-----END PUBLIC KEY-----')

    def get_private_key(self):
        private_key = self.private_key
        return RSA.importKey('-----BEGIN RSA PRIVATE KEY-----\n' + private_key + '\n-----END RSA PRIVATE KEY-----')


class AesUtils(object):
    mode = AES.MODE_CFB

    @classmethod
    def encrypt(cls, text):
        key = Random.get_random_bytes(AES.block_size)
        iv = Random.get_random_bytes(AES.block_size)
        cipher = AES.new(key, cls.mode, iv)
        return base64.b64encode(key + iv + cipher.encrypt(text))

    @classmethod
    def decrypt(cls, text):
        text = base64.b64decode(text)
        key = text[:16]
        iv = text[16:32]
        cipher = AES.new(key, cls.mode, iv)
        plain_text = cipher.decrypt(text[32:])
        return plain_text


class Md5Utils(object):

    @classmethod
    def sign(cls, data, key):
        return hashlib.md5(data + key).hexdigest()

    @classmethod
    def verify(cls, data, sign, key):
        return cls.sign(data, key) == sign
