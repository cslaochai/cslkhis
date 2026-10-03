package com.his.system.service.impl;

import com.his.system.service.SmsCodeService;
import com.his.system.config.SmsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务（发码 + 校验，Redis 存储）。
 *
 * <p><b>失败关闭：</b>未开启 mock 且未接网关时发码直接失败，不假装「已发送」。
 * 发码失败绝不让注册接口变成 500，调用方拿 {@link SendResult} 自行决定文案。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeServiceImpl implements SmsCodeService {

    private static final String CODE_KEY = "sms:code:%s:%s";
    private static final String LIMIT_KEY = "sms:limit:%s:%s";
    /** 同一验证码最多试错次数，超过即作废，防穷举 */
    private static final int MAX_RETRY = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SmsProperties properties;
    private final StringRedisTemplate redisTemplate;

    /**
     * 发送验证码。
     *
     * @param phone 手机号
     * @param scene 业务场景码（如 register），与校验时必须是同一个
     * @return 发送结果，mock 模式下编码非空供联调回显
     */
    public SendResult send(String phone, String scene) {
        String codeKey = codeKey(scene, phone);
        String limitKey = limitKey(scene, phone);
        Boolean first = redisTemplate.opsForValue()
                .setIfAbsent(limitKey, "1", properties.getResendSeconds(), TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(first)) {
            Long ttl = redisTemplate.getExpire(limitKey, TimeUnit.SECONDS);
            long wait = ttl == null || ttl < 0 ? properties.getResendSeconds() : ttl;
            return SendResult.fail("发送过于频繁，请 " + wait + " 秒后再试");
        }

        String code = randomCode(properties.getLength());
        if (!properties.isMock()) {
            log.warn("短信网关未接入，验证码不下发 phone={} scene={}", maskPhone(phone), scene);
            return SendResult.fail("短信通道未开通");
        }
        redisTemplate.opsForValue().set(codeKey, code, properties.getTtlSeconds(), TimeUnit.SECONDS);
        log.info("[SMS-MOCK] phone={} scene={} code={}", maskPhone(phone), scene, code);
        return SendResult.ok(code);
    }

    /**
     * 校验并消费验证码（一次一用，校验通过即删除）。
     *
     * @return null=校验通过；非 null=失败原因
     */
    public String verify(String phone, String scene, String code) {
        if (code == null || code.isBlank()) {
            return "请输入验证码";
        }
        String codeKey = codeKey(scene, phone);
        String cached = redisTemplate.opsForValue().get(codeKey);
        if (cached == null) {
            return "验证码已失效，请重新获取";
        }
        if (!cached.equals(code.trim())) {
            Long times = redisTemplate.opsForValue().increment(failKey(scene, phone));
            redisTemplate.expire(failKey(scene, phone), properties.getTtlSeconds(), TimeUnit.SECONDS);
            if (times != null && times >= MAX_RETRY) {
                redisTemplate.delete(codeKey);
                return "验证码错误次数过多，请重新获取";
            }
            return "验证码不正确";
        }
        redisTemplate.delete(codeKey);
        redisTemplate.delete(failKey(scene, phone));
        return null;
    }

    private static String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    private static String codeKey(String scene, String phone) {
        return String.format(CODE_KEY, scene, phone);
    }

    private static String limitKey(String scene, String phone) {
        return String.format(LIMIT_KEY, scene, phone);
    }

    private static String failKey(String scene, String phone) {
        return String.format("sms:fail:%s:%s", scene, phone);
    }

    /** 日志脱敏，避免手机号明文进日志 */
    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
