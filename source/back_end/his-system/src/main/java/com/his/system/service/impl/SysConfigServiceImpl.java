package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.exception.BusinessException;
import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.util.TextUtil;
import com.his.system.dto.HospitalInfoUpsertDTO;
import com.his.system.entity.SysConfig;
import com.his.system.mapper.SysConfigMapper;
import com.his.system.service.SysConfigService;
import com.his.system.vo.HospitalInfoVO;
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
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysConfig::getConfigKey, keys);
        Map<String, String> valueByKey = list(wrapper).stream()
                .collect(Collectors.toMap(SysConfig::getConfigKey, c -> c.getConfigValue() == null ? "" : c.getConfigValue(), (a, b) -> a));

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

    private void setValue(String configKey, String configName, String configValue) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey).last("LIMIT 1");
        SysConfig existing = getOne(wrapper);
        if (existing != null) {
            existing.setConfigValue(configValue);
            updateById(existing);
            return;
        }
        // B-条件必填：配置行已存在时允许提交空值清空，只有首次建档才必填，DTO 注解无法表达，保留
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
}
