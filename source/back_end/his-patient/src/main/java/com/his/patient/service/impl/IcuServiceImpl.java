package com.his.patient.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.AdmitStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.patient.dto.*;
import com.his.patient.entity.BizIcuMonitor;
import com.his.patient.entity.BizIcuStay;
import com.his.patient.enums.IcuOutDestEnum;
import com.his.patient.enums.IcuStayStatusEnum;
import com.his.patient.mapper.BizIcuMonitorMapper;
import com.his.patient.mapper.BizIcuStayMapper;
import com.his.patient.service.IcuService;
import com.his.patient.vo.IcuVO;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * ICU 专科监护服务实现。
 *
 * <p>口径：
 * <ol>
 *   <li>入科：一次住院同时仅一条在科记录；一张 ICU 床同时仅一名在科患者。
 *       患者/来源科室/病区床位全部服务端按 admissionId、bedId 重查快照。</li>
 *   <li>床位复用床位（bed_type='ICU'），不反向改写床位的占用列，
 *       避免与转科/换床两套账互相覆盖（sql/108 设计要点 3）。</li>
 *   <li>出科：终态，须给出转出去向；转院/死亡/自动离院必须写转归说明。出科后监护记录封账禁写。</li>
 *   <li>监护记录：一条一时刻，撞 uk(stay_id, record_time) 直接拒绝；GCS 总分与液体平衡服务端回算；
 *       记录时刻不得早于入科、不得晚于当前。stay.monitor_count 每次增改后回算。</li>
 *   <li>操作人一律服务端取当前登录人；说明类文本服务端截列宽。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IcuServiceImpl extends ServiceImpl<BizIcuStayMapper, BizIcuStay> implements IcuService {

    private static final int REASON_MAX = 200;
    private static final int DIAG_MAX = 255;
    private static final int DESC_MAX = 400;

    private final BizIcuStayMapper bizIcuStayMapper;
    private final BizIcuMonitorMapper bizIcuMonitorMapper;
    private final RedisSequenceService redisSequenceService;

    private static void assertGcs(Integer gcs) {
        if (gcs != null && (gcs < 3 || gcs > 15)) {
            throw new BusinessException("GCS 总分应在 3~15 之间");
        }
    }

    private static BigDecimal balance(BigDecimal intake, BigDecimal output) {
        if (intake == null && output == null) {
            return null;
        }
        return NumUtil.orZero(intake).subtract(NumUtil.orZero(output)).setScale(1, RoundingMode.HALF_UP);
    }

    private static Integer flag(Integer value) {
        return value == null ? 0 : (value == 1 ? 1 : 0);
    }

    @Override
    public PageResult<IcuVO.StayVO> stayListPage(IcuStayQueryPageDTO query) {
        Page<IcuVO.StayVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        List<IcuVO.StayVO> records = bizIcuStayMapper.selectStayPage(page,
                TextUtil.trimToNull(query.getStayNo()), TextUtil.trimToNull(query.getPatientName()),
                query.getStartDate(), query.getEndDate(), query.getWardId(),
                query.getCareLevel(), query.getStatus());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    // 内部工具

    @Override
    public IcuVO.StayVO stayGetById(Long id) {
        IcuVO.StayVO vo = bizIcuStayMapper.selectStayById(id);
        if (vo == null) {
            throw new BusinessException("入科记录不存在或已删除");
        }
        return vo;
    }

    @Override
    public List<IcuVO.AdmissionVO> admissionsForIcu(String keyword, Integer limit) {
        int size = limit == null || limit <= 0 || limit > 200 ? 50 : limit;
        return bizIcuStayMapper.selectAdmissionCandidates(TextUtil.trimToNull(keyword), size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IcuVO.StayVO stayUpsert(IcuStayUpsertDTO dto) {
        IcuVO.AdmissionVO admission = bizIcuStayMapper.selectAdmissionSnapshot(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        if (!Objects.equals(admission.getAdmitStatus(), AdmitStatusEnum.IN_HOSPITAL.getCode())) {
            throw new BusinessException("患者已出院，不能办理 ICU 入科");
        }
        if (dto.getInTime().isAfter(TimeUtil.nowSeconds())) {
            throw new BusinessException("入科时间不能晚于当前时间");
        }
        if (admission.getAdmitTime() != null && dto.getInTime().isBefore(admission.getAdmitTime())) {
            throw new BusinessException("入科时间不能早于入院时间");
        }
        Integer careLevel = dto.getCareLevel();
        if (careLevel == null || careLevel < 1 || careLevel > 3) {
            throw new BusinessException("监护等级不合法");
        }
        assertGcs(dto.getInGcs());
        IcuVO.BedVO bed = requireIcuBed(dto.getBedId());

        BizIcuStay stay;
        if (dto.getId() == null) {
            if (bizIcuStayMapper.countActiveByAdmission(dto.getAdmissionId()) > 0) {
                throw new BusinessException("该次住院已有在科记录（一次住院同时仅一条）");
            }
            if (bizIcuStayMapper.countActiveByBed(bed.getBedId(), null) > 0) {
                throw new BusinessException("床位 " + bed.getBedNo() + " 已有在科患者，请先安排出科或换床");
            }
            stay = new BizIcuStay();
            stay.setStayNo(redisSequenceService.generateIcuStayNo());
            stay.setAdmissionId(admission.getAdmissionId());
            stay.setPatientId(admission.getPatientId());
            stay.setStatus(IcuStayStatusEnum.IN.getCode());
            stay.setMonitorCount(0);
            CurrentUser operatorUser = UserUtils.getCurrentUser();
            if (operatorUser == null) {
                throw new BusinessException("当前用户信息不存在");
            }
            stay.setInBy(operatorUser.getRealName());
        } else {
            stay = requireActiveStay(dto.getId());
            if (!Objects.equals(stay.getAdmissionId(), dto.getAdmissionId())) {
                throw new BusinessException("入科记录不允许改挂到另一次住院");
            }
            if (!Objects.equals(stay.getBedId(), bed.getBedId())
                    && bizIcuStayMapper.countActiveByBed(bed.getBedId(), stay.getId()) > 0) {
                throw new BusinessException("床位 " + bed.getBedNo() + " 已有在科患者，请换床");
            }
        }
        stay.setPatientNo(admission.getPatientNo());
        stay.setPatientName(admission.getPatientName());
        stay.setFromDeptId(admission.getDeptId());
        stay.setFromDeptName(admission.getDeptName());
        stay.setWardId(bed.getWardId());
        stay.setWardName(bed.getWardName());
        stay.setBedId(bed.getBedId());
        stay.setBedNo(bed.getBedNo());
        stay.setCareLevel(careLevel);
        stay.setInTime(TimeUtil.toSeconds(dto.getInTime()));
        stay.setInDiag(TextUtil.cutToNull(dto.getInDiag(), DIAG_MAX));
        stay.setInGcs(dto.getInGcs());
        stay.setRemark(TextUtil.cutToNull(dto.getRemark(), DIAG_MAX * 2));
        saveStay(stay);
        return stayGetById(stay.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IcuVO.StayVO stayOut(IcuStayOutDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizIcuStay stay = requireActiveStay(dto.getId());
        LocalDateTime outTime = TimeUtil.toSeconds(dto.getOutTime());
        if (outTime.isAfter(TimeUtil.nowSeconds())) {
            throw new BusinessException("出科时间不能晚于当前时间");
        }
        if (outTime.isBefore(stay.getInTime())) {
            throw new BusinessException("出科时间不能早于入科时间");
        }
        Integer dest = dto.getOutDest();
        // ③业务规则：码值合法性（"转出去向不能为空"已收口到 DTO @NotNull + @Valid）
        if (dest == null || dest < 1 || dest > 6) {
            throw new BusinessException("转出去向不合法");
        }
        // ①条件必填：只有转院/死亡/自动离院这三种去向才必填转归说明，@NotBlank 会把普通转科挡成 400
        if ((dest == IcuOutDestEnum.TRANSFER_HOSPITAL.getCode()
                || dest == IcuOutDestEnum.DEATH.getCode()
                || dest == IcuOutDestEnum.SELF_DISCHARGE.getCode())
                && !StringUtils.hasText(dto.getOutReason())) {
            throw new BusinessException("转院/死亡/自动离院必须填写转归说明");
        }
        assertGcs(dto.getOutGcs());
        stay.setStatus(IcuStayStatusEnum.OUT.getCode());
        stay.setOutTime(outTime);
        stay.setOutDest(dest);
        stay.setOutReason(TextUtil.cutToNull(dto.getOutReason(), REASON_MAX));
        stay.setOutGcs(dto.getOutGcs());
        stay.setOutBy(operatorUser.getRealName());
        if (bizIcuStayMapper.updateById(stay) <= 0) {
            throw new BusinessException("出科登记失败");
        }
        return stayGetById(stay.getId());
    }

    @Override
    public List<IcuVO.BedVO> bedBoard(Long wardId) {
        return bizIcuStayMapper.selectBedBoard(wardId);
    }

    @Override
    public PageResult<IcuVO.MonitorVO> monitorListPage(IcuMonitorQueryPageDTO query) {
        Page<IcuVO.MonitorVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        List<IcuVO.MonitorVO> records = bizIcuMonitorMapper.selectMonitorPage(page, query.getStayId(),
                query.getStartDate(), query.getEndDate());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public List<IcuVO.MonitorVO> monitorTrend(Long stayId, Integer hours) {
        requireStay(stayId);
        LocalDateTime since = hours == null || hours <= 0 ? null : TimeUtil.nowSeconds().minusHours(hours);
        return bizIcuMonitorMapper.selectMonitorTrend(stayId, since, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IcuVO.MonitorVO monitorUpsert(IcuMonitorUpsertDTO dto) {
        BizIcuStay stay = requireStay(dto.getStayId());
        if (stay.getStatus() != IcuStayStatusEnum.IN.getCode()) {
            throw new BusinessException("患者已出科，监护记录已封账，不能再登记");
        }
        LocalDateTime recordTime = TimeUtil.toSeconds(dto.getRecordTime());
        if (recordTime.isAfter(TimeUtil.nowSeconds())) {
            throw new BusinessException("记录时刻不能晚于当前时间");
        }
        if (recordTime.isBefore(stay.getInTime())) {
            throw new BusinessException("记录时刻不能早于入科时间");
        }
        if (bizIcuMonitorMapper.countAtTime(stay.getId(), recordTime, dto.getId()) > 0) {
            throw new BusinessException("该时刻已有监护记录，请核对记录时刻");
        }
        Integer gcsTotal = resolveGcsTotal(dto);
        if (dto.getSpo2() != null && (dto.getSpo2() < 0 || dto.getSpo2() > 100)) {
            throw new BusinessException("SpO2 应在 0~100 之间");
        }
        if (dto.getFio2() != null && (dto.getFio2() < 21 || dto.getFio2() > 100)) {
            throw new BusinessException("FiO2 应在 21~100 之间");
        }

        BizIcuMonitor monitor;
        if (dto.getId() == null) {
            monitor = new BizIcuMonitor();
            monitor.setStayId(stay.getId());
            CurrentUser operatorUser = UserUtils.getCurrentUser();
            if (operatorUser == null) {
                throw new BusinessException("当前用户信息不存在");
            }
            monitor.setRecorderId(operatorUser.getEmployeeId());
            monitor.setRecorderName(operatorUser.getRealName());
        } else {
            monitor = bizIcuMonitorMapper.selectById(dto.getId());
            if (monitor == null) {
                throw new BusinessException("监护记录不存在或已删除");
            }
            if (!Objects.equals(monitor.getStayId(), stay.getId())) {
                throw new BusinessException("监护记录不允许改挂到另一次入科");
            }
        }
        monitor.setRecordTime(recordTime);
        monitor.setRecordDate(recordTime.toLocalDate());
        monitor.setTemperature(dto.getTemperature());
        monitor.setPulse(dto.getPulse());
        monitor.setRespiratory(dto.getRespiratory());
        monitor.setSbp(dto.getSbp());
        monitor.setDbp(dto.getDbp());
        monitor.setSpo2(dto.getSpo2());
        monitor.setGcsEye(dto.getGcsEye());
        monitor.setGcsVerbal(dto.getGcsVerbal());
        monitor.setGcsMotor(dto.getGcsMotor());
        monitor.setGcsTotal(gcsTotal);
        monitor.setPupil(TextUtil.cutToNull(dto.getPupil(), 128));
        monitor.setCvp(dto.getCvp());
        monitor.setVentMode(dto.getVentMode());
        monitor.setFio2(dto.getFio2());
        monitor.setPeep(dto.getPeep());
        monitor.setIntakeMl(dto.getIntakeMl());
        monitor.setOutputMl(dto.getOutputMl());
        monitor.setFluidBalance(balance(dto.getIntakeMl(), dto.getOutputMl()));
        monitor.setUrineMl(dto.getUrineMl());
        monitor.setHasAirway(flag(dto.getHasAirway()));
        monitor.setHasCvc(flag(dto.getHasCvc()));
        monitor.setHasArterial(flag(dto.getHasArterial()));
        monitor.setHasCatheter(flag(dto.getHasCatheter()));
        monitor.setHasDrain(flag(dto.getHasDrain()));
        monitor.setConditionDesc(TextUtil.cutToNull(dto.getConditionDesc(), DESC_MAX));
        monitor.setHandling(TextUtil.cutToNull(dto.getHandling(), DESC_MAX));
        monitor.setRemark(TextUtil.cutToNull(dto.getRemark(), DIAG_MAX * 2));
        boolean ok = monitor.getId() == null ? bizIcuMonitorMapper.insert(monitor) > 0 : bizIcuMonitorMapper.updateById(monitor) > 0;
        if (!ok) {
            throw new BusinessException("监护记录保存失败");
        }
        refreshMonitorCount(stay.getId());
        return bizIcuMonitorMapper.selectMonitorById(monitor.getId());
    }

    @Override
    public IcuVO.StatsVO stats(LocalDate startDate, LocalDate endDate, Integer lagHours) {
        // 最长统计窗口 30 天，超出就按 end 往前推 30 天，返回体里的 startDate 回显实际口径
        LocalDate end = endDate == null ? LocalDate.now() : endDate;
        LocalDate earliest = end.minusDays(29);
        LocalDate start = startDate == null || startDate.isBefore(earliest) ? earliest : startDate;
        IcuVO.StatsVO stats = bizIcuStayMapper.selectRangeSummary(start.atStartOfDay(), end.atTime(23, 59, 59));
        if (stats == null) {
            stats = new IcuVO.StatsVO();
        }
        stats.setStartDate(start);
        stats.setEndDate(end);
        int inCount = bizIcuStayMapper.countInDept();
        int bedTotal = bizIcuMonitorMapper.countIcuBeds();
        stats.setInCount(inCount);
        stats.setBedTotal(bedTotal);
        stats.setBedUseRate(bedTotal == 0 ? null
                : BigDecimal.valueOf(inCount).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(bedTotal), 1, RoundingMode.HALF_UP));
        stats.setMonitorTotalRange(bizIcuMonitorMapper.countRange(start.atStartOfDay(), end.atTime(23, 59, 59)));
        int stays = NumUtil.orDefault(stats.getInCountRange(), 0) + NumUtil.orDefault(stats.getOutCountRange(), 0);
        stats.setMonitorsPerStay(stays == 0 ? null
                : BigDecimal.valueOf(NumUtil.orDefault(stats.getMonitorTotalRange(), 0))
                .divide(BigDecimal.valueOf(stays), 2, RoundingMode.HALF_UP));
        stats.setCareLevels(bizIcuStayMapper.selectCareLevelBoard());
        stats.setVentModes(bizIcuMonitorMapper.selectLatestVentModes());
        IcuVO.StatsVO tubes = bizIcuMonitorMapper.selectTubeSummary();
        if (tubes == null) {
            // 在科患者一条监护记录都没有时 SUM() 出全 NULL 行，MyBatis 返回 null 对象；
            // 带管人数是护理质量口径的计数，出参要 0 而不是 null（前端卡片直接渲染）
            tubes = new IcuVO.StatsVO();
        }
        stats.setAirwayCount(NumUtil.orDefault(tubes.getAirwayCount(), 0));
        stats.setCvcCount(NumUtil.orDefault(tubes.getCvcCount(), 0));
        stats.setArterialCount(NumUtil.orDefault(tubes.getArterialCount(), 0));
        stats.setCatheterCount(NumUtil.orDefault(tubes.getCatheterCount(), 0));
        stats.setDrainCount(NumUtil.orDefault(tubes.getDrainCount(), 0));
        stats.setMonitorLagCount(bizIcuStayMapper.countMonitorLag(lagHours == null || lagHours <= 0 ? 6 : lagHours));
        return stats;
    }

    private IcuVO.BedVO requireIcuBed(Long bedId) {
        IcuVO.BedVO bed = bizIcuStayMapper.selectBedSnapshot(bedId);
        if (bed == null) {
            throw new BusinessException("床位不存在或已停用");
        }
        if (!"ICU".equals(bed.getBedType())) {
            throw new BusinessException("床位 " + bed.getBedNo() + " 不是 ICU 床，不能办理入科");
        }
        if (Objects.equals(bed.getBedStatus(), 0)) {
            throw new BusinessException("床位 " + bed.getBedNo() + " 已停用，请先在床位管理中启用");
        }
        return bed;
    }

    private BizIcuStay requireStay(Long id) {
        BizIcuStay stay = bizIcuStayMapper.selectById(id);
        if (stay == null) {
            throw new BusinessException("入科记录不存在或已删除");
        }
        return stay;
    }

    private BizIcuStay requireActiveStay(Long id) {
        BizIcuStay stay = requireStay(id);
        if (stay.getStatus() != IcuStayStatusEnum.IN.getCode()) {
            throw new BusinessException("该患者已出科，记录为终态不能再修改");
        }
        return stay;
    }

    private void saveStay(BizIcuStay stay) {
        boolean ok = stay.getId() == null ? bizIcuStayMapper.insert(stay) > 0 : bizIcuStayMapper.updateById(stay) > 0;
        if (!ok) {
            throw new BusinessException("入科记录保存失败");
        }
    }

    private void refreshMonitorCount(Long stayId) {
        BizIcuStay stay = bizIcuStayMapper.selectById(stayId);
        if (stay == null) {
            return;
        }
        stay.setMonitorCount(bizIcuMonitorMapper.countByStay(stayId));
        bizIcuStayMapper.updateById(stay);
    }

    /**
     * GCS 三项要么全空（未评估），要么全填；总分由服务端求和，不接收前端传值。
     */
    private Integer resolveGcsTotal(IcuMonitorUpsertDTO dto) {
        int filled = (dto.getGcsEye() == null ? 0 : 1) + (dto.getGcsVerbal() == null ? 0 : 1) + (dto.getGcsMotor() == null ? 0 : 1);
        if (filled == 0) {
            return null;
        }
        // ①条件必填：GCS 三项要么全空（本轮未评估）要么全填，@NotNull 会堵死"未评估"这条合法路径
        if (filled < 3) {
            throw new BusinessException("GCS 需同时填写睁眼、语言、运动三项");
        }
        if (dto.getGcsEye() < 1 || dto.getGcsEye() > 4
                || dto.getGcsVerbal() < 1 || dto.getGcsVerbal() > 5
                || dto.getGcsMotor() < 1 || dto.getGcsMotor() > 6) {
            throw new BusinessException("GCS 分项超出范围（睁眼1~4、语言1~5、运动1~6）");
        }
        return dto.getGcsEye() + dto.getGcsVerbal() + dto.getGcsMotor();
    }

}
