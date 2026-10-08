package com.his.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.operation.dto.CountItemInputUpsertDTO;
import com.his.operation.dto.CountPhaseDTO;
import com.his.operation.dto.CountQtyDTO;
import com.his.operation.dto.OperationCountUpsertDTO;
import com.his.operation.entity.BizOperationApply;
import com.his.operation.entity.BizOperationCount;
import com.his.operation.entity.BizOperationCountItem;
import com.his.operation.enums.CountItemCategoryEnum;
import com.his.operation.enums.CountPhaseEnum;
import com.his.operation.enums.CountResultEnum;
import com.his.operation.enums.CountStatusEnum;
import com.his.operation.enums.OperationApplyStatusEnum;
import com.his.operation.mapper.BizOperationApplyMapper;
import com.his.operation.mapper.BizOperationCountItemMapper;
import com.his.operation.mapper.BizOperationCountMapper;
import com.his.operation.service.OperationCountService;
import com.his.operation.vo.CountItemVO;
import com.his.operation.vo.OperationCountVO;
import com.his.patient.entity.BizPatient;
import com.his.patient.service.BizPatientService;
import com.his.common.service.RedisSequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 手术器械/敷料清点服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationCountServiceImpl extends ServiceImpl<BizOperationCountItemMapper, BizOperationCountItem> implements OperationCountService {

    private final RedisSequenceService redisSequenceService;


    private final BizPatientService bizPatientService;

    private final BizOperationCountMapper bizOperationCountMapper;

    private final BizOperationCountItemMapper bizOperationCountItemMapper;

    private final BizOperationApplyMapper bizOperationApplyMapper;

    private static String textOr(String value, String fallback) {
        return TextUtil.hasText(value) ? value : fallback;
    }

    @Override
    public OperationCountVO getByApply(Long applyId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (applyId == null) {
            throw new BusinessException("手术申请单ID不能为空");
        }
        BizOperationCount entity = bizOperationCountMapper.selectOne(
                new LambdaQueryWrapper<BizOperationCount>()
                        .eq(BizOperationCount::getApplyId, applyId)
                        .last("LIMIT 1"));
        return entity == null ? null : buildVO(entity);
    }

    @Override
    public OperationCountVO getDetailById(Long countId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (countId == null) {
            throw new BusinessException("清点单ID不能为空");
        }
        BizOperationCount entity = bizOperationCountMapper.selectById(countId);
        if (entity == null) {
            throw new BusinessException("手术清点单不存在");
        }
        return buildVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(OperationCountUpsertDTO dto) {
        BizOperationApply apply = bizOperationApplyMapper.selectById(dto.getApplyId());
        if (apply == null) {
            throw new BusinessException("手术申请单不存在");
        }
        if (Integer.valueOf(OperationApplyStatusEnum.CANCELLED.getCode()).equals(apply.getOperationStatus())) {
            throw new BusinessException("手术单 " + apply.getApplyNo() + " 已取消，不需要清点");
        }
        if (bizOperationCountMapper.selectCount(new LambdaQueryWrapper<BizOperationCount>()
                .eq(BizOperationCount::getApplyId, apply.getId())) > 0) {
            throw new BusinessException("该手术已有器械清点单，不能重复建档");
        }

        BizOperationCount entity = new BizOperationCount();
        entity.setCountNo(nextCountNo());
        entity.setApplyId(apply.getId());
        entity.setApplyNo(apply.getApplyNo());
        entity.setAdmissionId(apply.getAdmissionId());
        entity.setPatientId(apply.getPatientId());
        entity.setPatientName(apply.getPatientName());
        entity.setOperationRoom(apply.getOperationRoom());
        entity.setPlannedOperationName(apply.getPlannedOperationName());
        entity.setInstrumentNurseId(dto.getInstrumentNurseId());
        entity.setInstrumentNurseName(employeeNameOf(dto.getInstrumentNurseId()));
        entity.setCirculateNurseId(dto.getCirculateNurseId());
        entity.setCirculateNurseName(employeeNameOf(dto.getCirculateNurseId()));
        entity.setPhase(CountPhaseEnum.NONE.getCode());
        entity.setStatus(CountStatusEnum.RUNNING.getCode());
        entity.setDiscrepancyFlag(0);
        if (TextUtil.hasText(dto.getRemark())) {
            entity.setRemark(dto.getRemark());
        }
        bizOperationCountMapper.insert(entity);

        List<CountItemInputUpsertDTO> inputs = dto.getItems() == null ? List.of() : dto.getItems();
        int seq = 0;
        for (CountItemInputUpsertDTO input : inputs) {
            BizOperationCountItem item = new BizOperationCountItem();
            item.setCountId(entity.getId());
            item.setSeqNo(++seq);
            item.setItemCategory(input.getItemCategory());
            item.setItemName(input.getItemName().trim());
            item.setSpec(input.getSpec());
            item.setBeforeQty(input.getBeforeQty());
            item.setRemark(input.getRemark());
            bizOperationCountItemMapper.insert(item);
        }
        log.info("建立手术清点单 countNo={} applyNo={} 明细 {} 行 洗手护士={} 巡回护士={}",
                entity.getCountNo(), apply.getApplyNo(), seq,
                entity.getInstrumentNurseName(), entity.getCirculateNurseName());
        return entity.getCountNo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addItem(Long countId, CountItemInputUpsertDTO dto) {
        BizOperationCount entity = mustGet(countId);
        if (entity.getPhase() > CountPhaseEnum.NONE.getCode()) {
            throw new BusinessException("清点单 " + entity.getCountNo() + " 已进入「"
                    + CountPhaseEnum.labelOrUnknown(entity.getPhase())
                    + "」，不能再追加明细 —— 新加的那一行的术前基线是后补的，比对没有意义");
        }
        BizOperationCountItem item = new BizOperationCountItem();
        item.setCountId(entity.getId());
        item.setSeqNo(bizOperationCountItemMapper.maxSeqNo(entity.getId()) + 1);
        item.setItemCategory(dto.getItemCategory());
        item.setItemName(dto.getItemName().trim());
        item.setSpec(dto.getSpec());
        item.setBeforeQty(dto.getBeforeQty());
        item.setRemark(dto.getRemark());
        bizOperationCountItemMapper.insert(item);
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void countPhase(CountPhaseDTO dto) {
        BizOperationCount entity = mustGet(dto == null ? null : dto.getCountId());
        Integer phase = dto.getPhase();
        if (!Objects.equals(entity.getPhase() + 1, phase)) {
            throw new BusinessException("清点单 " + entity.getCountNo() + " 当前处于「"
                    + CountPhaseEnum.labelOrUnknown(entity.getPhase()) + "」，只能登记「"
                    + CountPhaseEnum.labelOrUnknown(entity.getPhase() + 1)
                    + "」（三阶段必须按顺序推进，跳过 = 关体后才第一次数，那就晚了）");
        }
        List<BizOperationCountItem> items = bizOperationCountItemMapper.selectByCount(entity.getId());
        if (items.isEmpty()) {
            throw new BusinessException("清点单 " + entity.getCountNo() + " 没有任何明细，先登记清点清单");
        }
        List<CountQtyDTO> quantities = dto.getQuantities() == null ? List.of() : dto.getQuantities();
        if (quantities.size() != items.size()) {
            throw new BusinessException("本次只给了 " + quantities.size() + " 项的数量，清单上共有 " + items.size()
                    + " 项 —— 漏项的清点等于没数（每一项都要报数）");
        }

        String nurseName = employeeNameOf(dto.getNurseId());
        LocalDateTime time = TimeUtil.nowSeconds();
        List<String> diffs = new ArrayList<>();

        for (BizOperationCountItem item : items) {
            CountQtyDTO qtyDto = quantities.stream()
                    .filter(q -> Objects.equals(q.getItemId(), item.getId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("明细「" + item.getItemName() + "」本次没有报数"));
            Integer qty = qtyDto.getQty();
            // D-业务规则：清点数量值域（非空且非负）叠加逐条漏项匹配，DTO 注解无法表达，保留
            if (qty == null || qty < 0) {
                throw new BusinessException("明细「" + item.getItemName() + "」的数量不能为空或负数");
            }
            switch (CountPhaseEnum.fromCode(phase)) {
                case BEFORE -> {
                    // 术前端：就是把这一版数量确立为基线，本身无从比较
                    item.setBeforeQty(qty);
                }
                case CLOSURE -> {
                    item.setClosureQty(qty);
                    if (item.getBeforeQty() != null && !Objects.equals(item.getBeforeQty(), qty)) {
                        diffs.add(item.getItemName() + "（术前 " + item.getBeforeQty() + " → 关体前 " + qty + "）");
                    }
                }
                default -> {
                    item.setFinalQty(qty);
                    if (item.getBeforeQty() != null && !Objects.equals(item.getBeforeQty(), qty)) {
                        diffs.add(item.getItemName() + "（术前 " + item.getBeforeQty() + " → 关体后 " + qty + "）");
                    }
                }
            }
            bizOperationCountItemMapper.updateById(item);
        }

        boolean diff = !diffs.isEmpty();
        // B-条件必填：仅当本次清点算出差异时才要求差异说明，依赖运行时比对结果，DTO 注解无法表达，保留
        if (diff && !TextUtil.hasText(dto.getDiffNote())) {
            throw new BusinessException("本次清点存在差异：" + String.join("；", diffs)
                    + "。必须填写差异说明（差了什么、怎么处理、结论如何）");
        }

        int result = diff ? CountResultEnum.DIFF.getCode() : CountResultEnum.SAME.getCode();
        switch (CountPhaseEnum.fromCode(phase)) {
            case BEFORE -> {
                entity.setBeforeNurseId(dto.getNurseId());
                entity.setBeforeNurseName(nurseName);
                entity.setBeforeTime(time);
                entity.setBeforeResult(result);
            }
            case CLOSURE -> {
                entity.setClosureNurseId(dto.getNurseId());
                entity.setClosureNurseName(nurseName);
                entity.setClosureTime(time);
                entity.setClosureResult(result);
            }
            default -> {
                entity.setFinalNurseId(dto.getNurseId());
                entity.setFinalNurseName(nurseName);
                entity.setFinalTime(time);
                entity.setFinalResult(result);
            }
        }
        entity.setPhase(phase);
        if (diff) {
            entity.setDiscrepancyFlag(1);
            entity.setStatus(CountStatusEnum.DIFF.getCode());
            entity.setDiffNote(TextUtil.hasText(entity.getDiffNote())
                    ? entity.getDiffNote() + "；" + dto.getDiffNote() : dto.getDiffNote());
        } else if (phase == CountPhaseEnum.FINAL.getCode()) {
            entity.setStatus(CountStatusEnum.DONE.getCode());
        } else {
            entity.setStatus(CountStatusEnum.RUNNING.getCode());
        }
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }
        bizOperationCountMapper.updateById(entity);
        log.info("手术清点 countNo={} 阶段={} 结果={} 核对人={}{}",
                entity.getCountNo(), CountPhaseEnum.labelOrUnknown(phase),
                CountResultEnum.labelOrUnknown(result), nurseName,
                diff ? " 差异：" + String.join("；", diffs) : "");
    }

    @Override
    public boolean unfinished(Long applyId) {
        if (applyId == null) {
            return false;
        }
        return bizOperationCountMapper.countUnfinished(applyId) > 0;
    }

    @Override
    public boolean hasDiscrepancy(Long applyId) {
        if (applyId == null) {
            return false;
        }
        return bizOperationCountMapper.countDiscrepancy(applyId) > 0;
    }

    private BizOperationCount mustGet(Long countId) {
        // C-非 DTO 入参：私有 helper 校验方法参数，被多入口复用，Bean Validation 不覆盖，保留
        if (countId == null) {
            throw new BusinessException("清点单ID不能为空");
        }
        BizOperationCount entity = bizOperationCountMapper.selectById(countId);
        if (entity == null) {
            throw new BusinessException("手术清点单不存在");
        }
        return entity;
    }

    private OperationCountVO buildVO(BizOperationCount entity) {
        OperationCountVO vo = new OperationCountVO();
        BeanUtils.copyProperties(entity, vo);
        BizOperationApply apply = bizOperationApplyMapper.selectById(entity.getApplyId());
        if (apply != null) {
            vo.setSurgeonName(apply.getSurgeonName());
            vo.setPlannedStartTime(apply.getPlannedStartTime());
            vo.setOperationStatus(apply.getOperationStatus());
            vo.setOperationStatusText(OperationApplyStatusEnum.getText(apply.getOperationStatus()));
        }
        if (entity.getPatientId() != null) {
            BizPatient patient = bizPatientService.getById(entity.getPatientId());
            if (patient != null) {
                vo.setPatientNo(patient.getPatientNo());
            }
        }

        vo.setPhaseText(CountPhaseEnum.getText(entity.getPhase()));
        vo.setStatusText(CountStatusEnum.getText(entity.getStatus()));
        vo.setBeforeResultText(CountResultEnum.getText(entity.getBeforeResult()));
        vo.setClosureResultText(CountResultEnum.getText(entity.getClosureResult()));
        vo.setFinalResultText(CountResultEnum.getText(entity.getFinalResult()));

        List<BizOperationCountItem> items = bizOperationCountItemMapper.selectByCount(entity.getId());
        List<CountItemVO> vos = new ArrayList<>();
        int totalBefore = 0;
        int totalClosure = 0;
        int totalFinal = 0;
        for (BizOperationCountItem item : items) {
            CountItemVO itemVo = new CountItemVO();
            BeanUtils.copyProperties(item, itemVo);
            itemVo.setItemCategoryText(CountItemCategoryEnum.getText(item.getItemCategory()));
            // ★ 判定基准是术前基线，不是上一段（连续两次都少一块纱布时，"与上段一致"会显示通过）
            if (item.getBeforeQty() != null && item.getFinalQty() != null) {
                itemVo.setConsistent(Objects.equals(item.getBeforeQty(), item.getFinalQty()));
                itemVo.setDiffQty(item.getFinalQty() - item.getBeforeQty());
            } else {
                itemVo.setConsistent(null);
            }
            vos.add(itemVo);
            totalBefore += NumUtil.orZero(item.getBeforeQty());
            totalClosure += NumUtil.orZero(item.getClosureQty());
            totalFinal += NumUtil.orZero(item.getFinalQty());
        }
        vo.setItems(vos);
        vo.setItemCount(vos.size());
        vo.setTotalBefore(totalBefore);
        vo.setTotalClosure(totalClosure);
        vo.setTotalFinal(totalFinal);

        vo.setCanAddItem(entity.getPhase() == CountPhaseEnum.NONE.getCode());
        vo.setCanCountBefore(entity.getPhase() == CountPhaseEnum.NONE.getCode());
        vo.setCanCountClosure(entity.getPhase() == CountPhaseEnum.BEFORE.getCode());
        vo.setCanCountFinal(entity.getPhase() == CountPhaseEnum.CLOSURE.getCode());

        String warn = null;
        if (Integer.valueOf(1).equals(entity.getDiscrepancyFlag())) {
            warn = "存在清点差异（已锁住手术完成登记）：" + textOr(entity.getDiffNote(), "未填写说明");
        } else if (entity.getStatus() == CountStatusEnum.RUNNING.getCode()) {
            warn = "清点尚未走完三轮（下一步：" + CountPhaseEnum.getText(entity.getPhase() + 1) + "）";
        }
        vo.setWarningText(warn);
        return vo;
    }

    private String employeeNameOf(Long empId) {
        if (empId == null) {
            return null;
        }
        String name = bizOperationApplyMapper.selectEmployeeName(empId);
        return TextUtil.hasText(name) ? name : "未知员工(ID=" + empId + ")";
    }

    private String nextCountNo() {
        return redisSequenceService.generateOperationCountNo();
    }
}