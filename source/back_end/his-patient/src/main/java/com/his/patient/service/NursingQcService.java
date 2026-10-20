package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.NursingQcDTO;
import com.his.patient.vo.NurseQcVO;

import java.util.List;

/**
 * 护理质控服务（sql/168）：护理部视角的质量账。
 */
public interface NursingQcService {

    /**
     * 病区下拉：只含当前岗位可见科室的启用病区，附床位数。
     * 用于「护理质控」页查询卡与各弹框的病区筛选。
     */
    List<NurseQcVO.Ward> wardSelectList(String keyword);

    /**
     * 检查人下拉：指定病区所属科室的在职人员。
     * 用于检查单评分表单选择检查人。
     */
    List<NurseQcVO.Inspector> inspectorSelectList(Long wardId, String keyword);

    /**
     * 检查项标准目录：按类别取启用检查项，category 空表示全部。
     * 用于检查单评分面板的逐项行来源。
     */
    List<NurseQcVO.ItemDef> itemSelectList(Integer category);

    /**
     * 检查单分页：按病区 × 月 × 类别（及状态）检索。
     * 用于「护理质控」页「检查表（评分）」页签的表格。
     */
    PageResult<NurseQcVO.CheckRow> checkListPage(NursingQcDTO.CheckPageQueryDTO query);

    /**
     * 检查单详情：主表 + 明细 + 标准目录，并给出本轮漏查项数。
     * 用于检查单详情弹框核对逐项扣分。
     */
    NurseQcVO.CheckDetail getDetailById(Long id);

    /**
     * 保存检查单：主表六个汇总数字由明细求和得出，不接受前端传值；已确认的单拒绝再改。
     * 用于「检查表（评分）」页签的新增/修改评分表单提交。
     */
    NurseQcVO.SaveResult checkUpsert(NursingQcDTO.CheckUpsertDTO dto);

    /**
     * 检查单确认/退回：2-确认即冻结明细，1-退回草稿后才能再改。
     * 用于检查单列表操作列的状态流转。
     */
    NurseQcVO.SaveResult checkStatus(NursingQcDTO.CheckStatus dto);

    /**
     * 删除检查单：连同明细物理删（唯一键不含删除标志，软删会挡住同号重建）。
     * 用于检查单列表操作列的删除动作。
     */
    void checkDeleteById(Long id);

    /**
     * 月度指标台账分页：每行带分子分母与来源备注。
     * 用于「护理质控」页「指标台账」页签的表格。
     */
    PageResult<NurseQcVO.LedgerRow> ledgerListPage(NursingQcDTO.LedgerPageQueryDTO query);

    /**
     * 当月四条 KPI 指标：永远各返回一项，没有台账的标注未重算。
     * 用于「质量看板」页签的月度指标卡片。
     */
    List<NurseQcVO.Kpi> monthMetrics(NursingQcDTO.MonthQueryDTO query);

    /**
     * 单条指标的逐月趋势点：wardId 空为可见范围内全院合并。
     * 用于「质量看板」页签的趋势图。
     */
    List<NurseQcVO.Kpi> trend(NursingQcDTO.TrendQueryDTO query);

    /**
     * 病区横向对比：某月某指标各病区落点，按指标值倒序。
     * 用于「质量看板」页签的病区对比榜。
     */
    List<NurseQcVO.LedgerRow> wardCompare(NursingQcDTO.CompareQueryDTO query);

    /**
     * 重算月度台账：已上报的行由 SQL 侧闸门跳过，不静默改写历史数字。
     * 用于「指标台账」页签发起某月某指标的重算。
     */
    NurseQcVO.RecalcResult recalc(NursingQcDTO.RecalcCommandDTO command);

    /**
     * 台账上报/退回：2-上报即锁定该月台账，1-退回后重算才会覆盖。
     * 用于「指标台账」页签操作列，上报结果供护理部月度通报取数。
     */
    NurseQcVO.ReportResult report(NursingQcDTO.ReportCommandDTO command);

    /**
     * 删除台账行：物理删，删掉后重新重算即可补回。
     * 用于「指标台账」页签操作列清理错算行。
     */
    void ledgerDeleteById(Long id);
}
