package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.CriticalValueHandleDTO;
import com.his.medicaltech.dto.CriticalValueQueryPageDTO;
import com.his.medicaltech.dto.CriticalValueReceiveDTO;
import com.his.medicaltech.entity.BizLabResult;
import com.his.medicaltech.entity.BizLaboratoryRecord;
import com.his.medicaltech.vo.BizCriticalValueVO;
import com.his.medicaltech.vo.CriticalValueStatsVO;

import java.util.List;

/**
 * 检验危急值闭环服务
 */
public interface CriticalValueService {

    /**
     * 根据本次录入的检验结果识别危急值并上报（写库 + 站内信通知开单医生）。
     * <p>
     * 由「录入检验结果」流程调用。判定完全走硬规则，<b>不调用模型</b>。
     *
     * @return 本次识别出的危急值条数
     */
    int detectAndReport(BizLaboratoryRecord record, List<BizLabResult> results);

    /**
     * 分页查询危急值
     */
    PageResult<BizCriticalValueVO> listPage(CriticalValueQueryPageDTO dto);

    /**
     * 按ID获取危急值详情
     */
    BizCriticalValueVO getById(Long criticalValueId);

    /**
     * 确认接收
     */
    boolean receive(CriticalValueReceiveDTO dto);

    /**
     * 记录处置措施
     */
    boolean handle(CriticalValueHandleDTO dto);

    /**
     * 统计（列表页顶部卡片）
     */
    CriticalValueStatsVO stats();

    /**
     * 超时升级：扫描「已超时仍未处置」的危急值，二次通知原收件人并上报同科室上级，
     * 每条只升级一次（escalate_status 落库防重复）。
     * 定时任务与手工补跑端点共用本方法；可重入，重跑影响 0 条。
     *
     * @return 本次完成升级的条数
     */
    int escalateOverdue();

    /**
     * 删除（逻辑删除）
     */
    boolean deleteById(Long criticalValueId);
}
