package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.pharmacy.entity.BizDrugSupplierReturn;
import com.his.pharmacy.vo.SupplierReturnVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 药品供应商退货单 Mapper（sql/154 ③级）
 */
@Mapper
public interface BizDrugSupplierReturnMapper extends BaseMapper<BizDrugSupplierReturn> {

    /** 自定义 SQL 不走 MP 的 @TableLogic，del_flag 必须手写 */
    String COLUMNS = "r.id, r.return_no, r.supplier_id, r.supplier_name, r.return_reason, r.src_ref_no, r.status, "
            + "r.total_items, r.total_quantity, r.total_amount, r.return_by, r.return_time, "
            + "r.cancel_by, r.cancel_time, r.cancel_reason, "
            + "r.create_by, r.create_time, r.update_by, r.update_time, r.remark ";

    @Select("<script>" +
            "SELECT " + COLUMNS +
            "FROM biz_drug_supplier_return r WHERE r.del_flag = 0 " +
            "<if test='returnNo != null and returnNo != \"\"'> AND r.return_no LIKE CONCAT('%', #{returnNo}, '%') </if> " +
            "<if test='supplierId != null'> AND r.supplier_id = #{supplierId} </if> " +
            "<if test='status != null'> AND r.status = #{status} </if> " +
            "<if test='keyword != null and keyword != \"\"'> AND (r.return_reason LIKE CONCAT('%', #{keyword}, '%') " +
            "       OR r.src_ref_no LIKE CONCAT('%', #{keyword}, '%')) </if> " +
            "<if test='dateStart != null and dateStart != \"\"'> AND DATE(r.create_time) &gt;= #{dateStart} </if> " +
            "<if test='dateEnd != null and dateEnd != \"\"'> AND DATE(r.create_time) &lt;= #{dateEnd} </if> " +
            "ORDER BY r.create_time DESC, r.id DESC" +
            "</script>")
    Page<SupplierReturnVO> selectReturnPage(Page<SupplierReturnVO> page,
                                            @Param("returnNo") String returnNo,
                                            @Param("supplierId") Long supplierId,
                                            @Param("status") Integer status,
                                            @Param("keyword") String keyword,
                                            @Param("dateStart") String dateStart,
                                            @Param("dateEnd") String dateEnd);

    @Select("SELECT " + COLUMNS + "FROM biz_drug_supplier_return r WHERE r.del_flag = 0 AND r.id = #{id}")
    SupplierReturnVO selectReturnById(@Param("id") Long id);

    /** 按主键取单并加行锁：确认退货是「判状态 → 扣库存 → 回写状态」，不锁会重复扣同一批货 */
    @Select("SELECT * FROM biz_drug_supplier_return WHERE id = #{id} AND del_flag = 0 FOR UPDATE")
    BizDrugSupplierReturn selectByIdForUpdate(@Param("id") Long id);

    /**
     * ⚠ 物理删：uk_supplier_return_no 不含 del_flag，软删的行仍占着单号（AGENTS §3）
     */
    @Delete("DELETE FROM biz_drug_supplier_return WHERE id = #{id}")
    int purgeById(@Param("id") Long id);
}
