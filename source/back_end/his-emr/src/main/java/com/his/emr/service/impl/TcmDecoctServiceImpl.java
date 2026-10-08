package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.PrescriptionTypeEnum;
import com.his.common.enums.TcmDecoctStatusEnum;
import com.his.common.enums.YesOrNoEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.emr.dto.TcmDecoctAdvanceDTO;
import com.his.emr.dto.TcmDecoctCancelDTO;
import com.his.emr.dto.TcmDecoctQueryPageDTO;
import com.his.emr.entity.BizPrescription;
import com.his.emr.entity.BizPrescriptionDetail;
import com.his.emr.entity.BizTcmDecoct;
import com.his.emr.mapper.BizPrescriptionDetailMapper;
import com.his.emr.mapper.BizPrescriptionMapper;
import com.his.emr.mapper.BizTcmDecoctMapper;
import com.his.emr.service.DecoctReceiptPrinter;
import com.his.emr.service.TcmDecoctService;
import com.his.emr.vo.TcmDecoctCountVO;
import com.his.emr.vo.TcmDecoctDetailVO;
import com.his.emr.vo.TcmDecoctVO;
import com.his.system.service.SysAuditLogService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 中药代煎台账实现（sql/139）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TcmDecoctServiceImpl extends ServiceImpl<BizTcmDecoctMapper, BizTcmDecoct> implements TcmDecoctService {

    /**
     * 煎法脚注里的"常规项"：水煎服不必出现在回执的特别提示里
     */
    private static final String METHOD_PLAIN = "水煎服";
    /**
     * 作废原因列宽
     */
    private static final int W_CANCEL_REASON = 200;

    private final BizPrescriptionMapper bizPrescriptionMapper;
    private final BizPrescriptionDetailMapper bizPrescriptionDetailMapper;
    private final RedisSequenceService redisSequenceService;
    private final DecoctReceiptPrinter decoctReceiptPrinter;
    private final SysAuditLogService sysAuditLogService;

    @Override
    public PageResult<TcmDecoctVO> listPage(TcmDecoctQueryPageDTO query) {
        Page<BizTcmDecoct> page = baseMapper.selectDecoctPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                query.getDecoctStatus(), TextUtil.hasText(query.getKeyword()) ? query.getKeyword().trim() : null);
        List<TcmDecoctVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public TcmDecoctDetailVO getDetailById(Long id) {
        return toDetailVO(require(id));
    }

    @Override
    public TcmDecoctCountVO getStatusCount() {
        TcmDecoctCountVO counts = new TcmDecoctCountVO();
        counts.setPending(countByStatus(TcmDecoctStatusEnum.PENDING.getCode()));
        counts.setDecocted(countByStatus(TcmDecoctStatusEnum.DECOCTED.getCode()));
        counts.setPicked(countByStatus(TcmDecoctStatusEnum.PICKED.getCode()));
        counts.setCancelled(countByStatus(TcmDecoctStatusEnum.CANCELLED.getCode()));
        return counts;
    }

    private long countByStatus(Integer status) {
        Long n = this.lambdaQuery().eq(BizTcmDecoct::getDecoctStatus, status).count();
        return n == null ? 0L : n;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizTcmDecoct createOnDispensed(Long prescriptionId) {
        if (prescriptionId == null) {
            return null;
        }
        BizTcmDecoct exists = findByPrescription(prescriptionId);
        if (exists != null) {
            return exists;
        }
        BizPrescription rx = bizPrescriptionMapper.selectById(prescriptionId);
        if (rx == null || !Objects.equals(PrescriptionTypeEnum.TCM.getCode(), rx.getPrescriptionType())
                || !Objects.equals(YesOrNoEnum.YES.getCode(), rx.getDecoctFlag())) {
            // 自煎方 / 非饮片方：本来就不该有代煎单，静默返回 null（调用方据此不建单，不报错）
            return null;
        }
        List<BizPrescriptionDetail> details = listDetails(prescriptionId);
        if (details.isEmpty()) {
            throw new BusinessException("处方 " + rx.getPrescriptionNo() + " 没有明细，不能生成代煎单");
        }

        BigDecimal totalGrams = BigDecimal.ZERO;
        for (BizPrescriptionDetail d : details) {
            if (d.getQuantity() != null) {
                totalGrams = totalGrams.add(d.getQuantity());
            }
        }

        BizTcmDecoct row = new BizTcmDecoct();
        row.setDecoctNo(redisSequenceService.generateTcmDecoctNo());
        row.setPrescriptionId(rx.getId());
        row.setPrescriptionNo(rx.getPrescriptionNo());
        row.setPatientId(rx.getPatientId());
        row.setPatientNo(rx.getPatientNo());
        row.setPatientName(rx.getPatientName());
        row.setDeptName(rx.getDeptName());
        row.setDoctorName(rx.getDoctorName());
        row.setDoseCount(rx.getDoseCount() == null ? 1 : rx.getDoseCount());
        row.setHerbCount(details.size());
        row.setTotalGrams(totalGrams.setScale(2, RoundingMode.HALF_UP));
        row.setMethodSummary(buildMethodSummary(details));
        row.setDecoctStatus(TcmDecoctStatusEnum.PENDING.getCode());
        row.setPharmacyId(currentDeptId());
        row.setPharmacyName(currentDeptName());
        row.setCreateBy(UserUtils.getCurrentUser().getRealName());
        this.save(row);
        log.info("代煎单已生成 {}（处方 {}，{} 剂 / {} 味 / {} g）",
                row.getDecoctNo(), row.getPrescriptionNo(), row.getDoseCount(), row.getHerbCount(), row.getTotalGrams());
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TcmDecoctDetailVO advance(TcmDecoctAdvanceDTO dto) {
        BizTcmDecoct row = require(dto.getId());
        int from = row.getDecoctStatus() == null ? TcmDecoctStatusEnum.PENDING.getCode() : row.getDecoctStatus();
        Integer target = dto.getTargetStatus();
        if (target == null || (target != 2 && target != 3)) {
            throw new BusinessException("只能推进到 2-已煎 或 3-已取");
        }
        if (from == 9) {
            throw new BusinessException("代煎单已作废，不能再推进");
        }
        if (target != from + 1) {
            throw new BusinessException("状态只能逐级推进（当前 "
                    + TcmDecoctStatusEnum.getText(from) + "，不能直接到 " + TcmDecoctStatusEnum.getText(target) + "）");
        }
        LocalDateTime now = TimeUtil.nowSeconds();
        row.setDecoctStatus(target);
        row.setOperatorId(UserUtils.getCurrentUser().getEmployeeId());
        row.setOperatorName(UserUtils.getCurrentUser().getRealName());
        row.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        if (target == 2) {
            row.setDecoctTime(now);
        } else {
            row.setPickupTime(now);
        }
        if (!this.updateById(row)) {
            throw new BusinessException("代煎状态更新失败");
        }
        sysAuditLogService.record(UserUtils.getCurrentUser().getEmployeeId(), UserUtils.getCurrentUser().getRealName(),
                "中药代煎", target == 2 ? "标记已煎" : "标记已取", "biz_tcm_decoct", row.getId(),
                "decoctNo=" + row.getDecoctNo() + " prescription=" + row.getPrescriptionNo()
                        + " " + TcmDecoctStatusEnum.getText(from) + "→" + TcmDecoctStatusEnum.getText(target),
                true, null);
        return toDetailVO(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TcmDecoctDetailVO cancel(TcmDecoctCancelDTO dto) {
        BizTcmDecoct row = require(dto.getId());
        int from = row.getDecoctStatus() == null ? TcmDecoctStatusEnum.PENDING.getCode() : row.getDecoctStatus();
        if (from == 9) {
            throw new BusinessException("该单已作废");
        }
        if (from == 3) {
            // 已取 = 汤液已经交到患者手上，事后作废只会让台账和实物对不上
            throw new BusinessException("代煎单已被患者取走，不能作废（请先走退药流程）");
        }
        String reason = TextUtil.cut(dto.getReason().trim(), W_CANCEL_REASON);
        row.setDecoctStatus(TcmDecoctStatusEnum.CANCELLED.getCode());
        row.setCancelReason(reason);
        row.setOperatorId(UserUtils.getCurrentUser().getEmployeeId());
        row.setOperatorName(UserUtils.getCurrentUser().getRealName());
        row.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        if (!this.updateById(row)) {
            throw new BusinessException("代煎单作废失败");
        }
        sysAuditLogService.record(UserUtils.getCurrentUser().getEmployeeId(), UserUtils.getCurrentUser().getRealName(),
                "中药代煎", "作废代煎单", "biz_tcm_decoct", row.getId(),
                "decoctNo=" + row.getDecoctNo() + " 原状态=" + TcmDecoctStatusEnum.getText(from) + " 原因=" + reason,
                true, null);
        return toDetailVO(row);
    }

    @Override
    public TcmDecoctDetailVO printReceipt(Long id) {
        BizTcmDecoct row = require(id);
        List<BizPrescriptionDetail> details = listDetails(row.getPrescriptionId());
        String taskId = decoctReceiptPrinter.printReceipt(row, details);
        sysAuditLogService.record(UserUtils.getCurrentUser().getEmployeeId(), UserUtils.getCurrentUser().getRealName(),
                "中药代煎", "打印代煎回执", "biz_tcm_decoct", row.getId(),
                "decoctNo=" + row.getDecoctNo() + " 剂数=" + row.getDoseCount()
                        + " 味数=" + row.getHerbCount() + " 总克数=" + row.getTotalGrams()
                        + " 打印任务=" + taskId,
                true, null);
        return toDetailVO(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOnReturn(Long prescriptionId) {
        if (prescriptionId == null) {
            return;
        }
        BizTcmDecoct row = findByPrescription(prescriptionId);
        if (row == null) {
            return;
        }
        int from = row.getDecoctStatus() == null ? TcmDecoctStatusEnum.PENDING.getCode() : row.getDecoctStatus();
        if (from != TcmDecoctStatusEnum.PENDING.getCode()) {
            // 已煎/已取的汤液是做出来了的东西，退不回架上；静默作废等于把实物从账上抹掉
            log.info("代煎单 {} 当前为 {}，退药不自动作废（需药房判断）", row.getDecoctNo(), TcmDecoctStatusEnum.getText(from));
            return;
        }
        row.setDecoctStatus(TcmDecoctStatusEnum.CANCELLED.getCode());
        row.setCancelReason(TextUtil.cut("发药已退，代煎单自动作废", W_CANCEL_REASON));
        row.setOperatorId(UserUtils.getCurrentUser().getEmployeeId());
        row.setOperatorName(UserUtils.getCurrentUser().getRealName());
        row.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        if (!this.updateById(row)) {
            throw new BusinessException("代煎单作废失败");
        }
        sysAuditLogService.record(UserUtils.getCurrentUser().getEmployeeId(), UserUtils.getCurrentUser().getRealName(),
                "中药代煎", "退药自动作废", "biz_tcm_decoct", row.getId(),
                "decoctNo=" + row.getDecoctNo() + " prescription=" + row.getPrescriptionNo() + " 待煎→已作废",
                true, null);
    }

    /**
     * 煎法脚注汇总：把每味的 route 按「先煎：牡蛎、龙骨；后下：薄荷」拼出来。
     * 水煎服是全方常规，不算脚注（否则每张方子的回执上都挂一串没用的提示）。
     */
    private String buildMethodSummary(List<BizPrescriptionDetail> details) {
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        for (BizPrescriptionDetail d : details) {
            String method = d.getRoute() == null ? "" : d.getRoute().trim();
            if (method.isEmpty() || METHOD_PLAIN.equals(method)) {
                continue;
            }
            grouped.computeIfAbsent(method, k -> new ArrayList<>())
                    .add(TextUtil.hasText(d.getDrugName()) ? d.getDrugName() : "未知药味");
        }
        if (grouped.isEmpty()) {
            return null;
        }
        String summary = grouped.entrySet().stream()
                .map(e -> e.getKey() + "：" + String.join("、", e.getValue()))
                .collect(Collectors.joining("；"));
        return TextUtil.cut(summary, 500);
    }

    private List<BizPrescriptionDetail> listDetails(Long prescriptionId) {
        return bizPrescriptionDetailMapper.selectList(new LambdaQueryWrapper<BizPrescriptionDetail>()
                .eq(BizPrescriptionDetail::getPrescriptionId, prescriptionId)
                .orderByAsc(BizPrescriptionDetail::getId));
    }

    private BizTcmDecoct findByPrescription(Long prescriptionId) {
        return this.lambdaQuery().eq(BizTcmDecoct::getPrescriptionId, prescriptionId).last("LIMIT 1").one();
    }

    private BizTcmDecoct require(Long id) {
        // C 类保留：私有兜底被推进/作废/打印等多个入口共用，Bean Validation 覆盖不到这一层
        if (id == null) {
            throw new BusinessException("代煎单ID不能为空");
        }
        BizTcmDecoct row = this.getById(id);
        if (row == null) {
            throw new BusinessException("代煎单不存在");
        }
        return row;
    }

    private TcmDecoctVO toVO(BizTcmDecoct row) {
        TcmDecoctVO vo = new TcmDecoctVO();
        BeanUtils.copyProperties(row, vo);
        vo.setDecoctStatusLabel(TcmDecoctStatusEnum.getText(row.getDecoctStatus()));
        vo.setGramsPerDose(perDoseGrams(row));
        return vo;
    }

    private TcmDecoctDetailVO toDetailVO(BizTcmDecoct row) {
        TcmDecoctDetailVO vo = new TcmDecoctDetailVO();
        BeanUtils.copyProperties(row, vo);
        vo.setDecoctStatusLabel(TcmDecoctStatusEnum.getText(row.getDecoctStatus()));
        vo.setGramsPerDose(perDoseGrams(row));
        List<TcmDecoctDetailVO.HerbLine> herbs = listDetails(row.getPrescriptionId()).stream().map(d -> {
            TcmDecoctDetailVO.HerbLine line = new TcmDecoctDetailVO.HerbLine();
            line.setId(d.getId());
            line.setDrugCode(d.getDrugCode());
            line.setDrugName(d.getDrugName());
            line.setSpecification(d.getSpecification());
            line.setPerDoseText(d.getSingleDosage());
            line.setGrams(d.getQuantity());
            line.setMethod(d.getRoute());
            return line;
        }).collect(Collectors.toList());
        vo.setHerbs(herbs);
        return vo;
    }

    /**
     * 每剂平均克数：煎药室按它加水（总克数 ÷ 剂数），除不尽保留 1 位。
     */
    private BigDecimal perDoseGrams(BizTcmDecoct row) {
        if (row.getTotalGrams() == null || row.getDoseCount() == null || row.getDoseCount() <= 0) {
            return null;
        }
        return row.getTotalGrams().divide(new BigDecimal(row.getDoseCount()), 1, RoundingMode.HALF_UP);
    }

    private Long currentDeptId() {
        return UserUtils.getCurrentUser() == null ? null : UserUtils.getCurrentUser().getDeptId();
    }

    private String currentDeptName() {
        return UserUtils.getCurrentUser() == null ? null : UserUtils.getCurrentUser().getDeptName();
    }
}
