package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.pharmacy.entity.BizNarcoticRegister;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 麻精药品专册 Mapper。
 */
@Mapper
public interface NarcoticRegisterMapper extends BaseMapper<BizNarcoticRegister> {

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

    /**
     * 专册登记号当日已用序号数（生成 NZ+日期+序号用）。
     *
     * <p>必须查库、不能靠进程内自增：序列表有唯一索引，进程内计数器重启归零会导致
     * 当天重号 → 唯一键冲突 → 发药事务整体回滚。
     *
     * <p>刻意不加 {@code del_flag = 0} —— 专册不做逻辑删除，且**已占用的登记号不能被复用**，
     * 所以统计口径是"物理存在的行"，删没删都要算。
     */
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

        public Long getDrugId() {
            return drugId;
        }

        public void setDrugId(Long drugId) {
            this.drugId = drugId;
        }

        public String getDrugCode() {
            return drugCode;
        }

        public void setDrugCode(String drugCode) {
            this.drugCode = drugCode;
        }

        public String getDrugName() {
            return drugName;
        }

        public void setDrugName(String drugName) {
            this.drugName = drugName;
        }

        public String getSpecification() {
            return specification;
        }

        public void setSpecification(String specification) {
            this.specification = specification;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getDosageForm() {
            return dosageForm;
        }

        public void setDosageForm(String dosageForm) {
            this.dosageForm = dosageForm;
        }

        public Integer getSpecialFlag() {
            return specialFlag;
        }

        public void setSpecialFlag(Integer specialFlag) {
            this.specialFlag = specialFlag;
        }
    }

    class BatchRow {
        private String batchNo;
        private java.math.BigDecimal qty;

        public String getBatchNo() {
            return batchNo;
        }

        public void setBatchNo(String batchNo) {
            this.batchNo = batchNo;
        }

        public java.math.BigDecimal getQty() {
            return qty;
        }

        public void setQty(java.math.BigDecimal qty) {
            this.qty = qty;
        }
    }
}
