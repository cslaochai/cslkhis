package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.charge.dto.BedsideAdmissionDTO;
import com.his.charge.dto.BedsideBillDTO;
import com.his.charge.dto.BedsidePayNoDTO;
import com.his.charge.dto.BedsideWardBoardQueryDTO;
import com.his.charge.dto.InpatientSettlementUpsertDTO;
import com.his.charge.dto.QrPayCreateDTO;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.mapper.BedsideSettlementMapper;
import com.his.charge.service.BedsideSettlementService;
import com.his.charge.service.InpatientAccountService;
import com.his.charge.service.PayOrderService;
import com.his.charge.service.QrPayService;
import com.his.charge.service.SettlementBillService;
import com.his.charge.vo.BedsideSettleResultVO;
import com.his.charge.vo.BedsideWardPatientVO;
import com.his.charge.vo.InpatientSettlementPreviewVO;
import com.his.charge.vo.InpatientSettlementVO;
import com.his.charge.vo.PayOrderVO;
import com.his.charge.vo.QrPayCreateVO;
import com.his.charge.vo.QrPayResultVO;
import com.his.common.enums.EncounterTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.spi.service.DeptScopeService;
import com.his.spi.service.InpatientFacadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 床旁结算实现：全部金额与出账逻辑委托既有服务（不复制一份规则），
 * 本类只做两件事——病区范围收口、给前端拼亮码锚点。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BedsideSettlementServiceImpl implements BedsideSettlementService {

    private final BedsideSettlementMapper bedsideSettlementMapper;
    private final InpatientAccountService inpatientAccountService;
    private final QrPayService qrPayService;
    private final PayOrderService payOrderService;
    private final SettlementBillService settlementBillService;
    private final DeptScopeService deptScopeService;
    private final InpatientFacadeService inpatientFacadeService;

    @Override
    public IPage<BedsideWardPatientVO> wardBoard(BedsideWardBoardQueryDTO query) {
        // 病区范围服务端算：全院角色给全集，受限岗位给授权科室，绝不让前端传 deptId
        List<Long> deptIds = deptScopeService.scopedDeptIds(null);
        String kw = query.getKeyword() == null ? null : query.getKeyword().trim();
        if (kw != null && kw.isEmpty()) {
            kw = null;
        }
        IPage<BedsideWardPatientVO> page = bedsideSettlementMapper.selectWardBoard(
                new Page<>(query.getPageNum(), query.getPageSize()), deptIds, kw);
        for (BedsideWardPatientVO row : page.getRecords()) {
            row.setSettled(row.getBillId() != null);
        }
        return page;
    }

    @Override
    public InpatientSettlementPreviewVO settlePreview(BedsideAdmissionDTO dto) {
        assertWardAccessible(dto.getAdmissionId());
        // settleMode 由调用方显式给（与住院结算页同契约：后端拿参保号也判不出"这次要不要走医保"）；
        // 选了医保但平台查无参保/停保 → 服务端降级自费原价，不拦单
        return inpatientAccountService.preview(dto.getAdmissionId(), dto.getSettleMode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BedsideSettleResultVO settle(InpatientSettlementUpsertDTO dto) {
        assertWardAccessible(dto.getAdmissionId());
        // 门禁先过再结账：InpatientAccountService.settle 自带"已结算不能再结/无待结算费用"校验，不复制
        InpatientSettlementVO settlement = inpatientAccountService.settle(dto);
        BedsideSettleResultVO result = new BedsideSettleResultVO();
        result.setSettlement(settlement);
        result.setUnpaidAmount(NumUtil.scale(NumUtil.orZero(settlement.getArrearsAmount()), 2));
        // 亮码锚点：结算单 VO 不带 billId，取本次入院最新的有效出院账单补上
        BizSettlementBill bill = settlementBillService.latestDischargeBill(dto.getAdmissionId());
        if (bill != null) {
            result.setBillId(bill.getId());
            result.setBillNo(bill.getBillNo());
        }
        return result;
    }

    @Override
    public InpatientSettlementVO settlementDetail(BedsideAdmissionDTO dto) {
        assertWardAccessible(dto.getAdmissionId());
        return inpatientAccountService.settlementDetail(dto.getAdmissionId());
    }

    @Override
    public QrPayCreateVO createQr(BedsideBillDTO dto) {
        assertBillWardAccessible(dto.getBillId());
        QrPayCreateDTO create = new QrPayCreateDTO();
        create.setBillId(dto.getBillId());
        return qrPayService.create(create);
    }

    @Override
    public QrPayResultVO queryQr(BedsidePayNoDTO dto) {
        PayOrderVO order = payOrderService.getByPayNo(dto.getPayNo());
        if (order == null || order.getBizId() == null) {
            throw new BusinessException("支付单不存在：" + dto.getPayNo());
        }
        assertBillWardAccessible(order.getBizId());
        return qrPayService.queryResult(dto.getPayNo());
    }

    @Override
    public void mockPaid(BedsidePayNoDTO dto, Integer channel) {
        PayOrderVO order = payOrderService.getByPayNo(dto.getPayNo());
        if (order == null || order.getBizId() == null) {
            throw new BusinessException("支付单不存在：" + dto.getPayNo());
        }
        assertBillWardAccessible(order.getBizId());
        qrPayService.mockMarkPaid(dto.getPayNo(), channel);
    }

    @Override
    public void closeQr(BedsideBillDTO dto) {
        assertBillWardAccessible(dto.getBillId());
        // 关该账单下全部待支付码（聚合码 + 小程序侧指定渠道的码），不是只关当前显示的那张
        qrPayService.closePendingByBill(dto.getBillId());
    }

    // ---------- 病区门禁 ----------

    /**
     * 写操作/详情的门禁：入院所属科室不在当前岗位科室范围内一律拒绝。
     * 入院不存在同样拒绝（null 科室对受限岗位恒无权，不得兜底放行）。
     */
    private void assertWardAccessible(Long admissionId) {
        if (admissionId == null) {
            throw new BusinessException("入院ID不能为空");
        }
        Long deptId = inpatientFacadeService.getAdmissionDeptId(admissionId);
        if (deptId == null) {
            throw new BusinessException("入院记录不存在");
        }
        deptScopeService.assertDeptAccessible(deptId);
    }

    /**
     * 账单维度的门禁：只认住院账单（床旁结算是住院场景），
     * 再按 encounterId 反查入院科室过同一道门禁。
     */
    private void assertBillWardAccessible(Long billId) {
        if (billId == null) {
            throw new BusinessException("账单ID不能为空");
        }
        BizSettlementBill bill = settlementBillService.getById(billId);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        if (!EncounterTypeEnum.INPATIENT.getCode().equals(bill.getEncounterType())) {
            throw new BusinessException("床旁结算只受理住院账单");
        }
        Long deptId = inpatientFacadeService.getAdmissionDeptId(bill.getEncounterId());
        if (deptId == null) {
            throw new BusinessException("入院记录不存在");
        }
        deptScopeService.assertDeptAccessible(deptId);
    }
}
