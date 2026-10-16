package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.system.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.pharmacy.dto.*;
import com.his.pharmacy.entity.BizDrugStock;
import com.his.pharmacy.entity.BizDrugTrace;
import com.his.pharmacy.enums.DrugTraceStatusEnum;
import com.his.pharmacy.mapper.BizDrugStockMapper;
import com.his.pharmacy.mapper.BizDrugTraceMapper;
import com.his.pharmacy.service.DrugTraceService;
import com.his.pharmacy.service.DrugTraceUploadChannelService;
import com.his.pharmacy.support.DrugTraceParser;
import com.his.pharmacy.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 药品追溯码采集与核对实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrugTraceServiceImpl extends ServiceImpl<BizDrugTraceMapper, BizDrugTrace> implements DrugTraceService {

    private final RedisSequenceService redisSequenceService;

    /**
     * 采集场景 / 核销场景
     */
    private static final int SCENE_COLLECT = 1;
    private static final int SCENE_DISPENSE = 2;

    /**
     * 上传状态
     */
    private static final int UPLOAD_PENDING = 0;
    private static final int UPLOAD_DONE = 1;
    private static final int UPLOAD_FAILED = 2;

    private static final int FAIL_REASON_MAX = 500;
    private static final int VOID_REASON_MAX = 200;
    private static final int MAX_UPLOAD_LIMIT = 500;

    private final BizDrugTraceMapper bizDrugTraceMapper;

    private final BizDrugStockMapper bizDrugStockMapper;

    private final DrugTraceUploadChannelService drugTraceUploadChannelService;

    private static String blankToNull(String v) {
        return TextUtil.hasText(v) ? v.trim() : null;
    }

    @Override
    public DrugTraceScanVO scan(DrugTraceScanDTO dto) {
        String code = dto.getTraceCode().trim();
        DrugTraceParser.TraceParts parts = DrugTraceParser.parse(code);

        DrugTraceScanVO vo = new DrugTraceScanVO();
        vo.setTraceCode(code);
        vo.setCodeType(parts.getCodeType());
        vo.setDrugDi(parts.getDrugDi());
        vo.setSerialNo(parts.getSerialNo());
        vo.setBatchNo(parts.getBatchNo());
        vo.setExpiryDate(parts.getExpiryDate());
        vo.setParsed(parts.isParsed());

        // 药品字典命中：解析出产品标识才查，查不到留给人工指定
        DrugDictSnapshotVO drug = null;
        if (parts.isParsed()) {
            drug = bizDrugTraceMapper.selectDrugByTraceKey(parts.getDrugDi());
        }
        if (drug == null && dto.getDrugId() != null) {
            // 人工指定药品（码识别不出时的兜底路径，采集按这个药品挂靠）
            drug = bizDrugTraceMapper.selectDrugSnapshot(dto.getDrugId());
        }
        if (drug == null && dto.getDrugId() == null && !parts.isParsed()) {
            vo.setTip("未能识别该码的药品标识（不是 GS1 码也不是 20 位药品追溯码），请人工选择药品后再采集");
        }
        if (drug != null) {
            vo.setMatched(true);
            fillDrug(vo, drug);
        }

        // 该码在台账的现状
        DrugTraceVO exist = bizDrugTraceMapper.selectByTraceCode(code);
        if (exist != null) {
            vo.setExists(true);
            vo.setExistTraceId(exist.getId());
            vo.setExistTraceNo(exist.getTraceNo());
            vo.setExistStatus(exist.getStatus());
        }

        // 批次候选（采集挂靠）
        Long drugId = vo.getDrugId();
        if (drugId != null) {
            vo.setBatches(bizDrugTraceMapper.selectStockOptions(drugId).stream().map(this::toBatchOption).toList());
        }

        // 采集闸门
        boolean canCollect = vo.isMatched() && vo.getDrugStatus() != null && vo.getDrugStatus() == 1
                && !vo.isExists() && vo.getBatches() != null && !vo.getBatches().isEmpty();
        vo.setCanCollect(canCollect);
        if (!canCollect && !TextUtil.hasText(vo.getTip())) {
            vo.setTip(collectTip(vo, parts));
        }

        // 核销闸门（带发药单才判）
        if (dto.getScene() != null && dto.getScene() == SCENE_DISPENSE) {
            if (dto.getDispensingId() == null) {
                vo.setTip("核销场景必须指定发药记录");
            } else {
                DrugDispensingSnapshotVO dp = bizDrugTraceMapper.selectDispensingSnapshot(dto.getDispensingId());
                if (dp == null) {
                    vo.setTip("发药记录不存在，请重新选择");
                } else {
                    vo.setDispensingId(dp.getId());
                    vo.setDispensingNo(dp.getDispensingNo());
                    vo.setDispensingDrugId(dp.getDrugId());
                    vo.setDispensingDrugName(dp.getDrugName());
                    vo.setDispensingQuantity(dp.getQuantity());
                    vo.setDispensingPatientId(dp.getPatientId());
                    vo.setDispensingPatientName(dp.getPatientName());
                    Integer dpStatus = dp.getDispensingStatus();
                    boolean ok;
                    if (dpStatus == null || dpStatus != DrugTraceStatusEnum.DISPENSED.getCode()) {
                        vo.setTip("该发药记录当前状态不是「已发药」，不能核销追溯码");
                        ok = false;
                    } else if (!vo.isExists()) {
                        vo.setTip("该追溯码尚未入库采集，不能核销（请先完成入库采集）");
                        ok = false;
                    } else if (vo.getExistStatus() != null && vo.getExistStatus() != DrugTraceStatusEnum.IN_STOCK.getCode()) {
                        vo.setTip(vo.getExistStatus() == DrugTraceStatusEnum.DISPENSED.getCode()
                                ? "该追溯码已核销过，同一码不允许重复发药（疑似回流药）"
                                : "该追溯码已作废（退药/报损/召回），不能核销");
                        ok = false;
                    } else if (vo.getDrugId() == null || vo.getDispensingDrugId() == null
                            || !vo.getDrugId().equals(vo.getDispensingDrugId())) {
                        vo.setTip("串码：该追溯码是「" + vo.getDrugName() + "」，发药记录是「" + vo.getDispensingDrugName() + "」");
                        ok = false;
                    } else {
                        ok = true;
                    }
                    vo.setCanDispense(ok);
                }
            }
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugTraceVO collect(DrugTraceCollectDTO dto, String operatorName) {
        String code = dto.getTraceCode().trim();
        if (bizDrugTraceMapper.selectByTraceCode(code) != null) {
            throw new BusinessException("该追溯码已采集过，同一码不允许重复采集（采集有误请先删除再重采）");
        }
        DrugTraceParser.TraceParts parts = DrugTraceParser.parse(code);

        DrugDictSnapshotVO drug;
        if (dto.getDrugId() != null) {
            drug = bizDrugTraceMapper.selectDrugSnapshot(dto.getDrugId());
            if (drug == null) {
                throw new BusinessException("药品字典中不存在该药品");
            }
        } else {
            if (!parts.isParsed()) {
                throw new BusinessException("未能识别该码的药品标识，请人工选择药品后再采集");
            }
            drug = bizDrugTraceMapper.selectDrugByTraceKey(parts.getDrugDi());
            if (drug == null) {
                throw new BusinessException("追溯标识 " + parts.getDrugDi() + " 未命中药品字典，请先在药品字典录入该追溯标识，或人工选择药品");
            }
        }
        // status 为 null 的字典行按停用处理：宁可拦住让人工确认，也不能放一个状态不明的药进台账
        if (drug.getStatus() == null || drug.getStatus() == 0) {
            throw new BusinessException("药品「" + drug.getDrugName() + "」已停用，不能采集");
        }

        BizDrugStock stock = bizDrugStockMapper.selectById(dto.getStockId());
        if (stock == null) {
            throw new BusinessException("挂靠批次不存在");
        }
        if (!drug.getId().equals(stock.getDrugId())) {
            throw new BusinessException("所选批次与药品不一致：批次属于另一个药品");
        }
        if (stock.getExpiryDate() != null && stock.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BusinessException("该批次已过有效期（" + stock.getExpiryDate() + "），不能挂靠采集");
        }

        Integer sourceType = dto.getSourceType() == null ? 1 : dto.getSourceType();
        BizDrugTrace t = new BizDrugTrace();
        t.setTraceNo(nextTraceNo());
        t.setTraceCode(code);
        t.setCodeType(parts.getCodeType());
        t.setDrugDi(parts.getDrugDi());
        t.setSerialNo(parts.getSerialNo());
        t.setCodeBatchNo(parts.getBatchNo());
        t.setCodeExpiryDate(parts.getExpiryDate());

        t.setDrugId(drug.getId());
        t.setDrugCode(drug.getDrugCode());
        t.setDrugName(drug.getDrugName());
        t.setGenericName(drug.getGenericName());
        t.setSpecification(drug.getSpecification());
        t.setDosageForm(drug.getDosageForm());
        t.setUnit(drug.getUnit());
        t.setManufacturer(drug.getManufacturer());
        t.setApprovalNumber(drug.getApprovalNumber());

        t.setStockId(stock.getId());
        t.setStockBatchNo(stock.getBatchNo());
        t.setSupplier(stock.getSupplier());
        t.setSupplierId(stock.getSupplierId());
        t.setSourceType(sourceType);

        if (dto.getInboundId() != null) {
            DrugInboundSnapshotVO ib = bizDrugTraceMapper.selectInboundSnapshot(dto.getInboundId());
            if (ib == null) {
                throw new BusinessException("来源入库单不存在");
            }
            t.setInboundId(ib.getId());
            t.setInboundNo(ib.getInboundNo());
            if (!TextUtil.hasText(t.getSupplier())) {
                t.setSupplier(ib.getSupplier());
            }
        }

        t.setStatus(DrugTraceStatusEnum.IN_STOCK.getCode());
        t.setScanTime(TimeUtil.nowSeconds());
        t.setOperatorName(operatorName);
        t.setUploadStatus(UPLOAD_PENDING);
        t.setRemark(dto.getRemark());
        bizDrugTraceMapper.insert(t);
        return bizDrugTraceMapper.selectTraceById(t.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugTraceVO verifyDispense(DrugTraceDispenseDTO dto, String operatorName) {
        String code = dto.getTraceCode().trim();
        DrugDispensingSnapshotVO dp = bizDrugTraceMapper.selectDispensingSnapshot(dto.getDispensingId());
        if (dp == null) {
            throw new BusinessException("发药记录不存在");
        }
        if (dp.getDispensingStatus() == null || dp.getDispensingStatus() != DrugTraceStatusEnum.DISPENSED.getCode()) {
            throw new BusinessException("仅「已发药」的记录可以核销追溯码");
        }
        DrugTraceVO exist = bizDrugTraceMapper.selectByTraceCode(code);
        if (exist == null) {
            throw new BusinessException("该追溯码尚未入库采集，不能核销（请先完成入库采集）");
        }
        if (exist.getStatus() == null || exist.getStatus() != DrugTraceStatusEnum.IN_STOCK.getCode()) {
            throw new BusinessException(exist.getStatus() != null && exist.getStatus() == DrugTraceStatusEnum.DISPENSED.getCode()
                    ? "该追溯码已核销过，同一码不允许重复发药（疑似回流药）"
                    : "该追溯码已作废（退药/报损/召回），不能核销");
        }
        Long dpDrugId = dp.getDrugId();
        if (exist.getDrugId() == null || dpDrugId == null || !exist.getDrugId().equals(dpDrugId)) {
            throw new BusinessException("串码：该追溯码是「" + exist.getDrugName() + "」，发药记录是「" + dp.getDrugName() + "」");
        }

        BizDrugTrace t = new BizDrugTrace();
        t.setId(exist.getId());
        t.setStatus(DrugTraceStatusEnum.DISPENSED.getCode());
        t.setDispensingId(dp.getId());
        t.setDispensingNo(dp.getDispensingNo());
        t.setPatientId(dp.getPatientId());
        t.setPatientNo(dp.getPatientNo());
        t.setPatientName(dp.getPatientName());
        t.setDispenseTime(TimeUtil.nowSeconds());
        t.setDispenseOperator(operatorName);
        // 核销是一次新事件，必须重新上传（上传过的是"采集"事件，不是"核销"事件）
        t.setUploadStatus(UPLOAD_PENDING);
        t.setUploadBatchNo("");
        t.setUploadFailReason("");
        bizDrugTraceMapper.updateById(t);
        return bizDrugTraceMapper.selectTraceById(exist.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugTraceVO voidTrace(DrugTraceVoidDTO dto, String operatorName) {
        DrugTraceVO exist = bizDrugTraceMapper.selectTraceById(dto.getTraceId());
        if (exist == null) {
            throw new BusinessException("追溯码记录不存在");
        }
        if (exist.getStatus() != null && exist.getStatus() == DrugTraceStatusEnum.VOID.getCode()) {
            throw new BusinessException("该追溯码已作废，请勿重复操作");
        }
        BizDrugTrace t = new BizDrugTrace();
        t.setId(exist.getId());
        t.setStatus(DrugTraceStatusEnum.VOID.getCode());
        t.setVoidType(dto.getVoidType());
        t.setVoidTime(TimeUtil.nowSeconds());
        t.setVoidReason(TextUtil.cut(TextUtil.hasText(dto.getReason()) ? dto.getReason() : "未填写原因", VOID_REASON_MAX));
        // 作废也是要上报的变更事件
        t.setUploadStatus(UPLOAD_PENDING);
        t.setUploadBatchNo("");
        t.setUploadFailReason("");
        bizDrugTraceMapper.updateById(t);
        return bizDrugTraceMapper.selectTraceById(exist.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugTraceUploadResultVO upload(DrugTraceUploadDTO dto, String operatorName) {
        List<Long> ids = new ArrayList<>();
        if (dto.getIds() != null && !dto.getIds().isEmpty()) {
            ids.addAll(dto.getIds());
        } else {
            int limit = Math.min(dto.getLimit() == null || dto.getLimit() <= 0 ? 200 : dto.getLimit(), MAX_UPLOAD_LIMIT);
            ids.addAll(bizDrugTraceMapper.selectUploadCandidates(limit));
        }
        DrugTraceUploadResultVO result = new DrugTraceUploadResultVO();
        String batchNo = nextUploadBatchNo();
        result.setUploadBatchNo(batchNo);
        result.setTotal(ids.size());
        result.setSuccess(0);
        result.setFailed(0);
        result.setFailures(new ArrayList<>());
        if (ids.isEmpty()) {
            return result;
        }

        List<BizDrugTrace> rows = new ArrayList<>();
        List<DrugTraceUploadChannelService.UploadLine> lines = new ArrayList<>();
        for (Long id : ids) {
            BizDrugTrace t = bizDrugTraceMapper.selectById(id);
            if (t == null) {
                continue;
            }
            if (t.getUploadStatus() != null && t.getUploadStatus() == UPLOAD_DONE) {
                // 已上传且状态未变的不再重复上报（重传只针对待上传/失败）
                continue;
            }
            rows.add(t);
            DrugTraceUploadChannelService.UploadLine line = new DrugTraceUploadChannelService.UploadLine();
            line.setTraceId(t.getId());
            line.setTraceNo(t.getTraceNo());
            line.setTraceCode(t.getTraceCode());
            line.setDrugCode(t.getDrugCode());
            line.setDrugName(t.getDrugName());
            line.setApprovalNumber(t.getApprovalNumber());
            line.setBatchNo(TextUtil.hasText(t.getStockBatchNo()) ? t.getStockBatchNo() : t.getCodeBatchNo());
            line.setEventType(t.getStatus() == null ? 1 : t.getStatus());
            line.setEventTime(t.getStatus() != null && t.getStatus() == DrugTraceStatusEnum.DISPENSED.getCode() ? t.getDispenseTime() : t.getScanTime());
            line.setPatientName(t.getPatientName());
            lines.add(line);
        }

        List<DrugTraceUploadChannelService.UploadAck> acks = drugTraceUploadChannelService.upload(lines);
        LocalDateTime now = TimeUtil.nowSeconds();
        int ok = 0;
        int fail = 0;
        for (int i = 0; i < rows.size(); i++) {
            BizDrugTrace t = rows.get(i);
            DrugTraceUploadChannelService.UploadAck ack = i < acks.size() ? acks.get(i) : null;
            boolean success = ack != null && ack.isSuccess();
            BizDrugTrace upd = new BizDrugTrace();
            upd.setId(t.getId());
            upd.setUploadBatchNo(batchNo);
            if (success) {
                upd.setUploadStatus(UPLOAD_DONE);
                upd.setUploadTime(now);
                upd.setUploadFailReason("");
                ok++;
            } else {
                upd.setUploadStatus(UPLOAD_FAILED);
                upd.setUploadFailReason(TextUtil.cut(ack == null ? "上传通道无回执" : ack.getMessage(), FAIL_REASON_MAX));
                fail++;
                DrugTraceUploadResultVO.FailItem item = new DrugTraceUploadResultVO.FailItem();
                item.setTraceId(t.getId());
                item.setTraceCode(t.getTraceCode());
                item.setDrugName(t.getDrugName());
                item.setReason(upd.getUploadFailReason());
                result.getFailures().add(item);
            }
            bizDrugTraceMapper.updateById(upd);
        }
        result.setTotal(rows.size());
        result.setSuccess(ok);
        result.setFailed(fail);
        log.info("[药品追溯码上传] batchNo={} operator={} total={} success={} failed={}", batchNo, operatorName, ids.size(), ok, fail);
        return result;
    }

    @Override
    public PageResult<DrugTraceVO> page(DrugTraceQueryPageDTO q) {
        Page<DrugTraceVO> page = bizDrugTraceMapper.selectTracePage(
                new Page<>(q.getPageNum(), q.getPageSize()),
                blankToNull(q.getKeyword()), q.getDrugId(), q.getStockId(), q.getStatus(),
                q.getUploadStatus(), q.getCodeType(), q.getSourceType(), q.getPatientId(), q.getDispensingId());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public DrugTraceVO getDetailById(Long id) {
        DrugTraceVO vo = bizDrugTraceMapper.selectTraceById(id);
        if (vo == null) {
            throw new BusinessException("追溯码记录不存在");
        }
        return vo;
    }

    @Override
    public DrugTraceReconcileVO reconcileStats() {
        DrugTraceReconcileVO vo = bizDrugTraceMapper.selectTraceStats();
        vo.setUnTracedDispense(bizDrugTraceMapper.countUnTracedDispense());
        vo.setRequiredUnTracedDispense(bizDrugTraceMapper.countRequiredUnTracedDispense());
        vo.setRecentDispenseTotal(bizDrugTraceMapper.countRecentDispense());
        return vo;
    }

    @Override
    public void deleteById(Long id) {
        DrugTraceVO exist = bizDrugTraceMapper.selectTraceById(id);
        if (exist == null) {
            throw new BusinessException("追溯码记录不存在");
        }
        int n = bizDrugTraceMapper.purgeById(id);
        if (n == 0) {
            throw new BusinessException("只能删除「在库且未上传」的误采记录；已核销或已上传的码属于医保数据，不允许删除");
        }
    }

    private void fillDrug(DrugTraceScanVO vo, DrugDictSnapshotVO drug) {
        vo.setDrugId(drug.getId());
        vo.setDrugCode(drug.getDrugCode());
        vo.setDrugName(drug.getDrugName());
        vo.setGenericName(drug.getGenericName());
        vo.setSpecification(drug.getSpecification());
        vo.setDosageForm(drug.getDosageForm());
        vo.setUnit(drug.getUnit());
        vo.setManufacturer(drug.getManufacturer());
        vo.setApprovalNumber(drug.getApprovalNumber());
        vo.setIsTraceRequired(drug.getIsTraceRequired());
        vo.setDrugStatus(drug.getStatus());
    }

    private DrugTraceScanVO.BatchOption toBatchOption(DrugStockOptionVO m) {
        DrugTraceScanVO.BatchOption opt = new DrugTraceScanVO.BatchOption();
        opt.setStockId(m.getId());
        opt.setBatchNo(m.getBatchNo());
        opt.setExpiryDate(m.getExpiryDate());
        opt.setQuantity(m.getQuantity());
        opt.setAvailableQuantity(m.getAvailableQuantity());
        opt.setStockRoom(m.getStockRoom());
        opt.setLocation(m.getLocation());
        opt.setSupplier(m.getSupplier());
        return opt;
    }

    private String collectTip(DrugTraceScanVO vo, DrugTraceParser.TraceParts parts) {
        if (!vo.isParsed()) {
            return "未能识别该码的药品标识（不是 GS1 码也不是 20 位药品追溯码），请人工选择药品后再采集";
        }
        if (!vo.isMatched()) {
            return "追溯标识 " + parts.getDrugDi() + " 未命中药品字典，请先在药品字典录入该追溯标识，或人工选择药品";
        }
        if (vo.getDrugStatus() != null && vo.getDrugStatus() == 0) {
            return "药品「" + vo.getDrugName() + "」已停用，不能采集";
        }
        if (vo.isExists()) {
            return "该追溯码已采集过（" + DrugTraceStatusEnum.labelOrUnknown(vo.getExistStatus()) + "），同一码不允许重复采集";
        }
        if (vo.getBatches() == null || vo.getBatches().isEmpty()) {
            return "该药品暂无库存批次，请先入库再采集";
        }
        return null;
    }

    private String nextTraceNo() {
        return redisSequenceService.generateDrugTraceNo();
    }

    private String nextUploadBatchNo() {
        return redisSequenceService.generateDrugUploadBatchNo();
    }
}
