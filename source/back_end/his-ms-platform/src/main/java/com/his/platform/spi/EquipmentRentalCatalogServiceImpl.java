package com.his.platform.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.EnableStatusEnum;
import com.his.common.util.TextUtil;
import com.his.platform.entity.SysEquipment;
import com.his.platform.mapper.SysEquipmentMapper;
import com.his.spi.service.EquipmentRentalCatalogService;
import com.his.spi.vo.EquipmentRentalCatalogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 共享池目录端口实现：只依赖 sys_equipment 的 mapper，不注入平台编排 Service，保持叶子实现。
 */
@Service
@RequiredArgsConstructor
public class EquipmentRentalCatalogServiceImpl implements EquipmentRentalCatalogService {

    private final SysEquipmentMapper sysEquipmentMapper;

    @Override
    public List<EquipmentRentalCatalogVO> listRentable(String keyword) {
        String kw = TextUtil.trim(keyword);
        LambdaQueryWrapper<SysEquipment> w = new LambdaQueryWrapper<SysEquipment>()
                .eq(SysEquipment::getRentable, EnableStatusEnum.ENABLED.getCode())
                .eq(SysEquipment::getStatus, 1)
                .eq(SysEquipment::getDelFlag, 0)
                .and(TextUtil.hasText(kw), x -> x
                        .like(SysEquipment::getEquipmentCode, kw)
                        .or().like(SysEquipment::getEquipmentName, kw)
                        .or().like(SysEquipment::getModel, kw)
                        .or().like(SysEquipment::getDeptName, kw))
                .orderByAsc(SysEquipment::getEquipmentName)
                .orderByAsc(SysEquipment::getEquipmentCode);
        return sysEquipmentMapper.selectList(w).stream().map(this::toVo).toList();
    }

    @Override
    public EquipmentRentalCatalogVO getRentableById(Long equipmentId) {
        SysEquipment e = sysEquipmentMapper.selectOne(new LambdaQueryWrapper<SysEquipment>()
                .eq(SysEquipment::getId, equipmentId)
                .eq(SysEquipment::getRentable, EnableStatusEnum.ENABLED.getCode())
                .eq(SysEquipment::getStatus, 1)
                .eq(SysEquipment::getDelFlag, 0));
        return e == null ? null : toVo(e);
    }

    private EquipmentRentalCatalogVO toVo(SysEquipment e) {
        EquipmentRentalCatalogVO vo = new EquipmentRentalCatalogVO();
        vo.setId(e.getId());
        vo.setEquipmentCode(e.getEquipmentCode());
        vo.setEquipmentName(e.getEquipmentName());
        vo.setCategory(e.getCategory());
        vo.setDeptId(e.getDeptId());
        vo.setDeptName(e.getDeptName());
        vo.setBrand(e.getBrand());
        vo.setModel(e.getModel());
        vo.setStatus(e.getStatus());
        vo.setRentable(e.getRentable());
        vo.setLastMaintainDate(e.getLastMaintainDate());
        return vo;
    }
}
