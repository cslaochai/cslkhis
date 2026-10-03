package com.his.emr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.his.emr.entity.BizPrescriptionAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 处方审方动作流水 Mapper（L7 审方退回重开闭环，只增）。
 */
@Mapper
public interface BizPrescriptionAuditLogMapper extends BaseMapper<BizPrescriptionAuditLog> {
}
