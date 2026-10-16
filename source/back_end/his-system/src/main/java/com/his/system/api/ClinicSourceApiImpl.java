package com.his.system.api;

import com.his.api.dto.ScheduleSelectQueryDTO;
import com.his.api.service.ClinicSourceApi;
import com.his.api.vo.ClinicSourceVO;
import com.his.system.service.BizClinicSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 患者端号源查询端口实现（不走院内科室数据权限，直接按患者选择取数）。
 */
@Service
@RequiredArgsConstructor
public class ClinicSourceApiImpl implements ClinicSourceApi {

    private final BizClinicSourceService bizClinicSourceService;

    @Override
    public List<ClinicSourceVO> scheduleSelectListUnbounded(ScheduleSelectQueryDTO queryDTO) {
        return bizClinicSourceService.scheduleSelectListUnbounded(queryDTO).stream()
                .map(source -> {
                    ClinicSourceVO vo = new ClinicSourceVO();
                    BeanUtils.copyProperties(source, vo);
                    return vo;
                })
                .toList();
    }
}
