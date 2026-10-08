package com.his.operation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.operation.dto.*;
import com.his.operation.vo.OperationApplyVO;
import com.his.operation.vo.OperationScheduleMatrixVO;

import java.util.List;

/**
 * 住院手术闭环服务
 */
public interface OperationApplyService {

    /**
     * 手术申请分页
     */
    IPage<OperationApplyVO> listPage(OperationApplyQueryPageDTO query);

    /**
     * 手术申请详情
     */
    OperationApplyVO getDetailById(Long applyId);

    /**
     * 某次住院的全部手术申请（按申请时间升序 = 这条链的发生顺序）
     */
    List<OperationApplyVO> listByAdmission(Long admissionId);

    /**
     * 发起 / 修改手术申请（返回手术申请单号；修改仅允许「待排期」）
     */
    String save(OperationApplyUpsertDTO dto);

    /**
     * 排台（待排期 → 已排期；已排期可改期）
     */
    void schedule(OperationScheduleDTO dto);

    /**
     * 术前核对（已排期 → 术前核对完成；必核项缺失直接拒绝）
     */
    void preopCheck(OperationPreopCheckDTO dto);

    /**
     * 完成（术前核对完成 → 已完成；回写病案首页手术明细 + 手术记录病历）
     */
    void finish(OperationFinishDTO dto);

    /**
     * 取消（仅待排期 / 已排期 → 已取消）
     */
    void cancel(OperationCancelDTO dto);

    /**
     * 未完成手术数（工作台角标）
     */
    long countUnfinished(Long admissionId);

    /**
     * 排台总表：某天 × 启用手术间的矩阵（含待排期暂存区与未登记手术间兜底桶）
     */
    OperationScheduleMatrixVO scheduleMatrix(String date);

    /**
     * 已用过的手术间（下拉候选）
     */
    List<String> roomList();

    /**
     * 术前核对要点字典
     */
    List<OperationApplyVO.CheckItem> checkItems();
}
