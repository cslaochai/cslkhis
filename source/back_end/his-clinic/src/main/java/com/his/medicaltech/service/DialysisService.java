package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.DialysisDTO;
import com.his.medicaltech.vo.DialysisVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 血液净化（透析）中心服务：透析档案、处方、机位台账、排班与治疗执行、工作量统计的业务收口。
 */
public interface DialysisService {

    /**
     * 透析档案分页：患者电话出参脱敏。
     * 用于透析中心「档案」查询卡。
     */
    PageResult<DialysisVO.ArchiveVO> archiveListPage(DialysisDTO.ArchiveQuery query);

    /**
     * 透析档案单条详情：电话回明文。
     * 用于档案编辑弹框回显（编辑提交会整对象回写，脱敏值会洗坏真号）。
     */
    DialysisVO.ArchiveVO archiveGetById(Long id);

    /**
     * 透析档案新增/修改：一人一档，患者快照由服务端重查覆盖前端传值。
     * 用于档案「新增/编辑」弹框保存。
     */
    DialysisVO.ArchiveVO archiveUpsert(DialysisDTO.ArchiveUpsert dto);

    /**
     * 档案治疗状态变更（在透/暂停/退出）：暂停与退出必填原因。
     * 用于档案行「状态变更」弹框提交。
     */
    DialysisVO.ArchiveVO archiveChangeStatus(DialysisDTO.ArchiveStatus dto);

    /**
     * 某档案的处方台账：按开具时间新→旧排列。
     * 用于档案详情「处方」页签展示。
     */
    List<DialysisVO.PrescriptionVO> prescriptionList(Long archiveId);

    /**
     * 透析处方新增/修改：新开处方自动停用该档案名下旧的有效处方。
     * 用于处方编辑弹框保存。
     */
    DialysisVO.PrescriptionVO prescriptionUpsert(DialysisDTO.PrescriptionUpsert dto);

    /**
     * 停用透析处方：有效处方转为停用。
     * 用于处方台账行「停用」操作。
     */
    DialysisVO.PrescriptionVO prescriptionStop(DialysisDTO.PrescriptionStop dto);

    /**
     * 机位台账分页。
     * 用于透析中心「机位管理」查询卡。
     */
    PageResult<DialysisVO.MachineVO> machineListPage(DialysisDTO.MachineQuery query);

    /**
     * 可用机位下拉：只返回当前可排班的机位。
     * 用于排班弹框的机位选择。
     */
    List<DialysisVO.MachineVO> machineSelectList();

    /**
     * 机位新增/修改：机位号唯一。
     * 用于机位管理弹框保存。
     */
    DialysisVO.MachineVO machineUpsert(DialysisDTO.MachineUpsert dto);

    /**
     * 透析单分页台账。
     * 用于透析中心「治疗单」查询卡。
     */
    PageResult<DialysisVO.SessionVO> sessionListPage(DialysisDTO.SessionQuery query);

    /**
     * 透析单单条详情。
     * 用于治疗单详情页取数。
     */
    DialysisVO.SessionVO sessionGetById(Long id);

    /**
     * 日看板：三时段 × 全机位的排班与治疗占位视图。
     * 用于透析中心首页日看板。
     *
     * @param date 看板日期，缺省为今天
     */
    DialysisVO.BoardVO board(LocalDate date);

    /**
     * 排班：占用机位生成透析单并快照当时处方。
     * 用于看板「排班」弹框提交。
     */
    DialysisVO.SessionVO schedule(DialysisDTO.Schedule dto);

    /**
     * 改期/改机位：仅「已排班」未执行的透析单可改。
     * 用于看板格子上已排班透析单的改期操作。
     */
    DialysisVO.SessionVO reschedule(DialysisDTO.Reschedule dto);

    /**
     * 上机：登记透前体重与通路评估后透析单转入治疗中。
     * 用于治疗单执行动作弹框「上机」提交。
     */
    DialysisVO.SessionVO startSession(DialysisDTO.SessionStart dto);

    /**
     * 下机：超滤量与实际治疗时长按登记值服务端回算。
     * 用于治疗单执行动作弹框「下机」提交。
     */
    DialysisVO.SessionVO finishSession(DialysisDTO.SessionFinish dto);

    /**
     * 登记不良反应：治疗中或已完成的透析单均可补记。
     * 用于治疗单执行动作弹框「不良反应」提交。
     */
    DialysisVO.SessionVO recordAdverse(DialysisDTO.AdverseUpsert dto);

    /**
     * 取消治疗单：原因必填并释放占用机位。
     * 用于治疗单行「取消」操作。
     */
    DialysisVO.SessionVO cancelSession(DialysisDTO.SessionCancel dto);

    /**
     * 工作量统计：治疗例次/超滤量/不良反应分布/机位负荷。
     * 用于透析中心首页工作量统计区取数。
     */
    DialysisVO.StatsVO stats(DialysisDTO.StatsQuery dto);
}
