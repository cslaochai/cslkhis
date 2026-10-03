package com.his.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.OrgUnitTypeEnum;
import com.his.common.enums.StaffTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.security.CurrentUser;
import com.his.security.UserUtils;
import com.his.system.entity.BizStaffDemand;
import com.his.system.mapper.BizStaffDemandMapper;
import com.his.system.service.StaffDemandService;
import com.his.system.vo.StaffDemandGapVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 人力需求与缺口服务实现。
 *
 * <p>这一层是闭环的第 ① 步：先回答「这天这个单元这个岗位需要多少人」，
 * 后面的排班（人手够不够）、校验（低于下限要拦）、复盘（为什么加班）才有分母。
 *
 * <p>两条硬边界：
 * <ol>
 *   <li><b>重算不盖手工</b>：派生只覆盖 source IN (1,2)，护士长拍板的 source=3 原样留着。</li>
 *   <li><b>单元必须带类型</b>：只传 orgId 不传 orgType 直接拒 —— 病区和科室的 id 不在
 *       同一个空间，只给 id 会把门诊科室的 id 当成病区去查（sql/209 的教训）。</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class StaffDemandServiceImpl extends ServiceImpl<BizStaffDemandMapper, BizStaffDemand>
        implements StaffDemandService {

    /** 重算一次最多往前铺多少天（防误点：把三年的需求一次性算出来没人看得完） */
    private static final int MAX_RECALC_DAYS = 31;

    private static final int SOURCE_CLINIC = 1;
    private static final int SOURCE_INPATIENT = 2;
    private static final int SOURCE_MANUAL = 3;

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
        DemandRecalcResult result = new DemandRecalcResult();
        String operator = operator();

        // 先清窗口内的派生产物：唯一键不含 del_flag，软删行会占键导致下次重算撞重复键
        baseMapper.purgeDerived(startDate, endDate);

        Long base = baseMapper.maxDerivedId();
        result.inpatient = baseMapper.deriveInpatient(startDate, endDate, base, operator,
                "住院派生：在院患者 × 护理等级工时，与病区核定下限取 MAX");
        result.clinicNurse = baseMapper.deriveClinicNurse(startDate, endDate, base + result.inpatient,
                operator, "门诊派生：分诊 1 人 + 跟诊 1 人/2 医生，与科室核定下限取 MAX");
        result.clinicDoctor = baseMapper.deriveClinicDoctor(startDate, endDate,
                base + result.inpatient + result.clinicNurse, operator,
                "门诊派生：医生需求 = 当日出诊医生数");
        return result;
    }

    @Override
    public void adjust(LocalDate demandDate, Integer orgType, Long orgId, Integer staffType,
                       Integer requiredCount, String remark) {
        if (demandDate == null) {
            throw new BusinessException("请选择要调整的日期");
        }
        if (orgType == null || orgId == null) {
            throw new BusinessException("请选择要调整的排班单元（并带上单元类型）");
        }
        if (staffType == null) {
            throw new BusinessException("请选择岗位类别");
        }
        if (requiredCount == null || requiredCount < 1) {
            throw new BusinessException("需求人数至少 1 人");
        }
        String orgName = orgType == OrgUnitTypeEnum.WARD.getCode()
                ? baseMapper.selectWardName(orgId)
                : baseMapper.selectDeptName(orgId);
        if (orgName == null) {
            throw new BusinessException("这个排班单元不存在或已停用，改不了它的需求");
        }
        Integer derived = baseMapper.selectRequired(demandDate, orgType, orgId, staffType);
        String basis = "护士长手工调整" + (derived == null ? "" : "（系统派生 " + derived + " 人）")
                + (remark == null || remark.isBlank() ? "" : "：" + remark.trim());

        Long id = baseMapper.maxDerivedId() + 1L;
        baseMapper.upsertManual(id, demandDate, orgType, orgId, orgName, staffType, requiredCount,
                basis, operator(), remark);
    }

    /** 缺口行补上给人看的文案：单元类型、岗位类别、需求来源 */
    private void fillText(StaffDemandGapVO row) {
        OrgUnitTypeEnum unit = OrgUnitTypeEnum.fromCode(row.getOrgType());
        if (unit != null) {
            row.setOrgTypeText(unit.getLabel());
        }
        StaffTypeEnum staff = StaffTypeEnum.fromCode(row.getStaffType());
        if (staff != null) {
            row.setStaffTypeText(staff.getLabel());
        }
        row.setDemandSourceText(switch (row.getDemandSource() == null ? 0 : row.getDemandSource()) {
            case SOURCE_CLINIC -> "门诊出诊派生";
            case SOURCE_INPATIENT -> "住院患者派生";
            case SOURCE_MANUAL -> "手工调整";
            default -> "未标注来源";
        });
    }

    private void requireRange(LocalDate startDate, LocalDate endDate) {
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

    private String operator() {
        CurrentUser user = UserUtils.getCurrentUser();
        return user == null || user.getUsername() == null ? "system" : user.getUsername();
    }
}
