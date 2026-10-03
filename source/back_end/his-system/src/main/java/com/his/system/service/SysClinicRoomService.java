package com.his.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.system.dto.ClinicRoomQueryDTO;
import com.his.system.dto.ClinicRoomUpsertDTO;
import com.his.system.entity.SysClinicRoom;
import com.his.system.vo.ClinicRoomVO;

import java.util.List;

public interface SysClinicRoomService extends IService<SysClinicRoom> {

    PageResult<ClinicRoomVO> listPage(ClinicRoomQueryDTO queryDTO);

    List<ClinicRoomVO> listAll(ClinicRoomQueryDTO queryDTO);

    ClinicRoomVO getInfo(Long roomId);

    /**
     * @return 面向用户的操作结果文案（新增/修改两条路文案不同）
     */
    String upsert(ClinicRoomUpsertDTO upsertDTO);

    void delete(Long roomId);
}
