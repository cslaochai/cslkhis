package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.emr.dto.ChronicCancelDTO;
import com.his.emr.dto.ChronicQueryPageDTO;
import com.his.emr.dto.ChronicUpsertDTO;
import com.his.emr.entity.BizChronicRecord;
import com.his.emr.enums.ChronicConfirmStatusEnum;
import com.his.emr.mapper.BizChronicRecordMapper;
import com.his.emr.service.ChronicRecordService;
import com.his.emr.vo.ChronicMyRecordsVO;
import com.his.emr.vo.ChronicRecordListVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 慢病建档/认定（M1，医生工作站）。
 *
 * <p>口径：
 * <ul>
 *   <li>建档即认定（确认状态=1），认定医生=当前登录医生；作废置 2（单向），可重新建档；</li>
 *   <li>同一患者同一慢病编码只允许一条有效档案；</li>
 *   <li>长处方开方资格 = 存在已认定的有效档案（开方侧校验，见 EmrServiceImpl）。</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ChronicRecordServiceImpl extends ServiceImpl<BizChronicRecordMapper, BizChronicRecord> implements ChronicRecordService {

    private final BizChronicRecordMapper bizChronicRecordMapper;
    private final RedisSequenceService redisSequenceService;

    @Override
    public ChronicRecordListVO upsert(ChronicUpsertDTO dto) {
        var current = UserUtils.getCurrentUser();
        // 同患者同慢病唯一有效档案
        Long exist = bizChronicRecordMapper.selectCount(new LambdaQueryWrapper<BizChronicRecord>()
                .eq(BizChronicRecord::getPatientId, dto.getPatientId())
                .eq(BizChronicRecord::getDiseaseCode, dto.getDiseaseCode())
                .eq(BizChronicRecord::getConfirmStatus, 1));
        if (exist != null && exist > 0) {
            throw new BusinessException("该患者已存在「" + dto.getDiseaseName() + "」的有效慢病档案，不可重复建档");
        }
        BizChronicRecord record = new BizChronicRecord();
        record.setRecordNo(redisSequenceService.generateChronicRecordNo());
        record.setPatientId(dto.getPatientId());
        record.setPatientNo(dto.getPatientNo());
        record.setPatientName(dto.getPatientName());
        record.setDiseaseCode(dto.getDiseaseCode());
        record.setDiseaseName(dto.getDiseaseName());
        record.setDoctorId(current.getEmployeeId() == null ? current.getUserId() : current.getEmployeeId());
        record.setDoctorName(current.getRealName());
        record.setDeptId(current.getDeptId());
        record.setDeptName(current.getDeptName());
        record.setConfirmStatus(ChronicConfirmStatusEnum.CONFIRMED.getCode());
        record.setConfirmTime(LocalDateTime.now());
        record.setRemark(dto.getRemark());
        bizChronicRecordMapper.insert(record);
        return toVO(record);
    }

    @Override
    public void cancel(ChronicCancelDTO dto) {
        BizChronicRecord record = bizChronicRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException("慢病档案不存在");
        }
        if (record.getConfirmStatus() != 1) {
            throw new BusinessException("仅「已认定」的慢病档案可作废");
        }
        record.setConfirmStatus(ChronicConfirmStatusEnum.CANCELLED.getCode());
        record.setConfirmTime(LocalDateTime.now());
        bizChronicRecordMapper.updateById(record);
    }

    @Override
    public PageResult<ChronicRecordListVO> listPage(ChronicQueryPageDTO dto) {
        LambdaQueryWrapper<BizChronicRecord> wrapper = new LambdaQueryWrapper<>();
        if (dto.getPatientId() != null) {
            wrapper.eq(BizChronicRecord::getPatientId, dto.getPatientId());
        }
        if (TextUtil.hasText(dto.getPatientName())) {
            wrapper.like(BizChronicRecord::getPatientName, dto.getPatientName().trim());
        }
        if (TextUtil.hasText(dto.getDiseaseKeyword())) {
            String kw = dto.getDiseaseKeyword().trim();
            wrapper.and(w -> w.like(BizChronicRecord::getDiseaseName, kw)
                    .or().like(BizChronicRecord::getDiseaseCode, kw));
        }
        if (TextUtil.hasText(dto.getRecordNo())) {
            wrapper.like(BizChronicRecord::getRecordNo, dto.getRecordNo().trim());
        }
        if (dto.getConfirmStatus() != null) {
            wrapper.eq(BizChronicRecord::getConfirmStatus, dto.getConfirmStatus());
        }
        wrapper.orderByDesc(BizChronicRecord::getId);
        Page<BizChronicRecord> page = bizChronicRecordMapper.selectPage(
                Page.of(dto.getPageNum(), dto.getPageSize()), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(),
                page.getSize(), page.getPages(), toVOList(page.getRecords()));
    }

    @Override
    public List<ChronicRecordListVO> activeList(Long patientId) {
        return toVOList(bizChronicRecordMapper.selectList(new LambdaQueryWrapper<BizChronicRecord>()
                .eq(BizChronicRecord::getPatientId, patientId)
                .eq(BizChronicRecord::getConfirmStatus, 1)
                .orderByDesc(BizChronicRecord::getId)));
    }

    @Override
    public List<ChronicRecordListVO> listByPatient(Long patientId) {
        // 收口：拿不到 patientId 就返回空，绝不退化成「不过滤=查全院」
        if (patientId == null) {
            return List.of();
        }
        List<BizChronicRecord> rows = bizChronicRecordMapper.selectList(new LambdaQueryWrapper<BizChronicRecord>()
                .eq(BizChronicRecord::getPatientId, patientId)
                .orderByDesc(BizChronicRecord::getId));
        return toVOList(rows);
    }

    private List<ChronicRecordListVO> toVOList(List<BizChronicRecord> rows) {
        List<ChronicRecordListVO> vos = new ArrayList<>(rows.size());
        for (BizChronicRecord row : rows) {
            vos.add(toVO(row));
        }
        return vos;
    }

    private ChronicRecordListVO toVO(BizChronicRecord row) {
        ChronicRecordListVO vo = new ChronicRecordListVO();
        BeanUtils.copyProperties(row, vo);
        return vo;
    }

    @Override
    public ChronicMyRecordsVO myRecords() {
        List<ChronicRecordListVO> records = listByPatient(UserUtils.getCurrentUser().getPatientId());
        ChronicMyRecordsVO vo = new ChronicMyRecordsVO();
        vo.setRecords(records);
        vo.setLongRxEligible(records.stream().anyMatch(r -> Integer.valueOf(1).equals(r.getConfirmStatus())));
        return vo;
    }
}
