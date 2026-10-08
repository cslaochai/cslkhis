package com.his.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.pharmacy.dto.StockBatchMoveDTO;
import com.his.pharmacy.dto.StockDeductResultDTO;
import com.his.pharmacy.dto.BizDrugStockUpsertDTO;
import com.his.pharmacy.entity.BizDrugStock;
import com.his.pharmacy.vo.BizDrugStockLogVO;
import com.his.pharmacy.vo.BizDrugStockVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 药房服务接口
 */
public interface PharmacyService extends IService<BizDrugStock> {

    /**
     * 查询库存列表（联表药品字典带出药品信息，drugName 模糊药名/编码）
     *
     * @param stockRoom 库位过滤（NULL=两层都看；1-药库 2-药房，见 StockRoomEnum）
     */
    PageResult<BizDrugStockVO> selectStockPage(String drugName, Integer stockStatus, Integer stockRoom,
                                               int pageNum, int pageSize);

    /**
     * 批次候选（调拨/退货建单选批次用）：只出该库位可用量 &gt; 0 的批次
     *
     * @param onlyWithSupplier true=只出已挂供应商档案的批次（供应商退货必须知道退给谁）
     */
    List<BizDrugStockVO> selectBatchCandidates(Integer stockRoom, String keyword, boolean onlyWithSupplier);

    /**
     * 查询库存预警列表（联表药品字典带出药品信息）
     */
    List<BizDrugStockVO> selectStockWarningList(Integer stockStatus);

    /**
     * 获取库存详情（联表药品字典带出药品信息）
     */
    BizDrugStockVO getStockDetailById(Long stockId);

    /**
     * 新增库存（建批次，字典校验 + 同批号防重）
     */
    boolean addStock(BizDrugStockUpsertDTO upsertDTO);

    /**
     * 入库
     */
    boolean inboundStock(Long stockId, BigDecimal quantity);

    /**
     * 出库
     */
    boolean outboundStock(Long stockId, BigDecimal quantity);

    /**
     * 开方锁库：按药品跨批次（FEFO 顺序）把可用量转成锁定量。
     *
     * <p>不能按单批次锁：发药是 FEFO **跨批次**扣的，锁在批次 A、扣在批次 B 等于没锁，
     * 而且批次 A 可用为 0 时会把整张病历保存打死（开不出方，但架上的药其实是够的）。
     * 该药品一个批次都没有时直接放过 —— 建没建批不是医生的锅，发药那一步才是硬闸门。
     */
    void lockStockByDrug(Long drugId, BigDecimal quantity);

    /**
     * 释放锁定（撤销处方/病历）：按药品跨批次把锁定量退回可用量，只退实际锁着的部分
     */
    void unlockStockByDrug(Long drugId, BigDecimal quantity);

    /**
     * 根据药品ID查询库存
     */
    BizDrugStock getStockByDrugId(Long drugId);

    /**
     * FEFO 扣减库存（发药出库）：先过期先出，跨批次扣减，逐批落流水。
     * 总可用量不足时抛 BusinessException，整单回滚。
     *
     * @return 该药品全部批次合计的前后数量（写回发药记录的配药前/配药后库存）
     */
    StockDeductResultDTO deductStockFefo(Long drugId, BigDecimal quantity, String sourceType,
                                      Long sourceId, String sourceNo, String operatorName);

    /**
     * 退药回库：并入该药品现有数量最大的批次并落流水（type=3 退药回库）。
     * 若该药品已无任何批次，则以来源单号建新批次。
     */
    void restoreStock(Long drugId, BigDecimal quantity, String sourceType,
                      Long sourceId, String sourceNo, String operatorName);

    /**
     * 按「药品+批号」入库（采购入库专用）
     *
     * <p>命中既有批次 → 加量：成本按**实际进货金额累加**（入库总额 += 本次数量 × 本次进价），
     * 成本单价回算为加权均价 —— 同批号两次进价不同时，库存总额仍然是真实的累计投入，
     * 不会像"沿用旧成本价重算"那样把差额抹掉。
     * <br>未命中 → 按批号新建批次（有效期必填，缺了建不了批次）。
     *
     * <p>库存流水带来源单（sourceType/sourceId/sourceNo），可反查"这批药是哪张采购单进来的"。
     */
    BizDrugStock inboundByBatch(BizDrugStock batchInfo, BigDecimal quantity,
                                String sourceType, Long sourceId, String sourceNo, String operatorName);

    /**
     * 库存流水分页（联表药品字典带出药名）
     */
    PageResult<BizDrugStockLogVO> selectStockLogPage(String drugName, Integer changeType,
                                                     int pageNum, int pageSize);

    /**
     * 盘点差异过账（sql/127）：把批次余额按<b>差量</b>调整并落流水。
     *
     * <p>new = 当前数量 + 差异数（<b>不是</b>覆盖成实盘数）：快照之后发生的发药/入库
     * 都有自己的流水行，覆盖会把它们抹掉、凭空造出药来。
     * <br>available = new - 当前锁定量；new 为负或小于锁定量（已开方未发药）时抛异常整单回滚，
     * 宁可让药师回去重点差异，也不能把可用库存打成负数。
     *
     * @param diffQuantity 差异数量（实盘 - 账面快照；正=盘盈 type 5，负=盘亏 type 6，0 由调用方挡掉）
     */
    void postStocktakeDiff(Long stockId, BigDecimal diffQuantity,
                           Long stocktakeId, String stocktakeNo,
                           String operatorName, String reason);

    /**
     * 按<b>指定批次</b>精确扣减（调拨发出 type=7 / 供应商退货 type=9，sql/154）
     *
     * <p>与 {@link #deductStockFefo} 的区别是这里必须由调用方点定批次：调拨与退货都是
     * "搬这一批货"，FEFO 顺扣会把别的批次的药也算进去，账面就会出现"没动过的批次少了几盒"。
     * <br>闸门三条，任一不过整单回滚：批次不存在 / 批次不在 expectStockRoom（防止把药库的货
     * 当成药房的搬走）/ 可用量不足（锁定量是已开方答应给患者的药，不许调拨也不许退货）。
     *
     * @return 扣减后的批次（调用方要拿 before/after 写单据明细）
     */
    BizDrugStock deductStockBatch(StockBatchMoveDTO move);

    /**
     * 把数量落到某库位的同批号批次上（调拨接收 type=8，sql/154）
     *
     * <p>目标库位已有同批号批次 → 直接加量（成本价不变：同一批货的成本不因搬运而变）；
     * 没有 → 按快照建新批（效期/批号/成本/供应商全部带走，否则新批次无从追溯）。
     * <br>⚠ 不写"接收方成本加权平均"：入库那套 {@code inboundByBatch} 是两次真实进货合并计价，
     * 调拨不是一笔新进货，加权重算会把药库的成本与药房的成本互相洗掉。
     */
    BizDrugStock addStockToRoom(StockBatchMoveDTO move);
}
