package com.his.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.constant.DictType;
import com.his.common.enums.*;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.operation.dto.*;
import com.his.operation.entity.BizOperationApply;
import com.his.operation.entity.BizOperationCount;
import com.his.operation.entity.BizOperationSafetyCheck;
import com.his.operation.entity.SysOperationRoom;
import com.his.operation.enums.OperationAnesthesiaMethodEnum;
import com.his.operation.enums.OperationApplyStatusEnum;
import com.his.operation.mapper.BizOperationApplyMapper;
import com.his.operation.mapper.BizOperationCountMapper;
import com.his.operation.mapper.BizOperationSafetyCheckMapper;
import com.his.operation.mapper.SysOperationRoomMapper;
import com.his.operation.service.OperationApplyService;
import com.his.operation.support.AnesthesiaCalcs;
import com.his.operation.support.OperationCheckItems;
import com.his.operation.support.SafetyCheckItems;
import com.his.operation.vo.OperationApplyVO;
import com.his.operation.vo.OperationScheduleMatrixVO;
import com.his.patient.entity.BizAdmission;
import com.his.patient.entity.BizInpatientOperation;
import com.his.patient.entity.BizInpatientRecord;
import com.his.patient.entity.BizPatient;
import com.his.patient.enums.InpatientRecordTypeEnum;
import com.his.patient.service.BizPatientService;
import com.his.patient.service.InpatientRecordService;
import com.his.patient.service.InpatientService;
import com.his.patient.vo.WardVO;
import com.his.system.dto.TechAuthGateDTO;
import com.his.system.entity.CurrentUser;
import com.his.system.service.DictCacheService;
import com.his.system.service.EmployeeTechAuthService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * 住院手术闭环服务实现（P4.3）。
 *
 * <p>本类固化了这些<b>至少踩过一次或一定会被追问</b>的点：
 *
 * <ol>
 *   <li><b>未排期不可核对 / 未核对不可完成</b>：与"医嘱未校对不可执行"同源。
 *       手术安全核查的价值就在于"在切皮之前核过"，术后补记录是伪造。</li>
 *   <li><b>术前核对完成后不可取消</b>：患者已进入手术区流程，停台是另一件事。</li>
 *   <li><b>排台冲突必须拦（同手术间时间区间重叠）</b>：这是"排台"这个动作唯一真正的价值。
 *       端点相接（上一台 10:00 结束、下一台 10:00 开始）不算冲突。</li>
 *   <li><b>主要手术唯一</b>：首页主要手术只能 1 条，与"主要诊断必须且只能 1 条"同口径。</li>
 *   <li><b>完成才回写，且一个事务里回写两份文书</b>：病案首页手术明细 + record_type=5 手术记录。
 *       回写失败整笔回滚 —— 不允许"状态已完成、首页和病历里查不到"。</li>
 *   <li><b>首页记的是"实际做的"手术，不是拟施</b>：高编高套最常见的手法就是
 *       "只做了探查却编切除术"，首页写拟施等于帮忙造假。</li>
 *   <li><b>系统回写行不靠 remark 文本前缀区分</b>：靠病案首页手术明细的申请ID。
 *       文本会被改、会被人抄，列不会。</li>
 *   <li><b>时间一律截到秒</b>：库表 DATETIME(0) 会四舍五入，不截就"写进去的 ≠ 读回来的"。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationApplyServiceImpl extends ServiceImpl<BizOperationApplyMapper, BizOperationApply> implements OperationApplyService {
    /**
     * 术前核对完成后多久没结束算"卡住"（查询时算，不落状态列）
     */
    private static final long STALLED_HOURS = 24;

    private final BizOperationApplyMapper bizOperationApplyMapper;

    private final BizPatientService bizPatientService;

    private final InpatientService inpatientService;

    private final InpatientRecordService inpatientRecordService;

    private final EmployeeTechAuthService employeeTechAuthService;

    private final BizOperationCountMapper bizOperationCountMapper;

    private final SysOperationRoomMapper sysOperationRoomMapper;

    private final BizOperationSafetyCheckMapper bizOperationSafetyCheckMapper;

    private final DictCacheService dictCacheService;

    // 查询
    private static String textOr(String value, String fallback) {
        return TextUtil.hasText(value) ? value : fallback;
    }

    /**
     * 时间统一截到秒，保证「写进去的 = 读回来的」（库表是 DATETIME(0)，MySQL 会四舍五入）
     */
    /**
     * 下界宽松解析：{@code yyyy-MM-dd} → 当天 00:00:00；带时分秒则原样使用（含）。
     */
    private static String normalizeFrom(String raw) {
        Parsed p = parse(raw);
        return p == null ? null : p.from;
    }

    /**
     * 上界宽松解析：{@code yyyy-MM-dd} → <b>次日</b> 00:00:00（不含，这样能覆盖当天最后一秒）；
     * 带时分秒则原样使用（不含）。
     */
    private static String normalizeTo(String raw) {
        Parsed p = parse(raw);
        return p == null ? null : p.to;
    }

    private static Parsed parse(String raw) {
        if (!TextUtil.hasText(raw)) {
            return null;
        }
        String s = raw.trim();
        try {
            if (s.length() == 10) {
                LocalDate d = LocalDate.parse(s);
                return new Parsed(TimeUtil.dayStart(d).format(DateFormats.DATETIME),
                        TimeUtil.dayStart(d.plusDays(1)).format(DateFormats.DATETIME));
            }
            LocalDateTime t = LocalDateTime.parse(s, DateFormats.DATETIME);
            return new Parsed(t.format(DateFormats.DATETIME), t.format(DateFormats.DATETIME));
        } catch (DateTimeParseException e) {
            // 明确报格式问题，不静默忽略、也不让它变成 500
            throw new BusinessException("时间格式不正确：" + raw + "（应为 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）");
        }
    }

    @Override
    public IPage<OperationApplyVO> listPage(OperationApplyQueryPageDTO query) {
        if (query == null) {
            query = new OperationApplyQueryPageDTO();
        }
        query.setPlannedDateFrom(normalizeFrom(query.getPlannedDateFrom()));
        query.setPlannedDateTo(normalizeTo(query.getPlannedDateTo()));
        IPage<OperationApplyVO> page = bizOperationApplyMapper.selectApplyPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query);
        page.getRecords().forEach(this::decorate);
        return page;
    }

    @Override
    public OperationApplyVO getDetailById(Long applyId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (applyId == null) {
            throw new BusinessException("手术申请单ID不能为空");
        }
        OperationApplyVO vo = bizOperationApplyMapper.selectApplyById(applyId);
        if (vo == null) {
            throw new BusinessException("手术申请单不存在");
        }
        decorate(vo);
        return vo;
    }

    // 一、申请（待排期）

    @Override
    public List<OperationApplyVO> listByAdmission(Long admissionId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (admissionId == null) {
            throw new BusinessException("入院ID不能为空");
        }
        List<OperationApplyVO> list = bizOperationApplyMapper.selectByAdmission(admissionId);
        list.forEach(this::decorate);
        return list;
    }

    // 二、排台（待排期 → 已排期；已排期可改期）

    @Override
    public long countUnfinished(Long admissionId) {
        return bizOperationApplyMapper.countUnfinished(admissionId);
    }

    // 三、术前核对（已排期 → 术前核对完成）

    @Override
    public List<String> roomList() {
        // 手术间主数据（启用）优先，再并入历史 distinct —— 老申请单用过的自由文本
        // 不能丢（否则改排一台历史手术时下拉里找不到它原来的手术间）。
        List<String> rooms = new ArrayList<>();
        sysOperationRoomMapper.selectList(new LambdaQueryWrapper<SysOperationRoom>()
                        .eq(SysOperationRoom::getStatus, 1)
                        .orderByAsc(SysOperationRoom::getSortOrder)
                        .orderByAsc(SysOperationRoom::getRoomCode))
                .forEach(r -> rooms.add(r.getRoomName()));
        for (String legacy : bizOperationApplyMapper.selectRoomList()) {
            if (!rooms.contains(legacy)) {
                rooms.add(legacy);
            }
        }
        return rooms;
    }

    // 四、完成（术前核对完成 → 已完成；回写首页明细 + 手术记录病历）

    @Override
    public OperationScheduleMatrixVO scheduleMatrix(String date) {
        LocalDate day;
        try {
            day = TextUtil.hasText(date) ? LocalDate.parse(date.trim()) : LocalDate.now();
        } catch (DateTimeParseException e) {
            throw new BusinessException("日期格式不正确：" + date + "（应为 yyyy-MM-dd）");
        }

        OperationScheduleMatrixVO matrix = new OperationScheduleMatrixVO();
        matrix.setDate(day.format(DateTimeFormatter.ISO_LOCAL_DATE));

        // 列 = 启用中的手术间主数据（顺序即总表列序）
        List<SysOperationRoom> enabledRooms = sysOperationRoomMapper.selectList(
                new LambdaQueryWrapper<SysOperationRoom>()
                        .eq(SysOperationRoom::getStatus, 1)
                        .orderByAsc(SysOperationRoom::getSortOrder)
                        .orderByAsc(SysOperationRoom::getRoomCode));

        LocalDateTime from = TimeUtil.dayStart(day);
        LocalDateTime to = TimeUtil.dayStart(day.plusDays(1));
        List<OperationApplyVO> scheduled = bizOperationApplyMapper.selectScheduledBetween(from, to);
        List<OperationApplyVO> pending = bizOperationApplyMapper.selectUnscheduled();
        scheduled.forEach(this::decorate);
        pending.forEach(this::decorate);

        // 三方核查轮数：只在这张表里批量回填一次（列表接口不查，避免 N+1）
        Map<Long, Long> phaseCounts = phaseCountsOf(scheduled, pending);
        scheduled.forEach(vo -> vo.setSafetyCheckPhases(
                phaseCounts.getOrDefault(vo.getId(), 0L).intValue()));
        pending.forEach(vo -> vo.setSafetyCheckPhases(0));

        // 落桶：手术间名与主数据对上的进对应列，对不上的进 others —— 一台都不能丢
        Map<String, List<OperationApplyVO>> byRoom = new LinkedHashMap<>();
        for (OperationApplyVO vo : scheduled) {
            byRoom.computeIfAbsent(textOr(vo.getOperationRoom(), "未指定手术间"),
                    k -> new ArrayList<>()).add(vo);
        }
        List<OperationScheduleMatrixVO.RoomColumn> columns = new ArrayList<>();
        for (SysOperationRoom room : enabledRooms) {
            OperationScheduleMatrixVO.RoomColumn col = new OperationScheduleMatrixVO.RoomColumn();
            col.setRoomId(room.getId());
            col.setRoomCode(room.getRoomCode());
            col.setRoomName(room.getRoomName());
            col.setLocation(room.getLocation());
            List<OperationApplyVO> ops = byRoom.remove(room.getRoomName());
            col.setOps(ops == null ? new ArrayList<>() : ops);
            columns.add(col);
        }
        List<OperationScheduleMatrixVO.RoomColumn> others = new ArrayList<>();
        byRoom.forEach((name, ops) -> {
            OperationScheduleMatrixVO.RoomColumn col = new OperationScheduleMatrixVO.RoomColumn();
            col.setRoomName(name);
            col.setOps(ops);
            others.add(col);
        });
        others.sort(Comparator.comparing(OperationScheduleMatrixVO.RoomColumn::getRoomName));

        matrix.setRooms(columns);
        matrix.setOthers(others);
        matrix.setUnscheduled(pending);
        matrix.setScheduledCount(scheduled.size());
        matrix.setEmergencyCount((int) scheduled.stream()
                .filter(vo -> Objects.equals(1, vo.getIsEmergency())).count());
        return matrix;
    }

    // 五、取消（仅待排期 / 已排期 → 已取消）

    /**
     * 批量取多张申请单的已签核查轮数（一次 IN 查询；唯一键 uk_check_apply_phase 保证一申请一时段一行，行数即轮数）
     */
    private Map<Long, Long> phaseCountsOf(List<OperationApplyVO>... groups) {
        List<Long> ids = new ArrayList<>();
        for (List<OperationApplyVO> group : groups) {
            group.forEach(vo -> {
                if (vo.getId() != null) {
                    ids.add(vo.getId());
                }
            });
        }
        Map<Long, Long> result = new HashMap<>();
        if (ids.isEmpty()) {
            return result;
        }
        List<BizOperationSafetyCheck> rows = bizOperationSafetyCheckMapper.selectList(
                new LambdaQueryWrapper<BizOperationSafetyCheck>()
                        .select(BizOperationSafetyCheck::getApplyId)
                        .in(BizOperationSafetyCheck::getApplyId, ids));
        for (BizOperationSafetyCheck row : rows) {
            result.merge(row.getApplyId(), 1L, Long::sum);
        }
        return result;
    }

    // 回写：手术记录病历

    @Override
    public List<OperationApplyVO.CheckItem> checkItems() {
        List<OperationApplyVO.CheckItem> list = new ArrayList<>();
        OperationCheckItems.all().forEach((code, label) -> {
            OperationApplyVO.CheckItem item = new OperationApplyVO.CheckItem();
            item.setCode(code);
            item.setLabel(label);
            item.setRequired(OperationCheckItems.REQUIRED.contains(code));
            list.add(item);
        });
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String save(OperationApplyUpsertDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizAdmission admission = inpatientService.getAdmissionById(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        if (!Objects.equals(AdmitStatusEnum.IN_HOSPITAL.getCode(), admission.getAdmitStatus())) {
            throw new BusinessException("该患者当前不是「在院」状态，不能申请手术（已出院的住院不能开手术单）");
        }
        BizPatient patient = bizPatientService.getById(admission.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }

        int isMain = dto.getIsMain() == null ? 1 : dto.getIsMain();
        boolean create = dto.getId() == null;

        BizOperationApply entity;
        if (create) {
            entity = new BizOperationApply();
            entity.setAdmissionId(dto.getAdmissionId());
            entity.setAdmissionNo(admission.getAdmissionNo());
            entity.setPatientId(admission.getPatientId());
            entity.setPatientName(patient.getPatientName());
            entity.setGender(patient.getGender());
            entity.setAge(patient.getAge());
            entity.setApplyDeptId(admission.getDeptId());
            entity.setApplyDeptName(deptNameOf(admission.getDeptId()));
            entity.setApplyWardName(wardNameOf(admission.getWardId()));
            entity.setApplyBedNo(bedNoOf(admission.getBedId()));
            entity.setApplyDoctorId(operatorUser.getEmployeeId());
            entity.setApplyDoctorName(operatorUser.getRealName());
            entity.setApplyTime(TimeUtil.nowSeconds());
            entity.setApplyNo(nextApplyNo());
            entity.setOperationStatus(OperationApplyStatusEnum.PENDING_SCHEDULE.getCode());
        } else {
            entity = mustGet(dto.getId());
            if (!Objects.equals(OperationApplyStatusEnum.PENDING_SCHEDULE.getCode(), entity.getOperationStatus())) {
                throw new BusinessException("手术单 " + entity.getApplyNo() + " 当前状态为「"
                        + OperationApplyStatusEnum.labelOrUnknown(entity.getOperationStatus())
                        + "」，只有「待排期」可以修改申请内容（排台后术式已对外承诺，改请先取消或走停手术）");
            }
            if (!Objects.equals(entity.getAdmissionId(), dto.getAdmissionId())) {
                throw new BusinessException("不允许把手术单改挂到另一次住院上");
            }
        }

        // 重复申请：同一住院 + 同一拟施术式，不允许并存两条未完成申请（四核对的"重复"）
        if (bizOperationApplyMapper.countUnfinishedSameName(dto.getAdmissionId(), dto.getPlannedOperationName()) > 0) {
            throw new BusinessException("该住院已有一条未完成的「" + dto.getPlannedOperationName()
                    + "」手术申请，请先完成或取消后再发起（同一台手术申请两次属于重复）");
        }
        // 主要手术唯一：同一次住院只能有一条主要手术申请
        if (isMain == 1
                && bizOperationApplyMapper.countMainOperation(dto.getAdmissionId(), entity.getId()) > 0) {
            throw new BusinessException("该住院已有一条「主要手术」申请，同一次住院只允许一条"
                    + "（首页主要手术只能有 1 条，与本条并存会导致首页出现两条主手术）");
        }

        entity.setPlannedOperationCode(dto.getPlannedOperationCode());
        entity.setPlannedOperationName(dto.getPlannedOperationName());
        entity.setOperationLevel(dto.getOperationLevel());
        entity.setIncisionLevel(dto.getIncisionLevel());
        entity.setAnesthesiaType(dto.getAnesthesiaType());
        entity.setPreopDiagnosis(dto.getPreopDiagnosis());
        entity.setOperationReason(dto.getOperationReason());
        entity.setIsEmergency(dto.getIsEmergency() == null ? 0 : dto.getIsEmergency());
        entity.setIsMain(isMain);
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }

        if (create) {
            bizOperationApplyMapper.insert(entity);
        } else {
            bizOperationApplyMapper.updateById(entity);
        }
        // 准入闸（sql/155）：申请人可以替团队开单，但本人必须是「有手术资质」的人 ——
        // 药师、管理员、纯门诊医师拿着账号不该能发起手术申请。级别闸在排台定术者时判。
        gateTechAuth(entity.getApplyDoctorId(), TechAuthCategoryEnum.SURGERY.getCode(), 1,
                entity, "手术申请人", null);
        log.info("{}手术申请 applyNo={} admissionId={} 术式={} 急诊={} 主要={} 申请人={}",
                create ? "发起" : "修改", entity.getApplyNo(), entity.getAdmissionId(),
                entity.getPlannedOperationName(), entity.getIsEmergency(), entity.getIsMain(), operatorUser.getRealName());
        return entity.getApplyNo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void schedule(OperationScheduleDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        LocalDateTime start = TimeUtil.toSeconds(dto.getPlannedStartTime());
        LocalDateTime end = TimeUtil.toSeconds(dto.getPlannedEndTime());
        // D-业务规则：时间先后关系，DTO 注解无法表达，保留
        if (!end.isAfter(start)) {
            throw new BusinessException("计划结束时间必须晚于开始时间");
        }

        BizOperationApply entity = mustGet(dto.getApplyId());
        if (Objects.equals(OperationApplyStatusEnum.FINISHED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo() + " 已完成，不能改排台信息");
        }
        if (Objects.equals(OperationApplyStatusEnum.CANCELLED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo() + " 已取消，不能排台");
        }
        if (Objects.equals(OperationApplyStatusEnum.PREOP_CHECKED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 已完成术前核对，改时段需要重新核对，请先与手术室确认后再停台重排");
        }

        // 排台冲突：同手术间、时间区间重叠、在途/已完成的手术
        List<BizOperationApply> conflicts = bizOperationApplyMapper.selectRoomConflicts(
                dto.getOperationRoom(), start, end, entity.getId());
        if (!conflicts.isEmpty()) {
            BizOperationApply c = conflicts.get(0);
            throw new BusinessException("「" + dto.getOperationRoom() + "」在 "
                    + c.getPlannedStartTime().format(DateFormats.TIME_MINUTE) + "~" + c.getPlannedEndTime().format(DateFormats.TIME_MINUTE)
                    + " 已被 " + c.getApplyNo() + "（" + c.getPatientName() + " "
                    + c.getPlannedOperationName() + "）占用，请换手术间或换时段");
        }

        String surgeonName = employeeNameOf(dto.getSurgeonId());
        entity.setOperationRoom(dto.getOperationRoom());
        entity.setPlannedStartTime(start);
        entity.setPlannedEndTime(end);
        entity.setSurgeonId(dto.getSurgeonId());
        entity.setSurgeonName(surgeonName);
        entity.setAssistantName(dto.getAssistantName());
        entity.setAnesthetistId(dto.getAnesthetistId());
        entity.setAnesthetistName(employeeNameOf(dto.getAnesthetistId()));
        entity.setScheduleDoctorId(operatorUser.getEmployeeId());
        entity.setScheduleDoctorName(operatorUser.getRealName());
        entity.setScheduleTime(TimeUtil.nowSeconds());
        entity.setScheduleRemark(dto.getScheduleRemark());
        entity.setOperationStatus(OperationApplyStatusEnum.SCHEDULED.getCode());
        bizOperationApplyMapper.updateById(entity);
        // 分级授权闸（sql/155）：排台是"这台手术由谁来做"的唯一事实来源，所以级别闸落在这里。
        // 主刀按手术级别要求「手术类」授权，麻醉医师按同级要求「麻醉类」授权。
        gateTechAuth(dto.getSurgeonId(), TechAuthCategoryEnum.SURGERY.getCode(),
                entity.getOperationLevel(), entity, "主刀医师", surgeonName);
        if (dto.getAnesthetistId() != null) {
            gateTechAuth(dto.getAnesthetistId(), TechAuthCategoryEnum.ANESTHESIA.getCode(),
                    entity.getOperationLevel(), entity, "麻醉医师", entity.getAnesthetistName());
        }
        log.info("排台 applyNo={} 手术间={} {}~{} 主刀={} 排台人={}",
                entity.getApplyNo(), dto.getOperationRoom(),
                start.format(DateFormats.DATETIME), end.format(DateFormats.DATETIME), surgeonName, operatorUser.getRealName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void preopCheck(OperationPreopCheckDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizOperationApply entity = mustGet(dto.getApplyId());
        if (Objects.equals(OperationApplyStatusEnum.PENDING_SCHEDULE.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 尚未排台，不能做术前核对（手术间/时段/主刀都还没定，核对的是一个不存在的手术）");
        }
        if (!Objects.equals(OperationApplyStatusEnum.SCHEDULED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo() + " 当前状态为「"
                    + OperationApplyStatusEnum.labelOrUnknown(entity.getOperationStatus())
                    + "」，只有「已排期」可以做术前核对");
        }

        Set<Integer> codes;
        try {
            codes = OperationCheckItems.parse(dto.getCheckItems());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        List<String> missing = OperationCheckItems.missingRequired(codes);
        if (!missing.isEmpty()) {
            throw new BusinessException("术前核对必核项未完成：" + String.join("；", missing)
                    + "。这 4 项少核任何一项，术前核对就不成立");
        }

        entity.setPreopCheckItems(OperationCheckItems.serialize(codes));
        entity.setPreopNote(dto.getPreopNote());
        entity.setPreopCheckDoctorId(operatorUser.getEmployeeId());
        entity.setPreopCheckDoctorName(operatorUser.getRealName());
        entity.setPreopCheckTime(TimeUtil.nowSeconds());
        entity.setOperationStatus(OperationApplyStatusEnum.PREOP_CHECKED.getCode());
        if (TextUtil.hasText(dto.getRemark())) {
            entity.setRemark(dto.getRemark());
        }
        bizOperationApplyMapper.updateById(entity);
        log.info("术前核对完成 applyNo={} 核对项={} 异常说明={} 核对人={}",
                entity.getApplyNo(), entity.getPreopCheckItems(),
                TextUtil.hasText(dto.getPreopNote()) ? dto.getPreopNote() : "无", operatorUser.getRealName());
    }

    // 展示态

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(OperationFinishDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizOperationApply entity = mustGet(dto.getApplyId());
        if (Objects.equals(OperationApplyStatusEnum.CANCELLED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo() + " 已取消，不能完成");
        }
        if (Objects.equals(OperationApplyStatusEnum.FINISHED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo() + " 已完成，不能重复回写"
                    + "（重复执行会产生第二份手术记录与第二条首页手术明细）");
        }
        if (!Objects.equals(OperationApplyStatusEnum.PREOP_CHECKED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo() + " 当前状态为「"
                    + OperationApplyStatusEnum.labelOrUnknown(entity.getOperationStatus())
                    + "」，未完成术前核对不能登记完成（术后补一条核对记录属于伪造，必须先把核对做完）");
        }

        LocalDateTime start = TimeUtil.toSeconds(dto.getOperationStartTime());
        LocalDateTime end = TimeUtil.toSeconds(dto.getOperationEndTime());
        // D-业务规则：时间先后关系，DTO 注解无法表达，保留
        if (!end.isAfter(start)) {
            throw new BusinessException("实际结束时间必须晚于开始时间");
        }

        BizAdmission admission = inpatientService.getAdmissionById(entity.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在，无法回写（admissionId=" + entity.getAdmissionId() + "）");
        }
        if (!Objects.equals(AdmitStatusEnum.IN_HOSPITAL.getCode(), admission.getAdmitStatus())) {
            throw new BusinessException("该患者已出院，不能再登记手术完成（手术是住院期间发生的事件；"
                    + "出院后发现漏登记，请走病案质控缺陷流程补记，而不是直接改已结算的住院）");
        }

        // 归档闸门：已归档的病案首页不能再加手术明细（同"归档后病历不可修改"）
        if (inpatientService.isSummaryArchived(entity.getAdmissionId())) {
            throw new BusinessException("该住院的病案首页已归档，不能再回写手术明细（归档数据不可改）");
        }

        // 器械清点闸门（G15）：只要这台手术建过清点单，就必须三轮走完且对得上，
        // 否则不允许登记手术完成。清点单本身是可选登记的 —— 没建单不拦，
        // 建了单却对不上是最危险的状态（清点对数正是"异物遗留"唯一能在关闭体腔前发现的机制）。
        guardCount(entity);

        // 三方安全核查闸门（P134.2）：同清点的口径 —— 签过就必须签满三轮，
        // 一轮都没签不拦（核查单目前允许不建，学习阶段先把"建了却没签完"这种最危险状态拦住）。
        guardSafetyCheck(entity);

        LocalDateTime now = TimeUtil.nowSeconds();
        String actualName = dto.getActualOperationName().trim();
        String basis = buildOperationBasis(dto);

        BizInpatientOperation op = new BizInpatientOperation();
        op.setAdmissionId(entity.getAdmissionId());
        op.setApplyId(entity.getId());
        op.setIsMain(entity.getIsMain());
        op.setOperationCode(TextUtil.hasText(dto.getActualOperationCode())
                ? dto.getActualOperationCode() : entity.getPlannedOperationCode());
        op.setOperationName(actualName);
        op.setOperationDate(start);
        op.setOperationLevel(entity.getOperationLevel());
        op.setIncisionLevel(entity.getIncisionLevel());
        op.setAnesthesiaType(entity.getAnesthesiaType());
        op.setSurgeonId(entity.getSurgeonId());
        op.setSurgeonName(entity.getSurgeonName());
        op.setAssistantName(entity.getAssistantName());
        op.setOperationBasis(basis);
        op.setRemark("系统回写：手术申请单号 " + entity.getApplyNo()
                + "，主刀 " + textOr(entity.getSurgeonName(), "未指定")
                + "，麻醉方式 " + OperationAnesthesiaMethodEnum.getText(entity.getAnesthesiaType()));
        // 首页明细的重复闸、序号重排、is_surgery 置位都在首页写入方里做（它才是这张表的所有者）
        op = inpatientService.appendSurgeryOperation(op);

        // ② 回写手术记录病历（record_type=5）
        BizInpatientRecord record = writeBackRecord(entity, admission, dto, start, end, basis, now);

        entity.setActualOperationCode(dto.getActualOperationCode());
        entity.setActualOperationName(actualName);
        entity.setOperationStartTime(start);
        entity.setOperationEndTime(end);
        entity.setBloodLoss(dto.getBloodLoss());
        entity.setIntraopFindings(dto.getIntraopFindings());
        entity.setIntraopProcedure(dto.getIntraopProcedure());
        entity.setPostopNote(dto.getPostopNote());
        entity.setSpecimenSent(dto.getSpecimenSent());
        entity.setFinishDoctorId(operatorUser.getEmployeeId());
        entity.setFinishDoctorName(operatorUser.getRealName());
        entity.setFinishTime(now);
        entity.setOperationId(op.getId());
        entity.setRecordId(record.getId());
        entity.setOperationStatus(OperationApplyStatusEnum.FINISHED.getCode());
        if (TextUtil.hasText(dto.getRemark())) {
            entity.setRemark(dto.getRemark());
        }
        bizOperationApplyMapper.updateById(entity);

        log.info("手术完成 applyNo={} 术式={} {}~{} 首页明细ID={} 病历号={} 录入人={}",
                entity.getApplyNo(), actualName, start.format(DateFormats.DATETIME), end.format(DateFormats.DATETIME),
                op.getId(), record.getRecordNo(), operatorUser.getRealName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(OperationCancelDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizOperationApply entity = mustGet(dto.getApplyId());
        if (Objects.equals(OperationApplyStatusEnum.CANCELLED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo() + " 已取消，不能重复取消");
        }
        if (Objects.equals(OperationApplyStatusEnum.FINISHED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 已完成，不能取消；已完成的手术已经写进首页和病历，取消它就是销毁证据");
        }
        if (Objects.equals(OperationApplyStatusEnum.PREOP_CHECKED.getCode(), entity.getOperationStatus())) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 已完成术前核对，不能取消（患者已进入手术区流程）；要停台请由手术室登记停手术并写明原因");
        }
        entity.setOperationStatus(OperationApplyStatusEnum.CANCELLED.getCode());
        entity.setCancelReason(dto.getCancelReason());
        entity.setCancelDoctorId(operatorUser.getEmployeeId());
        entity.setCancelDoctorName(operatorUser.getRealName());
        entity.setCancelTime(TimeUtil.nowSeconds());
        bizOperationApplyMapper.updateById(entity);
        log.info("取消手术 applyNo={} 原因={} 操作人={}",
                entity.getApplyNo(), dto.getCancelReason(), operatorUser.getRealName());
    }

    // 工具

    /**
     * 把这次手术回写成一份住院病历（record_type=5 手术记录，状态直接「已提交」）。
     *
     * <p>签名的医生是<b>主刀医师</b>，不是录入人 —— 手术记录的责任人是术者，
     * 用"谁点的按钮"当签名，会让病案首页的医师签名与手术记录打架。
     *
     * <p>结构化要素按 {@code RecordStructuredFields.OPERATION_ELEMENTS} 那三项写：
     * 术前诊断（diagnosis_name）、手术经过（course_note）、来源申请单与术者（remark）。
     * 这三项必然同时写下，所以系统回写的手术记录结构化率是 100% —— 这是事实，不是凑分。
     */
    private BizInpatientRecord writeBackRecord(BizOperationApply entity, BizAdmission admission,
                                               OperationFinishDTO dto,
                                               LocalDateTime start, LocalDateTime end,
                                               String basis, LocalDateTime now) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizPatient patient = bizPatientService.getById(admission.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在，无法回写手术记录");
        }

        String deptName = deptNameOf(admission.getDeptId());
        String wardName = wardNameOf(admission.getWardId());
        String bedNo = bedNoOf(admission.getBedId());

        StringBuilder course = new StringBuilder();
        course.append("手术名称：").append(dto.getActualOperationName()).append('\n');
        course.append("手术时间：").append(start.format(DateFormats.DATETIME)).append(" ~ ").append(end.format(DateFormats.DATETIME))
                .append("（").append(AnesthesiaCalcs.durationText(
                        Duration.between(start, end).toMinutes())).append("）\n");
        course.append("术中所见：").append(dto.getIntraopFindings()).append('\n');
        course.append("手术经过：").append(dto.getIntraopProcedure()).append('\n');
        course.append("术后处理：").append(dto.getPostopNote());
        if (dto.getBloodLoss() != null) {
            course.append('\n').append("术中出血量：").append(dto.getBloodLoss()).append(" ml");
        }
        if (TextUtil.hasText(dto.getSpecimenSent())) {
            course.append('\n').append("标本送检：").append(dto.getSpecimenSent());
        }
        if (TextUtil.hasText(entity.getPreopCheckItems())) {
            course.append('\n').append("术前核对：")
                    .append(OperationCheckItems.summaryText(entity.getPreopCheckItems()));
        }

        BizInpatientRecord record = new BizInpatientRecord();
        record.setAdmissionId(admission.getAdmissionId());
        record.setPatientId(admission.getPatientId());
        record.setPatientNo(patient.getPatientNo());
        record.setPatientName(patient.getPatientName());
        record.setGender(patient.getGender());
        record.setAge(patient.getAge());
        record.setAgeUnit(1);
        record.setDeptId(admission.getDeptId());
        record.setDeptName(deptName);
        record.setWardId(admission.getWardId());
        record.setWardName(wardName);
        record.setBedNo(bedNo);
        record.setRecordType(InpatientRecordTypeEnum.OPERATION_RECORD.getCode());
        record.setRecordTitle("手术记录");
        record.setRecordTime(end);
        // 术前诊断 → diagnosisName 列（手术记录的结构化"术前诊断"要素取这一列）
        record.setDiagnosisName(entity.getPreopDiagnosis());
        record.setCourseNote(course.toString());
        record.setRemark("系统回写：手术申请单号 " + entity.getApplyNo()
                + "，主刀 " + textOr(entity.getSurgeonName(), "未指定")
                + "，麻醉方式 " + OperationAnesthesiaMethodEnum.getText(entity.getAnesthesiaType())
                + "，手术级别 " + dictCacheService.getDicDataLabel(DictType.OPERATION_LEVEL, entity.getOperationLevel())
                + "，切口等级 " + dictCacheService.getDicDataLabel(DictType.INCISION_LEVEL, entity.getIncisionLevel()));
        record.setRecordStatus(RecordStatusEnum.SUBMITTED.getCode());
        // 签名 = 主刀医师；主刀缺失才回落到录入人（宁可记"谁录的"，也不留空签名）
        record.setDoctorId(entity.getSurgeonId() != null ? entity.getSurgeonId() : operatorUser.getEmployeeId());
        record.setDoctorName(TextUtil.hasText(entity.getSurgeonName())
                ? entity.getSurgeonName() : operatorUser.getRealName());
        record.setSubmitTime(now);
        // 病历号取号与落库归病历文书的写入方（手术侧只负责把这台手术写成文书内容）
        return inpatientRecordService.appendClosedLoopRecord(record);
    }

    /**
     * 器械清点闸门。
     *
     * <p>为什么放在 finish() 里而不是只在清点单上标个状态：
     * "手术做完了"是这台手术对外生效的那一刻，物没对数就让它生效，
     * 等于把唯一能在关腔前拦住的机会让给事后追溯。
     */
    private void guardCount(BizOperationApply entity) {
        long sheets = bizOperationCountMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizOperationCount>()
                        .eq(BizOperationCount::getApplyId, entity.getId()));
        if (sheets == 0) {
            return;
        }
        if (bizOperationCountMapper.countDiscrepancy(entity.getId()) > 0) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 的器械清点存在未处理的差异，不能登记手术完成 —— "
                    + "先把差异查清并在清点单上写明处理结果（这一条是异物遗留唯一能在关腔前发现的机制）");
        }
        if (bizOperationCountMapper.countUnfinished(entity.getId()) > 0) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 的器械清点尚未走完三轮（术前 → 关体前 → 关体后），不能登记手术完成");
        }
    }

    /**
     * 三方安全核查闸门（P134.2）。
     *
     * <p>与 {@code guardCount} 同一条理由："手术完成"是这台手术对外生效的那一刻；
     * 核查签了两轮就登记完成，意味着麻醉实施前/术后离室那一次核对根本没做，
     * 而单子上却留着看起来走过流程的签名 —— 这比没签更糟。
     */
    private void guardSafetyCheck(BizOperationApply entity) {
        long phases = bizOperationSafetyCheckMapper.countPhases(entity.getId());
        if (phases > 0 && phases < SafetyCheckItems.ALL_PHASES.size()) {
            throw new BusinessException("手术单 " + entity.getApplyNo()
                    + " 的安全核查只签了 " + phases + " 轮（共 3 轮：麻醉实施前 → 手术开始前 → 患者离开手术室前），"
                    + "签过就要签满，否则不能登记手术完成");
        }
    }

    /**
     * 首页手术明细的"手术依据"：四核对里"编码有没有病历支持"的那一段。
     */
    private String buildOperationBasis(OperationFinishDTO dto) {
        String basis = "手术记录：术中所见 "
                + dto.getIntraopFindings() + "；手术经过 " + dto.getIntraopProcedure();
        return basis.length() > 490 ? basis.substring(0, 490) + "…" : basis;
    }

    private void decorate(OperationApplyVO vo) {
        vo.setOperationStatusText(OperationApplyStatusEnum.getText(vo.getOperationStatus()));
        vo.setOperationLevelText(dictCacheService.getDicDataLabel(DictType.OPERATION_LEVEL, vo.getOperationLevel()));
        vo.setIncisionLevelText(dictCacheService.getDicDataLabel(DictType.INCISION_LEVEL, vo.getIncisionLevel()));
        vo.setAnesthesiaTypeText(OperationAnesthesiaMethodEnum.getText(vo.getAnesthesiaType()));
        vo.setIsEmergencyText(YesOrNoEnum.getText(vo.getIsEmergency()));
        vo.setIsMainText(vo.getIsMain() == null ? "—" : (vo.getIsMain() == 1 ? "主要手术" : "次要手术"));
        vo.setGenderText(SysGenderEnum.getText(vo.getGender()));
        vo.setAdmitStatusText(AdmitStatusEnum.getText(vo.getAdmitStatus()));
        vo.setPreopCheckItemsText(OperationCheckItems.summaryText(vo.getPreopCheckItems()));
        vo.setCheckItemOptions(checkItems());

        boolean pending = Objects.equals(OperationApplyStatusEnum.PENDING_SCHEDULE.getCode(), vo.getOperationStatus());
        boolean scheduled = Objects.equals(OperationApplyStatusEnum.SCHEDULED.getCode(), vo.getOperationStatus());
        boolean checked = Objects.equals(OperationApplyStatusEnum.PREOP_CHECKED.getCode(), vo.getOperationStatus());
        boolean finished = Objects.equals(OperationApplyStatusEnum.FINISHED.getCode(), vo.getOperationStatus());
        boolean cancelled = Objects.equals(OperationApplyStatusEnum.CANCELLED.getCode(), vo.getOperationStatus());

        vo.setCanEdit(pending);
        vo.setCanSchedule(pending || scheduled);
        vo.setCanPreopCheck(scheduled);
        vo.setCanFinish(checked);
        vo.setCanCancel(pending || scheduled);

        if (vo.getPlannedStartTime() != null && vo.getPlannedEndTime() != null) {
            vo.setPlannedTimeText(vo.getPlannedStartTime().format(DateFormats.DATETIME) + " ~ "
                    + vo.getPlannedEndTime().format(DateFormats.TIME_MINUTE));
        }
        if (vo.getOperationStartTime() != null && vo.getOperationEndTime() != null) {
            long minutes = Math.max(0, Duration.between(
                    vo.getOperationStartTime(), vo.getOperationEndTime()).toMinutes());
            vo.setDurationMinutes(minutes);
            vo.setDurationText(AnesthesiaCalcs.durationText(minutes));
        }
        vo.setWaitText(waitText(vo, pending, scheduled, checked));

        LocalDateTime now = TimeUtil.nowSeconds();
        boolean stalled = checked && vo.getPreopCheckTime() != null
                && now.isAfter(vo.getPreopCheckTime().plusHours(STALLED_HOURS));
        vo.setStalled(stalled);
        if (stalled) {
            vo.setStalledText("术前核对完成已超过 " + STALLED_HOURS + " 小时仍未登记手术完成");
        }
        if (finished && (vo.getOperationId() == null || vo.getRecordId() == null)) {
            // 链断了的假数据：状态"已完成"却没有首页明细/病历锚点。必须能看见，不允许静默。
            vo.setStalled(true);
            vo.setStalledText("已完成但回写链不完整（首页明细ID/病历ID 缺失），请核查");
        }
        if (cancelled) {
            vo.setWaitText(null);
        }
    }

    private String waitText(OperationApplyVO vo, boolean pending, boolean scheduled, boolean checked) {
        LocalDateTime now = TimeUtil.nowSeconds();
        if (pending && vo.getApplyTime() != null) {
            long m = Math.max(0, Duration.between(vo.getApplyTime(), now).toMinutes());
            return "申请后已等待 " + AnesthesiaCalcs.durationText(m) + " 未排台";
        }
        if (scheduled && vo.getPlannedStartTime() != null) {
            if (now.isBefore(vo.getPlannedStartTime())) {
                return "距计划开始 " + AnesthesiaCalcs.durationText(
                        Duration.between(now, vo.getPlannedStartTime()).toMinutes());
            }
            return "已过计划开始时间 " + AnesthesiaCalcs.durationText(
                    Duration.between(vo.getPlannedStartTime(), now).toMinutes());
        }
        if (checked && vo.getPreopCheckTime() != null) {
            return "核对完成已 " + AnesthesiaCalcs.durationText(
                    Duration.between(vo.getPreopCheckTime(), now).toMinutes()) + "，尚未结束";
        }
        return null;
    }

    private BizOperationApply mustGet(Long applyId) {
        BizOperationApply entity = bizOperationApplyMapper.selectById(applyId);
        if (entity == null) {
            throw new BusinessException("手术申请单不存在");
        }
        return entity;
    }

    /**
     * 科室名（取不到就返回原文案"未知科室(ID=x)"，绝不编一个科室名）
     */
    private String deptNameOf(Long deptId) {
        if (deptId == null) {
            return null;
        }
        String name = bizOperationApplyMapper.selectDeptName(deptId);
        return TextUtil.hasText(name) ? name : "未知科室(ID=" + deptId + ")";
    }

    private String wardNameOf(Long wardId) {
        if (wardId == null) {
            return null;
        }
        WardVO ward = inpatientService.getWardById(wardId);
        return ward == null ? "未知病区(ID=" + wardId + ")" : ward.getWardName();
    }

    private String bedNoOf(Long bedId) {
        return inpatientService.getBedNoById(bedId);
    }

    /**
     * 员工姓名（服务端查名，不信任前端传来的姓名 —— 姓名是可以随便伪造的字符串）
     */
    private String employeeNameOf(Long empId) {
        if (empId == null) {
            return null;
        }
        String name = bizOperationApplyMapper.selectEmployeeName(empId);
        return TextUtil.hasText(name) ? name : "未知员工(ID=" + empId + ")";
    }

    private String nextApplyNo() {
        String prefix = "SS" + LocalDate.now().format(DateFormats.COMPACT_DATE);
        long seq = bizOperationApplyMapper.countByNoPrefix(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    /**
     * 技术授权准入闸：择期拒单，急诊由授权服务写越权登记后放行（同一事务，越权登记与手术单同生共死）。
     *
     * <p>拒单信息一定带上「谁是这个角色、要求几级」，否则收费台/手术室只能反查是谁被谁拦了。
     */
    private void gateTechAuth(Long employeeId, int authCategory, Integer requiredLevel,
                              BizOperationApply entity, String roleName, String personName) {
        TechAuthGateDTO gate = new TechAuthGateDTO();
        gate.setEmployeeId(employeeId);
        gate.setAuthCategory(authCategory);
        gate.setRequiredLevel(requiredLevel);
        gate.setItemCode(entity.getPlannedOperationCode());
        gate.setEmergency(Objects.equals(1, entity.getIsEmergency()));
        gate.setSourceType(TechOverrideSourceEnum.OPERATION_APPLY.getCode());
        gate.setSourceId(entity.getId());
        gate.setSourceNo(entity.getApplyNo());
        gate.setReason(roleName + "「" + textOr(personName, "未指名") + "」在急诊手术 " + entity.getApplyNo()
                + "（" + entity.getPlannedOperationName() + "）上越权，该手术要求 " + requiredLevel + " 级授权");
        try {
            employeeTechAuthService.gate(gate);
        } catch (BusinessException e) {
            throw new BusinessException(roleName + "（" + textOr(personName, "未指名") + "）" + e.getMessage());
        }
    }

    /**
     * 留痕一律用**员工ID**（不是用户的ID），与医嘱/站内信同一口径
     */
    private record Parsed(String from, String to) {
    }
}