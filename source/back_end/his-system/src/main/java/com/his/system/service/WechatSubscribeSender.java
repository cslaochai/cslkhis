package com.his.system.service;

import java.util.Map;

public interface WechatSubscribeSender {

    String send(String openid, String scene, String page, Map<String, String> data);
}
