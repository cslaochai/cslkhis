package com.his.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.platform.dto.*;
import com.his.platform.entity.SysConfig;
import com.his.platform.vo.*;

/**
 * 系统参数配置服务
 */
public interface SysConfigService extends IService<SysConfig> {

    /**
     * 读取医院基础信息（hospital.name / address / phone / email）
     */
    HospitalInfoVO getHospitalInfo();

    /**
     * 保存医院基础信息，按键存在则更新、不存在则插入
     */
    void upsertHospitalInfo(HospitalInfoUpsertDTO upsertDTO);

    /**
     * 读取急诊参数（emergency.* 键）
     */
    EmergencyConfigVO getEmergencyConfig();

    /**
     * 保存急诊参数（emergency.* 键）
     */
    void upsertEmergencyConfig(EmergencyConfigUpsertDTO upsertDTO);

    /**
     * 读取检验危急值参数（lab.* 键）
     */
    LabCriticalConfigVO getLabCriticalConfig();

    /**
     * 保存检验危急值参数（lab.* 键）
     */
    void upsertLabCriticalConfig(LabCriticalConfigUpsertDTO upsertDTO);

    /**
     * 读取住院 / 床位 / 转诊 / 住院证参数（inpatient.* / duty.coord.* / bed.* / admission_order.* 键）
     */
    InpatientConfigVO getInpatientConfig();

    /**
     * 保存住院 / 床位 / 转诊 / 住院证参数（inpatient.* / duty.coord.* / bed.* / admission_order.* 键）
     */
    void upsertInpatientConfig(InpatientConfigUpsertDTO upsertDTO);

    /**
     * 读取电子签名 / 可信时间戳参数（sign.* 键）
     */
    SignConfigVO getSignConfig();

    /**
     * 保存电子签名 / 可信时间戳参数（sign.* 键）
     */
    void upsertSignConfig(SignConfigUpsertDTO upsertDTO);

    /**
     * 读取系统参数值；key 不存在或值为空时返回 defaultValue。
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * 设置系统参数值（存在则更新，不存在则插入），用于运行时修改系统参数。
     */
    void setConfigValue(String configKey, String configName, String configValue);
}
