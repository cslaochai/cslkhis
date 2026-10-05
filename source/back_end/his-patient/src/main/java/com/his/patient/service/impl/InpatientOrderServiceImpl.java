package com.his.patient.service.impl;
import com.his.common.enums.AdmitStatusEnum;
import com.his.patient.enums.OrderExecStatusEnum;
import com.his.patient.enums.OrderSourceEnum;
import com.his.patient.enums.OrderUrgentEnum;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.dto.SignCommandDTO;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.service.EmrSignatureService;
import com.his.common.vo.SignatureVO;
import com.his.fee.dto.FeeBookDTO;
import com.his.fee.entity.BizFeeRecord;
import com.his.fee.support.FeeCatalogResolver;
import com.his.patient.dto.*;
import com.his.patient.entity.*;
import com.his.patient.enums.InpatientOrderStatusEnum;
import com.his.patient.enums.OrderClassEnum;
import com.his.patient.enums.OrderTypeEnum;
import com.his.patient.mapper.*;
import com.his.patient.service.ArrearsControlGate;
import com.his.patient.service.DietPlanService;
import com.his.patient.service.InpatientOrderService;
import com.his.patient.support.InpatientOrderItemRules;
import com.his.patient.support.OrderChargeInvoker;
import com.his.patient.vo.InpatientOrderExecVO;
import com.his.patient.vo.InpatientOrderVO;
import com.his.patient.vo.WardVO;
import com.his.security.UserUtils;
import com.his.security.entity.CurrentUser;
import com.his.system.dto.TechAuthGateDTO;
import com.his.system.enums.BizTypeEnum;
import com.his.system.service.EmployeeTechAuthService;
import com.his.system.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 住院医嘱服务实现（P1：医嘱 → 校对 → 执行 → 计费）。
 *
 * <p>除接口注释里那三条铁律（未校对不可执行 / 长期只能停不能作废 / 同组套同起同停）之外，
 * 本类还固化了这些**至少踩过一次或一定会被追问**的点：
 *
 * <ol>
 *   <li><b>批量动作「先全量校验、再写入」</b>：批量校对/批量执行只要有一条状态不合法就整批拒绝，
 *       并指名是哪条医嘱。部分成功会让护士不知道哪些生效了，进而重复点或漏点。</li>
 *   <li><b>停止不修改执行行</b>：停止只影响后续。未执行的计划行保持「待执行」不动 ——
 *       把它改成「已退回」等于编造一个"护士退回"的事实，而队列查询已经用
 *       {@code order_status IN (2,3)} 把它们排除干净了。</li>
 *   <li><b>计划行按天生成、查询时补当天（无定时任务）</b>：见 {@link #backfillTodayPlans}，
 *       同「危急值超时是查询时算的」口径 —— 状态由事实推导，不靠后台任务把状态"跑"出来。</li>
 *   <li><b>「未计费」必须可见</b>：收费模块缺席时执行照常成功，但要把"未计费"写进执行备注，
 *       绝不静默当作已计费（同「发送方 status=1 只证明我发过」）。</li>
 *   <li><b>金额一律快照</b>：单价开立时写进医嘱行，执行时不再回查字典 ——
 *       否则调价后账单与医嘱会对不上，四核对会出现假阳性。</li>
 *   <li><b>时间截到秒</b>：库表是 {@code DATETIME(0)}，MySQL 会四舍五入，不截就会
 *       "写进去的 ≠ 读回来的"。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InpatientOrderServiceImpl implements InpatientOrderService {

    // 医嘱状态

    // 执行状态

    // 医嘱类型

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 一次查询最多补多少条计划行。
     * <p>补计划发生在 GET 里，必须有上限：异常数据（比如几百条跨月未执行的长期医嘱）
     * 不能把一次翻页查询拖成批量写。
     */
    private static final int BACKFILL_LIMIT = 500;

    private final BizInpatientOrderMapper orderMapper;
    private final BizInpatientOrderExecMapper execMapper;
    private final BizAdmissionMapper admissionMapper;
    private final BizPatientMapper patientMapper;
    private final SysBedMapper bedMapper;
    private final OrderChargeInvoker chargeInvoker;
    /**
     * 膳食方案（sql/168）：临床营养医嘱校对即派生、停/作废即同步停/废
     */
    private final DietPlanService dietPlanService;
    /**
     * 电子签名（P5.5）：开立签名 + 校对签名双签
     */
    private final EmrSignatureService signatureService;
    /**
     * 站内信（inpat-order 发送方）：医嘱校对完成 → 通知开嘱医生
     */
    private final SysMessageService sysMessageService;
    /**
     * 欠费管控（G20）：SPI 由 his-charge 提供；缺席 fail-open 放行，只拦新增的择期类医嘱
     */
    private final ObjectProvider<ArrearsControlGate> arrearsControlGate;
    /**
     * 技术授权准入闸（sql/155）：无手术资质的人不能开手术医嘱
     */
    private final EmployeeTechAuthService techAuthService;

    // 开立 / 修改

    private static String orderClassDesc(Integer orderClass) {
        String text = OrderClassEnum.getText(orderClass);
        return "—".equals(text) ? "" : text;
    }

    private static String appendNote(String origin, String extra) {
        if (!StringUtils.hasText(origin)) {
            return extra;
        }
        return origin + "；" + extra;
    }

    /**
     * 时间统一截到秒，保证「写进去的 = 读回来的」（库表是 DATETIME(0)，MySQL 会四舍五入）
     */
    private static LocalDateTime toSeconds(LocalDateTime time) {
        return time == null ? null : time.truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String save(InpatientOrderUpsertDTO dto) {
        // 保留（类别②）：整个 DTO 为 null 不是字段校验，Bean Validation 覆盖不到
        if (dto == null) {
            throw new BusinessException("入院ID不能为空");
        }
        // 医嘱类型取值区间：码值合法性（类别③），留在 service
        if (!Objects.equals(OrderTypeEnum.LONG.getCode(), dto.getOrderType()) && !Objects.equals(OrderTypeEnum.TEMP.getCode(), dto.getOrderType())) {
            throw new BusinessException("医嘱类型取值不合法（应为 1-长期 2-临时）");
        }
        List<InpatientOrderItemDTO> items = dto.getItems();
        for (InpatientOrderItemDTO item : items) {
            InpatientOrderItemRules.validate(item);
        }

        BizAdmission admission = admissionMapper.selectById(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        if (!Objects.equals(AdmitStatusEnum.IN_HOSPITAL.getCode(), admission.getAdmitStatus())) {
            throw new BusinessException("该患者已出院，不能再开立医嘱");
        }

        // G20 欠费管控：策略启用且欠费达停费线时，拦截新增的择期类医嘱（检查/检验/治疗）。
        // 药品/手术/急救类永不拦截——急救被欠费卡住是医疗事故；修改/停嘱不拦；gate 缺席 fail-open。
        if (dto.getId() == null) {
            ArrearsControlGate gate = arrearsControlGate.getIfAvailable();
            if (gate != null) {
                Set<Integer> classes = items.stream()
                        .map(InpatientOrderItemDTO::getOrderClass)
                        .filter(Objects::nonNull)
                        .collect(java.util.stream.Collectors.toSet());
                ArrearsControlGate.CheckResult check =
                        gate.checkNewOrder(new ArrearsControlGate.OrderCheck(admission.getAdmissionId(), classes));
                if (check != null && !check.isAllowed()) {
                    throw new BusinessException(check.getReason());
                }
            }
        }

        LocalDateTime now = toSeconds(LocalDateTime.now());

        // 准入闸（sql/155）：手术医嘱（order_class=6）要求开单人本人有「手术类」技术授权。
        // 级别不在这里判 —— 医嘱只写"要做手术"，几级由手术申请单定，级别闸落在排台（见 OperationApplyServiceImpl）。
        // 与欠费闸相反，这里 fail-closed：判不出授权就拒单，宁可让医生去补授权。
        if (dto.getId() == null
                && items.stream().anyMatch(i -> Objects.equals(OrderClassEnum.OPERATION.getCode(), i.getOrderClass()))) {
            TechAuthGateDTO authGate = new TechAuthGateDTO();
            authGate.setEmployeeId(UserUtils.getCurrentEmployeeId());
            authGate.setAuthCategory(TechAuthCategoryEnum.SURGERY.getCode());
            authGate.setRequiredLevel(1);
            authGate.setItemCode(items.stream().map(InpatientOrderItemDTO::getItemCode)
                    .filter(StringUtils::hasText).findFirst().orElse(null));
            authGate.setEmergency(false);
            techAuthService.gate(authGate);
        }

        // 修改：仅「待校对」的单条医嘱，且不允许改动整组共享字段
        if (dto.getId() != null) {
            return updateOne(dto, admission, now);
        }

        // 新增
        BizPatient patient = patientMapper.selectById(admission.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }

        // 归属快照：医嘱行上写下的必须是"开立当时的事实"（患者可能改床、转科，医嘱不该跟着变）
        Long deptId = admission.getDeptId();
        String deptName = null;
        String wardName = null;
        if (admission.getWardId() != null) {
            WardVO ward = bedMapper.selectWardById(admission.getWardId());
            if (ward != null) {
                wardName = ward.getWardName();
                deptName = ward.getDeptName();
                if (deptId == null) {
                    deptId = ward.getDeptId();
                }
            }
        }
        String bedNo = null;
        if (admission.getBedId() != null) {
            SysBed bed = bedMapper.selectById(admission.getBedId());
            if (bed != null) {
                bedNo = bed.getBedNo();
            }
        }

        String orderGroup = StringUtils.hasText(dto.getOrderGroup()) ? dto.getOrderGroup() : nextOrderGroup();
        LocalDateTime startTime = toSeconds(dto.getStartTime() != null ? dto.getStartTime() : now);
        LocalDateTime planEndTime = toSeconds(dto.getPlanEndTime());
        if (planEndTime != null && planEndTime.isBefore(startTime)) {
            throw new BusinessException("计划结束时间不能早于开始时间");
        }

        Long doctorId = currentEmpId();
        String doctorName = currentName();
        int source = dto.getSource() != null ? dto.getSource() : 1;
        int isUrgent = Objects.equals(1, dto.getIsUrgent()) ? 1 : 0;

        List<String> orderNos = new ArrayList<>(items.size());
        for (InpatientOrderItemDTO item : items) {
            BizInpatientOrder order = new BizInpatientOrder();
            order.setOrderNo(nextOrderNo());
            order.setAdmissionId(admission.getAdmissionId());
            order.setPatientId(admission.getPatientId());
            order.setPatientNo(patient.getPatientNo());
            order.setPatientName(patient.getPatientName());
            order.setDeptId(deptId);
            order.setDeptName(deptName);
            order.setWardId(admission.getWardId());
            order.setWardName(wardName);
            order.setBedNo(bedNo);
            order.setOrderType(dto.getOrderType());
            order.setOrderGroup(orderGroup);

            order.setOrderClass(item.getOrderClass());
            order.setItemCode(item.getItemCode());
            order.setItemName(item.getItemName());
            order.setSpec(item.getSpec());
            order.setUnit(item.getUnit());
            order.setDosage(item.getDosage());
            order.setDosageUnit(item.getDosageUnit());
            order.setRoute(item.getRoute());
            order.setFrequency(item.getFrequency());
            BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE;
            BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            order.setQuantity(qty);
            order.setPrice(price);
            order.setAmount(qty.multiply(price).setScale(2, RoundingMode.HALF_UP));

            order.setStartTime(startTime);
            order.setPlanEndTime(planEndTime);
            order.setOrderTime(now);
            order.setDoctorId(doctorId);
            order.setDoctorName(doctorName);
            // 新开医嘱一律「待校对」：护士没核对过之前，它不进执行队列
            order.setOrderStatus(InpatientOrderStatusEnum.PENDING_VERIFY.getCode());
            order.setIsUrgent(isUrgent);
            order.setSource(source);
            order.setRemark(dto.getRemark());
            orderMapper.insert(order);
            // 开立即签：签名的内容取自**库里的医嘱行**，所以必须 insert 之后再签。
            // 签名失败直接抛，整个开立事务回滚 —— 不留下"医嘱在、签名没签上"的缺口。
            signOrder(order, SignScene.ORDER_CREATE, "医嘱开立");
            orderNos.add(order.getOrderNo());
        }

        log.info("开立医嘱 admissionId={} 组套={} 类型={} 条数={} 医嘱号={} 医生={}",
                admission.getAdmissionId(), orderGroup, OrderTypeEnum.getText(dto.getOrderType()),
                orderNos.size(), orderNos, doctorName);
        return orderGroup;
    }

    // 护士校对

    /**
     * 修改一条「待校对」的医嘱。
     *
     * <p>刻意**不允许改动整组共享字段**（医嘱类型 / 开始时间 / 加急标志）：这几项是同组套
     * 「同起同停」的物理载体，允许单条改动，同一组套内就会出现两种开始时间，那条规则当场名存实亡。
     * 需要变更就走「停止原组套 → 重新开立」。
     */
    private String updateOne(InpatientOrderUpsertDTO dto, BizAdmission admission, LocalDateTime now) {
        BizInpatientOrder order = orderMapper.selectById(dto.getId());
        if (order == null) {
            throw new BusinessException("医嘱不存在");
        }
        if (!Objects.equals(order.getAdmissionId(), admission.getAdmissionId())) {
            throw new BusinessException("该医嘱不属于本次住院，不能修改");
        }
        if (!Objects.equals(InpatientOrderStatusEnum.PENDING_VERIFY.getCode(), order.getOrderStatus())) {
            throw new BusinessException("医嘱 " + order.getOrderNo() + " 当前状态为「"
                    + InpatientOrderStatusEnum.labelOrUnknown(order.getOrderStatus())
                    + "」，只有「待校对」的医嘱可以修改；已校对的请先停止后重新开立");
        }
        List<InpatientOrderItemDTO> items = dto.getItems();
        if (items.size() != 1) {
            throw new BusinessException("修改医嘱一次只能改一条项目（当前 " + items.size() + " 条）");
        }
        if (dto.getOrderType() != null && !Objects.equals(dto.getOrderType(), order.getOrderType())) {
            throw new BusinessException("修改医嘱不能改变医嘱类型（同一组套必须同起同停）；如需变更请停止原组套后重新开立");
        }
        if (dto.getStartTime() != null && !toSeconds(dto.getStartTime()).equals(order.getStartTime())) {
            throw new BusinessException("修改医嘱不能改变开始时间（同一组套必须同起同停）；如需变更请停止原组套后重新开立");
        }
        if (dto.getIsUrgent() != null
                && !Objects.equals(Objects.equals(1, dto.getIsUrgent()) ? 1 : 0, order.getIsUrgent())) {
            throw new BusinessException("修改医嘱不能改变加急标志（同一组套必须一致）");
        }

        InpatientOrderItemDTO item = items.get(0);
        order.setOrderClass(item.getOrderClass());
        order.setItemCode(item.getItemCode());
        order.setItemName(item.getItemName());
        order.setSpec(item.getSpec());
        order.setUnit(item.getUnit());
        order.setDosage(item.getDosage());
        order.setDosageUnit(item.getDosageUnit());
        order.setRoute(item.getRoute());
        order.setFrequency(item.getFrequency());
        BigDecimal qty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE;
        BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
        order.setQuantity(qty);
        order.setPrice(price);
        order.setAmount(qty.multiply(price).setScale(2, RoundingMode.HALF_UP));
        if (dto.getPlanEndTime() != null) {
            order.setPlanEndTime(toSeconds(dto.getPlanEndTime()));
        }
        if (dto.getRemark() != null) {
            order.setRemark(dto.getRemark());
        }
        orderMapper.updateById(order);
        // 内容变了 → 原来那份开立签名绑的是旧内容，已经失效。
        // 正确处理是「作废旧签名 + 按新内容重签」，而不是留着一条验不过的签名让人猜。
        // 作废会把医嘱行上的 doctor_sign_id 清空，于是紧接着的重签不会被 blockReason 拦住。
        invalidateDoctorSignIfAny(order, "医嘱内容被修改（" + currentName() + "），原开立签名对新内容已失效");
        signOrder(order, SignScene.ORDER_CREATE, "医嘱修改后重签");

        log.info("修改医嘱 orderNo={} 组套={} 项目={}", order.getOrderNo(), order.getOrderGroup(), order.getItemName());
        return order.getOrderGroup();
    }

    /**
     * 作废该医嘱上的开立签名（没有就什么也不做）；不吞异常
     */
    private void invalidateDoctorSignIfAny(BizInpatientOrder order, String reason) {
        if (order.getDoctorSignId() == null) {
            return;
        }
        signatureService.invalidate(order.getDoctorSignId(), reason, currentEmpId(), currentName());
        order.setDoctorSignId(null);
        order.setDoctorSignedTime(null);
    }

    // 停止 / 作废

    /**
     * 给一条医嘱签名；失败直接抛（不吞），由调用方的业务事务整体回滚
     */
    private void signOrder(BizInpatientOrder order, SignScene scene, String actionLabel) {
        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(SignBizType.INPATIENT_ORDER.getCode());
        cmd.setBizId(order.getId());
        cmd.setSignScene(scene.getCode());
        cmd.setSignerId(currentEmpId());
        cmd.setSignerName(currentName());
        cmd.setSignerDeptId(currentDeptId());
        cmd.setSignerDeptName(currentDeptName());
        try {
            SignatureVO sig = signatureService.sign(cmd);
            if (scene == SignScene.ORDER_VERIFY) {
                order.setNurseSignId(sig.getId());
                order.setNurseSignedTime(sig.getSignedTime());
            } else {
                order.setDoctorSignId(sig.getId());
                order.setDoctorSignedTime(sig.getSignedTime());
            }
        } catch (BusinessException e) {
            throw new BusinessException(actionLabel + "失败（医嘱 " + order.getOrderNo() + "）：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int verify(InpatientOrderVerifyDTO dto) {
        if (dto == null || dto.getOrderIds() == null || dto.getOrderIds().isEmpty()) {
            throw new BusinessException("请选择要校对的医嘱");
        }
        List<Long> ids = dto.getOrderIds().stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            throw new BusinessException("请选择要校对的医嘱");
        }

        LocalDateTime now = toSeconds(LocalDateTime.now());
        Long nurseId = currentEmpId();
        String nurseName = currentName();

        // 第一遍：全量校验。批量校对要么全成功、要么一条都不改 ——
        // 部分成功会让护士不知道哪些生效了，进而重复点或漏点（漏点就是医嘱漏执行）。
        List<BizInpatientOrder> orders = new ArrayList<>(ids.size());
        for (Long id : ids) {
            BizInpatientOrder order = orderMapper.selectById(id);
            if (order == null) {
                throw new BusinessException("医嘱不存在（ID=" + id + "）");
            }
            if (!Objects.equals(InpatientOrderStatusEnum.PENDING_VERIFY.getCode(), order.getOrderStatus())) {
                throw new BusinessException("医嘱 " + order.getOrderNo() + " 当前状态为「"
                        + InpatientOrderStatusEnum.labelOrUnknown(order.getOrderStatus()) + "」，不能重复校对");
            }
            orders.add(order);
        }

        for (BizInpatientOrder order : orders) {
            order.setOrderStatus(InpatientOrderStatusEnum.VERIFIED.getCode());
            order.setVerifyNurseId(nurseId);
            order.setVerifyNurseName(nurseName);
            order.setVerifyTime(now);
            orderMapper.updateById(order);
            // 校对即签：先落库（签名服务读库放行判断），再签校对名。
            // 这条签名的内容里会带上开立签名的摘要 —— 护士校对之后医生再改医嘱，
            // 第二环验签会当场断掉，医嘱上的"双签"才不是摆设。
            signOrder(order, SignScene.ORDER_VERIFY, "医嘱校对");
            ensurePlanForToday(order, now);
            // 临床营养医嘱校对通过 → 同事务派生膳食方案。
            // 校对是"护士确认这条医嘱真实存在"的唯一时点，未校对的医嘱不该出现在营养科工作台上；
            // 派生后方案表就是订餐与执行率的唯一事实来源，不再回头解析医嘱正文。
            if (Objects.equals(OrderClassEnum.NUTRITION.getCode(), order.getOrderClass())) {
                dietPlanService.deriveFromOrder(order);
            }
        }
        log.info("医嘱校对完成 条数={} 护士={} 医嘱号={}", orders.size(), nurseName,
                orders.stream().map(BizInpatientOrder::getOrderNo).toList());
        notifyVerified(orders, nurseName);
        return orders.size();
    }

    /**
     * inpat-order 发送方：校对完成 → 站内信通知开嘱医生。
     *
     * <p><b>通知型（handle_status=null）</b>：校对完成不需要医生动作，医嘱进入执行队列
     * 即闭环（已读即闭环）。按「开嘱医生 + 住院」分组合并成一条，避免批量校对刷屏；
     * 组内含急嘱时整组升 warning，否则 info。发送失败只记日志，不影响校对事务。
     */
    private void notifyVerified(List<BizInpatientOrder> orders, String nurseName) {
        try {
            Map<String, List<BizInpatientOrder>> groups = new LinkedHashMap<>();
            for (BizInpatientOrder order : orders) {
                if (order.getDoctorId() == null) {
                    continue;
                }
                groups.computeIfAbsent(order.getDoctorId() + "-" + order.getAdmissionId(),
                        k -> new ArrayList<>()).add(order);
            }
            for (List<BizInpatientOrder> group : groups.values()) {
                BizInpatientOrder first = group.get(0);
                boolean hasUrgent = group.stream().anyMatch(o -> Objects.equals(1, o.getIsUrgent()));
                String orderNosText = group.size() <= 3
                        ? group.stream().map(BizInpatientOrder::getOrderNo).reduce((a, b) -> a + "、" + b).orElse("")
                        : group.stream().limit(3).map(BizInpatientOrder::getOrderNo).reduce((a, b) -> a + "、" + b).orElse("")
                        + " 等 " + group.size() + " 条";
                String content = String.format("您为患者 %s（%s）开立的医嘱 %s 已由护士 %s 校对完成，进入执行队列。",
                        first.getPatientName(),
                        first.getBedNo() == null ? "在院" : first.getBedNo() + "床",
                        orderNosText, nurseName);
                String payload = cn.hutool.json.JSONUtil.toJsonStr(new java.util.LinkedHashMap<String, Object>() {{
                    put("patientName", first.getPatientName());
                    put("bedNo", first.getBedNo());
                    put("orderNo", first.getOrderNo());
                    put("count", group.size());
                    put("verifyNurse", nurseName);
                }});
                sysMessageService.sendSystemMessage(first.getDoctorId(), first.getDoctorName(),
                        "住院医嘱提醒：" + (hasUrgent ? "急嘱已校对" : "医嘱已校对") + " " + group.size() + " 条",
                        content, BizTypeEnum.INPAT_ORDER.getType(), first.getId(),
                        hasUrgent ? "warning" : "info", payload, null);
            }
        } catch (Exception ex) {
            log.warn("[医嘱校对] 校对完成通知发送失败 护士={} 条数={}", nurseName, orders.size(), ex);
        }
    }

    /**
     * 批量停止某次住院的长期医嘱（转科用）。
     *
     * <p>与 {@link #stop} 共用同一套校验与写库逻辑（直接调本类方法，不另写一份），
     * 但在两处刻意收窄：
     * <ol>
     *   <li><b>只挑「已校对 / 执行中」的长期医嘱</b>：临时医嘱本就有始有终，转科不改变已执行的事实；
     *       「待校对」的按铁律只能由原科室医生作废，这里跳过并留给调用方回报。</li>
     *   <li><b>同组套只停一次</b>：{@code stop} 内部会展开整组，重复调用第二次会撞上
     *       "已停止不能重复停止" —— 所以先按 orderGroup 去重。</li>
     * </ol>
     *
     * <p>刻意**不抛异常**：转科不能被某条医嘱卡住（并发停止、脏状态都可能发生）。
     * 被跳过的医嘱由调用方再查一次现状后写进转科单说明 —— 静默跳过才是真问题。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int stopLongOrders(Long admissionId, String reason) {
        // 保留（类别②）：转科等内部调用直接传参，非 web 绑定入参，Bean Validation 不生效
        if (admissionId == null) {
            throw new BusinessException("入院ID不能为空");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("停止原因不能为空（停止是一个医疗决定，必须有人负责）");
        }
        List<BizInpatientOrder> longs = orderMapper.selectList(new LambdaQueryWrapper<BizInpatientOrder>()
                .eq(BizInpatientOrder::getAdmissionId, admissionId)
                .eq(BizInpatientOrder::getOrderType, OrderTypeEnum.LONG.getCode())
                .in(BizInpatientOrder::getOrderStatus, InpatientOrderStatusEnum.VERIFIED.getCode(), InpatientOrderStatusEnum.EXECUTING.getCode())
                .orderByAsc(BizInpatientOrder::getOrderTime)
                .orderByAsc(BizInpatientOrder::getId));
        if (longs.isEmpty()) {
            return 0;
        }

        Set<String> handledGroups = new HashSet<>();
        int affected = 0;
        int failed = 0;
        for (BizInpatientOrder order : longs) {
            if (StringUtils.hasText(order.getOrderGroup())) {
                if (!handledGroups.add(order.getOrderGroup())) {
                    continue;
                }
            }
            InpatientOrderStopDTO dto = new InpatientOrderStopDTO();
            dto.setOrderId(order.getId());
            dto.setStopReason(reason);
            try {
                // 直接调本类方法：复用同一套校验与写库逻辑（规则只有一份）。
                // 因为不走代理，这里的异常不会把外层事务标记成 rollback-only。
                affected += stop(dto);
            } catch (Exception e) {
                failed++;
                log.warn("批量停医嘱失败 orderNo={} 状态={} 原因={}",
                        order.getOrderNo(), order.getOrderStatus(), e.getMessage());
            }
        }
        log.info("批量停止长期医嘱 admissionId={} 受影响 {} 条，失败 {} 条，原因={}",
                admissionId, affected, failed, reason);
        return affected;
    }

    // 查询

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int stop(InpatientOrderStopDTO dto) {
        // 保留（类别②）：除 Controller 外还被 stopLongOrders 内部构造 DTO 直接调用，
        // 注解校验只在 web 绑定跑，内部路径必须留下这道闸
        if (dto == null || dto.getOrderId() == null) {
            throw new BusinessException("医嘱ID不能为空");
        }
        if (!StringUtils.hasText(dto.getStopReason())) {
            throw new BusinessException("停止原因不能为空（停止是一个医疗决定，必须有人负责）");
        }
        BizInpatientOrder target = orderMapper.selectById(dto.getOrderId());
        if (target == null) {
            throw new BusinessException("医嘱不存在");
        }
        if (Objects.equals(InpatientOrderStatusEnum.STOPPED.getCode(), target.getOrderStatus())) {
            throw new BusinessException("医嘱 " + target.getOrderNo() + " 已停止，不能重复停止");
        }
        if (Objects.equals(InpatientOrderStatusEnum.CANCELLED.getCode(), target.getOrderStatus())) {
            throw new BusinessException("医嘱 " + target.getOrderNo() + " 已作废，无需停止");
        }
        if (Objects.equals(InpatientOrderStatusEnum.FINISHED.getCode(), target.getOrderStatus())) {
            throw new BusinessException("医嘱 " + target.getOrderNo() + " 已完成，不能停止");
        }
        // 待校对(1) 请走作废：停止的语义是"后续不再做"，而待校对的医嘱还没进入执行阶段
        if (!Objects.equals(InpatientOrderStatusEnum.VERIFIED.getCode(), target.getOrderStatus())
                && !Objects.equals(InpatientOrderStatusEnum.EXECUTING.getCode(), target.getOrderStatus())) {
            throw new BusinessException("医嘱 " + target.getOrderNo() + " 当前状态为「"
                    + InpatientOrderStatusEnum.labelOrUnknown(target.getOrderStatus())
                    + "」，只有「已校对 / 执行中」的医嘱可以停止；「待校对」的请改用作废");
        }

        LocalDateTime now = toSeconds(LocalDateTime.now());
        Long doctorId = currentEmpId();
        String doctorName = currentName();

        List<BizInpatientOrder> targets = new ArrayList<>();
        if (StringUtils.hasText(target.getOrderGroup())) {
            // 同组套同起同停：不允许"一组药停一半"
            targets = orderMapper.selectList(new LambdaQueryWrapper<BizInpatientOrder>()
                    .eq(BizInpatientOrder::getAdmissionId, target.getAdmissionId())
                    .eq(BizInpatientOrder::getOrderGroup, target.getOrderGroup())
                    .in(BizInpatientOrder::getOrderStatus, InpatientOrderStatusEnum.PENDING_VERIFY.getCode(), InpatientOrderStatusEnum.VERIFIED.getCode(), InpatientOrderStatusEnum.EXECUTING.getCode()));
        }
        if (targets.isEmpty()) {
            targets = List.of(target);
        }

        int stopped = 0;
        int cancelled = 0;
        for (BizInpatientOrder order : targets) {
            if (Objects.equals(InpatientOrderStatusEnum.PENDING_VERIFY.getCode(), order.getOrderStatus())) {
                // 组内还没被护士看到的成员：按作废处理（作废只允许发生在待校对）
                order.setOrderStatus(InpatientOrderStatusEnum.CANCELLED.getCode());
                cancelled++;
            } else {
                order.setOrderStatus(InpatientOrderStatusEnum.STOPPED.getCode());
                order.setStopTime(now);
                stopped++;
            }
            order.setStopDoctorId(doctorId);
            order.setStopDoctorName(doctorName);
            order.setStopReason(dto.getStopReason());
            orderMapper.updateById(order);
            // 刻意不动执行行：停止只影响后续，未执行的计划仍如实留在"待执行"，
            // 队列查询用 order_status IN (2,3) 把它们排除。改成"已退回"等于编造事实。
            syncDietPlanOnStopOrCancel(order, now, dto.getStopReason());
        }
        log.info("停止医嘱 orderNo={} 组套={} 停止 {} 条 / 作废 {} 条 原因={} 操作人={}",
                target.getOrderNo(), target.getOrderGroup(), stopped, cancelled, dto.getStopReason(), doctorName);
        return targets.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(InpatientOrderCancelDTO dto) {
        // 保留（类别②）：整个 DTO 为 null 不是字段校验，Bean Validation 覆盖不到
        if (dto == null) {
            throw new BusinessException("医嘱ID不能为空");
        }
        BizInpatientOrder target = orderMapper.selectById(dto.getOrderId());
        if (target == null) {
            throw new BusinessException("医嘱不存在");
        }

        List<BizInpatientOrder> targets;
        if (StringUtils.hasText(target.getOrderGroup())) {
            // 组套是一个「开立单元」：开错就是整组开错，所以整组作废。
            // 但组内只要有一条已经进到校对之后，就不允许整组抹掉 —— 那是停止的地盘。
            targets = orderMapper.selectList(new LambdaQueryWrapper<BizInpatientOrder>()
                    .eq(BizInpatientOrder::getAdmissionId, target.getAdmissionId())
                    .eq(BizInpatientOrder::getOrderGroup, target.getOrderGroup())
                    .in(BizInpatientOrder::getOrderStatus, InpatientOrderStatusEnum.PENDING_VERIFY.getCode(), InpatientOrderStatusEnum.VERIFIED.getCode(), InpatientOrderStatusEnum.EXECUTING.getCode(), InpatientOrderStatusEnum.FINISHED.getCode()));
            for (BizInpatientOrder o : targets) {
                if (!Objects.equals(InpatientOrderStatusEnum.PENDING_VERIFY.getCode(), o.getOrderStatus())) {
                    throw new BusinessException("组套 " + target.getOrderGroup() + " 内的医嘱 " + o.getOrderNo()
                            + " 已是「" + InpatientOrderStatusEnum.labelOrUnknown(o.getOrderStatus())
                            + "」，不能整组作废；已校对的医嘱请用「停止」");
                }
            }
        } else {
            if (!Objects.equals(InpatientOrderStatusEnum.PENDING_VERIFY.getCode(), target.getOrderStatus())) {
                throw new BusinessException("医嘱 " + target.getOrderNo() + " 当前状态为「"
                        + InpatientOrderStatusEnum.labelOrUnknown(target.getOrderStatus())
                        + "」，只有「待校对」的医嘱可以作废；已校对的请用「停止」");
            }
            targets = List.of(target);
        }
        if (targets.isEmpty()) {
            throw new BusinessException("医嘱 " + target.getOrderNo() + " 当前状态为「"
                    + InpatientOrderStatusEnum.labelOrUnknown(target.getOrderStatus())
                    + "」，只有「待校对」的医嘱可以作废；已校对的请用「停止」");
        }

        Long doctorId = currentEmpId();
        String doctorName = currentName();
        for (BizInpatientOrder order : targets) {
            order.setOrderStatus(InpatientOrderStatusEnum.CANCELLED.getCode());
            order.setStopDoctorId(doctorId);
            order.setStopDoctorName(doctorName);
            order.setStopReason(dto.getCancelReason());
            orderMapper.updateById(order);
            syncDietPlanOnStopOrCancel(order, toSeconds(LocalDateTime.now()), dto.getCancelReason());
        }
        log.info("作废医嘱 组套={} 条数={} 原因={} 操作人={}",
                target.getOrderGroup(), targets.size(), dto.getCancelReason(), doctorName);
    }

    /**
     * 临床营养医嘱（orderClass=10）停止/作废 → 膳食方案同步停/废（sql/168）。
     *
     * <p>没有这一步的后果是"医嘱停了、食堂还在送饭"：订餐是从方案表批量生成的，
     * 方案不与医嘱同生死，执行率与餐费都会对不上真实医疗行为。
     * <p>方案不存在时静默返回（这条医嘱可能从未被校对过，本来就没有方案）。
     */
    private void syncDietPlanOnStopOrCancel(BizInpatientOrder order, LocalDateTime when, String reason) {
        if (!Objects.equals(OrderClassEnum.NUTRITION.getCode(), order.getOrderClass())) {
            return;
        }
        if (Objects.equals(InpatientOrderStatusEnum.CANCELLED.getCode(), order.getOrderStatus())) {
            dietPlanService.cancelFromOrder(order.getId());
        } else {
            dietPlanService.stopFromOrder(order.getId(), when, reason);
        }
    }

    @Override
    public IPage<InpatientOrderVO> listPage(InpatientOrderQueryPageDTO query) {
        Page<InpatientOrderVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<InpatientOrderVO> result = orderMapper.selectOrderPage(page, query);
        result.getRecords().forEach(this::decorateOrder);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IPage<InpatientOrderExecVO> execPendingList(OrderExecQueryPageDTO query) {
        backfillTodayPlans(query.getAdmissionId(), query.getPatientId());
        Page<InpatientOrderExecVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<InpatientOrderExecVO> result = execMapper.selectPendingPage(page, query);
        result.getRecords().forEach(this::decorateExec);
        return result;
    }

    // 执行

    @Override
    public IPage<InpatientOrderExecVO> execList(OrderExecQueryPageDTO query) {
        Page<InpatientOrderExecVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<InpatientOrderExecVO> result = execMapper.selectExecPage(page, query);
        result.getRecords().forEach(this::decorateExec);
        return result;
    }

    @Override
    public long countPendingVerify(Long admissionId) {
        return orderMapper.selectCount(new LambdaQueryWrapper<BizInpatientOrder>()
                .eq(BizInpatientOrder::getOrderStatus, InpatientOrderStatusEnum.PENDING_VERIFY.getCode())
                .eq(admissionId != null, BizInpatientOrder::getAdmissionId, admissionId));
    }

    @Override
    public long countPendingExec(Long admissionId) {
        return execMapper.countPendingByAdmission(admissionId);
    }

    // 计划行（按天生成 / 查询补当天）

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int execComplete(OrderExecCompleteDTO dto) {
        if (dto == null || dto.getExecIds() == null || dto.getExecIds().isEmpty()) {
            throw new BusinessException("请选择要处理的执行记录");
        }
        int status = dto.getExecStatus() != null ? dto.getExecStatus() : ExecStatusEnum.EXECUTED.getCode();
        if (!Objects.equals(ExecStatusEnum.EXECUTED.getCode(), status) && !Objects.equals(ExecStatusEnum.SKIPPED.getCode(), status)) {
            throw new BusinessException("执行结果取值不合法（应为 2-已执行 3-已跳过）");
        }
        if (Objects.equals(ExecStatusEnum.SKIPPED.getCode(), status) && !StringUtils.hasText(dto.getExecNote())) {
            throw new BusinessException("跳过必须写明原因（飞检问的是「这条医嘱为什么没有执行记录」，答「删了」不成立）");
        }
        List<Long> ids = dto.getExecIds().stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            throw new BusinessException("请选择要处理的执行记录");
        }

        LocalDateTime execTime = toSeconds(dto.getExecTime() != null ? dto.getExecTime() : LocalDateTime.now());
        Long nurseId = currentEmpId();
        String nurseName = currentName();

        // 第一遍：全量校验（含父医嘱状态）
        List<BizInpatientOrderExec> rows = new ArrayList<>(ids.size());
        Map<Long, BizInpatientOrder> orderById = new HashMap<>();
        Map<Long, BizAdmission> admissionCache = new HashMap<>();
        for (Long id : ids) {
            BizInpatientOrderExec exec = execMapper.selectById(id);
            if (exec == null) {
                throw new BusinessException("执行记录不存在（ID=" + id + "）");
            }
            if (!Objects.equals(ExecStatusEnum.PENDING.getCode(), exec.getExecStatus())) {
                throw new BusinessException("该执行记录已是「"
                        + OrderExecStatusEnum.labelOrUnknown(exec.getExecStatus()) + "」，不能重复处理");
            }
            BizInpatientOrder order = orderMapper.selectById(exec.getOrderId());
            if (order == null) {
                throw new BusinessException("执行记录（ID=" + id + "）对应的医嘱不存在");
            }
            // ★ 铁律：未校对不可执行。已停止/已作废同样不许执行 —— 只说"状态不对"不够，要说清是哪种。
            if (!Objects.equals(InpatientOrderStatusEnum.VERIFIED.getCode(), order.getOrderStatus())
                    && !Objects.equals(InpatientOrderStatusEnum.EXECUTING.getCode(), order.getOrderStatus())) {
                throw new BusinessException("医嘱 " + order.getOrderNo() + " 当前状态为「"
                        + InpatientOrderStatusEnum.labelOrUnknown(order.getOrderStatus()) + "」，不能执行");
            }
            rows.add(exec);
            orderById.put(exec.getId(), order);
        }

        int processed = 0;
        for (BizInpatientOrderExec exec : rows) {
            BizInpatientOrder order = orderById.get(exec.getId());
            exec.setExecStatus(status);
            exec.setExecTime(execTime);
            exec.setExecNurseId(nurseId);
            exec.setExecNurseName(nurseName);
            if (StringUtils.hasText(dto.getExecNote())) {
                exec.setExecNote(dto.getExecNote());
            }

            if (Objects.equals(ExecStatusEnum.EXECUTED.getCode(), status)) {
                // 记账走独立事务（OrderChargeInvoker）：记账失败绝不能把"护士已经做过了"一起回滚
                BizFeeRecord chargeResult = null;
                try {
                    chargeResult = chargeInvoker.book(buildChargeFee(order, exec, admissionCache));
                } catch (Exception e) {
                    log.error("医嘱 {} 第 {} 次执行计费失败（执行记录仍会照常更新）",
                            order.getOrderNo(), exec.getExecSeq(), e);
                }
                if (chargeResult != null) {
                    // 存的就是 L1 记账行的 (id, feeNo)
                    exec.setFeeRecordId(chargeResult.getId());
                    exec.setFeeNo(chargeResult.getFeeNo());
                } else {
                    // 「未记账」必须让护士站看得见：只写日志等于让账少收没人知道
                    exec.setExecNote(appendNote(exec.getExecNote(), "未记账：记账入参不全或记账失败"));
                    log.warn("医嘱 {} 第 {} 次执行未记账（记账行ID为空），已写入执行备注",
                            order.getOrderNo(), exec.getExecSeq());
                }
                // 父医嘱状态推进：临时执行一次即完结；长期首次执行转「执行中」
                if (Objects.equals(OrderTypeEnum.TEMP.getCode(), order.getOrderType())) {
                    order.setOrderStatus(InpatientOrderStatusEnum.FINISHED.getCode());
                } else if (Objects.equals(InpatientOrderStatusEnum.VERIFIED.getCode(), order.getOrderStatus())) {
                    order.setOrderStatus(InpatientOrderStatusEnum.EXECUTING.getCode());
                }
                orderMapper.updateById(order);
            } else {
                // 跳过：不计费。临时医嘱仍停在「已校对」—— 这次没做不等于做完了，
                // 系统不替临床把"没做"记成"完成"；这条医嘱需要医生重新开立或停止。
                if (Objects.equals(OrderTypeEnum.TEMP.getCode(), order.getOrderType())) {
                    log.warn("临时医嘱 {} 第 {} 次被跳过，该医嘱仍为「已校对」且今日已无待执行计划，需医生处理",
                            order.getOrderNo(), exec.getExecSeq());
                }
            }

            execMapper.updateById(exec);
            processed++;
        }
        log.info("医嘱执行处理完成 条数={} 结果={} 护士={}", processed,
                OrderExecStatusEnum.getText(status), nurseName);
        return processed;
    }

    /**
     * 一次执行 = 一笔记账行（L1），全部快照由医嘱与执行记录带过去，记账侧不回查医嘱。
     *
     * <p>三条口径：
     * <ol>
     *   <li><b>幂等锚点来源ID = 本次执行记录ID</b>，不是医嘱ID：长期医嘱一条要执行几十次，
     *       每次都是一笔真实费用，按医嘱ID判重会把第二次起的执行当成「重复记账」跳过 = 静默少收钱。
     *       医嘱这条边由来源编号（医嘱号）与备注里的医嘱ID 表达，四核对照样能追回原医嘱。</li>
     *   <li><b>科室取开立科室快照</b>：转科后开的医嘱算新科室；病区/床位是执行时的物理位置，不是钱的归属。</li>
     *   <li><b>金额按单价 × 数量现算</b>：医嘱上的金额快照只用来核对，不一致时以现算为准并 warn，
     *       因为单价快照才是应收的依据。</li>
     * </ol>
     */
    private FeeBookDTO buildChargeFee(BizInpatientOrder order,
                                      BizInpatientOrderExec exec,
                                      Map<Long, BizAdmission> admissionCache) {
        FeeBookDTO dto = new FeeBookDTO();
        dto.setPatientId(order.getPatientId());
        dto.setPatientNo(order.getPatientNo());
        dto.setPatientName(order.getPatientName());
        dto.setEncounterType(EncounterTypeEnum.INPATIENT.getCode());
        dto.setEncounterId(order.getAdmissionId());
        BizAdmission admission = admissionCache.computeIfAbsent(order.getAdmissionId(),
                id -> admissionMapper.selectById(id));
        if (admission != null) {
            dto.setEncounterNo(admission.getAdmissionNo());
        }
        dto.setDeptId(order.getDeptId());
        dto.setDeptName(order.getDeptName());
        dto.setDoctorId(order.getDoctorId());
        dto.setDoctorName(order.getDoctorName());
        int itemType = InpatientOrderItemRules.chargeItemTypeOf(order.getOrderClass());
        BigDecimal quantity = order.getQuantity() == null ? BigDecimal.ONE : order.getQuantity();
        BigDecimal price = order.getPrice() == null ? BigDecimal.ZERO : order.getPrice();
        BigDecimal computed = price.multiply(quantity);
        if (order.getAmount() != null && order.getAmount().compareTo(computed) != 0) {
            log.warn("医嘱 {} 第 {} 次执行：金额快照 ¥{} 与 单价×数量 ¥{} 不一致，按后者记账（请核对开立时的价格快照）",
                    order.getOrderNo(), exec.getExecSeq(),
                    order.getAmount().toPlainString(), computed.toPlainString());
        }
        dto.setItemType(itemType);
        dto.setItemCode(StringUtils.hasText(order.getItemCode()) ? order.getItemCode() : order.getOrderNo());
        dto.setItemName(order.getItemName());
        dto.setSpecification(order.getSpec());
        dto.setUnit(order.getUnit());
        dto.setPrice(price);
        dto.setQuantity(quantity);
        dto.setSourceType(FeeSourceTypeEnum.INPATIENT_ORDER.getCode());
        dto.setSourceId(exec.getId());
        dto.setSourceNo(order.getOrderNo());
        dto.setCatalogType(FeeCatalogResolver.byItemType(itemType));
        dto.setRemark("住院" + orderClassDesc(order.getOrderClass()) + "医嘱执行记账（第 "
                + (exec.getExecSeq() == null ? 1 : exec.getExecSeq()) + " 次，医嘱 " + order.getId() + "）");
        return dto;
    }

    /**
     * 补当天计划行 —— 本项目刻意<b>不引入定时任务</b>。
     *
     * <p>长期医嘱是"按天执行"的：三天前校对通过的长期医嘱，如果没人管，今天的执行队列里
     * 就不会有它 —— 那是**医嘱漏执行**，是护理质量的红线。两种解法：
     * <ol>
     *   <li>定时任务每天 0 点扫全表生成；</li>
     *   <li>查询待执行队列时，为"该有今天这次"的医嘱补一行（幂等）。</li>
     * </ol>
     * 这里选 2，与「危急值超时是查询时算的」同一个口径：<b>状态由事实推导，不靠后台任务
     * 把状态"跑"出来</b>。定时任务会带来"服务停机那天全院的长期医嘱计划集体缺失"这种
     * 静默故障，而补计划是幂等的（唯一索引 {@code uk_ioe_order_plan_date} 兜底），漏不了也重不了。
     *
     * <p>补计划的<b>边界</b>：只补「已校对 / 执行中」的<b>长期</b>医嘱。未校对的医嘱永远补不出来
     * —— 否则"未校对不可执行"这条铁律会被补计划悄悄绕过。
     */
    private void backfillTodayPlans(Long admissionId, Long patientId) {
        LocalDateTime now = toSeconds(LocalDateTime.now());
        List<BizInpatientOrder> actives = orderMapper.selectList(new LambdaQueryWrapper<BizInpatientOrder>()
                .eq(BizInpatientOrder::getOrderType, OrderTypeEnum.LONG.getCode())
                .in(BizInpatientOrder::getOrderStatus, InpatientOrderStatusEnum.VERIFIED.getCode(), InpatientOrderStatusEnum.EXECUTING.getCode())
                .eq(admissionId != null, BizInpatientOrder::getAdmissionId, admissionId)
                .eq(patientId != null, BizInpatientOrder::getPatientId, patientId)
                .le(BizInpatientOrder::getStartTime, now)
                .and(w -> w.isNull(BizInpatientOrder::getPlanEndTime)
                        .or().gt(BizInpatientOrder::getPlanEndTime, now))
                .last("LIMIT " + BACKFILL_LIMIT));
        if (actives.isEmpty()) {
            return;
        }
        int created = 0;
        for (BizInpatientOrder order : actives) {
            if (ensurePlanForToday(order, now)) {
                created++;
            }
        }
        if (created > 0) {
            log.info("补生成当天医嘱计划 条数={}（admissionId={} patientId={}）", created, admissionId, patientId);
        }
    }

    // 展示态

    /**
     * 幂等地生成「今天」的计划行。
     *
     * @return 是否真的新建了一行
     */
    private boolean ensurePlanForToday(BizInpatientOrder order, LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        if (order.getStartTime() != null && order.getStartTime().toLocalDate().isAfter(today)) {
            // 明天才开始：今天不该有这次执行
            return false;
        }
        if (order.getPlanEndTime() != null && !order.getPlanEndTime().isAfter(now)) {
            return false;
        }
        if (execMapper.countByOrderAndDate(order.getId(), today) > 0) {
            return false;
        }
        BizInpatientOrderExec exec = new BizInpatientOrderExec();
        exec.setOrderId(order.getId());
        exec.setAdmissionId(order.getAdmissionId());
        exec.setPatientId(order.getPatientId());
        exec.setExecSeq((int) execMapper.countByOrder(order.getId()) + 1);
        exec.setPlanDate(today);
        exec.setPlanTime(planTimeOf(order, now));
        exec.setExecStatus(ExecStatusEnum.PENDING.getCode());
        try {
            execMapper.insert(exec);
            return true;
        } catch (DuplicateKeyException e) {
            // 并发下唯一索引 uk_ioe_order_plan_date 兜底：别的线程刚补过，不算失败
            log.debug("医嘱 {} 今日计划行已存在（唯一索引拦下）", order.getOrderNo());
            return false;
        }
    }

    /**
     * 计划执行时间。
     * <p>刻意<b>不做 max(候选, now)</b>：把过期的计划时间"顺延"到当前时刻，
     * 队列就再也看不出"这条本该 08:00 做、现在 14:00 还没做"。宁可让队列显示过期时刻，
     * 也不要抹掉这条信息。
     */
    private LocalDateTime planTimeOf(BizInpatientOrder order, LocalDateTime now) {
        LocalDateTime start = order.getStartTime();
        if (start == null) {
            return now;
        }
        if (start.toLocalDate().equals(now.toLocalDate())) {
            return start;
        }
        // 跨天续用的长期医嘱：沿用原时刻，让队列按护士熟悉的班次时点排序
        return now.toLocalDate().atTime(start.toLocalTime());
    }

    // 编号 / 当前用户 / 时间

    private void decorateOrder(InpatientOrderVO vo) {
        vo.setOrderTypeText(OrderTypeEnum.getText(vo.getOrderType()));
        vo.setOrderClassText(OrderClassEnum.getText(vo.getOrderClass()));
        vo.setOrderStatusText(InpatientOrderStatusEnum.getText(vo.getOrderStatus()));
        vo.setSourceText(OrderSourceEnum.getText(vo.getSource()));
        vo.setIsUrgentText(OrderUrgentEnum.getText(vo.getIsUrgent()));

        boolean pendingVerify = Objects.equals(InpatientOrderStatusEnum.PENDING_VERIFY.getCode(), vo.getOrderStatus());
        vo.setCanVerify(pendingVerify);
        vo.setCanCancel(pendingVerify);
        // 修改与作废同窗口：只有「待校对」能改（一改就作废旧签名重签，护士校对过之后再改等于篡改已核对内容）
        vo.setCanEdit(pendingVerify);
        vo.setCanStop(Objects.equals(InpatientOrderStatusEnum.VERIFIED.getCode(), vo.getOrderStatus())
                || Objects.equals(InpatientOrderStatusEnum.EXECUTING.getCode(), vo.getOrderStatus()));

        // 签名情况：三态分开说，不合并成"已签名/未签名"两个值 ——
        // "只签了开立名"在管理上是待办（护士还没核对），
        // 与"开立名都没签"（历史数据 / 开立时签名失败）是两件事。
        boolean doctorSigned = vo.getDoctorSignId() != null;
        boolean nurseSigned = vo.getNurseSignId() != null;
        vo.setDoctorSigned(doctorSigned);
        vo.setNurseSigned(nurseSigned);
        vo.setSignStatusText(doctorSigned && nurseSigned ? "双签完成"
                : doctorSigned ? "已开立签名（待校对签名）"
                : nurseSigned ? "仅校对签名（缺开立签名，需排查）"
                : "未签名");

        if (vo.getTodayExecCount() == null) {
            vo.setTodayExecCount(0);
        }
        if (vo.getPendingExecCount() == null) {
            vo.setPendingExecCount(0);
        }
    }

    private void decorateExec(InpatientOrderExecVO vo) {
        vo.setOrderTypeText(OrderTypeEnum.getText(vo.getOrderType()));
        vo.setOrderClassText(OrderClassEnum.getText(vo.getOrderClass()));
        vo.setOrderStatusText(InpatientOrderStatusEnum.getText(vo.getOrderStatus()));
        vo.setExecStatusText(OrderExecStatusEnum.getText(vo.getExecStatus()));
        vo.setCharged(vo.getFeeRecordId() != null);
        vo.setInfusion(InpatientInfusionServiceImpl.isInfusionRoute(vo.getRoute()));
    }

    private String nextOrderNo() {
        String prefix = "YZ" + LocalDate.now().format(NO_DATE);
        long seq = orderMapper.countByOrderNoPrefix(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    private String nextOrderGroup() {
        String prefix = "G" + LocalDate.now().format(NO_DATE);
        long seq = orderMapper.countByOrderGroupPrefix(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    /**
     * 医嘱的医生/护士留痕一律用**员工ID**（不是用户的ID），与站内信收件人同一口径
     */
    private Long currentEmpId() {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            if (user == null) {
                return null;
            }
            return user.getEmployeeId() != null ? user.getEmployeeId() : user.getUserId();
        } catch (Exception e) {
            // 非请求线程（定时/脚本）取不到上下文
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

    /**
     * 签名人的科室快照（人的科室会变，签名行必须记"签的那一刻在哪个科室"）
     */
    private Long currentDeptId() {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            return user == null ? null : user.getDeptId();
        } catch (Exception e) {
            return null;
        }
    }

    private String currentDeptName() {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            return user == null ? null : user.getDeptName();
        } catch (Exception e) {
            return null;
        }
    }
}
