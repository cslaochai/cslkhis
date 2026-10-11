package com.his.system.api.service;

import com.his.system.api.vo.ExecStatusVO;

import java.util.List;

/**
 * 申请单执行进度查询扩展点（由 his-medicaltech 模块提供实现）。
 */
public interface ApplyExecStatusApi {

    /**
     * 批量查询检查申请单的执行进度。
     *
     * @param applyIds 检查申请单 ID 集合（可为空）
     * @return 与入参对应的执行进度；没有执行记录的申请单不会有返回项
     */
    List<ExecStatusVO> listInspectionExecStatus(List<Long> applyIds);

    /**
     * 批量查询检验申请单的执行进度，语义同 {@link #listInspectionExecStatus(List)}。
     *
     * @param applyIds 检验申请单 ID 集合（可为空）
     * @return 执行进度列表
     */
    List<ExecStatusVO> listLaboratoryExecStatus(List<Long> applyIds);
}
