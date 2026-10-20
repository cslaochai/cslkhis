package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.InfectiousReportDTO;
import com.his.emr.dto.InfectiousReportPageQueryDTO;
import com.his.emr.vo.InfectiousReportVO;

import java.util.List;

/**
 * 传染病报告卡服务：填卡→审核→退报重报→直报（报文预览）+ 时限催报。
 */
public interface InfectiousReportService {

    /**
     * 报卡分页：按状态、类别、逾期标志与填报时间区间筛选，填报时间倒序，科室数据范围随登录岗位收口。
     * 用于「传染病报告卡」页的查询卡与报卡列表。
     */
    PageResult<InfectiousReportVO.Row> page(InfectiousReportPageQueryDTO q);

    /**
     * 单张报卡详情：连直报报文预览一次给出，已直报的卡据此复核法定留痕内容。
     * 用于报卡详情弹框取数，也用于填卡保存后回读最新行。
     */
    InfectiousReportVO.Detail getDetailById(Long id);

    /**
     * 法定传染病病种下拉：只出启用目录项，按甲乙丙类别与编码排序，单次最多 50 条。
     * 用于填卡弹框选病种，选中项带报卡时限与 ICD 编码供表单回显。
     */
    List<InfectiousReportVO.DiseaseSelectListVO> selectList(String keyword);

    /**
     * 报卡统计：待审核/已审核/已直报/已退报四态计数，加超时未报、今日新增与甲类在办，科室数据范围随登录岗位收口。
     * 用于「传染病报告卡」页顶部统计卡。
     */
    InfectiousReportVO.Stats stats();

    /**
     * 填卡与修改：新建落待审核态并按病种时限算出报卡截止日期，退报后重报走同一入口、报卡次数由服务端递增。
     * 用于「传染病报告卡」页填卡弹框保存；「我的应报未报」与门诊日志的「去报卡」跳转后也在此补卡。
     */
    Long upsert(InfectiousReportDTO.Upsert dto);

    /**
     * 审核报卡：待审核转已审核并记审核人与意见，只有待审核状态可审。
     * 用于「传染病报告卡」页报卡列表行的审核操作。
     */
    void audit(InfectiousReportDTO.Audit dto);

    /**
     * 退报：待审核或已审核的卡退回填报人，退报原因必填，已直报的终态卡不许退。
     * 用于「传染病报告卡」页报卡列表行的退报操作。
     */
    void returnCard(InfectiousReportDTO.ReturnCard dto);

    /**
     * 直报：已审核的卡组装标准直报报文落库并转已直报，返回报文文本。
     * 用于「传染病报告卡」页报卡列表行的直报操作；对接疾控时在本节点外发同一份报文。
     */
    String directReport(Long id);

    /**
     * 超时限催报：扫逾期未审核的卡向填报人发站内信，一张卡每天最多催一条，返回本轮发送条数。
     * 用于「传染病报告卡」页统计区的催报按钮补跑，与每日定时催报共用同一入口。
     */
    int notifyOverdue();
}
