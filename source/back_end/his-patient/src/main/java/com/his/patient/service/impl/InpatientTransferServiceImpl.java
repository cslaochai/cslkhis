package com.his.patient.service.impl;
import com.his.common.util.TimeUtil;
import com.his.patient.enums.BedStatusEnum;
import com.his.patient.enums.InpatientOrderStatusEnum;
import com.his.patient.enums.InpatientRecordTypeEnum;
import com.his.patient.enums.OrderTypeEnum;
import com.his.patient.enums.SummaryStatusEnum;
import com.his.patient.enums.TransferStatusEnum;
import com.his.patient.enums.TransferTypeEnum;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.enums.AdmitStatusEnum;
import com.his.common.enums.RecordStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.patient.dto.InpatientTransferAcceptDTO;
import com.his.patient.dto.InpatientTransferCancelDTO;
import com.his.patient.dto.InpatientTransferQueryPageDTO;
import com.his.patient.dto.InpatientTransferUpsertDTO;
import com.his.patient.entity.*;
import com.his.patient.mapper.*;
import com.his.patient.service.InpatientOrderService;
import com.his.patient.service.InpatientTransferService;
import com.his.patient.vo.InpatientTransferVO;
import com.his.patient.vo.WardVO;
import com.his.system.utils.UserUtils;
import com.his.system.entity.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 住院转科实现（P4.2：发起 → 转入科室接收 → 停原医嘱 + 换科室换床 + 回写病历）。
 *
 * <p>除了"两段式"这条主结构，本类还固化了几个**一定会被追问**的点：
 *
 * <ol>
 *   <li><b>接收时重新校验目标床位</b>：发起时看到的空闲床，到接收时可能已被别人占用。
 *       拿 save 时的快照当事实，会把两个患者塞进同一张床（而且不报错）。</li>
 *   <li><b>入院科室不可变</b>：入院记录.admit_dept_id 入院时写死，
 *       科室ID 是"当前科室"。不分开的话，第一次转科就把入院科别永久冲掉，
 *       病案首页再也填不出来。</li>
 *   <li><b>医嘱处置必须落到纸面</b>：能停的长期医嘱随转科停掉，停不掉的（待校对）
 *       逐条写进医嘱备注 —— 「停止」是医疗决定，不能被转科顺手做掉、
 *       更不能被静默跳过。</li>
 *   <li><b>转科必须留下病历</b>：接收即回写 {@code record_type=10} 的转科记录，
 *       并回填记录ID。做了却没写，就是四核对里"病历有医嘱没记"那一类缺陷。</li>
 *   <li><b>时间截到秒</b>：库表是 {@code DATETIME(0)}，MySQL 会四舍五入，不截就会
 *       "写进去的 ≠ 读回来的"。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InpatientTransferServiceImpl implements InpatientTransferService {

    // 转科状态

    // 床位状态

    // 入院状态

    // 医嘱

    // 病历

    // 病案首页

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BizInpatientTransferMapper transferMapper;
    private final BizAdmissionMapper admissionMapper;
    private final SysBedMapper bedMapper;
    private final BizPatientMapper patientMapper;
    private final BizInpatientRecordMapper recordMapper;
    private final BizInpatientSummaryMapper summaryMapper;
    private final BizInpatientOrderMapper orderMapper;
    private final InpatientOrderService orderService;

    // 查询

    @Override
    public IPage<InpatientTransferVO> listPage(InpatientTransferQueryPageDTO query) {
        if (query == null) {
            query = new InpatientTransferQueryPageDTO();
        }
        IPage<BizInpatientTransfer> page = transferMapper.selectTransferPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query);
        List<BizInpatientTransfer> records = page.getRecords();

        Map<Long, String> recordNos = loadRecordNos(records);
        Map<Long, Integer> admitStatus = loadAdmitStatus(records);

        return page.convert(e -> toVO(e,
                recordNos.get(e.getRecordId()),
                admitStatus.get(e.getAdmissionId())));
    }

    @Override
    public InpatientTransferVO getDetailById(Long transferId) {
        // 保留（类别②）：入参是普通 Long（GET @RequestParam 绑定，非 request DTO 字段），注解无处安放
        if (transferId == null) {
            throw new BusinessException("转科记录ID不能为空");
        }
        BizInpatientTransfer entity = transferMapper.selectById(transferId);
        if (entity == null) {
            throw new BusinessException("转科记录不存在");
        }
        Map<Long, String> recordNos = loadRecordNos(List.of(entity));
        Map<Long, Integer> admitStatus = loadAdmitStatus(List.of(entity));
        return toVO(entity, recordNos.get(entity.getRecordId()), admitStatus.get(entity.getAdmissionId()));
    }

    @Override
    public List<InpatientTransferVO> listByAdmission(Long admissionId) {
        // 保留（类别②）：入参是普通 Long（GET @RequestParam 绑定，非 request DTO 字段），注解无处安放
        if (admissionId == null) {
            throw new BusinessException("入院ID不能为空");
        }
        List<BizInpatientTransfer> list = transferMapper.selectByAdmission(admissionId);
        if (list.isEmpty()) {
            return List.of();
        }
        Map<Long, String> recordNos = loadRecordNos(list);
        Map<Long, Integer> admitStatus = loadAdmitStatus(list);
        return list.stream()
                .map(e -> toVO(e, recordNos.get(e.getRecordId()), admitStatus.get(e.getAdmissionId())))
                .collect(Collectors.toList());
    }

    @Override
    public long countPending(Long toDeptId, Long admissionId) {
        return transferMapper.countPending(toDeptId, admissionId);
    }

    // 发起

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String save(InpatientTransferUpsertDTO dto) {
        int type = dto.getTransferType() == null ? 1 : dto.getTransferType();

        BizAdmission admission = admissionMapper.selectById(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        if (!Objects.equals(AdmitStatusEnum.IN_HOSPITAL.getCode(), admission.getAdmitStatus())) {
            throw new BusinessException("该患者已出院，不能转科");
        }
        // 同科室请走换床：两者后果完全不同（换床不动科室、不停医嘱、不改首页），
        // 允许同科室走转科，等于每天制造一批"科室没变但转科次数 +1"的假轨迹。
        if (Objects.equals(admission.getDeptId(), dto.getToDeptId())) {
            throw new BusinessException("转入科室与原科室相同，同科室挪床请走「换床」");
        }
        if (transferMapper.countPendingByAdmission(dto.getAdmissionId()) > 0) {
            throw new BusinessException("该患者已有一条待接收的转科申请，请先由转入科室接收或取消");
        }

        SysBed bed = bedMapper.selectById(dto.getToBedId());
        if (bed == null) {
            throw new BusinessException("转入床位不存在");
        }
        checkTargetBed(bed, dto.getToDeptId(), dto.getToWardId());

        WardVO toWard = bedMapper.selectWardById(dto.getToWardId());
        if (toWard == null) {
            throw new BusinessException("转入病区不存在");
        }
        if (!Objects.equals(toWard.getDeptId(), dto.getToDeptId())) {
            throw new BusinessException("转入病区不属于转入科室（病区「" + toWard.getWardName()
                    + "」属于 " + toWard.getDeptName() + "）");
        }

        BizPatient patient = patientMapper.selectById(admission.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }

        LocalDateTime now = TimeUtil.toSeconds(LocalDateTime.now());

        BizInpatientTransfer entity = new BizInpatientTransfer();
        entity.setTransferNo(nextTransferNo());
        entity.setAdmissionId(admission.getAdmissionId());
        entity.setAdmissionNo(admission.getAdmissionNo());
        entity.setPatientId(admission.getPatientId());
        entity.setPatientName(patient.getPatientName());

        // 转出侧全部取快照：发起之后、接收之前，患者还可能换了床（同科室换床不受待接收限制），
        // 真正生效时以接收那一刻的 admission 为准 —— 见 accept()
        entity.setFromDeptId(admission.getDeptId());
        entity.setFromDeptName(deptNameOf(admission.getDeptId()));
        entity.setFromWardId(admission.getWardId());
        entity.setFromWardName(wardNameOf(admission.getWardId()));
        entity.setFromBedId(admission.getBedId());
        entity.setFromBedNo(bedNoOf(admission.getBedId()));

        entity.setToDeptId(dto.getToDeptId());
        entity.setToDeptName(toWard.getDeptName());
        entity.setToWardId(dto.getToWardId());
        entity.setToWardName(toWard.getWardName());
        entity.setToBedId(bed.getBedId());
        entity.setToBedNo(bed.getBedNo());

        entity.setTransferType(type);
        entity.setTransferReason(dto.getTransferReason());
        entity.setHospitalDays(calcHospitalDays(admission.getAdmitTime(), now));
        entity.setStopOrdersCount(0);
        entity.setApplyDoctorId(currentEmpId());
        entity.setApplyDoctorName(currentName());
        entity.setApplyTime(now);
        entity.setTransferStatus(TransferStatusEnum.PENDING.getCode());
        if (StringUtils.hasText(dto.getRemark())) {
            entity.setRemark(dto.getRemark());
        }
        transferMapper.insert(entity);

        log.info("转科申请已提交 transferNo={} admissionId={} {} → {} 床位 {} 发起人={}",
                entity.getTransferNo(), admission.getAdmissionId(),
                entity.getFromDeptName(), entity.getToDeptName(), entity.getToBedNo(), entity.getApplyDoctorName());
        return entity.getTransferNo();
    }

    // 接收（转科在这一刻才真正生效）

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void accept(InpatientTransferAcceptDTO dto) {
        BizInpatientTransfer entity = transferMapper.selectById(dto.getTransferId());
        if (entity == null) {
            throw new BusinessException("转科记录不存在");
        }
        if (!Objects.equals(TransferStatusEnum.PENDING.getCode(), entity.getTransferStatus())) {
            throw new BusinessException("该转科申请当前状态为「"
                    + TransferStatusEnum.labelOrUnknown(entity.getTransferStatus())
                    + "」，只有「待接收」可以接收");
        }

        BizAdmission admission = admissionMapper.selectById(entity.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在，无法接收转科");
        }
        if (!Objects.equals(AdmitStatusEnum.IN_HOSPITAL.getCode(), admission.getAdmitStatus())) {
            throw new BusinessException("该患者已办理出院，不能接收转科；请取消本次申请");
        }

        // ★ 关键：重新校验目标床位。save 时它空闲，现在可能已被别人占用。
        SysBed newBed = bedMapper.selectById(entity.getToBedId());
        if (newBed == null) {
            throw new BusinessException("转入床位已不存在，请取消本次转科并重新申请");
        }
        checkTargetBed(newBed, entity.getToDeptId(), entity.getToWardId());

        // 患者可能在待接收期间被换了床，转出侧以**当前**实际床位为准（轨迹仍保留发起时的快照值）
        Long oldBedId = admission.getBedId();
        Long oldWardId = admission.getWardId();

        LocalDateTime now = TimeUtil.toSeconds(LocalDateTime.now());

        // 1) 医嘱：先处置，后换科。停不掉的一定写进 order_remark，绝不静默。
        String orderRemark = settleLongOrders(entity);

        // 2) 床位：释放原床 + 占用新床
        releaseBed(oldBedId);
        occupyBed(newBed, admission.getPatientId(), newBed.getWardId());

        // 3) 入院记录：dept_id 是"当前科室"，随转科更新；admit_dept_id 一动不动
        admission.setDeptId(entity.getToDeptId());
        admission.setWardId(newBed.getWardId());
        admission.setBedId(newBed.getBedId());
        admissionMapper.updateById(admission);
        if (oldWardId != null && !Objects.equals(oldWardId, newBed.getWardId())) {
            bedMapper.syncWardOccupied(oldWardId);
        }

        // 4) 病案首页：草稿态才同步（dept = 出院科别），并补入院科别
        String summaryNote = syncSummary(entity, admission, newBed);

        // 5) 回写转科记录病历（record_type=10）
        BizPatient patient = patientMapper.selectById(admission.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在，无法回写转科病历");
        }
        BizInpatientRecord record = writeBackRecord(entity, patient, orderRemark, now);

        // 6) 转科单收尾
        entity.setTransferStatus(TransferStatusEnum.DONE.getCode());
        entity.setReceiveTime(now);
        entity.setReceiveDoctorId(currentEmpId());
        entity.setReceiveDoctorName(currentName());
        entity.setRecordId(record.getId());
        entity.setStopOrdersCount(countStoppedFromRemark(orderRemark));
        entity.setOrderRemark(orderRemark);
        if (StringUtils.hasText(dto.getRemark())) {
            entity.setRemark(appendNote(entity.getRemark(), dto.getRemark()));
        }
        if (summaryNote != null) {
            entity.setRemark(appendNote(entity.getRemark(), summaryNote));
        }
        transferMapper.updateById(entity);

        log.info("转科已生效 transferNo={} admissionId={} {} → {} 停医嘱={} 回写病历={} 接收人={}",
                entity.getTransferNo(), admission.getAdmissionId(), entity.getFromDeptName(),
                entity.getToDeptName(), entity.getStopOrdersCount(), record.getRecordNo(),
                entity.getReceiveDoctorName());
    }

    // 取消

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(InpatientTransferCancelDTO dto) {
        BizInpatientTransfer entity = transferMapper.selectById(dto.getTransferId());
        if (entity == null) {
            throw new BusinessException("转科记录不存在");
        }
        if (!Objects.equals(TransferStatusEnum.PENDING.getCode(), entity.getTransferStatus())) {
            throw new BusinessException("该转科申请当前状态为「"
                    + TransferStatusEnum.labelOrUnknown(entity.getTransferStatus())
                    + "」，只有「待接收」可以取消；已接收的转科要转回去，请再发起一次转科");
        }
        entity.setTransferStatus(TransferStatusEnum.CANCELLED.getCode());
        entity.setCancelReason(dto.getCancelReason());
        transferMapper.updateById(entity);

        log.info("转科申请已取消 transferNo={} 原因={} 操作人={}",
                entity.getTransferNo(), dto.getCancelReason(), currentName());
    }

    // 内部：医嘱处置

    /**
     * 接收时的医嘱处置：停掉原科室「已校对 / 执行中」的长期医嘱。
     *
     * <p>返回一段**人话描述**，直接进医嘱备注：
     * 停了几条写几条；停不掉的（「待校对」，按六条铁律只能由原科室医生作废）逐条列出医嘱号。
     * 医嘱一条都没有时也写明"无需处置"，避免把"没有医嘱"和"忘了处置"混成一个空白。
     */
    private String settleLongOrders(BizInpatientTransfer entity) {
        String reason = "转科：" + entity.getFromDeptName() + " → " + entity.getToDeptName();
        int affected = orderService.stopLongOrders(entity.getAdmissionId(), reason);

        // 再查一次：仍有「待校对」的长期医嘱就是没处置掉的（stop 刻意不碰它们）
        List<BizInpatientOrder> left = orderMapper.selectList(new LambdaQueryWrapper<BizInpatientOrder>()
                .eq(BizInpatientOrder::getAdmissionId, entity.getAdmissionId())
                .eq(BizInpatientOrder::getOrderType, OrderTypeEnum.LONG.getCode())
                .in(BizInpatientOrder::getOrderStatus, InpatientOrderStatusEnum.PENDING_VERIFY.getCode(), InpatientOrderStatusEnum.VERIFIED.getCode(), InpatientOrderStatusEnum.EXECUTING.getCode())
                .orderByAsc(BizInpatientOrder::getOrderNo));

        StringBuilder sb = new StringBuilder();
        if (affected == 0 && left.isEmpty()) {
            return "本次转科无长期医嘱需要处置（临时医嘱不受转科影响）";
        }
        sb.append("已随转科停止长期医嘱 ").append(affected).append(" 条");
        if (!left.isEmpty()) {
            sb.append("；仍有 ").append(left.size()).append(" 条长期医嘱未停（状态：");
            sb.append(left.stream()
                    .map(o -> o.getOrderNo() + "/" + InpatientOrderStatusEnum.labelOrUnknown(o.getOrderStatus()))
                    .collect(Collectors.joining("、")));
            sb.append("），需由原科室医生处理");
        }
        return sb.toString();
    }

    /**
     * 从 order_remark 文案里取回"已停止 N 条"的 N —— 只解析自己写的那一种句式，取不到就当 0
     */
    private int countStoppedFromRemark(String remark) {
        if (!StringUtils.hasText(remark)) {
            return 0;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("已随转科停止长期医嘱 (\\d+) 条").matcher(remark);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    // 内部：床位

    /**
     * 目标床位校验：必须空闲，且确实属于目标科室与目标病区
     */
    private void checkTargetBed(SysBed bed, Long toDeptId, Long toWardId) {
        if (!Objects.equals(BedStatusEnum.FREE.getCode(), bed.getBedStatus())) {
            throw new BusinessException("转入床位「" + bed.getBedNo() + "」当前不可用（"
                    + BedStatusEnum.labelOrUnknown(bed.getBedStatus()) + "）");
        }
        if (!Objects.equals(bed.getDeptId(), toDeptId)) {
            throw new BusinessException("转入床位「" + bed.getBedNo() + "」不属于目标科室");
        }
        if (!Objects.equals(bed.getWardId(), toWardId)) {
            throw new BusinessException("转入床位「" + bed.getBedNo() + "」不属于目标病区");
        }
    }

    /**
     * 床位占用：状态置占用 + 记患者 + 同步病区冗余计数。
     * <p>与 {@code InpatientServiceImpl} 的同名方法同源（都以床位为准，
     * 病区.occupied_beds 只是冗余展示字段）—— 改一处必须两处一起改。
     */
    private void occupyBed(SysBed bed, Long patientId, Long wardId) {
        bedMapper.update(null, new LambdaUpdateWrapper<SysBed>()
                .eq(SysBed::getBedId, bed.getBedId())
                .set(SysBed::getBedStatus, BedStatusEnum.OCCUPIED.getCode())
                .set(SysBed::getPatientId, patientId)
                .set(SysBed::getUpdateTime, LocalDateTime.now()));
        bedMapper.syncWardOccupied(wardId);
    }

    /**
     * 床位释放：状态置空闲 + 清患者。
     * <p>必须用 UpdateWrapper 显式 {@code set(patientId, null)} —— {@code updateById} 的
     * NOT_NULL 策略会跳过 null 字段，导致患者一直挂在床位上（脏占用）。
     */
    private void releaseBed(Long bedId) {
        if (bedId == null) {
            return;
        }
        SysBed bed = bedMapper.selectById(bedId);
        if (bed == null) {
            return;
        }
        bedMapper.update(null, new LambdaUpdateWrapper<SysBed>()
                .eq(SysBed::getBedId, bedId)
                .set(SysBed::getBedStatus, BedStatusEnum.FREE.getCode())
                .set(SysBed::getPatientId, null)
                .set(SysBed::getUpdateTime, LocalDateTime.now()));
        bedMapper.syncWardOccupied(bed.getWardId());
    }

    // 内部：病案首页

    /**
     * 病案首页同步：{@code dept_id/dept_name/ward/bed_no} 是**出院科别**，随转科走；
     * 若首页还没填过入院科别（历史数据），这次一并补上。
     *
     * <p>已提交 / 已归档的首页**不动**（那是正式文书，改了没有留痕），
     * 但要把这件事写进转科单备注 —— 悄悄不更新最危险：出院科别会永远停在入院科室。
     */
    private String syncSummary(BizInpatientTransfer entity, BizAdmission admission, SysBed newBed) {
        BizInpatientSummary summary = summaryMapper.selectOne(new LambdaQueryWrapper<BizInpatientSummary>()
                .eq(BizInpatientSummary::getAdmissionId, admission.getAdmissionId()));
        if (summary == null) {
            return null;
        }
        if (!Objects.equals(SummaryStatusEnum.DRAFT.getCode(), summary.getSummaryStatus())) {
            log.warn("病案首页非草稿（状态={}），未随转科更新出院科别 admissionId={}",
                    summary.getSummaryStatus(), admission.getAdmissionId());
            return "病案首页状态为「" + (summary.getSummaryStatus() != null && summary.getSummaryStatus() == 2 ? "已提交" : "已归档")
                    + "」，未随转科更新出院科别，请病案室按转科轨迹核对";
        }
        if (summary.getAdmitDeptId() == null) {
            // 先取旧值（此时 dept 还是入院科室）再覆盖，顺序反了就把入院科别写成了出院科别
            summary.setAdmitDeptId(summary.getDeptId());
            summary.setAdmitDeptName(summary.getDeptName());
        }
        summary.setDeptId(entity.getToDeptId());
        summary.setDeptName(entity.getToDeptName());
        summary.setWardId(entity.getToWardId());
        summary.setWardName(entity.getToWardName());
        summary.setBedNo(entity.getToBedNo());
        summaryMapper.updateById(summary);
        return null;
    }

    // 内部：回写转科记录病历

    /**
     * 把这次转科回写成一份住院病历文书（record_type=10 转科记录，状态直接「已提交」）。
     *
     * <p>病历正文自洽：只翻病历就能读懂"从哪来、到哪去、医嘱怎么处置的"，
     * 不必回头查转科表。签名医生 = 接收医生（谁接手谁负责）。
     */
    private BizInpatientRecord writeBackRecord(BizInpatientTransfer entity, BizPatient patient,
                                               String orderRemark, LocalDateTime now) {
        BizInpatientRecord record = new BizInpatientRecord();
        record.setRecordNo(nextRecordNo());
        record.setAdmissionId(entity.getAdmissionId());
        record.setPatientId(entity.getPatientId());
        record.setPatientNo(patient.getPatientNo());
        record.setPatientName(patient.getPatientName());
        record.setGender(patient.getGender());
        record.setAge(patient.getAge());
        record.setAgeUnit(1);
        // 转科记录属于**转入科室**的文书（记录的是转科完成那一刻患者所在科室）
        record.setDeptId(entity.getToDeptId());
        record.setDeptName(entity.getToDeptName());
        record.setWardId(entity.getToWardId());
        record.setWardName(entity.getToWardName());
        record.setBedNo(entity.getToBedNo());
        record.setRecordType(InpatientRecordTypeEnum.TRANSFER.getCode());
        record.setRecordTitle(truncate(entity.getFromDeptName() + " → " + entity.getToDeptName() + " 转科记录", 200));
        record.setRecordTime(now);

        StringBuilder body = new StringBuilder();
        body.append("转出：").append(nvl(entity.getFromDeptName()))
                .append(" ").append(nvl(entity.getFromWardName()))
                .append(" ").append(nvl(entity.getFromBedNo())).append("床\n");
        body.append("转入：").append(nvl(entity.getToDeptName()))
                .append(" ").append(nvl(entity.getToWardName()))
                .append(" ").append(nvl(entity.getToBedNo())).append("床\n");
        body.append("转科类型：").append(TransferTypeEnum.getText(entity.getTransferType()))
                .append("；发起时已住院 ").append(entity.getHospitalDays() == null ? "—" : entity.getHospitalDays()).append(" 天\n");
        body.append("医嘱处置：").append(nvl(orderRemark));
        record.setCourseNote(body.toString());
        // 转科原因写在 remark（结构化要素清单取的也是这一列，与会诊记录同口径）
        record.setRemark("系统回写：转科单号 " + entity.getTransferNo()
                + "，转科原因：" + entity.getTransferReason());
        record.setRecordStatus(RecordStatusEnum.SUBMITTED.getCode());
        record.setDoctorId(entity.getReceiveDoctorId() != null ? entity.getReceiveDoctorId() : currentEmpId());
        record.setDoctorName(entity.getReceiveDoctorName() != null ? entity.getReceiveDoctorName() : currentName());
        record.setSubmitTime(now);
        recordMapper.insert(record);
        return record;
    }

    // 内部：编号 / 时间 / 用户

    private String nextTransferNo() {
        String prefix = "ZK" + LocalDate.now().format(NO_DATE);
        long seq = transferMapper.countByNoPrefix(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    private String nextRecordNo() {
        String prefix = "BL" + LocalDate.now().format(NO_DATE);
        long seq = recordMapper.countByRecordNoPrefix(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    private int calcHospitalDays(LocalDateTime admitTime, LocalDateTime end) {
        if (admitTime == null) {
            return 0;
        }
        long days = java.time.temporal.ChronoUnit.DAYS
                .between(admitTime.toLocalDate(), end.toLocalDate());
        return days < 1 ? 1 : (int) days;
    }

    /**
     * 留痕一律用**员工ID**（不是用户的ID），与医嘱/站内信同一口径
     */
    private Long currentEmpId() {
        return UserUtils.getCurrentUser().getEmployeeId();
    }

    private String currentName() {
        return UserUtils.getCurrentUser().getRealName();
    }

    // 内部：名称 / 组装

    private String deptNameOf(Long deptId) {
        if (deptId == null) {
            return "未知科室";
        }
        String name = transferMapper.selectDeptName(deptId);
        return StringUtils.hasText(name) ? name : "未知科室(ID=" + deptId + ")";
    }

    private String wardNameOf(Long wardId) {
        if (wardId == null) {
            return null;
        }
        WardVO ward = bedMapper.selectWardById(wardId);
        return ward == null ? null : ward.getWardName();
    }

    private String bedNoOf(Long bedId) {
        if (bedId == null) {
            return null;
        }
        SysBed bed = bedMapper.selectById(bedId);
        return bed == null ? null : bed.getBedNo();
    }

    private Map<Long, String> loadRecordNos(List<BizInpatientTransfer> list) {
        Set<Long> ids = list.stream()
                .map(BizInpatientTransfer::getRecordId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        if (ids.isEmpty()) {
            // 注意：绝不能用 Map.of() —— ImmutableCollections.MapN.get(null) 会 requireNonNull 抛 NPE。
            // 调用方按主键取值，主键本身可能就是 null（如待接收/已取消的转科还没有回写病历，record_id 为 null）。
            return Collections.emptyMap();
        }
        List<BizInpatientRecord> records = recordMapper.selectBatchIds(ids);
        Map<Long, String> map = new LinkedHashMap<>();
        for (BizInpatientRecord r : records) {
            map.put(r.getId(), r.getRecordNo());
        }
        return map;
    }

    private Map<Long, Integer> loadAdmitStatus(List<BizInpatientTransfer> list) {
        Set<Long> ids = list.stream()
                .map(BizInpatientTransfer::getAdmissionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<BizAdmission> admissions = admissionMapper.selectBatchIds(ids);
        Map<Long, Integer> map = new LinkedHashMap<>();
        for (BizAdmission a : admissions) {
            map.put(a.getAdmissionId(), a.getAdmitStatus() == null ? 0 : a.getAdmitStatus());
        }
        return map;
    }

    private InpatientTransferVO toVO(BizInpatientTransfer e, String recordNo, Integer admitStatus) {
        InpatientTransferVO vo = new InpatientTransferVO();
        BeanUtils.copyProperties(e, vo);
        vo.setTransferTypeText(TransferTypeEnum.getText(e.getTransferType()));
        vo.setTransferStatusText(TransferStatusEnum.getText(e.getTransferStatus()));
        vo.setRecordNo(recordNo);

        boolean pending = Objects.equals(TransferStatusEnum.PENDING.getCode(), e.getTransferStatus());
        if (e.getApplyTime() != null) {
            LocalDateTime end = e.getReceiveTime() != null ? e.getReceiveTime() : LocalDateTime.now();
            long minutes = Math.max(0, Duration.between(e.getApplyTime(), end).toMinutes());
            vo.setWaitingMinutes(minutes);
            vo.setWaitText(pending ? "已等待 " + minutes + " 分钟" : "耗时 " + minutes + " 分钟");
        }
        // 按钮可用性由后端给：待接收且该次住院仍在院（已出院的转科只能取消，接收会直接被拒）
        vo.setCanAccept(pending && Objects.equals(AdmitStatusEnum.IN_HOSPITAL.getCode(), admitStatus));
        vo.setCanCancel(pending);
        return vo;
    }

    private String appendNote(String original, String note) {
        if (!StringUtils.hasText(note)) {
            return original;
        }
        return StringUtils.hasText(original) ? original + "；" + note : note;
    }

    private String nvl(String s) {
        return StringUtils.hasText(s) ? s : "—";
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
