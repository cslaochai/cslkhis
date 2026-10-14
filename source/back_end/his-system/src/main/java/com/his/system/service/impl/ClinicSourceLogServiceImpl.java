package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.ClinicSourceChangeTypeEnum;
import com.his.common.util.TextUtil;
import com.his.system.dto.ClinicSourceLogQueryDTO;
import com.his.system.entity.BizClinicSource;
import com.his.system.entity.BizClinicSourceLog;
import com.his.system.mapper.BizClinicSourceLogMapper;
import com.his.system.mapper.BizClinicSourceMapper;
import com.his.system.service.ClinicSourceLogService;
import com.his.system.vo.ClinicSourceLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 门诊排班变更留痕服务实现。
 */
@Service
@RequiredArgsConstructor
public class ClinicSourceLogServiceImpl extends ServiceImpl<BizClinicSourceLogMapper, BizClinicSourceLog>
        implements ClinicSourceLogService {

    /**
     * 留痕原因列宽：超长直接截断，避免「原始报错塞进原因列」把整笔业务顶成 500
     */
    private static final int W_REASON = 200;

    private final BizClinicSourceMapper clinicSourceMapper;

    @Override
    public Long record(Long scheduleId, ClinicSourceChangeTypeEnum type, Integer amount, String reason, BizClinicSource schedule) {
        BizClinicSourceLog log = new BizClinicSourceLog();
        log.setScheduleId(scheduleId);
        log.setActionType(type.getCode());
        log.setAmount(amount);
        if (reason != null) {
            log.setReason(reason.length() > W_REASON ? reason.substring(0, W_REASON) : reason);
        }
        log.setOccurTime(LocalDateTime.now());
        // 快照列冗余：删除/改期后排班行已不在，审计要能读出「谁、哪天、哪个科的什么动作」
        if (schedule != null) {
            log.setScheduleDate(schedule.getScheduleDate());
            log.setDeptName(schedule.getDeptName());
            log.setDoctorName(schedule.getDoctorName());
        }
        save(log);
        return log.getId();
    }

    @Override
    public IPage<ClinicSourceLogVO> pageByQuery(ClinicSourceLogQueryDTO queryDTO) {
        List<Long> scheduleIds = null;
        boolean tableFiltered = false;
        if (queryDTO.getDeptId() != null || TextUtil.hasText(queryDTO.getStartDate()) || TextUtil.hasText(queryDTO.getEndDate())) {
            // 科室/日期优先按排班表过滤（日志快照列是文本科室名/发生日期，排班表是当前权威）；
            // 排班表捞不到的删除类日志再按快照列日期兜底
            tableFiltered = true;
            LambdaQueryWrapper<BizClinicSource> scheduleWrapper = new LambdaQueryWrapper<BizClinicSource>()
                    .eq(queryDTO.getDeptId() != null, BizClinicSource::getDeptId, queryDTO.getDeptId())
                    .ge(TextUtil.hasText(queryDTO.getStartDate()), BizClinicSource::getScheduleDate, queryDTO.getStartDate())
                    .le(TextUtil.hasText(queryDTO.getEndDate()), BizClinicSource::getScheduleDate, queryDTO.getEndDate())
                    .select(BizClinicSource::getId);
            scheduleIds = clinicSourceMapper.selectList(scheduleWrapper).stream()
                    .map(BizClinicSource::getId).toList();
            if (queryDTO.getDeptId() != null && scheduleIds.isEmpty()) {
                // 筛了科室但排班表无该科排班：删除类日志的排班已物理删，按不到科室，直接空页
                // （若只筛日期，不能短路——删除类日志要按快照列日期兜住）
                return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize(), 0);
            }
        }

        // 快照列参与过滤：删除类日志排班已不在排班表，按快照列日期捞（列存的是发生时的日期）
        LambdaQueryWrapper<BizClinicSourceLog> wrapper = new LambdaQueryWrapper<BizClinicSourceLog>()
                .eq(queryDTO.getActionType() != null, BizClinicSourceLog::getActionType, queryDTO.getActionType())
                .in(tableFiltered && scheduleIds != null && !scheduleIds.isEmpty(), BizClinicSourceLog::getScheduleId, scheduleIds)
                .ge(TextUtil.hasText(queryDTO.getStartDate()), BizClinicSourceLog::getScheduleDate, queryDTO.getStartDate())
                .le(TextUtil.hasText(queryDTO.getEndDate()), BizClinicSourceLog::getScheduleDate, queryDTO.getEndDate())
                // 二级键 id：同一秒内连续两条变更的排序不稳定（翻页/追溯都会乱行）
                .orderByDesc(BizClinicSourceLog::getOccurTime).orderByDesc(BizClinicSourceLog::getId);
        IPage<BizClinicSourceLog> page = page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);

        // 快照列优先（含删除类日志），排班表兜底（兼容 262 前未冗余的行——此类行排班还在才可能命中）
        List<Long> ids = page.getRecords().stream().map(BizClinicSourceLog::getScheduleId).filter(java.util.Objects::nonNull).distinct().toList();
        Map<Long, BizClinicSource> schedules = ids.isEmpty() ? Map.of()
                : clinicSourceMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(BizClinicSource::getId, s -> s));

        List<ClinicSourceLogVO> vos = new ArrayList<>(page.getRecords().size());
        for (BizClinicSourceLog row : page.getRecords()) {
            ClinicSourceLogVO vo = new ClinicSourceLogVO();
            vo.setId(row.getId());
            vo.setScheduleId(row.getScheduleId());
            vo.setActionType(row.getActionType());
            vo.setActionTypeText(ClinicSourceChangeTypeEnum.getText(row.getActionType()));
            vo.setAmount(row.getAmount());
            vo.setReason(row.getReason());
            vo.setOccurTime(row.getOccurTime());
            vo.setCreateBy(row.getCreateBy());
            vo.setScheduleDate(row.getScheduleDate() == null ? null : row.getScheduleDate().toString());
            vo.setDeptName(row.getDeptName());
            vo.setDoctorName(row.getDoctorName());
            if (row.getDoctorName() == null) {
                BizClinicSource schedule = schedules.get(row.getScheduleId());
                if (schedule != null) {
                    vo.setScheduleDate(schedule.getScheduleDate() == null ? null : schedule.getScheduleDate().toString());
                    vo.setDeptName(schedule.getDeptName());
                    vo.setDoctorName(schedule.getDoctorName());
                }
            }
            vos.add(vo);
        }
        IPage<ClinicSourceLogVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(vos);
        return result;
    }
}
