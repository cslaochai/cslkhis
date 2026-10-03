package com.his.patient.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.patient.entity.BizInpatientOrderTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BizInpatientOrderTemplateMapper extends BaseMapper<BizInpatientOrderTemplate> {

    /**
     * 科室名（跨模块读科室，走裸 SQL：his-patient 不依赖 his-system 的科室 Service）。
     * 取不到返回 null —— JOIN 不到的科室ID 宁可显示空，也不替它猜一个科室名。
     */
    @Select("SELECT dept_name FROM sys_department WHERE id = #{deptId} AND del_flag = 0")
    String selectDeptName(@Param("deptId") Long deptId);
}
