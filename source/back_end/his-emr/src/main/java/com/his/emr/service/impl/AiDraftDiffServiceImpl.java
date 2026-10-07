package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.emr.dto.AiDraftDiffQueryPageDTO;
import com.his.emr.entity.BizAiDraftDiff;
import com.his.emr.mapper.BizAiDraftDiffMapper;
import com.his.emr.service.AiDraftDiffService;
import com.his.emr.support.DraftDiffSupport;
import com.his.emr.vo.AiDraftDiffListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiDraftDiffServiceImpl extends ServiceImpl<BizAiDraftDiffMapper, BizAiDraftDiff> implements AiDraftDiffService {

    private final BizAiDraftDiffMapper bizAiDraftDiffMapper;

    private final DraftDiffSupport draftDiffSupport;

    @Override
    public boolean record(Long recordId, Long registId, Long patientId, String patientNo, String patientName,
                          Long deptId, String deptName, Long doctorId, String doctorName,
                          String draftText, String finalText) {
        if (!StringUtils.hasText(draftText) || !StringUtils.hasText(finalText)) {
            return false;
        }
        List<DraftDiffSupport.Segment> segments = draftDiffSupport.diff(draftText, finalText);
        boolean changed = segments.stream().anyMatch(s -> s.getType() != 0);

        BizAiDraftDiff entity = new BizAiDraftDiff();
        entity.setRecordId(recordId);
        entity.setRegistId(registId);
        entity.setPatientId(patientId);
        entity.setPatientNo(patientNo);
        entity.setPatientName(patientName);
        entity.setDeptId(deptId);
        entity.setDeptName(deptName);
        entity.setDoctorId(doctorId);
        entity.setDoctorName(doctorName);
        entity.setDraftText(draftText.trim());
        entity.setFinalText(finalText.trim());
        entity.setDiffJson(draftDiffSupport.toJson(segments));
        entity.setChanged(changed ? 1 : 0);
        bizAiDraftDiffMapper.insert(entity);
        return true;
    }

    @Override
    public PageResult<AiDraftDiffListVO> listPage(AiDraftDiffQueryPageDTO dto) {
        AiDraftDiffQueryPageDTO q = dto == null ? new AiDraftDiffQueryPageDTO() : dto;
        LambdaQueryWrapper<BizAiDraftDiff> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(q.getPatientName()), BizAiDraftDiff::getPatientName,
                        q.getPatientName() == null ? null : q.getPatientName().trim())
                .like(StringUtils.hasText(q.getDoctorName()), BizAiDraftDiff::getDoctorName,
                        q.getDoctorName() == null ? null : q.getDoctorName().trim())
                .eq(q.getChanged() != null, BizAiDraftDiff::getChanged, q.getChanged())
                .orderByDesc(BizAiDraftDiff::getCreateTime)
                .orderByDesc(BizAiDraftDiff::getId);
        Page<BizAiDraftDiff> page = bizAiDraftDiffMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), wrapper);
        List<AiDraftDiffListVO> voList = page.getRecords().stream()
                .map(entity -> {
                    AiDraftDiffListVO vo = new AiDraftDiffListVO();
                    BeanUtils.copyProperties(entity, vo);
                    return vo;
                }).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }
}
