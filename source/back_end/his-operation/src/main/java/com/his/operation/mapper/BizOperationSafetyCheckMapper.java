package com.his.operation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.operation.entity.BizOperationSafetyCheck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 手术安全核查 Mapper。
 */
@Mapper
public interface BizOperationSafetyCheckMapper extends BaseMapper<BizOperationSafetyCheck> {

    /**
     * 某台手术的已签核查行（按法定时段升序，0~3 行）
     */
    @Select("""
            SELECT * FROM biz_operation_safety_check
            WHERE del_flag = 0 AND apply_id = #{applyId}
            ORDER BY phase ASC
            """)
    List<BizOperationSafetyCheck> selectByApply(@Param("applyId") Long applyId);

    /**
     * 某台手术的已签核查轮数（0~3）：finish() 闸门用 —— 建过单就必须签满三轮
     */
    @Select("SELECT COUNT(DISTINCT phase) FROM biz_operation_safety_check WHERE del_flag = 0 AND apply_id = #{applyId}")
    long countPhases(@Param("applyId") Long applyId);

    /**
     * 当天已生成的核查单号条数（单号序号用）
     */
    @Select("SELECT COUNT(*) FROM biz_operation_safety_check WHERE del_flag = 0 AND check_no LIKE CONCAT(#{prefix}, '%')")
    long countByNoPrefix(@Param("prefix") String prefix);

    /**
     * 员工姓名（三方签名一律服务端查名，不信任前端传来的姓名）
     */
    @Select("SELECT emp_name FROM sys_employee WHERE id = #{empId} AND del_flag = 0")
    String selectEmployeeName(@Param("empId") Long empId);
}
