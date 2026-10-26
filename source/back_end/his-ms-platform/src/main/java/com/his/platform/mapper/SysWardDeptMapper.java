package com.his.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.platform.entity.SysWardDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 病区-科室多对多 Mapper。
 */
@Mapper
public interface SysWardDeptMapper extends BaseMapper<SysWardDept> {

    /**
     * 按科室反查该科室可收治的病区ID（含跨科收治的，不只本位病区）。
     *
     * <p>用于「护士按病区取数」「排班按科室找可选病区」等场景：
     * 在引入多对多之前，这些地方只能按 {@code sys_ward.dept_id} 单值过滤，导致
     * 跨科收治的病区对收治科室不可见。
     */
    @Select("SELECT wd.ward_id FROM sys_ward_dept wd "
            + "JOIN sys_ward w ON w.ward_id = wd.ward_id AND w.status = 1 "
            + "WHERE wd.dept_id = #{deptId} ORDER BY wd.is_primary DESC, wd.sort_order ASC")
    List<Long> selectWardIdsByDeptId(@Param("deptId") Long deptId);

    /**
     * 取某病区的本位科室ID（多对多表口径）。
     * 取不到返回 NULL——调用方需容忍空，不要当成异常。
     */
    @Select("SELECT wd.dept_id FROM sys_ward_dept wd "
            + "WHERE wd.ward_id = #{wardId} AND wd.is_primary = 1 LIMIT 1")
    Long selectPrimaryDeptId(@Param("wardId") Long wardId);
}
