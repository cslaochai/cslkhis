package com.his.patient.spi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.PageResult;
import com.his.common.enums.CheckupAbnormalFlagEnum;
import com.his.common.enums.CheckupStatusEnum;
import com.his.patient.service.CheckupService;
import com.his.patient.vo.CheckupVO;
import com.his.spi.service.CheckupFacadeService;
import com.his.spi.vo.PatientCheckupItemVO;
import com.his.spi.vo.PatientCheckupRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 体检报告患者查询端口实现：复用 CheckupService 的读取与状态收口，不碰登记/录入/总检。
 *
 * <p>依赖图已核对：CheckupServiceImpl 只依赖本模块 mapper 与 RedisSequenceService / DictCacheService，
 * 无跨模块编排依赖，不构成 Bean 环。</p>
 */
@Service
@RequiredArgsConstructor
public class CheckupFacadeServiceImpl implements CheckupFacadeService {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CheckupService checkupService;

    @Override
    public PageResult<PatientCheckupRecordVO> myRecords(Long patientId, int pageNum, int pageSize) {
        IPage<CheckupVO.RecordVO> page = checkupService.myRecords(patientId, pageNum, pageSize);
        List<PatientCheckupRecordVO> records = new ArrayList<>();
        for (CheckupVO.RecordVO vo : page.getRecords()) {
            records.add(toRecord(vo, false));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public PatientCheckupRecordVO recordDetail(Long recordId) {
        if (recordId == null) {
            return null;
        }
        CheckupVO.RecordVO vo = checkupService.getRecord(recordId);
        return vo == null ? null : toRecord(vo, true);
    }

    private PatientCheckupRecordVO toRecord(CheckupVO.RecordVO vo, boolean withItems) {
        PatientCheckupRecordVO result = new PatientCheckupRecordVO();
        result.setRecordId(vo.getId());
        result.setPatientId(vo.getPatientId());
        result.setRecordNo(vo.getRecordNo());
        result.setPackageName(vo.getPackageName());
        result.setCheckupDate(vo.getCheckupDate() == null ? null : vo.getCheckupDate().toString());
        result.setTotalAmount(vo.getTotalAmount());
        result.setRecordStatus(vo.getRecordStatus());
        result.setRecordStatusText(CheckupStatusEnum.getLabel(vo.getRecordStatus()));
        result.setConclusion(vo.getConclusion());
        result.setDoctorName(vo.getDoctorName());
        result.setReportTime(vo.getReportTime() == null ? null : TIME.format(vo.getReportTime()));
        if (withItems && vo.getResults() != null) {
            List<PatientCheckupItemVO> items = new ArrayList<>(vo.getResults().size());
            for (CheckupVO.ResultVO r : vo.getResults()) {
                PatientCheckupItemVO item = new PatientCheckupItemVO();
                item.setItemName(r.getItemName());
                item.setRefStandard(r.getRefStandard());
                item.setResultValue(r.getResultValue());
                item.setAbnormalFlag(r.getAbnormalFlag());
                item.setAbnormalText(CheckupAbnormalFlagEnum.getLabel(r.getAbnormalFlag()));
                item.setSummaryText(r.getSummaryText());
                items.add(item);
            }
            result.setItems(items);
        }
        return result;
    }
}
