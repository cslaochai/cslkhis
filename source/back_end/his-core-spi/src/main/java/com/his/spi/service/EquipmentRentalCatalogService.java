package com.his.spi.service;

import com.his.spi.vo.EquipmentRentalCatalogVO;

import java.util.List;

/**
 * 设备共享池目录端口：只读设备台账，宿主模块是 platform（sys_equipment 的宿主）。
 * <p>
 * 设备共享调配模块只依赖这个取数端口，不跨模块读 sys_equipment 实体。
 */
public interface EquipmentRentalCatalogService {

    /**
     * 查询共享池目录。keyword 匹配设备编码/名称/型号/科室；status 固定取在用（1）。
     */
    List<EquipmentRentalCatalogVO> listRentable(String keyword);

    /**
     * 按设备台账ID取共享池设备；不存在/未启用/不在共享池返回 null。
     */
    EquipmentRentalCatalogVO getRentableById(Long equipmentId);
}
