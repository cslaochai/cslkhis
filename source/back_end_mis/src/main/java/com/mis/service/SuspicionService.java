package com.mis.service;

import com.mis.common.base.PageVO;
import com.mis.dto.SuspicionHandleDTO;
import com.mis.dto.SuspicionQueryDTO;
import com.mis.entity.MssSuspicion;
import com.mis.vo.ActionVO;

/**
 * 疑点稽核
 */
public interface SuspicionService {

    PageVO<MssSuspicion> suspicionPage(SuspicionQueryDTO query);

    /** 一键稽核：按启用规则扫描结算数据并幂等落库，返回本次新增疑点数 */
    ActionVO runAudit();

    MssSuspicion getDetail(Long id);

    /** 确认违规（并自动生成追回单） */
    ActionVO confirm(SuspicionHandleDTO dto);

    /** 排除疑点 */
    ActionVO exclude(SuspicionHandleDTO dto);
}
