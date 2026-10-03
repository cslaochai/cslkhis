package com.his.equipment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.equipment.dto.EquipmentDTO;
import com.his.equipment.vo.EquipmentVO;
import com.his.equipment.vo.MaintainVO;
import com.his.equipment.vo.MeteringVO;

public interface EquipmentService {

    IPage<EquipmentVO> listPage(EquipmentDTO.QueryPage q);

    EquipmentVO getDetailById(Long equipmentId);

    IPage<MaintainVO> maintainListPage(EquipmentDTO.MaintainQueryPage q);

    MaintainVO maintainCreate(EquipmentDTO.MaintainCreate dto);

    void maintainDelete(Long id);

    IPage<MeteringVO> meteringListPage(EquipmentDTO.MeteringQueryPage q);

    MeteringVO meteringCreate(EquipmentDTO.MeteringCreate dto);

    void meteringDelete(Long id);
}
