package com.his.system.api.service;

import com.his.system.api.vo.SettlementStateVO;

/**
 * 住院结算状态扩展点（由 his-charge 模块提供实现）。
 */
public interface InpatientSettlementApi {

    /**
     * 查询某次住院的结算状态。
     *
     * @param admissionId 入院ID
     * @return 结算状态；没有任何结算单时返回 {@code settled=false} 的状态对象
     */
    SettlementStateVO state(Long admissionId);
}
