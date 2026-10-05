package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.pharmacy.dto.DrugTraceCollectDTO;
import com.his.pharmacy.dto.DrugTraceDispenseDTO;
import com.his.pharmacy.dto.DrugTraceQueryPageDTO;
import com.his.pharmacy.dto.DrugTraceScanDTO;
import com.his.pharmacy.dto.DrugTraceUploadDTO;
import com.his.pharmacy.dto.DrugTraceVoidDTO;
import com.his.pharmacy.entity.BizDrugStock;
import com.his.pharmacy.entity.BizDrugTrace;
import com.his.pharmacy.enums.DrugTraceStatusEnum;
import com.his.pharmacy.mapper.BizDrugStockMapper;
import com.his.pharmacy.mapper.BizDrugTraceMapper;
import com.his.pharmacy.service.DrugTraceService;
import com.his.pharmacy.service.DrugTraceUploadChannelService;
import com.his.pharmacy.support.DrugTraceParser;
import com.his.pharmacy.vo.DrugTraceReconcileVO;
import com.his.pharmacy.vo.DrugTraceScanVO;
import com.his.pharmacy.vo.DrugTraceUploadResultVO;
import com.his.pharmacy.vo.DrugTraceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 药品追溯码采集与核对实现。
 *
 * <p>三条铁律：
 * <ol>
 *   <li><b>一码只能采一次</b>（uk_drug_trace_code）：重复采集直接拒绝，采集错了就删掉重采，不允许覆盖。</li>
 *   <li><b>未采集不许核销</b>：发药窗口扫到的码必须能在台账里找到"在库"的那一行 ——
 *       这是医保局查回流药的唯一抓手，放行"先发后补"等于把闸门拆了。</li>
 *   <li><b>发出去再退回的药不得再销售</b>：退药/报损/召回一律置已作废，没有回库重发这条路径。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrugTraceServiceImpl implements DrugTraceService {

    /** 采集场景 / 核销场景 */
    private static final int SCENE_COLLECT = 1;
    private static final int SCENE_DISPENSE = 2;

    /** 上传状态 */
    private static final int UPLOAD_PENDING = 0;
    private static final int UPLOAD_DONE = 1;
    private static final int UPLOAD_FAILED = 2;

    private static final int FAIL_REASON_MAX = 500;
    private static final int VOID_REASON_MAX = 200;
    private static final int MAX_UPLOAD_LIMIT = 500;

    private final BizDrugTraceMapper traceMapper;
    private final BizDrugStockMapper stockMapper;
    private final DrugTraceUploadChannelService uploadGateway;


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
        Map<String, Object> drug = null;
        if (parts.isParsed()) {
            drug = traceMapper.selectDrugByTraceKey(parts.getDrugDi());
        }
        if (drug == null && dto.getDrugId() != null) {
            // 人工指定药品（码识别不出时的兜底路径，采集按这个药品挂靠）
            drug = traceMapper.selectDrugSnapshot(dto.getDrugId());
        }
        if (drug == null && dto.getDrugId() == null && !parts.isParsed()) {
            vo.setTip("未能识别该码的药品标识（不是 GS1 码也不是 20 位药品追溯码），请人工选择药品后再采集");
        }
        if (drug != null) {
            vo.setMatched(true);
            fillDrug(vo, drug);
        }

        // 该码在台账的现状
        DrugTraceVO exist = traceMapper.selectByTraceCode(code);
        if (exist != null) {
            vo.setExists(true);
            vo.setExistTraceId(exist.getId());
            vo.setExistTraceNo(exist.getTraceNo());
            vo.setExistStatus(exist.getStatus());
        }

        // 批次候选（采集挂靠）
        Long drugId = vo.getDrugId();
        if (drugId != null) {
            vo.setBatches(traceMapper.selectStockOptions(drugId).stream().map(this::toBatchOption).toList());
        }

        // 采集闸门
        boolean canCollect = vo.isMatched() && vo.getDrugStatus() != null && vo.getDrugStatus() == 1
                && !vo.isExists() && vo.getBatches() != null && !vo.getBatches().isEmpty();
        vo.setCanCollect(canCollect);
        if (!canCollect && !StringUtils.hasText(vo.getTip())) {
            vo.setTip(collectTip(vo, parts));
        }

        // 核销闸门（带发药单才判）
        if (dto.getScene() != null && dto.getScene() == SCENE_DISPENSE) {
            if (dto.getDispensingId() == null) {
                vo.setTip("核销场景必须指定发药记录");
            } else {
                Map<String, Object> dp = traceMapper.selectDispensingSnapshot(dto.getDispensingId());
                if (dp == null) {
                    vo.setTip("发药记录不存在，请重新选择");
                } else {
                    vo.setDispensingId(toLong(dp.get("id")));
                    vo.setDispensingNo(str(dp.get("dispensing_no")));
                    vo.setDispensingDrugId(toLong(dp.get("drug_id")));
                    vo.setDispensingDrugName(str(dp.get("drug_name")));
                    vo.setDispensingQuantity(toDecimal(dp.get("quantity")));
                    vo.setDispensingPatientId(toLong(dp.get("patient_id")));
                    vo.setDispensingPatientName(str(dp.get("patient_name")));
                    int dpStatus = toInt(dp.get("dispensing_status"));
                    boolean ok;
                    if (dpStatus != DrugTraceStatusEnum.DISPENSED.getCode()) {
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
        if (traceMapper.selectByTraceCode(code) != null) {
            throw new BusinessException("该追溯码已采集过，同一码不允许重复采集（采集有误请先删除再重采）");
        }
        DrugTraceParser.TraceParts parts = DrugTraceParser.parse(code);

        Map<String, Object> drug;
        if (dto.getDrugId() != null) {
            drug = traceMapper.selectDrugSnapshot(dto.getDrugId());
            if (drug == null) {
                throw new BusinessException("药品字典中不存在该药品");
            }
        } else {
            if (!parts.isParsed()) {
                throw new BusinessException("未能识别该码的药品标识，请人工选择药品后再采集");
            }
            drug = traceMapper.selectDrugByTraceKey(parts.getDrugDi());
            if (drug == null) {
                throw new BusinessException("追溯标识 " + parts.getDrugDi() + " 未命中药品字典，请先在药品字典录入该追溯标识，或人工选择药品");
            }
        }
        if (toInt(drug.get("status")) == 0) {
            throw new BusinessException("药品「" + str(drug.get("drug_name")) + "」已停用，不能采集");
        }

        BizDrugStock stock = stockMapper.selectById(dto.getStockId());
        if (stock == null) {
            throw new BusinessException("挂靠批次不存在");
        }
        if (!toLong(drug.get("id")).equals(stock.getDrugId())) {
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

        t.setDrugId(toLong(drug.get("id")));
        t.setDrugCode(str(drug.get("drug_code")));
        t.setDrugName(str(drug.get("drug_name")));
        t.setGenericName(str(drug.get("generic_name")));
        t.setSpecification(str(drug.get("specification")));
        t.setDosageForm(str(drug.get("dosage_form")));
        t.setUnit(str(drug.get("unit")));
        t.setManufacturer(str(drug.get("manufacturer")));
        t.setApprovalNumber(str(drug.get("approval_number")));

        t.setStockId(stock.getId());
        t.setStockBatchNo(stock.getBatchNo());
        t.setSupplier(stock.getSupplier());
        t.setSupplierId(stock.getSupplierId());
        t.setSourceType(sourceType);

        if (dto.getInboundId() != null) {
            Map<String, Object> ib = traceMapper.selectInboundSnapshot(dto.getInboundId());
            if (ib == null) {
                throw new BusinessException("来源入库单不存在");
            }
            t.setInboundId(toLong(ib.get("id")));
            t.setInboundNo(str(ib.get("inbound_no")));
            if (!StringUtils.hasText(t.getSupplier())) {
                t.setSupplier(str(ib.get("supplier")));
            }
        }

        t.setStatus(DrugTraceStatusEnum.IN_STOCK.getCode());
        t.setScanTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        t.setOperatorName(operatorName);
        t.setUploadStatus(UPLOAD_PENDING);
        t.setRemark(dto.getRemark());
        traceMapper.insert(t);
        return traceMapper.selectTraceById(t.getId());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugTraceVO verifyDispense(DrugTraceDispenseDTO dto, String operatorName) {
        String code = dto.getTraceCode().trim();
        Map<String, Object> dp = traceMapper.selectDispensingSnapshot(dto.getDispensingId());
        if (dp == null) {
            throw new BusinessException("发药记录不存在");
        }
        if (toInt(dp.get("dispensing_status")) != DrugTraceStatusEnum.DISPENSED.getCode()) {
            throw new BusinessException("仅「已发药」的记录可以核销追溯码");
        }
        DrugTraceVO exist = traceMapper.selectByTraceCode(code);
        if (exist == null) {
            throw new BusinessException("该追溯码尚未入库采集，不能核销（请先完成入库采集）");
        }
        if (exist.getStatus() == null || exist.getStatus() != DrugTraceStatusEnum.IN_STOCK.getCode()) {
            throw new BusinessException(exist.getStatus() != null && exist.getStatus() == DrugTraceStatusEnum.DISPENSED.getCode()
                    ? "该追溯码已核销过，同一码不允许重复发药（疑似回流药）"
                    : "该追溯码已作废（退药/报损/召回），不能核销");
        }
        Long dpDrugId = toLong(dp.get("drug_id"));
        if (exist.getDrugId() == null || dpDrugId == null || !exist.getDrugId().equals(dpDrugId)) {
            throw new BusinessException("串码：该追溯码是「" + exist.getDrugName() + "」，发药记录是「" + str(dp.get("drug_name")) + "」");
        }

        BizDrugTrace t = new BizDrugTrace();
        t.setId(exist.getId());
        t.setStatus(DrugTraceStatusEnum.DISPENSED.getCode());
        t.setDispensingId(toLong(dp.get("id")));
        t.setDispensingNo(str(dp.get("dispensing_no")));
        t.setPatientId(toLong(dp.get("patient_id")));
        t.setPatientNo(str(dp.get("patient_no")));
        t.setPatientName(str(dp.get("patient_name")));
        t.setDispenseTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        t.setDispenseOperator(operatorName);
        // 核销是一次新事件，必须重新上传（上传过的是"采集"事件，不是"核销"事件）
        t.setUploadStatus(UPLOAD_PENDING);
        t.setUploadBatchNo("");
        t.setUploadFailReason("");
        traceMapper.updateById(t);
        return traceMapper.selectTraceById(exist.getId());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugTraceVO voidTrace(DrugTraceVoidDTO dto, String operatorName) {
        DrugTraceVO exist = traceMapper.selectTraceById(dto.getTraceId());
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
        t.setVoidTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        t.setVoidReason(cut(StringUtils.hasText(dto.getReason()) ? dto.getReason() : "未填写原因", VOID_REASON_MAX));
        // 作废也是要上报的变更事件
        t.setUploadStatus(UPLOAD_PENDING);
        t.setUploadBatchNo("");
        t.setUploadFailReason("");
        traceMapper.updateById(t);
        return traceMapper.selectTraceById(exist.getId());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugTraceUploadResultVO upload(DrugTraceUploadDTO dto, String operatorName) {
        List<Long> ids = new ArrayList<>();
        if (dto.getIds() != null && !dto.getIds().isEmpty()) {
            ids.addAll(dto.getIds());
        } else {
            int limit = Math.min(dto.getLimit() == null || dto.getLimit() <= 0 ? 200 : dto.getLimit(), MAX_UPLOAD_LIMIT);
            ids.addAll(traceMapper.selectUploadCandidates(limit));
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
            BizDrugTrace t = traceMapper.selectById(id);
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
            line.setBatchNo(StringUtils.hasText(t.getStockBatchNo()) ? t.getStockBatchNo() : t.getCodeBatchNo());
            line.setEventType(t.getStatus() == null ? 1 : t.getStatus());
            line.setEventTime(t.getStatus() != null && t.getStatus() == DrugTraceStatusEnum.DISPENSED.getCode() ? t.getDispenseTime() : t.getScanTime());
            line.setPatientName(t.getPatientName());
            lines.add(line);
        }

        List<DrugTraceUploadChannelService.UploadAck> acks = uploadGateway.upload(lines);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
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
                upd.setUploadFailReason(cut(ack == null ? "上传通道无回执" : ack.getMessage(), FAIL_REASON_MAX));
                fail++;
                DrugTraceUploadResultVO.FailItem item = new DrugTraceUploadResultVO.FailItem();
                item.setTraceId(t.getId());
                item.setTraceCode(t.getTraceCode());
                item.setDrugName(t.getDrugName());
                item.setReason(upd.getUploadFailReason());
                result.getFailures().add(item);
            }
            traceMapper.updateById(upd);
        }
        result.setTotal(rows.size());
        result.setSuccess(ok);
        result.setFailed(fail);
        log.info("[药品追溯码上传] batchNo={} operator={} total={} success={} failed={}", batchNo, operatorName, ids.size(), ok, fail);
        return result;
    }


    @Override
    public PageResult<DrugTraceVO> page(DrugTraceQueryPageDTO q) {
        Page<DrugTraceVO> page = traceMapper.selectTracePage(
                new Page<>(q.getPageNum(), q.getPageSize()),
                blankToNull(q.getKeyword()), q.getDrugId(), q.getStockId(), q.getStatus(),
                q.getUploadStatus(), q.getCodeType(), q.getSourceType(), q.getPatientId(), q.getDispensingId());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public DrugTraceVO getDetailById(Long id) {
        DrugTraceVO vo = traceMapper.selectTraceById(id);
        if (vo == null) {
            throw new BusinessException("追溯码记录不存在");
        }
        return vo;
    }

    @Override
    public DrugTraceReconcileVO reconcileStats() {
        Map<String, Object> s = traceMapper.selectTraceStats();
        DrugTraceReconcileVO vo = new DrugTraceReconcileVO();
        vo.setTotal(toLongObj(s == null ? null : s.get("total")));
        vo.setInStock(toLongObj(s == null ? null : s.get("in_stock")));
        vo.setDispensed(toLongObj(s == null ? null : s.get("dispensed")));
        vo.setVoided(toLongObj(s == null ? null : s.get("voided")));
        vo.setPendingUpload(toLongObj(s == null ? null : s.get("pending_upload")));
        vo.setUploaded(toLongObj(s == null ? null : s.get("uploaded")));
        vo.setUploadFailed(toLongObj(s == null ? null : s.get("upload_failed")));
        vo.setDrugKinds(toLongObj(s == null ? null : s.get("drug_kinds")));
        vo.setUnTracedDispense(toLongObj(firstValue(traceMapper.countUnTracedDispense())));
        vo.setRequiredUnTracedDispense(toLongObj(firstValue(traceMapper.countRequiredUnTracedDispense())));
        vo.setRecentDispenseTotal(toLongObj(firstValue(traceMapper.countRecentDispense())));
        return vo;
    }

    @Override
    public void deleteById(Long id) {
        DrugTraceVO exist = traceMapper.selectTraceById(id);
        if (exist == null) {
            throw new BusinessException("追溯码记录不存在");
        }
        int n = traceMapper.purgeById(id);
        if (n == 0) {
            throw new BusinessException("只能删除「在库且未上传」的误采记录；已核销或已上传的码属于医保数据，不允许删除");
        }
    }


    private void fillDrug(DrugTraceScanVO vo, Map<String, Object> drug) {
        vo.setDrugId(toLong(drug.get("id")));
        vo.setDrugCode(str(drug.get("drug_code")));
        vo.setDrugName(str(drug.get("drug_name")));
        vo.setGenericName(str(drug.get("generic_name")));
        vo.setSpecification(str(drug.get("specification")));
        vo.setDosageForm(str(drug.get("dosage_form")));
        vo.setUnit(str(drug.get("unit")));
        vo.setManufacturer(str(drug.get("manufacturer")));
        vo.setApprovalNumber(str(drug.get("approval_number")));
        vo.setIsTraceRequired(toIntObj(drug.get("is_trace_required")));
        vo.setDrugStatus(toIntObj(drug.get("status")));
    }

    private DrugTraceScanVO.BatchOption toBatchOption(Map<String, Object> m) {
        DrugTraceScanVO.BatchOption opt = new DrugTraceScanVO.BatchOption();
        opt.setStockId(toLong(m.get("id")));
        opt.setBatchNo(str(m.get("batch_no")));
        Object expiry = m.get("expiry_date");
        if (expiry instanceof LocalDate ld) {
            opt.setExpiryDate(ld);
        } else if (expiry instanceof java.sql.Date sd) {
            opt.setExpiryDate(sd.toLocalDate());
        }
        opt.setQuantity(toDecimal(m.get("quantity")));
        opt.setAvailableQuantity(toDecimal(m.get("available_quantity")));
        opt.setStockRoom(toIntObj(m.get("stock_room")));
        opt.setLocation(str(m.get("location")));
        opt.setSupplier(str(m.get("supplier")));
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
        return "DR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }

    private String nextUploadBatchNo() {
        return "UP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%02d", ThreadLocalRandom.current().nextInt(100));
    }

    private Object firstValue(Map<String, Object> m) {
        return m == null ? null : m.get("cnt");
    }

    private static String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static Long toLong(Object v) {
        return v == null ? null : ((Number) v).longValue();
    }

    private static Long toLongObj(Object v) {
        return v == null ? 0L : ((Number) v).longValue();
    }

    private static int toInt(Object v) {
        return v == null ? 0 : ((Number) v).intValue();
    }

    private static Integer toIntObj(Object v) {
        return v == null ? null : ((Number) v).intValue();
    }

    private static BigDecimal toDecimal(Object v) {
        return v == null ? null : new BigDecimal(String.valueOf(v));
    }

    private static String cut(String v, int max) {
        if (v == null) {
            return null;
        }
        return v.length() <= max ? v : v.substring(0, max);
    }

    private static String blankToNull(String v) {
        return StringUtils.hasText(v) ? v.trim() : null;
    }
}
