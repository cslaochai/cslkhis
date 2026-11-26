package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.exception.BusinessException;
import com.his.common.support.FieldSpec;
import com.his.common.util.TextUtil;
import com.his.platform.dto.*;
import com.his.platform.entity.SysConfig;
import com.his.platform.mapper.SysConfigMapper;
import com.his.platform.service.FieldChangeRecorder;
import com.his.platform.service.SysConfigService;
import com.his.platform.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    /**
     * 对象类型：系统参数
     */
    private static final String SYS_CONFIG = "SYS_CONFIG";

    /**
     * 参数参与留痕的字段：sys_config 的可见事实就是 configValue，configName 只是给人看的标签。
     */
    private static final List<FieldSpec> CONFIG_FIELDS = FieldSpec.list(
            FieldSpec.of("configValue", "参数值")
    );

    private final FieldChangeRecorder fieldChangeRecorder;

    @Override
    public HospitalInfoVO getHospitalInfo() {
        List<String> keys = List.of(SystemConfigKeyConst.HOSPITAL_NAME, SystemConfigKeyConst.HOSPITAL_ADDRESS, SystemConfigKeyConst.HOSPITAL_PHONE, SystemConfigKeyConst.HOSPITAL_EMAIL);
        Map<String, String> valueByKey = selectValues(keys);

        HospitalInfoVO vo = new HospitalInfoVO();
        vo.setHospitalName(valueByKey.get(SystemConfigKeyConst.HOSPITAL_NAME));
        vo.setHospitalAddress(valueByKey.get(SystemConfigKeyConst.HOSPITAL_ADDRESS));
        vo.setHospitalPhone(valueByKey.get(SystemConfigKeyConst.HOSPITAL_PHONE));
        vo.setHospitalEmail(valueByKey.get(SystemConfigKeyConst.HOSPITAL_EMAIL));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertHospitalInfo(HospitalInfoUpsertDTO upsertDTO) {
        String reason = upsertDTO.getReason();
        setValue(SystemConfigKeyConst.HOSPITAL_NAME, "医院名称", upsertDTO.getHospitalName(), reason);
        setValue(SystemConfigKeyConst.HOSPITAL_ADDRESS, "医院地址", upsertDTO.getHospitalAddress(), reason);
        setValue(SystemConfigKeyConst.HOSPITAL_PHONE, "联系电话", upsertDTO.getHospitalPhone(), reason);
        if (TextUtil.hasText(upsertDTO.getHospitalEmail())) {
            setValue(SystemConfigKeyConst.HOSPITAL_EMAIL, "医院邮箱", upsertDTO.getHospitalEmail(), reason);
        }
    }

    @Override
    public EmergencyConfigVO getEmergencyConfig() {
        List<String> keys = List.of(
                SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX + "1",
                SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX + "2",
                SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX + "3",
                SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX + "4",
                SystemConfigKeyConst.EMERGENCY_WAIT_FALLBACK_RECEIVER,
                SystemConfigKeyConst.EMERGENCY_OBSERVATION_WARN_HOURS,
                SystemConfigKeyConst.EMERGENCY_OBSERVATION_MAX_HOURS);
        Map<String, String> valueByKey = selectValues(keys);

        EmergencyConfigVO vo = new EmergencyConfigVO();
        vo.setWaitDeadlineLevel1(toInt(valueByKey, SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX + "1"));
        vo.setWaitDeadlineLevel2(toInt(valueByKey, SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX + "2"));
        vo.setWaitDeadlineLevel3(toInt(valueByKey, SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX + "3"));
        vo.setWaitDeadlineLevel4(toInt(valueByKey, SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX + "4"));
        vo.setWaitFallbackReceiver(valueByKey.get(SystemConfigKeyConst.EMERGENCY_WAIT_FALLBACK_RECEIVER));
        vo.setObservationWarnHours(toInt(valueByKey, SystemConfigKeyConst.EMERGENCY_OBSERVATION_WARN_HOURS));
        vo.setObservationMaxHours(toInt(valueByKey, SystemConfigKeyConst.EMERGENCY_OBSERVATION_MAX_HOURS));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertEmergencyConfig(EmergencyConfigUpsertDTO upsertDTO) {
        String reason = upsertDTO.getReason();
        setConfigValue(SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX + "1", "急诊候诊时限-Ⅰ级濒危(分钟)",
                String.valueOf(upsertDTO.getWaitDeadlineLevel1()), reason);
        setConfigValue(SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX + "2", "急诊候诊时限-Ⅱ级危重(分钟)",
                String.valueOf(upsertDTO.getWaitDeadlineLevel2()), reason);
        setConfigValue(SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX + "3", "急诊候诊时限-Ⅲ级急症(分钟)",
                String.valueOf(upsertDTO.getWaitDeadlineLevel3()), reason);
        setConfigValue(SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX + "4", "急诊候诊时限-Ⅳ级非急症(分钟)",
                String.valueOf(upsertDTO.getWaitDeadlineLevel4()), reason);
        setConfigValue(SystemConfigKeyConst.EMERGENCY_WAIT_FALLBACK_RECEIVER, "急诊候诊超时-兜底接收人",
                TextUtil.nullToEmpty(upsertDTO.getWaitFallbackReceiver()), reason);
        setConfigValue(SystemConfigKeyConst.EMERGENCY_OBSERVATION_WARN_HOURS, "急诊留观预警时限(小时)",
                String.valueOf(upsertDTO.getObservationWarnHours()), reason);
        setConfigValue(SystemConfigKeyConst.EMERGENCY_OBSERVATION_MAX_HOURS, "急诊留观上限时限(小时)",
                String.valueOf(upsertDTO.getObservationMaxHours()), reason);
    }

    @Override
    public LabCriticalConfigVO getLabCriticalConfig() {
        List<String> keys = List.of(SystemConfigKeyConst.LAB_CRITICAL_VALUE_DEADLINE_MINUTES,
                SystemConfigKeyConst.LAB_CRITICAL_VALUE_FALLBACK_RECEIVER);
        Map<String, String> valueByKey = selectValues(keys);

        LabCriticalConfigVO vo = new LabCriticalConfigVO();
        vo.setCriticalValueDeadlineMinutes(toInt(valueByKey, SystemConfigKeyConst.LAB_CRITICAL_VALUE_DEADLINE_MINUTES));
        vo.setCriticalValueFallbackReceiver(valueByKey.get(SystemConfigKeyConst.LAB_CRITICAL_VALUE_FALLBACK_RECEIVER));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertLabCriticalConfig(LabCriticalConfigUpsertDTO upsertDTO) {
        String reason = upsertDTO.getReason();
        setConfigValue(SystemConfigKeyConst.LAB_CRITICAL_VALUE_DEADLINE_MINUTES, "危急值处置时限",
                String.valueOf(upsertDTO.getCriticalValueDeadlineMinutes()), reason);
        setConfigValue(SystemConfigKeyConst.LAB_CRITICAL_VALUE_FALLBACK_RECEIVER, "危急值兜底接收人",
                TextUtil.nullToEmpty(upsertDTO.getCriticalValueFallbackReceiver()), reason);
    }

    @Override
    public InpatientConfigVO getInpatientConfig() {
        List<String> keys = List.of(SystemConfigKeyConst.INPATIENT_LEAVE_MAX_HOURS,
                SystemConfigKeyConst.DUTY_COORD_BED_WAIT_HOURS,
                SystemConfigKeyConst.DUTY_COORD_REFERRAL_PENDING_HOURS,
                SystemConfigKeyConst.BED_WAIT_MAX_DAYS,
                SystemConfigKeyConst.ADMISSION_ORDER_VALID_DAYS);
        Map<String, String> valueByKey = selectValues(keys);

        InpatientConfigVO vo = new InpatientConfigVO();
        vo.setLeaveMaxHours(toInt(valueByKey, SystemConfigKeyConst.INPATIENT_LEAVE_MAX_HOURS));
        vo.setBedWaitHours(toInt(valueByKey, SystemConfigKeyConst.DUTY_COORD_BED_WAIT_HOURS));
        vo.setReferralPendingHours(toInt(valueByKey, SystemConfigKeyConst.DUTY_COORD_REFERRAL_PENDING_HOURS));
        vo.setBedWaitMaxDays(toInt(valueByKey, SystemConfigKeyConst.BED_WAIT_MAX_DAYS));
        vo.setAdmissionOrderValidDays(toInt(valueByKey, SystemConfigKeyConst.ADMISSION_ORDER_VALID_DAYS));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertInpatientConfig(InpatientConfigUpsertDTO upsertDTO) {
        String reason = upsertDTO.getReason();
        setConfigValue(SystemConfigKeyConst.INPATIENT_LEAVE_MAX_HOURS, "住院请假时长上限",
                String.valueOf(upsertDTO.getLeaveMaxHours()), reason);
        setConfigValue(SystemConfigKeyConst.DUTY_COORD_BED_WAIT_HOURS, "等床催总值班时限",
                String.valueOf(upsertDTO.getBedWaitHours()), reason);
        setConfigValue(SystemConfigKeyConst.DUTY_COORD_REFERRAL_PENDING_HOURS, "转诊待确认催总值班时限",
                String.valueOf(upsertDTO.getReferralPendingHours()), reason);
        setConfigValue(SystemConfigKeyConst.BED_WAIT_MAX_DAYS, "床位最长可等待天数",
                String.valueOf(upsertDTO.getBedWaitMaxDays()), reason);
        setConfigValue(SystemConfigKeyConst.ADMISSION_ORDER_VALID_DAYS, "住院证有效期",
                String.valueOf(upsertDTO.getAdmissionOrderValidDays()), reason);
    }

    @Override
    public SignConfigVO getSignConfig() {
        List<String> keys = List.of(SystemConfigKeyConst.SIGN_TIME_SOURCE,
                SystemConfigKeyConst.SIGN_CERT_VALID_DAYS,
                SystemConfigKeyConst.SIGN_CERT_AUTO_ISSUE,
                SystemConfigKeyConst.SIGN_PIN_TTL_MINUTES);
        Map<String, String> valueByKey = selectValues(keys);

        SignConfigVO vo = new SignConfigVO();
        vo.setTimeSource(toInt(valueByKey, SystemConfigKeyConst.SIGN_TIME_SOURCE));
        vo.setCertValidDays(toInt(valueByKey, SystemConfigKeyConst.SIGN_CERT_VALID_DAYS));
        vo.setCertAutoIssue(toInt(valueByKey, SystemConfigKeyConst.SIGN_CERT_AUTO_ISSUE));
        vo.setPinTtlMinutes(toInt(valueByKey, SystemConfigKeyConst.SIGN_PIN_TTL_MINUTES));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertSignConfig(SignConfigUpsertDTO upsertDTO) {
        String reason = upsertDTO.getReason();
        setConfigValue(SystemConfigKeyConst.SIGN_TIME_SOURCE, "签名时间来源",
                String.valueOf(upsertDTO.getTimeSource()), reason);
        setConfigValue(SystemConfigKeyConst.SIGN_CERT_VALID_DAYS, "签名证书默认有效期（天）",
                String.valueOf(upsertDTO.getCertValidDays()), reason);
        setConfigValue(SystemConfigKeyConst.SIGN_CERT_AUTO_ISSUE, "缺证书时自动签发",
                String.valueOf(upsertDTO.getCertAutoIssue()), reason);
        setConfigValue(SystemConfigKeyConst.SIGN_PIN_TTL_MINUTES, "签名PIN免输入时长（分钟）",
                String.valueOf(upsertDTO.getPinTtlMinutes()), reason);
    }

    /**
     * 按 key 集合一次性读取配置值；key 不存在或值为空时映射为空串，避免前端拿到 null 报错。
     */
    private Map<String, String> selectValues(List<String> keys) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysConfig::getConfigKey, keys);
        return list(wrapper).stream()
                .collect(Collectors.toMap(SysConfig::getConfigKey, c -> c.getConfigValue() == null ? "" : c.getConfigValue(), (a, b) -> a));
    }

    /**
     * 把配置值解析成 Integer；空值/非法值返回 null（页面回显为空，不误显示）。
     */
    private Integer toInt(Map<String, String> valueByKey, String key) {
        String v = valueByKey.get(key);
        if (!TextUtil.hasText(v)) {
            return null;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setValue(String configKey, String configName, String configValue, String reason) {
        saveConfigValue(configKey, configName, configValue, reason, true);
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        SysConfig config = getByKey(configKey);
        return (config != null && TextUtil.hasText(config.getConfigValue())) ? config.getConfigValue() : defaultValue;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setConfigValue(String configKey, String configName, String configValue) {
        saveConfigValue(configKey, configName, configValue, null, false);
    }

    /**
     * 带变更原因的写入口（仅供参数配置页的五个 upsert 使用）：键不存在时允许空值。
     */
    private void setConfigValue(String configKey, String configName, String configValue, String reason) {
        saveConfigValue(configKey, configName, configValue, reason, false);
    }

    /**
     * 参数写入的唯一落点：写库 + 字段级留痕（参数值旧值→新值 + 变更原因）。
     *
     * <p>参数是"全院判定口径"，改一个阈值就等于改所有科室的判定结果，故每条参数单独记一行，
     * 而不是把整页参数当成一坨 —— 事后翻日志要能回答"急诊留观上限是谁从几改成几的"。
     *
     * <p>requiredWhenCreate：参数表（sys_config）里原本不存在的键，医院信息类不允许建空值
     * （否则页面回显空、判定逻辑取到空串），其余数值型键允许空。
     */
    private void saveConfigValue(String configKey, String configName, String configValue,
                                 String reason, boolean requiredWhenCreate) {
        SysConfig existing = getByKey(configKey);
        String oldValue = existing == null ? null : existing.getConfigValue();
        if (existing != null) {
            existing.setConfigValue(configValue);
            updateById(existing);
        } else if (TextUtil.hasText(configValue) || !requiredWhenCreate) {
            SysConfig entity = new SysConfig();
            entity.setConfigName(configName);
            entity.setConfigKey(configKey);
            entity.setConfigValue(configValue);
            entity.setConfigType(0);
            entity.setIsSystem(1);
            save(entity);
        } else {
            throw new BusinessException(configName + "不能为空");
        }
        // 落库后回读比对：updateById 会忽略 null 字段，直接拿入参比会把"没变"记成"改成空"
        SysConfig after = getByKey(configKey);
        if (after == null) {
            return;
        }
        SysConfig before = existing == null ? null : snapshotValue(oldValue);
        fieldChangeRecorder.record(SYS_CONFIG, configKey, configKey, configName,
                before, after, CONFIG_FIELDS, reason);
    }

    private SysConfig getByKey(String configKey) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey).last("LIMIT 1");
        return getOne(wrapper);
    }

    /**
     * 只带 configValue 的快照：留痕字段清单里就这一个，其余列不参与比对。
     */
    private SysConfig snapshotValue(String configValue) {
        SysConfig snapshot = new SysConfig();
        snapshot.setConfigValue(configValue);
        return snapshot;
    }
}
