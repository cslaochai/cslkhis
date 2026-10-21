package com.his.outpatient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.enums.NurseScheduleChangeTypeEnum;
import com.his.outpatient.dto.NurseScheduleLogQueryDTO;
import com.his.outpatient.entity.BizSchedule;
import com.his.outpatient.vo.NurseScheduleLogVO;

/**
 * 护理排班审批与变更留痕服务（280）。
 *
 * <p>提交/审核发布/撤回/进入调班/改格/删格/整周流转/复制上周都经此落账，与业务事务同生共死；
 * 原因列超宽截断，防止把业务顶成 50。
 */
public interface NurseScheduleLogService {

    /**
     * 记录一条留痕。
     *
     * @param scheduleId   排班行ID（整周提交/发布/复制等无单行的动作可传 null）
     * @param type         动作类型
     * @param amount       变更数量（批量铺/复制/整周条数，可空）
     * @param reason       变更/审批原因或意见（超 200 截断，可空）
     * @param row          排班快照（取 日期/单元名/科室名/护士名；删除类务必在删前传入，否则审计只剩裸 ID）
     * @param fromSnapshot 变更前快照文本（班次/状态，可空）
     * @param toSnapshot   变更后快照文本（班次/状态，可空）
     */
    Long record(Long scheduleId, NurseScheduleChangeTypeEnum type, Integer amount, String reason,
                BizSchedule row, String fromSnapshot, String toSnapshot);

    /**
     * 分页查询（按发生时间倒序）。
     */
    IPage<NurseScheduleLogVO> pageByQuery(NurseScheduleLogQueryDTO query);
}
