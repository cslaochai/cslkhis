package com.his.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.AdmissionOrderCancelDTO;
import com.his.patient.dto.AdmissionOrderQueryPageDTO;
import com.his.patient.dto.AdmissionOrderUpsertDTO;
import com.his.patient.entity.BizAdmissionOrder;
import com.his.patient.vo.AdmissionOrderVO;

import java.time.LocalDateTime;

/**
 * 住院证（入院通知单）服务 —— 门诊转住院的凭据
 */
public interface AdmissionOrderService {

    /**
     * 开住院证（门诊医生站），返回住院证ID
     */
    Long create(AdmissionOrderUpsertDTO dto);

    /**
     * 住院证分页（住院处待收治看板 / 按患者反查）
     */
    IPage<AdmissionOrderVO> listPage(AdmissionOrderQueryPageDTO query);

    /**
     * 住院证详情
     */
    AdmissionOrderVO detail(Long id);

    /**
     * 作废住院证（仅「待收治」可作废）
     */
    void cancel(AdmissionOrderCancelDTO dto);

    /**
     * 待收治且未过期的证数量
     */
    long countPending();

    // 供住院收治流程调用

    /**
     * 取出可收治的住院证，并做完所有前置校验。
     * <p>校验不通过直接抛 {@code BusinessException}，调用方不必自己判。
     *
     * @throws com.his.common.exception.BusinessException 证不存在 / 已被收治 / 已作废 / 已过期
     */
    BizAdmissionOrder requireAdmittable(Long orderId);

    /**
     * 收治成功后回填：状态置「已收治」+ 回写入院ID / 实际科室 / 收治时间
     */
    void markAdmitted(BizAdmissionOrder order, Long admissionId, Long admitDeptId, LocalDateTime admitTime);
}
