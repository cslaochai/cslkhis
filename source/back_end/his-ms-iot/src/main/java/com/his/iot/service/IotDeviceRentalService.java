package com.his.iot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.iot.dto.*;
import com.his.iot.vo.DeviceRentalCatalogVO;
import com.his.iot.vo.DeviceRentalOrderVO;

import java.util.List;

/**
 * 设备共享调配：共享池目录、申请单、审批、发放、归还、验收。
 */
public interface IotDeviceRentalService {

    /**
     * 共享池目录（设备台账可共享且在用），带当前可借/总台数。
     */
    List<DeviceRentalCatalogVO> listCatalog(String keyword);

    /**
     * 申请单分页。临床岗位只看本科室申请；设备科/管理员看全院。
     */
    IPage<DeviceRentalOrderVO> listPage(DeviceRentalPageQueryDTO query);

    DeviceRentalOrderVO getDetail(Long id);

    /**
     * 临床科室发起申请（一行一台设备）。
     */
    Long create(DeviceRentalCreateDTO dto);

    /**
     * 审批：通过进入待发放，驳回进入已驳回。
     */
    void approve(DeviceRentalApproveDTO dto);

    /**
     * 设备科发放：分配具体设备；IoT 泵同时绑定患者。
     */
    void issue(DeviceRentalActionDTO dto);

    /**
     * 临床申请归还：设备进入待验收。
     */
    void markReturn(DeviceRentalReturnDTO dto);

    /**
     * 设备科验收：IoT 设备解绑，明细完成，库存恢复。
     */
    void accept(DeviceRentalActionDTO dto);
}
