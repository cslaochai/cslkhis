package com.his.charge.settle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.charge.settle.entity.BizMedpaySettle;
import com.his.charge.settle.vo.MedpaySettleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 按病种付费结算结果 Mapper（P4）。
 * <p>跨模块只读走裸 SQL：结算账单在 charge 域，病案首页在病案域，这里 join 一张「账单+首页」事实行。
 */
@Mapper
public interface MedpaySettleMapper extends BaseMapper<BizMedpaySettle> {

    /**
     * 出院结算账单 + 病案首页组合行（结算账单 encounter_id = 住院 admission_id，取首页全维度供分组）。
     */
    String SETTLE_BILL_SELECT = """
            SELECT b.id AS settleBillId, b.bill_no AS billNo, b.encounter_type AS encounterType,
                   b.bill_type AS billType, b.patient_name AS patientName, b.total_amount AS totalAmount,
                   s.id AS summaryId, s.admission_id AS admissionId, s.gender AS gender, s.age AS age,
                   s.age_unit AS ageUnit, s.birth_weight AS birthWeight,
                   s.main_diagnosis_code AS mainDiagnosisCode, s.main_diagnosis_name AS mainDiagnosisName,
                   s.is_surgery AS isSurgery
              FROM biz_settlement_bill b
              LEFT JOIN biz_inpatient_summary s ON s.admission_id = b.encounter_id AND s.del_flag = 0
             WHERE b.del_flag = 0 AND b.id = #{billId}
            """;

    @Select(SETTLE_BILL_SELECT + " LIMIT 1")
    MedpaySettleVO.BillRow selectSettleBillRow(@Param("billId") Long billId);

    /**
     * 最近可结算的出院结算账单（批量计算默认集；排除作废/退费单）。
     */
    @Select("""
            SELECT id FROM biz_settlement_bill
             WHERE del_flag = 0 AND encounter_type = 2 AND bill_type = 4
               AND bill_status <> 4 AND bill_status <> 5
             ORDER BY id DESC LIMIT #{limit}
            """)
    List<Long> selectRecentSettleBillIds(@Param("limit") int limit);
}
