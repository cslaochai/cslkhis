package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.mapper.BizAppointInfoMapper;
import com.his.appoint.vo.BizAppointInfoListVO;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.mapper.BizSettlementBillMapper;
import com.his.charge.vo.BizSettlementBillVO;
import com.his.common.enums.BillStatusEnum;
import com.his.medicaltech.service.ReportService;
import com.his.medicaltech.vo.ChargeStatsVO;
import com.his.medicaltech.vo.DrugStatsVO;
import com.his.medicaltech.vo.MedicalTechStatsVO;
import com.his.medicaltech.vo.OutpatientStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 报表服务实现
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final BizAppointInfoMapper bizAppointInfoMapper;
    private final BizSettlementBillMapper bizSettlementBillMapper;

    @Override
    public OutpatientStatsVO getOutpatientStats(String startDate, String endDate) {
        LambdaQueryWrapper<BizAppointInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(startDate != null, BizAppointInfo::getVisitDate, startDate)
                .le(endDate != null, BizAppointInfo::getVisitDate, endDate);
        List<BizAppointInfo> registList = bizAppointInfoMapper.selectList(wrapper);

        long totalCount = registList.size();
        long maleCount = registList.stream().filter(r -> r.getGender() != null && r.getGender() == 1).count();
        long femaleCount = totalCount - maleCount;

        OutpatientStatsVO result = new OutpatientStatsVO();
        result.setTotalCount(totalCount);
        result.setMaleCount(maleCount);
        result.setFemaleCount(femaleCount);
        result.setRegistList(registList.stream().map(entity -> {
            BizAppointInfoListVO vo = new BizAppointInfoListVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList()));
        return result;
    }

    @Override
    public ChargeStatsVO getChargeStats(String startDate, String endDate) {
        // 四层口径：已支付（bill_status=3）的结算账单，按账务归属日 bill_date 聚合；金额取应收合计
        LambdaQueryWrapper<BizSettlementBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(startDate != null, BizSettlementBill::getBillDate, startDate)
                .le(endDate != null, BizSettlementBill::getBillDate, endDate)
                .eq(BizSettlementBill::getBillStatus, BillStatusEnum.PAID.getCode());
        List<BizSettlementBill> bills = bizSettlementBillMapper.selectList(wrapper);

        BigDecimal totalAmount = bills.stream()
                .map(b -> b.getPayableAmount() == null ? BigDecimal.ZERO : b.getPayableAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ChargeStatsVO result = new ChargeStatsVO();
        result.setTotalCount((long) bills.size());
        result.setTotalAmount(totalAmount);
        result.setBills(bills.stream().map(entity -> {
            BizSettlementBillVO vo = new BizSettlementBillVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList()));
        return result;
    }

    @Override
    public DrugStatsVO getDrugStats(String startDate, String endDate) {
        // 药品统计需要关联药房模块数据
        DrugStatsVO result = new DrugStatsVO();
        result.setMessage("药品统计功能待实现");
        return result;
    }

    @Override
    public MedicalTechStatsVO getMedicalTechStats(String startDate, String endDate) {
        // 医技统计需要关联医技模块数据
        MedicalTechStatsVO result = new MedicalTechStatsVO();
        result.setMessage("医技统计功能待实现");
        return result;
    }
}
