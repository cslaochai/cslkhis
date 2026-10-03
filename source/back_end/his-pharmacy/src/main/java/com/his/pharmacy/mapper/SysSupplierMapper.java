package com.his.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.pharmacy.entity.SysSupplier;
import com.his.pharmacy.vo.SysSupplierVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 供应商 Mapper
 */
@Mapper
public interface SysSupplierMapper extends BaseMapper<SysSupplier> {

    /**
     * 供应商分页（keyword 命中编码/名称/联系人）
     * 排序：正常状态优先 → 评级高优先 → 主键兜底（分页必须补唯一二级键，否则同秒插入的行在翻页时会重复或丢失）
     */
    @Select("<script>" +
            "SELECT supplier_id, supplier_code, supplier_name, contact_person, phone, address, " +
            "       license_no, license_expiry, rating, status, remark, " +
            "       create_by, create_time, update_by, update_time " +
            "FROM sys_supplier " +
            "WHERE del_flag = 0 " +
            "<if test='keyword != null and keyword != \"\"'> AND (supplier_name LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR supplier_code LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR contact_person LIKE CONCAT('%', #{keyword}, '%')) </if> " +
            "<if test='status != null'> AND status = #{status} </if> " +
            "ORDER BY status DESC, rating DESC, supplier_id ASC" +
            "</script>")
    Page<SysSupplierVO> selectSupplierPage(Page<SysSupplierVO> page,
                                           @Param("keyword") String keyword,
                                           @Param("status") Integer status);

    /**
     * 供应商编码唯一性校验（编辑时排除自身）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_supplier WHERE del_flag = 0 AND supplier_code = #{supplierCode} " +
            "<if test='excludeId != null'> AND supplier_id &lt;&gt; #{excludeId} </if>" +
            "</script>")
    long countByCode(@Param("supplierCode") String supplierCode, @Param("excludeId") Long excludeId);

    /**
     * 该供应商是否已被采购订单引用（被引用则不允许删除，删了历史订单的供应商就成孤儿了）
     */
    @Select("SELECT COUNT(*) FROM biz_purchase_order WHERE del_flag = 0 AND supplier_id = #{supplierId}")
    long countOrderRef(@Param("supplierId") Long supplierId);

    /**
     * 供应商详情（下拉/详情共用）
     */
    @Select("SELECT supplier_id, supplier_code, supplier_name, contact_person, phone, address, " +
            "       license_no, license_expiry, rating, status, remark, " +
            "       create_by, create_time, update_by, update_time " +
            "FROM sys_supplier WHERE del_flag = 0 AND supplier_id = #{supplierId}")
    SysSupplierVO selectSupplierById(@Param("supplierId") Long supplierId);
}
