package com.his.medicaltech.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.medicaltech.entity.BizTransfusionApprove;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用血分级审批流水 Mapper（sql/93，只增不改）。
 */
@Mapper
public interface BizTransfusionApproveMapper extends BaseMapper<BizTransfusionApprove> {

    /**
     * 按申请单取审批流水（时间正序 = 逐级链的自然顺序）。
     */
    @Select("""
            SELECT * FROM biz_transfusion_approve
            WHERE apply_id = #{applyId} AND del_flag = 0
            ORDER BY id ASC
            """)
    List<BizTransfusionApprove> selectByApply(@Param("applyId") Long applyId);

    /**
     * 审批人职称快照（员工的名称，审批时刻定格；查不到不阻断）。
     */
    @Select("SELECT title FROM sys_employee WHERE id = #{empId} AND del_flag = 0")
    String selectEmpTitle(@Param("empId") Long empId);
}
