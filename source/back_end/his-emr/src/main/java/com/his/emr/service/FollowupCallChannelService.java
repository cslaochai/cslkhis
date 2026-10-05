package com.his.emr.service;

import com.his.emr.entity.BizFollowupTask;
import com.his.emr.enums.FollowupCallChannelEnum;

/**
 * 随访电话外呼通道。当前只通人工通道（mock=登记待呼，护士自己拨号后回填结果）；
 * 真实自动外呼（语音机器人/运营商线路）接入时在此按配置分支扩展，
 * 不另起接口 + Mock 实现的两层写法。
 */
public interface FollowupCallChannelService {

    /**
     * 发起一通外呼，返回实际走的通道。
     * 自动通道未接入时直接报错拒绝 —— 登记成功却假装「已呼叫」是造假。
     */
    FollowupCallChannelEnum dial(BizFollowupTask task);
}
