package com.his.emr.service.impl;

import com.his.common.exception.BusinessException;
import com.his.emr.entity.BizFollowupTask;
import com.his.emr.enums.FollowupCallChannelEnum;
import com.his.emr.service.FollowupCallChannelService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 外呼通道实现：followup.call-channel=mock（默认）走人工 —— 只登记「待外呼」，
 * 电话由护士自己拨；配了其他值说明运营上期望自动外呼，但线路没接入，
 * fail-fast 报错，绝不把「没拨出去」记成「已呼出」。
 */
@Service
public class FollowupCallChannelServiceImpl implements FollowupCallChannelService {

    private static final String CHANNEL_MOCK = "mock";

    @Value("${followup.call-channel:mock}")
    private String callChannel;

    @Override
    public FollowupCallChannelEnum dial(BizFollowupTask task) {
        if (!CHANNEL_MOCK.equalsIgnoreCase(StringUtils.hasText(callChannel) ? callChannel.trim() : CHANNEL_MOCK)) {
            throw new BusinessException("自动外呼通道未接入（followup.call-channel=" + callChannel + "），请走人工电话拨打");
        }
        return FollowupCallChannelEnum.MANUAL;
    }
}
