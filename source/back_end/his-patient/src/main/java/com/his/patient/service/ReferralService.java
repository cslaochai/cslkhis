package com.his.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.ReferralDTO;
import com.his.patient.vo.ReferralVO;

/**
 * 双向转诊服务：转诊单登记、审核接收、完成/取消闭环与超时催办的业务收口。
 */
public interface ReferralService {

    /**
     * 登记一张双向转诊单：写明转出/接收机构与科室、患者及转诊原因。
     * 用于「双向转诊」页的新增转诊表单。
     */
    ReferralVO create(ReferralDTO.Create dto);

    /**
     * 转诊单分页查询。
     * 用于「双向转诊」页查询卡与列表表格。
     */
    IPage<ReferralVO> listPage(ReferralDTO.PageQuery q);

    /**
     * 单张转诊单详情：含状态流转记录与各环节备注。
     * 供转诊审核、完成环节回看原单时取数。
     */
    ReferralVO getDetailById(Long referralId);

    /**
     * 确认接收转诊（待确认→已确认）：可指定接收科室并记审核意见。
     * 用于「双向转诊」页的确认接收弹框。
     */
    ReferralVO audit(ReferralDTO.Audit dto);

    /**
     * 完成转诊（已确认→已完成）。
     * 用于「双向转诊」页列表操作列的完成动作。
     */
    ReferralVO finish(ReferralDTO.Finish dto);

    /**
     * 取消转诊（非终态→已取消）：取消原因必填。
     * 用于「双向转诊」页列表操作列的取消动作。
     */
    ReferralVO cancel(ReferralDTO.Cancel dto);

    /**
     * 超时催办：把待确认超时的转诊单批量生成当日总值班待办，返回本轮发出的条数。
     * 由定时任务每 30 分钟扫描一轮，转诊岗也可手工补跑。
     *
     * @return 本轮发出的待办条数
     */
    int escalatePendingToDuty();
}
