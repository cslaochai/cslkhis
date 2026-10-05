package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.exception.BusinessException;
import com.his.patient.dto.PatientTagBatchUpsertDTO;
import com.his.patient.dto.PatientTagDelDTO;
import com.his.patient.dto.PatientTagUpsertDTO;
import com.his.patient.entity.BizPatientTagRelation;
import com.his.patient.mapper.BizPatientTagRelationMapper;
import com.his.patient.service.BizPatientTagRelationService;
import com.his.system.entity.SysPatientTag;
import com.his.system.service.PatientTagService;
import com.his.system.vo.SysPatientTagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BizPatientTagRelationServiceImpl
        extends ServiceImpl<BizPatientTagRelationMapper, BizPatientTagRelation>
        implements BizPatientTagRelationService {

    private final PatientTagService patientTagService;

    @Override
    public List<Long> listPatientIdsByTagId(Long tagId) {
        LambdaQueryWrapper<BizPatientTagRelation> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(BizPatientTagRelation::getTagId, tagId);
        return this.list(tagWrapper).stream()
                .map(BizPatientTagRelation::getPatientId)
                .collect(Collectors.toList());
    }

    @Override
    public List<SysPatientTagVO> listTagsByPatientId(Long patientId) {
        LambdaQueryWrapper<BizPatientTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizPatientTagRelation::getPatientId, patientId);
        List<BizPatientTagRelation> relations = this.list(wrapper);
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> tagIds = relations.stream()
                .map(BizPatientTagRelation::getTagId)
                .collect(Collectors.toList());
        LambdaQueryWrapper<SysPatientTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.in(SysPatientTag::getTagId, tagIds);
        List<SysPatientTag> tags = patientTagService.list(tagWrapper);
        return tags.stream().map(tag -> {
            SysPatientTagVO vo = new SysPatientTagVO();
            BeanUtils.copyProperties(tag, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<Long, List<SysPatientTagVO>> mapTagsByPatientIds(List<Long> patientIds) {
        if (patientIds == null || patientIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<BizPatientTagRelation> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.in(BizPatientTagRelation::getPatientId, patientIds);
        List<BizPatientTagRelation> relations = this.list(relWrapper);
        List<Long> tagIds = relations.stream()
                .map(BizPatientTagRelation::getTagId)
                .distinct()
                .collect(Collectors.toList());
        if (tagIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SysPatientTag> tags = patientTagService.listByIds(tagIds);
        Map<Long, SysPatientTagVO> tagVoMap = tags.stream().map(tag -> {
            SysPatientTagVO tvo = new SysPatientTagVO();
            BeanUtils.copyProperties(tag, tvo);
            return tvo;
        }).collect(Collectors.toMap(SysPatientTagVO::getTagId, t -> t, (a, b) -> a));

        Map<Long, List<SysPatientTagVO>> tagsMap = new HashMap<>();
        for (BizPatientTagRelation relation : relations) {
            SysPatientTagVO tvo = tagVoMap.get(relation.getTagId());
            if (tvo != null) {
                tagsMap.computeIfAbsent(relation.getPatientId(), k -> new ArrayList<>()).add(tvo);
            }
        }
        return tagsMap;
    }

    @Override
    public void addTag(PatientTagUpsertDTO tagDTO) {
        LambdaQueryWrapper<BizPatientTagRelation> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(BizPatientTagRelation::getPatientId, tagDTO.getPatientId())
                .eq(BizPatientTagRelation::getTagId, tagDTO.getTagId());
        if (this.count(checkWrapper) > 0) {
            throw new BusinessException("该标签已存在");
        }
        BizPatientTagRelation relation = new BizPatientTagRelation();
        relation.setPatientId(tagDTO.getPatientId());
        relation.setTagId(tagDTO.getTagId());
        relation.setSourceType(tagDTO.getSourceType() != null ? tagDTO.getSourceType() : 1);
        this.save(relation);
    }

    @Override
    public void deleteTag(PatientTagDelDTO delDTO) {
        LambdaQueryWrapper<BizPatientTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizPatientTagRelation::getPatientId, delDTO.getPatientId())
                .eq(BizPatientTagRelation::getTagId, delDTO.getTagId());
        this.remove(wrapper);
    }

    @Override
    public void batchAdd(PatientTagBatchUpsertDTO batchDTO) {
        for (PatientTagUpsertDTO tagDTO : batchDTO.getTagDTOs()) {
            LambdaQueryWrapper<BizPatientTagRelation> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(BizPatientTagRelation::getPatientId, batchDTO.getPatientId())
                    .eq(BizPatientTagRelation::getTagId, tagDTO.getTagId());
            if (this.count(checkWrapper) == 0) {
                BizPatientTagRelation relation = new BizPatientTagRelation();
                relation.setPatientId(batchDTO.getPatientId());
                relation.setTagId(tagDTO.getTagId());
                relation.setSourceType(tagDTO.getSourceType() != null ? tagDTO.getSourceType() : 2);
                this.save(relation);
            }
        }
    }
}
