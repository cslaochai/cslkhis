package com.his.inpatient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.inpatient.dto.*;
import com.his.inpatient.vo.InpatientOrderExecVO;
import com.his.inpatient.vo.InpatientOrderVO;

/**
 * 住院医嘱服务。
 */
public interface InpatientOrderService {

    /**
     * 开立 / 修改医嘱（一次提交 = 一个组套）
     *
     * @param dto  医嘱入参
     * @param pin 签名人本次输入的 PIN（软签二次确认，明文不入库）
     * @return 本次的组套号（orderGroup）
     */
    String save(InpatientOrderUpsertDTO dto, String pin);

    /**
     * 护士医嘱校对（批量）：校对通过才生成执行计划、才进执行队列
     *
     * @param dto  校对入参
     * @param pin 签名人本次输入的 PIN（软签二次确认，明文不入库）
     * @return 实际校对成功的条数
     */
    int verify(InpatientOrderVerifyDTO dto, String pin);

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
    IPage<InpatientOrderVO> listPage(InpatientOrderPageQueryDTO query);

    /**
     * 护士待执行队列（按 plan_time 升序；查询时会补当天的长期医嘱计划）
     */
    IPage<InpatientOrderExecVO> execPendingList(OrderExecPageQueryDTO query);

    /**
     * 医嘱执行（批量）：已执行要计费，已跳过不计费但必须写原因
     *
     * @return 实际处理的条数
     */
    int execComplete(OrderExecCompleteDTO dto);

    /**
     * 执行记录查询（含已执行 / 已跳过）
     */
    IPage<InpatientOrderExecVO> execList(OrderExecPageQueryDTO query);

    /**
     * 待校对医嘱数（护士站卡片）
     */
    long countPendingVerify(Long admissionId);

    /**
     * 待执行医嘱数（护士站卡片）
     */
    long countPendingExec(Long admissionId);
}
