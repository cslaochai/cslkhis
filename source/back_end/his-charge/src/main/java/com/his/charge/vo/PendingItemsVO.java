package com.his.charge.vo;

import com.his.emr.vo.BizInspectionApplyVO;
import com.his.emr.vo.BizLaboratoryApplyVO;
import com.his.emr.vo.BizPrescriptionVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 患者待缴费项目出参
 */
@Data
public class PendingItemsVO {

    /**
     * 未缴费处方列表
     */
    private List<BizPrescriptionVO> prescriptions;

    /**
     * 未缴费检查申请列表
     */
    private List<BizInspectionApplyVO> inspections;

    /**
     * 未缴费检验申请列表
     */
    private List<BizLaboratoryApplyVO> laboratories;

    /**
     * 待缴费总金额，单位：元
     */
    private BigDecimal totalAmount;

}
