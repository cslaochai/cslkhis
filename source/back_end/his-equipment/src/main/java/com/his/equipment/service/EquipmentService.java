package com.his.equipment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.equipment.dto.EquipmentQueryPageDTO;
import com.his.equipment.dto.MaintainCreateDTO;
import com.his.equipment.dto.MaintainQueryPageDTO;
import com.his.equipment.dto.MeteringCreateDTO;
import com.his.equipment.dto.MeteringQueryPageDTO;
import com.his.equipment.vo.EquipmentVO;
import com.his.equipment.vo.MaintainVO;
import com.his.equipment.vo.MeteringVO;

public interface EquipmentService {

    IPage<EquipmentVO> listPage(EquipmentQueryPageDTO q);

    EquipmentVO getDetailById(Long equipmentId);

    IPage<MaintainVO> maintainListPage(MaintainQueryPageDTO q);

    MaintainVO maintainCreate(MaintainCreateDTO dto);

    void maintainDelete(Long id);

    IPage<MeteringVO> meteringListPage(MeteringQueryPageDTO q);

    MeteringVO meteringCreate(MeteringCreateDTO dto);

    void meteringDelete(Long id);
}
