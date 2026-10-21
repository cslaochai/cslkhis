package com.his.clinical.service.impl;

import com.his.common.exception.BusinessException;
import com.his.clinical.config.FollowupCallProperties;
import com.his.clinical.entity.BizFollowupTask;
import com.his.common.enums.FollowupCallChannelEnum;
import com.his.clinical.service.FollowupCallChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 外呼通道实现：followup.call-channel=mock（默认）走人工 —— 只登记「待外呼」，
 */
@Service
@RequiredArgsConstructor
public class FollowupCallChannelServiceImpl implements FollowupCallChannelService {

    private final FollowupCallProperties followupCallProperties;

    @Override
    public FollowupCallChannelEnum dial(BizFollowupTask task) {
        if (!followupCallProperties.isMockChannel()) {
            throw new BusinessException("自动外呼通道（followup.call-channel=" + followupCallProperties.effectiveCallChannel()
                    + "）尚未对接真实线路：拨号分支待线路凭据到位后按手册 G-15 施工，当前请走人工电话拨打");
        }
        return FollowupCallChannelEnum.MANUAL;
    }
}
