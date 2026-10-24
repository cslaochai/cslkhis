package com.his.charge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.charge.dto.BedsideAdmissionDTO;
import com.his.charge.dto.BedsideBillDTO;
import com.his.charge.dto.BedsidePayNoDTO;
import com.his.charge.dto.BedsideWardBoardQueryDTO;
import com.his.charge.dto.InpatientSettlementUpsertDTO;
import com.his.charge.dto.QrPayCreateDTO;
import com.his.charge.vo.BedsideSettleResultVO;
import com.his.charge.vo.BedsideWardPatientVO;
import com.his.charge.vo.InpatientSettlementPreviewVO;
import com.his.charge.vo.InpatientSettlementVO;
import com.his.charge.vo.QrPayCreateVO;
import com.his.charge.vo.QrPayResultVO;

/**
 * 床旁结算（护士站）：病区护士对分管在院患者办出院结算 + 亮聚合码。
 *
 * <p>与收费台的边界：收费台是收费员的岗位桌面（finance:cashier:*），谁来都能收；
 * 床旁结算是病区场景，每张单子都带<b>病区门禁</b>——入院所属科室不在当前岗位科室范围内，
 * 连试算都不给看（不是只靠列表收口，写操作入口逐次复核）。
 */
public interface BedsideSettlementService {

    /**
     * 病区在院患者榜（按当前岗位科室收口）。
     */
    IPage<BedsideWardPatientVO> wardBoard(BedsideWardBoardQueryDTO query);

    /**
     * 出院结算试算（病区门禁）。
     */
    InpatientSettlementPreviewVO settlePreview(BedsideAdmissionDTO dto);

    /**
     * 办理出院结算并带回亮码锚点（病区门禁）。
     */
    BedsideSettleResultVO settle(InpatientSettlementUpsertDTO dto);

    /**
     * 该入院当前有效的出院结算单（没有返回 null；病区门禁）。
     */
    InpatientSettlementVO settlementDetail(BedsideAdmissionDTO dto);

    /**
     * 按账单出聚合码（病区门禁：账单对应的入院必须在本岗位病房内）。
     */
    QrPayCreateVO createQr(BedsideBillDTO dto);

    /**
     * 轮询收款结果（病区门禁：支付单对应的账单必须在本岗位病房内）。
     */
    QrPayResultVO queryQr(BedsidePayNoDTO dto);

    /**
     * 模拟患者已付款（仅聚合码模拟模式的验收入口）：把「患者照单付款」登记进模拟存储，
     * 之后轮询即按实收入账。真模式一律拒绝。病区门禁同轮询。
     *
     * @param channel 模拟落地的资金渠道（1-微信 2-支付宝 3-云闪付），不传默认微信
     */
    void mockPaid(BedsidePayNoDTO dto, Integer channel);

    /**
     * 关掉该账单上全部待支付码（患者不付了/改收现金；病区门禁）。
     */
    void closeQr(BedsideBillDTO dto);
}
