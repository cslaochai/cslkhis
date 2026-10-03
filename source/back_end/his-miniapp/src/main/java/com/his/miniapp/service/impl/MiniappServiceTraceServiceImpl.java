package com.his.miniapp.service.impl;

import com.his.miniapp.dto.ServiceTraceDTO;
import com.his.miniapp.entity.SysServiceTrace;
import com.his.miniapp.mapper.MiniappServiceTraceMapper;
import com.his.miniapp.service.MiniappServiceTraceService;
import com.his.security.CurrentUser;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 客服页自助行为埋点。
 *
 * <p><b>失败一律吞掉</b>：埋点是给运营看的，不是患者要看的。
 * 写不进去最多是这条指标少一个样本，让患者看到「提交失败」才是真的事故。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniappServiceTraceServiceImpl implements MiniappServiceTraceService {

    private static final int EVENT_KEY_MAX = 200;

    private static final int SESSION_ID_MAX = 64;

    private final MiniappServiceTraceMapper traceMapper;

    @Override
    public void record(ServiceTraceDTO dto) {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            SysServiceTrace entity = new SysServiceTrace();
            if (user != null) {
                entity.setUserId(user.getUserId());
                entity.setPatientId(user.getPatientId());
                entity.setCreateBy(user.getUsername());
            }
            entity.setSessionId(cut(dto.getSessionId(), SESSION_ID_MAX));
            entity.setEventType(cut(dto.getEventType(), 32));
            entity.setEventKey(cut(dto.getEventKey(), EVENT_KEY_MAX));
            entity.setFaqId(parseId(dto.getFaqId()));
            entity.setHitCount(dto.getHitCount());
            traceMapper.insert(entity);
        } catch (Exception ex) {
            log.warn("[客服埋点] 写入失败，忽略 type={} key={} err={}",
                    dto.getEventType(), dto.getEventKey(), ex.getMessage());
        }
    }

    /**
     * ID 只按字符串解析：前端传来的雪花 ID 一旦经过 JSON 数字就会丢精度，
     * 表现为「埋点的 faqId 对不上任何一条 FAQ」，而且不报错。
     */
    private static Long parseId(String value) {
        if (!StringUtils.hasText(value) || !value.matches("\\d{1,20}")) {
            return null;
        }
        return Long.parseLong(value);
    }

    private static String cut(String text, int max) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String value = text.trim();
        return value.length() <= max ? value : value.substring(0, max);
    }
}
