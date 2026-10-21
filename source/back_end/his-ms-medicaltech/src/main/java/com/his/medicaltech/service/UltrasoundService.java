package com.his.medicaltech.service;


import com.his.common.base.PageResult;
import com.his.medicaltech.dto.UltrasoundDTO;
import com.his.medicaltech.entity.BizUltrasoundRecord;
import com.his.medicaltech.vo.UltrasoundVO;

import java.util.List;

/**
 * 超声工作站服务：超声检查登记、执行、结构化测量与报告审核发布的业务收口。
 */
public interface UltrasoundService extends com.baomidou.mybatisplus.extension.service.IService<BizUltrasoundRecord> {

    /**
     * 超声检查记录分页：按检查号、患者、超声类型与状态筛选。
     * 用于超声工作站查询卡与结果表格。
     */
    PageResult<UltrasoundVO.ListVO> pageVO(UltrasoundDTO.Query q);

    /**
     * 超声检查按状态与当日量的统计。
     * 用于超声工作站顶部的统计卡。
     */
    UltrasoundVO.StatsVO stats();

    /**
     * 单条超声检查详情，含结构化测量值。
     * 用于详情弹框按记录取数，也用于各操作成功后的弹框刷新。
     */
    UltrasoundVO.DetailVO getDetail(Long recordId);

    /**
     * 某条检查的结构化测量值清单（测量项、值、单位与参考范围）。
     * 用于超声工作站的测量值查看弹框。
     */
    List<UltrasoundVO.MeasureVO> listMeasures(Long recordId);

    /**
     * 登记或修改超声检查：无编号时新建，有编号时改登记段。
     * 用于超声工作站的登记与修改弹框。
     */
    UltrasoundVO.DetailVO upsertRecord(UltrasoundDTO.RecordUpsert dto);

    /**
     * 患者到检签到，把登记态推进到已签到。
     * 用于超声分诊叫号后对排队患者的签到动作。
     */
    void checkIn(Long recordId);

    /**
     * 记录实际执行结果：超声医师与检查过程信息。
     * 用于超声执行弹框保存本次上机内容。
     */
    void execute(UltrasoundDTO.Execute dto);

    /**
     * 整单覆盖保存结构化测量值，异常标志由服务端按参考范围判定，返回落库条数。
     * 用于测量值弹框一次提交全部测量项。
     */
    int saveMeasures(UltrasoundDTO.MeasureSave dto);

    /**
     * 出具超声报告：超声所见与提示意见落定并记报告医师。
     * 用于超声报告弹框的保存与出报告。
     */
    void report(UltrasoundDTO.Report dto);

    /**
     * 审核超声报告，审核人不得是报告人本人。
     * 用于超声工作站待审核报告的审核环节。
     */
    void audit(UltrasoundDTO.Audit dto);

    /**
     * 发布已审核的超声报告，发布后申请方可读取结果。
     * 用于超声工作站对审核通过报告的放行动作。
     */
    void publish(Long recordId);

    /**
     * 取消超声检查并记录取消原因。
     * 用于患者弃检或检查改期时在列表行发起作废。
     */
    void cancel(UltrasoundDTO.Cancel dto);
}
