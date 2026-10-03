package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.BloodDTO;
import com.his.medicaltech.entity.BizBloodInventory;
import com.his.medicaltech.vo.BloodVO;

public interface BloodService {

    BizBloodInventory inbound(BloodDTO.Inbound dto);

    PageResult<BloodVO.InventoryVO> inventoryPage(BloodDTO.InventoryQuery q);

    BloodVO.InventoryVO toInvVo(BizBloodInventory b);

    BloodVO.StatsVO inventoryStats();

    void reserve(BloodDTO.BagAction dto);

    void cancelReserve(BloodDTO.BagAction dto);

    void issue(BloodDTO.BagAction dto);

    void scrap(BloodDTO.BagAction dto);

    void returnBag(BloodDTO.BagAction dto);

    PageResult<BloodVO.CrossmatchVO> crossmatchPage(BloodDTO.CrossmatchQuery q);

    BloodVO.CrossmatchVO crossmatchCreate(BloodDTO.CrossmatchCreate dto);

    void crossmatchExecute(BloodDTO.CrossmatchExecute dto);

    void crossmatchVerify(BloodDTO.CrossmatchVerify dto);

    void crossmatchVoid(Long matchId);

    PageResult<BloodVO.StockLogVO> logPage(BloodDTO.LogQuery q);
}
