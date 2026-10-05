package com.his.operation.service;

import com.his.common.base.PageResult;
import com.his.operation.dto.*;
import com.his.operation.vo.DaySurgeryApplyVO;
import com.his.operation.vo.DaySurgeryItemVO;
import com.his.operation.vo.DaySurgeryStatVO;

import java.util.List;

/**
 * 日间手术服务。
 *
 * <p>闭环：准入目录 → 预约登记 → 术前评估 → 手术安排 → 术后观察 → 出院 / 转住院 → 24h 随访。
 */
public interface DaySurgeryService {

    // 准入目录

    PageResult<DaySurgeryItemVO> itemListPage(DaySurgeryItemQueryPageDTO dto);

    /**
     * 启用中的术式下拉（预约用；停用术式不可新预约）
     */
    List<DaySurgeryItemVO> itemSelectList(Long deptId);

    DaySurgeryItemVO itemUpsert(DaySurgeryItemUpsertDTO dto);

    /**
     * 启停（停用后不可新预约，存量单不受影响）
     */
    DaySurgeryItemVO itemUpdateStatus(Long id, Integer status);

    // 登记单

    PageResult<DaySurgeryApplyVO> listPage(DaySurgeryQueryPageDTO dto);

    DaySurgeryApplyVO getDetailById(Long id);

    /**
     * 预约登记 / 修改（仅待评估可改；术式必须存在于启用中的目录）
     */
    DaySurgeryApplyVO applyUpsert(DaySurgeryApplyUpsertDTO dto);

    /**
     * 术前评估（不通过不得安排手术）
     */
    DaySurgeryApplyVO evaluate(DaySurgeryEvalDTO dto);

    /**
     * 安排手术（评估通过 → 已安排）
     */
    DaySurgeryApplyVO arrange(DaySurgeryArrangeDTO dto);

    /**
     * 完成手术（已安排 → 术后观察）
     */
    DaySurgeryApplyVO finishSurgery(DaySurgeryFinishDTO dto);

    /**
     * 离院登记（术后观察 → 已出院；转普通住院走 transferToIpd）
     */
    DaySurgeryApplyVO discharge(DaySurgeryDischargeDTO dto);

    /**
     * 转住院（术后观察 → 已转住院，住院号必填，终态）
     */
    DaySurgeryApplyVO transferToIpd(DaySurgeryTransferDTO dto);

    /**
     * 取消（非终态 → 已取消，原因必填）
     */
    DaySurgeryApplyVO cancel(DaySurgeryActionDTO dto);

    /**
     * 登记随访（已出院 / 已转住院后，24h 内必访一次）
     */
    DaySurgeryApplyVO follow(DaySurgeryFollowDTO dto);

    /**
     * 删除（软删；仅待评估且无随访）
     */
    boolean deleteById(Long id);

    DaySurgeryStatVO stat();
}
