package com.his.outpatient.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * sys_employee 只读窄口（本模块裸 SQL，跨模块铁律：只读字典/主数据走本模块 Mapper，不建跨模块依赖）。
 *
 * <p>目前只服务于急诊诊查费定价：按接诊医生取职称码，进 {@code TitleTierMapping} 推档位。
 * 只 SELECT title 一列——职称码是稳定契约（101医士/102医师/201主治/301副主任/401主任），
 * 文案可能被字典维护改掉，码值不会。
 */
@Mapper
public interface SysEmployeeTitleMapper {

    /**
     * 取员工职称码；查无此人 / 已逻辑删除返回 null（调用方按普通档兜底，不因取不到职称拦流程）。
     */
    @Select("SELECT title FROM sys_employee WHERE id = #{employeeId} AND del_flag = 0")
    String selectTitleById(@Param("employeeId") Long employeeId);
}
