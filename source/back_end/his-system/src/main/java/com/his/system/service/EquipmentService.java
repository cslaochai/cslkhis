package com.his.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.system.dto.EquipmentQueryPageDTO;
import com.his.system.dto.MaintainCreateDTO;
import com.his.system.dto.MaintainQueryPageDTO;
import com.his.system.dto.MeteringCreateDTO;
import com.his.system.dto.MeteringQueryPageDTO;
import com.his.system.vo.EquipmentVO;
import com.his.system.vo.MaintainVO;
import com.his.system.vo.MeteringVO;

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
