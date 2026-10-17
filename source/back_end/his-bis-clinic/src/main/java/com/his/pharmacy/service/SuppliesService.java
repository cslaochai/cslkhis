package com.his.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.pharmacy.dto.ConsumableUpsertDTO;
import com.his.pharmacy.entity.BizConsumableStock;
import com.his.pharmacy.vo.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 物资耗材服务接口
 */
public interface SuppliesService extends IService<BizConsumableStock> {

    /**
     * 耗材字典分页
     */
    PageResult<SysConsumableVO> selectConsumablePage(String keyword, Integer category, Integer status,
                                                     int pageNum, int pageSize);

    /**
     * 耗材字典单条
     */
    SysConsumableVO getConsumableById(Long id);

    /**
     * 耗材字典新增/修改（id 空=新增，编码唯一）
     */
    boolean consumableUpsert(ConsumableUpsertDTO dto);

    /**
     * 启用耗材下拉
     */
    List<ConsumableSelectListVO> selectEnabledConsumables();

    /**
     * 库存分页（JOIN 字典）
     */
    PageResult<BizConsumableStockVO> selectStockPage(String keyword, Integer category, Integer stockStatus,
                                                     int pageNum, int pageSize);

    /**
     * 库存详情（JOIN 字典）
     */
    BizConsumableStockVO getStockDetailById(Long stockId);

    /**
     * 建批入库（同耗材同批号已存在则拒绝，走补货入库）
     */
    boolean addStock(Long consumableId, String batchNo, java.time.LocalDate productionDate,
                     java.time.LocalDate expiryDate, BigDecimal quantity, BigDecimal costPrice,
                     String location, String supplier, String operatorName);

    /**
     * 补货入库
     */
    boolean inboundStock(Long stockId, BigDecimal quantity, String operatorName);

    /**
     * 其他出库（手工出库，落流水）
     */
    boolean outboundStock(Long stockId, BigDecimal quantity, String operatorName);

    /**
     * 科室领用：FEFO 扣库存 + 流水（type=2）+ 台账
     */
    boolean consume(Long consumableId, BigDecimal quantity, Long deptId, String purpose, String operatorName);

    /**
     * 领用台账分页
     */
    PageResult<BizConsumableConsumeVO> selectConsumePage(String keyword, Long deptId, int pageNum, int pageSize);

    /**
     * 出入库流水分页
     */
    PageResult<BizConsumableStockLogVO> selectStockLogPage(String keyword, Integer changeType,
                                                           int pageNum, int pageSize);
}
