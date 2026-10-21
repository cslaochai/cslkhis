package com.his.clinical.service;

import com.his.common.base.PageResult;
import com.his.clinical.dto.TreatmentDTO;
import com.his.clinical.vo.TreatmentVO;

import java.util.List;

/**
 * 门诊治疗站服务：治疗疗程开单、按次执行打卡与按次计费的执行台账的业务收口。
 */
public interface TreatmentService {

    /**
     * 治疗申请（疗程）分页：按患者、就诊、疗程状态、治疗项目与开单日期区间筛选，开单科室数据范围随登录岗位收口。
     * 用于「门诊治疗站」页「治疗开单」页签的查询卡与疗程列表。
     */
    PageResult<TreatmentVO.ApplyVO> listPageApplies(TreatmentDTO.ApplyQuery q);

    /**
     * 疗程详情：疗程头信息连名下全部按次执行流水一起给出，按次序升序。
     * 用于「治疗开单」页签疗程行点开的详情弹框，开单与重排保存后也回读这一份。
     */
    TreatmentVO.ApplyDetailVO getDetail(Long applyId);

    /**
     * 按次执行流水分页：按计划日期、执行状态、计费状态与患者筛选，流水经名下疗程收口到开单科室。
     * 用于「门诊治疗站」页「今日治疗台」与「执行与计费台账」两个页签共用的流水表格。
     */
    PageResult<TreatmentVO.ExecVO> listPageExecs(TreatmentDTO.ExecQuery q);

    /**
     * 流水状态分布计数：执行状态与计费状态逐档给出条数，与流水分页同一套筛选条件但不带上被统计的那一维（否则选中「已执行」时「待执行」恒显示 0）。
     * 用于「今日治疗台」与「执行与计费台账」流水表上方的状态筛选片。
     */
    List<TreatmentVO.StatusCountVO> statusCount(TreatmentDTO.ExecQuery q);

    /**
     * 治疗台看板汇总：在办疗程数、今日应做/已做/待做、逾期未打卡次数、已执行未计费与计费失败次数、今日治疗费合计。
     * 用于「门诊治疗站」页「今日治疗台」页签顶部统计卡。
     */
    TreatmentVO.StatsVO stats();

    /**
     * 治疗项目下拉：按关键字取启用治疗项目，带单次价格与可解析出的执行科室，限制返回条数。
     * 用于治疗开单弹框选项目，临床路径模板配置治疗节点时同源取数。
     */
    List<TreatmentVO.ItemSelectListVO> itemSelectList(String keyword, Integer limit);

    /**
     * 新开疗程或整体重排疗程：按总次数与间隔天数一次落完整排期，只有名下一次卡都没打过的疗程才允许重排。
     * 用于「治疗开单」页签的新开单与重排弹框保存。
     */
    TreatmentVO.ApplyDetailVO upsertApply(TreatmentDTO.ApplyUpsertDTO dto);

    /**
     * 单次改期：只挪未执行的那一次排期到指定日期，已执行的历史日期不动。
     * 用于「今日治疗台」流水行的改期弹框，患者临时有事顺延后续治疗时提交。
     */
    TreatmentVO.ExecVO rescheduleExec(TreatmentDTO.ExecReschedule dto);

    /**
     * 按次打卡：确认某一次治疗做完并按次序校验，随后按次记账；计费走独立事务，失败只在流水上留痕、不回滚打卡。
     * 用于治疗台或台账流水行的执行弹框确认时提交。
     */
    TreatmentVO.ExecVO executeExec(TreatmentDTO.ExecExecute dto);

    /**
     * 计费补记：给已打卡却没记上账的那一次重试一次记账，已记账的再补会被拒绝，不出双份账。
     * 用于「今日治疗台」与「执行与计费台账」流水行的补记操作。
     */
    TreatmentVO.ExecVO retryCharge(Long recordId);

    /**
     * 取消疗程：疗程转已取消并连带取消未执行的排期次，已执行且已计费的账不在这里退（退费走收费窗口）。
     * 用于「治疗开单」页签疗程行的取消操作，患者停止后续治疗时登记原因后提交。
     *
     * @return 连带取消的未执行次数
     */
    int cancelApply(TreatmentDTO.ApplyCancel dto);

    /**
     * 删除疗程：连名下排期行一起删掉，只允许一次都没执行过的疗程。
     * 用于「治疗开单」页签疗程行的删除操作，误开且未执行时收回。
     */
    void deleteApply(Long applyId);
}
