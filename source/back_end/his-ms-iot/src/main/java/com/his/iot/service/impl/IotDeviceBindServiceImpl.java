package com.his.iot.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.AdmitStatusEnum;
import com.his.common.enums.IotBindSourceEnum;
import com.his.common.enums.IotBindStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.utils.UserUtils;
import com.his.iot.dto.IotBindQueryDTO;
import com.his.iot.dto.IotDeviceBindDTO;
import com.his.iot.dto.IotDeviceUnbindDTO;
import com.his.iot.entity.IotDevice;
import com.his.iot.entity.IotDeviceBind;
import com.his.iot.mapper.IotDeviceBindMapper;
import com.his.iot.mapper.IotDeviceMapper;
import com.his.iot.service.IotDeviceBindService;
import com.his.iot.vo.IotBindCandidateDeviceVO;
import com.his.iot.vo.IotDeviceBindVO;
import com.his.spi.service.AdmissionQueryFacadeService;
import com.his.spi.service.InpatientFacadeService;
import com.his.spi.vo.AdmissionActiveBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 设备—患者绑定实现。
 * <p>
 * 绑定对象是「在院就诊（admission_id）」而不是床位：泵跟着患者走，转床不需要重绑。
 * 床位/科室/病区快照由服务端按 admissionId 从住院域现取，不信任页面传来的床位数据（那是几秒前的图）。
 */
@Service
@RequiredArgsConstructor
public class IotDeviceBindServiceImpl extends ServiceImpl<IotDeviceBindMapper, IotDeviceBind>
        implements IotDeviceBindService {

    /** 输液泵绑定弹框的默认设备类型（护士站当前只接这一类） */
    private static final String DEFAULT_DEVICE_TYPE = "infusion";

    private final IotDeviceMapper iotDeviceMapper;
    private final AdmissionQueryFacadeService admissionQueryFacadeService;
    private final InpatientFacadeService inpatientFacadeService;

    @Override
    public List<IotDeviceBindVO> listActiveBinds(IotBindQueryDTO query) {
        List<IotDeviceBindVO> binds = baseMapper.selectActiveBinds(query.getDeviceType());
        for (IotDeviceBindVO vo : binds) {
            vo.setBindSourceText(IotBindSourceEnum.getLabel(vo.getBindSource()));
        }
        return binds;
    }

    @Override
    public List<IotBindCandidateDeviceVO> selectBindableDevices(IotBindQueryDTO query) {
        String deviceType = query.getDeviceType() == null ? DEFAULT_DEVICE_TYPE : query.getDeviceType();
        return baseMapper.selectBindableDevices(deviceType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bindDevice(IotDeviceBindDTO dto) {
        IotDevice device = iotDeviceMapper.selectOne(Wrappers.<IotDevice>lambdaQuery()
                .eq(IotDevice::getDeviceNo, dto.getDeviceNo()));
        if (device == null) {
            throw new BusinessException(400, "设备不存在：" + dto.getDeviceNo());
        }
        if (!Objects.equals(device.getEnabled(), 1)) {
            throw new BusinessException(400, "设备已停用，不能绑定：" + dto.getDeviceNo());
        }
        // 设备类型必须与档案一致：手工输入设备号时，不能把气体终端/床垫当成输液泵绑到患者身上
        if (dto.getDeviceType() != null && !dto.getDeviceType().equals(device.getDeviceType())) {
            throw new BusinessException(400, "设备类型不一致：" + dto.getDeviceNo() + " 是 " + device.getDeviceType());
        }
        Integer bindSource = dto.getBindSource() == null ? IotBindSourceEnum.SCAN.getCode() : dto.getBindSource();
        if (IotBindSourceEnum.fromCode(bindSource) == null) {
            throw new BusinessException(400, "绑定方式取值不合法：" + bindSource);
        }
        CurrentUser operator = UserUtils.getCurrentUser();
        if (operator == null) {
            throw new BusinessException(401, "未获取到登录用户，不能绑定设备");
        }
        IotDeviceBind previous = findActiveBind(dto.getDeviceNo());
        if (previous != null) {
            // 出院时护士常常来不及解绑，泵直接留在床边。这里自愈：旧患者已出院就自动回收，
            // 仍在院则拒绝（一台泵不能同时挂在两个人身上）。
            AdmissionActiveBriefVO previousAdmission =
                    admissionQueryFacadeService.findAdmissionBrief(previous.getAdmissionId());
            boolean discharged = previousAdmission != null
                    && Objects.equals(previousAdmission.getAdmitStatus(), AdmitStatusEnum.DISCHARGED.getCode());
            if (!discharged) {
                throw new BusinessException(400, "该设备已绑定在其他在院患者，请先解绑：" + dto.getDeviceNo());
            }
            release(previous, "患者已出院自动解绑", operator);
        }

        // 在院校验 + 床位快照都走住院域端口：已出院/不存在直接拒，床位取当前值而不是前端传来的
        AdmissionActiveBriefVO admission = admissionQueryFacadeService.findAdmissionBrief(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException(400, "住院就诊不存在：" + dto.getAdmissionId());
        }
        if (!Objects.equals(admission.getAdmitStatus(), AdmitStatusEnum.IN_HOSPITAL.getCode())) {
            throw new BusinessException(400, "患者已出院，不能绑定设备");
        }

        IotDeviceBind bind = new IotDeviceBind();
        bind.setDeviceNo(device.getDeviceNo());
        bind.setDeviceType(device.getDeviceType());
        bind.setPatientId(admission.getPatientId());
        bind.setAdmissionId(admission.getAdmissionId());
        bind.setBedId(admission.getBedId());
        bind.setBedNo(admission.getBedId() == null ? null : inpatientFacadeService.getBedNo(admission.getBedId()));
        bind.setWardId(admission.getWardId());
        bind.setDeptId(admission.getDeptId());
        bind.setBindSource(bindSource);
        bind.setBindTime(LocalDateTime.now());
        bind.setBindBy(operator.getRealName());
        bind.setBindById(operator.getEmployeeId());
        bind.setBindStatus(IotBindStatusEnum.IN_USE.getCode());
        bind.setRemark(dto.getRemark());
        save(bind);
        return bind.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindDevice(IotDeviceUnbindDTO dto) {
        IotDeviceBind bind = findActiveBind(dto.getDeviceNo());
        if (bind == null) {
            throw new BusinessException(400, "设备当前没有在用绑定：" + dto.getDeviceNo());
        }
        CurrentUser operator = UserUtils.getCurrentUser();
        if (operator == null) {
            throw new BusinessException(401, "未获取到登录用户，不能解绑设备");
        }
        release(bind, dto.getReason() == null || dto.getReason().isBlank() ? "护士站解绑" : dto.getReason(), operator);
    }

    /**
     * 解绑一条在用绑定（换泵/治疗结束/出院回收），行保留。
     */
    private void release(IotDeviceBind bind, String reason, CurrentUser operator) {
        bind.setBindStatus(IotBindStatusEnum.RELEASED.getCode());
        bind.setUnbindTime(LocalDateTime.now());
        bind.setUnbindBy(operator.getRealName());
        bind.setUnbindById(operator.getEmployeeId());
        bind.setUnbindReason(reason);
        updateById(bind);
    }

    /**
     * 一台设备同时只会有一条在用绑定（业务约束；表上没有唯一键——历史解绑行会重复 device_no）。
     */
    @Override
    public IotDeviceBind findActiveBind(String deviceNo) {
        return getOne(Wrappers.<IotDeviceBind>lambdaQuery()
                .eq(IotDeviceBind::getDeviceNo, deviceNo)
                .eq(IotDeviceBind::getBindStatus, IotBindStatusEnum.IN_USE.getCode())
                .last("LIMIT 1"), false);
    }
}
