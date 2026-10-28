package com.his.clinical.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.enums.CdrInspApplyStatusEnum;
import com.his.common.util.TextUtil;
import com.his.clinical.entity.BizInspectionApply;
import com.his.clinical.entity.BizLaboratoryApply;
import com.his.clinical.mapper.BizInspectionApplyMapper;
import com.his.clinical.mapper.BizLaboratoryApplyMapper;
import com.his.spi.dto.InspectionApplyPageQueryDTO;
import com.his.spi.service.EmrApplyFacadeService;
import com.his.spi.vo.InspectionApplyBriefVO;
import com.his.spi.vo.LaboratoryApplyBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 申请单事实端口实现。
 */
@Service
@RequiredArgsConstructor
public class EmrApplyFacadeServiceImpl implements EmrApplyFacadeService {

    private final BizInspectionApplyMapper bizInspectionApplyMapper;
    private final BizLaboratoryApplyMapper bizLaboratoryApplyMapper;

    @Override
    public InspectionApplyBriefVO getInspectionApplyById(Long applyId) {
        return applyId == null ? null : toInspectionVo(bizInspectionApplyMapper.selectById(applyId));
    }

    @Override
    public LaboratoryApplyBriefVO getLaboratoryApplyById(Long applyId) {
        return applyId == null ? null : toLaboratoryVo(bizLaboratoryApplyMapper.selectById(applyId));
    }

    @Override
    public PageResult<InspectionApplyBriefVO> pageInspectionApply(InspectionApplyPageQueryDTO query) {
        Page<BizInspectionApply> page = bizInspectionApplyMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), inspectionWrapper(query));
        List<InspectionApplyBriefVO> records = new ArrayList<>();
        for (BizInspectionApply apply : page.getRecords()) {
            records.add(toInspectionVo(apply));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public long countInspectionApply(InspectionApplyPageQueryDTO query) {
        // 计数只取筛选条件：排序列喂给 count 不影响结果，但会撞 ONLY_FULL_GROUP_BY 之外的方言差异，
        // 因此这里重新组一份不带 orderBy 的同类条件，而不是复用 wrapper 再 orderBy 后计数。
        LambdaQueryWrapper<BizInspectionApply> w = new LambdaQueryWrapper<>();
        applyConditions(w, query);
        return bizInspectionApplyMapper.selectCount(w);
    }

    @Override
    public int markInspectionBooked(Long applyId, LocalDateTime appointmentTime, String operator) {
        if (applyId == null) {
            return 0;
        }
        // 占号→已预约(3)：按主键命中推进并回写预约时刻。是否可预约的前置状态
        // （2-已缴费 / 急诊绿色通道 1-已提交，以及残留 3 无在办单的重约恢复）由调用方
        // 编排侧校验，此处不叠加 apply_status 守卫，保留重约恢复路径的命中能力；
        // del_flag=0 由 @TableLogic 自动追加。
        return bizInspectionApplyMapper.update(null, new LambdaUpdateWrapper<BizInspectionApply>()
                .eq(BizInspectionApply::getId, applyId)
                .set(BizInspectionApply::getApplyStatus, CdrInspApplyStatusEnum.BOOKED.getCode())
                .set(BizInspectionApply::getAppointmentTime, appointmentTime)
                .set(BizInspectionApply::getUpdateBy, operator)
                .set(BizInspectionApply::getUpdateTime, LocalDateTime.now().withNano(0)));
    }

    @Override
    public int markInspectionArrived(Long applyId, String operator) {
        if (applyId == null) {
            return 0;
        }
        // 到检→检查中(4)：WHERE 守卫 apply_status=3，仅仍「已预约」的单推进，
        // 并发重复签到只命中一方，不覆盖已进入执行链或已回退的状态。
        return bizInspectionApplyMapper.update(null, new LambdaUpdateWrapper<BizInspectionApply>()
                .eq(BizInspectionApply::getId, applyId)
                .eq(BizInspectionApply::getApplyStatus, CdrInspApplyStatusEnum.BOOKED.getCode())
                .set(BizInspectionApply::getApplyStatus, CdrInspApplyStatusEnum.EXAMINING.getCode())
                .set(BizInspectionApply::getUpdateBy, operator)
                .set(BizInspectionApply::getUpdateTime, LocalDateTime.now().withNano(0)));
    }

    @Override
    public int revertInspectionBooked(Long applyId, Integer prevStatus, String operator) {
        if (applyId == null || prevStatus == null) {
            return 0;
        }
        // 取消/爽约回退：WHERE 守卫 apply_status=3，按预约前状态精确回退并清空预约时刻；
        // 已进入执行链(4检查中/5已出报告)的单不被回退。
        return bizInspectionApplyMapper.update(null, new LambdaUpdateWrapper<BizInspectionApply>()
                .eq(BizInspectionApply::getId, applyId)
                .eq(BizInspectionApply::getApplyStatus, CdrInspApplyStatusEnum.BOOKED.getCode())
                .set(BizInspectionApply::getApplyStatus, prevStatus)
                .set(BizInspectionApply::getAppointmentTime, null)
                .set(BizInspectionApply::getUpdateBy, operator)
                .set(BizInspectionApply::getUpdateTime, LocalDateTime.now().withNano(0)));
    }

    @Override
    public Map<Long, Integer> mapReportReadyCountByRegistIds(Collection<Long> registIds) {
        if (registIds == null || registIds.isEmpty()) {
            return Map.of();
        }
        // 「有新报告」= 检查/检验申请单已推进到 5-已出报告。检查、检验两张表同一套
        // apply_status 枚举（CdrInspApplyStatusEnum），各一次 IN 查询后按 registId 合计。
        // del_flag=0 由 @TableLogic 自动追加
        Map<Long, Integer> result = new HashMap<>();
        List<BizInspectionApply> insList = bizInspectionApplyMapper.selectList(
                new LambdaQueryWrapper<BizInspectionApply>()
                        .in(BizInspectionApply::getRegistId, registIds)
                        .eq(BizInspectionApply::getApplyStatus, CdrInspApplyStatusEnum.REPORTED.getCode()));
        for (BizInspectionApply apply : insList) {
            if (apply.getRegistId() != null) {
                result.merge(apply.getRegistId(), 1, Integer::sum);
            }
        }
        List<BizLaboratoryApply> labList = bizLaboratoryApplyMapper.selectList(
                new LambdaQueryWrapper<BizLaboratoryApply>()
                        .in(BizLaboratoryApply::getRegistId, registIds)
                        .eq(BizLaboratoryApply::getApplyStatus, CdrInspApplyStatusEnum.REPORTED.getCode()));
        for (BizLaboratoryApply apply : labList) {
            if (apply.getRegistId() != null) {
                result.merge(apply.getRegistId(), 1, Integer::sum);
            }
        }
        return result;
    }

    private LambdaQueryWrapper<BizInspectionApply> inspectionWrapper(InspectionApplyPageQueryDTO query) {
        LambdaQueryWrapper<BizInspectionApply> w = new LambdaQueryWrapper<>();
        applyConditions(w, query);
        return w.orderByDesc(BizInspectionApply::getIsEmergency).orderByAsc(BizInspectionApply::getId);
    }

    private void applyConditions(LambdaQueryWrapper<BizInspectionApply> w, InspectionApplyPageQueryDTO query) {
        List<Integer> statusIn = query.getApplyStatusIn();
        List<Long> excludeIds = query.getExcludeApplyIds();
        String kw = TextUtil.trim(query.getKeyword());
        w.in(statusIn != null && !statusIn.isEmpty(), BizInspectionApply::getApplyStatus, statusIn)
                .notIn(excludeIds != null && !excludeIds.isEmpty(), BizInspectionApply::getId, excludeIds)
                .and(TextUtil.hasText(kw), x -> x.like(BizInspectionApply::getApplyNo, kw)
                        .or().like(BizInspectionApply::getPatientName, kw)
                        .or().like(BizInspectionApply::getInspectionItemName, kw))
                .eq(query.getPatientId() != null, BizInspectionApply::getPatientId, query.getPatientId())
                .eq(query.getIsEmergency() != null, BizInspectionApply::getIsEmergency, query.getIsEmergency())
                .ge(query.getStartDate() != null, BizInspectionApply::getVisitDate, query.getStartDate())
                .le(query.getEndDate() != null, BizInspectionApply::getVisitDate, query.getEndDate());
    }

    private InspectionApplyBriefVO toInspectionVo(BizInspectionApply apply) {
        if (apply == null) {
            return null;
        }
        InspectionApplyBriefVO vo = new InspectionApplyBriefVO();
        vo.setId(apply.getId());
        vo.setApplyNo(apply.getApplyNo());
        vo.setPatientId(apply.getPatientId());
        vo.setPatientNo(apply.getPatientNo());
        vo.setPatientName(apply.getPatientName());
        vo.setGender(apply.getGender());
        vo.setAge(apply.getAge());
        vo.setRegistId(apply.getRegistId());
        vo.setVisitDate(apply.getVisitDate());
        vo.setDeptId(apply.getDeptId());
        vo.setDeptName(apply.getDeptName());
        vo.setDoctorId(apply.getDoctorId());
        vo.setDoctorName(apply.getDoctorName());
        vo.setInspectionItemId(apply.getInspectionItemId());
        vo.setInspectionItemCode(apply.getInspectionItemCode());
        vo.setInspectionItemName(apply.getInspectionItemName());
        vo.setInspectionDeptId(apply.getInspectionDeptId());
        vo.setInspectionDeptName(apply.getInspectionDeptName());
        vo.setBodyPart(apply.getBodyPart());
        vo.setInspectionPurpose(apply.getInspectionPurpose());
        vo.setClinicalDiagnosis(apply.getClinicalDiagnosis());
        vo.setPrice(apply.getPrice());
        vo.setIsEmergency(apply.getIsEmergency());
        vo.setApplyStatus(apply.getApplyStatus());
        return vo;
    }

    private LaboratoryApplyBriefVO toLaboratoryVo(BizLaboratoryApply apply) {
        if (apply == null) {
            return null;
        }
        LaboratoryApplyBriefVO vo = new LaboratoryApplyBriefVO();
        vo.setId(apply.getId());
        vo.setApplyNo(apply.getApplyNo());
        vo.setPatientId(apply.getPatientId());
        vo.setPatientNo(apply.getPatientNo());
        vo.setPatientName(apply.getPatientName());
        vo.setGender(apply.getGender());
        vo.setAge(apply.getAge());
        vo.setVisitDate(apply.getVisitDate());
        vo.setDeptId(apply.getDeptId());
        vo.setDeptName(apply.getDeptName());
        vo.setDoctorId(apply.getDoctorId());
        vo.setDoctorName(apply.getDoctorName());
        vo.setLaboratoryItemId(apply.getLaboratoryItemId());
        vo.setLaboratoryItemCode(apply.getLaboratoryItemCode());
        vo.setLaboratoryItemName(apply.getLaboratoryItemName());
        vo.setLaboratoryDeptId(apply.getLaboratoryDeptId());
        vo.setLaboratoryDeptName(apply.getLaboratoryDeptName());
        vo.setSpecimenType(apply.getSpecimenType());
        vo.setPrice(apply.getPrice());
        return vo;
    }
}
