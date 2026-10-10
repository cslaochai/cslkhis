package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
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
import com.his.system.provider.DeptScopeService;
import com.his.system.service.RedisSequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 住院证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdmissionOrderServiceImpl extends ServiceImpl<BizAdmissionOrderMapper, BizAdmissionOrder> implements AdmissionOrderService {

    private final RedisSequenceService redisSequenceService;

    private static final int VALID_DAYS_FALLBACK = 7;

    private final BizAdmissionOrderMapper bizAdmissionOrderMapper;

    private final BizAdmissionMapper bizAdmissionMapper;

    private final BizPatientMapper bizPatientMapper;

    private final SysConfigMapper sysConfigMapper;

    private final ObjectProvider<BedCenterService> bedCenterProvider;

    private final DeptScopeService deptScopeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AdmissionOrderUpsertDTO dto) {
        BizPatient patient = bizPatientMapper.selectById(dto.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }

        // 同一挂号只能有一张有效证（作废/过期后可重开）
        if (dto.getRegistId() != null && bizAdmissionOrderMapper.countActiveByRegist(dto.getRegistId()) > 0) {
            throw new BusinessException("该挂号已存在有效的住院证，无需重复开证；如需变更请先作废原证");
        }

        // 同一患者也不该同时拿两张「待收治」的证：患者拿着两张证到入院处，
        BizAdmissionOrder pendingOfPatient = bizAdmissionOrderMapper.selectOne(new LambdaQueryWrapper<BizAdmissionOrder>()
                .eq(BizAdmissionOrder::getPatientId, dto.getPatientId())
                .eq(BizAdmissionOrder::getOrderStatus, AdmissionOrderStatusEnum.PENDING.getCode())
                .and(w -> w.isNull(BizAdmissionOrder::getValidUntil)
                        .or().gt(BizAdmissionOrder::getValidUntil, TimeUtil.nowSeconds()))
                .orderByDesc(BizAdmissionOrder::getOrderTime)
                .last("LIMIT 1"));
        if (pendingOfPatient != null) {
            throw new BusinessException("该患者已有待收治的住院证（" + pendingOfPatient.getOrderNo()
                    + "，拟收治 " + (pendingOfPatient.getApplyDeptName() != null
                    ? pendingOfPatient.getApplyDeptName() : "未知科室")
                    + "），请先作废原证再开新证");
        }

        if (bizAdmissionMapper.countInHospitalByPatient(dto.getPatientId()) > 0) {
            throw new BusinessException("该患者当前在院，无需再开住院证");
        }
        // 前端选拟收治科室（B 类）：越权科室直接拒绝
        if (dto.getApplyDeptId() != null) {
            deptScopeService.assertDeptAccessible(dto.getApplyDeptId());
        }

        LocalDateTime now = TimeUtil.nowSeconds();
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

        bizAdmissionOrderMapper.insert(order);

        try {
            BedCenterService bedCenter = bedCenterProvider.getIfAvailable();
            if (bedCenter != null) {
                bedCenter.syncFromOrder(order);
            }
        } catch (Exception e) {
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
        List<Long> deptIds = deptScopeService.scopedDeptIds(query.getApplyDeptId());
        IPage<AdmissionOrderVO> result = bizAdmissionOrderMapper.selectOrderPage(page, query, deptIds);
        result.getRecords().forEach(this::decorate);
        return result;
    }

    @Override
    public AdmissionOrderVO detail(Long id) {
        AdmissionOrderVO vo = bizAdmissionOrderMapper.selectOrderDetail(id);
        if (vo == null) {
            throw new BusinessException("住院证不存在");
        }
        deptScopeService.assertDeptAccessible(vo.getApplyDeptId());
        decorate(vo);
        return vo;
    }

    @Override
    public long countPending() {
        return bizAdmissionOrderMapper.countPending(deptScopeService.scopedDeptIds(null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(AdmissionOrderCancelDTO dto) {
        BizAdmissionOrder order = bizAdmissionOrderMapper.selectById(dto.getId());
        if (order == null) {
            throw new BusinessException("住院证不存在");
        }
        deptScopeService.assertDeptAccessible(order.getApplyDeptId());
        if (Objects.equals(AdmissionOrderStatusEnum.ADMITTED.getCode(), order.getOrderStatus())) {
            throw new BusinessException("该住院证已用于办理入院，不能作废；如需处理请走退院/出院流程");
        }
        if (Objects.equals(AdmissionOrderStatusEnum.VOIDED.getCode(), order.getOrderStatus())) {
            throw new BusinessException("该住院证已作废，不能重复作废");
        }

        order.setOrderStatus(AdmissionOrderStatusEnum.VOIDED.getCode());
        order.setCancelReason(dto.getCancelReason());
        bizAdmissionOrderMapper.updateById(order);

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
        if (orderId == null) {
            throw new BusinessException("住院证ID不能为空");
        }
        BizAdmissionOrder order = bizAdmissionOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("住院证不存在");
        }
        if (!Objects.equals(AdmissionOrderStatusEnum.PENDING.getCode(), order.getOrderStatus())) {
            // 已收治 / 已作废都要说清是哪种，别只说一句"状态不对"
            throw new BusinessException("住院证当前状态为「"
                    + AdmissionOrderStatusEnum.labelOrUnknown(order.getOrderStatus()) + "」，不能收治");
        }
        if (order.getValidUntil() != null && order.getValidUntil().isBefore(TimeUtil.nowSeconds())) {
            throw new BusinessException("住院证已于 " + order.getValidUntil() + " 过期，不能再收治，请重新开证");
        }
        return order;
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAdmitted(BizAdmissionOrder order, Long admissionId, Long admitDeptId, LocalDateTime admitTime) {
        // 重新取一次并再判状态：并发下两个入院处同时点"收治"时，靠这里的乐观判断挡住第二个人
        BizAdmissionOrder latest = bizAdmissionOrderMapper.selectById(order.getId());
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
        bizAdmissionOrderMapper.updateById(latest);
        log.info("住院证已收治 orderNo={} admissionId={} admitDeptId={}",
                latest.getOrderNo(), admissionId, admitDeptId);
    }

    /**
     * 补充展示态：文案、是否过期、是否调过科。
     */
    private void decorate(AdmissionOrderVO vo) {
        vo.setGenderText(SysGenderEnum.getText(vo.getGender()));
        vo.setOrderStatusText(AdmissionOrderStatusEnum.getText(vo.getOrderStatus()));

        boolean pending = Objects.equals(AdmissionOrderStatusEnum.PENDING.getCode(), vo.getOrderStatus());
        boolean expired = pending && vo.getValidUntil() != null
                && vo.getValidUntil().isBefore(TimeUtil.nowSeconds());
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
                .eq(SysConfig::getConfigKey, SystemConfigKeyConst.ADMISSION_ORDER_VALID_DAYS));
        if (config == null || !TextUtil.hasText(config.getConfigValue())) {
            log.warn("未配置 {}，住院证有效期按兜底值 {} 天", SystemConfigKeyConst.ADMISSION_ORDER_VALID_DAYS, VALID_DAYS_FALLBACK);
            return VALID_DAYS_FALLBACK;
        }
        try {
            int days = Integer.parseInt(config.getConfigValue().trim());
            if (days <= 0) {
                log.warn("配置 {} = {} 非法（必须为正数），按兜底值 {} 天",
                        SystemConfigKeyConst.ADMISSION_ORDER_VALID_DAYS, config.getConfigValue(), VALID_DAYS_FALLBACK);
                return VALID_DAYS_FALLBACK;
            }
            return days;
        } catch (NumberFormatException e) {
            log.warn("配置 {} = {} 不是数字，按兜底值 {} 天",
                    SystemConfigKeyConst.ADMISSION_ORDER_VALID_DAYS, config.getConfigValue(), VALID_DAYS_FALLBACK);
            return VALID_DAYS_FALLBACK;
        }
    }

    private String nextOrderNo() {
        return redisSequenceService.generateAdmissionOrderNo();
    }
}
