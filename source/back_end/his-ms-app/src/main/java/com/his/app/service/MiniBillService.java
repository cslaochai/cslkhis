package com.his.app.service;

import com.his.app.vo.MiniBillDetailVO;
import com.his.app.vo.MiniBillListVO;

import java.util.List;

/**
 * 患者端费用明细：按就诊人查历史账单与逐项明细。
 */
public interface MiniBillService {

    /**
     * 我的账单列表（全部状态，按结算时间倒序）
     */
    List<MiniBillListVO> myBills(Long patientId, int limit);

    /**
     * 账单详情（账单头 + 明细行）
     */
    MiniBillDetailVO billDetail(Long patientId, Long billId);
}
