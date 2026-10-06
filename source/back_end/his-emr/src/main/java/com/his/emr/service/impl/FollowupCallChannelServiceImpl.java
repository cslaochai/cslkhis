package com.his.emr.service.impl;

import com.his.common.exception.BusinessException;
import com.his.emr.config.FollowupCallProperties;
import com.his.emr.entity.BizFollowupTask;
import com.his.emr.enums.FollowupCallChannelEnum;
import com.his.emr.service.FollowupCallChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 外呼通道实现：{@code followup.call-channel=mock}（默认）走人工 —— 只登记「待外呼」，
 * 电话由护士自己拨；配了其他值说明运营上期望自动外呼，但线路没接入，
 * fail-fast 报错，绝不把「没拨出去」记成「已呼出」。
 */
@Service
@RequiredArgsConstructor
public class FollowupCallChannelServiceImpl implements FollowupCallChannelService {

    private final FollowupCallProperties callProperties;

    @Override
    public FollowupCallChannelEnum dial(BizFollowupTask task) {
        if (!callProperties.isMockChannel()) {
            throw new BusinessException("自动外呼通道（followup.call-channel=" + callProperties.effectiveCallChannel()
                    + "）尚未对接真实线路：拨号分支待线路凭据到位后按手册 G-15 施工，当前请走人工电话拨打");
        }
        return FollowupCallChannelEnum.MANUAL;
    }
}
