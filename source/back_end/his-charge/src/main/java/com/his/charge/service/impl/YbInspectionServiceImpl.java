package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.charge.dto.YbCancelDTO;
import com.his.charge.dto.YbInspectConcludeDTO;
import com.his.charge.dto.YbInspectionQueryPageDTO;
import com.his.charge.dto.YbInspectionUpsertDTO;
import com.his.charge.entity.BizYbDeductNotice;
import com.his.charge.entity.BizYbInspection;
import com.his.charge.mapper.BizYbDeductNoticeMapper;
import com.his.charge.mapper.BizYbInspectionMapper;
import com.his.charge.service.YbInspectionService;
import com.his.charge.vo.YbInspectionDeductCountVO;
import com.his.charge.vo.YbInspectionListVO;
import com.his.common.base.PageResult;
import com.his.common.base.RedisSequenceService;
import com.his.common.exception.BusinessException;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 飞检批次服务实现。
 *
 * <p>批次是「问题从哪来」的唯一抓手：扣款通知挂批次号，飞检结束后能反查这个批次一共扣了多少、
 * 处理到哪一步。作废闸门（名下有扣款通知就不许作废）就是为了让问题不会蒸发。
 */
@Service
@RequiredArgsConstructor
public class YbInspectionServiceImpl implements YbInspectionService {

    /**
     * 状态：1-进行中 2-已结项 3-已作废
     */
    private static final int STATUS_RUNNING = 1;
    private static final int STATUS_CONCLUDED = 2;
    private static final int STATUS_CANCELLED = 3;

    private final BizYbInspectionMapper inspectionMapper;
    private final BizYbDeductNoticeMapper deductNoticeMapper;
    private final RedisSequenceService sequenceService;

    @Override
    public PageResult<YbInspectionListVO> listPage(YbInspectionQueryPageDTO queryDTO) {
        LambdaQueryWrapper<BizYbInspection> wrapper = new LambdaQueryWrapper<BizYbInspection>()
                .eq(queryDTO.getInspectType() != null, BizYbInspection::getInspectType, queryDTO.getInspectType())
                .eq(queryDTO.getStatus() != null, BizYbInspection::getStatus, queryDTO.getStatus())
                .and(isText(queryDTO.getKeyword()), w -> w
                        .like(BizYbInspection::getInspectNo, queryDTO.getKeyword())
                        .or().like(BizYbInspection::getFundOrg, queryDTO.getKeyword())
                        .or().like(BizYbInspection::getInspectTeam, queryDTO.getKeyword())
                        .or().like(BizYbInspection::getOurReceiver, queryDTO.getKeyword()))
                .orderByAsc(BizYbInspection::getStatus)
                .orderByDesc(BizYbInspection::getInspectDate);
        IPage<BizYbInspection> page = inspectionMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<YbInspectionListVO> voList = page.getRecords().stream().map(this::toVO).toList();
        fillDeductCount(voList);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public YbInspectionListVO getById(Long id) {
        BizYbInspection entity = require(id);
        YbInspectionListVO vo = toVO(entity);
        fillDeductCount(List.of(vo));
        return vo;
    }

    @Override
    public List<YbInspectionListVO> selectRunningList() {
        return inspectionMapper.selectList(new LambdaQueryWrapper<BizYbInspection>()
                        .eq(BizYbInspection::getStatus, STATUS_RUNNING)
                        .orderByDesc(BizYbInspection::getInspectDate))
                .stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public YbInspectionListVO upsert(YbInspectionUpsertDTO dto) {
        if (dto.getInspectEndDate().isBefore(dto.getInspectStartDate())) {
            throw new BusinessException("审核目标期间止不能早于起");
        }
        BizYbInspection entity;
        boolean creating = dto.getId() == null;
        if (creating) {
            entity = new BizYbInspection();
            entity.setInspectNo(sequenceService.generateYbInspectNo());
            entity.setStatus(STATUS_RUNNING);
        } else {
            entity = require(dto.getId());
            // 已结项的批次是留档事实，已作废的是废单，都不再接受内容修改
            if (!Integer.valueOf(STATUS_RUNNING).equals(entity.getStatus())) {
                throw new BusinessException("仅「进行中」的批次可修改，已结项/已作废的单据内容不可再改");
            }
        }
        entity.setInspectType(dto.getInspectType());
        entity.setFundOrg(cut(dto.getFundOrg(), 100));
        entity.setInspectStartDate(dto.getInspectStartDate());
        entity.setInspectEndDate(dto.getInspectEndDate());
        entity.setInspectDate(dto.getInspectDate());
        entity.setInspectTeam(cut(dto.getInspectTeam(), 200));
        entity.setOurReceiver(cut(dto.getOurReceiver(), 64));
        entity.setRemark(cut(dto.getRemark(), 500));
        entity.setUpdateBy(UserUtils.getCurrentEmployeeName());
        if (creating) {
            inspectionMapper.insert(entity);
        } else {
            inspectionMapper.updateById(entity);
        }
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void conclude(YbInspectConcludeDTO dto) {
        BizYbInspection entity = require(dto.getId());
        if (!Integer.valueOf(STATUS_RUNNING).equals(entity.getStatus())) {
            throw new BusinessException("仅「进行中」的批次可结项");
        }
        entity.setStatus(STATUS_CONCLUDED);
        entity.setConclusion(cut(dto.getConclusion(), 1000));
        entity.setConcludeTime(LocalDateTime.now());
        entity.setConcludeBy(UserUtils.getCurrentEmployeeName());
        entity.setUpdateBy(UserUtils.getCurrentEmployeeName());
        inspectionMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(YbCancelDTO dto) {
        BizYbInspection entity = require(dto.getId());
        if (!Integer.valueOf(STATUS_RUNNING).equals(entity.getStatus())) {
            throw new BusinessException("仅「进行中」的批次可作废");
        }
        Long related = deductNoticeMapper.selectCount(new LambdaQueryWrapper<BizYbDeductNotice>()
                .eq(BizYbDeductNotice::getInspectionId, entity.getId()));
        if (related != null && related > 0) {
            throw new BusinessException("该批次名下已有 " + related + " 张扣款通知，问题已入账不能作废，请先处理扣款单");
        }
        entity.setStatus(STATUS_CANCELLED);
        entity.setCancelReason(cut(dto.getReason(), 500));
        entity.setCancelBy(UserUtils.getCurrentEmployeeName());
        entity.setCancelTime(LocalDateTime.now());
        entity.setUpdateBy(UserUtils.getCurrentEmployeeName());
        inspectionMapper.updateById(entity);
    }

    private BizYbInspection require(Long id) {
        BizYbInspection entity = id == null ? null : inspectionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("飞检批次不存在或已删除");
        }
        return entity;
    }

    /**
     * 名下扣款单数与金额：一张聚合 SQL 捞回本页批次，避免逐行 count 打爆连接
     */
    private void fillDeductCount(List<YbInspectionListVO> voList) {
        if (voList.isEmpty()) {
            return;
        }
        List<Long> ids = voList.stream().map(YbInspectionListVO::getId).toList();
        Map<Long, YbInspectionDeductCountVO> counted = deductNoticeMapper.countByInspectionIds(ids).stream()
                .collect(Collectors.toMap(YbInspectionDeductCountVO::getInspectionId, Function.identity(), (a, b) -> a));
        for (YbInspectionListVO vo : voList) {
            YbInspectionDeductCountVO hit = counted.get(vo.getId());
            vo.setDeductCount(hit == null ? 0 : hit.getDeductCount());
            vo.setDeductAmountSum(hit == null || hit.getDeductAmountSum() == null
                    ? BigDecimal.ZERO : hit.getDeductAmountSum());
        }
    }

    private YbInspectionListVO toVO(BizYbInspection entity) {
        YbInspectionListVO vo = new YbInspectionListVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private boolean isText(String text) {
        return text != null && !text.isBlank();
    }

    /**
     * 写库文本先截列宽：超长会把「保存失败」升级成 500，用户连原因都看不到
     */
    private String cut(String text, int max) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }
}
