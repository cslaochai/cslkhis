package com.his.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.InpatientOrderCancelDTO;
import com.his.patient.dto.InpatientOrderQueryPageDTO;
import com.his.patient.dto.InpatientOrderStopDTO;
import com.his.patient.dto.InpatientOrderUpsertDTO;
import com.his.patient.dto.InpatientOrderVerifyDTO;
import com.his.patient.dto.OrderExecCompleteDTO;
import com.his.patient.dto.OrderExecQueryPageDTO;
import com.his.patient.vo.InpatientOrderExecVO;
import com.his.patient.vo.InpatientOrderVO;

/**
 * 住院医嘱服务。
 *
 * <p>状态机（唯一合法路径，任何其它路径组合都必须被拒绝并说清原因）：
 * <pre>
 *   开立 ──→ 1 待校对 ──校对──→ 2 已校对 ──执行──→ 3 执行中（长期）/ 4 已完成（临时）
 *              │                  │
 *           作废│               停止│
 *              ↓                  ↓
 *            6 已作废            5 已停止
 * </pre>
 *
 * <p>三条不可破的业务铁律：
 * <ol>
 *   <li><b>未校对不可执行</b>：未校对(1)的医嘱不进护士执行队列 —— 医嘱双人核对的最低要求。</li>
 *   <li><b>长期医嘱只能停、不能作废</b>：已执行的次数是既成事实，停止只影响后续。</li>
 *   <li><b>同一组套同起同停</b>：停止时按 `orderGroup` 整组停，不允许"一组药停一半"。</li>
 * </ol>
 */
public interface InpatientOrderService {

    /**
     * 开立 / 修改医嘱（一次提交 = 一个组套）
     *
     * @return 本次的组套号（orderGroup）
     */
    String save(InpatientOrderUpsertDTO dto);

    /**
     * 护士医嘱校对（批量）：校对通过才生成执行计划、才进执行队列
     *
     * @return 实际校对成功的条数
     */
    int verify(InpatientOrderVerifyDTO dto);

    /**
     * 停止长期医嘱（同组套整组停）
     *
     * @return 实际被停止的医嘱条数
     */
    int stop(InpatientOrderStopDTO dto);

    /**
     * 批量停止某次住院的长期医嘱（转科时调用）。
     *
     * <p>只停「已校对 / 执行中」的长期医嘱；「待校对」的按六条铁律只能由原科室医生作废，
     * 因此**跳过并留给调用方回报**，绝不代劳、也绝不静默。
     *
     * <p>刻意不抛异常：转科是一个必须完成的动作，不能被某一条医嘱的状态卡住。
     * 跳过谁、为什么，由调用方查一次剩余医嘱后写进转科单的医嘱处置说明。
     *
     * @return 受影响的医嘱条数（含同组套内按作废处理的成员）
     */
    int stopLongOrders(Long admissionId, String reason);

    /**
     * 作废医嘱（仅「待校对」可作废）
     */
    void cancel(InpatientOrderCancelDTO dto);

    /**
     * 医嘱分页（医生站 / 护士站共用）
     */
    IPage<InpatientOrderVO> listPage(InpatientOrderQueryPageDTO query);

    /**
     * 护士待执行队列（按 plan_time 升序；查询时会补当天的长期医嘱计划）
     */
    IPage<InpatientOrderExecVO> execPendingList(OrderExecQueryPageDTO query);

    /**
     * 医嘱执行（批量）：已执行要计费，已跳过不计费但必须写原因
     *
     * @return 实际处理的条数
     */
    int execComplete(OrderExecCompleteDTO dto);

    /**
     * 执行记录查询（含已执行 / 已跳过）
     */
    IPage<InpatientOrderExecVO> execList(OrderExecQueryPageDTO query);

    /**
     * 待校对医嘱数（护士站卡片）
     */
    long countPendingVerify(Long admissionId);

    /**
     * 待执行医嘱数（护士站卡片）
     */
    long countPendingExec(Long admissionId);
}
