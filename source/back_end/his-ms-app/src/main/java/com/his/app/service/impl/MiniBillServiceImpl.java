package com.his.app.service.impl;

import com.his.app.service.MiniBillService;
import com.his.app.vo.MiniBillDetailVO;
import com.his.app.vo.MiniBillListVO;
import com.his.common.enums.BillStatusEnum;
import com.his.common.enums.BillTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.spi.service.ChargeFacadeService;
import com.his.spi.service.PatientFacadeService;
import com.his.spi.vo.BizSettlementBillVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MiniBillServiceImpl implements MiniBillService {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ChargeFacadeService chargeFacadeService;
    private final PatientFacadeService patientFacadeService;

    @Override
    public List<MiniBillListVO> myBills(Long patientId, int limit) {
        requireAccessible(patientId);
        return chargeFacadeService.listBillsByPatient(patientId, limit).stream().map(this::toRow).toList();
    }

    @Override
    public MiniBillDetailVO billDetail(Long patientId, Long billId) {
        requireAccessible(patientId);
        BizSettlementBillVO bill = chargeFacadeService.getBillDetail(billId);
        if (bill == null) {
            throw new BusinessException("账单不存在或已删除");
        }
        // 归属从账单反查，不看前端传的是谁：拿别人的 billId 也读不到
        if (!Objects.equals(patientId, bill.getPatientId())) {
            throw new BusinessException("无权查看该账单");
        }
        MiniBillDetailVO vo = new MiniBillDetailVO();
        vo.setBill(bill);
        vo.setBillStatusText(BillStatusEnum.getLabel(bill.getBillStatus()));
        vo.setBillTypeText(BillTypeEnum.getLabel(bill.getBillType()));
        vo.setItems(chargeFacadeService.listBillItems(billId));
        return vo;
    }

    private MiniBillListVO toRow(BizSettlementBillVO bill) {
        MiniBillListVO vo = new MiniBillListVO();
        vo.setBillId(bill.getId());
        vo.setBillNo(bill.getBillNo());
        vo.setBillType(bill.getBillType());
        vo.setBillTypeText(BillTypeEnum.getLabel(bill.getBillType()));
        vo.setBillStatus(bill.getBillStatus());
        vo.setBillStatusText(BillStatusEnum.getLabel(bill.getBillStatus()));
        vo.setTotalAmount(bill.getTotalAmount());
        vo.setPayableAmount(bill.getPayableAmount());
        vo.setPaidAmount(bill.getPaidAmount());
        vo.setUnpaidAmount(bill.getUnpaidAmount());
        vo.setBillTime(bill.getBillTime() == null ? null : TIME.format(bill.getBillTime()));
        return vo;
    }

    private void requireAccessible(Long patientId) {
        if (patientId == null) {
            throw new BusinessException("就诊人不能为空");
        }
        if (!patientFacadeService.canAccessPatient(patientId)) {
            throw new BusinessException("无权查看该就诊人的费用明细");
        }
    }
}
