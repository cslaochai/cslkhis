package com.his.emr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.emr.entity.BizDrugDispensing;
import com.his.emr.vo.BizDrugDispensingVO;
import com.his.emr.vo.DrugDispensingCountVO;

/**
 * 药品发药服务接口
 * 闭环口径：发药 = 审方闸门校验 + **麻精限量闸门** + **麻精双人复核** + FEFO 扣库存（落流水）
 * + 明细状态 + 处方状态联动 + **麻精自动写专册**；
 * 退药 = 回库存（落流水）+ 明细置 3 + 处方置 6。
 *
 * <p>麻精相关的三道闸门由 {@link NarcoticControlService} 提供，顺序是：
 * ① 限量（按处方整体判一次，超限整单不发）→ ② 双人复核（按药品判）→ ③ 发药成功后写专册。
 * 顺序不能换：先扣库存再校验，校验失败要回滚库存，白白制造一次流水。
 */
public interface DrugDispensingService extends IService<BizDrugDispensing> {

    /**
     * 发药明细分页（患者姓名/处方号模糊 + 状态过滤）。
     *
     * <p>出参带 {@code specialFlag}（管制分类，由药品主数据补齐）——
     * 发药窗口据此在**点发药之前**提示"这行要不要选复核药师"。
     */
    PageResult<BizDrugDispensingVO> selectDispensingPage(Long patientId, String patientName, String prescriptionNo,
                                                         Integer dispensingStatus, int pageNum, int pageSize);

    /**
     * 单行发药（一行 = 一个药品 × 一张处方）
     *
     * @param checkerId       复核药师ID（麻精需要双人复核时必填；姓名由服务端反查）
     * @param overLimitReason 二类精神药品超 7 日的理由（其余情形不生效）
     */
    boolean dispense(Long dispensingId, Long pharmacistId, String pharmacistName,
                     Long checkerId, String overLimitReason);

    /**
     * 按处方整单发药：该处方全部待发明细一次发完，任一药品库存不足整单回滚
     */
    boolean dispenseByPrescription(Long prescriptionId, Long pharmacistId, String pharmacistName,
                                   Long checkerId, String overLimitReason);

    /**
     * 退药（回库存 + 处方状态联动）
     */
    boolean returnDrug(Long dispensingId, String reason);

    /**
     * 获取发药详情（同样带 {@code specialFlag}）
     */
    BizDrugDispensingVO getDispensingDetail(Long dispensingId);

    /**
     * 三态计数（待发药/已发药/已退药，全库口径）
     */
    DrugDispensingCountVO getStatusCount();
}
