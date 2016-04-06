# !-*- coding:utf-8 -*-

import urllib
from collections import OrderedDict


def to_string(val):
    if isinstance(val, int) or isinstance(val, long) or isinstance(val, float):
        return str(val)
    elif val is None:
        return ''
    else:
        return val.encode('utf-8')


def create_link_string(params):
    """把字典所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串

    :param params: dict 需要拼接的数组
    """
    args = ''
    for key, val in params.iteritems():
        args += key + '=' + to_string(val) + '&'

    return args[0:-1]


def create_quote_link_string(params):
    """把字典所有元素，按照“参数="参数值"”的模式用“&”字符拼接成字符串

    :param params: dict 需要拼接的数组
    """
    args = ''
    for key, val in params.iteritems():
        args += key + '="' + to_string(val) + '"&'

    return args[0:-1]


def create_link_string_urlencode(params):
    """把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串，并对字符串做urlencode编码
    :param params: dict 需要拼接的数组
    """
    return urllib.urlencode(params)


def create_quote_link_string_urlencode(params):
    """把数组所有元素，按照'参数="参数值"'的模式用“&”字符拼接成字符串，并对字符串做urlencode编码
    :param params: dict 需要拼接的数组
    """
    args = ''
    for key, val in params.iteritems():
        args += key + '="' + urllib.quote_plus(to_string(val)) + '"&'

    return args[0:-1]


def para_filter(params, filter_params=None):
    """除去数组中的空值和签名参数

    :param params: dict 签名参数组
    """
    filtered_params = OrderedDict()

    if filter_params is None:
        filter_params = ['sign', 'sign_type']

    for key, val in params.iteritems():
        if key not in filter_params and val != '' and val is not None:
            filtered_params[key] = val
    return filtered_params


def arg_sort(d):
    """对数组排序

    :param d: dict 排序前的数组
    """
    return OrderedDict(sorted(d.items(), key=lambda t: t[0]))


def charset_encode(string, output_charset='', input_charset=''):
    """实现多种字符编码方式

    :param string: str 需要编码的字符串
    :param output_charset: str 输出的编码格式
    :param input_charset: str 输入的编码格式
    """
    if input_charset == output_charset or string == '':
        output = string
    else:
        if input_charset:
            output = string.decode(input_charset).encode(output_charset)
        else:
            output = string.encode(output_charset)
    return output


def charset_decode(string, input_charset='', output_charset=''):
    """实现多种字符编码方式

    :param string: str 需要编码的字符串
    :param input_charset: str 输入的编码格式
    :param output_charset: str 输出的编码格式
    """
    if input_charset == output_charset or string == '':
        output = string
    else:
        if input_charset:
            output = string.decode(input_charset).encode(output_charset)
        else:
            output = string.encode(output_charset)
    return output


def get_sign_string(params, filter_params=None):
    # 除去待签名参数数组中的空值和签名参数
    filtered_params = para_filter(params, filter_params=filter_params)

    # 对待签名参数数组排序
    sorted_params = arg_sort(filtered_params)

    # 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
    return create_link_string(sorted_params)
