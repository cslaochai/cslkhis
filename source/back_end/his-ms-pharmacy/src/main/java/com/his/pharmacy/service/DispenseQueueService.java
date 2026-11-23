package com.his.pharmacy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.pharmacy.dto.DispenseQueuePageQueryDTO;
import com.his.pharmacy.entity.BizDispenseQueue;
import com.his.pharmacy.vo.DispenseQueueVO;

import java.util.List;

/**
 * 门诊取药队列服务接口
 *
 * <p>数据链路：处方缴费 → 生成 {@code biz_drug_dispensing} 待发药行（由收费/处方模块调用
 * {@code DispensingFacadeService}）→ **患者到药房刷证签到**才在本表落一行并取号 →
 * 窗口叫号（把号绑到窗口）→ 药师扫处方发药 → 整单发完回写状态。
 *
 * <p>「患者到场」是这一层存在的唯一理由：缴费只代表钱交了，不代表人来了。
 * 没有这一层，药房既不知道队伍有多长，也不知道窗口该叫谁。
 */
public interface DispenseQueueService extends IService<BizDispenseQueue> {

    /**
     * 处方签到取号（同一处方当天幂等：已签到的直接返回原号，不重复发号）
     */
    DispenseQueueVO signIn(Long prescriptionId, Integer signInType);

    /**
     * 按处方号签到取号（药房窗口扫处方单，手里只有号没有 ID）
     */
    DispenseQueueVO signInByPrescriptionNo(String prescriptionNo, Integer signInType);

    /**
     * 按患者签到：该患者名下所有待发药处方各取一个号
     *
     * <p>刷身份证/就诊卡时前端只知道患者，一个患者可能同时有多张已缴费处方。
     * 每张处方独立取号，因为处方才是取药单元（一个号对应一袋药）。
     */
    List<DispenseQueueVO> signInByPatient(Long patientId, Integer signInType);

    /**
     * 该患者名下所有待发药处方（刷证后先列出来，让药师确认取哪几张）
     */
    List<DispenseQueueVO> listWaitingByPatient(Long patientId);

    /**
     * 该处方当天的取药号（没签到返回 null）
     */
    DispenseQueueVO getByPrescription(Long prescriptionId);

    /**
     * 队列分页
     */
    PageResult<DispenseQueueVO> selectQueuePage(DispenseQueuePageQueryDTO queryDTO);

    /**
     * 窗口叫下一位：取最早签到、尚未分配到窗口的待取药号，绑到该窗口并计一次叫号
     */
    DispenseQueueVO callNext(Long windowId);

    /**
     * 重叫：把该窗口当前正在叫的号再叫一次（患者没听见）
     */
    DispenseQueueVO recall(Long windowId);

    /**
     * 该窗口当前正在叫的号（叫号屏轮询）
     */
    DispenseQueueVO currentCall(Long windowId);

    /**
     * 过号
     */
    boolean skip(Long queueId, String reason);

    /**
     * 取消取药号（处方作废/患者不取了）
     */
    boolean cancel(Long queueId, String reason);

    /**
     * 过号后恢复排队：号不变，按当前时间重新排到队尾，清掉原来占的窗口
     */
    boolean revive(Long queueId);

    /**
     * 发药回写：刷新已发药条数；整单发完置「已发药」并记完成时间
     *
     * <p>找不到队列行时静默返回 —— 患者没签到、药师直接在发药列表里发药是合法路径，
     * 不能因为没取号就阻断发药。
     */
    void syncOnDispensed(Long prescriptionId);
}
