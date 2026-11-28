package com.his.iot.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.iot.dto.IotDeviceQueryDTO;
import com.his.iot.entity.IotDevice;
import com.his.iot.mapper.IotDeviceMapper;
import com.his.iot.service.IotDeviceService;
import com.his.iot.vo.IotDeviceVO;
import com.his.spi.service.DeptScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 设备档案只读查询。只返回启用设备：停用设备不参与任何监控页面。
 * <p>
 * 科室收口走 {@link DeptScopeService}：传了 deptId 先过授权门禁，选中父科室按子树展开并与授权集合交集；
 * 不传时受限角色落授权科室集合，全院角色不加科室条件。档案表 dept_id 为 null 的设备在受限角色下不可见。
 */
@Service
@RequiredArgsConstructor
public class IotDeviceServiceImpl extends ServiceImpl<IotDeviceMapper, IotDevice> implements IotDeviceService {

    private final DeptScopeService deptScopeService;

    @Override
    public List<IotDeviceVO> selectList(IotDeviceQueryDTO query) {
        Set<Long> scopeDeptIds = resolveScopeDeptIds(query.getDeptId());
        // 受限角色且授权子树非空才拼 IN；交集为空时强制空结果，不能退化成不加科室条件=全院
        boolean restricted = deptScopeService.isScoped() && !scopeDeptIds.isEmpty();
        return list(Wrappers.<IotDevice>lambdaQuery()
                .eq(IotDevice::getEnabled, 1)
                .eq(query.getDeviceType() != null && !query.getDeviceType().isBlank(),
                        IotDevice::getDeviceType, query.getDeviceType())
                .eq(query.getDeviceNo() != null && !query.getDeviceNo().isBlank(),
                        IotDevice::getDeviceNo, query.getDeviceNo())
                .in(restricted, IotDevice::getDeptId, scopeDeptIds)
                .eq(!restricted && deptScopeService.isScoped(), IotDevice::getDeptId, -1L)
                .orderByAsc(IotDevice::getDeviceType)
                .orderByAsc(IotDevice::getDeviceNo))
                .stream().map(this::toVO).toList();
    }

    /**
     * 解析进 SQL 的科室集合：门禁 → 子树展开 → 与授权集合交集。全院角色返回空集合（不加 IN 条件）。
     */
    private Set<Long> resolveScopeDeptIds(Long requestedDeptId) {
        Long target = deptScopeService.resolveDeptId(requestedDeptId);
        Set<Long> scope = new HashSet<>();
        if (target != null) {
            scope.addAll(deptScopeService.descendantDeptIds(target));
        } else {
            scope.addAll(deptScopeService.allowedDeptIds());
        }
        if (deptScopeService.isScoped()) {
            scope.retainAll(deptScopeService.allowedDeptIds());
        }
        return scope;
    }

    private IotDeviceVO toVO(IotDevice d) {
        IotDeviceVO vo = new IotDeviceVO();
        vo.setDeviceNo(d.getDeviceNo());
        vo.setDeviceType(d.getDeviceType());
        vo.setPowerType(d.getPowerType());
        vo.setDeviceName(d.getDeviceName());
        vo.setDeptName(d.getDeptName());
        vo.setLocation(d.getLocation());
        vo.setBedNo(d.getBedNo());
        vo.setEquipmentId(d.getEquipmentId());
        vo.setEnabled(d.getEnabled());
        return vo;
    }
}
