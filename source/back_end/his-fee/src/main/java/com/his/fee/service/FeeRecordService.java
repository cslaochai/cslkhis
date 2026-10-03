package com.his.fee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.fee.dto.FeeBookDTO;
import com.his.fee.dto.FeeRecordQueryPageDTO;
import com.his.fee.dto.FeeReverseDTO;
import com.his.fee.entity.BizFeeRecord;
import com.his.fee.vo.BizFeeRecordDetailVO;
import com.his.fee.vo.BizFeeRecordVO;
import com.his.fee.vo.FeeTypeSumVO;
import com.his.common.base.PageResult;

import java.math.BigDecimal;
import java.util.List;

/**
 * 费用记账服务（L1）：应收的唯一来源。
 *
 * <p><b>本层铁律：记账行一经写入，金额列不再 UPDATE。</b>
 * 记错了不改成，而是"红冲"——写一条负数行把原行冲掉（两行用 {@code orig_fee_id} 互指）。
 * 之所以不许就地改数：应收是医保核查、退费追溯、科室收入统计的共同依据，
 * 就地 update 等于销毁"这笔费用历史上是多少"，事后谁也拿不出证据。
 *
 * <p>部分冲减只写负行、不改原行，净额靠 SUM 现算，不靠某一行的镜像列。
 */
public interface FeeRecordService extends IService<BizFeeRecord> {

    /**
     * 记账（幂等）：同一来源单据 + 同一项目已记过有效费用时返回既有行，不重复记。
     *
     * <p>幂等是必需的：医嘱执行、发药、打卡这些动作都可能被重复触发，
     * 少一道判重就是多收一笔钱，而且没有任何地方会报错。
     */
    BizFeeRecord book(FeeBookDTO dto);

    /**
     * 手工补记账的 HTTP 出参：与 {@link #book} 同一事务、同一条链，只把出参换成 VO。
     */
    BizFeeRecordVO bookVO(FeeBookDTO dto);

    /**
     * 批量记账（一次开单多个项目）。整批同事务：要么全记上，要么一条都不记。
     */
    List<BizFeeRecord> bookBatch(List<FeeBookDTO> list);

    /**
     * 整行红冲：写一条等额负行，并把原行与本行一起置 4-已红冲（两条都不再参与应收净额）。
     * 只有「1-待结算」的行可以直接红冲；已进账单的要先取消结算，已结算的走退费申请。
     */
    BizFeeRecord reverse(Long feeId, String reason);

    /**
     * 部分红冲：只写一条负行（数量=本次冲减、金额取负），原行状态不动，应收净额随之减少。
     * 冲减到零时等价于整行红冲（原行与负行一起置 4）。
     *
     * <p>不做"红冲原行 + 新记剩余行"那种拆行：拆出来的剩余行既不是临床上发生的费用，
     * 也会让幂等判重把后续执行当成重复记账，净额还容易在四舍五入上漂几分钱。
     *
     * @param quantity 本次冲减的数量（正数，不得超过本行剩余可冲额）
     * @return 本次写出的负行
     */
    BizFeeRecord reversePartial(Long feeId, BigDecimal quantity, String reason);

    /**
     * 红冲的 HTTP 出参：数量为空走整行冲、给了数量走部分冲减，这个分支只在这里写一次。
     */
    BizFeeRecordVO reverseVO(FeeReverseDTO dto);

    /**
     * 退费流程专用的红冲：允许冲掉「3-已结算」的行（钱已收、现在退给对方，应收当然跟着减）。
     *
     * <p>单独开一个方法而不是放宽 {@link #reverse} 的门禁：红冲的 HTTP 口子谁都能调，
     * 若它也允许冲已结算行，就会出现"账上冲了费用、钱却没退"的挂账。
     * <b>本方法必须与退款流水在同一事务内调用</b>，调用方只有 {@code PaymentService}。
     */
    BizFeeRecord reverseForRefund(Long feeId, String reason);

    /**
     * 某次就诊未结算的记账行（结算台选行用）
     */
    List<BizFeeRecord> listPending(Integer encounterType, Long encounterId);

    /**
     * 某次就诊未结算应收合计（含红冲负行，净额才是真应收）
     */
    BigDecimal sumPendingAmount(Integer encounterType, Long encounterId);

    /**
     * 某次就诊按项目类型聚合的应收净额（不问结算状态）：
     * 引导单与科室报表要的是"这次就诊发生了多少钱、花在哪"。
     */
    List<FeeTypeSumVO> sumNetGroupByItemType(Integer encounterType, Long encounterId);

    /**
     * 某次就诊发生过的记账行明细（含红冲负行，不问结算状态）：住院日清单的事实来源。
     */
    List<BizFeeRecord> listNetByEncounter(Integer encounterType, Long encounterId);

    /**
     * 某次就诊的应收净额（同上口径，只要合计）：住院账务概览与欠费榜的"已发生费用"。
     */
    BigDecimal sumNetAmount(Integer encounterType, Long encounterId);

    /**
     * 结算层回写：锁定（进账单）/ 结清 / 解锁（账单作废）。
     *
     * <p>写权收在本服务，结算层只能通过这里改记账行状态 —— 否则"谁能把待结算改成已锁定"
     * 就没有唯一答案，锁与解锁会各写各的。
     */
    void lockToBill(List<Long> feeIds, Long billId);

    /**
     * 账单付清后把本账单锁定的行提为「3-已结算」：只有钱收齐了才算结算完成。
     *
     * <p>按账单ID 整批提而不是传 ID 清单，是因为调用方（支付层）手上只有账单；
     * 已红冲的行不在匹配范围内，本来就不该跟着"结清"。
     *
     * @return 实际提上来的行数
     */
    int markSettledByBill(Long billId);

    /**
     * 账单作废/整单撤销时把行放回待结算（不动资金，只是解锁）。
     *
     * <p>已红冲的行会被跳过而不是报错：撤销一张付过款的账单时，钱退回去的同时那些行就已经
     * 冲掉了（状态 4），这里再走一遍只是"没有需要解锁的行"。
     *
     * @return 实际解锁的行数
     */
    int releaseFromBill(List<Long> feeIds);

    /** 本账单名下的记账行（含红冲负行），撤销账单时按这张清单逐行冲减 */
    List<BizFeeRecord> listByBill(Long billId);

    /**
     * 按来源单据找回记账行：临床侧"退药/作废要冲哪一笔账"的入口。
     *
     * <p>只返回<b>有效正数行</b>（跳过红冲负行与已全额红冲的行），因为冲账的对象是当初记下的那笔应收。
     * 一张来源单记了多个项目时靠 {@code itemCode} 定位；传 {@code null} 表示"这张单子只有一行"。
     *
     * @return 找不到返回 {@code null}（来源单没记过账 —— 调用方自己决定是报错还是留痕）
     */
    BizFeeRecord findBookedBySource(Integer sourceType, Long sourceId, String itemCode);

    PageResult<BizFeeRecordVO> selectPage(FeeRecordQueryPageDTO query);

    BizFeeRecordDetailVO getDetailById(Long id);
}
