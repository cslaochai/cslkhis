package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.BloodDTO;
import com.his.medicaltech.entity.BizBloodInventory;
import com.his.medicaltech.vo.BloodVO;

/**
 * 血库储血台账服务：血袋入库、库存异动、配血单全流程与出入库流水的业务收口。
 */
public interface BloodService {

    /**
     * 血袋入库登记：写入库存行并记入库流水。
     * 用于血库储血台账页「入库」弹框确认后落账。
     */
    BizBloodInventory inbound(BloodDTO.Inbound dto);

    /**
     * 库存台账分页：按袋号/血型/成分/状态/效期预警天数过滤。
     * 用于血库储血台账页库存查询卡。
     */
    PageResult<BloodVO.InventoryVO> inventoryPage(BloodDTO.InventoryQuery q);

    /**
     * 库存行转台账出参：补齐状态文案等展示字段。
     * 入库登记回执行渲染时调用。
     */
    BloodVO.InventoryVO toInvVo(BizBloodInventory b);

    /**
     * 库存统计：在库/预留/效期预警总量与分血型分布。
     * 用于血库储血台账页顶部统计卡。
     */
    BloodVO.StatsVO inventoryStats();

    /**
     * 预留血袋：把在库血袋置为已预留并记流水。
     * 用于库存台账行「预留」操作。
     */
    void reserve(BloodDTO.BagAction dto);

    /**
     * 取消预留：把已预留血袋释回在库。
     * 用于库存台账行「取消预留」操作。
     */
    void cancelReserve(BloodDTO.BagAction dto);

    /**
     * 发血：仅「已预留」（预留由配血复核相合产生）血袋可发出，必须带用血申请单号，并发下靠状态守卫防一袋两发。
     * 用于库存台账行「发血」操作。
     */
    void issue(BloodDTO.BagAction dto);

    /**
     * 血袋报废：置为报废态并记流水，原因必填。
     * 用于库存台账行「报废」操作。
     */
    void scrap(BloodDTO.BagAction dto);

    /**
     * 血袋退回：仅「在库/已预留」血袋可退回，原因必填。
     * 用于库存台账行「退回」操作。
     */
    void returnBag(BloodDTO.BagAction dto);

    /**
     * 配血单分页：按单号/袋号/患者/状态/结果过滤。
     * 用于血库储血台账页配血查询卡。
     */
    PageResult<BloodVO.CrossmatchVO> crossmatchPage(BloodDTO.CrossmatchQuery q);

    /**
     * 新建配血单：生成待配血单据。
     * 用于配血台账「新建配血单」弹框提交。
     */
    BloodVO.CrossmatchVO crossmatchCreate(BloodDTO.CrossmatchCreate dto);

    /**
     * 执行配血：登记配血结果，单据转入待复核。
     * 用于配血台账行「执行配血」操作。
     */
    void crossmatchExecute(BloodDTO.CrossmatchExecute dto);

    /**
     * 配血复核：复核人不得是配血人；结果相合时自动预留名下血袋。
     * 用于配血台账行「复核」操作。
     */
    void crossmatchVerify(BloodDTO.CrossmatchVerify dto);

    /**
     * 作废配血单：已复核的单据不可作废。
     * 用于配血台账行「作废」操作。
     */
    void crossmatchVoid(Long matchId);

    /**
     * 出入库流水分页：逐笔记录血袋状态迁移与操作人。
     * 用于血库储血台账页流水查询卡。
     */
    PageResult<BloodVO.StockLogVO> logPage(BloodDTO.LogQuery q);
}
