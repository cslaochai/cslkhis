package com.his.medicaltech.service;


import com.his.common.base.PageResult;
import com.his.medicaltech.dto.EndoscopyDTO;
import com.his.medicaltech.entity.BizEndoscopyRecord;
import com.his.medicaltech.vo.EndoscopyVO;

/**
 * 内镜工作站服务：内镜检查登记、执行、活检送检与报告审核发布的业务收口。
 */
public interface EndoscopyService extends com.baomidou.mybatisplus.extension.service.IService<BizEndoscopyRecord> {

    /**
     * 内镜检查记录分页：按检查号、患者、内镜类型与状态筛选。
     * 用于内镜工作站查询卡与结果表格。
     */
    PageResult<EndoscopyVO.ListVO> pageVO(EndoscopyDTO.Query q);

    /**
     * 内镜检查按状态与当日量的统计。
     * 用于内镜工作站顶部的统计卡。
     */
    EndoscopyVO.StatsVO stats();

    /**
     * 单条内镜检查详情：登记信息、镜下所见、活检与报告签名一次取全。
     * 用于详情弹框按记录取数，也用于各操作成功后的弹框刷新。
     */
    EndoscopyVO.DetailVO getDetail(Long recordId);

    /**
     * 登记或修改内镜检查：无编号时新建，有编号时改登记段。
     * 用于内镜工作站的登记与修改弹框。
     */
    EndoscopyVO.DetailVO upsertRecord(EndoscopyDTO.RecordUpsert dto);

    /**
     * 患者到检签到，把登记态推进到已签到。
     * 用于内镜室叫号后对排队患者的签到动作。
     */
    void checkIn(Long recordId);

    /**
     * 记录实际执行结果：内镜医师、镜下所见与活检情况。
     * 用于内镜执行弹框保存本次检查过程。
     */
    void execute(EndoscopyDTO.Execute dto);

    /**
     * 活检标本送检病理，生成病理单并回填病理号；同一部位重复送检拒绝。
     * 用于执行完成后向病理科发起送检，返回值即病理号。
     */
    String sendBiopsy(EndoscopyDTO.BiopsySend dto);

    /**
     * 出具内镜报告：镜下所见与诊断意见落定并记报告医师。
     * 用于内镜报告弹框的保存与出报告。
     */
    void report(EndoscopyDTO.Report dto);

    /**
     * 审核内镜报告，审核人不得是报告人本人。
     * 用于内镜工作站待审核报告的审核环节。
     */
    void audit(EndoscopyDTO.Audit dto);

    /**
     * 发布已审核的内镜报告，发布后申请方可读取结果。
     * 用于内镜工作站对审核通过报告的放行动作。
     */
    void publish(Long recordId);

    /**
     * 取消内镜检查并记录取消原因。
     * 用于患者弃检或检查改期时在列表行发起作废。
     */
    void cancel(EndoscopyDTO.Cancel dto);
}
