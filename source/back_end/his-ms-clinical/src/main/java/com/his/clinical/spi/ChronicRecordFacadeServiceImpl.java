package com.his.clinical.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.clinical.entity.BizChronicRecord;
import com.his.clinical.mapper.BizChronicRecordMapper;
import com.his.common.enums.ChronicConfirmStatusEnum;
import com.his.spi.service.ChronicRecordFacadeService;
import com.his.spi.vo.ChronicRecordListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ChronicRecordFacadeService 的病历域实现（叶子端口：只依赖本模块 mapper）。
 */
@Service
@RequiredArgsConstructor
public class ChronicRecordFacadeServiceImpl implements ChronicRecordFacadeService {

    private final BizChronicRecordMapper bizChronicRecordMapper;

    @Override
    public List<ChronicRecordListVO> activeList(Long patientId) {
        if (patientId == null) {
            return Collections.emptyList();
        }
        return bizChronicRecordMapper.selectList(new LambdaQueryWrapper<BizChronicRecord>()
                        .eq(BizChronicRecord::getPatientId, patientId)
                        .eq(BizChronicRecord::getConfirmStatus, ChronicConfirmStatusEnum.CONFIRMED.getCode())
                        .orderByDesc(BizChronicRecord::getId)).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private ChronicRecordListVO toVO(BizChronicRecord row) {
        ChronicRecordListVO vo = new ChronicRecordListVO();
        vo.setId(row.getId());
        vo.setRecordNo(row.getRecordNo());
        vo.setPatientId(row.getPatientId());
        vo.setPatientNo(row.getPatientNo());
        vo.setPatientName(row.getPatientName());
        vo.setDiseaseCode(row.getDiseaseCode());
        vo.setDiseaseName(row.getDiseaseName());
        vo.setDoctorId(row.getDoctorId());
        vo.setDoctorName(row.getDoctorName());
        vo.setDeptId(row.getDeptId());
        vo.setDeptName(row.getDeptName());
        vo.setConfirmStatus(row.getConfirmStatus());
        vo.setConfirmTime(row.getConfirmTime());
        vo.setRemark(row.getRemark());
        vo.setCreateTime(row.getCreateTime());
        return vo;
    }
}
