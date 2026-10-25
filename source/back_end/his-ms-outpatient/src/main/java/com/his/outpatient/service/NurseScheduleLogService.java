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

    /**
     * 查最近一次「提交审核」动作的提交人（员工档案ID）：先按排班行查单行提交留痕，
     * 查不到再按排班单元名查整周提交留痕。用于审核发布前的自审闸定位编制人。
     *
     * @param scheduleId 排班行ID（整周审核传 null，只走单元名回溯）
     * @param orgName    排班单元名称（病区/门诊科室名快照）
     * @return 提交留痕的经办员工ID；没有任何提交留痕时返回 {@code null}，由调用方决定回退口径
     */
    Long latestSubmitterId(Long scheduleId, String orgName);
}
