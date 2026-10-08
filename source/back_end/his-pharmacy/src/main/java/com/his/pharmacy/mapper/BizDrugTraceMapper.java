package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.pharmacy.entity.BizDrugTrace;
import com.his.pharmacy.vo.DrugDictSnapshotVO;
import com.his.pharmacy.vo.DrugDispensingSnapshotVO;
import com.his.pharmacy.vo.DrugInboundSnapshotVO;
import com.his.pharmacy.vo.DrugStockOptionVO;
import com.his.pharmacy.vo.DrugTraceReconcileVO;
import com.his.pharmacy.vo.DrugTraceVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 药品追溯码台账Mapper
 *
 * <p>两条跨模块口径（都是<b>裸 SQL 只读快照</b>，his-pharmacy 不依赖 his-emr / his-system 的实体）：
 * <ul>
 *   <li>药品字典在 his-system，按追溯标识反查药品只取快照列，不建外键。</li>
 *   <li>药品发药记录在 his-emr，核销时按 id 取一次快照写进台账。</li>
 * </ul>
 * 追溯码原文的唯一键不含删除标记，删除必须走 {@link #purgeById} 物理删
 * （误采的码软删会占着唯一键，下次再扫这个码就插不进了）。
 */
@Mapper
public interface BizDrugTraceMapper extends BaseMapper<BizDrugTrace> {

    /** 台账列表公共列（新增列只改这一处） */
    String COLUMNS = "t.id, t.trace_no, t.trace_code, t.code_type, t.drug_di, t.serial_no, t.code_batch_no, "
            + "t.code_expiry_date, t.drug_id, t.drug_code, t.drug_name, t.generic_name, t.specification, "
            + "t.dosage_form, t.unit, t.manufacturer, t.approval_number, t.stock_id, t.stock_batch_no, "
            + "t.supplier, t.supplier_id, t.source_type, t.inbound_id, t.inbound_no, t.status, t.scan_time, "
            + "t.operator_name, t.dispensing_id, t.dispensing_no, t.patient_id, t.patient_no, t.patient_name, "
            + "t.visit_type, t.regist_id, t.admission_id, t.dept_id, t.dept_name, t.dispense_time, "
            + "t.dispense_operator, t.upload_status, t.upload_batch_no, t.upload_time, t.upload_fail_reason, "
            + "t.void_type, t.void_time, t.void_reason, t.remark, t.create_time ";

    @Select("<script>" +
            "SELECT " + COLUMNS +
            "FROM biz_drug_trace t " +
            "WHERE t.del_flag = 0 " +
            "<if test='keyword != null and keyword != \"\"'> AND (t.trace_code LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR t.drug_name LIKE CONCAT('%', #{keyword}, '%') OR t.drug_code LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR t.stock_batch_no LIKE CONCAT('%', #{keyword}, '%') OR t.patient_name LIKE CONCAT('%', #{keyword}, '%')) </if> " +
            "<if test='drugId != null'> AND t.drug_id = #{drugId} </if> " +
            "<if test='stockId != null'> AND t.stock_id = #{stockId} </if> " +
            "<if test='status != null'> AND t.status = #{status} </if> " +
            "<if test='uploadStatus != null'> AND t.upload_status = #{uploadStatus} </if> " +
            "<if test='codeType != null'> AND t.code_type = #{codeType} </if> " +
            "<if test='sourceType != null'> AND t.source_type = #{sourceType} </if> " +
            "<if test='patientId != null'> AND t.patient_id = #{patientId} </if> " +
            "<if test='dispensingId != null'> AND t.dispensing_id = #{dispensingId} </if> " +
            "ORDER BY t.id DESC" +
            "</script>")
    Page<DrugTraceVO> selectTracePage(Page<DrugTraceVO> page,
                                      @Param("keyword") String keyword,
                                      @Param("drugId") Long drugId,
                                      @Param("stockId") Long stockId,
                                      @Param("status") Integer status,
                                      @Param("uploadStatus") Integer uploadStatus,
                                      @Param("codeType") Integer codeType,
                                      @Param("sourceType") Integer sourceType,
                                      @Param("patientId") Long patientId,
                                      @Param("dispensingId") Long dispensingId);

    /** 按码原文查（同一码只可能有一条：追溯码原文唯一键） */
    @Select("SELECT " + COLUMNS + "FROM biz_drug_trace t WHERE t.del_flag = 0 AND t.trace_code = #{code} LIMIT 1")
    DrugTraceVO selectByTraceCode(@Param("code") String code);

    @Select("SELECT " + COLUMNS + "FROM biz_drug_trace t WHERE t.del_flag = 0 AND t.id = #{id} LIMIT 1")
    DrugTraceVO selectTraceById(@Param("id") Long id);

    /**
     * 按追溯标识反查药品字典（跨模块裸 SQL，his-system 的药品字典）
     * <p>GS1 的 GTIN-14 命中 trace_di，20 位码的本体码命中 trace_code_prefix，两个键一起试。
     */
    @Select("SELECT d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.unit, "
            + "d.manufacturer, d.approval_number, d.is_trace_required, d.status "
            + "FROM sys_drug d "
            + "WHERE d.del_flag = 0 AND (d.trace_di = #{key} OR d.trace_code_prefix = #{key}) "
            + "ORDER BY d.id LIMIT 1")
    DrugDictSnapshotVO selectDrugByTraceKey(@Param("key") String key);

    /** 药品字典快照（跨模块裸 SQL，his-system 的药品字典） */
    @Select("SELECT d.id, d.drug_code, d.drug_name, d.generic_name, d.specification, d.dosage_form, d.unit, "
            + "d.manufacturer, d.approval_number, d.is_trace_required, d.status "
            + "FROM sys_drug d WHERE d.del_flag = 0 AND d.id = #{id} LIMIT 1")
    DrugDictSnapshotVO selectDrugSnapshot(@Param("id") Long id);

    /** 发药单快照（跨模块裸 SQL，his-emr 的药品发药记录） */
    @Select("SELECT dp.id, dp.dispensing_no, dp.prescription_id, dp.prescription_no, dp.patient_id, dp.patient_no, "
            + "dp.patient_name, dp.drug_id, dp.drug_code, dp.drug_name, dp.specification, dp.unit, dp.quantity, "
            + "dp.dispensing_status, dp.dispensing_time "
            + "FROM biz_drug_dispensing dp WHERE dp.del_flag = 0 AND dp.id = #{id} LIMIT 1")
    DrugDispensingSnapshotVO selectDispensingSnapshot(@Param("id") Long id);

    /** 入库单快照（本模块的药品入库单） */
    @Select("SELECT ib.id, ib.inbound_no, ib.supplier FROM biz_drug_inbound ib "
            + "WHERE ib.del_flag = 0 AND ib.id = #{id} LIMIT 1")
    DrugInboundSnapshotVO selectInboundSnapshot(@Param("id") Long id);

    /** 该药品可挂靠批次（采集时选批次，FEFO 序；药库 1 在前、药房 2 在后） */
    @Select("SELECT s.id, s.drug_id, s.batch_no, s.expiry_date, s.quantity, s.available_quantity, "
            + "s.stock_room, s.location, s.supplier, s.supplier_id "
            + "FROM biz_drug_stock s WHERE s.del_flag = 0 AND s.drug_id = #{drugId} "
            + "ORDER BY s.stock_room ASC, s.expiry_date ASC, s.id ASC")
    List<DrugStockOptionVO> selectStockOptions(@Param("drugId") Long drugId);

    /** 待上传 / 上传失败的码（批量上传用） */
    @Select("SELECT t.id FROM biz_drug_trace t WHERE t.del_flag = 0 AND t.upload_status IN (0, 2) "
            + "ORDER BY t.id ASC LIMIT #{limit}")
    List<Long> selectUploadCandidates(@Param("limit") int limit);

    /**
     * 码状态分布
     *
     * <p>别名一律写成 VO 字段名的驼峰（{@code map-underscore-to-camel-case} 只对 Bean 生效，
     * 但写齐别名才能让「列名↔字段名」的对应关系在 SQL 里一眼可查）。
     * {@code SUM} 用 {@code COALESCE(..., 0)} 兜底：零行时 MySQL 的 SUM 返回 NULL，
     * 空台账必须显示「0 条」而不是「null 条」。
     */
    @Select("SELECT COUNT(*) AS total, "
            + "COALESCE(SUM(CASE WHEN t.status = 1 THEN 1 ELSE 0 END), 0) AS inStock, "
            + "COALESCE(SUM(CASE WHEN t.status = 2 THEN 1 ELSE 0 END), 0) AS dispensed, "
            + "COALESCE(SUM(CASE WHEN t.status = 3 THEN 1 ELSE 0 END), 0) AS voided, "
            + "COALESCE(SUM(CASE WHEN t.upload_status = 0 THEN 1 ELSE 0 END), 0) AS pendingUpload, "
            + "COALESCE(SUM(CASE WHEN t.upload_status = 1 THEN 1 ELSE 0 END), 0) AS uploaded, "
            + "COALESCE(SUM(CASE WHEN t.upload_status = 2 THEN 1 ELSE 0 END), 0) AS uploadFailed, "
            + "COUNT(DISTINCT t.drug_id) AS drugKinds "
            + "FROM biz_drug_trace t WHERE t.del_flag = 0")
    DrugTraceReconcileVO selectTraceStats();

    /**
     * 近 30 天已发药但未核销追溯码的发药行数（医保稽核点："发药没扫码"）
     * <p>只算近 30 天：政策前的历史发药本来就没有码，算进来是个永远降不下去的假指标。
     */
    @Select("SELECT COUNT(*) FROM biz_drug_dispensing dp "
            + "WHERE dp.del_flag = 0 AND dp.dispensing_status = 2 "
            + "AND COALESCE(dp.dispensing_time, dp.create_time) >= DATE_SUB(NOW(), INTERVAL 30 DAY) "
            + "AND NOT EXISTS (SELECT 1 FROM biz_drug_trace t WHERE t.del_flag = 0 AND t.dispensing_id = dp.id)")
    long countUnTracedDispense();

    /** 其中「必须采集」品种（麻精/集采/医保谈判）的行数 */
    @Select("SELECT COUNT(*) FROM biz_drug_dispensing dp "
            + "JOIN sys_drug d ON d.id = dp.drug_id AND d.del_flag = 0 AND d.is_trace_required = 1 "
            + "WHERE dp.del_flag = 0 AND dp.dispensing_status = 2 "
            + "AND COALESCE(dp.dispensing_time, dp.create_time) >= DATE_SUB(NOW(), INTERVAL 30 DAY) "
            + "AND NOT EXISTS (SELECT 1 FROM biz_drug_trace t WHERE t.del_flag = 0 AND t.dispensing_id = dp.id)")
    long countRequiredUnTracedDispense();

    /** 近 30 天已发药总行数（覆盖率分母） */
    @Select("SELECT COUNT(*) FROM biz_drug_dispensing dp "
            + "WHERE dp.del_flag = 0 AND dp.dispensing_status = 2 "
            + "AND COALESCE(dp.dispensing_time, dp.create_time) >= DATE_SUB(NOW(), INTERVAL 30 DAY)")
    long countRecentDispense();

    /**
     * 物理删除（追溯码原文的唯一键不含删除标记，软删会占键）
     * <p>只允许删「在库且未上传」的误采记录 —— 已核销/已上传的码是医保数据，删不得。
     */
    @Delete("DELETE FROM biz_drug_trace WHERE id = #{id} AND status = 1 AND upload_status = 0")
    int purgeById(@Param("id") Long id);
}
