package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.operation.entity.BizDaySurgeryItem;
import com.his.operation.vo.DaySurgeryItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 日间手术准入目录 Mapper。
 */
@Mapper
public interface BizDaySurgeryItemMapper extends BaseMapper<BizDaySurgeryItem> {

    @Select("""
            <script>
            SELECT i.* FROM biz_day_surgery_item i
             WHERE i.del_flag = 0
               <if test="keyword != null and keyword != ''">
                 AND (i.item_code LIKE CONCAT('%', #{keyword}, '%')
                   OR i.item_name LIKE CONCAT('%', #{keyword}, '%'))
               </if>
               <if test="deptId != null"> AND i.dept_id = #{deptId}</if>
               <if test="enabledOnly != null and enabledOnly == true"> AND i.status = 1</if>
             ORDER BY i.status DESC, i.item_code ASC
            </script>
            """)
    List<DaySurgeryItemVO> selectItemPage(IPage<DaySurgeryItemVO> page,
                                          @Param("keyword") String keyword,
                                          @Param("deptId") Long deptId,
                                          @Param("enabledOnly") Boolean enabledOnly);

    @Select("SELECT i.* FROM biz_day_surgery_item i WHERE i.id = #{id} AND i.del_flag = 0")
    DaySurgeryItemVO selectItemById(@Param("id") Long id);

    /**
     * 预约下拉：只给启用中的术式（停用术式不可新预约，存量单不受影响）
     */
    @Select("""
            <script>
            SELECT i.* FROM biz_day_surgery_item i
             WHERE i.del_flag = 0 AND i.status = 1
               <if test="deptId != null"> AND i.dept_id = #{deptId}</if>
             ORDER BY i.item_code ASC
            </script>
            """)
    List<DaySurgeryItemVO> selectEnabledList(@Param("deptId") Long deptId);

    @Select("SELECT d.dept_name FROM sys_department d WHERE d.id = #{deptId} AND d.del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);

    /**
     * 同编码是否已存在（新增/改编码时校验，excludeId 为自己）
     */
    @Select("SELECT COUNT(*) FROM biz_day_surgery_item WHERE del_flag = 0 AND item_code = #{code} AND id <> #{excludeId}")
    int countByCode(@Param("code") String code, @Param("excludeId") Long excludeId);
}
