package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.NurseScheduleChangeTypeEnum;
import com.his.common.util.TextUtil;
import com.his.system.dto.NurseScheduleLogQueryDTO;
import com.his.system.entity.BizSchedule;
import com.his.system.entity.BizNurseScheduleLog;
import com.his.system.mapper.BizNurseScheduleLogMapper;
import com.his.system.mapper.BizScheduleMapper;
import com.his.system.service.NurseScheduleLogService;
import com.his.system.vo.NurseScheduleLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 护理排班审批与变更留痕服务实现（280）。
 */
@Service
@RequiredArgsConstructor
public class NurseScheduleLogServiceImpl extends ServiceImpl<BizNurseScheduleLogMapper, BizNurseScheduleLog>
        implements NurseScheduleLogService {

    /** 留痕原因列宽：超长截断，避免原始报错塞进原因列把整笔业务顶成 500 */
    private static final int W_REASON = 200;

    private final BizScheduleMapper bizScheduleMapper;

    @Override
    public Long record(Long scheduleId, NurseScheduleChangeTypeEnum type, Integer amount, String reason,
                       BizSchedule row, String fromSnapshot, String toSnapshot) {
        BizNurseScheduleLog log = new BizNurseScheduleLog();
        log.setScheduleId(scheduleId);
        log.setActionType(type == null ? null : type.getCode());
        log.setAmount(amount);
        if (reason != null) {
            log.setReason(reason.length() > W_REASON ? reason.substring(0, W_REASON) : reason);
        }
        log.setOccurTime(LocalDateTime.now());
        log.setFromSnapshot(fromSnapshot);
        log.setToSnapshot(toSnapshot);
        // 冗余快照：排班被删/改后审计仍能读出「谁、哪天、哪个单元、哪位护士」
        if (row != null) {
            log.setScheduleDate(row.getScheduleDate());
            log.setOrgName(row.getOrgName());
            log.setDeptName(row.getDeptName());
            log.setNurseName(row.getEmployeeName());
        }
        save(log);
        return log.getId();
    }

    @Override
    public IPage<NurseScheduleLogVO> pageByQuery(NurseScheduleLogQueryDTO queryDTO) {
        List<Long> scheduleIds = null;
        if (queryDTO.getDeptId() != null) {
            // 科室先按排班表过滤；删除类留痕的排班已物理删，按不到科室就空页
            LambdaQueryWrapper<BizSchedule> sw = new LambdaQueryWrapper<BizSchedule>()
                    .eq(BizSchedule::getDeptId, queryDTO.getDeptId())
                    .select(BizSchedule::getId);
            scheduleIds = bizScheduleMapper.selectList(sw).stream().map(BizSchedule::getId).toList();
            if (scheduleIds.isEmpty()) {
                return new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize(), 0);
            }
        }

        LambdaQueryWrapper<BizNurseScheduleLog> wrapper = new LambdaQueryWrapper<BizNurseScheduleLog>()
                .eq(queryDTO.getActionType() != null, BizNurseScheduleLog::getActionType, queryDTO.getActionType())
                .in(scheduleIds != null && !scheduleIds.isEmpty(), BizNurseScheduleLog::getScheduleId, scheduleIds)
                .ge(TextUtil.hasText(queryDTO.getStartDate()), BizNurseScheduleLog::getScheduleDate, queryDTO.getStartDate())
                .le(TextUtil.hasText(queryDTO.getEndDate()), BizNurseScheduleLog::getScheduleDate, queryDTO.getEndDate())
                .like(TextUtil.hasText(queryDTO.getNurseName()), BizNurseScheduleLog::getNurseName, queryDTO.getNurseName())
                // 二级键 id：同一秒内连续两条留痕排序不稳定
                .orderByDesc(BizNurseScheduleLog::getOccurTime).orderByDesc(BizNurseScheduleLog::getId);
        IPage<BizNurseScheduleLog> page = page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);

        List<NurseScheduleLogVO> vos = new ArrayList<>(page.getRecords().size());
        for (BizNurseScheduleLog row : page.getRecords()) {
            NurseScheduleLogVO vo = new NurseScheduleLogVO();
            vo.setId(row.getId());
            vo.setScheduleId(row.getScheduleId());
            vo.setActionType(row.getActionType());
            vo.setActionTypeText(NurseScheduleChangeTypeEnum.getLabel(row.getActionType()));
            vo.setAmount(row.getAmount());
            vo.setReason(row.getReason());
            vo.setOccurTime(row.getOccurTime());
            vo.setCreateBy(row.getCreateBy());
            vo.setScheduleDate(row.getScheduleDate() == null ? null : row.getScheduleDate().toString());
            vo.setOrgName(row.getOrgName());
            vo.setDeptName(row.getDeptName());
            vo.setNurseName(row.getNurseName());
            vo.setFromSnapshot(row.getFromSnapshot());
            vo.setToSnapshot(row.getToSnapshot());
            vos.add(vo);
        }
        IPage<NurseScheduleLogVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(vos);
        return result;
    }
}
