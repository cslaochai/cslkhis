package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.medicaltech.entity.DrgSimResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * DRG 模拟 Mapper：DRG 分组模拟结果走 MP，病案首页/组表跨模块只读走裸 SQL。
 */
@Mapper
public interface DrgSimMapper extends BaseMapper<DrgSimResult> {

    /**
     * 病案首页 + 实际费用（首页快照优先，没有再看出院结算账单；单条模拟/批量模拟共用）
     *
     * <p>费用用标量子查询而不是 LEFT JOIN 账单表：一次住院除了出院结算还可能有中途结算账单，
     * join 会把一行首页放大成多行，DRG 模拟就凭空多出几个病例。
     */
    String SUMMARY_SELECT = """
            SELECT s.id summary_id, s.patient_name, s.main_diagnosis_code, s.main_diagnosis_name,
                   s.is_surgery, s.inpatient_days, s.death_flag,
                   IFNULL(IFNULL(s.total_amount, (
                       SELECT SUM(b.total_amount) FROM biz_settlement_bill b
                        WHERE b.del_flag = 0 AND b.encounter_type = 2 AND b.bill_type = 4
                          AND b.bill_status <> 4 AND b.encounter_id = s.admission_id)), 0) actual_amount
              FROM biz_inpatient_summary s
             WHERE s.del_flag = 0
            """;

    @Select(SUMMARY_SELECT + " AND s.id = #{summaryId} LIMIT 1")
    Map<String, Object> selectSummary(@Param("summaryId") Long summaryId);

    @Select(SUMMARY_SELECT + " ORDER BY s.id DESC LIMIT #{limit}")
    List<Map<String, Object>> selectSummaries(@Param("limit") int limit);

    /**
     * 可模拟首页列表（带已有模拟结果标记）
     */
    @Select("""
            SELECT s.id summary_id, s.patient_name, s.dept_name, s.discharge_time,
                   s.main_diagnosis_code, s.main_diagnosis_name, s.is_surgery,
                   r.drg_code, r.profit_amount
              FROM biz_inpatient_summary s
              LEFT JOIN biz_drg_sim_result r ON r.summary_id = s.id AND r.del_flag = 0
             WHERE s.del_flag = 0
             ORDER BY s.id DESC
             LIMIT #{limit}
            """)
    List<Map<String, Object>> summaryList(@Param("limit") int limit);

    /**
     * 组表（CHS-DRG 种子）
     */
    @Select("""
            SELECT id, drg_code, drg_name, mdc_code, adrg_code, weight, pay_standard, source, version, status
              FROM sys_drg_group WHERE del_flag = 0 ORDER BY drg_code
            """)
    List<Map<String, Object>> groupList();
}
