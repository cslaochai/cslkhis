package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.constant.DictType;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.emr.entity.BizInspectionApply;
import com.his.emr.mapper.BizInspectionApplyMapper;
import com.his.medicaltech.dto.ExamApptDTO;
import com.his.medicaltech.entity.BizExamAppointment;
import com.his.medicaltech.entity.BizExamDevice;
import com.his.medicaltech.entity.BizExamDeviceItem;
import com.his.medicaltech.entity.BizExamSlot;
import com.his.medicaltech.mapper.BizExamAppointmentMapper;
import com.his.medicaltech.mapper.BizExamDeviceItemMapper;
import com.his.medicaltech.mapper.BizExamDeviceMapper;
import com.his.medicaltech.mapper.ExamApplyWriterMapper;
import com.his.medicaltech.service.ExamAppointmentService;
import com.his.medicaltech.service.ExamSlotService;
import com.his.medicaltech.support.ExamGrid;
import com.his.medicaltech.vo.ExamApptMessagePayloadVO;
import com.his.medicaltech.vo.ExamApptVO;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysInspectionItem;
import com.his.system.enums.BizTypeEnum;
import com.his.system.mapper.SysInspectionItemMapper;
import com.his.system.service.DictCacheService;
import com.his.system.service.SysMessageService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 检查预约服务：待预约申请、占号/改约/取消/到检/完成/爽约，以及设备与患者两侧的冲突检测。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamAppointmentServiceImpl extends ServiceImpl<BizExamAppointmentMapper, BizExamAppointment> implements ExamAppointmentService {

    private static final int APPT_BOOKED = 1;
    private static final int APPT_ARRIVED = 2;
    private static final int APPT_FINISHED = 3;
    private static final int APPT_CANCELLED = 4;
    private static final int APPT_NOSHOW = 5;

    /**
     * 爽约判定的操作人。检查爽约由 {@code ExamNoShowTrigger} 每 10 分钟扫一次，
     * 调度线程没有登录态 —— 这是全项目第二处（第一处是日终结转）允许落系统值的路径，
     * 落库可一眼看出「这单不是人来取消的」。人工改约/取消一律走真人操作人。
     */
    private static final String SYSTEM_NOSHOW_OPERATOR = "system:examNoShow";

    private static final int APPLY_SUBMITTED = 1;
    private static final int APPLY_PAID = 2;
    private static final int APPLY_BOOKED = 3;

    private static final int DEVICE_OPEN = 1;
    private static final int SLOT_LOCKED = 0;


    /**
     * 待预约事实：active_flag=1 才有在办预约（唯一索引位）
     */
    private static final String NO_ACTIVE_APPOINTMENT =
            "SELECT 1 FROM biz_exam_appointment ea WHERE ea.apply_id = biz_inspection_apply.id "
                    + "AND ea.active_flag = 1 AND ea.del_flag = 0";

    private final BizInspectionApplyMapper bizInspectionApplyMapper;
    private final SysInspectionItemMapper sysInspectionItemMapper;
    private final BizExamDeviceMapper bizExamDeviceMapper;
    private final BizExamDeviceItemMapper bizExamDeviceItemMapper;
    private final BizExamAppointmentMapper bizExamAppointmentMapper;
    private final ExamApplyWriterMapper examApplyWriterMapper;
    private final ExamSlotService examSlotService;
    private final RedisSequenceService redisSequenceService;
    private final SysMessageService sysMessageService;
    private final DictCacheService dictCacheService;

    // 待预约申请

    // 预约台账

    public PageResult<ExamApptVO.ApplyVO> pendingListPage(ExamApptDTO.ApplyQuery q) {
        LambdaQueryWrapper<BizInspectionApply> w = new LambdaQueryWrapper<BizInspectionApply>()
                .in(BizInspectionApply::getApplyStatus, APPLY_SUBMITTED, APPLY_PAID)
                .notExists(NO_ACTIVE_APPOINTMENT);
        String kw = TextUtil.trim(q.getKeyword());
        w.and(TextUtil.hasText(kw), x -> x.like(BizInspectionApply::getApplyNo, kw)
                        .or().like(BizInspectionApply::getPatientName, kw)
                        .or().like(BizInspectionApply::getInspectionItemName, kw))
                .eq(q.getPatientId() != null, BizInspectionApply::getPatientId, q.getPatientId())
                .eq(q.getIsEmergency() != null, BizInspectionApply::getIsEmergency, q.getIsEmergency())
                .ge(q.getStartDate() != null, BizInspectionApply::getVisitDate, q.getStartDate())
                .le(q.getEndDate() != null, BizInspectionApply::getVisitDate, q.getEndDate())
                .orderByDesc(BizInspectionApply::getIsEmergency)
                .orderByAsc(BizInspectionApply::getId);
        Page<BizInspectionApply> page = bizInspectionApplyMapper.selectPage(
                new Page<>(q.getPageNum(), q.getPageSize()), w);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                toApplyVos(page.getRecords()));
    }

    public PageResult<ExamApptVO.ApptVO> listPage(ExamApptDTO.ApptQuery q) {
        Page<BizExamAppointment> page = bizExamAppointmentMapper.selectPage(
                new Page<>(q.getPageNum(), q.getPageSize()), apptFilter(q));
        List<ExamApptVO.ApptVO> records = new ArrayList<>();
        for (BizExamAppointment a : page.getRecords()) {
            records.add(toApptVo(a));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    // 占号 / 改约 / 取消

    /**
     * 各状态单数：与 listPage 同一套筛选条件，逐状态 count（状态是固定 5 档，比拼接 group by 更不易写错）
     */
    public List<ExamApptVO.StatusCountVO> statusCount(ExamApptDTO.ApptQuery q) {
        // 状态分布是「换一档看看还有多少」的导航，本身不能被 status 过滤条件支配 ——
        // 否则点中某一档后其余四档全部归零，分布条当场失去意义。
        q.setStatus(null);
        List<ExamApptVO.StatusCountVO> out = new ArrayList<>();
        long total = 0;
        for (int status = APPT_BOOKED; status <= APPT_NOSHOW; status++) {
            LambdaQueryWrapper<BizExamAppointment> w = apptFilter(q).eq(BizExamAppointment::getStatus, status);
            long n = bizExamAppointmentMapper.selectCount(w);
            total += n;
            ExamApptVO.StatusCountVO v = new ExamApptVO.StatusCountVO();
            v.setStatus(status);
            v.setStatusText(dictCacheService.getDicDataLabel(DictType.EXAM_APPOINT_STATUS, status));
            v.setCount(n);
            out.add(v);
        }
        ExamApptVO.StatusCountVO all = new ExamApptVO.StatusCountVO();
        all.setStatus(null);
        all.setStatusText("全部");
        all.setCount(total);
        out.add(all);
        return out;
    }

    public ExamApptVO.ApptDetailVO getDetail(Long apptId) {
        BizExamAppointment a = requireAppt(apptId);
        ExamApptVO.ApptDetailVO vo = new ExamApptVO.ApptDetailVO();
        BeanUtils.copyProperties(toApptVo(a), vo);
        vo.setPrevApplyStatusText(dictCacheService.getDicDataLabel(DictType.INSPECTION_APPLY_STATUS, a.getPrevApplyStatus()));
        vo.setApplyNoSnapshot(a.getApplyNo());
        return vo;
    }

    public ExamApptVO.StatsVO stats() {
        ExamApptVO.StatsVO vo = new ExamApptVO.StatsVO();
        LocalDate today = LocalDate.now();
        for (ExamApptVO.StatusCountVO s : statusCount(dayQuery(today))) {
            if (s.getStatus() == null) {
                continue;
            }
            switch (s.getStatus()) {
                case APPT_BOOKED -> vo.setTodayBooked(s.getCount());
                case APPT_ARRIVED -> vo.setTodayArrived(s.getCount());
                case APPT_FINISHED -> vo.setTodayFinished(s.getCount());
                case APPT_CANCELLED -> vo.setTodayCancelled(s.getCount());
                case APPT_NOSHOW -> vo.setTodayNoShow(s.getCount());
                default -> {
                }
            }
        }
        vo.setPendingApplies(bizInspectionApplyMapper.selectCount(new LambdaQueryWrapper<BizInspectionApply>()
                .in(BizInspectionApply::getApplyStatus, APPLY_SUBMITTED, APPLY_PAID)
                .notExists(NO_ACTIVE_APPOINTMENT)));
        vo.setActiveTotal(bizExamAppointmentMapper.selectCount(new LambdaQueryWrapper<BizExamAppointment>()
                .eq(BizExamAppointment::getActiveFlag, 1)
                .in(BizExamAppointment::getStatus, APPT_BOOKED, APPT_ARRIVED)));
        vo.setDeviceOpen(bizExamDeviceMapper.selectCount(new LambdaQueryWrapper<BizExamDevice>()
                .eq(BizExamDevice::getStatus, DEVICE_OPEN)));
        vo.setDevicePaused(bizExamDeviceMapper.selectCount(new LambdaQueryWrapper<BizExamDevice>()
                .ne(BizExamDevice::getStatus, DEVICE_OPEN)));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public ExamApptVO.ApptDetailVO book(ExamApptDTO.Book dto) {
        BizInspectionApply apply = requireApply(dto.getApplyId());
        int prevStatus = gateApplyStatus(apply);
        String remark = dto.getRemark();
        if (prevStatus == APPLY_SUBMITTED) {
            remark = "急诊绿色通道：申请单未缴费先占号。" + (remark == null ? "" : " " + remark);
        }
        BizExamAppointment appt = doBook(apply, dto.getDeviceId(), dto.getExamDate(), dto.getStartTime(),
                prevStatus, remark);
        notifyDoctor(appt, "检查预约成功");
        return getDetail(appt.getId());
    }

    /**
     * 改约 = 终结旧单（记 4-已取消，原因写"改约"）+ 按新时段占号。
     *
     * <p>不留"一条记录改来改去"的做法：占号历史是唯一索引和号源计数的依据，
     * 原地改字段会让"这台机器这个时段曾经约给谁"彻底查不出来。
     */
    @Transactional(rollbackFor = Exception.class)
    public ExamApptVO.ApptDetailVO reschedule(ExamApptDTO.Reschedule dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizExamAppointment old = requireAppt(dto.getApptId());
        if (old.getActiveFlag() == null) {
            throw new BusinessException("预约单 " + old.getApptNo() + " 已终结，不能再改约");
        }
        if (old.getStatus() != APPT_BOOKED) {
            throw new BusinessException("仅「已预约」可改约（当前："
                    + dictCacheService.getDicDataLabel(DictType.EXAM_APPOINT_STATUS, old.getStatus()) + "）");
        }
        BizInspectionApply apply = requireApply(old.getApplyId());
        int prevStatus = old.getPrevApplyStatus() == null ? APPLY_PAID : old.getPrevApplyStatus();
        releaseOld(old, "改约：" + dto.getExamDate() + " " + dto.getStartTime() + "，原因：" + dto.getReason(),
                APPT_CANCELLED, operatorUser.getRealName());
        BizExamAppointment fresh = doBook(apply, dto.getDeviceId(), dto.getExamDate(), dto.getStartTime(),
                prevStatus, "改约自 " + old.getApptNo() + "（" + old.getExamDate() + " " + old.getStartTime()
                        + "-" + old.getEndTime() + " @" + old.getDeviceName() + "）；原因：" + dto.getReason());
        notifyDoctor(fresh, "检查改约");
        return getDetail(fresh.getId());
    }

    /**
     * 取消预约。只放开「已预约」——患者已到检后再反悔，属于医技执行域的取消登记，
     * 不在这里改一套平行的取消口径（否则申请单会停在「检查中」却被退号）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(ExamApptDTO.Cancel dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizExamAppointment appt = requireAppt(dto.getApptId());
        if (appt.getActiveFlag() == null) {
            throw new BusinessException("预约单 " + appt.getApptNo() + " 已终结");
        }
        if (appt.getStatus() != APPT_BOOKED) {
            throw new BusinessException("仅「已预约」可取消（当前："
                    + dictCacheService.getDicDataLabel(DictType.EXAM_APPOINT_STATUS, appt.getStatus()) + "）；已到检请在检查工作站取消登记");
        }
        releaseOld(appt, dto.getCancelReason(), APPT_CANCELLED, operatorUser.getRealName());
    }

    // 时段推荐

    /**
     * 到检：申请单从「已预约」推进到「检查中」；号源不释放（机器时间已经花掉了）
     */
    @Transactional(rollbackFor = Exception.class)
    public void arrive(ExamApptDTO.ApptIdOnly dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizExamAppointment appt = requireAppt(dto.getApptId());
        if (appt.getStatus() != APPT_BOOKED) {
            throw new BusinessException("仅「已预约」可签到（当前："
                    + dictCacheService.getDicDataLabel(DictType.EXAM_APPOINT_STATUS, appt.getStatus()) + "）");
        }
        BizExamAppointment update = new BizExamAppointment();
        update.setId(appt.getId());
        update.setStatus(APPT_ARRIVED);
        update.setArriveTime(LocalDateTime.now().withNano(0));
        bizExamAppointmentMapper.updateById(update);
        int moved = examApplyWriterMapper.markArrived(appt.getApplyId(), operatorUser.getRealName());
        if (moved == 0) {
            log.warn("[检查预约] {} 到检时申请单 {} 未停在「已预约」，不覆盖其当前状态",
                    appt.getApptNo(), appt.getApplyNo());
        }
    }

    // 内部：占号

    @Transactional(rollbackFor = Exception.class)
    public void finish(ExamApptDTO.ApptIdOnly dto) {
        BizExamAppointment appt = requireAppt(dto.getApptId());
        if (appt.getStatus() != APPT_ARRIVED) {
            throw new BusinessException("请先签到再完成检查（当前："
                    + dictCacheService.getDicDataLabel(DictType.EXAM_APPOINT_STATUS, appt.getStatus()) + "）");
        }
        BizExamAppointment update = new BizExamAppointment();
        update.setId(appt.getId());
        update.setStatus(APPT_FINISHED);
        update.setFinishTime(LocalDateTime.now().withNano(0));
        bizExamAppointmentMapper.updateById(update);
    }

    /**
     * 爽约扫描：停在「已预约」且时段已过。退号 + 申请单精确回退到预约前状态。
     *
     * <p>不自动推进到「检查中→已出报告」那条链：没来人这件事只有当班的人知道，
     * 系统能做的只是把号源还回去、把申请单退回待预约。
     */
    @Transactional(rollbackFor = Exception.class)
    public int autoNoShow() {
        List<BizExamAppointment> overdue = bizExamAppointmentMapper.selectOverdueNoShow();
        int done = 0;
        for (BizExamAppointment appt : overdue) {
            try {
                releaseOld(appt, "超时未到检，系统自动判为爽约", APPT_NOSHOW, SYSTEM_NOSHOW_OPERATOR);
                done++;
            } catch (Exception e) {
                log.warn("[检查预约] 爽约处理失败 {}：{}", appt.getApptNo(), e.getMessage());
            }
        }
        return done;
    }

    /**
     * 推荐可选时段：只在该项目的可承接设备之间选，按「日期 → 时刻」给最近的空档。
     *
     * <p>本方法会为候选设备×日期补齐号源格子（幂等写入），因为「有没有号」这个
     * 问题的答案本身就依赖格子存在；不补齐就只能报"未生成号源"，等于没回答问题。
     */
    @Transactional(rollbackFor = Exception.class)
    public ExamApptVO.RecommendVO recommend(ExamApptDTO.Recommend dto) {
        BizInspectionApply apply = requireApply(dto.getApplyId());
        ExamApptVO.RecommendVO vo = new ExamApptVO.RecommendVO();
        vo.setApplyId(apply.getId());
        vo.setItemName(apply.getInspectionItemName());

        List<BizExamDeviceItem> maps = bizExamDeviceItemMapper.selectList(new LambdaQueryWrapper<BizExamDeviceItem>()
                .eq(BizExamDeviceItem::getItemId, apply.getInspectionItemId()));
        if (maps.isEmpty()) {
            vo.setMessage("项目「" + apply.getInspectionItemName() + "」尚未配置可承接设备，请到「设备与号源」页维护映射");
            vo.setOptions(List.of());
            return vo;
        }

        LocalDate from = dto.getExamDate() == null ? LocalDate.now() : dto.getExamDate();
        int afterMin = TextUtil.hasText(dto.getAfterTime()) ? ExamGrid.toMin(dto.getAfterTime()) : -1;
        List<ExamApptVO.SlotOptionVO> options = new ArrayList<>();
        Integer shownMinutes = null;
        for (BizExamDeviceItem map : maps) {
            BizExamDevice device = bizExamDeviceMapper.selectForUpdate(map.getDeviceId());
            if (device == null || device.getStatus() != DEVICE_OPEN
                    || (dto.getDeviceId() != null && !dto.getDeviceId().equals(device.getId()))) {
                continue;
            }
            // 时长必须按「这台设备做这个项目」算：1.5T 比 3.0T 慢，覆盖列不同
            int minutes = resolveMinutes(map);
            if (minutes > device.getMaxSlotMinutes()) {
                continue;
            }
            if (shownMinutes == null) {
                shownMinutes = minutes;
            }
            LocalDate limit = LocalDate.now().plusDays(device.getAheadDays());
            for (int d = 0; d < 3 && !from.plusDays(d).isAfter(limit); d++) {
                LocalDate date = from.plusDays(d);
                if (options.size() >= 10) {
                    break;
                }
                List<ExamApptVO.SlotOptionVO> found = new ArrayList<>();
                for (BizExamSlot cell : freeCells(device, date, minutes, afterMin, d == 0)) {
                    int startMin = ExamGrid.toMin(cell.getStartTime());
                    ExamApptVO.SlotOptionVO o = new ExamApptVO.SlotOptionVO();
                    o.setDeviceId(device.getId());
                    o.setDeviceCode(device.getDeviceCode());
                    o.setDeviceName(device.getDeviceName());
                    o.setDeviceTypeText(dictCacheService.getDicDataLabel(DictType.EXAM_DEVICE_TYPE, device.getDeviceType()));
                    o.setRoomName(device.getRoomName());
                    o.setExamDate(date);
                    o.setStartTime(cell.getStartTime());
                    o.setEndTime(ExamGrid.toHHmm(startMin + minutes));
                    o.setAvailableSource(cell.getAvailableSource());
                    found.add(o);
                }
                options.addAll(found);
            }
        }
        vo.setExamMinutes(shownMinutes);
        options.sort((a, b) -> {
            int byDate = a.getExamDate().compareTo(b.getExamDate());
            return byDate != 0 ? byDate : a.getStartTime().compareTo(b.getStartTime());
        });
        if (options.size() > 10) {
            options = new ArrayList<>(options.subList(0, 10));
        }
        vo.setOptions(options);
        vo.setMessage(options.isEmpty()
                ? "近 3 天内该项目暂无满足时长要求的空档，可放宽日期或先锁号腾挪"
                : "找到 " + options.size() + " 个可选时段（按日期与时刻就近排序）");
        return vo;
    }

    private BizExamAppointment doBook(BizInspectionApply apply, Long deviceId, LocalDate examDate,
                                      String startTime, Integer prevStatus, String remark) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizExamDevice device = bizExamDeviceMapper.selectForUpdate(deviceId);
        if (device == null) {
            throw new BusinessException("预约设备不存在：" + deviceId);
        }
        if (device.getStatus() != DEVICE_OPEN) {
            throw new BusinessException("设备「" + device.getDeviceName() + "」"
                    + dictCacheService.getDicDataLabel(DictType.EXAM_DEVICE_STATUS, device.getStatus()) + "，暂不受理预约");
        }
        BizExamDeviceItem map = bizExamDeviceItemMapper.selectOne(new LambdaQueryWrapper<BizExamDeviceItem>()
                .eq(BizExamDeviceItem::getDeviceId, device.getId())
                .eq(BizExamDeviceItem::getItemId, apply.getInspectionItemId()));
        if (map == null) {
            throw new BusinessException("设备「" + device.getDeviceName() + "」未配置项目「"
                    + apply.getInspectionItemName() + "」，不能在其上预约；请到「设备与号源」页维护可开展项目");
        }
        int minutes = resolveMinutes(map);
        if (minutes > device.getMaxSlotMinutes()) {
            throw new BusinessException("项目时长 " + minutes + " 分钟超过设备「" + device.getDeviceName()
                    + "」单次可占上限 " + device.getMaxSlotMinutes() + " 分钟（超长时程项目不走分时段号源，请人工登记）");
        }
        LocalDate today = LocalDate.now();
        if (examDate.isBefore(today)) {
            throw new BusinessException("检查日期不得早于今天：" + examDate);
        }
        if (examDate.isAfter(today.plusDays(device.getAheadDays()))) {
            throw new BusinessException("设备「" + device.getDeviceName() + "」只开放提前 "
                    + device.getAheadDays() + " 天内的预约");
        }
        int startMin = ExamGrid.toMin(startTime);
        int endMin = startMin + minutes;
        if (LocalDateTime.of(examDate, LocalTime.of(startMin / 60, startMin % 60)).isBefore(LocalDateTime.now())) {
            throw new BusinessException("开始时刻已过：" + examDate + " " + startTime + "，请选择之后的时段");
        }

        List<BizExamSlot> cells = examSlotService.ensureLockedDay(device, examDate);
        if (cells.isEmpty()) {
            throw new BusinessException("设备「" + device.getDeviceName() + "」在 " + examDate + " 切不出号源格子");
        }
        int[] span = examSlotService.spanOf(cells, startMin, endMin);

        List<BizExamAppointment> occupants = bizExamAppointmentMapper.selectOccupants(device.getId(), examDate);
        for (int i = span[0]; i <= span[1]; i++) {
            BizExamSlot cell = cells.get(i);
            if (cell.getStatus() != null && cell.getStatus() == SLOT_LOCKED) {
                throw new BusinessException(examDate + " " + cell.getStartTime() + "-" + cell.getEndTime()
                        + " 已锁号，不能占号");
            }
            int used = cell.getUsedSource() == null ? 0 : cell.getUsedSource();
            int total = cell.getTotalSource() == null ? device.getParallelCount() : cell.getTotalSource();
            if (used >= total) {
                throw new BusinessException("设备冲突：" + device.getDeviceName() + " " + examDate + " "
                        + cell.getStartTime() + "-" + cell.getEndTime() + " 已约满（" + used + "/" + total + "），占用者："
                        + occupantsOverlapping(occupants, cell));
            }
        }

        // 患者侧冲突：同一患者同一天两段检查重叠
        List<BizExamAppointment> samePatient = bizExamAppointmentMapper
                .selectPatientDayForUpdate(apply.getPatientId(), examDate);
        for (BizExamAppointment other : samePatient) {
            if (other.getApplyId().equals(apply.getId())) {
                continue;
            }
            int os = ExamGrid.toMin(other.getStartTime());
            int oe = ExamGrid.toMin(other.getEndTime());
            if (os < endMin && oe > startMin) {
                throw new BusinessException("患者冲突：" + apply.getPatientName() + " 在 " + examDate
                        + " 已有预约 " + other.getApptNo() + "（" + other.getStartTime() + "-" + other.getEndTime()
                        + " " + other.getDeviceName() + "），与 " + startTime + "-"
                        + ExamGrid.toHHmm(endMin) + " 时间重叠");
            }
        }

        BizExamAppointment appt = new BizExamAppointment();
        LocalDateTime now = LocalDateTime.now().withNano(0);
        appt.setActiveFlag(1);
        appt.setApptNo(redisSequenceService.generateExamAppointNo());
        appt.setApplyId(apply.getId());
        appt.setApplyNo(apply.getApplyNo());
        appt.setPrevApplyStatus(prevStatus);
        appt.setPatientId(apply.getPatientId());
        appt.setPatientNo(apply.getPatientNo());
        appt.setPatientName(apply.getPatientName());
        appt.setGender(apply.getGender());
        appt.setAge(apply.getAge());
        appt.setApplyDeptId(apply.getDeptId());
        appt.setApplyDeptName(apply.getDeptName());
        appt.setDoctorId(apply.getDoctorId());
        appt.setDoctorName(apply.getDoctorName());
        appt.setItemId(apply.getInspectionItemId());
        appt.setItemCode(apply.getInspectionItemCode());
        appt.setItemName(apply.getInspectionItemName());
        appt.setBodyPart(apply.getBodyPart());
        appt.setExamMinutes(minutes);
        appt.setDeviceId(device.getId());
        appt.setDeviceCode(device.getDeviceCode());
        appt.setDeviceName(device.getDeviceName());
        appt.setExamDeptId(device.getDeptId());
        appt.setExamDeptName(device.getDeptName());
        appt.setRoomName(device.getRoomName());
        appt.setExamDate(examDate);
        appt.setStartTime(startTime);
        appt.setEndTime(ExamGrid.toHHmm(endMin));
        appt.setIsEmergency(apply.getIsEmergency());
        appt.setStatus(APPT_BOOKED);
        appt.setBookBy(operatorUser.getRealName());
        appt.setBookTime(now);
        appt.setRemark(TextUtil.cut(remark, 480));
        bizExamAppointmentMapper.insert(appt);

        examSlotService.claim(device, examDate, cells, span);

        LocalDateTime appointmentTime = LocalDateTime.of(examDate, LocalTime.of(startMin / 60, startMin % 60));
        if (examApplyWriterMapper.markBooked(apply.getId(), appointmentTime, operatorUser.getRealName()) == 0) {
            throw new BusinessException("申请单状态推进失败（可能已被他人处理），预约已回滚，请刷新后重试");
        }
        return appt;
    }

    /**
     * 取消/改约/爽约的共同收尾：退号 + 终结唯一索引位 + 申请单精确回退。
     *
     * <p>操作人由调用方传入而不是内部取：三个调用点里两个是人工操作、一个是定时任务，
     * 口径不同（见 {@link #SYSTEM_NOSHOW_OPERATOR}）。
     */
    private void releaseOld(BizExamAppointment appt, String reason, int targetStatus, String operator) {
        BizExamDevice device = bizExamDeviceMapper.selectForUpdate(appt.getDeviceId());
        LocalDate date = appt.getExamDate();
        if (device != null) {
            List<BizExamSlot> cells = examSlotService.ensureLockedDay(device, date);
            int[] span = safeSpan(cells, appt);
            if (span != null) {
                examSlotService.release(device, date, cells, span);
            }
        }
        LocalDateTime now = LocalDateTime.now().withNano(0);
        BizExamAppointment update = new BizExamAppointment();
        update.setId(appt.getId());
        update.setStatus(targetStatus);
        if (targetStatus == APPT_NOSHOW) {
            update.setNoshowTime(now);
        } else {
            update.setCancelTime(now);
        }
        update.setCancelReason(TextUtil.cut(reason, 480));
        bizExamAppointmentMapper.updateById(update);
        bizExamAppointmentMapper.markInactive(appt.getId());
        int prev = appt.getPrevApplyStatus() == null ? APPLY_PAID : appt.getPrevApplyStatus();
        if (examApplyWriterMapper.revertBooked(appt.getApplyId(), prev, operator) == 0) {
            log.warn("[检查预约] {} 终结后申请单 {} 未停在「已预约」，状态不回退（可能已进入执行链）",
                    appt.getApptNo(), appt.getApplyNo());
        }
    }

    /**
     * 按预约单自己的起止时刻反推格子下标。
     *
     * <p>设备开放时间被改过时可能反推不出（格子已不在窗口内）：此时不能退号，
     * 也不能因此拒绝取消 —— 号源计数会漂，由 /slotRecalc 以预约单为事实复算修平。
     */
    private int[] safeSpan(List<BizExamSlot> cells, BizExamAppointment appt) {
        try {
            return examSlotService.spanOf(cells, ExamGrid.toMin(appt.getStartTime()), ExamGrid.toMin(appt.getEndTime()));
        } catch (BusinessException e) {
            log.warn("[检查预约] {} 的时段已不在设备 {} 当前号源网格内，跳过退号（交给对账修平）：{}",
                    appt.getApptNo(), appt.getDeviceName(), e.getMessage());
            return null;
        }
    }

    /**
     * @return 申请单在预约前应处于的状态（取消时按它精确回退）
     */
    private int gateApplyStatus(BizInspectionApply apply) {
        int status = apply.getApplyStatus() == null ? 0 : apply.getApplyStatus();
        if (status == APPLY_BOOKED) {
            BizExamAppointment active = bizExamAppointmentMapper.selectOne(new LambdaQueryWrapper<BizExamAppointment>()
                    .eq(BizExamAppointment::getApplyId, apply.getId())
                    .eq(BizExamAppointment::getActiveFlag, 1));
            if (active != null) {
                throw new BusinessException("申请单 " + apply.getApplyNo() + " 已有在办预约 " + active.getApptNo()
                        + "，请改用「改约」");
            }
            return APPLY_PAID;
        }
        if (status == APPLY_PAID) {
            return APPLY_PAID;
        }
        if (status == APPLY_SUBMITTED && isEmergency(apply)) {
            return APPLY_SUBMITTED;
        }
        throw new BusinessException("申请单 " + apply.getApplyNo() + " 当前状态「"
                + dictCacheService.getDicDataLabel(DictType.INSPECTION_APPLY_STATUS, status) + "」不可预约："
                + (status == APPLY_SUBMITTED ? "检查须先缴费再预约（急诊可走绿色通道）" : "只有已缴费或急诊已提交的申请单可预约"));
    }

    private List<BizExamSlot> freeCells(BizExamDevice device, LocalDate date, int minutes,
                                        int afterMin, boolean checkPast) {
        List<BizExamSlot> cells = examSlotService.ensureLockedDay(device, date);
        List<BizExamSlot> out = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (BizExamSlot cell : cells) {
            int startMin = ExamGrid.toMin(cell.getStartTime());
            if (afterMin >= 0 && startMin < afterMin) {
                continue;
            }
            if (checkPast && LocalDateTime.of(date, LocalTime.of(startMin / 60, startMin % 60)).isBefore(now)) {
                continue;
            }
            int[] span;
            try {
                // 落不进完整格子（跨过午休或超出开放窗口）的起点不是可选项
                span = examSlotService.spanOf(cells, startMin, startMin + minutes);
            } catch (BusinessException e) {
                continue;
            }
            boolean free = true;
            for (int j = span[0]; j <= span[1]; j++) {
                BizExamSlot c = cells.get(j);
                int used = c.getUsedSource() == null ? 0 : c.getUsedSource();
                int total = c.getTotalSource() == null ? device.getParallelCount() : c.getTotalSource();
                if ((c.getStatus() != null && c.getStatus() == SLOT_LOCKED) || used >= total) {
                    free = false;
                    break;
                }
            }
            if (free) {
                out.add(cell);
            }
        }
        return out;
    }

    // 内部：查询辅助

    private String occupantsOverlapping(List<BizExamAppointment> occupants, BizExamSlot cell) {
        List<String> nos = new ArrayList<>();
        for (BizExamAppointment a : occupants) {
            int as = ExamGrid.toMin(a.getStartTime());
            int ae = ExamGrid.toMin(a.getEndTime());
            int cs = ExamGrid.toMin(cell.getStartTime());
            int ce = ExamGrid.toMin(cell.getEndTime());
            if (as < ce && ae > cs) {
                nos.add(a.getApptNo() + "(" + a.getPatientName() + " " + a.getStartTime() + "-" + a.getEndTime() + ")");
            }
        }
        return nos.isEmpty() ? "无（计数漂移，请对账）" : String.join("、", nos);
    }

    private int resolveMinutes(BizExamDeviceItem map) {
        if (map.getExamMinutes() != null && map.getExamMinutes() > 0) {
            return map.getExamMinutes();
        }
        SysInspectionItem item = map.getItemId() == null ? null : sysInspectionItemMapper.selectById(map.getItemId());
        if (item == null || item.getDuration() == null || item.getDuration() <= 0) {
            throw new BusinessException("项目「" + map.getItemName() + "」的检查时长未维护，无法计算占号区间");
        }
        return item.getDuration();
    }

    private void notifyDoctor(BizExamAppointment appt, String action) {
        if (appt.getDoctorId() == null) {
            return;
        }
        try {
            String title = action + "：" + appt.getItemName();
            String content = String.format(
                    "患者 %s（%s）的检查「%s」已安排在 %s %s-%s %s（%s），预约单号 %s。",
                    appt.getPatientName(), appt.getPatientNo(), appt.getItemName(),
                    appt.getExamDate(), appt.getStartTime(), appt.getEndTime(),
                    appt.getDeviceName(), appt.getRoomName() == null ? "" : appt.getRoomName(),
                    appt.getApptNo());
            ExamApptMessagePayloadVO payload = new ExamApptMessagePayloadVO();
            payload.setApptNo(appt.getApptNo());
            payload.setPatientName(appt.getPatientName());
            payload.setItemName(appt.getItemName());
            payload.setDeviceName(appt.getDeviceName());
            sysMessageService.sendSystemMessage(appt.getDoctorId(), appt.getDoctorName(), title, content,
                    BizTypeEnum.APPOINTMENT.getType(), appt.getId(), "info",
                    cn.hutool.json.JSONUtil.toJsonStr(payload), null);
        } catch (Exception e) {
            log.warn("[检查预约] 站内信发送失败 {}：{}", appt.getApptNo(), e.getMessage());
        }
    }

    private LambdaQueryWrapper<BizExamAppointment> apptFilter(ExamApptDTO.ApptQuery q) {
        String kw = TextUtil.trim(q.getKeyword());
        LambdaQueryWrapper<BizExamAppointment> w = new LambdaQueryWrapper<>();
        if (TextUtil.hasText(q.getApptNo())) {
            w.eq(BizExamAppointment::getApptNo, q.getApptNo().trim());
        }
        w.and(TextUtil.hasText(kw), x -> x.like(BizExamAppointment::getPatientName, kw)
                        .or().like(BizExamAppointment::getApptNo, kw)
                        .or().like(BizExamAppointment::getItemName, kw)
                        .or().like(BizExamAppointment::getDeviceName, kw))
                .eq(q.getPatientId() != null, BizExamAppointment::getPatientId, q.getPatientId())
                .eq(q.getDeviceId() != null, BizExamAppointment::getDeviceId, q.getDeviceId())
                .eq(q.getExamDeptId() != null, BizExamAppointment::getExamDeptId, q.getExamDeptId())
                .eq(q.getStatus() != null, BizExamAppointment::getStatus, q.getStatus())
                .ge(q.getStartDate() != null, BizExamAppointment::getExamDate, q.getStartDate())
                .le(q.getEndDate() != null, BizExamAppointment::getExamDate, q.getEndDate())
                .orderByDesc(BizExamAppointment::getExamDate)
                .orderByAsc(BizExamAppointment::getStartTime);
        if (q.getDeviceType() != null) {
            List<Long> deviceIds = bizExamDeviceMapper.selectList(new LambdaQueryWrapper<BizExamDevice>()
                            .eq(BizExamDevice::getDeviceType, q.getDeviceType()))
                    .stream().map(BizExamDevice::getId).toList();
            if (deviceIds.isEmpty()) {
                w.eq(BizExamAppointment::getId, -1L);
            } else {
                w.in(BizExamAppointment::getDeviceId, deviceIds);
            }
        }
        return w;
    }

    private ExamApptDTO.ApptQuery dayQuery(LocalDate date) {
        ExamApptDTO.ApptQuery q = new ExamApptDTO.ApptQuery();
        q.setStartDate(date);
        q.setEndDate(date);
        return q;
    }

    private List<ExamApptVO.ApplyVO> toApplyVos(List<BizInspectionApply> applies) {
        Map<Long, Integer> deviceCounts = deviceItemCountMap();
        List<Long> ids = applies.stream().map(BizInspectionApply::getId).toList();
        Map<Long, BizExamAppointment> activeByApply = new HashMap<>();
        if (!ids.isEmpty()) {
            for (BizExamAppointment a : bizExamAppointmentMapper.selectList(new LambdaQueryWrapper<BizExamAppointment>()
                    .in(BizExamAppointment::getApplyId, ids)
                    .eq(BizExamAppointment::getActiveFlag, 1))) {
                activeByApply.put(a.getApplyId(), a);
            }
        }
        List<ExamApptVO.ApplyVO> out = new ArrayList<>();
        for (BizInspectionApply a : applies) {
            ExamApptVO.ApplyVO v = new ExamApptVO.ApplyVO();
            v.setApplyId(a.getId());
            v.setApplyNo(a.getApplyNo());
            v.setPatientId(a.getPatientId());
            v.setPatientNo(a.getPatientNo());
            v.setPatientName(a.getPatientName());
            v.setGender(a.getGender());
            v.setAge(a.getAge());
            v.setVisitDate(a.getVisitDate());
            v.setApplyDeptName(a.getDeptName());
            v.setDoctorName(a.getDoctorName());
            v.setItemId(a.getInspectionItemId());
            v.setItemCode(a.getInspectionItemCode());
            v.setItemName(a.getInspectionItemName());
            v.setBodyPart(a.getBodyPart());
            v.setPrice(a.getPrice());
            v.setIsEmergency(a.getIsEmergency());
            v.setApplyStatus(a.getApplyStatus());
            v.setApplyStatusText(dictCacheService.getDicDataLabel(DictType.INSPECTION_APPLY_STATUS, a.getApplyStatus()));
            v.setDeviceCount(deviceCounts.getOrDefault(a.getInspectionItemId(), 0));
            v.setExamMinutes(dictMinutes(a.getInspectionItemId()));
            BizExamAppointment active = activeByApply.get(a.getId());
            if (active != null) {
                v.setActiveApptId(active.getId());
                v.setActiveApptNo(active.getApptNo());
                v.setActiveExamDate(active.getExamDate());
                v.setActiveTimeRange(active.getStartTime() + "-" + active.getEndTime());
            }
            out.add(v);
        }
        return out;
    }

    private Integer dictMinutes(Long itemId) {
        SysInspectionItem item = itemId == null ? null : sysInspectionItemMapper.selectById(itemId);
        return item == null ? null : item.getDuration();
    }

    private Map<Long, Integer> deviceItemCountMap() {
        Map<Long, Integer> map = new HashMap<>();
        for (BizExamDeviceItem m : bizExamDeviceItemMapper.selectList(new LambdaQueryWrapper<>())) {
            map.merge(m.getItemId(), 1, Integer::sum);
        }
        return map;
    }

    private ExamApptVO.ApptVO toApptVo(BizExamAppointment a) {
        ExamApptVO.ApptVO v = new ExamApptVO.ApptVO();
        BeanUtils.copyProperties(a, v);
        v.setStatusText(dictCacheService.getDicDataLabel(DictType.EXAM_APPOINT_STATUS, a.getStatus()));
        v.setTimeRange(a.getStartTime() + "-" + a.getEndTime());
        return v;
    }

    private BizInspectionApply requireApply(Long applyId) {
        BizInspectionApply apply = applyId == null ? null : bizInspectionApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("检查申请单不存在：" + applyId);
        }
        if (apply.getInspectionItemId() == null) {
            throw new BusinessException("申请单 " + apply.getApplyNo() + " 未关联检查项目，无法预约");
        }
        if (apply.getPatientId() == null) {
            throw new BusinessException("申请单 " + apply.getApplyNo() + " 未关联患者，无法预约");
        }
        return apply;
    }

    private BizExamAppointment requireAppt(Long apptId) {
        BizExamAppointment appt = apptId == null ? null : bizExamAppointmentMapper.selectById(apptId);
        if (appt == null) {
            throw new BusinessException("检查预约单不存在：" + apptId);
        }
        return appt;
    }

    private boolean isEmergency(BizInspectionApply apply) {
        return apply.getIsEmergency() != null && apply.getIsEmergency() == 1;
    }
}
