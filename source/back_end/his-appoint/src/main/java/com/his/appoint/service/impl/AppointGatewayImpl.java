package com.his.appoint.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.appoint.entity.BizAppointInfo;
import com.his.appoint.mapper.BizAppointInfoMapper;
import com.his.charge.service.AppointGateway;
import com.his.charge.support.ChargeDeptResolver;
import com.his.charge.vo.RegistBrief;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * {@link AppointGateway} 在 his-appoint 侧的实现。
 *
 * <p>接口由收费域 his-charge 声明、这里负责实现：挂号域依赖收费域（挂号即结算、
 * 记账能力在 charge），收费域反过来不依赖挂号域。若接口留在本模块，
 * charge 就得依赖 appoint，而 appoint 又要依赖 charge，Maven 判定成环直接拒绝构建。
 */
@Service
@RequiredArgsConstructor
public class AppointGatewayImpl implements AppointGateway {

    private final BizAppointInfoMapper appointInfoMapper;

    @Override
    public RegistBrief findRegist(Long registId) {
        if (registId == null) {
            return null;
        }
        return toBrief(appointInfoMapper.selectById(registId));
    }

    @Override
    public ChargeDeptResolver.DeptRef findDeptByRegistNo(String registNo) {
        if (registNo == null || registNo.isBlank()) {
            return null;
        }
        BizAppointInfo row = appointInfoMapper.selectOne(new LambdaQueryWrapper<BizAppointInfo>()
                .eq(BizAppointInfo::getRegistNo, registNo)
                .last("LIMIT 1"));
        return row == null ? null
                : new ChargeDeptResolver.DeptRef(row.getDeptId(), row.getDeptName());
    }

    private RegistBrief toBrief(BizAppointInfo e) {
        if (e == null) {
            return null;
        }
        RegistBrief brief = new RegistBrief();
        brief.setId(e.getId());
        brief.setRegistNo(e.getRegistNo());
        brief.setPatientId(e.getPatientId());
        brief.setPatientName(e.getPatientName());
        brief.setGender(e.getGender());
        brief.setAge(e.getAge());
        brief.setDeptId(e.getDeptId());
        brief.setDeptName(e.getDeptName());
        brief.setDoctorId(e.getDoctorId());
        brief.setDoctorName(e.getDoctorName());
        brief.setVisitType(e.getVisitType());
        brief.setVisitDate(e.getVisitDate());
        brief.setMedicalInsuranceType(e.getMedicalInsuranceType());
        brief.setMedicalInsuranceNo(e.getMedicalInsuranceNo());
        return brief;
    }
}
