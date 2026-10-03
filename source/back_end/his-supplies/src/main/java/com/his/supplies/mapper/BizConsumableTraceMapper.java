package com.his.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.supplies.entity.BizConsumableTrace;
import com.his.supplies.vo.BizConsumableTraceVO;
import com.his.supplies.vo.ConsumableTraceDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 高值耗材使用溯源台账Mapper
 */
@Mapper
public interface BizConsumableTraceMapper extends BaseMapper<BizConsumableTrace> {

    String TRACE_COLS =
            "t.id, t.create_time, t.trace_no, t.udi_code, t.udi_di, t.udi_serial, t.udi_batch, t.udi_expiry_date, " +
            "t.consumable_id, t.consumable_code, t.consumable_name, t.specification, t.unit, t.reg_cert_no, t.retail_price, " +
            "t.stock_id, t.batch_no, t.supplier, t.patient_id, t.patient_no, t.patient_name, " +
            "t.visit_type, t.regist_id, t.admission_id, t.dept_id, t.dept_name, t.usage_time, t.operator_name, " +
            "t.charge_status, t.fee_no, t.fee_record_id, t.charge_fail_reason, " +
            "t.status, t.void_time, t.void_reason, t.remark ";

    /**
     * 溯源台账分页（正/反向追溯同一入口：keyword 覆盖追溯码/UDI/患者/耗材）
     */
    @Select("<script>" +
            "SELECT " + TRACE_COLS +
            "FROM biz_consumable_trace t WHERE t.del_flag = 0 " +
            "<if test='keyword != null and keyword != \"\"'> AND (t.trace_no LIKE CONCAT('%', #{keyword}, '%') " +
            "  OR t.udi_code LIKE CONCAT('%', #{keyword}, '%') OR t.udi_serial LIKE CONCAT('%', #{keyword}, '%') " +
            "  OR t.patient_name LIKE CONCAT('%', #{keyword}, '%') OR t.patient_no LIKE CONCAT('%', #{keyword}, '%') " +
            "  OR t.consumable_name LIKE CONCAT('%', #{keyword}, '%')) </if> " +
            "<if test='consumableId != null'> AND t.consumable_id = #{consumableId} </if> " +
            "<if test='patientId != null'> AND t.patient_id = #{patientId} </if> " +
            "<if test='chargeStatus != null'> AND t.charge_status = #{chargeStatus} </if> " +
            "<if test='status != null'> AND t.status = #{status} </if> " +
            "ORDER BY t.id DESC" +
            "</script>")
    Page<BizConsumableTraceVO> selectTracePage(Page<BizConsumableTraceVO> page,
                                               @Param("keyword") String keyword,
                                               @Param("consumableId") Long consumableId,
                                               @Param("patientId") Long patientId,
                                               @Param("chargeStatus") Integer chargeStatus,
                                               @Param("status") Integer status);

    /**
     * 溯源详情（JOIN 字典补厂家/类别，JOIN 批次补剩余量/位置/入库经办）
     */
    @Select("SELECT " + TRACE_COLS + ", " +
            "c.manufacturer, c.category, " +
            "s.quantity AS batch_quantity, s.location, s.cost_price, " +
            "s.create_time AS stock_in_time, s.create_by AS stock_in_by, " +
            "d.amount AS charge_amount " +
            "FROM biz_consumable_trace t " +
            "LEFT JOIN sys_consumable c ON t.consumable_id = c.id " +
            "LEFT JOIN biz_consumable_stock s ON t.stock_id = s.id " +
            "LEFT JOIN biz_fee_record d ON d.id = t.fee_record_id AND d.del_flag = 0 " +
            "WHERE t.del_flag = 0 AND t.id = #{traceId}")
    ConsumableTraceDetailVO selectTraceDetail(@Param("traceId") Long traceId);

    /**
     * 同一 UDI 是否已有"使用中"记录（防重复扫码登记；作废重扫是允许的）
     */
    @Select("SELECT COUNT(*) FROM biz_consumable_trace " +
            "WHERE del_flag = 0 AND status = 1 AND udi_code = #{udiCode}")
    long countActiveByUdi(@Param("udiCode") String udiCode);

    /**
     * 高值字典（含停用判断的原始行，扫码匹配用）
     */
    @Select("SELECT id, consumable_code, consumable_name, specification, unit, manufacturer, " +
            "       retail_price, is_high_value, udi_di, reg_cert_no, status " +
            "FROM sys_consumable WHERE del_flag = 0 AND udi_di = #{udiDi} LIMIT 1")
    com.his.supplies.entity.SysConsumable selectByUdiDi(@Param("udiDi") String udiDi);

    /**
     * 患者快照兜底（直查患者基本信息不引 his-patient 依赖，同科室口径）
     */
    @Select("SELECT patient_no, patient_name FROM biz_patient WHERE id = #{patientId} AND del_flag = 0")
    java.util.Map<String, Object> selectPatientSnapshot(@Param("patientId") Long patientId);
}
