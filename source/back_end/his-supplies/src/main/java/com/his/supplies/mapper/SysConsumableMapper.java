package com.his.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.supplies.entity.SysConsumable;
import com.his.supplies.vo.SysConsumableVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 耗材字典Mapper
 */
@Mapper
public interface SysConsumableMapper extends BaseMapper<SysConsumable> {

    /**
     * 字典分页（名称模糊 + 类别/状态过滤）
     */
    @Select("<script>" +
            "SELECT * FROM sys_consumable WHERE del_flag = 0 " +
            "<if test='keyword != null and keyword != \"\"'> AND (consumable_name LIKE CONCAT('%', #{keyword}, '%') OR consumable_code LIKE CONCAT('%', #{keyword}, '%')) </if> " +
            "<if test='category != null'> AND category = #{category} </if> " +
            "<if test='status != null'> AND status = #{status} </if> " +
            "ORDER BY create_time DESC, id DESC" +
            "</script>")
    Page<SysConsumableVO> selectConsumablePage(Page<SysConsumableVO> page,
                                               @Param("keyword") String keyword,
                                               @Param("category") Integer category,
                                               @Param("status") Integer status);

    /**
     * 启用字典下拉（全量，含库存页选耗材用）
     */
    @Select("SELECT id, consumable_code, consumable_name, specification, unit, retail_price " +
            "FROM sys_consumable WHERE del_flag = 0 AND status = 1 ORDER BY consumable_code ASC")
    List<com.his.supplies.vo.ConsumableSelectListVO> selectEnabledList();

    /**
     * 编码防重
     */
    @Select("SELECT COUNT(*) FROM sys_consumable WHERE consumable_code = #{code} AND del_flag = 0 AND id != #{excludeId}")
    long countByCode(@Param("code") String code, @Param("excludeId") Long excludeId);
}
