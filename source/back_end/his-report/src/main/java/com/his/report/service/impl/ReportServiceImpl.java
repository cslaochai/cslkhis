package com.his.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.charge.entity.BizSettlementBill;
import com.his.charge.mapper.BizSettlementBillMapper;
import com.his.charge.vo.BizSettlementBillVO;
import com.his.common.enums.BillStatusEnum;
import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.mapper.BizAppointInfoMapper;
import com.his.appoint.vo.BizAppointInfoListVO;
import com.his.report.service.ReportService;
import com.his.report.vo.ChargeStatsVO;
import com.his.report.vo.DrugStatsVO;
import com.his.report.vo.MedicalTechStatsVO;
import com.his.report.vo.OutpatientStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 报表服务实现
 *
 * <p>旧首页「按角色返回 admin/doctor/nurse 三视角」的 getDashboardStats 已随门户工作台一期 A
 * 下线（在 Service 里按 role_code 分支决定给谁看什么，正是门户工作台要消灭的东西）。
 * 那六段聚合 SQL 原样迁到工作台卡片的 provider，由各卡片各取所需。
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final BizAppointInfoMapper registInfoMapper;
    private final BizSettlementBillMapper settlementBillMapper;

    @Override
    public OutpatientStatsVO getOutpatientStats(String startDate, String endDate) {
        LambdaQueryWrapper<BizAppointInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(startDate != null, BizAppointInfo::getVisitDate, startDate)
                .le(endDate != null, BizAppointInfo::getVisitDate, endDate);
        List<BizAppointInfo> registList = registInfoMapper.selectList(wrapper);

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
        List<BizSettlementBill> bills = settlementBillMapper.selectList(wrapper);

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
