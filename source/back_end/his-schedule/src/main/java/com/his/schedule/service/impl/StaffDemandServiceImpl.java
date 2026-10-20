package com.his.schedule.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.OrgUnitTypeEnum;
import com.his.common.enums.StaffDemandSourceEnum;
import com.his.common.enums.StaffTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.schedule.entity.BizStaffDemand;
import com.his.schedule.mapper.BizStaffDemandMapper;
import com.his.schedule.service.StaffDemandService;
import com.his.schedule.vo.StaffDemandGapVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 人力需求与缺口服务实现。
 */
@Service
@RequiredArgsConstructor
public class StaffDemandServiceImpl extends ServiceImpl<BizStaffDemandMapper, BizStaffDemand> implements StaffDemandService {

    /**
     * 重算一次最多往前铺多少天（防误点：把三年的需求一次性算出来没人看得完）
     */
    private static final int MAX_RECALC_DAYS = 31;

    @Override
    public List<StaffDemandGapVO> gapList(LocalDate startDate, LocalDate endDate,
                                          Integer orgType, Long orgId, Integer staffType) {
        requireRange(startDate, endDate);
        if (orgId != null && orgType == null) {
            // 两种单元的 id 不在同一空间，只给 id 等于让系统猜它是病区还是科室
            throw new BusinessException("查某个单元的缺口时必须同时给单元类型（1-科室 2-病区）");
        }
        List<StaffDemandGapVO> rows = baseMapper.selectGap(startDate, endDate, orgType, orgId, staffType);
        rows.forEach(this::fillText);
        return rows;
    }

    @Override
    public DemandRecalcResult recalc(LocalDate startDate, LocalDate endDate) {
        requireRange(startDate, endDate);
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        DemandRecalcResult result = new DemandRecalcResult();
        String operator = operatorUser.getRealName();
        Long operatorId = operatorUser.getEmployeeId();

        // 先清窗口内的派生产物：唯一键不含 del_flag，软删行会占键导致下次重算撞重复键
        baseMapper.purgeDerived(startDate, endDate);

        Long base = baseMapper.maxDerivedId();
        result.inpatient = baseMapper.deriveInpatient(startDate, endDate, base, operator, operatorId,
                "住院派生：在院患者 × 护理等级工时，与病区核定下限取 MAX");
        result.clinicNurse = baseMapper.deriveClinicNurse(startDate, endDate, base + result.inpatient,
                operator, operatorId, "门诊派生：分诊 1 人 + 跟诊 1 人/2 医生，与科室核定下限取 MAX");
        result.clinicDoctor = baseMapper.deriveClinicDoctor(startDate, endDate,
                base + result.inpatient + result.clinicNurse, operator, operatorId,
                "门诊派生：医生需求 = 当日出诊医生数");
        return result;
    }

    @Override
    public void adjust(LocalDate demandDate, Integer orgType, Long orgId, Integer staffType,
                       Integer requiredCount, String remark) {
        // D-业务规则：非空与值域（至少 1 人）混写在一个条件里，标量参数不许挂约束注解，DTO 注解无法表达，保留
        if (requiredCount == null || requiredCount < 1) {
            throw new BusinessException("需求人数至少 1 人");
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        String orgName = orgType == OrgUnitTypeEnum.WARD.getCode()
                ? baseMapper.selectWardName(orgId)
                : baseMapper.selectDeptName(orgId);
        if (orgName == null) {
            throw new BusinessException("这个排班单元不存在或已停用，改不了它的需求");
        }
        Integer derived = baseMapper.selectRequired(demandDate, orgType, orgId, staffType);
        String basis = "护士长手工调整" + (derived == null ? "" : "（系统派生 " + derived + " 人）")
                + (!TextUtil.hasText(remark) ? "" : "：" + remark.trim());

        Long id = baseMapper.maxDerivedId() + 1L;
        baseMapper.upsertManual(id, demandDate, orgType, orgId, orgName, staffType, requiredCount,
                basis, operatorUser.getRealName(), operatorUser.getEmployeeId(), remark);
    }

    /**
     * 缺口行补上给人看的文案：单元类型、岗位类别、需求来源
     */
    private void fillText(StaffDemandGapVO row) {
        OrgUnitTypeEnum unit = OrgUnitTypeEnum.fromCode(row.getOrgType());
        if (unit != null) {
            row.setOrgTypeText(unit.getLabel());
        }
        StaffTypeEnum staff = StaffTypeEnum.fromCode(row.getStaffType());
        if (staff != null) {
            row.setStaffTypeText(staff.getLabel());
        }
        StaffDemandSourceEnum source = row.getDemandSource() == null
                ? null : StaffDemandSourceEnum.fromCode(row.getDemandSource());
        row.setDemandSourceText(source == null ? "未标注来源" : source.getLabel());
    }

    private void requireRange(LocalDate startDate, LocalDate endDate) {
        // C-非 web 入参：私有日期区间守卫，被 gapList/recalc 等多个入口共用（与跨字段规则混在同一段），Bean Validation 不覆盖内部调用，保留
        if (startDate == null || endDate == null) {
            throw new BusinessException("请选择日期区间");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        if (startDate.plusDays(MAX_RECALC_DAYS).isBefore(endDate)) {
            throw new BusinessException("一次最多算 " + MAX_RECALC_DAYS + " 天的需求");
        }
    }

}
