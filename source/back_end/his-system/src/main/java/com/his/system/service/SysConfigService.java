package com.his.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.system.dto.HospitalInfoUpsertDTO;
import com.his.system.entity.SysConfig;
import com.his.system.vo.HospitalInfoVO;

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
}
