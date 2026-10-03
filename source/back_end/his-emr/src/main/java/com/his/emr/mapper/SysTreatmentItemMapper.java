package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.SysTreatmentItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysTreatmentItemMapper extends BaseMapper<SysTreatmentItem> {

    /**
     * 开单快照读：项目本身 + 能解析出来的执行科室名。
     *
     * <p>科室名一律 LEFT JOIN 科室现算：老字典里科室ID 只有
     * 101/105 两个值且在科室表中**不存在**（孤儿引用），直接快照字典里的科室会写进一个
     * 根本不存在的科室。查不到就返回 NULL，页面显示「未指定」，不兜底。
     *
     * <p>跨模块只读一张字典表、不引 his-system 的实体依赖，与 G21 的
     * {@code BizExamDeviceMapper.selectEquipmentOptions} 同一口径。
     */
    @Select("SELECT i.id AS itemId, i.item_code AS itemCode, i.item_name AS itemName, "
            + "       i.item_type AS itemType, i.price AS price, i.duration AS duration, "
            + "       i.usage_method AS usageMethod, i.status AS status, "
            + "       i.dept_id AS execDeptId, d.dept_name AS execDeptName "
            + "  FROM sys_treatment_item i "
            + "  LEFT JOIN sys_department d ON d.id = i.dept_id AND d.del_flag = 0 "
            + " WHERE i.id = #{itemId} AND i.del_flag = 0")
    Map<String, Object> selectApplySnapshot(@Param("itemId") Long itemId);

    /**
     * 开单候选项目：与 {@link #selectApplySnapshot} 同一套列与同一套 LEFT JOIN 口径，
     * 免得「列表里显示的科室」和「开单快照存的科室」两处各查各的。
     */
    @Select("<script>"
            + "SELECT i.id AS itemId, i.item_code AS itemCode, i.item_name AS itemName, "
            + "       i.item_type AS itemType, i.price AS price, i.duration AS duration, "
            + "       i.usage_method AS usageMethod, i.status AS status, "
            + "       i.dept_id AS execDeptId, d.dept_name AS execDeptName "
            + "  FROM sys_treatment_item i "
            + "  LEFT JOIN sys_department d ON d.id = i.dept_id AND d.del_flag = 0 "
            + " WHERE i.del_flag = 0 AND i.status = 1 "
            + "   <if test='kw != null and kw != \"\"'>"
            + "     AND (i.item_name LIKE CONCAT('%', #{kw}, '%') OR i.item_code LIKE CONCAT('%', #{kw}, '%'))"
            + "   </if>"
            + " ORDER BY i.item_type, i.item_code LIMIT #{limit}"
            + "</script>")
    List<Map<String, Object>> selectOptions(@Param("kw") String kw, @Param("limit") int limit);
}
