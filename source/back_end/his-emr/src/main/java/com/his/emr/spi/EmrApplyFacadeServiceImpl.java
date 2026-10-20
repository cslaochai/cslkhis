package com.his.emr.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.util.TextUtil;
import com.his.emr.entity.BizInspectionApply;
import com.his.emr.entity.BizLaboratoryApply;
import com.his.emr.mapper.BizInspectionApplyMapper;
import com.his.emr.mapper.BizLaboratoryApplyMapper;
import com.his.spi.dto.InspectionApplyPageQueryDTO;
import com.his.spi.service.EmrApplyFacadeService;
import com.his.spi.vo.InspectionApplyBriefVO;
import com.his.spi.vo.LaboratoryApplyBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 申请单事实端口实现。
 */
@Service
@RequiredArgsConstructor
public class EmrApplyFacadeServiceImpl implements EmrApplyFacadeService {

    private final BizInspectionApplyMapper bizInspectionApplyMapper;
    private final BizLaboratoryApplyMapper bizLaboratoryApplyMapper;

    @Override
    public InspectionApplyBriefVO getInspectionApplyById(Long applyId) {
        return applyId == null ? null : toInspectionVo(bizInspectionApplyMapper.selectById(applyId));
    }

    @Override
    public LaboratoryApplyBriefVO getLaboratoryApplyById(Long applyId) {
        return applyId == null ? null : toLaboratoryVo(bizLaboratoryApplyMapper.selectById(applyId));
    }

    @Override
    public PageResult<InspectionApplyBriefVO> pageInspectionApply(InspectionApplyPageQueryDTO query) {
        Page<BizInspectionApply> page = bizInspectionApplyMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), inspectionWrapper(query));
        List<InspectionApplyBriefVO> records = new ArrayList<>();
        for (BizInspectionApply apply : page.getRecords()) {
            records.add(toInspectionVo(apply));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public long countInspectionApply(InspectionApplyPageQueryDTO query) {
        // 计数只取筛选条件：排序列喂给 count 不影响结果，但会撞 ONLY_FULL_GROUP_BY 之外的方言差异，
        // 因此这里重新组一份不带 orderBy 的同类条件，而不是复用 wrapper 再 orderBy 后计数。
        LambdaQueryWrapper<BizInspectionApply> w = new LambdaQueryWrapper<>();
        applyConditions(w, query);
        return bizInspectionApplyMapper.selectCount(w);
    }

    private LambdaQueryWrapper<BizInspectionApply> inspectionWrapper(InspectionApplyPageQueryDTO query) {
        LambdaQueryWrapper<BizInspectionApply> w = new LambdaQueryWrapper<>();
        applyConditions(w, query);
        return w.orderByDesc(BizInspectionApply::getIsEmergency).orderByAsc(BizInspectionApply::getId);
    }

    private void applyConditions(LambdaQueryWrapper<BizInspectionApply> w, InspectionApplyPageQueryDTO query) {
        List<Integer> statusIn = query.getApplyStatusIn();
        List<Long> excludeIds = query.getExcludeApplyIds();
        String kw = TextUtil.trim(query.getKeyword());
        w.in(statusIn != null && !statusIn.isEmpty(), BizInspectionApply::getApplyStatus, statusIn)
                .notIn(excludeIds != null && !excludeIds.isEmpty(), BizInspectionApply::getId, excludeIds)
                .and(TextUtil.hasText(kw), x -> x.like(BizInspectionApply::getApplyNo, kw)
                        .or().like(BizInspectionApply::getPatientName, kw)
                        .or().like(BizInspectionApply::getInspectionItemName, kw))
                .eq(query.getPatientId() != null, BizInspectionApply::getPatientId, query.getPatientId())
                .eq(query.getIsEmergency() != null, BizInspectionApply::getIsEmergency, query.getIsEmergency())
                .ge(query.getStartDate() != null, BizInspectionApply::getVisitDate, query.getStartDate())
                .le(query.getEndDate() != null, BizInspectionApply::getVisitDate, query.getEndDate());
    }

    private InspectionApplyBriefVO toInspectionVo(BizInspectionApply apply) {
        if (apply == null) {
            return null;
        }
        InspectionApplyBriefVO vo = new InspectionApplyBriefVO();
        vo.setId(apply.getId());
        vo.setApplyNo(apply.getApplyNo());
        vo.setPatientId(apply.getPatientId());
        vo.setPatientNo(apply.getPatientNo());
        vo.setPatientName(apply.getPatientName());
        vo.setGender(apply.getGender());
        vo.setAge(apply.getAge());
        vo.setRegistId(apply.getRegistId());
        vo.setVisitDate(apply.getVisitDate());
        vo.setDeptId(apply.getDeptId());
        vo.setDeptName(apply.getDeptName());
        vo.setDoctorId(apply.getDoctorId());
        vo.setDoctorName(apply.getDoctorName());
        vo.setInspectionItemId(apply.getInspectionItemId());
        vo.setInspectionItemCode(apply.getInspectionItemCode());
        vo.setInspectionItemName(apply.getInspectionItemName());
        vo.setInspectionDeptId(apply.getInspectionDeptId());
        vo.setInspectionDeptName(apply.getInspectionDeptName());
        vo.setBodyPart(apply.getBodyPart());
        vo.setInspectionPurpose(apply.getInspectionPurpose());
        vo.setClinicalDiagnosis(apply.getClinicalDiagnosis());
        vo.setPrice(apply.getPrice());
        vo.setIsEmergency(apply.getIsEmergency());
        vo.setApplyStatus(apply.getApplyStatus());
        return vo;
    }

    private LaboratoryApplyBriefVO toLaboratoryVo(BizLaboratoryApply apply) {
        if (apply == null) {
            return null;
        }
        LaboratoryApplyBriefVO vo = new LaboratoryApplyBriefVO();
        vo.setId(apply.getId());
        vo.setApplyNo(apply.getApplyNo());
        vo.setPatientId(apply.getPatientId());
        vo.setPatientNo(apply.getPatientNo());
        vo.setPatientName(apply.getPatientName());
        vo.setGender(apply.getGender());
        vo.setAge(apply.getAge());
        vo.setVisitDate(apply.getVisitDate());
        vo.setDeptId(apply.getDeptId());
        vo.setDeptName(apply.getDeptName());
        vo.setDoctorId(apply.getDoctorId());
        vo.setDoctorName(apply.getDoctorName());
        vo.setLaboratoryItemId(apply.getLaboratoryItemId());
        vo.setLaboratoryItemCode(apply.getLaboratoryItemCode());
        vo.setLaboratoryItemName(apply.getLaboratoryItemName());
        vo.setLaboratoryDeptId(apply.getLaboratoryDeptId());
        vo.setLaboratoryDeptName(apply.getLaboratoryDeptName());
        vo.setSpecimenType(apply.getSpecimenType());
        vo.setPrice(apply.getPrice());
        return vo;
    }
}
