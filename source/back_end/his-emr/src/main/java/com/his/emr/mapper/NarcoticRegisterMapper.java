package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizNarcoticRegister;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 麻精药品专册 Mapper。
 *
 * <p><b>为什么这里有几条裸 SQL {@code @Select} 去读别的模块的表</b>：
 * 本模块（his-emr）需要三份"主数据"来做麻精判定与登记 ——
 * 药品的管制分类（药品字典，属 his-system）、
 * 患者实名信息（患者基本信息，属 his-patient）、
 * FEFO 实际扣减批次（药品库存流水，属 his-pharmacy）。
 * 按本工程约定「A 不得直接调 B 的 Mapper」，跨模块读表统一走**本模块自建的裸 SQL Mapper**，
 * 只取需要的列，不引入对方实体。
 *
 * <p>⚠ 裸 SQL 不受 {@code @TableLogic} 约束 → 每一条都必须显式写 {@code del_flag = 0}，
 * 漏写会把已删数据当成有效数据（本仓已踩过：挂号单改名后裸 SQL 写死旧表名，
 * 编译不报错、运行 500）。
 */
@Mapper
public interface NarcoticRegisterMapper extends BaseMapper<BizNarcoticRegister> {

    /**
     * 药品管制分类行（跨模块读药品字典）
     */
    class DrugSpecialRow {
        private Long drugId;
        private String drugCode;
        private String drugName;
        private String specification;
        private String unit;
        private String dosageForm;
        private Integer specialFlag;

        public Long getDrugId() { return drugId; }
        public void setDrugId(Long drugId) { this.drugId = drugId; }
        public String getDrugCode() { return drugCode; }
        public void setDrugCode(String drugCode) { this.drugCode = drugCode; }
        public String getDrugName() { return drugName; }
        public void setDrugName(String drugName) { this.drugName = drugName; }
        public String getSpecification() { return specification; }
        public void setSpecification(String specification) { this.specification = specification; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public String getDosageForm() { return dosageForm; }
        public void setDosageForm(String dosageForm) { this.dosageForm = dosageForm; }
        public Integer getSpecialFlag() { return specialFlag; }
        public void setSpecialFlag(Integer specialFlag) { this.specialFlag = specialFlag; }
    }

    /**
     * 按药品ID批量取管制分类（跨模块读药品字典）
     */
    @Select("<script>" +
            "SELECT id AS drugId, drug_code AS drugCode, drug_name AS drugName, specification, unit," +
            "       dosage_form AS dosageForm, special_flag AS specialFlag " +
            "  FROM sys_drug WHERE del_flag = 0 AND id IN " +
            "<foreach collection='drugIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<DrugSpecialRow> selectDrugSpecial(@Param("drugIds") List<Long> drugIds);

    /**
     * 取某次发药 FEFO 实际扣减的批次（跨模块读药品库存流水）。
     *
     * <p>按 source_type='dispensing' + source_id=发药明细ID 回查，
     * 把批号按扣减顺序去重拼接 —— 跨批次发药时能看出"这批药是从哪几个批号里出的"。
     * <p>{@code change_quantity} 出库为负，这里取绝对值得到每批实际发出量。
     */
    @Select("SELECT batch_no AS batchNo, ABS(change_quantity) AS qty " +
            "  FROM biz_drug_stock_log " +
            " WHERE del_flag = 0 AND source_type = #{sourceType} AND source_id = #{sourceId} " +
            "   AND change_quantity < 0 AND batch_no IS NOT NULL " +
            " ORDER BY id ASC")
    List<BatchRow> selectDeductBatches(@Param("sourceType") String sourceType, @Param("sourceId") Long sourceId);

    class BatchRow {
        private String batchNo;
        private java.math.BigDecimal qty;

        public String getBatchNo() { return batchNo; }
        public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
        public java.math.BigDecimal getQty() { return qty; }
        public void setQty(java.math.BigDecimal qty) { this.qty = qty; }
    }

    /**
     * 患者身份证号（跨模块读患者基本信息）
     */
    @Select("SELECT id_card FROM biz_patient WHERE id = #{patientId} AND del_flag = 0 LIMIT 1")
    String selectPatientIdCard(@Param("patientId") Long patientId);

    /**
     * 复核人姓名（跨模块读员工）—— 只认**在职且持药学岗位**的员工。
     *
     * <p>复核人姓名**由服务端按 ID 反查**，不取前端传值 ——
     * 双人复核是签名性质的动作，姓名可随手传就等于复核记录可以编。
     *
     * <p>⚠ 三个条件都不能少，每一个都对应一个真实的漏判路径：
     * <ol>
     *   <li>列名是 {@code emp_name}，**不是 employee_name** —— 裸 SQL 写错列名编译期不报错，
     *       运行时才 500（本仓挂号单改名那次同类）。</li>
     *   <li>{@code status = 1} 是**启用**（列注释 0-停用 1-启用）。已停用员工签名没有法律效力。</li>
     *   <li>{@code emp_type = 4} 是**药剂师** —— 法条要求复核人"具有药师以上技术职称"
     *       （《医疗用毒性药品管理办法》第9条、《医疗机构麻醉药品、第一类精神药品管理规定》第17条）。
     *       不加这条，护士、收费员都能当麻精复核人，双人复核就只剩"两个人"的形式。
     *       ⚠ 岗位判定走 {@code emp_type} 而不是名称：本库 title 是脏数据
     *       （实测出现 '302'、'101' 这种数字），拿它判岗位会全表误判。</li>
     * </ol>
     */
    @Select("SELECT emp_name FROM sys_employee " +
            " WHERE id = #{employeeId} AND del_flag = 0 AND status = 1 AND emp_type = 4 LIMIT 1")
    String selectActivePharmacistName(@Param("employeeId") Long employeeId);

    /**
     * 专册登记号当日已用序号数（生成 NZ+日期+序号用）。
     *
     * <p>必须查库、不能靠进程内自增：序列表有唯一索引，进程内计数器重启归零会导致
     * 当天重号 → 唯一键冲突 → 发药事务整体回滚。
     *
     * <p>刻意不加 {@code del_flag = 0} —— 专册不做逻辑删除，且**已占用的登记号不能被复用**，
     * 所以统计口径是"物理存在的行"，删没删都要算。
     */
    @Select("SELECT COUNT(*) FROM biz_narcotic_register WHERE register_no LIKE CONCAT(#{prefix}, '%')")
    long countByRegisterNoPrefix(@Param("prefix") String prefix);

    /**
     * 该登记号是否已被占用（生成时探测，碰到占用就跳到下一个序号）
     */
    @Select("SELECT COUNT(*) FROM biz_narcotic_register WHERE register_no = #{registerNo}")
    long countByRegisterNo(@Param("registerNo") String registerNo);
}
