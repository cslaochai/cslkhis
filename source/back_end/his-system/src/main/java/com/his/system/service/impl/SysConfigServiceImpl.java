package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.exception.BusinessException;
import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.util.TextUtil;
import com.his.system.dto.EmergencyConfigUpsertDTO;
import com.his.system.dto.HospitalInfoUpsertDTO;
import com.his.system.dto.InpatientConfigUpsertDTO;
import com.his.system.dto.LabCriticalConfigUpsertDTO;
import com.his.system.dto.SignConfigUpsertDTO;
import com.his.system.entity.SysConfig;
import com.his.system.mapper.SysConfigMapper;
import com.his.system.service.SysConfigService;
import com.his.system.vo.EmergencyConfigVO;
import com.his.system.vo.HospitalInfoVO;
import com.his.system.vo.InpatientConfigVO;
import com.his.system.vo.LabCriticalConfigVO;
import com.his.system.vo.SignConfigVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

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
        setValue(SystemConfigKeyConst.HOSPITAL_NAME, "医院名称", upsertDTO.getHospitalName());
        setValue(SystemConfigKeyConst.HOSPITAL_ADDRESS, "医院地址", upsertDTO.getHospitalAddress());
        setValue(SystemConfigKeyConst.HOSPITAL_PHONE, "联系电话", upsertDTO.getHospitalPhone());
        if (TextUtil.hasText(upsertDTO.getHospitalEmail())) {
            setValue(SystemConfigKeyConst.HOSPITAL_EMAIL, "医院邮箱", upsertDTO.getHospitalEmail());
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
        setConfigValue(SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX + "1", "急诊候诊时限-Ⅰ级濒危(分钟)",
                String.valueOf(upsertDTO.getWaitDeadlineLevel1()));
        setConfigValue(SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX + "2", "急诊候诊时限-Ⅱ级危重(分钟)",
                String.valueOf(upsertDTO.getWaitDeadlineLevel2()));
        setConfigValue(SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX + "3", "急诊候诊时限-Ⅲ级急症(分钟)",
                String.valueOf(upsertDTO.getWaitDeadlineLevel3()));
        setConfigValue(SystemConfigKeyConst.EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX + "4", "急诊候诊时限-Ⅳ级非急症(分钟)",
                String.valueOf(upsertDTO.getWaitDeadlineLevel4()));
        setConfigValue(SystemConfigKeyConst.EMERGENCY_WAIT_FALLBACK_RECEIVER, "急诊候诊超时-兜底接收人",
                TextUtil.nullToEmpty(upsertDTO.getWaitFallbackReceiver()));
        setConfigValue(SystemConfigKeyConst.EMERGENCY_OBSERVATION_WARN_HOURS, "急诊留观预警时限(小时)",
                String.valueOf(upsertDTO.getObservationWarnHours()));
        setConfigValue(SystemConfigKeyConst.EMERGENCY_OBSERVATION_MAX_HOURS, "急诊留观上限时限(小时)",
                String.valueOf(upsertDTO.getObservationMaxHours()));
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
        setConfigValue(SystemConfigKeyConst.LAB_CRITICAL_VALUE_DEADLINE_MINUTES, "危急值处置时限",
                String.valueOf(upsertDTO.getCriticalValueDeadlineMinutes()));
        setConfigValue(SystemConfigKeyConst.LAB_CRITICAL_VALUE_FALLBACK_RECEIVER, "危急值兜底接收人",
                TextUtil.nullToEmpty(upsertDTO.getCriticalValueFallbackReceiver()));
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
        setConfigValue(SystemConfigKeyConst.INPATIENT_LEAVE_MAX_HOURS, "住院请假时长上限",
                String.valueOf(upsertDTO.getLeaveMaxHours()));
        setConfigValue(SystemConfigKeyConst.DUTY_COORD_BED_WAIT_HOURS, "等床催总值班时限",
                String.valueOf(upsertDTO.getBedWaitHours()));
        setConfigValue(SystemConfigKeyConst.DUTY_COORD_REFERRAL_PENDING_HOURS, "转诊待确认催总值班时限",
                String.valueOf(upsertDTO.getReferralPendingHours()));
        setConfigValue(SystemConfigKeyConst.BED_WAIT_MAX_DAYS, "床位最长可等待天数",
                String.valueOf(upsertDTO.getBedWaitMaxDays()));
        setConfigValue(SystemConfigKeyConst.ADMISSION_ORDER_VALID_DAYS, "住院证有效期",
                String.valueOf(upsertDTO.getAdmissionOrderValidDays()));
    }

    @Override
    public SignConfigVO getSignConfig() {
        List<String> keys = List.of(SystemConfigKeyConst.SIGN_TIME_SOURCE,
                SystemConfigKeyConst.SIGN_CERT_VALID_DAYS,
                SystemConfigKeyConst.SIGN_CERT_AUTO_ISSUE);
        Map<String, String> valueByKey = selectValues(keys);

        SignConfigVO vo = new SignConfigVO();
        vo.setTimeSource(toInt(valueByKey, SystemConfigKeyConst.SIGN_TIME_SOURCE));
        vo.setCertValidDays(toInt(valueByKey, SystemConfigKeyConst.SIGN_CERT_VALID_DAYS));
        vo.setCertAutoIssue(toInt(valueByKey, SystemConfigKeyConst.SIGN_CERT_AUTO_ISSUE));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertSignConfig(SignConfigUpsertDTO upsertDTO) {
        setConfigValue(SystemConfigKeyConst.SIGN_TIME_SOURCE, "签名时间来源",
                String.valueOf(upsertDTO.getTimeSource()));
        setConfigValue(SystemConfigKeyConst.SIGN_CERT_VALID_DAYS, "签名证书默认有效期（天）",
                String.valueOf(upsertDTO.getCertValidDays()));
        setConfigValue(SystemConfigKeyConst.SIGN_CERT_AUTO_ISSUE, "缺证书时自动签发",
                String.valueOf(upsertDTO.getCertAutoIssue()));
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

    private void setValue(String configKey, String configName, String configValue) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey).last("LIMIT 1");
        SysConfig existing = getOne(wrapper);
        if (existing != null) {
            existing.setConfigValue(configValue);
            updateById(existing);
            return;
        }
        if (!TextUtil.hasText(configValue)) {
            throw new BusinessException(configName + "不能为空");
        }
        SysConfig entity = new SysConfig();
        entity.setConfigName(configName);
        entity.setConfigKey(configKey);
        entity.setConfigValue(configValue);
        entity.setConfigType(0);
        entity.setIsSystem(1);
        save(entity);
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey).last("LIMIT 1");
        SysConfig config = getOne(wrapper);
        return (config != null && TextUtil.hasText(config.getConfigValue())) ? config.getConfigValue() : defaultValue;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setConfigValue(String configKey, String configName, String configValue) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey).last("LIMIT 1");
        SysConfig existing = getOne(wrapper);
        if (existing != null) {
            existing.setConfigValue(configValue);
            updateById(existing);
            return;
        }
        SysConfig entity = new SysConfig();
        entity.setConfigName(configName);
        entity.setConfigKey(configKey);
        entity.setConfigValue(configValue);
        entity.setConfigType(0);
        entity.setIsSystem(1);
        save(entity);
    }
}
