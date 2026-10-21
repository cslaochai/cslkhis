package com.his.clinical.service;

import com.his.common.base.PageResult;
import com.his.clinical.dto.*;
import com.his.clinical.vo.*;

import java.util.List;

/**
 * 临床路径服务：模板（草稿→使用中→已停用）+ 入径 + 变异登记 + 完成/退径 + 变异分析。
 */
public interface PathwayService {

    // 模板

    PageResult<PathwayVO> listPage(PathwayPageQueryDTO dto);

    /**
     * 单张路径模板详情：连阶段与步骤明细一起返回，步骤顺序即入径时的快照顺序。
     * 用于「临床路径」页「路径模板」页签的详情弹框与编辑弹框打开时的表单回显。
     */
    PathwayVO getDetailById(Long id);

    /**
     * 使用中模板下拉（入径选择用）
     */
    List<PathwayVO> activeSelectList(Long deptId);

    /**
     * 新增/修改草稿（步骤整组替换；使用中/已停用锁定不可编辑）
     */
    PathwayVO pathwayUpsert(PathwayUpsertDTO dto);

    /**
     * 发布：草稿→使用中（须有步骤；回算 total_days；同码仅一张使用中）
     */
    PathwayVO publishPathway(PathwayActionDTO dto);

    /**
     * 停用：使用中→已停用（存量入径不受影响）
     */
    PathwayVO deprecatePathway(PathwayActionDTO dto);

    // 入径 / 变异 / 终态

    PageResult<PathwayEnrollVO> enrollListPage(EnrollPageQueryDTO dto);

    /**
     * 入径详情：模板快照、当前路径日、步骤完成进度与名下变异台账一次给全。
     * 用于「临床路径」页「在径管理」页签行点开的入径详情，完成与退径都在这一屏发起。
     */
    PathwayEnrollVO enrollGetDetailById(Long id);

    /**
     * 可入径候选：在院且无在径记录的住院
     */
    List<PathwayAdmissionVO> admissionsForEnroll(String keyword, Integer limit);

    /**
     * 入径登记（id 为空新增；在径状态可改入径日期）
     */
    PathwayEnrollVO enrollUpsert(EnrollUpsertDTO dto);

    /**
     * 登记变异（追加台账，仅在径；回算 variance_count）
     */
    PathwayEnrollVO varianceUpsert(VarianceUpsertDTO dto);

    /**
     * 完成入径：在径转已完成并记完成日期与完成人，名下有变异记录不挡完成。
     * 用于入径详情里路径按疗程走完时的完成操作。
     */
    PathwayEnrollVO finishEnroll(EnrollActionDTO dto);

    /**
     * 退径（原因必填，终态）
     */
    PathwayEnrollVO abortEnroll(EnrollActionDTO dto);

    // 分析

    PathwayAnalysisVO analysis(Long pathwayId);

    // 医生站软约束（只读，不拦截）

    /**
     * 医生站横幅：该住院的在径记录（含派生路径日+模板步骤）；不在径返回 null
     */
    PathwayEnrollVO activeEnrollByAdmission(Long admissionId);

    /**
     * 开单偏离预检：本次医嘱条目 vs 在径模板（仅带编码步骤可比对）
     */
    OrderCheckVO orderCheck(OrderCheckDTO dto);
}
