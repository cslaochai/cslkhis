package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.DispenseQueueStatusEnum;
import com.his.common.enums.DispenseSignInTypeEnum;
import com.his.common.enums.DispensingStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.common.utils.UserUtils;
import com.his.pharmacy.dto.DispenseQueuePageQueryDTO;
import com.his.pharmacy.entity.BizDispenseQueue;
import com.his.pharmacy.entity.BizDrugDispensing;
import com.his.pharmacy.mapper.BizDispenseQueueMapper;
import com.his.pharmacy.mapper.BizDrugDispensingMapper;
import com.his.pharmacy.service.DispenseQueueService;
import com.his.pharmacy.vo.DispenseQueueVO;
import com.his.pharmacy.vo.DispenseWaitingRxVO;
import com.his.spi.service.RedisSequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 门诊取药队列服务实现
 */
@Service
@RequiredArgsConstructor
public class DispenseQueueServiceImpl extends ServiceImpl<BizDispenseQueueMapper, BizDispenseQueue>
        implements DispenseQueueService {

    /**
     * 取药号 Redis 计数器模块名（当天归零，与 queue_date 同一天口径）
     */
    private static final String QUEUE_NO_KEY = "DISPENSE_QUEUE";

    /**
     * 并发叫号的重取次数：多个窗口同时点「下一位」时，抢输的那个重取一次即可
     */
    private static final int CALL_RETRY = 3;

    private final RedisSequenceService redisSequenceService;
    private final BizDrugDispensingMapper dispensingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DispenseQueueVO signIn(Long prescriptionId, Integer signInType) {
        if (prescriptionId == null) {
            throw new BusinessException("处方ID不能为空");
        }
        LocalDate today = LocalDate.now();
        BizDispenseQueue exists = baseMapper.selectByPrescriptionAndDate(prescriptionId, today);
        if (exists != null) {
            return reuseExisting(exists);
        }
        return doSignIn(prescriptionId, signInType, today);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DispenseQueueVO signInByPrescriptionNo(String prescriptionNo, Integer signInType) {
        if (!TextUtil.hasText(prescriptionNo)) {
            throw new BusinessException("处方号不能为空");
        }
        List<BizDrugDispensing> rows = dispensingMapper.selectList(
                new LambdaQueryWrapper<BizDrugDispensing>()
                        .eq(BizDrugDispensing::getPrescriptionNo, prescriptionNo.trim())
                        .eq(BizDrugDispensing::getDispensingStatus, DispensingStatusEnum.PENDING.getCode())
                        .eq(BizDrugDispensing::getDelFlag, 0)
                        .orderByAsc(BizDrugDispensing::getId));
        if (rows.isEmpty()) {
            throw new BusinessException("未找到待取药处方：" + prescriptionNo);
        }
        return signIn(rows.get(0).getPrescriptionId(), signInType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DispenseQueueVO> signInByPatient(Long patientId, Integer signInType) {
        if (patientId == null) {
            throw new BusinessException("患者ID不能为空");
        }
        LocalDate today = LocalDate.now();
        List<DispenseWaitingRxVO> rxList = baseMapper.selectWaitingRxByPatient(patientId);
        if (rxList.isEmpty()) {
            throw new BusinessException("该患者没有待取药的处方（可能未缴费或已发药）");
        }
        List<DispenseQueueVO> result = new ArrayList<>();
        for (DispenseWaitingRxVO rx : rxList) {
            BizDispenseQueue exists = baseMapper.selectByPrescriptionAndDate(rx.getPrescriptionId(), today);
            if (exists != null) {
                // 已签到的处方不再发新号；已过号/已取消的跳过，让药师在队列页单独处理
                if (exists.getQueueStatus() != null
                        && exists.getQueueStatus() == DispenseQueueStatusEnum.WAITING.getCode()) {
                    result.add(toVO(exists));
                }
                continue;
            }
            result.add(doSignIn(rx.getPrescriptionId(), signInType, today));
        }
        if (result.isEmpty()) {
            throw new BusinessException("该患者今天的取药号已全部过号或取消，请到人工窗口处理");
        }
        return result;
    }

    @Override
    public List<DispenseQueueVO> listWaitingByPatient(Long patientId) {
        if (patientId == null) {
            throw new BusinessException("患者ID不能为空");
        }
        LocalDate today = LocalDate.now();
        List<DispenseWaitingRxVO> rxList = baseMapper.selectWaitingRxByPatient(patientId);
        List<DispenseQueueVO> result = new ArrayList<>();
        for (DispenseWaitingRxVO rx : rxList) {
            DispenseQueueVO vo = new DispenseQueueVO();
            vo.setPrescriptionId(rx.getPrescriptionId());
            vo.setPrescriptionNo(rx.getPrescriptionNo());
            vo.setPatientId(rx.getPatientId());
            vo.setPatientNo(rx.getPatientNo());
            vo.setPatientName(rx.getPatientName());
            vo.setPrescriptionItemCount(rx.getItemCount());
            vo.setDispensedItemCount(0);
            BizDispenseQueue queue = baseMapper.selectByPrescriptionAndDate(rx.getPrescriptionId(), today);
            if (queue != null) {
                vo.setId(queue.getId());
                vo.setQueueNo(queue.getQueueNo());
                vo.setQueueStatus(queue.getQueueStatus());
                vo.setSignInTime(queue.getSignInTime());
                vo.setSignInType(queue.getSignInType());
                vo.setWindowId(queue.getWindowId());
                vo.setWindowName(queue.getWindowName());
                vo.setCallCount(queue.getCallCount());
                vo.setCallTime(queue.getCallTime());
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public DispenseQueueVO getByPrescription(Long prescriptionId) {
        if (prescriptionId == null) {
            throw new BusinessException("处方ID不能为空");
        }
        return toVO(baseMapper.selectByPrescriptionAndDate(prescriptionId, LocalDate.now()));
    }

    @Override
    public PageResult<DispenseQueueVO> selectQueuePage(DispenseQueuePageQueryDTO queryDTO) {
        LocalDate queueDate = queryDTO.getQueueDate() == null ? LocalDate.now() : queryDTO.getQueueDate();
        Page<BizDispenseQueue> page = baseMapper.selectQueuePage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),
                queueDate, queryDTO.getQueueStatus(), queryDTO.getWindowId(),
                queryDTO.getPatientName(), queryDTO.getPrescriptionNo());
        List<DispenseQueueVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DispenseQueueVO callNext(Long windowId) {
        String windowName = requireActiveWindow(windowId);
        LocalDate today = LocalDate.now();
        for (int i = 0; i < CALL_RETRY; i++) {
            BizDispenseQueue next = baseMapper.selectNextWaiting(today);
            if (next == null) {
                throw new BusinessException("当前没有等待取药的号");
            }
            int updated = baseMapper.bindWindow(next.getId(), windowId, windowName,
                    TimeUtil.nowSeconds(), currentOperator());
            if (updated > 0) {
                // 重新查一次：叫号次数与叫号时间是被 SQL 改的，本地对象还是旧值
                return toVO(this.getById(next.getId()));
            }
        }
        throw new BusinessException("叫号失败，请重试");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DispenseQueueVO recall(Long windowId) {
        requireActiveWindow(windowId);
        BizDispenseQueue current = baseMapper.selectCurrentCall(windowId);
        if (current == null) {
            throw new BusinessException("该窗口当前没有正在叫的号");
        }
        int updated = baseMapper.recall(current.getId(), windowId, TimeUtil.nowSeconds(), currentOperator());
        if (updated == 0) {
            throw new BusinessException("重叫失败，该号已不在本窗口");
        }
        return toVO(this.getById(current.getId()));
    }

    @Override
    public DispenseQueueVO currentCall(Long windowId) {
        if (windowId == null) {
            throw new BusinessException("发药窗口ID不能为空");
        }
        return toVO(baseMapper.selectCurrentCall(windowId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean skip(Long queueId, String reason) {
        return closeWaiting(queueId, DispenseQueueStatusEnum.SKIPPED, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancel(Long queueId, String reason) {
        return closeWaiting(queueId, DispenseQueueStatusEnum.CANCELLED, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revive(Long queueId) {
        BizDispenseQueue queue = this.getById(queueId);
        if (queue == null) {
            throw new BusinessException("取药号不存在");
        }
        if (queue.getQueueStatus() == null
                || queue.getQueueStatus() != DispenseQueueStatusEnum.SKIPPED.getCode()) {
            throw new BusinessException("只有已过号的号才能恢复排队");
        }
        // 号不变（患者手上那张纸还能用），按当前时间重新排到队尾；原窗口占用必须清掉，
        // 否则 selectNextWaiting 的 window_id IS NULL 条件永远取不到这个号。
        // 这里用 lambdaUpdate 显式 set null：updateById 会忽略 null 字段，清不掉窗口。
        return this.lambdaUpdate()
                .eq(BizDispenseQueue::getId, queueId)
                .set(BizDispenseQueue::getQueueStatus, DispenseQueueStatusEnum.WAITING.getCode())
                .set(BizDispenseQueue::getSignInTime, TimeUtil.nowSeconds())
                .set(BizDispenseQueue::getWindowId, null)
                .set(BizDispenseQueue::getWindowName, null)
                .set(BizDispenseQueue::getCallCount, 0)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncOnDispensed(Long prescriptionId) {
        if (prescriptionId == null) {
            return;
        }
        BizDispenseQueue queue = baseMapper.selectByPrescriptionAndDate(prescriptionId, LocalDate.now());
        if (queue == null || queue.getQueueStatus() == null
                || queue.getQueueStatus() != DispenseQueueStatusEnum.WAITING.getCode()) {
            // 患者没签到就直接发药是合法路径，这里不补队列行、不报错
            return;
        }
        Long total = countRxItem(prescriptionId, true);
        Long dispensed = countRxItem(prescriptionId, false);
        BizDispenseQueue update = new BizDispenseQueue();
        update.setId(queue.getId());
        update.setDispensedItemCount(dispensed == null ? 0 : dispensed.intValue());
        boolean finished = total != null && total > 0 && dispensed != null && dispensed >= total;
        if (finished) {
            update.setQueueStatus(DispenseQueueStatusEnum.DISPENSED.getCode());
            update.setDispenseTime(TimeUtil.nowSeconds());
        }
        this.updateById(update);
    }

    /**
     * 建一行取药号（调用方已确认当天没有该处方的队列行）
     */
    private DispenseQueueVO doSignIn(Long prescriptionId, Integer signInType, LocalDate today) {
        if (signInType != null && DispenseSignInTypeEnum.fromCode(signInType) == null) {
            throw new BusinessException("签到方式不合法：" + signInType);
        }
        List<BizDrugDispensing> pendingList = dispensingMapper.selectList(
                new LambdaQueryWrapper<BizDrugDispensing>()
                        .eq(BizDrugDispensing::getPrescriptionId, prescriptionId)
                        .eq(BizDrugDispensing::getDispensingStatus, DispensingStatusEnum.PENDING.getCode())
                        .eq(BizDrugDispensing::getDelFlag, 0)
                        .orderByAsc(BizDrugDispensing::getId));
        if (pendingList.isEmpty()) {
            throw new BusinessException("该处方没有待发药药品，无需取号（未缴费或已发药）");
        }
        BizDrugDispensing snapshot = pendingList.get(0);
        Long total = countRxItem(prescriptionId, true);

        BizDispenseQueue queue = new BizDispenseQueue();
        queue.setQueueDate(today);
        queue.setQueueNo(String.format("%04d", redisSequenceService.next(QUEUE_NO_KEY)));
        queue.setPrescriptionId(prescriptionId);
        queue.setPrescriptionNo(snapshot.getPrescriptionNo());
        queue.setPrescriptionItemCount(total == null ? pendingList.size() : total.intValue());
        queue.setDispensedItemCount(0);
        queue.setPatientId(snapshot.getPatientId());
        queue.setPatientNo(snapshot.getPatientNo());
        queue.setPatientName(snapshot.getPatientName());
        queue.setQueueStatus(DispenseQueueStatusEnum.WAITING.getCode());
        queue.setSignInTime(TimeUtil.nowSeconds());
        queue.setSignInType(signInType);
        queue.setCallCount(0);
        queue.setDelFlag(0);
        this.save(queue);
        return toVO(queue);
    }

    /**
     * 当天已有队列行的处理：只有「待取药」才把原号还回去，其余状态都是在跑流程时不该出现的重复签到
     */
    private DispenseQueueVO reuseExisting(BizDispenseQueue exists) {
        int status = exists.getQueueStatus() == null ? 0 : exists.getQueueStatus();
        if (status == DispenseQueueStatusEnum.DISPENSED.getCode()) {
            throw new BusinessException("该处方已发药，无需重复取号");
        }
        if (status == DispenseQueueStatusEnum.SKIPPED.getCode()) {
            throw new BusinessException("该处方今天已过号，请在取药队列里恢复排队");
        }
        if (status == DispenseQueueStatusEnum.CANCELLED.getCode()) {
            throw new BusinessException("该处方今天的取药号已取消");
        }
        return toVO(exists);
    }

    /**
     * 该处方待发药 + 已发药的明细条数（{@code includePending=false} 时只算已发药）
     */
    private Long countRxItem(Long prescriptionId, boolean includePending) {
        LambdaQueryWrapper<BizDrugDispensing> wrapper = new LambdaQueryWrapper<BizDrugDispensing>()
                .eq(BizDrugDispensing::getPrescriptionId, prescriptionId);
        if (includePending) {
            wrapper.in(BizDrugDispensing::getDispensingStatus,
                    DispensingStatusEnum.PENDING.getCode(), DispensingStatusEnum.DISPENSED.getCode());
        } else {
            wrapper.eq(BizDrugDispensing::getDispensingStatus, DispensingStatusEnum.DISPENSED.getCode());
        }
        return dispensingMapper.selectCount(wrapper);
    }

    /**
     * 把待取药号关成过号/取消
     */
    private boolean closeWaiting(Long queueId, DispenseQueueStatusEnum target, String reason) {
        if (!TextUtil.hasText(reason)) {
            throw new BusinessException("请填写" + target.getLabel() + "原因");
        }
        BizDispenseQueue queue = this.getById(queueId);
        if (queue == null) {
            throw new BusinessException("取药号不存在");
        }
        if (queue.getQueueStatus() == null
                || queue.getQueueStatus() != DispenseQueueStatusEnum.WAITING.getCode()) {
            throw new BusinessException("只有待取药的号才能" + target.getLabel());
        }
        BizDispenseQueue update = new BizDispenseQueue();
        update.setId(queueId);
        update.setQueueStatus(target.getCode());
        update.setCancelReason(TextUtil.cut(reason, 200));
        return this.updateById(update);
    }

    /**
     * 窗口必须是启用的发药窗口（station_kind = 6）：拿不到名称即视为无效 id，
     * 不猜、不回落成「任意窗口」，否则叫号会把号绑到输液室或分诊台。
     */
    private String requireActiveWindow(Long windowId) {
        if (windowId == null) {
            throw new BusinessException("发药窗口ID不能为空");
        }
        String windowName = baseMapper.selectActiveWindowName(windowId);
        if (!TextUtil.hasText(windowName)) {
            throw new BusinessException("该窗口不是启用的发药窗口，无法叫号");
        }
        return windowName;
    }

    /**
     * 操作人：窗口工作站有登录态；自助签到机可能没有，取不到按「系统」记，不阻断取号
     */
    private String currentOperator() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || !TextUtil.hasText(user.getRealName())) {
            return "系统";
        }
        return user.getRealName();
    }

    private DispenseQueueVO toVO(BizDispenseQueue entity) {
        if (entity == null) {
            return null;
        }
        DispenseQueueVO vo = new DispenseQueueVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
