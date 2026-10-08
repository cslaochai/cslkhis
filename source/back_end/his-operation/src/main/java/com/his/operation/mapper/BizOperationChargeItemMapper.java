package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.operation.entity.BizOperationChargeItem;
import com.his.operation.vo.TreatmentItemPriceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 手术麻醉计费明细 Mapper。
 */
@Mapper
public interface BizOperationChargeItemMapper extends BaseMapper<BizOperationChargeItem> {

    @Select("""
            SELECT * FROM biz_operation_charge_item
            WHERE del_flag = 0 AND apply_id = #{applyId}
            ORDER BY source_type ASC, id ASC
            """)
    List<BizOperationChargeItem> selectByApply(@Param("applyId") Long applyId);

    @Select("""
            SELECT * FROM biz_operation_charge_item
            WHERE del_flag = 0 AND source_type = #{sourceType} AND source_id = #{sourceId}
            ORDER BY id ASC
            """)
    List<BizOperationChargeItem> selectBySource(@Param("sourceType") Integer sourceType,
                                                @Param("sourceId") Long sourceId);

    /**
     * 幂等自检：同一来源同一项目是否已经有记账行（含已删除以外的所有状态）。
     */
    @Select("""
            SELECT COUNT(*) FROM biz_operation_charge_item
            WHERE source_type = #{sourceType} AND source_id = #{sourceId} AND item_code = #{itemCode}
            """)
    long countSame(@Param("sourceType") Integer sourceType,
                   @Param("sourceId") Long sourceId,
                   @Param("itemCode") String itemCode);

    /**
     * 取价目表（治疗项目字典，跨模块只读）。
     *
     * <p><b>只 select 表里真实存在的列</b>：治疗项目字典没有单位 / {@code spec}
     * 两列（真实列只有 item_code / item_name / item_type / dept_id / price / duration / usage_method / status），
     * 写上去编译不报错、运行时整条 SQL 报 Unknown column → 被 GlobalExceptionHandler 兜成 500，
     * 现象是"点提交就 500"，看不出是取价挂了。跨模块裸 SQL 一定要先
     * {@code information_schema.COLUMNS} 对列名（与表 RENAME 那次是同一类坑）。
     * 单位缺失由调用方兜底成"次"，规格缺失就是 null —— 这也是 {@link TreatmentItemPriceVO}
     * 只有两列的原因：表里确实只有这两列。
     *
     * @return 项目名与单价；项目不存在或已停用返回 {@code null}
     */
    @Select("""
            SELECT item_name AS itemName, price FROM sys_treatment_item
            WHERE item_code = #{itemCode} AND del_flag = 0 AND status = 1
            LIMIT 1
            """)
    TreatmentItemPriceVO selectTreatmentItem(@Param("itemCode") String itemCode);
}
