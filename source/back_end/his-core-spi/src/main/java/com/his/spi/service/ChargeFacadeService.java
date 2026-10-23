package com.his.spi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.spi.dto.BillPayDTO;
import com.his.spi.dto.FeeBookDTO;
import com.his.spi.dto.PrepayPageQueryDTO;
import com.his.spi.dto.PrepayUpsertDTO;
import com.his.spi.vo.BillBriefVO;
import com.his.spi.vo.BillItemVO;
import com.his.spi.vo.BizSettlementBillVO;
import com.his.spi.vo.ArrearsControlCheckResultVO;
import com.his.spi.vo.CancelCommandVO;
import com.his.spi.vo.FeeRecordVO;
import com.his.spi.vo.FeeTypeSumVO;
import com.his.spi.vo.InsuranceBriefVO;
import com.his.spi.vo.PendingBillVO;
import com.his.spi.vo.PrepayBalanceVO;
import com.his.spi.vo.PrepayVO;
import com.his.spi.vo.RegistChargeCommandVO;
import com.his.spi.vo.SettlementStateVO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 收费域对外提供的端口（依赖倒置，实现落在 his-charge）：
 * 记账/红冲、账单查询与收款、待缴账单、预交金、挂号费结算/退号联动、住院结算状态。
 *
 * <p>事务口径：除特别注明外，本端口方法<b>加入调用方事务</b>；
 * 需要"记账失败不拖垮主流程"的调用方自己用 {@code REQUIRES_NEW} 包一层。
 */
public interface ChargeFacadeService {

    // ---------- L1 记账（应收唯一来源） ----------

    /**
     * 记一笔费用（幂等）：同一来源单据 + 同一项目已记过有效费用时返回既有行，不重复记。
     *
     * @return 记账行投影；不存在"半失败"状态
     */
    FeeRecordVO bookFee(FeeBookDTO dto);

    /**
     * 批量记账（一次开单多个项目）。整批同事务：要么全记上，要么一条都不记。
     */
    List<FeeRecordVO> bookBatch(List<FeeBookDTO> list);

    /**
     * 按来源单据找回记账行：临床侧"退药/作废要冲哪一笔账"的入口。
     *
     * @return 找不到返回 {@code null}（来源单没记过账 —— 调用方自己决定是报错还是留痕）
     */
    FeeRecordVO findBookedBySource(Integer sourceType, Long sourceId, String itemCode);

    /**
     * 整行红冲：写一条等额负行，并把原行与本行一起置 4-已红冲。
     *
     * @return 本次写出的负行
     */
    FeeRecordVO reverse(Long feeId, String reason);

    /**
     * 某次就诊按项目类型聚合的应收净额（不问结算状态）：
     * 引导单与科室报表要的是"这次就诊发生了多少钱、花在哪"。
     */
    List<FeeTypeSumVO> sumNetGroupByItemType(Integer encounterType, Long encounterId);

    // ---------- 账单与收款 ----------

    /**
     * 批量查询账单简要信息（挂号看板装配缴费状态用，禁止在循环里单条查）。
     *
     * @param billIds 账单ID集合
     * @return billId -> 简要信息
     */
    Map<Long, BillBriefVO> mapBillsByIds(Collection<Long> billIds);

    /**
     * 查询单张账单简要信息
     *
     * @return 不存在时返回 null
     */
    BillBriefVO getBill(Long billId);

    /**
     * 账单剩余应缴（应缴 − 已缴，两位小数）。
     *
     * @return 账单不存在时返回 {@code null}；无应缴返回 0
     */
    BigDecimal unpaidAmount(Long billId);

    /**
     * 账单收款（与院内收费台同一入口）：金额以账单剩余应缴为准的校验在实现内。
     */
    void payBill(BillPayDTO dto);

    /**
     * 患者某人的待缴账单（账单头 + 摊行明细）。
     */
    List<PendingBillVO> pendingBills(Long patientId);

    /**
     * 某次就诊的待缴账单（未结清的账单：1-待支付 / 2-部分支付 / 6-挂账欠费）。
     * 用于结诊文书（引导单/处方签）出聚合支付码：患者扫码付本次就诊待缴费用。
     *
     * @return 无账单或全部结清时返回空列表
     */
    List<BillBriefVO> pendingBillsOfEncounter(Integer encounterType, Long encounterId);

    // ---------- 账单读侧（费用解释 / 报表） ----------

    /**
     * 账单完整详情（台账出参口径）。
     *
     * @return 不存在时返回 {@code null}
     */
    BizSettlementBillVO getBillDetail(Long billId);

    /**
     * 账单明细行（费用解释等场景按目录类别/项目类型自行聚合）。
     */
    List<BillItemVO> listBillItems(Long billId);

    /**
     * 该账单最近一次医保结算简要。
     *
     * @return 未走医保结算时返回 {@code null}
     */
    InsuranceBriefVO latestInsurance(Long billId);

    /**
     * 账务归属日区间内、已支付状态的结算账单（四层口径报表用）。
     *
     * @param startDate 账务归属日起（yyyy-MM-dd，可空）；endDate 账务归属日止（可空）
     */
    List<BizSettlementBillVO> paidBills(String startDate, String endDate);

    // ---------- 住院预交金 ----------

    /**
     * 预交金充值/退款（患者端自助充值与柜面同入口、同一套流水）。
     */
    void savePrepay(PrepayUpsertDTO dto);

    /**
     * 住院预交金余额（充值/退款合计与现算余额）。
     */
    PrepayBalanceVO prepayBalance(Long admissionId);

    /**
     * 预交金流水分页。
     */
    IPage<PrepayVO> prepayPage(PrepayPageQueryDTO query);

    // ---------- 挂号费联动 ----------

    /**
     * 挂号费结算：记账（挂号费 + 诊查费）并立即出账。
     *
     * @return 已落库的账单简要信息；<b>免收时返回 null</b>（没有应收，也就没有账单）
     */
    BillBriefVO createRegistCharge(RegistChargeCommandVO command);

    /**
     * 挂号作废时联动处理账单 —— <b>退号不退钱是本系统里最容易漏掉的一环</b>。
     *
     * <p>口径：整单撤销 = 收进来的钱按原收款流水逐路退回 + 本账单的记账行全额红冲 + 账单作废，
     * 三件事必须在<b>同一个事务</b>里。只置挂号状态不处理钱，会留下「已退号 + 账单仍是已支付」
     * 的对账缺口；反过来只退钱不冲应收，收费台上就永远挂着这张收不到的账。
     *
     * @return true=本次确实撤销；false=没有账单或账单早已关闭（幂等跳过）
     */
    boolean cancelRegistCharge(CancelCommandVO command);

    // ---------- 挂号试算（收费预估） ----------

    /**
     * 挂号试算按结算方式 + 医保类型取统筹报销比例（收费预估用，原收费域 InsurancePolicyService 能力）。
     *
     * <p>口径与收费域一致：自费/未配置政策返回 0（全额自付）；命中启用政策返回其统筹比例（如 85.00 表示 85%）。
     *
     * @param settlementType      结算方式（政策档案结算类型：2-城镇职工医保 3-城乡居民医保 4-公费医疗；1/空=自费无政策）
     * @param medicalInsuranceType 医保类型编码（费别键，对应医保类型枚举的结算类型）
     * @return 统筹报销比例；无配置时返回 {@code 0}
     */
    BigDecimal getCoverageRatio(Integer settlementType, Integer medicalInsuranceType);

    // ---------- 住院结算状态 ----------

    /**
     * 查询某次住院的结算状态。
     *
     * @param admissionId 入院ID
     * @return 结算状态；没有任何结算单时返回 {@code settled=false} 的状态对象
     */
    SettlementStateVO state(Long admissionId);

    // ---------- 住院医嘱开立欠费管控（G20） ----------

    /**
     * 新开医嘱前的欠费管控闸门：策略启用且欠费达停费线时拦截新增的择期类医嘱。
     *
     * <p>药品/手术/急救类永不拦截（急救被欠费卡住是医疗事故）；修改/停嘱/取消不拦。
     * 调用方仅对「新增」医嘱调用。
     *
     * @param admissionId  入院ID
     * @param orderClasses 本次医嘱涉及的医嘱类别集合（1-药品 2-检查 3-检验 4-治疗 5-护理
     *                     6-手术 7-输血 8-监护 9-其他 10-临床营养）
     * @return null=策略未启用或无欠费拦截（放行）；非 null 且 {@code allowed=false} 表示被拦截，
     *         {@code reason} 可直接展示给医生
     */
    ArrearsControlCheckResultVO checkNewOrderArrears(Long admissionId, Collection<Integer> orderClasses);
}
