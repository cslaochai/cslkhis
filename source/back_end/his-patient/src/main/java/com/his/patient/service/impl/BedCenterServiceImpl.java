package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.util.TimeUtil;
import com.his.common.exception.BusinessException;
import com.his.common.enums.SysGenderEnum;
import com.his.patient.dto.*;
import com.his.patient.entity.*;
import com.his.patient.enums.BedAllocateStatusEnum;
import com.his.patient.enums.BedStatusEnum;
import com.his.patient.enums.BedWaitStatusEnum;
import com.his.patient.enums.BedPriorityEnum;
import com.his.patient.enums.BedTypeEnum;
import com.his.patient.enums.BedGenderLimitEnum;
import com.his.patient.mapper.*;
import com.his.patient.service.BedCenterService;
import com.his.patient.service.InpatientService;
import com.his.patient.vo.*;
import com.his.system.utils.UserUtils;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysConfig;
import com.his.system.enums.BizTypeEnum;
import com.his.system.mapper.SysConfigMapper;
import com.his.system.service.DutyRosterService;
import com.his.system.service.SysMessageService;
import com.his.system.vo.DutyOfficerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import com.his.system.service.DictCacheService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 床位服务中心实现
 *
 * <p><b>本类固化的业务规则</b>（每条都对应一个真实的排 incons 场景）：
 * <ol>
 *   <li><b>排序 = priority DESC → register_time ASC → id ASC</b>。不是先到先得：
 *       危重症排在普通患者前面是这个域的规矩本身，一旦能被人从外面调参数排序，队列就没意义了。</li>
 *   <li><b>安排床位 = 锁定 + 挂患者</b>（床位置 3 并写 patient_id）。不锁，
 *       两个不同的排队记录会被安排到同一张床上，而双方看到的都是"这张床是我的"。</li>
 *   <li><b>释放床位必须用 UpdateWrapper 显式 set(patientId, null)</b>：
 *       updateById 的 NOT_NULL 策略会跳过 null 字段，结果是床变空闲了但患者还挂在床上，
 *       <b>不报错</b>，只是数据脏 —— 这是本项目最典型的静默错误。</li>
 *   <li><b>已收治的排队记录不可取消、不可改需求</b>：人已经躺在医院里了，
 *       回过头改"当初排队的优先级"既不改变任何事实，也让台账失去可信度。</li>
 *   <li><b>等待超时是查询时算的</b>（{@code expired}），不改状态、不起定时任务 ——
 *       把时间流逝伪装成一次业务动作就没法区分"过期"与"被人取消"了（同住院证的口径）。</li>
 *   <li><b>普通床位需求不给 ICU / VIP 床</b>：的心血管重症资源被普通择期占用是完全不用等的代价。
 *       反过来需求 VIP 时给普通床是可以的（降级），但要在候选上标明档位。</li>
 *   <li><b>床位中心的估算不使用病区.total_beds / occupied_beds</b>（演示数据），
 *       一律 realtime COUNT 床位。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BedCenterServiceImpl implements BedCenterService {
    @Autowired
    private DictCacheService dictText;

    /**
     * 等待超时的最长天数；缺失或非法一律回落 7 天（不回落成"永不超时"）
     */
    private static final String MAX_WAIT_DAYS_KEY = "bed.wait.max_days";
    private static final int MAX_WAIT_DAYS_FALLBACK = 7;

    /**
     * 匹配候选的数量上限：全院上千张床全列出来等于没列
     */
    private static final int MATCH_LIMIT = 30;

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    /**
     * 等床多久没安排就找总值班（系统参数：duty.coord.bed_wait_hours，缺失/非法回落 24）
     */
    private static final String DUTY_BED_WAIT_HOURS_KEY = "duty.coord.bed_wait_hours";
    private static final int DUTY_BED_WAIT_HOURS_FALLBACK = 24;
    private final BizBedWaitMapper waitMapper;
    private final BedCenterMapper allocateMapper;
    private final SysBedMapper bedMapper;
    /**
     * 床位图聚合（与护士站共用，护士看在院患者，这里看可调配性）
     */
    private final BedMapMapper bedMapMapper;
    private final BizPatientMapper patientMapper;
    private final BizAdmissionOrderMapper orderMapper;
    private final BizAdmissionMapper admissionMapper;
    private final SysConfigMapper sysConfigMapper;
    /**
     * 收治复写入院主流程：不重写一套 admit，否则两条入口各推进一步就会打架
     */
    private final InpatientService inpatientService;
    /**
     * 全院当天谁负责：跨科调配与等床超时的兜底收口人（sql/169）
     */
    private final DutyRosterService dutyRosterService;
    private final SysMessageService sysMessageService;

    // 等床队列

    private static int indexOf(List<Long> ids, Long id) {
        if (ids == null || ids.isEmpty() || id == null) {
            return 0;
        }
        for (int i = 0; i < ids.size(); i++) {
            if (Objects.equals(ids.get(i), id)) {
                return i + 1;
            }
        }
        return 0;
    }

    private static String defaultStr(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private static long hoursBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            return 0;
        }
        return Math.max(0, Duration.between(TimeUtil.toSeconds(from), TimeUtil.toSeconds(to)).toHours());
    }

    /**
     * 时间统一截到秒：库表是 DATETIME(0)，写进去会被四舍五入，不截会导致"写进去的 ≠ 读回来的"
     */
    @Override
    public IPage<BedWaitVO> queuePage(BedWaitQueryPageDTO query) {
        LambdaQueryWrapper<BizBedWait> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizBedWait::getDelFlag, 0)
                .eq(query.getWaitStatus() != null, BizBedWait::getWaitStatus, query.getWaitStatus())
                .eq(query.getApplyDeptId() != null, BizBedWait::getApplyDeptId, query.getApplyDeptId())
                .eq(query.getPriority() != null, BizBedWait::getPriority, query.getPriority())
                .eq(query.getBedType() != null, BizBedWait::getBedType, query.getBedType())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(BizBedWait::getPatientName, query.getKeyword())
                        .or().like(BizBedWait::getWaitNo, query.getKeyword())
                        .or().like(BizBedWait::getPhone, query.getKeyword()));
        if (Boolean.TRUE.equals(query.getOverdueOnly())) {
            wrapper.lt(BizBedWait::getRegisterTime, LocalDateTime.now().minusDays(maxWaitDays()));
        }
        // 排序：在办的（等待中/已安排）浮到顶部，危重优先，同级按登记先后，末位补 id 保证翻页不重不漏。
        // 少了最前面那一档，昨天已收治/已取消的旧记录会挤在本页第一行 —— 队列页顶部必须是"现在要办的事"。
        wrapper.last("ORDER BY (wait_status IN (0,1)) DESC, priority DESC, register_time ASC, id ASC");

        Page<BizBedWait> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<BizBedWait> result = waitMapper.selectPage(page, wrapper);

        // 全局位次：一次拉齐等待中的顺序，避免 offset 分页里序号从页首重新数。
        // 正常院区的等待队列是几十条量级；这里一次拉齐是为了让翻到第 2 页时序号不重新从 1 开始。
        List<Long> waitingIds = Collections.emptyList();
        try {
            waitingIds = waitMapper.selectWaitingIds();
        } catch (Exception e) {
            log.warn("[床位中心] 排队位次计算失败，本次不返回 seq", e);
        }
        final List<Long> seqSource = waitingIds;

        IPage<BedWaitVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(w -> decorate(w, seqSource))
                .collect(Collectors.toList()));
        return voPage;
    }

    // 安排 / 释放 / 取消 / 收治

    @Override
    public BedWaitVO queueDetail(Long waitId) {
        // 详情页也要给全局位次：拿空列表进来 seq 恒为 0，页面上「排第几位」就成了假的
        List<Long> ids = Collections.emptyList();
        try {
            ids = waitMapper.selectWaitingIds();
        } catch (Exception e) {
            log.warn("[床位中心] 排队位次计算失败，详情本次不返回 seq", e);
        }
        return decorate(requireWait(waitId), ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsertWait(BedWaitUpsertDTO dto) {
        // 保留（类别②）：整个 DTO 为 null 不是字段校验，Bean Validation 覆盖不到
        if (dto == null) {
            throw new BusinessException("登记内容不能为空");
        }
        BizPatient patient = patientMapper.selectById(dto.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }

        BizAdmissionOrder order = null;
        if (dto.getAdmissionOrderId() != null) {
            order = orderMapper.selectById(dto.getAdmissionOrderId());
            if (order == null) {
                throw new BusinessException("住院证不存在");
            }
            if (!Objects.equals(order.getPatientId(), dto.getPatientId())) {
                throw new BusinessException("住院证与所选患者不一致（证上患者：" + order.getPatientName() + "）");
            }
            // 一张证只能排一次队：重复的队列会让入院处不知道该按哪条安排床位
            String dup = existsByOrder(dto.getAdmissionOrderId(), dto.getId());
            if (dup != null) {
                throw new BusinessException("该住院证已登记床位排队（等待号 " + dup + "），请勿重复登记");
            }
        }

        if (dto.getId() != null) {
            return updateWait(dto, order);
        }
        return insertWait(dto, order);
    }

    private Long insertWait(BedWaitUpsertDTO dto, BizAdmissionOrder order) {
        // 同一患者在同一时刻只能排一次队：两条"等待中"的记录会让床位分配的对象变得不确定
        long active = waitMapper.selectCount(new LambdaQueryWrapper<BizBedWait>()
                .eq(BizBedWait::getDelFlag, 0)
                .eq(BizBedWait::getPatientId, dto.getPatientId())
                .in(BizBedWait::getWaitStatus, BedWaitStatusEnum.PENDING.getCode(), BedWaitStatusEnum.ARRANGED.getCode()));
        if (active > 0) {
            throw new BusinessException("该患者已在床位排队队列中，不能重复登记");
        }

        BizBedWait wait = new BizBedWait();
        wait.setWaitNo(nextWaitNo());
        wait.setAdmissionOrderId(dto.getAdmissionOrderId());
        wait.setPatientId(dto.getPatientId());
        wait.setPatientNo(dto.getPatientNo());
        wait.setPatientName(dto.getPatientName());
        wait.setGender(dto.getGender());
        wait.setAge(dto.getAge());
        wait.setPhone(dto.getPhone());
        wait.setApplyDeptId(resolveApplyDeptId(dto, order));
        wait.setApplyDeptName(resolveApplyDeptName(dto, order, wait.getApplyDeptId()));
        wait.setExpectWardId(dto.getExpectWardId());
        wait.setBedType(defaultStr(dto.getBedType(), "normal"));
        wait.setPriority(dto.getPriority() == null ? 1 : dto.getPriority());
        wait.setGenderLimit(dto.getGenderLimit() == null ? 0 : dto.getGenderLimit());
        wait.setIsolationFlag(dto.getIsolationFlag() == null ? 0 : dto.getIsolationFlag());
        wait.setExpectAdmitDate(dto.getExpectAdmitDate() != null ? dto.getExpectAdmitDate()
                : order != null && order.getExpectAdmitTime() != null ? order.getExpectAdmitTime().toLocalDate() : null);
        wait.setDiagnosisName(dto.getDiagnosisName());
        wait.setWaitStatus(BedWaitStatusEnum.PENDING.getCode());
        wait.setRegisterTime(TimeUtil.toSeconds(LocalDateTime.now()));
        wait.setRemark(dto.getRemark());
        waitMapper.insert(wait);

        log.info("床位排队登记 waitNo={} waitId={} patient={} applyDept={} priority={} 床型={}",
                wait.getWaitNo(), wait.getId(), wait.getPatientName(), wait.getApplyDeptName(),
                wait.getPriority(), wait.getBedType());
        return wait.getId();
    }

    private Long updateWait(BedWaitUpsertDTO dto, BizAdmissionOrder order) {
        BizBedWait wait = requireWait(dto.getId());
        if (Objects.equals(BedWaitStatusEnum.ADMITTED.getCode(), wait.getWaitStatus())) {
            throw new BusinessException("该患者已收治入院，不能修改排队信息");
        }
        if (Objects.equals(BedWaitStatusEnum.CANCELLED.getCode(), wait.getWaitStatus())) {
            throw new BusinessException("该排队记录已取消，请重新登记");
        }
        BizBedWait upd = new BizBedWait();
        upd.setId(wait.getId());
        upd.setApplyDeptId(resolveApplyDeptId(dto, order));
        upd.setExpectWardId(dto.getExpectWardId());
        upd.setBedType(defaultStr(dto.getBedType(), wait.getBedType()));
        upd.setPriority(dto.getPriority() == null ? wait.getPriority() : dto.getPriority());
        upd.setGenderLimit(dto.getGenderLimit() == null ? wait.getGenderLimit() : dto.getGenderLimit());
        upd.setIsolationFlag(dto.getIsolationFlag() == null ? wait.getIsolationFlag() : dto.getIsolationFlag());
        upd.setExpectAdmitDate(dto.getExpectAdmitDate());
        upd.setDiagnosisName(dto.getDiagnosisName());
        upd.setPhone(dto.getPhone());
        upd.setRemark(dto.getRemark());
        // 改的是"需求"，不是"事实"：assigned_* / wait_status / register_time 一概不在这里动
        waitMapper.updateById(upd);

        log.info("床位排队修改 waitNo={} waitId={} priority={} 床型={}", wait.getWaitNo(), wait.getId(),
                upd.getPriority(), upd.getBedType());
        return wait.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignBed(BedAssignUpsertDTO dto) {
        BizBedWait wait = requireWait(dto.getWaitId());
        if (Objects.equals(BedWaitStatusEnum.ADMITTED.getCode(), wait.getWaitStatus())) {
            throw new BusinessException("该患者已收治入院，无需再安排床位");
        }
        if (Objects.equals(BedWaitStatusEnum.CANCELLED.getCode(), wait.getWaitStatus())) {
            throw new BusinessException("该排队记录已取消，请先重新登记");
        }

        SysBed bed = bedMapper.selectById(dto.getBedId());
        if (bed == null || !Objects.equals(0, bed.getDelFlag())) {
            throw new BusinessException("床位不存在");
        }
        if (!Objects.equals(BedStatusEnum.FREE.getCode(), bed.getBedStatus())) {
            throw new BusinessException("床位当前不可用（" + BedStatusEnum.labelOrUnknown(bed.getBedStatus()) + "），请选择空闲床位");
        }
        if (bed.getPatientId() != null) {
            throw new BusinessException("床位仍挂着其他患者，不能安排");
        }
        // 双保险：锁定态以外还要确认这张床没有被别的排队记录预留过。
        // 只用 bed_status 判的话，脏数据（状态是 1 但台账里有在途预留）会把同一张床给两个人。
        String conflict = existReservedAllocate(dto.getBedId());
        if (conflict != null) {
            throw new BusinessException("该床位已被其他排队记录预留（调配单 " + conflict + "），请换一张");
        }

        // 改派：先把原床退干净，再占新床。顺序反了会出现"两张床都被同一个人占着"的瞬间。
        if (Objects.equals(BedWaitStatusEnum.ARRANGED.getCode(), wait.getWaitStatus()) && wait.getAssignedBedId() != null) {
            doRelease(wait, "改派床位：释放原预留床", BedAllocateStatusEnum.VOID.getCode());
        }

        String wardName = wardName(bed.getWardId());
        String deptName = deptName(bed.getDeptId());

        LambdaUpdateWrapper<SysBed> upd = new LambdaUpdateWrapper<>();
        upd.eq(SysBed::getBedId, bed.getBedId())
                .set(SysBed::getBedStatus, BedStatusEnum.LOCKED.getCode())
                .set(SysBed::getPatientId, wait.getPatientId())
                .set(SysBed::getRemark, "床位中心预留：" + wait.getPatientName() + "（等待号 " + wait.getWaitNo() + "）");
        bedMapper.update(null, upd);
        // 锁定的床不计入病区占用数 —— 占用数的定义是 bed_status=2（人真的住进去了）

        LocalDateTime now = TimeUtil.toSeconds(LocalDateTime.now());
        BizBedWait arrange = new BizBedWait();
        arrange.setId(wait.getId());
        arrange.setWaitStatus(BedWaitStatusEnum.ARRANGED.getCode());
        arrange.setAssignedBedId(bed.getBedId());
        arrange.setAssignedBedNo(bed.getBedNo());
        arrange.setAssignedWardId(bed.getWardId());
        arrange.setAssignedWardName(wardName);
        arrange.setAssignedDeptId(bed.getDeptId());
        arrange.setAssignedDeptName(deptName);
        arrange.setAssignedTime(now);
        arrange.setAssignedBy(currentName());
        waitMapper.updateById(arrange);

        boolean cross = wait.getApplyDeptId() != null && !Objects.equals(wait.getApplyDeptId(), bed.getDeptId());
        BizBedAllocate alloc = new BizBedAllocate();
        alloc.setAllocateNo(nextAllocateNo());
        alloc.setBedId(bed.getBedId());
        alloc.setBedNo(bed.getBedNo());
        alloc.setWardId(bed.getWardId());
        alloc.setWardName(wardName);
        alloc.setOwnDeptId(bed.getDeptId());
        alloc.setOwnDeptName(deptName);
        // 使用科室取患者的拟收治科室：这样台账上"谁的床在被谁借用"一目了然
        alloc.setUseDeptId(wait.getApplyDeptId());
        alloc.setUseDeptName(wait.getApplyDeptName());
        alloc.setWaitId(wait.getId());
        alloc.setPatientId(wait.getPatientId());
        alloc.setPatientName(wait.getPatientName());
        alloc.setAllocType(cross ? 2 : 1);
        alloc.setAllocStatus(BedAllocateStatusEnum.RESERVED.getCode());
        alloc.setOperatorId(currentEmpId());
        alloc.setOperatorName(currentName());
        alloc.setOperateTime(now);
        alloc.setRemark(dto.getRemark());
        allocateMapper.insert(alloc);

        log.info("床位安排成功 waitNo={} patient={} bed={}{} 归属科室={} 使用科室={} 类型={}",
                wait.getWaitNo(), wait.getPatientName(), deptName, bed.getBedNo(),
                deptName, wait.getApplyDeptName(), cross ? "跨科调配" : "本科室预留");

        // 跨科调配必须让总值班知道：这张床属于别人的科室，患者躺在别人科里，
        // 「接收科室肯不肯收、护理归谁、什么时候搬回本科」这三件事只有总值班能拍板。
        // 此前调配只落一条台账，协调全靠打电话 —— 台账上记着、电话没打，出了事查不到人。
        if (cross) {
            notifyDutyCrossDept(wait, alloc, bed, deptName);
        }
    }

    /**
     * 跨科调配 → 给当日总值班发协调待办（bizType=duty-coord，bizId=调配单ID）。
     *
     * <p>bizId 用<b>调配单</b>而不是等床单：等床超时催办也走 duty-coord，
     * 两条事件共用一个 bizId 会被判重互相吃掉（等床催过之后，真正安排床位的那条通知就发不出来了）。
     */
    private void notifyDutyCrossDept(BizBedWait wait, BizBedAllocate alloc, SysBed bed, String ownDeptName) {
        DutyOfficerVO duty = dutyRosterService.current();
        if (duty == null || duty.getEmployeeId() == null) {
            // 今天没排总值班：不静默吞掉 —— 写日志，运维能从日志里看到"协调无人接手"
            log.warn("[床位调配] 跨科调配无人协调：waitNo={} 患者={} 占用 {} {} 床，但当日总值班未排班",
                    wait.getWaitNo(), wait.getPatientName(), ownDeptName, bed.getBedNo());
            return;
        }
        String title = "跨科调配协调：" + wait.getPatientName() + " → " + ownDeptName + bed.getBedNo() + " 床";
        String content = String.format(
                "患者 %s（等床号 %s，申请科室 %s）已安排到 %s 的 %s 床（跨科调配，调配单 %s）。"
                        + "请协调接收科室确认：护理归属、何时搬回本科室；接收科室拒收时由总值班裁定。",
                wait.getPatientName(), wait.getWaitNo(),
                wait.getApplyDeptName() == null ? "-" : wait.getApplyDeptName(),
                ownDeptName, bed.getBedNo(), alloc.getAllocateNo());
        java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("waitNo", wait.getWaitNo());
        payload.put("patientName", wait.getPatientName());
        payload.put("allocateNo", alloc.getAllocateNo());
        payload.put("ownDeptName", ownDeptName);
        payload.put("useDeptName", wait.getApplyDeptName());
        payload.put("bedNo", bed.getBedNo());
        sysMessageService.sendSystemMessage(duty.getEmployeeId(), duty.getEmployeeName(), title, content,
                BizTypeEnum.DUTY_COORD.getType(), alloc.getId(),
                "warning", cn.hutool.json.JSONUtil.toJsonStr(payload), 0);
    }

    /**
     * 等床超时 → 催当日总值班（定时 + 手工补跑）。
     *
     * <p>判重：同一条等床记录对同一收件人只催一次（{@code receiverIdsOfBiz}）。
     * 所以 1 小时一轮的扫描绝大多数轮次影响 0 条，不会把收件箱刷满；
     * 真正的闭环动作是「安排床位」或「取消排队」，做完了这条就不再被扫到。
     */
    @Override
    public int escalateWaitToDuty() {
        int hours = dutyBedWaitHours();
        LocalDateTime deadLine = LocalDateTime.now().minusHours(hours);
        List<BizBedWait> overdue = waitMapper.selectList(new LambdaQueryWrapper<BizBedWait>()
                .eq(BizBedWait::getWaitStatus, BedWaitStatusEnum.PENDING.getCode())
                .lt(BizBedWait::getRegisterTime, deadLine)
                .orderByAsc(BizBedWait::getRegisterTime));
        if (overdue.isEmpty()) {
            return 0;
        }
        DutyOfficerVO duty = dutyRosterService.current();
        if (duty == null || duty.getEmployeeId() == null) {
            log.warn("[等床协调] {} 条等床记录已等待超过 {} 小时，但当日总值班未排班，无法催办", overdue.size(), hours);
            return 0;
        }
        int sent = 0;
        for (BizBedWait wait : overdue) {
            try {
                // 时间窗判重而不是「一人一次」：等床这事儿只要没安排就会一直挂着，
                // 永久判重等于催过一次之后再无人过问；按阈值重催（默认 24 小时一次）
                // 才能保证队列里的人不会被忘掉，又不至于把收件箱刷爆。
                if (sentWithinHours(BizTypeEnum.DUTY_COORD.getType(), wait.getId(), duty.getEmployeeId(), hours)) {
                    continue;
                }
                long waited = wait.getRegisterTime() == null ? 0
                        : Duration.between(wait.getRegisterTime(), LocalDateTime.now()).toHours();
                String title = "等床超时需协调：" + wait.getPatientName() + "（已等 " + waited + " 小时）";
                String content = String.format(
                        "患者 %s（等床号 %s，申请科室 %s，%s）已等床 %d 小时仍未安排床位（阈值 %d 小时）。"
                                + "本科室无床时请总值班跨科协调床位或说明处置意见，不要让患者无限期等待。",
                        wait.getPatientName(), wait.getWaitNo(),
                        wait.getApplyDeptName() == null ? "-" : wait.getApplyDeptName(),
                        wait.getDiagnosisName() == null ? "诊断未填" : wait.getDiagnosisName(),
                        waited, hours);
                java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
                payload.put("waitNo", wait.getWaitNo());
                payload.put("patientName", wait.getPatientName());
                payload.put("applyDeptName", wait.getApplyDeptName());
                payload.put("waitedHours", waited);
                if (sysMessageService.sendSystemMessage(duty.getEmployeeId(), duty.getEmployeeName(), title, content,
                        BizTypeEnum.DUTY_COORD.getType(), wait.getId(),
                        "urgent", cn.hutool.json.JSONUtil.toJsonStr(payload), 0)) {
                    sent++;
                }
            } catch (Exception ex) {
                log.error("[等床协调] waitNo={} 催办失败：{}", wait.getWaitNo(), ex.getMessage(), ex);
            }
        }
        if (sent > 0) {
            log.warn("[等床协调] 本次向总值班发出 {} 条等床超时待办（阈值 {} 小时）", sent, hours);
        }
        return sent;
    }

    /**
     * 同一业务单对同一收件人在 {@code hours} 小时内是否发过同类待办。
     *
     * <p>为什么不用 {@code receiverIdsOfBiz}（永久判重）：催办要催到事情被解决为止，
     * 永久判重的结果是"催过一次就永远不再催"，而等床/转诊这类挂住的单恰恰会越挂越久。
     */
    private boolean sentWithinHours(String bizType, Long bizId, Long receiverId, int hours) {
        try {
            Long cnt = sysMessageService.lambdaQuery()
                    .eq(com.his.system.entity.SysMessage::getBizType, bizType)
                    .eq(com.his.system.entity.SysMessage::getBizId, bizId)
                    .eq(com.his.system.entity.SysMessage::getReceiverId, receiverId)
                    // 只跟「催办类（urgent）」判重：跨科调配那条 warning 是通知不是催办，
                    // 把它算进来会让等床催办永远发不出去（同一 waitId 已被"发过"）。
                    .eq(com.his.system.entity.SysMessage::getSeverity, "urgent")
                    .ge(com.his.system.entity.SysMessage::getSendTime, LocalDateTime.now().minusHours(hours))
                    .count();
            return cnt != null && cnt > 0;
        } catch (Exception ex) {
            // 判重查询失败不能当成"没发过"（会变成每轮狂发），也不能当成"发过"（会永远不催）：
            // 按"已发过"处理更安全，下一轮再试。
            log.error("[等床协调] 判重查询失败，本轮跳过 bizId={}：{}", bizId, ex.getMessage());
            return true;
        }
    }

    // 统计 / 床位池 / 匹配

    /**
     * 等床催总值班阈值；缺失/非法一律回落 24 小时（不回落成"永不催"）
     */
    private int dutyBedWaitHours() {
        try {
            SysConfig cfg = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                    .eq(SysConfig::getConfigKey, DUTY_BED_WAIT_HOURS_KEY).last("LIMIT 1"));
            if (cfg == null || !StringUtils.hasText(cfg.getConfigValue())) {
                return DUTY_BED_WAIT_HOURS_FALLBACK;
            }
            int v = Integer.parseInt(cfg.getConfigValue().trim());
            return v > 0 ? v : DUTY_BED_WAIT_HOURS_FALLBACK;
        } catch (Exception ex) {
            return DUTY_BED_WAIT_HOURS_FALLBACK;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseBed(BedWaitOperateDTO dto) {
        BizBedWait wait = requireWait(dto.getWaitId());
        if (!Objects.equals(BedWaitStatusEnum.ARRANGED.getCode(), wait.getWaitStatus())) {
            throw new BusinessException("只有「已安排床位」的排队记录可以退回队列（当前："
                    + BedWaitStatusEnum.labelOrUnknown(wait.getWaitStatus()) + "）");
        }
        String reason = StringUtils.hasText(dto.getReason()) ? dto.getReason() : "床位中心退回队列";
        doRelease(wait, reason, BedAllocateStatusEnum.RELEASED.getCode());
        log.info("床位退回队列 waitNo={} waitId={} 原因={}", wait.getWaitNo(), wait.getId(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelWait(BedWaitOperateDTO dto) {
        BizBedWait wait = requireWait(dto.getWaitId());
        if (Objects.equals(BedWaitStatusEnum.ADMITTED.getCode(), wait.getWaitStatus())) {
            throw new BusinessException("该患者已收治入院，不能取消排队");
        }
        if (Objects.equals(BedWaitStatusEnum.CANCELLED.getCode(), wait.getWaitStatus())) {
            throw new BusinessException("该排队记录已取消，无需重复操作");
        }
        // 保留（类别①条件必填）：同一 DTO 被「退回队列」接口复用，那里原因是选填（服务端兜默认值），
        // 字段上加 @NotBlank 会把那条合法请求一起挡成 400
        if (!StringUtils.hasText(dto.getReason())) {
            throw new BusinessException("取消原因不能为空（患者去了别的医院还是转为门诊随访，对床位周转的解释完全不同）");
        }
        // 取消前先还床：否则这张床会一直锁着一个再也不会来的人
        if (Objects.equals(BedWaitStatusEnum.ARRANGED.getCode(), wait.getWaitStatus()) && wait.getAssignedBedId() != null) {
            doRelease(wait, "取消排队：" + dto.getReason(), BedAllocateStatusEnum.RELEASED.getCode());
        }
        BizBedWait upd = new BizBedWait();
        upd.setId(wait.getId());
        upd.setWaitStatus(BedWaitStatusEnum.CANCELLED.getCode());
        upd.setCancelReason(dto.getReason());
        upd.setCancelTime(TimeUtil.toSeconds(LocalDateTime.now()));
        waitMapper.updateById(upd);
        log.info("床位排队取消 waitNo={} waitId={} 原因={}", wait.getWaitNo(), wait.getId(), dto.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String admit(BedWaitAdmitDTO dto) {
        BizBedWait wait = requireWait(dto.getWaitId());
        if (!Objects.equals(BedWaitStatusEnum.ARRANGED.getCode(), wait.getWaitStatus())) {
            throw new BusinessException("只有「已安排床位」的排队记录可以办理入院（当前："
                    + BedWaitStatusEnum.labelOrUnknown(wait.getWaitStatus()) + "）");
        }
        if (wait.getAssignedBedId() == null || wait.getAssignedWardId() == null) {
            throw new BusinessException("该排队记录没有已安排的床位，请先在床位池安排床位");
        }
        // 保留（类别①条件必填）：有住院证时途径由服务端强制为门诊，DTO 上的 @NotNull 会把这类合法请求挡成 400
        // 有证 = 门诊转住院（途径由 InpatientService 强制为 1）；无证必须有途径（病案首页必填）
        if (wait.getAdmissionOrderId() == null && dto.getAdmitWay() == null) {
            throw new BusinessException("入院途径不能为空（病案首页必填项）");
        }

        InpatientAdmitDTO admitDto = new InpatientAdmitDTO();
        admitDto.setPatientId(wait.getPatientId());
        admitDto.setWardId(wait.getAssignedWardId());
        admitDto.setBedId(wait.getAssignedBedId());
        admitDto.setAdmissionOrderId(wait.getAdmissionOrderId());
        admitDto.setAdmitDoctorId(dto.getAdmitDoctorId());
        admitDto.setAdmitTime(dto.getAdmitTime());
        admitDto.setAdmitWay(dto.getAdmitWay());
        admitDto.setAdmitDiagnosisName(wait.getDiagnosisName());
        admitDto.setRemark(StringUtils.hasText(dto.getRemark())
                ? dto.getRemark() : "床位中心收治：" + wait.getWaitNo());
        // deptId 故意不传：入院科室由床位所属病区推导。
        // 跨科调配时这一步尤其重要 —— 人躺在哪个科就归哪个科，
        // 传了"他原本想去的那个科"就会出现人在骨科床、系统在呼吸科的分裂状态。

        Long admissionId = inpatientService.admit(admitDto);

        LocalDateTime admitTime = TimeUtil.toSeconds(dto.getAdmitTime() != null ? dto.getAdmitTime() : LocalDateTime.now());
        BizBedWait upd = new BizBedWait();
        upd.setId(wait.getId());
        upd.setWaitStatus(BedWaitStatusEnum.ADMITTED.getCode());
        upd.setAdmissionId(admissionId);
        upd.setAdmitTime(admitTime);
        waitMapper.updateById(upd);

        LambdaUpdateWrapper<BizBedAllocate> allocUpd = new LambdaUpdateWrapper<>();
        allocUpd.eq(BizBedAllocate::getWaitId, wait.getId())
                .eq(BizBedAllocate::getAllocStatus, BedAllocateStatusEnum.RESERVED.getCode())
                .set(BizBedAllocate::getAllocStatus, BedAllocateStatusEnum.ADMITTED.getCode())
                .set(BizBedAllocate::getAdmissionId, admissionId);
        allocateMapper.update(null, allocUpd);

        log.info("床位中心收治成功 waitNo={} waitId={} admissionId={} patient={}",
                wait.getWaitNo(), wait.getId(), admissionId, wait.getPatientName());
        return String.valueOf(admissionId);
    }

    @Override
    public BedWaitStatsVO queueStats() {
        BedWaitStatsVO vo = new BedWaitStatsVO();
        int maxDays = maxWaitDays();
        vo.setMaxWaitDays(maxDays);

        vo.setWaitingNormal(countBy(BedWaitStatusEnum.PENDING.getCode(), 1));
        vo.setWaitingUrgent(countBy(BedWaitStatusEnum.PENDING.getCode(), 2));
        vo.setWaitingCritical(countBy(BedWaitStatusEnum.PENDING.getCode(), 3));
        vo.setWaitingTotal(vo.getWaitingNormal() + vo.getWaitingUrgent() + vo.getWaitingCritical());
        vo.setArrangedCount(countBy(BedWaitStatusEnum.ARRANGED.getCode(), null));

        LocalDate today = LocalDate.now();
        vo.setAdmittedToday(countToday(today, BedWaitStatusEnum.ADMITTED.getCode(), "admit_time"));
        vo.setCancelledToday(countToday(today, BedWaitStatusEnum.CANCELLED.getCode(), "cancel_time"));
        vo.setOverdueCount(waitMapper.selectCount(new LambdaQueryWrapper<BizBedWait>()
                .eq(BizBedWait::getDelFlag, 0)
                .eq(BizBedWait::getWaitStatus, BedWaitStatusEnum.PENDING.getCode())
                .lt(BizBedWait::getRegisterTime, LocalDateTime.now().minusDays(maxDays))));

        List<BizBedWait> waiting = waitMapper.selectList(new LambdaQueryWrapper<BizBedWait>()
                .eq(BizBedWait::getDelFlag, 0)
                .eq(BizBedWait::getWaitStatus, BedWaitStatusEnum.PENDING.getCode()));
        LocalDateTime now = LocalDateTime.now();
        long sum = 0;
        long max = 0;
        for (BizBedWait w : waiting) {
            long hours = hoursBetween(w.getRegisterTime(), now);
            sum += hours;
            max = Math.max(max, hours);
        }
        vo.setAvgWaitHours(waiting.isEmpty() ? 0 : sum / waiting.size());
        vo.setMaxWaitHours(max);

        BedOverviewVO.Summary summary = allocateMapper.selectHospitalSummary();
        vo.setTotalBeds(summary.getTotalBeds());
        vo.setFreeBeds(summary.getFreeBeds());
        vo.setLockedBeds(summary.getLockedBeds());
        vo.setOccupiedBeds(summary.getOccupiedBeds());

        vo.setCrossDeptCount(allocateMapper.selectCount(new LambdaQueryWrapper<BizBedAllocate>()
                .eq(BizBedAllocate::getDelFlag, 0)
                .eq(BizBedAllocate::getAllocStatus, BedAllocateStatusEnum.RESERVED.getCode())
                .eq(BizBedAllocate::getAllocType, 2)));
        return vo;
    }

    @Override
    public long countWaiting() {
        return countBy(BedWaitStatusEnum.PENDING.getCode(), null);
    }

    @Override
    public List<BedMatchVO> matchBeds(Long waitId) {
        BizBedWait wait = requireWait(waitId);
        if (Objects.equals(BedWaitStatusEnum.ADMITTED.getCode(), wait.getWaitStatus()) || Objects.equals(BedWaitStatusEnum.CANCELLED.getCode(), wait.getWaitStatus())) {
            throw new BusinessException("该排队记录已结束（"
                    + BedWaitStatusEnum.labelOrUnknown(wait.getWaitStatus()) + "），不需要再匹配床位");
        }
        String need = defaultStr(wait.getBedType(), "normal");
        List<BedMatchVO> candidates = allocateMapper.selectMatchableBeds();
        List<BedMatchVO> result = new ArrayList<>();
        for (BedMatchVO b : candidates) {
            if (!usableFor(need, b.getBedType())) {
                continue;
            }
            boolean sameDept = wait.getApplyDeptId() != null && Objects.equals(wait.getApplyDeptId(), b.getDeptId());
            boolean sameType = Objects.equals(need, b.getBedType());
            int level = sameDept ? (sameType ? 1 : 2) : (sameType ? 3 : 4);
            int score = switch (level) {
                case 1 -> 100;
                case 2 -> 80;
                case 3 -> 50;
                default -> 30;
            };
            boolean expectMatched = wait.getExpectWardId() != null && Objects.equals(wait.getExpectWardId(), b.getWardId());
            if (expectMatched) {
                score += 20;
            }
            b.setMatchLevel(level);
            b.setMatchLevelText(dictText.getDicDataLabel("biz_patient_bedMatchLevelEnum", level));
            b.setMatchScore(score);
            b.setBedTypeText(BedTypeEnum.getText(b.getBedType()));
            b.setExpectWardMatched(expectMatched);
            b.setGenderHint(wait.getGenderLimit() != null && !Objects.equals(0, wait.getGenderLimit()));
            b.setIsolationHint(Objects.equals(1, wait.getIsolationFlag()));
            b.setMatchReason(matchReason(level, expectMatched, sameDept, b));
            result.add(b);
        }
        // deptName / bedNo 在候选里可能为 null（靠 LEFT JOIN 出来的都有可能造不出名字），nullsFirst 顶住
        result.sort(Comparator.comparing(BedMatchVO::getMatchScore, Comparator.reverseOrder())
                .thenComparing(BedMatchVO::getDeptName, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(BedMatchVO::getBedNo, Comparator.nullsFirst(
                        Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()))));
        return result.size() > MATCH_LIMIT ? result.subList(0, MATCH_LIMIT) : result;
    }

    // 与住院证 / 入院的联动

    @Override
    public BedPoolVO bedPool(BedPoolQueryPageDTO query) {
        if (query == null) {
            query = new BedPoolQueryPageDTO();
        }
        Long deptId = query.getDeptId();
        Long wardId = query.getWardId();
        // 三元表达式两边必须是同一个包装类型：写成 `? BedStatusEnum.FREE.getCode() : query.getBedStatus()` 时
        // 左边是 int 常量、右边是 Integer，Java 会把右边拆箱 —— 前端不传 bedStatus 时直接 NPE，
        // 被兜成 500，报错文案完全不指向这里（已踩过一次）
        Integer bedStatus = Boolean.TRUE.equals(query.getAvailableOnly()) ? Integer.valueOf(BedStatusEnum.FREE.getCode()) : query.getBedStatus();
        String bedType = StringUtils.hasText(query.getBedType()) ? query.getBedType() : null;
        String keyword = StringUtils.hasText(query.getKeyword()) ? query.getKeyword().trim() : null;

        BedPoolVO vo = new BedPoolVO();
        vo.setTotal(allocateMapper.countBedPool(deptId, wardId, bedStatus, bedType, keyword));
        long offset = (Math.max(1L, query.getPageNum()) - 1) * Math.max(1L, query.getPageSize());
        List<BedPoolVO.BedRow> rows = allocateMapper.selectBedPool(deptId, wardId, bedStatus, bedType,
                keyword, offset, Math.max(1L, query.getPageSize()));
        for (BedPoolVO.BedRow row : rows) {
            row.setBedStatusText(BedStatusEnum.getText(row.getBedStatus()));
            row.setBedTypeText(BedTypeEnum.getText(row.getBedType()));
        }
        vo.setRows(rows);
        return vo;
    }

    @Override
    public BedMapVO bedMap(BedMapQueryDTO query) {
        BedMapVO result = new BedMapVO();
        List<BedMapVO.DeptOption> deptOptions = bedMapMapper.selectDeptOptions();
        result.setDeptOptions(deptOptions);

        // 1) 科室：本域刻意不调 DeptScopeProvider —— 它的存在意义就是跨科找床，按岗位收口等于瞎。
        //    但不传 deptId 时也不能把全院 1000+ 张床一次画出来（画得下也没人看），
        //    所以优先落当前账号的主岗位科室，主科室没床才退到第一个有床科室。
        Long deptId = query == null ? null : query.getDeptId();
        if (deptId == null) {
            CurrentUser user = UserUtils.getCurrentUser();
            Long ownDept = user == null ? null : user.getDeptId();
            boolean ownHasBed = ownDept != null
                    && deptOptions.stream().anyMatch(o -> ownDept.equals(o.getDeptId()));
            if (ownHasBed) {
                deptId = ownDept;
            } else if (!deptOptions.isEmpty()) {
                deptId = deptOptions.get(0).getDeptId();
            }
        }
        result.setDeptId(deptId);
        if (deptId == null) {
            result.setBeds(Collections.emptyList());
            result.setWardOptions(Collections.emptyList());
            result.setSummary(new BedMapVO.Summary());
            return result;
        }

        // 2) 病区必须落在已确定的科室内（防跨科窥探，与护士站同口径）
        Long wardId = query == null ? null : query.getWardId();
        if (wardId != null) {
            BedMapVO.WardOption owner = bedMapMapper.selectWardOwner(wardId);
            if (owner == null) {
                throw new BusinessException("病区不存在");
            }
            if (!deptId.equals(owner.getDeptId())) {
                throw new BusinessException("该病区不属于当前科室，无法查看");
            }
            result.setWardId(wardId);
            result.setWardName(owner.getWardName());
        }

        List<BedMapVO.BedCard> beds = bedMapMapper.selectBedCards(deptId, wardId);
        for (BedMapVO.BedCard bed : beds) {
            bed.setBedStatusText(BedStatusEnum.getText(bed.getBedStatus()));
            // 预留去向：锁定床必须说得出"留给谁、多急"。否则护士站看到的就是一张
            // 点开什么都没有的死床，床位中心也没法在图上直接放人。
            if (bed.getReservedPriority() != null) {
                bed.setReservedPriorityText(BedPriorityEnum.getText(bed.getReservedPriority()));
            }
            if (bed.getAllocType() != null) {
                bed.setAllocTypeText(BedAllocTypeEnum.getText(bed.getAllocType()));
            }
            // 动作可用性一律服务端算：前端不自判状态机，避免"页面说能点、接口说不行"
            Integer st = bed.getBedStatus();
            bed.setCanReserve(Objects.equals(BedStatusEnum.FREE.getCode(), st));
            boolean reserved = Objects.equals(BedStatusEnum.LOCKED.getCode(), st) && bed.getReservedWaitId() != null;
            bed.setCanRelease(reserved);
            bed.setCanAdmit(reserved);
        }
        result.setBeds(beds);
        if (!beds.isEmpty()) {
            result.setDeptName(beds.get(0).getDeptName());
        }
        result.setWardOptions(bedMapMapper.selectWardOptions(deptId));
        result.setSummary(summarizeMap(beds));
        return result;
    }

    /**
     * 调配视角的顶部统计：只算"床能不能用"这一维。
     * 护理级别/术后天数/危重那些是护士站视角的统计，这里不掺 —— 一张图两种诉求混着算，
     * 最后谁都不知道那个百分比在说什么。
     */
    private BedMapVO.Summary summarizeMap(List<BedMapVO.BedCard> beds) {
        BedMapVO.Summary s = new BedMapVO.Summary();
        for (BedMapVO.BedCard bed : beds) {
            s.setTotalBeds(s.getTotalBeds() + 1);
            Integer st = bed.getBedStatus();
            if (st == null) {
                continue;
            }
            BedStatusEnum bedStatus = BedStatusEnum.fromCode(st);
            if (bedStatus == null) {
                continue;
            }
            switch (bedStatus) {
                case FREE -> s.setFree(s.getFree() + 1);
                case OCCUPIED -> s.setOccupied(s.getOccupied() + 1);
                case REPAIR -> s.setRepair(s.getRepair() + 1);
                case LOCKED -> s.setLocked(s.getLocked() + 1);
            }
        }
        // 使用率的分母排除维修与预留：这两类床本来就不可能被占用，算进分母会让"还有多少床可用"失真
        int usable = s.getTotalBeds() - s.getRepair() - s.getLocked();
        s.setUsageRate(usable <= 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(s.getOccupied()).multiply(BigDecimal.valueOf(100L))
                .divide(BigDecimal.valueOf(usable), 1, RoundingMode.HALF_UP));
        return s;
    }

    // 内部

    @Override
    public BedOverviewVO overview() {
        BedOverviewVO vo = new BedOverviewVO();
        BedOverviewVO.Summary summary = allocateMapper.selectHospitalSummary();
        summary.setUsableBeds(summary.getTotalBeds() - summary.getRepairBeds());
        summary.setUsageRate(summary.getUsableBeds() <= 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(summary.getOccupiedBeds())
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(summary.getUsableBeds()), 1, RoundingMode.HALF_UP));
        summary.setLentOutBeds(allocateMapper.selectCount(new LambdaQueryWrapper<BizBedAllocate>()
                .eq(BizBedAllocate::getDelFlag, 0)
                .eq(BizBedAllocate::getAllocStatus, BedAllocateStatusEnum.RESERVED.getCode())));
        vo.setSummary(summary);
        vo.setDeptRows(allocateMapper.selectDeptRows());
        return vo;
    }

    @Override
    public void syncFromOrder(BizAdmissionOrder order) {
        if (order == null || order.getId() == null) {
            return;
        }
        if (existsByOrder(order.getId(), null) != null) {
            return;
        }
        // 患者已经有一条无证排队的记录 → 把这张证接上去，而不是新建一个平行的队列。
        // 「有证无队」是数据断裂（拿这张证去入院处收治时，队列里查不到人在等），
        // 一张证在系统里必须有且只有一条排队记录对应的事实。
        BizBedWait pending = waitMapper.selectOne(new LambdaQueryWrapper<BizBedWait>()
                .eq(BizBedWait::getDelFlag, 0)
                .eq(BizBedWait::getPatientId, order.getPatientId())
                .isNull(BizBedWait::getAdmissionOrderId)
                .in(BizBedWait::getWaitStatus, BedWaitStatusEnum.PENDING.getCode(), BedWaitStatusEnum.ARRANGED.getCode())
                .orderByDesc(BizBedWait::getId).last("LIMIT 1"));
        if (pending != null) {
            BizBedWait linkUpd = new BizBedWait();
            linkUpd.setId(pending.getId());
            linkUpd.setAdmissionOrderId(order.getId());
            linkUpd.setRemark("系统补充：关联住院证 " + order.getOrderNo());
            waitMapper.updateById(linkUpd);
            log.info("已有排队记录关联住院证 waitNo={} orderNo={} patient={}",
                    pending.getWaitNo(), order.getOrderNo(), order.getPatientName());
            return;
        }
        long active = waitMapper.selectCount(new LambdaQueryWrapper<BizBedWait>()
                .eq(BizBedWait::getDelFlag, 0)
                .eq(BizBedWait::getPatientId, order.getPatientId())
                .in(BizBedWait::getWaitStatus, BedWaitStatusEnum.PENDING.getCode(), BedWaitStatusEnum.ARRANGED.getCode()));
        if (active > 0) {
            // 已经在队列里的人再开一张证不该被队列拒绝，也不该产生第二条排队记录
            log.info("患者{}已在床位排队中，本次开证不重复入队", order.getPatientName());
            return;
        }
        BizBedWait wait = new BizBedWait();
        wait.setWaitNo(nextWaitNo());
        wait.setAdmissionOrderId(order.getId());
        wait.setPatientId(order.getPatientId());
        wait.setPatientNo(order.getPatientNo());
        wait.setPatientName(order.getPatientName());
        wait.setGender(order.getGender());
        wait.setAge(order.getAge());
        wait.setPhone(order.getPhone());
        wait.setApplyDeptId(order.getApplyDeptId());
        wait.setApplyDeptName(order.getApplyDeptName());
        wait.setBedType("normal");
        wait.setPriority(1);
        wait.setGenderLimit(0);
        wait.setIsolationFlag(0);
        wait.setExpectAdmitDate(order.getExpectAdmitTime() != null ? order.getExpectAdmitTime().toLocalDate() : null);
        wait.setDiagnosisName(order.getDiagnosisName());
        wait.setWaitStatus(BedWaitStatusEnum.PENDING.getCode());
        wait.setRegisterTime(TimeUtil.toSeconds(order.getOrderTime() != null ? order.getOrderTime() : LocalDateTime.now()));
        wait.setRemark("系统自动：随住院证 " + order.getOrderNo() + " 入队");
        waitMapper.insert(wait);
        log.info("住院证自动入床队列 waitNo={} orderNo={} patient={}", wait.getWaitNo(), order.getOrderNo(), order.getPatientName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelByOrder(Long orderId, String reason) {
        if (orderId == null) {
            return;
        }
        BizBedWait wait = waitMapper.selectOne(new LambdaQueryWrapper<BizBedWait>()
                .eq(BizBedWait::getDelFlag, 0)
                .eq(BizBedWait::getAdmissionOrderId, orderId)
                .in(BizBedWait::getWaitStatus, BedWaitStatusEnum.PENDING.getCode(), BedWaitStatusEnum.ARRANGED.getCode())
                .orderByDesc(BizBedWait::getId).last("LIMIT 1"));
        if (wait == null) {
            return;
        }
        if (Objects.equals(BedWaitStatusEnum.ARRANGED.getCode(), wait.getWaitStatus()) && wait.getAssignedBedId() != null) {
            doRelease(wait, reason, BedAllocateStatusEnum.RELEASED.getCode());
            // doRelease 会把 wait_status 拉回 PENDING，这里再覆盖成 CANCELLED
        }
        BizBedWait upd = new BizBedWait();
        upd.setId(wait.getId());
        upd.setWaitStatus(BedWaitStatusEnum.CANCELLED.getCode());
        upd.setCancelReason(reason);
        upd.setCancelTime(TimeUtil.toSeconds(LocalDateTime.now()));
        waitMapper.updateById(upd);
        log.info("住院证作废联动退出队列 waitNo={} waitId={} 原因={}", wait.getWaitNo(), wait.getId(), reason);
    }

    @Override
    public void markAdmittedByPatient(Long patientId, Long admissionId, LocalDateTime admitTime) {
        if (patientId == null || admissionId == null) {
            return;
        }
        List<BizBedWait> rows = waitMapper.selectList(new LambdaQueryWrapper<BizBedWait>()
                .eq(BizBedWait::getDelFlag, 0)
                .eq(BizBedWait::getPatientId, patientId)
                .in(BizBedWait::getWaitStatus, BedWaitStatusEnum.PENDING.getCode(), BedWaitStatusEnum.ARRANGED.getCode()));
        if (rows.isEmpty()) {
            return;
        }
        LocalDateTime time = TimeUtil.toSeconds(admitTime == null ? LocalDateTime.now() : admitTime);
        for (BizBedWait w : rows) {
            BizBedWait upd = new BizBedWait();
            upd.setId(w.getId());
            upd.setWaitStatus(BedWaitStatusEnum.ADMITTED.getCode());
            upd.setAdmissionId(admissionId);
            upd.setAdmitTime(time);
            waitMapper.updateById(upd);

            LambdaUpdateWrapper<BizBedAllocate> allocUpd = new LambdaUpdateWrapper<>();
            allocUpd.eq(BizBedAllocate::getWaitId, w.getId())
                    .eq(BizBedAllocate::getAllocStatus, BedAllocateStatusEnum.RESERVED.getCode())
                    .set(BizBedAllocate::getAllocStatus, BedAllocateStatusEnum.ADMITTED.getCode())
                    .set(BizBedAllocate::getAdmissionId, admissionId);
            allocateMapper.update(null, allocUpd);
        }
        log.info("入院回填队列：patientId={} 收治={} 条，admissionId={}", patientId, rows.size(), admissionId);
    }

    /**
     * 释放当前锁定的床位，并按结局收敛台账：
     * 改派用 4-已作废（那张床确实被拿走过，只是没用上），退回/取消用 3-已释放。
     */
    private void doRelease(BizBedWait wait, String reason, int allocEndStatus) {
        Long bedId = wait.getAssignedBedId();
        if (bedId != null) {
            SysBed bed = bedMapper.selectById(bedId);
            if (bed != null && Objects.equals(BedStatusEnum.LOCKED.getCode(), bed.getBedStatus())) {
                // 必须显式 set null：updateById 的 NOT_NULL 策略会跳过 null，留下"床空了人还挂着"的脏数据
                LambdaUpdateWrapper<SysBed> upd = new LambdaUpdateWrapper<>();
                upd.eq(SysBed::getBedId, bedId)
                        .set(SysBed::getBedStatus, BedStatusEnum.FREE.getCode())
                        .set(SysBed::getPatientId, null)
                        .set(SysBed::getRemark, null);
                bedMapper.update(null, upd);
            }
            LambdaUpdateWrapper<BizBedAllocate> allocUpd = new LambdaUpdateWrapper<>();
            allocUpd.eq(BizBedAllocate::getBedId, bedId)
                    .eq(BizBedAllocate::getWaitId, wait.getId())
                    .eq(BizBedAllocate::getAllocStatus, BedAllocateStatusEnum.RESERVED.getCode())
                    .set(BizBedAllocate::getAllocStatus, allocEndStatus)
                    .set(BizBedAllocate::getReleaseTime, TimeUtil.toSeconds(LocalDateTime.now()))
                    .set(BizBedAllocate::getReleaseReason, reason);
            allocateMapper.update(null, allocUpd);
        }

        LambdaUpdateWrapper<BizBedWait> upd = new LambdaUpdateWrapper<>();
        upd.eq(BizBedWait::getId, wait.getId())
                // set null 必须用 Wrapper：见上面那条 NOT_NULL 的坑
                .set(BizBedWait::getWaitStatus, BedWaitStatusEnum.PENDING.getCode())
                .set(BizBedWait::getAssignedBedId, null)
                .set(BizBedWait::getAssignedBedNo, null)
                .set(BizBedWait::getAssignedWardId, null)
                .set(BizBedWait::getAssignedWardName, null)
                .set(BizBedWait::getAssignedDeptId, null)
                .set(BizBedWait::getAssignedDeptName, null)
                .set(BizBedWait::getAssignedTime, null)
                .set(BizBedWait::getAssignedBy, null);
        waitMapper.update(null, upd);
    }

    /**
     * 等待时长的人读文案（时长格式化，不是码值映射）。
     *
     * <p>只表达"等了多久"，不做任何判断 —— 是否超时由 {@code expired} 单独给，
     * 不要在这里把"等了 8 天"直接说成"已超时"（阈值是可配的，写死在文案里改配置就失效）。
     */
    private static String waitDurationText(long hours) {
        if (hours < 1) {
            return "不足 1 小时";
        }
        if (hours < 24) {
            return hours + " 小时";
        }
        long days = hours / 24;
        long rest = hours % 24;
        return rest == 0 ? days + " 天" : days + " 天 " + rest + " 小时";
    }

    private BedWaitVO decorate(BizBedWait wait, List<Long> waitingIds) {
        BedWaitVO vo = new BedWaitVO();
        BeanUtils.copyProperties(wait, vo);
        vo.setGenderText(SysGenderEnum.getText(wait.getGender()));
        vo.setBedTypeText(BedTypeEnum.getText(wait.getBedType()));
        vo.setPriorityText(BedPriorityEnum.getText(wait.getPriority()));
        vo.setGenderLimitText(BedGenderLimitEnum.getText(wait.getGenderLimit()));
        vo.setWaitStatusText(BedWaitStatusEnum.getText(wait.getWaitStatus()));

        long hours = hoursBetween(wait.getRegisterTime(), LocalDateTime.now());
        vo.setWaitHours(hours);
        vo.setWaitDurationText(waitDurationText(hours));
        // 超时是查询时算出来的展示态：wait_status 仍然是「等待中」，
        // 前端要同时看到这两件事 —— 只看状态会把过期排队当成可用，只看超时又会丢掉真实状态
        vo.setExpired(Objects.equals(BedWaitStatusEnum.PENDING.getCode(), wait.getWaitStatus()) && hours >= maxWaitDays() * 24L);

        vo.setCrossDept(wait.getAssignedDeptId() != null && wait.getApplyDeptId() != null
                && !Objects.equals(wait.getAssignedDeptId(), wait.getApplyDeptId()));

        vo.setSeq(indexOf(waitingIds, wait.getId()));

        if (wait.getAdmissionId() != null) {
            vo.setAdmissionNo(admissionNo(wait.getAdmissionId()));
        }
        if (wait.getAdmissionOrderId() != null) {
            BizAdmissionOrder order = orderMapper.selectById(wait.getAdmissionOrderId());
            vo.setAdmissionOrderNo(order == null ? null : order.getOrderNo());
        }

        boolean pending = Objects.equals(BedWaitStatusEnum.PENDING.getCode(), wait.getWaitStatus());
        boolean arranged = Objects.equals(BedWaitStatusEnum.ARRANGED.getCode(), wait.getWaitStatus());
        // 操作可用性全部后端判：前端自己拼状态机的话，"已安排能不能改派"要在两边各写一遍，然后各自漂移
        vo.setCanAssign(pending || arranged);
        vo.setCanRelease(arranged);
        vo.setCanCancel(pending || arranged);
        vo.setCanAdmit(arranged);
        return vo;
    }

    private BizBedWait requireWait(Long waitId) {
        // 保留（类别②非 web 入口）：私有方法被本类多个动作入口复用（GET 标量参数与 DTO 字段都从这里取值），
        // 不经 HTTP 参数绑定，Bean Validation 不生效
        if (waitId == null) {
            throw new BusinessException("排队记录ID不能为空");
        }
        BizBedWait wait = waitMapper.selectById(waitId);
        if (wait == null) {
            throw new BusinessException("排队记录不存在");
        }
        return wait;
    }

    /**
     * 该住院证是否已经在队列里（返回重复记录的等待号）
     */
    private String existsByOrder(Long orderId, Long excludeWaitId) {
        LambdaQueryWrapper<BizBedWait> w = new LambdaQueryWrapper<>();
        w.eq(BizBedWait::getDelFlag, 0).eq(BizBedWait::getAdmissionOrderId, orderId);
        if (excludeWaitId != null) {
            w.ne(BizBedWait::getId, excludeWaitId);
        }
        BizBedWait dup = waitMapper.selectOne(w.orderByDesc(BizBedWait::getId).last("LIMIT 1"));
        return dup == null ? null : dup.getWaitNo();
    }

    /**
     * 该床位是否已有在途预留（返回调配单号）
     */
    private String existReservedAllocate(Long bedId) {
        BizBedAllocate alloc = allocateMapper.selectOne(new LambdaQueryWrapper<BizBedAllocate>()
                .eq(BizBedAllocate::getDelFlag, 0)
                .eq(BizBedAllocate::getBedId, bedId)
                .eq(BizBedAllocate::getAllocStatus, BedAllocateStatusEnum.RESERVED.getCode())
                .orderByDesc(BizBedAllocate::getId).last("LIMIT 1"));
        return alloc == null ? null : alloc.getAllocateNo();
    }

    /**
     * 普通需求不许占用专科资源，反过来可以降级。
     *
     * <p>把 ICU / VIP 床安排给普通择期患者，代价是真正需要的人来的时候没床 ——
     * 这是"资源被低效占用"，比让普通患者多等两天严重得多。
     */
    private boolean usableFor(String need, String bedType) {
        String type = defaultStr(bedType, "normal");
        if (Objects.equals(need, "ICU")) {
            return Objects.equals("ICU", type);
        }
        if (Objects.equals(need, "VIP")) {
            return Objects.equals("VIP", type) || Objects.equals("normal", type);
        }
        return Objects.equals("normal", type);
    }

    private String matchReason(int level, boolean expectMatched, boolean sameDept, BedMatchVO bed) {
        String base = switch (level) {
            case 1 -> "本科室 " + bed.getDeptName() + "，床位类型完全匹配";
            case 2 -> "本科室 " + bed.getDeptName() + "，床位类型降级可用";
            case 3 -> "跨科调配（归属 " + bed.getDeptName() + "），床位类型完全匹配";
            default -> "跨科调配（归属 " + bed.getDeptName() + "），床位类型降级可用";
        };
        return expectMatched ? base + "，且命中期望病区 " + bed.getWardName() : base;
    }

    private long countBy(int waitStatus, Integer priority) {
        LambdaQueryWrapper<BizBedWait> w = new LambdaQueryWrapper<>();
        w.eq(BizBedWait::getDelFlag, 0).eq(BizBedWait::getWaitStatus, waitStatus);
        if (priority != null) {
            w.eq(BizBedWait::getPriority, priority);
        }
        return waitMapper.selectCount(w);
    }

    private long countToday(LocalDate today, int waitStatus, String timeCol) {
        LocalDateTime start = today.atStartOfDay();
        LambdaQueryWrapper<BizBedWait> w = new LambdaQueryWrapper<>();
        w.eq(BizBedWait::getDelFlag, 0)
                .eq(BizBedWait::getWaitStatus, waitStatus)
                .ge(waitStatus == BedWaitStatusEnum.ADMITTED.getCode() ? BizBedWait::getAdmitTime : BizBedWait::getCancelTime, start)
                .lt(waitStatus == BedWaitStatusEnum.ADMITTED.getCode() ? BizBedWait::getAdmitTime : BizBedWait::getCancelTime, start.plusDays(1));
        return waitMapper.selectCount(w);
    }

    private String admissionNo(Long admissionId) {
        try {
            BizAdmission admission = admissionMapper.selectById(admissionId);
            return admission == null ? null : admission.getAdmissionNo();
        } catch (Exception e) {
            log.warn("[床位中心] 住院号查询失败 admissionId={}", admissionId, e);
            return null;
        }
    }

    private String wardName(Long wardId) {
        return wardId == null ? null : allocateMapper.selectWardName(wardId);
    }

    /**
     * 科室名称
     * <p>注意科室的主键叫 {@code id} 不叫 dept_id（本项目已踩过一次），
     * 所以这里只能单查，不能拿 SysBedMapper.selectWardById 顶。
     */
    private String deptName(Long deptId) {
        return deptId == null ? null : allocateMapper.selectDeptName(deptId);
    }

    private Long resolveApplyDeptId(BedWaitUpsertDTO dto, BizAdmissionOrder order) {
        if (dto.getApplyDeptId() != null) {
            return dto.getApplyDeptId();
        }
        if (order != null && order.getApplyDeptId() != null) {
            return order.getApplyDeptId();
        }
        return null;
    }

    private String resolveApplyDeptName(BedWaitUpsertDTO dto, BizAdmissionOrder order, Long deptId) {
        if (StringUtils.hasText(dto.getApplyDeptName())) {
            return dto.getApplyDeptName();
        }
        if (order != null && StringUtils.hasText(order.getApplyDeptName())) {
            return order.getApplyDeptName();
        }
        return deptId == null ? null : deptName(deptId);
    }

    private String nextWaitNo() {
        String prefix = "DC" + LocalDate.now().format(NO_DATE);
        return prefix + String.format("%03d", waitMapper.countByWaitNoPrefix(prefix) + 1);
    }

    private String nextAllocateNo() {
        String prefix = "TP" + LocalDate.now().format(NO_DATE);
        return prefix + String.format("%03d", allocateMapper.countByAllocateNoPrefix(prefix) + 1);
    }

    /**
     * 最长等待天数：读不到或非法一律回落 7 天（不回落成"永不超时"）
     */
    private int maxWaitDays() {
        try {
            SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                    .eq(SysConfig::getConfigKey, MAX_WAIT_DAYS_KEY));
            if (config == null || !StringUtils.hasText(config.getConfigValue())) {
                return MAX_WAIT_DAYS_FALLBACK;
            }
            int days = Integer.parseInt(config.getConfigValue().trim());
            return days > 0 ? days : MAX_WAIT_DAYS_FALLBACK;
        } catch (Exception e) {
            log.warn("配置 {} 读取失败，按兜底值 {} 天", MAX_WAIT_DAYS_KEY, MAX_WAIT_DAYS_FALLBACK);
            return MAX_WAIT_DAYS_FALLBACK;
        }
    }

    /**
     * 留痕一律用员工ID（不是用户的ID），与医嘱/站内信同一口径
     */
    private Long currentEmpId() {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            if (user == null) {
                return null;
            }
            return user.getEmployeeId() != null ? user.getEmployeeId() : user.getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    private String currentName() {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            if (user == null) {
                return null;
            }
            if (StringUtils.hasText(user.getEmployeeName())) {
                return user.getEmployeeName();
            }
            if (StringUtils.hasText(user.getRealName())) {
                return user.getRealName();
            }
            return user.getUsername();
        } catch (Exception e) {
            return null;
        }
    }
}