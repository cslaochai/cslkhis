package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.util.TimeUtil;
import com.his.common.exception.BusinessException;
import com.his.common.enums.SysGenderEnum;
import com.his.patient.dto.AdmissionOrderCancelDTO;
import com.his.patient.dto.AdmissionOrderQueryPageDTO;
import com.his.patient.dto.AdmissionOrderUpsertDTO;
import com.his.patient.entity.BizAdmissionOrder;
import com.his.patient.entity.BizPatient;
import com.his.patient.enums.AdmissionOrderStatusEnum;
import com.his.patient.mapper.BizAdmissionMapper;
import com.his.patient.mapper.BizAdmissionOrderMapper;
import com.his.patient.mapper.BizPatientMapper;
import com.his.patient.service.AdmissionOrderService;
import com.his.patient.service.BedCenterService;
import com.his.patient.vo.AdmissionOrderVO;
import com.his.system.entity.SysConfig;
import com.his.system.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 住院证服务实现
 *
 * <p>固化的业务规则（都是三甲真实流程里会被追问的点）：
 * <ol>
 *   <li><b>一张有效证只能对一次收治</b>：状态为「已收治」的证再收治必须被拒，不能静默覆盖。</li>
 *   <li><b>过期不落库</b>：有效期到了只是"查询时算出来"的展示态，不把时间流逝伪装成一次业务动作
 *       （同"危急值超时"的口径）。</li>
 *   <li><b>作废必须写原因</b>：让证消失得有人负责。</li>
 *   <li><b>已收治的证不能作废</b>：入院这件事已经发生了。</li>
 *   <li>未知码值一律渲染成「未知(码值)」，不回落成合法值。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdmissionOrderServiceImpl implements AdmissionOrderService {

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String VALID_DAYS_CONFIG_KEY = "admission_order.valid_days";

    /**
     * 配置缺失时的兜底有效期（天）。宁可给一个保守值，也不让证永久有效。
     */
    private static final int VALID_DAYS_FALLBACK = 7;

    private final BizAdmissionOrderMapper orderMapper;
    private final BizAdmissionMapper admissionMapper;
    private final BizPatientMapper patientMapper;
    private final SysConfigMapper sysConfigMapper;

    /**
     * 床位服务中心（开证 → 自动进等床队列）。
     *
     * <p><b>为什么用 ObjectProvider 而不是直接注入</b>：BedCenterService 反过来依赖
     * InpatientService（收治要走入院主流程），InpatientService 又依赖本服务（按证收治），
     * 直接注入会形成 BedCenter → Inpatient → AdmissionOrder → BedCenter 的构造环，
     * Spring Boot 默认禁止循环引用，启动即失败。ObjectProvider 是<b>惰性</b>的，
     * 构造期不解析目标 Bean，环自然断掉；取不到实现时（理论上不会）降级为不入队，
     * 绝不让一张证因为队列那边的任何问题而开不出来。
     */
    private final ObjectProvider<BedCenterService> bedCenterProvider;

    // 开证

    /**
     * 时间统一截到秒，保证「写进去的 = 读回来的」（库表是 DATETIME(0)，MySQL 会四舍五入）
     */
    // 查询

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AdmissionOrderUpsertDTO dto) {
        BizPatient patient = patientMapper.selectById(dto.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }

        // 同一挂号只能有一张有效证（作废/过期后可重开）
        if (dto.getRegistId() != null && orderMapper.countActiveByRegist(dto.getRegistId()) > 0) {
            throw new BusinessException("该挂号已存在有效的住院证，无需重复开证；如需变更请先作废原证");
        }

        // 同一患者也不该同时拿两张「待收治」的证：患者拿着两张证到入院处，
        // 入院处无从判断该收哪一科，最后只能打电话问医生。这里直接拦下来，并说清是哪张证挡着。
        BizAdmissionOrder pendingOfPatient = orderMapper.selectOne(new LambdaQueryWrapper<BizAdmissionOrder>()
                .eq(BizAdmissionOrder::getPatientId, dto.getPatientId())
                .eq(BizAdmissionOrder::getOrderStatus, AdmissionOrderStatusEnum.PENDING.getCode())
                .and(w -> w.isNull(BizAdmissionOrder::getValidUntil)
                        .or().gt(BizAdmissionOrder::getValidUntil, TimeUtil.toSeconds(LocalDateTime.now())))
                .orderByDesc(BizAdmissionOrder::getOrderTime)
                .last("LIMIT 1"));
        if (pendingOfPatient != null) {
            throw new BusinessException("该患者已有待收治的住院证（" + pendingOfPatient.getOrderNo()
                    + "，拟收治 " + (pendingOfPatient.getApplyDeptName() != null
                    ? pendingOfPatient.getApplyDeptName() : "未知科室")
                    + "），请先作废原证再开新证");
        }

        // 已在院的患者不需要再开住院证（他已经在床上了）。不拦的话会出现
        // "某人在院 + 同时手持一张待收治住院证"的怪状态，入院处查半天查不明白。
        if (admissionMapper.countInHospitalByPatient(dto.getPatientId()) > 0) {
            throw new BusinessException("该患者当前在院，无需再开住院证");
        }

        LocalDateTime now = TimeUtil.toSeconds(LocalDateTime.now());
        int validDays = validDays();

        BizAdmissionOrder order = new BizAdmissionOrder();
        order.setOrderNo(nextOrderNo());

        // 患者快照：证面写下的就是当时的事实
        order.setPatientId(dto.getPatientId());
        order.setPatientNo(dto.getPatientNo() != null ? dto.getPatientNo() : patient.getPatientNo());
        order.setPatientName(dto.getPatientName());
        order.setGender(dto.getGender() != null ? dto.getGender() : patient.getGender());
        order.setAge(dto.getAge() != null ? dto.getAge() : patient.getAge());
        order.setPhone(dto.getPhone() != null ? dto.getPhone() : patient.getPhone());
        order.setIdCard(dto.getIdCard() != null ? dto.getIdCard() : patient.getIdCard());

        // 来源门诊线索
        order.setRegistId(dto.getRegistId());
        order.setRegistNo(dto.getRegistNo());
        order.setVisitId(dto.getVisitId());

        // 开证方
        order.setSourceDeptId(dto.getSourceDeptId());
        order.setSourceDeptName(dto.getSourceDeptName());
        order.setSourceDoctorId(dto.getSourceDoctorId());
        order.setSourceDoctorName(dto.getSourceDoctorName());

        // 拟收治
        order.setApplyDeptId(dto.getApplyDeptId());
        order.setApplyDeptName(dto.getApplyDeptName());
        order.setDiagnosisCode(dto.getDiagnosisCode());
        order.setDiagnosisName(dto.getDiagnosisName());
        order.setDiagnosisNote(dto.getDiagnosisNote());

        // 医保
        order.setInsuranceType(dto.getInsuranceType() != null
                ? dto.getInsuranceType() : patient.getMedicalInsuranceType());
        order.setMedicalInsuranceNo(dto.getMedicalInsuranceNo() != null
                ? dto.getMedicalInsuranceNo() : patient.getMedicalInsuranceNo());

        // 状态
        order.setOrderStatus(AdmissionOrderStatusEnum.PENDING.getCode());
        order.setExpectAdmitTime(dto.getExpectAdmitTime());
        order.setOrderTime(now);
        order.setValidUntil(now.plusDays(validDays));
        order.setRemark(dto.getRemark());

        orderMapper.insert(order);

        // 开证 = 进等床队列（同一事务）：「有人要住院」和「有人在床位队伍里排队」在系统里
        // 必须是同一次动作。分成两步写，就一定会出现"有证无队"或"有队无证"。
        try {
            BedCenterService bedCenter = bedCenterProvider.getIfAvailable();
            if (bedCenter != null) {
                bedCenter.syncFromOrder(order);
            }
        } catch (Exception e) {
            // 入队失败不能把已经开出去的证吞掉：证是给患者拿在手上的凭据，
            // 它一旦开出就是既成事实，队列漏了去床位中心补登记即可。
            log.warn("[住院证] 开证后自动进床位队列失败 orderNo={} —— 请到床位服务中心手工登记",
                    order.getOrderNo(), e);
        }

        log.info("开住院证成功 orderNo={} orderId={} patientId={} registNo={} applyDeptId={} 有效期至 {}",
                order.getOrderNo(), order.getId(), order.getPatientId(),
                order.getRegistNo(), order.getApplyDeptId(), order.getValidUntil());
        return order.getId();
    }

    @Override
    public IPage<AdmissionOrderVO> listPage(AdmissionOrderQueryPageDTO query) {
        Page<AdmissionOrderVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<AdmissionOrderVO> result = orderMapper.selectOrderPage(page, query);
        result.getRecords().forEach(this::decorate);
        return result;
    }

    @Override
    public AdmissionOrderVO detail(Long id) {
        // 保留（类别②）：入参是普通 Long（GET @RequestParam 绑定），不是 request DTO 字段，注解无处挂载
        if (id == null) {
            throw new BusinessException("住院证ID不能为空");
        }
        AdmissionOrderVO vo = orderMapper.selectOrderDetail(id);
        if (vo == null) {
            throw new BusinessException("住院证不存在");
        }
        decorate(vo);
        return vo;
    }

    // 作废

    @Override
    public long countPending() {
        return orderMapper.countPending();
    }

    // 收治流程回调

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(AdmissionOrderCancelDTO dto) {
        BizAdmissionOrder order = orderMapper.selectById(dto.getId());
        if (order == null) {
            throw new BusinessException("住院证不存在");
        }
        if (Objects.equals(AdmissionOrderStatusEnum.ADMITTED.getCode(), order.getOrderStatus())) {
            throw new BusinessException("该住院证已用于办理入院，不能作废；如需处理请走退院/出院流程");
        }
        if (Objects.equals(AdmissionOrderStatusEnum.VOIDED.getCode(), order.getOrderStatus())) {
            throw new BusinessException("该住院证已作废，不能重复作废");
        }

        order.setOrderStatus(AdmissionOrderStatusEnum.VOIDED.getCode());
        order.setCancelReason(dto.getCancelReason());
        orderMapper.updateById(order);

        // 证作废 = 退出等床队列（同一事务）：留着一条"还在等床"的记录，
        // 这张床会一直被锁着等一个永远不会来的人。
        try {
            BedCenterService bedCenter = bedCenterProvider.getIfAvailable();
            if (bedCenter != null) {
                bedCenter.cancelByOrder(order.getId(), "住院证作废：" + dto.getCancelReason());
            }
        } catch (Exception e) {
            log.warn("[住院证] 作废后同步取消床位排队失败 orderNo={}", order.getOrderNo(), e);
        }

        log.info("住院证作废 orderNo={} 原因={}", order.getOrderNo(), dto.getCancelReason());
    }

    @Override
    public BizAdmissionOrder requireAdmittable(Long orderId) {
        // 保留（类别②非 web 入口）：按证收治时由住院主流程直接传参调用，不经 HTTP 参数绑定，注解跑不到这一层
        if (orderId == null) {
            throw new BusinessException("住院证ID不能为空");
        }
        BizAdmissionOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("住院证不存在");
        }
        if (!Objects.equals(AdmissionOrderStatusEnum.PENDING.getCode(), order.getOrderStatus())) {
            // 已收治 / 已作废都要说清是哪种，别只说一句"状态不对"
            throw new BusinessException("住院证当前状态为「"
                    + AdmissionOrderStatusEnum.labelOrUnknown(order.getOrderStatus()) + "」，不能收治");
        }
        if (order.getValidUntil() != null && order.getValidUntil().isBefore(TimeUtil.toSeconds(LocalDateTime.now()))) {
            throw new BusinessException("住院证已于 " + order.getValidUntil() + " 过期，不能再收治，请重新开证");
        }
        return order;
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAdmitted(BizAdmissionOrder order, Long admissionId, Long admitDeptId, LocalDateTime admitTime) {
        // 重新取一次并再判状态：并发下两个入院处同时点"收治"时，靠这里的乐观判断挡住第二个人
        BizAdmissionOrder latest = orderMapper.selectById(order.getId());
        if (latest == null) {
            throw new BusinessException("住院证不存在");
        }
        if (!Objects.equals(AdmissionOrderStatusEnum.PENDING.getCode(), latest.getOrderStatus())) {
            throw new BusinessException("住院证已被收治或作废，不能重复收治");
        }
        latest.setOrderStatus(AdmissionOrderStatusEnum.ADMITTED.getCode());
        latest.setAdmissionId(admissionId);
        latest.setAdmitTime(admitTime);
        latest.setAdmitDeptId(admitDeptId);
        orderMapper.updateById(latest);
        log.info("住院证已收治 orderNo={} admissionId={} admitDeptId={}",
                latest.getOrderNo(), admissionId, admitDeptId);
    }

    /**
     * 补充展示态：文案、是否过期、是否调过科。
     * <p>过期是**算出来的**，不是库里存的——库里那条记录仍然是「待收治」，
     * 前端要同时看到"待收治"和"已过期"两件事，才不会把过期证当成能用的证。
     */
    private void decorate(AdmissionOrderVO vo) {
        vo.setGenderText(SysGenderEnum.getText(vo.getGender()));
        vo.setOrderStatusText(AdmissionOrderStatusEnum.getText(vo.getOrderStatus()));

        boolean pending = Objects.equals(AdmissionOrderStatusEnum.PENDING.getCode(), vo.getOrderStatus());
        boolean expired = pending && vo.getValidUntil() != null
                && vo.getValidUntil().isBefore(TimeUtil.toSeconds(LocalDateTime.now()));
        vo.setExpired(expired);

        vo.setDeptAdjusted(vo.getAdmitDeptId() != null && vo.getApplyDeptId() != null
                && !Objects.equals(vo.getAdmitDeptId(), vo.getApplyDeptId()));

        if (vo.getGenderText() == null) {
            vo.setGenderText("—");
        }
    }

    /**
     * 有效期天数：读系统参数，缺失或非法一律回落 7 天（不回落成"永久有效"）
     */
    private int validDays() {
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, VALID_DAYS_CONFIG_KEY));
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            log.warn("未配置 {}，住院证有效期按兜底值 {} 天", VALID_DAYS_CONFIG_KEY, VALID_DAYS_FALLBACK);
            return VALID_DAYS_FALLBACK;
        }
        try {
            int days = Integer.parseInt(config.getConfigValue().trim());
            if (days <= 0) {
                log.warn("配置 {} = {} 非法（必须为正数），按兜底值 {} 天",
                        VALID_DAYS_CONFIG_KEY, config.getConfigValue(), VALID_DAYS_FALLBACK);
                return VALID_DAYS_FALLBACK;
            }
            return days;
        } catch (NumberFormatException e) {
            log.warn("配置 {} = {} 不是数字，按兜底值 {} 天",
                    VALID_DAYS_CONFIG_KEY, config.getConfigValue(), VALID_DAYS_FALLBACK);
            return VALID_DAYS_FALLBACK;
        }
    }

    private String nextOrderNo() {
        String prefix = "RZ" + LocalDate.now().format(NO_DATE);
        long seq = orderMapper.countByOrderNoPrefix(prefix) + 1;
        return prefix + String.format("%03d", seq);
    }
}
