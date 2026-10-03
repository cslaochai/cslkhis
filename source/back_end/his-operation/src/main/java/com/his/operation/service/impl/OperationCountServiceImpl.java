package com.his.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.exception.BusinessException;
import com.his.patient.entity.BizPatient;
import com.his.operation.dto.CountItemInputUpsertDTO;
import com.his.operation.dto.CountPhaseDTO;
import com.his.operation.dto.CountQtyDTO;
import com.his.operation.dto.OperationCountUpsertDTO;
import com.his.operation.entity.BizOperationApply;
import com.his.operation.entity.BizOperationCount;
import com.his.operation.entity.BizOperationCountItem;
import com.his.operation.mapper.BizOperationApplyMapper;
import com.his.operation.mapper.BizOperationCountItemMapper;
import com.his.operation.mapper.BizOperationCountMapper;
import com.his.operation.service.OperationCountService;
import com.his.operation.support.AnesthesiaLabels;
import com.his.operation.support.OperationApplyLabels;
import com.his.operation.vo.CountItemVO;
import com.his.operation.vo.OperationCountVO;
import com.his.patient.service.PatientService;
import com.his.security.CurrentUser;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 手术器械/敷料清点服务实现（G15 并行链）。
 *
 * <p>本类固化了这些<b>至少踩过一次或一定会被追问</b>的点：
 *
 * <ol>
 *   <li><b>三阶段必须顺序推进</b>：术前 → 关体前 → 关体后。允许跳过意味着
 *       "关体后才第一次数"，那时已经晚了。</li>
 *   <li><b>每一阶段都要逐项给全数量</b>：漏项的清点等于没数 ——
 *       "止血钳没数但纱布数了"、迭代器还在跑就把交出去，是异物遗留的标准剧本。</li>
 *   <li><b>清单在开始数之前定</b>：一旦进入任何清点阶段就不再允许加行，
 *       否则新增的那一行的基线数量是后补的，比对毫无意义。</li>
 *   <li><b>一致性的判定对象是术前基线</b>：关体前/后都要回到"术前有多少"这个基准，
 *       而不是和上一段比（连续两次都少一块纱布时，"与上段一致"会显示通过）。</li>
 *   <li><b>对不上就是差异，必须写 diff_note</b>：这个标记会一路锁住手术完成登记
 *       （{@code OperationApplyServiceImpl#finish()}）。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationCountServiceImpl implements OperationCountService {

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BizOperationCountMapper countMapper;
    private final BizOperationCountItemMapper itemMapper;
    private final BizOperationApplyMapper applyMapper;
    private final PatientService patientService;

    @Override
    public OperationCountVO getByApply(Long applyId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (applyId == null) {
            throw new BusinessException("手术申请单ID不能为空");
        }
        BizOperationCount entity = countMapper.selectOne(
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
        BizOperationCount entity = countMapper.selectById(countId);
        if (entity == null) {
            throw new BusinessException("手术清点单不存在");
        }
        return buildVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(OperationCountUpsertDTO dto) {
        BizOperationApply apply = applyMapper.selectById(dto.getApplyId());
        if (apply == null) {
            throw new BusinessException("手术申请单不存在");
        }
        if (Integer.valueOf(OperationApplyLabels.ST_CANCELLED).equals(apply.getOperationStatus())) {
            throw new BusinessException("手术单 " + apply.getApplyNo() + " 已取消，不需要清点");
        }
        if (countMapper.selectCount(new LambdaQueryWrapper<BizOperationCount>()
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
        entity.setPhase(AnesthesiaLabels.COUNT_PHASE_NONE);
        entity.setStatus(AnesthesiaLabels.COUNT_STATUS_RUNNING);
        entity.setDiscrepancyFlag(0);
        if (StringUtils.hasText(dto.getRemark())) {
            entity.setRemark(dto.getRemark());
        }
        countMapper.insert(entity);

        List<CountItemInputUpsertDTO> inputs = dto.getItems() == null ? List.of() : dto.getItems();
        int seq = 0;
        for (CountItemInputUpsertDTO input : inputs) {
            if (!AnesthesiaLabels.isValidCountCategory(input.getItemCategory())) {
                throw new BusinessException("清点项类别取值不合法（应为 1-器械 2-敷料 3-缝针 4-刀片 5-其他）");
            }
            BizOperationCountItem item = new BizOperationCountItem();
            item.setCountId(entity.getId());
            item.setSeqNo(++seq);
            item.setItemCategory(input.getItemCategory());
            item.setItemName(input.getItemName().trim());
            item.setSpec(input.getSpec());
            item.setBeforeQty(input.getBeforeQty());
            item.setRemark(input.getRemark());
            itemMapper.insert(item);
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
        if (entity.getPhase() > AnesthesiaLabels.COUNT_PHASE_NONE) {
            throw new BusinessException("清点单 " + entity.getCountNo() + " 已进入「"
                    + AnesthesiaLabels.countPhaseText(entity.getPhase())
                    + "」，不能再追加明细 —— 新加的那一行的术前基线是后补的，比对没有意义");
        }
        if (!AnesthesiaLabels.isValidCountCategory(dto.getItemCategory())) {
            throw new BusinessException("清点项类别取值不合法（应为 1-器械 2-敷料 3-缝针 4-刀片 5-其他）");
        }
        BizOperationCountItem item = new BizOperationCountItem();
        item.setCountId(entity.getId());
        item.setSeqNo(itemMapper.maxSeqNo(entity.getId()) + 1);
        item.setItemCategory(dto.getItemCategory());
        item.setItemName(dto.getItemName().trim());
        item.setSpec(dto.getSpec());
        item.setBeforeQty(dto.getBeforeQty());
        item.setRemark(dto.getRemark());
        itemMapper.insert(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void countPhase(CountPhaseDTO dto) {
        BizOperationCount entity = mustGet(dto == null ? null : dto.getCountId());
        Integer phase = dto.getPhase();
        if (phase == null || phase < AnesthesiaLabels.COUNT_PHASE_BEFORE || phase > AnesthesiaLabels.COUNT_PHASE_FINAL) {
            throw new BusinessException("清点阶段取值不合法（应为 1-术前 2-关体前 3-关体后）");
        }
        if (!Objects.equals(entity.getPhase() + 1, phase)) {
            throw new BusinessException("清点单 " + entity.getCountNo() + " 当前处于「"
                    + AnesthesiaLabels.countPhaseText(entity.getPhase()) + "」，只能登记「"
                    + AnesthesiaLabels.countPhaseText(entity.getPhase() + 1)
                    + "」（三阶段必须按顺序推进，跳过 = 关体后才第一次数，那就晚了）");
        }
        List<BizOperationCountItem> items = itemMapper.selectByCount(entity.getId());
        if (items.isEmpty()) {
            throw new BusinessException("清点单 " + entity.getCountNo() + " 没有任何明细，先登记清点清单");
        }
        List<CountQtyDTO> quantities = dto.getQuantities() == null ? List.of() : dto.getQuantities();
        if (quantities.size() != items.size()) {
            throw new BusinessException("本次只给了 " + quantities.size() + " 项的数量，清单上共有 " + items.size()
                    + " 项 —— 漏项的清点等于没数（每一项都要报数）");
        }

        String nurseName = employeeNameOf(dto.getNurseId());
        LocalDateTime time = now();
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
            switch (phase) {
                case AnesthesiaLabels.COUNT_PHASE_BEFORE -> {
                    // 术前端：就是把这一版数量确立为基线，本身无从比较
                    item.setBeforeQty(qty);
                }
                case AnesthesiaLabels.COUNT_PHASE_CLOSURE -> {
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
            itemMapper.updateById(item);
        }

        boolean diff = !diffs.isEmpty();
        // B-条件必填：仅当本次清点算出差异时才要求差异说明，依赖运行时比对结果，DTO 注解无法表达，保留
        if (diff && !StringUtils.hasText(dto.getDiffNote())) {
            throw new BusinessException("本次清点存在差异：" + String.join("；", diffs)
                    + "。必须填写差异说明（差了什么、怎么处理、结论如何）");
        }

        int result = diff ? AnesthesiaLabels.COUNT_RESULT_DIFF : AnesthesiaLabels.COUNT_RESULT_SAME;
        switch (phase) {
            case AnesthesiaLabels.COUNT_PHASE_BEFORE -> {
                entity.setBeforeNurseId(dto.getNurseId());
                entity.setBeforeNurseName(nurseName);
                entity.setBeforeTime(time);
                entity.setBeforeResult(result);
            }
            case AnesthesiaLabels.COUNT_PHASE_CLOSURE -> {
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
            entity.setStatus(AnesthesiaLabels.COUNT_STATUS_DIFF);
            entity.setDiffNote(StringUtils.hasText(entity.getDiffNote())
                    ? entity.getDiffNote() + "；" + dto.getDiffNote() : dto.getDiffNote());
        } else if (phase == AnesthesiaLabels.COUNT_PHASE_FINAL) {
            entity.setStatus(AnesthesiaLabels.COUNT_STATUS_DONE);
        } else {
            entity.setStatus(AnesthesiaLabels.COUNT_STATUS_RUNNING);
        }
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }
        countMapper.updateById(entity);
        log.info("手术清点 countNo={} 阶段={} 结果={} 核对人={}{}",
                entity.getCountNo(), AnesthesiaLabels.countPhaseText(phase),
                AnesthesiaLabels.countResultText(result), nurseName,
                diff ? " 差异：" + String.join("；", diffs) : "");
    }

    @Override
    public boolean unfinished(Long applyId) {
        if (applyId == null) {
            return false;
        }
        return countMapper.countUnfinished(applyId) > 0;
    }

    @Override
    public boolean hasDiscrepancy(Long applyId) {
        if (applyId == null) {
            return false;
        }
        return countMapper.countDiscrepancy(applyId) > 0;
    }

    // 内部

    private BizOperationCount mustGet(Long countId) {
        // C-非 DTO 入参：私有 helper 校验方法参数，被多入口复用，Bean Validation 不覆盖，保留
        if (countId == null) {
            throw new BusinessException("清点单ID不能为空");
        }
        BizOperationCount entity = countMapper.selectById(countId);
        if (entity == null) {
            throw new BusinessException("手术清点单不存在");
        }
        return entity;
    }

    private OperationCountVO buildVO(BizOperationCount entity) {
        OperationCountVO vo = new OperationCountVO();
        BeanUtils.copyProperties(entity, vo);
        BizOperationApply apply = applyMapper.selectById(entity.getApplyId());
        if (apply != null) {
            vo.setSurgeonName(apply.getSurgeonName());
            vo.setPlannedStartTime(apply.getPlannedStartTime());
            vo.setOperationStatus(apply.getOperationStatus());
            vo.setOperationStatusText(OperationApplyLabels.statusText(apply.getOperationStatus()));
        }
        if (entity.getPatientId() != null) {
            BizPatient patient = patientService.getById(entity.getPatientId());
            if (patient != null) {
                vo.setPatientNo(patient.getPatientNo());
            }
        }

        vo.setPhaseText(AnesthesiaLabels.countPhaseText(entity.getPhase()));
        vo.setStatusText(AnesthesiaLabels.countStatusText(entity.getStatus()));
        vo.setBeforeResultText(AnesthesiaLabels.countResultText(entity.getBeforeResult()));
        vo.setClosureResultText(AnesthesiaLabels.countResultText(entity.getClosureResult()));
        vo.setFinalResultText(AnesthesiaLabels.countResultText(entity.getFinalResult()));

        List<BizOperationCountItem> items = itemMapper.selectByCount(entity.getId());
        List<CountItemVO> vos = new ArrayList<>();
        int totalBefore = 0;
        int totalClosure = 0;
        int totalFinal = 0;
        for (BizOperationCountItem item : items) {
            CountItemVO itemVo = new CountItemVO();
            BeanUtils.copyProperties(item, itemVo);
            itemVo.setItemCategoryText(AnesthesiaLabels.countCategoryText(item.getItemCategory()));
            // ★ 判定基准是术前基线，不是上一段（连续两次都少一块纱布时，"与上段一致"会显示通过）
            if (item.getBeforeQty() != null && item.getFinalQty() != null) {
                itemVo.setConsistent(Objects.equals(item.getBeforeQty(), item.getFinalQty()));
                itemVo.setDiffQty(item.getFinalQty() - item.getBeforeQty());
            } else {
                itemVo.setConsistent(null);
            }
            vos.add(itemVo);
            totalBefore += nz(item.getBeforeQty());
            totalClosure += nz(item.getClosureQty());
            totalFinal += nz(item.getFinalQty());
        }
        vo.setItems(vos);
        vo.setItemCount(vos.size());
        vo.setTotalBefore(totalBefore);
        vo.setTotalClosure(totalClosure);
        vo.setTotalFinal(totalFinal);

        vo.setCanAddItem(entity.getPhase() == AnesthesiaLabels.COUNT_PHASE_NONE);
        vo.setCanCountBefore(entity.getPhase() == AnesthesiaLabels.COUNT_PHASE_NONE);
        vo.setCanCountClosure(entity.getPhase() == AnesthesiaLabels.COUNT_PHASE_BEFORE);
        vo.setCanCountFinal(entity.getPhase() == AnesthesiaLabels.COUNT_PHASE_CLOSURE);

        String warn = null;
        if (Integer.valueOf(1).equals(entity.getDiscrepancyFlag())) {
            warn = "存在清点差异（已锁住手术完成登记）：" + textOr(entity.getDiffNote(), "未填写说明");
        } else if (entity.getStatus() == AnesthesiaLabels.COUNT_STATUS_RUNNING) {
            warn = "清点尚未走完三轮（下一步：" + AnesthesiaLabels.countPhaseText(entity.getPhase() + 1) + "）";
        }
        vo.setWarningText(warn);
        return vo;
    }

    private String employeeNameOf(Long empId) {
        if (empId == null) {
            return null;
        }
        String name = applyMapper.selectEmployeeName(empId);
        return StringUtils.hasText(name) ? name : "未知员工(ID=" + empId + ")";
    }

    private String nextCountNo() {
        String prefix = "QD" + LocalDate.now().format(NO_DATE);
        long seq = countMapper.countByNoPrefix(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    private static int nz(Integer value) {
        return value == null ? 0 : value;
    }

    private static String textOr(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
