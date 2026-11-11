package com.his.app.vo;

import com.his.spi.vo.BillItemVO;
import com.his.spi.vo.BizSettlementBillVO;
import lombok.Data;

import java.util.List;

/**
 * 患者端-账单详情（账单头 + 逐项明细）。
 */
@Data
public class MiniBillDetailVO {

    /**
     * 账单头（应缴/已缴/医保分摊/状态）
     */
    private BizSettlementBillVO bill;

    /**
     * 账单状态文案（由 BillStatusEnum 给出，前端不自行映射码值）
     */
    private String billStatusText;

    /**
     * 账单类型文案
     */
    private String billTypeText;

    /**
     * 明细行（项目名称、目录类别、金额与医保分摊）
     */
    private List<BillItemVO> items;
}
